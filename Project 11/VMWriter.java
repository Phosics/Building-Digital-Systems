import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {

	private BufferedWriter writer;

	public VMWriter(File outputFile) throws IOException {
		writer = new BufferedWriter(new FileWriter(outputFile));
	}

	public void writePush(String segment, int index) throws IOException {
		writer.write(String.format("push %s %s", segment, index));
		writer.newLine();
	}

	public void writePop(String segment, int index) throws IOException {
		writer.write(String.format("pop %s %s", segment, index));
		writer.newLine();
	}

	public void writeBinaryArithmetic(char arithmeticCommand) throws IOException {
		switch (arithmeticCommand) {
		case '+':
			write("add");
			break;
		case '*':
			writeCall("Math.multiply", 2);
			break;
		case '/':
			writeCall("Math.divide", 2);
			break;
		case '-':
			write("sub");
			break;
		case '=':
			write("eq");
			break;
		case '>':
			write("gt");
			break;
		case '<':
			write("lt");
			break;
		case '&':
			write("and");
			break;
		case '|':
			write("or");
			break;
		}
	}

	public void writeUnaryArithmetic(char arithmeticCommand) throws IOException {
		switch (arithmeticCommand) {
		case '-':
			write("neg");
			break;
		case '~':
			write("not");
			break;
		}
	}

	public void writeLabel(String label) throws IOException {
		write(String.format("label %s", label));
	}

	public void writeGoto(String label) throws IOException {
		write(String.format("goto %s", label));
	}

	public void writeIf(String label) throws IOException {
		write(String.format("if-goto %s", label));
	}

	public void writeCall(String name, int nArg) throws IOException {
		write(String.format("call %s %d", name, nArg));
	}

	public void writeFunction(String name, int nLocals) throws IOException {
		write(String.format("function %s %d", name, nLocals));
	}

	public void writeReturn() throws IOException {
		write("return");
	}

	public void close() throws IOException {
		writer.close();
	}

	private void write(String text) throws IOException {
		writer.write(text);
		writer.newLine();
	}
}
