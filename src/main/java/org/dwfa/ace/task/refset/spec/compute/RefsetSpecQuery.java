package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents the data provided by a refset spec.
 * It can hold 0 or more subqueries, and 0 or more statements.
 * The query can be executed by passing in a concept to test.
 * @author Chrissy Hill
 *
 */
public class RefsetSpecQuery {

	private Set<RefsetSpecQuery> subqueries;
	private Set<RefsetSpecStatement> statements;
	
	private boolean useAndQualifier;
	
	public RefsetSpecQuery(boolean useAndQualifier) {
		
		// create query object (statements + any sub-queries)
		subqueries = new HashSet<RefsetSpecQuery>();
		statements = new HashSet<RefsetSpecStatement>();
		
		this.useAndQualifier = useAndQualifier;
		
	}
	
	public RefsetSpecQuery addSubquery(boolean useAndQualifier) {
		RefsetSpecQuery subquery = new RefsetSpecQuery(useAndQualifier);
		subqueries.add(subquery);
		return subquery;
	}
	
	public RefsetSpecStatement addStatement(boolean negate, I_GetConceptData refsetType, I_GetConceptData refsetDestination) {
		RefsetSpecStatement statement = new RefsetSpecStatement(negate, refsetType, refsetDestination);
		statements.add(statement);
		return statement;
	}
	
	/**
	 * Executes the specified query.
	 * @return True if query conditions are met, false otherwise.
	 * @throws TerminologyException 
	 * @throws IOException 
	 */
	public boolean execute(I_GetConceptData concept) throws IOException, TerminologyException {
		
		// iterate through statements and queries
		if (useAndQualifier) {
			for (RefsetSpecStatement statement : statements) {
				if (!statement.execute(concept)) {
					return false; // can exit the AND early, as at least one statement is returning false
				}
			}
			
			for (RefsetSpecQuery subquery : subqueries) {
				if (!subquery.execute(concept)) {
					return false; // can exit the AND early, as at least one query is returning false
				}
			}
			
			return true; // all queries and statements have returned true, therefore AND will return true
		} else { // use OR qualifier
			
			for (RefsetSpecStatement statement : statements) {
				if (statement.execute(concept)) {
					return true; // exit the OR statement early, as at least one statement has returned true
				}
			}
			
			for (RefsetSpecQuery subquery : subqueries) {
				if (subquery.execute(concept)) {
					return true; // exit the OR statement early, as at least one query has returned true
				}
			}
			
			return false; // no queries or statements have returned true, therefore the OR will return false
		}
	}
	
	/**
	 * Negate the current query.
	 */
	public void negateQuery() {
		
		// recursively negate the current query
		useAndQualifier = !useAndQualifier;
		
		for (RefsetSpecStatement statement : statements) {
			statement.negateStatement();
		}
		for (RefsetSpecQuery query : subqueries) {
			query.negateQuery();
		}
	}
}
