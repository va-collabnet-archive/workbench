/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        Object oldValue = this.defaultForStringRefset;
        this.defaultForStringRefset = defaultForStringRefset;
        pcs.firePropertyChange("defaultForStringRefset", oldValue, defaultForStringRefset);
    }

    public List<String> getStringPopupItems() {
        return stringPopupItems;
    }

    public void setStringPopupItems(List<String> stringPopupItems) {
        Object oldValue = this.stringPopupItems;
        this.stringPopupItems = stringPopupItems;
        pcs.firePropertyChange("stringPopupItems", oldValue, stringPopupItems);
    }

}
