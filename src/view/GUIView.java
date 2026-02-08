package view;

import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JList;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;

import controller.Features;
import model.event.Location;
import model.event.ReadOnlyCalendarEvent;
import model.event.Status;

/**
 * An implementation of the {@code IGUIView} interface, creating an interactive calendar
 * application GUI.
 */
public class GUIView extends JFrame implements IGUIView {
  private static final int MAX_EVENT_LIST_SIZE = 10;
  private static final int DEFAULT_CALENDAR_WIDTH = 500;
  private static final int DEFAULT_CALENDAR_HEIGHT = 500;
  private static final int MAX_DESCRIPTION_TEXT_LENGTH = 50;

  private final JScrollPane scrollPane;
  private final Set<String> calendars;

  private Features features;
  private JComboBox<String> calendarMenu;
  private JLabel eventsStartingFromLabel;
  private String currentCalendarName;
  private boolean firstTimeDisplaying;

  /**
   * Renders the initial graphical user interface view with a title.
   *
   * @param title the title of our GUI.
   */
  public GUIView(String title) {
    super(title);

    firstTimeDisplaying = true;
    currentCalendarName = "Default";
    calendars = new TreeSet<>();
    calendars.add(currentCalendarName);

    this.setLocationRelativeTo(null);
    this.setSize(DEFAULT_CALENDAR_WIDTH, DEFAULT_CALENDAR_HEIGHT);
    this.setMinimumSize(new Dimension(DEFAULT_CALENDAR_WIDTH, DEFAULT_CALENDAR_HEIGHT));
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout());

    // Create the top panel with buttons and labels
    this.add(createTopPanel(), BorderLayout.PAGE_START);

    // Create the center scroll panel with an empty list of events
    scrollPane = new JScrollPane();
    scrollPane.setPreferredSize(new Dimension(
            (int) (0.9 * DEFAULT_CALENDAR_WIDTH), (int) (0.76 * DEFAULT_CALENDAR_HEIGHT)));
    showEvents(new LinkedList<>());
    this.add(scrollPane, BorderLayout.CENTER);

    // Create the bottom panel with the left and right arrow buttons and add it to the GUI
    this.add(createBottomPanel(), BorderLayout.PAGE_END);

