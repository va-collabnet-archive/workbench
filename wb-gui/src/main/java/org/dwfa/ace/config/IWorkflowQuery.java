package org.dwfa.ace.config;

public interface IWorkflowQuery {

	public static final String REFSET = "refset";
	public static final String CONCEPT = "concept";
	public static final String PARENT = "parent";

	/**
	 * Is this concept in the workflow Type is Concept, Parent(Concept), Refset
	 * **/
	boolean isConceptInWorkflow(String id, String type);

	/**
	 * Is this concept Active in the workflow Type is Concept, Parent(Concept),
	 * Refset
	 * **/
	boolean isConceptActiveInWorkflow(String id, String type);
}
