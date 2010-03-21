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
import org.dwfa.ace.refset.I_RefsetDefaultsLanguage;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsLanguage extends RefsetDefaults implements I_RefsetDefaultsLanguage {

    private I_GetConceptData defaultAcceptabilityForLanguageRefset;
    private I_IntList acceptabilityPopupIds = new IntList();

    private I_GetConceptData defaultCorrectnessForLanguageRefset;
    private I_IntList correctnessPopupIds = new IntList();

    private I_GetConceptData defaultDegreeOfSynonymyForLanguageRefset;
    private I_IntList degreeOfSynonymyPopupIds = new IntList();

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultAcceptabilityForLanguageRefset.getUids());
        IntList.writeIntList(out, acceptabilityPopupIds);
        out.writeObject(defaultCorrectnessForLanguageRefset.getUids());
        IntList.writeIntList(out, correctnessPopupIds);
        out.writeObject(defaultDegreeOfSynonymyForLanguageRefset.getUids());
        IntList.writeIntList(out, degreeOfSynonymyPopupIds);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultAcceptabilityForLanguageRefset = readConcept(in);
            acceptabilityPopupIds = IntList.readIntListIgnoreMapErrors(in);
            defaultCorrectnessForLanguageRefset = readConcept(in);
            correctnessPopupIds = IntList.readIntListIgnoreMapErrors(in);
            defaultDegreeOfSynonymyForLanguageRefset = readConcept(in);
            degreeOfSynonymyPopupIds = IntList.readIntListIgnoreMapErrors(in);
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetDefaultsLanguage() throws TerminologyException, IOException {
        super();
        defaultAcceptabilityForLanguageRefset = ConceptBean.get(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
        acceptabilityPopupIds.add(defaultAcceptabilityForLanguageRefset.getConceptId());
        acceptabilityPopupIds.add(ArchitectonicAuxiliary.Concept.NOT_SPECIFIED.localize().getNid());
        acceptabilityPopupIds.add(ArchitectonicAuxiliary.Concept.INVALID.localize().getNid());
        acceptabilityPopupIds.add(ArchitectonicAuxiliary.Concept.NOT_RECOMMENDED.localize().getNid());
        acceptabilityPopupIds.add(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.localize().getNid());

        defaultCorrectnessForLanguageRefset = ConceptBean.get(ArchitectonicAuxiliary.Concept.RECOMMENDED.getUids());
        correctnessPopupIds.add(defaultCorrectnessForLanguageRefset.getConceptId());
        correctnessPopupIds.add(ArchitectonicAuxiliary.Concept.INCORRECT.localize().getNid());

        defaultDegreeOfSynonymyForLanguageRefset = ConceptBean.get(ArchitectonicAuxiliary.Concept.SYNONYMOUS.getUids());
        degreeOfSynonymyPopupIds.add(defaultDegreeOfSynonymyForLanguageRefset.getConceptId());
        degreeOfSynonymyPopupIds.add(ArchitectonicAuxiliary.Concept.NEAR_SYNONYMOUS.localize().getNid());
        degreeOfSynonymyPopupIds.add(ArchitectonicAuxiliary.Concept.NON_SYNONYMOUS.localize().getNid());
    }

    public I_IntList getAcceptabilityPopupIds() {
        return acceptabilityPopupIds;
    }

    public I_IntList getCorrectnessPopupIds() {
        return correctnessPopupIds;
    }

    public I_GetConceptData getDefaultAcceptabilityForLanguageRefset() {
        return defaultAcceptabilityForLanguageRefset;
    }

    public void setDefaultAcceptabilityForLanguageRefset(I_GetConceptData defaultAcceptabilityForLanguageRefset) {
        Object oldValue = this.defaultAcceptabilityForLanguageRefset;
        this.defaultAcceptabilityForLanguageRefset = defaultAcceptabilityForLanguageRefset;
        pcs.firePropertyChange("defaultAcceptabilityForLanguageRefset", oldValue, defaultAcceptabilityForLanguageRefset);
    }

    public I_GetConceptData getDefaultCorrectnessForLanguageRefset() {
        return defaultCorrectnessForLanguageRefset;
    }

    public void setDefaultCorrectnessForLanguageRefset(I_GetConceptData defaultCorrectnessForLanguageRefset) {
        Object oldValue = this.defaultCorrectnessForLanguageRefset;
        this.defaultCorrectnessForLanguageRefset = defaultCorrectnessForLanguageRefset;
        pcs.firePropertyChange("defaultCorrectnessForLanguageRefset", oldValue, defaultCorrectnessForLanguageRefset);
    }

    public I_GetConceptData getDefaultDegreeOfSynonymyForLanguageRefset() {
        return defaultDegreeOfSynonymyForLanguageRefset;
    }

    public void setDefaultDegreeOfSynonymyForLanguageRefset(I_GetConceptData defaultDegreeOfSynonymyForLanguageRefset) {
        Object oldValue = this.defaultDegreeOfSynonymyForLanguageRefset;
        this.defaultDegreeOfSynonymyForLanguageRefset = defaultDegreeOfSynonymyForLanguageRefset;
        pcs.firePropertyChange("defaultDegreeOfSynonymyForLanguageRefset", oldValue,
            defaultDegreeOfSynonymyForLanguageRefset);
    }

    public I_IntList getDegreeOfSynonymyPopupIds() {
        return degreeOfSynonymyPopupIds;
    }

}
