package model.application;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.calendar.BetterCalendar;
import model.calendar.IBetterCalendar;
import model.calendar.ICalendar;
import model.event.CalendarEvent;
import model.event.IEventSeries;
import model.event.EventSeries;
import model.event.ISingleEvent;
import model.event.comparators.LexicographicalComparator;
import model.event.comparators.StartDateTimeComparator;

/**
 * An implementation of the {@code ICalendarManager} interface that has the ability to manage
 * calendars. The {@code CalendarManager} class gives each calendar a unique name and assigns it
 * a timezone, with the ability to copy events over from one calendar to another.
 */
public class CalendarManager implements ICalendarManager {
  private final Map<String, IBetterCalendar> calendars;
  private String currentCalendarName;

  /**
   * Constructs an {@code Application} object.
   */
  public CalendarManager() {
    this.calendars = new HashMap<>();
  }

  @Override
  public void createCalendar(String calendarName, String timeZone) {
    if (this.calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("Calendar with this name already exists.");
    }
    calendars.put(calendarName, new BetterCalendar(timeZone));
  }

  @Override
  public String getCurrentCalendarName() {
    return currentCalendarName;
  }

  @Override
  public IBetterCalendar getCurrentCalendar() {
    return calendars.get(currentCalendarName);
  }

  @Override
  public void editCalendarTimeZone(String calendarName, String timeZone) {
    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("The calendar to be edited does not exist.");
    }

