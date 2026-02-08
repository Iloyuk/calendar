import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import controller.IController;
import controller.CommandLineController;
import model.application.ICalendarManager;
import model.calendar.BetterCalendar;
import model.calendar.IBetterCalendar;
import model.event.CalendarEvent;
import model.event.EventSeries;
import model.event.IEventSeries;
import model.event.ISingleEvent;
import model.event.ReadOnlyCalendarEvent;
import model.event.SingleEvent;
import view.IView;

import static org.junit.Assert.assertEquals;

/**
 * Class testing the correctness of our {@code InteractiveController} object.
 */
public class CommandLineControllerTest {
  private StringBuilder log;
  private ICalendarManager mock;
  private IView view;
  private IController controller;
  private String initialInput;
  private String initialOutput;

  @Before
  public void setup() {
    log = new StringBuilder();
    mock = new MockCalendarManager(log);
    view = new MockView(log);
    initialInput = "create calendar --name test --timezone America/New_York\n"
            + "use calendar --name test\n";
    initialOutput = "calendar added: name=test,timezone=America/New_York\n"
            + "calendar set to: test\n";
  }

  /*
   * Create event command tests
   */
  @Test
  public void testControllerRejectsInvalidFirstKeyword() {
    Readable command = new StringReader(initialInput + "test\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'test': Invalid input syntax, cannot parse first keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand1() {
    Readable command = new StringReader(initialInput + "create\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create': Invalid input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand2() {
    Readable command = new StringReader(initialInput + "create event\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event': Invalid input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand3() {
    Readable command = new StringReader(initialInput + "create event test\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test': Invalid input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand4() {
    Readable command = new StringReader(initialInput + "create event test on\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on': Invalid input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand5() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats': Invalid input syntax, "
            + "incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand6() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats BadDays for 3 times\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats BadDays for 3 times': "
            + "Invalid input syntax, cannot parse days of week!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand7() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats MTW for\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats MTW for': Invalid "
            + "input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCommand8() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats MTW for -1 s\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats MTW for -1 s': Invalid "
            + "input syntax, missing \"times\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateOnDate() {
    Readable command = new StringReader(initialInput + "create event test on 2025-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01': Invalid input syntax, cannot parse date!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateOnCommandWithNegativeOccurrences() {
    Readable command = new StringReader(initialInput + "create event test on 2025-01-01 repeats MTW"
            + " for -1 times\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats MTW for -1 times': "
            + "Number of occurrences must be at least 1.";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateOnCommandWithInvalidEndDate1() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats M until 2024-01-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats M until 2024-01-01': "
            + "Event series cannot end before the current event ends.";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateOnCommandWithInvalidEndDate2() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-01-01 repeats M until 2024-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test on 2025-01-01 repeats M until 2024-01': "
            + "Invalid input syntax, cannot parse date!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateOnCommandWithStartDateTimeButNoEndDateTime1() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T01:00 repeats M until 2024-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T01:00 repeats M until 2024-01': "
            + "Invalid input syntax, missing \"to\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateOnCommandWithStartDateTimeButNoEndDateTime2() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T01:00 to repeats M until 2024-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T01:00 to repeats M until 2024-01': "
            + "Invalid input syntax, cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateFromCommandWithInvalidStartDateTime() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T9:00 to 2025-01-01T10:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T9:00 to 2025-01-01T10:00': "
            + "Invalid input syntax, cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateFromCommandWithInvalidEndDateTime() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T08:00 to 2025-01-01T9:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T08:00 to 2025-01-01T9:00': "
            + "Invalid input syntax, cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateFromCommandWithInvalidDaysOfWeek() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T08:00 to"
            + " 2025-01-01T09:00 repeats BadDays for 3 times\nexit"); // invalid end date
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T08:00 to 2025-01-01T09:00 repeats "
            + "BadDays for 3 times': Invalid input syntax, cannot parse days of week!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateFromCommandWithNegativeOccurrences() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T08:00 to"
            + " 2025-01-01T09:00 repeats MTWRF for -3 times\nexit"); // invalid end date
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T08:00 to 2025-01-01T09:00 repeats "
            + "MTWRF for -3 times': Number of occurrences must be at least 1.";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsCreateFromCommandWithInvalidSeriesEndDate() {
    Readable command = new StringReader(initialInput
            + "create event test from 2025-01-01T08:00 to"
            + " 2025-01-01T09:00 repeats MTWRF until 2024-01-01\nexit"); // invalid end date
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create event test from 2025-01-01T08:00 to 2025-01-01T09:00 repeats "
            + "MTWRF until 2024-01-01': Event series cannot end before the current event ends.";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreateAllDayEvent() {
    Readable command = new StringReader(initialInput
            + "create event test on 2025-03-02\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "event added: subject=test,start=2025-03-02T08:00,end=2025-03-02T17:00\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreateEventWithTwoWords() {
    Readable command = new StringReader(initialInput
            + "create event \"2 words\" on 2025-03-02\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "event added: subject=2 words,start=2025-03-02T08:00,end=2025-03-02T17:00\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreateEventWithMultipleWords() {
    Readable command = new StringReader(initialInput
            + "create event \"omg many words\" on 2025-03-02\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "event added: subject=omg many words,start=2025-03-02T08:00,end=2025-03-02T17:00\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreatesAllDayRepeatingForEvent() {
    Readable command = new StringReader(initialInput
            + "create event birthday on 2024-07-01 repeats MTW for 5 times\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "series added: subject=birthday,start=2024-07-01T08:00,end=2024-07-09T17:00"
            + ",days=[MONDAY, TUESDAY, WEDNESDAY],occurs=5\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreatesAllDayRepeatingUntilEvent() {
    Readable command = new StringReader(initialInput
            + "create event OOD on 2025-05-05 repeats MTWR until 2025-05-12\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "series added: subject=OOD,start=2025-05-05T08:00,end=2025-05-12T17:00"
            + ",days=[MONDAY, TUESDAY, WEDNESDAY, THURSDAY],occurs=5\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreatesTimedEvent() {
    Readable command = new StringReader(initialInput
            + "create event womp from 2025-05-05T09:56"
            + " to 2025-05-09T18:12\nexit"); // test multiple days since it's not in a series
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "event added: subject=womp,start=2025-05-05T09:56,end=2025-05-09T18:12\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreatesTimedRepeatingForEvent() {
    Readable command = new StringReader(initialInput
            + "create event cool-event from 2025-05-05T10:30"
            + " to 2025-05-05T13:30 repeats MFU for 5 times\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "series added: subject=cool-event,start=2025-05-05T10:30,end=2025-05-16T13:30"
            + ",days=[MONDAY, FRIDAY, SUNDAY],occurs=5\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesCreatesTimedRepeatingUntilEvent() {
    Readable command = new StringReader(initialInput
            + "create event repeating from 2025-05-05T12:15"
            + " to 2025-05-05T14:00 repeats MTWRF until 2025-05-20\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "series added: subject=repeating,start=2025-05-05T12:15,end=2025-05-20T14:00"
            + ",days=[MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY],occurs=12\n";
    assertEquals(initialOutput + e, log.toString());
  }

  /*
   * Edit event command tests
   */
  @Test
  public void testControllerRejectsInvalidEditCommand1() {
    Readable command = new StringReader(initialInput + "edit\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit': Invalid input syntax, incomplete command!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCommand2() {
    Readable command = new StringReader(initialInput
            + "edit invalid subject test from 2025-05-05T18:00"
            + " to 2025-05-05T20:00 with new\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit invalid subject test from 2025-05-05T18:00 to 2025-05-05T20:00 "
            + "with new': Invalid input syntax, cannot parse second keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCommand3() {
    Readable command = new StringReader(initialInput
            + "edit events subject test from 2025-05-05T18:00"
            + " to 2025-05-05T20:00 with new\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit events subject test from 2025-05-05T18:00 to 2025-05-05T20:00 "
            + "with new': Invalid input syntax, missing \"with\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCommand4() {
    Readable command = new StringReader(initialInput
            + "edit series subject test from 2025-05-05T18:00"
            + " to 2025-05-05T20:00 with new\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit series subject test from 2025-05-05T18:00 to 2025-05-05T20:00 "
            + "with new': Invalid input syntax, missing \"with\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCommandProperties() {
    Readable command = new StringReader(initialInput
            + "edit event badproperty test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with Public\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit event badproperty test from 2025-05-05T18:00 to 2025-05-05T20:00 "
            + "with Public': Invalid input syntax, expected one of [subject, start, end, "
            + "description, location, status], but got \"badproperty\"!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesEditEvent() {
    Readable command = new StringReader(initialInput
            + "edit event subject \"hello world\" from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with \"new hello\"\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=hello world,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=subject,new=new hello\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesEditEvents() {
    Readable command = new StringReader(initialInput
            + "edit events subject \"hello world\" from"
            + " 2025-05-05T18:00 with \"new hello\"\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited events: subject=hello world,start=2025-05-05T18:00,"
            + "property=subject,new=new hello\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesEditSeries() {
    Readable command = new StringReader(initialInput
            + "edit series subject \"hello world\" from"
            + " 2025-05-05T18:00 with \"new hello\"\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited series: subject=hello world,start=2025-05-05T18:00,"
            + "property=subject,new=new hello\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesStartProperties() {
    Readable command = new StringReader(initialInput
            + "edit event start test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with 2025-05-05T19:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=test,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=start,new=2025-05-05T19:00\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesEndProperties() {
    Readable command = new StringReader(initialInput
            + "edit event end test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with 2025-05-05T21:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=test,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=end,new=2025-05-05T21:00\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesDescriptionProperties() {
    Readable command = new StringReader(initialInput
            + "edit event description test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with \"omg this is a very cool multi"
            + " word long description\"\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=test,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=description,new=omg this is a very cool multi word long description\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesLocationProperties() {
    Readable command = new StringReader(initialInput
            + "edit event location test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with Online\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=test,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=location,new=Online\n";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerParsesStatusProperties() {
    Readable command = new StringReader(initialInput
            + "edit event status test from"
            + " 2025-05-05T18:00 to 2025-05-05T20:00 with Public\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "edited event: subject=test,start=2025-05-05T18:00,end=2025-05-05T20:00,"
            + "property=status,new=Public\n";
    assertEquals(initialOutput + e, log.toString());
  }

  /*
   * Query command tests
   */
  @Test
  public void testControllerRejectsInvalidPrintCommand1() {
    Readable command = new StringReader(initialInput
            + "print BadKeyword on 2025-05-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print BadKeyword on 2025-05-01': Invalid input syntax, "
            + "cannot parse next keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidPrintCommand2() {
    Readable command = new StringReader(initialInput
            + "print events after 2025-05-01\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print events after 2025-05-01': Invalid input syntax, "
            + "cannot parse next keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsPrintEventsCommandWithInvalidDate() {
    Readable command = new StringReader(initialInput
            + "print events on 2025-05-1\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print events on 2025-05-1': Invalid input syntax, cannot parse date!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsPrintEventsCommandWithInvalidStartDateTime() {
    Readable command = new StringReader(initialInput
            + "print events from 2025-05-01T36:18"
            + " to 2025-05-01T23:23\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print events from 2025-05-01T36:18 to 2025-05-01T23:23': "
            + "Invalid input syntax, cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsPrintEventsCommandWithoutToKeyword() {
    Readable command = new StringReader(initialInput
            + "print events from 2025-05-01T22:18"
            + " 2025-05-01T23:23\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print events from 2025-05-01T22:18 2025-05-01T23:23': "
            + "Invalid input syntax, missing \"to\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsPrintEventsCommandWithInvalidEndDateTime() {
    Readable command = new StringReader(initialInput
            + "print events from 2025-05-01T22:18"
            + " to 2025-05-01T25:23\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'print events from 2025-05-01T22:18 to 2025-05-01T25:23': "
            + "Invalid input syntax, cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsShowStatusCommandWithoutStatusKeyword() {
    Readable command = new StringReader(initialInput + "show from 2025-05-01T10:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'show from 2025-05-01T10:00': Invalid input "
            + "syntax, cannot parse next keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsShowStatusCommandWithoutOnKeyword() {
    Readable command = new StringReader(initialInput + "show status 2025-05-01T10:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'show status 2025-05-01T10:00': Invalid input syntax,"
            + " missing \"on\" keyword!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerRejectsShowStatusCommandWithInvalidDateTime() {
    Readable command = new StringReader(initialInput + "show status on 2025-13-01T22:18\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'show status on 2025-13-01T22:18': Invalid input syntax, "
            + "cannot parse date/time!";
    assertEquals(initialOutput + e, log.toString());
  }

  @Test
  public void testControllerShowsStatus() {
    Readable command = new StringReader(initialInput + "show status on 2025-01-01T12:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "showed status with: date/time=2025-01-01T12:00"
            + "Status is available on 2025-01-01T12:00.";
    assertEquals(initialOutput + e, log.toString());
  }

  /*
   * Create calendar command tests
   */
  @Test
  public void testControllerParsesCreateCalendarWithNameAndTimezone() {
    Readable command = new StringReader("create calendar --name test "
            + "--timezone America/New_York\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "calendar added: name=test,timezone=America/New_York\n";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCalendarCommand1() {
    Readable command = new StringReader("create calendar -name test "
            + "--timezone America/New_York\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create calendar -name test --timezone America/New_York': "
            + "Invalid input syntax, missing \"--name\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidCreateCalendarCommand2() {
    Readable command = new StringReader("create calendar --name test "
            + "--timezane America/New_York\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'create calendar --name test --timezane America/New_York': "
            + "Invalid input syntax, missing \"--timezone\" keyword!";
    assertEquals(e, log.toString());
  }

  /*
   * Edit calendar command tests
   */
  @Test
  public void testControllerRejectsInvalidEditCalendarCommand1() {
    Readable command = new StringReader("edit calendar -name test --property name test2\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit calendar -name test --property name test2': "
            + "Invalid input syntax, missing \"--name\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCalendarCommand2() {
    Readable command = new StringReader("edit calendar --name test "
            + "-property badProperty test2\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit calendar --name test -property badProperty test2': "
            + "Invalid input syntax, missing \"--property\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsInvalidEditCalendarCommand3() {
    Readable command = new StringReader("edit calendar --name test "
            + "--property badProperty test2\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'edit calendar --name test --property badProperty test2': "
            + "Invalid input syntax, expected one of [name, timezone], but got \"badProperty\"!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesEditCalendarCommand1() {
    Readable command = new StringReader("edit calendar --name test "
            + "--property name test2\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "calendar name edited: old=test,new=test2\n";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesEditCalendarCommand2() {
    Readable command = new StringReader("edit calendar --name test "
            + "--property timezone America/Los_Angeles\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "calendar timezone edited: name=test,new timezone=America/Los_Angeles\n";
    assertEquals(e, log.toString());
  }

  /*
   * Use calendar command tests
   */
  @Test
  public void testControllerRejectsInvalidUseCalendarCommand() {
    Readable command = new StringReader("use calendar --nam test\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'use calendar --nam test': Invalid input syntax, "
            + "missing \"--name\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesUseCalendarCommand() {
    Readable command = new StringReader("use calendar --name test\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "calendar set to: test\n";
    assertEquals(e, log.toString());
  }

  /*
   * Copy event command tests
   */
  @Test
  public void testControllerRejectsCopyEventCommandWithoutOnKeyword() {
    Readable command = new StringReader("copy event test 2025-05-05T08:00"
            + " --target calendarName to 2025-05-05T09:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy event test 2025-05-05T08:00 --target calendarName "
            + "to 2025-05-05T09:00': Invalid input syntax, missing \"on\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventCommandWithInvalidFirstDateTime() {
    Readable command = new StringReader("copy event test on 2025-05-05T8:00"
            + " --target calendarName to 2025-05-05T09:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy event test on 2025-05-05T8:00 --target calendarName "
            + "to 2025-05-05T09:00': Invalid input syntax, cannot parse date/time!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventCommandWithoutTargetKeyword() {
    Readable command = new StringReader("copy event test on 2025-05-05T08:00"
            + " target calendarName to 2025-05-05T09:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy event test on 2025-05-05T08:00 target calendarName "
            + "to 2025-05-05T09:00': Invalid input syntax, missing \"--target\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventCommandWithoutToKeyword() {
    Readable command = new StringReader("copy event test on 2025-05-05T08:00"
            + " --target calendarName 2025-05-05T09:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy event test on 2025-05-05T08:00 --target calendarName "
            + "2025-05-05T09:00': Invalid input syntax, missing \"to\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventCommandWithInvalidSecondDateTime() {
    Readable command = new StringReader("copy event test on 2025-05-05T08:00"
            + " --target calendarName to 2025-05-05T9:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy event test on 2025-05-05T08:00 --target calendarName to "
            + "2025-05-05T9:00': Invalid input syntax, cannot parse date/time!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesCopyEventCommand() {
    Readable command = new StringReader("copy event test on 2025-05-05T08:00"
            + " --target calName to 2025-05-05T09:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "event copied: name=test,start=2025-05-05T08:00,target=calName,"
            + "new-start=2025-05-05T09:00\n";
    assertEquals(e, log.toString());
  }

  /*
   * Copy events command tests
   */
  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords1() {
    Readable command = new StringReader("copy events from 2025-05-05"
            + " --target calName to 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events from 2025-05-05 --target calName to 2025-05-06': "
            + "Invalid input syntax, expected one of [on, between], but got \"from\"!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords2() {
    Readable command = new StringReader("copy events on 2025-05-05"
            + " --targe calName to 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events on 2025-05-05 --targe calName to 2025-05-06': "
            + "Invalid input syntax, missing \"--target\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords3() {
    Readable command = new StringReader("copy events on 2025-05-05"
            + " --target calName 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events on 2025-05-05 --target calName 2025-05-06':"
            + " Invalid input syntax, missing \"to\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords4() {
    Readable command = new StringReader("copy events between 2025-05-05"
            + " 2025-05-07 --target calName to 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events between 2025-05-05 2025-05-07 --target "
            + "calName to 2025-05-06': Invalid input syntax, missing \"and\" keyword!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords5() {
    Readable command = new StringReader("copy events on 2025-05-05T08:00"
            + " --target calName 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events on 2025-05-05T08:00 --target calName 2025-05-06': "
            + "Invalid input syntax, cannot parse date!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords6() {
    Readable command = new StringReader("copy events on 2025-05-05T08:00"
            + " --target calName 2025-05-06T08:00\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events on 2025-05-05T08:00 --target calName 2025-05-06T08:00': "
            + "Invalid input syntax, cannot parse date!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerRejectsCopyEventsCommandWithInvalidKeywords7() {
    Readable command = new StringReader("copy events between 2025-05-05T08:00"
            + " 2025-05-07T08:00 --target calName to 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "Error in 'copy events between 2025-05-05T08:00 2025-05-07T08:00 --target calName "
            + "to 2025-05-06': Invalid input syntax, cannot parse date!";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesCopyEventsCommand1() {
    Readable command = new StringReader("copy events on 2025-05-05"
            + " --target calName to 2025-05-06\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "events copied: start-interval=2025-05-05T00:00,end-interval=2025-05-05T23:59:59,"
            + "target=calName,start-date2025-05-06\n";
    assertEquals(e, log.toString());
  }

  @Test
  public void testControllerParsesCopyEventsCommand2() {
    Readable command = new StringReader("copy events between 2025-05-05 and 2025-05-06"
            + " --target calName to 2025-05-10\nexit");
    controller = new CommandLineController(command, mock, view);

    controller.run();
    String e = "events copied: start-interval=2025-05-05T00:00,end-interval=2025-05-06T23:59:59,"
            + "target=calName,start-date2025-05-10\n";
    assertEquals(e, log.toString());
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
      if (event.getClass() == SingleEvent.class) {
        log.append("event added: subject=").append(event.getSubject())
                .append(",start=").append(event.getStartDateTime())
                .append(",end=").append(event.getEndDateTime());
      } else if (event.getClass() == EventSeries.class) {
        IEventSeries series = (EventSeries) event; // safe cast b/c we checked above
        log.append("series added: subject=").append(series.getSubject())
                .append(",start=").append(series.getStartDateTime())
                .append(",end=").append(series.getEndDateTime())
                .append(",days=").append(Arrays.toString(series.getOccurringDays()))
                .append(",occurs=").append(series.getEvents().size());
      }
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
      log.append("printed event(s) with: start=").append(start.toString())
              .append(",end=").append(end.toString());
      return List.of(); // we don't care about the return value
    }

    @Override
    public IEventSeries isInSeries(ISingleEvent original) {
      return null;
    }

    @Override
    public boolean containsTime(LocalDateTime dateTime) {
      log.append("showed status with: date/time=").append(dateTime.toString());
      return false; // we don't care about the return value
    }

    @Override
    public boolean canAddSingleEvent(ISingleEvent newEvent) {
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
      return List.of();
    }

    @Override
    public ZoneId getZoneId() {
      return null;
    }

    @Override
    public BetterCalendar makeCalWithNewTimeZone(String newTimeZone) {
      return null;
    }
  }

  /**
   * Mock view class for helping test the correctness of our controller.
   */
  private static class MockView implements IView {
    private final StringBuilder log;

    public MockView(StringBuilder log) {
      this.log = log;
    }

    @Override
    public void showEvents(List<ReadOnlyCalendarEvent> events) {
      // empty because we never use this part of the mock view
    }

    @Override
    public void writeMessage(String message) {
      log.append(message);
    }
  }

}