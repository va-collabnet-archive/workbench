package org.dwfa.svn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.log.HtmlHandler;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

public class SvnPanel extends JPanel {
	
	private class LogLevelListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			logHandler.setLevel((Level) logLevel.getSelectedItem());
			SvnLog.setLevel(logHandler.getLevel());
			SvnLog.info("Level set to: " + logHandler.getLevel());
		}

	}
	private class PurgeListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			purge();
		}

	}
	private class CheckoutListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			checkout();
		}

	}
	private class UpdateListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			update();
		}
	}
	private class CommitListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			commit();
		}
	}
	private class CleanupListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			cleanup();
		}

	}
	private class StatusListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			status();
		}

	}
	private class ClearLogListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			clearLog();
		}

	}
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField repository;
	private JTextField workingCopy;
	private SvnPrompter authenticator;
	private I_ConfigAceFrame aceFrameConfig;
	private HtmlHandler logHandler;
	private JComboBox logLevel;
	
	public SvnPanel(I_ConfigAceFrame aceFrameConfig) throws Exception {
		super(new GridBagLayout());
		this.aceFrameConfig = aceFrameConfig;
		authenticator = new SvnPrompter();
		authenticator.setParentContainer(this);
		authenticator.setUsername(aceFrameConfig.getUsername());
		authenticator.setPassword(aceFrameConfig.getPassword());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 0;
		c.weighty = 0;
		c.gridx = 0;
		c.gridy = 0;
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.EAST;
		this.add(new JLabel("Repository: "), c);
		c.gridy++;
		this.add(new JLabel("Working copy: "), c);
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.gridwidth = 7;
		c.anchor = GridBagConstraints.WEST;
		repository = new JTextField(aceFrameConfig.getSvnRepository());
		this.add(repository, c);
		c.gridy++;
		workingCopy = new JTextField(aceFrameConfig.getSvnWorkingCopy());
		this.add(workingCopy, c);
		c.gridwidth = 1;
		c.gridy++;
		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		this.add(new JLabel(), c);
		c.gridx++;
		JButton status = new JButton("status");
		status.addActionListener(new StatusListener());
		this.add(status, c);
		c.gridx++;
		JButton commit = new JButton("commit");
		commit.addActionListener(new CommitListener());
		this.add(commit, c);
		c.gridx++;
		JButton update = new JButton("update");
		update.addActionListener(new UpdateListener());
		this.add(update, c);
		c.gridx++;
		JButton cleanup = new JButton("cleanup");
		cleanup.addActionListener(new CleanupListener());
		this.add(cleanup, c);
		c.gridx++;
		JButton checkout = new JButton("get");
		checkout.addActionListener(new CheckoutListener());
		this.add(checkout, c);
		c.gridx++;
		JButton purge = new JButton("purge");
		purge.addActionListener(new PurgeListener());
		this.add(purge, c);
		c.gridx = 0;
		c.gridy++;
		this.add(new JLabel("log level:"), c);
		c.gridx++;
		Level[] levels = {Level.CONFIG, Level.INFO, Level.FINE, Level.FINER, Level.FINEST, Level.ALL};
		logLevel = new JComboBox(levels);
		logLevel.setSelectedItem(Level.INFO);
		logLevel.addActionListener(new LogLevelListener());
		this.add(logLevel, c);
		c.gridx++;
		JButton clear = new JButton("clear log");
		clear.addActionListener(new ClearLogListener());
		this.add(clear, c);
		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 8;
		c.gridheight = 10;
		c.weighty = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		
        JEditorPane logOut = new JEditorPane("text/html", "<html>");
        logHandler = new HtmlHandler(logOut, "svn");
        logHandler.setLevel(Level.INFO);
        SvnLog.addHandler(logHandler);
		
		this.add(new JScrollPane(logOut), c);
		
	}
	
	private void logDetails() throws ClientException {
		if (SvnLog.isLoggable(Level.FINE)) {
			SvnLog.fine("working copy Author: " + Svn.getSvnClient().info(workingCopy.getText()).getAuthor());
			SvnLog.fine("working copy CopyRev: " + Svn.getSvnClient().info(workingCopy.getText()).getCopyRev());
			SvnLog.fine("working copy CopyUrl: " + Svn.getSvnClient().info(workingCopy.getText()).getCopyUrl());
			SvnLog.fine("working copy LastChangedRevision: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedRevision());
			SvnLog.fine("working copy Name: " + Svn.getSvnClient().info(workingCopy.getText()).getName());
			SvnLog.fine("working copy NodeKind: " + Svn.getSvnClient().info(workingCopy.getText()).getNodeKind());
			SvnLog.fine("working copy Repository: " + Svn.getSvnClient().info(workingCopy.getText()).getRepository());
			SvnLog.fine("working copy Revision: " + Svn.getSvnClient().info(workingCopy.getText()).getRevision());
			SvnLog.fine("working copy Schedule: " + Svn.getSvnClient().info(workingCopy.getText()).getSchedule());
			SvnLog.fine("working copy Url: " + Svn.getSvnClient().info(workingCopy.getText()).getUrl());
			SvnLog.fine("working copy Uuid: " + Svn.getSvnClient().info(workingCopy.getText()).getUuid());
			SvnLog.fine("working copy LastChangedDate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedDate());
			SvnLog.fine("working copy LastChangedRevision: " + Svn.getSvnClient().info(workingCopy.getText()).getLastChangedRevision());
			SvnLog.fine("working copy LastDatePropsUpdate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastDatePropsUpdate());
			SvnLog.fine("working copy LastDateTextUpdate: " + Svn.getSvnClient().info(workingCopy.getText()).getLastDateTextUpdate());
		}
	}
	public void status() {
		SvnLog.info("starting status");
		try {
			handleAuthentication();
			Status[] status = Svn.getSvnClient().status(workingCopy.getText(), false, false, false);
			for (Status s: status) {
				SvnLog.info("Managed: " + s.isManaged() + " status: " + s.getTextStatusDescription() + " " + s.getPath());	
			}
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished status");
	}
	public void cleanup() {
		SvnLog.info("starting cleanup");
		try {
			handleAuthentication();
			Svn.getSvnClient().cleanup(workingCopy.getText());
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished cleanup");
	}
	public void commit() {
		SvnLog.info("Starting Commit");
		try {
			
			Status[] status = Svn.getSvnClient().status(workingCopy.getText(), false, false, false);
			String commitMessage = authenticator.askQuestion(repository.getText(), "commit message: ", true);
			for (Status s: status) {
				if (s.isManaged() == false) {
					if (s.getPath().toLowerCase().endsWith(".jcs")) {
						Svn.getSvnClient().add(s.getPath(), false);
						SvnLog.info("Adding: " + s.getPath());
					}
				}
			}
			
			handleAuthentication();
			Svn.getSvnClient().commit(new String[] { workingCopy.getText() }, commitMessage, true);
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished commit");
	}
	public void purge() {
		SvnLog.info("Starting purge");
		try {
			handleAuthentication();
			Status[] status = Svn.getSvnClient().status(workingCopy.getText(), false, false, false);
			for (Status s: status) {
				if (s.isManaged() == true) {
					if (s.getTextStatus() == StatusKind.missing) {
						String purgeMessage = authenticator.askQuestion(repository.getText(), s.getPath() + " purge message: ", true);
						Svn.getSvnClient().remove(new String[] {s.getPath() }, purgeMessage, true);
						SvnLog.info("removed: " + s.getPath());
					}
				}
			}
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished purge");
	}
	public void update() {
		SvnLog.info("starting update");
		try {
			handleAuthentication();
			Svn.getSvnClient().update( workingCopy.getText(), Revision.HEAD, true);
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished update");
	}
	public void checkout() {
		SvnLog.info("starting get");
		try {
			handleAuthentication();
			Svn.getSvnClient().checkout(repository.getText(),
					workingCopy.getText(), Revision.HEAD, true);
			logDetails();
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished get");
	}
	private void handleAuthentication() {
		Svn.getSvnClient().password("");
		Svn.getSvnClient().setPrompt(authenticator);
	}
	
	public void clearLog() {
		logHandler.clearLog();
	}

}
