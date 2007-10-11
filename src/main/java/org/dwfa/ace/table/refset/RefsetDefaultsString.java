package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.refset.I_RefsetDefaultsString;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsString extends RefsetDefaults implements I_RefsetDefaultsString {

    private String defaultForStringRefset;

    private List<String> stringPopupItems = new ArrayList<String>();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(stringPopupItems);
       out.writeObject(defaultForStringRefset);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           stringPopupItems = (List<String>) in.readObject();
           defaultForStringRefset = (String) in.readObject();
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }

    }

    public RefsetDefaultsString() throws TerminologyException, IOException {
       super();
       defaultForStringRefset = "comment";
       stringPopupItems.add(defaultForStringRefset);
       stringPopupItems.add("combo 2");
    }


    public String getDefaultForStringRefset() {
        return defaultForStringRefset;
    }

    public void setDefaultForStringRefset(String defaultForStringRefset) {
        this.defaultForStringRefset = defaultForStringRefset;
    }

    public List<String> getStringPopupItems() {
        return stringPopupItems;
    }

 }


