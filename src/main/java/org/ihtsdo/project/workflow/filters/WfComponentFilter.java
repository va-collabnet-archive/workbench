package org.ihtsdo.project.workflow.filters;

import java.io.IOException;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.workflow.model.WfInstance;

public class WfComponentFilter implements WorkflowSearchFilterBI {

	private String wfInstanceTextFilter;

	private WfComponentFilter(String wfInstanceTextFilter) {
		super();
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	@Override
	public boolean filter(WfInstance instance) {
		try {
			I_GetConceptData instanceComponent = Terms.get().getConcept(instance.getComponentId());
			return instanceComponent.getInitialText().contains(wfInstanceTextFilter);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setWfInstanceTextFilter(String wfInstanceTextFilter) {
		this.wfInstanceTextFilter = wfInstanceTextFilter;
	}

	public String getWfInstanceTextFilter() {
		return wfInstanceTextFilter;
	}

}
