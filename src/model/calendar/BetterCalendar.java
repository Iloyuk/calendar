package model.calendar;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import model.event.CalendarEvent;

/**
 * A concrete implementation of {@code IBetterCalendar}, now with the ability to represent
 * different timezones.
 */
public class BetterCalendar extends Calendar implements IBetterCalendar {
  private final TimeZone timeZone;

  /**
   * Constructs a {@code BetterCalendar} with the specified name and timezone.
   *
   * @param timezone the timezone of the calendar in string format.
   * @throws IllegalArgumentException if the timezone is invalid.
   */
  public BetterCalendar(String timezone) {
    super();

    try {
      ZoneId zoneId = ZoneId.of(timezone);
      this.timeZone = TimeZone.getTimeZone(zoneId);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid timezone specified!");
    }
  }

  private BetterCalendar(TimeZone timeZone, Set<CalendarEvent> events) {
    super(events);
    this.timeZone = timeZone;
  }

  @Override
  public ZoneId getZoneId() {
    return timeZone.toZoneId();
  }

  @Override
  public IBetterCalendar makeCalWithNewTimeZone(String newTimeZoneString) {
    ZoneId zoneId;
    try {
      zoneId = ZoneId.of(newTimeZoneString);
    } catch (DateTimeException e) {
      throw new IllegalArgumentException("Invalid timezone specified!");
    }
    TimeZone newTimeZone = TimeZone.getTimeZone(zoneId);

    Set<CalendarEvent> events = new HashSet<>();
    for (CalendarEvent event : this.getEvents()) {
      CalendarEvent newEvent = event.setTimeZone(timeZone, newTimeZone);
      events.add(newEvent);
    }

    return new BetterCalendar(newTimeZone, events);
  }

}
