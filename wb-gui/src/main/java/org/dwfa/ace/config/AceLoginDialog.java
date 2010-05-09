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
package org.dwfa.ace.config;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.svn.Svn;

/**
 * Login Dialog for ace.
 * 
 * Captures use subversion flag, profile and users password.
 * 
 * @author steve crow, ean dungey, keith campbell
 */
public class AceLoginDialog extends javax.swing.JDialog implements ActionListener {
    private static final long serialVersionUID = -4458854470566944865L;
    private File profile;

    public AceLoginDialog(JFrame topFrame) {

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());

        userLabel = new javax.swing.JLabel("User:", SwingConstants.RIGHT);
        passwordLabel = new javax.swing.JLabel("Password:", SwingConstants.RIGHT);
        passwordField = new javax.swing.JPasswordField();
        passwordField.setText("");
        passwordField.setColumns(20);
        profileSelectionBox = new javax.swing.JComboBox();
        svnConnectCheckBox = new javax.swing.JCheckBox();

        cancelButton = new javax.swing.JButton();
        loginButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Login");
        setResizable(false);
        setModal(true);

        profileSelectionBox.setBorder(null);

        svnConnectCheckBox.setSelected(true);
        svnConnectCheckBox.setText("Connect to subversion");
        svnConnectCheckBox.addActionListener(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;

        content.add(userLabel, gbc);
        gbc.gridx++;
        gbc.gridwidth = 3;
        content.add(profileSelectionBox, gbc);
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        content.add(passwordLabel, gbc);
        gbc.gridwidth = 3;
        gbc.gridx++;
        content.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy++;

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        loginButton.setText("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginButtonActionPerformed((File) profileSelectionBox.getSelectedItem());
            }
        });

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        content.add(svnConnectCheckBox, gbc);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        content.add(new JPanel(), gbc);
        gbc.gridx++;
        content.add(new JPanel(), gbc);
        gbc.gridx++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        content.add(cancelButton, gbc);
        gbc.gridx++;
        content.add(loginButton, gbc);
        if (JComponent.class.isAssignableFrom(content.getClass())) {
            JComponent jc = (JComponent) content;
            jc.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        } else {
            AceLog.getAppLog().info("Container class is: " + content.getClass());
        }

        getRootPane().setDefaultButton(loginButton);
        profileSelectionBox.requestFocusInWindow();
        pack();
    }

    /**
     * Close this dialog.
     * 
     * @param evt ActionEvent
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
    }

    /**
     * Set the profile file and closes the dialog.
     * 
     * @param profile
     */
    private void loginButtonActionPerformed(File profile) {
        if (profile != null) {
            setProfile(profile);
        }
        dispose();
    }

    private static class ProfileRenderer extends DefaultListCellRenderer {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) {
                setText("");
            } else if (File.class.isAssignableFrom(value.getClass())) {
                File f = (File) value;
                setText(f.getName());
            }

            return this;
        }

    }

    /**
     * Sets the profile dir and returns the
     * 
     * @param profileDirToSet the last profile used.
     * @return File
     * @throws TaskFailedException if there is no profile set. NB
     *             profileDirToSet can be null.
     */
    public File getUserProfile(File profileDirToSet) throws TaskFailedException {
        if (profileDirToSet != null) {
            List<File> profiles = new ArrayList<File>();
            getProfiles(profiles, new File("profiles"));
            profileSelectionBox.setModel(new DefaultComboBoxModel(profiles.toArray()));
            profileSelectionBox.validate();
            profileSelectionBox.setSelectedItem(profileDirToSet);
            profileSelectionBox.setRenderer(new ProfileRenderer());
        }

        setVisible(true);

        return getProfile();
    }

    private void getProfiles(List<File> profiles, File dir) {
        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    getProfiles(profiles, f);
                } else if (f.getName().toLowerCase().endsWith(".ace")) {
                    profiles.add(f);
                }
            }
        }
    }

    /**
     * Entered password.
     * 
     * @return String
     */
    public char[] getPassword() {
        return passwordField.getPassword();
    }

    /**
     * True if the user is connecting to SVN
     * 
     * @return boolean
     */
    public boolean connectToSvn() {
        return svnConnectCheckBox.getModel().isSelected();
    }

    /**
     * Get the profile file to use.
     * 
     * @return File
     * @throws TaskFailedException if the profile is null.
     */
    private File getProfile() throws TaskFailedException {
        if (profile == null) {
            throw new TaskFailedException("No Profile selected.");
        }
        return profile;
    }

    private void setProfile(File profileToSet) {
        this.profile = profileToSet;
    }

    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JComboBox profileSelectionBox;
    private javax.swing.JCheckBox svnConnectCheckBox;

    @Override
    public void actionPerformed(ActionEvent e) {
        Svn.setConnectedToSvn(connectToSvn());
    }
}
