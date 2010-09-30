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
package org.ihtsdo.tk.helper;

import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.tk.helper.templates.AbstractTemplate;

/**
 * The Class ResultsCollector.
 */
public class ResultsCollector {
	
	/** The alert list. */
	//private List<AlertToDataConstraintFailure> alertList;
	private List<ResultsItem> resultsItems;
	private List<AbstractTemplate> templates;
	
	/**
	 * Instantiates a new results collector.
	 * 
	 * @param alertList the alert list
	 * @param approvedRels the approved rels
	 * @param messagesSet the messages set
	 */
	public ResultsCollector() {
		super();
		this.resultsItems = new ArrayList<ResultsItem>();
		this.templates = new ArrayList<AbstractTemplate>();
	}

	public void addResultsItem(ResultsItem resultsItem) {
		this.resultsItems.add(resultsItem);
	}

	public List<ResultsItem> getResultsItems() {
		return resultsItems;
	}

	public void setResultsItems(List<ResultsItem> resultsItems) {
		this.resultsItems = resultsItems;
	}

	public List<AbstractTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(List<AbstractTemplate> templates) {
		this.templates = templates;
	}
	
	public void addTemplate(AbstractTemplate template) {
		this.templates.add(template);
	}
}
