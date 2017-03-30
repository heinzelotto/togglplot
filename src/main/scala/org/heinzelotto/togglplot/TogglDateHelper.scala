package org.heinzelotto.togglplot

import java.util.{Calendar, Date, Locale}

/**
  * Created by felix on 18.01.17.
  */
object TogglDateHelper {
  def stripDate(d: Date): Date = {
    val cal = Calendar.getInstance()
    cal.setTime(d)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.getTime()
  }

  def dateToCal(d: Date): Calendar = {
    var dayToAdd = Calendar.getInstance()
    dayToAdd.setTime(d)
    dayToAdd
  }

  def getCurDayCalendar() : Calendar = {
    var curDayCal = Calendar.getInstance(new Locale("de")) // FIXME: is this necessary/does this have an effect?
                                                            // Use the timezone from Toggl.

    if (curDayCal.get(Calendar.HOUR_OF_DAY) < 4)
      curDayCal.add(Calendar.DAY_OF_MONTH, -1)

    // zero the time of day
    curDayCal.set(Calendar.HOUR_OF_DAY, 0)
    curDayCal.set(Calendar.MINUTE, 0)
    curDayCal.set(Calendar.SECOND, 0)
    curDayCal.set(Calendar.MILLISECOND, 0)

    curDayCal
  }
}
