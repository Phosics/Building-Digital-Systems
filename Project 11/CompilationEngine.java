import java.io.File;
import java.io.IOException;

public class CompilationEngine {
	private static final String CONSTANT = "constant";
	private static final String ARGUMENT = "argument";
	private static final String THIS = "this";
	private static final String THAT = "that";
	private static final String TEMP = "temp";
	private static final String POINTER = "pointer";

	private static final String METHOD = "method";
	private static final String FUNCTION = "function";
	private static final String CONSTRUCTOR = "constructor";
	private static final String VAR = "var";
	private static final String STATIC = "static";
	private static final String FIELD = "field";
	private static final String LET = "let";
	private static final String DO = "do";
	private static final String IF = "if";
	private static final String ELSE = "else";
	private static final String WHILE = "while";
	private static final String RETURN = "return";
	private static final String TRUE = "true";
	private static final String FALSE = "false";
	private static final String NULL = "null";
	
	private int whileCounter = 0;
	private int ifCounter = 0;

	private JackTokenizer tokenizer;
	private VMWriter writer;
	private SymbolTable symbolTable;

	private String className;

	public CompilationEngine(File inputFile, File outputFile) throws IOException {
		writer = new VMWriter(outputFile);
		tokenizer = new JackTokenizer(inputFile);
		symbolTable = new SymbolTable();
	}

	public void compile() throws IOException {
		tokenizer.advance();

		compileClass();

		writer.close();
	}

	private void compileClass() throws IOException {
		// 'class' className '{' classVarDec* subroutineDec* '}'
		tokenizer.advance();

		className = tokenizer.keyword();
		tokenizer.advance();
		tokenizer.advance();

		while (tokenizer.tokenType().isKeyword() && isVarTypeClassKeyword()) {
			compileClassVarDec();
		}

		while (tokenizer.tokenType().isKeyword() && isMethodKeyword()) {
			compileSubroutineDec();
		}

		tokenizer.advance();
	}

	private void compileClassVarDec() throws IOException {
		// ('static'|'field') type varName (',' varName)* ';'
		String type, name;
		SymbolTable.Kind kind;

		kind = SymbolTable.Kind.get(tokenizer.keyword());
		tokenizer.advance();

		type = compileType();
		name = tokenizer.identifier();
		tokenizer.advance();

		symbolTable.define(name, type, kind);

		while (tokenizer.symbol() == ',') {
			tokenizer.advance();

			name = tokenizer.identifier();
			tokenizer.advance();

			symbolTable.define(name, type, kind);
		}

		tokenizer.advance();
	}

	private void compileSubroutineDec() throws IOException {
		// ('constructor'|'function'|'method') ('void'|type) subroutineName
		// '('parameterList ')' subroutineBody
		String subroutineType, subroutineName;

		subroutineType = tokenizer.keyword();
		tokenizer.advance();
		tokenizer.advance();
		
		symbolTable.startSubroutine();
		whileCounter = 0;
		ifCounter = 0;
		
		subroutineName = tokenizer.identifier();
		tokenizer.advance();
		tokenizer.advance();
		
		if (subroutineType.equals(METHOD)) {
			symbolTable.define(THIS, className, SymbolTable.Kind.ARG);
		}

		compileParameterList();

		tokenizer.advance();

		compileSubroutineBody(subroutineName, subroutineType);
	}

	private void compileConstructor() throws IOException {
		writer.writePush(CONSTANT, symbolTable.varCount(SymbolTable.Kind.FIELD));
		writer.writeCall("Memory.alloc", 1);
		writer.writePop(POINTER, 0);
	}

	private void compileMethod() throws IOException {
		writer.writePush(ARGUMENT, 0);
		writer.writePop(POINTER, 0);
	}

	private void compileParameterList() throws IOException {
		// ((type varName) (',' type varName)*)?
		if (tokenizer.tokenType().isSymbol() && tokenizer.symbol() == ')') {
			return;
		}

		String parameterType, parameterName;
		boolean isFirstTime = true;

		
		do {
			if(isFirstTime) {
				isFirstTime = !isFirstTime;
			} else {
				tokenizer.advance();
			}
			
			parameterType = compileType();
			parameterName = tokenizer.identifier();
			tokenizer.advance();

			symbolTable.define(parameterName, parameterType, SymbolTable.Kind.ARG);
		} while(tokenizer.symbol() == ',');
	}

