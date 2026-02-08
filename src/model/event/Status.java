package model.event;

/**
 * An enumeration of the possible statuses of an event.
 */
public enum Status {
  PUBLIC, PRIVATE;

  /**
   * Gets the {@code Status} enumeration from the representing string.
   *
   * @param str the string.
   * @return The {@code Status} enumeration.
   */
  public static Status getStatus(String str) {
    String lowercase = str.toLowerCase();
    if (lowercase.equals("public")) {
      return PUBLIC;
    } else if (lowercase.equals("private")) {
      return PRIVATE;
    }
    return null;
  }

  /**
   * Gets the string representation of this {@code Status}. For example, PUBLIC -> "Public".
   *
   * @return the string representation.
   */
  @Override
  public String toString() {
    if (this == PUBLIC) {
      return "Public";
    } else if (this == PRIVATE) {
      return "Private";
    }
    return ""; // shouldn't ever get here
  }
}