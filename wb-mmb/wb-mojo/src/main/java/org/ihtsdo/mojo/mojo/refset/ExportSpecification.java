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
package org.ihtsdo.mojo.mojo.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.mojo.ConceptDescriptor;
import org.ihtsdo.mojo.mojo.PositionDescriptor;

public class ExportSpecification {

    private ConceptDescriptor root;

    private ConceptDescriptor[] relTypesForHierarchy;

    private PositionDescriptor[] positionsForExport;

    private ConceptDescriptor[] statusValuesForExport;

    private ExportSpecification[] exclusions;

    private CheckSpec spec;

    private class CheckSpec {
        private I_GetConceptData checkSpecRoot;

        private I_IntSet relTypeIntSet;

        private HashSet<I_Position> positions;

        private I_IntSet statusValues;

        private CheckSpec[] checkSpecExclusions;

        private ArrayList<I_GetConceptData> statusValueList;

        private ArrayList<I_GetConceptData> relTypes;

        public CheckSpec() throws Exception {
            super();
            this.checkSpecRoot = root.getVerifiedConcept();
            I_TermFactory termFactory = LocalVersionedTerminology.get();

            if (positionsForExport != null && positionsForExport.length > 0) {
                positions = new HashSet<I_Position>(positionsForExport.length);
                for (PositionDescriptor pd : positionsForExport) {
                    positions.add(pd.getPosition());
                }
            }

            statusValueList = new ArrayList<I_GetConceptData>();
            if (statusValuesForExport != null && statusValuesForExport.length > 0) {
                statusValues = termFactory.newIntSet();
                for (ConceptDescriptor status : statusValuesForExport) {
                    I_GetConceptData statusConcept = status.getVerifiedConcept();
                    statusValues.add(statusConcept.getConceptId());
                    statusValueList.add(statusConcept);
                }
            }

            relTypes = new ArrayList<I_GetConceptData>();
            if (relTypesForHierarchy != null && relTypesForHierarchy.length > 0) {
                relTypeIntSet = termFactory.newIntSet();
                for (ConceptDescriptor relType : relTypesForHierarchy) {
                    I_GetConceptData relTypeConcept = relType.getVerifiedConcept();
                    relTypeIntSet.add(relTypeConcept.getConceptId());
                    relTypes.add(relTypeConcept);
                }
            }

            if (exclusions != null && exclusions.length > 0) {
                this.checkSpecExclusions = new CheckSpec[exclusions.length];
                for (int i = 0; i < checkSpecExclusions.length; i++) {
                    checkSpecExclusions[i] = exclusions[i].getSpec();
                }
            }
            if (AceLog.getAppLog().isLoggable(Level.INFO)) {
                AceLog.getAppLog().info(
                    " Created CheckSpec for positions: " + positions + " with status: " + statusValueList
                        + ", rel types:" + relTypes + ", root:" + checkSpecRoot);
            }

        }

        public String toString() {
            return "CheckSpec positions: " + positions + " with status: " + statusValueList + ", rel types:" + relTypes
                + ", root:" + checkSpecRoot;
        }

        public boolean test(I_GetConceptData testConcept) throws IOException, TerminologyException {
            boolean allowed = false;
            if (checkSpecRoot.equals(testConcept)) {
                return true;
            }
            if (checkSpecRoot.isParentOf(testConcept, statusValues, relTypeIntSet, new PositionSetReadOnly(positions), null, null)
                || root.equals(testConcept)) {
                allowed = true;
            }
            if (allowed && checkSpecExclusions != null) {
                for (CheckSpec excludeSpec : checkSpecExclusions) {
                    if (excludeSpec.test(testConcept)) {
                        allowed = false;
                        break;
                    }
                }
            }
            return allowed;
        }
    }

    private CheckSpec getSpec() throws Exception {
        if (spec == null) {
            spec = new CheckSpec();
        }
        return spec;
    }

    public boolean test(I_GetConceptData testConcept) throws Exception {
        return getSpec().test(testConcept);
    }

    public ConceptDescriptor getRoot() {
        return root;
    }

    public void setRoot(ConceptDescriptor root) {
        this.root = root;
    }

    public ConceptDescriptor[] getRelTypesForHierarchy() {
        return relTypesForHierarchy;
    }

    public void setRelTypesForHierarchy(ConceptDescriptor[] relTypesForHierarchy) {
        this.relTypesForHierarchy = relTypesForHierarchy;
    }

    public ConceptDescriptor[] getStatusValuesForExport() {
        return statusValuesForExport;
    }

    public void setStatusValuesForExport(ConceptDescriptor[] statusValuesForExport) {
        this.statusValuesForExport = statusValuesForExport;
    }

    public PositionDescriptor[] getPositionsForExport() {
        return positionsForExport;
    }

    public void setPositionsForExport(PositionDescriptor[] positionsForExport) {
        this.positionsForExport = positionsForExport;
    }

    public ExportSpecification[] getExclusions() {
        return exclusions;
    }

    public void setExclusions(ExportSpecification[] exclusions) {
        this.exclusions = exclusions;
    }
}
