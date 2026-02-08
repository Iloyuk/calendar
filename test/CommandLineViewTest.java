import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.LinkedList;

import model.event.ReadOnlyCalendarEvent;
import model.event.EventSeries;
import model.event.SingleEvent;
import view.IView;
import view.CommandLineView;

import static org.junit.Assert.assertEquals;

/**
 * Test class for verifying the correctness of our {@code CommandLineView} object.
 */
public class CommandLineViewTest {
  private ByteArrayOutputStream outputStream;
  private IView commandLineView;
  private List<ReadOnlyCalendarEvent> events;

  @Before
  public void setup() {
    outputStream = new ByteArrayOutputStream();
    commandLineView = new CommandLineView(new PrintStream(outputStream));
    events = new LinkedList<>();

    ReadOnlyCalendarEvent singleEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2025, 5, 5, 9, 0, 0),
            LocalDateTime.of(2025, 5, 5, 10, 0, 0)).build();

    ReadOnlyCalendarEvent eventSeries = new EventSeries(
            new SingleEvent.Builder("Weekly Meeting",
                    LocalDateTime.of(2025, 6, 2, 8, 0, 0),
                    LocalDateTime.of(2025, 6, 2, 9, 0, 0))
                    .build(),
            3, DayOfWeek.MONDAY);

    events.add(singleEvent);
    events.add(eventSeries);
  }

  @Test
  public void testViewWritesMessageProperly() {
    commandLineView.writeMessage("Test output");
    assertEquals("Test output\n", outputStream.toString());
  }

  @Test
  public void testViewDisplaysEventsProperly() {
    commandLineView.showEvents(events);
    String expected = "• [Subject: Meeting, Start: 2025-05-05T09:00, End: 2025-05-05T10:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Weekly Meeting, Start: 2025-06-02T08:00, End: 2025-06-02T09:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Weekly Meeting, Start: 2025-06-09T08:00, End: 2025-06-09T09:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n"
            + "• [Subject: Weekly Meeting, Start: 2025-06-16T08:00, End: 2025-06-16T09:00,"
            + " Description: N/A, Location: N/A, Status: N/A]\n\n";
    assertEquals(expected, outputStream.toString());
  }

}