package org.dwfa.ace.search;

import java.awt.Component;
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

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntIterator;
import org.apache.commons.collections.primitives.IntList;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.vodb.VodbEnv;
import org.dwfa.vodb.types.ConceptBean;

@BeanList(specs = { @Spec(directory = "tasks/ide/gui/workflow/detail sheet", type = BeanType.TASK_BEAN) })
public class GetSearchCriterionFromWorkflowDetailsPanelAndSearch extends AbstractTask implements I_TrackContinuation {
	private static final long serialVersionUID = 1;

	private static final int dataVersion = 3;

	private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();
	private String positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
	private String resultSetPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
	
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
				positionSetPropName = ProcessAttachmentKeys.POSITION_SET.getAttachmentKey();
			}
			if (objDataVersion >= 3) {
				resultSetPropName = (String) in.readObject();
			} else {
				resultSetPropName = ProcessAttachmentKeys.UUID_LIST_LIST.getAttachmentKey();
			}
		} else {
			throw new IOException("Can't handle dataversion: " + objDataVersion);
		}
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
	public Condition evaluate(I_EncodeBusinessProcess process,
			I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) process.readProperty(getProfilePropName());
			Set<I_Position> positionSet = (Set<I_Position>) process.readProperty(getPositionSetPropName());
			worker.getLogger().info("Position set for search: " + positionSet);
			JPanel workflowDetailsSheet = config.getWorkflowDetailsSheet();
			for (Component c: workflowDetailsSheet.getComponents()) {
				if (DifferenceSearchPanel.class.isAssignableFrom(c.getClass())) {
					final IntList matches = new ArrayIntList();
					DifferenceSearchPanel dsp = (DifferenceSearchPanel) c;
					
					CountDownLatch conceptLatch = new CountDownLatch(LocalVersionedTerminology.get().getConceptCount());
					I_ConfigAceFrame differenceSearchConfig = new DifferenceSearchConfig(config,
							positionSet);
					AceConfig.getVodb().searchConcepts((I_TrackContinuation) this, matches,
							conceptLatch, dsp.getCriterion(),
							differenceSearchConfig);
					conceptLatch.await();
					worker.getLogger().info("Search found: " + matches.size() + " matches.");
					List<List<UUID>> idListList = new ArrayList<List<UUID>>();
					IntIterator matchesIterator = matches.iterator();
					while (matchesIterator.hasNext()) {
						ConceptBean matchConcept = ConceptBean.get(matchesIterator.next());
						idListList.add(matchConcept.getUids());
					}
					process.setProperty(getResultSetPropName(), idListList);
					if (matches.size() < 1000) {
						if (VodbEnv.isHeadless() == false) {
					         final I_ModelTerminologyList model = (I_ModelTerminologyList) config.getBatchConceptList().getModel();
					         AceLog.getAppLog().info("Adding list of size: " + idListList.size());
					         
					         SwingUtilities.invokeAndWait(new Runnable() {

					            public void run() {
					            	IntIterator matchesIterator = matches.iterator();
					            	while (matchesIterator.hasNext()) {
					                        I_GetConceptData conceptInList = ConceptBean.get(matchesIterator.next());
					                        model.addElement(conceptInList);
					                 }
					            }
					             
					         });
					         config.showListView();
						}				         
					} else {
						if (VodbEnv.isHeadless() == false) {
					         final I_ModelTerminologyList model = (I_ModelTerminologyList) config.getBatchConceptList().getModel();
					         AceLog.getAppLog().info("Adding first 1000 from a list of size: " + idListList.size());
					         
					         SwingUtilities.invokeAndWait(new Runnable() {

					            public void run() {
					            	for (int i = 0; i < 1000; i++) {
					                    I_GetConceptData conceptInList = ConceptBean.get(matches.get(i));
					                    model.addElement(conceptInList);
					                 }
					            }
					             
					         });
					         config.showListView();
						}				         
					}
					return Condition.CONTINUE;
				}
			}
		} catch (Exception e) {
			throw new TaskFailedException(e);
		}
		throw new TaskFailedException("Could not find: DifferenceSearchPanel");
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
		return true;
	}

	public String getResultSetPropName() {
		return resultSetPropName;
	}

	public void setResultSetPropName(String resultSetPropName) {
		this.resultSetPropName = resultSetPropName;
	}

}
