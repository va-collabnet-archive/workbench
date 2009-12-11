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
package org.dwfa.svn;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.tigris.subversion.javahl.Depth;
import org.tigris.subversion.javahl.DirEntry;
import org.tigris.subversion.javahl.Info2;
import org.tigris.subversion.javahl.InfoCallback;
import org.tigris.subversion.javahl.ListCallback;
import org.tigris.subversion.javahl.Lock;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusCallback;
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
		
	public static Status[] status(SubversionData svd, SvnPrompter authenticator) {
        Status[] status = null;
        String workingCopy = svd.getWorkingCopyStr();
        if (workingCopy.endsWith("/") == false) {
            workingCopy = workingCopy + "/";
        }
		SvnLog.info("starting status for working copy: " + workingCopy + 
                    ", absolute file:" +new File(workingCopy).getAbsoluteFile());
		try {
			handleAuthentication(authenticator);
			int depth = Depth.unknown;
            boolean onServer = false;
            boolean getAll = false;
            boolean noIgnore = false;
            boolean ignoreExternals = false;
            String[] changelists = null;
            HandleStatus statusHandler = new HandleStatus();
			Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth,
		            onServer,
		            getAll,
		            noIgnore,
		            ignoreExternals,
		            changelists,
		            statusHandler);
			for (Status s: statusHandler.getStatusList()) {
				SvnLog.info("Managed: " + s.isManaged() + " status: " + s.getTextStatusDescription() + " " + s.getPath());	
			}
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished status for working copy: " + workingCopy);
        return status;
	}
	public static void cleanup(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("starting cleanup");
		try {
			handleAuthentication(authenticator);
			Svn.getSvnClient().cleanup(svd.getWorkingCopyStr());
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
			
			Status[] status = status(svd, authenticator);
            
            int newFiles = 0;
            int deletedFiles = 0;
            int modifiedFiles = 0;
			for (Status s: status) {
                if (s.isManaged()) {
                    if (s.getTextStatusDescription().equalsIgnoreCase("missing") ) {
                    	boolean force = true;
                        boolean keepLocal = false;
                        Map<String, String> revpropTable = new HashMap<String, String>();
                        Svn.getSvnClient().remove(new String[] { s.getPath() }, "pr01", force,
                        		keepLocal, revpropTable);
                        SvnLog.info("Removing: " + s.getPath());
                        deletedFiles++;
                    } else if (s.getTextStatusDescription().equalsIgnoreCase("modified") ) {
                        modifiedFiles++;
                    }
                } else if (s.isIgnored() == false) {
                    int depth = Depth.unknown;
                    boolean force = false;
                    boolean noIgnores = false;
                    boolean addParents = true;
                    Svn.getSvnClient().add(s.getPath(), depth,
                            force,
                            noIgnores,
                            addParents);
                    SvnLog.info("Adding: " + s.getPath());
                    newFiles++;
				} else {
                    SvnLog.info("Not adding: " + s.getPath());
                }
			}
            
            if (newFiles + deletedFiles + modifiedFiles > 0) {
                String defalutMessage = "new: " + newFiles + " deleted: " + deletedFiles + 
                    " modified: " + modifiedFiles;
                String commitMessage = authenticator.askQuestion(svd.getRepositoryUrlStr(), "commit message: ", 
                                                                 defalutMessage, true);
                handleAuthentication(authenticator);
                int depth = Depth.unknown;
                boolean noUnlock = true;
                boolean keepChangelist = false;
                String[] changelists = null;
                Map<String, String> revpropTable = new HashMap<String, String>();
                Svn.getSvnClient().commit(new String[] { svd.getWorkingCopyStr() }, commitMessage, 
                		depth,
                        noUnlock,
                        keepChangelist,
                        changelists,
                        revpropTable);
             }
			
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished commit");
	}
	
	private static class HandleStatus implements StatusCallback {
		List<Status> statusList = new ArrayList<Status>();
		public List<Status> getStatusList() {
			return statusList;
		}
		public void doStatus(Status s) {
			statusList.add(s);
		}
	}
	
	public static void purge(SubversionData svd, SvnPrompter authenticator) {
		SvnLog.info("Starting purge");
		try {
			handleAuthentication(authenticator);
			int depth = Depth.unknown;
            boolean onServer = false;
            boolean getAll = false;
            boolean noIgnore = false;
            boolean ignoreExternals = false;
            String[] changelists = null;
            HandleStatus statusHandler = new HandleStatus();
			Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth,
		            onServer,
		            getAll,
		            noIgnore,
		            ignoreExternals,
		            changelists,
		            statusHandler);
			for (Status s: statusHandler.getStatusList()) {
				if (s.isManaged() == true) {
					if (s.getTextStatus() == StatusKind.missing) {
						String purgeMessage = authenticator.askQuestion(svd.getRepositoryUrlStr(), s.getPath() + 
								" purge message: ", true);
						boolean force = true;
						boolean keepLocal = false;
						Map<String, String> revisionPropMap = new HashMap<String, String>();
						Svn.getSvnClient().remove(new String[] {s.getPath() }, 
								purgeMessage, force, keepLocal, revisionPropMap);
						SvnLog.info("removed: " + s.getPath());
					}
				}
			}
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
			int depth = Depth.unknown;
			boolean depthIsSticky = false;
			boolean ignoreExternals = false;
			boolean allowUnverObstructions = false;
			Svn.getSvnClient().update(svd.getWorkingCopyStr(),
					Revision.HEAD,
		            depth,
		            depthIsSticky,
		            ignoreExternals,
		            allowUnverObstructions);
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
			Revision revision = Revision.HEAD;
			Revision pegRevision = Revision.HEAD;
			int depth = Depth.infinity;
			boolean ignoreExternals = false;
			boolean allowUnverObstructions = false;
			Svn.getSvnClient().checkout(svd.getRepositoryUrlStr(),
					svd.getWorkingCopyStr(), revision,
					pegRevision, depth, ignoreExternals,
					allowUnverObstructions);
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

    private static class HandleInfo implements InfoCallback {
    	Info2 info;
		public Info2 getInfo() {
			return info;
		}
		public void singleInfo(Info2 info) {
			this.info = info;
		}
    	
    }
	public static void completeRepoInfo(SubversionData svd) {
		try {
            String pathOrUrl = svd.getWorkingCopyStr();
            Revision revision = Revision.HEAD;
            Revision pegRevision  = Revision.HEAD;
            int depth = Depth.unknown;
            String[] changelists = null;
            HandleInfo callback = new HandleInfo();
			Svn.getSvnClient().info2(pathOrUrl,
			           revision,
			           pegRevision,
			           depth,
			           changelists,
			           callback);
			svd.setRepositoryUrlStr(callback.info.getUrl());
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		
	}
	private static class ListHandler implements ListCallback {
		List<String> dirList = new ArrayList<String>();
		public void doEntry(DirEntry dirent, Lock lock) {
			dirList.add(dirent.getAbsPath() + "/" + dirent.getPath());
		}
		
	}
	public static List<String> list(SubversionData svd) {
		String url = svd.getRepositoryUrlStr(); 
		Revision revision = Revision.HEAD; 
		Revision pegRevision = Revision.HEAD; 
		int depth = Depth.infinity; 
		int direntFields = DirEntry.Fields.all; 
		boolean fetchLocks = false; 
		ListHandler callback = new ListHandler();
		try {
			Svn.getSvnClient().list(url, revision, pegRevision, depth, direntFields, fetchLocks, callback);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		return callback.dirList;
	}
}
