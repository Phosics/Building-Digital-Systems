import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	public enum Kind {
		ARG("argument"), VAR("var", "local"), STATIC("static"), FIELD("field", "this"), NONE(null);

		private String type;
		private String segemnt;

		private Kind(String type) {
			this(type, type);
		}

		private Kind(String type, String segment) {
			this.type = type;
			this.segemnt = segment;
		}

		public String getType() {
			return this.type;
		}

		public String getSegment() {
			return this.segemnt;
		}

		public boolean isNotNone() {
			return this != NONE;
		}
		
		public static Kind get(String type) {
			for (Kind kind : values()) {
				if (kind.type.equals(type)) {
					return kind;
				}
			}

			return null;
		}
	}

	private int staticAmount = -1;
	private int fieldAmount = -1;
	private int argAmount;
	private int varAmount;
	
	public static final String KIND_VAR = "var";
	public static final String KIND_ARG = "argument";
	public static final String KIND_FIELD = "field";
	public static final String KIND_STATIC = "static";
	public static final String KIND_NONE = "none";

	private Map<String, TableValue> classSymbolTable = new HashMap<>();
	private Map<String, TableValue> subroutineSymbolTable;

	public void startSubroutine() {
		varAmount = -1;
		argAmount = -1;
		subroutineSymbolTable = new HashMap<>();
	}

	public void define(String name, String type, Kind kind) {
		switch (kind) {
		case VAR:
			subroutineSymbolTable.put(name, new TableValue(type, kind, ++varAmount));
			break;
		case ARG:
			subroutineSymbolTable.put(name, new TableValue(type, kind, ++argAmount));
			break;
		case FIELD:
			classSymbolTable.put(name, new TableValue(type, kind, ++fieldAmount));
			break;
		case STATIC:
			classSymbolTable.put(name, new TableValue(type, kind, ++staticAmount));
			break;
		default:
			break;
		}
	}

	public int varCount(Kind kind) {
		switch (kind) {
		case VAR:
			return varAmount + 1;
		case ARG:
			return argAmount + 1;
		case FIELD:
			return fieldAmount + 1;
		case STATIC:
			return staticAmount + 1;
		default:
			return -1;
		}
	}

	public Kind kindOf(String name) {
		TableValue value = searchByName(name);

		if (value == null) {
			return Kind.NONE;
		}

		return value.getKind();
	}
	
	public String typeOf(String name) {
		TableValue value = searchByName(name);

		if (value == null) {
			return null;
		}

		return value.getType();
	}

	public int indexOf(String name) {
		TableValue value = searchByName(name);

		if (value == null) {
			return -1;
		}

		return value.getIndex();
	}

	private TableValue searchByName(String name) {
		if (classSymbolTable.get(name) != null) {
			return classSymbolTable.get(name);
		}

		return subroutineSymbolTable.get(name);
	}
}