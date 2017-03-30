package org.heinzelotto.togglplot

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.util.Calendar

import ch.simas.jtoggl._

/** Interface to Toggl via the JToggl API. Provides us with all functions we need
  *
  * @param apiToken The user's Toggl API token. Can be obtained from the Toggl web-app.
  */
class TogglIface (apiToken: String) {

  // api access parameters
  private val jToggl = new JToggl(apiToken, "api_token")
  jToggl.setThrottlePeriod(700)
  jToggl.switchLoggingOff()

  // NOTE: We only process tasks in the first workspace
  private val workspaces = jToggl.getWorkspaces
  assert (workspaces.size > 0)
  private val wsp = workspaces.head

  /** Returns all Task entries started during a given Day
    *
    * A Day is defined as the 24h period starting at 4am on a given date (local time as set in the Toggl profile settings).
    * Includes the currently running task, even though this one has a special start and end date.
    *
    * @param d date as a java Calendar object
    * @return List of JToggl TimeEntry objects
    */
  def getTimeEntriesOfDay(d: Calendar): List[TimeEntry] = {
    val day = d.clone.asInstanceOf[Calendar]
    day.set(Calendar.HOUR_OF_DAY, 4)
    day.set(Calendar.MINUTE, 0)
    day.set(Calendar.SECOND, 0)

    var nextDay = day.clone().asInstanceOf[Calendar]
    nextDay.set(Calendar.DAY_OF_MONTH, day.get(Calendar.DAY_OF_MONTH) + 1)
    val e: List[TimeEntry] = jToggl.getTimeEntries(day.getTime, nextDay.getTime).asScala.toList
    e
  }

  /*def getTimeEntriesToday(): List[TimeEntry] = {
    var today = Calendar.getInstance(new Locale("de"))
    // day shift is set to 4 am, so if we are between 0 am and 4 am, then today should actually point to 4 am the day before
    if(today.get(Calendar.HOUR_OF_DAY) < 4)
      today.set(Calendar.DAY_OF_MONTH, today.get(Calendar.DAY_OF_MONTH) - 1)

    today.set(Calendar.HOUR_OF_DAY, 4)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)

    val current = new Date()


      val e: List[TimeEntry] = jToggl.getTimeEntries(today.getTime, current).asScala.toList
      //println("number of entries: " + e.size)
      e

  }*/
}
