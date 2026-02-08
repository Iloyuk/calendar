package model.event;

/**
 * An enumeration of the possible locations of an event.
 */
public enum Location {
  PHYSICAL, ONLINE;

  /**
   * Gets the {@code Location} enumeration from the representing string.
   *
   * @param str the string.
   * @return The {@code Location} enumeration.
   */
  public static Location getLocation(String str) {
    String lowercase = str.toLowerCase();
    if (lowercase.equals("online")) {
      return ONLINE;
    } else if (lowercase.equals("physical")) {
      return PHYSICAL;
    }
    return null;
  }

  /**
   * Gets the string representation of this {@code Location}. For example, PHYSICAL -> "Physical".
   *
   * @return the string representation.
   */
  @Override
  public String toString() {
    if (this == PHYSICAL) {
      return "Physical";
    } else if (this == ONLINE) {
      return "Online";
    }
    return ""; // shouldn't ever get here
  }
}