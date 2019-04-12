import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CompilationEngine {
	private static final String CLASS = "class";
	private static final String CLASS_VAR_DEC = "classVarDec";
	private static final String SUBROUTINE_DEC = "subroutineDec";
	private static final String PARAMETER_LIST = "parameterList";
	private static final String SUBROUTINE_BODY = "subroutineBody";
	private static final String VAR_DEC = "varDec";
	private static final String STATEMENTS = "statements";
	private static final String LET_STATEMENT = "letStatement";
	private static final String IF_STATEMENT = "ifStatement";
	private static final String WHILE_STATEMENT = "whileStatement";
	private static final String DO_STATEMENT = "doStatement";
	private static final String RETURN_STATEMENT = "returnStatement";
	private static final String EXPRESSION = "expression";
	private static final String TERM = "term";
	private static final String EXPRESSION_LIST = "expressionList";
	private static final String STRING_CONSTANT = "stringConstant";
	private static final String INTEGER_CONSTANT = "integerConstant";

	private static final boolean NEW_LINE = true;
	private static final boolean NO_NEW_LINE = false;

	private static final boolean WRITE_SUBROUTINE_NAME = true;
	private static final boolean DONT_WRITE_SUBROUTINE_NAME = false;

	private static final String METHOD = "method";
	private static final String FUNCTION = "function";
	private static final String CONSTRUCTOR = "constructor";
	private static final String VOID = "void";
	private static final String VAR = "var";
	private static final String STATIC = "static";
	private static final String FIELD = "field";
	private static final String LET = "let";
	private static final String DO = "do";
	private static final String IF = "if";
	private static final String ELSE = "else";
	private static final String WHILE = "while";
	private static final String RETURN = "return";

	private JackTokenizer tokenizer;
	private BufferedWriter writer;

	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFile));
		tokenizer = new JackTokenizer(inputFile);
	}

	public void compile() throws IOException {
		tokenizer.advance();

		compileClass();

		writer.close();
	}

	private void compileClass() throws IOException {
		// 'class' className '{' classVarDec* subroutineDec* '}'
		startCommand(CLASS);

		writeKeyword();
		writeIdentifier();
		writeSymbol();

		while (tokenizer.tokenType().isKeyword() && isVarTypeClassKeyword()) {
			compileClassVarDec();
		}

		while (tokenizer.tokenType().isKeyword() && isMethodKeyword()) {
			compileSubroutineDec();
		}

		writeSymbol();

		endCommand(CLASS);
	}

	private void compileClassVarDec() throws IOException {
		// ('static'|'field') type varName (',' varName)* ';'
		startCommand(CLASS_VAR_DEC);

		writeKeyword();
		compileType();
		writeIdentifier();

		while (tokenizer.symbol() == ',') {
			writeSymbol();
			writeIdentifier();
		}

		writeSymbol();

		endCommand(CLASS_VAR_DEC);
	}

	private void compileSubroutineDec() throws IOException {
		// ('constructor'|'function'|'method') ('void'|type) subroutineName
		// '('parameterList ')' subroutineBody
		startCommand(SUBROUTINE_DEC);

		writeKeyword();

		if (tokenizer.tokenType().isKeyword() && tokenizer.keyword().equals(VOID)) {
			writeKeyword();
		} else {
			compileType();
		}

		writeIdentifier();
		writeSymbol();
		compileParameterList();
		writeSymbol();
		compileSubroutineBody();

		endCommand(SUBROUTINE_DEC);
	}

	private void compileParameterList() throws IOException {
		// ((type varName) (',' type varName)*)?
		startCommand(PARAMETER_LIST);

		if (tokenizer.tokenType().isSymbol() && tokenizer.symbol() == ')') {
			endCommand(PARAMETER_LIST);

			return;
		}

		compileType();
		writeIdentifier();

		while (tokenizer.symbol() == ',') {
			writeSymbol();
			compileType();
			writeIdentifier();
		}

		endCommand(PARAMETER_LIST);
	}

	private void compileSubroutineBody() throws IOException {
		// '{' varDec* statements '}'
		startCommand(SUBROUTINE_BODY);
		writeSymbol();

		while (tokenizer.tokenType().isKeyword() && tokenizer.keyword().equals(VAR)) {
			compileVarDec();
		}

		compileStatements();
		writeSymbol();

		endCommand(SUBROUTINE_BODY);
	}

	private void compileVarDec() throws IOException {
		// 'var' type varName (',' varName)* ';'
		startCommand(VAR_DEC);

		writeKeyword();
		compileType();
		writeIdentifier();

		while (tokenizer.symbol() == ',') {
			writeSymbol();
			writeIdentifier();
		}

		writeSymbol();

		endCommand(VAR_DEC);
	}

	private void compileStatements() throws IOException {
		// statement*
		startCommand(STATEMENTS);

		while (tokenizer.tokenType().isKeyword()) {
			switch (tokenizer.keyword()) {
			case LET:
				compileLet();
				break;
			case IF:
				compileIf();
				break;
			case WHILE:
				compileWhile();
				break;
			case DO:
				compileDo();
				break;
			case RETURN:
				compileReturn();
				break;
			}
		}

		endCommand(STATEMENTS);
	}

	private void compileLet() throws IOException {
		// 'let' varName ('[' expression ']')? '=' expression ';'
		startCommand(LET_STATEMENT);

		writeKeyword();
		writeIdentifier();

		if (tokenizer.symbol() == '[') {
			writeSymbol();
			compileExpression();
			writeSymbol();
		}

		writeSymbol();
		compileExpression();
		writeSymbol();

		endCommand(LET_STATEMENT);
	}

	private void compileIf() throws IOException {
		// 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}' )?
		startCommand(IF_STATEMENT);

		writeKeyword();
		writeSymbol();
		compileExpression();
		writeSymbol();
		writeSymbol();
		compileStatements();
		writeSymbol();

		if (tokenizer.keyword().equals(ELSE)) {
			writeKeyword();
			writeSymbol();
			compileStatements();
			writeSymbol();
		}

		endCommand(IF_STATEMENT);
	}

	private void compileWhile() throws IOException {
		// 'while' '(' expression ')' '{' statements '}'
		startCommand(WHILE_STATEMENT);

		writeKeyword();
		writeSymbol();
		compileExpression();
		writeSymbol();
		writeSymbol();
		compileStatements();
		writeSymbol();

		endCommand(WHILE_STATEMENT);
	}

	private void compileDo() throws IOException {
		// 'do' subroutineCall ';'
		startCommand(DO_STATEMENT);

		writeKeyword();
		compileSubroutineCall(WRITE_SUBROUTINE_NAME);
		writeSymbol();

		endCommand(DO_STATEMENT);
	}

	private void compileReturn() throws IOException {
		// 'return' expression? ';'
		startCommand(RETURN_STATEMENT);

		writeKeyword();

		if (!tokenizer.tokenType().isSymbol() || tokenizer.symbol() != ';') {
			compileExpression();
		}

		writeSymbol();

		endCommand(RETURN_STATEMENT);
	}

	private void compileExpression() throws IOException {
		// temp (op temp)*
		startCommand(EXPRESSION);

		compileTerm();

		while (isBinaryOperator(tokenizer.symbol())) {
			writeSymbol();
			compileTerm();
		}

		endCommand(EXPRESSION);
	}

	private void compileTerm() throws IOException {
		// integerConstant
		// | stringConstant
		// | keywordConstant -> true|false|null|this
		// | varName
		// | varName '[' expression ']'
		// | subroutineCall
		// | '(' expression ')'
		// | unaryOp Term
		startCommand(TERM);

		switch (tokenizer.tokenType()) {
		case INT_CONST:
			writeInteger();
			break;
		case STRING_CONST:
			writeStringConst();
			break;
		case KEYWORD:
			writeKeyword();
			break;
		case IDENTIFIER:
			writeIdentifier();

			// SubroutineCall
			if (tokenizer.symbol() == '.') {
				compileSubroutineCall(DONT_WRITE_SUBROUTINE_NAME);
			}
			// Array
			else if (tokenizer.symbol() == '[') {
				writeSymbol();
				compileExpression();
				writeSymbol();
			}

			break;
		case SYMBOL:
			// unary op
			if (isUnaryOperator(tokenizer.symbol())) {
				writeSymbol();
				compileTerm();
			}
			// expression
			else if (tokenizer.symbol() == '(') {
				writeSymbol();
				compileExpression();
				writeSymbol();
			}

			break;
		}

		endCommand(TERM);
	}

	private void compileExpressionList() throws IOException {
		// (expression (',' expression)* )?
		startCommand(EXPRESSION_LIST);

		if (tokenizer.symbol() == ')') {
			endCommand(EXPRESSION_LIST);

			return;
		}

		compileExpression();

		while (tokenizer.symbol() == ',') {
			writeSymbol();
			compileExpression();
		}

		endCommand(EXPRESSION_LIST);
	}

	private void compileSubroutineCall(boolean writeSubroutineName) throws IOException {
		// subroutineName '(' expressionList ')'
		// | (className|varName) '.' subroutineName '(' expressionList ')'

		if (writeSubroutineName) {
			writeIdentifier();
		}

		writeSymbol();

		if (tokenizer.tokenType().isIdentifier()) {
			writeIdentifier();
			writeSymbol();
		}

		compileExpressionList();
		writeSymbol();
	}

	private void compileType() throws IOException {
		// 'int' | 'char' | 'boolean' | className

		if (tokenizer.tokenType().isKeyword()) {
			writeKeyword();
		} else {
			writeIdentifier();
		}
	}

	private boolean isMethodKeyword() throws IOException {
		return tokenizer.keyword().equals(FUNCTION) || tokenizer.keyword().equals(CONSTRUCTOR)
				|| tokenizer.keyword().equals(METHOD);
	}

	private boolean isVarTypeClassKeyword() throws IOException {
		return tokenizer.keyword().equals(STATIC) || tokenizer.keyword().equals(FIELD);
	}

	private boolean isUnaryOperator(char op) {
		return op == '~' || op == '-';
	}

	private boolean isBinaryOperator(char op) {
		return op == '=' || op == '*' || op == '>' || op == '<' || op == '&' || op == '|' || op == '/' || op == '-'
				|| op == '+';
	}

	private void writeSymbol() throws IOException {
		String stringSymbol = null;

		switch (tokenizer.symbol()) {
		case ('\"'):
			stringSymbol = "&quot;";
			break;
		case ('>'):
			stringSymbol = "&gt;";
			break;
		case ('&'):
			stringSymbol = "&amp;";
			break;
		case ('<'):
			stringSymbol = "&lt;";
			break;
		default:
			stringSymbol = String.valueOf(tokenizer.symbol());
		}

		write(TokenType.SYMBOL.name().toLowerCase(), stringSymbol);
	}

	private void writeInteger() throws IOException {
		write(INTEGER_CONSTANT, String.valueOf(tokenizer.intVal()));
	}

	private void writeStringConst() throws IOException {
		String value = tokenizer.stringVal();

		write(STRING_CONSTANT, value.substring(1, value.length() - 1));
	}

	private void writeKeyword() throws IOException {
		write(TokenType.KEYWORD.name().toLowerCase(), tokenizer.keyword());
	}

	private void writeIdentifier() throws IOException {
		write(TokenType.IDENTIFIER.name().toLowerCase(), tokenizer.identifier());
	}

	private void write(String tag, String value) throws IOException {
		startCommand(tag, NO_NEW_LINE);
		writer.write(" " + value + " ");
		endCommand(tag);

		// reading the next token
		tokenizer.advance();
	}

	private void startCommand(String value) throws IOException {
		startCommand(value, NEW_LINE);
	}

	private void startCommand(String value, boolean newLine) throws IOException {
		writer.write("<" + value + ">");

		if (newLine) {
			writer.newLine();
		}
	}

	private void endCommand(String value) throws IOException {
		writer.write("</" + value + ">");

		writer.newLine();
	}
}