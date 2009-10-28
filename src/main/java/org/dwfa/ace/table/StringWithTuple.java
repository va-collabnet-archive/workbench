package org.dwfa.ace.table;



public abstract class StringWithTuple implements I_CellTextWithTuple {

	private String cellText;
	private boolean isInConflict;
	
	public StringWithTuple(String cellText, boolean isInConflict) {
		super();
		this.cellText = cellText;
		this.isInConflict = isInConflict;
	}

	public String toString() {
		String text = cellText;
		if (isInConflict()) {
			if (text != null && text.startsWith("<html>")) {
				text = text.substring(5);
			}
			text = "<html><em style=\"color:red\">" + text + "</em>";
		}
		return text;
	}

	public int compareTo(StringWithTuple another) {
		return cellText.compareTo(another.cellText);
	}
	
	public String getCellText() {
		return cellText;
	}

	public boolean isInConflict() {
		return isInConflict;
	}

}
