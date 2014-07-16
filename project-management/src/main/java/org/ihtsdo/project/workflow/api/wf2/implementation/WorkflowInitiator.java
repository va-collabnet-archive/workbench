package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WorkflowInitiatiorBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;

public class WorkflowInitiator implements WorkflowInitiatiorBI {

	public static Map<Integer, LruCache> alreadySeen;
//	private static ConceptChronicleBI rootConcept;
	private static WorkflowStoreBI wfStore;
	private PropertyChangeEvent myEvt;
	private static Set<ConceptChronicleBI> nonRootConcepts = new HashSet<ConceptChronicleBI>();


	public WorkflowInitiator() {
		alreadySeen = new HashMap<Integer,LruCache>();
	}

	@Override
	public boolean evaluateForWorkflowInitiation(
			PropertyChangeEvent event) throws Exception {
		myEvt = event;
		I_GetConceptData workflow = Terms.get().getActiveAceFrameConfig().getDefaultWorkflowForChangedConcept();
		final Integer workflowNid;
		if (workflow == null) {
			workflowNid = 0;
		} else {
			workflowNid = workflow.getNid();
		}

		if (alreadySeen.get(workflowNid) == null) {
			alreadySeen.put(workflowNid, new LruCache<Integer, Long>(1000));
		}


		ConceptChronicleBI concept=null;
		try {
			I_RepresentIdSet idSet=(I_RepresentIdSet)myEvt.getNewValue();
			if (idSet!=null){
				boolean individualCommit = (idSet.cardinality() < 10);
				NidBitSetItrBI possibleItr = idSet.iterator();
				while (possibleItr.next()) {
					System.out.println("AlreadySeen: " + alreadySeen.get(workflowNid).containsKey(possibleItr.nid()));
					long timeDiff = Long.MIN_VALUE;
					if (alreadySeen.get(workflowNid).containsKey(possibleItr.nid())) {
						timeDiff = System.currentTimeMillis() - (Long) alreadySeen.get(workflowNid).get(possibleItr.nid());
						System.out.println("Diff cache time: " + timeDiff);
					}
					alreadySeen.get(workflowNid).put(possibleItr.nid(), System.currentTimeMillis());
					
					if ( timeDiff == Long.MIN_VALUE || timeDiff >  12000) {
						concept=Ts.get().getConcept(possibleItr.nid());
						if (concept!=null){
							System.out.println("Sending to workflow: " + concept.toString());
							addComponentToDefaultWorklist(concept, individualCommit);
						}
					}
				}

				if (!individualCommit) {
					Ts.get().commit();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CancellationException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private static boolean addComponentToDefaultWorklist(
			ConceptChronicleBI concept, boolean commit) {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();

//			if (rootConcept == null) {
//				rootConcept = Ts.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
//			}
			
			if (nonRootConcepts.isEmpty()) {
				nonRootConcepts.add(Ts.get().getConcept(UUID.fromString("1c698388-c309-3dfa-96f0-86248753fac5")));
				nonRootConcepts.add(Ts.get().getConcept(UUID.fromString("f4d2fabc-7e96-3b3a-a348-ae867ba74029")));
			}

			boolean nonRootConceptModification = false;
			for (ConceptChronicleBI nonRoot : nonRootConcepts) {
				if (Ts.get().isKindOf(concept.getConceptNid(), nonRoot.getConceptNid(), config.getViewCoordinate())) {
					nonRootConceptModification = true;
				}
			}

			if (!nonRootConceptModification) {

				I_GetConceptData worklistConcept = config.getDefaultWorkflowForChangedConcept();
				I_GetConceptData projectConcept = config.getDefaultProjectForChangedConcept();

				//System.out.println("worklistConcept: " + worklistConcept);

				if (worklistConcept==null){
					return false;
				}
				if (wfStore==null){
					wfStore=new WorkflowStore();
				}
				WorkList workList = (WorkList) wfStore.getWorklist(worklistConcept.getPrimUuid());
				Collection<WfProcessInstanceBI> instances = wfStore.getProcessInstances(concept.getPrimUuid());

				boolean alreadyActiveInProject = false;
				WfProcessInstanceBI completeInstanceInSameWorklist = null;

				for (WfProcessInstanceBI loopInstance : instances) {
					if (!loopInstance.isCompleted()){
						I_TerminologyProject loopProject = TerminologyProjectDAO.getProjectForWorklist((WorkList) loopInstance.getWorkList(), config);
						if (projectConcept.getNid() == loopProject.getId()) {
							alreadyActiveInProject = true;
							break;
						}
					} else {
						if (loopInstance.getWorkList().getUuid().equals(workList.getUuid())) {
							completeInstanceInSameWorklist = loopInstance;
						}
					}
				}

//				WfProcessInstanceBI instance = wfStore.getProcessInstance(workList, concept.getPrimUuid());
//
//				System.out.println("instance: " + instance);
//				System.out.println("instance.isActive(): " + instance.isActive());
//				System.out.println("instance.isCompleted(): " + instance.isCompleted());

				if ( alreadyActiveInProject ){ //instance!=null
					return false;
				}
				
				if (completeInstanceInSameWorklist != null && 
						(System.currentTimeMillis() - completeInstanceInSameWorklist.getLastChangeTime()) > 3000 ) {
					//I_GetConceptData assingStatus = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids());
					I_GetConceptData assingStatus;
			        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
			        	assingStatus = Terms.get().getConcept(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"));
			        } else {
			        	assingStatus = Terms.get().getConcept(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"));
			        }
			        completeInstanceInSameWorklist.setState(new WfState(assingStatus.toString(), assingStatus.getPrimUuid()));
					if (commit) {
						concept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
					}
				} else {
					workList.createInstanceForComponent(concept.getPrimUuid(), new WfProcessDefinition(workList.getWorkflowDefinition()), commit);
				}

				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return false;
	}

	private class LruCache<A, B> extends LinkedHashMap<A, B> {
		private final int maxEntries;

		public LruCache(final int maxEntries) {
			super(maxEntries + 1, 1.0f, true);
			this.maxEntries = maxEntries;
		}

		/**
		 * Returns <tt>true</tt> if this <code>LruCache</code> has more entries than the maximum specified when it was
		 * created.
		 *
		 * <p>
		 * This method <em>does not</em> modify the underlying <code>Map</code>; it relies on the implementation of
		 * <code>LinkedHashMap</code> to do that, but that behavior is documented in the JavaDoc for
		 * <code>LinkedHashMap</code>.
		 * </p>
		 *
		 * @param eldest
		 *            the <code>Entry</code> in question; this implementation doesn't care what it is, since the
		 *            implementation is only dependent on the size of the cache
		 * @return <tt>true</tt> if the oldest
		 * @see java.util.LinkedHashMap#removeEldestEntry(Map.Entry)
		 */
		@Override
		protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
			return super.size() > maxEntries;
		}
	}


}
