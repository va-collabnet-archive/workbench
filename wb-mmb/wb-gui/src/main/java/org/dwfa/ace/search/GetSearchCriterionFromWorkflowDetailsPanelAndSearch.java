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
package org.dwfa.ace.search;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.app.DwfaEnv;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class GetSearchCriterionFromWorkflowDetailsPanelAndSearch extends
		AbstractTask implements I_TrackContinuation, ActionListener {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 3;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE
			.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET
			.getAttachmentKey();
	private String resultSetPropName = ProcessAttachmentKeys.UUID_LIST_LIST
			.getAttachmentKey();

	private transient CountDownLatch conceptLatch;

	private transient int total;

	public transient String upperProgressMessage = "Performing search...";

	public transient String lowerProgressMessage = "progress: ";

	private transient boolean continueWork = true;

	private I_ConfigAceFrame config;

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.writeInt(dataVersion);
		out.writeObject(profilePropName);
		out.writeObject(positionSetPropName);
		out.writeObject(resultSetPropName);
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		int objDataVersion = in.readInt();
		if (objDataVersion <= dataVersion) {
			profilePropName = (String) in.readObject();
			if (objDataVersion >= 2) {
				positionSetPropName = (String) in.readObject();
			} else {
				positionSetPropName = ProcessAttachmentKeys.POSITION_SET
						.getAttachmentKey();
			}
			if (objDataVersion >= 3) {
				resultSetPropName = (String) in.readObject();
			} else {
				resultSetPropName = ProcessAttachmentKeys.UUID_LIST_LIST
						.getAttachmentKey();
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
		upperProgressMessage = "Performing search...";
		lowerProgressMessage = "progress: ";
		continueWork = true;
	}

	public String getProfilePropName() {
		return profilePropName;
	}

	public void setProfilePropName(String profilePropName) {
		this.profilePropName = profilePropName;
	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	@SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		try {
			total = -1;
			config = (I_ConfigAceFrame) process
					.getProperty(getProfilePropName());
			Set<I_Position> positionSet = (Set<I_Position>) process
					.getProperty(getPositionSetPropName());
			worker.getLogger().info("Position set for search: " + positionSet);
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			for (Component c : workflowDetailsSheet.getComponents()) {
				if (DifferenceSearchPanel.class.isAssignableFrom(c.getClass())) {
					DifferenceSearchPanel dsp = (DifferenceSearchPanel) c;
					total = Terms.get().getConceptCount();
					final I_RepresentIdSet matches = Terms.get()
							.getEmptyIdSet();
					conceptLatch = new CountDownLatch(total);
					if (DwfaEnv.isHeadless() == false) {
						new ProgressUpdator(matches);
					}
					I_ConfigAceFrame differenceSearchConfig = new DifferenceSearchConfig(
							config, positionSet);
					Terms.get().searchConcepts((I_TrackContinuation) this,
							matches, conceptLatch, dsp.getCriterion(),
							differenceSearchConfig);
					conceptLatch.await();
					worker.getLogger().info(
							"Search found: " + matches.cardinality()
									+ " matches.");
					List<List<UUID>> idListList = new ArrayList<List<UUID>>();
					I_IterateIds matchesIterator = matches.iterator();
					while (matchesIterator.next()) {
						I_GetConceptData matchConcept = Terms.get().getConcept(
								matchesIterator.nid());
						idListList.add(matchConcept.getUids());
					}
					process.setProperty(getResultSetPropName(), idListList);
					if (matches.cardinality() < 1000) {
						if (DwfaEnv.isHeadless() == false) {
							final I_ModelTerminologyList model = (I_ModelTerminologyList) config
									.getBatchConceptList().getModel();
							AceLog.getAppLog()
									.info(
											"Adding list of size: "
													+ idListList.size());

							SwingUtilities.invokeAndWait(new Runnable() {

								public void run() {
									I_IterateIds matchesIterator = matches
											.iterator();
									try {
										while (matchesIterator.next()) {
											I_GetConceptData conceptInList;
											conceptInList = Terms.get()
													.getConcept(
															matchesIterator
																	.nid());
											model.addElement(conceptInList);
										}
									} catch (TerminologyException e) {
										throw new RuntimeException(e);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}
								}

							});
							config.showListView();
						}
					} else {
						if (DwfaEnv.isHeadless() == false) {
							final I_ModelTerminologyList model = (I_ModelTerminologyList) config
									.getBatchConceptList().getModel();
							AceLog.getAppLog().info(
									"Adding first 5000 from a list of size: "
											+ idListList.size());

							SwingUtilities.invokeAndWait(new Runnable() {

								public void run() {
									I_IterateIds matchesIterator = matches
											.iterator();
									for (int i = 0; i < 5000; i++) {
										try {
											matchesIterator.next();
											I_GetConceptData conceptInList = Terms
													.get().getConcept(
															matchesIterator
																	.nid());
											model.addElement(conceptInList);
										} catch (TerminologyException e) {
											throw new RuntimeException(e);
										} catch (IOException e) {
											throw new RuntimeException(e);
										}
									}
								}

							});
							config.showListView();
						}
					}
					continueWork = false; // Search work is done...
					return Condition.CONTINUE;
				}
			}
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		throw new TaskFailedException("Could not find: DifferenceSearchPanel");
	}

	private class ProgressUpdator implements I_UpdateProgress {
		Timer updateTimer;

		boolean firstUpdate = true;

		ActivityPanel activity = new ActivityPanel(true, null, config);

		private long startTime;

		I_RepresentIdSet idSet;

		public ProgressUpdator(I_RepresentIdSet idSet) {
			super();
			this.idSet = idSet;
			updateTimer = new Timer(1000, this);
			activity
					.addActionListener(GetSearchCriterionFromWorkflowDetailsPanelAndSearch.this);
			actionPerformed(null);
			updateTimer.start();
			startTime = System.currentTimeMillis();
		}

		public void actionPerformed(ActionEvent e) {
			if (firstUpdate) {
				firstUpdate = false;
				try {
					ActivityViewer.addActivity(activity);
				} catch (Exception e1) {
					AceLog.getAppLog().alertAndLogException(e1);
				}
			}
			activity.setIndeterminate(total == -1);
			int processed = (int) (total - conceptLatch.getCount());
			activity.setValue(processed);
			activity.setMaximum(total);
			activity.setProgressInfoUpper(upperProgressMessage);
			lowerProgressMessage = "Matches: " + idSet.cardinality()
					+ " Concepts processed: " + processed;
			activity.setProgressInfoLower(lowerProgressMessage);
			if (!continueWork) {
				long endTime = System.currentTimeMillis() - startTime;
				upperProgressMessage = "Search complete";
				lowerProgressMessage = lowerProgressMessage
						+ " Elapsed seconds: " + endTime / 1000;
				activity.setProgressInfoUpper(upperProgressMessage);
				activity.setProgressInfoLower(lowerProgressMessage);
				activity.complete();
				updateTimer.stop();
			}
		}

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess,
	 *      org.dwfa.bpa.process.I_Work)
	 */
	public void complete(I_EncodeBusinessProcess process, I_Work worker)
			throws TaskFailedException {
		// Nothing to do

	}

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
	 */
	public Collection<Condition> getConditions() {
		return AbstractTask.CONTINUE_CONDITION;
	}

	public String getPositionSetPropName() {
		return positionSetPropName;
	}

	public void setPositionSetPropName(String positionSetPropName) {
		this.positionSetPropName = positionSetPropName;
	}

	public boolean continueWork() {
		return continueWork;
	}

	public String getResultSetPropName() {
		return resultSetPropName;
	}

	public void setResultSetPropName(String resultSetPropName) {
		this.resultSetPropName = resultSetPropName;
	}

	public void actionPerformed(ActionEvent e) {
		continueWork = false;
	}
}
