package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.refset.I_RefsetDefaultsInteger;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsInteger extends RefsetDefaults implements I_RefsetDefaultsInteger {

   public RefsetDefaultsInteger() throws TerminologyException, IOException {
      super();
      defaultForIntegerRefset = 1;
      integerPopupItems = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
   }

   private Integer defaultForIntegerRefset;

   private Integer[] integerPopupItems;

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeInt(defaultForIntegerRefset);
      out.writeObject(integerPopupItems);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultForIntegerRefset = in.readInt();
         integerPopupItems = (Integer[]) in.readObject();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }

   public int getDefaultForIntegerRefset() {
      return defaultForIntegerRefset;
   }

   public void setDefaultForIntegerRefset(Integer defaultForIntegerRefset) {
      Object oldValue = this.defaultForIntegerRefset;
      this.defaultForIntegerRefset = defaultForIntegerRefset;
      pcs.firePropertyChange("defaultForIntegerRefset", oldValue, defaultForIntegerRefset);
   }

   public Integer[] getIntegerPopupItems() {
      return integerPopupItems;
   }

   public void setIntegerPopupItems(Integer[] integerPopupItems) {
      this.integerPopupItems = integerPopupItems;
   }

}
