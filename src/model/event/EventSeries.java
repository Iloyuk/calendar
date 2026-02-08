package model.event;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import model.calendar.ICalendar;

/**
 * Represents a series of events in a calendar system.
 */
public class EventSeries implements IEventSeries, Iterable<ISingleEvent> {
  private final List<ISingleEvent> events;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final DayOfWeek[] occurringDays;

  /**
   * Constructs an {@code EventSeries} object with a given event, the number of occurrences, and
   * the days of the week on which the events occur. The event's starting date/time and ending
   * date/time must be on the same day. If the event's starting date is not in the provided days
   * of the week, then it gets automatically moved to the first valid day of the week.
   *
   * @param event       the event to be added.
   * @param occurrences the number of times this event occurs.
   * @param days        the days of the week this event occurs on.
   * @throws IllegalArgumentException if the event's start and end date are not on the same day,
   *                                  or if the number of occurrences is below 1.
   */
  public EventSeries(ISingleEvent event, int occurrences,
                     DayOfWeek... days) throws IllegalArgumentException {

    if (event.getStartDateTime().getDayOfMonth() != event.getEndDateTime().getDayOfMonth()) {
      throw new IllegalArgumentException("Event must start and end on the same day.");
    } else if (occurrences < 1) {
      throw new IllegalArgumentException("Number of occurrences must be at least 1.");
    }

    this.events = new ArrayList<>();
    this.occurringDays = days;

    // Get our current starting date/time and ending date/time from our SingleEvent
    LocalDateTime currentStartingDateTime = event.getStartDateTime();
    LocalDateTime currentEndingDateTime = event.getEndDateTime();
    LocalDateTime tempEndDateTime = null;
    int occurrencesUsed = 0;

    while (occurrencesUsed < occurrences) {
      if (isDateOnCorrectDaysOfWeek(currentStartingDateTime)) {
        addEventToList(event, currentStartingDateTime, currentEndingDateTime);
        occurrencesUsed++;
      }

      if (occurrencesUsed == occurrences) {
        tempEndDateTime = currentEndingDateTime;
      }

      currentStartingDateTime = currentStartingDateTime.plusDays(1);
      currentEndingDateTime = currentEndingDateTime.plusDays(1);
    }

    this.startDateTime = events.get(0).getStartDateTime();
    this.endDateTime = tempEndDateTime;
  }

  /**
   * Constructs an {@code EventSeries} object with a given event, the ending date of the series,
   * and the days of the week on which the events occur. The event's starting date/time and ending
   * date/time must be on the same day. If the event's starting date is not in the provided days
   * of the week, then it gets automatically moved to the first valid day of the week.
   *
   * @param event   the event to be added.
   * @param endDate the number of times this event occurs.
   * @param days    the days of the week this event occurs on.
   * @throws IllegalArgumentException if the event's start and end date are not on the same day,
   *                                  or if the number of occurrences is below 1.
   */
  public EventSeries(ISingleEvent event, LocalDate endDate,
                     DayOfWeek... days) throws IllegalArgumentException {

    if (event.getStartDateTime().getDayOfMonth() != event.getEndDateTime().getDayOfMonth()) {
      throw new IllegalArgumentException("Event must start and end on the same day.");
    } else if (endDate.isBefore(event.getEndDateTime().toLocalDate())) {
      throw new IllegalArgumentException("Event series cannot end before the current event ends.");
    }

    this.events = new ArrayList<>();
    this.endDateTime = LocalDateTime.of(endDate, event.getEndDateTime().toLocalTime());
    this.occurringDays = days;

    // Get our current starting date/time and ending date/time from our SingleEvent
    LocalDateTime currentStartingDateTime = event.getStartDateTime();
    LocalDateTime currentEndingDateTime = event.getEndDateTime();

    // While our current starting/ending date/time is before the true endDate, add the events
    // that are on the days of the week we listed to our list of events
    while (currentEndingDateTime.isBefore(endDateTime)
            || currentEndingDateTime.equals(endDateTime)) {
      if (isDateOnCorrectDaysOfWeek(currentStartingDateTime)) {
        addEventToList(event, currentStartingDateTime, currentEndingDateTime);
      }
      currentStartingDateTime = currentStartingDateTime.plusDays(1);
      currentEndingDateTime = currentEndingDateTime.plusDays(1);
    }

    this.startDateTime = events.get(0).getStartDateTime();
  }

