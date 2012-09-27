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
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.I_RefsetDefaultsConcept;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsConcept extends RefsetDefaults implements I_RefsetDefaultsConcept {

    private I_IntList conceptPopupIds = new IntList();

    private I_GetConceptData defaultForConceptRefset;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultForConceptRefset.getUids());
        IntList.writeIntList(out, conceptPopupIds);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultForConceptRefset = readConcept(in);
            conceptPopupIds = IntList.readIntListIgnoreMapErrors(in);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetDefaultsConcept() throws TerminologyException, IOException {
        super();
        defaultForConceptRefset = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());
        conceptPopupIds.add(defaultForConceptRefset.getConceptNid());
    }

    public I_GetConceptData getDefaultForConceptRefset() {
        return defaultForConceptRefset;
    }

    public void setDefaultForConceptRefset(I_GetConceptData defaultForConceptRefset) {
        Object oldValue = this.defaultForConceptRefset;
        this.defaultForConceptRefset = defaultForConceptRefset;
        pcs.firePropertyChange("defaultForConceptRefset", oldValue, defaultForConceptRefset);
    }

    public I_IntList getConceptPopupIds() {
        return conceptPopupIds;
    }

}
