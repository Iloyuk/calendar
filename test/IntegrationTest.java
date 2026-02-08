import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import controller.IController;
import controller.CommandLineController;
import model.application.ICalendarManager;
import model.application.CalendarManager;
import model.event.ISingleEvent;
import view.IView;
import view.CommandLineView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class verifying the correctness of our MVC architecture.
 */
public class IntegrationTest {
  private ByteArrayOutputStream outputStream;
  private ICalendarManager application;
  private IView view;
  private String setDefaultCalendar;

  @Before
  public void setup() {
    outputStream = new ByteArrayOutputStream();
    application = new CalendarManager();
    view = new CommandLineView(new PrintStream(outputStream));
    setDefaultCalendar = "create calendar --name test --timezone America/New_York\n"
            + "use calendar --name test\n";
  }

  @Test
  public void testIntegrationForAllDayEventCreationAndPrinting() {
    Readable command = new StringReader(setDefaultCalendar + "create event test on 2025-06-05\n"
            + "create event test2 on 2025-06-05\n"
            + "print events on 2025-06-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Calendar should create two all-day events on 2025-06-05
    String expectedModelOutput = "• [Subject: test, Start: 2025-06-05T08:00, End: 2025-06-05T17:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-06-05T08:00, End: 2025-06-05T17:00,"
            + " Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    // View should display the two all-day events on 2025-06-05
    String expectedViewOutput = "All events in test on 2025-06-05:\n"
            + "• [Subject: test, Start: 2025-06-05T08:00, End: 2025-06-05T17:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n• [Subject: test2, Start: 2025-06-05T08:00, End:"
            + " 2025-06-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForInvalidEventSeriesCreation() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-06T08:00 repeats MTWRFSU until 2025-07-01\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Make sure the model doesn't actually create anything
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();
    assertTrue(events.isEmpty());

    // View should display error message because event doesn't start and end on the same day
    String expectedViewOutput = "Error in 'create event test from 2025-06-05T12:00 to "
            + "2025-06-06T08:00 repeats MTWRFSU until 2025-07-01': Event must start and end "
            + "on the same day.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForCreatingAndEditingEventSeriesStartDate() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit events start test from 2025-06-10T12:00 with 2025-06-12T12:30\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Tests if model moves the event creation upwards to the first valid day of week as well
    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-16T12:30, End: 2025-06-16T13:30,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-17T12:30, End: 2025-06-17T13:30,"
            + " Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    // View should display two separate print events commands; one before and one after the edit
    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n• [Subject: test, Start: 2025-06-10T12:00,"
            + " End: 2025-06-10T13:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\nAll events in test from 2025-06-01T00:00 to"
            + " 2025-07-01T00:00:\n• [Subject: test, Start: 2025-06-09T12:00,"
            + " End: 2025-06-09T13:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-16T12:30, End: 2025-06-16T13:30,"
            + " Description: N/A, Location: N/A, Status: N/A]\n• [Subject: test,"
            + " Start: 2025-06-17T12:30, End: 2025-06-17T13:30, Description: N/A, "
            + "Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForCreatingAndEditingEventSeriesStartDatePastDay() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit events start test from 2025-06-10T12:00 with 2025-06-09T12:00\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Model should only have two events now, since we moved the series to overlap with an event
    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00,"
            + " Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsEditingEventSeriesPastStartDay() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit series start test from 2025-06-10T12:00 with 2025-06-09T12:00\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Model should display the same events, unchanged, before and after
    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "Error in 'edit series start test from 2025-06-10T12:00 with 2025-06-09T12:00': "
            + "Cannot change startDate to a different day, use the 'edit events' command instead!\n"
            + "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForCreatingAndEditingEntireEventSeriesDescription() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit series description test from 2025-06-10T12:00 with \"new desc\"\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: new desc, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description:"
            + " new desc, Location: N/A, Status: N/A]\n• [Subject: test, Start: 2025-06-11T12:00,"
            + " End: 2025-06-11T13:00, Description: new desc, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n• [Subject: test, Start: 2025-06-10T12:00,"
            + " End: 2025-06-10T13:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\nAll events in test from 2025-06-01T00:00 to"
            + " 2025-07-01T00:00:\n• [Subject: test, Start: 2025-06-09T12:00,"
            + " End: 2025-06-09T13:00, Description: new desc, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00,"
            + " Description: new desc, Location: N/A, Status: N/A]\n• [Subject: test,"
            + " Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: new desc, "
            + "Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForCreatingAndEditingEntireEventSeriesLocation() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit series location test from 2025-06-10T12:00 with physical\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: N/A, Location: Physical, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: Physical, Status: N/A]\n• [Subject: test, Start: 2025-06-11T12:00,"
            + " End: 2025-06-11T13:00, Description: N/A, Location: Physical, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n• [Subject: test, Start: 2025-06-10T12:00,"
            + " End: 2025-06-10T13:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\nAll events in test from 2025-06-01T00:00 to"
            + " 2025-07-01T00:00:\n• [Subject: test, Start: 2025-06-09T12:00,"
            + " End: 2025-06-09T13:00, Description: N/A, Location: Physical, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00,"
            + " Description: N/A, Location: Physical, Status: N/A]\n• [Subject: test,"
            + " Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A, "
            + "Location: Physical, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationForCreatingAndEditingEntireEventSeriesStatus() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-06-05T12:00 to"
            + " 2025-06-05T13:00 repeats MTW for 3 times\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\n"
            + "edit series status test from 2025-06-10T12:00 with public\n"
            + "print events from 2025-06-01T00:00 to 2025-07-01T00:00\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00,"
            + " Description: N/A, Location: N/A, Status: Public]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00, Description: N/A,"
            + " Location: N/A, Status: Public]\n• [Subject: test, Start: 2025-06-11T12:00,"
            + " End: 2025-06-11T13:00, Description: N/A, Location: N/A, Status: Public]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));

    String expectedViewOutput = "All events in test from 2025-06-01T00:00 to 2025-07-01T00:00:\n"
            + "• [Subject: test, Start: 2025-06-09T12:00, End: 2025-06-09T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n• [Subject: test, Start: 2025-06-10T12:00,"
            + " End: 2025-06-10T13:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\nAll events in test from 2025-06-01T00:00 to"
            + " 2025-07-01T00:00:\n• [Subject: test, Start: 2025-06-09T12:00,"
            + " End: 2025-06-09T13:00, Description: N/A, Location: N/A, Status: Public]\n"
            + "• [Subject: test, Start: 2025-06-10T12:00, End: 2025-06-10T13:00,"
            + " Description: N/A, Location: N/A, Status: Public]\n• [Subject: test,"
            + " Start: 2025-06-11T12:00, End: 2025-06-11T13:00, Description: N/A, "
            + "Location: N/A, Status: Public]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsInvalidEventCreation1() {
    // should not be able to create any events from these commands
    Readable command = new StringReader(setDefaultCalendar
            + "create event test\n"
            + "create events test\n"
            + "create event test a\n"
            + "create event test on\n"
            + "create event test on 2025-05\n"
            + "create event test from 2025-05-05T18:00 to\n"
            + "create event test from 2025-05-05T18:00 to 2025-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();
    assertTrue(events.isEmpty());

    String expectedViewOutput = "Error in 'create event test': Invalid input syntax, "
            + "incomplete command!\n"
            + "Error in 'create events test': Invalid input syntax, missing \"event\" "
            + "keyword!\n"
            + "Error in 'create event test a': Invalid input syntax, cannot parse next "
            + "keyword!\n"
            + "Error in 'create event test on': Invalid input syntax, incomplete "
            + "command!\n"
            + "Error in 'create event test on 2025-05': Invalid input syntax, cannot parse "
            + "date!\n"
            + "Error in 'create event test from 2025-05-05T18:00 to': Invalid input syntax, "
            + "incomplete command!\n"
            + "Error in 'create event test from 2025-05-05T18:00 to 2025-05': Invalid input "
            + "syntax, cannot parse date/time!\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsInvalidEventCreation2() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-05-05T10:00"
            + " to 2025-05-05T09:00\n"
            + "create event test from 2025-06-05T10:00 to 2025-06-05T11:00 repeats R for -1 times\n"
            + "create event test from 2025-06-05T10:00 to 2025-06-05T11:00 repeats D for 3 times\n"
            + "create event test from 2025-06-05T10:00 to 2025-06-06T11:00 repeats R for 3 times\n"
            + "create event test from 2025-06-05T10:00 to 2025-06-05T11:00 repeats R until "
            + "2025-06-04\n"
            + "create event test on 2025-06-05 repeats D for 3 times\n"
            + "create event test on 2025-06-05 repeats R for -1 times\n"
            + "create event test on 2025-06-05 repeats R until 2025-06-04\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "";
    assertEquals(expectedModelOutput, events.stream().map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertTrue(events.isEmpty());

    String expectedViewOutput = "Error in 'create event test from 2025-05-05T10:00 to "
            + "2025-05-05T09:00': Event start date/time cannot be before end date/time!\n"
            + "Error in 'create event test from 2025-06-05T10:00 to 2025-06-05T11:00 "
            + "repeats R for -1 times': Number of occurrences must be at least 1.\n"
            + "Error in 'create event test from 2025-06-05T10:00 to 2025-06-05T11:00 "
            + "repeats D for 3 times': Invalid input syntax, cannot parse days of week!\n"
            + "Error in 'create event test from 2025-06-05T10:00 to 2025-06-06T11:00 "
            + "repeats R for 3 times': Event must start and end on the same day.\n"
            + "Error in 'create event test from 2025-06-05T10:00 to 2025-06-05T11:00 "
            + "repeats R until 2025-06-04': Event series cannot end before the current event "
            + "ends.\nError in 'create event test on 2025-06-05 repeats D for 3 times': Invalid "
            + "input syntax, cannot parse days of week!\n"
            + "Error in 'create event test on 2025-06-05 repeats R for -1 times': Number "
            + "of occurrences must be at least 1.\n"
            + "Error in 'create event test on 2025-06-05 repeats R until 2025-06-04': "
            + "Event series cannot end before the current event ends.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsInvalidEventEditing() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-05-05T08:00"
            + " to 2025-05-05T10:00 repeats MTW for 3 times\n"
            + "edit event subject tests from 2025-05-05T08:00 to 2025-05-05T10:00 with e\n"
            + "edit events\n"
            + "create event subject test from 2025-05-05T08:00 to 2025-05-05T11:00 with a\n"
            + "create event subject test from 2025-05 to 2025-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();
    assertEquals(3, events.size());

    String expectedViewOutput = "Error in 'edit event subject tests from 2025-05-05T08:00 to "
            + "2025-05-05T10:00 with e': Event does not exist in the calendar.\n"
            + "Error in 'edit events': Invalid input syntax, incomplete command!\n"
            + "Error in 'create event subject test from 2025-05-05T08:00 to 2025-05-05T11:00 with "
            + "a': Invalid input syntax, cannot parse next keyword!\n"
            + "Error in 'create event subject test from 2025-05 to 2025-05': Invalid input syntax, "
            + "cannot parse next keyword!\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationDisplaysEventStatus() {
    Readable command = new StringReader(setDefaultCalendar
            + "create event test from 2025-05-05T08:00"
            + " to 2025-05-05T10:00 repeats MTW for 3 times\n"
            + "show status on 2025-05-04T09:00\n" // should be free
            + "show status on 2025-05-05T09:00\n" // should be busy
            + "show status on 2025-05-06T09:00\n" // should be busy
            + "show status on 2025-05-07T09:00\n" // should be busy
            + "show status on 2025-05-08T09:00\nexit"); // should be free
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Status is available on 2025-05-04T09:00.\n"
            + "Status is busy on 2025-05-05T09:00.\n"
            + "Status is busy on 2025-05-06T09:00.\n"
            + "Status is busy on 2025-05-07T09:00.\n"
            + "Status is available on 2025-05-08T09:00.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationWorksWithDifferentCalendars() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event Event1 on 2025-05-05"
            + "\ncreate event Event2 on 2025-05-05"
            + "\nuse calendar --name PST"
            + "\ncreate event Event3 on 2025-05-05"
            + "\nuse calendar --name GMT\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // EST calendar should have two events
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "• [Subject: Event1, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Event2, Start: 2025-05-05T08:00, End: 2025-05-05T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(2, events.size());

    // PST calendar should have one event
    application.setCalendarInUse("PST");
    events = application.getCurrentCalendar().query(LocalDateTime.MIN, LocalDateTime.MAX);

    expectedModelOutput = "• [Subject: Event3, Start: 2025-05-05T08:00, End: 2025-05-05T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(1, events.size());

    // View should print an invalid switch to a nonexistent calendar
    String expectedViewOutput = "Error in 'use calendar --name GMT': The calendar to be set in "
            + "use does not exist.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationWorksWithEditingCalendarNames() {
    Readable command = new StringReader("create calendar --name China --timezone Asia/Shanghai\n"
            + "create calendar --name America --timezone America/New_York\n"
            + "use calendar --name China\n"
            + "print events on 2025-05-05\n"
            + "edit calendar --name America --property name \"New America\"\n"
            + "print events on 2025-05-05\n"
            + "edit calendar --name China --property name \"New China\"\n"
            + "print events on 2025-05-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Current calendar name should only change when we're editing the current calendar in use
    String expectedViewOutput = "All events in China on 2025-05-05:\n"
            + "All events in China on 2025-05-05:\n"
            + "All events in New China on 2025-05-05:\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationWorksWithEditingCalendarTimezoneWithSingleEvents() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\nuse calendar --name EST"
            + "\ncreate event test from 2025-05-05T10:00 to 2025-05-06T10:00"
            + "\nprint events from 2025-05-05T00:00 to 2025-05-06T23:59:59"
            + "\nedit calendar --name EST --property timezone America/Los_Angeles"
            + "\ncreate event newTest on 2025-05-05"
            + "\nprint events from 2025-05-05T00:00 to 2025-05-06T23:59:59"
            + "\nedit calendar --name EST --property timezone America/New_York"
            + "\nprint events from 2025-05-05T00:00 to 2025-05-06T23:59:59\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    String expectedModelOutput = "• [Subject: test, Start: 2025-05-05T10:00, End: 2025-05-06T10:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: newTest, Start: 2025-05-05T11:00, End: 2025-05-05T20:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(2, events.size());

    String expectedViewOutput = "All events in EST from 2025-05-05T00:00 to 2025-05-06T23:59:59:\n"
            + "• [Subject: test, Start: 2025-05-05T10:00, End: 2025-05-06T10:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "All events in EST from 2025-05-05T00:00 to 2025-05-06T23:59:59:\n"
            + "• [Subject: test, Start: 2025-05-05T07:00, End: 2025-05-06T07:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: newTest, Start: 2025-05-05T08:00, End: 2025-05-05T17:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "All events in EST from 2025-05-05T00:00 to 2025-05-06T23:59:59:\n"
            + "• [Subject: test, Start: 2025-05-05T10:00, End: 2025-05-06T10:00, Description: N/A,"
            + " Location: N/A, Status: N/A]\n"
            + "• [Subject: newTest, Start: 2025-05-05T11:00, End: 2025-05-05T20:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationWorksWithEditingCalendarTimezoneWithEventSeries() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\nuse calendar --name EST"
            + "\ncreate event Series from 2025-05-05T20:00 to 2025-05-05T21:00 repeats R for "
            + "3 times\nprint events from 2025-05-01T00:00 to 2025-06-01T23:59:59"
            + "\nedit calendar --name EST --property timezone Asia/Shanghai"
            + "\nprint events from 2025-05-01T00:00 to 2025-06-01T23:59:59"
            + "\nedit events subject Series from 2025-05-09T08:00 with \"New Series\""
            + "\nprint events from 2025-05-01T00:00 to 2025-06-01T23:59:59\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Make sure the series stays a series and all the times get moved +12 hours
    String expectedModelOutput = "• [Subject: New Series, Start: 2025-05-09T08:00, "
            + "End: 2025-05-09T09:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: New Series, Start: 2025-05-16T08:00, End: 2025-05-16T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: New Series, Start: 2025-05-23T08:00, End: 2025-05-23T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());

    String expectedViewOutput = "All events in EST from 2025-05-01T00:00 to 2025-06-01T23:59:59:\n"
            + "• [Subject: Series, Start: 2025-05-08T20:00, End: 2025-05-08T21:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Series, Start: 2025-05-15T20:00, End: 2025-05-15T21:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Series, Start: 2025-05-22T20:00, End: 2025-05-22T21:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "All events in EST from 2025-05-01T00:00 to 2025-06-01T23:59:59:\n"
            + "• [Subject: Series, Start: 2025-05-09T08:00, End: 2025-05-09T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Series, Start: 2025-05-16T08:00, End: 2025-05-16T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Series, Start: 2025-05-23T08:00, End: 2025-05-23T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "All events in EST from 2025-05-01T00:00 to 2025-06-01T23:59:59:\n"
            + "• [Subject: New Series, Start: 2025-05-09T08:00, End: 2025-05-09T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: New Series, Start: 2025-05-16T08:00, End: 2025-05-16T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: New Series, Start: 2025-05-23T08:00, End: 2025-05-23T09:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsCreatingCalendarWithInvalidTimezone() {
    Readable command = new StringReader("create calendar --name EST --timezone abnd\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'create calendar --name EST --timezone abnd': "
            + "Invalid timezone specified!\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsUsingInvalidCalendar() {
    Readable command = new StringReader("use calendar --name EST\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'use calendar --name EST': The calendar to be "
            + "set in use does not exist.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsCopyingEventsToInvalidCalendar() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-05"
            + "\ncopy events on 2025-05-05 --target PST to 2025-06-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'copy events on 2025-05-05 --target PST to 2025-06-05':"
            + " Could not find target calendar to copy event to!\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsCopyingEventsWithInvalidInterval() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-05"
            + "\ncopy events between 2025-05-05 and 2025-05-04 --target PST to 2025-06-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'copy events between 2025-05-05 and 2025-05-04 --target"
            + " PST to 2025-06-05': End date cannot be before the starting date.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsCopyingEventsWithBadDateTimeInput() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-05"
            + "\ncopy events on 2025-05-05 --target PST to 2025-06\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'copy events on 2025-05-05 --target PST to 2025-06': "
            + "Invalid input syntax, cannot parse date!\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationRejectsCopyingEventToSameEventInSameCalendar() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-05"
            + "\ncopy events on 2025-05-05 --target EST to 2025-05-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    String expectedViewOutput = "Error in 'copy events on 2025-05-05 --target EST to 2025-05-05': "
            + "Event cannot be added to the calendar.\n";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationCopiesSingleEventsToDifferentDateTime() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test1 on 2025-05-05"
            + "\ncreate event test2 from 2025-05-05T12:00 to 2025-05-05T15:00"
            + "\ncopy events on 2025-05-05 --target PST to 2025-06-05");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // Application should have 2 events in the EST calendar
    String expectedModelOutput = "• [Subject: test1, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-05-05T12:00, End: 2025-05-05T15:00,"
            + " Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(2, events.size());

    // Set current calendar to PST now
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should have the same 2 events, but at different date AND moved back by 3 hours
    expectedModelOutput = "• [Subject: test1, Start: 2025-06-05T05:00, End: 2025-06-05T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-06-05T09:00, End: 2025-06-05T12:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(2, events.size());
  }

  @Test
  public void testIntegrationCopiesAllEventsOnDayToAnotherDay() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test1 on 2025-05-05"
            + "\ncreate event test2 on 2025-05-05 repeats M for 3 times"
            + "\ncopy events on 2025-05-05 --target PST to 2025-06-05\nexit");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have 4 events; 1 single event and 1 event series with 3 events
    String expectedModelOutput = "• [Subject: test1, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-05-05T08:00, End: 2025-05-05T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-05-12T08:00, End: 2025-05-12T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-05-19T08:00, End: 2025-05-19T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(4, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should only have 2 events
    expectedModelOutput = "• [Subject: test1, Start: 2025-06-05T05:00, End: 2025-06-05T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-06-05T05:00, End: 2025-06-05T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(2, events.size());

    String expectedViewOutput = "";
    assertEquals(expectedViewOutput, outputStream.toString());
  }

  @Test
  public void testIntegrationCopiesAllSingleEventsInIntervalToAnotherDay() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test1 on 2025-05-05"
            + "\ncreate event test2 on 2025-05-06"
            + "\ncreate event test3 on 2025-05-07"
            + "\ncopy events between 2025-05-05 and 2025-05-07 --target PST to 2025-06-05");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have 3 single events
    String expectedModelOutput = "• [Subject: test1, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-05-06T08:00, End: 2025-05-06T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test3, Start: 2025-05-07T08:00, End: 2025-05-07T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should also have 3 single events, but moved starting to 2025-06-05 and back 3h
    expectedModelOutput = "• [Subject: test1, Start: 2025-06-05T05:00, End: 2025-06-05T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test2, Start: 2025-06-06T05:00, End: 2025-06-06T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test3, Start: 2025-06-07T05:00, End: 2025-06-07T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());
  }

  @Test
  public void testIntegrationCopiesAllEventSeriesInIntervalToAnotherDay() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-05 repeats M for 3 times"
            + "\ncopy events between 2025-05-05 and 2025-05-25 --target PST to 2025-06-05");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have an event series of 3 events
    String expectedModelOutput = "• [Subject: test, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-05-12T08:00, End: 2025-05-12T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-05-19T08:00, End: 2025-05-19T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should also have an event series of 3 events
    expectedModelOutput = "• [Subject: test, Start: 2025-06-05T05:00, End: 2025-06-05T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-12T05:00, End: 2025-06-12T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: test, Start: 2025-06-19T05:00, End: 2025-06-19T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());
  }

  @Test
  public void testIntegrationCopiesMixOfSingleEventsAndEventSeriesInIntervalToAnotherDay() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test on 2025-05-04"
            + "\ncreate event testSeries on 2025-05-05 repeats M for 3 times"
            + "\ncopy events between 2025-05-03 and 2025-05-20 --target PST to 2025-06-05");
    // ^^ The first event's start is one day AFTER the interval start, so the copied first
    // event should also be one day after the "to" date
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have 4 events
    String expectedModelOutput = "• [Subject: test, Start: 2025-05-04T08:00, "
            + "End: 2025-05-04T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-05-05T08:00, End: 2025-05-05T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-05-12T08:00, End: 2025-05-12T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-05-19T08:00, End: 2025-05-19T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(4, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should also have 4 events
    expectedModelOutput = "• [Subject: test, Start: 2025-06-06T05:00, "
            + "End: 2025-06-06T14:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-06-07T05:00, End: 2025-06-07T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-06-14T05:00, End: 2025-06-14T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-06-21T05:00, End: 2025-06-21T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(4, events.size());
  }

  @Test
  public void testIntegrationRestructuresEventSeriesWhenCopiedToDifferentDaysOfWeek() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event testSeries on 2025-05-05 repeats M for 3 times"
            + "\ncopy events between 2025-05-05 and 2025-05-20 --target PST to 2025-06-05"
            + "\nuse calendar --name PST"
            + "\nedit series subject testSeries from 2025-06-05T05:00 with \"new testSeries\"");
    // ^^ only the first testSeries should be changed to "new testSeries"
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have 3 events
    String expectedModelOutput = "• [Subject: testSeries, Start: 2025-05-05T08:00, "
            + "End: 2025-05-05T17:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-05-12T08:00, End: 2025-05-12T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-05-19T08:00, End: 2025-05-19T17:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar's event series should now be split, no longer being an event series, which
    // means changing the name from testSeries -> "new testSeries" should only change the first one
    expectedModelOutput = "• [Subject: new testSeries, Start: 2025-06-05T05:00, "
            + "End: 2025-06-05T14:00, Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-06-12T05:00, End: 2025-06-12T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: testSeries, Start: 2025-06-19T05:00, End: 2025-06-19T14:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(3, events.size());
  }

  @Test
  public void testIntegrationCopiesMultiDayEvent() {
    Readable command = new StringReader("create calendar --name EST --timezone America/New_York"
            + "\ncreate calendar --name PST --timezone America/Los_Angeles"
            + "\nuse calendar --name EST"
            + "\ncreate event test from 2025-05-01T08:00 to 2025-05-05T08:00"
            + "\ncopy events between 2025-05-01 and 2025-05-05 --target PST to 2025-06-05");
    IController controller = new CommandLineController(command, application, view);
    controller.run();

    // Set current calendar to EST
    application.setCalendarInUse("EST");
    List<ISingleEvent> events = getAllEventsInCurrentCalendar();

    // EST calendar should have a multi-day event
    String expectedModelOutput = "• [Subject: test, Start: 2025-05-01T08:00, "
            + "End: 2025-05-05T08:00, Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(1, events.size());

    // Set current calendar to PST
    application.setCalendarInUse("PST");
    events = getAllEventsInCurrentCalendar();

    // PST calendar should retain the multi-day event
    expectedModelOutput = "• [Subject: test, Start: 2025-06-05T05:00, End: 2025-06-09T05:00, "
            + "Description: N/A, Location: N/A, Status: N/A]";
    assertEquals(expectedModelOutput, events.stream()
            .map(ISingleEvent::toString)
            .collect(Collectors.joining("\n")));
    assertEquals(1, events.size());
  }

  private List<ISingleEvent> getAllEventsInCurrentCalendar() {
    return application.getCurrentCalendar().query(LocalDateTime.MIN, LocalDateTime.MAX);
  }

}