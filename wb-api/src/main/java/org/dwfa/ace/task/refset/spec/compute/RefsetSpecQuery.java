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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;

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

    private static Executor refsetExecutorPool =
            Executors.newCachedThreadPool(new NamedThreadFactory(new ThreadGroup("RefsetSpecQuery "), "pool "));
    
    public static KindOfCacheBI myStaticIsACache;

    private ArrayList<RefsetSpecQuery> subqueries;
    private ArrayList<RefsetSpecStatement> statements;
    private ArrayList<RefsetSpecComponent> allComponents;

    private PositionSetReadOnly v1_is = null;
    private PositionSetReadOnly v2_is = null;
    
    public PositionSetReadOnly getV1Is() {
		return v1_is;
	}

	public void setV1Is(PositionSetReadOnly position_set_read_only) {
		v1_is = position_set_read_only;
	}

	public PositionSetReadOnly getV2Is() {
		return v2_is;
	}

	public void setV2Is(PositionSetReadOnly position_set_read_only) {
		v2_is = position_set_read_only;
	}

    public ArrayList<RefsetSpecComponent> getAllComponents() {
        return allComponents;
    }

    public enum GROUPING_TYPE {
        OR(RefsetAuxiliary.Concept.REFSET_OR_GROUPING, true), NEGATED_OR(RefsetAuxiliary.Concept.REFSET_OR_GROUPING, false), AND(RefsetAuxiliary.Concept.REFSET_AND_GROUPING, true), NEGATED_AND(RefsetAuxiliary.Concept.REFSET_AND_GROUPING, false), CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, true), NOT_CONCEPT_CONTAINS_REL(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_REL_GROUPING, false), CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, true), NOT_CONCEPT_CONTAINS_DESC(RefsetAuxiliary.Concept.CONCEPT_CONTAINS_DESC_GROUPING, false), V1(RefsetAuxiliary.Concept.DIFFERENCE_V1_GROUPING, true), V2(RefsetAuxiliary.Concept.DIFFERENCE_V2_GROUPING, true);

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
    private boolean allComponentsNeedsResort = true;
    private boolean continueComputation = true;

    public RefsetSpecQuery(I_GetConceptData groupingConcept, boolean groupingTypeTruth, int refsetSpecNid,
            I_ConfigAceFrame config) throws Exception {
        super(refsetSpecNid, config);
        // create query object (statements + any sub-queries)
        executionOrderComparator = new RefsetSpecCalculationOrderComparator();
        calculationOrderComparator = new RefsetSpecExecutionOrderComparator();

        subqueries = new ArrayList<RefsetSpecQuery>();
        statements = new ArrayList<RefsetSpecStatement>();
        allComponents = new ArrayList<RefsetSpecComponent>();

        this.groupingType = getGroupingTypeFromConcept(groupingConcept, groupingTypeTruth);
        termFactory = Terms.get();

        totalStatementCount = 0;
        continueComputation = true;
    }

    public ArrayList<RefsetSpecQuery> getSubqueries() {
        return subqueries;
    }

    public ArrayList<RefsetSpecStatement> getStatements() {
        return statements;
    }

    private GROUPING_TYPE getGroupingTypeFromConcept(I_GetConceptData concept, boolean groupingTypeTruth)
			throws TerminologyException, IOException {
		for (GROUPING_TYPE gt : GROUPING_TYPE.values()) {
            if (concept.getConceptNid() == gt.nid) {
                if (groupingTypeTruth == gt.truth) {
				return gt;
		}
	}
        }
        throw new TerminologyException("No valid grouping token specified : " + concept.getInitialText());
    }

    public RefsetSpecQuery addSubquery(I_GetConceptData groupingConcept, boolean groupingTypeTruth) throws Exception {
        RefsetSpecQuery subquery = new RefsetSpecQuery(groupingConcept, groupingTypeTruth, getRefsetSpecNid(), config);
 		if (this.getV1Is() != null)
			subquery.setV1Is(this.getV1Is());
		if (this.getV2Is() != null)
			subquery.setV2Is(this.getV2Is());
	    subqueries.add(subquery);
        allComponents.add(subquery);
        allComponentsNeedsResort = true;
        return subquery;
    }

    public RefsetSpecStatement addRelStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint, int refsetSpecNid) throws Exception {
        RefsetSpecStatement statement =
                new RelationshipStatement(useNotQualifier, groupingToken, constraint, refsetSpecNid, config);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addConceptStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint, int refsetSpecNid) throws Exception {
        RefsetSpecStatement statement =
                new ConceptStatement(useNotQualifier, groupingToken, constraint, refsetSpecNid, config);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier, I_GetConceptData groupingToken,
            I_AmTermComponent constraint, int refsetSpecNid) throws Exception {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier, groupingToken, constraint, refsetSpecNid, config);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    public RefsetSpecStatement addDescStatement(boolean useNotQualifier, I_GetConceptData groupingToken, String constraint,
            int refsetSpecNid) throws Exception {
        RefsetSpecStatement statement = new DescStatement(useNotQualifier, groupingToken, constraint, refsetSpecNid, config);
        statements.add(statement);
        allComponents.add(statement);
        allComponentsNeedsResort = true;
        return statement;
    }

    @SuppressWarnings("unchecked")
    public I_RepresentIdSet getPossibleConceptsInterruptable(final I_RepresentIdSet parentPossibleConcepts,
            final Collection<I_ShowActivity> activities) throws TerminologyException, IOException, ComputationCanceled {

        FutureTask task = new FutureTask(new Callable<I_RepresentIdSet>() {
            public I_RepresentIdSet call() throws Exception {
                return getPossibleConcepts(parentPossibleConcepts, activities);
            }
        });

        refsetExecutorPool.execute(task);

        I_RepresentIdSet results;
        try {
            results = (I_RepresentIdSet) task.get();
            if (results == null) {
                throw new ComputationCanceled("Compute cancelled");
            }
            return results;
        } catch (InterruptedException e) {
            for (I_ShowActivity activity : activities) {
                activity.cancel();
            }
            throw new ComputationCanceled("Compute cancelled");
        } catch (ExecutionException e) {
            if (getRootCause(e) instanceof TerminologyException) {
                throw new TerminologyException(e.getMessage());
            } else if (getRootCause(e) instanceof IOException) {
                throw new IOException(e.getMessage());
            } else if (getRootCause(e) instanceof ComputationCanceled) {
                throw new ComputationCanceled(e.getMessage());
            } else if (getRootCause(e) instanceof InterruptedException) {
                throw new ComputationCanceled(e.getMessage());
            } else {
                System.out.println(">>>>> UNKNOWN exception cause : " + getRootCause(e));
                e.printStackTrace();
                throw new TerminologyException(e);
            }
        }
    }

    private Throwable getRootCause(Exception e) {
        Throwable prevCause = e;
        Throwable rootCause = e.getCause();
        while (rootCause != null) {
            prevCause = rootCause;
            rootCause = rootCause.getCause();
        }

        return prevCause;
    }

    public I_RepresentIdSet getPossibleConcepts(final I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException, ComputationCanceled {
        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }
        I_ShowActivity activity =
                Terms.get().newActivityPanel(true, config, "<html>Possible: <br>" + this.toHtmlFragment(0), true);
        activities.add(activity);
        activity.setMaximum(statements.size() + subqueries.size());
        activity.setValue(0);
        activity.setIndeterminate(true);
        activity.addStopActionListener(new StopActionListener(this, Thread.currentThread()));
        long startTime = System.currentTimeMillis();

        if (allComponentsNeedsResort) {
            Collections.sort(allComponents, calculationOrderComparator);
            allComponentsNeedsResort = false;
        }

        AceLog.getAppLog().info(">> Start of " + this.toString());
        I_RepresentIdSet possibleConcepts = null;
        // process all statements and subqueries
        switch (groupingType) {
        case AND:
        case NEGATED_OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
                //activity.setProgressInfoLower("Spec is invalid - dangling AND.");
                //throw new TerminologyException("Spec is invalid - dangling AND.\n" + this.toString());
            }

            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(parentPossibleConcepts, activities);
                } else {
                    possibleConcepts.and(component.getPossibleConcepts(possibleConcepts, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }
            break;

        case OR:
        case NEGATED_AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
                //activity.setProgressInfoLower("Spec is invalid - dangling OR.");
                //throw new TerminologyException("Spec is invalid - dangling OR.\n" + this.toString());
            }

            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(parentPossibleConcepts, activities);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(parentPossibleConcepts, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }

            break;
        case CONCEPT_CONTAINS_DESC:
        case NOT_CONCEPT_CONTAINS_DESC:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
                //activity.setProgressInfoLower("Spec is invalid - dangling concept-contains-desc.");
                //throw new TerminologyException("Spec is invalid - dangling concept-contains-desc.");
            }
            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(parentPossibleConcepts, activities);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(parentPossibleConcepts, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }
            break;
        case CONCEPT_CONTAINS_REL:
        case NOT_CONCEPT_CONTAINS_REL:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
                //activity.setProgressInfoLower("Spec is invalid - dangling concept-contains-rel.");
                //throw new TerminologyException("Spec is invalid - dangling concept-contains-rel.");
            }

            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(parentPossibleConcepts, activities);
                } else {
                    possibleConcepts.or(component.getPossibleConcepts(parentPossibleConcepts, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }

            break;
		case V1:
		case V2:
			// TODO - EKM
			if (statements.size() == 0 && subqueries.size() == 0) {
				activity.complete();
                activity.setProgressInfoLower("Spec is invalid - dangling " + groupingType + ".");
                throw new TerminologyException("Spec is invalid - dangling " + groupingType + ".\n" + this.toString());
			}

			for (RefsetSpecComponent component : allComponents) {
				if (!continueComputation) {
					throw new ComputationCanceled("Compute cancelled");
				}
				activity.setProgressInfoLower("Initializing...");
				if (possibleConcepts == null) {
                    possibleConcepts = component.getPossibleConcepts(parentPossibleConcepts, activities);
				} else {
                    possibleConcepts.or(component.getPossibleConcepts(parentPossibleConcepts, activities));
				}
				activity.setValue(activity.getValue() + 1);
			}
			break;
		default:
			throw new TerminologyException("Unknown grouping type.");
		}
        long endTime = System.currentTimeMillis();
        long elapsed = endTime - startTime;
        String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);
        AceLog.getAppLog().info(this + " possibleConceptTime: " + elapsedStr);
        if (possibleConcepts == null) {
            possibleConcepts = Terms.get().getEmptyIdSet();
        }
        setPossibleConceptsCount(possibleConcepts.cardinality());
        String incomingCount = "All";
        if (parentPossibleConcepts != null) {
            incomingCount = "" + parentPossibleConcepts.cardinality();
        }
        activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; Incoming count: " + incomingCount + "; Outgoing count: "
            + possibleConcepts.cardinality());

        activity.complete();
        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }
        return possibleConcepts;

    }

    public String toHtmlFragment(int depth) {
        try {
            StringBuffer padding = new StringBuffer();
            padding.append("<br>&nbsp;");
            for (int i = 0; i < depth; i++) {
                padding.append("&nbsp;&nbsp;&nbsp;");
            }
            String paddingStr = padding.toString();
            StringBuffer buff = new StringBuffer();
            buff.append(groupingType.getTruth() + " " + termFactory.getConcept(groupingType.getNid()).getInitialText());
            int count = 0;
            for (RefsetSpecStatement s : statements) {
                buff.append(paddingStr);
                buff.append(s.toHtmlFragment());
                count++;
                if (count > 4) {
                    buff.append(paddingStr);
                    buff.append("<font color='forestgreen'>truncated (count: " + statements.size() + ")...</font>");
                    break;
                }
            }
            for (RefsetSpecQuery sq : subqueries) {
                buff.append(paddingStr);
                buff.append(sq.toHtmlFragment(depth + 1));
            }
            return buff.toString();
        } catch (Exception e) {
            return "UNKNOWN QUERY";
        }
    }

    public String toString() {
        try {
            StringBuffer buff = new StringBuffer();
            buff.append(groupingType.getTruth() + " " + termFactory.getConcept(groupingType.getNid()).getInitialText());
            for (RefsetSpecStatement s : statements) {
                buff.append("\n    ");
                buff.append(s.toString());
            }
            for (RefsetSpecQuery sq : subqueries) {
                buff.append("\n    ");
                buff.append(sq.toString());
            }
            return buff.toString();
        } catch (Exception e) {
            return "UNKNOWN QUERY";
        }
    }

    public boolean execute(I_AmTermComponent component, Collection<I_ShowActivity> activities) throws IOException,
			TerminologyException, ComputationCanceled {
		return execute(component, null, null, null, activities);
	}

    /**
     * Executes the specified query.
     * 
     * @return True if query conditions are met, false otherwise.
     * @throws TerminologyException
     * @throws IOException
     */
    public boolean execute(I_AmTermComponent component, GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is, Collection<I_ShowActivity> activities) throws IOException,
            TerminologyException, ComputationCanceled {

        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }

        if (allComponentsNeedsResort) {
            Collections.sort(allComponents, executionOrderComparator);
            allComponentsNeedsResort = false;
        }

        // process all statements and subqueries
        switch (groupingType) {
        case AND:
        case NEGATED_OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                //throw new TerminologyException("Spec is invalid - dangling AND.");
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (!specComponent.execute(component, version, v1_is, v2_is, activities)) {
                    // can exit the AND early, as at least one statement is
                    // returning false
                    return false;
                }
            }

            // all queries and statements have returned true, therefore AND will
            // return true
            return true;
        case OR:
        case NEGATED_AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                //throw new TerminologyException("Spec is invalid - dangling OR.\n\n" + this.toString() + "\n\n"
                //    + Terms.get().getConcept(getRefsetSpecNid()).toLongString());
            }

            for (RefsetSpecComponent specComponent : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (specComponent.execute(component, version, v1_is, v2_is, activities)) {
                    // exit the OR statement early, as at least one statement
                    // has returned true
                    return true;
                }
            }

            // no queries or statements have returned true, therefore the OR
            // will return false
            return false;
        case CONCEPT_CONTAINS_DESC:
            return executeConceptContainsDesc(component, version, v1_is, v2_is, activities);
        case NOT_CONCEPT_CONTAINS_DESC:
            return !executeConceptContainsDesc(component, version, v1_is, v2_is, activities);
        case CONCEPT_CONTAINS_REL:
            return executeConceptContainsRel(component, version, v1_is, v2_is, activities);
        case NOT_CONCEPT_CONTAINS_REL:
            return !executeConceptContainsRel(component, version, v1_is, v2_is, activities);
		case V1:
		case V2:
			if (statements.size() == 0 && subqueries.size() == 0) {
                throw new TerminologyException("Spec is invalid - dangling " + groupingType + ".\n\n" + this.toString()
                    + "\n\n" + Terms.get().getConcept(getRefsetSpecNid()).toLongString());
			}
			// Implicit OR ??
			for (RefsetSpecComponent specComponent : allComponents) {
				if (!continueComputation) {
					throw new ComputationCanceled("Compute cancelled");
				}
                if (specComponent.execute(component, groupingType, v1_is, v2_is, activities)) {
					// exit the OR statement early, as at least one statement
					// has returned true
					return true;
				}
			}
			// no queries or statements have returned true, therefore the OR
			// will return false
			return false;
		default:
			throw new TerminologyException("Unknown grouping type.");
		}

    }

    private boolean executeConceptContainsDesc(I_AmTermComponent component, GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is, Collection<I_ShowActivity> activities)
            throws TerminologyException, IOException, ComputationCanceled {
        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }
        if (statements.size() == 0 && subqueries.size() == 0) {
            //throw new TerminologyException("Spec is invalid - dangling concept-contains-desc.");
        }

        I_GetConceptData descriptionConcept = (I_GetConceptData) component;
        List<? extends I_DescriptionTuple> descriptionTuples =
                descriptionConcept.getDescriptionTuples(null, null, termFactory.getActiveAceFrameConfig()
                    .getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

        for (I_DescriptionTuple tuple : descriptionTuples) {
            I_DescriptionVersioned descVersioned = tuple.getDescVersioned();
            boolean valid = false;

            for (RefsetSpecStatement statement : statements) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (!statement.execute(descVersioned, version, v1_is, v2_is, activities)) {
                    // can exit the execution early, as at least one statement
                    // is returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            for (RefsetSpecQuery subquery : subqueries) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (!subquery.execute(descVersioned, version, v1_is, v2_is, activities)) {
                    // can exit the execution early, as at least one query is
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
    }

    private boolean executeConceptContainsRel(I_AmTermComponent component, GROUPING_TYPE version, PositionSetBI v1_is, PositionSetBI v2_is, Collection<I_ShowActivity> activities)
            throws TerminologyException, IOException, ComputationCanceled {
        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }
        if (statements.size() == 0 && subqueries.size() == 0) {
            //throw new TerminologyException("Spec is invalid - dangling concept-contains-rel.");
        }

        I_GetConceptData relQueryConcept = (I_GetConceptData) component;
        Collection<? extends I_RelVersioned> relTuples = relQueryConcept.getSourceRels();

        for (I_RelVersioned versionedTuple : relTuples) {
            boolean valid = false;

            for (RefsetSpecStatement statement : statements) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (!statement.execute(versionedTuple, version, v1_is, v2_is, activities)) {
                    // can exit the execution early, as at least one statement
                    // is returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            for (RefsetSpecQuery subquery : subqueries) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                if (!subquery.execute(versionedTuple, version, v1_is, v2_is, activities)) {
                    // can exit the execution early, as at least one query is
                    // returning false
                    valid = false;
                    break;
                } else {
                    valid = true;
                }
            }

            if (valid) { // this relationship meets criteria
                return true;
            }
        }

        return false; // no relationships met criteria

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
        case NEGATED_AND:
            groupingType = GROUPING_TYPE.AND;
            break;
        case OR:
            groupingType = GROUPING_TYPE.AND;
            break;
        case NEGATED_OR:
            groupingType = GROUPING_TYPE.OR;
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
                results.add(((I_AmTermComponent) statement.queryConstraint).getNid());
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

    @SuppressWarnings("unchecked")
    public I_RepresentIdSet getPossibleDescriptionsInterruptable(final I_RepresentIdSet parentPossibleDescriptions,
            final Collection<I_ShowActivity> activities) throws TerminologyException, IOException, ComputationCanceled {

        FutureTask task = new FutureTask(new Callable<I_RepresentIdSet>() {
            public I_RepresentIdSet call() throws Exception {
                return getPossibleDescriptions(parentPossibleDescriptions, activities);
            }
        });

        Executor ex = Executors.newFixedThreadPool(1);
        ex.execute(task);

        I_RepresentIdSet results;
        try {
            results = (I_RepresentIdSet) task.get();
            if (results == null) {
                throw new ComputationCanceled("Compute cancelled");
            }
            return results;
        } catch (InterruptedException e) {
            throw new ComputationCanceled("Compute cancelled");
        } catch (ExecutionException e) {
            if (getRootCause(e) instanceof TerminologyException) {
                throw new TerminologyException(e.getMessage());
            } else if (getRootCause(e) instanceof IOException) {
                throw new IOException(e.getMessage());
            } else if (getRootCause(e) instanceof ComputationCanceled) {
                throw new ComputationCanceled(e.getMessage());
            } else if (getRootCause(e) instanceof InterruptedException) {
                throw new ComputationCanceled(e.getMessage());
            } else {
                System.out.println(">>>>> UNKNOWN exception cause : " + getRootCause(e));
                e.printStackTrace();
                throw new TerminologyException(e);
            }
        }
    }

    public I_RepresentIdSet getPossibleDescriptions(I_RepresentIdSet parentPossibleDescriptions,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException, ComputationCanceled {

        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }

        I_ShowActivity activity =
                Terms.get().newActivityPanel(true, config, "<html>Possible: <br>" + this.toHtmlFragment(0), true);
        activity.setMaximum(statements.size() + subqueries.size());
        activity.setValue(0);
        activity.setIndeterminate(true);
        activity.addStopActionListener(new StopActionListener(this, Thread.currentThread()));

        if (allComponentsNeedsResort) {
            Collections.sort(allComponents, calculationOrderComparator);
            allComponentsNeedsResort = false;
        }

        long startTime = System.currentTimeMillis();
        AceLog.getAppLog().info(">> Start of " + this.toString());
        I_RepresentIdSet possibleDescriptions = null;
        // process all statements and subqueries
        switch (groupingType) {
        case AND:
        case NEGATED_OR:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
            }

            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleDescriptions == null) {
                    possibleDescriptions = component.getPossibleDescriptions(parentPossibleDescriptions, activities);
                } else {
                    possibleDescriptions.and(component.getPossibleDescriptions(possibleDescriptions, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }

            break;
        case OR:
        case NEGATED_AND:
            if (statements.size() == 0 && subqueries.size() == 0) {
                activity.complete();
            }

            for (RefsetSpecComponent component : allComponents) {
                if (!continueComputation) {
                    throw new ComputationCanceled("Compute cancelled");
                }
                activity.setProgressInfoLower("Initializing...");
                if (possibleDescriptions == null) {
                    possibleDescriptions = component.getPossibleDescriptions(parentPossibleDescriptions, activities);
                } else {
                    possibleDescriptions.or(component.getPossibleDescriptions(parentPossibleDescriptions, activities));
                }
                activity.setValue(activity.getValue() + 1);
            }

            break;
        case CONCEPT_CONTAINS_DESC:
            activity.complete();
            activity.setProgressInfoLower("Concept-contains-desc is not supported within a description refset calculation.");
            throw new TerminologyException("Concept-contains-desc is not supported within a description refset calculation.");
        case NOT_CONCEPT_CONTAINS_DESC:
            activity.complete();
            activity
                .setProgressInfoLower("NOT Concept-contains-desc is not supported within a description refset calculation.");
            throw new TerminologyException(
                "NOT Concept-contains-desc is not supported within a description refset calculation.");
        case CONCEPT_CONTAINS_REL:
            activity.complete();
            activity.setProgressInfoLower("Concept-contains-rel is not supported within a description refset calculation.");
            throw new TerminologyException("Concept-contains-rel is not supported within a description refset calculation.");
        case NOT_CONCEPT_CONTAINS_REL:
            activity.complete();
            activity
                .setProgressInfoLower("NOT Concept-contains-rel is not supported within a description refset calculation.");
            throw new TerminologyException(
                "NOT Concept-contains-rel is not supported within a description refset calculation.");
        default:
            throw new TerminologyException("Unknown grouping type.");
        }
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minutes = elapsedTime / 60000;
        long seconds = (elapsedTime % 60000) / 1000;
        AceLog.getAppLog().info(this + " possibleConceptTime: " + minutes + " minutes, " + seconds + " seconds.");
        if (possibleDescriptions == null) {
            possibleDescriptions = Terms.get().getEmptyIdSet();
        }
        setPossibleConceptsCount(possibleDescriptions.cardinality());
        String incomingCount = "All";
        if (parentPossibleDescriptions != null) {
            incomingCount = "" + parentPossibleDescriptions.cardinality();
        }
        activity.setProgressInfoLower("Elapsed: " + elapsedTime + "; Incoming count: " + incomingCount
            + "; Outgoing count: " + possibleDescriptions.cardinality());
        activity.complete();
        if (!continueComputation) {
            throw new ComputationCanceled("Compute cancelled");
        }
        return possibleDescriptions;

    }

    @Override
    public I_RepresentIdSet getPossibleRelationships(I_RepresentIdSet parentPossibleConcepts,
            Collection<I_ShowActivity> activities) throws TerminologyException, IOException {
        throw new TerminologyException("Get possible relationships unimplemented.");
    }

    public class StopActionListener implements ActionListener {

        RefsetSpecQuery query;
        Thread queryThread;

        public StopActionListener(RefsetSpecQuery query, Thread queryThread) {
            this.query = query;
            this.queryThread = queryThread;
        }

        public void actionPerformed(ActionEvent e) {
            query.setContinueComputation(false);
            queryThread.interrupt();
        }
    }

    public void setContinueComputation(boolean continueComputation) {
        this.continueComputation = continueComputation;
    }
}
