package model.event;

import java.time.LocalDateTime;
import java.util.TimeZone;

/**
 * An interface representing an event in a calendar system.
 */
public interface ISingleEvent extends CalendarEvent {
  /**
   * Creates a new event from {@code this} with the specified {@code subject}.
   *
   * @param subject the subject of the new event.
   * @return a new event with the original values but the new specified subject.
   */
  ISingleEvent createNewEventWithSubject(String subject);

  /**
   * Creates a new event from {@code this} with the specified {@code endDateTime}.
   *
   * @param endDateTime the end date/time of the new event, throws an exception if the
   *                    event is in a series and the end date/time is not on the
   *                    same day as the start date/time.
   *                    throws an exception if the end date/time is before the start date/time.
   * @return a new event with the original values but the new specified end date/time.
   */
  ISingleEvent createNewEventWithNewEndDate(LocalDateTime endDateTime);

  /**
   * Creates a new event from {@code this} with the specified {@code description}.
   *
   * @param description the description of the new event.
   * @return a new event with the original values but the new specified description.
   */
  ISingleEvent createNewEventWithNewDescription(String description);

  /**
   * Creates a new event from {@code this} with the specified {@code location}.
   *
   * @param location the location of the new event.
   * @return a new event with the original values but the new specified location.
   */
  ISingleEvent createNewEventWithNewLocation(Location location);

  /**
   * Creates a new event from {@code this} with the specified {@code status}.
   *
   * @param status the status of the new event.
   * @return a new event with the original values but the new specified status.
   */
  ISingleEvent createNewEventWithNewStatus(Status status);

  /**
   * Creates a new event from {@code this} with the specified {@code startDateTime} and
   * {@code endDateTime}.
   *
   * @param startDateTime the start date/time of the new event.
   * @param endDateTime the end date/time of the new event.
   * @return a new event with the original values but the new starting and ending date/times.
   */
  ISingleEvent createNewEventWithNewStartDateAndEndDate(LocalDateTime startDateTime,
                                                       LocalDateTime endDateTime);

  /**
   * Creates a new event with a new start date, also moves the end date
   * to maintain the same duration.
   *
   * @param value the new start date and time for the event
   * @return a new event with the updated start date
   */
  ISingleEvent createNewEventWithNewStartDate(LocalDateTime value);

  /**
   * Sets the time zone for this event.
   *
   * @param currentTimeZone the current time zone of the event.
   * @param timeZone the new time zone to set for the event.
   * @return a new event with the updated Time of the updated time zone.
   */
  ISingleEvent setTimeZone(TimeZone currentTimeZone, TimeZone timeZone);
}


