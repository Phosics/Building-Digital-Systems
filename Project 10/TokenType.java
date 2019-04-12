
public enum TokenType {
	KEYWORD, SYMBOL, IDENTIFIER, INT_CONST, STRING_CONST;
	
	public boolean isKeyword() {
		return this == KEYWORD;
	}
	
	public boolean isIdentifier() {
		return this == TokenType.IDENTIFIER;
	}
	
	public boolean isSymbol() {
		return this == TokenType.SYMBOL;
	}
}
