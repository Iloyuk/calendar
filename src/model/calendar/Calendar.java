package model.calendar;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import model.event.EventSeries;
import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.Location;
import model.event.CalendarEvent;
import model.event.Status;
import model.event.comparators.LexicographicalComparator;
import model.event.comparators.StartDateTimeComparator;

/**
 * Represents a calendar system that can hold both singular events and event series, with
 * the ability to edit and print its events.
 */
public class Calendar implements ICalendar {
  private final Set<CalendarEvent> events;

  /**
   * Constructs a {@code Calendar} object with an empty set of events.
   */
  public Calendar() {
    this.events = new HashSet<>();
  }

  protected Calendar(Set<CalendarEvent> events) {
    this.events = events;
  }

  protected Set<CalendarEvent> getEvents() {
    return events;
  }

  @Override
  public void addEvent(CalendarEvent event) {
    if (event.canAddToCalendar(this)) {
      events.add(event);
    } else {
      throw new IllegalArgumentException("Event cannot be added to the calendar.");
    }
  }

  @Override
  public void editEvent(String property, String subject, LocalDateTime startDateTime,
                        LocalDateTime endDateTime, String newValue) {
    final ISingleEvent event =
            getSingleEventWithStartAndEndDate(subject, startDateTime, endDateTime);
    if (event == null) {
      throw new IllegalArgumentException("Event not found");
    }
    final IEventSeries series = isInSeries(event);

    List<CalendarEvent> eventUpdated = new ArrayList<>();
    switch (property) {
      case "subject":
        eventUpdated.add(updateEventSubject(event, series, newValue));
        break;
      case "start":
        LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
        eventUpdated.addAll(updateEventStartDate(event, series, newStartDateTime));
        break;
      case "end":
        LocalDateTime newEndDateTime = LocalDateTime.parse(newValue);
        eventUpdated.addAll(updateEventEndDate(event, series, newEndDateTime));
        break;
      case "description":
        eventUpdated.add(updateEventDescription(event, series, newValue));
        break;
      case "location":
        if (!checkRightLocation(newValue)) {
          throw new IllegalArgumentException("Invalid Location value: " + newValue);
        }
        eventUpdated.add(updateEventLocation(event, series, Location.getLocation(newValue)));
        break;
      case "status":
        if (!checkRightStatus(newValue)) {
          throw new IllegalArgumentException("Invalid status value: " + newValue);
        }
        eventUpdated.add(updateEventStatus(event, series, Status.getStatus(newValue)));
        break;
      default:
        throw new IllegalArgumentException("Invalid input: unsupported property");
    }

    if (series != null) {
      edit(series, eventUpdated);
    } else {
      edit(event, eventUpdated);
    }
  }

