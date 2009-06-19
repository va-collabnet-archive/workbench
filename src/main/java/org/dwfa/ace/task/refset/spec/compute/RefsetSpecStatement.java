package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * @author Chrissy Hill
 *
 */
public class RefsetSpecStatement {

	/**
	 * Whether to use the NOT qualifier.
	 */
	private boolean useNotQualifier;
	
	/**
	 * The type of query - e.g. "Concept is", "Concept is member of" etc.
	 */
	private I_GetConceptData queryType;
	
	/**
	 * The concept to which the query type is applied.
	 * e.g. if query type is "concept is" and query destination is "paracetamol",
	 * then the statement would be "concept is":"paracetamol".
	 */
	private I_GetConceptData queryDestination;
	
	private I_TermFactory termFactory;
	
	/**
	 * Constructor for refset spec statement.
	 * @param useNotQualifier Whether to use the NOT qualifier.
	 * @param queryType The query type to use (e.g. "concept is")
	 * @param queryDestination The destination concept (e.g. "paracetamol")
	 */
	public RefsetSpecStatement(boolean useNotQualifier, I_GetConceptData queryType, 
			I_GetConceptData queryDestination) {
		
		this.useNotQualifier = useNotQualifier;
		this.queryType = queryType;
		this.queryDestination = queryDestination;
		termFactory = LocalVersionedTerminology.get();
	}
	
	/**
	 * Executes the specified statement.
	 * @return boolean based on the result of the statement execution.
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	public boolean execute(I_GetConceptData concept) throws IOException, TerminologyException {

		boolean statementResult = getStatementResult(concept);
		
		if (useNotQualifier) { // if the statement has a negation associated with it then we need to negate the results
			System.out.println(concept.getInitialText() + " " + queryType.getInitialText() + " " 
					+ queryDestination.getInitialText() 
					+ " result: " + !statementResult);
			return !statementResult;
		} else {
			System.out.println(concept.getInitialText() + " " + queryType.getInitialText() + " " 
					+ queryDestination.getInitialText() 
					+ " result: " + statementResult);
			return statementResult;
		}
	}
	
	/**
	 * Negates the statement by inverting the current associated negation.
	 */
	public void negateStatement() {
		useNotQualifier = !useNotQualifier;
	}
	
	/**
	 * Calculates the results of the statement.
	 * @param concept The concept whose truth is being tested. e.g. 
	 * test if "Panadeine" passes the test of "Concept is: paracetamol".
	 * @return A boolean representing the success of the statement execution.
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean getStatementResult(I_GetConceptData concept) throws IOException, TerminologyException {
		
		if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS.getUids()))) {
			return conceptIs(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_MEMBER_OF.getUids()))) {
			return conceptIsMemberOf(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_KIND_OF.getUids()))) { 
			return conceptIsKindOf(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS.getUids()))) {
			return conceptStatusIs(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_STATUS_IS_KIND_OF.getUids()))) {
			return conceptStatusIsKindOf(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_IS_CHILD_OF.getUids()))) {
			return conceptIsChildOf(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING.getUids()))) {
			return conceptContainsRelGrouping(concept);
		} else if (queryType.equals(termFactory.getConcept(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING.getUids()))) {
			return conceptContainsDescGrouping(concept);
		} else {
			throw new TerminologyException("Unknown query type : " + queryType.getInitialText());
		}
	}
	
	/**
	 * Tests of the current concept is a member of the specified refset.
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException 
	 */
	private boolean conceptIsMemberOf(I_GetConceptData concept) throws IOException, TerminologyException {
		
		// get all extensions for this concept
		List<I_ThinExtByRefVersioned> extensions = 
			termFactory.getAllExtensionsForComponent(concept.getConceptId());
		
		for (I_ThinExtByRefVersioned ext : extensions) {
			if (ext.getRefsetId() == queryDestination.getConceptId()) { // check they are of the specified refset
				
				List<? extends I_ThinExtByRefPart> parts = ext.getVersions();
				
				I_ThinExtByRefPart latestPart = null;
				int latestPartVersion = Integer.MIN_VALUE;
				
				// get latest part & check that it is current
				for (I_ThinExtByRefPart part : parts) {
					if (part.getVersion() > latestPartVersion) {
						latestPartVersion = part.getVersion();
						latestPart = part;
					}
				}
				
				if (latestPart.getStatusId() == termFactory.getConcept(
						ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId()) {
					return true;
				}
			}
		}

		return false; 
	}
	
	private boolean conceptContainsRelGrouping(I_GetConceptData concept) throws TerminologyException {
		throw new TerminologyException("Unimplemented query : contains rel grouping"); // unimplemented
	}
	
	private boolean conceptContainsDescGrouping(I_GetConceptData concept) throws TerminologyException {
		throw new TerminologyException("Unimplemented query : contains desc grouping"); // unimplemented
	}
	
	/**
	 * Tests of the current concept is the same as the destination concept.
	 * @param concept
	 * @return
	 */
	private boolean conceptIs(I_GetConceptData concept) {
		return concept.equals(queryDestination);
	}
	
	/**
	 * Tests if the current concept is a child of the destination concept. This does not 
	 * return true if they are the same concept.
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptIsChildOf(I_GetConceptData concept) throws IOException, TerminologyException {
		return queryDestination.isParentOf(concept, true); 
	}
	
	/**
	 * Tests if the current concept is a child of or the same as the destination concept.
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptIsKindOf(I_GetConceptData concept) throws IOException, TerminologyException {
		return queryDestination.isParentOfOrEqualTo(concept, true); 
	}
	
	/**
	 * Tests if the current concept has a status of the status represented by the destination concept.
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIs(I_GetConceptData concept) throws IOException, TerminologyException {
		List<I_ConceptAttributeTuple> tuples = concept.getConceptAttributeTuples(null, null); 
		
		// get latest tuple
		I_ConceptAttributeTuple latestTuple = null;
		int latestTupleVersion = Integer.MIN_VALUE;
		for (I_ConceptAttributeTuple tuple : tuples) {
			if (tuple.getVersion() > latestTupleVersion) {
				latestTupleVersion = tuple.getVersion();
				latestTuple = tuple;
			}
		}
		
		if (latestTuple != null && latestTuple.getConceptStatus() == 
				queryDestination.getConceptId()) {
				return true; 
		}
		
		return false;
	}
	
	/**
	 * Tests if the current concept has a status of the status represented by the destination concept,
	 * or any of its children.
	 * @param concept
	 * @return
	 * @throws IOException
	 * @throws TerminologyException
	 */
	private boolean conceptStatusIsKindOf(I_GetConceptData concept) throws IOException, TerminologyException {
	
		// get list of all children of input concept
		
		// call conceptStatusIs on each
		
		// return true if any of the calls return true
		throw new TerminologyException("Unimplemented query : concept status is kind of"); // unimplemented
	}
}
