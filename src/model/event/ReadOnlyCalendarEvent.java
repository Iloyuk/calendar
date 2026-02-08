package model.event;

import java.time.LocalDateTime;

/**
 * A read-only interface representing an event in the calendar.
 */
public interface ReadOnlyCalendarEvent {
  /**
   * Gets the start date and time of the event.
   *
   * @return the start date and time of the event
   */
  LocalDateTime getStartDateTime();

  /**
   * Gets the end date and time of the event.
   *
   * @return the end date and time of the event
   */
  LocalDateTime getEndDateTime();

  /**
   * Gets the subject of the event.
   *
   * @return the subject of the event
   */
  String getSubject();

  /**
   * Gets the description of the event.
   *
   * @return the description of the event
   */
  String getDescription();

  /**
   * Gets the location of the event.
   *
   * @return the location of the event
   */
  Location getLocation();

  /**
   * Gets the status of an event, whether it is public or private.
   *
   * @return the status of the event
   */
  Status getStatus();
}
