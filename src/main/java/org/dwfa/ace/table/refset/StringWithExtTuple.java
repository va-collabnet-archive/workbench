package org.dwfa.ace.table.refset;

import org.dwfa.vodb.types.ThinExtByRefTuple;

public class StringWithExtTuple {
   String str;
   ThinExtByRefTuple tuple;
   public StringWithExtTuple(String str, ThinExtByRefTuple tuple) {
      super();
      this.str = str;
      this.tuple = tuple;
   }
   public String getStr() {
      return str;
   }
   public ThinExtByRefTuple getTuple() {
      return tuple;
   }
   public String toString() {
      return str.toString();
   }
}