  @Override
  public void editEvents(String property, String subject, LocalDateTime startDateTime,
                         String commandType, String newValue) {
    List<ISingleEvent> matchedEvents = getSingleEventsWithStartDate(subject, startDateTime);
    if (matchedEvents.isEmpty()) {
      throw new IllegalArgumentException("No matching events found!");
    }

    if (matchedEvents.size() > 1) {
      throw new IllegalArgumentException("Multiple matching events found, cannot edit event");
    }
    final ISingleEvent event = matchedEvents.get(0);
    final IEventSeries series = isInSeries(event);

    List<CalendarEvent> eventUpdated = new ArrayList<>();
    switch (property) {
      case "subject":
        if (commandType.equals("events")) {
          eventUpdated.add(updateEventsSubject(event, series, newValue));
        } else {
          eventUpdated.add(updateSeriesSubject(event, series, newValue));
        }

        break;
      case "start":
        LocalDateTime newStartDateTime = LocalDateTime.parse(newValue);
        if (commandType.equals("events")) {
          eventUpdated.addAll(updateEventsStartDate(event, series, newStartDateTime));
        } else {
          eventUpdated.add(updateSeriesStartDate(event, series, newStartDateTime));
        }
        break;
      case "end":
        LocalDateTime newEndDateTime = LocalDateTime.parse(newValue);
        if (commandType.equals("events")) {
          eventUpdated.add(updateEventsEndDate(event, series, newEndDateTime));
        } else {
          eventUpdated.add(updateSeriesEndDate(event, series, newEndDateTime));
        }
        break;
      case "description":
        if (commandType.equals("events")) {
          eventUpdated.add(updateEventsDescription(event, series, newValue));
        } else {
          eventUpdated.add(updateSeriesDescription(event, series, newValue));
        }
        break;
      case "location":
        if (!checkRightLocation(newValue)) {
          throw new IllegalArgumentException("Invalid Location value: " + newValue);
        }
        if (commandType.equals("events")) {
          eventUpdated.add(updateEventsLocation(event, series, Location.getLocation(newValue)));
        } else {
          eventUpdated.add(updateSeriesLocation(event, series, Location.getLocation(newValue)));
        }
        break;
      case "status":
        if (!checkRightStatus(newValue)) {
          throw new IllegalArgumentException("Invalid status value: " + newValue);
        }
        if (commandType.equals("events")) {
          eventUpdated.add(updateEventsStatus(event, series, Status.getStatus(newValue)));
        } else {
          eventUpdated.add(updateSeriesStatus(event, series, Status.getStatus(newValue)));
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid input: unsupported property");
    }

    if (series != null) {
      edit(series, eventUpdated);
    } else {
      edit(event, eventUpdated);
    }
  }

  @Override
  public List<ISingleEvent> query(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end dates cannot be null.");
    } else if (end.isBefore(start)) {
      throw new IllegalArgumentException("End date cannot be before the starting date.");
    }

    List<ISingleEvent> queriedEvents = new LinkedList<>();
    for (CalendarEvent event : events) {
      List<ISingleEvent> singleEvents = event.getEventsInRange(start, end);
      queriedEvents.addAll(singleEvents);
    }

    queriedEvents.sort(new StartDateTimeComparator(new LexicographicalComparator()));
    return queriedEvents;
  }

  @Override
  public ISingleEvent getSingleEventWithStartAndEndDate(String subject, LocalDateTime startDateTime,
                                                       LocalDateTime endDateTime) {
    for (CalendarEvent event : events) {
      ISingleEvent result = event.getCorrespondingEvent(subject, startDateTime, endDateTime);
      if (result != null) {
        return result;
      }
    }
    throw new IllegalArgumentException("Event does not exist in the calendar.");
  }

