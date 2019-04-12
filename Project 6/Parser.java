
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Parser {
	public static final String NO_DESTINATION = "";
	public static final String NO_JUMP = "";

	private static final char A_COMMAND_START_CHAR = '@';
	private static final char L_COMMAND_START_CHAR = '(';

	private static final String EQUAL_FOR_C_COMMAND = "=";
	private static final String JUMP_FOR_C_COMMAND = ";";
	private static final String COMMENT_COMMAND = "//";

	private String fileName;
	private BufferedReader reader;
	private String currentLine;

	private int indexForJump;
	private int indexForEqual;

	public Parser(String filename) throws IOException {
		fileName = filename;

		reader = new BufferedReader(new FileReader(fileName));
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
				updateCurrentLineValues(nextCurrentLine);

				return;
			}

			nextCurrentLine = reader.readLine();
		}

		// If no more lines
		currentLine = null;
		reader.close();
	}

	public String symbol() {
		int substringTo = currentLine.length() - 1;

		if (commandType() == CommandType.A_COMMAND) {
			substringTo++;
		}

		return currentLine.substring(1, substringTo);
	}

	public String dest() {
		return isDestExists() ? currentLine.substring(0, indexForEqual) : NO_DESTINATION;
	}

	public String comp() {
		if (!isDestExists() && isJumpExists()) {
			return currentLine.substring(0, indexForJump);
		} else if (isDestExists() && isJumpExists()) {
			return currentLine.substring(indexForEqual + 1, indexForJump);
		} else if (isDestExists() && !isJumpExists()) {
			return currentLine.substring(indexForEqual + 1);
		}

		// No dest and no jump -> only comp
		return currentLine;
	}

	public String jump() {
		if (!isJumpExists()) {
			return NO_JUMP;
		}

		return currentLine.substring(indexForJump + 1);
	}

	public CommandType commandType() {
		char firstCommandChar = currentLine.charAt(0);

		if (firstCommandChar == A_COMMAND_START_CHAR) {
			return CommandType.A_COMMAND;
		}

		if (firstCommandChar == L_COMMAND_START_CHAR) {
			return CommandType.L_COMMAND;
		}

		return CommandType.C_COMMAND;
	}

	private String removeCommentsAndSpaces(String line) {
		return line.split(COMMENT_COMMAND)[0].trim();
	}

	private void updateCurrentLineValues(String nextCurrentLine) {
		indexForJump = nextCurrentLine.indexOf(JUMP_FOR_C_COMMAND);
		indexForEqual = nextCurrentLine.indexOf(EQUAL_FOR_C_COMMAND);

		currentLine = nextCurrentLine;
	}

	private boolean isDestExists() {
		return indexForEqual != -1;
	}

	private boolean isJumpExists() {
		return indexForJump != -1;
	}
}