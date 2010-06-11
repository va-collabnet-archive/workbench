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
package org.dwfa.ace.list;

import java.io.IOException;
import java.util.List;

import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntList;

public class TerminologyIntListModel implements I_ModelTerminologyList {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private IntList elements;

    public TerminologyIntListModel(IntList elements) {
        super();
        this.elements = elements;
    }

    public I_GetConceptData getElementAt(int index) {
        try {
            return LocalVersionedTerminology.get().getConcept(elements.get(index));
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    public int getSize() {
        return elements.size();
    }

    public boolean addElement(I_GetConceptData o) {
        boolean rv = elements.add(o.getConceptId());
        return rv;
    }

    public boolean addElements(List<I_GetConceptData> conceptList) {
        boolean rv = false;
        for (I_GetConceptData concept : conceptList) {
            rv |= addElement(concept); 
        }
        return rv;
    }
    
    public void addElement(int index, I_GetConceptData element) {
        elements.add(index, element.getConceptId());
    }

    public I_GetConceptData removeElement(int index) {
        int id = elements.remove(index);
        try {
            return LocalVersionedTerminology.get().getConcept(id);
        } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }

    public void clear() {
        elements.clear();
    }

    public void addListDataListener(ListDataListener l) {
        elements.addListDataListener(l);

    }

    public void removeListDataListener(ListDataListener l) {
        elements.removeListDataListener(l);

    }

    public IntList getElements() {
        return elements;
    }
}
