import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

import model.calendar.BetterCalendar;
import model.calendar.IBetterCalendar;
import model.event.CalendarEvent;
import model.event.EventSeries;
import model.event.ISingleEvent;
import model.event.Location;
import model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the correctness of the {@code BetterCalendar} class.
 */
public class BetterCalendarTest {
  private IBetterCalendar betterCalendar;
  private String timeZone1;
  private String timeZone2;

  @Before
  public void setUp() {
    timeZone1 = "America/New_York";
    betterCalendar = new BetterCalendar(timeZone1);
    timeZone2 = "Europe/Paris";
  }

  @Test
  public void testBetterCalendarConstructor() {
    betterCalendar = new BetterCalendar(timeZone1);
    assertNotNull(betterCalendar);
    betterCalendar = new BetterCalendar(timeZone2);
    assertNotNull(betterCalendar);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBetterCalendarInValidTimeZone() {
    betterCalendar = new BetterCalendar("timezone");
  }

  @Test
  public void testGetZoneId() {
    assertEquals(timeZone1, betterCalendar.getZoneId().toString());
    betterCalendar = new BetterCalendar(timeZone2);
    assertEquals(timeZone2, betterCalendar.getZoneId().toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetTimeZoneInValidZoneId() {
    new BetterCalendar("timezone").getZoneId();
  }

  @Test
  public void testMakeCalWithNewTimeZoneSingleEvent() {

    IBetterCalendar firstCal = new BetterCalendar(timeZone1);

    LocalDateTime sourceStart = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime sourceEnd = LocalDateTime.of(2023, 5, 15, 11, 0);
    CalendarEvent sourceEvent = new SingleEvent.Builder("Test Event", sourceStart, sourceEnd)
            .description("Description")
            .build();
    firstCal.addEvent(sourceEvent);
    IBetterCalendar resultCal = new BetterCalendar(timeZone2);
    LocalDateTime targetStart = LocalDateTime.of(2023, 5, 15, 16, 0);
    LocalDateTime targetEnd = LocalDateTime.of(2023, 5, 15, 17, 0);
    CalendarEvent targetEvent = new SingleEvent.Builder("Test Event", targetStart, targetEnd)
            .description("Description")
            .build();
    resultCal.addEvent(targetEvent);

    IBetterCalendar actualTargetCal = firstCal.makeCalWithNewTimeZone(timeZone2);
    assertEquals(timeZone2, actualTargetCal.getZoneId().toString());

    List<ISingleEvent> actualEvents = actualTargetCal.query(LocalDateTime.MIN, LocalDateTime.MAX);
    List<ISingleEvent> expectedEvents = resultCal.query(LocalDateTime.MIN, LocalDateTime.MAX);

    assertEquals(expectedEvents.size(), actualEvents.size());

    for (int i = 0; i < expectedEvents.size(); i++) {
      assertEquals(expectedEvents.get(i), actualEvents.get(i));
    }
  }


  @Test
  public void testMakeCalWithNewTimeZoneMultipleEvents() {
    IBetterCalendar firstCal = new BetterCalendar(timeZone1);

    LocalDateTime sourceStart1 = LocalDateTime.of(2023, 5, 15, 10, 0);
    LocalDateTime sourceEnd1 = LocalDateTime.of(2023, 5, 15, 11, 0);
    CalendarEvent sourceEvent1 = new SingleEvent.Builder("Test Event 1", sourceStart1, sourceEnd1)
            .description("Description 1")
            .build();
    firstCal.addEvent(sourceEvent1);

    LocalDateTime sourceStart2 = LocalDateTime.of(2023, 5, 16, 14, 30);
    LocalDateTime sourceEnd2 = LocalDateTime.of(2023, 5, 16, 15, 45);
    CalendarEvent sourceEvent2 = new SingleEvent.Builder("Test Event 2", sourceStart2, sourceEnd2)
            .description("Description 2")
            .location(Location.PHYSICAL)
            .build();
    firstCal.addEvent(sourceEvent2);

    LocalDateTime seriesStart = LocalDateTime.of(2023, 5, 17, 9, 0);
    LocalDateTime seriesEnd = LocalDateTime.of(2023, 5, 17, 9, 30);
    ISingleEvent singleEvent = new SingleEvent.Builder("Daily Meeting", seriesStart, seriesEnd)
            .description("Description 3")
            .build();
    CalendarEvent seriesEvent = new EventSeries(singleEvent,
            6, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);


    firstCal.addEvent(seriesEvent);

    IBetterCalendar resultCal = new BetterCalendar(timeZone2);

    LocalDateTime targetStart1 = LocalDateTime.of(2023, 5, 15, 16, 0);
    LocalDateTime targetEnd1 = LocalDateTime.of(2023, 5, 15, 17, 0);
    CalendarEvent targetEvent1 = new SingleEvent.Builder("Test Event 1", targetStart1, targetEnd1)
            .description("Description 1")
            .build();
    resultCal.addEvent(targetEvent1);

    LocalDateTime targetStart2 = LocalDateTime.of(2023, 5, 16, 20, 30);
    LocalDateTime targetEnd2 = LocalDateTime.of(2023, 5, 16, 21, 45);
    ISingleEvent targetEvent2 = new SingleEvent.Builder("Test Event 2", targetStart2, targetEnd2)
            .description("Description 2")
            .location(Location.PHYSICAL)
            .build();
    resultCal.addEvent(targetEvent2);

    LocalDateTime targetSeriesStart = LocalDateTime.of(2023, 5, 17, 15, 0);
    LocalDateTime targetSeriesEnd = LocalDateTime.of(2023, 5, 17, 15, 30);
    ISingleEvent singleEvent2 = new SingleEvent.Builder("Daily Meeting", targetSeriesStart,
            targetSeriesEnd)
            .description("Description 3")
            .build();
    CalendarEvent targetSeries = new EventSeries(singleEvent2, 6,
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY);
    resultCal.addEvent(targetSeries);

    IBetterCalendar actualTargetCal = firstCal.makeCalWithNewTimeZone(timeZone2);
    assertEquals(timeZone2, actualTargetCal.getZoneId().toString());

    List<ISingleEvent> actualEvents = actualTargetCal.query(LocalDateTime.MIN, LocalDateTime.MAX);
    List<ISingleEvent> expectedEvents = resultCal.query(LocalDateTime.MIN, LocalDateTime.MAX);

    assertEquals(expectedEvents.size(), actualEvents.size());

    for (int i = 0; i < expectedEvents.size(); i++) {
      assertEquals(expectedEvents.get(i), actualEvents.get(i));
    }
  }

}