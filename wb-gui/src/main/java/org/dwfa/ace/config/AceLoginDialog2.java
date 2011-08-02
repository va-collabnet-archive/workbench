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
import java.util.logging.Level;

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
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
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
    
	//Store UN etc in the SVN Prompter
	public SvnPrompter prompt = new SvnPrompter();

    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    private JComponent userControl;
    private javax.swing.JCheckBox svnConnectCheckBox;
    private Hashtable<String,File> nameProf = new Hashtable<String,File>();
    private String title = "";
    
    public String svnUrl;

    public AceLoginDialog2(JFrame topFrame,Hashtable<String,File> np) {
    	this.nameProf = np;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
    }
    
    public AceLoginDialog2(JFrame topFrame) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
    }
    
    private String getDefUser(){
    	 String defUN = System.getProperty("user.name");
    	 if(defUN == null){
    		 defUN = ""; 
    	 }
    	 
    	 return defUN;
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
    	int pnum = nameProf.size();
    	
    	if(pnum == 0){
    		//no profiles 
    		title = "No Profiles found. Download them from subversion?";
    		JTextField jtf = new JTextField();
    		jtf.setText(getDefUser());
    		userControl = jtf;
    		
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
            profileSelectionBox.setSelectedItem(getDefUser());
            profileSelectionBox.setRenderer(new ProfileRenderer());
            userControl = profileSelectionBox;
            return userControl;
    	}
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
        setTitle(getTitle());
        setResizable(false);
        setModal(true);

        //profileSelectionBox.setBorder(null);

        svnConnectCheckBox.setSelected(true);
        if(nameProf.size() == 0){
        	svnConnectCheckBox.setEnabled(false);
        }
        svnConnectCheckBox.setText("Connect to network resources");
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
    	//AceLog.getAppLog().info("loginButtonActionPerformed UN = "+un);
    	//if the password field is enabled get the password
    	String pw = null;
    	if(passwordField.isEnabled()){
    		pw = new String(passwordField.getPassword());
    		//AceLog.getAppLog().info("loginButtonActionPerformed pw = "+pw);
    		boolean ok  = checkSVN(getSvnUrl(),un,pw);	
    		if(ok){
    			this.prompt.setUsername(un);
    			this.prompt.setPassword(pw);
    			this.dispose();
    		}
    	}
    	if(!passwordField.isEnabled()){
    		this.prompt.setUsername(un);
			this.prompt.setPassword(null);
			//getProfile();
			this.dispose();
    	}
    }
    
    
    private boolean checkSVN(String url, String uname, String pw){
    	boolean ok = true;
    	SVNRepository repo= null;
    	 try { 
    		 //AceLog.getAppLog().info("checkSVN url = "+url +" pw = "+pw);  
    		 DAVRepositoryFactory.setup();
    	     SVNRepositoryFactoryImpl.setup();
    	     FSRepositoryFactory.setup();
    	     repo = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(url));
    	     //AceLog.getAppLog().info("checkSVN OK1");
    	     ISVNAuthenticationManager authManager = 
    	                  SVNWCUtil.createDefaultAuthenticationManager(uname, pw);
    	     //AceLog.getAppLog().info("checkSVN OK2");
    	     repo.setAuthenticationManager(authManager);
    	     repo.testConnection();
    	     
    	 }catch (SVNAuthenticationException svnAEx){
    		 ok = false;
    		 AceLog.getAppLog().alertAndLog(Level.SEVERE, "Authorization failed please check Username and Password", svnAEx);	 
    	 }
    	 catch (SVNException e){
    		 ok = false;
    	     AceLog.getAppLog().alertAndLog(Level.SEVERE, "Failed to connect to to repository. \n Please check network and URL. Currently trying to use: \n "+url+" \n Error = "+e.getMessage(), e);
    	 }

    	return ok;
    	
    }

    /**
     * Set the profile file and closes the dialog.
     * 
     * @param profile
     */
    /*private void loginButtonActionPerformed(File profile) {
        if (profile != null) {
            setProfile(profile);
        }
        dispose();
    }*/

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

    @Override
    public void actionPerformed(ActionEvent e) {
    	AceLog.getAppLog().info("actionPerformed connectToSvn = "+connectToSvn());
    	passwordField.setEnabled(connectToSvn());
    	passwordField.setVisible(connectToSvn());
    	passwordLabel.setVisible(connectToSvn());
        Svn.setConnectedToSvn(connectToSvn());
    }

	public String getSvnUrl() {
		return svnUrl;
	}

	public void setSvnUrl(String svnUrl) {
		this.svnUrl = svnUrl;
	}
	
    public String getTitle() {
    	if(title == null || title.length() == 0){
    		title = "Welcome to the Workbench";
    	}
    	return title;
        }

	public SvnPrompter getPrompt() {
		return prompt;
	}

	public void setPrompt(SvnPrompter prompt) {
		this.prompt = prompt;
	}

	public Hashtable<String, File> getNameProf() {
		return nameProf;
	}

	public void setNameProf(Hashtable<String, File> nameProf) {
		this.nameProf = nameProf;
	}
}
