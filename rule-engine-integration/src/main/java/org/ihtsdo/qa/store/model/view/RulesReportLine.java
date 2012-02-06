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
package org.ihtsdo.qa.store.model.view;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.ihtsdo.qa.store.model.Rule;

/**
 * The Class RulesReportLine.
 */
public class RulesReportLine {

	/** The rule. */
	private Rule rule;
	
	/** The status count. */
	private HashMap<Boolean, Integer> statusCount;
	
	/** The disposition status count. */
	private HashMap<UUID, Integer> dispositionStatusCount;
	
	/** The last execution time. */
	private Date lastExecutionTime;

	/**
	 * Instantiates a new rules report line.
	 */
	public RulesReportLine() {
		super();
	}

	/**
	 * Instantiates a new rules report line.
	 *
	 * @param rule the rule
	 * @param statusCount the status count
	 * @param dispositionStatusCount the disposition status count
	 * @param lastExecutionTime the last execution time
	 */
	public RulesReportLine(Rule rule, HashMap<Boolean, Integer> statusCount, HashMap<UUID, Integer> dispositionStatusCount, Date lastExecutionTime) {
		super();
		this.rule = rule;
		this.statusCount = statusCount;
		this.dispositionStatusCount = dispositionStatusCount;
		this.lastExecutionTime = lastExecutionTime;
	}

	/**
	 * Gets the rule.
	 *
	 * @return the rule
	 */
	public Rule getRule() {
		return rule;
	}

	/**
	 * Sets the rule.
	 *
	 * @param rule the new rule
	 */
	public void setRule(Rule rule) {
		this.rule = rule;
	}

	/**
	 * Gets the status count.
	 *
	 * @return the status count
	 */
	public HashMap<Boolean, Integer> getStatusCount() {
		return statusCount;
	}

	/**
	 * Sets the status count.
	 *
	 * @param statusCount the status count
	 */
	public void setStatusCount(HashMap<Boolean, Integer> statusCount) {
		this.statusCount = statusCount;
	}

	/**
	 * Gets the disposition status count.
	 *
	 * @return the disposition status count
	 */
	public HashMap<UUID, Integer> getDispositionStatusCount() {
		return dispositionStatusCount;
	}

	/**
	 * Sets the disposition status count.
	 *
	 * @param dispositionStatusCount the disposition status count
	 */
	public void setDispositionStatusCount(HashMap<UUID, Integer> dispositionStatusCount) {
		this.dispositionStatusCount = dispositionStatusCount;
	}

	/**
	 * Gets the last execution time.
	 *
	 * @return the last execution time
	 */
	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	/**
	 * Sets the last execution time.
	 *
	 * @param lastExecutionTime the new last execution time
	 */
	public void setLastExecutionTime(Date lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}

}
