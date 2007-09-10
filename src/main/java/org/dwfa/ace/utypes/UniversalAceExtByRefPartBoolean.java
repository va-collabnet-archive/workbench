package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UniversalAceExtByRefPartBoolean extends UniversalAceExtByRefPart {
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private static final int dataVersion = 1;
   
   private boolean booleanValue;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeBoolean(booleanValue);
   }

   @SuppressWarnings("unchecked")
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == dataVersion) {
         booleanValue = in.readBoolean();
      } else {
         throw new IOException("Can't handle dataversion: " + objDataVersion);
      }
   }

   public boolean getBooleanValue() {
      return booleanValue;
   }

   public void setBooleanValue(boolean value) {
      this.booleanValue = value;
   }

}
