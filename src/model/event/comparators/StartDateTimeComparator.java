package model.event.comparators;

import java.util.Comparator;

import model.event.ReadOnlyCalendarEvent;

/**
 * Comparator class for comparing the date/times of the {@code ReadOnlyCalendarEvent}.
 */
public class StartDateTimeComparator implements Comparator<ReadOnlyCalendarEvent> {
  private final Comparator<ReadOnlyCalendarEvent> secondaryComparator;

  /**
   * Default constructor for {@code StartDateTimeComparator}, with no secondary comparator.
   */
  public StartDateTimeComparator() {
    secondaryComparator = null;
  }

  /**
   * Constructs a {@code StartDateTimeComparator} with a secondary comparing technique,
   * if both the events' start date/times are the same.
   *
   * @param comparator the secondary comparator.
   */
  public StartDateTimeComparator(Comparator<ReadOnlyCalendarEvent> comparator) {
    secondaryComparator = comparator;
  }

  @Override
  public int compare(ReadOnlyCalendarEvent o1, ReadOnlyCalendarEvent o2) {
    if (secondaryComparator == null) {
      return o1.getStartDateTime().compareTo(o2.getStartDateTime());
    } else {
      if (o1.getStartDateTime().equals(o2.getStartDateTime())) {
        return secondaryComparator.compare(o1, o2);
      } else {
        return o1.getStartDateTime().compareTo(o2.getStartDateTime());
      }
    }
  }
}
