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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.forms.FormsTableModel;
import org.dwfa.ace.table.forms.FormsTableRenderer;
import org.dwfa.ace.task.classify.SnoAB;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;

/**
 * Classifier Computed Normal Form Table Panel
 * 
 * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
 * 
 * @author kazoo
 * 
 */

public class CNFormsTablePanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L; // Default Serial ID
    
    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    private I_ConfigAceFrame config;
    private I_GetConceptData theCBean;
    private SnoTable theSnoTable;

    private List<I_Position> cEditPathPos;
    private List<I_Position> cClassPathPos;


    public CNFormsTablePanel() {
        super();
        getClassifyPrefs();

        this.theSnoTable = new SnoTable();
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createTitledBorder("Normal Forms Table: "));
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            setConcept(theCBean, config);
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }
    
    /**
     * Called each time the concept selection changes.
     * 
     * @param theCBean
     * @param config
     * @throws IOException
     */
    public void setConcept(I_GetConceptData conceptIn, I_ConfigAceFrame config) throws IOException {
        this.theCBean = conceptIn;
        this.config = config;

        if (conceptIn != null) {
            // update the normal form data
            boolean isOK = theSnoTable.gatherFormData(theCBean);
            
            // UPDATE TABLE
            if (isOK)
                update();
        }
    }

    public void update() {
        this.removeAll();

        String[][] theTableStr = null;
        try {
            theTableStr = theSnoTable.getStringData();
        } catch (TerminologyException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        FormsTableModel theTableModel = new FormsTableModel(theTableStr);
        JTable table = new JTable(theTableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        FormsTableRenderer renderer = new FormsTableRenderer(config);
        table.setDefaultRenderer(Object.class, renderer);
        table.setDefaultRenderer(String.class, renderer);

        // set column widths
        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn tc = table.getColumnModel().getColumn(i);

            // 6 normal forms
            int columnWidth = 32;  // 32 * 6 = 192
            if (i < 6) {
                tc.setMaxWidth(columnWidth);
                tc.setMinWidth(columnWidth);
                tc.setPreferredWidth(columnWidth); 
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
    
    private void getClassifyPrefs() {
        tf = Terms.get();
        try {
            config = tf.getActiveAceFrameConfig();
            // CHECK CLASSIFIER ISA
            if (config.getClassifierIsaType() == null) {
                String errStr = "Classification 'Is a' -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }

            // CHECK & GET EDIT_PATH
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj != null) {
                I_Path cEditIPath = tf.getPath(cEditPathObj.getUids());
                cEditPathPos = new ArrayList<I_Position>();
                cEditPathPos.add(new Position(Integer.MAX_VALUE, cEditIPath));
                addPathOrigins(cEditPathPos, cEditIPath);
            }

            // CHECK & GET CLASSIFER_PATH
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj != null) {
                I_Path cClassIPath = tf.getPath(cClassPathObj.getUids());
                cClassPathPos = new ArrayList<I_Position>();
                cClassPathPos.add(new Position(Integer.MAX_VALUE, cClassIPath));
                addPathOrigins(cClassPathPos, cClassIPath);
            }

            //
        } catch (TerminologyException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SnoAB.posList = cClassPathPos;
    }

    private void addPathOrigins(List<I_Position> origins, I_Path p) {
        origins.addAll(p.getOrigins());
        for (I_Position o : p.getOrigins()) {
            addPathOrigins(origins, o.getPath());
        }
    }

}
