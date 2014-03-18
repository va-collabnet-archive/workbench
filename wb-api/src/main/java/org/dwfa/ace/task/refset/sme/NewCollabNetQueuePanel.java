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
package org.dwfa.ace.task.refset.sme;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.dwfa.ace.api.I_GetConceptData;

/**
 * The panel prompts the user for info required to create a new CollabNet queue:
 * 1) select workbench user from dropdown list
 * 2) select user from dropdown list (collabnet collabnetUsers)
 * 3) enter collabnet password
 * 
 * @author Chrissy Hill
 * 
 */
public class NewCollabNetQueuePanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    // components
    private JLabel workbenchUserLabel;
    private JLabel collabnetUserLabel;
    private JLabel collabnetPasswordLabel;

    private JComboBox workbenchUserComboBox;
    private JComboBox collabnetUserComboBox;
    private JPasswordField passwordTextField;

    private TreeSet<I_GetConceptData> workbenchUsers;
    private TreeMap<String, String> collabnetUsers = new TreeMap<String, String>();

    public NewCollabNetQueuePanel(TreeSet<I_GetConceptData> workbenchUsers, TreeMap<String, String> collabnetUsers) {
        super();
        this.workbenchUsers = workbenchUsers;
        this.collabnetUsers = collabnetUsers;
        init();
    }

    private void init() {
        setDefaultValues();
        layoutComponents();
    }

    private void setDefaultValues() {

        // labels
        workbenchUserLabel = new JLabel("Workbench user (required):");
        collabnetUserLabel = new JLabel("CollabNet user (required):");
        collabnetPasswordLabel = new JLabel("CollabNet password (required):");

        // buttons and boxes
        workbenchUserComboBox = new JComboBox(workbenchUsers.toArray());
        collabnetUserComboBox = new JComboBox(collabnetUsers.keySet().toArray());

        // text fields
        passwordTextField = new JPasswordField();
    }

    private void layoutComponents() {

        this.setLayout(new GridBagLayout());
        this.removeAll();

        // workbench user
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(workbenchUserLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        if (workbenchUsers.size() == 0) {
            this.add(new JLabel("No available workbench users."), gbc);
        } else {
            this.add(workbenchUserComboBox, gbc);
        }

        // Collabnet users
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(collabnetUserLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(collabnetUserComboBox, gbc);

        // Collabnet password
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        //gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(collabnetPasswordLabel, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        //gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 0, 5);
        this.add(passwordTextField, gbc);

        this.validate();
    }

    public String getCollabnetUserPassword() {
        String result = String.valueOf(passwordTextField.getPassword());
        if (result == null) {
            return null;
        } else if (result.trim().equals("")) {
            return null;
        } else {
            return result;
        }
    }

    public String getCollabnetUserName() {
        if (collabnetUsers.size() == 0) {
            return null;
        } else {
            String selectedFullName = (String) collabnetUserComboBox.getSelectedItem();
            return collabnetUsers.get(selectedFullName);
        }
    }

    public I_GetConceptData getWorkbenchUser() {
        if (workbenchUsers.size() == 0) {
            return null;
        } else {
            return (I_GetConceptData) workbenchUserComboBox.getSelectedItem();
        }
    }

    public void focusOnWorkbenchUser() {
        workbenchUserComboBox.requestFocusInWindow();
    }
}
