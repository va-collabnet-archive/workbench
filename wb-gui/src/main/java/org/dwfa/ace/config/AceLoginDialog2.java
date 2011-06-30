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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.svn.Svn;

/**
 * Login Dialog for ace.
 * 
 * Captures use subversion flag, profile and users password.
 * Amended from AceLoginDialog
 * Controls the login process. 
 * If connecting to svn then password else just user name
 * If no profiles found then forces the user to download then or hook out.
 * @author steve crow, ean dungey, keith campbell, adam flinton
 */
public class AceLoginDialog2 extends javax.swing.JDialog implements ActionListener {
    private static final long serialVersionUID = -4458854470566944865L;
    private File profile;
	//Store UN etc in the SVN Prompter
	public SvnPrompter prompt = new SvnPrompter();
	

    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    //private javax.swing.JComboBox profileSelectionBox;
    private javax.swing.JCheckBox svnConnectCheckBox;

    public String profileDirName = "profiles";
    public String profileFileEnding= ".ace";
    private List<File> profiles = new ArrayList<File>();
    private String title = "";

    public AceLoginDialog2(JFrame topFrame) {

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setProfiles();
        initComponents();
    }
    
    private JComponent getUserControl(){
    	//JComponent retval = null;
    	int pnum = profiles.size();
    	
    	if(pnum == 0){
    		//no profiles 
    		title = "No Profiles found. Download them from subversion?";
    		JTextField jtf = new JTextField();
    		return jtf;
    	}
    	if(pnum == 1){
    		JTextField jtf = new JTextField();
    		jtf.setText(getUserNameFromProfileFN(profiles.get(0).getName()));
    		jtf.setEditable(false);
    		return jtf;
    	}
    	else{
    		JComboBox profileSelectionBox = new JComboBox();
    		profileSelectionBox.setModel(new DefaultComboBoxModel(profiles.toArray()));
            profileSelectionBox.validate();
            //profileSelectionBox.setSelectedItem(profileDirToSet);
            profileSelectionBox.setRenderer(new ProfileRenderer());
            return profileSelectionBox;
    	}
    	
    	//return retval;
    }
    
    private String getUserNameFromProfileFN(String proFN){
    	int i = proFN.indexOf(profileFileEnding);
    	return proFN.substring(0,i);
    	
    	
    }

    private void initComponents() {

        Container content = getContentPane();
        content.setLayout(new GridBagLayout());

        userLabel = new javax.swing.JLabel("User:", SwingConstants.RIGHT);
        passwordLabel = new javax.swing.JLabel("Password:", SwingConstants.RIGHT);
        passwordField = new javax.swing.JPasswordField();
        passwordField.setText("");
        passwordField.setColumns(20);
        //profileSelectionBox = new javax.swing.JComboBox();
        svnConnectCheckBox = new javax.swing.JCheckBox();

        cancelButton = new javax.swing.JButton();
        loginButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(title);
        setResizable(false);
        setModal(true);

        //profileSelectionBox.setBorder(null);

        svnConnectCheckBox.setSelected(true);
        if(profiles.size() == 0){
        	svnConnectCheckBox.setEnabled(false);
        }
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
        //content.add(profileSelectionBox, gbc);
        content.add(getUserControl(), gbc);
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
        /*loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginButtonActionPerformed((File) profileSelectionBox.getSelectedItem());
            }
        });*/

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
        content.add(loginButton, gbc);
        gbc.gridx++;
        content.add(cancelButton, gbc);
        if (JComponent.class.isAssignableFrom(content.getClass())) {
            JComponent jc = (JComponent) content;
            jc.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        } else {
            AceLog.getAppLog().info("Container class is: " + content.getClass());
        }

        getRootPane().setDefaultButton(loginButton);
        //profileSelectionBox.requestFocusInWindow();
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
    public File getUserProfile() throws TaskFailedException {
        //if (profileDirToSet != null) {
            //List<File> profiles = new ArrayList<File>();
            getProfiles(new File(profileDirName));
           /* if(profiles.size() > 1){
            	setProfileSelectionBox();
            }*/
            
       // }

        //setVisible(true);

        return getProfile();
    }
    
    public void setProfiles(){
    	getProfiles(new File(profileDirName));
        /*if(profiles.size() > 1){
        	setProfileSelectionBox();
        }*/
    }
   
    
   /*private void setProfileSelectionBox(){
    	profileSelectionBox.setModel(new DefaultComboBoxModel(profiles.toArray()));
        profileSelectionBox.validate();
        //profileSelectionBox.setSelectedItem(profileDirToSet);
        profileSelectionBox.setRenderer(new ProfileRenderer());
    } */
    

    private void getProfiles(File dir) {
        if (dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    getProfiles(f);
                } else if (f.getName().toLowerCase().endsWith(profileFileEnding)) {
                    profiles.add(f);
                    AceLog.getAppLog().info("getProfiles adding "+f.getName() + " user name = "+getUserNameFromProfileFN(f.getName()));
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

    public void setConnectToSvn(boolean connect) {
        svnConnectCheckBox.setSelected(connect);
        Svn.setConnectedToSvn(connect);
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

    @Override
    public void actionPerformed(ActionEvent e) {
        Svn.setConnectedToSvn(connectToSvn());
    }
}
