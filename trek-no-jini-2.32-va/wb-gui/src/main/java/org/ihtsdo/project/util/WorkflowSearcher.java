/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.project.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.filter.WfSearchFilterBI;
import org.ihtsdo.project.filter.WfWorklistFilter;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfState;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorklistPage;
import org.ihtsdo.tk.workflow.api.WfFilterBI;

/**
 * The Class WorkflowSearcher.
 */
public class WorkflowSearcher {

	/** The tf. */
	private static I_TermFactory tf;

	/** The filters. */
	private List<WfSearchFilterBI> filters;

	/** The page. */
	private WorklistPage page;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The provider. */
	private WfComponentProvider provider;

	/**
	 * Instantiates a new workflow searcher.
	 */
	public WorkflowSearcher() {
		super();
		try {
			provider = new WfComponentProvider();
			tf = Terms.get();
			if (tf != null) {
				config = tf.getActiveAceFrameConfig();
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the all worklists count.
	 * 
	 * @return the all worklists count
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the worklist members count by state.
	 * 
	 * @return the worklist members count by state
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	public HashMap<WfState, Integer> getWorklistMembersCountByState(List<WfFilterBI> filters) throws IOException, TerminologyException {
		HashMap<WfState, Integer> result = new HashMap<WfState, Integer>();
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
		for (I_TerminologyProject i_TerminologyProject : projects) {
			List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
			for (WorkSet workSet : worksets) {
				List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
				for (WorkList loopWorkList : worklists) {
					HashMap<I_GetConceptData, Integer> members = TerminologyProjectDAO.getWorkListMemberStatuses(loopWorkList, config, filters);
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

	/**
	 * Gets the all wrokflow instances for worklist.
	 * 
	 * @param wlUuid
	 *            the wl uuid
	 * @return the all wrokflow instances for worklist
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<WfInstance> getAllWrokflowInstancesForWorklist(List<UUID> wlUuid) throws TerminologyException, IOException {
		List<WorkList> worklist = getWorklistForUUID(wlUuid);
		List<WfInstance> result = new ArrayList<WfInstance>();
		convertWlMembers(worklist, result);
		return result;
	}

	/**
	 * Gets the worklist for uuid.
	 * 
	 * @param wlUuids
	 *            the wl uuids
	 * @return the worklist for uuid
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the all wrokflow instances.
	 * 
	 * @return the all wrokflow instances
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Convert wl members.
	 * 
	 * @param worklist
	 *            the worklist
	 * @param result
	 *            the result
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void convertWlMembers(List<WorkList> worklist, List<WfInstance> result) throws TerminologyException, IOException {
		for (final WorkList wl : worklist) {
			WorkflowInterpreter.createWorkflowInterpreter(wl.getWorkflowDefinition());
			List<WorkListMember> wlMembers = wl.getWorkListMembers();
			for (WorkListMember workListMember : wlMembers) {
				result.add(workListMember.getWfInstance());
			}
		}
	}

	/**
	 * Gets the user worklists.
	 * 
	 * @param user
	 *            the user
	 * @return the user worklists
	 */
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
						if (true/** users.contains(user) */
						) {
							Integer wlMembersSize = getWlMembersSize(workList, user);
							if (wlMembersSize > 0) {
								result.put(workList, wlMembersSize);
							}
						}
					}
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * This is the union of getUserWorklists and getUserStatusList.. <BR>
	 * it takes advantage of the worklist members loop to calculate wl size and
	 * states size.
	 * 
	 * @param user
	 *            the user
	 * @return the count by worklist and state
	 */
	public HashMap<Object, Integer> getCountByWorklistAndState(WfUser user, List<WfFilterBI> filters) {
		HashMap<Object, Integer> result = new HashMap<Object, Integer>();
		I_ConfigAceFrame config;
		try {

			List<String[]> outboxTodoTaguuids = new ArrayList<String[]>();
			InboxTag outboxTag = TagManager.getInstance().getTagContent(TagManager.OUTBOX);
			InboxTag todoTag = TagManager.getInstance().getTagContent(TagManager.TODO);
			outboxTodoTaguuids.addAll(outboxTag.getUuidList());
			outboxTodoTaguuids.addAll(todoTag.getUuidList());

			config = Terms.get().getActiveAceFrameConfig();
			List<I_TerminologyProject> projects = TerminologyProjectDAO.getAllProjects(config);
			for (I_TerminologyProject i_TerminologyProject : projects) {
				List<WorkSet> worksets = TerminologyProjectDAO.getAllWorkSetsForProject(i_TerminologyProject, config);
				for (WorkSet workSet : worksets) {
					List<WorkList> worklists = TerminologyProjectDAO.getAllWorklistForWorkset(workSet, config);
					for (WorkList workList : worklists) {
						HashMap<I_GetConceptData, Integer> workListMembersStatuses = new HashMap<I_GetConceptData, Integer>();
						List<WfUser> users = workList.getUsers();
						if (users.contains(user)) {
							List<WorkListMember> allWorkListMembers = TerminologyProjectDAO.getAllWorkListMembers(workList, config);
							int size = 0;
							for (WorkListMember workListMember : allWorkListMembers) {
								boolean passed = true;
								for (WfFilterBI filter : filters) {
									if (!filter.evaluateInstance(workListMember.getWfInstance())) {
										passed = false;
									}
								}
								if(!passed){
									continue;
								}
								if (workListMember.getWfInstance().getDestination().equals(user)) {
									String conceptUuid = workListMember.getConcept().getPrimUuid().toString();
									String worklistUuid = workListMember.getWfInstance().getWorkList().getUids().iterator().next().toString();
									boolean contains = false;
									for (String[] outboxTodoUuids : outboxTodoTaguuids) {
										if (outboxTodoUuids[InboxTag.TERM_WORKLIST_UUID_INDEX].equals(worklistUuid)
												&& outboxTodoUuids[InboxTag.TERM_UUID_INDEX].equals(conceptUuid)) {
											contains = true;
										}
									}
									if (!contains) {
										size++;
										I_GetConceptData activityStatus = workListMember.getActivityStatus();
										Integer currentCount = workListMembersStatuses.get(activityStatus);
										if (currentCount == null) {
											currentCount = 0;
										}
										workListMembersStatuses.put(activityStatus, currentCount + 1);
									}
								}
							}

							Set<I_GetConceptData> keys = workListMembersStatuses.keySet();
							for (I_GetConceptData wlstatus : keys) {
								WfState state = provider.statusConceptToWfState(wlstatus);
								if (result.containsKey(state)) {
									int current = result.get(state);
									current += workListMembersStatuses.get(wlstatus);
									result.put(state, current);
								} else {
									result.put(state, workListMembersStatuses.get(wlstatus));
								}
							}

							result.put(workList, size);
						}
					}
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return result;
	}

	/**
	 * Gets the wl members size.
	 * 
	 * @param workList
	 *            the work list
	 * @param user
	 *            the user
	 * @return the wl members size
	 */
	private Integer getWlMembersSize(WorkList workList, WfUser user) {
		List<WorkListMember> allWorkListMembers = TerminologyProjectDAO.getAllWorkListMembers(workList, config);
		int size = 0;
		for (WorkListMember workListMember : allWorkListMembers) {
			if (workListMember.getWfInstance().getDestination().equals(user)) {
				size++;
			}
		}
		return size;
	}

	/**
	 * Gets the user status list.
	 * 
	 * @param user
	 *            the user
	 * @return the user status list
	 */
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
								I_GetConceptData activityStatus = loopMember.getActivityStatus();
								Integer currentCount = workListMembersStatuses.get(activityStatus);
								if (currentCount == null)
									currentCount = 0;
								workListMembersStatuses.put(activityStatus, currentCount + 1);
							}
						}
						Set<I_GetConceptData> keys = workListMembersStatuses.keySet();
						for (I_GetConceptData wlstatus : keys) {
							WfState state = provider.statusConceptToWfState(wlstatus);
							if (result.containsKey(state)) {
								int current = result.get(state);
								current += workListMembersStatuses.get(wlstatus);
								result.put(state, current);
							} else {
								result.put(state, workListMembersStatuses.get(wlstatus));
							}
						}
					}
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Random modify wl members.
	 */
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
							prom.setPromotionStatus(workListMember.getId(),
									Terms.get().uuidToNative(states.get(stateRnd.nextInt(states.size())).getId()));
						}
					}
				}
			}
			Terms.get().commit();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Search wf instances.
	 * 
	 * @param collection
	 *            the collection
	 * @return the list
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public List<WfInstance> searchWfInstances(Collection<WfFilterBI> collection) throws TerminologyException, IOException {

		List<WfInstance> candidates = new ArrayList<WfInstance>();
		List<WfInstance> results = new ArrayList<WfInstance>();

		List<UUID> wlUuid = new ArrayList<UUID>();
		for (WfFilterBI loopFilter : collection) {
			if (loopFilter instanceof WfWorklistFilter) {
				WfWorklistFilter wlFilter = (WfWorklistFilter) loopFilter;
				wlUuid.add(wlFilter.getWorklistUUID());
			}
		}

		if (!wlUuid.isEmpty()) {
			candidates = getAllWrokflowInstancesForWorklist(wlUuid);
		} else {
			candidates = getAllWrokflowInstances();
		}

		for (WfInstance loopInstance : candidates) {
			boolean accepted = true;
			for (WfFilterBI loopFilter : collection) {
				if (!loopFilter.evaluateInstance(loopInstance)) {
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
