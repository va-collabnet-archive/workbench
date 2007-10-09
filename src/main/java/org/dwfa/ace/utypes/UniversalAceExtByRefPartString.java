package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class UniversalAceExtByRefPartString extends UniversalAceExtByRefPart {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private String stringValue;

    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(stringValue);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
          stringValue = (String) in.readObject();
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }

    public String getStringValue() {
       return stringValue;
    }

    public void setStringValue(String value) {
       this.stringValue = value;
    }

 }
