package org.ihtsdo.project.search;

import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public class WorkflowInstanceSearchResult implements WfInstanceDataInterface {
	private String action;
	private String state;
	private String modeler;
	private long time;
	private String concept;
	private String fsn;

	public WorkflowInstanceSearchResult(WorkflowHistoryJavaBean bean) {
		action = bean.getAction().toString();
		state = bean.getState().toString();
		modeler = bean.getModeler().toString();
		time = bean.getWorkflowTime();
		concept = bean.getConcept().toString();
		fsn = bean.getFullySpecifiedName();
	}

	public WorkflowInstanceSearchResult(String action, String state, String modeler, long time, String concept, String fsn) {
		super();
		this.action = action;
		this.state = state;
		this.modeler = modeler;
		this.time = time;
		this.concept = concept;
		this.fsn = fsn;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setModeler(String modeler) {
		this.modeler = modeler;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setConcept(String concept) {
		this.concept = concept;
	}

	public void setFsn(String fsn) {
		this.fsn = fsn;
	}

	@Override
	public String getAction() {
		return action;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public String getModeler() {
		return modeler;
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public String getConcept() {
		return concept;
	}

	@Override
	public String getFsn() {
		return fsn;
	}

	@Override
	public int compareTo(WfInstanceDataInterface wfInstanceData) {
		return new Long(this.getTime()).compareTo(new Long(wfInstanceData.getTime()));
	}
}
