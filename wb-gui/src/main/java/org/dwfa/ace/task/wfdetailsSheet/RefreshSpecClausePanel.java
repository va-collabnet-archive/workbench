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
import java.awt.Dimension;
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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.RelationshipTableRenderer;
import org.dwfa.ace.table.SrcRelTableModel;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.refset.RefsetSpec;
import org.ihtsdo.tk.refset.SpecRefsetHelper;

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
    private ArrayList<TermComponentLabel> replacementConceptLabel;

    private I_GetConceptData refsetSpec;
    private Set<PositionBI> refsetSpecVersionSet;
    private PositionSetReadOnly sourceTerminologyVersionSet;
    private I_GetConceptData conceptUnderReview;
    private I_ConfigAceFrame frameConfig;
    private List<Collection<UUID>> clausesToUpdate;

    private SrcRelTableModel srcRelTableModel;

    private JTableWithDragImage relTable;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private HostProxy host = new HostProxy();

    private SpecRefsetHelper refsetHelper;

    @SuppressWarnings("unchecked")
    public RefreshSpecClausePanel(I_GetConceptData refsetIdentityConcept, PositionSetReadOnly refsetSpecVersionSet,
            PositionSetReadOnly sourceTerminologyVersionSet, List<Collection<UUID>> clausesToUpdate,
            I_ConfigAceFrame frameConfig) throws IOException, TerminologyException, Exception {
        super();
        replacementConceptLabel = null;
        replacementConceptLabel = new ArrayList<TermComponentLabel>();
        replacementConceptLabel.add(new TermComponentLabel(frameConfig));

        this.refsetHelper = new SpecRefsetHelper(frameConfig.getViewCoordinate(), frameConfig.getEditCoordinate());
        RefsetSpec specHelper = new RefsetSpec((ConceptChronicleBI) refsetIdentityConcept, true, frameConfig.getViewCoordinate());
        this.refsetSpec = (I_GetConceptData) specHelper.getRefsetSpecConcept();
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
                (List<I_ExtendByRefVersion>) member.getTuples(frameConfig.getAllowedStatus(), new PositionSetReadOnly(
                    refsetSpecVersionSet), frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());
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
            } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
                I_ExtendByRefPartCidCidCid cccPart = (I_ExtendByRefPartCidCidCid) tuple.getMutablePart();
                I_GetConceptData part1 = tf.getConcept(cccPart.getC1id());
                I_GetConceptData part2 = tf.getConcept(cccPart.getC2id());
                I_GetConceptData part3 = tf.getConcept(cccPart.getC3id());

                if (part1.getConceptAttributeTuples(notCurrentStatus, sourceTerminologyVersionSet,
                    frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy()).size() > 0) {
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
        relTable.getTableHeader().setToolTipText("Click to specify sorting");
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

    @SuppressWarnings("unchecked")
    private void layoutRefreshSpecClausePanel() throws IOException, TerminologyException {
        this.removeAll();
        JPanel replacementConceptsPanel = new JPanel();
        replacementConceptsPanel.setLayout(new GridBagLayout());
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // -------------------------------------------------
        // refset spec...
        // -------------------------------------------------
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Refset Spec:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
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
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Clause to Update:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        StringBuffer buff = new StringBuffer();

        I_TermFactory tf = Terms.get();
        Collection<UUID> clauseIds = clausesToUpdate.get(0);
        I_ExtendByRef member = tf.getExtension(Terms.get().uuidToNative(clauseIds));

        List<I_ExtendByRefVersion> tuples =
                (List<I_ExtendByRefVersion>) member.getTuples(frameConfig.getAllowedStatus(), frameConfig
                    .getViewPositionSetReadOnly(), frameConfig.getPrecedence(), frameConfig.getConflictResolutionStrategy());

        I_ExtendByRefVersion tuple = tuples.iterator().next();
        if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ExtendByRefPartCidCid ccPart = (I_ExtendByRefPartCidCid) tuple.getMutablePart();
            buff.append(tf.getConcept(ccPart.getC1id()).toString());
            buff.append(" ");
            buff.append(tf.getConcept(ccPart.getC2id()).toString());
        } else if (tuple.getTypeId() == RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid()) {
            I_ExtendByRefPartCidCidCid cccPart = (I_ExtendByRefPartCidCidCid) tuple.getMutablePart();
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
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Specification Version:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
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
        gbc.insets = new Insets(5, 10, 5, 5);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Terminology Version:"), gbc);

        gbc = new GridBagConstraints();
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);
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
        gbc.insets = new Insets(25, 10, 5, 5);
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
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, indentSize, 5, 5);
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
        GridBagConstraints scrollPaneGbc = new GridBagConstraints();
        scrollPaneGbc = new GridBagConstraints();
        scrollPaneGbc.gridx = 0;
        scrollPaneGbc.gridy = 6;
        scrollPaneGbc.weighty = 0.0;
        scrollPaneGbc.insets = new Insets(5, indentSize, 5, 5);
        scrollPaneGbc.anchor = GridBagConstraints.LINE_START;
        scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Concept Relations:"), scrollPaneGbc);

        scrollPaneGbc = new GridBagConstraints();
        scrollPaneGbc.gridx = 0;
        scrollPaneGbc.gridy = 7;
        scrollPaneGbc.weightx = 1;
        scrollPaneGbc.weighty = 1;
        scrollPaneGbc.gridwidth = 4;
        scrollPaneGbc.gridheight = 1;
        scrollPaneGbc.insets = new Insets(5, indentSize + 15, 5, 5);
        scrollPaneGbc.anchor = GridBagConstraints.FIRST_LINE_START;
        scrollPaneGbc.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(relTable), scrollPaneGbc);

        // -------------------------------------------------
        // Refresh Action....
        // -------------------------------------------------
        scrollPaneGbc = new GridBagConstraints();
        scrollPaneGbc.gridx = 0;
        scrollPaneGbc.gridy = 8;
        scrollPaneGbc.weighty = 0.0;
        scrollPaneGbc.insets = new Insets(5, indentSize, 5, 5);
        scrollPaneGbc.anchor = GridBagConstraints.LINE_START;
        scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Refresh Action:"), scrollPaneGbc);

        scrollPaneGbc = new GridBagConstraints();
        scrollPaneGbc.gridx = 3;
        scrollPaneGbc.gridy = 8;
        scrollPaneGbc.weighty = 0.0;
        scrollPaneGbc.insets = new Insets(5, 5, 5, 5);
        scrollPaneGbc.anchor = GridBagConstraints.LINE_START;
        scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;
        add(updateOptions, scrollPaneGbc);

        // -------------------------------------------------
        // Concept Drop Zone...
        // -------------------------------------------------

        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
            scrollPaneGbc = new GridBagConstraints();
            scrollPaneGbc.gridx = 0;
            scrollPaneGbc.gridy = 9;
            scrollPaneGbc.weighty = 0.0;
            scrollPaneGbc.insets = new Insets(5, indentSize, 5, 5);
            scrollPaneGbc.anchor = GridBagConstraints.NORTHWEST;
            scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Concept Replacement:"), scrollPaneGbc);

            scrollPaneGbc = new GridBagConstraints();
            scrollPaneGbc.gridx = 1;
            scrollPaneGbc.gridy = 9;
            scrollPaneGbc.weighty = 0.0;
            scrollPaneGbc.insets = new Insets(5, 1, 5, 1);
            scrollPaneGbc.anchor = GridBagConstraints.NORTHEAST;
            scrollPaneGbc.fill = GridBagConstraints.NONE;
            JButton addNewReplacementSlotButton = new JButton("+");
            addNewReplacementSlotButton.setToolTipText("Add empty replacement slot");
            addNewReplacementSlotButton.addActionListener(new AddReplacementButtonListener());
            add(addNewReplacementSlotButton, scrollPaneGbc);

            scrollPaneGbc = new GridBagConstraints();
            scrollPaneGbc.gridx = 2;
            scrollPaneGbc.gridy = 9;
            scrollPaneGbc.weighty = 0.0;
            scrollPaneGbc.insets = new Insets(5, 0, 5, 5);
            scrollPaneGbc.anchor = GridBagConstraints.NORTHEAST;
            scrollPaneGbc.fill = GridBagConstraints.NONE;
            JButton removeReplacementSlotButton = new JButton("-");
            removeReplacementSlotButton.setToolTipText("Remove empty replacement slot");
            removeReplacementSlotButton.addActionListener(new RemoveReplacementButtonListener());
            add(removeReplacementSlotButton, scrollPaneGbc);

            scrollPaneGbc = new GridBagConstraints();
            scrollPaneGbc.gridx = 0;
            scrollPaneGbc.gridy = 0;
            scrollPaneGbc.weighty = 0.0;
            scrollPaneGbc.weightx = 1.0;
            scrollPaneGbc.insets = new Insets(5, 5, 5, 5);
            scrollPaneGbc.anchor = GridBagConstraints.LINE_START;
            scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;
            int labelCount = 0;
            Dimension maximumSize = new Dimension();
            for (TermComponentLabel label : replacementConceptLabel) {
                label.setBorder(BorderFactory.createLoweredBevelBorder());
                label.setBackground(Color.green);
                scrollPaneGbc.gridy = labelCount;
                replacementConceptsPanel.add(label, scrollPaneGbc);
                labelCount++;
                if (labelCount == 4) {
                    maximumSize = replacementConceptsPanel.getPreferredSize();
            }
        }
            scrollPaneGbc = new GridBagConstraints();
            scrollPaneGbc.gridx = 3;
            scrollPaneGbc.gridy = 9;
            scrollPaneGbc.weighty = 0.0;
            scrollPaneGbc.insets = new Insets(5, 5, 5, 5);
            scrollPaneGbc.anchor = GridBagConstraints.LINE_START;
            scrollPaneGbc.fill = GridBagConstraints.HORIZONTAL;

            JScrollPane scrollPane = new JScrollPane(replacementConceptsPanel);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(null);
            scrollPane.setMaximumSize(null);
            scrollPane.setMinimumSize(null);
            if (labelCount >= 4) {
                scrollPane.setPreferredSize(maximumSize);
                scrollPane.setMaximumSize(maximumSize);
                scrollPane.setMinimumSize(maximumSize);
            } else {
                scrollPane.setPreferredSize(replacementConceptsPanel.getPreferredSize());
                scrollPane.setMaximumSize(replacementConceptsPanel.getPreferredSize());
                scrollPane.setMinimumSize(replacementConceptsPanel.getPreferredSize());
            }
            //scrollPane.setVerticalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            add(scrollPane, scrollPaneGbc);
        }
        //gbc = new GridBagConstraints();
        // gbc.gridx = 0;
        // gbc.gridy = 9;
        //  gbc.weighty = 0.0;
        //  gbc.insets = new Insets(5, indentSize, 5, 5);
        //  gbc.anchor = GridBagConstraints.LINE_START;
        //  gbc.fill = GridBagConstraints.HORIZONTAL;
        //  JScrollPane scrollPane = new JScrollPane(replacementConceptsPanel);
        //  scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //   add(scrollPane, gbc);

        // -------------------------------------------------
        // Editor Comments...
        // -------------------------------------------------
        if (updateOptions.equals(SKIP_OPTION) == false) {

            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 9 + replacementConceptLabel.size();
            gbc.weighty = 0.0;
            gbc.insets = new Insets(5, indentSize, 5, 5);
            gbc.anchor = GridBagConstraints.LINE_START;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(new JLabel("Comments:"), gbc);

            gbc = new GridBagConstraints();
            gbc.gridx = 3;
            gbc.gridy = 9 + replacementConceptLabel.size();
            gbc.weightx = 1;
            gbc.weighty = 1;
            gbc.gridheight = 3;
            gbc.insets = new Insets(5, 5, 5, 5);
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
        RefsetSpec refsetSpecHelper = new RefsetSpec(refsetSpec, frameConfig.getViewCoordinate());
        I_GetConceptData commentRefset = (I_GetConceptData) refsetSpecHelper.getCommentsRefsetConcepts().iterator().next();

        if (updateOptions.getSelectedItem().equals(REPLACE_OPTION)) {
            // Do replacement here...

            // Make sure a replacement Concept has been set
            if (replacementConceptLabel == null) {
                // Warn the user and skip
                JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                    "Please choose a 'Concept Replacement' to use this Refresh Action.", "", JOptionPane.ERROR_MESSAGE);
            } else {
                boolean replacementFound = false;
                for (TermComponentLabel label : replacementConceptLabel) {
                    if (label.getTermComponent() != null) {
                        replacementFound = true;
                    }
                }
                if (!replacementFound) {
                    // Warn the user and skip
                    JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                        "Please choose a 'Concept Replacement' to use this Refresh Action.", "", JOptionPane.ERROR_MESSAGE);
                } else {
                    Collection<UUID> clauseIds = clausesToUpdate.remove(0);
                    I_ExtendByRef member = tf.getExtension(tf.uuidToNative(clauseIds));
                    List<I_ExtendByRefVersion> tuples = new ArrayList<I_ExtendByRefVersion>();
                    member.addTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), tuples, config
                        .getPrecedence(), config.getConflictResolutionStrategy());
                    PathSetReadOnly promotionPath = new PathSetReadOnly(config.getPromotionPathSet());
                    PositionBI viewPosition = config.getViewPositionSet().iterator().next();
                    PathBI editPath = config.getEditingPathSet().iterator().next();
                    boolean retire = true;
                    I_ExtendByRefPartCidCid newRetiredPart = null;
                    for (TermComponentLabel label : replacementConceptLabel) {
                        if (label.getTermComponent() != null) {

                        for (I_ExtendByRefVersion tuple : tuples) {
                            if (newRetiredPart == null) {
                                newRetiredPart =
                                        (I_ExtendByRefPartCidCid) tuple.getMutablePart().makeAnalog(retiredNid,
                                            Long.MAX_VALUE,
                                            config.getEditCoordinate().getAuthorNid(),
                                            config.getEditCoordinate().getModuleNid(),
                                            editPath.getConceptNid());
                            }
                            if (retire) {
                                member.addVersion(newRetiredPart);
                            }

                            RefsetPropertyMap propMap = new RefsetPropertyMap();
                            int cNid1 = 0;
                            int cNid2 = 0;
                            if (newRetiredPart.getC1id() == conceptUnderReview.getConceptNid()) {
                                cNid1 = label.getTermComponent().getNid();
                            } else {
                                cNid1 = newRetiredPart.getC1id();
                            }
                            if (newRetiredPart.getC2id() == conceptUnderReview.getConceptNid()) {
                                cNid2 = label.getTermComponent().getNid();
                            } else {
                                cNid2 = newRetiredPart.getC2id();
                            }
                            I_ExtendByRef newMember = null;
                            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(frameConfig.getEditCoordinate(), frameConfig.getViewCoordinate());
                            switch (REFSET_TYPES.nidToType(tuple.getTypeId())) {
                            case CID_CID:
                                 RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID,
                                        member.getComponentNid(),
                                        member.getRefsetId());
                                memberBp.put(RefexCAB.RefexProperty.CNID1, cNid1);
                                memberBp.put(RefexCAB.RefexProperty.CNID2, cNid2);
                                newMember = (I_ExtendByRef) builder.constructIfNotCurrent(memberBp);
                                break;
                            case CID_CID_CID:
                                int cNid3 = 0;
                                I_ExtendByRefPartCidCidCid c3Part = (I_ExtendByRefPartCidCidCid) newRetiredPart;
                                if (c3Part.getC3id() == conceptUnderReview.getConceptNid()) {
                                    cNid3 = label.getTermComponent().getNid();
                                } else {
                                    cNid3 =  c3Part.getC3id();
                                }
                                memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID_CID,
                                        member.getComponentNid(),
                                        member.getRefsetId());
                                memberBp.put(RefexCAB.RefexProperty.CNID1, cNid1);
                                memberBp.put(RefexCAB.RefexProperty.CNID2, cNid2);
                                memberBp.put(RefexCAB.RefexProperty.CNID3, cNid3);
                                builder.constructIfNotCurrent(memberBp);
                                break;
                            default:
                                throw new Exception("Can't handle: " + REFSET_TYPES.nidToType(member.getTypeId()));
                            }
                            if (writeComment) {
                                RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                                        newMember.getComponentNid(),
                                        commentRefset.getConceptNid());
                                memberBp.put(RefexCAB.RefexProperty.STRING1, editorComments.getText());
                                comment = (I_ExtendByRef) builder.constructIfNotCurrent(memberBp);
                                tf.addUncommittedNoChecks(commentRefset);
                            }
                            tf.addUncommittedNoChecks(refsetSpec);
                            tf.commit();
                            if (retire) {
                                member.promote(viewPosition, promotionPath, retiredSet, 
                                        frameConfig.getPrecedence(), frameConfig.getEditCoordinate().getAuthorNid());
                            }
                            newMember.promote(viewPosition, promotionPath, currentSet, 
                                    frameConfig.getPrecedence(),frameConfig.getEditCoordinate().getAuthorNid());
                            tf.addUncommittedNoChecks(refsetSpec);
                            if (comment != null) {
                                comment.promote(viewPosition, promotionPath, currentSet, 
                                        frameConfig.getPrecedence(), frameConfig.getEditCoordinate().getAuthorNid());
                                tf.addUncommittedNoChecks(commentRefset);
                            }
                            tf.commit();
                            if (retire) {
                                retire = false; // only retire the clause once, even if multiple replacements are added
                            }
                        }
                    }
                    }

                    refsetSpecHelper.setLastEditTime(System.currentTimeMillis(), config.getEditCoordinate());
                    frameConfig.fireRefsetSpecChanged(member);
                    frameConfig.refreshRefsetTab();
                }
            } // End check for replacementConceptLabel
        } else if (updateOptions.getSelectedItem().equals(RETIRE_OPTION)) {
            // Do retire here...
            Collection<UUID> clauseIds = clausesToUpdate.remove(0);
            I_ExtendByRef member = tf.getExtension(tf.uuidToNative(clauseIds));
            List<I_ExtendByRefVersion> tuples = new ArrayList<I_ExtendByRefVersion>();
            member.addTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), tuples, config.getPrecedence(),
                config.getConflictResolutionStrategy());
            PathSetReadOnly promotionPath = new PathSetReadOnly(config.getPromotionPathSet());
            PositionBI viewPosition = config.getViewPositionSet().iterator().next();

            for (I_ExtendByRefVersion tuple : tuples) {
                tuple.addVersion((I_ExtendByRefPart) tuple.getMutablePart().makeAnalog(retiredNid,
                        Long.MAX_VALUE,
                        config.getEditCoordinate().getAuthorNid(),
                        config.getEditCoordinate().getModuleNid(),
                        viewPosition.getPath().getConceptNid()));
            }
            tf.addUncommittedNoChecks(refsetSpec);
            if (writeComment) {
                RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                        member.getComponentNid(),
                        commentRefset.getConceptNid());
                memberBp.put(RefexCAB.RefexProperty.STRING1, editorComments.getText());
                TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(frameConfig.getEditCoordinate(), frameConfig.getViewCoordinate());
                comment = (I_ExtendByRef) builder.constructIfNotCurrent(memberBp);
                tf.addUncommittedNoChecks(commentRefset);
            }
            tf.commit();
            member.promote(viewPosition, promotionPath, retiredSet, 
                    frameConfig.getPrecedence(), frameConfig.getEditCoordinate().getAuthorNid());
            tf.addUncommitted(member);
            if (comment != null) {
                comment.promote(viewPosition, promotionPath, currentSet, 
                        frameConfig.getPrecedence(), frameConfig.getEditCoordinate().getAuthorNid());
                tf.addUncommitted(comment);
            }
            tf.commit();
            refsetSpecHelper.setLastEditTime(System.currentTimeMillis(), config.getEditCoordinate());
            frameConfig.fireRefsetSpecChanged(member);
            frameConfig.refreshRefsetTab();

        } else if (updateOptions.getSelectedItem().equals(SKIP_OPTION)) {
            // remove from front, add to end...
            clausesToUpdate.add(clausesToUpdate.remove(0));
        }
    }

    public class AddReplacementButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {

            try {
                replacementConceptLabel.add(new TermComponentLabel(frameConfig));
                layoutRefreshSpecClausePanel();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    public class RemoveReplacementButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent arg0) {

            try {

                TermComponentLabel labelToRemove = null;
                for (TermComponentLabel label : replacementConceptLabel) {
                    if (label.getText().equals("<html><font color=red>Empty")) {
                        labelToRemove = label;
                    }
                }
                if (labelToRemove != null) {
                    if (replacementConceptLabel.size() > 1) {
                        replacementConceptLabel.remove(labelToRemove);
                    }
                }

                layoutRefreshSpecClausePanel();
            } catch (IOException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

}