  /**
   * Constructs an {@code EventSeries} object with a list of events, the end date,
   * and the days of the week.
   *
   * @param events     the list of events in this series.
   * @param endDate    the end date of the series.
   * @param daysOfWeek the days of the week that the event occurs on.
   */
  public EventSeries(List<ISingleEvent> events, LocalDate endDate, DayOfWeek[] daysOfWeek) {
    this.events = new ArrayList<>(events);
    this.endDateTime = LocalDateTime.of(endDate, events.get(0).getEndDateTime().toLocalTime());
    this.occurringDays = daysOfWeek.clone();
    this.startDateTime = events.get(0).getStartDateTime();
  }

  @Override
  public String getSubject() {
    return events.get(0).getSubject();
  }

  @Override
  public LocalDateTime getStartDateTime() {
    return startDateTime;
  }

  @Override
  public LocalDateTime getEndDateTime() {
    return this.events.get(this.events.size() - 1).getEndDateTime();
  }

  @Override
  public String getDescription() {
    return events.get(0).getDescription();
  }

  @Override
  public Location getLocation() {
    return events.get(0).getLocation();
  }

  @Override
  public Status getStatus() {
    return events.get(0).getStatus();
  }

  @Override
  public List<ISingleEvent> getEventsInRange(LocalDateTime start, LocalDateTime end) {
    List<ISingleEvent> queriedEvents = new ArrayList<>();
    for (ISingleEvent event : this.events) {
      queriedEvents.addAll(event.getEventsInRange(start, end));
    }
    return queriedEvents;
  }

  @Override
  public boolean canAddToCalendar(ICalendar calendar) {
    for (ISingleEvent event : this.getEvents()) {
      if (!calendar.canAddSingleEvent(event)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean matchesWith(ISingleEvent newEvent) {
    return events.contains(newEvent);
  }

  @Override
  public boolean containsTime(LocalDateTime dateTime) {
    for (ISingleEvent event : events) {
      if (event.containsTime(dateTime)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public ISingleEvent getCorrespondingEvent(String subject, LocalDateTime startDateTime,
                                           LocalDateTime endDateTime) {
    for (ISingleEvent event : this.events) {
      if (event.getSubject().equals(subject)
              && event.getStartDateTime().equals(startDateTime)
              && event.getEndDateTime().equals(endDateTime)) {
        return event;
      }
    }
    return null;
  }

  @Override
  public IEventSeries getSeriesIfFound(ISingleEvent original) {
    for (ISingleEvent event : this.events) {
      if (event.equals(original)) {
        return this;
      }
    }
    return null;
  }

  @Override
  public ISingleEvent getCorrespondingEventFromStartDate(String subject,
                                                        LocalDateTime startDateTime) {
    for (ISingleEvent event : this.events) {
      if (event.getCorrespondingEventFromStartDate(subject, startDateTime) != null) {
        return event.getCorrespondingEventFromStartDate(subject, startDateTime);
      }
    }
    return null;
  }

  @Override
  public IEventSeries setTimeZone(TimeZone currentTimeZone, TimeZone timeZone) {
    List<ISingleEvent> newEvents = new ArrayList<>();
    for (ISingleEvent event : this.events) {
      ISingleEvent newEvent = event.setTimeZone(currentTimeZone, timeZone);
      newEvents.add(newEvent);
    }
    return new EventSeries(newEvents, this.endDateTime.toLocalDate(), this.occurringDays);
  }

  @Override
  public List<ISingleEvent> getEvents() {
    return new ArrayList<>(events);
  }

  @Override
  public DayOfWeek[] getOccurringDays() {
    return Arrays.copyOf(this.occurringDays, this.occurringDays.length);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    for (ISingleEvent event : this.events) {
      result.append(event.toString()).append("\n");
    }
    return result.toString();
  }

  @Override
  public Iterator<ISingleEvent> iterator() {
    return events.iterator();
  }

  private boolean isDateOnCorrectDaysOfWeek(LocalDateTime dateTime) {
    for (DayOfWeek day : occurringDays) {
      if (dateTime.getDayOfWeek() == day) {
        return true;
      }
    }
    return false;
  }

  private void addEventToList(ISingleEvent event, LocalDateTime startingDateTime,
                              LocalDateTime endingDateTime) {
    this.events.add(new SingleEvent.Builder(
            event.getSubject(), startingDateTime, endingDateTime)
            .description(event.getDescription())
            .location(event.getLocation())
            .status(event.getStatus())
            .build());
  }
}


