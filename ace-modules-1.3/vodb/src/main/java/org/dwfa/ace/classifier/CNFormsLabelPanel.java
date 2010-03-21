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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.classify.SnoGrp;
import org.dwfa.ace.task.classify.SnoGrpList;
import org.dwfa.ace.task.classify.SnoRel;
import org.dwfa.ace.task.classify.SnoTable;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinConTuple;
import org.dwfa.vodb.types.ThinRelTuple;

/**
 * Classifier Normal Form (Label Format) Panel
 * 
 * 
 * @author kazoo
 * 
 */

/*
 * :NYI:
 * 1. clarify minmax !!!
 * 2. handle path comparison
 * 3.
 */

public class CNFormsLabelPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    /**
     * <b>DeltaColors</b><br>
     * Uses <code>AWT Color</code> object which use some of the following <a
     * href=http://www.w3schools.com/html/html_colornames.asp>color names &
     * values</a>. These colors are used to highlight differences.
     */
    public static class DeltaColors {

        private List<Color> colorList = new ArrayList<Color>(); // AWT: Color

        int currentColor = 0;

        public DeltaColors() {
            super();
            // Link for colors
            // http://www.w3schools.com/html/html_colornames.asp
            colorList.add(new Color(0x5F9EA0));
            colorList.add(new Color(0x7FFF00));
            colorList.add(new Color(0xD2691E));
            colorList.add(new Color(0x6495ED));
            colorList.add(new Color(0xDC143C));
            colorList.add(new Color(0xB8860B));
            colorList.add(new Color(0xFF8C00));
            colorList.add(new Color(0x8FBC8F));
            colorList.add(new Color(0x483D8B));
            colorList.add(new Color(0x1E90FF));
            colorList.add(new Color(0xFFD700));
            colorList.add(new Color(0xF0E68C));
            colorList.add(new Color(0x90EE90));
            colorList.add(new Color(0x8470FF)); // 14 colors
        }

        public Color getNextColor() {
            if (currentColor == colorList.size()) {
                reset();
            }
            return colorList.get(currentColor++);
        }

        public void reset() {
            currentColor = 0;
        }
    }

    // ** WORKBENCH PARTICULARS **
    private ConceptBean theCBean;
    private I_ConfigAceFrame config;

    // ** CLASSIFIER PARTICULARS **
    List<I_Position> cEditPathPos; // Edit (Stated) Path I_Positions
    List<I_Position> cClassPathPos; // Classifier (Inferred) Path I_Positions
    private SnoTable cSnoTable;

    // ** CONFIGURATION PARTICULARS **
    private boolean debug = false; // :DEBUG:
    boolean showGroupLabels = true; // toggles grouped vs. single label display

    // ** GUI PARTICULARS **
    private JPanel commonJPanel;
    private JPanel commonPartJPanel;
    private JPanel deltaJPanel;
    private JPanel deltaPartJPanel;
    private JPanel formsJPanel; // sub panels added using tmpJPanel

    private JCheckBox showStatusCB = new JCheckBox("show status");
    private JCheckBox showDetailCB = new JCheckBox("show detail");
    private JCheckBox showDistFormCB = new JCheckBox("Distribution");
    private JCheckBox showAuthFormCB = new JCheckBox("Authoring");
    private JCheckBox showLongFormCB = new JCheckBox("Long Canonical");
    private JCheckBox showShortFormCB = new JCheckBox("Short Canonical");

    private DeltaColors colors = new DeltaColors();

    // JLabel with ActionListener
    private List<I_ImplementActiveLabel> commonLabels;

    // AWT: Dimension(int Width, int Height) in pixels(???)
    private Dimension maxPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 4000);
    private Dimension minPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 100);

    private void setMinMaxSize(JPanel panel) {
        panel.setMinimumSize(minPartPanelSize);
        panel.setMaximumSize(maxPartPanelSize);
    }

    private JPanel newMinMaxJPanel() {
        JPanel p = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();

                // DO NOT ALLOW "PREFERRED" WIDTH TO GO BELOW "MIN"
                d.width = Math.max(d.width, minPartPanelSize.width);

                // DO NOT ALLOW "PREFERRED" HEIGHT TO GO BELOW "MIN"
                d.height = Math.max(d.height, minPartPanelSize.height);
                // DO NOT ALLOW "PREFERRED" HEIGHT TO GO ABOVE "MAX"
                d.height = Math.min(d.height, maxPartPanelSize.height);
                return d;
            }
        };
        setMinMaxSize(p);
        return p;
    }

    public CNFormsLabelPanel(ConceptBean conceptIn, List<I_Position> cEditPathPos, List<I_Position> cClassPathPos,
            SnoTable cSnoTable) {
        super();
        this.theCBean = conceptIn;
        this.cEditPathPos = cEditPathPos;
        this.cClassPathPos = cClassPathPos;
        this.cSnoTable = cSnoTable;

        setLayout(new GridBagLayout()); // CNFormsLabelPanel LayoutManager
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST; // Place
        // CNFormsLabelPanel

        // TOP ROW
        c.gridy = 0; // first row
        c.gridx = 0; // reset at west side of row
        c.weightx = 0.0; // no extra space
        c.weighty = 0.0; // no extra space
        c.gridwidth = 1;
        c.fill = GridBagConstraints.NONE;

        // ADD CHECK BOXES
        c.gridy++;// next row
        c.gridx = 0;
        c.gridwidth = 5;
        JLabel label = new JLabel("Normal Forms Expanded View:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        add(label, c);
        c.gridx++;
        add(showDistFormCB, c);
        c.gridx++;
        add(showAuthFormCB, c);
        c.gridx++;
        add(showLongFormCB, c);
        c.gridx++;
        add(showShortFormCB, c);

        // FORM SELECTION CHECKBOX ROW
        c.gridy++; // next row
        c.gridx = 0; // first cell in row
        c.gridwidth = 1;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;

        label = new JLabel("Information:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        add(label, c);

        c.gridx++;
        add(showDetailCB, c);
        c.gridx++;
        add(showStatusCB, c);

        // SETUP CHECKBOX VALUES & LISTENER
        showStatusCB.setSelected(false);
        showStatusCB.addActionListener(this);
        showDetailCB.setSelected(false);
        showDetailCB.addActionListener(this);
        showDistFormCB.setSelected(false);
        showDistFormCB.addActionListener(this);
        showAuthFormCB.setSelected(false);
        showAuthFormCB.addActionListener(this);
        showLongFormCB.setSelected(false);
        showLongFormCB.addActionListener(this);
        showShortFormCB.setSelected(false);
        showShortFormCB.addActionListener(this);

        // COMMON & DIFFERENT PANELS ROW
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        commonJPanel = newMinMaxJPanel();
        commonJPanel.setLayout(new GridLayout(0, 1));
        commonJPanel.setName("Common Panel");
        commonJPanel.setBorder(BorderFactory.createTitledBorder("Common: "));
        add(commonJPanel, c);

        c.gridx = c.gridx + 1;
        deltaJPanel = newMinMaxJPanel();
        deltaJPanel.setLayout(new GridLayout(0, 1));
        deltaJPanel.setName("Differences Panel");
        deltaJPanel.setBorder(BorderFactory.createTitledBorder("Different: "));
        add(deltaJPanel, c);

        // FORMS PANEL ROW
        c.gridy++;// next row
        c.gridx = 0; // reset at west side of row
        c.gridwidth = 2; // number of cells in row
        c.fill = GridBagConstraints.BOTH;
        formsJPanel = new JPanel(new GridBagLayout());
        formsJPanel.setName("Forms Panel");
        formsJPanel.setBorder(BorderFactory.createTitledBorder("Forms: "));
        JScrollPane formJScrollPane = new JScrollPane(formsJPanel);
        formJScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(formJScrollPane, c);

    }

    public void actionPerformed(ActionEvent e) {
        try {
            setConcept(theCBean, config);
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }

    public void setConcept(ConceptBean conceptIn, I_ConfigAceFrame config) throws IOException {
        this.theCBean = conceptIn;
        this.config = config;

        commonJPanel.removeAll();
        deltaJPanel.removeAll();
        formsJPanel.removeAll(); // FORMS HAS SUBPANELS: STATED & COMPUTED

        if (conceptIn == null)
            return;

        // COMMON & DIFFERENT SECTION
        // COMMON PANEL
        commonLabels = getCommonLabels(showDetailCB.isSelected(), showStatusCB.isSelected(), config); // ####
        commonPartJPanel = new JPanel();
        setMinMaxSize(commonPartJPanel);
        commonPartJPanel.setLayout(new BoxLayout(commonPartJPanel, BoxLayout.Y_AXIS));
        for (I_ImplementActiveLabel l : commonLabels) {
            commonPartJPanel.add(l.getLabel());
        }

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridheight = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        commonJPanel.add(commonPartJPanel, c);

        // DELTA (DIFFERENCES) PANEL
        Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
        Map<I_DescriptionTuple, Color> desColorMap = new HashMap<I_DescriptionTuple, Color>();
        Map<I_RelTuple, Color> relColorMap = new HashMap<I_RelTuple, Color>();
        colors.reset();
        Collection<I_ImplementActiveLabel> deltaLabels = getDeltaLabels(showDetailCB.isSelected(),
            showStatusCB.isSelected(), config, colors, conAttrColorMap, desColorMap, relColorMap); // ####
        deltaPartJPanel = new JPanel();
        deltaPartJPanel.setLayout(new BoxLayout(deltaPartJPanel, BoxLayout.Y_AXIS));
        for (I_ImplementActiveLabel l : deltaLabels) {
            deltaPartJPanel.add(l.getLabel());
        }
        deltaJPanel.add(deltaPartJPanel);

        // FORM STATED PANEL
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0; // horizontal free space distribution weight
        c.weighty = 0; // vertical free space distribution weight
        c.gridx = 0;
        c.gridy = 0;

        JPanel tmpJPanel;
        tmpJPanel = newFormStatedJPanel("Stated Form:", config, conAttrColorMap, desColorMap, relColorMap); // ####
        setMinMaxSize(tmpJPanel);
        formsJPanel.add(tmpJPanel, c);

        // FORM DISTRIBUTION NORMAL PANEL
        if (showDistFormCB.isSelected()) {
            c.gridx++;
            if (c.gridx == 2) {
                c.gridx = 0;
                c.gridy++;
            }
            tmpJPanel = newFormDistJPanel("Distribution Normal Form:", config, conAttrColorMap, desColorMap,
                relColorMap); // ####
            setMinMaxSize(tmpJPanel);
            formsJPanel.add(tmpJPanel, c);
        }

        // AUTHORING NORMAL FORM PANEL
        if (showAuthFormCB.isSelected()) {
            c.gridx++;
            if (c.gridx == 2) {
                c.gridx = 0;
                c.gridy++;
            }
            tmpJPanel = newFormAuthJPanel("Authoring Normal Form:", config, conAttrColorMap, desColorMap, relColorMap); // ####
            setMinMaxSize(tmpJPanel);
            formsJPanel.add(tmpJPanel, c);
        }

        // LONG CANONICAL FORM PANEL
        if (showLongFormCB.isSelected()) {
            c.gridx++;
            if (c.gridx == 2) {
                c.gridx = 0;
                c.gridy++;
            }
            tmpJPanel = newFormLongJPanel("Long Canonical Form:", config, conAttrColorMap, desColorMap, relColorMap); // ####
            setMinMaxSize(tmpJPanel);
            formsJPanel.add(tmpJPanel, c);
        }

        // FORM SHORT CANONICAL PANEL
        if (showShortFormCB.isSelected()) {
            c.gridx++;
            if (c.gridx == 2) {
                c.gridx = 0;
                c.gridy++;
            }
            tmpJPanel = newFormShortJPanel("Short Canonical Form:", config, conAttrColorMap, desColorMap, relColorMap); // ####
            setMinMaxSize(tmpJPanel);
            formsJPanel.add(tmpJPanel, c);
        }
    }

    public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm, boolean showStatus,
            I_ConfigAceFrame config) throws IOException {
        List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

        // GET CONCEPT ATTRIBUTES
        Set<I_ConceptAttributeTuple> commonConTuples = this.theCBean.getCommonConceptAttributeTuples(config); // ####
        // COMMON
        // CON
        // CREATE CONCEPT ATTRIBUTE LABELS
        if (commonConTuples != null) {
            for (I_ConceptAttributeTuple t : commonConTuples) {
                I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
                setBorder(conAttrLabel.getLabel(), null);
                labelList.add(conAttrLabel);
            }
        }

        // GET SOURCE RELATIONSHIPS
        Set<I_RelTuple> commonRelTuples = this.theCBean.getCommonRelTuples(config); // ####
        // COMMON
        // REL
        // CREATE RELATIONSHIP LABELS
        if (commonRelTuples != null) {
            for (I_RelTuple t : commonRelTuples) {
                I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
                setBorder(relLabel.getLabel(), null);
                labelList.add(relLabel);
            }
        }

        return labelList;
    } // getCommonLabels

    public Collection<I_ImplementActiveLabel> getDeltaLabels(boolean showLongForm, boolean showStatus,
            I_ConfigAceFrame config, DeltaColors colors, Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> descColorMap, Map<I_RelTuple, Color> relColorMap) throws IOException {

        Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
        Set<I_RelTuple> allRelTuples = new HashSet<I_RelTuple>();

        // FIND ALL...
        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> posSet = new HashSet<I_Position>();
            posSet.add(p);

            // concept attributes
            List<I_ConceptAttributeTuple> conTuplesForPosition = this.theCBean.getConceptAttributeTuples(
                config.getAllowedStatus(), posSet, false); // ####
            // ALL
            // COMMON
            // CON
            allConAttrTuples.addAll(conTuplesForPosition);

            // relationships
            List<I_RelTuple> relTuplesForPosition = this.theCBean.getSourceRelTuples(config.getAllowedStatus(), null,
                posSet, false); // ####
            // ALL
            // REL
            allRelTuples.addAll(relTuplesForPosition);
        }

        // FIND & REMOVE COMMON...
        Set<I_ConceptAttributeTuple> commonConAttrTuples = this.theCBean.getCommonConceptAttributeTuples(config); // ####
        // COMMON
        // CON
        allConAttrTuples.removeAll(commonConAttrTuples);
        Set<I_RelTuple> commonRelTuples = this.theCBean.getCommonRelTuples(config); // ####
        // COMMON
        // REL
        allRelTuples.removeAll(commonRelTuples);

        Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();
        // CREATE CONCEPT ATTRIBUTE LABELS
        for (I_ConceptAttributeTuple t : allConAttrTuples) {
            I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
            Color deltaColor = colors.getNextColor();
            conAttrColorMap.put(t, deltaColor);
            setBorder(conAttrLabel.getLabel(), deltaColor);
            labelList.add(conAttrLabel);
        }
        // CREATE RELATIONSHIP LABELS
        for (I_RelTuple t : allRelTuples) {
            I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
            Color deltaColor = colors.getNextColor();
            relColorMap.put(t, deltaColor);
            setBorder(relLabel.getLabel(), deltaColor);
            labelList.add(relLabel);
        }

        return labelList;
    } // getDeltaLabels

    /**
     * <b>Authoring Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
     */
    public JPanel newFormAuthJPanel(String label, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
            showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        SnoGrpList isaSGList = cSnoTable.getIsaProx();
        List<I_RelTuple> isaList = new ArrayList<I_RelTuple>();
        for (SnoGrp sg : isaSGList)
            for (SnoRel sr : sg)
                isaList.add(new ThinRelTuple(sr.relVers, sr.relPart));
        for (I_RelTuple t : isaList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, showDetailCB.isSelected(),
                showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tLabel);
            Color deltaColor = relColorMap.get(t);
            setBorder(tLabel.getLabel(), deltaColor);
            formJPanel.add(tLabel.getLabel(), c);
            c.gridy++;
        }

        // FIND NON-REDUNDANT ROLES, DIFFERENTIATED FROM PROXIMATE ISA
        SnoGrpList sgl = cSnoTable.getRoleDiffFromProx();
        // SHOW ROLE SET
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                        showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue; // :TODO: investigate why empty sets exist
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                            showStatusCB.isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Distribution Normal Form</b><li>Most Proximate Supertypes (IS-A)</li>
     */
    public JPanel newFormDistJPanel(String label, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
            showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        SnoGrpList isaSGList = cSnoTable.getIsaProx();
        List<I_RelTuple> isaList = new ArrayList<I_RelTuple>();
        for (SnoGrp sg : isaSGList)
            for (SnoRel sr : sg)
                isaList.add(new ThinRelTuple(sr.relVers, sr.relPart));
        for (I_RelTuple rTuple : isaList) {
            tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
        SnoGrpList sgl = cSnoTable.getRoleDiffFromRootList();
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                        showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                            showStatusCB.isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Long Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)</li>
     * 
     */
    public JPanel newFormLongJPanel(String label, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabel(cTuple, showDetailCB.isSelected(),
            showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        SnoGrpList isaSGList = cSnoTable.getIsaProxPrim();
        List<I_RelTuple> isaList = new ArrayList<I_RelTuple>();
        for (SnoGrp sg : isaSGList)
            for (SnoRel sr : sg)
                isaList.add(new ThinRelTuple(sr.relVers, sr.relPart));
        for (I_RelTuple rTuple : isaList) {
            tmpTLabel = TermLabelMaker.newLabel(rTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES, NON-REDUNDANT, DIFFERENTIATED FROM ROOT
        SnoGrpList sgl = cSnoTable.getRoleDiffFromRootList();
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                        showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // set to true to show one label per
                    // group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 relationship per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                            showStatusCB.isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    /**
     * <b>Short Canonical Form</b><li>Most Proximate PRIMITIVE Supertypes (IS-A)
     * </li>
     */
    public JPanel newFormShortJPanel(String label, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
            showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL PRIMITIVE ISAs -- as relationships
        SnoGrpList isaSGList = cSnoTable.getIsaProxPrim();
        List<I_RelTuple> isaList = new ArrayList<I_RelTuple>();
        for (SnoGrp sg : isaSGList)
            for (SnoRel sr : sg)
                isaList.add(new ThinRelTuple(sr.relVers, sr.relPart));
        for (I_RelTuple t : isaList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, showDetailCB.isSelected(),
                showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tLabel);
            Color deltaColor = relColorMap.get(t);
            setBorder(tLabel.getLabel(), deltaColor);
            formJPanel.add(tLabel.getLabel(), c);
            c.gridy++;
        }

        // SHOW ROLES
        SnoGrpList sgl = cSnoTable.getRoleDiffFromProxPrim();
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                        showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                            showStatusCB.isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));

        return formJPanel;
    }

    public JPanel newFormStatedJPanel(String label, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException {
        JPanel formJPanel = newMinMaxJPanel();
        formJPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;

        List<LabelForTuple> tLabelList = new ArrayList<LabelForTuple>();
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // SHOW SELF CONCEPT
        I_ConceptAttributeTuple cTuple = findSelf(theCBean, cEditPathPos);
        I_ImplementActiveLabel tmpTLabel = TermLabelMaker.newLabelForm(cTuple, showDetailCB.isSelected(),
            showStatusCB.isSelected());
        tLabelList.add((LabelForTuple) tmpTLabel);
        Color tmpDeltaColor = conAttrColorMap.get(cTuple);
        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
        formJPanel.add(tmpTLabel.getLabel(), c);
        c.gridy++;

        // SHOW PROXIMAL ISAs -- as relationships
        SnoGrpList isaSGList = cSnoTable.getStatedIsaProx();
        List<I_RelTuple> isaList = new ArrayList<I_RelTuple>();
        for (SnoGrp sg : isaSGList)
            for (SnoRel sr : sg)
                isaList.add(new ThinRelTuple(sr.relVers, sr.relPart));
        for (I_RelTuple rTuple : isaList) {
            tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
            tLabelList.add((LabelForTuple) tmpTLabel);
            tmpDeltaColor = relColorMap.get(rTuple);
            setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
            formJPanel.add(tmpTLabel.getLabel(), c);
            c.gridy++;
        }

        // GET IMMEDIATE PROXIMAL ROLES & SEPARATE INTO GROUPS
        SnoGrpList sgl = cSnoTable.getStatedRole();
        // !!! :FIXME: UNGROUPED, NOT LONGER JUST THE "0" GROUP
        if (sgl.size() > 0) {
            int i = 0;
            SnoGrp sg = sgl.get(0);
            // show each of the non-Rels
            if (sg.size() > 0 && sg.get(0).group == 0) {
                for (SnoRel sr : sg) {
                    I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                    tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                        showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(rTuple);
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                }
                i++; // skip past 0 index of the "un-grouped"
            }

            // show each of the groups
            for (; i < sgl.size(); i++) {
                sg = sgl.get(i);
                if (sg.size() == 0)
                    continue;
                if (showGroupLabels) { // true shows one label per group
                    List<I_RelTuple> grpTuple = new ArrayList<I_RelTuple>();
                    for (SnoRel sr : sg) {
                        grpTuple.add(new ThinRelTuple(sr.relVers, sr.relPart));
                    }
                    tmpTLabel = TermLabelMaker.newLabel(grpTuple, showDetailCB.isSelected(), showStatusCB.isSelected());
                    tLabelList.add((LabelForTuple) tmpTLabel);
                    tmpDeltaColor = relColorMap.get(grpTuple.get(0));
                    setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                    formJPanel.add(tmpTLabel.getLabel(), c);
                    c.gridy++;
                } else { // if false, show 1 rel per label
                    for (SnoRel sr : sg) {
                        I_RelTuple rTuple = new ThinRelTuple(sr.relVers, sr.relPart);
                        tmpTLabel = TermLabelMaker.newLabelForm(rTuple, showDetailCB.isSelected(),
                            showStatusCB.isSelected());
                        tLabelList.add((LabelForTuple) tmpTLabel);
                        tmpDeltaColor = relColorMap.get(rTuple);
                        setBorder(tmpTLabel.getLabel(), tmpDeltaColor);
                        formJPanel.add(tmpTLabel.getLabel(), c);
                        c.gridy++;
                    }
                    c.gridy++;
                }
            }
        }

        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        formJPanel.add(new JPanel(), c);
        formJPanel.setBorder(BorderFactory.createTitledBorder(label));
        return formJPanel;
    }

    private void setBorder(JLabel tLabel, Color deltaColor) {
        if (deltaColor == null) {
            deltaColor = Color.white;
        }
        Dimension size = tLabel.getSize();
        tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 5, 1, 5, deltaColor),
                BorderFactory.createEmptyBorder(1, 3, 1, 3))));
        size.width = size.width + 18;
        size.height = size.height + 6;
        tLabel.setSize(size);
        tLabel.setPreferredSize(size);
        tLabel.setMaximumSize(size);
        tLabel.setMinimumSize(size);
    }

    private I_ConceptAttributeTuple findSelf(ConceptBean cBean, List<I_Position> posList) {
        try {
            I_ConceptAttributeVersioned cv = cBean.getConceptAttributes();
            List<I_ConceptAttributePart> cvList = cv.getVersions();
            I_ConceptAttributePart cp1 = null;
            for (I_Position pos : posList) { // !!! <-- NullPointerException
                int tmpCountDupl = 0;
                for (I_ConceptAttributePart cp : cvList) {
                    // FIND MOST RECENT
                    if (cp.getPathId() == pos.getPath().getConceptId()) {
                        if (cp1 == null) {
                            cp1 = cp; // ... KEEP FIRST_INSTANCE PART
                        } else if (cp1.getVersion() < cp.getVersion()) {
                            cp1 = cp; // ... KEEP MORE_RECENT PART
                        } else if (cp1.getVersion() == cp.getVersion()) {
                            // !!! THIS DUPLICATE SHOULD NEVER HAPPEN
                            tmpCountDupl++;
                        }
                    }
                }
                // cp1.getStatusId() == isCURRENT
                if (cp1 != null) { // IF FOUND ON THIS PATH, STOP SEARCHING
                    return new ThinConTuple(cv, cp1);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return null;
    }

}
