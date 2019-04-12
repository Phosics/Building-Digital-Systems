import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CodeWriter {
	private static final String POP = "Pop";
	private static final String PUSH = "Push";

	private static final String FALSE = "0";
	private static final String TRUE = "-1";

	private static final String CONSTANT = "constant";
	private static final String LOCAL = "local";
	private static final String ARGUMENT = "argument";
	private static final String THIS = "this";
	private static final String THAT = "that";
	private static final String TEMP = "temp";
	private static final String STATIC = "static";
	private static final String POINTER = "pointer";

	private static final String LINE = "%s\n";

	private String filename;
	private BufferedWriter writer;
	private int equalIndex = 0;

	public CodeWriter(String filename) throws IOException {
		this.filename = new File(filename).getName().replace(".asm", "");
		writer = new BufferedWriter(new FileWriter(filename));
	}

	public void writeAritmetic(String command) throws IOException {
		writeComment(command);

		switch (command) {
		case "add":
			writeBinaryCommand("+");
			break;
		case "sub":
			writeBinaryCommand("-");
			break;
		case "and":
			writeBinaryCommand("&");
			break;
		case "or":
			writeBinaryCommand("|");
			break;
		case "neg":
			writeUnaryCommand("-");
			break;
		case "not":
			writeUnaryCommand("!");
			break;
		case "lt":
			writeLogicalCommand("JLT");
			break;
		case "gt":
			writeLogicalCommand("JGT");
			break;
		case "eq":
			writeLogicalCommand("JEQ");
			break;
		default:
			break;
		}
	}

	public void writePushPop(CommandType commandType, String segment, int index) throws IOException {
		writeComment(commandType, segment, index);

		if (commandType == CommandType.C_PUSH) {
			writePush(segment, index);
		} else {
			writePop(segment, index);
		}
	}
	
	private void writePop(String segment, int index) throws IOException {
		switch (segment) {
		case LOCAL:
			writeToSegment("LCL", index);
			break;
		case ARGUMENT:
			writeToSegment("ARG", index);
			break;
		case THIS:
			writeToSegment("THIS", index);
			break;
		case THAT:
			writeToSegment("THAT", index);
			break;
		case TEMP:
			loadNumber(index);

			writeCommand("@5");
			writeCommand("D=D+A");

			writeCommand("@R13");
			writeCommand("M=D");

			pop();

			writeCommand("@R13");
			writeCommand("A=M");
			writeCommand("M=D");
			break;
		case STATIC:
			pop();

			writeCommand("@" + filename + "." + index);
			writeCommand("M=D");
			break;
		case POINTER:
			if (index == 0) {
				pop();

				writeCommand("@THIS");
				writeCommand("M=D");
			} else {
				pop();

				writeCommand("@THAT");
				writeCommand("M=D");
			}
			break;
		}

	}

	private void writePush(String segment, int index) throws IOException {
		switch (segment) {
		case CONSTANT:
			loadNumber(index);
			push();
			break;
		case LOCAL:
			pushValue("LCL", index);
			break;
		case ARGUMENT:
			pushValue("ARG", index);
			break;
		case THIS:
			pushValue("THIS", index);
			break;
		case THAT:
			pushValue("THAT", index);
			break;
		case TEMP:
			loadNumber(index);

			// D = M[D + 5]
			writeCommand("@5");
			writeCommand("A=A+D");
			writeCommand("D=M");

			push();
			break;
		case STATIC:
			pushLabel(filename + "." + index);
			break;
		case POINTER:
			if (index == 0) {
				pushLabel("THIS");
			} else {
				pushLabel("THAT");
			}
			break;
		}
	}

	public void close() throws IOException {
		writeCommand("(ENDFILE)");
		writeCommand("@ENDFILE");
		writeCommand("0;JMP");

		writer.close();
	}
	
	private void writeToSegment(String segment, int index) throws IOException {
		loadNumber(index);

		// D = index + segment
		writeCommand("@" + segment);
		writeCommand("D=D+M");

		// R13 = D
		writeCommand("@R13");
		writeCommand("M=D");

		pop();

		// M[index + segment] = D
		writeCommand("@R13");
		writeCommand("A=M");
		writeCommand("M=D");
	}

	private void pushLabel(String label) throws IOException {
		loadLabel(label);
		push();
	}

	private void raiseStackPointer() throws IOException {
		writeCommand("@SP");
		writeCommand("M=M+1");
	}

	private void push() throws IOException {
		// *SP = D
		writeCommand("@SP");
		writeCommand("A=M");
		writeCommand("M=D");

		// SP++
		writeCommand("@SP");
		writeCommand("M=M+1");
	}

	private void loadNumber(int index) throws IOException {
		writeCommand(String.format("@%s", index));
		writeCommand("D=A");
	}

	private void loadLabel(String label) throws IOException {
		writeCommand(String.format("@%s", label));
		writeCommand("D=M");
	}

	private void pop() throws IOException {
		writeCommand("@SP");
		writeCommand("AM=M-1");
		writeCommand("D=M");
	}

	private void pushValue(String segmentName, int index) throws IOException {
		loadNumber(index);

		// D = *segmentName
		writeCommand("@" + segmentName);
		writeCommand("A=D+M");
		writeCommand("D=M");

		push();
	}

	private void pushWithoutRaise(String value) throws IOException {
		writeCommand("@SP");
		writeCommand("A=M");
		writeCommand("M=" + value);
	}

	private void jump(String address) throws IOException {
		writeCommand("@" + address);
		writeCommand("0;JMP");
	}

	private void writeLogicalCommand(String jump) throws IOException {
		pop();

		// D = D - *SP
		writeCommand("@SP");
		writeCommand("AM=M-1");
		writeCommand("D=M-D");

		// Jump if condition exists
		writeCommand("@EQ" + equalIndex);
		writeCommand("D;" + jump);

		pushWithoutRaise(FALSE);

		// Jump to end
		jump("EQEND" + equalIndex);

		// Set Stack to TRUE
		writeCommand("(EQ" + equalIndex + ")");
		pushWithoutRaise(TRUE);

		writeCommand("(EQEND" + equalIndex + ")");

		raiseStackPointer();
		equalIndex++;
	}

	private void writeUnaryCommand(String operator) throws IOException {
		pop();

		// M = (operator)D
		writeCommand("M=" + operator + "D");

		raiseStackPointer();
	}

	private void writeBinaryCommand(String operator) throws IOException {
		pop();

		writeCommand("@SP");
		writeCommand("M=M-1");
		writeCommand("A=M");
		writeCommand("M=M" + operator + "D");

		raiseStackPointer();
	}

	private void writeComment(CommandType commandType, String segment, int index) throws IOException {
		String commandTypeValue = commandType == CommandType.C_POP ? POP : PUSH;

		writeCommand(String.format("//%s %s %s", commandTypeValue, segment, index));
	}

	private void writeComment(String comment) throws IOException {
		writeCommand(String.format("//%s", comment));
	}

	private void writeCommand(String command) throws IOException {
		writer.write(String.format(LINE, command));
	}
}
