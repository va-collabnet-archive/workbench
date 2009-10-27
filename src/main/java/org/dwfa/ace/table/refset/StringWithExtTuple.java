package org.dwfa.ace.table.refset;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.ace.table.StringWithTuple;

public class StringWithExtTuple extends StringWithTuple implements Comparable<StringWithExtTuple>, I_CellTextWithTuple {

    private I_ThinExtByRefTuple tuple;
    private int id;

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple, int id) {
        this(cellText, tuple, id, false);
    }
    
    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple, int id, boolean isInConflict) {
       super(cellText, isInConflict);
       this.tuple = tuple;
       this.id = id;
    }

    public I_ThinExtByRefTuple getTuple() {
       return tuple;
    }

    public int compareTo(StringWithExtTuple another) {
       return getCellText().compareTo(another.getCellText());
    }

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
 }