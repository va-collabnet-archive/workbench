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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.util.DynamicWidthComboBox;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 * The select user panel allows the user to select a particular user from the
 * drop down list.
 * 
 * @author Chrissy Hill
 * 
 */
public class SelectUserPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel userLabel;

    private DynamicWidthComboBox userComboBox;

    public SelectUserPanel() {
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

        // buttons and boxes
        userComboBox = new DynamicWidthComboBox(getValidUsers().toArray());
        userComboBox.setMaximumSize(new Dimension(200, 25));
        userComboBox.setMinimumSize(new Dimension(200, 25));
        userComboBox.setPreferredSize(new Dimension(200, 25));
    }

    private void addListeners() {
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

        // column filler
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
     * Calculates a set of valid users - a user is valid is they are a child of
     * the User concept in the top hierarchy,
     * and have a description of type "user inbox".
     * 
     * @return The set of valid users.
     */
    private TreeSet<I_GetConceptData> getValidUsers() {
        TreeSet<I_GetConceptData> validUsers = new TreeSet<I_GetConceptData>();
        try {
            I_GetConceptData userParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());

            I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
            I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
            Set<Integer> currentStatuses = helper.getCurrentStatusIds();

            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(allowedTypes);
            I_GetConceptData descriptionType =
                    Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
            I_IntSet descAllowedTypes = Terms.get().newIntSet();
            descAllowedTypes.add(descriptionType.getConceptNid());

            for (I_GetConceptData user : allUsers) {

                I_DescriptionTuple latestTuple = null;
                int latestVersion = Integer.MIN_VALUE;

                List<? extends I_DescriptionTuple> descriptionResults =
                        user.getDescriptionTuples(null, descAllowedTypes, Terms.get().getActiveAceFrameConfig()
                            .getViewPositionSetReadOnly(), config.getPrecedence(), config
                            .getConflictResolutionStrategy());
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

    public Set<PositionBI> getPositions(I_TermFactory termFactory) throws Exception {
        I_ConfigAceFrame activeProfile = termFactory.getActiveAceFrameConfig();
        Set<PathBI> editingPaths = activeProfile.getEditingPathSet();
        Set<PositionBI> allPositions = new HashSet<PositionBI>();
        for (PathBI path : editingPaths) {
            allPositions.add(termFactory.newPosition(path, Integer.MAX_VALUE));
            for (PositionBI position : path.getOrigins()) {
                addOriginPositions(termFactory, position, allPositions);
            }
        }
        return allPositions;
    }

    private void addOriginPositions(I_TermFactory termFactory, PositionBI position, Set<PositionBI> allPositions) {
        allPositions.add(position);
        for (PositionBI originPosition : position.getPath().getOrigins()) {
            addOriginPositions(termFactory, originPosition, allPositions);
        }
    }

    public I_GetConceptData getSelectedUser() {
        return (I_GetConceptData) userComboBox.getSelectedItem();
    }

}