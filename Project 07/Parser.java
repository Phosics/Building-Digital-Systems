import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
	private static final String SPACE = " ";
	private static final String COMMENT_COMMAND = "//";

	private static final String PUSH_COMMAND_FIRST_WORD = "push";
	private static final String POP_COMMAND_FIRST_WORD = "pop";

	private BufferedReader reader;
	private String currentLine;
	private CommandType commandType;

	public Parser(String filename) throws IOException {
		reader = new BufferedReader(new FileReader(filename));
	}

	public boolean hasMoreCommands() {
		return currentLine != null;
	}

	public void advance() throws IOException {
		String nextCurrentLine = reader.readLine();

		while (nextCurrentLine != null) {
			nextCurrentLine = removeCommentsAndSpaces(nextCurrentLine);

			// Found an non-empty line
			if (!nextCurrentLine.isEmpty()) {
				this.currentLine = nextCurrentLine;
				findCommandType();

				return;
			}

			nextCurrentLine = reader.readLine();
		}

		// If no more lines
		currentLine = null;
		reader.close();
	}

	public CommandType getCommandType() {
		return commandType;
	}

	public String arg1() {
		return currentLine.split(SPACE)[0];
	}

	public int arg2() {
		return Integer.parseInt(currentLine.split(SPACE)[1]);
	}

	private void findCommandType() {
		String[] args = currentLine.split(SPACE);

		if (args[0].equals(PUSH_COMMAND_FIRST_WORD)) {
			commandType = CommandType.C_PUSH;

			currentLine = args[1] + SPACE + args[2];
		} else if (args[0].equals(POP_COMMAND_FIRST_WORD)) {
			commandType = CommandType.C_POP;

			currentLine = args[1] + SPACE + args[2];
		} else {
			commandType = CommandType.C_ARITHMETIC;
		}
	}

	private String removeCommentsAndSpaces(String line) {
		return line.split(COMMENT_COMMAND)[0].trim();
	}
}
