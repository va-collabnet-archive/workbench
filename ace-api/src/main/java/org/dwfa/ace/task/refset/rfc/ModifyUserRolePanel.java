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
package org.dwfa.ace.task.refset.rfc;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.refset.spec.SpecRefsetHelper;
import org.dwfa.ace.task.util.DynamicWidthComboBox;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * The modify user role panel allows the user to select a user from the drop down list and remove their roles
 * as required.
 * 
 * @author Chrissy Hill
 * 
 */
public class ModifyUserRolePanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel userLabel;
    private JLabel roleLabel;
    private JLabel hierarchyLabel;
    private JButton removeRoleButton;
    // private JButton addRoleButton;
    // private JButton modifyRoleButton;
    private DynamicWidthComboBox userComboBox;
    private DynamicWidthComboBox roleComboBox;
    private DynamicWidthComboBox hierarchyComboBox;

    public ModifyUserRolePanel() {
        super();
        init();
    }

    private void init() {
        setDefaultValues();
        addListeners();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        userLabel = new JLabel("User:");
        hierarchyLabel = new JLabel("Hierarchy:");
        roleLabel = new JLabel("Role:");

        // buttons and boxes
        removeRoleButton = new JButton("Remove role");
        // addRoleButton = new JButton("Add new role");
        // modifyRoleButton = new JButton("Modify role");
        userComboBox = new DynamicWidthComboBox(getValidUsers().toArray());
        userComboBox.setMaximumSize(new Dimension(200, 25));
        userComboBox.setMinimumSize(new Dimension(200, 25));
        userComboBox.setPreferredSize(new Dimension(200, 25));
        hierarchyComboBox = new DynamicWidthComboBox(getValidHierarchies().toArray());
        hierarchyComboBox.setMaximumSize(new Dimension(200, 25));
        hierarchyComboBox.setMinimumSize(new Dimension(200, 25));
        hierarchyComboBox.setPreferredSize(new Dimension(200, 25));
        roleComboBox = new DynamicWidthComboBox(getValidRoles().toArray());
        roleComboBox.setMaximumSize(new Dimension(200, 25));
        roleComboBox.setMinimumSize(new Dimension(200, 25));
        roleComboBox.setPreferredSize(new Dimension(200, 25));

    }

    private void addListeners() {
        removeRoleButton.addActionListener(new RemoveRoleButtonListener());
        // addRoleButton.addActionListener(new AddRoleButtonListener());
        // .addActionListener(new ModifyRoleButtonListener());
        userComboBox.addActionListener(new UserComboBoxListener());
        hierarchyComboBox.addActionListener(new HierarchyComboBoxListener());
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // user label & box
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(userLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        if (userComboBox.getItemCount() == 0) {
            this.add(new JLabel("No available users."), gridBagConstraints);
        } else {
            this.add(userComboBox, gridBagConstraints);
        }

        // hierarchy label & box
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(hierarchyLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        try {
            hierarchyComboBox = new DynamicWidthComboBox(getValidHierarchies().toArray());
        } catch (Exception e) {
            hierarchyComboBox = new DynamicWidthComboBox();
            e.printStackTrace();
        }
        hierarchyComboBox.setMaximumSize(new Dimension(200, 25));
        hierarchyComboBox.setMinimumSize(new Dimension(200, 25));
        hierarchyComboBox.setPreferredSize(new Dimension(200, 25));
        this.add(hierarchyComboBox, gridBagConstraints);

        // role label & box
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.FIRST_LINE_START;
        this.add(roleLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(10, 10, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        try {
            roleComboBox = new DynamicWidthComboBox(getValidRoles().toArray());
        } catch (Exception e) {
            roleComboBox = new DynamicWidthComboBox();
            e.printStackTrace();
        }
        roleComboBox.setMaximumSize(new Dimension(200, 25));
        roleComboBox.setMinimumSize(new Dimension(200, 25));
        roleComboBox.setPreferredSize(new Dimension(200, 25));
        this.add(roleComboBox, gridBagConstraints);

        // buttons
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
        gridBagConstraints.weighty = 0.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_START;
        this.add(removeRoleButton, gridBagConstraints);

        /*
         * gridBagConstraints = new GridBagConstraints();
         * gridBagConstraints.gridx = 1;
         * gridBagConstraints.gridy = 3;
         * gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
         * gridBagConstraints.weighty = 0.0;
         * gridBagConstraints.anchor = GridBagConstraints.LINE_START;
         * this.add(addRoleButton, gridBagConstraints);
         * gridBagConstraints = new GridBagConstraints();
         * gridBagConstraints.gridx = 2;
         * gridBagConstraints.gridy = 3;
         * gridBagConstraints.insets = new Insets(0, 5, 10, 10); // padding
         * gridBagConstraints.weighty = 0.0;
         * gridBagConstraints.anchor = GridBagConstraints.LINE_START;
         * this.add(modifyRoleButton, gridBagConstraints);
         */

        // column filler
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.anchor = GridBagConstraints.LINE_END;
        this.add(Box.createGlue(), gridBagConstraints);

        I_GetConceptData currentUser = (I_GetConceptData) userComboBox.getSelectedItem();
        I_GetConceptData currentHierarchy = (I_GetConceptData) hierarchyComboBox.getSelectedItem();
        I_GetConceptData currentRole = (I_GetConceptData) roleComboBox.getSelectedItem();
        if (currentUser == null || currentHierarchy == null || currentRole == null) {
            removeRoleButton.setEnabled(false);
        } else {
            removeRoleButton.setEnabled(true);
        }
        // modifyRoleButton.setEnabled(false); // TODO

        this.repaint();
        this.revalidate();

    }

    /**
     * Calculates a set of valid users - a user is valid is they are a child of the User concept in the top hierarchy,
     * and have a description of type "user inbox".
     * 
     * @return The set of valid users.
     */
    private Set<I_GetConceptData> getValidUsers() {
        HashSet<I_GetConceptData> validUsers = new HashSet<I_GetConceptData>();
        try {
            I_GetConceptData userParent =
                    LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_IntSet allowedTypes = LocalVersionedTerminology.get().newIntSet();
            allowedTypes.add(LocalVersionedTerminology.get().getConcept(
                ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());
            SpecRefsetHelper helper = new SpecRefsetHelper();
            Set<Integer> currentStatuses = helper.getCurrentStatusIds();

            Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(allowedTypes, true, true);
            I_GetConceptData descriptionType =
                    LocalVersionedTerminology.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
            I_IntSet descAllowedTypes = LocalVersionedTerminology.get().newIntSet();
            descAllowedTypes.add(descriptionType.getConceptId());
            Set<I_Position> positions = getPositions(LocalVersionedTerminology.get());

            for (I_GetConceptData user : allUsers) {

                I_DescriptionTuple latestTuple = null;
                int latestVersion = Integer.MIN_VALUE;

                List<? extends I_DescriptionTuple> descriptionResults =
                        user.getDescriptionTuples(null, descAllowedTypes, LocalVersionedTerminology.get()
                            .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true);
                for (I_DescriptionTuple descriptionTuple : descriptionResults) {

                    if (descriptionTuple.getVersion() > latestVersion) {
                        latestVersion = descriptionTuple.getVersion();
                        latestTuple = descriptionTuple;
                    }
                }
                if (latestTuple != null) {
                    for (int currentStatusId : currentStatuses) {
                        if (latestTuple.getStatusId() == currentStatusId) {
                            validUsers.add(user);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return validUsers;
    }

    private Set<I_GetConceptData> getValidHierarchies() {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData currentUser = (I_GetConceptData) userComboBox.getSelectedItem();
        HashSet<I_GetConceptData> hierarchies = new HashSet<I_GetConceptData>();
        I_IntSet roleAllowedTypes = termFactory.newIntSet();

        try {
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatus = helper.getCurrentStatusIntSet();

            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.OWNER_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ADMIN_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.AUTHOR_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.SME_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWER_ROLE.getUids())
                .getConceptId());

            Set<I_Position> positions = getPositions(termFactory);

            List<? extends I_RelVersioned> roleRels = currentUser.getSourceRels();
            // (null, roleAllowedTypes, positions, true, true);

            for (I_RelVersioned versioned : roleRels) {
                // I_RelVersioned versioned = roleRel.getFixedPart();

                if (versioned.getLastTuple().getStatusId() != ArchitectonicAuxiliary.Concept.RETIRED.localize()
                    .getNid()) {
                    if (currentStatus.contains(versioned.getLastTuple().getStatusId())) {
                        if (roleAllowedTypes.contains(versioned.getLastTuple().getTypeId())) {
                            hierarchies.add(termFactory.getConcept(versioned.getC2Id()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hierarchies;
    }

    private Set<I_GetConceptData> getValidRoles() {
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        I_GetConceptData currentUser = (I_GetConceptData) userComboBox.getSelectedItem();
        I_GetConceptData currentHierarchy = (I_GetConceptData) hierarchyComboBox.getSelectedItem();
        HashSet<I_GetConceptData> roles = new HashSet<I_GetConceptData>();
        I_IntSet roleAllowedTypes = termFactory.newIntSet();

        try {
            SpecRefsetHelper helper = new SpecRefsetHelper();
            I_IntSet currentStatus = helper.getCurrentStatusIntSet();
            Set<I_Position> positions = getPositions(termFactory);

            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.OWNER_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.ADMIN_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.AUTHOR_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.SME_ROLE.getUids())
                .getConceptId());
            roleAllowedTypes.add(termFactory.getConcept(ArchitectonicAuxiliary.Concept.REVIEWER_ROLE.getUids())
                .getConceptId());

            List<? extends I_RelVersioned> roleRels = currentUser.getSourceRels();// (null, roleAllowedTypes, positions,
            // true,
            // true);

            for (I_RelVersioned roleRel : roleRels) {
                if (roleRel.getLastTuple().getStatusId() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()) {
                    if (currentStatus.contains(roleRel.getLastTuple().getStatusId())) {
                        if (roleAllowedTypes.contains(roleRel.getLastTuple().getTypeId())) {
                            if (currentHierarchy.getConceptId() == roleRel.getC2Id()) {
                                roles.add(termFactory.getConcept(roleRel.getLastTuple().getTypeId()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return roles;
    }

    public Set<I_Position> getPositions(I_TermFactory termFactory) throws Exception {
        I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();
        Set<I_Path> editingPaths = activeProfile.getEditingPathSet();
        Set<I_Position> allPositions = new HashSet<I_Position>();
        for (I_Path path : editingPaths) {
            allPositions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
            for (I_Position position : path.getOrigins()) {
                addOriginPositions(termFactory, position, allPositions);
            }
        }
        return allPositions;
    }

    private void addOriginPositions(I_TermFactory termFactory, I_Position position, Set<I_Position> allPositions) {
        allPositions.add(position);
        for (I_Position originPosition : position.getPath().getOrigins()) {
            addOriginPositions(termFactory, originPosition, allPositions);
        }
    }

    public class UserComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            layoutComponents();
        }
    }

    public class HierarchyComboBoxListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            layoutComponents();
        }
    }

    public class RemoveRoleButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent arg0) {
            try {
                I_TermFactory termFactory = LocalVersionedTerminology.get();
                I_GetConceptData currentUser = (I_GetConceptData) userComboBox.getSelectedItem();
                I_GetConceptData currentHierarchy = (I_GetConceptData) hierarchyComboBox.getSelectedItem();
                I_GetConceptData currentRole = (I_GetConceptData) roleComboBox.getSelectedItem();
                if (currentUser != null && currentHierarchy != null && currentRole != null) {
                    // get associated relationship
                    I_IntSet roleAllowedTypes = termFactory.newIntSet();
                    roleAllowedTypes.add(currentRole.getConceptId());
                    SpecRefsetHelper helper = new SpecRefsetHelper();
                    I_IntSet currentStatus = helper.getCurrentStatusIntSet();

                    List<? extends I_RelTuple> roleRels =
                            currentUser.getSourceRelTuples(currentStatus, roleAllowedTypes, termFactory
                                .getActiveAceFrameConfig().getViewPositionSetReadOnly(), true, true);

                    for (I_RelTuple roleRel : roleRels) {
                        if (currentHierarchy.getConceptId() == roleRel.getC2Id()) {
                            // retire this rel
                            I_RelVersioned relVersioned = roleRel.getFixedPart();

                            for (I_Path editPath : termFactory.getActiveAceFrameConfig().getEditingPathSet()) {
                                I_RelPart newPart = relVersioned.getLastTuple().getMutableIdPart().duplicate();
                                if (newPart.getStatusId() != ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()) {
                                    newPart.setStatusId(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
                                    newPart.setVersion(Integer.MAX_VALUE);
                                    newPart.setPathId(editPath.getConceptId());
                                    relVersioned.addVersion(newPart);
                                }
                            }
                        }
                    }
                }
                termFactory.addUncommittedNoChecks(currentUser);
                termFactory.commit();
                I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
                Set<I_Position> viewPositionSet = config.getViewPositionSet();
                PathSetReadOnly promotionPaths = new PathSetReadOnly(config.getPromotionPathSet());
                if (viewPositionSet.size() != 1 || promotionPaths.size() != 1) {
                    throw new TaskFailedException(
                        "There must be only one view position, and one promotion path: viewPaths " + viewPositionSet
                            + " promotionPaths: " + promotionPaths);
                }
                I_Position viewPosition = viewPositionSet.iterator().next();
               currentUser.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
            		   config.getAllowedStatus());
                termFactory.addUncommittedNoChecks(currentUser);
                termFactory.commit();

                // refresh panel display
                layoutComponents();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class ModifyRoleButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO Auto-generated method stub
        }
    }

    public class AddRoleButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            // TODO
        }
    }

}