  @Override
  public boolean canAddSingleEvent(ISingleEvent newEvent) {
    if (newEvent == null) {
      return false;
    } else {
      for (CalendarEvent event : events) { // This checks all event series in our calendar
        if (event.matchesWith(newEvent)) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public IEventSeries isInSeries(ISingleEvent original) {
    for (CalendarEvent event : events) {
      if (event.getSeriesIfFound(original) != null) {
        return event.getSeriesIfFound(original);
      }
    }
    return null;
  }

  @Override
  public boolean containsTime(LocalDateTime dateTime) {
    for (CalendarEvent event : events) {
      if (event.containsTime(dateTime)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ISingleEvent> getSingleEventsWithStartDate(String subject,
                                                        LocalDateTime startDateTime) {
    List<ISingleEvent> listOfEvents = new ArrayList<>();
    for (CalendarEvent event : events) {
      if (event.getCorrespondingEventFromStartDate(subject, startDateTime) != null) {
        listOfEvents.add(event.getCorrespondingEventFromStartDate(subject, startDateTime));
      }
    }
    return listOfEvents;
  }

  /**
   * Edits a current event in the calendar, replacing it with a new event or a list of new events
   * if that does not conflict with any existing events.
   *
   * @param currentEvent the current event to be edited.
   * @param newEvents    the new event(s) to be used in place of the current event(s).
   */
  private void edit(CalendarEvent currentEvent, List<CalendarEvent> newEvents) {
    if (events.contains(currentEvent)) {
      Set<CalendarEvent> uniqueEvents = new HashSet<>(newEvents);
      if (uniqueEvents.size() != newEvents.size()) {
        throw new IllegalArgumentException("Duplicate events found in the new events list.");
      }
      events.remove(currentEvent);
      for (CalendarEvent event : newEvents) {
        if (!event.canAddToCalendar(this)) {
          events.add(currentEvent);
          throw new IllegalArgumentException("New event cannot be added to the calendar,"
                  + " due to a time or name conflict.");
        }
      }
      events.addAll(newEvents);
    } else {
      throw new IllegalArgumentException("Event does not exist in the calendar.");
    }
  }

  private CalendarEvent updateEventSubject(ISingleEvent original, IEventSeries series,
                                           String subject) {
    if (series == null) {
      return original.createNewEventWithSubject(subject);
    }
    List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
    int index = newEvents.indexOf(original);
    newEvents.remove(index);
    newEvents.add(index, original.createNewEventWithSubject(subject));

    return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays());

  }

  private List<CalendarEvent> updateEventStartDate(ISingleEvent original, IEventSeries series,
                                                   LocalDateTime startDate) {
    if (series == null) {
      return List.of(original.createNewEventWithNewStartDate(startDate));
    } else {
      List<CalendarEvent> updatedEvents = new ArrayList<>();
      if (startDate.equals(original.getStartDateTime())) {
        updatedEvents.add(series);
        return updatedEvents;
      }
      List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
      newEvents.remove(original);
      updatedEvents.add(original.createNewEventWithNewStartDate(startDate));
      updatedEvents.add(new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays()));
      return updatedEvents;
    }
  }


  private List<CalendarEvent> updateEventEndDate(ISingleEvent original, IEventSeries series,
                                                 LocalDateTime endDate) {
    List<CalendarEvent> result = new ArrayList<>();
    if (series == null) {
      result.add(original.createNewEventWithNewEndDate(endDate));
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
      if (endDate.toLocalDate().equals(original.getEndDateTime().toLocalDate())) {
        int index = newEvents.indexOf(original);
        newEvents.remove(index);
        newEvents.add(index, original.createNewEventWithNewEndDate(endDate));
        result.add(new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
                series.getOccurringDays()));
      } else {
        newEvents.remove(original);
        if (!newEvents.isEmpty()) {
          result.add(new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
                  series.getOccurringDays()));
        }
        result.add(original.createNewEventWithNewEndDate(endDate));
      }
    }
    return result;
  }


  private CalendarEvent updateEventDescription(ISingleEvent original, IEventSeries series,
                                               String description) {
    if (series == null) {
      return original.createNewEventWithNewDescription(description);
    }
    List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
    int index = newEvents.indexOf(original);
    newEvents.remove(index);
    newEvents.add(index, original.createNewEventWithNewDescription(description));

    return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays());
  }

  private CalendarEvent updateEventLocation(ISingleEvent original, IEventSeries series,
                                            Location location) {
    if (series == null) {
      return original.createNewEventWithNewLocation(location);
    }
    List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
    int index = newEvents.indexOf(original);
    newEvents.remove(index);
    newEvents.add(index, original.createNewEventWithNewLocation(location));

    return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays());
  }

