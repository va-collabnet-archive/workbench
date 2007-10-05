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
import org.dwfa.ace.api.SubversionData;
import org.dwfa.log.HtmlHandler;
import org.dwfa.queue.ObjectServerCore;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusKind;

public class SvnPanel extends JPanel {
	
	private class LogLevelListener implements ActionListener {

		HtmlHandler logHandler;
		JComboBox logLevel;
		
		public LogLevelListener(HtmlHandler logHandler, JComboBox logLevel) {
			super();
			this.logHandler = logHandler;
			this.logLevel = logLevel;
		}

		public void actionPerformed(ActionEvent arg0) {
			logHandler.setLevel((Level) logLevel.getSelectedItem());
			SvnLog.setLevel(logHandler.getLevel());
			SvnLog.info("Level set to: " + logHandler.getLevel());
		}

	}
	private class PurgeListener implements ActionListener {

		SubversionData svd;
		public PurgeListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			purge(svd, authenticator);
		}

	}
	private class CheckoutListener implements ActionListener {

		SubversionData svd;
		public CheckoutListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			checkout(svd, authenticator);
		}

	}
	private class UpdateListener implements ActionListener {

		SubversionData svd;
		public UpdateListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			update(svd, authenticator);
		}
	}
	private class CommitListener implements ActionListener {

		SubversionData svd;
		public CommitListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			commit(svd, authenticator);
		}
	}
	private class CleanupListener implements ActionListener {

		SubversionData svd;
		public CleanupListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			cleanup(svd, authenticator);
		}

	}
	private class StatusListener implements ActionListener {

		SubversionData svd;
		public StatusListener(SubversionData svd) {
			super();
			this.svd = svd;
		}
		public void actionPerformed(ActionEvent arg0) {
			status(svd, authenticator);
		}

	}
	private class ClearLogListener implements ActionListener {

		HtmlHandler logHandler;
		public ClearLogListener(HtmlHandler logHandler) {
			super();
			this.logHandler = logHandler;
		}
		public void actionPerformed(ActionEvent arg0) {
			logHandler.clearLog();
		}

	}
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SvnPrompter authenticator;
	
	public SvnPanel(I_ConfigAceFrame aceFrameConfig, String tabName) throws Exception {
		super(new GridBagLayout());
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
		SubversionData svd = aceFrameConfig.getSubversionMap().get(tabName);
		JTextField repository = new JTextField(svd.getRepositoryUrlStr());
		repository.setEditable(false);
		this.add(repository, c);
		c.gridy++;
		JTextField workingCopy = new JTextField(svd.getWorkingCopyStr());
		workingCopy.setEditable(false);
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
		status.addActionListener(new StatusListener(svd));
		this.add(status, c);
		c.gridx++;
		JButton commit = new JButton("commit");
		commit.addActionListener(new CommitListener(svd));
		this.add(commit, c);
		c.gridx++;
		JButton update = new JButton("update");
		update.addActionListener(new UpdateListener(svd));
		this.add(update, c);
		c.gridx++;
		JButton cleanup = new JButton("cleanup");
		cleanup.addActionListener(new CleanupListener(svd));
		this.add(cleanup, c);
		c.gridx++;
		JButton checkout = new JButton("get");
		checkout.addActionListener(new CheckoutListener(svd));
		this.add(checkout, c);
		c.gridx++;
		JButton purge = new JButton("purge");
		purge.addActionListener(new PurgeListener(svd));
		this.add(purge, c);
		c.gridx = 0;
		c.gridy++;
		this.add(new JLabel("log level:"), c);
		c.gridx++;
		Level[] levels = {Level.CONFIG, Level.INFO, Level.FINE, Level.FINER, Level.FINEST, Level.ALL};
		JComboBox logLevel = new JComboBox(levels);
		logLevel.setSelectedItem(Level.INFO);
		this.add(logLevel, c);
		c.gridx++;
		JButton clear = new JButton("clear log");
		this.add(clear, c);
		c.gridy = 4;
		c.gridx = 0;
		c.gridwidth = 8;
		c.gridheight = 10;
		c.weighty = 1;
		c.weightx = 0;
		c.fill = GridBagConstraints.BOTH;
		
        JEditorPane logOut = new JEditorPane("text/html", "<html>");
        HtmlHandler logHandler = new HtmlHandler(logOut, "svn");
        logHandler.setLevel(Level.INFO);
        SvnLog.addHandler(logHandler);
		clear.addActionListener(new ClearLogListener(logHandler));
		logLevel.addActionListener(new LogLevelListener(logHandler, logLevel));
		
		this.add(new JScrollPane(logOut), c);
		
	}
	
	private static void logDetails(SubversionData svd) throws ClientException {
		if (SvnLog.isLoggable(Level.FINE)) {
			SvnLog.fine("working copy Author: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getAuthor());
			SvnLog.fine("working copy CopyRev: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getCopyRev());
			SvnLog.fine("working copy CopyUrl: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getCopyUrl());
			SvnLog.fine("working copy LastChangedRevision: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getLastChangedRevision());
			SvnLog.fine("working copy Name: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getName());
			SvnLog.fine("working copy NodeKind: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getNodeKind());
			SvnLog.fine("working copy Repository: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getRepository());
			SvnLog.fine("working copy Revision: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getRevision());
			SvnLog.fine("working copy Schedule: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getSchedule());
			SvnLog.fine("working copy Url: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getUrl());
			SvnLog.fine("working copy Uuid: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getUuid());
			SvnLog.fine("working copy LastChangedDate: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getLastChangedDate());
			SvnLog.fine("working copy LastChangedRevision: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getLastChangedRevision());
			SvnLog.fine("working copy LastDatePropsUpdate: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getLastDatePropsUpdate());
			SvnLog.fine("working copy LastDateTextUpdate: " + Svn.getSvnClient().info(svd.getWorkingCopyStr()).getLastDateTextUpdate());
		}
	}
	public static void status(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("starting status");
		try {
			handleAuthentication(authenticator);
			Status[] status = Svn.getSvnClient().status(svd.getWorkingCopyStr(), false, false, false);
			for (Status s: status) {
				SvnLog.info("Managed: " + s.isManaged() + " status: " + s.getTextStatusDescription() + " " + s.getPath());	
			}
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished status");
	}
	public static void cleanup(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("starting cleanup");
		try {
			handleAuthentication(authenticator);
			Svn.getSvnClient().cleanup(svd.getWorkingCopyStr());
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished cleanup");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}
	public static void commit(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("Starting Commit");
		try {
			
			Status[] status = Svn.getSvnClient().status(svd.getWorkingCopyStr(), false, false, false);
			String commitMessage = authenticator.askQuestion(svd.getRepositoryUrlStr(), "commit message: ", true);
			for (Status s: status) {
				if (s.isManaged() == false && s.isIgnored() == false) {
                    Svn.getSvnClient().add(s.getPath(), true);
                    SvnLog.info("Adding: " + s.getPath());
				}
			}
			
			handleAuthentication(authenticator);
			Svn.getSvnClient().commit(new String[] { svd.getWorkingCopyStr() }, commitMessage, true);
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished commit");
	}
	public static void purge(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("Starting purge");
		try {
			handleAuthentication(authenticator);
			Status[] status = Svn.getSvnClient().status(svd.getWorkingCopyStr(), false, false, false);
			for (Status s: status) {
				if (s.isManaged() == true) {
					if (s.getTextStatus() == StatusKind.missing) {
						String purgeMessage = authenticator.askQuestion(svd.getRepositoryUrlStr(), s.getPath() + " purge message: ", true);
						Svn.getSvnClient().remove(new String[] {s.getPath() }, purgeMessage, true);
						SvnLog.info("removed: " + s.getPath());
					}
				}
			}
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished purge");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}
	public static void update(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("starting update");
		try {
			handleAuthentication(authenticator);
			Svn.getSvnClient().update(svd.getWorkingCopyStr(), Revision.HEAD, true);
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished update");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}
	public static void checkout(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("starting get");
		try {
			handleAuthentication(authenticator);
			Svn.getSvnClient().checkout(svd.getRepositoryUrlStr(),
					svd.getWorkingCopyStr(), Revision.HEAD, true);
			logDetails(svd);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished get");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}
	private static void handleAuthentication(SvnPrompter authenticator) {
		Svn.getSvnClient().password("");
		Svn.getSvnClient().setPrompt(authenticator);
	}

    public SvnPrompter getAuthenticator() {
        return authenticator;
    }
	
}
