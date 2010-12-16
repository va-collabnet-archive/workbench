package org.ihtsdo.qa.store.model.view;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.ihtsdo.qa.store.model.Rule;

public class RulesReportLine {

	private Rule rule;
	private HashMap<Boolean, Integer> statusCount;
	private HashMap<UUID, Integer> dispositionStatusCount;
	private Date lastExecutionTime;

	public RulesReportLine() {
		super();
	}

	public RulesReportLine(Rule rule, HashMap<Boolean, Integer> statusCount, HashMap<UUID, Integer> dispositionStatusCount, Date lastExecutionTime) {
		super();
		this.rule = rule;
		this.statusCount = statusCount;
		this.dispositionStatusCount = dispositionStatusCount;
		this.lastExecutionTime = lastExecutionTime;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public HashMap<Boolean, Integer> getStatusCount() {
		return statusCount;
	}

	public void setStatusCount(HashMap<Boolean, Integer> statusCount) {
		this.statusCount = statusCount;
	}

	public HashMap<UUID, Integer> getDispositionStatusCount() {
		return dispositionStatusCount;
	}

	public void setDispositionStatusCount(HashMap<UUID, Integer> dispositionStatusCount) {
		this.dispositionStatusCount = dispositionStatusCount;
	}

	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

}
