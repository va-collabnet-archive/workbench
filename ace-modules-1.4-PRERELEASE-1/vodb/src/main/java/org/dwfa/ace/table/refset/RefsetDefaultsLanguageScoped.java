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
import org.dwfa.ace.refset.I_RefsetDefaultsLanguageScoped;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsLanguageScoped extends RefsetDefaultsLanguage implements I_RefsetDefaultsLanguageScoped {

    private I_GetConceptData defaultScopeForScopedLanguageRefset;
    private I_IntList scopePopupIds = new IntList();

    private I_GetConceptData defaultTagForScopedLanguageRefset;
    private I_IntList tagPopupIds = new IntList();

    private int defaultPriorityForScopedLanguageRefset;
    private Integer[] priorityPopupItems;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(defaultScopeForScopedLanguageRefset.getUids());
        IntList.writeIntList(out, scopePopupIds);
        out.writeObject(defaultTagForScopedLanguageRefset.getUids());
        IntList.writeIntList(out, tagPopupIds);
        out.writeInt(defaultPriorityForScopedLanguageRefset);
        out.writeObject(priorityPopupItems);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            defaultScopeForScopedLanguageRefset = readConcept(in);
            scopePopupIds = IntList.readIntListIgnoreMapErrors(in);
            defaultTagForScopedLanguageRefset = readConcept(in);
            tagPopupIds = IntList.readIntListIgnoreMapErrors(in);
            defaultPriorityForScopedLanguageRefset = in.readInt();
            priorityPopupItems = (Integer[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public RefsetDefaultsLanguageScoped() throws TerminologyException, IOException {
        super();
        defaultScopeForScopedLanguageRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
        scopePopupIds.add(defaultScopeForScopedLanguageRefset.getConceptId());

        defaultTagForScopedLanguageRefset = ConceptBean.get(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids());
        tagPopupIds.add(defaultTagForScopedLanguageRefset.getConceptId());

        defaultPriorityForScopedLanguageRefset = 1;
        priorityPopupItems = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    }

    public int getDefaultPriorityForScopedLanguageRefset() {
        return defaultPriorityForScopedLanguageRefset;
    }

    public void setDefaultPriorityForScopedLanguageRefset(int defaultPriorityForScopedLanguageRefset) {
        Object oldValue = this.defaultPriorityForScopedLanguageRefset;
        this.defaultPriorityForScopedLanguageRefset = defaultPriorityForScopedLanguageRefset;
        pcs.firePropertyChange("defaultPriorityForScopedLanguageRefset", oldValue,
            defaultPriorityForScopedLanguageRefset);
    }

    public I_GetConceptData getDefaultScopeForScopedLanguageRefset() {
        return defaultScopeForScopedLanguageRefset;
    }

    public void setDefaultScopeForScopedLanguageRefset(I_GetConceptData defaultScopeForScopedLanguageRefset) {
        Object oldValue = this.defaultScopeForScopedLanguageRefset;
        this.defaultScopeForScopedLanguageRefset = defaultScopeForScopedLanguageRefset;
        pcs.firePropertyChange("defaultScopeForScopedLanguageRefset", oldValue, defaultScopeForScopedLanguageRefset);
    }

    public I_GetConceptData getDefaultTagForScopedLanguageRefset() {
        return defaultTagForScopedLanguageRefset;
    }

    public void setDefaultTagForScopedLanguageRefset(I_GetConceptData defaultTagForScopedLanguageRefset) {
        Object oldValue = this.defaultTagForScopedLanguageRefset;
        this.defaultTagForScopedLanguageRefset = defaultTagForScopedLanguageRefset;
        pcs.firePropertyChange("defaultTagForScopedLanguageRefset", oldValue, defaultTagForScopedLanguageRefset);
    }

    public Integer[] getPriorityPopupItems() {
        return priorityPopupItems;
    }

    public void setPriorityPopupItems(Integer[] priorityPopupItems) {
        Object oldValue = this.priorityPopupItems;
        this.priorityPopupItems = priorityPopupItems;
        pcs.firePropertyChange("priorityPopupItems", oldValue, priorityPopupItems);
    }

    public I_IntList getScopePopupIds() {
        return scopePopupIds;
    }

    public I_IntList getTagPopupIds() {
        return tagPopupIds;
    }

}
