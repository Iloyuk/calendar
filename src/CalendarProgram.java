import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

import controller.GuiController;
import controller.IController;
import controller.CommandLineController;
import model.application.CalendarManager;
import view.GUIView;
import view.CommandLineView;

/**
 * Our main class for running the calendar program.
 */
public class CalendarProgram {

  /**
   * Runs the {@code CalendarProgram} program.
   *
   * @param args the additional arguments to run the program with.
   */
  public static void main(String[] args) {
    try {
      IController controller = getControllerFromArgs(args);
      controller.run();
    } catch (IllegalArgumentException | FileNotFoundException e) {
      System.out.println("Error: " + e.getMessage());
    }
  }

  private static IController getControllerFromArgs(String[] args)
          throws FileNotFoundException {
    if (args.length == 0) {
      return new GuiController(new CalendarManager(), new GUIView("Calendar Application"));
    } else {
      return new CommandLineController(getReaderFromArgs(args), new CalendarManager(),
              new CommandLineView(System.out));
    }
  }

  private static Readable getReaderFromArgs(String[] args)
          throws IllegalArgumentException, FileNotFoundException {
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--mode")) {
        if (i + 1 >= args.length) {
          throw new IllegalArgumentException("Expected argument after '--mode'");
        } else {
          if (args[i + 1].equalsIgnoreCase("interactive")) {
            return new InputStreamReader(System.in);
          } else if (args[i + 1].equalsIgnoreCase("headless")) {
            if (i + 2 >= args.length) {
              throw new IllegalArgumentException("Expected .txt file for headless mode.");
            } else {
              return new FileReader(args[i + 2]);
            }
          } else {
            throw new IllegalArgumentException(args[i + 1] + " is not a valid mode");
          }
        }
      }
    }
    return null;
  }
}
