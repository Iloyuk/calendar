package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.event.Location;
import model.event.ReadOnlyCalendarEvent;
import model.event.Status;

/**
 * A list of features that can be used in the calendar GUI application.
 */
public interface Features {
  /**
   * Creates a {@code SingleEvent} in the current calendar with the specified properties.
   *
   * @param subject the subject of the event.
   * @param start the start date/time of the event.
   * @param end the end date/time of the event.
   * @param description the event's description.
   * @param location the event's location.
   * @param status the event's status.
   */
  void createEvent(String subject, LocalDateTime start, LocalDateTime end, String description,
                   Location location, Status status);

  /**
   * Tells the view to display all events in the current calendar starting from the
   * {@code start} date.
   *
   * @param start the start date to start getting events from.
   */
  void showEventsFrom(LocalDate start);

  /**
   * Changes the view's currently displayed calendar to the provided name.
   *
   * @param calendarName the name of the calendar to change to.
   */
  void changeCalendar(String calendarName);

  /**
   * Creates a new calendar with the provided name and timezone.
   *
   * @param calendarName the name of the calendar to create.
   * @param timezone the timezone of the calendar to create.
   */
  void addCalendar(String calendarName, String timezone);

  /**
   * Edits an event's property with the new value.
   *
   * @param event the event to be edited.
   * @param property the property of the event to be edited.
   * @param value the new value of the property.
   */
  void editEvent(ReadOnlyCalendarEvent event, String property, String value);
}
