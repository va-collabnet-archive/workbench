package org.ihtsdo.tk.api.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

public interface WorkflowHandlerBI {
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept) throws IOException, ContraditionException;

	public Collection<UUID> getAllAvailableWorkflowActionUids(); 

	public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> beans, ConceptSpec action) throws IOException, ContraditionException;

	public boolean isActiveAction(Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID action);
}
