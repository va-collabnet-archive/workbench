package org.dwfa.ace.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.mail.AuthenticationFailedException;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.svn.SvnPrompter;

public class ChangeEnvironmentPassword implements ActionListener {

	public void actionPerformed(ActionEvent e) {
		try {
			SvnPrompter prompter = new SvnPrompter();
			prompter.prompt("Current username/password", AceConfig.config
					.getUsername());
			if (prompter.getUsername() != null) {
				if (prompter.getUsername().equals(
						AceConfig.config.getUsername()) == false) {
					throw new AuthenticationFailedException(
							"username does not match");
				}
			}
			if (prompter.getPassword() != null) {
				if (prompter.getPassword().equals(
						AceConfig.config.getPassword()) == false) {
					throw new AuthenticationFailedException(
							"password does not match");
				}
			}
			prompter.prompt("New username/password", AceConfig.config
					.getUsername());
			AceConfig.config.setUsername(prompter.getUsername());
			AceConfig.config.setPassword(prompter.getPassword());

		} catch (Exception e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}

	}

}
