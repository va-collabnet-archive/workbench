package org.ihtsdo.project.workflow.model;

import java.io.File;
import java.util.UUID;

public class WfAction {

	private String name;
	private UUID id;
	private WfState consequence;
	private File businessProcess;
	
	public WfAction(String name) {
		super();
		this.name = name;
	}
	
	public WfAction(String name, UUID id, WfState consequence,
			File businessProcess) {
		super();
		this.name = name;
		this.id = id;
		this.consequence = consequence;
		this.businessProcess = businessProcess;
	}

	public WfAction() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public WfState getConsequence() {
		return consequence;
	}

	public void setConsequence(WfState consequence) {
		this.consequence = consequence;
	}

	public String toString() {
		return name;
	}

	public File getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(File businessProcess) {
		this.businessProcess = businessProcess;
	}

}
