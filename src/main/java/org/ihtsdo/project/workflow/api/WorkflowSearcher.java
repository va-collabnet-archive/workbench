package org.ihtsdo.project.workflow.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.lucene.index.Term;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.workflow.filters.WfWorklistFilter;
import org.ihtsdo.project.workflow.filters.WfSearchFilterBI;
import org.ihtsdo.project.workflow.model.WfInstance;
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
			config = tf.getActiveAceFrameConfig();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			candidates = provider.getAllWrokflowInstancesForWorklist(wlUuid);
		} else {
			candidates = provider.getAllWrokflowInstances();
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
