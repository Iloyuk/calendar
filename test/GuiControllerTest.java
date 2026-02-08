import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import controller.Features;
import controller.GuiController;
import model.application.ICalendarManager;
import model.calendar.BetterCalendar;
import model.calendar.IBetterCalendar;
import model.event.CalendarEvent;
import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.Location;
import model.event.ReadOnlyCalendarEvent;
import model.event.SingleEvent;
import model.event.Status;
import view.IGUIView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Class for testing the functionality of our GUI controller.
 */
public class GuiControllerTest {
  private StringBuilder log;
  private Features controller;
  private LocalDateTime start;
  private LocalDateTime end;
  private ReadOnlyCalendarEvent testEvent;

  @Before
  public void setup() {
    log = new StringBuilder();
    controller = new GuiController(new MockCalendarManager(log), new MockView(log));

    start = LocalDateTime.of(2023, 10, 1, 10, 0);
    end = LocalDateTime.of(2023, 10, 1, 11, 0);
    testEvent = new SingleEvent.Builder("test", start, end)
            .description("desc")
            .location(Location.ONLINE)
            .status(Status.PRIVATE)
            .build();
  }

  @Test
  public void testCreateEventWithBlankSubject() {
    controller.createEvent("   ", start, end, "test desc", null, null);
    String expected = "writeError called with message: Subject is empty, cannot add event!\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testCreateEventSubjectTooLong() {
    controller.createEvent("longgggggggggggggggggggggggggggggggggggggggggggggggggggg"
                    + "gggggggggggggggggggggggggggggggggggggggg", start, end,
            "Description", null, null);

    String expected = "writeError called with message: Subject is too long, cannot add event!\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testCreateEventSuccess() {
    controller.createEvent("Meeting", start, end,
            "Discuss project", Location.PHYSICAL, Status.PUBLIC);

    // Make sure that the controller successfully adds the event and
    String expectedContains = "event added: subject=Meeting,start=2023-10-01T10:00,"
            + "end=2023-10-01T11:00,desc=Discuss project,location=Physical,status=Public"
            + "\nqueried events with: start=2023-10-01T00:00,"
            + "end=+999999999-12-31T23:59:59.999999999";
    assertTrue(log.toString().contains(expectedContains));
  }

  @Test
  public void testShowEventsFrom() {
    controller.showEventsFrom(LocalDate.of(2025, 7, 1));

    String expectedContains = "queried events with: start=2025-07-01T00:00,"
            + "end=+999999999-12-31T23:59:59.999999999";
    assertTrue(log.toString().contains(expectedContains));
  }

  @Test
  public void testChangeCalendar() {
    controller.changeCalendar("Test Calendar");

    String expected = "calendar set to: Test Calendar\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testAddCalendarRejectsSpecificInstance() {
    controller.addCalendar("New Calendar", "America/New_York");

    // We reject adding "New Calendar" since we use that to make the pop-up for creating a calendar
    String expected = "writeError called with message: Cannot add this calendar name.\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testAddCalendarRejectsEmptyCalendarName() {
    controller.addCalendar("   ", "America/New_York");

    String expected = "writeError called with message: Cannot add calendar with a blank name!\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testAddCalendarSuccessful() {
    controller.addCalendar("PST", "America/Los_Angeles");

    // Make sure controller adds the name to the model, sets the current calendar to the new one,
    // and updates the view's calendar dropdown box with the new calendar
    String expected = "calendar added: name=PST,timezone=America/Los_Angeles\n"
            + "calendar set to: PST\n"
            + "addCalendarName called with calendarName: PST\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testEditEventRejectsBlankSubject() {
    controller.editEvent(testEvent, "subject", "    ");

    String expected = "writeError called with message: Cannot edit event, subject is empty!\n";
    assertEquals(expected, log.toString());
  }

  @Test
  public void testEditEventRejectsSubjectTooLong() {
    controller.editEvent(testEvent, "subject", "longgggggggggggggggggggggggggggggggg"
            + "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggg");

    String expected = "writeError called with message: Subject is too long, cannot update event!\n";
    assertEquals(expected, log.toString());
  }

  @Test(expected = NullPointerException.class)
  public void testEditEventSuccessful() {
    // Should throw a NullPointerException since it passed all the preconditions
    // but there's no event to actually edit
    controller.editEvent(testEvent, "subject", "new");
  }

  /**
   * Mock application class for helping test the correctness of our controller.
   */
  private static class MockCalendarManager implements ICalendarManager {
    private final IBetterCalendar mockCalendar;
    private final StringBuilder log;

    private MockCalendarManager(StringBuilder log) {
      mockCalendar = new MockCalendar(log);
      this.log = log;
    }

    @Override
    public void createCalendar(String calendarName, String timeZone) {
      log.append("calendar added: ")
              .append("name=").append(calendarName)
              .append(",timezone=").append(timeZone)
              .append("\n");
    }

    @Override
    public String getCurrentCalendarName() {
      return "";
    }

    @Override
    public IBetterCalendar getCurrentCalendar() {
      return mockCalendar;
    }

