package controller.commands;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import view.IView;

/**
 * Abstract command parser class with helpful methods for parsing commands.
 */
public abstract class AbstractCommandParser implements ICommandParser {
  protected final IView view;
  protected final Scanner command;

  protected AbstractCommandParser(IView view, Scanner command) {
    this.view = view;
    this.command = command;
  }

  /**
   * Returns an array of days of week from the next string in the command. Valid {@code daysOfWeek}
   * are strings with M, T, W, R, F, S, U, for Monday to Sunday, respectively. For example, "MFU"
   * is a valid input, and represents Monday, Friday, and Sunday. There must be at least one day
   * of week provided.
   *
   * @return The array of {@code DayOfWeek} representing the given input.
   * @throws IllegalArgumentException If the syntax entered is invalid.
   */
  protected DayOfWeek[] nextDaysOfWeek() throws IllegalArgumentException {
    String nextCommand = command.next();
    if (!nextCommand.matches("[MTWRFSU]+")) {
      throw new IllegalArgumentException("Invalid input syntax, cannot parse days of week!");
    }

    Set<DayOfWeek> days = new HashSet<>();
    for (char dayOfWeek : nextCommand.toCharArray()) {
      switch (dayOfWeek) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default: // shouldn't ever get here
          break;
      }
    }
    DayOfWeek[] daysArray = days.toArray(new DayOfWeek[0]);
    Arrays.sort(daysArray);
    return daysArray;
  }

  /**
   * Gets the number of occurrences of the event series from the command words.
   *
   * @return The number of occurrences of the event series.
   * @throws IllegalArgumentException If the number of occurrences cannot be parsed.
   */
  protected int nextOccurrences() throws IllegalArgumentException {
    try {
      int occurrences = command.nextInt();
      String nextWord = command.next();

      if (!nextWord.equals("times")) { // nextWord should be "times"
        throw new IllegalArgumentException("Invalid input syntax, missing \"times\" keyword!");
      } else if (command.hasNext()) { // command word list should be empty now
        throw new IllegalArgumentException("Invalid input syntax, command should be empty"
                + "past \"times\"!");
      } else {
        return occurrences;
      }
    } catch (InputMismatchException e) {
      throw new IllegalArgumentException("Invalid input syntax, cannot parse occurrences!");
    }
  }

  /**
   * Parses multiple words, as long as the words start with and end with double quotes. Example:
   * {@code "Hello world!"} can correctly be parsed, but {@code "Hello world} fails.
   *
   * @return the parsed word, without the double quotes.
   */
  protected String nextMultipleWords() {
    StringBuilder words = new StringBuilder(command.next());
    if (words.charAt(0) == '"') { // this means the subject has multiple words
      while (command.hasNext() && words.charAt(words.length() - 1) != '"') {
        words.append(" ").append(command.next()); // add the next word as well
      }
    }
    // Remove the double quotes
    if (words.indexOf("\"") == -1) { // quotes does not exist so it is a single word
      return words.toString();
    } else { // quotes DO exist so it is multiple words so we delete the quotes
      return words.deleteCharAt(0).deleteCharAt(words.length() - 1).toString();
    }
  }

  /**
   * Ensures that the next keyword in the command is the same as {@code expected}. The keyword
   * must be one word long and cannot contain any spaces.
   *
   * @param expected the next expected keyword.
   * @throws IllegalArgumentException if the next keyword is not {@code expected}.
   */
  protected void nextIsExactly(String expected) throws IllegalArgumentException {
    if (!command.next().equals(expected)) {
      throw new IllegalArgumentException("Invalid input syntax, missing \""
              + expected + "\" keyword!");
    }
  }

  /**
   * Ensures that the next keyword in the command is in the list of {@code options}.
   *
   * @param options the list of next expected keywords.
   * @return the keyword, if it exists.
   * @throws IllegalArgumentException if the next keyword is not in {@code options}.
   */
  protected String nextIsOneOf(List<String> options) throws IllegalArgumentException {
    String word = command.next();
    if (options.contains(word)) {
      return word; // successfully removed the expected word
    } else {
      // still remove the word, but throw an exception
      throw new IllegalArgumentException("Invalid input syntax, "
              + "expected one of " + options + ", but got \"" + word + "\"!");
    }
  }

  /**
   * Gets the next date, in YYYY-MM-DD format.
   *
   * @return the next date.
   * @throws IllegalArgumentException if the date cannot be parsed.
   */
  protected LocalDate nextDate() throws IllegalArgumentException {
    try {
      return LocalDate.parse(command.next());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid input syntax, cannot parse date!");
    }
  }

  /**
   * Gets the next date/time, in YYYY-MM-DDThh:mm format.
   *
   * @return the next date/time.
   * @throws IllegalArgumentException if the date/time cannot be parsed.
   */
  protected LocalDateTime nextDateTime() throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(command.next());
    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException("Invalid input syntax, cannot parse date/time!");
    }
  }

  /**
   * Gets the equivalent all-day event starting date/time with the provided date.
   *
   * @param date the provided date.
   * @return the equivalent all-day event starting date/time.
   */
  protected LocalDateTime getAllDayEventStartingDateTime(LocalDate date) {
    return LocalDateTime.of(date, LocalTime.of(8, 0, 0));
  }

  /**
   * Gets the equivalent all-day event ending date/time with the provided date.
   *
   * @param date the provided date.
   * @return the equivalent all-day event ending date/time.
   */
  protected LocalDateTime getAllDayEventEndingDateTime(LocalDate date) {
    return LocalDateTime.of(date, LocalTime.of(17, 0, 0));
  }

  /**
   * Gets the starting date/time with the provided date. For example, providing {@code 2025-05-05}
   * would return {@code 2025-05-05T00:00}.
   *
   * @param date the provided date.
   * @return the starting date/time.
   */
  protected LocalDateTime getDayStartingDateTime(LocalDate date) {
    return LocalDateTime.of(date, LocalTime.of(0, 0));
  }

  /**
   * Gets the ending date/time with the provided date. For example, providing {@code 2025-05-05}
   * would return {@code 2025-05-05T11:59:59}.
   *
   * @param date the provided date.
   * @return the ending date/time.
   */
  protected LocalDateTime getDayEndingDateTime(LocalDate date) {
    return LocalDateTime.of(date, LocalTime.of(23, 59, 59));
  }
}

