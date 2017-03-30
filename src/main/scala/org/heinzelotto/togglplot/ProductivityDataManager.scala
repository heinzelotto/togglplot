package org.heinzelotto.togglplot

import java.util.{Calendar, Date, Locale}

import scala.concurrent._
import ExecutionContext.Implicits.global
import ch.simas.jtoggl.TimeEntry

import TogglDateHelper._

/** Stores the productivity of one Day
  *
  * @param needsRefresh true if this Day was not yet over at the time of the last update.
  * @param dur Sum of length in s of all tasks started during this Day
  */
sealed case class TogglplotDayDatum(val needsRefresh: Boolean, val dur: Long) {
  private def efficiencyFromDuration(duration: Long): Double =
    duration / (60.0 * 60 * 24)

  val eff: Double = efficiencyFromDuration(dur)
}

/* // is a case class now so these are implicitly given
object TogglDatum {
  def apply(r: Boolean, d: Long): TogglDatum = new TogglDatum(c, d)
}*/

class ProductivityDataManager(val apiToken: String) {
  val toggl = new TogglIface(apiToken)

  // IO interface
  val csv = new CSVStatReader("log.csv")

  private var dayData: Map[Date, TogglplotDayDatum] = csv.readFromFile

  def getDaysData(days: List[Date]): Future[Map[Date, TogglplotDayDatum]] = Future {
    //println("GetDaysData START")

    val strippedDays = days.map(stripDate _)

    val dataAsList: List[(Date, TogglplotDayDatum)] = for {
      day <- strippedDays
    } yield {
      dayData.get(day) match {
        case x: Some[TogglplotDayDatum] =>
          if(!x.get.needsRefresh) {
            println("found day     " + new java.text.SimpleDateFormat("EEE MM-dd").format(day) + ", eff: " + x.get.eff)
            day -> x.get
          } else {
            println("reloading day " + new java.text.SimpleDateFormat("EEE MM-dd").format(day) + ":")
            val datum = downloadTogglDatumForSingleDate(day)
            println("new eff: " + datum.eff)
            // overwrite current entry (replaces old pair)
            dayData += day -> datum
            day -> datum
          }
        case None =>
          print("not found day " + new java.text.SimpleDateFormat("EEE MM-dd").format(day) + ", ")
          val datum = downloadTogglDatumForSingleDate(day)
          println("eff: " + datum.eff)
          // also directly add it to our map
          dayData += day -> datum // TODO: maybe change into mutable map so it doesn't have to instantiated anew, could be slow once the db gets massive
          day -> datum
      }
    }

    // save our new stuff
    // TODO: optimize file saving
    csv.writeToFile(dayData)

    //println("getDaysData COMPLETE!")

    Map(dataAsList: _*) // or maybe .toMap ? but i have to investigate the type safety of that, it let me create
    // a Map[Date, Date] out of a List[(Date, TogglDatum)] ?!?!?!
  }

  def markAsRefresh(days: List[Date]): Unit = {
    for(day <- days) {
      val strippedDay = stripDate(day)
      dayData.get(strippedDay) match {
        case x: Some[TogglplotDayDatum] =>
          dayData += strippedDay -> TogglplotDayDatum(true, x.get.dur)
        case None =>
          // nothing
          println("This date doesn't have an entry associated, cannot mark as needing refresh.")
      }
    }
  }

  private def downloadTogglDatumForSingleDate(day: Date): TogglplotDayDatum = {
    val duration = totalDuration(toggl.getTimeEntriesOfDay(dateToCal(day)))

    var curDayCal = Calendar.getInstance(new Locale("de"))
    if (curDayCal.get(Calendar.HOUR_OF_DAY) < 4)
      curDayCal.add(Calendar.DAY_OF_MONTH, -1)
    curDayCal.set(Calendar.HOUR_OF_DAY, 0) // zero the time of day
    curDayCal.set(Calendar.MINUTE, 0)
    curDayCal.set(Calendar.SECOND, 0)
    curDayCal.set(Calendar.MILLISECOND, 0)
    val today = curDayCal.getTime

    //println("today: " + today + "\ttarget: " + day + "\tcompared: " + (day compareTo today))

    (day compareTo today) match {
      case 0 => // day == today, so the data is not definitely final yet. needs refresh
        TogglplotDayDatum(true, duration)
      case -1 => // day lies at least 1 day in the past, we assume the data won't change anymore. doesn't need refresh
        TogglplotDayDatum(false, duration)
      case 1 =>
        throw new IllegalStateException("Day to check lies in the future. This should not happen.")
    }
  }

  private def totalDuration(dayEntries: List[TimeEntry]): Long = {
    val durations: List[Long] = for (e <- dayEntries) yield
      e.getDescription match {
        case "lol" =>
          0
        case _ =>
          if (e.getDuration < 0) {
            // in millis, and the duration of an entry that is still running is startdate - 1970epoch
            e.getDuration + new Date().getTime / 1000
          } else
            e.getDuration
      }
    //for (d <-durations) println(d)

    val totalDurationToday = durations.sum

    totalDurationToday
  }
}
