import org.junit.Before;
import org.junit.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import model.event.IEventSeries;
import model.event.EventSeries;
import model.event.ISingleEvent;
import model.event.SingleEvent;

import static org.junit.Assert.assertEquals;

/**
 * Test class verifying the correctness of the {@code EventSeries} class.
 */
public class EventSeriesTest {
  private ISingleEvent singleEvent;
  private ISingleEvent withEndDateEvent;
  private IEventSeries series;
  private List<ISingleEvent> events;

  @Before
  public void setup() {
    singleEvent = new SingleEvent.Builder("Birthday", LocalDate.of(2006, 7, 1))
            .build();
    withEndDateEvent = new SingleEvent.Builder("Get Coffee",
            LocalDateTime.of(2007, 5, 1, 10, 30, 0),
            LocalDateTime.of(2007, 5, 1, 12, 30, 0))
            .build();
    series = new EventSeries(singleEvent, 5,
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    events = series.getEvents();
  }

  @Test
  public void testEventSeriesConstructorWithAllDayEvent() {
    series = new EventSeries(singleEvent, LocalDate.of(2006, 7, 31),
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    assertEquals("Birthday", series.getSubject());
    // No time was provided for singleEvent, so constructor defaults to 8AM-5PM
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 30, 17, 0, 0), series.getEndDateTime());
  }

  @Test
  public void testEventSeriesConstructorWithStartingAndEndingDate() {

    series = new EventSeries(withEndDateEvent, LocalDate.of(2007, 5, 31),
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY);


    assertEquals("Get Coffee", series.getSubject());
    assertEquals(LocalDateTime.of(2007, 5, 1, 10, 30, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 29, 12, 30, 0), series.getEndDateTime());
    assertEquals(9, series.getEvents().size());
    assertEquals(LocalDateTime.of(2007, 5, 1, 10, 30, 0),
            series.getEvents().get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 7, 10, 30, 0),
            series.getEvents().get(1).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 8, 10, 30, 0),
            series.getEvents().get(2).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 14, 10, 30, 0),
            series.getEvents().get(3).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 15, 10, 30, 0),
            series.getEvents().get(4).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 21, 10, 30, 0),
            series.getEvents().get(5).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 22, 10, 30, 0),
            series.getEvents().get(6).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 28, 10, 30, 0),
            series.getEvents().get(7).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 29, 10, 30, 0),
            series.getEvents().get(8).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 29, 12, 30, 0),
            series.getEvents().get(8).getEndDateTime());
  }

  @Test
  public void testEventSeriesConstructorWithOccurrencesAllDay() {

    series = new EventSeries(singleEvent, 3, DayOfWeek.SATURDAY);

    assertEquals("Birthday", series.getSubject());
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0,
            0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 15, 17, 0,
            0), series.getEndDateTime());
    assertEquals(3, series.getEvents().size());
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0,
                    0),
            series.getEvents().get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 8, 8, 0, 0),
            series.getEvents().get(1).getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 15, 8, 0,
                    0),
            series.getEvents().get(2).getStartDateTime());
  }

  @Test
  public void testEventSeriesConstructorWithEventSeriesOccurrences() {

    series = new EventSeries(withEndDateEvent, 4, DayOfWeek.THURSDAY, DayOfWeek.TUESDAY);

    assertEquals("Get Coffee", series.getSubject());
    assertEquals(LocalDateTime.of(2007, 5, 1, 10, 30, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 10, 12, 30, 0), series.getEndDateTime());
    assertEquals(4, series.getEvents().size());
    assertEquals(LocalDateTime.of(2007, 5, 1, 10, 30, 0),
            series.getEvents().get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 3, 10, 30, 0),
            series.getEvents().get(1).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 8, 10, 30, 0),
            series.getEvents().get(2).getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 10, 10, 30, 0),
            series.getEvents().get(3).getStartDateTime());
  }

  @Test
  public void testListOfSingleEventConstructor() {
    series = new EventSeries(events, LocalDate.of(2006, 7, 15), series.getOccurringDays());

    assertEquals("Birthday", series.getSubject());
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 15, 17, 0, 0), series.getEndDateTime());
    assertEquals(5, series.getEvents().size());
  }

  @Test
  public void testOccurrenceConstructorHandlesWrongStartDate() {
    series = new EventSeries(singleEvent, LocalDate.of(2006, 7, 20), DayOfWeek.SUNDAY);

    assertEquals("Birthday", series.getSubject());
    assertEquals(LocalDateTime.of(2006, 7, 2, 8, 0, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 16, 17, 0, 0), series.getEndDateTime());
    assertEquals(3, series.getEvents().size());
  }

  @Test
  public void testEndDateConstructorHandlesWrongStartDate() {
    series = new EventSeries(withEndDateEvent, LocalDate.of(2007, 5, 20), DayOfWeek.MONDAY);

    assertEquals("Get Coffee", series.getSubject());
    assertEquals(LocalDateTime.of(2007, 5, 7, 10, 30, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 14, 12, 30, 0), series.getEndDateTime());
    assertEquals(2, series.getEvents().size());
  }

  @Test
  public void testOccurrenceConstructorHandlesWrongStartDateMultiDate() {
    series = new EventSeries(singleEvent, LocalDate.of(2006, 7, 20),
            DayOfWeek.THURSDAY, DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY);

    assertEquals("Birthday", series.getSubject());
    assertEquals(LocalDateTime.of(2006, 7, 2, 8, 0, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 20, 17, 0, 0), series.getEndDateTime());
    assertEquals(9, series.getEvents().size());
  }

  @Test
  public void testEndDateConstructorHandlesWrongStartDateMultiDate() {
    series = new EventSeries(withEndDateEvent, LocalDate.of(2007, 5, 20),
            DayOfWeek.MONDAY, DayOfWeek.SUNDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    assertEquals("Get Coffee", series.getSubject());
    assertEquals(LocalDateTime.of(2007, 5, 3, 10, 30, 0), series.getStartDateTime());
    assertEquals(LocalDateTime.of(2007, 5, 20, 12, 30, 0), series.getEndDateTime());
    assertEquals(11, series.getEvents().size());
  }

  @Test
  public void testGetEvents() {
    //This method is mainly already tested in another tests
    series = new EventSeries(singleEvent, LocalDate.of(2006, 7, 15), series.getOccurringDays());
    List<ISingleEvent> events = series.getEvents();

    assertEquals(5, events.size());
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0, 0), events.get(0).getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 15, 17, 0, 0), events.get(4).getEndDateTime());
    assertEquals("Birthday", events.get(0).getSubject());
  }

  @Test
  public void testGetOccurringDays() {
    //This method is mainly already tested in another tests
    series = new EventSeries(singleEvent, LocalDate.of(2006, 7, 15), series.getOccurringDays());
    DayOfWeek[] occurringDays = series.getOccurringDays();

    assertEquals(2, occurringDays.length);
    assertEquals(DayOfWeek.SATURDAY, occurringDays[0]);
    assertEquals(DayOfWeek.SUNDAY, occurringDays[1]);

    series = new EventSeries(withEndDateEvent, LocalDate.of(2007, 5, 20),
            DayOfWeek.MONDAY, DayOfWeek.SUNDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    occurringDays = series.getOccurringDays();

    assertEquals(4, occurringDays.length);
    assertEquals(DayOfWeek.MONDAY, occurringDays[0]);
    assertEquals(DayOfWeek.SUNDAY, occurringDays[1]);
    assertEquals(DayOfWeek.THURSDAY, occurringDays[2]);
    assertEquals(DayOfWeek.FRIDAY, occurringDays[3]);
  }

}