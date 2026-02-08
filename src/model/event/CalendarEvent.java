package model.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import model.calendar.ICalendar;

/**
 * An interface representing an event in the calendar.
 */
public interface CalendarEvent extends ReadOnlyCalendarEvent {
  /**
   * Gets the events in between the queried times.
   *
   * @param start the starting time.
   * @param end the ending time.
   * @return a list of all events between the queried times.
   */
  List<ISingleEvent> getEventsInRange(LocalDateTime start, LocalDateTime end);

  /**
   * Gets the corresponding event based on the subject and start and end date/time.
   * returns null if no such event exists.
   *
   * @param subject the subject of the event
   * @param startDateTime the start date/time of the event
   * @param endDateTime the end date/time of the event
   * @return the event that matches the given parameters, or null if no such event exists
   */
  ISingleEvent getCorrespondingEvent(String subject, LocalDateTime startDateTime,
                                    LocalDateTime endDateTime);

  /**
   * Checks if the event matches with a new event.
   *
   * @param newEvent the new event to check against
   * @return {@code true} if the event matches with the new event, {@code false} otherwise
   */
  boolean matchesWith(ISingleEvent newEvent);

  /**
   * Checks whether an event in the calendar is within the specified date/time.
   *
   * @param dateTime the provided date/time.
   * @return {@code true} if an event is within the specified date/time, {@code false} if otherwise.
   */
  boolean containsTime(LocalDateTime dateTime);

  /**
   * Checks if the event can be added to a given calendar.
   *
   * @param calendar the calendar to check against.
   * @return {@code true} if the event can be added, {@code false} otherwise
   */
  boolean canAddToCalendar(ICalendar calendar);

  /**
   * Gets the series if the given single event is part of an event series in the calendar,
   * otherwise returns null.
   *
   * @param original the single event to check
   * @return the event series that contains the single event,
   *         or null if it is not part of any series
   */
  IEventSeries getSeriesIfFound(ISingleEvent original);

  /**
   * Gets the events that match the given subject and start date/time.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date/time of the event.
   * @return the single event that match the given parameters (event could be in series),
   *         or null if no event found.
   */
  ISingleEvent getCorrespondingEventFromStartDate(String subject, LocalDateTime startDateTime);

  /**
   * returns the event with start date/time and end date/time in the new time zone.
   *
   * @param currentTimeZone the current time zone of the event.
   * @param timeZone the time zone to set for the event.
   * @return the updated CalendarEvent with the new time zone.
   */
  CalendarEvent setTimeZone(TimeZone currentTimeZone, TimeZone timeZone);
}
