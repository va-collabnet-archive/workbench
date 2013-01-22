package org.ihtsdo.tk.workflow.api;

import java.beans.PropertyChangeEvent;

public interface WorkflowInitiatiorBI {
	
	/**
	 * Evaluates a component change event, and based on component and
	 * context properties decides if one or more Process Instances
	 * are started, and what definitions, projects, etc will be used.
	 * 
	 * @param componentId
	 * @return
	 * @throws Exception
	 */
	public boolean evaluateForWorkflowInitiation(PropertyChangeEvent componentChangeEvent) throws Exception;

}
