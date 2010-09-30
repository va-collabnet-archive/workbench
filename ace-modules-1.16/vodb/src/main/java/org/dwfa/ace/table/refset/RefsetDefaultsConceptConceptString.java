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

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.refset.I_RefsetDefaultsConceptConceptString;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsConceptConceptString extends RefsetDefaultsConcept implements I_RefsetDefaultsConceptConceptString {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private String defaultForStringValue = "";

    private I_GetConceptData defaultForConcept2Refset;

    private String[] stringPopupItems = new String[] { " " };

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultForStringValue);
        out.writeObject(stringPopupItems);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultForStringValue = (String) in.readObject();
            stringPopupItems = (String[]) in.readObject();

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetDefaultsConceptConceptString() throws TerminologyException, IOException {
        super();
    }

    public String getDefaultForStringValue() {
        return defaultForStringValue;
    }

    public void setDefaultForStringValue(String defaultForStringValue) {
        this.defaultForStringValue = defaultForStringValue;
    }

    public String[] getStringPopupItems() {
        return stringPopupItems;
    }

    public void setIntegerPopupItems(String[] stringPopupItems) {
        this.stringPopupItems = stringPopupItems;
    }

    public void setDefaultForConcept2Refset(I_GetConceptData defaultForConcept2Refset) {
        this.defaultForConcept2Refset = defaultForConcept2Refset;
    }

    @Override
    public I_GetConceptData getDefaultForConcept2Refset() {
        return defaultForConcept2Refset;
    }
}
