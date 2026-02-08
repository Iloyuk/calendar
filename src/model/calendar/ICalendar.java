package model.calendar;

import java.time.LocalDateTime;
import java.util.List;

import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.CalendarEvent;

/**
 * An interface representing a calendar system.
 */
public interface ICalendar {
  /**
   * Adds a new event to the calendar. This can be either a single event or an event series.
   * if event have the same subject and start and end date/time as an existing event,
   * throw an exception.
   *
   * @param event the event to be added.
   */
  void addEvent(CalendarEvent event);

  /**
   * Edits an existing event's property with {@code newValue}.
   *
   * @param property the property of the event to be updated
   *                 (ex. {@code subject}, {@code start}, etc.)
   * @param subject the field of the property of the event that is going to be updated.
   * @param startDateTime the start date/time of the event.
   * @param endDateTime the end date/time of the event.
   * @param newValue the new field of the property.
   */
  void editEvent(String property, String subject, LocalDateTime startDateTime,
                 LocalDateTime endDateTime, String newValue);

  /**
   * Edits an existing event series' property with {@code newValue}.
   *
   * @param property the property of the event to be updated
   *                 (ex. {@code subject}, {@code start}, {@code end}, {@code description},
   *                 {@code location}, {@code status})
   * @param subject the field of the property of the event that is going to be updated.
   * @param startDateTime the start date/time of the event series.
   * @param commandType the type of command, either "series" or "events".
   * @param newValue the new field of the property.
   */
  void editEvents(String property, String subject, LocalDateTime startDateTime,
                  String commandType, String newValue);

  /**
   * Provides a list of events in the calendar that match the given date range, sorted from
   * the earliest start date to the latest.
   *
   * @param start the start date and time of the range.
   * @param end the end date of the time of the range.
   * @return a list of events that fall within the specified date range.
   */
  List<ISingleEvent> query(LocalDateTime start, LocalDateTime end);

  /**
   * Gets an event by its subject and start and end date/time.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date/time of the event.
   * @param endDateTime the end date/time of the event.
   * @return the event that matches the given parameters, throws an exception
   *         if no such event exists.
   */
  ISingleEvent getSingleEventWithStartAndEndDate(String subject, LocalDateTime startDateTime,
                                                LocalDateTime endDateTime);

  /**
   * Checks if the given single event is part of an event series in the calendar.
   *
   * @param original the single event to check.
   * @return the event series that contains the single event, or null if it is not part
   *         of any series.
   */
  IEventSeries isInSeries(ISingleEvent original);

  /**
   * Checks if any event in this calendar contains the specified date/time.
   *
   * @param dateTime the provided date/time.
   * @return {@code true} if an event has the specified date/time, {@code false} if otherwise.
   */
  boolean containsTime(LocalDateTime dateTime);

  /**
   * Checks if a single event can be added to the calendar.
   *
   * @param newEvent the single event to check.
   * @return true if the event can be added, false otherwise.
   */
  boolean canAddSingleEvent(ISingleEvent newEvent);

  /**
   * Gets a list of single events that match the given subject and start date/time.
   *
   * @param subject the subject of the event.
   * @param startDateTime the start date/time of the event.
   * @return a list of single events that match the given parameters (event could be in series and
   *         could be empty if no event found).
   */
  List<ISingleEvent> getSingleEventsWithStartDate(String subject, LocalDateTime startDateTime);

}