    @Override
    public void editCalendarTimeZone(String calendarName, String timezone) {
      log.append("calendar timezone edited: ")
              .append("name=").append(calendarName)
              .append(",new timezone=").append(timezone)
              .append("\n");
    }

    @Override
    public void editCalendarName(String oldName, String newName) {
      log.append("calendar name edited: ")
              .append("old=").append(oldName)
              .append(",new=").append(newName)
              .append("\n");
    }

    @Override
    public void setCalendarInUse(String calendarName) {
      log.append("calendar set to: ").append(calendarName).append("\n");
    }

    @Override
    public void copyEvent(String eventName, LocalDateTime eventStartDateTime, String targetCalendar,
                          LocalDateTime newStartDateTime) {
      log.append("event copied: ")
              .append("name=").append(eventName)
              .append(",start=").append(eventStartDateTime.toString())
              .append(",target=").append(targetCalendar)
              .append(",new-start=").append(newStartDateTime.toString())
              .append("\n");
    }

    @Override
    public void copyEvents(LocalDateTime start, LocalDateTime end, String targetCalendar,
                           LocalDate startDate) {
      log.append("events copied: ")
              .append("start-interval=").append(start.toString())
              .append(",end-interval=").append(end.toString())
              .append(",target=").append(targetCalendar)
              .append(",start-date").append(startDate.toString())
              .append("\n");
    }
  }

  /**
   * Mock calendar class for helping test the correctness of our controller.
   */
  private static class MockCalendar implements IBetterCalendar {
    private final StringBuilder log;

    public MockCalendar(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void addEvent(CalendarEvent event) {
      log.append("event added: subject=").append(event.getSubject())
              .append(",start=").append(event.getStartDateTime())
              .append(",end=").append(event.getEndDateTime())
              .append(",desc=").append(event.getDescription())
              .append(",location=").append(event.getLocation())
              .append(",status=").append(event.getStatus());
      log.append("\n");
    }

    @Override
    public void editEvent(String property, String subject, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, String newValue) {
      log.append("edited event: ");
      log.append("subject=").append(subject)
              .append(",start=").append(startDateTime.toString())
              .append(",end=").append(endDateTime.toString())
              .append(",property=").append(property)
              .append(",new=").append(newValue).append("\n");
    }

    @Override
    public void editEvents(String property, String subject, LocalDateTime startDateTime,
                           String commandType, String newValue) {
      if (commandType.equals("events")) {
        log.append("edited events: ");
      } else if (commandType.equals("series")) {
        log.append("edited series: ");
      }
      log.append("subject=").append(subject)
              .append(",start=").append(startDateTime.toString())
              .append(",property=").append(property)
              .append(",new=").append(newValue).append("\n");
    }

    @Override
    public List<ISingleEvent> query(LocalDateTime start, LocalDateTime end) {
      log.append("queried events with: start=").append(start.toString())
              .append(",end=").append(end.toString())
      .append("\n");
      return List.of(); // we don't care about the return value
    }

    @Override
    public IEventSeries isInSeries(ISingleEvent original) {
      // no use for this method in the mock
      return null;
    }

    @Override
    public boolean containsTime(LocalDateTime dateTime) {
      log.append("showed status with: date/time=").append(dateTime.toString())
      .append("\n");
      return false; // we don't care about the return value
    }

    @Override
    public boolean canAddSingleEvent(ISingleEvent newEvent) {
      // no use for this method in the mock
      return false;
    }

    @Override
    public SingleEvent getSingleEventWithStartAndEndDate(String subject,
                                                         LocalDateTime startDateTime,
                                                         LocalDateTime endDateTime) {
      log.append("edited event: subject=").append(subject)
              .append(",start=").append(startDateTime.toString())
              .append(",end=").append(endDateTime.toString());
      return null; // we don't care about the return value
    }

    @Override
    public List<ISingleEvent> getSingleEventsWithStartDate(String subject,
                                                           LocalDateTime startDateTime) {
      // no use for this method in the mock
      return List.of();
    }

    @Override
    public ZoneId getZoneId() {
      // no use for this method in the mock
      return null;
    }

    @Override
    public BetterCalendar makeCalWithNewTimeZone(String newTimeZone) {
      // no use for this method in the mock
      return null;
    }
  }

  /**
   * Mock view class for helping test the correctness of our controller.
   */
  public static class MockView implements IGUIView {
    StringBuilder log;

    public MockView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void writeError(String errorMsg) {
      log.append("writeError called with message: ")
              .append(errorMsg).append("\n");
    }

    @Override
    public void addFeatures(Features features) {
      // no use for this method in the mock
    }

    @Override
    public void addCalendarName(String calendarName) {
      log.append("addCalendarName called with calendarName: ")
              .append(calendarName).append("\n");
    }

    @Override
    public void goToPreviousCalendar() {
      // no use for this method in the mock
    }

    @Override
    public void writeMessage(String message) {
      log.append("writeMessage called with message: ")
              .append(message).append("\n");
    }

    @Override
    public void showEvents(List<ReadOnlyCalendarEvent> events) {
      // no use for this method in the mock
    }
  }

}