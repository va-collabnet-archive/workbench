package org.ihtsdo.project.workflow.api.wf2.implementation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.workflow2.WfFilterBI;
import org.ihtsdo.project.workflow2.WfProcessInstanceBI;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class WfInstanceSearcher extends SwingWorker<List<WfProcessInstanceBI>, WfProcessInstanceBI> implements ProcessUnfetchedConceptDataBI {

	private List<WfProcessInstanceBI> result = null;
	private boolean done = false;
	private List<WfFilterBI> filters;
	private WfInstanceContainer wfinstanceCont;
	private I_ConfigAceFrame config;

	// public CountDownLatch createWfInstances(
	// final ContinuationTrackerBI tracker,
	// WorkflowInstanceTableModel model, final List<WfFilterBI> checkList,
	// I_ConfigAceFrame config) throws IOException {
	// try {
	// WFInstanceCreator processor = new WFInstanceCreator(tracker,
	// checkList, config, model);
	// Ts.get().iterateConceptDataInParallel(processor);
	// } catch (Exception e) {
	// throw new IOException();
	// }
	// return new CountDownLatch(0);
	// }

	public WfInstanceSearcher(List<WfFilterBI> filters, WfInstanceContainer wfinstanceCont) {
		super();
		this.filters = filters;
		this.wfinstanceCont = wfinstanceCont;
	}

	@Override
	protected List<WfProcessInstanceBI> doInBackground() throws Exception {
		this.result = new ArrayList<WfProcessInstanceBI>();
		Ts.get().iterateConceptDataInParallel(this);
		done = true;
		return result;
	}

	@Override
	public boolean continueWork() {
		return !isCancelled() && !done;
	}

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return Terms.get().getConceptNidSet();
	}

	@Override
	public void processUnfetchedConceptData(int arg0, ConceptFetcherBI fetcher) throws Exception {
		try {
			ConceptChronicleBI concept = fetcher.fetch();
			if (this.continueWork()) {
				Workflow wf = new Workflow();
				Collection<WfProcessInstanceBI> wfinstances = wf.getProcessInstances(concept.getVersion(config.getViewCoordinate()));
				for (WfProcessInstanceBI wfProcessInstanceBI : wfinstances) {
					boolean apply = true;
					for (WfFilterBI filter : filters) {
						if (filter.evaluateInstance(wfProcessInstanceBI)) {
							apply = false;
							break;
						}
					}
					if (!apply) {
						continue;
					}
					publish(wfProcessInstanceBI);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	protected void process(List<WfProcessInstanceBI> chunks) {
		for (WfProcessInstanceBI wfProcessInstanceBI : chunks) {
			wfinstanceCont.addWfInstance(wfProcessInstanceBI);
		}
	}
	
	// private static class WFInstanceCreator implements
	// ProcessUnfetchedConceptDataBI {
	//
	// private List<WfFilterBI> checkList;
	// private static I_ConfigAceFrame config;
	// private WorkflowInstanceTableModel matches;
	// private NidBitSetBI nidSet;
	// private ContinuationTrackerBI tracker;
	// private int i = 0;
	// private boolean continueW = true;
	// private WorkflowDefinition wfDef;
	// private WorkList workList;
	// private static WorkflowInterpreter wfInt;
	//
	// // ~--- constructors
	// // -----------------------------------------------------
	// public WFInstanceCreator(ContinuationTrackerBI tracker, List<WfFilterBI>
	// checkList, I_ConfigAceFrame config, WorkflowInstanceTableModel matches2)
	// throws IOException {
	// super();
	// this.tracker = tracker;
	// this.checkList = checkList;
	// this.config = config;
	// this.matches = matches2;
	// this.nidSet = Bdb.getConceptDb().getConceptNidSet();
	//
	// wfDef = readWfDefinition(new
	// File("sampleProcesses/canada-fast-track-wf.wfd"));
	//
	// List<I_TerminologyProject> projects =
	// TerminologyProjectDAO.getAllProjects(config);
	// I_TerminologyProject porject = projects.get(0);
	//
	// List<WorkSet> wss =
	// TerminologyProjectDAO.getAllWorkSetsForProject(porject, config);
	//
	// WorkSet ws = wss.get(0);
	// System.out.println(ws.getName());
	//
	// List<PartitionScheme> pss = ws.getPartitionSchemes(config);
	//
	// PartitionScheme ps = pss.get(0);
	//
	// List<Partition> pns = ps.getPartitions();
	// Partition pn = pns.get(0);
	// try {
	// TerminologyProjectDAO.createNewNacWorkList(porject, wfDef,
	// getWorkflowMembers(wfDef), "nacworklist", config, null);
	// List<WorkList> allnacwl =
	// TerminologyProjectDAO.getAllNacWorkLists(porject, config);
	// for (WorkList workList : allnacwl) {
	// if (workList.getName().equals("nacworklist")) {
	// this.workList = workList;
	// }
	// }
	//
	// wfInt = WorkflowInterpreter.createWorkflowInterpreter(wfDef);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// // ~--- methods
	// // ----------------------------------------------------------
	// @Override
	// public boolean continueWork() {
	// return continueW;
	// }
	//
	// // ~--- get methods
	// // ------------------------------------------------------
	// public NidBitSetBI getNidSet() {
	// return nidSet;
	// }
	//
	// @Override
	// public void processUnfetchedConceptData(int arg0, ConceptFetcherBI
	// fetcher) throws Exception {
	// if (continueW) {
	// i++;
	// if (i == 30) {
	// continueW = false;
	// }
	// try {
	// ConceptChronicleBI concept = fetcher.fetch();
	// if (concept.toUserString().contains("Accident caused by angle grinder"))
	// {
	// I_GetConceptData assingStatus =
	// Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids());
	// Long statusDate = System.currentTimeMillis();
	// WorkListMember wlmemb = new WorkListMember(concept.toUserString(),
	// concept.getConceptNid(), concept.getUUIDs(), workList.getUuid(),
	// assingStatus, statusDate);
	// TerminologyProjectDAO.addConceptAsWorkListMember(wlmemb,
	// Terms.get().uuidToNative(workList.getUsers().iterator().next().getId()),
	// config);
	// Terms.get().commit();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw e;
	// }
	// } else {
	//
	// }
	// }
	//
	// /**
	// * Gets the workflow members.
	// *
	// * @param wfDef
	// * the wf def
	// * @return the workflow members
	// * @throws IOException
	// */
	// private static ArrayList<WfMembership>
	// getWorkflowMembers(WorkflowDefinition wfDef) throws IOException {
	// ArrayList<WfMembership> members = new ArrayList<WfMembership>();
	// I_GetConceptData userConcept = config.getDbConfig().getUserConcept();
	// WfUser loopuser = new WfUser(userConcept.getInitialText(),
	// userConcept.getPrimUuid());
	// for (WfRole loopRole : wfDef.getRoles()) {
	// WfPermission loopPermission = new WfPermission(UUID.randomUUID(),
	// loopRole, UUID.randomUUID());
	// List<WfPermission> listPerm = new ArrayList<WfPermission>();
	// listPerm.add(loopPermission);
	// loopuser.setPermissions(listPerm);
	//
	// members.add(new WfMembership(UUID.randomUUID(), loopuser, loopRole,
	// true));
	// }
	// return members;
	// }
	//
	// /**
	// * Read wf definition.
	// *
	// * @param file
	// * the file
	// * @return the workflow definition
	// */
	// public static WorkflowDefinition readWfDefinition(File file) {
	// XStream xStream = new XStream(new DomDriver());
	// WorkflowDefinition wfDef = (WorkflowDefinition) xStream.fromXML(file);
	// return wfDef;
	// }
	// }

}
