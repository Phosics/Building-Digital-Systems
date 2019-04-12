
import java.util.HashMap;
import java.util.Map;

public class Code {
	private static Map<String, String> jumpToBinary = new HashMap<>();
	private static Map<String, String> compToBinary = new HashMap<>();
	private static Map<String, String> destToBinary = new HashMap<>();

	static {
		destToBinary.put(Parser.NO_DESTINATION, "000");
		destToBinary.put("M", "001");
		destToBinary.put("D", "010");
		destToBinary.put("MD", "011");
		destToBinary.put("A", "100");
		destToBinary.put("AM", "101");
		destToBinary.put("AD", "110");
		destToBinary.put("AMD", "111");

		jumpToBinary.put(Parser.NO_JUMP, "000");
		jumpToBinary.put("JGT", "001");
		jumpToBinary.put("JEQ", "010");
		jumpToBinary.put("JGE", "011");
		jumpToBinary.put("JLT", "100");
		jumpToBinary.put("JNE", "101");
		jumpToBinary.put("JLE", "110");
		jumpToBinary.put("JMP", "111");

		compToBinary.put("0", "0101010");
		compToBinary.put("1", "0111111");
		compToBinary.put("-1", "0111010");
		compToBinary.put("D", "0001100");
		compToBinary.put("A", "0110000");
		compToBinary.put("!D", "0001101");
		compToBinary.put("!A", "0110001");
		compToBinary.put("-D", "0001111");
		compToBinary.put("-A", "0110011");
		compToBinary.put("D+1", "0011111");
		compToBinary.put("A+1", "0110111");
		compToBinary.put("D-1", "0001110");
		compToBinary.put("A-1", "0110010");
		compToBinary.put("D+A", "0000010");
		compToBinary.put("D-A", "0010011");
		compToBinary.put("A-D", "0000111");
		compToBinary.put("D&A", "0000000");
		compToBinary.put("D|A", "0010101");
		compToBinary.put("M", "1110000");
		compToBinary.put("!M", "1110001");
		compToBinary.put("-M", "1110011");
		compToBinary.put("M+1", "1110111");
		compToBinary.put("M-1", "1110010");
		compToBinary.put("D+M", "1000010");
		compToBinary.put("D-M", "1010011");
		compToBinary.put("M-D", "1000111");
		compToBinary.put("D&M", "1000000");
		compToBinary.put("D|M", "1010101");
	}

	public static String dest(String destCommand) {
		return destToBinary.get(destCommand);
	}

	public static String jump(String jumpCommand) {
		return jumpToBinary.get(jumpCommand);
	}

	public static String comp(String compCommand) {
		return compToBinary.get(compCommand);
	}
}
