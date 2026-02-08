import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import model.event.CalendarEvent;
import model.event.ISingleEvent;
import model.event.IEventSeries;
import model.event.SingleEvent;
import model.event.EventSeries;

import model.event.Location;
import model.event.Status;

import model.calendar.ICalendar;
import model.calendar.Calendar;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the correctness of the {@code CalendarEvent} class.
 */
public class CalendarEventTest {
  private LocalDateTime testStart;
  private LocalDateTime testEnd;
  private ISingleEvent testSingleEvent;
  private IEventSeries testEventSeries;
  private ISingleEvent testSingleEventNoDescription;
  private ISingleEvent testSingleEventNoLocation;
  private ISingleEvent testSingleEventNoStatus;
  private IEventSeries testEventSeriesNoDescription;
  private IEventSeries testEventSeriesNoLocation;
  private IEventSeries testEventSeriesNoStatus;
  private ICalendar calendar;

  @Before
  public void setup() {
    calendar = new Calendar();
    testStart = LocalDateTime.of(2023, 10, 15, 10, 0);
    testEnd = LocalDateTime.of(2023, 10, 15, 11, 30);

    testSingleEvent = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Event Description")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    testEventSeries = new EventSeries(testSingleEvent, 4, DayOfWeek.SUNDAY);

    testSingleEventNoDescription = new SingleEvent.Builder("No Description", testStart,
            testEnd)
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    testSingleEventNoLocation = new SingleEvent.Builder("No Location", testStart, testEnd)
            .description("Event Description")
            .status(Status.PUBLIC)
            .build();

    testSingleEventNoStatus = new SingleEvent.Builder("No Status", testStart, testEnd)
            .description("Event Description")
            .location(Location.PHYSICAL)
            .build();

    testEventSeriesNoDescription = new EventSeries(testSingleEventNoDescription, 4,
            DayOfWeek.SUNDAY);
    testEventSeriesNoLocation = new EventSeries(testSingleEventNoLocation, 4,
            DayOfWeek.SUNDAY);
    testEventSeriesNoStatus = new EventSeries(testSingleEventNoStatus, 4,
            DayOfWeek.SUNDAY);
  }

  @Test
  public void testGetStartDateTimeSingleEvent() {
    assertEquals(testStart, testSingleEvent.getStartDateTime());
  }

  @Test
  public void testGetStartDateTimeEventSeries() {
    assertEquals(testStart, testEventSeries.getStartDateTime());
  }

  @Test
  public void testGetEndDateTimeSingleEvent() {
    assertEquals(testEnd, testSingleEvent.getEndDateTime());
  }

  @Test
  public void testGetEndDateTimeEventSeries() {
    assertEquals(testEnd.plusWeeks(3), testEventSeries.getEndDateTime());
  }

  @Test
  public void testGetSubjectSingleEvent() {
    assertEquals("Test Event", testSingleEvent.getSubject());
  }

  @Test
  public void testGetSubjectEventSeries() {
    assertEquals("Test Event", testEventSeries.getSubject());
  }

  @Test
  public void testGetDescriptionSingleEvent() {
    assertEquals("Event Description", testSingleEvent.getDescription());
  }

  @Test
  public void testGetDescriptionEventSeries() {
    assertEquals("Event Description", testEventSeries.getDescription());
  }

  @Test
  public void testGetLocationSingleEvent() {
    assertEquals(Location.PHYSICAL, testSingleEvent.getLocation());
  }

  @Test
  public void testGetLocationEventSeries() {
    assertEquals(Location.PHYSICAL, testEventSeries.getLocation());
  }

  @Test
  public void testGetStatusSingleEvent() {
    assertEquals(Status.PUBLIC, testSingleEvent.getStatus());
  }

  @Test
  public void testGetStatusEventSeries() {
    assertEquals(Status.PUBLIC, testEventSeries.getStatus());
  }

  @Test
  public void testGetDescriptionSingleEventNoDescription() {
    assertNull(testSingleEventNoDescription.getDescription());
  }

