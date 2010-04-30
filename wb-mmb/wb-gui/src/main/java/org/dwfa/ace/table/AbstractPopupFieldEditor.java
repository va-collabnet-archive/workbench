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
package org.dwfa.ace.table;

import java.awt.Component;
import java.io.IOException;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public abstract class AbstractPopupFieldEditor extends DefaultCellEditor {
    private static final long serialVersionUID = 1L;
    private JComboBox combo;
    I_ConfigAceFrame config;

    public AbstractPopupFieldEditor(I_ConfigAceFrame config) throws TerminologyException, IOException {
        super(new JComboBox());
        combo = new JComboBox();
        combo.setMaximumRowCount(20);
        this.config = config;
        populatePopup();
        editorComponent = combo;

        delegate = new EditorDelegate() {
            private static final long serialVersionUID = 1L;

            public void setValue(Object value) {
                try {
					combo.setSelectedItem(getSelectedItem(value));
				} catch (TerminologyException e) {
					throw new RuntimeException(e);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
            }

            public Object getCellEditorValue() {
                return ((I_GetConceptData) combo.getSelectedItem()).getConceptId();
            }
        };
        combo.addActionListener(delegate);
    }

    public abstract I_GetConceptData getSelectedItem(Object value) throws TerminologyException, IOException;

    private void populatePopup() throws TerminologyException, IOException {
        combo.removeAllItems();
        for (int nid: getPopupValues()) {
        	if (Terms.get().hasConcept(nid)) {
                combo.addItem(Terms.get().getConcept(nid));
        	} else {
        		AceLog.getAppLog().warning("No concept for nid: " + nid);
        	}
        }
    }

    public abstract int[] getPopupValues();

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        try {
			populatePopup();
		} catch (TerminologyException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
