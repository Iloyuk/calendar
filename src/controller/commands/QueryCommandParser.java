package controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import model.calendar.ICalendar;
import model.event.ReadOnlyCalendarEvent;
import view.IView;

/**
 * Class that parses the "print" and "show" commands.
 */
public class QueryCommandParser extends AbstractCommandParser {
  private final ICalendar calendar;
  private final String calendarName;
  private final IView view;
  private final String firstCommand;

  /**
   * Creates the {@code QueryCommandParser} object.
   *
   * @param calendar the calendar to use.
   * @param calendarName the calendar's name.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   * @param firstCommand the first command for the query (either {@code print} or {@code show}).
   */
  public QueryCommandParser(ICalendar calendar, String calendarName, IView view,
                            Scanner command, String firstCommand) {
    super(view, command);
    this.calendar = calendar;
    this.calendarName = calendarName;
    this.view = view;
    this.firstCommand = firstCommand;
  }

  @Override
  public void parse() {
    switch (command.next()) {
      case "events":
        if (!firstCommand.equals("print")) {
          throw new IllegalArgumentException("Invalid input syntax!");
        } else {
          parsePrintEventsCommand();
        }
        break;
      case "status":
        if (!firstCommand.equals("show")) {
          throw new IllegalArgumentException("Invalid input syntax!");
        } else {
          parseShowStatusCommand();
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid input syntax, cannot parse next keyword!");
    }
  }

  private void parsePrintEventsCommand() {
    switch (command.next()) {
      case "on":
        parsePrintEventsOnCommand();
        break;
      case "from":
        parsePrintEventsFromCommand();
        break;
      default:
        throw new IllegalArgumentException("Invalid input syntax, cannot parse next keyword!");
    }
  }

  private void parsePrintEventsOnCommand() {
    LocalDate date = nextDate();
    List<ReadOnlyCalendarEvent> events = new LinkedList<>(calendar.query(
            getDayStartingDateTime(date), getDayEndingDateTime(date)));

    view.writeMessage(String.format("All events in %s on %s:", calendarName, date.toString()));
    view.showEvents(events);
  }

  private void parsePrintEventsFromCommand() {
    LocalDateTime startDateTime = nextDateTime();
    nextIsExactly("to");
    LocalDateTime endDateTime = nextDateTime();
    List<ReadOnlyCalendarEvent> events = new LinkedList<>(calendar.query(
            startDateTime, endDateTime));

    view.writeMessage(String.format("All events in %s from %s to %s:", calendarName,
            startDateTime.toString(), endDateTime.toString()));
    view.showEvents(events);
  }

  private void parseShowStatusCommand() {
    nextIsExactly("on");
    LocalDateTime dateTime = nextDateTime();
    if (calendar.containsTime(dateTime)) {
      view.writeMessage(String.format("Status is busy on %s.", dateTime.toString()));
    } else {
      view.writeMessage(String.format("Status is available on %s.", dateTime.toString()));
    }
  }
}