  @Test
  public void testGetLocationSingleEventNoLocation() {
    assertNull(testSingleEventNoLocation.getLocation());
  }

  @Test
  public void testGetStatusSingleEventNoStatus() {
    assertNull(testSingleEventNoStatus.getStatus());
  }

  @Test
  public void testGetDescriptionEventSeriesNoDescription() {
    assertNull(testEventSeriesNoDescription.getDescription());
  }

  @Test
  public void testGetLocationEventSeriesNoLocation() {
    assertNull(testEventSeriesNoLocation.getLocation());
  }

  @Test
  public void testGetStatusEventSeriesNoStatus() {
    assertNull(testEventSeriesNoStatus.getStatus());
  }

  @Test
  public void testGetEventsInRangeSingleEventInRange() {
    LocalDateTime start = testStart.minusHours(1);
    LocalDateTime end = testEnd.plusHours(1);

    List<ISingleEvent> events = testSingleEvent.getEventsInRange(start, end);

    assertEquals(1, events.size());
    assertEquals(testSingleEvent, events.get(0));
  }

  @Test
  public void testGetEventsInRangeSingleEventOutOfRange() {
    LocalDateTime start = testStart.plusHours(2);
    LocalDateTime end = testEnd.plusHours(3);

    List<ISingleEvent> events = testSingleEvent.getEventsInRange(start, end);

    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInRangeSeriesAllInRange() {
    LocalDateTime start = testStart.minusDays(1);
    LocalDateTime end = testStart.plusWeeks(4);

    List<ISingleEvent> events = testEventSeries.getEventsInRange(start, end);

    assertEquals(4, events.size());
    assertTrue(events.contains(testSingleEvent));
  }

  @Test
  public void testGetEventsInRangeSeriesPartiallyInRange() {
    LocalDateTime start = testStart.plusWeeks(1).minusDays(1);
    LocalDateTime end = testStart.plusWeeks(2).plusDays(1);

    List<ISingleEvent> events = testEventSeries.getEventsInRange(start, end);

    assertEquals(2, events.size());
    assertTrue(events.contains(testEventSeries.getEvents().get(1)));
    assertTrue(events.contains(testEventSeries.getEvents().get(2)));
  }

  @Test
  public void testGetEventsInRangeSeriesNoneInRange() {
    LocalDateTime start = testStart.plusWeeks(5);
    LocalDateTime end = testStart.plusWeeks(6);

    List<ISingleEvent> events = testEventSeries.getEventsInRange(start, end);

    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInRangeInvalidRange() {
    LocalDateTime start = testStart.plusDays(1);
    LocalDateTime end = testStart.minusDays(1);

    List<ISingleEvent> events = testSingleEvent.getEventsInRange(start, end);

    assertTrue(events.isEmpty());
  }

  @Test
  public void testGetEventsInRangeBoundary() {
    LocalDateTime start = testStart;
    LocalDateTime end = testEnd;

    List<ISingleEvent> events = testSingleEvent.getEventsInRange(start, end);

    assertEquals(1, events.size());
    assertEquals(testSingleEvent, events.get(0));
  }

  @Test
  public void testGetCorrespondingEventSingleEventMatch() {
    ISingleEvent result = testSingleEvent.getCorrespondingEvent(
            "Test Event", testStart, testEnd);

    assertEquals(testSingleEvent, result);
  }

  @Test
  public void testGetCorrespondingEventSingleEventNoMatchSubject() {
    ISingleEvent result = testSingleEvent.getCorrespondingEvent(
            "Wrong Subject", testStart, testEnd);

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventSingleEventNoMatchStartTime() {
    ISingleEvent result = testSingleEvent.getCorrespondingEvent(
            "Test Event", testStart.plusHours(1), testEnd);

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventSingleEventNoMatchEndTime() {
    ISingleEvent result = testSingleEvent.getCorrespondingEvent(
            "Test Event", testStart, testEnd.plusHours(1));

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventSeriesFirstEventMatch() {
    ISingleEvent firstEvent = testEventSeries.getEvents().get(0);
    ISingleEvent result = testEventSeries.getCorrespondingEvent(
            "Test Event", testStart, testEnd);

    assertEquals(firstEvent, result);
  }

  @Test
  public void testGetCorrespondingEventSeriesSecondEventMatch() {
    ISingleEvent secondEvent = testEventSeries.getEvents().get(1);
    LocalDateTime secondEventStart = testStart.plusWeeks(1);
    LocalDateTime secondEventEnd = testEnd.plusWeeks(1);

    ISingleEvent result = testEventSeries.getCorrespondingEvent(
            "Test Event", secondEventStart, secondEventEnd);

    assertEquals(secondEvent, result);
  }

  @Test
  public void testGetCorrespondingEventSeriesNoMatch() {
    LocalDateTime nonExistentStart = testStart.plusWeeks(5);
    LocalDateTime nonExistentEnd = testEnd.plusWeeks(5);

    ISingleEvent result = testEventSeries.getCorrespondingEvent(
            "Test Event", nonExistentStart, nonExistentEnd);

    assertNull(result);
  }

  @Test
  public void testSingleEventMatchesWithSameEvent() {
    ISingleEvent event = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Description")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    assertTrue(event.matchesWith(event));
  }

  @Test
  public void testSingleEventMatchesWithIdenticalEvent() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Description")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Description")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    assertTrue(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventNotMatchesWithDifferentSubject() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .build();
    ISingleEvent event2 = new SingleEvent.Builder("Different Event", testStart, testEnd)
            .build();

    assertFalse(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventNotMatchesWithDifferentStartTime() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .build();
    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart.plusMinutes(30),
            testEnd).build();

    assertFalse(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventNotMatchesWithDifferentEndTime() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd).build();
    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart,
            testEnd.plusMinutes(30)).build();

    assertFalse(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventMatchesWithDifferentDescription() {
    // Different description but same subject, start, and end should still match
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Description 1").build();
    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Description 2").build();

    assertTrue(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventMatchesWithDifferentLocation() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .location(Location.PHYSICAL).build();
    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .location(Location.ONLINE).build();

    assertTrue(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventMatchesWithDifferentStatus() {
    ISingleEvent event1 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .status(Status.PUBLIC).build();
    ISingleEvent event2 = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .status(Status.PRIVATE).build();

    assertTrue(event1.matchesWith(event2));
  }

  @Test
  public void testSingleEventMatchesWithNull() {
    ISingleEvent event = new SingleEvent.Builder("Test Event", testStart, testEnd).build();
    assertNotNull(event);
  }

  @Test
  public void testEventSeriesMatchesWithEventInSeries() {

    ISingleEvent baseEvent = new SingleEvent.Builder("Series Event", testStart, testEnd)
            .build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);

    ISingleEvent secondEvent = series.getEvents().get(1);

    assertTrue(series.matchesWith(secondEvent));
  }

  @Test
  public void testEventSeriesNotMatchesWithDifferentSubject() {
    ISingleEvent baseEvent = new SingleEvent.Builder("Series Event", testStart, testEnd)
            .build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);

    ISingleEvent differentEvent = new SingleEvent.Builder("Different Event",
            testStart.plusWeeks(1), testEnd.plusWeeks(1)).build();

    assertFalse(series.matchesWith(differentEvent));
  }

  @Test
  public void testEventSeriesNotMatchesWithDifferentDayPattern() {
    // Series occurs on Sundays
    ISingleEvent baseEvent = new SingleEvent.Builder("Series Event", testStart, testEnd)
            .build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);

    LocalDateTime mondayStart = testStart.plusDays(1);
    LocalDateTime mondayEnd = testEnd.plusDays(1);
    ISingleEvent mondayEvent = new SingleEvent.Builder("Series Event", mondayStart,
            mondayEnd).build();

    assertFalse(series.matchesWith(mondayEvent));
  }

  @Test
  public void testEventSeriesNotMatchesWithEventOutsideRange() {
    ISingleEvent baseEvent = new SingleEvent.Builder("Series Event", testStart, testEnd)
            .build();
    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.SUNDAY);

    LocalDateTime laterStart = testStart.plusWeeks(5);
    LocalDateTime laterEnd = testEnd.plusWeeks(5);
    ISingleEvent laterEvent = new SingleEvent.Builder("Series Event", laterStart, laterEnd)
            .build();

    assertFalse(series.matchesWith(laterEvent));
  }

  @Test
  public void testCanAddToCalendar() {
    assertTrue(testSingleEvent.canAddToCalendar(calendar));
    assertTrue(testEventSeries.canAddToCalendar(calendar));
  }

  @Test
  public void testCannotAddToCalendarWhenConflict() {
    calendar.addEvent(testSingleEvent);
    ISingleEvent conflictingEvent = new SingleEvent.Builder("Test Event", testStart, testEnd)
            .description("Conflicting")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    assertFalse(conflictingEvent.canAddToCalendar(calendar));
  }

  @Test
  public void testCannotAddEventSeriesWhenConflict() {
    calendar.addEvent(testSingleEvent);
    IEventSeries conflictingSeries = new EventSeries(testSingleEvent, 4,
            DayOfWeek.SUNDAY);

    assertFalse(conflictingSeries.canAddToCalendar(calendar));
  }

  @Test
  public void testCanAddEventDifferentSubject() {
    calendar.addEvent(testSingleEvent);
    ISingleEvent differentSubjectEvent = new SingleEvent.Builder("Different Subject",
            testStart, testEnd)
            .description("desc")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    assertTrue(differentSubjectEvent.canAddToCalendar(calendar));
  }

  @Test
  public void testCanAddEventSeriesDifferentSubject() {
    calendar.addEvent(testSingleEvent);
    IEventSeries differentSubjectSeries = new EventSeries(
            new SingleEvent.Builder("Different Subject", testStart, testEnd).build(),
            4, DayOfWeek.SUNDAY);

    assertTrue(differentSubjectSeries.canAddToCalendar(calendar));
  }

  @Test
  public void testSingleEventGetSeriesIfFoundReturnsNull() {
    ISingleEvent event = new SingleEvent.Builder("event", testStart, testEnd).build();
    assertNull(testSingleEvent.getSeriesIfFound(event));
  }

  @Test
  public void testEventSeriesGetSeriesIfFoundWithEventInSeries() {
    ISingleEvent eventInSeries = testEventSeries.getEvents().get(2);

    IEventSeries result = testEventSeries.getSeriesIfFound(eventInSeries);
    assertEquals(testEventSeries, result);
  }

  @Test
  public void testEventSeriesGetSeriesIfFoundWithEventNotInSeries() {
    ISingleEvent event = new SingleEvent.Builder("No Series", testStart, testEnd).build();
    assertNull(testEventSeries.getSeriesIfFound(event));
  }

  @Test
  public void testEventSeriesGetSeriesIfFoundWithSimilarEventNotInSeries() {
    ISingleEvent similar = new SingleEvent.Builder("Test Event",
            testStart.plusDays(2),
            testEnd.plusDays(2)).build();

    assertNull(testEventSeries.getSeriesIfFound(similar));
  }

  @Test
  public void testEventSeriesGetSeriesIfFoundWithNull() {
    assertNull(testEventSeries.getSeriesIfFound(null));
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSingleEventMatch() {
    ISingleEvent result = testSingleEvent.getCorrespondingEventFromStartDate(
            "Test Event", testStart);

    assertEquals(testSingleEvent, result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSingleEventWrongSubject() {
    ISingleEvent result = testSingleEvent.getCorrespondingEventFromStartDate(
            "Wrong Subject", testStart);

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSingleEventWrongStartTime() {
    ISingleEvent result = testSingleEvent.getCorrespondingEventFromStartDate(
            "Test Event", testStart.plusHours(1));

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSeriesFirstEventMatch() {
    ISingleEvent firstEvent = testEventSeries.getEvents().get(0);
    ISingleEvent result = testEventSeries.getCorrespondingEventFromStartDate(
            "Test Event", testStart);

    assertEquals(firstEvent, result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSeriesSecondEventMatch() {
    ISingleEvent secondEvent = testEventSeries.getEvents().get(1);
    LocalDateTime secondEventStart = testStart.plusWeeks(1);

    ISingleEvent result = testEventSeries.getCorrespondingEventFromStartDate(
            "Test Event", secondEventStart);

    assertEquals(secondEvent, result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateSeriesNoMatch() {
    LocalDateTime nonExistentStart = testStart.plusWeeks(5);

    ISingleEvent result = testEventSeries.getCorrespondingEventFromStartDate(
            "Test Event", nonExistentStart);

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateWithNullSubject() {
    ISingleEvent result = testSingleEvent.getCorrespondingEventFromStartDate(
            null, testStart);

    assertNull(result);
  }

  @Test
  public void testGetCorrespondingEventFromStartDateWithNullStartTime() {
    ISingleEvent result = testSingleEvent.getCorrespondingEventFromStartDate(
            "Test Event", null);

    assertNull(result);
  }


  @Test
  public void testSetTimeZoneSingleEventSameTimeZone() {
    TimeZone currentTz = TimeZone.getTimeZone("America/New_York");
    TimeZone newTz = TimeZone.getTimeZone("America/New_York");

    CalendarEvent updated = testSingleEvent.setTimeZone(currentTz, newTz);

    assertEquals(testSingleEvent.getStartDateTime(), updated.getStartDateTime());
    assertEquals(testSingleEvent.getEndDateTime(), updated.getEndDateTime());
  }

  @Test
  public void testSetTimeZoneSingleEventDifferentTimeZone() {
    TimeZone currentTz = TimeZone.getTimeZone("America/New_York"); // UTC-5
    TimeZone newTz = TimeZone.getTimeZone("Europe/London"); // UTC+0

    CalendarEvent updated = testSingleEvent.setTimeZone(currentTz, newTz);

    assertEquals(testSingleEvent.getStartDateTime().plusHours(5), updated.getStartDateTime());
    assertEquals(testSingleEvent.getEndDateTime().plusHours(5), updated.getEndDateTime());
  }

  @Test
  public void testSetTimeZoneEventSeriesAcrossTimeZones() {
    TimeZone currentTz = TimeZone.getTimeZone("America/Los_Angeles");
    TimeZone newTz = TimeZone.getTimeZone("Asia/Tokyo");

    CalendarEvent updated = testEventSeries.setTimeZone(currentTz, newTz);

    assertEquals(testEventSeries.getStartDateTime().plusHours(16), updated.getStartDateTime());
    assertEquals(testEventSeries.getEndDateTime().plusHours(17), updated.getEndDateTime());


    assertEquals(((EventSeries) updated).getEvents().get(0).getStartDateTime(),
            testEventSeries.getEvents().get(0).getStartDateTime().plusHours(16));

    assertEquals(((EventSeries) updated).getEvents().get(1).getStartDateTime(),
            testEventSeries.getEvents().get(1).getStartDateTime().plusHours(16));

    assertEquals(((EventSeries) updated).getEvents().get(2).getStartDateTime(),
            testEventSeries.getEvents().get(2).getStartDateTime().plusHours(16));
  }

  @Test
  public void testSetTimeZonePreservesEventProperties() {
    TimeZone currentTz = TimeZone.getTimeZone("America/New_York");
    TimeZone newTz = TimeZone.getTimeZone("Europe/Berlin");

    CalendarEvent updated = testSingleEvent.setTimeZone(currentTz, newTz);

    assertEquals(testSingleEvent.getSubject(), updated.getSubject());
    assertEquals(testSingleEvent.getDescription(), updated.getDescription());
    assertEquals(testSingleEvent.getLocation(), updated.getLocation());
    assertEquals(testSingleEvent.getStatus(), updated.getStatus());
  }

}







