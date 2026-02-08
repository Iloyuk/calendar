package controller.commands;

import java.util.Scanner;

import model.application.ICalendarManager;
import view.IView;

/**
 * Class that parses the "use calendar" command.
 */
public class UseCommandParser extends AbstractCommandParser {
  private final ICalendarManager application;

  /**
   * Creates the {@code UseCommandParser} object.
   *
   * @param application the program's application.
   * @param view the view to use.
   * @param command the command for the controller to parse.
   */
  public UseCommandParser(ICalendarManager application, IView view, Scanner command) {
    super(view, command);
    this.application = application;
  }

  @Override
  public void parse() {
    nextIsExactly("calendar");
    nextIsExactly("--name");
    String name = nextMultipleWords();
    application.setCalendarInUse(name);
  }
}
