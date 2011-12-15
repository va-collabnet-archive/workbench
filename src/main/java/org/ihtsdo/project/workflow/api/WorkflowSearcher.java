package org.ihtsdo.project.workflow.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.refset.RefsetSpecPanel.MemberSelectionListener;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.filters.WfWorklistFilter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorklistPage;

public class WorkflowSearcher {

	private static I_TermFactory tf;
	private List<WfSearchFilterBI> filters;
	private WorklistPage page;
	private I_ConfigAceFrame config;
	private WfComponentProvider provider;

	public WorkflowSearcher() {
		super();
		try {
			provider = new WfComponentProvider();
			tf = Terms.get();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public HashMap<WorkList, Integer> getAllWorklistsCount() throws TerminologyException, IOException {
		HashMap<WorkList, Integer> result = new HashMap<WorkList, Integer>();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList loopWorkList : worklists) {
					List<WorkListMember> members = TerminologyProjectDAO.getAllWorkListMembers(loopWorkList, config);
					result.put(loopWorkList, members.size());
				}
			}
		}
		return result;
	}

	public HashMap<WfState, Integer> getWorklistMembersCountByState() throws IOException, TerminologyException {
		HashMap<WfState, Integer> result = new HashMap<WfState, Integer>();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList loopWorkList : worklists) {
					HashMap<I_GetConceptData, Integer> members = TerminologyProjectDAO.getWorkListMemberStatuses(loopWorkList, config);
					Set<I_GetConceptData> keys = members.keySet();
					for (I_GetConceptData wl : keys) {
						WfState wfState = new WfState(wl.getInitialText(), wl.getPrimUuid());
						if (result.containsKey(wfState)) {
							Integer x = result.get(wfState);
							result.put(wfState, x + members.get(wl));
						} else {
							result.put(wfState, members.get(wl));
						}
					}
				}
			}
		}
		return result;
	}

	public List<WfInstance> getAllWrokflowInstancesForWorklist(List<UUID> wlUuid) throws TerminologyException, IOException {
		List<WorkList> worklist = getWorklistForUUID(wlUuid);
		List<WfInstance> result = new ArrayList<WfInstance>();
		convertWlMembers(worklist, result);
		return result;
	}

	private List<WorkList> getWorklistForUUID(List<UUID> wlUuids) throws TerminologyException, IOException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		List<WorkList> worklist = new ArrayList<WorkList>();
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList loopWorkList : worklists) {
					for (UUID wlUUID : wlUuids) {
						if (loopWorkList.getUids().contains(wlUUID)) {
							worklist.add(loopWorkList);
							break;
						}
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

	public HashMap<WorkList, Integer> getUserWorklists(WfUser user) {
		HashMap<WorkList, Integer> result = new HashMap<WorkList, Integer>();
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
			for (I_TerminologyProject i_TerminologyProject : projects) {
				List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
				for (WorkSet workSet : worksets) {
					List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList workList : worklists) {
						
						List<WfUser> users = workList.getUsers();
						if (true/**users.contains(user)*/) {
							Integer wlMembersSize = getWlMembersSize(workList, user);
							if(wlMembersSize > 0)
							result.put(workList, wlMembersSize);
						}
					}
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	private Integer getWlMembersSize(WorkList workList, WfUser user) {
		List<WorkListMember> allWorkListMembers = TerminologyProjectDAO.getAllWorkListMembers(workList, config);
		int size = 0;
		for (WorkListMember workListMember : allWorkListMembers) {
			if(workListMember.getWfInstance().getDestination().equals(user)){
				size++;
			}
		}
		return size;
	}

	public HashMap<WfState, Integer> getUserStatusList(WfUser user) {
		HashMap<WfState, Integer> result = new HashMap<WfState, Integer>();
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
			for (I_TerminologyProject i_TerminologyProject : projects) {
				List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
				for (WorkSet workSet : worksets) {
					List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList workList : worklists) {

						List<WorkListMember> members = TerminologyProjectDAO.getAllWorkListMembers(workList, config);
						HashMap<I_GetConceptData, Integer> workListMembersStatuses = new HashMap<I_GetConceptData, Integer>();

						for (WorkListMember loopMember : members) {
							if (loopMember.getWfInstance().getDestination().equals(user)) {
								I_GetConceptData activityStatus = tf.getConcept(loopMember.getActivityStatus());
								Integer currentCount = workListMembersStatuses.get(activityStatus);
								if (currentCount == null)
									currentCount = 0;
								workListMembersStatuses.put(activityStatus, currentCount + 1);
							}
						}
						Set<I_GetConceptData> keys = workListMembersStatuses.keySet();
						for (I_GetConceptData wlstatus : keys) {
							WfState state = provider.statusConceptToWfState(wlstatus);
							result.put(state, workListMembersStatuses.get(wlstatus));
						}
					}
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void randomModifyWlMembers() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			WfComponentProvider provider = new WfComponentProvider();

			List<WfUser> users = provider.getUsers();
			Random userRnd = new Random();

			List<WfState> states = provider.getStates();
			Random stateRnd = new Random();

			List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
			for (I_TerminologyProject i_TerminologyProject : projects) {
				List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
				for (WorkSet workSet : worksets) {
					List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList workList : worklists) {
						List<WorkListMember> members = TerminologyProjectDAO.getAllWorkListMembers(workList, config);
						PromotionAndAssignmentRefset prom = workList.getPromotionRefset(config);
						for (WorkListMember workListMember : members) {
							prom.setDestination(workListMember.getId(), Terms.get().uuidToNative(users.get(userRnd.nextInt(users.size())).getId()));
							prom.setPromotionStatus(workListMember.getId(), Terms.get().uuidToNative(states.get(stateRnd.nextInt(states.size())).getId()));
						}
					}
				}
			}
			Terms.get().commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<WfInstance> searchWfInstances(List<WfSearchFilterBI> filters) throws TerminologyException, IOException {

		List<WfInstance> candidates = new ArrayList<WfInstance>();
		List<WfInstance> results = new ArrayList<WfInstance>();

		List<UUID> wlUuid = null;
		for (WfSearchFilterBI loopFilter : filters) {
			if (loopFilter instanceof WfWorklistFilter) {
				WfWorklistFilter wlFilter = (WfWorklistFilter) loopFilter;
				wlUuid = wlFilter.getWorklistUUID();
			}
		}

		if (wlUuid != null) {
			candidates = getAllWrokflowInstancesForWorklist(wlUuid);
		} else {
			candidates = getAllWrokflowInstances();
		}

		for (WfInstance loopInstance : candidates) {
			boolean accepted = true;
			for (WfSearchFilterBI loopFilter : filters) {
				if (!loopFilter.filter(loopInstance)) {
					accepted = false;
					break;
				}
			}
			if (accepted) {
				results.add(loopInstance);
			}
		}
		return results;
	}

}
