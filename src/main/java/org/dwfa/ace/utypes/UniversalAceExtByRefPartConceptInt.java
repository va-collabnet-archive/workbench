package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UniversalAceExtByRefPartConceptInt extends
		UniversalAceExtByRefPartConcept {
	   /**
	    * 
	    */
	   private static final long serialVersionUID = 1L;

	   private static final int dataVersion = 1;

	   private int intValue;

	   private void writeObject(ObjectOutputStream out) throws IOException {
	      out.writeInt(dataVersion);
	      out.writeInt(intValue);
	   }

	   @SuppressWarnings("unchecked")
	   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	      int objDataVersion = in.readInt();
	      if (objDataVersion == dataVersion) {
	    	  intValue = in.readInt();
	      } else {
	         throw new IOException("Can't handle dataversion: " + objDataVersion);
	      }
	   }

	public int getIntValue() {
		return intValue;
	}

	public void setIntValue(int intValue) {
		this.intValue = intValue;
	}


}
