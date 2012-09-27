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
package org.ihtsdo.rules;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;

/**
 * The Class CheckConceptTask.
 */
public class CheckConceptTask extends SwingWorker<ResultsCollectorWorkBench, ResultsCollectorWorkBench> {
	
	/** The concept. */
	private I_GetConceptData concept;
	
	/** The context. */
	private I_GetConceptData context;
	
	/** The only uncommitted content. */
	private boolean onlyUncommittedContent = false;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The context helper. */
	private RulesContextHelper contextHelper; 
	
	/** The inferred origin. */
	private INFERRED_VIEW_ORIGIN inferredOrigin;
	
	/** The results. */
	private ResultsCollectorWorkBench results;

	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#doInBackground()
	 */
	@Override
	protected ResultsCollectorWorkBench doInBackground() throws Exception {
		if (concept != null && context != null && config != null && contextHelper != null && inferredOrigin != null) {
			results = RulesLibrary.checkConcept(concept, context, onlyUncommittedContent, 
					config, contextHelper, inferredOrigin);
		} else if (concept != null && context != null && config != null && inferredOrigin != null) {
			results = RulesLibrary.checkConcept(concept, context, onlyUncommittedContent, config, inferredOrigin);
		} else if (concept != null && context != null && config != null && contextHelper != null) {
			results = RulesLibrary.checkConcept(concept, context, onlyUncommittedContent, config, contextHelper);
		} else if (concept != null && context != null && config != null) {
			results = RulesLibrary.checkConcept(concept, context, onlyUncommittedContent, config);
		} else {
			AceLog.getAppLog().warning("Check concept problem, not enough data");
		}
		
		return results;
	}
	
	/* (non-Javadoc)
	 * @see javax.swing.SwingWorker#done()
	 */
	@Override
	protected void done() {
	}

	/**
	 * Gets the concept.
	 *
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		return concept;
	}

	/**
	 * Sets the concept.
	 *
	 * @param concept the new concept
	 */
	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	/**
	 * Gets the context.
	 *
	 * @return the context
	 */
	public I_GetConceptData getContext() {
		return context;
	}

	/**
	 * Sets the context.
	 *
	 * @param context the new context
	 */
	public void setContext(I_GetConceptData context) {
		this.context = context;
	}

	/**
	 * Checks if is only uncommitted content.
	 *
	 * @return true, if is only uncommitted content
	 */
	public boolean isOnlyUncommittedContent() {
		return onlyUncommittedContent;
	}

	/**
	 * Sets the only uncommitted content.
	 *
	 * @param onlyUncommittedContent the new only uncommitted content
	 */
	public void setOnlyUncommittedContent(boolean onlyUncommittedContent) {
		this.onlyUncommittedContent = onlyUncommittedContent;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public I_ConfigAceFrame getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 *
	 * @param config the new config
	 */
	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	/**
	 * Gets the context helper.
	 *
	 * @return the context helper
	 */
	public RulesContextHelper getContextHelper() {
		return contextHelper;
	}

	/**
	 * Sets the context helper.
	 *
	 * @param contextHelper the new context helper
	 */
	public void setContextHelper(RulesContextHelper contextHelper) {
		this.contextHelper = contextHelper;
	}

	/**
	 * Gets the inferred origin.
	 *
	 * @return the inferred origin
	 */
	public INFERRED_VIEW_ORIGIN getInferredOrigin() {
		return inferredOrigin;
	}

	/**
	 * Sets the inferred origin.
	 *
	 * @param inferredOrigin the new inferred origin
	 */
	public void setInferredOrigin(INFERRED_VIEW_ORIGIN inferredOrigin) {
		this.inferredOrigin = inferredOrigin;
	}

	/**
	 * Gets the results.
	 *
	 * @return the results
	 */
	public ResultsCollectorWorkBench getResults() {
		return results;
	}

	/**
	 * Sets the results.
	 *
	 * @param results the new results
	 */
	public void setResults(ResultsCollectorWorkBench results) {
		this.results = results;
	}
}
