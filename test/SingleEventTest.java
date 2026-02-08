import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import model.event.ISingleEvent;
import model.event.Location;
import model.event.Status;
import model.event.SingleEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * Test class verifying the correctness of our {@code SingleEvent} class.
 */
public class SingleEventTest {

  @Test
  public void testSingleEventBuilderWithOnlySubjectAndDate() {
    ISingleEvent singleEvent = new SingleEvent.Builder(
            "Birthday", LocalDate.of(2006, 7, 1)).build();

    assertEquals("Birthday", singleEvent.getSubject());
    // Start time should be 8:00AM since end time was not set
    assertEquals(LocalDateTime.of(2006, 7, 1, 8, 0, 0), singleEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2006, 7, 1, 17, 0, 0), singleEvent.getEndDateTime());
    assertNull(singleEvent.getDescription());
    assertNull(singleEvent.getLocation());
    assertNull(singleEvent.getStatus());
  }

  @Test
  public void testSingleEventBuilderWithPartialFields() {
    ISingleEvent singleEvent = new SingleEvent.Builder("CS3500",
            LocalDateTime.of(2025, 5, 26, 11, 40, 0),
            LocalDateTime.of(2025, 5, 26, 13, 20, 0))
            .description("OOD")
            .location(Location.PHYSICAL)
            .build();

    assertEquals("CS3500", singleEvent.getSubject());
    assertEquals(LocalDateTime.of(2025, 5, 26, 11, 40,
            0), singleEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 26, 13, 20,
            0), singleEvent.getEndDateTime());
    assertEquals("OOD", singleEvent.getDescription());
    assertEquals(Location.PHYSICAL, singleEvent.getLocation());
    assertNull(singleEvent.getStatus());
  }

  @Test
  public void testSingleEventBuilderWithAllFields() {
    ISingleEvent singleEvent = new SingleEvent.Builder("Dinner Out",
            LocalDateTime.of(2025, 5, 31, 18, 0, 0),
            LocalDateTime.of(2025, 5, 31, 19, 0, 0))
            .description("Dinner with friends")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    assertEquals("Dinner Out", singleEvent.getSubject());
    assertEquals(LocalDateTime.of(2025, 5, 31, 18, 0,
            0), singleEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2025, 5, 31, 19, 0,
            0), singleEvent.getEndDateTime());
    assertEquals("Dinner with friends", singleEvent.getDescription());
    assertEquals(Location.PHYSICAL, singleEvent.getLocation());
    assertEquals(Status.PRIVATE, singleEvent.getStatus());
  }

  @Test
  public void testSingleEventEqualsWhenSameSubjectAndTimes() {
    // Description, location, and status shouldn't matter for checking SingleEvent equality
    ISingleEvent event1 = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .description("Meeting 1")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    ISingleEvent event2 = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .description("Meeting 2")
            .location(Location.ONLINE)
            .status(Status.PUBLIC)
            .build();

    ISingleEvent event3 = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .description("Meeting 3")
            .location(Location.PHYSICAL)
            .status(Status.PUBLIC)
            .build();

    // Reflexive
    assertEquals(event1, event1);
    assertEquals(event1.hashCode(), event1.hashCode());
    // Symmetric
    assertEquals(event1, event2);
    assertEquals(event2, event1);
    assertEquals(event1.hashCode(), event2.hashCode());
    assertEquals(event2.hashCode(), event1.hashCode());
    // Transitive
    assertEquals(event1, event2);
    assertEquals(event2, event3);
    assertEquals(event1, event3);
    assertEquals(event1.hashCode(), event2.hashCode());
    assertEquals(event2.hashCode(), event3.hashCode());
    assertEquals(event1.hashCode(), event3.hashCode());
  }

  @Test
  public void testSingleEventDoesNotEqualWhenDifferentSubjectButSameTimes() {
    ISingleEvent event1 = new SingleEvent.Builder("Conference",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .build();

    ISingleEvent event2 = new SingleEvent.Builder("Workshop",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .build();

    assertNotEquals(event1, event2);
    assertNotEquals(event2, event1);
    assertNotEquals(event1.hashCode(), event2.hashCode());
    assertNotEquals(event2.hashCode(), event1.hashCode());
  }

  @Test
  public void testSingleEventDoesNotEqualWhenSameSubjectButDifferentTimes() {
    ISingleEvent event1 = new SingleEvent.Builder("Webinar",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .build();

    ISingleEvent event2 = new SingleEvent.Builder("Webinar",
            LocalDateTime.of(2023, 10, 1, 10, 0, 0),
            LocalDateTime.of(2023, 10, 1, 11, 0, 0))
            .build();

    assertNotEquals(event1, event2);
    assertNotEquals(event2, event1);
    assertNotEquals(event1.hashCode(), event2.hashCode());
    assertNotEquals(event2.hashCode(), event1.hashCode());
  }

  @Test
  public void testSingleEventDoesNotEqualWhenCompletelyDifferent() {
    ISingleEvent event1 = new SingleEvent.Builder("Project Meeting 1",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .build();

    ISingleEvent event2 = new SingleEvent.Builder("Project Meeting 2",
            LocalDateTime.of(2023, 10, 2, 9, 0, 0),
            LocalDateTime.of(2023, 10, 2, 10, 0, 0))
            .build();

    assertNotEquals(event1, event2);
    assertNotEquals(event2, event1);
    assertNotEquals(event1.hashCode(), event2.hashCode());
    assertNotEquals(event2.hashCode(), event1.hashCode());
  }

  @Test
  public void testCreateNewEventWithSubject() {
    // Create original event with all fields populated
    ISingleEvent originalEvent = new SingleEvent.Builder("Original Subject",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 0, 0))
            .description("Original description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    ISingleEvent newEvent = originalEvent.createNewEventWithSubject("New Subject");
    assertEquals("New Subject", newEvent.getSubject());

    assertEquals(originalEvent.getStartDateTime(), newEvent.getStartDateTime());
    assertEquals(originalEvent.getEndDateTime(), newEvent.getEndDateTime());
    assertEquals(originalEvent.getDescription(), newEvent.getDescription());
    assertEquals(originalEvent.getLocation(), newEvent.getLocation());
    assertEquals(originalEvent.getStatus(), newEvent.getStatus());

    assertEquals("Original Subject", originalEvent.getSubject());

    // Test with empty subject
    ISingleEvent emptySubjectEvent = originalEvent.createNewEventWithSubject("");
    assertEquals("", emptySubjectEvent.getSubject());

    // Test with null subject
    ISingleEvent nullSubjectEvent = originalEvent.createNewEventWithSubject(null);
    assertNull(nullSubjectEvent.getSubject());
  }

  @Test
  public void testCreateNewEventWithNewStartDateLaterSameDay() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Team meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    LocalDateTime newStartTime = LocalDateTime.of(2023, 10, 1, 14,
            0, 0);
    ISingleEvent laterEvent = originalEvent.createNewEventWithNewStartDate(newStartTime);

    assertEquals(newStartTime, laterEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 10, 1, 15, 30,
            0), laterEvent.getEndDateTime());
    assertEquals(originalEvent.getSubject(), laterEvent.getSubject());
    assertEquals(originalEvent.getDescription(), laterEvent.getDescription());
    assertEquals(originalEvent.getLocation(), laterEvent.getLocation());
    assertEquals(originalEvent.getStatus(), laterEvent.getStatus());
  }

  @Test
  public void testCreateNewEventWithNewStartDateEarlierSameDay() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Team meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    LocalDateTime earlierTime = LocalDateTime.of(2023, 10, 1, 7,
            0, 0);
    ISingleEvent earlierEvent = originalEvent.createNewEventWithNewStartDate(earlierTime);

    assertEquals(earlierTime, earlierEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 10, 1, 8, 30,
            0), earlierEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewStartDateDifferentDay() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Team meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    LocalDateTime differentDay = LocalDateTime.of(2023, 10, 15, 9,
            0, 0);
    ISingleEvent differentDayEvent = originalEvent.createNewEventWithNewStartDate(differentDay);

    assertEquals(differentDay, differentDayEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 10, 15, 10,
            30, 0), differentDayEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewStartDateOriginalUnchanged() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Team meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    originalEvent.createNewEventWithNewStartDate(LocalDateTime.of(2023,
            10, 1, 14, 0, 0));

    assertEquals(LocalDateTime.of(2023, 10, 1, 9,
            0, 0), originalEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 10, 1, 10,
            30, 0), originalEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewEndDate() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Team meeting")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    LocalDateTime laterEndTime = LocalDateTime.of(2023, 10, 1, 11,
            45, 0);
    ISingleEvent laterEvent = originalEvent.createNewEventWithNewEndDate(laterEndTime);

    assertEquals(laterEndTime, laterEvent.getEndDateTime());
    assertEquals(originalEvent.getStartDateTime(), laterEvent.getStartDateTime());
    assertEquals(originalEvent.getSubject(), laterEvent.getSubject());
    assertEquals(originalEvent.getDescription(), laterEvent.getDescription());
    assertEquals(originalEvent.getLocation(), laterEvent.getLocation());
    assertEquals(originalEvent.getStatus(), laterEvent.getStatus());

    assertEquals(LocalDateTime.of(2023, 10, 1, 10, 30,
            0), originalEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithSameEndDate() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .build();

    ISingleEvent newEvent = originalEvent.createNewEventWithNewEndDate(
            LocalDateTime.of(2023, 10, 1, 10, 30, 0));

    assertEquals(originalEvent, newEvent);
    assertNotEquals(System.identityHashCode(originalEvent), System.identityHashCode(newEvent));
  }

  @Test
  public void testCreateNewEventWithEndDateBeforeStartDate() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .build();

    assertThrows(IllegalArgumentException.class, () -> originalEvent.createNewEventWithNewEndDate(
            LocalDateTime.of(2023, 10, 1, 8, 0, 0)));
  }

  @Test
  public void testCreateNewEventWithNewDescription() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    ISingleEvent newEvent = originalEvent.createNewEventWithNewDescription("New description");
    assertEquals("New description", newEvent.getDescription());

    assertEquals(originalEvent.getSubject(), newEvent.getSubject());
    assertEquals(originalEvent.getStartDateTime(), newEvent.getStartDateTime());
    assertEquals(originalEvent.getEndDateTime(), newEvent.getEndDateTime());
    assertEquals(originalEvent.getLocation(), newEvent.getLocation());
    assertEquals(originalEvent.getStatus(), newEvent.getStatus());

    assertEquals("Original description", originalEvent.getDescription());
  }

  @Test
  public void testCreateNewEventWithEmptyDescription() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .build();

    ISingleEvent emptyDescEvent = originalEvent.createNewEventWithNewDescription("");
    assertEquals("", emptyDescEvent.getDescription());
  }

  @Test
  public void testCreateNewEventWithNullDescription() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .build();

    ISingleEvent nullDescEvent = originalEvent.createNewEventWithNewDescription(null);
    assertNull(nullDescEvent.getDescription());
  }

  @Test
  public void testCreateNewEventWithNewLocation() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    ISingleEvent newEvent = originalEvent.createNewEventWithNewLocation(Location.ONLINE);
    assertEquals(Location.ONLINE, newEvent.getLocation());

    assertEquals(originalEvent.getSubject(), newEvent.getSubject());
    assertEquals(originalEvent.getStartDateTime(), newEvent.getStartDateTime());
    assertEquals(originalEvent.getEndDateTime(), newEvent.getEndDateTime());
    assertEquals(originalEvent.getDescription(), newEvent.getDescription());
    assertEquals(originalEvent.getStatus(), newEvent.getStatus());

    assertEquals(Location.PHYSICAL, originalEvent.getLocation());
  }

  @Test
  public void testCreateNewEventWithNullLocation() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .location(Location.PHYSICAL)
            .build();

    ISingleEvent nullLocationEvent = originalEvent.createNewEventWithNewLocation(null);
    assertNull(nullLocationEvent.getLocation());
  }

  @Test
  public void testCreateNewEventWithNewStatus() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStatus(Status.PUBLIC);
    assertEquals(Status.PUBLIC, newEvent.getStatus());

    assertEquals(originalEvent.getSubject(), newEvent.getSubject());
    assertEquals(originalEvent.getStartDateTime(), newEvent.getStartDateTime());
    assertEquals(originalEvent.getEndDateTime(), newEvent.getEndDateTime());
    assertEquals(originalEvent.getDescription(), newEvent.getDescription());
    assertEquals(originalEvent.getLocation(), newEvent.getLocation());

    assertEquals(Status.PRIVATE, originalEvent.getStatus());
  }

  @Test
  public void testCreateNewEventWithNullStatus() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .status(Status.PRIVATE)
            .build();

    ISingleEvent nullStatusEvent = originalEvent.createNewEventWithNewStatus(null);
    assertNull(nullStatusEvent.getStatus());
  }

  @Test
  public void testCreateNewEventWithNewStartDateAndEndDate() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Original description")
            .location(Location.PHYSICAL)
            .status(Status.PRIVATE)
            .build();

    LocalDateTime newStartTime = LocalDateTime.of(2023, 10, 2, 13, 0, 0);
    LocalDateTime newEndTime = LocalDateTime.of(2023, 10, 2, 15, 0, 0);

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStartDateAndEndDate(
            newStartTime, newEndTime);

    assertEquals(newStartTime, newEvent.getStartDateTime());
    assertEquals(newEndTime, newEvent.getEndDateTime());
    assertEquals(originalEvent.getSubject(), newEvent.getSubject());
    assertEquals(originalEvent.getDescription(), newEvent.getDescription());
    assertEquals(originalEvent.getLocation(), newEvent.getLocation());
    assertEquals(originalEvent.getStatus(), newEvent.getStatus());

    assertEquals(LocalDateTime.of(2023, 10, 1, 9, 0, 0), originalEvent.getStartDateTime());
    assertEquals(LocalDateTime.of(2023, 10, 1, 10, 30, 0), originalEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewStartDateAndEndDateSameDay() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Presentation",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .build();

    LocalDateTime newStartTime = LocalDateTime.of(2023, 10, 1, 14, 0, 0);
    LocalDateTime newEndTime = LocalDateTime.of(2023, 10, 1, 16, 0, 0);

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStartDateAndEndDate(
            newStartTime, newEndTime);

    assertEquals(newStartTime, newEvent.getStartDateTime());
    assertEquals(newEndTime, newEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewStartDateAndEndDateZeroDuration() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Quick Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .build();

    LocalDateTime sameTime = LocalDateTime.of(2023, 10, 2, 15, 0, 0);

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStartDateAndEndDate(
            sameTime, sameTime);

    assertEquals(sameTime, newEvent.getStartDateTime());
    assertEquals(sameTime, newEvent.getEndDateTime());
  }

  @Test
  public void testCreateNewEventWithNewStartDateAndEndDateKeepsFields() {
    ISingleEvent originalEvent = new SingleEvent.Builder("Meeting",
            LocalDateTime.of(2023, 10, 1, 9, 0, 0),
            LocalDateTime.of(2023, 10, 1, 10, 30, 0))
            .description("Important meeting")
            .location(Location.ONLINE)
            .status(Status.PUBLIC)
            .build();

    LocalDateTime newStartTime = LocalDateTime.of(2023, 11, 15, 13, 0, 0);
    LocalDateTime newEndTime = LocalDateTime.of(2023, 11, 15, 14, 30, 0);

    ISingleEvent newEvent = originalEvent.createNewEventWithNewStartDateAndEndDate(
            newStartTime, newEndTime);

    assertEquals("Important meeting", newEvent.getDescription());
    assertEquals(Location.ONLINE, newEvent.getLocation());
    assertEquals(Status.PUBLIC, newEvent.getStatus());
  }

}

