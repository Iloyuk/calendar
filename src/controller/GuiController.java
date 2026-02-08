package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

import model.application.ICalendarManager;
import model.event.ISingleEvent;
import model.event.Location;
import model.event.ReadOnlyCalendarEvent;
import model.event.SingleEvent;
import model.event.Status;
import view.IGUIView;

/**
 * A concrete implementation of the calendar controller for interactive GUI usage.
 */
public class GuiController implements IController, Features {
  private static final int MAX_CALENDAR_NAME_SIZE = 15;
  private static final int MAX_SUBJECT_TEXT_LENGTH = 35;

  private final ICalendarManager manager;
  private final IGUIView view;
  private LocalDate currentViewDate;

  /**
   * Constructs a {@code GuiController}.
   *
   * @param manager the calendar manager object.
   * @param view the gui view object.
   */
  public GuiController(ICalendarManager manager, IGUIView view) {
    this.manager = manager;
    this.view = view;
    this.currentViewDate = null;
  }

  @Override
  public void run() {
    view.addFeatures(this);
    addCalendarHelper("Default", ZoneId.systemDefault().getId());
  }

  @Override
  public void createEvent(String subject, LocalDateTime start, LocalDateTime end,
                          String description, Location location, Status status) {
    if (subject.isBlank()) {
      view.writeError("Subject is empty, cannot add event!");
    } else if (subject.length() > MAX_SUBJECT_TEXT_LENGTH) {
      view.writeError("Subject is too long, cannot add event!");
    } else {
      try {
        // Create our event with the specified parameters
        ISingleEvent addedEvent = new SingleEvent.Builder(subject, start, end)
                .description(description.isBlank() ? null : description)
                .location(location)
                .status(status)
                .build();

        // If our current view date/time is null, we start tracking the events list from
        // the newly-added event's start date
        if (currentViewDate == null) {
          currentViewDate = LocalDate.from(addedEvent.getStartDateTime());
        }
        manager.getCurrentCalendar().addEvent(addedEvent);

        // Get all the events in our calendar from the start date sorted by start time
        List<ISingleEvent> events = manager.getCurrentCalendar()
                .query(currentViewDate.atStartOfDay(), LocalDateTime.MAX);

        // Display the next events starting from the current event
        view.showEvents(findEventsToDisplay(events));
      } catch (IllegalArgumentException e) {
        view.writeError(e.getMessage());
      }
    }
  }

  @Override
  public void showEventsFrom(LocalDate start) {
    if (start != null) {
      // Get all the events in total starting from *start*
      List<ISingleEvent> events = manager.getCurrentCalendar().query(LocalDateTime.of(start,
              LocalTime.of(0, 0, 0)), LocalDateTime.MAX);

      currentViewDate = start;
      view.showEvents(new LinkedList<>(events));
    }
  }

  @Override
  public void changeCalendar(String calendarName) {
    try {
      manager.setCalendarInUse(calendarName);
      this.showEventsFrom(currentViewDate);
    } catch (IllegalArgumentException e) {
      view.writeError(e.getMessage());
    }
  }

  @Override
  public void addCalendar(String calendarName, String timezone) {
    if (calendarName.equals("New Calendar")) {
      showErrorAndGoToLastCalendar("Cannot add this calendar name.");
    } else if (calendarName.isBlank()) {
      showErrorAndGoToLastCalendar("Cannot add calendar with a blank name!");
    } else if (calendarName.length() > MAX_CALENDAR_NAME_SIZE) {
      showErrorAndGoToLastCalendar("Calendar name is too long!");
    } else {
      addCalendarHelper(calendarName, timezone);
    }
  }

  @Override
  public void editEvent(ReadOnlyCalendarEvent event, String property, String newProp) {
    if (property.equals("subject") && newProp.isBlank()) {
      view.writeError("Cannot edit event, subject is empty!");
    } else if (property.equals("subject") && newProp.length() > MAX_SUBJECT_TEXT_LENGTH) {
      view.writeError("Subject is too long, cannot update event!");
    } else {
      try {
        // Update the event in the calendar
        manager.getCurrentCalendar().editEvent(property, event.getSubject(),
                event.getStartDateTime(), event.getEndDateTime(), newProp);
        view.writeMessage("Event updated successfully!");

        // Refresh the display
        List<ReadOnlyCalendarEvent> newEvents = new LinkedList<>(manager.getCurrentCalendar()
                .query(currentViewDate.atStartOfDay(), LocalDateTime.MAX));
        view.showEvents(newEvents);
      } catch (IllegalArgumentException e) {
        view.writeError(e.getMessage());
      }
    }
  }

  private void addCalendarHelper(String calendarName, String timezone) {
    // Both create a new calendar and set it to be in use, in the controller and the view
    try {
      manager.createCalendar(calendarName, timezone);
      manager.setCalendarInUse(calendarName);
      view.addCalendarName(calendarName);
      this.showEventsFrom(currentViewDate);
    } catch (IllegalArgumentException e) {
      showErrorAndGoToLastCalendar(e.getMessage());
    }
  }

  private List<ReadOnlyCalendarEvent> findEventsToDisplay(List<ISingleEvent> allEvents) {
    List<ReadOnlyCalendarEvent> viewedEvents = new LinkedList<>();

    int counter = 0;
    for (ISingleEvent event : allEvents) {
      // Add the event to the view as long as there's less than 10
      if (eventIsOnOrAfterDate(event, currentViewDate) && counter < 10) {
        viewedEvents.add(event);
        counter++;
      }

      // Exit the loop if we've found 10 events
      if (counter == 10) {
        break;
      }
    }

    return viewedEvents;
  }

  private void showErrorAndGoToLastCalendar(String errorMsg) {
    view.writeError(errorMsg);
    view.goToPreviousCalendar();
  }

  private boolean eventIsOnOrAfterDate(ISingleEvent event, LocalDate date) {
    LocalDate eventDate = LocalDate.from(event.getStartDateTime());
    return eventDate.equals(date) || eventDate.isAfter(date);
  }
}