	private void compileSubroutineBody(String subroutineName, String subroutineType) throws IOException {
		// '{' varDec* statements '}'
		tokenizer.advance();

		while (tokenizer.tokenType().isKeyword() && tokenizer.keyword().equals(VAR)) {
			compileVarDec();
		}

		writer.writeFunction(className + "." + subroutineName, symbolTable.varCount(SymbolTable.Kind.VAR));

		if (subroutineType.equals(CONSTRUCTOR)) {
			compileConstructor();
		} else if (subroutineType.equals(METHOD)) {
			compileMethod();
		}

		compileStatements();

		tokenizer.advance();
	}

	private void compileVarDec() throws IOException {
		// 'var' type varName (',' varName)* ';'
		String varType, varName;
		boolean isFirstTime = true;
		
		tokenizer.advance();
		varType = compileType();
		
		do {
			if(isFirstTime) {
				isFirstTime = !isFirstTime;
			} else {
				tokenizer.advance();
			}
			
			varName = tokenizer.identifier();
			tokenizer.advance();
			
			symbolTable.define(varName, varType, SymbolTable.Kind.VAR);
		} while(tokenizer.symbol() == ',');
		
		tokenizer.advance();
	}

	private void compileStatements() throws IOException {
		// statement*
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
	}

	private void compileLet() throws IOException {
		// 'let' varName ('[' expression ']')? '=' expression ';'
		boolean isArray = false;
		SymbolTable.Kind kind;

		tokenizer.advance();
		String varName = tokenizer.identifier();
		tokenizer.advance();
		kind = symbolTable.kindOf(varName);

		// If an array
		if (tokenizer.symbol() == '[') {
			isArray = true;
			tokenizer.advance();
			compileExpression();

			if(kind.isNotNone()) {
				writer.writePush(kind.getSegment(), symbolTable.indexOf(varName));
			}
			
			writer.writeBinaryArithmetic('+');
			tokenizer.advance();
		}

		tokenizer.advance();
		compileExpression();

		if (isArray) {
			writer.writePop(TEMP, 0);
			writer.writePop(POINTER, 1);
			writer.writePush(TEMP, 0);
			writer.writePop(THAT, 0);
		} else {
			writer.writePop(kind.getSegment(), symbolTable.indexOf(varName));
		}

		tokenizer.advance();
	}

	private void compileIf() throws IOException {
		// 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}' )?
		String ifTrueLabel = "IF_TRUE" + ifCounter;
		String ifFalseLabel = "IF_FALSE" + ifCounter;
		String ifEndLabel = "IF_END" + ifCounter++;

		tokenizer.advance();
		tokenizer.advance();

		compileExpression();

		writer.writeIf(ifTrueLabel);
		writer.writeGoto(ifFalseLabel);
		writer.writeLabel(ifTrueLabel);

		tokenizer.advance();
		tokenizer.advance();

		compileStatements();

		tokenizer.advance();


		if (tokenizer.keyword().equals(ELSE)) {
			writer.writeGoto(ifEndLabel);
			writer.writeLabel(ifFalseLabel);
			
			tokenizer.advance();
			tokenizer.advance();

			compileStatements();

			tokenizer.advance();
			writer.writeLabel(ifEndLabel);
		} else {
			writer.writeLabel(ifFalseLabel);
		}
	}

	private void compileWhile() throws IOException {
		// 'while' '(' expression ')' '{' statements '}'
		String whileStartLabel = "WHILE_EXP" + whileCounter;
		String whileEndLabel = "WHILE_END" + whileCounter++;

		tokenizer.advance();
		tokenizer.advance();

		writer.writeLabel(whileStartLabel);

		compileExpression();

		writer.writeUnaryArithmetic('~');
		writer.writeIf(whileEndLabel);

		tokenizer.advance();
		tokenizer.advance();

		compileStatements();

		writer.writeGoto(whileStartLabel);
		writer.writeLabel(whileEndLabel);

		tokenizer.advance();
	}

	private void compileDo() throws IOException {
		// 'do' subroutineCall ';'
		tokenizer.advance();

		String subroutineName = tokenizer.identifier();
		tokenizer.advance();

		compileSubroutineCall(subroutineName);
		writer.writePop(TEMP, 0);

		tokenizer.advance();
	}

	private void compileReturn() throws IOException {
		// 'return' expression? ';'
		tokenizer.advance();

		if (!tokenizer.tokenType().isSymbol() || tokenizer.symbol() != ';') {
			compileExpression();
		} else {
			// void
			writer.writePush(CONSTANT, 0);
		}

		tokenizer.advance();
		writer.writeReturn();
	}

