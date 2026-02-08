package view;

import java.util.List;

import model.event.ReadOnlyCalendarEvent;

/**
 * An interface representing a view for displaying calendar events.
 */
public interface IView {
  /**
   * Displays a message.
   *
   * @param message the message to display
   */
  void writeMessage(String message);

  /**
   * Displays a list of events, sorted in order starting from the earliest start date/time.
   *
   * @param events the list of events to display
   */
  void showEvents(List<ReadOnlyCalendarEvent> events);
}
