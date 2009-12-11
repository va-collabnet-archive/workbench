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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents the data provided by a refset spec. It can hold 0 or more
 * subqueries, and 0 or more statements. The query can be executed by passing in
 * a concept to test.
 * 
 * @author Chrissy Hill
 * 
 */
public class RefsetSpecQuery {

    private Set<RefsetSpecQuery> subqueries;
    private Set<RefsetSpecStatement> statements;

    private int groupingType;

    private final int OR_GROUPING = 1;
    private final int AND_GROUPING = 2;
    private final int CONCEPT_CONTAINS_REL_GROUPING = 3;
    private final int CONCEPT_NOT_CONTAINS_REL_GROUPING = 4;
    private final int CONCEPT_CONTAINS_DESC_GROUPING = 5;
    private final int CONCEPT_NOT_CONTAINS_DESC_GROUPING = 6;

    private I_TermFactory termFactory;

    private int totalStatementCount;

    public RefsetSpecQuery(I_GetConceptData groupingConcept)
            throws TerminologyException, IOException {

        // create query object (statements + any sub-queries)
        subqueries = new HashSet<RefsetSpecQuery>();
        statements = new HashSet<RefsetSpecStatement>();

        this.groupingType = getGroupingTypeFromConcept(groupingConcept);
        termFactory = LocalVersionedTerminology.get();

        totalStatementCount = 0;
    }

    private int getGroupingTypeFromConcept(I_GetConceptData concept)
            throws TerminologyException, IOException {
        if (concept.equals(LocalVersionedTerminology.get().getConcept(
                RefsetAuxiliary.Concept.REFSET_AND_GROUPING.getUids()))) {
            return AND_GROUPING;
        } else if (concept.equals(LocalVersionedTerminology.get().getConcept(
                RefsetAuxiliary.Concept.REFSET_OR_GROUPING.getUids()))) {
            return OR_GROUPING;
        } else if (concept.equals(LocalVersionedTerminology.get()
                .getConcept(
                        RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING
                                .getUids()))) {
            return CONCEPT_CONTAINS_REL_GROUPING;
        } else if (concept.equals(LocalVersionedTerminology.get().getConcept(
                RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING
                        .getUids()))) {
            return CONCEPT_CONTAINS_DESC_GROUPING;
        } else {
            throw new TerminologyException(
                    "No valid grouping token specified : "
                            + concept.getInitialText());
        }
    }

    public RefsetSpecQuery addSubquery(I_GetConceptData groupingConcept)
            throws TerminologyException, IOException {
        RefsetSpecQuery subquery = new RefsetSpecQuery(groupingConcept);
        subqueries.add(subquery);
        return subquery;
    }

    public RefsetSpecStatement addRelStatement(boolean useNotQualifier,
            I_GetConceptData groupingToken, I_GetConceptData constraint) {
        RefsetSpecStatement statement = new RelationshipStatement(
                useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        return statement;
    }

    public RefsetSpecStatement addConceptStatement(boolean useNotQualifier,
            I_GetConceptData groupingToken, I_GetConceptData constraint) {
        RefsetSpecStatement statement = new ConceptStatement(useNotQualifier,
                groupingToken, constraint);
        statements.add(statement);
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier,
            I_GetConceptData groupingToken, I_GetConceptData constraint) {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier,
                groupingToken, constraint);
        statements.add(statement);
        return statement;
    }

