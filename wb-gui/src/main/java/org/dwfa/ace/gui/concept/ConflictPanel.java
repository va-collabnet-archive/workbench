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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.dwfa.ace.I_ImplementActiveLabel;
import org.dwfa.ace.LabelForConceptAttributeTuple;
import org.dwfa.ace.LabelForDescriptionTuple;
import org.dwfa.ace.LabelForRelationshipTuple;
import org.dwfa.ace.LabelForTuple;
import org.dwfa.ace.TermLabelMaker;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.Position;

public class ConflictPanel extends JPanel implements ActionListener {

    public static class ConflictColors {
        private List<Color> conflictColors = new ArrayList<Color>();

        int currentColor = 0;

        public ConflictColors() {
            super();
            // Link for colors
            // http://www.w3schools.com/html/html_colornames.asp
            conflictColors.add(new Color(0x5F9EA0));
            conflictColors.add(new Color(0x7FFF00));
            conflictColors.add(new Color(0xD2691E));
            conflictColors.add(new Color(0x6495ED));
            conflictColors.add(new Color(0xDC143C));
            conflictColors.add(new Color(0xB8860B));
            conflictColors.add(new Color(0xFF8C00));
            conflictColors.add(new Color(0x8FBC8F));
            conflictColors.add(new Color(0x483D8B));
            conflictColors.add(new Color(0x1E90FF));
            conflictColors.add(new Color(0xFFD700));
            conflictColors.add(new Color(0xF0E68C));
            conflictColors.add(new Color(0x90EE90));
            conflictColors.add(new Color(0x8470FF));
        }

        public Color getColor() {
            if (currentColor == conflictColors.size()) {
                reset();
            }
            return conflictColors.get(currentColor++);
        }

        public void reset() {
            currentColor = 0;
        }
    }

