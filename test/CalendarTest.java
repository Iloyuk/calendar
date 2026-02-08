import org.junit.Before;
import org.junit.Test;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.Calendar;
import model.calendar.ICalendar;
import model.event.EventSeries;
import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.SingleEvent;
import model.event.Location;
import model.event.Status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Test class for verifying the correctness of our {@code Calendar} object.
 */
public class CalendarTest {
  private ICalendar calendar;
  private ISingleEvent testSingleEvent;
  private IEventSeries testEventSeries;
  private final LocalDateTime testStart = LocalDateTime.of(2023, 10,
          1, 10, 0);
  private final LocalDateTime testEnd = LocalDateTime.of(2023, 10, 1,
          11, 0);

  @Before
  public void setup() {
    calendar = new Calendar();
    testSingleEvent = new SingleEvent.Builder("Test Event",
            testStart, testEnd)
            .build();
    testEventSeries = new EventSeries(testSingleEvent, 5, DayOfWeek.SUNDAY);

  }

  @Test
  public void testAddSingleEventToEmptyCalendar() {
    ISingleEvent event = new SingleEvent.Builder("Meeting", testStart, testEnd).build();
    calendar.addEvent(event);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(1, events.size());
    assertEquals(event, events.get(0));
  }

  @Test
  public void testQueryRejectsInvalidDateRange() {
    LocalDateTime start = LocalDateTime.of(2025, 5, 1, 0, 0, 0);
    LocalDateTime end = LocalDateTime.of(2025, 4, 1, 0, 0, 0);
    assertThrows(IllegalArgumentException.class, () -> calendar.query(start, end));
  }

  @Test
  public void testAddEventSeriesToEmptyCalendar() {
    IEventSeries series = new EventSeries(testSingleEvent, 3, DayOfWeek.MONDAY);
    calendar.addEvent(series);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(3, events.size());
    for (ISingleEvent event : series.getEvents()) {
      assertTrue(events.contains(event));
    }
  }

