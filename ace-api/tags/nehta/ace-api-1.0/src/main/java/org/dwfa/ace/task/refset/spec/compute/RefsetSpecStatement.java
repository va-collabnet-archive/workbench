/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents partial information contained in a refset spec.
 * An example of a statement is : "NOT: Concept is : Paracetamol"
 * @author Chrissy Hill
 *
 */
public abstract class RefsetSpecStatement {

	/**
	 * Whether to use the NOT qualifier.
	 */
	protected boolean useNotQualifier;
	
	/**
	 * The type of query - e.g. "Concept is", "Concept is member of" etc.
	 */
	protected I_GetConceptData queryToken;
	
	/**
	 * The concept to which the query type is applied.
	 * e.g. if query type is "concept is" and query destination is "paracetamol",
	 * then the statement would be "concept is":"paracetamol".
	 */
	protected I_GetConceptData queryConstraint;
	
	protected I_TermFactory termFactory;
	
	/**
	 * Constructor for refset spec statement.
	 * @param useNotQualifier Whether to use the NOT qualifier.
	 * @param queryToken The query type to use (e.g. "concept is")
	 * @param queryConstraint The destination concept (e.g. "paracetamol")
	 */
	public RefsetSpecStatement(boolean useNotQualifier, 
			I_GetConceptData groupingToken, 
			I_GetConceptData constraint) {
		this.useNotQualifier = useNotQualifier;
		this.queryToken = groupingToken;
		this.queryConstraint = constraint;
		termFactory = LocalVersionedTerminology.get();
	}


	public boolean execute(I_AmTermComponent component)
		throws IOException, TerminologyException {
	
		boolean statementResult = getStatementResult(component);
		
		if (useNotQualifier) { 
			// if the statement has a negation associated with it then we need to negate the results
			return !statementResult;
		} else {
			return statementResult;
		}
	}
	
	public abstract boolean getStatementResult(I_AmTermComponent component) 
		throws IOException, TerminologyException;


	protected boolean isComponentStatus(I_GetConceptData requiredStatus, 
			List<I_AmTuple> tuples) {
		
		// get latest tuple
		I_AmTuple latestTuple = null;
		int latestTupleVersion = Integer.MIN_VALUE;
		for (I_AmTuple tuple : tuples) {
			if (tuple.getVersion() > latestTupleVersion) {
				latestTupleVersion = tuple.getVersion();
				latestTuple = tuple;
			}
		}
		
		if (latestTuple != null && latestTuple.getStatusId() == 
							requiredStatus.getConceptId()) {
			return true; 
		}
		
		return false;
	}
	
	protected boolean componentStatusIs(I_AmTuple tuple) {
		List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
		tuples.add(tuple);
		
		return isComponentStatus(queryConstraint, tuples);
	}
	
	protected boolean componentStatusIsKindOf(I_AmTuple tuple) throws IOException, TerminologyException {
		
		List<I_AmTuple> tuples = new ArrayList<I_AmTuple>();
		tuples.add(tuple);
		
		if (isComponentStatus(queryConstraint, tuples)) {
			return true;
		}
		
		I_IntSet allowedTypes = termFactory.newIntSet();
		allowedTypes.add(termFactory.getConcept(
				ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()).getConceptId());
		
		// get list of all children of input concept
		Set<I_GetConceptData> childStatuses = queryConstraint.getDestRelOrigins(
				termFactory.getActiveAceFrameConfig().getAllowedStatus(), allowedTypes, null, true, true);
		
		// call conceptStatusIs on each
		for (I_GetConceptData childStatus : childStatuses) {
			if (isComponentStatus(childStatus, tuples)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected boolean componentIsMemberOf(int componentId) throws IOException, TerminologyException {
		// get all extensions for this concept
		List<I_ThinExtByRefVersioned> extensions = 
			termFactory.getAllExtensionsForComponent(componentId);
		
		for (I_ThinExtByRefVersioned ext : extensions) {
			if (ext.getRefsetId() == queryConstraint.getConceptId()) { // check they are of the specified refset
				
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


	/**
	 * Negates the statement by inverting the current associated negation.
	 */
	public void negateStatement() {
		useNotQualifier = !useNotQualifier;
	}
}
