package org.dwfa.ace.table.refset;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.table.I_CellTextWithTuple;

public class StringWithExtTuple implements Comparable<StringWithExtTuple>, I_CellTextWithTuple {
    private String cellText;
    private I_ThinExtByRefTuple tuple;
    private int id;

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple, int id) {
       super();
       this.cellText = cellText;
       this.tuple = tuple;
       this.id = id;
    }

    public String getCellText() {
       return cellText;
    }

    public I_ThinExtByRefTuple getTuple() {
       return tuple;
    }

    public String toString() {
       return cellText;
    }

    public int compareTo(StringWithExtTuple another) {
       return cellText.compareTo(another.cellText);
    }

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
 }