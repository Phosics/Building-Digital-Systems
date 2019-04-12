import java.io.File;
import java.io.IOException;

public class VMTranslator {
	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);
		CodeWriter codeWriter = null;

		if (file.isFile()) {
			codeWriter = new CodeWriter(file.getPath().replace("vm", "asm"));
			handleFile(file.getPath(), codeWriter);
			codeWriter.close();
			return;
		}

		codeWriter = new CodeWriter(file.getPath() + "\\" + file.getName() + ".asm");
		codeWriter.writeInit();

		for (File subFile : file.listFiles()) {
			if (!subFile.getName().endsWith(".vm")) {
				continue;
			}

			codeWriter.setFileName(subFile.getName());
			handleFile(subFile.getPath(), codeWriter);
		}

		codeWriter.close();
	}

	private static void handleFile(String filename, CodeWriter codeWriter) throws IOException {
		Parser parser = new Parser(filename);

		parser.advance();

		while (parser.hasMoreCommands()) {

			switch (parser.getCommandType()) {
			case C_POP:
			case C_PUSH:
				codeWriter.writePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());
				break;
			case C_LABEL:
				codeWriter.writeLabel(parser.arg1());
				break;
			case C_IF:
				codeWriter.writeIf(parser.arg1());
				break;
			case C_GOTO:
				codeWriter.writeGoto(parser.arg1());
				break;
			case C_FUNCTION:
				codeWriter.writeFunction(parser.arg1(), parser.arg2());
				break;
			case C_RETURN:
				codeWriter.writeReturn();
				break;
			case C_CALL:
				codeWriter.writeCall(parser.arg1(), parser.arg2());
				break;
			default:
				codeWriter.writeAritmetic(parser.arg1());
			}

			parser.advance();
		}
	}

}
