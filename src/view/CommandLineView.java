package view;

import java.io.PrintStream;
import java.util.List;

import model.event.ReadOnlyCalendarEvent;
import model.event.comparators.StartDateTimeComparator;

/**
 * A command line view for displaying calendar events.
 */
public class CommandLineView implements IView {
  private final PrintStream out;

  /**
   * Constructs the {@code CommandLineView} object.
   *
   * @param out the specified PrintStream to write output to.
   */
  public CommandLineView(PrintStream out) {
    this.out = out;
  }

  @Override
  public void writeMessage(String message) {
    out.println(message);
  }

  @Override
  public void showEvents(List<ReadOnlyCalendarEvent> events) {
    events.sort(new StartDateTimeComparator());
    for (ReadOnlyCalendarEvent event : events) {
      out.printf("%s\n", event.toString());
    }
  }

}