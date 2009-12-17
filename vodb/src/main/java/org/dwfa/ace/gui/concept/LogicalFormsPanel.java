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
package org.dwfa.ace.gui.concept;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.classifier.DLPanel;
import org.dwfa.ace.classifier.DiffPathPanel;
import org.dwfa.ace.classifier.EquivPanel;
import org.dwfa.ace.classifier.CNFormsTablePanel;
import org.dwfa.ace.classifier.ViewPathPanel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoAB;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.Position;

/**
 * Stated & Inferred Panel view.
 * 
 */

public class LogicalFormsPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    // ** WORKBENCH PARTICULARS **
    private I_TermFactory tf;
    private I_ConfigAceFrame config;
    private ConceptBean theCBean;

    private List<I_Position> cEditPathPos;
    private List<I_Position> cClassPathPos;

    // ** CONFIGURATION PARTICULARS **
    private boolean debug = true; // :DEBUG:
    private boolean viewPathTF = false;

    // ** GUI **
    SnoTable theSnoTable = new SnoTable();
    private CNFormsTablePanel cnfTableJPanel;
    private DiffPathPanel diffPathJPanel;
    private EquivPanel equivJPanel;
    private DLPanel logicJPanel;
    private ViewPathPanel viewPathJPanel;

    // ** GUI LAYOUT PARTICULARS **
    final static String TAB_NFT = "Normal Forms";
    final static String TAB_DIFF = "Differences";
    final static String TAB_EQUIV = "Equivalents";
    final static String TAB_DL = "Logic";
    final static String TAB_VIEW = "Version";
    final static String TAB_STATS = "Stats";

    public LogicalFormsPanel() {
        super();
        getClassifyPrefs();

        // COMPONENT BORDER
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));

        // SETUP GRIDBAGLAYOUT
        // setLayout(new GridBagLayout());
        // GridBagConstraints c = new GridBagConstraints();
        // c.anchor = GridBagConstraints.FIRST_LINE_START;
        // c.fill = GridBagConstraints.HORIZONTAL;
        // c.gridy = 0; // first row
        // c.gridx = 0; // first column
        // c.weightx = 0.5; // use space space
        // c.weighty = 0.0; // no extra space
        // c.gridwidth = 1; // use one cell for component

        // SETUP TABBED PANE
        JTabbedPane tabbedPane = new JTabbedPane();

        // ADD COMPUTED NORMAL FORMS (CNF) TABLE VIEW
        cnfTableJPanel = new CNFormsTablePanel(theSnoTable, config);
        cnfTableJPanel.setToolTipText("Computed Normal Forms for the " + "currently selected concept.");
        tabbedPane.addTab(TAB_NFT, cnfTableJPanel);
        // add(cnfTableJPanel, c);

        // :NYI: ADD NORMAL FORMS EXPANDED (LABEL BASED) VIEW

        // ADD DIFF VIEW
        diffPathJPanel = new DiffPathPanel(config);
        diffPathJPanel.setBorder(BorderFactory.createTitledBorder(TAB_DIFF + ": "));
        diffPathJPanel.setToolTipText("All differenced between " + "the current and previous run of the Classifer.");
        tabbedPane.addTab(TAB_DIFF, diffPathJPanel);

        // ADD EQUIVALENCE VIEW
        equivJPanel = new EquivPanel(config);
        equivJPanel.setBorder(BorderFactory.createTitledBorder(TAB_EQUIV + ": "));
        equivJPanel.setToolTipText("All reported equivalent concepts found"
            + " in the most recent run of the Classifer.");
        tabbedPane.addTab(TAB_EQUIV, equivJPanel);

        // ADD DESCRIPTION LOGIC TAB
        logicJPanel = new DLPanel(config);
        logicJPanel.setBorder(BorderFactory.createTitledBorder(TAB_DL + ": "));
        logicJPanel.setToolTipText("Description Logic Declarations which will be " + "submitted to the Classifer.");
        tabbedPane.addTab(TAB_DL, logicJPanel);

        // ADD VIEW PATH VERSIONS TAB
        if (viewPathTF) {
            viewPathJPanel = new ViewPathPanel(config);
            viewPathJPanel.setBorder(BorderFactory.createTitledBorder(TAB_VIEW + ": "));
            tabbedPane.addTab(TAB_VIEW, viewPathJPanel);
        }

        this.add(tabbedPane);
    }

    /**
     * Called each time the concept selection changes.
     * 
     * @param theCBean
     * @param config
     * @throws IOException
     */
    public void setConcept(ConceptBean conceptIn, I_ConfigAceFrame config) throws IOException {
        this.theCBean = conceptIn;
        this.config = config;

        if (conceptIn != null) {
            // update the normal form data
            theSnoTable.gatherFormData(theCBean);

            // TABLE JPANEL
            cnfTableJPanel.update();

            // DIFFS JPANEL
            diffPathJPanel.update();

            // EQUIVALENTS JPANEL
            equivJPanel.update();

            // EQUIVALENTS JPANEL
            logicJPanel.update();

            // EQUIVALENTS JPANEL
            if (viewPathTF)
                viewPathJPanel.update();
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            setConcept(theCBean, config);
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }

    private void getClassifyPrefs() {
        tf = LocalVersionedTerminology.get();
        try {
            config = tf.getActiveAceFrameConfig();
            // CHECK CLASSIFIER ISA
            if (config.getClassifierIsaType() == null) {
                String errStr = "Classifier Is-a -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }

            // CHECK & GET EDIT_PATH
            I_GetConceptData cEditPathObj = config.getClassifierInputPath();
            if (cEditPathObj == null) {
                String errStr = "Classifier Input (Edit) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }
            I_Path cEditIPath = tf.getPath(cEditPathObj.getUids());
            cEditPathPos = new ArrayList<I_Position>();
            cEditPathPos.add(new Position(Integer.MAX_VALUE, cEditIPath));
            addPathOrigins(cEditPathPos, cEditIPath);

            // CHECK & GET CLASSIFER_PATH
            I_GetConceptData cClassPathObj = config.getClassifierOutputPath();
            if (cClassPathObj == null) {
                String errStr = "Classifier Output (Inferred) Path -- not set in Classifier preferences tab!";
                AceLog.getAppLog().alertAndLog(Level.SEVERE, errStr, new Exception(errStr));
                return;
            }
            I_Path cClassIPath = tf.getPath(cClassPathObj.getUids());
            cClassPathPos = new ArrayList<I_Position>();
            cClassPathPos.add(new Position(Integer.MAX_VALUE, cClassIPath));
            addPathOrigins(cClassPathPos, cClassIPath);

            //
        } catch (TerminologyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
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