    /**
     * Executes the specified query.
     * 
     * @return True if query conditions are met, false otherwise.
     * @throws TerminologyException
     * @throws IOException
     */
    public boolean execute(I_AmTermComponent component) throws IOException,
            TerminologyException {

        // process all statements and subqueries
        switch (groupingType) {
            case (AND_GROUPING):

                if (statements.size() == 0 && subqueries.size() == 0) {
                    throw new TerminologyException(
                            "Spec is invalid - dangling AND.");
                }

                for (RefsetSpecStatement statement : statements) {
                    if (!statement.execute(component)) {
                        // can exit the AND early, as at least one statement is
                        // returning false
                        return false;
                    }
                }

                for (RefsetSpecQuery subquery : subqueries) {
                    if (!subquery.execute(component)) {
                        // can exit the AND early, as at least one query is
                        // returning false
                        return false;
                    }
                }

                // all queries and statements have returned true, therefore AND
                // will return true
                return true;
            case (OR_GROUPING):

                if (statements.size() == 0 && subqueries.size() == 0) {
                    throw new TerminologyException(
                            "Spec is invalid - dangling OR.");
                }

                for (RefsetSpecStatement statement : statements) {
                    if (statement.execute(component)) {
                        // exit the OR statement early, as at least one
                        // statement has returned true
                        return true;
                    }
                }

                for (RefsetSpecQuery subquery : subqueries) {
                    if (subquery.execute(component)) {
                        // exit the OR statement early, as at least one query
                        // has returned true
                        return true;
                    }
                }

                // no queries or statements have returned true, therefore the OR
                // will return false
                return false;
            case (CONCEPT_CONTAINS_DESC_GROUPING):

                // for this concept. get all relationships.
                // execute all statements and queries on each relationship.
                I_GetConceptData concept = (I_GetConceptData) component;
                List<I_DescriptionTuple> descriptionTuples = concept
                        .getDescriptionTuples(termFactory
                                .getActiveAceFrameConfig().getAllowedStatus(),
                                null, termFactory.getActiveAceFrameConfig()
                                        .getViewPositionSet(), true);

                for (I_DescriptionTuple tuple : descriptionTuples) {
                    I_DescriptionVersioned descVersioned = tuple
                            .getDescVersioned();
                    boolean valid = false;

                    for (RefsetSpecStatement statement : statements) {
                        if (!statement.execute(descVersioned)) {
                            // can exit the AND early, as at least one statement
                            // is returning false
                            valid = false;
                            break;
                        } else {
                            valid = true;
                        }
                    }

                    for (RefsetSpecQuery subquery : subqueries) {
                        if (!subquery.execute(descVersioned)) {
                            // can exit the AND early, as at least one query is
                            // returning false
                            valid = false;
                            break;
                        } else {
                            valid = true;
                        }
                    }

                    if (valid) { // this description meets criteria
                        return true;
                    }
                }

                return false; // no descriptions met criteria
            case (CONCEPT_CONTAINS_REL_GROUPING):
                // TODO
                return true;
            case (CONCEPT_NOT_CONTAINS_REL_GROUPING):
                // TODO
                return true;
            case (CONCEPT_NOT_CONTAINS_DESC_GROUPING):
                // TODO
                return true;
            default:
                throw new TerminologyException("Unknown grouping type.");
        }
    }

    /**
     * Negate the current query.
     */
    public void negateQuery() {

        // recursively negate the current query
        switch (groupingType) {
            case (AND_GROUPING):
                groupingType = OR_GROUPING;
                break;
            case (OR_GROUPING):
                groupingType = AND_GROUPING;
                break;
            case (CONCEPT_CONTAINS_REL_GROUPING):
                groupingType = CONCEPT_NOT_CONTAINS_REL_GROUPING;
                break;
            case (CONCEPT_CONTAINS_DESC_GROUPING):
                groupingType = CONCEPT_NOT_CONTAINS_DESC_GROUPING;
                break;
            case (CONCEPT_NOT_CONTAINS_REL_GROUPING):
                groupingType = CONCEPT_CONTAINS_REL_GROUPING;
                break;
            case (CONCEPT_NOT_CONTAINS_DESC_GROUPING):
                groupingType = CONCEPT_CONTAINS_DESC_GROUPING;
                break;
            default:
                break;
        }

        for (RefsetSpecStatement statement : statements) {
            statement.negateStatement();
        }
        for (RefsetSpecQuery query : subqueries) {
            query.negateQuery();
        }
    }

    public int getTotalStatementCount() {
        totalStatementCount = statements.size();
        for (RefsetSpecQuery query : subqueries) {
            totalStatementCount = totalStatementCount
                    + query.getTotalStatementCount();
        }
        return totalStatementCount;
    }

    public boolean isValidQuery() {
        for (RefsetSpecQuery query : subqueries) {
            if (query.getTotalStatementCount() == 0) {
                return false;
            }
            if (!query.isValidQuery()) {
                return false;
            }
        }
        return true;
    }

    public void setTotalStatementCount(int totalStatementCount) {
        this.totalStatementCount = totalStatementCount;
    }
}
