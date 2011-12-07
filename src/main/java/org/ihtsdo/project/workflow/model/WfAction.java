package org.ihtsdo.project.workflow.model;

import java.util.List;
import java.util.UUID;

public abstract class WfAction {

	private String name;
	private UUID id;
	private WfState consequence;

	public WfAction() {
		super();
	}

	public abstract WfInstance doAction(WfInstance instance) throws Exception;

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

}