	private void compileExpression() throws IOException {
		// temp (op temp)*
		char operator;

		compileTerm();

		operator = tokenizer.symbol();

		while (isBinaryOperator(operator)) {
			tokenizer.advance();
			compileTerm();

			writer.writeBinaryArithmetic(operator);
			operator = tokenizer.symbol();
		}
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

		switch (tokenizer.tokenType()) {
		case INT_CONST:
			writer.writePush(CONSTANT, tokenizer.intVal());

			tokenizer.advance();
			break;
		case STRING_CONST:
			String stringConst = tokenizer.stringVal();
			tokenizer.advance();

			writer.writePush(CONSTANT, stringConst.length() - 2);
			writer.writeCall("String.new", 1);

			// Not including the " "
			for (int i = 1; i < stringConst.length() - 1; i++) {
				writer.writePush(CONSTANT, stringConst.charAt(i));
				writer.writeCall("String.appendChar", 2);
			}

			break;
		case KEYWORD:
			// true|false|null|this

			switch (tokenizer.keyword()) {
			case TRUE:
				writer.writePush(CONSTANT, 0);
				writer.writeUnaryArithmetic('~');
				break;
			case FALSE:
			case NULL:
				writer.writePush(CONSTANT, 0);
				break;
			case THIS:
				writer.writePush(POINTER, 0);
				break;
			}

			tokenizer.advance();
			break;
		case IDENTIFIER:
			String name = tokenizer.identifier();
			SymbolTable.Kind kind = symbolTable.kindOf(name);
			tokenizer.advance();

			// SubroutineCall
			if (tokenizer.symbol() == '.') {
				compileSubroutineCall(name);
			}
			// Array
			else if (tokenizer.symbol() == '[') {
				tokenizer.advance();
				compileExpression();
				tokenizer.advance();

				writer.writePush(kind.getSegment(), symbolTable.indexOf(name));
				writer.writeBinaryArithmetic('+');
				writer.writePop(POINTER, 1);
				writer.writePush(THAT, 0);
			}
			// varName
			else {
				writer.writePush(kind.getSegment(), symbolTable.indexOf(name));
			}

			break;
		case SYMBOL:
			char symbol = tokenizer.symbol();

			// unary op
			if (isUnaryOperator(symbol)) {
				tokenizer.advance();
				compileTerm();
				writer.writeUnaryArithmetic(symbol);
			}
			// expression
			else if (symbol == '(') {
				tokenizer.advance();
				compileExpression();
				tokenizer.advance();
			}

			break;
		}
	}

	private int compileExpressionList() throws IOException {
		// (expression (',' expression)* )?
		if (tokenizer.symbol() == ')') {
			return 0;
		}

		int amount = 1;

		compileExpression();

		while (tokenizer.symbol() == ',') {
			tokenizer.advance();
			compileExpression();

			amount++;
		}

		return amount;
	}

	private void compileSubroutineCall(String name) throws IOException {
		// subroutineName '(' expressionList ')'
		// | (className|varName) '.' subroutineName '(' expressionList ')'
		int amount = 0;
		String className = null;
		SymbolTable.Kind kind = symbolTable.kindOf(name);
		int varIndex = symbolTable.indexOf(name);

		// if it's a variable
		if (kind.isNotNone()) {
			name = symbolTable.typeOf(name);
		}

		if (tokenizer.tokenType().isSymbol() && tokenizer.symbol() == '.') {
			tokenizer.advance();

			className = name;
			name = tokenizer.identifier();
			tokenizer.advance();
		}

		tokenizer.advance();

		if (className == null) {
			// it's a variable
			writer.writePush(POINTER, 0);
		}

		if (kind.isNotNone()) {
			writer.writePush(kind.getSegment(), varIndex);
			amount++;
		}

		amount += compileExpressionList();

		if (className == null) {
			writer.writeCall(this.className + "." + name, amount + 1);
		} else {
			writer.writeCall(className + "." + name, amount);
		}

		tokenizer.advance();
	}

	private String compileType() throws IOException {
		// 'int' | 'char' | 'boolean' | className
		String type = null;

		if (tokenizer.tokenType().isKeyword()) {
			type = tokenizer.keyword();
		} else {
			type = tokenizer.identifier();
		}

		tokenizer.advance();

		return type;
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
}