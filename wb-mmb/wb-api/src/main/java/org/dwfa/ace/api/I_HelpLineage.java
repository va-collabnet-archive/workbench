package org.dwfa.ace.api;

import java.util.Set;

import org.dwfa.tapi.TerminologyException;

public interface I_HelpLineage {

    /**
     * A simple template for logic that defines if a process should be executed
     * on a particular subject (concept).
     */
    public interface LineageCondition {
        public boolean evaluate(I_GetConceptData concept) throws Exception;
    }


	public Set<I_GetConceptData> getParents(I_GetConceptData concept)
			throws Exception;

	public Set<I_GetConceptData> getChildren(I_GetConceptData concept)
			throws Exception;

	/**
	 * Get all the ancestors (parents, parents of parents, etc) of a particular
	 * concept.
	 */
	public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept,
			LineageCondition... conditions) throws Exception;

	/**
	 * Get all the descendants (children, children of children, etc) of a
	 * particular concept.
	 */
	public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept,
			LineageCondition... conditions) throws Exception;

	public boolean hasAncestor(I_GetConceptData concept,
			I_GetConceptData ancestor) throws TerminologyException;

	public boolean hasDescendant(I_GetConceptData concept,
			I_GetConceptData descendant) throws TerminologyException;

}