    IBetterCalendar originalCal = calendars.get(calendarName);
    IBetterCalendar newCal = originalCal.makeCalWithNewTimeZone(timeZone);
    calendars.put(calendarName, newCal);
  }

  @Override
  public void editCalendarName(String oldName, String newName) {
    if (!calendars.containsKey(oldName)) {
      throw new IllegalArgumentException("Could not find calendar to rename!");
    } else if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("New name is already taken!");
    }
    IBetterCalendar calendarToBeRenamed = calendars.remove(oldName);
    calendars.put(newName, calendarToBeRenamed);

    if (oldName.equals(currentCalendarName)) {
      currentCalendarName = newName;
    }
  }

  @Override
  public void setCalendarInUse(String calendarName) {
    if (!calendars.containsKey(calendarName)) {
      throw new IllegalArgumentException("The calendar to be set in use does not exist.");
    }
    currentCalendarName = calendarName;
  }

  @Override
  public void copyEvent(String eventName, LocalDateTime eventStartDateTime,
                        String targetCalendarName, LocalDateTime newStartDateTime) {
    ISingleEvent event = calendars.get(currentCalendarName).getSingleEventsWithStartDate(eventName,
            eventStartDateTime).get(0);
    CalendarEvent eventToCopy = event.createNewEventWithNewStartDateAndEndDate(
            newStartDateTime, newStartDateTime.plus(Duration.between(event.getStartDateTime(),
                    event.getEndDateTime())));
    if (calendars.containsKey(targetCalendarName)) {
      calendars.get(targetCalendarName).addEvent(eventToCopy);
    } else {
      throw new IllegalArgumentException("Could not find target calendar to copy event to!");
    }
  }

  @Override
  public void copyEvents(LocalDateTime start, LocalDateTime end, String targetCalendarName,
                         LocalDate newStartDate) {
    if (!calendars.containsKey(targetCalendarName)) {
      throw new IllegalArgumentException("Could not find target calendar to copy event to!");
    }

    List<ISingleEvent> events = calendars.get(currentCalendarName).query(start, end);

    if (events == null || events.isEmpty()) {
      return;
    }


    List<ISingleEvent> sortedEvents = new ArrayList<>(events);
    sortedEvents.sort(new StartDateTimeComparator(new LexicographicalComparator()));

    Period queryOffset = Period.between(start.toLocalDate(),
            sortedEvents.get(0).getStartDateTime().toLocalDate());

    LocalDate originalFirstDate = sortedEvents.get(0).getStartDateTime().toLocalDate();
    Period dateOffset = Period.between(originalFirstDate, newStartDate);

    ZoneId originalSource = calendars.get(currentCalendarName).getZoneId();
    ZoneId targetZone = calendars.get(targetCalendarName).getZoneId();

    Map<IEventSeries, Boolean> seriesValidity = new HashMap<>();
    Map<IEventSeries, List<ISingleEvent>> newValidSeriesEvents = new HashMap<>();

    for (ISingleEvent originalEvent : sortedEvents) {
      processEvent(originalEvent, dateOffset, calendars.get(targetCalendarName), seriesValidity,
              newValidSeriesEvents, calendars.get(currentCalendarName), originalSource, targetZone,
              queryOffset);
    }

    createSeries(calendars.get(targetCalendarName), newValidSeriesEvents);
  }

  private void processEvent(ISingleEvent originalEvent, Period dateOffset,
                            ICalendar targetCalendarName,
                            Map<IEventSeries, Boolean> seriesValidity,
                            Map<IEventSeries, List<ISingleEvent>> newValidSeriesEvents,
                            IBetterCalendar calendar, ZoneId originalSource, ZoneId targetZone,
                            Period offset) {

    LocalDateTime dateTimeWithDateChanged = originalEvent.getStartDateTime().plus(dateOffset);
    ZonedDateTime sourceZonedDateTime = dateTimeWithDateChanged.atZone(originalSource);
    ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(targetZone);
    LocalDateTime newStartDateTime = targetZonedDateTime.toLocalDateTime();
    newStartDateTime = newStartDateTime.plus(offset);

    Duration eventDuration = Duration.between(originalEvent.getStartDateTime(),
            originalEvent.getEndDateTime());
    LocalDateTime newEndDateTime = newStartDateTime.plus(eventDuration);

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStartDateAndEndDate(newStartDateTime,
            newEndDateTime);

    IEventSeries originalSeries = calendar.isInSeries(originalEvent);

    if (originalSeries == null) {
      targetCalendarName.addEvent(newEvent);
    } else {
      handleSeriesEvent(newEvent, originalEvent, targetCalendarName, seriesValidity,
              newValidSeriesEvents, calendar);
    }
  }

  private void handleSeriesEvent(ISingleEvent newEvent, ISingleEvent originalEvent,
                                 ICalendar targetCalendar,
                                 Map<IEventSeries, Boolean> seriesValidity,
                                 Map<IEventSeries, List<ISingleEvent>> newValidSeriesEvents,
                                 IBetterCalendar calendar) {

    IEventSeries originalSeries = calendar.isInSeries(originalEvent);

    if (!seriesValidity.containsKey(originalSeries)) {
      DayOfWeek originalDay = originalEvent.getStartDateTime().getDayOfWeek();
      DayOfWeek newDay = newEvent.getStartDateTime().getDayOfWeek();
      seriesValidity.put(originalSeries, originalDay.equals(newDay));
    }

    boolean isSeriesValid = seriesValidity.get(originalSeries);
    if (isSeriesValid) {
      if (!newValidSeriesEvents.containsKey(originalSeries)) {
        newValidSeriesEvents.put(originalSeries, new ArrayList<>());
      }
      newValidSeriesEvents.get(originalSeries).add(newEvent);
    } else {
      targetCalendar.addEvent(newEvent);
    }
  }

  private void createSeries(IBetterCalendar targetCalendar, Map<IEventSeries,
          List<ISingleEvent>> newValidSeriesEvents) {
    for (Map.Entry<IEventSeries, List<ISingleEvent>> entry : newValidSeriesEvents.entrySet()) {
      IEventSeries originalSeries = entry.getKey();
      List<ISingleEvent> newEventsForSeries = entry.getValue();
      if (!newEventsForSeries.isEmpty()) {
        IEventSeries newSeries = new EventSeries(newEventsForSeries,
                originalSeries.getEndDateTime().toLocalDate(),
                originalSeries.getOccurringDays());
        targetCalendar.addEvent(newSeries);
      }
    }
  }

}