package model.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.calendar.IBetterCalendar;

/**
 * An interface representing the part of the model that manages calendars and their timezones. It
 * provides methods to create, edit, and manage calendars, as well as copy events between calendars.
 */
public interface ICalendarManager {
  /**
   * Creates a new calendar with the specified name and time zone.
   *
   * @param calendarName the name of the calendar to create.
   * @param timeZone     the time zone for the calendar.
   */
  void createCalendar(String calendarName, String timeZone);

  /**
   * Gets the currently used calendar's name.
   *
   * @return the name of the current calendar in use.
   */
  String getCurrentCalendarName();

  /**
   * Gets the current calendar used by this application.
   *
   * @return the current calendar being used, or null if there is none.
   */
  IBetterCalendar getCurrentCalendar();

  /**
   * Edits an existing calendar's time zone, without changing the name.
   * Editing the time zone will convert all events in the calendar to the new time zone.
   *
   * @param calendarName  the name of calendar to be edited.
   * @param timeZone      the new timezone of the calendar.
   */
  void editCalendarTimeZone(String calendarName, String timeZone);

  /**
   * Edits an existing calendar's name with a new one without changing the time zone or events.
   *
   * @param oldName the old name of the calendar.
   * @param newName the new name of the calendar.
   */
  void editCalendarName(String oldName, String newName);

  /**
   * Sets the specified calendar's name as the current calendar in use.
   *
   * @param calendarName the calendar's name to set as in use.
   */
  void setCalendarInUse(String calendarName);

  /**
   * Copies an event to a target calendar at a specified start time.
   * (changes the end date/time of the event to match the new start date/time)
   *
   * @param eventName           the name of the event to copy.
   * @param eventStartDateTime  the start date/time for the event to copy.
   * @param targetCalendarName  the name of the calendar to copy the event to.
   * @param newStartDateTime    the new start date/time for the copied event.
   */
  void copyEvent(String eventName, LocalDateTime eventStartDateTime, String targetCalendarName,
                 LocalDateTime newStartDateTime);

  /**
   * Copies a list of events to a target calendar starting from a specified date.
   * (coverts the event to the new time zone).
   *
   * @param start               the start of the interval for the list of events to copy.
   * @param end                 the end of the interval for the list of events to copy.
   * @param targetCalendarName  the name of the calendar to copy the events to.
   * @param newStartDate        the date at which the copied events will start from.
   */
  void copyEvents(LocalDateTime start, LocalDateTime end, String targetCalendarName,
                  LocalDate newStartDate);
}