  @Test
  public void testAddSingleEventWithExistingNonConflictingEvents() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent newEvent = new SingleEvent.Builder("New Meeting",
            testStart.plusDays(1), testEnd.plusDays(1)).build();
    calendar.addEvent(newEvent);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(2, events.size());
    assertTrue(events.contains(testSingleEvent));
    assertTrue(events.contains(newEvent));
  }

  @Test
  public void testAddEventSeriesWithExistingNonConflictingEvents() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent baseEvent = new SingleEvent.Builder("Team Sync",
            testStart.plusDays(2), testEnd.plusDays(2)).build();
    IEventSeries series = new EventSeries(baseEvent, 3, DayOfWeek.WEDNESDAY);
    calendar.addEvent(series);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(4, events.size());
    assertTrue(events.contains(testSingleEvent));
    for (ISingleEvent event : series.getEvents()) {
      assertTrue(events.contains(event));
    }
  }

  @Test
  public void testAddDuplicateSingleEvent() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent duplicate = new SingleEvent.Builder("Test Event", testStart, testEnd).build();
    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(duplicate));
  }

  @Test
  public void testAddSingleEventConflictingWithSeriesOccurrence() {
    IEventSeries series = new EventSeries(testSingleEvent, 3, DayOfWeek.SUNDAY);
    calendar.addEvent(series);

    List<ISingleEvent> seriesEvents = series.getEvents();
    ISingleEvent conflict = new SingleEvent.Builder("Test Event",
            seriesEvents.get(0).getStartDateTime(),
            seriesEvents.get(0).getEndDateTime()).build();

    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(conflict));
  }

  @Test
  public void testAddSeriesConflictingWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    IEventSeries series = new EventSeries(testSingleEvent, 3, testStart.getDayOfWeek());
    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(series));
  }

  @Test
  public void testAddSeriesConflictingWithAnotherSeries() {
    IEventSeries series1 = new EventSeries(testSingleEvent, 3, DayOfWeek.SUNDAY);
    calendar.addEvent(series1);

    IEventSeries series2 = new EventSeries(testSingleEvent, 2, DayOfWeek.SUNDAY);
    assertThrows(IllegalArgumentException.class, () -> calendar.addEvent(series2));
  }

  @Test
  public void testAddSingleEventSameTimeButDifferentSubject() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent differentSubject = new SingleEvent.Builder("Different Subject",
            testStart, testEnd).build();
    calendar.addEvent(differentSubject);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(2, events.size());
    assertTrue(events.contains(testSingleEvent));
    assertTrue(events.contains(differentSubject));
  }

  @Test
  public void testAddSingleEventSameSubjectButDifferentTime() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent differentTime = new SingleEvent.Builder("Test Event",
            testStart.plusDays(1), testEnd.plusDays(1)).build();
    calendar.addEvent(differentTime);

    List<ISingleEvent> events = calendar.query(LocalDateTime.MIN, LocalDateTime.MAX);
    assertEquals(2, events.size());
    assertTrue(events.contains(testSingleEvent));
    assertTrue(events.contains(differentTime));
  }

  @Test
  public void testAddNullEvent() {
    assertThrows(NullPointerException.class, () -> calendar.addEvent(null));
  }

  @Test
  public void testAddEventSeriesWithNoOccurrences() {
    assertThrows(IllegalArgumentException.class, () ->
            new EventSeries(testSingleEvent, 0, DayOfWeek.MONDAY));

    assertEquals(0, calendar.query(LocalDateTime.MIN, LocalDateTime.MAX).size());
  }


  @Test
  public void testQueryEmptyCalendar() {
    List<ISingleEvent> events = calendar.query(testStart, testEnd);
    assertNotNull(events);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testQueryWithSingleEventInRange() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> events = calendar.query(testStart.minusHours(1), testEnd.plusHours(1));
    assertEquals(1, events.size());
    assertEquals(testSingleEvent, events.get(0));
  }

  @Test
  public void testQueryWithSingleEventOutsideRange() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> events = calendar.query(testStart.plusDays(1), testEnd.plusDays(1));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testQueryWithEventSeriesPartiallyInRange() {
    IEventSeries series = new EventSeries(testSingleEvent, 5, DayOfWeek.SUNDAY);
    calendar.addEvent(series);

    LocalDateTime rangeStart = testStart;
    LocalDateTime rangeEnd = testStart.plusWeeks(2);

    List<ISingleEvent> events = calendar.query(rangeStart, rangeEnd);

    assertTrue(!events.isEmpty() && events.size() < 5);
    for (ISingleEvent event : events) {
      assertTrue(!event.getStartDateTime().isBefore(rangeStart)
              && !event.getEndDateTime().isAfter(rangeEnd)
      );
    }
  }

  @Test
  public void testQueryWithMultipleEvents() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent event2 = new SingleEvent.Builder("Second Event",
            testStart.plusHours(2), testEnd.plusHours(2)).build();
    calendar.addEvent(event2);

    ISingleEvent eventOutside = new SingleEvent.Builder("Outside Range",
            testStart.plusDays(10), testEnd.plusDays(10)).build();
    calendar.addEvent(eventOutside);

    List<ISingleEvent> events = calendar.query(testStart.minusHours(1), testEnd.plusHours(3));
    assertEquals(2, events.size());
    assertTrue(events.contains(testSingleEvent));
    assertTrue(events.contains(event2));
    assertFalse(events.contains(eventOutside));
  }

  @Test
  public void testQueryWithRangeStartEqualsEventEnd() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> events = calendar.query(testEnd, testEnd.plusHours(1));
    assertTrue(events.isEmpty());
  }

  @Test
  public void testQueryWithRangeEndEqualsEventStart() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> events = calendar.query(testStart.minusHours(1), testStart);
    assertTrue(events.isEmpty());
  }

  @Test
  public void testQueryEntireEventSeries() {
    calendar.addEvent(testEventSeries);

    List<ISingleEvent> events = calendar.query(
            testStart.minusDays(1),
            testStart.plusWeeks(6));

    assertEquals(5, events.size());
    for (ISingleEvent event : testEventSeries.getEvents()) {
      assertTrue(events.contains(event));
    }
  }

  @Test
  public void testQuerySpecificSeriesOccurrence() {
    calendar.addEvent(testEventSeries);
    ISingleEvent thirdOccurrence = testEventSeries.getEvents().get(2);

    List<ISingleEvent> events = calendar.query(
            thirdOccurrence.getStartDateTime(),
            thirdOccurrence.getEndDateTime());

    assertEquals(1, events.size());
    assertEquals(thirdOccurrence, events.get(0));
  }

  @Test
  public void testQueryWithTwoOverlappingSeries() {
    calendar.addEvent(testEventSeries);

    ISingleEvent baseEvent = new SingleEvent.Builder("Other Series",
            testStart.plusDays(3), testEnd.plusDays(3)).build();
    IEventSeries secondSeries = new EventSeries(baseEvent, 3, DayOfWeek.WEDNESDAY);
    calendar.addEvent(secondSeries);

    List<ISingleEvent> events = calendar.query(testStart, testStart.plusWeeks(3));

    assertEquals(6, events.size());
  }

  @Test
  public void testQueryWithMixedEventsAtBoundary() {
    calendar.addEvent(testEventSeries);
    ISingleEvent lastSeriesEvent = testEventSeries.getEvents().get(4);
    ISingleEvent boundaryEvent = new SingleEvent.Builder("Event",
            lastSeriesEvent.getEndDateTime(),
            lastSeriesEvent.getEndDateTime().plusHours(1)).build();
    calendar.addEvent(boundaryEvent);

    List<ISingleEvent> events = calendar.query(
            lastSeriesEvent.getStartDateTime(),
            boundaryEvent.getEndDateTime());

    assertEquals(2, events.size());
    assertTrue(events.contains(lastSeriesEvent));
    assertTrue(events.contains(boundaryEvent));
  }

  @Test
  public void testGetSingleEventExisting() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent result = calendar.getSingleEventWithStartAndEndDate(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());

    assertEquals(testSingleEvent, result);
  }

  @Test
  public void testGetSingleEventNotFound() {
    calendar.addEvent(testSingleEvent);

    assertThrows(IllegalArgumentException.class, () -> calendar.getSingleEventWithStartAndEndDate(
            "Wrong Subject",
            testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testGetSingleEventFromSeries() {
    calendar.addEvent(testEventSeries);
    ISingleEvent seriesEvent = testEventSeries.getEvents().get(0);

    ISingleEvent result = calendar.getSingleEventWithStartAndEndDate(
            seriesEvent.getSubject(),
            seriesEvent.getStartDateTime(),
            seriesEvent.getEndDateTime());

    assertEquals(seriesEvent, result);
  }

  @Test
  public void testGetSingleEventWithNullSubject() {
    calendar.addEvent(testSingleEvent);

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate(null,
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime())
    );
  }

  @Test
  public void testGetSingleEventWithMultipleSimilarEvents() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent sameSubjectDifferentTime = new SingleEvent.Builder(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime().plusDays(1),
            testSingleEvent.getEndDateTime().plusDays(1))
            .build();
    calendar.addEvent(sameSubjectDifferentTime);

    ISingleEvent result = calendar.getSingleEventWithStartAndEndDate(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());

    assertEquals(testSingleEvent, result);
  }

  @Test
  public void testIsInSeriesWithEventFromSeries() {
    calendar.addEvent(testEventSeries);
    ISingleEvent eventFromSeries = testEventSeries.getEvents().get(2);

    IEventSeries result = calendar.isInSeries(eventFromSeries);

    assertEquals(testEventSeries, result);
  }

  @Test
  public void testIsInSeriesWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    IEventSeries result = calendar.isInSeries(testSingleEvent);

    assertNull(result);
  }

  @Test
  public void testIsInSeriesWithEventFromMultipleSeries() {
    calendar.addEvent(testEventSeries);

    ISingleEvent baseEvent = new SingleEvent.Builder("Tuesday Meeting",
            testStart.plusDays(1), testEnd.plusDays(1)).build();
    IEventSeries anotherSeries = new EventSeries(baseEvent, 3, DayOfWeek.TUESDAY);
    calendar.addEvent(anotherSeries);

    ISingleEvent eventFromFirstSeries = testEventSeries.getEvents().get(0);
    IEventSeries result = calendar.isInSeries(eventFromFirstSeries);

    assertEquals(testEventSeries, result);
  }

  @Test
  public void testIsInSeriesWithNonExistingEvent() {
    calendar.addEvent(testEventSeries);

    ISingleEvent nonExistingEvent = new SingleEvent.Builder("Not in series",
            testStart.plusDays(10), testEnd.plusDays(10)).build();

    IEventSeries result = calendar.isInSeries(nonExistingEvent);

    assertNull(result);
  }

  @Test
  public void testIsInSeriesWithNullEvent() {
    assertNull(calendar.isInSeries(null));
  }


  @Test
  public void testContainsTimeWithinEvent() {
    LocalDateTime eventStart = LocalDateTime.of(2023, 10, 15, 14, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 10, 15, 16, 0);
    ISingleEvent event = new SingleEvent.Builder("Meeting", eventStart, eventEnd).build();
    calendar.addEvent(event);

    LocalDateTime timeToCheck = LocalDateTime.of(2023, 10, 15, 15, 0);
    assertTrue(calendar.containsTime(timeToCheck));
  }

  @Test
  public void testContainsTimeOutsideEvents() {
    LocalDateTime eventStart = LocalDateTime.of(2023, 10, 15, 14, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 10, 15, 16, 0);
    ISingleEvent event = new SingleEvent.Builder("Meeting", eventStart, eventEnd).build();
    calendar.addEvent(event);

    LocalDateTime timeToCheck = LocalDateTime.of(2023, 10, 15, 17, 0);
    assertFalse(calendar.containsTime(timeToCheck));
  }

  @Test
  public void testContainsTimeWithMultipleEvents() {
    LocalDateTime eventStart1 = LocalDateTime.of(2023, 10, 15, 10, 0);
    LocalDateTime eventEnd1 = LocalDateTime.of(2023, 10, 15, 12, 0);
    ISingleEvent event1 = new SingleEvent.Builder("Meeting 1", eventStart1, eventEnd1).build();

    LocalDateTime eventStart2 = LocalDateTime.of(2023, 10, 15, 14, 0);
    LocalDateTime eventEnd2 = LocalDateTime.of(2023, 10, 15, 16, 0);
    ISingleEvent event2 = new SingleEvent.Builder("Meeting 2", eventStart2, eventEnd2).build();

    calendar.addEvent(event1);
    calendar.addEvent(event2);

    LocalDateTime timeInEvent1 = LocalDateTime.of(2023, 10, 15, 11, 0);
    LocalDateTime timeInEvent2 = LocalDateTime.of(2023, 10, 15, 15, 0);
    LocalDateTime timeNotInEvent = LocalDateTime.of(2023, 10, 15, 13, 0);

    assertTrue(calendar.containsTime(timeInEvent1));
    assertTrue(calendar.containsTime(timeInEvent2));
    assertFalse(calendar.containsTime(timeNotInEvent));
  }

  @Test
  public void testContainsTimeWithEventSeries() {
    LocalDateTime seriesStart = LocalDateTime.of(2023, 10, 1, 10, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 10, 1, 11, 0);
    ISingleEvent baseEvent = new SingleEvent.Builder("Weekly Meeting",
            seriesStart, seriesEnd).build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);

    calendar.addEvent(series);

    LocalDateTime timeInFirstEvent = LocalDateTime.of(2023, 10, 1, 10, 30);
    assertTrue(calendar.containsTime(timeInFirstEvent));

    LocalDateTime timeInThirdEvent = LocalDateTime.of(2023, 10, 15, 10, 30);
    assertTrue(calendar.containsTime(timeInThirdEvent));

    LocalDateTime timeNotInSeries = LocalDateTime.of(2023, 10, 15, 12, 0);
    assertFalse(calendar.containsTime(timeNotInSeries));
  }

  @Test
  public void testContainsTimeWithEmptyCalendar() {
    LocalDateTime timeToCheck = LocalDateTime.of(2023, 10, 15, 15, 0);
    assertFalse(calendar.containsTime(timeToCheck));
  }

  @Test
  public void testContainsTimeAtEventBoundaries() {
    LocalDateTime eventStart = LocalDateTime.of(2023, 10, 15, 14, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 10, 15, 16, 0);
    ISingleEvent event = new SingleEvent.Builder("Meeting", eventStart, eventEnd).build();
    calendar.addEvent(event);

    assertFalse(calendar.containsTime(eventStart));
    assertFalse(calendar.containsTime(eventEnd));
    assertTrue(calendar.containsTime(eventStart.plusSeconds(1)));
    assertTrue(calendar.containsTime(eventEnd.minusSeconds(1)));
  }

  @Test
  public void testCanAddSingleEventNoExistingEvents() {
    ISingleEvent newEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();

    assertTrue(calendar.canAddSingleEvent(newEvent));
  }

  @Test
  public void testCanAddSingleEventWithMatchingEvent() {
    ISingleEvent existingEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();
    calendar.addEvent(existingEvent);

    ISingleEvent newEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();

    assertFalse(calendar.canAddSingleEvent(newEvent));
  }

  @Test
  public void testCanAddSingleEventWithDifferentSubject() {
    ISingleEvent existingEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();
    calendar.addEvent(existingEvent);

    ISingleEvent newEvent = new SingleEvent.Builder("Different Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();

    assertTrue(calendar.canAddSingleEvent(newEvent));
  }

  @Test
  public void testCanAddSingleEventWithDifferentTime() {
    ISingleEvent existingEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 14, 0),
            LocalDateTime.of(2023, 10, 15, 16, 0)).build();
    calendar.addEvent(existingEvent);

    ISingleEvent newEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 15, 17, 0),
            LocalDateTime.of(2023, 10, 15, 19, 0)).build();

    assertTrue(calendar.canAddSingleEvent(newEvent));
  }

  @Test
  public void testCanAddSingleEventWithMatchingEventInSeries() {
    ISingleEvent baseEvent = new SingleEvent.Builder("Weekly Meeting",
            LocalDateTime.of(2023, 10, 1, 10, 0),
            LocalDateTime.of(2023, 10, 1, 12, 0)).build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);
    calendar.addEvent(series);

    ISingleEvent thirdEvent = series.getEvents().get(2);
    ISingleEvent newEvent = new SingleEvent.Builder(
            thirdEvent.getSubject(),
            thirdEvent.getStartDateTime(),
            thirdEvent.getEndDateTime()).build();

    assertFalse(calendar.canAddSingleEvent(newEvent));
  }

  @Test
  public void testGetSingleEventWithStartDateNoMatches() {
    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate("Test Event", testStart);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetSingleEventWithStartDateSingleMatch() {
    calendar.addEvent(testSingleEvent);
    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate("Test Event", testStart);

    assertEquals(1, result.size());
    assertEquals(testSingleEvent, result.get(0));
  }

  @Test
  public void testGetSingleEventWithStartDateMatchesInSeries() {
    calendar.addEvent(testEventSeries);

    ISingleEvent thirdEvent = testEventSeries.getEvents().get(2);
    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate(
            thirdEvent.getSubject(),
            thirdEvent.getStartDateTime());

    assertEquals(1, result.size());
    assertEquals(thirdEvent, result.get(0));
  }

  @Test
  public void testGetSingleEventWithStartDateMultipleMatches() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent sameStartEvent = new SingleEvent.Builder(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime().plusHours(1)).build();

    calendar.addEvent(new SingleEvent.Builder(
            sameStartEvent.getSubject(),
            sameStartEvent.getStartDateTime(),
            sameStartEvent.getEndDateTime()).build());

    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime());

    assertEquals(2, result.size());
    assertTrue(result.contains(testSingleEvent));
    assertTrue(result.contains(sameStartEvent));
  }

  @Test
  public void testGetSingleEventWithStartDateDifferentSubject() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate(
            "Different Subject",
            testSingleEvent.getStartDateTime());

    assertTrue(result.isEmpty());
  }

  @Test
  public void testGetSingleEventWithStartDateDifferentTime() {
    calendar.addEvent(testSingleEvent);

    List<ISingleEvent> result = calendar.getSingleEventsWithStartDate(
            testSingleEvent.getSubject(),
            testSingleEvent.getStartDateTime().plusHours(1));

    assertTrue(result.isEmpty());
  }

  @Test
  public void testEditSingleEventSubject() {
    ISingleEvent event = new SingleEvent.Builder("Original Subject", testStart, testEnd)
            .build();
    calendar.addEvent(event);

    calendar.editEvent("subject", "Original Subject", testStart, testEnd,
            "New Subject");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("New Subject",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals("New Subject", editedEvent.getSubject());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Original Subject", testStart,
                    testEnd));
  }

  @Test
  public void testEditEventInSeriesSubject() {
    calendar.addEvent(testEventSeries);

    calendar.editEvent("subject", "Test Event", testStart, testEnd,
            "Modified Subject");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Modified Subject",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals("Modified Subject", editedEvent.getSubject());

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
  }

  @Test
  public void testEditSingleEventStartTime() {
    ISingleEvent event = new SingleEvent.Builder("Test Event", testStart, testEnd).build();
    calendar.addEvent(event);

    LocalDateTime newStart = testStart.plusHours(2);
    calendar.editEvent("start", "Test Event", testStart, testEnd,
            newStart.toString());

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            newStart, testEnd.plusHours(2));
    assertNotNull(editedEvent);
    assertEquals(newStart, editedEvent.getStartDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event", testStart, testEnd));
  }

  @Test
  public void testEditEventInSeriesStartTime() {
    calendar.addEvent(testEventSeries);
    LocalDateTime newStart = testStart.plusHours(2);

    calendar.editEvent("start", "Test Event", testStart, testEnd,
            newStart.toString());

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            newStart, testEnd.plusHours(2));
    assertNotNull(editedEvent);
    assertEquals(newStart, editedEvent.getStartDateTime());

    IEventSeries resultSeries = calendar.isInSeries(editedEvent);
    assertNull(resultSeries);

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
  }

  @Test
  public void testEditEventInSeriesStartTimeSameTime() {
    calendar.addEvent(testEventSeries);
    calendar.editEvent("start", "Test Event", testStart, testEnd,
            testStart.toString());
    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals(testStart, editedEvent.getStartDateTime());
    assertEquals(testEventSeries.getEvents().get(0), editedEvent);
    assertEquals(calendar.isInSeries(editedEvent), testEventSeries);
  }

  @Test
  public void testEditSingleEventEndTime() {
    ISingleEvent event = new SingleEvent.Builder("Test Event", testStart, testEnd).build();
    calendar.addEvent(event);

    LocalDateTime newEnd = testEnd.plusHours(2);
    calendar.editEvent("end", "Test Event", testStart, testEnd, newEnd.toString());

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, newEnd);
    assertNotNull(editedEvent);
    assertEquals(newEnd, editedEvent.getEndDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event", testStart, testEnd));
  }

  @Test
  public void testEditEventInSeriesSameDateEndTime() {
    calendar.addEvent(testEventSeries);
    LocalDateTime newEnd = testEnd.plusHours(2);

    calendar.editEvent("end", "Test Event", testStart, testEnd, newEnd.toString());

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, newEnd);
    assertNotNull(editedEvent);
    assertEquals(newEnd, editedEvent.getEndDateTime());
    assertNotNull(calendar.isInSeries(editedEvent));

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
  }

  @Test
  public void testEditEventInSeriesEndTimeToDifferentDay() {
    calendar.addEvent(testEventSeries);
    LocalDateTime newEnd = testEnd.plusDays(1).plusHours(2);

    calendar.editEvent("end", "Test Event", testStart, testEnd, newEnd.toString());

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, newEnd);
    assertNotNull(editedEvent);
    assertEquals(newEnd, editedEvent.getEndDateTime());
    assertNull(calendar.isInSeries(editedEvent));
    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event", testStart, testEnd));
  }

  @Test
  public void testEditSingleEventDescription() {
    calendar.addEvent(testSingleEvent);

    calendar.editEvent("description", "Test Event", testStart,
            testEnd, "Updated description");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals("Updated description", editedEvent.getDescription());
    assertEquals(testStart, editedEvent.getStartDateTime());
    assertEquals(testEnd, editedEvent.getEndDateTime());
  }

  @Test
  public void testEditEventInSeriesDescription() {
    calendar.addEvent(testEventSeries);

    calendar.editEvent("description", "Test Event", testStart,
            testEnd, "Updated description");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals("Updated description", editedEvent.getDescription());

    IEventSeries series = calendar.isInSeries(editedEvent);
    assertNotNull(series);
    assertEquals(testEventSeries.getEvents().get(1), series.getEvents().get(1));

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
    assertNotEquals("Updated description", secondEvent.getDescription());
  }

  @Test
  public void testEditSingleEventLocation() {
    calendar.addEvent(testSingleEvent);

    calendar.editEvent("location", "Test Event", testStart, testEnd,
            "Online");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals(Location.ONLINE, editedEvent.getLocation());
    assertEquals(testStart, editedEvent.getStartDateTime());
    assertEquals(testEnd, editedEvent.getEndDateTime());
  }

  @Test
  public void testEditEventInSeriesLocation() {
    calendar.addEvent(testEventSeries);

    calendar.editEvent("location", "Test Event", testStart, testEnd,
            "Physical");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals(Location.PHYSICAL, editedEvent.getLocation());

    IEventSeries series = calendar.isInSeries(editedEvent);
    assertNotNull(series);

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
    assertNotEquals(Location.PHYSICAL, secondEvent.getLocation());
  }

  @Test
  public void testEditEventLocationInvalidValue() {
    calendar.addEvent(testSingleEvent);

    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvent("location", "Test Event", testStart, testEnd,
                    "wrong"));
  }

  @Test
  public void testEditSingleEventStatus() {
    calendar.addEvent(testSingleEvent);

    calendar.editEvent("status", "Test Event", testStart, testEnd,
            "Public");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals(Status.PUBLIC, editedEvent.getStatus());
  }

  @Test
  public void testEditEventInSeriesStatus() {
    calendar.addEvent(testEventSeries);

    calendar.editEvent("status", "Test Event", testStart, testEnd,
            "Private");

    ISingleEvent editedEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            testStart, testEnd);
    assertNotNull(editedEvent);
    assertEquals(Status.PRIVATE, editedEvent.getStatus());

    IEventSeries series = calendar.isInSeries(editedEvent);
    assertNotNull(series);

    LocalDateTime secondEventStart = testEventSeries.getEvents().get(1).getStartDateTime();
    LocalDateTime secondEventEnd = testEventSeries.getEvents().get(1).getEndDateTime();
    ISingleEvent secondEvent = calendar.getSingleEventWithStartAndEndDate("Test Event",
            secondEventStart, secondEventEnd);
    assertNotNull(secondEvent);
    assertNotEquals(Status.PRIVATE, secondEvent.getStatus());
  }

  @Test
  public void testEditEventStatusInvalidValue() {
    calendar.addEvent(testSingleEvent);

    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvent("status", "Test Event", testStart, testEnd,
                    "Wrong Status"));
  }

  @Test
  public void testEditNonStartPropertyOfEventInSeries() {
    calendar.addEvent(testEventSeries);

    calendar.editEvent("description", "Test Event", testStart, testEnd,
            "Modified description");

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, testEnd);
    assertNotNull(modifiedEvent);
    assertEquals("Modified description", modifiedEvent.getDescription());

    IEventSeries series = calendar.isInSeries(modifiedEvent);
    assertNotNull(series);
  }

  @Test
  public void testEditStartTimeOfEventInSeries() {
    calendar.addEvent(testEventSeries);
    LocalDateTime newStartTime = testStart.plusHours(3);
    calendar.editEvent("start", "Test Event", testStart, testEnd,
            newStartTime.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", newStartTime, testEnd.plusHours(3));
    assertNotNull(modifiedEvent);
    assertNull(calendar.isInSeries(modifiedEvent));

    List<ISingleEvent> remainingEvents = calendar.query(testStart.plusDays(7),
            testStart.plusDays(35));
    assertTrue(remainingEvents.stream().allMatch(e ->
            !e.getStartDateTime().equals(newStartTime) && calendar.isInSeries(e) != null));
  }

  @Test
  public void testEditInvalidProperty() {
    calendar.addEvent(testSingleEvent);

    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvent("Property", "Test Event", testStart, testEnd,
                    "New Value"));
  }

  @Test
  public void testEditNonExistentEvent() {
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvent("subject", "No Event",
                    testStart, testEnd, "New Subject"));
  }

  @Test
  public void testEditToCreateTimeConflict() {
    calendar.addEvent(testSingleEvent);

    ISingleEvent conflictingEvent = new SingleEvent.Builder("Test Event",
            testStart, testEnd.plusHours(2)).build();
    calendar.addEvent(conflictingEvent);

    LocalDateTime newEnd = testEnd.plusHours(2);

    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvent("end", "Test Event", testStart, testEnd,
                    newEnd.toString()));
  }

  @Test
  public void testEditSubjectOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    String newSubject = "NewSeries Subject";
    calendar.editEvents("subject", "Test Event", testStart,
            "series", newSubject);

    for (ISingleEvent event : testEventSeries.getEvents()) {
      ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
              newSubject, event.getStartDateTime(), event.getEndDateTime());
      assertNotNull(modifiedEvent);
      assertEquals(newSubject, modifiedEvent.getSubject());
    }
  }

  @Test
  public void testEditStartTimeOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newStart = testStart.plusDays(1);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("start", "Test Event", testStart,
                    "series", newStart.toString()));
  }

  @Test
  public void testEditStartTimeOfEntireSeriesInValidDate() {
    calendar.addEvent(testEventSeries);

    assertThrows(DateTimeException.class, () ->
            calendar.editEvents("start", "Test Event", testStart,
                    "series",
                    "NotDate"));

  }

  @Test
  public void testEditEndTimeOfEntireSeriesOverADay() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newEnd = testEnd.plusDays(1);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("end", "Test Event", testStart,
                    "series", newEnd.toString()));
  }

  @Test
  public void testEditEndTimeOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newEnd = testEnd.plusHours(1);
    calendar.editEvents("end", "Test Event", testStart, "series",
            newEnd.toString());

    for (ISingleEvent event : testEventSeries.getEvents()) {
      ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
              "Test Event", event.getStartDateTime(), LocalDateTime.of(
                      event.getEndDateTime().toLocalDate(), newEnd.toLocalTime()));
      assertNotNull(modifiedEvent);
      assertEquals(LocalDateTime.of(
                      event.getEndDateTime().toLocalDate(), newEnd.toLocalTime()),
              modifiedEvent.getEndDateTime());
    }
  }

  @Test
  public void testEditDescriptionOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    String newDescription = "Updated Description";
    calendar.editEvents("description",
            "Test Event", testStart, "series", newDescription);

    for (ISingleEvent event : testEventSeries.getEvents()) {
      ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
              "Test Event", event.getStartDateTime(), event.getEndDateTime());
      assertNotNull(modifiedEvent);
      assertEquals(newDescription, modifiedEvent.getDescription());
    }
  }

  @Test
  public void testEditLocationOfEntireSeriesWrongLocation() {
    calendar.addEvent(testEventSeries);

    String newLocation = "Updated Location";
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("location", "Test Event", testStart,
                    "series", newLocation));

  }

  @Test
  public void testEditLocationOfEntireSeriesLocation() {
    calendar.addEvent(testEventSeries);

    String newLocation = "Physical";
    calendar.editEvents("location", "Test Event", testStart, "series",
            newLocation);

    for (ISingleEvent event : testEventSeries.getEvents()) {
      ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
              "Test Event", event.getStartDateTime(), event.getEndDateTime());
      assertNotNull(modifiedEvent);
      assertEquals(Location.PHYSICAL, modifiedEvent.getLocation());
    }
  }

  @Test
  public void testEditStatusOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    String newStatus = "Public";
    calendar.editEvents("status", "Test Event", testStart,
            "series", newStatus);

    for (ISingleEvent event : testEventSeries.getEvents()) {
      ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
              "Test Event", event.getStartDateTime(), event.getEndDateTime());
      assertNotNull(modifiedEvent);
      assertEquals(Status.PUBLIC, modifiedEvent.getStatus());
    }

  }

  @Test
  public void testEditWrongStatusOfEntireSeries() {
    calendar.addEvent(testEventSeries);

    String newStatus = "Wrong Status";
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("status", "Test Event", testStart,
                    "series", newStatus));
  }

  @Test
  public void testEditEventsEventsWithInvalidProperty() {
    calendar.addEvent(testEventSeries);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("invalidProperty", "Test Event", testStart,
                    "events", "New Value"));
  }


  @Test
  public void testEditEventsWithSeriesInvalidNewValue() {
    calendar.addEvent(testEventSeries);
    assertThrows(DateTimeException.class, () ->
            calendar.editEvents("start", "Test Event", testStart,
                    "series", "wrong"));

  }


  @Test
  public void testEditSubjectOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newSubject = "Event Subject";
    calendar.editEvents("subject", "Test Event", testStart, "series",
            newSubject);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            newSubject, testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newSubject, modifiedEvent.getSubject());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditStartTimeOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    LocalDateTime newStart = testStart.plusHours(1);
    calendar.editEvents("start", "Test Event", testStart, "series",
            newStart.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", newStart, testSingleEvent.getEndDateTime().plusHours(1));
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newStart, modifiedEvent.getStartDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditEndTimeOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    LocalDateTime newEnd = testEnd.plusHours(1);
    calendar.editEvents("end", "Test Event", testStart, "series",
            newEnd.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(), newEnd);
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newEnd, modifiedEvent.getEndDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditDescriptionOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newDescription = "Updated Description";
    calendar.editEvents("description", "Test Event", testStart,
            "series", newDescription);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newDescription, modifiedEvent.getDescription());
  }

  @Test
  public void testEditLocationOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newLocation = "Online";
    calendar.editEvents("location", "Test Event", testStart,
            "series", newLocation);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(Location.ONLINE, modifiedEvent.getLocation());
  }

  @Test
  public void testEditStatusOfEntireSeriesSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newStatus = "Private";
    calendar.editEvents("status", "Test Event", testStart,
            "series", newStatus);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(Status.PRIVATE, modifiedEvent.getStatus());
  }

  @Test
  public void testEditEventsWithInvalidPropertyForSingleEvent() {
    calendar.addEvent(testSingleEvent);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("invalidProperty", "Test Event", testStart,
                    "series", "New Value"));
  }

  @Test
  public void testEditSubjectOfEvents() {
    calendar.addEvent(testEventSeries);

    String newSubject = "New Subject";
    calendar.editEvents("subject", "Test Event",
            testStart.plusDays(7), "events", newSubject);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            newSubject, testStart.plusDays(7), testEnd.plusDays(7));

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);

    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(newSubject, event.getSubject());
      } else {
        assertEquals("Test Event", event.getSubject());
      }
    }
  }

  @Test
  public void testEditStartTimeOfEvents() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newStart = testStart.plusDays(7).plusHours(1);
    calendar.editEvents("start", "Test Event",
            testStart.plusDays(7), "events", newStart.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", newStart, testEnd.plusDays(7).plusHours(1));

    ISingleEvent ogEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, testEnd);
    assertNotNull(ogEvent);

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);
    IEventSeries originalSeries = calendar.isInSeries(ogEvent);
    assertNotEquals(originalSeries, newSeries);

    assertEquals(4, newSeries.getEvents().size());
    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(LocalDateTime.of(event.getStartDateTime().toLocalDate(),
                newStart.toLocalTime()), event.getStartDateTime());
      } else {
        assertEquals(testStart, event.getStartDateTime());
      }
    }
  }

  @Test
  public void testEditStartTimeDifferentDayOfEvents() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newStart = testStart.plusDays(8).plusHours(1);
    calendar.editEvents("start", "Test Event",
            testStart.plusDays(7), "events", newStart.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", newStart.plusDays(6), testEnd.plusDays(14).plusHours(1));

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);
    assertEquals(4, newSeries.getEvents().size());
    assertEquals(newStart.plusDays(6), modifiedEvent.getStartDateTime());
    ISingleEvent firstEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart, testEnd);
    assertEquals(firstEvent.getStartDateTime(), testStart);
    assertEquals(newSeries.getEvents().get(0).getStartDateTime(), newStart.plusDays(6));
    assertEquals(newSeries.getEvents().get(1).getStartDateTime(), newStart.plusDays(13));
    assertEquals(newSeries.getEvents().get(2).getStartDateTime(), newStart.plusDays(20));
    assertEquals(newSeries.getEvents().get(3).getStartDateTime(), newStart.plusDays(27));


  }

  @Test
  public void testEditEndTimeOfEvents() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newEnd = testEnd.plusDays(7).plusHours(1);

    calendar.editEvents("end", "Test Event",
            testStart.plusDays(7), "events", newEnd.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart.plusDays(7), newEnd);

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);

    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(LocalDateTime.of(event.getEndDateTime().toLocalDate(),
                newEnd.toLocalTime()), event.getEndDateTime());
      } else {
        assertEquals(testEnd, event.getEndDateTime());
      }
    }
  }

  @Test
  public void testEditEndTimeOfEventsDay() {
    calendar.addEvent(testEventSeries);

    LocalDateTime newEnd = testEnd.plusDays(8).plusHours(1);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event", testStart.plusDays(7),
                    newEnd));

  }

  @Test
  public void testEditDescriptionOfEvents() {
    calendar.addEvent(testEventSeries);

    String newDescription = "Updated Description";
    calendar.editEvents("description", "Test Event",
            testStart.plusDays(7), "events", newDescription);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart.plusDays(7), testEnd.plusDays(7));

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);

    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(newDescription, event.getDescription());
      } else {
        assertNull(event.getDescription());
      }
    }
  }

  @Test
  public void testEditLocationOfEventsWrongLocation() {
    calendar.addEvent(testEventSeries);

    String newLocation = "Location";
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("location", "Test Event",
                    testStart.plusDays(7), "events", newLocation));
  }

  @Test
  public void testEditLocationOfEvents() {
    calendar.addEvent(testEventSeries);

    String newLocation = "Physical";
    calendar.editEvents("location", "Test Event",
            testStart.plusDays(7), "events", newLocation);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart.plusDays(7), testEnd.plusDays(7));

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);

    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(Location.PHYSICAL, event.getLocation());
      } else {
        assertNull(event.getLocation());
      }
    }
  }

  @Test
  public void testEditStatusOfEvents() {
    calendar.addEvent(testEventSeries);

    String newStatus = "Public";
    calendar.editEvents("status", "Test Event",
            testStart.plusDays(7), "events", newStatus);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testStart.plusDays(7), testEnd.plusDays(7));

    IEventSeries newSeries = calendar.isInSeries(modifiedEvent);

    for (ISingleEvent event : newSeries.getEvents()) {
      if (!event.getStartDateTime().isBefore(testStart.plusDays(7))) {
        assertEquals(Status.PUBLIC, event.getStatus());
      } else {
        assertNull(event.getStatus());
      }
    }
  }

  @Test
  public void testEditWrongStatusOfEvents() {
    calendar.addEvent(testEventSeries);

    String newStatus = "Wrong Status";
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("status", "Test Event",
                    testStart.plusDays(7), "events", newStatus));
  }

  @Test
  public void testEditEventsWithInvalidProperty() {
    calendar.addEvent(testEventSeries);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("invalidProperty", "Test Event",
                    testStart.plusDays(7), "events", "New Value"));
  }

  @Test
  public void testEditEventsWithInvalidNewValue() {
    calendar.addEvent(testEventSeries);
    assertThrows(DateTimeException.class, () ->
            calendar.editEvents("start", "Test Event",
                    testStart.plusDays(7), "events", "wrong"));
  }

  @Test
  public void testEditEventsSubjectWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newSubject = "New Subject";
    calendar.editEvents("subject", "Test Event", testStart,
            "events", newSubject);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            newSubject, testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newSubject, modifiedEvent.getSubject());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditEventsStartTimeWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    LocalDateTime newStart = testStart.plusHours(1);
    calendar.editEvents("start", "Test Event", testStart,
            "events", newStart.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", newStart, testSingleEvent.getEndDateTime().plusHours(1));
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newStart, modifiedEvent.getStartDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditEventsEndTimeWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    LocalDateTime newEnd = testEnd.plusHours(1);
    calendar.editEvents("end", "Test Event", testStart,
            "events", newEnd.toString());

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(), newEnd);
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newEnd, modifiedEvent.getEndDateTime());

    assertThrows(IllegalArgumentException.class, () ->
            calendar.getSingleEventWithStartAndEndDate("Test Event",
                    testSingleEvent.getStartDateTime(), testSingleEvent.getEndDateTime()));
  }

  @Test
  public void testEditEventsDescriptionWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newDescription = "Updated Description";
    calendar.editEvents("description", "Test Event", testStart,
            "events", newDescription);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(newDescription, modifiedEvent.getDescription());
  }

  @Test
  public void testEditEventsLocationWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newLocation = "Online";
    calendar.editEvents("location", "Test Event", testStart,
            "events", newLocation);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(Location.ONLINE, modifiedEvent.getLocation());
  }

  @Test
  public void testEditEventsStatusWithSingleEvent() {
    calendar.addEvent(testSingleEvent);

    String newStatus = "Private";
    calendar.editEvents("status", "Test Event", testStart,
            "events", newStatus);

    ISingleEvent modifiedEvent = calendar.getSingleEventWithStartAndEndDate(
            "Test Event", testSingleEvent.getStartDateTime(),
            testSingleEvent.getEndDateTime());
    assertNull(calendar.isInSeries(modifiedEvent));
    assertNotNull(modifiedEvent);
    assertEquals(Status.PRIVATE, modifiedEvent.getStatus());
  }

  @Test
  public void testEditEventsWithInvalidPropertyForSingleEventEvents() {
    calendar.addEvent(testSingleEvent);
    assertThrows(IllegalArgumentException.class, () ->
            calendar.editEvents("invalidProperty", "Test Event", testStart,
                    "events", "New Value"));
  }

}