import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JackTokenizer {

	private static final String KEYWORD_REGEX = "(class|constructor|function|method|field|static|var|int|char|boolean|void|true|false|null|this|let|do|if|else|while|return)";
	private static final String SYMBOLS_REGEX = "(\\+|\\-|\\{|}|;|,|=|\\.|\\[|]|/|\\*|&|\\||>|<|~|\\(|\\))";
	private static final String INTEGER_CONSTANTS_REGEX = "(3276[0-7]|327[0-5][0-9]|32[0-6][0-9][0-9]|3[0-1][0-9][0-9][0-9]|[1-2][0-9][0-9][0-9][0-9]|[1-9][0-9]{1,3}|[0-9])";
	private static final String STRINGS_REGEX = "(\".*\")";
	private static final String IDENTIFIERS_REGEX = "([a-zA-Z_][a-zA-Z0-9_]*)";

	private static final String REGEX = KEYWORD_REGEX + "|" + SYMBOLS_REGEX + "|" + INTEGER_CONSTANTS_REGEX + "|"
			+ STRINGS_REGEX + "|" + IDENTIFIERS_REGEX;

	private Pattern pattern;
	private Pattern keywordPattern;
	private Pattern symbolsPattern;
	private Pattern integerConstantsPattern;
	private Pattern stringsPattern;

	private BufferedReader reader;
	private String currentToken;
	private Matcher matcher;

	public JackTokenizer(File jackFile) throws FileNotFoundException {
		reader = new BufferedReader(new FileReader(jackFile));
		pattern = Pattern.compile(REGEX);
		keywordPattern = Pattern.compile(KEYWORD_REGEX);
		symbolsPattern = Pattern.compile(SYMBOLS_REGEX);
		integerConstantsPattern = Pattern.compile(INTEGER_CONSTANTS_REGEX);
		stringsPattern = Pattern.compile(STRINGS_REGEX);
	}

	public boolean hasMoreTokens() {
		return matcher != null;
	}

	public void advance() throws IOException {
		if (matcher != null && matcher.find()) {
			currentToken = matcher.group();
			return;
		}

		boolean isInComment = false;

		String nextCurrentLine = reader.readLine();

		while (nextCurrentLine != null) {
			nextCurrentLine = removeCommentsAndSpaces(nextCurrentLine);

			if (isInComment || nextCurrentLine.isEmpty()) {
				if (nextCurrentLine.endsWith("*/")) {
					isInComment = false;
				}

				nextCurrentLine = reader.readLine();
				continue;
			}

			if (nextCurrentLine.startsWith("/*") || nextCurrentLine.startsWith("/**")) {
				if (!nextCurrentLine.endsWith("*/") && !nextCurrentLine.endsWith("**/")) {
					isInComment = true;
				}

				nextCurrentLine = reader.readLine();
				continue;
			}

			matcher = pattern.matcher(nextCurrentLine);

			if (!matcher.find()) {
				nextCurrentLine = reader.readLine();
				continue;
			}

			currentToken = matcher.group();
			break;
		}

		if (nextCurrentLine == null) {
			matcher = null;
			reader.close();
		}
	}

	public TokenType tokenType() {
		if (keywordPattern.matcher(currentToken).matches()) {
			return TokenType.KEYWORD;
		}

		if (symbolsPattern.matcher(currentToken).matches()) {
			return TokenType.SYMBOL;
		}

		if (integerConstantsPattern.matcher(currentToken).matches()) {
			return TokenType.INT_CONST;
		}

		if (stringsPattern.matcher(currentToken).matches()) {
			return TokenType.STRING_CONST;
		}

		return TokenType.IDENTIFIER;
	}

	public String keyword() throws IOException {
		return currentToken;
	}

	public char symbol() {
		return currentToken.charAt(0);
	}

	public String identifier() {
		return currentToken;
	}

	public int intVal() {
		return Integer.parseInt(currentToken);
	}

	public String stringVal() {
		return currentToken;
	}

	private String removeCommentsAndSpaces(String line) {
		String[] parts = line.split("//");

		if (parts.length == 0) {
			return "";
		}

		return parts[0].trim();
	}
	
}