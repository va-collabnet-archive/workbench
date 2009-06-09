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
		String text = new String(cellText);
		if (isInConflict()) {
			if (text.startsWith("<html>")) {
				text = text.substring(5);
			}
			text = "<html><strong><em style=\"color:red\">" + text + "</em></strong>";
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
