package controller.commands;

import java.util.List;
import java.util.Scanner;

import model.application.ICalendarManager;
import view.IView;

/**
 * Class that parses the "edit calendar" command.
 */
public class EditCalendarCommandParser extends AbstractCommandParser {
  private final ICalendarManager application;

  /**
   * Creates the {@code EditCalendarCommandParser} object.
   *
   * @param application the program's application.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public EditCalendarCommandParser(ICalendarManager application, IView view, Scanner command) {
    super(view, command);
    this.application = application;
  }

  @Override
  public void parse() {
    nextIsExactly("--name");
    String calendarName = nextMultipleWords();
    nextIsExactly("--property");
    String propertyName = nextIsOneOf(List.of("name", "timezone"));
    String newValue = nextMultipleWords();

    switch (propertyName) {
      case "name":
        application.editCalendarName(calendarName, newValue);
        break;
      case "timezone":
        application.editCalendarTimeZone(calendarName, newValue);
        break;
      default: // shouldn't ever get here
        throw new IllegalArgumentException("Invalid input :(");
    }
  }
}