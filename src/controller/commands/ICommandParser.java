package controller.commands;

/**
 * Command pattern interface for parsing inputted commands.
 */
public interface ICommandParser {
  /**
   * Parses the command.
   */
  void parse();
}
