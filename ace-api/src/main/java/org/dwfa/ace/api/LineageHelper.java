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
package org.dwfa.ace.api;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

public class LineageHelper {

    protected I_TermFactory termFactory;

    protected Set<I_Position> viewPositions;
    protected I_IntSet allowedStatuses;
    protected I_IntSet isARelTypes;

    private Logger logger = Logger.getLogger(LineageHelper.class.getName());

    public LineageHelper() {
        try {
            termFactory = LocalVersionedTerminology.get();
        } catch (Exception e) {
            throw new TerminologyRuntimeException(e);
        }
    }

    protected I_TermFactory getTermFactory() {
        return termFactory;
    }

    public Set<I_GetConceptData> getParents(I_GetConceptData concept) throws Exception {
        return getAllAncestors(concept, new FirstRelationOnly());
    }

    public Set<I_GetConceptData> getChildren(I_GetConceptData concept) throws Exception {
        return getAllDescendants(concept, new FirstRelationOnly());
    }

    /**
     * Get all the ancestors (parents, parents of parents, etc) of a particular
     * concept.
     */
    public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept, Condition... conditions) throws Exception {

        if (conditions == null) {
            conditions = new Condition[] { new NotAlreadyVisited() };
        }

        // find all the parents
        Set<I_GetConceptData> parentConcepts = getAllAncestors(new HashSet<I_GetConceptData>(), concept,
            getAllowedStatuses(), getIsARelTypes(), getViewPositions(), 0, conditions);

        logger.fine("Found " + parentConcepts.size() + " ancestors of concept '" + concept.getInitialText() + "'.");

        return parentConcepts;
    }

    protected Set<I_GetConceptData> getAllAncestors(Set<I_GetConceptData> resultSet, I_GetConceptData child,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, int distance,
            Condition... conditions) throws Exception {

        ITERATE_PARENTS: for (I_RelTuple childTuple : child.getSourceRelTuples(allowedStatuses, allowedTypes,
            positions, false, true)) {
            I_GetConceptData parentConcept = termFactory.getConcept(childTuple.getC2Id());
            if (parentConcept.getConceptId() == child.getConceptId()) {
                continue ITERATE_PARENTS;
            }
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.evaluate(parentConcept, distance)) {
                        continue ITERATE_PARENTS;
                    }
                }
            }
            if (resultSet.add(parentConcept)) {
                resultSet.addAll(getAllAncestors(resultSet, parentConcept, allowedStatuses, allowedTypes, positions,
                    distance + 1, conditions));
            }
        }
        return resultSet;
    }

    /**
     * Get all the descendants (children, children of children, etc) of a
     * particular concept.
     */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, Condition... conditions) throws Exception {

        if (conditions == null) {
            conditions = new Condition[] { new NotAlreadyVisited() };
        }
        
        // find all the children
        Set<I_GetConceptData> descendants = getAllDescendants(new HashSet<I_GetConceptData>(), concept,
            getAllowedStatuses(), getIsARelTypes(), getViewPositions(), 0, conditions);

        logger.fine("Found " + descendants.size() + " descendants of concept '" + concept.getInitialText() + "'.");

        return descendants;
    }

    protected Set<I_GetConceptData> getAllDescendants(Set<I_GetConceptData> resultSet, I_GetConceptData parent,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, Set<I_Position> positions, int distance,
            Condition... conditions) throws Exception {

        ITERATE_CHILDREN: for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes,
            positions, false, true)) {
            I_GetConceptData childConcept = termFactory.getConcept(childTuple.getC1Id());
            if (childConcept.getConceptId() == parent.getConceptId()) {
                continue ITERATE_CHILDREN;
            }
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.evaluate(childConcept, distance)) {
                        continue ITERATE_CHILDREN;
                    }
                }
            }
            if (resultSet.add(childConcept)) {
                resultSet.addAll(getAllDescendants(resultSet, childConcept, allowedStatuses, allowedTypes, positions,
                    distance + 1, conditions));
            }
        }

        return resultSet;
    }

    public boolean hasAncestor(I_GetConceptData concept, I_GetConceptData ancestor) throws TerminologyException {
        try {
            CeaseWhenFound foundCondition = new CeaseWhenFound(ancestor);
            getAllAncestors(concept, foundCondition, new NotAlreadyVisited());
            return foundCondition.wasFound();

        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    public boolean hasDescendant(I_GetConceptData concept, I_GetConceptData descendant) throws TerminologyException {
        try {
            CeaseWhenFound foundCondition = new CeaseWhenFound(descendant);
            getAllDescendants(concept, foundCondition, new NotAlreadyVisited());
            return foundCondition.wasFound();

        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    /**
     * @return The view positions from the active config.
     *         Returns null if no config set or config contains no view
     *         positions.
     */
    protected Set<I_Position> getViewPositions() throws Exception {
        if (this.viewPositions == null) {
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            if (config != null) {
                this.viewPositions = config.getViewPositionSet();
            }

            if (this.viewPositions == null) {
                this.viewPositions = new HashSet<I_Position>();
            }
        }

        return (this.viewPositions.isEmpty()) ? null : this.viewPositions;
    }

    /**
     * @return The allowed status from the active config.
     *         Returns just "CURRENT" if no config set.
     */
    protected I_IntSet getAllowedStatuses() throws Exception {
        if (this.allowedStatuses == null) {
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

            if (config != null) {
                this.allowedStatuses = config.getAllowedStatus();
            } else {
                this.allowedStatuses = termFactory.newIntSet();
                this.allowedStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
            }
        }

        return this.allowedStatuses;
    }

    /**
     * @return By default (unless overridden by a subclass) will provide both
     *         the SNOMED and ArchitectonicAuxiliary IS_A concepts.
     */
    protected I_IntSet getIsARelTypes() throws Exception {
        if (this.isARelTypes == null) {
            this.isARelTypes = termFactory.newIntSet();
            this.isARelTypes.add(SNOMED.Concept.IS_A.localize().getNid());
            this.isARelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        }
        return this.isARelTypes;
    }

    protected void setIsARelTypes(final I_IntSet isARelTypes) {
        this.isARelTypes = isARelTypes;
    }

    /**
     * A simple template for logic that defines if a process should be executed
     * on a particular subject (concept).
     */
    public interface Condition {
        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception;
    }

    public class NotAlreadyVisited implements Condition {
        private HashSet<Integer> visited = new HashSet<Integer>();

        public NotAlreadyVisited() {
        };

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            return visited.add(concept.getConceptId());
        }
    }

    public class OrOperator implements Condition {
        private Condition[] conditions;

        public OrOperator(Condition... conditions) {
            this.conditions = conditions;
        }

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            for (Condition condition : this.conditions) {
                if (condition.evaluate(concept, distance)) {
                    return true;
                }
            }
            return false;
        }
    }

    public class CeaseWhenFound implements Condition {
        private boolean found = false;
        private I_GetConceptData concept;

        public CeaseWhenFound(I_GetConceptData concept) {
            this.concept = concept;
        };

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            if (found) {
                return false;
            } else {
                if (this.concept.equals(concept)) {
                    found = true;
                    return false;
                }
            }
            return true;
        }

        public boolean wasFound() {
            return this.found;
        }
    }

    public class FirstRelationOnly implements Condition {
        private boolean firstTime = true;

        public FirstRelationOnly() {
        }

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            if (firstTime) {
                firstTime = false;
                return true;
            } else {
                return false;
            }
        }
    }

    public class LimitedDistance implements Condition {
        private int limit;

        public LimitedDistance(int limit) {
            this.limit = limit;
        }

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            return (distance <= limit);
        }
    }

    public class DistanceTrackingCondition implements Condition {

        private TreeMap<Integer, HashSet<Integer>> distanceMap = new TreeMap<Integer, HashSet<Integer>>();

        public boolean evaluate(I_GetConceptData concept, int distance) throws Exception {
            if (distanceMap.containsKey(distance)) {
                distanceMap.get(distance).add(concept.getConceptId());
            } else {
                HashSet<Integer> conceptIds = new HashSet<Integer>();
                conceptIds.add(concept.getConceptId());
                distanceMap.put(distance, conceptIds);
            }
            return true;
        }

        public Collection<Integer> getConcepts(int distance) {
            return distanceMap.get(distance);
        }

        public int getMinimumDistance(int conceptId) {
            // Already sorted by distance so first found will be the minimum
            for (Integer distance : distanceMap.keySet()) {
                if (distanceMap.get(distance).contains(conceptId)) {
                    return distance;
                }
            }
            // Not found
            return -1;
        }

    }
}
