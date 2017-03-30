package org.heinzelotto.togglplot

import java.util.{Calendar, Date, Locale}
import javafx.scene.chart.XYChart.Series
import scalafx.application.JFXApp

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success}
import scala.util.Sorting
import scalafx.collections.ObservableBuffer
import scalafx.application.Platform
import scalafx.scene.chart._
import scalafx.Includes.handle
import scalafx.event._
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import scalafx.scene.control.Tooltip

import scalafx.beans.property.ObjectProperty
import scalafx.geometry.Point2D
import scalafx.scene.layout.StackPane
import scalafx.scene.layout.Region
import scalafx.scene.control.Label

class TogglProductivityGraph(val apiToken: String) {
  // our toggl/local storage abstraction
  val pdm = new ProductivityDataManager(apiToken)

  // UI
  private val evh1 = new EventHandler[ActionEvent] {
    override def handle(event: ActionEvent): Unit = {
      fillChartAsync()
    }
  }

  def timeStringFromEff(e: Double): String = {
    val hours = e*24
    hours.floor.toLong + "h" + ((hours%1)*60).toLong + "m"
  }

  val gui = new Gui(evh1)

  // fill chart with data (handle)
  def fillChartAsync(): Unit = {
    val futureChartData = queryChartData(185) // as future
    futureChartData onComplete {
      case Success(obsBufferAndQuantile) =>
        // code that updates the ui has to be called from the scalafx application thread, so we do this
        scalafx.application.Platform.runLater {
          gui.lineChart.data = obsBufferAndQuantile._1

          obsBufferAndQuantile._2 match {
            case x: Some[(Double, Double)] =>
              val eff = x.get._1
              val quantile = x.get._2
              gui.statLabel.setText("Today's efficiency so far: " + roundToOnePlace(eff*100) +
                "% (≘ " + timeStringFromEff(eff) + "/24h), better than " +
                roundToOnePlace(quantile*100) + "% of days. GO! GO! GO!")
            case None =>
              gui.statLabel.setText("No logged entries yet today! Better get started soon ;)")
          }

        }
        //println("Chart update COMPLETE!")
        println("--------------------------------")
      case Failure(t) =>
        println("An error has occured: " + t.getMessage)
    }
  }

  def roundToOnePlace(d: Double): Double =
    math.floor(d*10)/10

  private def findIdxOfLargestElemLeqThan(a: Array[Double], x: Double): Int = {
    def findHelper(l: Int, u: Int) : Int = {
      if (l + 1 == u)
        l
      else if (x >= a((l+u)/2))
        findHelper((l+u)/2, u)
      else
        findHelper(l, (l+u)/2)
    }

    findHelper(0, a.length)
  }

  private def quantile(samples: Array[Double], p: Double): Double = {
    // binary search that returns the indexof(largest element that is not bigger than p) / N
    if(samples.isEmpty)
      1
    else {
      val idx = findIdxOfLargestElemLeqThan(samples, p)
      idx.toDouble/samples.length
    }
  }

