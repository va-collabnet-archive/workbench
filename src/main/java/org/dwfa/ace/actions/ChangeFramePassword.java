package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.mail.AuthenticationFailedException;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.svn.SvnPrompter;

public class ChangeFramePassword implements ActionListener {

	ACE acePanel;
	
	
	public ChangeFramePassword(ACE acePanel) {
		super();
		this.acePanel = acePanel;
	}


	public void actionPerformed(ActionEvent e) {
		try {
			I_ConfigAceFrame frameConfig = acePanel.getAceFrameConfig();
			SvnPrompter prompter = new SvnPrompter();
			prompter.prompt("Current username/password", 
					frameConfig.getUsername());
			if (prompter.getUsername() != null) {
				if (prompter.getUsername().equals(
						frameConfig.getUsername()) == false) {
					throw new AuthenticationFailedException(
							"username does not match");
				}
			}
			if (prompter.getPassword() != null) {
				if (prompter.getPassword().equals(
						frameConfig.getPassword()) == false) {
					throw new AuthenticationFailedException(
							"password does not match");
				}
			}
			prompter.prompt("New username/password", AceConfig.config
					.getUsername());
			frameConfig.setUsername(prompter.getUsername());
			frameConfig.setPassword(prompter.getPassword());

		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
