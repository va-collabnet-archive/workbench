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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
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
    private Set<I_Position> sourceTerminologyVersionSet;
    private I_GetConceptData conceptUnderReview;
    private I_ConfigAceFrame frameConfig;
    private List<Collection<UUID>> clausesToUpdate;

    private SrcRelTableModel srcRelTableModel;

    private JTableWithDragImage relTable;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private HostProxy host = new HostProxy();

    public RefreshSpecClausePanel(I_GetConceptData refsetSpec, Set<I_Position> refsetSpecVersionSet,
            Set<I_Position> sourceTerminologyVersionSet, List<Collection<UUID>> clausesToUpdate,
            I_ConfigAceFrame frameConfig) throws IOException, TerminologyException {
        super();
        replacementConceptLabel = new TermComponentLabel(frameConfig);
        this.refsetSpec = refsetSpec;
        this.refsetSpecVersionSet = refsetSpecVersionSet;
        this.sourceTerminologyVersionSet = sourceTerminologyVersionSet;
        this.clausesToUpdate = clausesToUpdate;
        this.frameConfig = frameConfig;
        updateOptions.setSelectedItem(REPLACE_OPTION);
        Collection<UUID> clauseIds = clausesToUpdate.get(0);
        I_TermFactory tf = LocalVersionedTerminology.get();
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

        I_ThinExtByRefVersioned member = tf.getExtension(LocalVersionedTerminology.get().uuidToNative(clauseIds));
        List<I_ThinExtByRefTuple> tuples = member.getTuples(frameConfig.getAllowedStatus(), refsetSpecVersionSet,
            false, false);
        for (I_ThinExtByRefTuple tuple : tuples) {
            if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
                I_ThinExtByRefPartConceptConcept ccPart = (I_ThinExtByRefPartConceptConcept) tuple.getPart();
                I_GetConceptData part1 = tf.getConcept(ccPart.getC1id());
                I_GetConceptData part2 = tf.getConcept(ccPart.getC2id());

                if (part1.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet).size() > 0) {
                    this.conceptUnderReview = part1;
                }
                if (part2.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet).size() > 0) {
                    this.conceptUnderReview = part2;
                }
                break;
            } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize()
                .getNid()) {
                I_ThinExtByRefPartConceptConceptConcept cccPart = (I_ThinExtByRefPartConceptConceptConcept) tuple.getPart();
                I_GetConceptData part1 = tf.getConcept(cccPart.getC1id());
                I_GetConceptData part2 = tf.getConcept(cccPart.getC2id());
                I_GetConceptData part3 = tf.getConcept(cccPart.getC3id());

                boolean hasRetiredConcept = false;
                if (part1.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet).size() > 0) {
                    this.conceptUnderReview = part1;
                }
                if (part2.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet).size() > 0) {
                    this.conceptUnderReview = part2;
                }
                if (part3.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet).size() > 0) {
                    this.conceptUnderReview = part3;
                }
                break;
            }
        }

        srcRelTableModel = new SrcRelTableModel(host, getSrcRelColumns(), frameConfig);
        TableSorter relSortingTable = new TableSorter(srcRelTableModel);
        relTable = new JTableWithDragImage(relSortingTable);
        RelationshipTableRenderer renderer = new RelationshipTableRenderer();
        relTable.setDefaultRenderer(StringWithRelTuple.class, renderer);
        relSortingTable.setTableHeader(relTable.getTableHeader());
        relSortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");
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

        gbc.gridwidth = 1;
        gbc.gridheight = 1;

        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        add(new JLabel("refset spec:"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        TermComponentLabel refsetSpecLabel = new TermComponentLabel(frameConfig);
        refsetSpecLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        refsetSpecLabel.setTermComponent(refsetSpec);
        refsetSpecLabel.setFrozen(true);
        add(refsetSpecLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        add(new JLabel("clause to update:"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        StringBuffer buff = new StringBuffer();

        I_TermFactory tf = LocalVersionedTerminology.get();
        Collection<UUID> clauseIds = clausesToUpdate.get(0);
        I_ThinExtByRefVersioned member = tf.getExtension(LocalVersionedTerminology.get().uuidToNative(clauseIds));

        List<I_ThinExtByRefTuple> tuples = member.getTuples(frameConfig.getAllowedStatus(),
            frameConfig.getViewPositionSet(), false, false);

        I_ThinExtByRefTuple tuple = tuples.iterator().next();
        if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ThinExtByRefPartConceptConcept ccPart = (I_ThinExtByRefPartConceptConcept) tuple.getPart();
            buff.append(tf.getConcept(ccPart.getC1id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(ccPart.getC2id()).toString());
        } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ThinExtByRefPartConceptConceptConcept cccPart = (I_ThinExtByRefPartConceptConceptConcept) tuple.getPart();
            buff.append(tf.getConcept(cccPart.getC1id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(cccPart.getC2id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(cccPart.getC3id()).toString());
        }
        add(new JLabel(buff.toString()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        add(new JLabel("specification version:"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        add(new JLabel(refsetSpecVersionSet.toString()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        add(new JLabel("terminology version:"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        add(new JLabel(sourceTerminologyVersionSet.toString()), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;

        add(new JLabel("concept under review:"), gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        TermComponentLabel conceptUnderReviewLabel = new TermComponentLabel(frameConfig);
        conceptUnderReviewLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        conceptUnderReviewLabel.setTermComponent(conceptUnderReview);
        conceptUnderReviewLabel.setFrozen(true);
        add(conceptUnderReviewLabel, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.WEST;

        add(new JLabel("Concept relations:"), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(new JScrollPane(relTable), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;

        add(updateOptions, gbc);

        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.EAST;

            add(new JLabel("Concept replacement:"), gbc);

            gbc.gridy++;
            gbc.gridx = 0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.EAST;

            add(replacementConceptLabel, gbc);

        }

        gbc.gridy++;
        gbc.gridx = 0;

        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.EAST;

        if (updateOptions.equals(SKIP_OPTION) == false) {
            add(new JScrollPane(editorComments), gbc);
        }
        if (getRootPane() != null) {
            getRootPane().validate();
        }
        if (this.getParent() != null) {
            this.getParent().validate();
            this.getParent().repaint();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
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

        @Override
        public I_GetConceptData getHierarchySelection() {
            return frameConfig.getHierarchySelection();
        }

        @Override
        public boolean getShowHistory() {
            return false;
        }

        @Override
        public boolean getShowRefsets() {
            return false;
        }

        @Override
        public boolean getToggleState(TOGGLES toggle) {
            return false;
        }

        @Override
        public boolean getUsePrefs() {
            return false;
        }

        @Override
        public void setAllTogglesToState(boolean state) {
            // nothing to do...
        }

        @Override
        public void setLinkType(LINK_TYPE link) {
            // nothing to do...
        }

        @Override
        public void setToggleState(TOGGLES toggle, boolean state) {
            // nothing to do...
        }

        @Override
        public void unlink() {
            // nothing to do...
        }

        @Override
        public void addPropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.addPropertyChangeListener(property, l);
        }

        @Override
        public I_ConfigAceFrame getConfig() {
            return frameConfig;
        }

        @Override
        public I_AmTermComponent getTermComponent() {
            return conceptUnderReview;
        }

        @Override
        public void removePropertyChangeListener(String property, PropertyChangeListener l) {
            pcs.removePropertyChangeListener(property, l);
        }

        @Override
        public void setTermComponent(I_AmTermComponent termComponent) {
            conceptUnderReview = (I_GetConceptData) termComponent;
            pcs.firePropertyChange("termComponent", null, termComponent);
        }
    }

    public void performRefreshAction(I_ConfigAceFrame config) throws Exception {
        I_TermFactory tf = LocalVersionedTerminology.get();
        int currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
        IntSet currentSet = new IntSet();
        currentSet.add(currentNid);
        boolean writeComment = editorComments.getText().length() > 3;
        I_ThinExtByRefVersioned comment = null;
        I_IdVersioned commentId = null;
        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec);
        I_GetConceptData commentRefset = refsetSpecHelper.getCommentsRefsetConcept();

        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
            Collection<UUID> clauseIds = clausesToUpdate.remove(0);
            I_ThinExtByRefVersioned member = tf.getExtension(tf.uuidToNative(clauseIds));
            List<I_ThinExtByRefTuple> tuples = new ArrayList<I_ThinExtByRefTuple>();
            member.addTuples(config.getAllowedStatus(), config.getViewPositionSet(), tuples, false);
            for (I_Path p : config.getEditingPathSet()) {
                for (I_ThinExtByRefTuple tuple : tuples) {
                    I_ThinExtByRefPart newPart = tuple.getPart().duplicate();
                    if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
                        I_ThinExtByRefPartConceptConcept newCCPart = (I_ThinExtByRefPartConceptConcept) newPart;
                        newCCPart.setStatusId(currentNid);
                        newCCPart.setPathId(p.getConceptId());
                        newCCPart.setVersion(Integer.MAX_VALUE);
                        tuple.getCore().addVersion(newCCPart);
                        if (newCCPart.getC1id() == conceptUnderReview.getConceptId()) {
                            newCCPart.setC1id(replacementConceptLabel.getTermComponent().getNid());
                        }
                        if (newCCPart.getC2id() == conceptUnderReview.getConceptId()) {
                            newCCPart.setC2id(replacementConceptLabel.getTermComponent().getNid());
                        }
                    } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize()
                        .getNid()) {
                        I_ThinExtByRefPartConceptConceptConcept newCCCPart = (I_ThinExtByRefPartConceptConceptConcept) newPart;
                        newCCCPart.setStatusId(currentNid);
                        newCCCPart.setVersion(Integer.MAX_VALUE);
                        newCCCPart.setPathId(p.getConceptId());
                        tuple.getCore().addVersion(newCCCPart);
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
                    int commentNid = tf.uuidToNativeWithGeneration(memberUuids,
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), p, Integer.MAX_VALUE);
                    commentId = tf.getId(commentNid);
                    if (commentRefset != null && commentId != null) {
                        commentRefset.getUncommittedIdVersioned().add(commentId);
                        comment = tf.newExtensionNoChecks(commentRefset.getConceptId(), commentNid,
                            member.getComponentId(), RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid());
                        I_ThinExtByRefPartString commentExtPart = tf.newExtensionPart(I_ThinExtByRefPartString.class);
                        commentExtPart.setStringValue(editorComments.getText());
                        comment.addVersion(commentExtPart);
                        commentExtPart.setPathId(p.getConceptId());
                        commentExtPart.setStatusId(currentNid);
                        commentExtPart.setVersion(Integer.MAX_VALUE);
                        tf.addUncommitted(comment);
                        tf.addUncommitted(commentRefset);
                    }
                    //
                }
                tf.commit();
                member.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
                if (comment != null) {
                    comment.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
                    tf.addUncommitted(comment);
                    if (commentId != null) {
                        commentId.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
                        commentRefset.getUncommittedIdVersioned().add(commentId);
                        tf.addUncommitted(commentRefset);
                    }
                }
                tf.commit();
            }
            // Do replacement here...
        } else if (updateOptions.getSelectedItem().equals(RETIRE_OPTION)) {
            // Do retire here...
            int retiredNid = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
            IntSet retiredSet = new IntSet();
            retiredSet.add(retiredNid);
            Collection<UUID> clauseIds = clausesToUpdate.remove(0);
            I_ThinExtByRefVersioned member = tf.getExtension(tf.uuidToNative(clauseIds));
            List<I_ThinExtByRefTuple> tuples = new ArrayList<I_ThinExtByRefTuple>();
            member.addTuples(config.getAllowedStatus(), config.getViewPositionSet(), tuples, false);
            for (I_Path p : config.getEditingPathSet()) {
                for (I_ThinExtByRefTuple tuple : tuples) {
                    I_ThinExtByRefPart newPart = tuple.getPart().duplicate();
                    newPart.setVersion(Integer.MAX_VALUE);
                    newPart.setStatusId(retiredNid);
                    tuple.getCore().addVersion(newPart);
                    tf.addUncommitted(tuple.getCore());
                }
                if (writeComment) {
                    UUID memberUuid = UUID.randomUUID();
                    Collection<UUID> memberUuids = new ArrayList<UUID>();
                    memberUuids.add(memberUuid);
                    int commentNid = tf.uuidToNativeWithGeneration(memberUuids,
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), p, Integer.MAX_VALUE);
                    commentId = tf.getId(commentNid);
                    commentRefset.getUncommittedIdVersioned().add(commentId);
                    comment = tf.newExtensionNoChecks(commentRefset.getConceptId(), commentNid,
                        member.getComponentId(), RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid());
                    I_ThinExtByRefPartString commentExtPart = tf.newExtensionPart(I_ThinExtByRefPartString.class);
                    commentExtPart.setStringValue(editorComments.getText());
                    comment.addVersion(commentExtPart);
                    commentExtPart.setPathId(p.getConceptId());
                    commentExtPart.setStatusId(currentNid);
                    commentExtPart.setVersion(Integer.MAX_VALUE);
                    tf.addUncommitted(comment);
                    tf.addUncommitted(commentRefset);
                    //
                }
                tf.commit();
                member.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
                if (comment != null) {
                    comment.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
                    tf.addUncommitted(comment);
                    if (commentId != null) {
                        commentId.promote(new Position(Integer.MAX_VALUE, p), config.getPromotionPathSet(), currentSet);
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