    centerOnScreen();
    this.setVisible(true);
  }

  @Override
  public void showEvents(List<ReadOnlyCalendarEvent> events) {
    JPanel centerPanel = new JPanel();
    centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
    if (firstTimeDisplaying) {
      setEmptyCenterPanel(centerPanel);
      firstTimeDisplaying = false;
    } else {
      if (events.isEmpty()) {
        setEmptyCenterPanel(centerPanel);
      } else {
        // Only displays up to 10 events, or the list length
        for (int i = 0; i < MAX_EVENT_LIST_SIZE && i < events.size(); i++) {
          JButton eventButton = getEventButton(events.get(i));
          centerPanel.add(eventButton);
        }
      }
      try {
        String startText = String.format("Events starting from %s",
                EditViewPopup.getDateFromDropdowns().toString().replace("T", ", "));
        eventsStartingFromLabel.setText(startText);
      } catch (NullPointerException e) {
        if (!events.isEmpty()) {
          String startText = String.format("Events starting from %s",
                  events.get(0).getStartDateTime().toLocalDate().toString().replace("T", ", "));
          eventsStartingFromLabel.setText(startText);
        }
      }
    }

    scrollPane.setViewportView(centerPanel);
  }

  @Override
  public void writeError(String errorMsg) {
    JOptionPane.showMessageDialog(this,
            errorMsg,
            null,
            JOptionPane.ERROR_MESSAGE,
            getIconFromPath("sad_mark.png"));
  }

  @Override
  public void addFeatures(Features features) {
    this.features = features;
  }

  @Override
  public void addCalendarName(String calendarName) {
    calendars.add(calendarName);

    // Since calendarSet is sorted from our TreeSet, we add "New Calendar" to the end,
    // which is our functionality to create a new calendar
    List<String> calendarListWithCreate = new LinkedList<>(calendars);
    calendarListWithCreate.add("New Calendar");

    calendarMenu.setModel(
            new DefaultComboBoxModel<>(calendarListWithCreate.toArray(new String[0])));
    calendarMenu.setSelectedItem(calendarName);
    calendarMenu.revalidate();
    calendarMenu.repaint();

    currentCalendarName = calendarName;
  }

  @Override
  public void goToPreviousCalendar() {
    calendarMenu.setSelectedItem(currentCalendarName);
    features.changeCalendar(currentCalendarName);
  }

  @Override
  public void writeMessage(String message) {
    JOptionPane.showMessageDialog(this,
            message,
            null,
            JOptionPane.PLAIN_MESSAGE,
            getIconFromPath("happy_mark.png"));
  }

  private void changeCalendar(int selectedIndex) {
    String name = calendarMenu.getItemAt(selectedIndex);
    if (selectedIndex == calendarMenu.getItemCount() - 1) {
      new CreateCalendarPopup(this, features, currentCalendarName);
    } else {
      features.changeCalendar(name);
      currentCalendarName = name;
    }
  }

  private void setEmptyCenterPanel(JPanel centerPanel) {
    JLabel emptyEventLabel = new JLabel("No events");
    emptyEventLabel.setFont(new Font("Arial", Font.PLAIN, 20));
    emptyEventLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    emptyEventLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

    centerPanel.add(Box.createVerticalGlue());
    centerPanel.add(emptyEventLabel);
    centerPanel.add(Box.createVerticalGlue());
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new FlowLayout());

    // Create the "Create Event" button
    JButton createEventButton = new JButton("Create Event");
    createEventButton.addActionListener(e -> new CreateEventPopup(this, features));
    topPanel.add(createEventButton);

    // Create the text for saying which events to start displaying from
    eventsStartingFromLabel = new JLabel("No events to display");
    eventsStartingFromLabel.setFont(new Font("Arial", Font.BOLD, 13));
    topPanel.add(eventsStartingFromLabel);

    // Since calendarSet is sorted from our TreeSet, we add "New Calendar" to the end,
    // which is our functionality to create a new calendar
    List<String> calendarListWithCreate = new LinkedList<>(calendars);
    calendarListWithCreate.add("New Calendar");

    // Create the combo box with the calendars
    calendarMenu = new JComboBox<>(calendarListWithCreate.toArray(new String[0]));
    calendarMenu.addActionListener(e ->
            changeCalendar(calendarMenu.getSelectedIndex()));
    calendarMenu.setRenderer(getCalendarDropdownRenderer());

    topPanel.add(calendarMenu);
    return topPanel;
  }

  private JPanel createBottomPanel() {
    JButton changeEventButton = new JButton("Change Starting Date View");
    changeEventButton.addActionListener(e -> new EditViewPopup(this, features));

    JPanel bottomPanel = new JPanel();
    bottomPanel.setLayout(new FlowLayout());
    bottomPanel.add(changeEventButton);
    return bottomPanel;
  }

  private JButton getEventButton(ReadOnlyCalendarEvent event) {
    String subject = event.getSubject();
    String start = event.getStartDateTime().toString().replace("T", ", ");
    String end = event.getEndDateTime().toString().replace("T", ", ");
    String desc = Objects.toString(event.getDescription(), "No description");
    if (desc.length() > MAX_DESCRIPTION_TEXT_LENGTH) {
      desc = desc.substring(0, MAX_DESCRIPTION_TEXT_LENGTH);
      desc += "...";
    }
    String location = Objects.toString(event.getLocation(), "N/A");
    String status = Objects.toString(event.getStatus(), "N/A");

    String buttonText = String.format("<html>"
                    + "<div style='text-align:center; font-size:14px;'>"
                    + "<b>%s<b/>"
                    + "</div>"
                    + "<div style='text-align:center; font-size:11px;'>"
                    + "<b>%s</b> to <b>%s</b>"
                    + "</div>"
                    + "<div style='text-align:center; font-size:10px;'>"
                    + "%s"
                    + "</div>"
                    + "<div style='text-align:center; font-size:10px;'>"
                    + "Location: %s, Status: %s"
                    + "</div>"
                    + "</html>",
            subject, start, end, desc, location, status);
    Dimension buttonSize = new Dimension(DEFAULT_CALENDAR_WIDTH,
            (int) (0.2 * DEFAULT_CALENDAR_HEIGHT));

    JButton eventButton = new JButton(buttonText);
    eventButton.setPreferredSize(buttonSize);
    eventButton.setMaximumSize(buttonSize);
    eventButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    eventButton.addActionListener(e -> new CreateEventPopup(this, features, event));

    return eventButton;
  }

  private DefaultListCellRenderer getCalendarDropdownRenderer() {
    return new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super
                .getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        label.setHorizontalAlignment(SwingConstants.CENTER);

        if (index == calendarMenu.getItemCount() - 1) {
          label.setFont(label.getFont().deriveFont(Font.BOLD));
        } else {
          label.setFont(label.getFont().deriveFont(Font.PLAIN));
          if (index == calendarMenu.getSelectedIndex()) {
            label.setText("✓  " + label.getText());
          }
        }

        return label;
      }
    };
  }

  private Icon getIconFromPath(String pathToMark) {
    try {
      ImageIcon image =
              new ImageIcon(Objects.requireNonNull(getClass().getResource(pathToMark)));
      Image scaled = image.getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH);
      return new ImageIcon(scaled);
    } catch (NullPointerException e) {
      return null;
    }
  }

  /**
   * Creates a popup window for helping create a new event.
   */
  private static class CreateEventPopup {
    private static final int CREATE_POPUP_WIDTH = 400;
    private static final int CREATE_POPUP_HEIGHT = 650;
    private static final int EDIT_POPUP_WIDTH = 550;
    private static final int EDIT_POPUP_HEIGHT = 400;
    private static final int SUBJECT_FIELD_HEIGHT = 40;
    private static final int DESCRIPTION_FIELD_HEIGHT = 50;

    private final Features features;
    private final JDialog dialog;

    private JTextField eventNameInput;
    private JComboBox<Integer> startYearsDropdown;
    private JComboBox<String> startMonthsDropdown;
    private JComboBox<Integer> startDaysDropdown;
    private JComboBox<Integer> startHoursDropdown;
    private JComboBox<Integer> startMinsDropdown;
    private JComboBox<Integer> startSecsDropdown;

    private JComboBox<Integer> endYearsDropdown;
    private JComboBox<String> endMonthsDropdown;
    private JComboBox<Integer> endDaysDropdown;
    private JComboBox<Integer> endHoursDropdown;
    private JComboBox<Integer> endMinsDropdown;
    private JComboBox<Integer> endSecsDropdown;

    private Location location;
    private JTextField descriptionTextField;
    private Status status;

    final int yearIndex = 1;
    final int monthIndex = 3;
    final int dayIndex = 5;
    final int hourIndex = 7;
    final int minuteIndex = 9;
    final int secondIndex = 11;

    // Popup for creating the event
    private CreateEventPopup(JFrame parent, Features features) {
      this.features = features;

      // Create the contents of the popup window
      JPanel contents = createEventPopupContents();

      JOptionPane popup = new JOptionPane(contents,
              JOptionPane.PLAIN_MESSAGE,
              JOptionPane.DEFAULT_OPTION,
              null,
              new Object[]{},
              null);

      dialog = popup.createDialog(parent, "Create Event");
      dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
      dialog.setVisible(true);
    }

    // Popup for editing the event
    private CreateEventPopup(JFrame parent, Features features, ReadOnlyCalendarEvent event) {
      this.features = features;

      // Create the contents of the popup window
      JPanel contents = editEventPopupContents(event);

      JOptionPane popup = new JOptionPane(contents,
              JOptionPane.PLAIN_MESSAGE,
              JOptionPane.DEFAULT_OPTION,
              null,
              new Object[]{},
              null);

      dialog = popup.createDialog(parent, "Edit Event");
      dialog.setModal(true);
      dialog.setVisible(true);
    }

    private JPanel createEventPopupContents() {
      // Create all the contents that will go inside the popup window
      JPanel contents = new JPanel();
      contents.setLayout(new BorderLayout());
      Dimension contentsSize = new Dimension(CREATE_POPUP_WIDTH, CREATE_POPUP_HEIGHT);
      contents.setPreferredSize(contentsSize);
      contents.setMaximumSize(contentsSize);

      JPanel centerPanel = createEventPopupCenterPanel();
      JPanel bottomPanel = createEventPopupBottomPanel();

      // Add our panels to the contents panel
      contents.add(centerPanel, BorderLayout.CENTER);
      contents.add(bottomPanel, BorderLayout.PAGE_END);

      return contents;
    }

    private JPanel editEventPopupContents(ReadOnlyCalendarEvent event) {
      JPanel contents = new JPanel();
      contents.setLayout(new BorderLayout());
      Dimension contentsSize = new Dimension(EDIT_POPUP_WIDTH - 100, EDIT_POPUP_HEIGHT - 100);
      contents.setPreferredSize(contentsSize);
      contents.setMaximumSize(contentsSize);

      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
      centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

      JLabel eventInfoLabel = createNewEventLabel(event);
      centerPanel.add(eventInfoLabel);
      centerPanel.add(Box.createVerticalStrut(15));

      JLabel propertyLabel = new JLabel("Select property to edit:");
      propertyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      centerPanel.add(propertyLabel);

      JComboBox<String> propertyDropdown = createPropertyDropdown();
      centerPanel.add(propertyDropdown);
      centerPanel.add(Box.createVerticalStrut(10));

      JPanel inputPanel = new JPanel();
      inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
      inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      inputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

      // Default to showing subject input
      JTextField inputField = new JTextField(event.getSubject());
      inputField.setAlignmentX(Component.LEFT_ALIGNMENT);
      inputField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
              inputField.getPreferredSize().height));
      inputPanel.add(inputField);
      centerPanel.add(inputPanel);

      propertyDropdown.addActionListener(e -> {
        inputPanel.removeAll();
        String selected = (String) propertyDropdown.getSelectedItem();

        switch (selected) {
          case "Subject":
            JTextField subjectField = new JTextField(event.getSubject());
            subjectField.setAlignmentX(Component.LEFT_ALIGNMENT);
            subjectField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    subjectField.getPreferredSize().height));
            inputPanel.add(subjectField);
            break;

          case "Start Date/Time":
            JPanel dateTimePanel = getYMDHMSPanel(event.getStartDateTime());
            dateTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            inputPanel.add(dateTimePanel);
            break;

          case "End Date/Time":
            JPanel endDateTimePanel = getYMDHMSPanel(event.getEndDateTime());
            endDateTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            inputPanel.add(endDateTimePanel);
            break;

          case "Description":
            JTextField descField = new JTextField(
                    event.getDescription());
            descField.setAlignmentX(Component.LEFT_ALIGNMENT);
            descField.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                    descField.getPreferredSize().height));
            inputPanel.add(descField);
            break;

          case "Location":
            JPanel locationPanel = createLocationPanel(event);
            inputPanel.add(locationPanel);
            break;

          case "Status":
            JPanel statusPanel = createStatusPanel(event);
            inputPanel.add(statusPanel);
            break;

          default:
            break; // shouldn't ever get here
        }

        inputPanel.revalidate();
        inputPanel.repaint();
      });

      JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      JButton saveButton = new JButton("Save Changes");
      saveButton.addActionListener(e -> {
        updateEvent(event, propertyDropdown, inputPanel);
        dialog.dispose();
      });

      JButton cancelButton = new JButton("Cancel");
      cancelButton.addActionListener(e -> dialog.dispose());

      bottomPanel.add(saveButton);
      bottomPanel.add(cancelButton);

      contents.add(centerPanel, BorderLayout.CENTER);
      contents.add(bottomPanel, BorderLayout.SOUTH);

      return contents;
    }

    private JPanel createLocationPanel(ReadOnlyCalendarEvent event) {
      JPanel locationPanel = new JPanel();
      locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
      locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
      JRadioButton physicalBtn = new JRadioButton("Physical");
      JRadioButton onlineBtn = new JRadioButton("Online");
      JRadioButton naBtn = new JRadioButton("N/A");
      ButtonGroup group = new ButtonGroup();
      group.add(physicalBtn);
      group.add(onlineBtn);
      group.add(naBtn);

      if (event.getLocation() == Location.PHYSICAL) {
        physicalBtn.setSelected(true);
      } else if (event.getLocation() == Location.ONLINE) {
        onlineBtn.setSelected(true);
      } else {
        naBtn.setSelected(true);
      }

      locationPanel.add(physicalBtn);
      locationPanel.add(onlineBtn);
      locationPanel.add(naBtn);

      return locationPanel;
    }

    private JPanel createStatusPanel(ReadOnlyCalendarEvent event) {
      JPanel statusPanel = new JPanel();
      statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
      statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

      JRadioButton privateBtn = new JRadioButton("Private");
      JRadioButton publicBtn = new JRadioButton("Public");
      JRadioButton naStatusBtn = new JRadioButton("N/A");

      ButtonGroup statusGroup = new ButtonGroup();
      statusGroup.add(privateBtn);
      statusGroup.add(publicBtn);
      statusGroup.add(naStatusBtn);

      if (event.getStatus() == Status.PRIVATE) {
        privateBtn.setSelected(true);
      } else if (event.getStatus() == Status.PUBLIC) {
        publicBtn.setSelected(true);
      } else {
        naStatusBtn.setSelected(true);
      }

      statusPanel.add(privateBtn);
      statusPanel.add(publicBtn);
      statusPanel.add(naStatusBtn);

      return statusPanel;
    }

    private JComboBox<String> createPropertyDropdown() {
      String[] properties = {"Subject", "Start Date/Time", "End Date/Time",
          "Description", "Location", "Status"};
      JComboBox<String> propertyDropdown = new JComboBox<>(properties);
      propertyDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
      propertyDropdown.setMaximumSize(new Dimension(Integer.MAX_VALUE,
              propertyDropdown.getPreferredSize().height));
      return propertyDropdown;
    }

    private JLabel createNewEventLabel(ReadOnlyCalendarEvent event) {
      JLabel eventInfoLabel = new JLabel("<html><b>Current Event:</b> " + event.getSubject()
              + "<br>Start: " + event.getStartDateTime().toString().replace("T", " ")
              + "<br>End: " + event.getEndDateTime().toString().replace("T", " ") + "</html>");
      eventInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      return eventInfoLabel;
    }

    private void updateEvent(ReadOnlyCalendarEvent event, JComboBox<String> propertyDropdown,
                             JPanel inputPanel) {
      String selected = (String) propertyDropdown.getSelectedItem();
      String newValue;

      switch (Objects.requireNonNull(selected)) {
        case "Subject":
          newValue = ((JTextField) inputPanel.getComponent(0)).getText();
          features.editEvent(event, "subject", newValue);
          break;

        case "Start Date/Time":
          JPanel dateTimePanel = (JPanel) inputPanel.getComponent(0);
          newValue = getDateTimeStringFromPanel(dateTimePanel);
          features.editEvent(event, "start", newValue);
          break;

        case "End Date/Time":
          JPanel endDateTimePanel = (JPanel) inputPanel.getComponent(0);
          newValue = getDateTimeStringFromPanel(endDateTimePanel);
          features.editEvent(event, "end", newValue);
          break;

        case "Description":
          newValue = ((JTextField) inputPanel.getComponent(0)).getText();
          features.editEvent(event, "description", newValue);
          break;

        case "Location":
          JPanel locationPanel = (JPanel) inputPanel.getComponent(0);
          if (((JRadioButton) locationPanel.getComponent(0)).isSelected()) {
            newValue = "PHYSICAL";
          } else if (((JRadioButton) locationPanel.getComponent(1)).isSelected()) {
            newValue = "ONLINE";
          } else {
            newValue = "N/A";
          }
          features.editEvent(event, "location", newValue);
          break;

        case "Status":
          JPanel statusPanel = (JPanel) inputPanel.getComponent(0);
          if (((JRadioButton) statusPanel.getComponent(0)).isSelected()) {
            newValue = "PRIVATE";
          } else if (((JRadioButton) statusPanel.getComponent(1)).isSelected()) {
            newValue = "PUBLIC";
          } else {
            newValue = "N/A";
          }
          features.editEvent(event, "status", newValue);
          break;

        default:
          break; // should never get here
      }
    }

    private JPanel getYMDHMSPanel(LocalDateTime dateTime) {
      JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

      // Create components for year, month, day
      JComboBox<Integer> yearBox = new JComboBox<>();
      for (int i = 0; i <= 9999; i++) {
        yearBox.addItem(i);
      }
      yearBox.setSelectedItem(dateTime.getYear());

      JComboBox<String> monthBox = new JComboBox<>();
      for (Month month : Month.values()) {
        monthBox.addItem(month.name());
      }
      monthBox.setSelectedItem(dateTime.getMonth().name());

      JComboBox<Integer> dayBox = new JComboBox<>();
      for (int i = 1; i <= 31; i++) {
        dayBox.addItem(i);
      }
      dayBox.setSelectedItem(dateTime.getDayOfMonth());

      JComboBox<Integer> hourBox = new JComboBox<>();
      for (int i = 0; i < 24; i++) {
        hourBox.addItem(i);
      }
      hourBox.setSelectedItem(dateTime.getHour());

      JComboBox<Integer> minuteBox = new JComboBox<>();
      JComboBox<Integer> secondBox = new JComboBox<>();
      for (int i = 0; i < 60; i++) {
        minuteBox.addItem(i);
        secondBox.addItem(i);
      }
      minuteBox.setSelectedItem(dateTime.getMinute());
      secondBox.setSelectedItem(dateTime.getSecond());

      panel.add(new JLabel("Year:"));
      panel.add(yearBox);
      panel.add(new JLabel("Month:"));
      panel.add(monthBox);
      panel.add(new JLabel("Day:"));
      panel.add(dayBox);
      panel.add(new JLabel("Hour:"));
      panel.add(hourBox);
      panel.add(new JLabel("Minute:"));
      panel.add(minuteBox);
      panel.add(new JLabel("Second:"));
      panel.add(secondBox);

      return panel;
    }

    private String getDateTimeStringFromPanel(JPanel panel) {
      JComboBox<Integer> yearBox = (JComboBox<Integer>) panel.getComponent(yearIndex);
      JComboBox<String> monthBox = (JComboBox<String>) panel.getComponent(monthIndex);
      JComboBox<Integer> dayBox = (JComboBox<Integer>) panel.getComponent(dayIndex);
      JComboBox<Integer> hourBox = (JComboBox<Integer>) panel.getComponent(hourIndex);
      JComboBox<Integer> minuteBox = (JComboBox<Integer>) panel.getComponent(minuteIndex);
      JComboBox<Integer> secondBox = (JComboBox<Integer>) panel.getComponent(secondIndex);

      LocalDateTime dateTime = convertDate(yearBox, monthBox,
              dayBox, hourBox, minuteBox, secondBox);

      return dateTime.toString();
    }

    private JPanel createEventPopupCenterPanel() {
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
      centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

      JPanel subjectPanel = enterSubjectPanel();
      JPanel startDatePanel = enterDatePanel("Start Date:", true);
      JPanel endDatePanel = enterDatePanel("End Date:", false);
      JPanel locationPanel = getLocationCheckbox();

      centerPanel.add(subjectPanel);
      centerPanel.add(startDatePanel);
      centerPanel.add(endDatePanel);
      centerPanel.add(getDescriptionPanel());
      centerPanel.add(locationPanel);
      centerPanel.add(getStatusCheckbox());

      return centerPanel;
    }

    private LocalDateTime convertDate(JComboBox<Integer> yearDropdown,
                                      JComboBox<String> monthDropdown,
                                      JComboBox<Integer> dayDropdown,
                                      JComboBox<Integer> hourDropdown,
                                      JComboBox<Integer> minuteDropdown,
                                      JComboBox<Integer> secondDropdown) {
      int year = (int) yearDropdown.getSelectedItem();
      int month = Month.valueOf((String) monthDropdown.getSelectedItem()).getValue();
      int day = (int) dayDropdown.getSelectedItem();
      int hour = (int) hourDropdown.getSelectedItem();
      int minute = (int) minuteDropdown.getSelectedItem();
      int second = (int) secondDropdown.getSelectedItem();
      return LocalDateTime.of(year, month, day, hour, minute, second);
    }

    private JPanel enterSubjectPanel() {
      // Create the component for displaying the "Event Name" label
      JLabel eventNameLabel = new JLabel("Event Name:");
      eventNameLabel.setFont(new Font("Arial", Font.BOLD, 16));
      eventNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

      // Create the text field for inputting event subject
      eventNameInput = new JTextField();
      eventNameInput.setFont(new Font("Arial", Font.PLAIN, 16));
      Dimension textFieldSize = new Dimension(360, SUBJECT_FIELD_HEIGHT);
      eventNameInput.setPreferredSize(textFieldSize);
      eventNameInput.setMaximumSize(textFieldSize);

      JPanel subjectPanel = new JPanel();
      subjectPanel.add(eventNameLabel);
      subjectPanel.add(eventNameInput);

      subjectPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 20, 10));

      return subjectPanel;
    }

    private JPanel enterDatePanel(String text, boolean isStartDate) {
      // Create calendar dropdowns panel
      JPanel calendarPanel = new JPanel();
      calendarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      calendarPanel.setLayout(new BoxLayout(calendarPanel, BoxLayout.Y_AXIS));

      // Add the text for displaying whether it's a start or end
      JLabel setDate = new JLabel(text);
      setDate.setFont(new Font("Arial", Font.BOLD, 16));
      setDate.setAlignmentX(Component.CENTER_ALIGNMENT);
      calendarPanel.add(setDate);

      // Add the text for helping input the year/month/day
      JLabel helperText = new JLabel("Year | Month | Day");
      helperText.setAlignmentX(Component.CENTER_ALIGNMENT);
      calendarPanel.add(helperText);

      // Add the dropdown menus for inputting years/months/days
      JPanel ymdDropdowns = getYMDdropdown(isStartDate);
      calendarPanel.add(ymdDropdowns);

      // Add the text for helping input the fields of hours/minutes/seconds
      JLabel helperText2 = new JLabel("Hour | Minute | Second");
      helperText2.setAlignmentX(Component.CENTER_ALIGNMENT);
      calendarPanel.add(helperText2);

      // Add the dropdown menus for inputting hours/minutes/seconds
      JPanel hmsDropdowns = getHMSdropdown(isStartDate);
      calendarPanel.add(hmsDropdowns);

      return calendarPanel;
    }

    private JPanel getYMDdropdown(boolean isStart) {
      JComboBox<Integer> yearsDropdown;
      JComboBox<String> monthsDropdown;
      JComboBox<Integer> daysDropdown;
      // Create the calendar labels and inputs
      Integer[] years = new Integer[10000];
      for (int i = 0; i < 10000; i++) {
        years[i] = i;
      }
      yearsDropdown = new JComboBox<>(years);
      // Set the default year to the current year
      yearsDropdown.setSelectedItem(LocalDate.now().getYear());

      String[] months = new String[12];
      for (int i = 1; i <= 12; i++) {
        months[i - 1] = Month.of(i).name();
      }
      monthsDropdown = new JComboBox<>(months);
      // Set the default month to the current month
      monthsDropdown.setSelectedItem(LocalDate.now().getMonth().toString());

      Integer[] days = new Integer[31];
      for (int i = 1; i <= 31; i++) {
        days[i - 1] = i;
      }
      daysDropdown = new JComboBox<>(days);
      // Set the default day to the current day
      daysDropdown.setSelectedItem(LocalDate.now().getDayOfMonth());

      if (isStart) {
        this.startYearsDropdown = yearsDropdown;
        this.startMonthsDropdown = monthsDropdown;
        this.startDaysDropdown = daysDropdown;
      } else {
        this.endYearsDropdown = yearsDropdown;
        this.endMonthsDropdown = monthsDropdown;
        this.endDaysDropdown = daysDropdown;
      }

      JPanel ymdDropdowns = new JPanel();
      ymdDropdowns.setLayout(new FlowLayout());
      ymdDropdowns.setAlignmentX(Component.CENTER_ALIGNMENT);
      ymdDropdowns.add(yearsDropdown);
      ymdDropdowns.add(monthsDropdown);
      ymdDropdowns.add(daysDropdown);

      return ymdDropdowns;
    }

    private JPanel getHMSdropdown(boolean isStart) {
      JComboBox<Integer> hoursDropdown;
      JComboBox<Integer> minsDropdown;
      JComboBox<Integer> secsDropdown;
      // Create the calendar labels and inputs
      Integer[] hours = new Integer[24];
      for (int i = 0; i <= 23; i++) {
        hours[i] = i;
      }
      hoursDropdown = new JComboBox<>(hours);
      if (isStart) {
        hoursDropdown.setSelectedItem(8);
      } else {
        hoursDropdown.setSelectedItem(17);
      }

      Integer[] minAndSec = new Integer[60];
      for (int i = 0; i <= 59; i++) {
        minAndSec[i] = i;
      }
      minsDropdown = new JComboBox<>(minAndSec);
      minsDropdown.setSelectedItem(0);
      secsDropdown = new JComboBox<>(minAndSec);
      secsDropdown.setSelectedItem(0);

      if (isStart) {
        this.startHoursDropdown = hoursDropdown;
        this.startMinsDropdown = minsDropdown;
        this.startSecsDropdown = secsDropdown;
      } else {
        this.endHoursDropdown = hoursDropdown;
        this.endMinsDropdown = minsDropdown;
        this.endSecsDropdown = secsDropdown;
      }

      // Add the dropdown menus for inputting years/months/days
      JPanel hmsDropdowns = new JPanel();
      hmsDropdowns.setLayout(new FlowLayout());
      hmsDropdowns.setAlignmentX(Component.CENTER_ALIGNMENT);
      hmsDropdowns.add(hoursDropdown);
      hmsDropdowns.add(minsDropdown);
      hmsDropdowns.add(secsDropdown);

      return hmsDropdowns;
    }

    private JPanel getLocationCheckbox() {
      JLabel locationLabel = new JLabel("Location:");
      locationLabel.setFont(new Font("Arial", Font.BOLD, 16));
      JCheckBox physicalCheckbox = new JCheckBox("Physical");
      JCheckBox onlineCheckbox = new JCheckBox("Online");
      JCheckBox defaultCheckbox = new JCheckBox("N/A");
      ButtonGroup locationGroup = new ButtonGroup();
      locationGroup.add(physicalCheckbox);
      locationGroup.add(onlineCheckbox);
      locationGroup.add(defaultCheckbox);
      defaultCheckbox.setSelected(true);

      physicalCheckbox.addActionListener((ActionEvent e) -> {
        if (physicalCheckbox.isSelected()) {
          this.location = Location.PHYSICAL;
        }
      });

      onlineCheckbox.addActionListener((ActionEvent i) -> {
        if (onlineCheckbox.isSelected()) {
          this.location = Location.ONLINE;
        }
      });

      defaultCheckbox.addActionListener((ActionEvent j) -> {
        if (defaultCheckbox.isSelected()) {
          this.location = null;
        }
      });

      JPanel locationPanel = new JPanel();
      locationPanel.setLayout(new FlowLayout());
      locationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      locationPanel.add(locationLabel);
      locationPanel.add(physicalCheckbox);
      locationPanel.add(onlineCheckbox);
      locationPanel.add(defaultCheckbox);
      return locationPanel;
    }

    private JPanel getDescriptionPanel() {
      JLabel descriptionLabel = new JLabel("Description:");
      descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      descriptionTextField = new JTextField();
      descriptionTextField.setPreferredSize(
              new Dimension(CREATE_POPUP_WIDTH, DESCRIPTION_FIELD_HEIGHT));
      descriptionTextField.setMaximumSize(
              new Dimension(CREATE_POPUP_WIDTH, DESCRIPTION_FIELD_HEIGHT));
      JPanel descriptionPanel = new JPanel();
      descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
      descriptionLabel.setFont(new Font("Arial", Font.BOLD, 16));
      descriptionPanel.add(descriptionLabel);
      descriptionPanel.add(descriptionTextField);
      descriptionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
      descriptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
      return descriptionPanel;
    }

    private JPanel getStatusCheckbox() {
      JLabel statusLabel = new JLabel("Status:");
      statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
      JCheckBox privateCheckbox = new JCheckBox("Private");
      JCheckBox publicCheckbox = new JCheckBox("Public");
      JCheckBox defaultCheckbox = new JCheckBox("N/A");
      ButtonGroup statusGroup = new ButtonGroup();
      statusGroup.add(privateCheckbox);
      statusGroup.add(publicCheckbox);
      statusGroup.add(defaultCheckbox);
      defaultCheckbox.setSelected(true);

      privateCheckbox.addActionListener((ActionEvent e) -> {
        if (privateCheckbox.isSelected()) {
          this.status = Status.PRIVATE;
        }
      });

      publicCheckbox.addActionListener((ActionEvent i) -> {
        if (publicCheckbox.isSelected()) {
          this.status = Status.PUBLIC;
        }
      });

      defaultCheckbox.addActionListener((ActionEvent j) -> {
        if (defaultCheckbox.isSelected()) {
          this.status = null;
        }
      });

      JPanel statusPanel = new JPanel();
      statusPanel.add(statusLabel);
      statusPanel.add(privateCheckbox);
      statusPanel.add(publicCheckbox);
      statusPanel.add(defaultCheckbox);
      return statusPanel;
    }

    private JPanel createEventPopupBottomPanel() {
      // Create the bottom panel for the exit button
      JPanel bottomPanel = new JPanel();
      bottomPanel.setLayout(new FlowLayout());

      // Add the create event button to the bottom panel
      JButton createButton = new JButton("Create");
      createButton.setPreferredSize(new Dimension(80, 20));
      createButton.addActionListener(e ->
              features.createEvent(eventNameInput.getText(),
                      convertDate(startYearsDropdown, startMonthsDropdown, startDaysDropdown,
                              startHoursDropdown, startMinsDropdown, startSecsDropdown),
                      convertDate(endYearsDropdown, endMonthsDropdown, endDaysDropdown,
                              endHoursDropdown, endMinsDropdown, endSecsDropdown),
                      this.descriptionTextField.getText(),
                      this.location,
                      this.status));
      createButton.addActionListener(e -> dialog.dispose());
      bottomPanel.add(createButton);

      return bottomPanel;
    }
  }

  /**
   * Creates a popup window for helping change the current starting date of the scheduled view.
   */
  private static class EditViewPopup {
    private static JComboBox<Integer> yearsBox;
    private static JComboBox<String> monthsBox;
    private static JComboBox<Integer> daysBox;

    private final Features features;
    private final JDialog dialog;

    private EditViewPopup(JFrame parent, Features features) {
      this.features = features;

      // Create the contents of the popup window
      JPanel contents = createEditViewPopup();

      JOptionPane popup = new JOptionPane(contents,
              JOptionPane.PLAIN_MESSAGE,
              JOptionPane.DEFAULT_OPTION,
              null,
              new Object[]{},
              null);

      dialog = popup.createDialog(parent, "Edit Starting Date");
      dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
      dialog.setVisible(true);
    }

    private JPanel createEditViewPopup() {
      JPanel editViewPanel = new JPanel();
      editViewPanel.setSize(CreateEventPopup.EDIT_POPUP_WIDTH, CreateEventPopup.EDIT_POPUP_HEIGHT);
      editViewPanel.setLayout(new BoxLayout(editViewPanel, BoxLayout.Y_AXIS));

      // Create the label for describing this menu
      JLabel editViewLabel = new JLabel();
      editViewPanel.setFont(new Font("Arial", Font.BOLD, 16));
      editViewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
      editViewLabel.setText("Change scheduled view's starting date");

      // Create the button for editing the view once pressed
      JButton editViewButton = new JButton();
      editViewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
      editViewButton.setText("Change");
      editViewButton.addActionListener(e ->
              features.showEventsFrom(getDateFromDropdowns()));
      editViewButton.addActionListener(e -> dialog.dispose());

      editViewPanel.add(editViewLabel);
      editViewPanel.add(getYMDdropdown());
      editViewPanel.add(editViewButton);

      return editViewPanel;
    }

    private static LocalDate getDateFromDropdowns() {
      int year = (int) yearsBox.getSelectedItem();
      Month month = Month.valueOf((String) monthsBox.getSelectedItem());
      int day = (int) daysBox.getSelectedItem();
      return LocalDate.of(year, month, day);
    }

    private JPanel getYMDdropdown() {
      JComboBox<Integer> yearsDropdown;
      JComboBox<String> monthsDropdown;
      JComboBox<Integer> daysDropdown;
      // Create the calendar labels and inputs
      Integer[] years = new Integer[10000];
      for (int i = 0; i < 10000; i++) {
        years[i] = i;
      }
      yearsDropdown = new JComboBox<>(years);
      // Set the default year to the current year
      yearsDropdown.setSelectedItem(LocalDate.now().getYear());

      String[] months = new String[12];
      for (int i = 1; i <= 12; i++) {
        months[i - 1] = Month.of(i).name();
      }
      monthsDropdown = new JComboBox<>(months);
      // Set the default month to the current month
      monthsDropdown.setSelectedItem(LocalDate.now().getMonth().toString());

      Integer[] days = new Integer[31];
      for (int i = 1; i <= 31; i++) {
        days[i - 1] = i;
      }
      daysDropdown = new JComboBox<>(days);
      // Set the default day to the current day
      daysDropdown.setSelectedItem(LocalDate.now().getDayOfMonth());

      yearsBox = yearsDropdown;
      monthsBox = monthsDropdown;
      daysBox = daysDropdown;

      // Add the dropdown menus for inputting years/months/days
      JPanel ymdDropdowns = new JPanel();
      ymdDropdowns.setLayout(new FlowLayout());
      ymdDropdowns.setAlignmentX(Component.CENTER_ALIGNMENT);
      ymdDropdowns.add(yearsBox);
      ymdDropdowns.add(monthsBox);
      ymdDropdowns.add(daysBox);

      return ymdDropdowns;
    }
  }

  /**
   * Creates a popup window for helping create a new calendar.
   */
  private class CreateCalendarPopup {
    private static final int POPUP_WIDTH = 300;
    private static final int POPUP_HEIGHT = 300;
    private static final int TEXT_FIELD_WIDTH = 200;
    private static final int TEXT_FIELD_HEIGHT = 30;

    private final Features features;
    private final JDialog dialog;

    private JTextField calendarNameTextField;
    private JComboBox<String> timezoneDropdown;

    public CreateCalendarPopup(JFrame parent, Features features, String prevCalendar) {
      this.features = features;

      // Create the contents of the popup window
      JPanel contents = createCalendarPopup();

      JOptionPane popup = new JOptionPane(contents,
              JOptionPane.PLAIN_MESSAGE,
              JOptionPane.DEFAULT_OPTION,
              null,
              new Object[]{},
              null);

      dialog = popup.createDialog(parent, "Create Calendar");
      dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
      dialog.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
          calendarMenu.setSelectedItem(prevCalendar);
          features.changeCalendar(prevCalendar);
        }
      });
      dialog.setVisible(true);
    }

    private JPanel createCalendarPopup() {
      JButton createCalendarButton = new JButton("Create");
      createCalendarButton.setAlignmentX(Component.CENTER_ALIGNMENT);
      createCalendarButton.addActionListener(e ->
              features.addCalendar(calendarNameTextField.getText(),
                      (String) timezoneDropdown.getSelectedItem()));
      createCalendarButton.addActionListener(e -> dialog.dispose());

      JPanel popup = new JPanel();
      popup.setSize(POPUP_WIDTH, POPUP_HEIGHT);
      popup.setLayout(new BoxLayout(popup, BoxLayout.Y_AXIS));

      popup.add(getCalendarNamePanel());
      popup.add(getCalendarTimezonePanel());
      popup.add(createCalendarButton);

      return popup;
    }

    private JPanel getCalendarNamePanel() {
      JLabel calendarNameLabel = new JLabel();
      calendarNameLabel.setText("Calendar Name:");
      calendarNameLabel.setFont(new Font("Arial", Font.BOLD, 13));

      calendarNameTextField = new JTextField();
      calendarNameTextField.setPreferredSize(new Dimension(TEXT_FIELD_WIDTH, TEXT_FIELD_HEIGHT));

      JPanel calendarNamePanel = new JPanel();
      calendarNamePanel.setLayout(new FlowLayout());

      calendarNamePanel.add(calendarNameLabel);
      calendarNamePanel.add(calendarNameTextField);

      return calendarNamePanel;
    }

    private JPanel getCalendarTimezonePanel() {
      JLabel calendarNameLabel = new JLabel();
      calendarNameLabel.setText("Calendar Timezone:");
      calendarNameLabel.setFont(new Font("Arial", Font.BOLD, 13));

      Set<String> zoneIds = new TreeSet<>(ZoneId.getAvailableZoneIds());
      timezoneDropdown = new JComboBox<>(zoneIds.toArray(new String[0]));
      timezoneDropdown.setSelectedItem(ZoneId.systemDefault().getId());

      JPanel calendarTimezonePanel = new JPanel();
      calendarTimezonePanel.setLayout(new FlowLayout());

      calendarTimezonePanel.add(calendarNameLabel);
      calendarTimezonePanel.add(timezoneDropdown);

      return calendarTimezonePanel;
    }
  }

  private void centerOnScreen() {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension window = this.getSize();
    int x = (screen.width - window.width) / 2;
    int y = (screen.height - window.height) / 2;
    this.setLocation(x, y);
  }
}
