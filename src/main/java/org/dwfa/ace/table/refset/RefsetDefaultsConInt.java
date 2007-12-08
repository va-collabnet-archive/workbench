package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsConInt extends RefsetDefaultsConcept implements I_RefsetDefaultsConInt {


	/**
	    * 
	    */
	   private static final long serialVersionUID = 1L;

	   private static final int dataVersion = 1;
	   
	   private int defaultForIntegerValue = 1;
	   
	   private Integer[] integerPopupItems = new Integer[] {1, 2, 3};

	   private void writeObject(ObjectOutputStream out) throws IOException {
	      out.writeInt(dataVersion);
	      out.writeInt(defaultForIntegerValue);
	      out.writeObject(integerPopupItems);
	   }

	   @SuppressWarnings("unchecked")
	   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	      int objDataVersion = in.readInt();
	      if (objDataVersion == dataVersion) {
	    	  defaultForIntegerValue = in.readInt();
	    	  integerPopupItems = (Integer[]) in.readObject();

	      } else {
	         throw new IOException("Can't handle dataversion: " + objDataVersion);
	      }

	   }

	   public RefsetDefaultsConInt() throws TerminologyException, IOException {
			super();
	   }


	public int getDefaultForIntegerValue() {
		return defaultForIntegerValue;
	}

	public void setDefaultForIntegerValue(int defaultForIntegerValue) {
		this.defaultForIntegerValue = defaultForIntegerValue;
	}

	public Integer[] getIntegerPopupItems() {
		return integerPopupItems;
	}

	public void setIntegerPopupItems(Integer[] integerPopupItems) {
		this.integerPopupItems = integerPopupItems;
	}

}
