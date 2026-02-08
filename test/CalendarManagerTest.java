import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import model.application.CalendarManager;
import model.application.ICalendarManager;
import model.calendar.IBetterCalendar;
import model.event.EventSeries;
import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.Location;
import model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;


/**
 * Tests the correctness of the {@code CalendarManager} class.
 */
public class CalendarManagerTest {

  private ICalendarManager application;

  @Before
  public void setUp() {
    application = new CalendarManager();
  }

  @Test
  public void testCreateCalendar() {
    String calendarName = "Work Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(calendarName, timeZone);
    application.setCalendarInUse(calendarName);
    IBetterCalendar calendar = application.getCurrentCalendar();
    assertNotNull(calendar);
    assertEquals(calendarName, application.getCurrentCalendarName());
    assertEquals(timeZone, calendar.getZoneId().getId());

  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateCalendarInvalidTimezone() {
    String calendarName = "Work Calendar";
    String timeZone = "time";
    application.createCalendar(calendarName, timeZone);
  }

  @Test
  public void testCreateCalendarDuplicateName() {
    String calendarName = "Work Calendar";
    String timezone = "America/New_York";
    String timezone2 = "America/Los_Angeles";

    application.createCalendar(calendarName, timezone);
    assertThrows(IllegalArgumentException.class, () ->
            application.createCalendar(calendarName, timezone2));
  }

  @Test
  public void testCreateMultipleCalendars() {
    String[] names = {"Work", "Personal", "Family", "Holidays"};
    String timezone = "America/New_York";
    for (String name : names) {
      application.createCalendar(name, timezone);
    }

    for (String name : names) {
      application.setCalendarInUse(name);
      assertEquals(name, application.getCurrentCalendarName());
      assertNotNull(application.getCurrentCalendar());
    }


  }

  @Test
  public void testEditCalendarNameSuccessful() {
    String oldName = "Original Calendar";
    String newName = "Renamed Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(oldName, timeZone);
    application.setCalendarInUse(oldName);
    IBetterCalendar originalCalendar = application.getCurrentCalendar();
    assertNotNull(originalCalendar);

    application.editCalendarName(oldName, newName);

    assertEquals(newName, application.getCurrentCalendarName());
    IBetterCalendar renamedCalendar = application.getCurrentCalendar();
    assertNotNull(renamedCalendar);
    assertEquals(timeZone, renamedCalendar.getZoneId().getId());

    assertThrows(IllegalArgumentException.class, () ->
            application.setCalendarInUse(oldName));
  }

  @Test
  public void testEditCalendarNamePreservesCurrentInUse() {
    application.createCalendar("Calendar1", "America/New_York");
    application.createCalendar("Calendar2", "Europe/London");

    application.setCalendarInUse("Calendar1");
    application.editCalendarName("Calendar1", "RenamedCalendar");

    assertEquals("RenamedCalendar", application.getCurrentCalendarName());
  }

  @Test
  public void testEditCalendarNameNotInUse() {
    application.createCalendar("Calendar1", "America/New_York");
    application.createCalendar("Calendar2", "Europe/London");

    application.setCalendarInUse("Calendar2");

    application.editCalendarName("Calendar1", "RenamedCalendar");

    assertEquals("Calendar2", application.getCurrentCalendarName());

    application.setCalendarInUse("RenamedCalendar");
    assertNotNull(application.getCurrentCalendar());
    assertEquals("RenamedCalendar", application.getCurrentCalendarName());
  }

  @Test
  public void testEditCalendarTimeZoneSuccessful() {
    String calendarName = "Work Calendar";
    String originalTimeZone = "America/New_York";
    String newTimeZone = "Europe/London";

    application.createCalendar(calendarName, originalTimeZone);
    application.setCalendarInUse(calendarName);
    IBetterCalendar originalCalendar = application.getCurrentCalendar();
    assertNotNull(originalCalendar);

    application.editCalendarTimeZone(calendarName, newTimeZone);

    IBetterCalendar updatedCalendar = application.getCurrentCalendar();
    assertNotNull(updatedCalendar);
    assertEquals(calendarName, application.getCurrentCalendarName());
    assertEquals(newTimeZone, updatedCalendar.getZoneId().getId());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEditCalendarTimeZoneNonExistentCalendar() {
    application.editCalendarTimeZone("Wrong Cal", "America/New_York");
  }

  @Test
  public void testEditCalendarTimeZoneInvalidTimeZone() {
    String calendarName = "Work Calendar";
    application.createCalendar(calendarName, "America/New_York");

    assertThrows(IllegalArgumentException.class, () ->
            application.editCalendarTimeZone(calendarName, "Random/TimeZone"));
  }

  @Test
  public void testEditCalendarTimeZoneSameName() {
    String calendarName = "Work Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(calendarName, timeZone);
    application.setCalendarInUse(calendarName);

    application.editCalendarTimeZone(calendarName, timeZone);

    IBetterCalendar calendar = application.getCurrentCalendar();
    assertEquals(timeZone, calendar.getZoneId().getId());
  }

  @Test
  public void testEditCalendarTimeZoneEventsConversion() {
    application.createCalendar("Calendar", "America/New_York");
    application.setCalendarInUse("Calendar");

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Test Event", eventStart, eventStart.plusHours(1))
                    .build());

    application.editCalendarTimeZone("Calendar", "Europe/London");

    // Verify event is at 15:00 London time (5 hours ahead)
    ISingleEvent convertedEvent = application.getCurrentCalendar()
            .query(LocalDateTime.of(2023, 5, 15, 0, 0),
                    LocalDateTime.of(2023, 5, 15, 23, 59))
            .get(0);

    assertEquals("Test Event", convertedEvent.getSubject());
    assertEquals(15, convertedEvent.getStartDateTime().getHour());
    assertEquals(16, convertedEvent.getEndDateTime().getHour());
  }

  @Test
  public void testEditCalendarTimeZoneSameNameWithSeries() {
    String calendarName = "Event Calendar";
    application.createCalendar(calendarName, "America/New_York");
    application.setCalendarInUse(calendarName);

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    ISingleEvent event = new SingleEvent.Builder("Test Event", eventStart, eventStart.plusHours(1))
            .description("Description")
            .build();
    IEventSeries seriesEvent = new EventSeries(event, 3, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    application.getCurrentCalendar().addEvent(seriesEvent);

    application.editCalendarTimeZone(calendarName, "Europe/London");

    IBetterCalendar updatedCalendar = application.getCurrentCalendar();
    List<ISingleEvent> events = updatedCalendar.query(
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 22, 23, 59)
    );

    assertEquals(3, events.size());
    for (ISingleEvent convertedEvent : events) {
      assertEquals("Test Event", convertedEvent.getSubject());
      assertEquals(15, convertedEvent.getStartDateTime().getHour());
      assertEquals(16, convertedEvent.getEndDateTime().getHour());
      assertNotNull(updatedCalendar.isInSeries(convertedEvent));
    }

    IEventSeries series = updatedCalendar.isInSeries(events.get(0));
    for (int i = 1; i < events.size(); i++) {
      assertEquals(series, updatedCalendar.isInSeries(events.get(i)));
    }
  }

  @Test
  public void testSetCalendarInUseSuccessful() {
    String[] names = {"Work", "Personal", "Study", "Family"};
    String timezone = "America/New_York";

    for (String name : names) {
      application.createCalendar(name, timezone);
    }

    for (String name : names) {
      application.setCalendarInUse(name);
      assertEquals(name, application.getCurrentCalendarName());
      assertNotNull(application.getCurrentCalendar());
      assertEquals(timezone, application.getCurrentCalendar().getZoneId().getId());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCalendarInUseNonExistent() {
    application.setCalendarInUse("NonExistentCalendar");
  }

  @Test
  public void testSetCalendarInUseAfterCalendarCreation() {
    String calendarName = "New Calendar";
    String timezone = "Europe/Berlin";
    application.createCalendar(calendarName, timezone);

    assertNull(application.getCurrentCalendarName());
    application.setCalendarInUse(calendarName);
    assertEquals(calendarName, application.getCurrentCalendarName());
    assertNotNull(application.getCurrentCalendar());
  }


  @Test
  public void testSetCalendarInUseSwitchingBetweenCalendars() {
    String calendar1 = "Calendar1";
    String calendar2 = "Calendar2";
    String timezone1 = "America/New_York";
    String timezone2 = "Asia/Tokyo";

    application.createCalendar(calendar1, timezone1);
    application.createCalendar(calendar2, timezone2);

    application.setCalendarInUse(calendar1);
    assertEquals(calendar1, application.getCurrentCalendarName());
    assertEquals(timezone1, application.getCurrentCalendar().getZoneId().getId());

    application.setCalendarInUse(calendar2);
    assertEquals(calendar2, application.getCurrentCalendarName());
    assertEquals(timezone2, application.getCurrentCalendar().getZoneId().getId());

    application.setCalendarInUse(calendar1);
    assertEquals(calendar1, application.getCurrentCalendarName());
    assertEquals(timezone1, application.getCurrentCalendar().getZoneId().getId());
  }

  @Test
  public void testEditCalendarTimeZoneShiftsEventToNewDay() {
    String calendarName = "International Calendar";
    String originalTimeZone = "America/New_York";
    String newTimeZone = "Asia/Tokyo";

    application.createCalendar(calendarName, originalTimeZone);
    application.setCalendarInUse(calendarName);

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 23, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15, 23, 30);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Late Night Call", eventStart, eventEnd)
                    .description("Call with Tokyo office")
                    .build());

    application.editCalendarTimeZone(calendarName, newTimeZone);

    List<ISingleEvent> events = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 16, 0, 0),
            LocalDateTime.of(2023, 5, 16, 23, 59));

