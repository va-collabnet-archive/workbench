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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;

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
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

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
    private JComponent userControl;
    //private javax.swing.JComboBox profileSelectionBox;
    private javax.swing.JCheckBox svnConnectCheckBox;

    public String profileDirName = "profiles";
    public String profileFileEnding= ".ace";
    //private List<File> profiles = new ArrayList<File>();
    private Hashtable<String,File> nameProf = new Hashtable<String,File>();
    private String title = "";

    public AceLoginDialog2(JFrame topFrame) {

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setProfiles();
        initComponents();
    }
    
    private String getUserValue(){
    	String uv = "";
    	if (userControl instanceof JTextField){
    		uv = ((JTextField) userControl).getText();
    	}
    	if (userControl instanceof JComboBox){
    		uv = (String)((JComboBox) userControl).getSelectedItem();
    	}
    	
    	return uv;
    }
    
    private JComponent getUserControl(){
    	//JComponent retval = null;
    	int pnum = nameProf.size();
    	
    	if(pnum == 0){
    		//no profiles 
    		title = "No Profiles found. Download them from subversion?";
    		userControl = new JTextField();
    		return userControl;
    	}
    	if(pnum == 1){
    		title = "Please login";
    		JTextField jtf = new JTextField();
    		jtf.setText(nameProf.keys().nextElement());
    		jtf.setEditable(false);
    		userControl = jtf;
    		return userControl;
    	}
    	else{
    		title = "Please choose a user and login";
    		JComboBox profileSelectionBox = new JComboBox();
    		profileSelectionBox.setModel(new DefaultComboBoxModel(nameProf.keySet().toArray()));
            profileSelectionBox.validate();
            profileSelectionBox.setSelectedItem(nameProf.keys().nextElement());
            profileSelectionBox.setRenderer(new ProfileRenderer());
            userControl = profileSelectionBox;
            return userControl;
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
        if(nameProf.size() == 0){
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
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loginButtonActionPerformed(e);
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
        
    	for(Frame f:Frame.getFrames()){
    		f.dispose();
    	}

    	this.dispose();
    	System.exit(0);
    }
    
    /**
     * Login Action
     * 
     * @param evt ActionEvent
     */
    private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {
        
    	
    	// get the username
    	String un = getUserValue();
    	
    	AceLog.getAppLog().info("loginButtonActionPerformed UN = "+un);
    
    	//if the password field is enabled get the password
    	String pw = null;
    	if(passwordField.isEnabled()){
    		pw = new String(passwordField.getPassword());
    		AceLog.getAppLog().info("loginButtonActionPerformed pw = "+pw);
    		String em = checkSVN("",un,pw);	
    		
    	}
    	
    	    	
    	//if No profiles try to check out from SVN and then reload.
    	
    	
    	//If Profiles found use the UserName that to get the profile

    	
    	//try to 
    	
    	
    	
    	
    }
    
    
    private String checkSVN(String url, String uname, String pw){
    	
    	SVNRepository repo= null;
    	 try { 
    	     repo = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));
    	     ISVNAuthenticationManager authManager = 
    	                  SVNWCUtil.createDefaultAuthenticationManager(uname, pw);
    	     repo.setAuthenticationManager(authManager);
    	     repo.testConnection();
    	 } catch (SVNException e){
    	     e.printStackTrace();
    	     
    	 }

    	
    	
    	
    	
    	String err_msg = null;
		//try to see if the login credentials work with svn
		
		
		// see that there is a svn repo defined
		
		
		// then see if you can reach it
		
		
		// then see if the creds work
    	
    	
    	return err_msg;
    	
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
                	
                	nameProf.put(getUserNameFromProfileFN(f.getName()), f);
                    //profiles.add(f);
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
    	AceLog.getAppLog().info("actionPerformed connectToSvn = "+connectToSvn());
    	passwordField.setEnabled(connectToSvn());
    	passwordField.setVisible(connectToSvn());
    	passwordLabel.setVisible(connectToSvn());
        Svn.setConnectedToSvn(connectToSvn());
    }
}
