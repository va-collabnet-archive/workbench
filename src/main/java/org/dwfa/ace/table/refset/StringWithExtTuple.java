package org.dwfa.ace.table.refset;

import org.dwfa.ace.table.I_CellTextWithTuple;
import org.dwfa.vodb.types.ThinExtByRefTuple;

public class StringWithExtTuple implements Comparable, I_CellTextWithTuple {
    String cellText;

    ThinExtByRefTuple tuple;

    public StringWithExtTuple(String cellText, ThinExtByRefTuple tuple) {
       super();
       this.cellText = cellText;
       this.tuple = tuple;
    }

    public String getCellText() {
       return cellText;
    }

    public ThinExtByRefTuple getTuple() {
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