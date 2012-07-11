package org.ihtsdo.tk.api.workflow;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

public interface WorkflowHandlerBI {
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate ViewCoordinate) throws IOException, ContradictionException;

	public List<UUID> getAllAvailableWorkflowActionUids(); 

	public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> beans, ConceptSpec actionSpec) throws IOException, ContradictionException;

	public boolean isActiveAction(Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID actionUuid);
}
