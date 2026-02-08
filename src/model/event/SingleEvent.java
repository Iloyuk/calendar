package model.event;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

import model.calendar.ICalendar;

/**
 * Represents a single event in a calendar system.
 */
public class SingleEvent implements ISingleEvent {
  private final String subject;
  private final LocalDateTime startDateTime;
  // The rest of these are optional
  private final LocalDateTime endDateTime;
  private final String description;
  private final Location location;
  private final Status status;

  /**
   * Constructs a {@code SingleEvent} object from the inner builder class.
   */
  private SingleEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                      String description, Location location, Status status)
          throws IllegalArgumentException {
    if (endDateTime.isBefore(startDateTime)) {
      throw new IllegalArgumentException("Event start date/time cannot be before end date/time!");
    } else {
      this.subject = subject;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.description = description;
      this.location = location;
      this.status = status;
    }
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return this.startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return this.endDateTime;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public Status getStatus() {
    return this.status;
  }

  @Override
  public List<ISingleEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    List<ISingleEvent> returnedEvent = new ArrayList<>();
    if (!this.getStartDateTime().isBefore(start) && !this.getEndDateTime().isAfter(end)) {
      returnedEvent.add(this);
    }
    return returnedEvent;
  }

  @Override
  public boolean canAddToCalendar(ICalendar calendar) {
    return calendar.canAddSingleEvent(this);
  }

  @Override
  public boolean matchesWith(ISingleEvent newEvent) {
    return this.equals(newEvent);
  }

  @Override
  public boolean containsTime(LocalDateTime dateTime) {
    return startDateTime.isBefore(dateTime) && endDateTime.isAfter(dateTime);
  }

  @Override
  public ISingleEvent getCorrespondingEvent(String subject, LocalDateTime startDateTime,
                                           LocalDateTime endDateTime) {
    if (this.subject.equals(subject)
            && this.startDateTime.equals(startDateTime)
            && this.endDateTime.equals(endDateTime)) {
      return this;
    }
    return null;
  }

  @Override
  public IEventSeries getSeriesIfFound(ISingleEvent original) {
    return null;
  }

  @Override
  public ISingleEvent getCorrespondingEventFromStartDate(String subject,
                                                        LocalDateTime startDateTime) {
    if (this.getSubject().equals(subject) && this.getStartDateTime().equals(startDateTime)) {
      return this;
    }
    return null;
  }

  @Override
  public ISingleEvent setTimeZone(TimeZone currentTimeZone, TimeZone timeZone) {
    ZoneId currentZoneId = currentTimeZone.toZoneId();
    ZoneId newZoneId = timeZone.toZoneId();
    ZonedDateTime startZonedDateTime = this.startDateTime.atZone(currentZoneId);
    ZonedDateTime endZonedDateTime = this.endDateTime.atZone(currentZoneId);
    ZonedDateTime newStartZonedDateTime = startZonedDateTime.withZoneSameInstant(newZoneId);
    ZonedDateTime newEndZonedDateTime = endZonedDateTime.withZoneSameInstant(newZoneId);
    LocalDateTime newStartDateTime = newStartZonedDateTime.toLocalDateTime();
    LocalDateTime newEndDateTime = newEndZonedDateTime.toLocalDateTime();
    return this.createNewEventWithNewStartDateAndEndDate(newStartDateTime, newEndDateTime);
  }

  @Override
  public ISingleEvent createNewEventWithSubject(String subject) {
    return new SingleEvent.Builder(subject, this.startDateTime, this.endDateTime)
            .description(this.description)
            .location(this.location)
            .status(this.status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewStartDate(LocalDateTime startDateTime) {
    Duration shiftDuration = Duration.between(this.startDateTime, startDateTime);
    LocalDateTime newEndDateTime = this.endDateTime.plus(shiftDuration);
    return new SingleEvent.Builder(this.subject, startDateTime, newEndDateTime)
            .description(this.description)
            .location(this.location)
            .status(this.status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewEndDate(LocalDateTime value) {
    if (value.isBefore(this.startDateTime)) {
      throw new IllegalArgumentException("End date/time cannot be before start date/time.");
    }
    return new SingleEvent.Builder(this.subject, this.startDateTime, value)
            .description(this.description)
            .location(this.location)
            .status(this.status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewDescription(String description) {
    return new SingleEvent.Builder(this.subject, this.startDateTime, this.endDateTime)
            .description(description)
            .location(this.location)
            .status(this.status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewLocation(Location location) {
    if (location != Location.ONLINE && location != Location.PHYSICAL && location != null) {
      throw new IllegalArgumentException("Location must be either 'Online' or 'Physical'.");
    }
    return new SingleEvent.Builder(this.subject, this.startDateTime, this.endDateTime)
            .description(this.description)
            .location(location)
            .status(this.status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewStatus(Status status) {
    if (status != Status.PUBLIC && status != Status.PRIVATE && status != null) {
      throw new IllegalArgumentException("Status must be either 'Public' or 'Private'.");
    }
    return new SingleEvent.Builder(this.subject, this.startDateTime, this.endDateTime)
            .description(this.description)
            .location(this.location)
            .status(status)
            .build();
  }

  @Override
  public ISingleEvent createNewEventWithNewStartDateAndEndDate(LocalDateTime newStartDateTime,
                                                              LocalDateTime newEndDateTime) {
    return new SingleEvent.Builder(this.subject, newStartDateTime, newEndDateTime)
            .description(this.description)
            .location(this.location)
            .status(this.status)
            .build();
  }

  /**
   * Determines whether two {@code SingleEvent} objects are equal. Two events are considered equal
   * if they have the same subject, start date/time, and end date/time.
   *
   * @param other The other {@code SingleEvent} to be compared with this one.
   * @return {@code true} if the two {@code SingleEvent}s are equal, {@code false} otherwise.
   */
  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof SingleEvent)) {
      return false;
    } else {
      SingleEvent otherEvent = (SingleEvent) other;
      return this.subject.equals(otherEvent.subject)
              && this.startDateTime.equals(otherEvent.startDateTime)
              && this.endDateTime.equals(otherEvent.endDateTime);
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(subject, startDateTime, endDateTime);
  }

  @Override
  public String toString() {
    String description = this.description == null ? "N/A" : this.description;

    String locationString;
    if (this.location == null) {
      locationString = "N/A";
    } else {
      locationString = this.location.toString();
    }

    String statusString;
    if (this.status == null) {
      statusString = "N/A";
    } else {
      statusString = this.status.toString();
    }

    return String.format("• [Subject: %s, Start: %s, End: %s, Description: %s, Location: %s,"
            + " Status: %s]", subject, startDateTime.toString(), endDateTime.toString(),
            description, locationString, statusString);
  }

  /**
   * A builder class for creating {@code SingleEvent} objects.
   */
  public static class Builder {
    private final String subject;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    // The rest of these are optional
    private String description;
    private Location location;
    private Status status;

    /**
     * Constructor to set default values to the {@code SingleEvent} fields.
     *
     * @param subject the subject of the event.
     * @param startDateTime the start date/time of the event.
     */
    public Builder(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime) {
      this.subject = subject;
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
      this.description = null;
      this.status = null;
      this.location = null;
    }

    /**
     * Constructor to create an all-day event with the specified date.
     *
     * @param subject the subject of the event.
     * @param date the starting date of the event.
     */
    public Builder(String subject, LocalDate date) {
      this.subject = subject;
      this.startDateTime = LocalDateTime.of(date, LocalTime.of(8, 0, 0));
      this.endDateTime = LocalDateTime.of(date, LocalTime.of(17, 0, 0));
      this.description = null;
      this.status = null;
      this.location = null;
    }

    /**
     * Sets the description of the event.
     *
     * @param description the description of the event.
     * @return returns the builder with the updated description.
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the location of the event.
     *
     * @param location the location of the event.
     * @return the builder with the updated location.
     */
    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the status of the event.
     *
     * @param status the status of the event.
     * @return the builder with the update status.
     */
    public Builder status(Status status) {
      this.status = status;
      return this;
    }

    /**
     * Builds the {@code SingleEvent} object with the provided parameters. If no endDateTime
     * is provided, the builder defaults to create an all-day event, which is defined to be from
     * 8AM to 5PM.
     *
     * @return a new {@code SingleEvent} object.
     */
    public SingleEvent build() {
      return new SingleEvent(subject, startDateTime, endDateTime, description, location, status);
    }
  }
}
