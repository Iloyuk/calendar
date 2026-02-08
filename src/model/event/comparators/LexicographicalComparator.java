package model.event.comparators;

import java.util.Comparator;

import model.event.ReadOnlyCalendarEvent;

/**
 * Comparator class for lexicographically comparing the strings of the
 * {@code ReadOnlyCalendarEvent}'s subjects.
 */
public class LexicographicalComparator implements Comparator<ReadOnlyCalendarEvent> {
  private final Comparator<ReadOnlyCalendarEvent> secondaryComparator;

  /**
   * Default constructor for {@code LexicographicalComparator}, with no secondary comparator.
   */
  public LexicographicalComparator() {
    secondaryComparator = null;
  }

  /**
   * Constructs a {@code Lexicographical} with a secondary comparing technique,
   * if both the events' start date/times are the same.
   *
   * @param comparator the secondary comparator.
   */
  public LexicographicalComparator(Comparator<ReadOnlyCalendarEvent> comparator) {
    secondaryComparator = comparator;
  }

  @Override
  public int compare(ReadOnlyCalendarEvent o1, ReadOnlyCalendarEvent o2) {
    if (secondaryComparator == null) {
      return o1.getSubject().compareTo(o2.getSubject());
    } else {
      if (o1.getSubject().equals(o2.getSubject())) {
        return secondaryComparator.compare(o1, o2);
      } else {
        return o1.getSubject().compareTo(o2.getSubject());
      }
    }
  }
}
