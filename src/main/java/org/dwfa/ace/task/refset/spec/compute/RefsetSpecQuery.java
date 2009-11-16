package org.dwfa.ace.task.refset.spec.compute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;

/**
 * Represents the data provided by a refset spec. It can hold 0 or more
 * subqueries, and 0 or more statements. The query can be executed by passing in
 * a concept to test.
 * 
 * This class provides a preliminary optimization by providing a function to
 * return the possible concepts that should be tested based on the concepts in
 * the spec.
 * 
 * @author Chrissy Hill, Keith Campbell
 * 
 */
public class RefsetSpecQuery extends RefsetSpecComponent {

    private ArrayList<RefsetSpecQuery> subqueries;
    private ArrayList<RefsetSpecStatement> statements;
    private ArrayList<RefsetSpecComponent> allComponents;

    public enum GROUPING_TYPE {
        OR(RefsetAuxiliary.Concept.REFSET_OR_GROUPING, true),
        AND(RefsetAuxiliary.Concept.REFSET_AND_GROUPING, true),
        CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, true),
        NOT_CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, false),
        CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, true),
        NOT_CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, false);

        private int nid;
        private boolean truth;

        private GROUPING_TYPE(I_ConceptualizeUniversally concept, boolean truth) {
            try {
                this.nid = concept.localize().getNid();
            } catch (TerminologyException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.truth = truth;
        }

        public int getNid() {
            return nid;
        }

        public boolean getTruth() {
            return truth;
        }
    };

    private GROUPING_TYPE groupingType;

    private I_TermFactory termFactory;

    private int totalStatementCount;
    private RefsetSpecCalculationOrderComparator executionOrderComparator;
    private RefsetSpecExecutionOrderComparator calculationOrderComparator;

    public RefsetSpecQuery(I_GetConceptData groupingConcept) throws TerminologyException, IOException {

        // create query object (statements + any sub-queries)
        executionOrderComparator = new RefsetSpecCalculationOrderComparator();
        calculationOrderComparator = new RefsetSpecExecutionOrderComparator();

        subqueries = new ArrayList<RefsetSpecQuery>();
        statements = new ArrayList<RefsetSpecStatement>();
        allComponents = new ArrayList<RefsetSpecComponent>();

        this.groupingType = getGroupingTypeFromConcept(groupingConcept);
        termFactory = LocalVersionedTerminology.get();

        totalStatementCount = 0;
    }

    private GROUPING_TYPE getGroupingTypeFromConcept(I_GetConceptData concept) throws TerminologyException, IOException {
        if (concept.getConceptId() == GROUPING_TYPE.AND.nid) {
            return GROUPING_TYPE.AND;
        } else if (concept.getConceptId() == GROUPING_TYPE.OR.nid) {
            return GROUPING_TYPE.OR;
        } else if (concept.getConceptId() == GROUPING_TYPE.CONCEPT_CONTAINS_REL.nid) {
            return GROUPING_TYPE.CONCEPT_CONTAINS_REL;
        } else if (concept.getConceptId() == GROUPING_TYPE.CONCEPT_CONTAINS_DESC.nid) {
            return GROUPING_TYPE.CONCEPT_CONTAINS_DESC;
        } else {
            throw new TerminologyException("No valid grouping token specified : " + concept.getInitialText());
        }
    }

    public RefsetSpecQuery addSubquery(I_GetConceptData groupingConcept) throws TerminologyException, IOException {
        RefsetSpecQuery subquery = new RefsetSpecQuery(groupingConcept);
        subqueries.add(subquery);
        allComponents.add(subquery);
        return subquery;
    }

    public RefsetSpecStatement addRelStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_GetConceptData constraint) {
        RefsetSpecStatement statement = new RelationshipStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        return statement;
    }

    public RefsetSpecStatement addConceptStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_GetConceptData constraint) {
        RefsetSpecStatement statement = new ConceptStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_GetConceptData constraint) {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier, groupingToken, constraint);
        statements.add(statement);
        allComponents.add(statement);
        return statement;
    }

    public I_RepresentIdSet getPossibleConcepts(I_ConfigAceFrame config, I_RepresentIdSet parentPossibleConcepts)
            throws TerminologyException, IOException {

        Collections.sort(allComponents, calculationOrderComparator);

        long startTime = System.currentTimeMillis();
        AceLog.getAppLog().info(">> Start of " + this.toString());
        I_RepresentIdSet possibleConcepts = null;
        // process all statements and subqueries
        switch (groupingType) {
        case AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.and(component.getPossibleConcepts(config, possibleConcepts));
                }
            }

            break;

        case OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling OR.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(config, parentPossibleConcepts);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(config, parentPossibleConcepts));
                }
            }

            break;
        case CONCEPT_CONTAINS_DESC:
        case CONCEPT_CONTAINS_REL:
        case NOT_CONCEPT_CONTAINS_REL:
        case NOT_CONCEPT_CONTAINS_DESC:
            throw new TerminologyException("Unsupported operation exception. Optimization not complete");
        default:
            throw new TerminologyException("Unknown grouping type.");
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        AceLog.getAppLog().info(this + " possibleConceptTime: " + minutes + " minutes, " + seconds + " seconds.");
        setPossibleConceptsCount(possibleConcepts.size());
        return possibleConcepts;
    }

    public String toString() {
        try {
            return groupingType.getTruth() + " " + termFactory.getConcept(groupingType.getNid()).getInitialText();
        } catch (Exception e) {
            return "UNKNOWN QUERY";
        }
    }

    /**
     * Executes the specified query.
     * 
     * @return True if query conditions are met, false otherwise.
     * @throws TerminologyException
     * @throws IOException
     */
    public boolean execute(I_AmTermComponent component) throws IOException, TerminologyException {

        Collections.sort(allComponents, executionOrderComparator);

        // process all statements and subqueries
        switch (groupingType) {
        case AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (!specComponent.execute(component)) {
                    // can exit the AND early, as at least one statement is
                    // returning false
                    return false;
                }
            }

            // all queries and statements have returned true, therefore AND
            // will return true
            return true;
        case OR:

            if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling OR.");
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (specComponent.execute(component)) {
                    // exit the OR statement early, as at least one
                    // statement has returned true
                    return true;
                }
            }

            // no queries or statements have returned true, therefore the OR
            // will return false
            return false;
        case CONCEPT_CONTAINS_DESC:

            // for this concept. get all relationships.
            // execute all statements and queries on each relationship.
            I_GetConceptData concept = (I_GetConceptData) component;
            List<I_DescriptionTuple> descriptionTuples =
                    concept.getDescriptionTuples(termFactory.getActiveAceFrameConfig().getAllowedStatus(), null,
                        termFactory.getActiveAceFrameConfig().getViewPositionSet(), true);

            for (I_DescriptionTuple tuple : descriptionTuples) {
                I_DescriptionVersioned descVersioned = tuple.getDescVersioned();
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
        case CONCEPT_CONTAINS_REL:
            // TODO
            return true;
        case NOT_CONCEPT_CONTAINS_REL:
            // TODO
            return true;
        case NOT_CONCEPT_CONTAINS_DESC:
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
        case AND:
            groupingType = GROUPING_TYPE.OR;
            break;
        case OR:
            groupingType = GROUPING_TYPE.AND;
            break;
        case CONCEPT_CONTAINS_REL:
            groupingType = GROUPING_TYPE.NOT_CONCEPT_CONTAINS_REL;
            break;
        case CONCEPT_CONTAINS_DESC:
            groupingType = GROUPING_TYPE.NOT_CONCEPT_CONTAINS_DESC;
            break;
        case NOT_CONCEPT_CONTAINS_REL:
            groupingType = GROUPING_TYPE.CONCEPT_CONTAINS_REL;
            break;
        case NOT_CONCEPT_CONTAINS_DESC:
            groupingType = GROUPING_TYPE.CONCEPT_CONTAINS_DESC;
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
            totalStatementCount = totalStatementCount + query.getTotalStatementCount();
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

    public Set<Integer> getNestedRefsets() {
        Set<Integer> results = new HashSet<Integer>();
        for (RefsetSpecStatement statement : statements) {
            switch (statement.getTokenEnum()) {
            case CONCEPT_IS_MEMBER_OF:
            case DESC_IS_MEMBER_OF:
            case REL_IS_MEMBER_OF:
                results.add(statement.queryConstraint.getConceptId());
                break;
            default:
                break;
            }
        }
        for (RefsetSpecQuery query : subqueries) {
            results.addAll(query.getNestedRefsets());
        }
        return results;
    }

    public void setTotalStatementCount(int totalStatementCount) {
        this.totalStatementCount = totalStatementCount;
    }

    public GROUPING_TYPE getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(GROUPING_TYPE groupingType) {
        this.groupingType = groupingType;
    }
}
