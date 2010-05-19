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
package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;

public class ExtensionValidator {

    private I_TermFactory termFactory = null;
    private HashMap<String, Integer> extensions = new HashMap<String, Integer>();

    public ExtensionValidator() throws TerminologyException, IOException {
        termFactory = Terms.get();

        extensions.put(I_HostConceptPlugins.REFSET_TYPES.BOOLEAN.name(), Terms.get().uuidToNative(ConceptConstants.BOOLEAN_EXT.getUuids()));
        extensions.put(I_HostConceptPlugins.REFSET_TYPES.CONCEPT.name(), Terms.get().uuidToNative(ConceptConstants.CONCEPT_EXT.getUuids()));
        extensions.put(I_HostConceptPlugins.REFSET_TYPES.CON_INT.name(), Terms.get().uuidToNative(ConceptConstants.CON_INT_EXT.getUuids()));
        extensions.put(I_HostConceptPlugins.REFSET_TYPES.INTEGER.name(), Terms.get().uuidToNative(ConceptConstants.INT_EXT.getUuids()));
        extensions.put(I_HostConceptPlugins.REFSET_TYPES.STRING.name(), Terms.get().uuidToNative(ConceptConstants.STRING_EXT.getUuids()));

    }// End constructor

    public List<AlertToDataConstraintFailure> validate(int componentId, int refsetType, boolean forCommit)
            throws TaskFailedException {

        List<AlertToDataConstraintFailure> alertList = new ArrayList<AlertToDataConstraintFailure>();
        List<I_GetConceptData> conceptTypesInError = new ArrayList<I_GetConceptData>();
        List<I_GetConceptData> distinctRefsets = new ArrayList<I_GetConceptData>();

        I_GetConceptData inclusionTypeConcept = null;

        try {

            I_IntSet allowedTypes = termFactory.newIntSet();

            // check that the SNOMED is-a exists in the current database before
            // adding it
            if (termFactory.hasId(SNOMED.Concept.IS_A.getUids())) {
                allowedTypes.add(Terms.get().uuidToNative(ConceptConstants.SNOMED_IS_A.getUuids()));
            }

            allowedTypes.add(Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

            // refesetType == null --> do all refset types (wildcarrd)

            if (refsetType == -1) {
                for (I_HostConceptPlugins.REFSET_TYPES rsTypes : I_HostConceptPlugins.REFSET_TYPES.values()) {

                    int typeId = extensions.get(rsTypes.name()).intValue();

                    validate(componentId, typeId, forCommit);
                }// End for loop
            }// End if

            // Could not use switch statement as it requires constant. Did not
            // want to hard code Nid
            if (refsetType == extensions.get(REFSET_TYPES.BOOLEAN.name()).intValue()) {

            } else if (refsetType == extensions.get(REFSET_TYPES.CONCEPT.name()).intValue()) {
                /*
                 * Get concept for Refset Auxilary -> "inclusion type"
                 */
                inclusionTypeConcept = termFactory.getConcept(Terms.get().uuidToNative(ConceptConstants.INCLUSION_TYPE.getUuids()));
            } else if (refsetType == extensions.get(REFSET_TYPES.CON_INT.name()).intValue()) {
            } else if (refsetType == extensions.get(REFSET_TYPES.INTEGER.name()).intValue()) {
            } else if (refsetType == extensions.get(REFSET_TYPES.STRING.name()).intValue()) {
            } 

            if (inclusionTypeConcept == null)
                return alertList;

            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
            /*
             * Get "is a" source rels for concept
             */
            Set<? extends I_GetConceptData> inclusionTypes = inclusionTypeConcept.getDestRelOrigins(null, allowedTypes, null,
                config.getPrecedence(), config.getConflictResolutionStrategy());

            for (I_ExtendByRef ext : termFactory.getAllExtensionsForComponent(componentId, true)) {

                List<? extends I_ExtendByRefPart> extensionVersions = ext.getMutableParts();
                int latest = Integer.MIN_VALUE;
                for (I_ExtendByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() > latest) {
                        latest = currentVersion.getVersion();
                    }
                }// End 1st inner for loop

                boolean alertAdded = false;

                for (I_ExtendByRefPart currentVersion : extensionVersions) {
                    if (currentVersion.getVersion() == latest) {
                        I_ExtendByRefPartCid temp = (I_ExtendByRefPartCid) currentVersion;
                        // System.out.println("ext part version type concept -> "
                        // + termFactory.getConcept(temp.getConceptId()));
                        I_GetConceptData extConceptType = termFactory.getConcept(temp.getC1id());

                        // Check 1 >>> concept value is child of Refset Auxilary
                        // -> inclusion Type
                        if (!inclusionTypes.contains(extConceptType)) {

                            if (!conceptTypesInError.contains(extConceptType)) {
                                conceptTypesInError.add(extConceptType);
                                String alertString = "<html>The concept type " + extConceptType.getInitialText()
                                    + "<br>is not a child of  " + inclusionTypeConcept.getInitialText()
                                    + "<br>Please cancel edits...";

                                AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                                if (forCommit) {
                                    alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                                }
                                AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
                                    alertString, termFactory.getConcept(componentId));

                                if (!alertList.contains(alert)) {
                                    alertList.add(alert);
                                }
                                alertAdded = true;
                            }// End if
                        }// End if

                        // Check 2 >>> refset does not exist twice against
                        // concept
                        I_GetConceptData refsetConcept = termFactory.getConcept(ext.getRefsetId());
                        if (!distinctRefsets.contains(refsetConcept)) {
                            distinctRefsets.add(refsetConcept);
                        } else {
                            String alertString = "<html>The refset " + refsetConcept.getInitialText()
                                + "<br>has been added more than once" + "<br>Please cancel edits...";

                            AlertToDataConstraintFailure.ALERT_TYPE alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                            if (forCommit) {
                                alertType = AlertToDataConstraintFailure.ALERT_TYPE.WARNING;
                            }
                            // AlertToDataConstraintFailure alert = new
                            // AlertToDataConstraintFailure(alertType,
                            // alertString,
                            // termFactory.getConcept(extension.getComponentId()));
                            AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(alertType,
                                alertString, termFactory.getConcept(refsetConcept.getConceptId()));

                            if (!alertList.contains(alert)) {
                                alertList.add(alert);
                            }
                            alertAdded = true;
                        }// End if

                        if (alertAdded)
                            break;

                    }// End 2nd inner for loop

                }// End if
            }// End for loop

        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        }

        return alertList;

    }// End method validate

}// End class ExtensionValidator
