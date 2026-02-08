package model.calendar;

import java.time.ZoneId;

/**
 * An interface representing a newly-improved calendar, now with the ability to
 * represent timezones in its events.
 */
public interface IBetterCalendar extends ICalendar {
  /**
   * Gets the timezone id of this calendar.
   *
   * @return the timezone id.
   */
  ZoneId getZoneId();

  /**
   * Creates a new calendar with the same name as this one, but with a new time zone.
   *
   * @param   newTimeZoneString the new time zone for the calendar.
   * @return  a new BetterCalendar with the specified time zone and the same name and all the
   *          events in the calendar converted.
   */
  IBetterCalendar makeCalWithNewTimeZone(String newTimeZoneString);
}
