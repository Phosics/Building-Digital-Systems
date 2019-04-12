import java.io.File;
import java.io.IOException;

public class JackAnalyzer {

	public static void main(String[] args) throws IOException {
		File file = new File(args[0]);

		if (file.isFile()) {
			handleFile(file);

			return;
		}
		for (File child : file.listFiles()) {
			if (!child.getName().endsWith(".jack")) {
				continue;
			}

			handleFile(child);
		}
	}

	public static void handleFile(File inputfile) throws IOException {
		new CompilationEngine(inputfile, new File(inputfile.getPath().replace(".jack", ".xml"))).compile();
	}
}