    public class ImplementActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            implementResolution();
            /*
             * if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
             * AceLog.getEditLog().info(" conflict resolution commit skipped
             * since
             * shift key down. "); } else { try { Terms.get().commit(); } catch
             * (IOException e1) { AceLog.getEditLog().alertAndLogException(e1);
             * } }
             */
        }

    }

    private class ConflictLabelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            switch (e.getID()) {
            case LabelForTuple.DOUBLE_CLICK:
                addToResolution(e);
                break;

            case LabelForTuple.POPUP:
                showAddPopup(e);
                break;

            default:
                AceLog.getAppLog().alertAndLogException(
                    new Exception("Can't handle event id: " + e.getID() + " " + e.getActionCommand()));
                break;
            }
        }

    }

    private class VersionLabelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            switch (e.getID()) {
            case LabelForTuple.DOUBLE_CLICK:
                addToResolution(e);
                break;

            case LabelForTuple.POPUP:
                showAddPopup(e);
                break;

            default:
                AceLog.getAppLog().alertAndLogException(
                    new Exception("Can't handle event id: " + e.getID() + " " + e.getActionCommand()));
                break;
            }
        }

    }

    private class AddListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            LabelForTuple partLabel = (LabelForTuple) addPopup.getClientProperty("partLabel");
            try {
                I_ImplementActiveLabel copy = partLabel.copy();
                copy.getLabel().setBorder(partLabel.getLabel().getBorder());
                copy.getLabel().setPreferredSize((partLabel.getLabel().getPreferredSize()));
                copy.getLabel().setMinimumSize((partLabel.getLabel().getMinimumSize()));
                copy.getLabel().setMaximumSize((partLabel.getLabel().getMaximumSize()));
                addToResolutionPanel(copy);
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
        }

    }

    private class AddAllListener implements ActionListener {

        List<LabelForTuple> tuples = new ArrayList<LabelForTuple>();

        public AddAllListener(List<LabelForTuple> tuples) {
            super();
            this.tuples = tuples;
        }

        public void actionPerformed(ActionEvent e) {
            for (LabelForTuple partLabel : tuples) {
                try {
                    I_ImplementActiveLabel copy = partLabel.copy();
                    copy.getLabel().setBorder(partLabel.getLabel().getBorder());
                    copy.getLabel().setPreferredSize((partLabel.getLabel().getPreferredSize()));
                    copy.getLabel().setMinimumSize((partLabel.getLabel().getMinimumSize()));
                    copy.getLabel().setMaximumSize((partLabel.getLabel().getMaximumSize()));
                    addToResolutionPanelIfNew(copy);
                } catch (IOException e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
        }

    }

    private class RemoveListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            LabelForTuple partLabel = (LabelForTuple) deletePopup.getClientProperty("partLabel");
            removeFromResolutionPanel(partLabel);
        }

    }

    private class ResolutionLabelActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            showRemovePopup(e);
        }

    }

    ConflictLabelActionListener conflictLabelListener = new ConflictLabelActionListener();

    VersionLabelActionListener versionLabelListener = new VersionLabelActionListener();

    ResolutionLabelActionListener resolutionLabelListener = new ResolutionLabelActionListener();

    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private ConflictColors colors = new ConflictColors();

    private JPanel resolutionPanel;

    private JPanel differencePanel;

    private JPanel versionPanel;

    private Dimension maxPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 4000);

    private Dimension minPartPanelSize = new Dimension(TermLabelMaker.LABEL_WIDTH + 20, 100);

    private JCheckBox showStatus = new JCheckBox("status");

    private JCheckBox longForm = new JCheckBox("long form");

    private I_GetConceptData cb;

    private I_ConfigAceFrame config;

    private JButton resolveButton = new JButton("implement");

    private List<I_ImplementActiveLabel> resolutionLabels;

    private JPanel resolutionPartPanel;

    private JPopupMenu deletePopup = new JPopupMenu();

    private JPopupMenu addPopup = new JPopupMenu();

    public ConflictPanel() {
        super();
        initWithGridBagLayout();
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 3),
            BorderFactory.createLineBorder(Color.GRAY)));
        showStatus.setSelected(false);
        showStatus.addActionListener(this);
        longForm.addActionListener(this);
        resolveButton.addActionListener(new ImplementActionListener());
        JMenuItem addItem = new JMenuItem("Add to resolution");
        addItem.addActionListener(new AddListener());
        addPopup.addSeparator();
        addPopup.add(addItem);

        JMenuItem removeItem = new JMenuItem("Remove from resolution");
        deletePopup.addSeparator();
        removeItem.addActionListener(new RemoveListener());
        deletePopup.add(removeItem);

    }

    private void showRemovePopup(ActionEvent e) {
        JLabel partLabel = (JLabel) e.getSource();
        Point location = partLabel.getMousePosition();
        deletePopup.putClientProperty("partLabel", partLabel);
        deletePopup.show((Component) e.getSource(), location.x, location.y);
    }

    private void showAddPopup(ActionEvent e) {
        JLabel partLabel = (JLabel) e.getSource();
        Point location = partLabel.getMousePosition();
        addPopup.putClientProperty("partLabel", partLabel);
        addPopup.show((Component) e.getSource(), location.x, location.y);
    }

    private void setMinAndMax(JPanel panel) {
        panel.setMinimumSize(minPartPanelSize);
        panel.setMaximumSize(maxPartPanelSize);
    }

    private JPanel getMinAndMaxPanel() {
        JPanel p = new JPanel() {
            /**
          * 
          */
            private static final long serialVersionUID = 1L;

            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, minPartPanelSize.width);
                d.height = Math.max(d.height, minPartPanelSize.height);
                d.height = Math.min(d.height, maxPartPanelSize.height);
                d.height = Math.min(d.height, maxPartPanelSize.height);
                return d;
            }
        };
        setMinAndMax(p);
        return p;
    }

    private void initWithGridBagLayout() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        c.gridy++;
        JLabel label = new JLabel("Conflicts:");
        label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 0));
        add(label, c);
        c.gridx++;

        // add(resolveButton, c);
        c.gridx++;
        add(longForm, c);
        c.gridx++;
        add(showStatus, c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;

        resolutionPanel = getMinAndMaxPanel();
        resolutionPanel.setLayout(new GridBagLayout());
        resolutionPanel.setName("resolutionPanel");
        resolutionPanel.setBorder(BorderFactory.createTitledBorder("Resolution: "));
        add(resolutionPanel, c);
        c.gridx = c.gridx + c.gridwidth;
        differencePanel = getMinAndMaxPanel();
        differencePanel.setLayout(new GridLayout(0, 1));
        differencePanel.setName("difference panel");
        differencePanel.setBorder(BorderFactory.createTitledBorder("Differences: "));
        add(differencePanel, c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 4;
        versionPanel = new JPanel(new GridBagLayout());
        versionPanel.setName("versionPanel");
        versionPanel.setBorder(BorderFactory.createTitledBorder("Versions: "));
        JScrollPane differenceScroller = new JScrollPane(versionPanel);
        differenceScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(differenceScroller, c);
    }

    private void implementResolution() {
        if (config.getEditingPathSet().size() > 0) {
            try {
                HashMap<Integer, I_DescriptionTuple> descsForResolution = new HashMap<Integer, I_DescriptionTuple>();
                HashMap<Integer, I_RelTuple> relsForResolution = new HashMap<Integer, I_RelTuple>();
                HashMap<Integer, I_ConceptAttributeTuple> attributesForResolution = new HashMap<Integer, I_ConceptAttributeTuple>();
                for (I_ImplementActiveLabel l : resolutionLabels) {
                    if (LabelForDescriptionTuple.class.isAssignableFrom(l.getClass())) {
                        LabelForDescriptionTuple ldt = (LabelForDescriptionTuple) l;
                        descsForResolution.put(ldt.getDesc().getDescId(), ldt.getDesc());
                    } else if (LabelForRelationshipTuple.class.isAssignableFrom(l.getClass())) {
                        LabelForRelationshipTuple lrt = (LabelForRelationshipTuple) l;
                        relsForResolution.put(lrt.getRel().getRelId(), lrt.getRel());
                    } else if (LabelForConceptAttributeTuple.class.isAssignableFrom(l.getClass())) {
                        LabelForConceptAttributeTuple lcat = (LabelForConceptAttributeTuple) l;
                        attributesForResolution.put(lcat.getConAttr().getConId(), lcat.getConAttr());
                    } else {
                        throw new UnsupportedOperationException(l.getClass().getName());
                    }
                }
                if (AceLog.getEditLog().isLoggable(Level.FINE)) {
                    AceLog.getEditLog().fine("descsForResolution: " + descsForResolution);
                    AceLog.getEditLog().fine("relsForResolution: " + relsForResolution);
                    AceLog.getEditLog().fine("attributesForResolution: " + attributesForResolution);
                    AceLog.getEditLog().fine("edit path set: " + config.getEditingPathSet());
                    AceLog.getEditLog().fine("view position set: " + config.getViewPositionSet());
                }
                for (I_Path editPath : config.getEditingPathSet()) {
                    processPathForImplementation(descsForResolution, relsForResolution, attributesForResolution,
                        editPath);
                }
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
            Terms.get().addUncommitted(cb);

        } else {
            JOptionPane.showMessageDialog(this, "You must select at least one path to edit on...");
        }
    }

    private void processPathForImplementation(HashMap<Integer, I_DescriptionTuple> descsForResolution,
            HashMap<Integer, I_RelTuple> relsForResolution,
            HashMap<Integer, I_ConceptAttributeTuple> attributesForResolution, I_Path editPath) throws IOException,
            TerminologyException {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("processing editPath: " + editPath);
        }
        Set<I_Position> positions = new HashSet<I_Position>();
        positions.add(new Position(Integer.MAX_VALUE, editPath));
        List<I_ConceptAttributeVersioned> attributeList = new ArrayList<I_ConceptAttributeVersioned>();
        attributeList.add(cb.getConceptAttributes());

        for (I_ConceptAttributeVersioned attributes : attributeList) {
            processAttributesForImplementation(attributesForResolution, editPath, positions, attributes);
        }

        for (I_DescriptionVersioned desc : cb.getDescriptions()) {
            processDescriptionsForImplementation(descsForResolution, editPath, positions, desc);
        }

        for (I_RelVersioned rel : cb.getSourceRels()) {
            processRelsForResolution(relsForResolution, editPath, positions, rel);
        }
    }

    private void processRelsForResolution(HashMap<Integer, I_RelTuple> relsForResolution, I_Path editPath,
            Set<I_Position> positions, I_RelVersioned rel) throws IOException, TerminologyException {
        if (AceLog.getEditLog().isLoggable(Level.FINE)) {
            AceLog.getEditLog().fine("  processing rel: " + rel.getRelId() + " " + rel);
        }
        throw new UnsupportedOperationException();
    }

    private void processDescriptionsForImplementation(HashMap<Integer, I_DescriptionTuple> descsForResolution,
            I_Path editPath, Set<I_Position> positions, I_DescriptionVersioned desc) throws IOException,
            TerminologyException {
    	throw new UnsupportedOperationException();
    }

    private void processAttributesForImplementation(HashMap<Integer, I_ConceptAttributeTuple> attributesForResolution,
            I_Path editPath, Set<I_Position> positions, I_ConceptAttributeVersioned attributes) throws IOException,
            TerminologyException {
        throw new UnsupportedOperationException("Model change requires reimplementation...");
    }

    private void addAttrPart(HashMap<Integer, I_ConceptAttributeTuple> attrsForResolution, I_Path editPath,
            I_ConceptAttributeVersioned attr) {
        I_ConceptAttributePart newPart = (I_ConceptAttributePart) attrsForResolution.get(attr.getConId()).makeAnalog(config.getDefaultStatus().getConceptId(), editPath.getConceptId(), Integer.MAX_VALUE);
  
        attr.addVersion(newPart);
    }

    private void addDescPart(HashMap<Integer, I_DescriptionTuple> descsForResolution, I_Path editPath,
            I_DescriptionVersioned desc) {
        I_DescriptionPart newPart = (I_DescriptionPart) descsForResolution.get(desc.getDescId()).makeAnalog(config.getDefaultStatus().getConceptId(), editPath.getConceptId(), Integer.MAX_VALUE);
 
        desc.addVersion(newPart);
    }

    private void retireDescPart(I_Path editPath, I_DescriptionVersioned desc) throws IOException, TerminologyException {
        I_DescriptionPart newPart = (I_DescriptionPart) desc.getLastTuple().makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), editPath.getConceptId(), Integer.MAX_VALUE);

        desc.addVersion(newPart);
    }

    private void addRelPart(HashMap<Integer, I_RelTuple> relsForResolution, I_Path editPath, I_RelVersioned rel) {
        I_RelPart newPart = (I_RelPart) relsForResolution.get(rel.getRelId()).makeAnalog(config.getDefaultStatus().getConceptId(), editPath.getConceptId(), Integer.MAX_VALUE);

        rel.addVersion(newPart);
    }

    private void retireRelPart(I_Path editPath, I_RelVersioned rel) throws IOException, TerminologyException {
        I_RelPart newPart = (I_RelPart) rel.getLastTuple().makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), editPath.getConceptId(), Integer.MAX_VALUE);

        rel.addVersion(newPart);
    }

    private void addToResolution(ActionEvent e) {
        I_ImplementActiveLabel source = (I_ImplementActiveLabel) e.getSource();
        try {
            I_ImplementActiveLabel copy = source.copy();
            copy.getLabel().setBorder(source.getLabel().getBorder());
            copy.getLabel().setPreferredSize((source.getLabel().getPreferredSize()));
            copy.getLabel().setMinimumSize((source.getLabel().getMinimumSize()));
            copy.getLabel().setMaximumSize((source.getLabel().getMaximumSize()));
            addToResolutionPanel(copy);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }

    private void removeFromResolutionPanel(I_ImplementActiveLabel activeLabel) {
        if (resolutionLabels.contains(activeLabel)) {
            resolutionLabels.remove(activeLabel);
            if (resolutionLabels.size() == 0) {
                resolveButton.setEnabled(false);
                resolveButton.setVisible(false);
            }
            resolutionPartPanel.remove(activeLabel.getLabel());
            resolutionPartPanel.revalidate();
        } else {
            JOptionPane.showMessageDialog(ConflictPanel.this, "Tuple is not part of resolution...");
        }
    }

    private void addToResolutionPanel(I_ImplementActiveLabel activeLabel) {
        addToResolutionPanel(activeLabel, true);
    }

    private void addToResolutionPanel(I_ImplementActiveLabel activeLabel, boolean showAlert) {
        if (resolutionLabels.contains(activeLabel)) {
            if (showAlert) {
                JOptionPane.showMessageDialog(ConflictPanel.this, "Tuple is already part of resolution...");
            }
        } else {
            resolveButton.setEnabled(true);
            resolveButton.setVisible(true);
            resolutionLabels.add(activeLabel);
            activeLabel.addActionListener(resolutionLabelListener);
            resolutionPartPanel.add(activeLabel.getLabel());
            resolutionPartPanel.revalidate();
        }
    }

    private void addToResolutionPanelIfNew(I_ImplementActiveLabel activeLabel) {
        addToResolutionPanel(activeLabel, false);
    }

    public void setConcept(I_GetConceptData cb, I_ConfigAceFrame config) throws IOException, TerminologyException {
        this.cb = cb;
        this.config = config;
        resolveButton.setEnabled(false);
        resolveButton.setVisible(false);
        conflictLabelListener = new ConflictLabelActionListener();
        versionLabelListener = new VersionLabelActionListener();
        resolutionLabelListener = new ResolutionLabelActionListener();
        resolutionPanel.removeAll();
        differencePanel.removeAll();
        versionPanel.removeAll();
        if (cb != null) {
            resolutionLabels = getCommonLabels(longForm.isSelected(), showStatus.isSelected(), config);
            if (resolutionLabels.size() > 0) {
                resolveButton.setEnabled(true);
                resolveButton.setVisible(true);
            }
            resolutionPartPanel = new JPanel();
            setMinAndMax(resolutionPartPanel);
            resolutionPartPanel.setLayout(new BoxLayout(resolutionPartPanel, BoxLayout.Y_AXIS));
            for (I_ImplementActiveLabel l : resolutionLabels) {
                resolutionPartPanel.add(l.getLabel());
                l.addActionListener(resolutionLabelListener);
            }
            GridBagConstraints resolutionConstraints = new GridBagConstraints();
            resolutionConstraints.anchor = GridBagConstraints.SOUTHEAST;
            resolutionConstraints.fill = GridBagConstraints.NONE;
            resolutionConstraints.gridheight = 1;
            resolutionConstraints.gridwidth = 1;
            resolutionConstraints.gridx = 0;
            resolutionConstraints.gridy = 1;
            resolutionConstraints.weightx = 1;
            resolutionConstraints.weighty = 1;
            resolutionPanel.add(resolveButton, resolutionConstraints);

            resolutionConstraints.anchor = GridBagConstraints.NORTHWEST;
            resolutionConstraints.gridwidth = 2;
            resolutionConstraints.gridx = 0;
            resolutionConstraints.gridx = 0;
            resolutionConstraints.gridy = 0;
            resolutionConstraints.weighty = 0;
            resolutionPanel.add(resolutionPartPanel, resolutionConstraints);

            Map<I_ConceptAttributeTuple, Color> conAttrColorMap = new HashMap<I_ConceptAttributeTuple, Color>();
            Map<I_DescriptionTuple, Color> desColorMap = new HashMap<I_DescriptionTuple, Color>();
            Map<I_RelTuple, Color> relColorMap = new HashMap<I_RelTuple, Color>();
            colors.reset();
            Collection<I_ImplementActiveLabel> conflictingLabels = getConflictingLabels(longForm.isSelected(),
                showStatus.isSelected(), config, colors, conAttrColorMap, desColorMap, relColorMap);
            JPanel conflictPartPanel = new JPanel();
            conflictPartPanel.setLayout(new BoxLayout(conflictPartPanel, BoxLayout.Y_AXIS));
            for (I_ImplementActiveLabel l : conflictingLabels) {
                conflictPartPanel.add(l.getLabel());
            }
            differencePanel.add(conflictPartPanel);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            c.anchor = GridBagConstraints.NORTHWEST;
            c.weightx = 0;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;

            for (I_Position p : config.getViewPositionSet()) {
                JPanel statePanel = getVersionView(p, config, conAttrColorMap, desColorMap, relColorMap);
                setMinAndMax(statePanel);
                versionPanel.add(statePanel, c);
                c.gridx++;
                if (c.gridx == 2) {
                    c.gridx = 0;
                    c.gridy++;
                }
            }
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            versionPanel.add(new JPanel(), c);
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            setConcept(cb, config);
            revalidate();
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLog(this, Level.SEVERE, "Database Exception: " + e1.getLocalizedMessage(), e1);
        }
    }

    public List<I_ImplementActiveLabel> getCommonLabels(boolean showLongForm, boolean showStatus,
            I_ConfigAceFrame config) throws IOException, TerminologyException {
        List<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>();

        // concept attributes
        Set<? extends I_ConceptAttributeTuple> commonConceptAttributes = this.cb.getCommonConceptAttributeTuples(config);
        if (commonConceptAttributes != null) {
            for (I_ConceptAttributeTuple t : commonConceptAttributes) {
                I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
                conAttrLabel.addActionListener(resolutionLabelListener);
                setBorder(conAttrLabel.getLabel(), null);
                labelList.add(conAttrLabel);
            }
        }

        // descriptions
        Set<? extends I_DescriptionTuple> commonDescTuples = this.cb.getCommonDescTuples(config);
        if (commonDescTuples != null) {
            for (I_DescriptionTuple t : commonDescTuples) {
                I_ImplementActiveLabel descLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
                descLabel.addActionListener(resolutionLabelListener);
                setBorder(descLabel.getLabel(), null);
                labelList.add(descLabel);
            }
        }
        // src relationships
        Set<? extends I_RelTuple> commonRelTuples = this.cb.getCommonRelTuples(config);
        if (commonRelTuples != null) {
            for (I_RelTuple t : commonRelTuples) {
                I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
                relLabel.addActionListener(resolutionLabelListener);
                setBorder(relLabel.getLabel(), null);
                labelList.add(relLabel);
            }
        }

        return labelList;
    }

    private void setBorder(JLabel tLabel, Color conflictColor) {
        if (conflictColor == null) {
            conflictColor = Color.white;
        }
        Dimension size = tLabel.getSize();
        tLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(),
            BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 5, 1, 5, conflictColor),
                BorderFactory.createEmptyBorder(1, 3, 1, 3))));
        size.width = size.width + 18;
        size.height = size.height + 6;
        tLabel.setSize(size);
        tLabel.setPreferredSize(size);
        tLabel.setMaximumSize(size);
        tLabel.setMinimumSize(size);
    }

    public Collection<I_ImplementActiveLabel> getConflictingLabels(boolean showLongForm, boolean showStatus,
            I_ConfigAceFrame config, ConflictColors colors, Map<I_ConceptAttributeTuple, Color> conAttrColorMap,
            Map<I_DescriptionTuple, Color> descColorMap, Map<I_RelTuple, Color> relColorMap) throws IOException, TerminologyException {

        Set<I_ConceptAttributeTuple> allConAttrTuples = new HashSet<I_ConceptAttributeTuple>();
        Set<I_DescriptionTuple> allDescTuples = new HashSet<I_DescriptionTuple>();
        Set<I_RelTuple> allRelTuples = new HashSet<I_RelTuple>();

        for (I_Position p : config.getViewPositionSet()) {
            Set<I_Position> posSet = new HashSet<I_Position>();
            posSet.add(p);
            PositionSetReadOnly positionSet = new PositionSetReadOnly(posSet);
            // concept attributes
            List<? extends I_ConceptAttributeTuple> conAttrTuplesForPosition = this.cb.getConceptAttributeTuples(
                config.getAllowedStatus(), positionSet, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            allConAttrTuples.addAll(conAttrTuplesForPosition);

            // descriptions
            List<? extends I_DescriptionTuple> descTuplesForPosition = this.cb.getDescriptionTuples(config.getAllowedStatus(),
                null, positionSet, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            allDescTuples.addAll(descTuplesForPosition);

            // relationships
            List<? extends I_RelTuple> relTuplesForPosition = this.cb.getSourceRelTuples(config.getAllowedStatus(), null,
                positionSet, 
                config.getPrecedence(), config.getConflictResolutionStrategy());
            allRelTuples.addAll(relTuplesForPosition);
        }

        Set<? extends I_ConceptAttributeTuple> commonConAttrTuples = this.cb.getCommonConceptAttributeTuples(config);
        allConAttrTuples.removeAll(commonConAttrTuples);

        Set<? extends I_DescriptionTuple> commonDescTuples = this.cb.getCommonDescTuples(config);
        allDescTuples.removeAll(commonDescTuples);

        Set<? extends I_RelTuple> commonRelTuples = this.cb.getCommonRelTuples(config);
        allRelTuples.removeAll(commonRelTuples);

        Collection<I_ImplementActiveLabel> labelList = new ArrayList<I_ImplementActiveLabel>(allDescTuples.size());

        for (I_ConceptAttributeTuple t : allConAttrTuples) {
            I_ImplementActiveLabel conAttrLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
            conAttrLabel.addActionListener(conflictLabelListener);
            Color conflictColor = colors.getColor();
            conAttrColorMap.put(t, conflictColor);
            setBorder(conAttrLabel.getLabel(), conflictColor);
            labelList.add(conAttrLabel);
        }
        for (I_DescriptionTuple t : allDescTuples) {
            I_ImplementActiveLabel descLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
            descLabel.addActionListener(conflictLabelListener);
            Color conflictColor = colors.getColor();
            descColorMap.put(t, conflictColor);
            setBorder(descLabel.getLabel(), conflictColor);
            labelList.add(descLabel);
        }
        for (I_RelTuple t : allRelTuples) {
            I_ImplementActiveLabel relLabel = TermLabelMaker.newLabel(t, showLongForm, showStatus);
            relLabel.addActionListener(conflictLabelListener);
            Color conflictColor = colors.getColor();
            relColorMap.put(t, conflictColor);
            setBorder(relLabel.getLabel(), conflictColor);
            labelList.add(relLabel);
        }

        return labelList;
    }

    public JPanel getVersionView(I_Position p, I_ConfigAceFrame config,
            Map<I_ConceptAttributeTuple, Color> conAttrColorMap, Map<I_DescriptionTuple, Color> desColorMap,
            Map<I_RelTuple, Color> relColorMap) throws IOException, TerminologyException {
        JPanel versionView = getMinAndMaxPanel();
        versionView.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 0;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        Set<I_Position> pSet = new HashSet<I_Position>(1);
        pSet.add(p);
        PositionSetReadOnly posSet = new PositionSetReadOnly(pSet);
        c.gridx = 1;

        JButton addAll = new JButton("add all");
        List<LabelForTuple> tuples = new ArrayList<LabelForTuple>();
        addAll.addActionListener(new AddAllListener(tuples));
        c.anchor = GridBagConstraints.NORTHEAST;
        versionView.add(addAll, c);
        c.gridx = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.NORTHWEST;

        // concept attributes
        List<? extends I_ConceptAttributeTuple> conAttrList = this.cb.getConceptAttributeTuples(config.getAllowedStatus(), posSet, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_ConceptAttributeTuple t : conAttrList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, false, false);
            tuples.add((LabelForTuple) tLabel);
            tLabel.addActionListener(versionLabelListener);
            Color conflictColor = conAttrColorMap.get(t);
            setBorder(tLabel.getLabel(), conflictColor);
            versionView.add(tLabel.getLabel(), c);
            c.gridy++;
        }

        // descriptions
        List<? extends I_DescriptionTuple> descList = this.cb.getDescriptionTuples(config.getAllowedStatus(), null, posSet, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_DescriptionTuple t : descList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, false, false);
            tuples.add((LabelForTuple) tLabel);
            tLabel.addActionListener(versionLabelListener);
            Color conflictColor = desColorMap.get(t);
            setBorder(tLabel.getLabel(), conflictColor);
            versionView.add(tLabel.getLabel(), c);
            c.gridy++;
        }
        // rels
        List<? extends I_RelTuple> relList = this.cb.getSourceRelTuples(config.getAllowedStatus(), null, posSet, 
            config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple t : relList) {
            I_ImplementActiveLabel tLabel = TermLabelMaker.newLabel(t, false, false);
            tuples.add((LabelForTuple) tLabel);
            tLabel.addActionListener(versionLabelListener);
            Color conflictColor = relColorMap.get(t);
            setBorder(tLabel.getLabel(), conflictColor);
            versionView.add(tLabel.getLabel(), c);
            c.gridy++;
        }
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 2;
        versionView.add(new JPanel(), c);
        versionView.setBorder(BorderFactory.createTitledBorder(p.toString()));
        return versionView;
    }

}
