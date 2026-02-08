package model.event;

import java.time.DayOfWeek;
import java.util.List;

/**
 * An interface representing an event series in the calendar.
 */
public interface IEventSeries extends CalendarEvent {
  /**
   * Returns the list of all the events in this series.
   *
   * @return the list of SingleEvent objects representing the events in this series.
   */
  List<ISingleEvent> getEvents();

  /**
   * Returns an array of all the days of the week that this event occurs on.
   *
   * @return the array of the days of week.
   */
  DayOfWeek[] getOccurringDays();
}