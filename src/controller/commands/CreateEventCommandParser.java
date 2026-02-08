package controller.commands;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Scanner;

import model.event.CalendarEvent;
import model.event.EventSeries;
import model.calendar.ICalendar;
import model.event.SingleEvent;
import view.IView;

/**
 * Class that parses the "create event" command.
 */
public class CreateEventCommandParser extends AbstractCommandParser {
  private final ICalendar calendar;
  private final String nextWord;

  /**
   * Creates the {@code CreateCommandParser} object.
   *
   * @param calendar the calendar to use.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public CreateEventCommandParser(ICalendar calendar, IView view, Scanner command, String next) {
    super(view, command);
    this.calendar = calendar;
    nextWord = next;
  }

  @Override
  public void parse() {
    if (!nextWord.equals("event")) {
      throw new IllegalArgumentException("Invalid input syntax, missing \"event\" keyword!");
    }

    String subject = nextMultipleWords(); // parse the event subject next
    // parse the second word in our command words: "on" or "from"
    switch (command.next()) {
      case "on":
        parseCreateOnCommand(calendar, subject);
        break;
      case "from":
        parseCreateFromCommand(calendar, subject);
        break;
      default:
        throw new IllegalArgumentException("Invalid input syntax, cannot parse next keyword!");
    }
  }

  // Helper method for parsing the "create event <eventSubject> on" command.
  private void parseCreateOnCommand(ICalendar calendar,
                                    String subject) throws IllegalArgumentException {
    LocalDate date = nextDate();
    if (!command.hasNext()) { // we have reached the end of our command
      CalendarEvent newEvent = new SingleEvent.Builder(subject,
              getAllDayEventStartingDateTime(date), getAllDayEventEndingDateTime(date)).build();
      calendar.addEvent(newEvent);
    } else { // the command hasn't finished yet, which means we're creating an event series
      parseCreateOnRepeatingCommand(calendar, subject, date);
    }
  }

  // Helper method for parsing the "create event <eventSubject> on <dateString> repeats" command.
  private void parseCreateOnRepeatingCommand(ICalendar calendar, String subject,
                                             LocalDate date) throws IllegalArgumentException {
    nextIsExactly("repeats"); // next word MUST be "repeats"
    DayOfWeek[] daysOfWeek = nextDaysOfWeek();

    // parse the third word in our command words: "for" or "until"
    CalendarEvent newEvent;
    switch (command.next()) {
      case "for":
        newEvent = new EventSeries(
                new SingleEvent.Builder(subject, getAllDayEventStartingDateTime(date),
                        getAllDayEventEndingDateTime(date)).build(), nextOccurrences(), daysOfWeek);
        calendar.addEvent(newEvent);
        break;
      case "until":
        newEvent = new EventSeries(
                new SingleEvent.Builder(subject, getAllDayEventStartingDateTime(date),
                        getAllDayEventEndingDateTime(date)).build(), nextDate(), daysOfWeek);
        calendar.addEvent(newEvent);
        break;
      default:
        throw new IllegalArgumentException("Invalid input syntax, cannot parse next keyword!");
    }
  }

  // Helper method for parsing the "create event <eventSubject> from <dateStringTtimeString>
  // to <dateStringTtimeString>" command.
  private void parseCreateFromCommand(ICalendar calendar,
                                      String subject) throws IllegalArgumentException {
    LocalDateTime startDateTime = nextDateTime();
    nextIsExactly("to"); // next word MUST be "to"
    LocalDateTime endDateTime = nextDateTime();

    if (!command.hasNext()) { // we have reached the end of our command
      CalendarEvent newEvent = new SingleEvent.Builder(subject, startDateTime, endDateTime).build();
      calendar.addEvent(newEvent);
    } else { // the command hasn't finished yet, which means we're creating an event series
      parseCreateFromRepeatingCommand(calendar, subject, startDateTime, endDateTime);
    }
  }

  // Helper method for parsing the "create event <eventSubject> from <dateStringTtimeString>
  // to <dateStringTtimeString> repeats" command.
  private void parseCreateFromRepeatingCommand(ICalendar calendar, String subject,
                                               LocalDateTime startDateTime,
                                               LocalDateTime endDateTime)
          throws IllegalArgumentException {
    nextIsExactly("repeats"); // next word MUST be "repeats"
    DayOfWeek[] daysOfWeek = nextDaysOfWeek();

    // parse the third word in our command words: "for" or "until"
    CalendarEvent newEvent;
    switch (command.next()) {
      case "for":
        newEvent = new EventSeries(
                new SingleEvent.Builder(subject, startDateTime, endDateTime).build(),
                nextOccurrences(), daysOfWeek);
        calendar.addEvent(newEvent);
        break;
      case "until":
        newEvent = new EventSeries(
                new SingleEvent.Builder(subject, startDateTime, endDateTime).build(),
                nextDate(), daysOfWeek);
        calendar.addEvent(newEvent);
        break;
      default:
        throw new IllegalArgumentException("Invalid input syntax, cannot parse next keyword!");
    }
  }
}
