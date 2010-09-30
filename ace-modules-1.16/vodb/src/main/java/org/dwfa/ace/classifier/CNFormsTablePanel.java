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
package org.dwfa.ace.classifier;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.table.forms.FormsTableModel;
import org.dwfa.ace.table.forms.FormsTableRenderer;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.tapi.TerminologyException;

/**
 * Classifier Computed Normal Form Table Panel
 * 
 * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
 * 
 * @author kazoo
 * 
 */

public class CNFormsTablePanel extends JPanel {
    private static final long serialVersionUID = 1L; // Default Serial ID
    SnoTable theSnoTable;
    I_ConfigAceFrame config;

    public CNFormsTablePanel(SnoTable st, I_ConfigAceFrame caf) {
        this.theSnoTable = st;
        this.config = caf;
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder("Normal Forms Table: "));
    }

    public void update() {
        this.removeAll();

        String[][] theTableStr = null;
        try {
            theTableStr = theSnoTable.getStringData();
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FormsTableModel theTableModel = new FormsTableModel(theTableStr);
        JTable table = new JTable(theTableModel);

        FormsTableRenderer renderer = new FormsTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // set column widths
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);

            // 6 normal forms
            if (i < 6) {
                tc.setMaxWidth(22);
                tc.setMinWidth(22);
                tc.setPreferredWidth(22); // 22 * 6 = 132
                tc.setResizable(false);
            } else {
                tc.setPreferredWidth(400 + 368);
            }
        }

        // set row heights
        int totalRowHeight = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            // count <br>
            int lineCount = 1;
            int lineIdx = theTableStr[i][6].indexOf("<br>");
            while (lineIdx > -1) {
                lineCount++;
                lineIdx = theTableStr[i][6].indexOf("<br>", lineIdx + 4);
            }
            // set row height
            table.setRowHeight(i, 18 * lineCount);
            totalRowHeight += 18 * lineCount;
        }
        table.setPreferredScrollableViewportSize(new Dimension(900, totalRowHeight));

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        this.add(new JScrollPane(table), c);

    }

}
