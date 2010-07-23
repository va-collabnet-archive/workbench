/**
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
package org.ihtsdo.rules.testmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.ihtsdo.testmodel.ResultsCollector;

/**
 * The Class ResultsCollectorWorkBench.
 */
public class ResultsCollectorWorkBench extends ResultsCollector{
	
	/** The alert list. */
	private List<AlertToDataConstraintFailure> alertList;
	
	
	/**
	 * Instantiates a new results collector.
	 * 
	 * @param alertList the alert list
	 * @param approvedRels the approved rels
	 * @param messagesSet the messages set
	 */
	public ResultsCollectorWorkBench() {
		super();
		this.alertList = new ArrayList<AlertToDataConstraintFailure>();
	}
	
	/**
	 * Gets the alert list.
	 * 
	 * @return the alert list
	 */
	public List<AlertToDataConstraintFailure> getAlertList() {
		return alertList;
	}
	
	/**
	 * Sets the alert list.
	 * 
	 * @param alertList the new alert list
	 */
	public void setAlertList(List<AlertToDataConstraintFailure> alertList) {
		this.alertList = alertList;
	}
}