  private CalendarEvent updateEventStatus(ISingleEvent original, IEventSeries series,
                                          Status status) {
    if (series == null) {
      return original.createNewEventWithNewStatus(status);
    }
    List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
    int index = newEvents.indexOf(original);
    newEvents.remove(index);
    newEvents.add(index, original.createNewEventWithNewStatus(status));

    return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays());
  }

  private CalendarEvent updateEventsSubject(ISingleEvent original,
                                            IEventSeries series, String subject) {
    if (series == null) {
      return original.createNewEventWithSubject(subject);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>(series.getEvents());
      int index = newEvents.indexOf(original);
      newEvents.remove(index);
      newEvents.add(index, original.createNewEventWithSubject(subject));
      LocalDateTime afterDateTime = original.getStartDateTime();
      for (int i = newEvents.size() - 1; i >= 0; i--) {
        ISingleEvent event = newEvents.get(i);
        if (event.getStartDateTime().isAfter(afterDateTime)) {
          newEvents.remove(event);
          newEvents.add(event.createNewEventWithSubject(subject));
        }
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private List<CalendarEvent> updateEventsStartDate(ISingleEvent original, IEventSeries series,
                                                    LocalDateTime newStartDateTime) {
    if (series == null) {
      return List.of(original.createNewEventWithNewStartDate(newStartDateTime));
    }

    if (newStartDateTime.toLocalDate().equals(original.getStartDateTime().toLocalDate())) {
      return updateTimeOnSameDay(original, series, newStartDateTime);
    }

    return updateToDifferentDay(original, series, newStartDateTime);
  }

  private CalendarEvent updateEventsEndDate(ISingleEvent original, IEventSeries series,
                                            LocalDateTime endDate) {
    if (series == null) {
      return original.createNewEventWithNewEndDate(endDate);
    } else {
      if (!endDate.toLocalDate().equals(original.getStartDateTime().toLocalDate())) {
        throw new IllegalArgumentException("End date must be on the same day as the event's"
                + "start date!");
      }
      List<ISingleEvent> newEvents = new ArrayList<>();

      for (ISingleEvent event : series.getEvents()) {
        if (event.getStartDateTime().isBefore(original.getStartDateTime())) {
          newEvents.add(event);
        } else {
          LocalDateTime newEndTime = LocalDateTime.of(
                  event.getEndDateTime().toLocalDate(),
                  endDate.toLocalTime());
          newEvents.add(event.createNewEventWithNewEndDate(newEndTime));
        }
      }

      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateEventsDescription(ISingleEvent original, IEventSeries series,
                                                String description) {
    if (series == null) {
      return original.createNewEventWithNewDescription(description);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      LocalDateTime afterDateTime = original.getStartDateTime();

      for (ISingleEvent event : series.getEvents()) {
        if (event.getStartDateTime().isBefore(afterDateTime)) {
          newEvents.add(event);
        } else {
          newEvents.add(event.createNewEventWithNewDescription(description));
        }
      }

      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateEventsLocation(ISingleEvent original, IEventSeries series,
                                             Location location) {
    if (series == null) {
      return original.createNewEventWithNewLocation(location);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      LocalDateTime afterDateTime = original.getStartDateTime();

      for (ISingleEvent event : series.getEvents()) {
        if (event.getStartDateTime().isBefore(afterDateTime)) {
          newEvents.add(event);
        } else {
          newEvents.add(event.createNewEventWithNewLocation(location));
        }
      }

      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateEventsStatus(ISingleEvent original, IEventSeries series,
                                           Status status) {
    if (series == null) {
      return original.createNewEventWithNewStatus(status);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      LocalDateTime afterDateTime = original.getStartDateTime();

      for (ISingleEvent event : series.getEvents()) {
        if (event.getStartDateTime().isBefore(afterDateTime)) {
          newEvents.add(event);
        } else {
          newEvents.add(event.createNewEventWithNewStatus(status));
        }
      }

      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }


  private List<CalendarEvent> updateTimeOnSameDay(ISingleEvent original, IEventSeries series,
                                                  LocalDateTime newStartDateTime) {
    Duration durationDiff = Duration.between(original.getStartDateTime(), newStartDateTime);
    List<ISingleEvent> newEvents = new ArrayList<>();
    List<ISingleEvent> eventsToUpdate = new ArrayList<>();

    for (ISingleEvent event : series.getEvents()) {
      if (event.getStartDateTime().isBefore(original.getStartDateTime())) {
        newEvents.add(event);
      } else if (event.equals(original)) {
        LocalDateTime newEndDateTime = original.getEndDateTime().plus(durationDiff);
        eventsToUpdate.add(original.createNewEventWithNewStartDateAndEndDate(newStartDateTime,
                newEndDateTime));
      } else {
        LocalDateTime updatedStart = event.getStartDateTime().plusNanos(durationDiff.toNanos());
        LocalDateTime updatedEnd = event.getEndDateTime().plusNanos(durationDiff.toNanos());
        eventsToUpdate.add(event.createNewEventWithNewStartDateAndEndDate(updatedStart,
                updatedEnd));
      }
    }

    return List.of(new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
                    series.getOccurringDays()),
            new EventSeries(eventsToUpdate, series.getEndDateTime().toLocalDate(),
                    series.getOccurringDays()));
  }


  private List<CalendarEvent> updateToDifferentDay(ISingleEvent original, IEventSeries series,
                                                   LocalDateTime newStartDateTime) {
    List<CalendarEvent> updatedEvents = new ArrayList<>();
    List<ISingleEvent> beforeEvents = new ArrayList<>();
    List<ISingleEvent> afterEvents = new ArrayList<>();
    // Adjust to valid occurring day if needed
    LocalDateTime adjustedStartDateTime = adjustToOccurringDay(newStartDateTime,
            series.getOccurringDays());

    // Separate events into before and after the modified event
    for (ISingleEvent event : series.getEvents()) {
      if (event.getStartDateTime().isBefore(original.getStartDateTime())) {
        if (event.getStartDateTime().isBefore(adjustedStartDateTime)) {
          beforeEvents.add(event);
        }
      } else if (!event.equals(original)) {
        afterEvents.add(event);
      }
    }
    // Add first series if events exist before the changed one
    if (!beforeEvents.isEmpty()) {
      updatedEvents.add(new EventSeries(beforeEvents,
              beforeEvents.get(beforeEvents.size() - 1).getEndDateTime().toLocalDate(),
              series.getOccurringDays()));
    }

    // Create new series with adjusted events
    Duration eventDuration = Duration.between(original.getStartDateTime(),
            original.getEndDateTime());
    LocalDateTime adjustedEndDateTime = adjustedStartDateTime.plus(eventDuration);

    List<ISingleEvent> newSeriesEvents = new ArrayList<>();
    newSeriesEvents.add(original.createNewEventWithNewStartDateAndEndDate(adjustedStartDateTime,
            adjustedEndDateTime));

    // Calculate pattern for remaining events
    if (!afterEvents.isEmpty()) {
      LocalDateTime newSeriesStartDate = adjustedStartDateTime.toLocalDate().atStartOfDay()
              .plusDays(1);

      for (ISingleEvent event : afterEvents) {
        LocalDateTime nextDate = findNextOccurringDate(
                newSeriesStartDate,
                event.getStartDateTime(),
                series.getOccurringDays());

        // Keep original time but use the new date
        LocalDateTime nextStart = LocalDateTime.of(nextDate.toLocalDate(),
                newStartDateTime.toLocalTime());
        LocalDateTime nextEnd = LocalDateTime.of(nextDate.toLocalDate(),
                newStartDateTime.plus(eventDuration).toLocalTime());

        newSeriesEvents.add(event.createNewEventWithNewStartDateAndEndDate(nextStart, nextEnd));
        newSeriesStartDate = nextDate.plusDays(1);
      }
    }

    updatedEvents.add(new EventSeries(newSeriesEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays()));

    return updatedEvents;
  }


  private LocalDateTime findNextOccurringDate(LocalDateTime startDate, LocalDateTime originalDate,
                                              DayOfWeek[] occurringDays) {

    LocalDateTime currentDate = startDate;
    while (true) {
      for (DayOfWeek day : occurringDays) {
        if (currentDate.getDayOfWeek() == day) {
          return currentDate;
        }
      }
      currentDate = currentDate.plusDays(1);
    }
  }

  private LocalDateTime adjustToOccurringDay(LocalDateTime dateTime, DayOfWeek[] occurringDays) {
    if (occurringDays.length == 0) {
      return dateTime;
    }

    // Check if the date is already on an occurring day
    for (DayOfWeek day : occurringDays) {
      if (dateTime.getDayOfWeek() == day) {
        return dateTime;
      }
    }

    // Find next occurring day
    LocalDateTime adjusted = dateTime;
    while (true) {
      adjusted = adjusted.plusDays(1);
      for (DayOfWeek day : occurringDays) {
        if (adjusted.getDayOfWeek() == day) {
          return adjusted;
        }
      }
    }
  }

  private CalendarEvent updateSeriesSubject(ISingleEvent original, IEventSeries series,
                                            String subject) {
    if (series == null) {
      return original.createNewEventWithSubject(subject);
    }
    List<ISingleEvent> newEvents = new ArrayList<>();
    for (ISingleEvent event : series.getEvents()) {
      newEvents.add(event.createNewEventWithSubject(subject));
    }

    return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
            series.getOccurringDays());
  }

  private CalendarEvent updateSeriesStartDate(ISingleEvent original, IEventSeries series,
                                              LocalDateTime startDate) {
    if (series == null) {
      return original.createNewEventWithNewStartDate(startDate);
    } else {
      if (!startDate.toLocalDate().equals(original.getStartDateTime().toLocalDate())) {
        throw new IllegalArgumentException("Cannot change startDate to a different day, "
                + "use the 'edit events' command instead!");
      }
      List<ISingleEvent> newEvents = new ArrayList<>();
      for (ISingleEvent event : series.getEvents()) {
        newEvents.add(event.createNewEventWithNewStartDate(LocalDateTime.of(
                event.getStartDateTime().toLocalDate(), startDate.toLocalTime())));
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateSeriesEndDate(ISingleEvent event, IEventSeries series,
                                            LocalDateTime newEndDateTime) {
    if (series == null) {
      return event.createNewEventWithNewEndDate(newEndDateTime);
    } else {
      if (!newEndDateTime.toLocalDate().equals(event.getStartDateTime().toLocalDate())) {
        throw new IllegalArgumentException("Cannot change end date to a different day in an"
                + " event series");
      }
      List<ISingleEvent> newEvents = new ArrayList<>();
      for (ISingleEvent e : series.getEvents()) {
        newEvents.add(e.createNewEventWithNewEndDate(LocalDateTime.of(
                e.getEndDateTime().toLocalDate(), newEndDateTime.toLocalTime())));
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateSeriesDescription(ISingleEvent original, IEventSeries series,
                                                String description) {
    if (series == null) {
      return original.createNewEventWithNewDescription(description);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      for (ISingleEvent event : series.getEvents()) {
        newEvents.add(event.createNewEventWithNewDescription(description));
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateSeriesLocation(ISingleEvent original, IEventSeries series,
                                             Location location) {
    if (series == null) {
      return original.createNewEventWithNewLocation(location);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      for (ISingleEvent event : series.getEvents()) {
        newEvents.add(event.createNewEventWithNewLocation(location));
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private CalendarEvent updateSeriesStatus(ISingleEvent original, IEventSeries series,
                                           Status status) {
    if (series == null) {
      return original.createNewEventWithNewStatus(status);
    } else {
      List<ISingleEvent> newEvents = new ArrayList<>();
      for (ISingleEvent event : series.getEvents()) {
        newEvents.add(event.createNewEventWithNewStatus(status));
      }
      return new EventSeries(newEvents, series.getEndDateTime().toLocalDate(),
              series.getOccurringDays());
    }
  }

  private boolean checkRightStatus(String status) {
    status = status.toLowerCase();
    return status.equals("public") || status.equals("private");
  }

  private boolean checkRightLocation(String location) {
    location = location.toLowerCase();
    return location.equals("online") || location.equals("physical");
  }

}

