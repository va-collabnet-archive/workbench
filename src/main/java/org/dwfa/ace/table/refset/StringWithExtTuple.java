package org.dwfa.ace.table.refset;

import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.table.I_CellTextWithTuple;

public class StringWithExtTuple implements Comparable, I_CellTextWithTuple {
    String cellText;

    I_ThinExtByRefTuple tuple;

    public StringWithExtTuple(String cellText, I_ThinExtByRefTuple tuple) {
       super();
       this.cellText = cellText;
       this.tuple = tuple;
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

    public int compareTo(Object o) {
       StringWithExtTuple another = (StringWithExtTuple) o;
       return cellText.compareTo(another.cellText);
    }
 }