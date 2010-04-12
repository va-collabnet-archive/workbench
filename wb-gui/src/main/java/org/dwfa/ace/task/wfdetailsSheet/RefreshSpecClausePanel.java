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
package org.dwfa.ace.task.wfdetailsSheet;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

public class RefreshSpecClausePanel extends JPanel implements ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final String REPLACE_OPTION = "replace concept with:";
    private static final String RETIRE_OPTION = "retire spec clause";
    private static final String SKIP_OPTION = "skip clause and place at end of list";

    private JComboBox updateOptions = new JComboBox(new String[] { REPLACE_OPTION, RETIRE_OPTION, SKIP_OPTION });
    private JTextArea editorComments = new JTextArea();
    private TermComponentLabel replacementConceptLabel;

    private I_GetConceptData refsetSpec;
    private Set<I_Position> refsetSpecVersionSet;
    private PositionSetReadOnly sourceTerminologyVersionSet;
    private I_GetConceptData conceptUnderReview;
    private I_ConfigAceFrame frameConfig;
    private List<Collection<UUID>> clausesToUpdate;

    private SrcRelTableModel srcRelTableModel;

    private JTableWithDragImage relTable;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private HostProxy host = new HostProxy();

    public RefreshSpecClausePanel(I_GetConceptData refsetIdentityConcept, PositionSetReadOnly refsetSpecVersionSet,
            PositionSetReadOnly sourceTerminologyVersionSet, List<Collection<UUID>> clausesToUpdate,
            I_ConfigAceFrame frameConfig) throws IOException, TerminologyException {
        super();
        replacementConceptLabel = null;
        replacementConceptLabel = new TermComponentLabel(frameConfig);
        this.refsetSpec =
        	Terms.get().getRefsetHelper(frameConfig).getSpecificationRefsetForRefset(refsetIdentityConcept, frameConfig).iterator().next();
        this.refsetSpecVersionSet = refsetSpecVersionSet;
        this.sourceTerminologyVersionSet = sourceTerminologyVersionSet;
        this.clausesToUpdate = clausesToUpdate;
        this.frameConfig = frameConfig;
        updateOptions.setSelectedItem(REPLACE_OPTION);
        Collection<UUID> clauseIds = clausesToUpdate.get(0);
        I_TermFactory tf = Terms.get();
        I_IntSet notCurrentStatus = tf.newIntSet();
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.CONFLICTING.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.NOT_YET_CREATED.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.RETIRED_MISSPELLED.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.IMPLIED_RELATIONSHIP.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid());
        notCurrentStatus.add(ArchitectonicAuxiliary.Concept.EXTINCT.localize().getNid());

        I_ExtendByRef member = tf.getExtension(Terms.get().uuidToNative(clauseIds));
        List<I_ExtendByRefVersion> tuples =
                (List<I_ExtendByRefVersion>) member.getTuples(frameConfig.getAllowedStatus(),
                    new PositionSetReadOnly(refsetSpecVersionSet),
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());
        for (I_ExtendByRefVersion tuple : tuples) {
            if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
                I_ExtendByRefPartCidCid ccPart = (I_ExtendByRefPartCidCid) tuple.getMutablePart();
                I_GetConceptData part1 = tf.getConcept(ccPart.getC1id());
                I_GetConceptData part2 = tf.getConcept(ccPart.getC2id());

                if (part1.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet, 
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()).size() > 0) {
                    this.conceptUnderReview = part1;
                }
                if (part2.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet, 
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()).size() > 0) {
                    this.conceptUnderReview = part2;
                }
                break;
            } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize()
                .getNid()) {
                I_ExtendByRefPartCidCidCid cccPart =
                        (I_ExtendByRefPartCidCidCid) tuple.getMutablePart();
                I_GetConceptData part1 = tf.getConcept(cccPart.getC1id());
                I_GetConceptData part2 = tf.getConcept(cccPart.getC2id());
                I_GetConceptData part3 = tf.getConcept(cccPart.getC3id());

                boolean hasRetiredConcept = false;
                if (part1.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet, frameConfig.getPrecedence(),
                    frameConfig.getConflictResolutionStrategy()).size() > 0) {
                    this.conceptUnderReview = part1;
                }
                if (part2.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet, 
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()).size() > 0) {
                    this.conceptUnderReview = part2;
                }
                if (part3.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet,
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()).size() > 0) {
                    this.conceptUnderReview = part3;
                }
                break;
            }
        }

        srcRelTableModel = new SrcRelTableModel(host, getSrcRelColumns(), frameConfig);
        relTable = new JTableWithDragImage(srcRelTableModel);
        RelationshipTableRenderer renderer = new RelationshipTableRenderer();
        SortClickListener.setupSorter(relTable);
        relTable.setDefaultRenderer(StringWithRelTuple.class, renderer);
        relTable.getTableHeader().setToolTipText(
            "Click to specify sorting");
        REL_FIELD[] columnEnums = srcRelTableModel.getColumnEnums();
        for (int i = 0; i < relTable.getColumnCount(); i++) {
            TableColumn column = relTable.getColumnModel().getColumn(i);
            REL_FIELD columnDesc = columnEnums[i];
            column.setIdentifier(columnDesc);
            column.setPreferredWidth(columnDesc.getPref());
            column.setMaxWidth(columnDesc.getMax());
            column.setMinWidth(columnDesc.getMin());
        }

        layoutRefreshSpecClausePanel();
        this.updateOptions.addActionListener(this);
        host.setTermComponent(conceptUnderReview);
    }

    private REL_FIELD[] getSrcRelColumns() {
        List<REL_FIELD> fields = new ArrayList<REL_FIELD>();
        fields.add(REL_FIELD.REL_TYPE);
        fields.add(REL_FIELD.DEST_ID);
        return fields.toArray(new REL_FIELD[fields.size()]);
    }

    private void layoutRefreshSpecClausePanel() throws IOException, TerminologyException {
        this.removeAll();
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // -------------------------------------------------
        // refset spec...
        // -------------------------------------------------
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 10, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Refset Spec:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        TermComponentLabel refsetSpecLabel = new TermComponentLabel(frameConfig);
        refsetSpecLabel.setTermComponent(refsetSpec);
        refsetSpecLabel.setFrozen(true);
        add(refsetSpecLabel, gbc);

        // -------------------------------------------------
        // Clause to update...
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 10, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Clause to Update:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        StringBuffer buff = new StringBuffer();

        I_TermFactory tf = Terms.get();
        Collection<UUID> clauseIds = clausesToUpdate.get(0);
        I_ExtendByRef member = tf.getExtension(Terms.get().uuidToNative(clauseIds));

        List<I_ExtendByRefVersion> tuples =
                (List<I_ExtendByRefVersion>) member.getTuples(frameConfig.getAllowedStatus(), 
                    frameConfig.getViewPositionSetReadOnly(), 
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());

        I_ExtendByRefVersion tuple = tuples.iterator().next();
        if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ExtendByRefPartCidCid ccPart = (I_ExtendByRefPartCidCid) tuple.getMutablePart();
            buff.append(tf.getConcept(ccPart.getC1id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(ccPart.getC2id()).toString());
        } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ExtendByRefPartCidCidCid cccPart =
                    (I_ExtendByRefPartCidCidCid) tuple.getMutablePart();
            buff.append(tf.getConcept(cccPart.getC1id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(cccPart.getC2id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(cccPart.getC3id()).toString());
        }
        add(new JLabel(buff.toString()), gbc);

        // -------------------------------------------------
        // Specification Version ....
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 10, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Specification Version:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel(refsetSpecVersionSet.toString()), gbc);

        // -------------------------------------------------
        // Terminology Version ....
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 10, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Terminology Version:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel(sourceTerminologyVersionSet.toString()), gbc);

        // -------------------------------------------------
        // Concept Under Review
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(25, 10, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font sansSerifFontBold14 = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        JLabel curLabel = new JLabel("Concept Under Review:");
        curLabel.setFont(sansSerifFontBold14);
        add(curLabel, gbc);

        int indentSize = 30;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 0.0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, indentSize, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font sansSerifFontBold12 = new Font(Font.SANS_SERIF, Font.BOLD, 12);
        TermComponentLabel conceptUnderReviewLabel = new TermComponentLabel(frameConfig);
        conceptUnderReviewLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 20, 5));
        conceptUnderReviewLabel.setTermComponent(conceptUnderReview);
        conceptUnderReviewLabel.setFrozen(true);
        conceptUnderReviewLabel.setFont(sansSerifFontBold12);
        conceptUnderReviewLabel.setForeground(Color.blue);
        conceptUnderReviewLabel.setBorder(BorderFactory.createLineBorder(Color.black, 3));
        add(conceptUnderReviewLabel, gbc);

        // -------------------------------------------------
        // Concept Relations....
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, indentSize, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Concept Relations:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, indentSize + 15, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(relTable), gbc);

        // -------------------------------------------------
        // Refresh Action....
        // -------------------------------------------------
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, indentSize, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Refresh Action:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 8;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(updateOptions, gbc);

        // -------------------------------------------------
        // Concept Drop Zone...
        // -------------------------------------------------
        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 9;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5, indentSize, 5, 5); // padding (Top, Left, Bottom, Right)
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Concept Replacement:"), gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 9;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            replacementConceptLabel.setBorder(BorderFactory.createLoweredBevelBorder());
            replacementConceptLabel.setBackground(Color.green);
            add(replacementConceptLabel, gbc);

        }

        // -------------------------------------------------
        // Editor Comments...
        // -------------------------------------------------
        if (updateOptions.equals(SKIP_OPTION) == false) {

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 10;
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5, indentSize, 5, 5); // padding (Top, Left, Bottom, Right)
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Comments:"), gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 2;
            gbc.gridy = 10;
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridheight = 3;
            gbc.insets = new Insets(5, 5, 5, 5); // padding (Top, Left, Bottom, Right)
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.BOTH;
            editorComments.setLineWrap(true);
            editorComments.setWrapStyleWord(true);
            JScrollPane commentsScrollPane = new JScrollPane(editorComments);
            commentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            add(commentsScrollPane, gbc);
        }

        // -------------------------------------------------
        // Refresh The Panel...
        // -------------------------------------------------
        if (getRootPane() != null) {
            getRootPane().validate();
        }
        if (this.getParent() != null) {
            this.getParent().validate();
            this.getParent().repaint();
        }
    }

    // @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            // @Override
            public void run() {
                try {
                    layoutRefreshSpecClausePanel();
                } catch (IOException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                } catch (TerminologyException e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        });
    }

    private class HostProxy implements I_HostConceptPlugins {

        // @Override
        public I_GetConceptData getHierarchySelection() {
            return frameConfig.getHierarchySelection();
        }

        // @Override
        public boolean getShowHistory() {
            return false;
        }

        // @Override
        public boolean getShowRefsets() {
            return false;
        }

        // @Override
        public boolean getToggleState(TOGGLES toggle) {
            return false;
        }

        // @Override
        public boolean getUsePrefs() {
            return false;
        }

        // @Override
        public void setAllTogglesToState(boolean state) {
            // nothing to do...
        }

        // @Override
        public void setLinkType(LINK_TYPE link) {
            // nothing to do...
        }

        // @Override
        public void setToggleState(TOGGLES toggle, boolean state) {
            // nothing to do...
        }

        // @Override
        public void unlink() {
            // nothing to do...
        }

        // @Override
        public void addPropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.addPropertyChangeListener(property, l);
        }

        // @Override
        public I_ConfigAceFrame getConfig() {
            return frameConfig;
        }

        // @Override
        public I_AmTermComponent getTermComponent() {
            return conceptUnderReview;
        }

        // @Override
        public void removePropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.removePropertyChangeListener(property, l);
        }

        // @Override
        public void setTermComponent(I_AmTermComponent termComponent) {
            conceptUnderReview = (I_GetConceptData) termComponent;
            pcs.firePropertyChange("termComponent", null, termComponent);
        }
    }

    public void performRefreshAction(I_ConfigAceFrame config) throws Exception {
        I_TermFactory tf = Terms.get();
        int currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        int retiredNid = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
        IntSet currentSet = new IntSet();
        currentSet.add(currentNid);
        IntSet retiredSet = new IntSet();
        retiredSet.add(retiredNid);
        boolean writeComment = editorComments.getText().length() > 3;
        I_ExtendByRef comment = null;
        I_Identify commentId = null;
        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
        I_GetConceptData commentRefset = refsetSpecHelper.getCommentsRefsetConcept();

        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
            // Do replacement here...

            // Make sure a replacement Concept has been set
            if (replacementConceptLabel == null || replacementConceptLabel.getTermComponent() == null) {
                // Warn the user and skip
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Please choose a 'Concept Replacement' to use this Refresh Action.", "", JOptionPane.ERROR_MESSAGE);
            } else {
                Collection<UUID> clauseIds = clausesToUpdate.remove(0);
                I_ExtendByRef member = tf.getExtension(tf.uuidToNative(clauseIds));
                List<I_ExtendByRefVersion> tuples = new ArrayList<I_ExtendByRefVersion>();
                member.addTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), tuples, 
                                config.getPrecedence(), config.getConflictResolutionStrategy());
                PathSetReadOnly promotionPath = new PathSetReadOnly(config.getPromotionPathSet());
                int newMemberNid =
                        tf.uuidToNative(UUID.randomUUID());
                I_Identify newMemberId = tf.getId(newMemberNid);
                I_ExtendByRef newMember =
                        tf.newExtensionNoChecks(member.getRefsetId(), newMemberNid, member.getComponentId(), member
                            .getTypeId());

                for (I_Path p : config.getEditingPathSet()) {

                    for (I_ExtendByRefVersion tuple : tuples) {

                        I_ExtendByRefPart newRetiredPart =
                                (I_ExtendByRefPart) tuple.getMutablePart().makeAnalog(retiredNid, p.getConceptId(),
                                    Long.MAX_VALUE);
                        tuple.addVersion(newRetiredPart);
                        tf.addUncommitted(tuple.getCore());

                        if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {

                            I_ExtendByRefPartCidCid newCCPart =
                                    (I_ExtendByRefPartCidCid) tuple.getMutablePart().makeAnalog(currentNid,
                                        p.getConceptId(), Long.MAX_VALUE);
                            newMember.addVersion(newCCPart);
                            tf.addUncommitted(newMember);

                            if (newCCPart.getC1id() == conceptUnderReview.getConceptId()) {
                                newCCPart.setC1id(replacementConceptLabel.getTermComponent().getNid());
                                tf.addUncommitted(comment);
                                tf.addUncommitted(commentRefset);
                            }
                            if (newCCPart.getC2id() == conceptUnderReview.getConceptId()) {
                                newCCPart.setC2id(replacementConceptLabel.getTermComponent().getNid());
                            }
                        } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION
                            .localize().getNid()) {
                            I_ExtendByRefPartCidCidCid newCCCPart =
                                    (I_ExtendByRefPartCidCidCid) tuple.getMutablePart().makeAnalog(
                                        currentNid, p.getConceptId(), Long.MAX_VALUE);
                            newMember.addVersion(newCCCPart);
                            tf.addUncommitted(newMember);
                            if (newCCCPart.getC1id() == conceptUnderReview.getConceptId()) {
                                newCCCPart.setC1id(replacementConceptLabel.getTermComponent().getNid());
                            }
                            if (newCCCPart.getC2id() == conceptUnderReview.getConceptId()) {
                                newCCCPart.setC2id(replacementConceptLabel.getTermComponent().getNid());
                            }
                            if (newCCCPart.getC3id() == conceptUnderReview.getConceptId()) {
                                newCCCPart.setC3id(replacementConceptLabel.getTermComponent().getNid());
                            }
                        }
                    }

                    if (writeComment) {
                        UUID memberUuid = UUID.randomUUID();
                        Collection<UUID> memberUuids = new ArrayList<UUID>();
                        memberUuids.add(memberUuid);
                        int commentNid =
                                tf.uuidToNative(memberUuids);
                        commentId = tf.getId(commentNid);
                        if (commentRefset != null && commentId != null) {
                            commentRefset.getUncommittedIdVersioned().add(commentId);
                            comment =
                                    tf.newExtensionNoChecks(commentRefset.getConceptId(), commentNid, newMember
                                        .getMemberId(), RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid());
                            I_ExtendByRefPartStr commentExtPart =
                                    tf.newExtensionPart(I_ExtendByRefPartStr.class);
                            commentExtPart.setStringValue(editorComments.getText());
                            comment.addVersion(commentExtPart);
                            commentExtPart.setPathId(p.getConceptId());
                            commentExtPart.setStatusId(currentNid);
                            commentExtPart.setTime(Long.MAX_VALUE);
                            tf.addUncommitted(comment);
                            tf.addUncommitted(commentRefset);
                        }
                        //
                    }
                    tf.commit();
                    member.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                        retiredSet, frameConfig.getPrecedence());
                    tf.addUncommitted(member);
                    newMember.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                        currentSet, frameConfig.getPrecedence());
                    tf.addUncommitted(newMember);
                    newMemberId.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                        currentSet, frameConfig.getPrecedence());
                    if (comment != null) {
                        comment.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                            currentSet, frameConfig.getPrecedence());
                        tf.addUncommitted(comment);
                        if (commentId != null) {
                            commentId.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                                currentSet, frameConfig.getPrecedence());
                            commentRefset.getUncommittedIdVersioned().add(commentId);
                            tf.addUncommitted(commentRefset);
                        }
                    }
                    tf.commit();
                } // End For
            } // End check for replacementConceptLabel

        } else if (updateOptions.getSelectedItem().equals(RETIRE_OPTION)) {
            // Do retire here...
            Collection<UUID> clauseIds = clausesToUpdate.remove(0);
            I_ExtendByRef member = tf.getExtension(tf.uuidToNative(clauseIds));
            List<I_ExtendByRefVersion> tuples = new ArrayList<I_ExtendByRefVersion>();
            member.addTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), tuples, 
                                config.getPrecedence(), config.getConflictResolutionStrategy());
            PathSetReadOnly promotionPath = new PathSetReadOnly(config.getPromotionPathSet());
            for (I_Path p : config.getEditingPathSet()) {
                for (I_ExtendByRefVersion tuple : tuples) {
                    I_ExtendByRefPart newPart =
                            (I_ExtendByRefPart) tuple.getMutablePart().makeAnalog(retiredNid, p.getConceptId(),
                                Long.MAX_VALUE);
                    tuple.getCore().addVersion(newPart);
                    tf.addUncommitted(tuple.getCore());
                }
                if (writeComment) {
                    UUID memberUuid = UUID.randomUUID();
                    Collection<UUID> memberUuids = new ArrayList<UUID>();
                    memberUuids.add(memberUuid);
                    int commentNid =
                            tf.uuidToNative(memberUuids);
                    commentId = tf.getId(commentNid);
                    commentRefset.getUncommittedIdVersioned().add(commentId);
                    comment =
                            tf.newExtensionNoChecks(commentRefset.getConceptId(), commentNid, member.getMemberId(),
                                RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid());
                    I_ExtendByRefPartStr commentExtPart = tf.newExtensionPart(I_ExtendByRefPartStr.class);
                    commentExtPart.setStringValue(editorComments.getText());
                    comment.addVersion(commentExtPart);
                    commentExtPart.setPathId(p.getConceptId());
                    commentExtPart.setStatusId(currentNid);
                    commentExtPart.setTime(Long.MAX_VALUE);
                    tf.addUncommitted(comment);
                    tf.addUncommitted(commentRefset);
                    //
                }
                tf.commit();
                member.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                    retiredSet, frameConfig.getPrecedence());
                tf.addUncommitted(member);
                if (comment != null) {
                    comment.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                        currentSet, frameConfig.getPrecedence());
                    tf.addUncommitted(comment);
                    if (commentId != null) {
                        commentId.promote(new Position(Integer.MAX_VALUE, p), promotionPath, 
                            currentSet, frameConfig.getPrecedence());
                        commentRefset.getUncommittedIdVersioned().add(commentId);
                        tf.addUncommitted(commentRefset);
                    }
                }
                tf.commit();
            }

        } else if (updateOptions.getSelectedItem().equals(SKIP_OPTION)) {
            // remove from front, add to end...
            clausesToUpdate.add(clausesToUpdate.remove(0));
        }
    }

}
