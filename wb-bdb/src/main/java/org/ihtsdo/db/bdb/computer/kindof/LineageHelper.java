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
package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpLineage;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

public class LineageHelper implements I_HelpLineage {

    private Logger logger = Logger.getLogger(LineageHelper.class.getName());
	private I_ConfigAceFrame config;
	private long lastAccess = System.currentTimeMillis();
	private boolean clone = false;

    public LineageHelper(I_ConfigAceFrame config) {
    	this(config, null);
    }
    public LineageHelper(I_ConfigAceFrame config, I_IntSet isARelTypes) {
		this.config = config;
		if (isARelTypes != null) {
			useConfigClone();
			this.config.getDestRelTypes().clear();
			this.config.getDestRelTypes().addAll(isARelTypes.getSetValues());
		}
    }
	protected void useConfigClone()  {
		try {
			if (!clone) {
				MarshalledObject<I_ConfigAceFrame> marshalledConfig = new MarshalledObject<I_ConfigAceFrame>(config);
				this.config = marshalledConfig.get();
				this.clone = true;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

    protected void access() {
    	lastAccess = System.currentTimeMillis();
    }
    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#getParents(org.dwfa.ace.api.I_GetConceptData)
	 */
    public Set<I_GetConceptData> getParents(I_GetConceptData concept) throws Exception {
    	access();
    	return getAllAncestors(concept, new FirstRelationOnly());
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#getChildren(org.dwfa.ace.api.I_GetConceptData)
	 */
    public Set<I_GetConceptData> getChildren(I_GetConceptData concept) throws Exception {
    	access();
        return getAllDescendants(concept, new FirstRelationOnly());
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#getAllAncestors(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.LineageHelper.Condition)
	 */
    public Set<I_GetConceptData> getAllAncestors(I_GetConceptData concept, LineageCondition... conditions) throws Exception {
    	access();

        if (conditions == null) {
            conditions = new LineageCondition[] { new NotAlreadyVisited() };
        }

        // find all the parents
        Set<I_GetConceptData> parentConcepts = getAllAncestors(new HashSet<I_GetConceptData>(), concept,
            getAllowedStatuses(), getIsARelTypes(), getViewPositions(), conditions);

        logger.fine("Found " + parentConcepts.size() + " ancestors of concept '" + concept.getInitialText() + "'.");

        return parentConcepts;
    }

    protected Set<I_GetConceptData> getAllAncestors(Set<I_GetConceptData> resultSet, I_GetConceptData child,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, PositionSetReadOnly positions, LineageCondition... conditions)
            throws Exception {
    	access();

        ITERATE_PARENTS: for (I_RelTuple childTuple : child.getSourceRelTuples(allowedStatuses, allowedTypes,
            positions,
            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            I_GetConceptData parentConcept = Terms.get().getConcept(childTuple.getC2Id());
            if (parentConcept.getConceptId() == child.getConceptId()) {
                continue ITERATE_PARENTS;
            }
            if (conditions != null) {
                for (LineageCondition condition : conditions) {
                    if (!condition.evaluate(parentConcept)) {
                        continue ITERATE_PARENTS;
                    }
                }
            }
            if (resultSet.add(parentConcept)) {
                resultSet.addAll(getAllAncestors(resultSet, parentConcept, allowedStatuses, allowedTypes, positions,
                    conditions));
            }
        }
        return resultSet;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#getAllDescendants(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.LineageHelper.Condition)
	 */
    public Set<I_GetConceptData> getAllDescendants(I_GetConceptData concept, LineageCondition... conditions) throws Exception {
    	access();

        if (conditions == null) {
            conditions = new LineageCondition[] { new NotAlreadyVisited() };
        }

        // find all the children
        Set<I_GetConceptData> descendants = getAllDescendants(new HashSet<I_GetConceptData>(), concept,
            getAllowedStatuses(), getIsARelTypes(), getViewPositions(), conditions);

        logger.fine("Found " + descendants.size() + " descendants of concept '" + concept.getInitialText() + "'.");

        return descendants;
    }

    protected Set<I_GetConceptData> getAllDescendants(Set<I_GetConceptData> resultSet, I_GetConceptData parent,
            I_IntSet allowedStatuses, I_IntSet allowedTypes, PositionSetReadOnly positions, LineageCondition... conditions)
            throws Exception {

    	access();
        ITERATE_CHILDREN: for (I_RelTuple childTuple : parent.getDestRelTuples(allowedStatuses, allowedTypes,
            positions,
            getConfig().getPrecedence(), getConfig().getConflictResolutionStrategy())) {
            I_GetConceptData childConcept = Terms.get().getConcept(childTuple.getC1Id());
            if (childConcept.getConceptId() == parent.getConceptId()) {
                continue ITERATE_CHILDREN;
            }
            if (conditions != null) {
                for (LineageCondition condition : conditions) {
                    if (!condition.evaluate(childConcept)) {
                        continue ITERATE_CHILDREN;
                    }
                }
            }
            if (resultSet.add(childConcept)) {
                resultSet.addAll(getAllDescendants(resultSet, childConcept, allowedStatuses, allowedTypes, positions,
                    conditions));
            }
        }

        return resultSet;
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#hasAncestor(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_GetConceptData)
	 */
    public boolean hasAncestor(I_GetConceptData concept, I_GetConceptData ancestor) throws TerminologyException {
    	access();
        try {
            CeaseWhenFound foundCondition = new CeaseWhenFound(ancestor);
            getAllAncestors(concept, foundCondition, new NotAlreadyVisited());
            return foundCondition.wasFound();

        } catch (Exception e) {
            throw new TerminologyException(e);
        }
    }

    /* (non-Javadoc)
	 * @see org.dwfa.ace.api.I_HelpLineage#hasDescendant(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_GetConceptData)
	 */
    public boolean hasDescendant(I_GetConceptData concept, I_GetConceptData descendant) throws TerminologyException {
    	access();
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
    protected PositionSetReadOnly getViewPositions() throws Exception {
    	access();
        return config.getViewPositionSetReadOnly();
    }

    /**
     * @return The allowed status from the active config.
     *         Returns just "CURRENT" if no config set.
     */
    protected I_IntSet getAllowedStatuses() throws Exception {
    	access();
        return config.getAllowedStatus();
    }

    /**
     * @return By default (unless overridden by a subclass) will provide both
     *         the SNOMED and ArchitectonicAuxiliary IS_A concepts.
     */
    protected I_IntSet getIsARelTypes() throws Exception {
    	access();
        return getConfig().getDestRelTypes();
    }

    protected class NotAlreadyVisited implements LineageCondition {
        private HashSet<Integer> visited = new HashSet<Integer>();

        public NotAlreadyVisited() {
        };

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            return visited.add(concept.getConceptId());
        }
    }

    protected class OrOperator implements LineageCondition {
        private LineageCondition[] conditions;

        public OrOperator(LineageCondition... conditions) {
            this.conditions = conditions;
        }

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            for (LineageCondition condition : this.conditions) {
                if (condition.evaluate(concept)) {
                    return true;
                }
            }
            return false;
        }
    }

    protected class CeaseWhenFound implements LineageCondition {
        private boolean found = false;
        private I_GetConceptData concept;

        public CeaseWhenFound(I_GetConceptData concept) {
            this.concept = concept;
        };

        public boolean evaluate(I_GetConceptData concept) throws Exception {
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

    protected class FirstRelationOnly implements LineageCondition {
        private boolean firstTime = true;

        public FirstRelationOnly() {
        }

        public boolean evaluate(I_GetConceptData concept) throws Exception {
            if (firstTime) {
                firstTime = false;
                return true;
            } else {
                return false;
            }
        }
    }

	public I_ConfigAceFrame getConfig() {
		return config;
	}
	public long getLastAccess() {
		return lastAccess;
	}

}
