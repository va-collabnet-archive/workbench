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

import org.dwfa.ace.refset.I_RefsetDefaultsConInt;
import org.dwfa.tapi.TerminologyException;

public class RefsetDefaultsConInt extends RefsetDefaultsConcept implements I_RefsetDefaultsConInt {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private int defaultForIntegerValue = 1;

    private Integer[] integerPopupItems = new Integer[] { 1, 2, 3 };

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(defaultForIntegerValue);
        out.writeObject(integerPopupItems);
    }

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

    @Override
    public int getDefaultForIntegerRefset() {
        return defaultForIntegerValue;
    }

}
