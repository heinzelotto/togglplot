package org.heinzelotto.togglplot


import javafx.event.{ActionEvent, EventHandler}

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.geometry._
import scalafx.scene.Scene
import scalafx.scene.chart._
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{HBox, VBox}
import scalafx.event._
import scalafx.Includes.handle

/**
  * Created by felix on 16.07.16.
  */
/*class Gui(val buttonHandleGetChart: EventHandler[ActionEvent],
          val buttonHandleUpdateToday: EventHandler[ActionEvent]
         ) extends JFXApp {*/

class Gui(val buttonHandleGetChart: EventHandler[ActionEvent]
         ) extends JFXApp {
  // UI setup
  var statLabel = Label("No logged entries yet today! Better get started soon ;)")
  statLabel.setStyle("-fx-font: 18 arial;color:#ee3300")

  val xAxis = new NumberAxis(-185, 1, 5)
  val yAxis = new NumberAxis(0, 24, 1)
  var lineChart = LineChart(xAxis, yAxis)
  lineChart.title = "time efficiency & today's percentile efficiency"
  //lineChart.data =
  lineChart.prefHeight = 600

  var buttonGetChart = new Button{
    id = "getChart"
    text = "Update!"
    tooltip = "Get new Data"
    //onAction = handle(updateToday(lineChart))
    onAction = buttonHandleGetChart
  }

  /*var buttonUpdateToday = new Button{
    id = "updateToday"
    text = "Update Today!"
    tooltip = "Refresh today's data"
    //onAction = handle(updateToday(lineChart))
    onAction = buttonHandleUpdateToday
  }*/

  //statLabel.setText("")
  //var vbox = new VBox(lineChart, statLabel, new HBox(buttonGetChart, buttonUpdateToday))
  var vbox = new VBox(lineChart, statLabel, buttonGetChart)
  vbox.setAlignment(Pos.Center)

  stage = new PrimaryStage {
    title = "TogglPlot"
    scene = new Scene(1200, 650) {
      root = vbox
    }
  }
  // UI setup complete
}