  private def createBufferAndQuantileFromData(dd: Map[Date, TogglplotDayDatum]):
    (ObservableBuffer[Series[Number, Number]], Option[(Double, Double)]) =
  {
    val answer = new ObservableBuffer[Series[Number, Number]]

    val timeEfficiency = new XYChart.Series[Number, Number] {
      name = "effic%"
    }


    //val indexes = List.range(-togglData.length + 1, 1)

    // TODO: make better
    var curDayCal = Calendar.getInstance(new Locale("de"))
    if (curDayCal.get(Calendar.HOUR_OF_DAY) < 4)
      curDayCal.add(Calendar.DAY_OF_MONTH, -1)
    curDayCal.set(Calendar.HOUR_OF_DAY, 0) // zero the time of day
    curDayCal.set(Calendar.MINUTE, 0)
    curDayCal.set(Calendar.SECOND, 0)
    curDayCal.set(Calendar.MILLISECOND, 0)
    val today = curDayCal.getTime

    // TODO: Remove todays value, as we should be comparing our performance to the past days
    // excluding today. this makes the difference between the quantile being i/n and (i+1)/(n+1)
    val efficiencies = dd.values.map(_.eff).toArray
    Sorting.quickSort(efficiencies)

    val todayEffAndQuantile: Option[(Double, Double)] =
      dd.get(today) match {
        case td: Some[TogglplotDayDatum] =>
          if(td.get.dur > 0)
            Some((td.get.eff, quantile(efficiencies, td.get.eff)))
          else
            None // if we have an entry, but it is empty, still return None
        // this muddles the semantics of None <-> today has not been fetched a bit and shifts them towards
        // None <-> no data or duration = 0, but whatever. today will never be unfetched anyways
        case None =>
          None
      }

    // get the 50% quantile to color it later
    val q50: Double = if (!efficiencies.isEmpty) efficiencies((efficiencies.length-1)/2) else 0


    for((day, datum) <- dd) {
      val daysDiff = (day.getTime - today.getTime) / (1000 * 60 * 60 * 24)

      // have nice custom nodes in our chart that display eff and date
      val data = XYChart.Data[Number, Number](daysDiff, datum.eff*24)

      val sp = new StackPane
      sp.prefHeight = 12
      sp.prefWidth = 12
      sp.minWidth = Region.USE_PREF_SIZE
      sp.minHeight = Region.USE_PREF_SIZE

      data.setNode(sp)



      val bgStyle = if(datum.eff == q50) "-fx-background-color: BLUE;" else ""
      data.getNode.setStyle(bgStyle)

      var tt = new Tooltip(new java.text.SimpleDateFormat("EEE MM-dd").format(day) + "\n" +
        roundToOnePlace(datum.eff*100) + "% (" + timeStringFromEff(datum.eff) + ")")

      //Tooltip.install(data.getNode, tt)

      data.getNode.setOnMouseEntered(new EventHandler[MouseEvent] {
        def handle(mouseEvent: MouseEvent) {
          data.getNode.setStyle("-fx-background-color: RED;")
          val origin: Node = mouseEvent.getSource.asInstanceOf[Node]
          tt.show(origin, mouseEvent.getScreenX, mouseEvent.getScreenY)
        }
      })

      data.getNode.setOnMouseExited(new EventHandler[MouseEvent] {
        def handle(mouseEvent: MouseEvent) {
          data.getNode.setStyle(bgStyle)
          tt.hide()
        }
      })

      timeEfficiency.data.getValue.add( data )
      //percentileEfficiency.data.getValue().add( XYChart.Data[Number, Number](idx, quantile(eff)) )
    }

    answer.addAll(timeEfficiency)

    (answer, todayEffAndQuantile)
  }

  private def queryChartData(numDays : Int): Future[(ObservableBuffer[Series[Number, Number]], Option[(Double, Double)])] = {
    // set up date list
    var dayList: List[Date] = Nil

    // find out in which Day (24h period starting at 4 am) we are in
    val curDay = Calendar.getInstance(new Locale("de"))
    if (curDay.get(Calendar.HOUR_OF_DAY) < 4)
      curDay.add(Calendar.DAY_OF_MONTH, -1)

    for (i <- 1 to numDays) {
      val newDay = curDay.clone().asInstanceOf[Calendar]
      dayList = newDay.getTime :: dayList
      curDay.add(Calendar.DAY_OF_MONTH, -1) // decrement the current day for the next iteration
    }

    val futureDayData = pdm.getDaysData(dayList)

    futureDayData.map(createBufferAndQuantileFromData)
  }


  // TODO: reimplement this in the *future*
  /*def updateToday(chart: LineChart[Number, Number]) = {
    // note: program currently requires restart if day changes. won't shift everything back by 1 day in this case yet.
    try {
      val today = getTogglData(1)
      val obsoleteLastEntry = togglData.last
      togglData = togglData.init ::: List( Tuple3(obsoleteLastEntry._1, today.head._2, today.head._3) )
      val xySeries = chart.getData.get(0)
      val todayDataPoint = xySeries.getData.get(xySeries.getData.size-1)

      val todayEff = today.head._3
      todayDataPoint.setYValue(todayEff)

      val efficiencies = togglData.map(_._3).toArray
      Sorting.quickSort(efficiencies)

      statLabel.setText("Today's efficiency so far: " + roundToOnePlace(todayEff*100) + "% (≘ " + roundToOnePlace(todayEff*24) + "h/24h), better than " +
        roundToOnePlace(quantile(efficiencies, todayEff)*100) + "% of days. GO! GO! GO!")
    } catch {
      case e: Exception =>
        println(e.toString)
    }
  }*/
}

object TogglProductivityGraph {
  def main(args: Array[String]): Unit = {
    if(args.length != 1) {
      println("Usage: togglproductivitygraph apiKey")
      //sys.exit(1)
      // launch anyway for testing //TODO: remove my secret credentials!!! :o
      val mainclass = new TogglProductivityGraph("98e3a243a871362c96d64ddaf0d8e01f")
      mainclass.gui.main(Array())
    } else {
      // launch by launching the ScalaFX main method
      val mainclass = new TogglProductivityGraph(args(0))
      mainclass.gui.main(Array())
    }
  }
}
