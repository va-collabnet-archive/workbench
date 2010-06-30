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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

import javax.swing.JOptionPane;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_HandleSubversion;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.SubversionData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.svn.SvnPrompter;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.queue.ObjectServerCore;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.time.TimeUtil;
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
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.javahl.SVNClientImpl;

public class Svn implements I_HandleSubversion {

	enum SvnImpl {
		NATIVE,
		SVN_KIT
	};

	private static boolean connectedToSvn = false;

	private static SvnImpl impl = SvnImpl.SVN_KIT;

	private static SVNClientInterface client;

	private static SvnPrompter prompter = new SvnPrompter();

	public static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	public static SVNClientInterface getSvnClient() {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		if (client == null) {

			switch (impl) {
			case NATIVE:
				client = new SVNClient();
				AceLog.getAppLog().info("Created native svn client: " + client.getVersion());
				break;

			case SVN_KIT:
				SVNClientImpl c = SVNClientImpl.newInstance();
				SVNClientImpl.setRuntimeCredentialsStorage(null);
				c.setClientCredentialsStorage(null);
				client = c;
				client.password("");
				client.username("");

				AceLog.getAppLog().info("Created Svnkit pure java svn client: " + client.getVersion());
				break;

			default:
				throw new RuntimeException("Can't handle svn impl type: " + impl);
			}
			client.setPrompt(prompter);
			// The SVNClient needs an implementation of Notify before
			// successfully executing any other methods.
			client.notification2(new Notify2() {
				public void onNotify(NotifyInformation info) {
					try {
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							String path = info.getPath();
							String nodeKindName = NodeKind.getNodeKindName(info.getKind());
							String contentStateName = convertStatus(info.getContentState());
							String propertyStateName = convertStatus(info.getPropState());
							String errorMsg = info.getErrMsg();
							String mimeType = info.getMimeType();
							String revision = Long.toString(info.getRevision());
							String lock = toString(info.getLock());
							String lockState = Integer.toString(info.getLockState());
							String action = convertAction(info.getAction());

							SvnLog.info("svn onNotify: path: " + path + "\n kind: " + nodeKindName + " content state: "
									+ contentStateName + " prop state: " + propertyStateName + "\n err msg: " + errorMsg
									+ " mime : " + mimeType + " revision: " + revision + "\n lock: " + lock
									+ " lock state: " + lockState + " action: " + action);
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
					if (infoAction >= 0 && infoAction < NotifyAction.actionNames.length) {
						return NotifyAction.actionNames[infoAction];
					}
					return Integer.toString(infoAction);
				}

				private String convertStatus(int infoState) {
					if (infoState >= 0 && infoState < NotifyStatus.statusNames.length) {
						return NotifyStatus.statusNames[infoState];
					}
					return Integer.toString(infoState);
				}
			});
		}
		return client;
	}

	public static Status[] status(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive) 
	throws TaskFailedException {
		return status(svd, authenticator, interactive, false);
	}

	public static Status[] status(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive, boolean onServer)
	throws TaskFailedException {

		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		Status[] status = null;
		Svn.getSvnClient().setPrompt(authenticator);
		String workingCopy = svd.getWorkingCopyStr();
		if (workingCopy.endsWith("/") == false) {
			workingCopy = workingCopy + "/";
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion status");
		try {

			SvnLog.info("starting status for working copy: " + workingCopy + ", absolute file:"
					+ new File(workingCopy).getAbsoluteFile());
			try {
				int depth = Depth.unknown;
				boolean getAll = false;
				boolean noIgnore = false;
				boolean ignoreExternals = false;
				String[] changelists = null;
				HandleStatus statusHandler = new HandleStatus();
				Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth, onServer, getAll, noIgnore, ignoreExternals,
						changelists, statusHandler);
				for (Status s : statusHandler.getStatusList()) {
					SvnLog.info("Managed: " + s.isManaged() + " status: " + s.getTextStatusDescription() + " "
							+ s.getPath());
				}
				status = statusHandler.statusList.toArray(new Status[statusHandler.statusList.size()]);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished status for working copy: " + workingCopy + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished status for working copy: " + workingCopy);
			return status;
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}
		}
	}

	public static I_ShowActivity setupActivityPanel(String message)
			throws TaskFailedException {
		try {
			I_ShowActivity activity = Terms.get().newActivityPanel(true, Terms.get().getActiveAceFrameConfig(), message, false);
			activity.setIndeterminate(true);
			activity.setProgressInfoLower("Performing subversion operation...");
			return activity;
		} catch (TerminologyException e1) {
			throw new TaskFailedException(e1);
		} catch (IOException e1) {
			throw new TaskFailedException(e1);
		}
	}

