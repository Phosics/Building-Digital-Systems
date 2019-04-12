
public class TableValue {
	private String type;
	private int index;
	private SymbolTable.Kind kind;

	public TableValue(String type, SymbolTable.Kind kind, int index) {
		this.type = type;
		this.kind = kind;
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

	public SymbolTable.Kind getKind() {
		return kind;
	}
}
