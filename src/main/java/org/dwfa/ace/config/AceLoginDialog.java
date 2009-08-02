package org.dwfa.ace.config;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationException;

import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.fd.FileDialogUtil;
import org.tigris.subversion.javahl.ClientException;

/**
 * Login Dialog for ace.
 * 
 * Captures use subversion flag, profile and users password.
 *  
 * @author steve crow, ean dungey, keith campbell
 */
public class AceLoginDialog extends javax.swing.JDialog  {
	private static final long serialVersionUID = -4458854470566944865L;
	private File profile;
	private File profileDir;
	private FilenameFilter profileFileFilter;
	private Configuration jiniConfig;
	private Properties aceProperties;
	private transient JFrame parentFrame;
	
    public AceLoginDialog(Properties acePropertiesToSet, Configuration jiniConfigToSet, JFrame topFrame) {
    	
    	setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        initComponents();
        
        aceProperties = acePropertiesToSet;
        jiniConfig = jiniConfigToSet;
        profileFileFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ace");
			}
		};
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
        selectProfileButton = new javax.swing.JButton();
        svnConnectCheckBox = new javax.swing.JCheckBox();
        
        cancelButton = new javax.swing.JButton();
        loginButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("ACE User Login");
        setResizable(false);
        setModal(true);


        profileSelectionBox.setBorder(null);
        
        selectProfileButton.setText("Other...");
        selectProfileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectProfileButtonActionPerformed(evt);
            }
        });

        svnConnectCheckBox.setSelected(true);
        svnConnectCheckBox.setText("Connect to subversion");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        
        content.add(userLabel, gbc);
        gbc.gridx++;
        content.add(profileSelectionBox, gbc);
        gbc.gridx++;
        content.add(selectProfileButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        content.add(passwordLabel, gbc);
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
				loginButtonActionPerformed((String) profileSelectionBox.getSelectedItem());
			}
		});

        gbc.fill = GridBagConstraints.NONE;
        gbc.gridy++;
        gbc.gridx++;
        content.add(svnConnectCheckBox, gbc);
        gbc.gridx++;
        content.add(cancelButton, gbc);
        gbc.gridx++;
        content.add(loginButton, gbc);
        
        pack();
    }

    /**
     * Pops a FileDialogUtil dialog allowing the user to navigate to a profile file.
     * 
     * @param evt ActionEvent
     */
    private void selectProfileButtonActionPerformed(java.awt.event.ActionEvent evt) {
		try {

			if (!Boolean.parseBoolean((String) aceProperties.get("initialized"))) {
				new AceSvn(AceRunner.class, jiniConfig).handleSvnProfileCheckout(aceProperties);
			}

			parentFrame = new JFrame();
			boolean newFrame = true;
			if (OpenFrames.getNumOfFrames() > 0) {
				newFrame = false;
				parentFrame = OpenFrames.getFrames().iterator().next();
				AceLog.getAppLog().info("### Using an existing frame LD");
			} else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						public void run() {
							parentFrame.setContentPane(new JLabel("The Terminology IDE is starting..."));
							parentFrame.pack();
							Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
							parentFrame.setLocation(d.width/2, d.height/2);
							parentFrame.setVisible(true);
							OpenFrames.addFrame(parentFrame);
							AceLog.getAppLog().info("### Adding a new frame LD");
						}
						
					});
				} catch (InterruptedException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (InvocationTargetException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			
			
			setProfile(FileDialogUtil.getExistingFile(
					"Please select your user profile:", profileFileFilter,
					profileDir, parentFrame));

			if (newFrame) {
				OpenFrames.removeFrame(parentFrame);
				parentFrame.setVisible(false);
			}

			profileSelectionBox.setModel(new javax.swing.DefaultComboBoxModel(
					profileDir.list(profileFileFilter)));
			profileSelectionBox.validate();

		} catch (TaskFailedException e) {
			throw new RuntimeException(e);
		} catch (ClientException e) {
			throw new RuntimeException(e);
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Close this dialog.
     * 
     * @param evt ActionEvent
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.dispose();
        System.exit(0);
    }

    /**
     * Set the profile file and closes the dialog.
     * 
     * @param profile
     */
    private void loginButtonActionPerformed(String profile) {
		if (profile != null) {
    		setProfile(new File(profileDir.getPath() + File.separatorChar + profile));
    	}
        dispose();
    }

    /**
     * Sets the profile dir and returns the 
     * 
     * @param profileDirToSet the last profile used.
     * @return File
     * @throws TaskFailedException if there is no profile set. NB profileDirToSet can be null.
     */
    public File getUserProfile(final File profileDirToSet) throws TaskFailedException {
		if (profileDirToSet != null) {
			if (profileDirToSet.isFile()) {
				this.profileDir = profileDirToSet.getParentFile();
			} else {
				this.profileDir = profileDirToSet;
			}

			profileSelectionBox.setModel(new javax.swing.DefaultComboBoxModel(
					profileDir.list(profileFileFilter)));
			profileSelectionBox.validate();
		}

    	setVisible(true);

    	return getProfile();
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
		profileDir = profileToSet.getParentFile();
		this.profile = profileToSet;
	}


    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel userLabel;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JButton loginButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JComboBox profileSelectionBox;
    private javax.swing.JButton selectProfileButton;
    private javax.swing.JCheckBox svnConnectCheckBox;
}