	public static void cleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion cleanup");
		try {

			SvnLog.info("starting cleanup");
			Svn.getSvnClient().setPrompt(authenticator);
			try {
				Svn.getSvnClient().cleanup(svd.getWorkingCopyStr());
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished cleanup for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished cleanup");
			ObjectServerCore.refreshServers();
			SvnLog.info("refreshed Object Servers");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public static void commit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion commit");
		try {

			SvnLog.info("Starting Commit");
			Svn.getSvnClient().setPrompt(authenticator);
			try {

				Status[] status = status(svd, authenticator, interactive, true);

				int newFiles = 0;
				int deletedFiles = 0;
				int modifiedFiles = 0;
				for (Status s : status) {
					if (s.isManaged()) {
						if (s.getTextStatus() == StatusKind.missing) {
							boolean force = true;
							boolean keepLocal = false;
							Map<String, String> revpropTable = new HashMap<String, String>();
							Svn.getSvnClient().remove(new String[] { s.getPath() }, "pr01", force, keepLocal, revpropTable);
							SvnLog.info("Removing: " + s.getPath());
							deletedFiles++;
						} else if (s.isModified()) {
							if (s.getRepositoryTextStatus() == StatusKind.modified) {
								// Conflict exists. 
								StringBuffer msg = new StringBuffer();
								msg.append("<html>File has been modified locally and on server: <br><br>&nbsp;>&nbsp;>&nbsp;>&nbsp;");
								msg.append(s.getPath());
								msg.append("<br><br>Accept recommendation to revert to version on server?");
								boolean resolve = authenticator.askYesNo("Warning: local mods and server mods", msg.toString(), true);
								if (resolve) {
									revert(s);
								} else {
									throw new TaskFailedException("Conflict between local copy and server: " + s.getPath());
								}
							} else {
								modifiedFiles++;
							}

						}
					} else if (s.isIgnored() == false && new File(s.getPath()).exists() && s.getPath().contains(".llog") == false) {
						int depth = Depth.infinity;
						boolean force = false;
						boolean noIgnores = false;
						boolean addParents = true;
						Svn.getSvnClient().add(s.getPath(), depth, force, noIgnores, addParents);
						SvnLog.info("Adding: " + s.getPath());
						newFiles++;
					} else if (s.isDeleted() && s.getRepositoryTextStatus() == StatusKind.deleted) {
						// prevent tree conflict. 
						SvnLog.info("Preventing dual deletion tree conflict by reverting local copy: " + s.getPath());
						revert(s);
					} else {
						SvnLog.info("Not adding: " + s.getPath());
					}
				}

				if (newFiles + deletedFiles + modifiedFiles > 0) {
					String commitMessage = "new: " + newFiles + " deleted: " + deletedFiles + " modified: " + modifiedFiles;
					SvnLog.info(commitMessage);
					if (interactive && SvnPrompter.class.isAssignableFrom(authenticator.getClass())) {
						SvnLog.info("getting commit message...");
						SvnPrompter p = (SvnPrompter) authenticator;
						commitMessage = p.askQuestion(svd.getRepositoryUrlStr(), "commit message: ", commitMessage, true);
					}
					if (interactive) {
						SvnLog.info("authenticating");
						handleAuthentication(authenticator);
					}
					int depth = Depth.unknown;
					boolean noUnlock = true;
					boolean keepChangelist = false;
					String[] changelists = null;
					Map<String, String> revpropTable = new HashMap<String, String>();
					SvnLog.info("start commit: " + svd.getWorkingCopyStr());
					Svn.getSvnClient().commit(new String[] { svd.getWorkingCopyStr() }, commitMessage, depth, noUnlock,
							keepChangelist, changelists, revpropTable);
					SvnLog.info("finish commit");
				}

			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished commit for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished commit");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
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

	public static void purge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion purge");
		try {

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
				Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth, onServer, getAll, noIgnore, ignoreExternals,
						changelists, statusHandler);
				for (Status s : statusHandler.getStatusList()) {
					if (s.isManaged() == true) {
						if (s.getTextStatus() == StatusKind.missing) {
							String purgeMessage =
								authenticator.askQuestion(svd.getRepositoryUrlStr(), s.getPath() + " purge message: ",
										true);
							boolean force = true;
							boolean keepLocal = false;
							Map<String, String> revisionPropMap = new HashMap<String, String>();
							Svn.getSvnClient().remove(new String[] { s.getPath() }, purgeMessage, force, keepLocal,
									revisionPropMap);
							SvnLog.info("removed: " + s.getPath());
						}
					}
				}
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished purge for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished purge");
			ObjectServerCore.refreshServers();
			SvnLog.info("refreshed Object Servers");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public static void revert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion revert");
		try {

			SvnLog.info("Starting revert");
			Svn.getSvnClient().setPrompt(authenticator);
			try {
				int depth = Depth.unknown;
				boolean onServer = false;
				boolean getAll = false;
				boolean noIgnore = false;
				boolean ignoreExternals = false;
				String[] changelists = null;
				HandleStatus statusHandler = new HandleStatus();
				Svn.getSvnClient().status(svd.getWorkingCopyStr(), depth, onServer, getAll, noIgnore, ignoreExternals,
						changelists, statusHandler);
				for (Status s : statusHandler.getStatusList()) {
					if (s.isManaged() == true) {
						if (s.getTextStatus() == StatusKind.missing) {
							revert(s);
						} else if (s.getTextStatus() == StatusKind.modified) {
							revert(s);
						} else if (s.getTextStatus() == StatusKind.deleted) {
							revert(s);
						} else if (s.getTextStatus() == StatusKind.conflicted) {
							revert(s);
						} else if (s.getTextStatus() == StatusKind.merged) {
							revert(s);
						} else if (s.getTextStatus() == StatusKind.unversioned) {
							revert(s);
						}
					} else {
						File fileToRevert = new File(s.getPath());
						if (fileToRevert.isHidden() || fileToRevert.getName().startsWith(".")) {
							// don't mess with the hidden files.
						} else {
							FileIO.recursiveDelete(fileToRevert);
						}
					}
				}
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished revert for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			} catch (IOException e) {
				SvnLog.alertAndLog(e);
				SvnLog.info("finished revert for working copy: " + svd.getWorkingCopyStr() + "with exception: "
						+ e.getLocalizedMessage());
				throw new TaskFailedException(e);
			}
			SvnLog.info("finished revert");
			ObjectServerCore.refreshServers();
			SvnLog.info("refreshed Object Servers");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	private static void revert(Status s) throws ClientException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Svn.getSvnClient().revert(s.getPath(), Depth.infinity, null);
		SvnLog.info("reverted: " + s.getPath());
	}

	public static void update(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion update");
		try {

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
				Svn.getSvnClient().update(svd.getWorkingCopyStr(), Revision.HEAD, depth, depthIsSticky, ignoreExternals,
						allowUnverObstructions);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished update for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished update");
			ObjectServerCore.refreshServers();
			SvnLog.info("refreshed Object Servers");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public static void updateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion update database");
		try {

			SvnLog.info("starting database update");
			Svn.getSvnClient().setPrompt(authenticator);
			try {
				Svn.getSvnClient().revert(svd.getWorkingCopyStr(), Depth.infinity, null);
				SvnLog.info(" reverted: " + svd.getWorkingCopyStr());
				if (interactive) {
					handleAuthentication(authenticator);
				}
				switchToReadOnlyMirror(svd);
				int depth = Depth.unknown;
				boolean depthIsSticky = false;
				boolean ignoreExternals = false;
				boolean allowUnverObstructions = false;
				Svn.getSvnClient().update(svd.getWorkingCopyStr(), Revision.HEAD, depth, depthIsSticky, ignoreExternals,
						allowUnverObstructions);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished database update for working copy: " + svd.getWorkingCopyStr()
							+ "with exception: " + e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished database update");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public static void checkout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion checkout");
		try {

			SvnLog.info("starting checkout");
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
				Svn.getSvnClient().checkout(svd.getRepositoryUrlStr(), svd.getWorkingCopyStr(), revision, pegRevision,
						depth, ignoreExternals, allowUnverObstructions);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished checkout for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished checkout");
			ObjectServerCore.refreshServers();
			SvnLog.info("refreshed Object Servers");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	private static void handleAuthentication(PromptUserPassword3 authenticator) {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Svn.getSvnClient().password("");
		Svn.getSvnClient().setPrompt(authenticator);
	}

	private static class HandleSingleInfo implements InfoCallback {
		Info2 info;

		@SuppressWarnings("unused")
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

	private static Info2 getRepoInfo(SubversionData svd) throws ClientException {
		String pathOrUrl = svd.getWorkingCopyStr();
		Revision revision = Revision.HEAD;
		Revision pegRevision = Revision.HEAD;
		int depth = Depth.unknown;
		String[] changelists = null;
		HandleSingleInfo callback = new HandleSingleInfo();
		Svn.getSvnClient().info2(pathOrUrl, revision, pegRevision, depth, changelists, callback);
		return callback.info;
	}

	private static class ListHandler implements ListCallback {
		List<String> dirList = new ArrayList<String>();

		public void doEntry(DirEntry dirent, Lock lock) {
			dirList.add(dirent.getAbsPath() + "/" + dirent.getPath());
		}

	}

	public static List<String> list(SubversionData svd) throws TaskFailedException {
		String url = svd.getRepositoryUrlStr();
		Revision revision = Revision.HEAD;
		Revision pegRevision = Revision.HEAD;
		int depth = Depth.infinity;
		int direntFields = DirEntry.Fields.all;
		boolean fetchLocks = false;
		ListHandler callback = new ListHandler();
		try {
			switchToReadOnlyMirror(svd);
			Svn.getSvnClient().list(url, revision, pegRevision, depth, direntFields, fetchLocks, callback);
		} catch (ClientException e) {
			if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
				SvnLog.alertAndLog(e);
				throw new TaskFailedException(e);
			}
		}
		return callback.dirList;
	}

	public static List<Info2> info(SubversionData svd) throws TaskFailedException {
		String path = svd.getWorkingCopyStr();
		Revision revision = Revision.HEAD;
		Revision pegRevision = Revision.HEAD;
		int depth = Depth.infinity;
		String[] changeLists = null;
		HandleInfo callback = new HandleInfo();
		try {
			Svn.getSvnClient().info2(path, revision, pegRevision, depth, changeLists, callback);
		} catch (ClientException e) {
			SvnLog.alertAndLog(e);
			throw new TaskFailedException(e);
		}
		return callback.infoList;
	}

	public void svnCheckout(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		checkout(svd, authenticator, interactive);
	}

	public void svnCleanup(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		cleanup(svd, authenticator, interactive);
	}

	public void svnCommit(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		commit(svd, authenticator, interactive);
	}

	public void svnImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		doImport(svd, authenticator, interactive);
	}

	public void svnPurge(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		purge(svd, authenticator, interactive);
	}

	public void svnRevert(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		revert(svd, authenticator, interactive);
	}

	public void svnStatus(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		status(svd, authenticator, interactive);
	}

	public void svnUpdate(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		update(svd, authenticator, interactive);
	}

	public void svnUpdateDatabase(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		updateDatabase(svd, authenticator, interactive);
	}

	public void svnUpdateDatabase(SubversionData svd) throws TaskFailedException {
		updateDatabase(svd, prompter, true);
	}

	public void svnCheckout(SubversionData svd) throws TaskFailedException {
		svnCheckout(svd, prompter, true);
	}

	public void svnCleanup(SubversionData svd) throws TaskFailedException {
		svnCleanup(svd, prompter, true);
	}

	public void svnCommit(SubversionData svd) throws TaskFailedException {
		svnCommit(svd, prompter, true);
	}

	public void svnImport(SubversionData svd) throws TaskFailedException {
		svnImport(svd, prompter, true);
	}

	public void svnPurge(SubversionData svd) throws TaskFailedException {
		svnPurge(svd, prompter, true);
	}

	public void svnRevert(SubversionData svd) throws TaskFailedException {
		svnRevert(svd, prompter, true);
	}

	public void svnStatus(SubversionData svd) throws TaskFailedException {
		svnStatus(svd, prompter, true);
	}

	public void svnUpdate(SubversionData svd) throws TaskFailedException {
		svnUpdate(svd, prompter, true);
	}

	public void svnCompleteRepoInfo(SubversionData svd) {
		completeRepoInfo(svd);
	}

	public List<String> svnList(SubversionData svd) throws TaskFailedException {
		return list(svd);
	}

	public void svnLock(SubversionData svd, File toLock) throws TaskFailedException {
		svnLock(svd, toLock, prompter, true);
	}

	public void svnUnlock(SubversionData svd, File toUnLock) throws TaskFailedException {
		svnUnlock(svd, toUnLock, prompter, true);
	}

	public void svnUnlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		unlock(svd, toUnlock, authenticator, interactive);
	}

	public static void unlock(SubversionData svd, File toUnlock, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion unlock");
		try {

			SvnLog.info("Starting unlock");
			Svn.getSvnClient().setPrompt(authenticator);
			try {

				if (interactive) {
					handleAuthentication(authenticator);
				}
				Svn.getSvnClient().lock(new String[] { toUnlock.getAbsolutePath() }, svd.getUsername(), false);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished unlock for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished unlock");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public void svnLock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Svn.lock(svd, toLock, authenticator, interactive);
	}

	public static void lock(SubversionData svd, File toLock, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion lock");
		try {

			SvnLog.info("Starting lock");
			Svn.getSvnClient().setPrompt(authenticator);
			try {

				if (interactive) {
					handleAuthentication(authenticator);
				}
				Svn.getSvnClient().lock(new String[] { toLock.getAbsolutePath() }, svd.getUsername(), false);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished lock for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished lock");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	public static void doImport(SubversionData svd, PromptUserPassword3 authenticator, boolean interactive)
	throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		SvnLog.info("Acquiring svn write lock");
		rwl.writeLock().lock();
		long startTime = System.currentTimeMillis();
		I_ShowActivity activity = setupActivityPanel("Subversion import");
		try {

			SvnLog.info("Starting import");
			Svn.getSvnClient().setPrompt(authenticator);
			try {
				String message = "Importing " + svd.getRepositoryUrlStr();
				if (interactive && SvnPrompter.class.isAssignableFrom(authenticator.getClass())) {
					SvnPrompter p = (SvnPrompter) authenticator;
					message = p.askQuestion(svd.getRepositoryUrlStr(), "import message: ", message, true);
				}
				if (interactive) {
					handleAuthentication(authenticator);
				}
				String path = svd.getWorkingCopyStr();
				String url = svd.getRepositoryUrlStr();
				int depth = Depth.infinity;
				boolean noIgnore = false;
				boolean ignoreUnknownNodeTypes = false;
				Map<?, ?> revpropTable = null;
				Svn.getSvnClient().doImport(path, url, message, depth, noIgnore, ignoreUnknownNodeTypes, revpropTable);
			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished import for working copy: " + svd.getWorkingCopyStr() + "with exception: "
							+ e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
			SvnLog.info("finished import");
		} finally {
			rwl.writeLock().unlock();
			try {
				long elapsedTime = System.currentTimeMillis() - startTime;
				String elapsed = "Elapsed time: " + 
					TimeUtil.getElapsedTimeString(elapsedTime);
				activity.setProgressInfoLower(elapsed);
				activity.complete();
			} catch (ComputationCanceled e) {
				throw new TaskFailedException(e);
			}

		}
	}

	private static boolean supportReadOnlySwitch = false;

	private static void switchToReadOnlyMirror(SubversionData svd) throws TaskFailedException {
		if (!isConnectedToSvn()) {
			JOptionPane.showMessageDialog(null, "Skipping SVN task as not connected to SVN", "Not connected to SVN",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (svd.getWorkingCopyStr() == null) {
			return;
		}
		if (supportReadOnlySwitch) {
			SvnLog.info("Starting switch to read only");
			try {
				File workingDir = new File(svd.getWorkingCopyStr());
				File svnDir = new File(workingDir, ".svn");
				if (svnDir.exists()) {
					String currentRepo = normalizeEnding(getRepoInfo(svd).getUrl());
					String newRepo = normalizeEnding(svd.getPreferredReadRepository());
					if (currentRepo.equals(newRepo)) {
						SvnLog.info("Finished switch to read only: no change necessary");
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
					SvnLog.info(" switching from: " + getRepoInfo(svd).getUrl() + " to: " + url);
					long version =
						Svn.getSvnClient().doSwitch(path, url, revision, pegRevision, depth, depthIsSticky,
								ignoreExternals, allowUnverObstructions);
					SvnLog.info("Finished switch to read only at version: " + version);
				} else {
					SvnLog.info("Finished switch to read only: Not a working directory: " + workingDir);
				}

			} catch (ClientException e) {
				if (e.getAprError() != SVNErrorCode.CANCELLED.getCode()) {
					SvnLog.alertAndLog(e);
					SvnLog.info("finished switch to read only for working copy: " + svd.getWorkingCopyStr()
							+ "with exception: " + e.getLocalizedMessage());
					throw new TaskFailedException(e);
				}
			}
		}
	}

	private static String normalizeEnding(String currentRepo) {
		if (currentRepo.endsWith("/")) {
			currentRepo = currentRepo.substring(0, currentRepo.length() - 1);
		}
		return currentRepo;
	}

	public static boolean isConnectedToSvn() {
		return connectedToSvn;
	}

	public static void setConnectedToSvn(boolean connectedToSvn) {
		Svn.connectedToSvn = connectedToSvn;
		ACE.setSynchronizeButtonIsEnabled(connectedToSvn);
	}
}
