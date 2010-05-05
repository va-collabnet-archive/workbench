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
package org.dwfa.ace.task.refset.spec.status;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.util.DynamicWidthComboBox;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * This panel allows the user to select a refset spec that they have owner access to.
 * 
 * @author Chrissy Hill
 * 
 */
public class ModifyOverallSpecStatusPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel refsetLabel;
    private JLabel refsetStatusLabel;
    private JLabel newStatusLabel;

    private DynamicWidthComboBox refsetComboBox;
    private DynamicWidthComboBox statusComboBox;
    
    I_ConfigAceFrame config;

    public ModifyOverallSpecStatusPanel() throws TerminologyException, IOException {
        super();
        //TODO use other than termFactory.getActiveAceFrameConfig();
        config = Terms.get().getActiveAceFrameConfig();
        init(config);
    }

    private void init(I_ConfigAceFrame config) {
        setDefaultValues();
        addListeners();
        layoutComponents(config);
    }

    private void setDefaultValues() {

        // combo box
        refsetComboBox = new DynamicWidthComboBox(getValidRefsets().toArray());
        statusComboBox = new DynamicWidthComboBox(getValidStatuses().toArray());

        // labels
        refsetLabel = new JLabel("Refset:");
        refsetStatusLabel = new JLabel("Existing status: ");
        newStatusLabel = new JLabel("New status: ");
    }

    private Set<I_GetConceptData> getValidStatuses() {
        Set<I_GetConceptData> statuses = new HashSet<I_GetConceptData>();
        try {
            statuses.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
            statuses.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IN_DEVELOPMENT.getUids()));
            statuses.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_EDITABLE.getUids()));
            statuses.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IN_REVIEW.getUids()));
            statuses.add(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.COMPLETED.getUids()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return statuses;
    }

    private String getPromotionStatus(I_ConfigAceFrame config) {
        I_GetConceptData selectedRefset = getRefset();
        if (selectedRefset != null) {
            RefsetSpec spec = new RefsetSpec(selectedRefset, true, config);
            return spec.getOverallSpecStatusString();
        } else {
            return "";
        }
    }

    private void addListeners() {
        refsetComboBox.addActionListener(new RefsetListener());
    }

    private void layoutComponents(I_ConfigAceFrame config) {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // refset label and combo box
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (refsetComboBox.getItemCount() == 0) {
            this.add(new JLabel("User does not have owner role permission for any refsets."), gridBagConstraints);
        } else {
            this.add(refsetComboBox, gridBagConstraints);
        }

        // refset status label
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(refsetStatusLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        JLabel promotionStatusLabel = new JLabel(getPromotionStatus(config));
        this.add(promotionStatusLabel, gridBagConstraints);

        // new status label and combo box
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(newStatusLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (statusComboBox.getItemCount() == 0) {
            this.add(new JLabel("No statuses to display."), gridBagConstraints);
        } else {
            this.add(statusComboBox, gridBagConstraints);
        }

        // filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

        this.repaint();
        this.revalidate();

    }

    /**
     * Calculates a set of valid refsets. Valid refsets include any that the current user has owner access to.
     * 
     * @return The set of valid parents.
     */
    private Set<I_GetConceptData> getValidRefsets() {
        HashSet<I_GetConceptData> validRefsets = new HashSet<I_GetConceptData>();
        try {

            I_TermFactory termFactory = Terms.get();
            I_GetConceptData activeUser = termFactory.getActiveAceFrameConfig().getDbConfig().getUserConcept();
            I_GetConceptData ownerRole = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OWNER_ROLE.getUids());

            I_IntSet roleAllowedTypes = termFactory.newIntSet();
            roleAllowedTypes.add(ownerRole.getConceptId());

            I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(termFactory.getActiveAceFrameConfig());
            I_IntSet currentStatuses = helper.getCurrentStatusIntSet();
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            List<? extends I_RelTuple> roleRels =
                    activeUser.getSourceRelTuples(currentStatuses, roleAllowedTypes, termFactory
                        .getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                        config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_RelTuple roleRel : roleRels) {

                I_GetConceptData hierarchy = termFactory.getConcept(roleRel.getC2Id());

                Set<? extends I_GetConceptData> children =
                        hierarchy.getDestRelOrigins(currentStatuses, termFactory.getActiveAceFrameConfig()
                            .getDestRelTypes(), termFactory.getActiveAceFrameConfig().getViewPositionSetReadOnly(), 
                            config.getPrecedence(), config.getConflictResolutionStrategy());
                for (I_GetConceptData child : children) {

                    RefsetSpec spec = new RefsetSpec(child, true, config);
                    if (spec.getRefsetSpecConcept() != null) {
                        validRefsets.add(child);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return validRefsets;
    }

    public I_GetConceptData getRefset() {
        return (I_GetConceptData) refsetComboBox.getSelectedItem();
    }

    public I_GetConceptData getStatus() {
        return (I_GetConceptData) statusComboBox.getSelectedItem();
    }

    class RefsetListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            layoutComponents(config);
        }
    }
}