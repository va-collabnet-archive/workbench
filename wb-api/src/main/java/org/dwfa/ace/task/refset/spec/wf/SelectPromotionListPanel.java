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
package org.dwfa.ace.task.refset.spec.wf;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.util.LogWithAlerts;

/**
 * A panel to select the promotion list the user wishes to review/edit.
 * The list choices are:
 * 1) unreviewed additions
 * 2) unreviewed deletions
 * 3) reviewed rejected additions
 * 4) reviewed rejected deletions
 * 5) reviewed approved addition
 * 6) reviewed approved deletions
 * 
 * @author Chrissy Hill
 * 
 */
public class SelectPromotionListPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private I_TermFactory termFactory;
    private I_HelpSpecRefset refsetHelper;

    // components
    private ButtonGroup options;

    private String newAdditionsStr = "New additions";
    private String newDeletionsStr = "New deletions";
    private String approvedAdditionsStr = "Approved additions";
    private String approvedDeletionsStr = "Approved deletions";
    private String rejectedAdditionsStr = "Non-approved additions";
    private String rejectedDeletionsStr = "Non-approved deletions";

    private I_GetConceptData promotionRefsetConcept;
    private I_GetConceptData unreviewedAdditionStatus;
    private I_GetConceptData unreviewedDeletionStatus;
    private I_GetConceptData reviewedApprovedDeletionStatus;
    private I_GetConceptData reviewedApprovedAdditionStatus;
    private I_GetConceptData reviewedRejectedDeletionStatus;
    private I_GetConceptData reviewedRejectedAdditionStatus;

    private I_GetConceptData selectedConceptType;

    public SelectPromotionListPanel(I_GetConceptData promotionRefsetConcept) throws Exception {
        super();
        refsetHelper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
        this.promotionRefsetConcept = promotionRefsetConcept;
        termFactory = Terms.get();
        unreviewedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids());
        unreviewedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids());
        reviewedApprovedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.getUids());
        reviewedApprovedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.getUids());
        reviewedRejectedAdditionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.getUids());
        reviewedRejectedDeletionStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.getUids());
        init();
    }

    private void init() throws Exception {
        layoutComponents();
    }

    private void layoutComponents() throws Exception {
        boolean setDefaultSelection = false;

        this.setLayout(new GridBagLayout());
        int y = 0;

        options = new ButtonGroup();

        JRadioButton option1 = new JRadioButton(newAdditionsStr);
        option1.setActionCommand(newAdditionsStr);
        option1.addActionListener(this);
        options.add(option1);

        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()), unreviewedAdditionStatus)
            .size() == 0) {
            option1.setEnabled(false);
        } else {
            option1.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(unreviewedAdditionStatus);
                option1.setSelected(true);
            }
        }

        JRadioButton option2 = new JRadioButton(newDeletionsStr);
        option2.setActionCommand(newDeletionsStr);
        option2.addActionListener(this);
        options.add(option2);
        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()), unreviewedDeletionStatus)
            .size() == 0) {
            option2.setEnabled(false);
        } else {
            option2.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(unreviewedDeletionStatus);
                option2.setSelected(true);
            }
        }

        JRadioButton option3 = new JRadioButton(approvedAdditionsStr);
        option3.setActionCommand(approvedAdditionsStr);
        option3.addActionListener(this);
        options.add(option3);
        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()),
            reviewedApprovedAdditionStatus).size() == 0) {
            option3.setEnabled(false);
        } else {
            option3.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(reviewedApprovedAdditionStatus);
                option3.setSelected(true);
            }
        }

        JRadioButton option4 = new JRadioButton(approvedDeletionsStr);
        option4.setActionCommand(approvedDeletionsStr);
        option4.addActionListener(this);
        options.add(option4);
        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()),
            reviewedApprovedDeletionStatus).size() == 0) {
            option4.setEnabled(false);
        } else {
            option4.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(reviewedApprovedDeletionStatus);
                option4.setSelected(true);
            }
        }

        JRadioButton option5 = new JRadioButton(rejectedAdditionsStr);
        option5.setActionCommand(rejectedAdditionsStr);
        option5.addActionListener(this);
        options.add(option5);
        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()),
            reviewedRejectedAdditionStatus).size() == 0) {
            option5.setEnabled(false);
        } else {
            option5.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(reviewedRejectedAdditionStatus);
                option5.setSelected(true);
            }
        }

        JRadioButton option6 = new JRadioButton(rejectedDeletionsStr);
        option6.setActionCommand(rejectedDeletionsStr);
        option6.addActionListener(this);
        options.add(option6);
        if (refsetHelper.filterListByConceptType(
            termFactory.getRefsetExtensionMembers(promotionRefsetConcept.getConceptNid()),
            reviewedRejectedDeletionStatus).size() == 0) {
            option6.setEnabled(false);
        } else {
            option6.setEnabled(true);
            if (!setDefaultSelection) {
                setDefaultSelection = true;
                setSelectedPromotionStatus(reviewedRejectedDeletionStatus);
                option6.setSelected(true);
            }
        }

        if (setDefaultSelection) {

            Enumeration<AbstractButton> buttons = options.getElements();

            JLabel label = new JLabel("The following lists are available:");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y;
            gridBagConstraints.insets = new Insets(20, 5, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            this.add(label, gridBagConstraints);
            y++;

            while (buttons.hasMoreElements()) {
                AbstractButton button = buttons.nextElement();

                // add button to panel
                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = y;
                gridBagConstraints.insets = new Insets(0, 10, 10, 10); // padding
                gridBagConstraints.weighty = 0.0;
                gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
                this.add(button, gridBagConstraints);

                y++;
            }

            // column filler
            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 3;
            gridBagConstraints.gridy = y + 2;
            gridBagConstraints.weighty = 1.0;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            this.add(Box.createGlue(), gridBagConstraints);
        } else {
            JLabel label = new JLabel("The current refset has no members and therefore no promotion members.");
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y;
            gridBagConstraints.insets = new Insets(20, 5, 10, 10); // padding
            gridBagConstraints.weighty = 0.0;
            gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
            this.add(label, gridBagConstraints);
            JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
                "The current refset has no members. Please run the refset compute.", "", JOptionPane.ERROR_MESSAGE);
            // TODO
        }
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String actionCommand = e.getActionCommand();
            if (actionCommand.equals(newAdditionsStr)) {
                setSelectedPromotionStatus(unreviewedAdditionStatus);
            } else if (actionCommand.equals(newDeletionsStr)) {
                setSelectedPromotionStatus(unreviewedDeletionStatus);
            } else if (actionCommand.equals(approvedAdditionsStr)) {
                setSelectedPromotionStatus(reviewedApprovedAdditionStatus);
            } else if (actionCommand.equals(approvedDeletionsStr)) {
                setSelectedPromotionStatus(reviewedApprovedDeletionStatus);
            } else if (actionCommand.equals(rejectedAdditionsStr)) {
                setSelectedPromotionStatus(reviewedRejectedAdditionStatus);
            } else if (actionCommand.equals(rejectedDeletionsStr)) {
                setSelectedPromotionStatus(reviewedRejectedDeletionStatus);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setSelectedPromotionStatus(I_GetConceptData list) {
        selectedConceptType = list;
    }

    public I_GetConceptData getSelectedPromotionStatus() {
        return selectedConceptType;
    }
}
