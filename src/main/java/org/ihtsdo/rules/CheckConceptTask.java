package org.ihtsdo.rules;

import javax.swing.SwingWorker;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.rules.RulesLibrary.INFERRED_VIEW_ORIGIN;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;

public class CheckConceptTask extends SwingWorker<ResultsCollectorWorkBench, ResultsCollectorWorkBench> {
	
	private I_GetConceptData concept;
	private I_GetConceptData context;
	private boolean onlyUncommittedContent = false;
	private I_ConfigAceFrame config;
	private RulesContextHelper contextHelper; 
	private INFERRED_VIEW_ORIGIN inferredOrigin;

	@Override
	protected ResultsCollectorWorkBench doInBackground() throws Exception {
		ResultsCollectorWorkBench results = null;
		
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
			//log problem, not enough data
		}
		
		return results;
	}

	public I_GetConceptData getConcept() {
		return concept;
	}

	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	public I_GetConceptData getContext() {
		return context;
	}

	public void setContext(I_GetConceptData context) {
		this.context = context;
	}

	public boolean isOnlyUncommittedContent() {
		return onlyUncommittedContent;
	}

	public void setOnlyUncommittedContent(boolean onlyUncommittedContent) {
		this.onlyUncommittedContent = onlyUncommittedContent;
	}

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	public RulesContextHelper getContextHelper() {
		return contextHelper;
	}

	public void setContextHelper(RulesContextHelper contextHelper) {
		this.contextHelper = contextHelper;
	}

	public INFERRED_VIEW_ORIGIN getInferredOrigin() {
		return inferredOrigin;
	}

	public void setInferredOrigin(INFERRED_VIEW_ORIGIN inferredOrigin) {
		this.inferredOrigin = inferredOrigin;
	}

}
