package org.ihtsdo.project.workflow.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.api.Precedence;

public class WfComponentProvider {

	public List<WfUser> getUsers() {
		List<WfUser> wfUsers = new ArrayList<WfUser>();
		try {
			I_GetConceptData roleParent =
				Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getUids());

			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();

			Set<? extends I_GetConceptData> allUsers = roleParent.getDestRelOrigins(Terms.get().getActiveAceFrameConfig().getAllowedStatus(),
					allowedTypes, Terms.get().getActiveAceFrameConfig().getViewPositionSetReadOnly(), Precedence.TIME,
					Terms.get().getActiveAceFrameConfig().getConflictResolutionStrategy());

			for (I_GetConceptData user : allUsers) {

				I_ConceptAttributeVersioned attr = user.getConceptAttributes();
				if (TerminologyProjectDAO.isActive(attr.getStatusNid())){
					wfUsers.add(new WfUser(attr.toUserString(),user.getUids().iterator().next(),null));

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return wfUsers;
	}

	public List<WfInstance> getAllWrokflowInstancesForWorklist(List<UUID> wlUuid) throws TerminologyException, IOException {
		List<WorkList> worklist = getWorklistForUUID(wlUuid);
		List<WfInstance> result = new ArrayList<WfInstance>();
		convertWlMembers(worklist, result);
		return result;
	}

	private List<WorkList> getWorklistForUUID(List<UUID> wlUuid) throws TerminologyException, IOException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		List<WorkList> worklist = null;
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList loopWorkList : worklists) {
					if (loopWorkList.getUids().contains(wlUuid)) {
						worklist.add(loopWorkList);
					}
				}
			}
		}
		return worklist;
	}

	public List<WfInstance> getAllWrokflowInstances() throws TerminologyException, IOException {
		List<WfInstance> result = new ArrayList<WfInstance>();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				convertWlMembers(worklists, result);
			}
		}
		return result;
	}

	private void convertWlMembers(List<WorkList> worklist, List<WfInstance> result) throws TerminologyException, IOException {
		for (WorkList wl : worklist) {
			List<WorkListMember> wlMembers = wl.getWorkListMembers();
			for (WorkListMember workListMember : wlMembers) {
				result.add(workListMember.getWfInstance());
			}
		}
	}

	public List<WfRole> getRoles() {
		List<WfRole> wfRole = new ArrayList<WfRole>();

		return wfRole;
	}

	public List<WfPermission> getPermissions() {
		List<WfPermission> wfPermission = new ArrayList<WfPermission>();

		return wfPermission;
	}

	public List<WfState> getStates() {
		List<WfState> wfState = new ArrayList<WfState>();

		return wfState;
	}

	public WfState statusConceptToWfState(I_GetConceptData status) {
		WfState state = null;
		try {
			state = new WfState(status.getInitialText(), status.getPrimUuid());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return state;
	}

	public WfUser userConceptToWfUser(I_GetConceptData user) {
		WfUser wfUser = null;
		try {
			wfUser = new WfUser(user.getInitialText(), user.getPrimUuid(), getPermissions());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wfUser;
	}
	private static final String END_FILE = ".wfd";
	public static List<File> getWorkflowDefinitionFiles(){
		File folder=new File("./sampleProcesses");
		List<File> retFiles=loadFiles( folder,END_FILE);
		return retFiles;
	}

	private static List<File> loadFiles(File folder,String endFile) {
		File[] fileList = folder.listFiles();
		List<File> retFiles=new ArrayList<File>();
		for (File file : fileList) {
			if (file.isDirectory()) {
				List<File>tmpFiles=loadFiles(file,endFile);
				retFiles.addAll(tmpFiles);
			} else {
				if(!file.isHidden() && file.getName().toLowerCase().endsWith(endFile)){
					retFiles.add(file);
				}
			}
		}
		return retFiles;
	}

}
