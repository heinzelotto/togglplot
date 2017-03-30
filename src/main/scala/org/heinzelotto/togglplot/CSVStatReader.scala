package org.heinzelotto.togglplot

import scala.io.Source
import java.io.{File, FileNotFoundException, PrintWriter}
import java.util.Date

import scala.collection.mutable

/** Interface for storage of the productivity data to disk. */
class CSVStatReader(val filename: String) {
  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")

  /** Returns the productivity data structure parsed from storage file.
    *
    * Can be called repeatedly.
    */
  def readFromFile: Map[Date, TogglplotDayDatum] = {
    var m = Map.empty[Date, TogglplotDayDatum]
    try {
      val bufferedFile = Source.fromFile(filename)
      for (l <- bufferedFile.getLines()) {
        if(l.nonEmpty) {
          val cols = l.split(",")
          m += dateFormat.parse(cols(0)) -> TogglplotDayDatum(cols(1).toBoolean, cols(2).toLong)
        }
      }
      m
    } catch {
      case e: FileNotFoundException =>
        println("Datafile '"+filename + "' not existing yet.")
        Map.empty[Date, TogglplotDayDatum]
      case e: Exception =>
        println(e.getMessage)
        Map.empty[Date, TogglplotDayDatum]
    }
  }

  /** Saves current state to file
    *
    * Is called repeatedly. Order is not preserved.
    */
  def writeToFile(m: Map[Date, TogglplotDayDatum]): Unit = {
    val writer = new PrintWriter(new File(filename))

    val csvLines = for{(day, td) <- m} yield
      dateFormat.format(day) + "," + td.needsRefresh.toString + "," + td.dur.toString

    for(l <- csvLines)
      writer.write(l + "\n")

    writer.close()
  }
}
