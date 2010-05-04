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
package org.dwfa.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.refset.ExportSpecification;
import org.dwfa.tapi.TerminologyException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Given a root node, this mojo will copy the latest version of every component
 * in this hierarchy
 * o the destination path if it isn't already on that path.
 *
 * @goal copy-hierarchy-to-path
 *
 */
public class CopyHierarchyToPath extends AbstractMojo implements I_ProcessConcepts {

    /**
     * Path to copy the data to
     *
     * @parameter
     * @required
     */
    ConceptDescriptor toPath = null;

    /**
     * Root node to copy data from
     *
     * @parameter
     * @required
     */
    ConceptDescriptor rootNode = null;

    /**
     * Relationship type for the hierarchy - defaults to SNOMED "Is a"
     *
     * @parameter
     */
    ConceptDescriptor[] hierarchyRelationshipTypes = null;

    /**
     * Allowed statuses for the hierarchy - defaults to active or a child of
     * active
     *
     * @parameter
     */
    ConceptDescriptor[] hierarchyStatuses = null;

    /**
     * Exclusion from the hierarchy
     *
     * @parameter
     */
    ExportSpecification[] exclusions = null;

    /**
     * Flag that indicates if the just the latest data or all history should be
     * copied - defaults to latest only
     *
     * @parameter
     */
    boolean latestStateOnly = true;

    /**
     * Indicates whether to copy inactive versions of an entity even if the
     * entity doesn't
     * exist on the target path (true), or to omit inactive versions in the copy
     * if the
     * entity doesn't exist in the target path (false). Default is false.
     *
     * @parameter
     */
    private boolean copyInactiveVersionsNotInTarget = false;

    private int toPathId;

    private I_TermFactory tf;

    private I_WriteDirectToDb directInterface;

    private int conceptAttributeCount;

    private int descriptionCount;

    private int extCount;

    private int idCount;

    private int imageCount;

    private int relCount;

    private int conceptCount;

    private I_IntSet allowedStatus;

    private I_GetConceptData active;

    private Map<Class, Method> methodCache = new HashMap<Class, Method>();

    /**
     * Allows specification of paths for source data that should be excluded
     * from
     * the copy
     *
     * @parameter
     */
    private ConceptDescriptor[] excludedPaths;

    private List<Integer> excludedPathIds = new ArrayList<Integer>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        tf = LocalVersionedTerminology.get();

        try {

            if (hierarchyRelationshipTypes == null) {

                hierarchyRelationshipTypes = new ConceptDescriptor[] { new ConceptDescriptor(
                    "c93a30b9-ba77-3adb-a9b8-4589c9f8fb25", "Is a (attribute)") };
            }

            if (hierarchyStatuses == null) {

                // Initialise the array of allowed statuses (if none are provide
                // from the configuration)
                // with the status concept "active" and its children

                ConceptDescriptor activeStatus = new ConceptDescriptor("32dc7b19-95cc-365e-99c9-5095124ebe72", "active");
                ConceptDescriptor currentStatus = new ConceptDescriptor("2faa9261-8fb2-11db-b606-0800200c9a66",
                    "current");

                hierarchyStatuses = new ConceptDescriptor[] { activeStatus, currentStatus };

                Set<I_GetConceptData> children = activeStatus.getVerifiedConcept().getDestRelOrigins(
                    toIntSet(hierarchyStatuses), toIntSet(hierarchyRelationshipTypes), null, false, false);

                ArrayList<ConceptDescriptor> statusDescriptors = new ArrayList<ConceptDescriptor>();
                statusDescriptors.add(activeStatus);
                for (I_GetConceptData child : children) {
                    ConceptDescriptor childStatus = new ConceptDescriptor();
                    childStatus.setDescription(child.getInitialText());
                    childStatus.setUuid(child.getUids().get(0).toString());
                    statusDescriptors.add(childStatus);
                }

                hierarchyStatuses = statusDescriptors.toArray(new ConceptDescriptor[statusDescriptors.size()]);
            }

            if (excludedPaths != null) {
                for (ConceptDescriptor path : excludedPaths) {
                    excludedPathIds.add(path.getVerifiedConcept().getConceptId());
                }
            }

            allowedStatus = toIntSet(hierarchyStatuses);

            toPathId = toPath.getVerifiedConcept().getConceptId();
            directInterface = tf.getDirectInterface();

            active = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());