    assertEquals(1, events.size());
    ISingleEvent convertedEvent = events.get(0);
    assertEquals("Late Night Call", convertedEvent.getSubject());
    assertEquals(LocalDate.of(2023, 5, 16), convertedEvent.getStartDateTime().toLocalDate());
    assertTrue(convertedEvent.getStartDateTime().getHour() >= 12);
  }

  @Test
  public void testSetCalendarInUseWithEvents() {
    String calendar1 = "Calendar1";
    String calendar2 = "Calendar2";
    String timezone = "America/New_York";

    application.createCalendar(calendar1, timezone);
    application.createCalendar(calendar2, timezone);

    application.setCalendarInUse(calendar1);
    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15, 11, 0);
    ISingleEvent event = new SingleEvent.Builder("Meeting", eventStart, eventEnd)
            .description("Team meeting")
            .build();
    application.getCurrentCalendar().addEvent(event);

    // Switch to second calendar and verify event doesn't exist
    application.setCalendarInUse(calendar2);
    List<ISingleEvent> events = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 15, 23, 59)
    );
    assertEquals(0, events.size());

    // Switch back to first calendar and verify event exists
    application.setCalendarInUse(calendar1);
    events = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 15, 23, 59)
    );
    assertEquals(1, events.size());
    assertEquals("Meeting", events.get(0).getSubject());
  }

  @Test
  public void testSetCalendarInUseSameCalendarMultipleTimes() {
    String calendarName = "Work";
    String timezone = "America/New_York";
    application.createCalendar(calendarName, timezone);

    for (int i = 0; i < 5; i++) {
      application.setCalendarInUse(calendarName);
      assertEquals(calendarName, application.getCurrentCalendarName());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testSetCalendarInUseWithNullName() {
    application.setCalendarInUse(null);
  }

  @Test
  public void testCopyEventSuccessful() {
    // Setup calendars
    application.createCalendar("Source", "America/New_York");
    application.createCalendar("Target", "America/New_York");
    application.setCalendarInUse("Source");

    // Create and add event
    String eventName = "Meeting";
    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime eventEnd = eventStart.plusHours(1).plusMinutes(30);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder(eventName, eventStart, eventEnd)
                    .description("Planning")
                    .location(Location.PHYSICAL)
                    .build());

    LocalDateTime newStart = LocalDateTime.of(2023, 6, 20, 14, 0);
    application.copyEvent(eventName, eventStart, "Target", newStart);

    List<ISingleEvent> sourceEvents = application.getCurrentCalendar()
            .query(eventStart.toLocalDate().atStartOfDay(), eventStart.toLocalDate()
                    .plusDays(1).atStartOfDay());
    assertEquals(1, sourceEvents.size());

    application.setCalendarInUse("Target");
    List<ISingleEvent> targetEvents = application.getCurrentCalendar()
            .query(newStart.toLocalDate().atStartOfDay(), newStart.toLocalDate()
                    .plusDays(1).atStartOfDay());

    assertEquals(1, targetEvents.size());
    ISingleEvent copied = targetEvents.get(0);
    assertEquals(eventName, copied.getSubject());
    assertEquals(newStart, copied.getStartDateTime());
    assertEquals(Duration.between(eventStart, eventEnd),
            Duration.between(copied.getStartDateTime(), copied.getEndDateTime()));
  }

  @Test
  public void testCopyEventNonExistentTargetCalendar() {
    String sourceCalName = "Source Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    String eventName = "Important Meeting";
    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15, 11, 0);
    ISingleEvent event = new SingleEvent.Builder(eventName, eventStart, eventEnd)
            .description("Team meeting")
            .build();
    application.getCurrentCalendar().addEvent(event);

    assertThrows(IllegalArgumentException.class, () ->
            application.copyEvent(eventName, eventStart, "NonExistentCalendar",
                    LocalDateTime.of(2023, 6, 20, 14, 0)));
  }

  @Test
  public void testCopyEventWithNoMatchingEvent() {
    String sourceCalName = "Source Calendar";
    String targetCalName = "Target Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.createCalendar(targetCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15, 11, 0);
    ISingleEvent event = new SingleEvent.Builder("Meeting", eventStart, eventEnd)
            .description("Team meeting")
            .build();
    application.getCurrentCalendar().addEvent(event);

    // Try copying a non-existent event or wrong start time
    assertThrows(IndexOutOfBoundsException.class, () -> {
      application.copyEvent("wrong event", eventStart, targetCalName,
              LocalDateTime.of(2023, 6, 20, 14, 0));
    });

    assertThrows(IndexOutOfBoundsException.class, () -> {
      application.copyEvent("Meeting", LocalDateTime.of(2023, 5, 15, 11, 0), targetCalName,
              LocalDateTime.of(2023, 6, 20, 14, 0));
    });
  }

  @Test
  public void testCopyEventToSameCalendar() {
    String calendarName = "Work Calendar";
    application.createCalendar(calendarName, "America/New_York");
    application.setCalendarInUse(calendarName);

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    ISingleEvent event = new SingleEvent.Builder("Team Meeting", eventStart,
            eventStart.plusHours(1))
            .description("Weekly sync")
            .build();
    application.getCurrentCalendar().addEvent(event);

    LocalDateTime newStartTime = LocalDateTime.of(2023, 5, 22, 10, 0);
    application.copyEvent("Team Meeting", eventStart, calendarName, newStartTime);

    List<ISingleEvent> allEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 1, 0, 0),
            LocalDateTime.of(2023, 5, 31, 23, 59));

    assertEquals(2, allEvents.size());
    assertTrue(allEvents.stream().anyMatch(e -> e.getStartDateTime().equals(eventStart)));
    assertTrue(allEvents.stream().anyMatch(e -> e.getStartDateTime().equals(newStartTime)));
  }

  @Test
  public void testCopyEventPreservesAllProperties() {
    String sourceCalName = "Source";
    String targetCalName = "Target";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.createCalendar(targetCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    String eventName = "Quarterly Review";
    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 13, 0);
    LocalDateTime eventEnd = LocalDateTime.of(2023, 5, 15, 15, 0);
    ISingleEvent event = new SingleEvent.Builder(eventName, eventStart, eventEnd)
            .description("Financial performance review")
            .location(Location.PHYSICAL)
            .build();
    application.getCurrentCalendar().addEvent(event);

    LocalDateTime newStartTime = LocalDateTime.of(2023, 6, 15, 10, 0);
    application.copyEvent(eventName, eventStart, targetCalName, newStartTime);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 6, 15, 0, 0),
            LocalDateTime.of(2023, 6, 15, 23, 59));

    assertEquals(1, targetEvents.size());
    ISingleEvent copiedEvent = targetEvents.get(0);

    assertEquals(eventName, copiedEvent.getSubject());
    assertEquals("Financial performance review", copiedEvent.getDescription());
    assertEquals(Location.PHYSICAL, copiedEvent.getLocation());

    assertEquals(Duration.between(eventStart, eventEnd),
            Duration.between(copiedEvent.getStartDateTime(), copiedEvent.getEndDateTime()));
  }

  @Test
  public void testCopyEventFromSeries() {
    application.createCalendar("Source", "America/New_York");
    application.createCalendar("Target", "America/New_York");
    application.setCalendarInUse("Source");

    LocalDateTime eventStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    ISingleEvent baseEvent = new SingleEvent.Builder("Weekly Meeting", eventStart,
            eventStart.plusHours(1))
            .description("Team sync")
            .location(Location.ONLINE)
            .build();
    application.getCurrentCalendar().addEvent(new EventSeries(baseEvent, 3, DayOfWeek.MONDAY));

    LocalDateTime secondEventStart = LocalDateTime.of(2023, 5, 22, 10, 0);
    LocalDateTime newStartTime = LocalDateTime.of(2023, 6, 5, 14, 0);
    application.copyEvent("Weekly Meeting", secondEventStart, "Target", newStartTime);

    List<ISingleEvent> sourceEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 15, 0, 0),
            LocalDateTime.of(2023, 5, 29, 23, 59));
    assertEquals(3, sourceEvents.size());
    for (ISingleEvent event : sourceEvents) {
      assertNotNull(application.getCurrentCalendar().isInSeries(event));
    }

    application.setCalendarInUse("Target");
    ISingleEvent copiedEvent = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 6, 5, 0, 0),
            LocalDateTime.of(2023, 6, 5, 23, 59)).get(0);

    assertEquals("Weekly Meeting", copiedEvent.getSubject());
    assertEquals("Team sync", copiedEvent.getDescription());
    assertEquals(Location.ONLINE, copiedEvent.getLocation());
    assertEquals(newStartTime, copiedEvent.getStartDateTime());

    assertNull(application.getCurrentCalendar().isInSeries(copiedEvent));
  }

  @Test
  public void testCopyEventsSuccessful() {
    String sourceCalName = "Source Calendar";
    String targetCalName = "Target Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.createCalendar(targetCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 15, 0, 0); // Monday
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Morning Meeting", baseDate.withHour(9), baseDate.withHour(10))
                    .description("meeting")
                    .build());

    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Lunch", baseDate.withHour(12), baseDate.withHour(13))
                    .build());

    LocalDateTime day2 = baseDate.plusDays(1);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Interview", day2.withHour(11), day2.withHour(12))
                    .location(Location.PHYSICAL)
                    .build());

    LocalDateTime day3 = baseDate.plusDays(2);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Project Review", day3.withHour(14), day3.withHour(16))
                    .description("Project Review")
                    .build());

    LocalDate newStartDate = LocalDate.of(2023, 6, 15); // Starting a month later
    application.copyEvents(
            baseDate,
            day3.withHour(23).withMinute(59),
            targetCalName,
            newStartDate);

    List<ISingleEvent> sourceEvents = application.getCurrentCalendar().query(
            baseDate,
            day3.withHour(23).withMinute(59));
    assertEquals(4, sourceEvents.size());

    application.setCalendarInUse(targetCalName);
    LocalDateTime targetStart = LocalDateTime.of(newStartDate, LocalTime.MIN);
    LocalDateTime targetEnd = LocalDateTime.of(newStartDate.plusDays(2), LocalTime.MAX);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar()
            .query(targetStart, targetEnd);

    assertEquals(4, targetEvents.size());

    Map<String, ISingleEvent> eventsByName = targetEvents.stream()
            .collect(Collectors.toMap(ISingleEvent::getSubject, e -> e));

    ISingleEvent morningMeeting = eventsByName.get("Morning Meeting");
    assertNotNull(morningMeeting);
    assertEquals(newStartDate, morningMeeting.getStartDateTime().toLocalDate());
    assertEquals(9, morningMeeting.getStartDateTime().getHour());
    assertEquals(10, morningMeeting.getEndDateTime().getHour());

    ISingleEvent interview = eventsByName.get("Interview");
    assertNotNull(interview);
    assertEquals(newStartDate.plusDays(1), interview.getStartDateTime().toLocalDate());
    assertEquals(11, interview.getStartDateTime().getHour());
    assertEquals(12, interview.getEndDateTime().getHour());

    assertEquals("Project Review", eventsByName.get("Project Review").getDescription());
    assertEquals(Location.PHYSICAL, eventsByName.get("Interview").getLocation());
  }

  @Test
  public void testCopyEventsBetweenDifferentTimeZones() {
    String sourceCalName = "New York Calendar";
    String targetCalName = "Berlin Calendar";

    application.createCalendar(sourceCalName, "America/New_York");
    application.createCalendar(targetCalName, "Europe/Berlin");  // UTC+1/+2
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 15, 0, 0);

    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Morning Call", baseDate.plusHours(9), baseDate.plusHours(10))
                    .build());

    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Evening Call", baseDate.plusHours(17), baseDate.plusHours(18))
                    .build());

    LocalDate newStartDate = LocalDate.of(2023, 6, 10);
    application.copyEvents(
            baseDate,
            baseDate.plusDays(1).minusSeconds(1),
            targetCalName,
            newStartDate);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> berlinEvents = application.getCurrentCalendar().query(
            LocalDateTime.MIN,
            LocalDateTime.MAX);

    assertEquals(2, berlinEvents.size());

    Map<String, ISingleEvent> eventsByName = berlinEvents.stream()
            .collect(Collectors.toMap(ISingleEvent::getSubject, e -> e));

    ISingleEvent morningCall = eventsByName.get("Morning Call");
    assertTrue(morningCall.getStartDateTime().getHour() >= 14
            && morningCall.getStartDateTime().getHour() <= 16);

    ISingleEvent eveningCall = eventsByName.get("Evening Call");
    assertTrue(eveningCall.getStartDateTime().getHour() >= 22
            || eveningCall.getStartDateTime().getHour() <= 1);
  }

  @Test
  public void testCopyEventsEmptyInterval() {
    String sourceCalName = "Source";
    String targetCalName = "Target";
    application.createCalendar(sourceCalName, "America/New_York");
    application.createCalendar(targetCalName, "America/New_York");
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 15, 10, 0);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Event Outside Range", baseDate, baseDate.plusHours(1))
                    .build());

    LocalDateTime intervalStart = LocalDateTime.of(2023, 6, 1, 0, 0);
    LocalDateTime intervalEnd = LocalDateTime.of(2023, 6, 2, 0, 0);
    LocalDate newStartDate = LocalDate.of(2023, 7, 1);

    application.copyEvents(intervalStart, intervalEnd, targetCalName, newStartDate);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(newStartDate, LocalTime.MIN),
            LocalDateTime.of(newStartDate.plusMonths(1), LocalTime.MAX));

    assertEquals(0, targetEvents.size());
  }

  @Test
  public void testCopyEventsNonExistentTargetCalendar() {
    String sourceCalName = "Source";
    application.createCalendar(sourceCalName, "America/New_York");
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 15, 10, 0);
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Test Event", baseDate, baseDate.plusHours(1))
                    .build());

    assertThrows(IllegalArgumentException.class, () ->
            application.copyEvents(
                    baseDate,
                    baseDate.plusHours(1),
                    null,
                    LocalDate.of(2023, 6, 1)));
  }

  @Test
  public void testCopyEventsMultipleDaysSpan() {
    String sourceCalName = "Source";
    String targetCalName = "Target";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.createCalendar(targetCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 1, 0, 0);

    for (int day = 0; day < 5; day++) {
      LocalDateTime eventDate = baseDate.plusDays(day);
      application.getCurrentCalendar().addEvent(
              new SingleEvent.Builder("Event " + (day + 1),
                      eventDate.withHour(10 + day),
                      eventDate.withHour(11 + day))
                      .description("Day " + (day + 1) + " event")
                      .build());
    }

    LocalDate newStartDate = LocalDate.of(2023, 7, 10);
    application.copyEvents(
            baseDate,
            baseDate.plusDays(4).plusHours(23).plusMinutes(59),
            targetCalName,
            newStartDate);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(newStartDate, LocalTime.MIN),
            LocalDateTime.of(newStartDate.plusDays(4), LocalTime.MAX));

    assertEquals(5, targetEvents.size());

    Map<String, ISingleEvent> copiedEventsByName = targetEvents.stream()
            .collect(Collectors.toMap(ISingleEvent::getSubject, e -> e));

    for (int day = 0; day < 5; day++) {
      ISingleEvent event = copiedEventsByName.get("Event " + (day + 1));
      assertEquals(newStartDate.plusDays(day), event.getStartDateTime().toLocalDate());
      assertEquals(10 + day, event.getStartDateTime().getHour());
      assertEquals(11 + day, event.getEndDateTime().getHour());
      assertEquals("Day " + (day + 1) + " event", event.getDescription());
    }
  }

  @Test
  public void testCopyEventsPreservesEventSeries() {
    String sourceCalName = "Source Calendar";
    String targetCalName = "Target Calendar";
    String timeZone = "America/New_York";

    application.createCalendar(sourceCalName, timeZone);
    application.createCalendar(targetCalName, timeZone);
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseEventStart = LocalDateTime.of(2023, 5, 1, 10, 0); // Monday
    LocalDateTime baseEventEnd = LocalDateTime.of(2023, 5, 1, 11, 0);

    ISingleEvent baseEvent = new SingleEvent.Builder("Recurring Meeting", baseEventStart,
            baseEventEnd)
            .description("Weekly planning")
            .location(Location.ONLINE)
            .build();

    IEventSeries series = new EventSeries(baseEvent, 4, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
    application.getCurrentCalendar().addEvent(series);

    List<ISingleEvent> sourceEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 1, 0, 0),
            LocalDateTime.of(2023, 5, 10, 23, 59));
    assertEquals(4, sourceEvents.size());

    for (ISingleEvent event : sourceEvents) {
      assertNotNull(application.getCurrentCalendar().isInSeries(event));
    }

    LocalDate newStartDate = LocalDate.of(2023, 6, 5);
    application.copyEvents(
            LocalDateTime.of(2023, 5, 1, 0, 0),
            LocalDateTime.of(2023, 5, 10, 23, 59),
            targetCalName,
            newStartDate);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 6, 5, 0, 0),
            LocalDateTime.of(2023, 6, 14, 23, 59));

    assertEquals(4, targetEvents.size());

    for (ISingleEvent event : targetEvents) {
      assertEquals("Recurring Meeting", event.getSubject());
      assertEquals("Weekly planning", event.getDescription());
      assertEquals(Location.ONLINE, event.getLocation());
    }

    boolean hasMonday1 = false;
    boolean hasWednesday1 = false;
    boolean hasMonday2 = false;
    boolean hasWednesday2 = false;

    for (ISingleEvent event : targetEvents) {
      if (event.getStartDateTime().toLocalDate().equals(newStartDate)
              && event.getStartDateTime().getDayOfWeek() == DayOfWeek.MONDAY) {
        hasMonday1 = true;
      } else if (event.getStartDateTime().toLocalDate().equals(newStartDate.plusDays(2))
              && event.getStartDateTime().getDayOfWeek() == DayOfWeek.WEDNESDAY) {
        hasWednesday1 = true;
      } else if (event.getStartDateTime().toLocalDate().equals(newStartDate.plusDays(7))
              && event.getStartDateTime().getDayOfWeek() == DayOfWeek.MONDAY) {
        hasMonday2 = true;
      } else if (event.getStartDateTime().toLocalDate().equals(newStartDate.plusDays(9))
              && event.getStartDateTime().getDayOfWeek() == DayOfWeek.WEDNESDAY) {
        hasWednesday2 = true;
      }
    }

    assertTrue(hasMonday1);
    assertTrue(hasWednesday1);
    assertTrue(hasMonday2);
    assertTrue(hasWednesday2);

  }

  @Test
  public void testCopyEventsMixedSingleAndSeriesEvents() {
    String sourceCalName = "Source";
    String targetCalName = "Target";
    application.createCalendar(sourceCalName, "America/New_York");
    application.createCalendar(targetCalName, "America/New_York");
    application.setCalendarInUse(sourceCalName);

    LocalDateTime baseDate = LocalDateTime.of(2023, 5, 1, 9, 0); // Monday

    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Monday Meeting", baseDate, baseDate.plusHours(1))
                    .description("Weekly sync").build());
    application.getCurrentCalendar().addEvent(
            new SingleEvent.Builder("Wednesday Lunch", baseDate.plusDays(2).withHour(12),
                    baseDate.plusDays(2).withHour(13))
                    .description("Team lunch")
                    .location(Location.PHYSICAL).build());

    ISingleEvent standupBase = new SingleEvent.Builder("Daily Standup",
            baseDate.withHour(9).withMinute(30),
            baseDate.withHour(9).withMinute(30).plusMinutes(30))
            .description("Quick team check-in")
            .location(Location.ONLINE).build();
    application.getCurrentCalendar().addEvent(new EventSeries(standupBase, 5,
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY));

    ISingleEvent biWeeklyBase = new SingleEvent.Builder("Project Review",
            baseDate.plusDays(1).withHour(14), baseDate.plusDays(1).withHour(15))
            .description("Bi-weekly progress check").build();
    application.getCurrentCalendar().addEvent(new EventSeries(biWeeklyBase, 2,
            DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));

    List<ISingleEvent> sourceEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 5, 1, 0, 0), LocalDateTime.of(2023, 5, 5, 23, 59));
    assertEquals(9, sourceEvents.size());

    LocalDate newStartDate = LocalDate.of(2023, 6, 12); // Also a Monday
    application.copyEvents(
            LocalDateTime.of(2023, 5, 1, 0, 0),
            LocalDateTime.of(2023, 5, 5, 23, 59),
            targetCalName, newStartDate);

    application.setCalendarInUse(targetCalName);
    List<ISingleEvent> targetEvents = application.getCurrentCalendar().query(
            LocalDateTime.of(2023, 6, 12, 0, 0), LocalDateTime.of(2023, 6, 16, 23, 59));
    assertEquals(9, targetEvents.size());

    Map<String, List<ISingleEvent>> eventsBySubject = targetEvents.stream()
            .collect(Collectors.groupingBy(ISingleEvent::getSubject));

    assertEquals(1, eventsBySubject.get("Monday Meeting").size());
    ISingleEvent mondayMeeting = eventsBySubject.get("Monday Meeting").get(0);
    assertEquals(newStartDate, mondayMeeting.getStartDateTime().toLocalDate());
    assertNull(application.getCurrentCalendar().isInSeries(mondayMeeting));

    assertEquals(1, eventsBySubject.get("Wednesday Lunch").size());
    ISingleEvent lunch = eventsBySubject.get("Wednesday Lunch").get(0);
    assertEquals(newStartDate.plusDays(2), lunch.getStartDateTime().toLocalDate());
    assertEquals(Location.PHYSICAL, lunch.getLocation());
    assertNull(application.getCurrentCalendar().isInSeries(lunch));

    assertEquals(5, eventsBySubject.get("Daily Standup").size());
    List<ISingleEvent> standupEvents = eventsBySubject.get("Daily Standup");
    IEventSeries standupSeries = application.getCurrentCalendar().isInSeries(standupEvents.get(0));
    assertNotNull(standupSeries);
    assertEquals(5, standupSeries.getOccurringDays().length);

    assertEquals(2, eventsBySubject.get("Project Review").size());
    List<ISingleEvent> reviewEvents = eventsBySubject.get("Project Review");
    IEventSeries reviewSeries = application.getCurrentCalendar().isInSeries(reviewEvents.get(0));
    assertNotNull(reviewSeries);
    assertEquals(2, reviewSeries.getOccurringDays().length);
    assertEquals(DayOfWeek.TUESDAY, reviewSeries.getOccurringDays()[0]);
    assertEquals(DayOfWeek.THURSDAY, reviewSeries.getOccurringDays()[1]);
  }

}