package controller.commands;

import java.util.Scanner;

import model.application.ICalendarManager;
import view.IView;

/**
 * Class that parses the "create calendar" command.
 */
public class CreateCalendarCommandParser extends AbstractCommandParser {
  private final ICalendarManager application;

  /**
   * Creates the {@code CreateCalendarCommandParser} object.
   *
   * @param application the program's application.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public CreateCalendarCommandParser(ICalendarManager application, IView view, Scanner command) {
    super(view, command);
    this.application = application;
  }

  @Override
  public void parse() {
    nextIsExactly("--name");
    String name = nextMultipleWords();
    nextIsExactly("--timezone");
    String area = command.next();
    application.createCalendar(name, area);
  }
}
