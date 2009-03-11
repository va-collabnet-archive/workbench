package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.refset.I_RefsetDefaultsBoolean;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsBoolean extends RefsetDefaults implements I_RefsetDefaultsBoolean {

   
   public RefsetDefaultsBoolean() throws TerminologyException, IOException {
      super();
   }

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeBoolean(defaultForBooleanRefset);
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         defaultForBooleanRefset = in.readBoolean();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }

   }


   
   public Boolean[] getBooleanPopupItems() {
       return new Boolean[] {true, false};
   }

   private boolean defaultForBooleanRefset = true;

   public boolean getDefaultForBooleanRefset() {
      return defaultForBooleanRefset;
   }

   public void setDefaultForBooleanRefset(boolean defaultForBooleanRefset) {
      Object oldValue = this.defaultForBooleanRefset;
      this.defaultForBooleanRefset = defaultForBooleanRefset;
      pcs.firePropertyChange("defaultForBooleanRefset", oldValue, defaultForBooleanRefset);
   }

}
