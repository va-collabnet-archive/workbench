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

import java.beans.PropertyChangeSupport;
import java.io.IOException;

import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.PropertyChangeSupportWithPropagationId;
import org.dwfa.vodb.types.IntList;

public class TerminologyIntListModel implements I_ModelTerminologyList {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private IntList elements;
    private boolean inPreferences = false;
    private AceFrameConfig config;
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupportWithPropagationId(this);

    public TerminologyIntListModel(IntList elements) {
        super();
        this.elements = elements;
    }
    
    public TerminologyIntListModel(IntList elements, boolean inPreferences, AceFrameConfig config) {
        super();
        this.elements = elements;
        this.inPreferences = inPreferences;
        this.config = config;
    }

    public I_GetConceptData getElementAt(int index) {
        try {
            return Terms.get().getConcept(elements.get(index));
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
        boolean rv = elements.add(o.getConceptNid());
        if(inPreferences){
            config.fireUpdateLangPref();
        }
        return rv;
    }

    public void addElement(int index, I_GetConceptData element) {
        elements.add(index, element.getConceptNid());
        if(inPreferences){
            config.fireUpdateLangPref();
        }
    }

    public I_GetConceptData removeElement(int index) {
        int id = elements.remove(index);
        if(inPreferences){
            config.fireUpdateLangPref();
        }
        try {
            return Terms.get().getConcept(id);
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
