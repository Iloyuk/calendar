package view;

import controller.Features;

/**
 * An interface defining the behavior of a GUI view for displaying calendar information.
 */
public interface IGUIView extends IView {
  /**
   * Displays an error message.
   *
   * @param errorMsg the message to be written.
   */
  void writeError(String errorMsg);

  /**
   * Adds the list of methods that the view needs to call back to the controller for.
   *
   * @param features the list of methods.
   */
  void addFeatures(Features features);

  /**
   * Add a calendar's name to list of calendars.
   *
   * @param calendarName the name of the calendar.
   */
  void addCalendarName(String calendarName);

  /**
   * Revert to the previous calendar that was being used.
   */
  void goToPreviousCalendar();
}