            processAllChildren(rootNode.getVerifiedConcept(), toIntSet(hierarchyRelationshipTypes), allowedStatus);
        } catch (Exception e) {
            throw new MojoExecutionException("Failed executing hierarchy copy due to exception", e);
        }

    }

    private I_IntSet toIntSet(ConceptDescriptor[] concepts) throws Exception {
        I_IntSet intset = tf.newIntSet();
        for (ConceptDescriptor conceptDescriptor : concepts) {
            intset.add(conceptDescriptor.getVerifiedConcept().getConceptId());
        }
        return intset;
    }

    private void processAllChildren(I_GetConceptData concept, I_IntSet allowedType, I_IntSet allowedStatus)
            throws Exception {
        processConcept(concept);

        Set<I_GetConceptData> children = concept.getDestRelOrigins(allowedStatus, allowedType, null, false, false);
        for (I_GetConceptData getConceptData : children) {
            processAllChildren(getConceptData, allowedType, allowedStatus);
        }
    }

    public void processConcept(I_GetConceptData concept) throws Exception {
        if (++conceptCount % 1000 == 0) {
            getLog().info("processed concept " + conceptCount);
        }

        if (inExclusions(concept)) {
            getLog().info("Suppressed copy of " + concept + " due to exclusions");
        } else {
            getLog().info("concept " + concept + " copied to path " + toPath);

            processConceptAttributes(concept.getConceptAttributes());
            processDescription(concept.getDescriptions());
            processExtensions(concept.getConceptId());
            processId(concept.getId());
            processImages(concept.getImages());
            processRelationship(concept.getSourceRels());
        }
    }

    private void processExtensions(int id) throws IOException, Exception {
        for (I_ThinExtByRefVersioned ext : tf.getRefsetExtensionMembers(id)) {
            processExtensionByReference(ext);
        }
    }

    private boolean inExclusions(I_GetConceptData concept) throws Exception {
        if (exclusions == null) {
            return false;
        }
        for (ExportSpecification spec : exclusions) {
            if (spec.test(concept)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the latest tuples for the given pathid - if the pathid is null the
     * latest
     * tuples will be returned for all paths
     *
     * @param <T> tuple type
     * @param tuples list of tuples to examine
     * @param pathid path
     * @return
     */
    private <T extends I_AmTuple> Map<Integer, T> getLatestTuples(Collection<T> tuples, Integer pathid) {
        Map<Integer, T> map = new HashMap<Integer, T>();
        for (T tuple : tuples) {
            if (pathid != null && tuple.getPathId() == pathid) {
                int termComponentId = tuple.getFixedPart().getNid();
                if (map.containsKey(termComponentId)) {
                    if (map.get(termComponentId).getVersion() < tuple.getVersion()) {
                        map.put(termComponentId, tuple);
                    }
                } else {
                    map.put(termComponentId, tuple);
                }
            }
        }
        return map;
    }

    public void processConceptAttributes(I_ConceptAttributeVersioned conceptAttributeVersioned) throws Exception {

        if (++conceptAttributeCount % 1000 == 0) {
            getLog().info("processed concept attribute " + conceptAttributeCount);
        }

        List<I_ConceptAttributeTuple> allTuples = conceptAttributeVersioned.getTuples();

        boolean datachanged = processEntity(conceptAttributeVersioned, allTuples);
        if (datachanged) {
            directInterface.writeConceptAttributes(conceptAttributeVersioned);
        }
    }

    private <T extends I_AmTuple> boolean processEntity(I_AmTermComponent termComponent, List<T> allTuples)
            throws Exception {
        I_AmTuple latestPart = null;
        boolean datachanged = false;

        // are we copying all versions or just the latest state
        if (latestStateOnly) {
            // just the latest state - find the latest exportable part
            for (T t : allTuples) {
                if (!excludedPathIds.contains(t.getPathId())
                    && (latestPart == null || t.getVersion() > latestPart.getVersion())) {
                    latestPart = t;
                }
            }
            Map<Integer, T> latestInTargetPath = getLatestTuples(allTuples, toPathId);
            // if a part was found to copy and we are either
            // - copying parts regardless of the target path state
            // - the target path contains a version of the entity already
            // - or the latest part is active
            // then copy the part
            if (latestPart != null
                && latestPart.getPathId() != toPathId
                && (copyInactiveVersionsNotInTarget
                    || latestInTargetPath.containsKey(latestPart.getFixedPart().getNid()) || isActive(latestPart.getStatusId()))) {
                duplicateTuple(latestPart);
                datachanged = true;
            }
        } else {
            // straight copy
            for (T t : allTuples) {
                if (!excludedPathIds.contains(t.getPathId()) && toPathId != t.getPathId()) {
                    duplicateTuple(t);
                    datachanged = true;
                }
            }
        }
        return datachanged;
    }

    private boolean isActive(int statusId) throws IOException, TerminologyException {
        return active.isParentOf(tf.getConcept(statusId), true);
    }

    private void processDescription(List<I_DescriptionVersioned> descriptions) throws Exception {
        for (I_DescriptionVersioned descriptionVersioned : descriptions) {
            processDescription(descriptionVersioned);
            processId(tf.getId(descriptionVersioned.getDescId()));
            processExtensions(descriptionVersioned.getDescId());
        }
    }

    public void processDescription(I_DescriptionVersioned descriptionVersioned) throws Exception {

        if (++descriptionCount % 1000 == 0) {
            getLog().info("processed description " + descriptionCount);
        }

        List<I_DescriptionTuple> allTuples = descriptionVersioned.getTuples();

        boolean datachanged = processEntity(descriptionVersioned, allTuples);

        if (datachanged) {
            directInterface.writeDescription(descriptionVersioned);
        }
    }

    public void processExtensionByReference(I_ThinExtByRefVersioned extByRef) throws Exception {

        if (++extCount % 1000 == 0) {
            getLog().info("processed extension " + extCount);
        }

        List<I_ThinExtByRefTuple> allTuples = extByRef.getTuples(null, null, true, false);

        boolean datachanged = processEntity(extByRef, allTuples);

        if (datachanged) {
            directInterface.writeExt(extByRef);
        }
    }

    public void processId(I_IdVersioned idVersioned) throws Exception {

        if (++idCount % 1000 == 0) {
            getLog().info("processed id " + idCount);
        }

        List<I_IdTuple> allTuples = idVersioned.getTuples();

        boolean datachanged = processEntity(idVersioned, allTuples);

        if (datachanged) {
            directInterface.writeId(idVersioned);
        }
    }

    private void processImages(List<I_ImageVersioned> images) throws Exception {
        for (I_ImageVersioned imageVersioned : images) {
            processImages(imageVersioned);
            processId(tf.getId(imageVersioned.getImageId()));
            processExtensions(imageVersioned.getImageId());
        }
    }

    public void processImages(I_ImageVersioned imageVersioned) throws Exception {

        if (++imageCount % 1000 == 0) {
            getLog().info("processed image " + imageCount);
        }

        boolean datachanged = processEntity(imageVersioned, imageVersioned.getTuples());

        if (datachanged) {
            directInterface.writeImage(imageVersioned);
        }
    }

    private void processRelationship(List<I_RelVersioned> sourceRels) throws Exception {
        for (I_RelVersioned relVersioned : sourceRels) {
            processRelationship(relVersioned);
            processId(tf.getId(relVersioned.getRelId()));
            processExtensions(relVersioned.getRelId());
        }
    }

    public void processRelationship(I_RelVersioned relVersioned) throws Exception {

        if (++relCount % 1000 == 0) {
            getLog().info("processed relationship " + relCount);
        }

        boolean datachanged = processEntity(relVersioned, relVersioned.getTuples());

        if (datachanged) {
            directInterface.writeRel(relVersioned);
        }
    }

    private <T extends I_AmTuple> void duplicateTuple(T latestPart) throws Exception {
        I_AmPart newPart = latestPart.duplicate();
        newPart.setPathId(toPathId);

        I_AmTermComponent termComponent = latestPart.getFixedPart();

        getAddVersionMethod(newPart, termComponent).invoke(termComponent, newPart);
    }

    private Method getAddVersionMethod(I_AmPart newPart, I_AmTermComponent termComponent) throws Exception,
            NoSuchMethodException {

        Class<? extends I_AmTermComponent> termComponentClass = termComponent.getClass();
        Method result = methodCache.get(termComponentClass);

        if (result == null) {
            Class<? extends I_AmPart> partClass = null;
            if (newPart instanceof I_ThinExtByRefPart) {
                partClass = I_ThinExtByRefPart.class;
            } else {
                for (Class interfaceClass : newPart.getClass().getInterfaces()) {
                    if (I_AmPart.class.isAssignableFrom(interfaceClass)) {
                        partClass = interfaceClass;
                        break;
                    }
                }
                if (partClass == null) {
                    throw new Exception(newPart.getClass() + " does not implement a child of I_AmPart");
                }
            }
            result = termComponentClass.getMethod("addVersion", partClass);
            methodCache.put(termComponentClass, result);
        }
        return result;
    }

}
