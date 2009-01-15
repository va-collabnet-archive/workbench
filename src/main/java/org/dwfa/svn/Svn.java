package org.dwfa.svn;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.queue.ObjectServerCore;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.javahl.Depth;
import org.tigris.subversion.javahl.DirEntry;
import org.tigris.subversion.javahl.Info2;
import org.tigris.subversion.javahl.InfoCallback;
import org.tigris.subversion.javahl.ListCallback;
import org.tigris.subversion.javahl.Lock;
import org.tigris.subversion.javahl.NodeKind;
import org.tigris.subversion.javahl.Notify2;
import org.tigris.subversion.javahl.NotifyAction;
import org.tigris.subversion.javahl.NotifyInformation;
import org.tigris.subversion.javahl.NotifyStatus;
import org.tigris.subversion.javahl.PromptUserPassword3;
import org.tigris.subversion.javahl.Revision;
import org.tigris.subversion.javahl.SVNClient;
import org.tigris.subversion.javahl.SVNClientInterface;
import org.tigris.subversion.javahl.Status;
import org.tigris.subversion.javahl.StatusCallback;
import org.tigris.subversion.javahl.StatusKind;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class Svn implements I_HandleSubversion {

	enum SvnImpl {
		NATIVE, SVN_KIT
	};

	private static SvnImpl impl = SvnImpl.SVN_KIT;

	private static SVNClientInterface client;

	private static SvnPrompter prompter = new SvnPrompter();

	public static SVNClientInterface getSvnClient() {
		if (client == null) {
			switch (impl) {
			case NATIVE:
				client = new SVNClient();
				AceLog.getAppLog().info("Created native svn client: " + client);
				break;

			case SVN_KIT:
				client = SVNClientImpl.newInstance();
				AceLog.getAppLog().info(
						"Created Svnkit pure java svn client: " + client);
				break;

			default:
				throw new RuntimeException("Can't handle svn impl type: "
						+ impl);
			}
			client.setPrompt(prompter);
			// The SVNClient needs an implementation of Notify before
			// successfully executing any other methods.
			client.notification2(new Notify2() {
				public void onNotify(NotifyInformation info) {
					try {
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							String path = info.getPath();
							String nodeKindName = NodeKind.getNodeKindName(info
									.getKind());
							String contentStateName = convertStatus(info
									.getContentState());
							String propertyStateName = convertStatus(info
									.getPropState());
							String errorMsg = info.getErrMsg();
							String mimeType = info.getMimeType();
							String revision = Long.toString(info.getRevision());
							String lock = toString(info.getLock());
							String lockState = Integer.toString(info
									.getLockState());
							String action = convertAction(info.getAction());

							SvnLog.info("svn onNotify: " + " path: " + path
									+ "\n" + " kind: " + nodeKindName + " "
									+ " content state: " + contentStateName
									+ " prop state: " + propertyStateName
									+ " \n" + " err msg: " + errorMsg + " "
									+ " mime : " + mimeType + " "
									+ " revision: " + revision + " \n"
									+ " lock: " + lock + " " + " lock state: "
									+ lockState + " " + " action: " + action);
						}
					} catch (Throwable t) {
						AceLog.getAppLog().alertAndLogException(t);
					}
				}

				private String toString(Object obj) {
					if (obj == null) {
						return "null";
					}
					return obj.toString();
				}

				private String convertAction(int infoAction) {
					if (infoAction >= 0
							&& infoAction < NotifyAction.actionNames.length) {
						return NotifyAction.actionNames[infoAction];
					}
					return Integer.toString(infoAction);
				}

				private String convertStatus(int infoState) {
					if (infoState >= 0
							&& infoState < NotifyStatus.statusNames.length) {
						return NotifyStatus.statusNames[infoState];
					}
					return Integer.toString(infoState);
				}
			});
		}
		return client;
	}

	public static Status[] status(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		Status[] status = null;
		Svn.getSvnClient().setPrompt(authenticator);
		String workingCopy = svd.getWorkingCopyStr();
		if (workingCopy.endsWith("/") == false) {
			workingCopy = workingCopy + "/";
		}
		SvnLog.info("starting status for working copy: " + workingCopy
				+ ", absolute file:" + new File(workingCopy).getAbsoluteFile());
		try {
			int depth = Depth.unknown;
			boolean onServer = false;
			boolean getAll = false;
			boolean noIgnore = false;
			boolean ignoreExternals = false;
			String[] changelists = null;
			HandleStatus statusHandler = new HandleStatus();
			Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth, onServer,
					getAll, noIgnore, ignoreExternals, changelists,
					statusHandler);
			for (Status s : statusHandler.getStatusList()) {
				SvnLog.info("Managed: " + s.isManaged() + " status: "
						+ s.getTextStatusDescription() + " " + s.getPath());
			}
			status = statusHandler.statusList
					.toArray(new Status[statusHandler.statusList.size()]);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished status for working copy: " + workingCopy);
		return status;
	}

	public static void cleanup(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("starting cleanup");
		Svn.getSvnClient().setPrompt(authenticator);
		try {
			Svn.getSvnClient().cleanup(svd.getWorkingCopyStr());
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished cleanup");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}

	public static void commit(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("Starting Commit");
		Svn.getSvnClient().setPrompt(authenticator);
		try {

			Status[] status = status(svd, authenticator, interactive);

			int newFiles = 0;
			int deletedFiles = 0;
			int modifiedFiles = 0;
			for (Status s : status) {
				if (s.isManaged()) {
					if (s.getTextStatusDescription()
							.equalsIgnoreCase("missing")) {
						boolean force = true;
						boolean keepLocal = false;
						Map<String, String> revpropTable = new HashMap<String, String>();
						Svn.getSvnClient().remove(new String[] { s.getPath() },
								"pr01", force, keepLocal, revpropTable);
						SvnLog.info("Removing: " + s.getPath());
						deletedFiles++;
					} else if (s.getTextStatusDescription().equalsIgnoreCase(
							"modified")) {
						modifiedFiles++;
					}
				} else if (s.isIgnored() == false) {
					int depth = Depth.infinity;
					boolean force = false;
					boolean noIgnores = false;
					boolean addParents = true;
					Svn.getSvnClient().add(s.getPath(), depth, force,
							noIgnores, addParents);
					SvnLog.info("Adding: " + s.getPath());
					newFiles++;
				} else {
					SvnLog.info("Not adding: " + s.getPath());
				}
			}

			if (newFiles + deletedFiles + modifiedFiles > 0) {
				String commitMessage = "new: " + newFiles + " deleted: "
						+ deletedFiles + " modified: " + modifiedFiles;
				if (interactive
						&& SvnPrompter.class.isAssignableFrom(authenticator
								.getClass())) {
					SvnPrompter p = (SvnPrompter) authenticator;
					commitMessage = p.askQuestion(svd.getRepositoryUrlStr(),
							"commit message: ", commitMessage, true);
				}
				if (interactive) {
					handleAuthentication(authenticator);
				}
				switchToReadWriteRepository(svd);
				int depth = Depth.unknown;
				boolean noUnlock = true;
				boolean keepChangelist = false;
				String[] changelists = null;
				Map<String, String> revpropTable = new HashMap<String, String>();
				Svn.getSvnClient().commit(
						new String[] { svd.getWorkingCopyStr() },
						commitMessage, depth, noUnlock, keepChangelist,
						changelists, revpropTable);
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

	public static void purge(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("Starting purge");
		Svn.getSvnClient().setPrompt(authenticator);
		try {
			int depth = Depth.unknown;
			boolean onServer = false;
			boolean getAll = false;
			boolean noIgnore = false;
			boolean ignoreExternals = false;
			String[] changelists = null;
			HandleStatus statusHandler = new HandleStatus();
			Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth, onServer,
					getAll, noIgnore, ignoreExternals, changelists,
					statusHandler);
			for (Status s : statusHandler.getStatusList()) {
				if (s.isManaged() == true) {
					if (s.getTextStatus() == StatusKind.missing) {
						String purgeMessage = authenticator.askQuestion(svd
								.getRepositoryUrlStr(), s.getPath()
								+ " purge message: ", true);
						boolean force = true;
						boolean keepLocal = false;
						Map<String, String> revisionPropMap = new HashMap<String, String>();
						Svn.getSvnClient()
								.remove(new String[] { s.getPath() },
										purgeMessage, force, keepLocal,
										revisionPropMap);
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

	public static void update(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("starting update");
		Svn.getSvnClient().setPrompt(authenticator);
		try {
			if (interactive) {
				handleAuthentication(authenticator);
			}
			switchToReadOnlyMirror(svd);
			int depth = Depth.unknown;
			boolean depthIsSticky = false;
			boolean ignoreExternals = false;
			boolean allowUnverObstructions = false;
			Svn.getSvnClient().update(svd.getWorkingCopyStr(), Revision.HEAD,
					depth, depthIsSticky, ignoreExternals,
					allowUnverObstructions);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished update");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}

	public static void checkout(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("starting get");
		Svn.getSvnClient().setPrompt(authenticator);
		try {
			if (interactive) {
				handleAuthentication(authenticator);
			}
			switchToReadOnlyMirror(svd);
			Revision revision = Revision.HEAD;
			Revision pegRevision = Revision.HEAD;
			int depth = Depth.infinity;
			boolean ignoreExternals = false;
			boolean allowUnverObstructions = false;
			Svn.getSvnClient().checkout(svd.getRepositoryUrlStr(),
					svd.getWorkingCopyStr(), revision, pegRevision, depth,
					ignoreExternals, allowUnverObstructions);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		SvnLog.info("finished get");
		ObjectServerCore.refreshServers();
		SvnLog.info("refreshed Object Servers");
	}

	private static void handleAuthentication(PromptUserPassword3 authenticator) {
		Svn.getSvnClient().password("");
		Svn.getSvnClient().setPrompt(authenticator);
	}

	private static class HandleSingleInfo implements InfoCallback {
		Info2 info;

		public Info2 getInfo() {
			return info;
		}

		public void singleInfo(Info2 info) {
			this.info = info;
		}

	}
	private static class HandleInfo implements InfoCallback {
		List<Info2> infoList = new ArrayList<Info2>();

		public void singleInfo(Info2 info) {
			infoList.add(info);
		}

	}

	public static void completeRepoInfo(SubversionData svd) {
		try {
			svd.setRepositoryUrlStr(getRepoInfo(svd).getUrl());
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
	}

	private static Info2 getRepoInfo(SubversionData svd)
			throws ClientException {
		String pathOrUrl = svd.getWorkingCopyStr();
		Revision revision = Revision.HEAD;
		Revision pegRevision = Revision.HEAD;
		int depth = Depth.unknown;
		String[] changelists = null;
		HandleSingleInfo callback = new HandleSingleInfo();
		Svn.getSvnClient().info2(pathOrUrl, revision, pegRevision, depth,
				changelists, callback);
		return callback.info;
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
			switchToReadOnlyMirror(svd);
			Svn.getSvnClient().list(url, revision, pegRevision, depth,
					direntFields, fetchLocks, callback);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		return callback.dirList;
	}
	public static List<Info2> info(SubversionData svd) {
		String path = svd.getWorkingCopyStr();
		Revision revision = Revision.HEAD;
		Revision pegRevision = Revision.HEAD;
		int depth = Depth.infinity;
		String[] changeLists = null;
		HandleInfo callback = new HandleInfo();
		try {
			Svn.getSvnClient().info2(path, revision, pegRevision, depth,
					changeLists, callback);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
		}
		return callback.infoList;
	}

	public void svnCheckout(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		checkout(svd, authenticator, interactive);
	}

	public void svnCleanup(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		cleanup(svd, authenticator, interactive);
	}

	public void svnCommit(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		commit(svd, authenticator, interactive);
	}

	public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator,
			boolean interactive) {
		purge(svd, authenticator, interactive);
	}

	public void svnStatus(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		status(svd, authenticator, interactive);
	}

	public void svnUpdate(SubversionData svd,
			PromptUserPassword3 authenticator, boolean interactive) {
		update(svd, authenticator, interactive);
	}

	public void svnCheckout(SubversionData svd) {
		svnCheckout(svd, prompter, true);
	}

	public void svnCleanup(SubversionData svd) {
		svnCleanup(svd, prompter, true);
	}

	public void svnCommit(SubversionData svd) {
		svnCommit(svd, prompter, true);
	}

	public void svnPurge(SubversionData svd) {
		svnPurge(svd, prompter, true);
	}

	public void svnStatus(SubversionData svd) {
		svnStatus(svd, prompter, true);
	}

	public void svnUpdate(SubversionData svd) {
		svnUpdate(svd, prompter, true);
	}

	public void svnCompleteRepoInfo(SubversionData svd) {
		completeRepoInfo(svd);
	}

	public List<String> svnList(SubversionData svd) {
		return list(svd);
	}

	public boolean svnLock(SubversionData svd, File toLock) {
		return svnLock(svd, toLock, prompter, true);
	}

	public boolean svnUnlock(SubversionData svd, File toUnLock) {
		return svnUnlock(svd, toUnLock, prompter, true);
	}

	public boolean svnUnlock(SubversionData svd, File toUnlock,
			PromptUserPassword3 authenticator, boolean interactive) {
		return unlock(svd, toUnlock, authenticator, interactive);
	}

	public static boolean unlock(SubversionData svd, File toUnlock,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("Starting unlock");
		Svn.getSvnClient().setPrompt(authenticator);
		try {

			if (interactive) {
				handleAuthentication(authenticator);
			}
			switchToReadWriteRepository(svd);
			Svn.getSvnClient().lock(
					new String[] { toUnlock.getAbsolutePath() },
					svd.getUsername(), false);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
			SvnLog.info("finished unlock with exception: " + e.toString());
			return false;
		}
		SvnLog.info("finished unlock");
		return true;
	}

	public boolean svnLock(SubversionData svd, File toLock,
			PromptUserPassword3 authenticator, boolean interactive) {
		return false;
	}

	public static boolean lock(SubversionData svd, File toLock,
			PromptUserPassword3 authenticator, boolean interactive) {
		SvnLog.info("Starting lock");
		Svn.getSvnClient().setPrompt(authenticator);
		try {

			if (interactive) {
				handleAuthentication(authenticator);
			}
			switchToReadWriteRepository(svd);
			Svn.getSvnClient().lock(new String[] { toLock.getAbsolutePath() },
					svd.getUsername(), false);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
			SvnLog.info("finished lock with exception: " + e.toString());
			return false;
		}
		SvnLog.info("finished lock");
		return true;
	}

	private static void switchToReadOnlyMirror(SubversionData svd) {
		SvnLog.info("Starting switch to read only");
		try {
			if (getRepoInfo(svd).getUrl().equals(svd.getPreferredReadRepository())) {
				SvnLog.info("Finised switch to read only: no change necessary");
				return;
			}
			String path = svd.getWorkingCopyStr();
			String url = svd.getPreferredReadRepository();
			Revision revision = Revision.HEAD;
			Revision pegRevision = Revision.HEAD;
			int depth = Depth.infinity;
			boolean depthIsSticky = true;
			boolean ignoreExternals = true;
			boolean allowUnverObstructions = false;
			long version = Svn.getSvnClient().doSwitch(path, url, revision,
					pegRevision, depth, depthIsSticky, ignoreExternals,
					allowUnverObstructions);
			SvnLog.info("Finished switch to read only at version: " + version);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
			SvnLog.info("Finished switch to read only: " + e.toString());
		}
	}

	private static void switchToReadWriteRepository(SubversionData svd) {
		SvnLog.info("Starting switch to read/write");
		try {
			if (getRepoInfo(svd).getUrl().equals(svd.getRepositoryUrlStr())) {
				SvnLog.info("Finished switch to read/write: no change necessary");
				return;
			}
			SvnLog.info("1");
			String path = svd.getWorkingCopyStr();
			SvnLog.info("2");
			String url = svd.getRepositoryUrlStr();
			SvnLog.info("3");
			Revision revision = Revision.HEAD;
			SvnLog.info("4");
			Revision pegRevision = Revision.HEAD;
			int depth = Depth.infinity;
			boolean depthIsSticky = true;
			boolean ignoreExternals = true;
			boolean allowUnverObstructions = false;
			SvnLog.info("5");
			long version = Svn.getSvnClient().doSwitch(path, url, revision,
					pegRevision, depth, depthIsSticky, ignoreExternals,
					allowUnverObstructions);
			SvnLog.info("Finished switch to read/write at version: " + version);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
			SvnLog.info("Finished switch to read/write: " + e.toString());
		}
	}

}
