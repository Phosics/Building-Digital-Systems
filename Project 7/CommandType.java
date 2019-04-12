
public enum CommandType {
	C_ARITHMETIC, C_PUSH, C_POP, C_LABEL, C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL;

	public boolean isPushPop() {
		return this == C_PUSH || this == C_POP;
	}
}
