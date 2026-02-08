package controller.commands;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import model.calendar.ICalendar;
import view.IView;

/**
 * Class that parses the "edit event" command.
 */
public class EditEventCommandParser extends AbstractCommandParser {
  private final ICalendar calendar;
  private final String nextWord;

  /**
   * Creates the {@code EditCommandParser} object.
   *
   * @param calendar the calendar to use.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public EditEventCommandParser(ICalendar calendar, IView view, Scanner command, String next) {
    super(view, command);
    this.calendar = calendar;
    nextWord = next;
  }

  @Override
  public void parse() {
    try {
      switch (nextWord) {
        case "event":
        case "series":
        case "events":
          parseEditByType(nextWord);
          break;
        default:
          throw new IllegalArgumentException("Invalid input syntax, cannot parse second keyword!");
      }
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid input syntax, cannot parse date/time");
    }
  }

  private void parseEditByType(String commandType) {
    String property = nextIsOneOf(List.of("subject", "start", "end", "description", "location",
            "status"));
    String subject = nextMultipleWords();
    nextIsExactly("from");
    LocalDateTime startDateTime = nextDateTime();

    if (commandType.equals("event")) {
      nextIsExactly("to");
      LocalDateTime endDateTime = nextDateTime();
      nextIsExactly("with");
      String newValue = nextMultipleWords();
      handleEditEvent(property, subject, startDateTime, endDateTime, newValue);
    } else { // command type is edit "events" or edit "series"
      nextIsExactly("with");
      String newValue = nextMultipleWords();
      handleEditSeriesOrEvents(commandType, property, subject, startDateTime, newValue);
    }
  }

  private void handleEditEvent(String property, String subject, LocalDateTime startDateTime,
                               LocalDateTime endDateTime, String newValue) {
    calendar.editEvent(property, subject, startDateTime, endDateTime, newValue);
  }

  private void handleEditSeriesOrEvents(String commandType, String property, String subject,
                                        LocalDateTime startDateTime, String newValue) {

    calendar.editEvents(property, subject, startDateTime, commandType, newValue);
  }
}