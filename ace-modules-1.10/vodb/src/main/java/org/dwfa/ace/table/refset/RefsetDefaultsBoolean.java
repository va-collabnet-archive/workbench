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
        return new Boolean[] { true, false };
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
