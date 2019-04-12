import java.io.IOException;

public class VMTranslator {
	public static void main(String[] args) throws IOException {
		String filename = args[0];
		Parser parser = new Parser(filename);
		CodeWriter codeWriter = new CodeWriter(filename.replace("vm", "asm"));

		parser.advance();

		while (parser.hasMoreCommands()) {
			if (parser.getCommandType().isPushPop()) {
				codeWriter.writePushPop(parser.getCommandType(), parser.arg1(), parser.arg2());

			} else {
				codeWriter.writeAritmetic(parser.arg1());
			}

			parser.advance();
		}

		codeWriter.close();
	}

}
