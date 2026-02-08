package controller.commands;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import model.application.ICalendarManager;
import view.IView;

/**
 * Class that parses the "copy" command.
 */
public class CopyEventCommandParser extends AbstractCommandParser {
  private final ICalendarManager application;

  /**
   * Creates the {@code CopyEventCommandParser} object.
   *
   * @param application the program's application.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public CopyEventCommandParser(ICalendarManager application, IView view, Scanner command) {
    super(view, command);
    this.application = application;
  }

  @Override
  public void parse() {
    String copyType = nextIsOneOf(List.of("event", "events"));
    switch (copyType) {
      case "event":
        copyEventParser();
        break;
      case "events":
        copyEventsParser();
        break;
      default: // shouldn't ever get here
        throw new IllegalArgumentException("Invalid input :(");
    }
  }

  private void copyEventParser() {
    String eventName = nextMultipleWords();
    nextIsExactly("on");
    LocalDateTime onDateTime = nextDateTime();
    nextIsExactly("--target");
    String calendarName = nextMultipleWords();
    nextIsExactly("to");
    LocalDateTime toDateTime = nextDateTime();
    application.copyEvent(eventName, onDateTime, calendarName, toDateTime);
  }

  private void copyEventsParser() {
    String nextKeyword = nextIsOneOf(List.of("on", "between"));
    switch (nextKeyword) {
      case "on":
        copyEventsOnParser();
        break;
      case "between":
        copyEventsBetweenParser();
        break;
      default: // shouldn't ever get here
        throw new IllegalArgumentException("Invalid input :(");
    }
  }

  private void copyEventsOnParser() {
    LocalDate onDate = nextDate();
    nextIsExactly("--target");
    String calendarName = nextMultipleWords();
    nextIsExactly("to");
    LocalDate toDate = nextDate();
    application.copyEvents(getDayStartingDateTime(onDate), getDayEndingDateTime(onDate),
            calendarName, toDate);
  }

  private void copyEventsBetweenParser() {
    LocalDate firstDate = nextDate();
    nextIsExactly("and");
    LocalDate secondDate = nextDate();
    nextIsExactly("--target");
    String calendarName = nextMultipleWords();
    nextIsExactly("to");
    LocalDate toDate = nextDate();
    application.copyEvents(getDayStartingDateTime(firstDate), getDayEndingDateTime(secondDate),
            calendarName, toDate);
  }
}
