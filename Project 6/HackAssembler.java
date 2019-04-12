
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HackAssembler {

	private static Map<String, Integer> symbolTable = new HashMap<>();

	static {
		for (int i = 0; i < 16; i++) {
			symbolTable.put("R" + i, i);
		}

		symbolTable.put("SCREEN", 16384);
		symbolTable.put("KBD", 24576);

		symbolTable.put("SP", 0);
		symbolTable.put("LCL", 1);
		symbolTable.put("ARG", 2);
		symbolTable.put("THIS", 3);
		symbolTable.put("THAT", 4);

	}

	public static void main(String[] args) throws IOException {
		String filename = args[0];
		Parser parser = new Parser(filename);

		firstPass(parser);
		parser = new Parser(filename);

		secondPass(parser, filename);
	}

	private static void firstPass(Parser parser) throws IOException {
		int lineNumber = 0;

		parser.advance();

		while (parser.hasMoreCommands()) {
			CommandType commandType = parser.commandType();

			if (commandType != CommandType.L_COMMAND) {
				lineNumber++;
				parser.advance();

				continue;
			}

			symbolTable.put(parser.symbol(), lineNumber);
			parser.advance();
		}

	}

	private static void secondPass(Parser parser, String filename) throws IOException {
		File newFile = new File(filename.replace("asm", "hack"));
		newFile.createNewFile();

		BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
		int nextVariableAddress = 16;

		parser.advance();

		while (parser.hasMoreCommands()) {
			if (parser.commandType() == CommandType.L_COMMAND) {
				parser.advance();

				continue;
			}

			String binaryString = "";

			if (parser.commandType() == CommandType.A_COMMAND) {
				String symbol = parser.symbol();
				Integer adderss = tryParseInteger(parser.symbol());

				// If it's not a number
				if (adderss == null) {
					adderss = symbolTable.get(symbol);

					// If it's not in the symbol table
					if (adderss == null) {
						adderss = nextVariableAddress;
						symbolTable.put(symbol, nextVariableAddress++);
					}
				}

				binaryString = addLeadingZeros(Integer.toBinaryString(adderss));

			} else if (parser.commandType() == CommandType.C_COMMAND) {
				binaryString = "111" + Code.comp(parser.comp()) + Code.dest(parser.dest()) + Code.jump(parser.jump());
			}

			bw.write(binaryString + "\n");
			parser.advance();
		}

		bw.close();

	}

	private static String addLeadingZeros(String numberString) {
		if (numberString.length() < 17) {
			for (int i = numberString.length(); i < 16; i++) {
				numberString = "0" + numberString;
			}
		} else {
			numberString = numberString.substring(numberString.length() - 16);
		}

		return numberString;
	}

	private static Integer tryParseInteger(String symbol) {
		try {
			return Integer.parseInt(symbol);

		} catch (NumberFormatException e) {
			return null;
		}
	}
}
