package controller;

import java.util.NoSuchElementException;
import java.util.Scanner;

import controller.commands.CopyEventCommandParser;
import controller.commands.CreateCalendarCommandParser;
import controller.commands.CreateEventCommandParser;
import controller.commands.EditCalendarCommandParser;
import controller.commands.EditEventCommandParser;
import controller.commands.QueryCommandParser;
import controller.commands.ICommandParser;
import controller.commands.UseCommandParser;
import model.application.ICalendarManager;
import view.IView;

/**
 * A concrete implementation of a calendar controller, allowing for interactive text-based usage.
 */
public class CommandLineController implements IController {
  private final Readable in;
  private final ICalendarManager manager;
  private final IView view;

  /**
   * Constructs an {@code InteractiveController} object.
   *
   * @param in the Readable object.
   * @param manager the IApplication object.
   * @param view the IView object.
   */
  public CommandLineController(Readable in, ICalendarManager manager, IView view) {
    this.in = in;
    this.manager = manager;
    this.view = view;
  }

  @Override
  public void run() {
    Scanner input = new Scanner(in);
    boolean quit = false;

    while (!quit) {
      String firstWord = "";

      try {
        firstWord = input.next();
      } catch (NoSuchElementException e) {
        view.writeMessage("Missing 'exit' command.");
        quit = true;
      }
      
      if (firstWord.equals("exit")) {
        quit = true;
      } else if (!quit) {
        String nextLn = input.nextLine();
        try {
          processCommand(firstWord, manager, view, new Scanner(nextLn));
        } catch (IllegalArgumentException e) {
          String errorMsg = String.format("Error in '%s%s': %s", firstWord, nextLn, e.getMessage());
          view.writeMessage(errorMsg);
        } catch (NullPointerException e) {
          String errorMsg = String.format("Error in '%s%s': Could not find calendar to use!",
                  firstWord, nextLn);
          view.writeMessage(errorMsg);
        }
      }
    }
  }

  private void processCommand(String next, ICalendarManager manager, IView view,
                              Scanner command) {
    try {
      ICommandParser commandParser;
      switch (next) {
        case "create":
          String nextAfterCreate = command.next();
          if (nextAfterCreate.equals("calendar")) {
            commandParser = new CreateCalendarCommandParser(manager, view, command);
          } else {
            commandParser = new CreateEventCommandParser(manager.getCurrentCalendar(),
                    view, command, nextAfterCreate);
          }
          break;
        case "edit":
          String nextAfterEdit = command.next();
          if (nextAfterEdit.equals("calendar")) {
            commandParser = new EditCalendarCommandParser(manager, view, command);
          } else {
            commandParser = new EditEventCommandParser(manager.getCurrentCalendar(),
                    view, command, nextAfterEdit);
          }
          break;
        case "print":
          commandParser = new QueryCommandParser(manager.getCurrentCalendar(),
                  manager.getCurrentCalendarName(), view, command, "print");
          break;
        case "show":
          commandParser = new QueryCommandParser(manager.getCurrentCalendar(),
                  manager.getCurrentCalendarName(), view, command, "show");
          break;
        case "use":
          commandParser = new UseCommandParser(manager, view, command);
          break;
        case "copy":
          commandParser = new CopyEventCommandParser(manager, view, command);
          break;
        default:
          throw new IllegalArgumentException("Invalid input syntax, cannot parse first keyword!");
      }
      commandParser.parse();
    } catch (NoSuchElementException e) {
      throw new IllegalArgumentException("Invalid input syntax, incomplete command!");
    }
  }

}
