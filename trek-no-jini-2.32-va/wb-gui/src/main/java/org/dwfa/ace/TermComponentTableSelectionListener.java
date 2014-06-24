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
package org.dwfa.ace;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.list.TerminologyTableModel.MODEL_FIELD;

public class TermComponentTableSelectionListener implements ListSelectionListener {

    I_ContainTermComponent linkedComponent;
    JTable table;

    private boolean warnForUncommitted = false;

    public TermComponentTableSelectionListener(I_ContainTermComponent linkedComponent, JTable table) {
        super();
        this.linkedComponent = linkedComponent;
        this.table = table;
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if(e.getValueIsAdjusting()){
            return;
        }
        I_GetConceptData currentBean = (I_GetConceptData) linkedComponent.getTermComponent();
	setLinkedComponent(e);
    }

    private void setLinkedComponent(ListSelectionEvent e) {
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        int index = lsm.getMinSelectionIndex();
        if (index == -1) {
            linkedComponent.setTermComponent(null);
        } else {
            int tableIndex = table.convertRowIndexToModel(index);
            linkedComponent.setTermComponent((I_GetConceptData) table.getModel().getValueAt(tableIndex, MODEL_FIELD.CONCEPT.ordinal()));
        }
    }

    public boolean getWarnForUncommitted() {
        return warnForUncommitted;
    }

    public void setWarnForUncommitted(boolean warnForUncommitted) {
        this.warnForUncommitted = warnForUncommitted;
    }

}
