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
package org.dwfa.ace.path;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TupleKey;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 */
public class CopyPathToPath implements I_ProcessConcepts {

    /**
     * Indicate if all history or only the latest state of the objects should be
     * copied - defaults to false
     */
    private boolean copyOnlyLatestState = false;

    /**
     * Indicates whether to read all parts of the object and copy any found, or
     * only the very latest part in time sequence across all paths
     */
    private boolean readLatestPartOnly = false;

    /**
     * Indicates whether to copy inactive versions of an entity even if the
     * entity doesn't
     * exist on the target path (true), or to omit inactive versions in the copy
     * if the
     * entity doesn't exist in the target path (false). Default is false.
     */
    private boolean copyInactiveVersionsNotInTarget = false;

    /**
     * Indicates whether to require a a concept to have attributes
     */
    private boolean validate = true;

    private HashSet<I_Position> sourcePositions = new HashSet<I_Position>();

    private int targetPathId;

    private int versionTime = Integer.MAX_VALUE;

    private int statusId = 0;

    private boolean duplicateVersionDescriptions = Boolean.FALSE;

    private boolean duplicateVersionRelationships = Boolean.FALSE;

    private I_TermFactory tf;

    private I_WriteDirectToDb directInterface;

    private int conceptAttributeCount;

    private int descriptionCount;

    private int extCount;

    private int idCount;

    private int imageCount;

    private int relCount;

    private int conceptCount;

    private List<I_DescriptionTuple> duplicateDescTuples = new ArrayList<I_DescriptionTuple>();

    private Map<TupleKey,List<I_RelTuple>> duplicateRelTuples = new HashMap<TupleKey, List<I_RelTuple>>();

    private Logger logger = Logger.getLogger(CopyPathToPath.class.getName());

    private I_GetConceptData active;

    private Map<Class, Method> methodCache = new HashMap<Class, Method>();

    public CopyPathToPath() throws IOException, TerminologyException {
        tf = LocalVersionedTerminology.get();
        directInterface = tf.getDirectInterface();
        active = tf.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
    }

    public void processConcept(I_GetConceptData concept) throws Exception {

        try {
            if (versionTime == Integer.MAX_VALUE) {
                setVersionTime(tf.convertToThinVersion(System.currentTimeMillis()));
            }

            if (++conceptCount % 1000 == 0) {
                logger.info("processed concept " + conceptCount);
            }

            duplicateDescTuples = new ArrayList<I_DescriptionTuple>();
            duplicateRelTuples = new HashMap<TupleKey, List<I_RelTuple>>();

            if (validate && hasAttributes(concept) || hasAttributes(concept)) {
                processConceptAttributes(concept.getConceptAttributes());
            }

            processDescription(concept.getDescriptions());
            processExtensions(concept.getConceptId());
            processId(concept.getId());
            processImages(concept.getImages());
            processRelationship(concept.getSourceRels());

            // Log all Duplicate-versioned descriptions and relationships on the
            // same path and timestamp here
            if (!duplicateDescTuples.isEmpty()) {
                duplicateVersionDescriptions = Boolean.TRUE;
                logger.severe("***** Duplicate-versioned description tuple(s) in concept: " + concept.getUids().get(0));
                for (I_DescriptionTuple tuple : duplicateDescTuples) {
                    logger.severe("Desc id: " + tuple.getDescId() + ", Path id: " + tuple.getPathId() + ", Version: "
                        + tuple.getVersion() + "\n\t(" + tuple.getText() + ")");
                }
            }
            for (List<I_RelTuple> duplicateList : duplicateRelTuples.values()) {
                if (duplicateList.size() > 1) {
                    duplicateVersionRelationships = Boolean.TRUE;
                    logger.severe("***** Duplicate-versioned relationship tuple(s) in concept: " + concept.getUids().get(0));
                    logger.severe("Rel id: " + duplicateList.get(0).getRelId() + " Has " + duplicateList.size());
                    for (I_RelTuple duplicateIRelTuple : duplicateList) {
                        logger.severe("Path id: " + tf.getConcept(duplicateIRelTuple.getPathId()).getInitialText()
                            + ", Version: "
                            + AceDateFormat.getRf2TimezoneDateFormat().format(new Date(duplicateIRelTuple.getTime()))
                            + "\n\t(" + tf.getConcept(duplicateIRelTuple.getTypeId()).getInitialText() + ")\n\t"
                            + duplicateIRelTuple.getRelId() + " " + duplicateIRelTuple.getC1Id() + " " + duplicateIRelTuple.getC2Id() + "\n\t"
                            + duplicateIRelTuple.getCharacteristicId() + " " + duplicateIRelTuple.getGroup() + "\n\t"
                            + duplicateIRelTuple.getRefinabilityId() + " " + duplicateIRelTuple.getStatusId());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("exception on processing concept " + concept);
            throw e;
        }
    }

    private boolean hasAttributes(final I_GetConceptData concept) throws IOException {
        return concept.getConceptAttributes() != null && concept.getConceptAttributes().versionCount() > 0;
    }

    private void processExtensions(int id) throws Exception {
        for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(id)) {
            processExtensionByReference(extension);
        }
    }

    private void processConceptAttributes(I_ConceptAttributeVersioned conceptAttributeVersioned) throws Exception {
        if (++conceptAttributeCount % 1000 == 0) {
            logger.info("processed concept attribute " + conceptAttributeCount);
        }

        boolean datachanged = false;

        datachanged = processEntity(conceptAttributeVersioned, conceptAttributeVersioned.getTuples());

        if (datachanged) {
            directInterface.writeConceptAttributes(conceptAttributeVersioned);
        }
    }

    private <T extends I_AmTuple> boolean processEntity(I_AmTermComponent termComponent, List<T> allTuples)
            throws Exception {
        I_AmTuple latestPart = null;
        boolean datachanged = false;

        Collection<T> tuples;
        if (readLatestPartOnly) {
            tuples = new ArrayList<T>();
            tuples.add(getLatest(allTuples));
        } else {
            tuples = allTuples;
        }

        // are we copying all versions or just the latest state
        if (copyOnlyLatestState) {
            // just the latest state - find the latest exportable part
            for (T t : tuples) {
                if (isValidForCopy(t)) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                }
            }
            Map<Integer, T> latestInTargetPath = getLatestTuples(allTuples, targetPathId);
            // if a part was found to copy and we are either
            // - copying parts regardless of the target path state
            // - the target path contains a version of the entity already
            // - or the latest part is active
            // then copy the part
            if (latestPart != null
                && latestPart.getPathId() != targetPathId
                && (copyInactiveVersionsNotInTarget
                    || latestInTargetPath.containsKey(latestPart.getFixedPart().getNid()) || isActive(latestPart.getStatusId()))) {
                duplicateTuple(latestPart);
                datachanged = true;
            }
        } else {
            // straight copy
            for (T t : tuples) {
                if (isValidForCopy(t)) {
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

    private <T extends I_AmTuple> T getLatest(List<T> tuples) {
        T latest = null;
        for (T t : tuples) {
            if (latest == null || latest.getVersion() < t.getVersion()) {
                latest = t;
            }
        }
        return latest;
    }

    private void processDescription(List<I_DescriptionVersioned> descriptions) throws Exception {
        for (I_DescriptionVersioned descriptionVersioned : descriptions) {
            processDescription(descriptionVersioned);
            processId(tf.getId(descriptionVersioned.getDescId()));
            processExtensions(descriptionVersioned.getDescId());
        }
    }

    private void processDescription(I_DescriptionVersioned descriptionVersioned) throws Exception {

        if (++descriptionCount % 1000 == 0) {
            logger.info("processed description " + descriptionCount);
        }

        Map<TupleKey, List<I_DescriptionTuple>> versionsMap = new HashMap<TupleKey, List<I_DescriptionTuple>>();

        // Check for multi-versioned descriptions, with the same path and
        // timestamp
        for (I_DescriptionTuple tuple : descriptionVersioned.getTuples(true)) {

            /**
             * add each tuple to a hashmap with a composite key of
             * <tupletype>id, pathid & timestamp.
             * If the current tuple exists in the map with these ids and time
             * then we have a
             * multi-versioned commit (bad). Store a copy of each of these so
             * they can be logged,
             * after iterating each version of this description/relationship,
             * and then fail the build once all
             * description/relationship have been copied (so we get to log all
             * bad data)
             */
            if (isValidForCopy(tuple)) {

                TupleKey key = new TupleKey(tuple.getDescId(), tuple.getPathId(), tuple.getVersion());
                List<I_DescriptionTuple> versions = new ArrayList<I_DescriptionTuple>();

                if (versionsMap.containsKey(key)) {

                    versions = versionsMap.get(key);

                    if (!duplicateDescTuples.contains(tuple)) {
                        duplicateDescTuples.add(tuple);
                    }
                }
                versions.add(tuple);
                versionsMap.put(key, versions);
            }
        }

        boolean datachanged = processEntity(descriptionVersioned, descriptionVersioned.getTuples(true));

        if (datachanged) {
            directInterface.writeDescription(descriptionVersioned);
        }
    }

    private void processExtensionByReference(I_ThinExtByRefVersioned extByRef) throws Exception {

        if (++extCount % 1000 == 0) {
            logger.info("processed extension " + extCount);
        }

        List<I_ThinExtByRefTuple> allTuples = extByRef.getTuples(null, null, true, false);

        boolean datachanged = processEntity(extByRef, allTuples);

        if (datachanged) {
            directInterface.writeExt(extByRef);
        }
    }

    private <T extends I_AmTuple> void duplicateTuple(T latestPart) throws Exception {
        I_AmPart newPart = latestPart.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        I_AmTermComponent termComponent = latestPart.getFixedPart();

        Class<? extends I_AmPart> partClass;
        if (newPart instanceof I_ThinExtByRefPart) {
            partClass = I_ThinExtByRefPart.class;
        } else {
            partClass = newPart.getClass();
        }
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

    private void processId(I_IdVersioned idVersioned) throws Exception {

        if (++idCount % 1000 == 0) {
            logger.info("processed id " + idCount);
        }

        boolean datachanged = processEntity(idVersioned, idVersioned.getTuples());

        if (datachanged) {
            directInterface.writeId(idVersioned);
        }
    }

    private void processImages(List<I_ImageVersioned> images) throws Exception {
        for (I_ImageVersioned imageVersioned : images) {
            processImages(imageVersioned);

            processId(tf.getId(imageVersioned.getNid()));
            processExtensions(imageVersioned.getNid());
        }
    }

    private void processImages(I_ImageVersioned imageVersioned) throws Exception {

        if (++imageCount % 1000 == 0) {
            logger.info("processed image " + imageCount);
        }

        boolean datachanged = processEntity(imageVersioned, imageVersioned.getTuples());

        if (datachanged) {
            directInterface.writeImage(imageVersioned);
        }
    }

    private void processRelationship(List<I_RelVersioned> sourceRels) throws Exception {
        for (I_RelVersioned relVersioned : sourceRels) {
            processRelationship(relVersioned);
            processId(tf.getId(relVersioned.getNid()));
            processExtensions(relVersioned.getNid());
        }
    }

    private void processRelationship(I_RelVersioned relVersioned) throws Exception {

        if (++relCount % 1000 == 0) {
            logger.info("processed relationship " + relCount);
        }

        Map<TupleKey, List<I_RelTuple>> versionsMap = new HashMap<TupleKey, List<I_RelTuple>>();

        // Check for multi-versioned relationships, with the same path and
        // timestamp
        for (I_RelTuple tuple : relVersioned.getTuples(true)) {

            /**
             * add each tuple to a hashmap with a composite key of
             * <tupletype>id, pathid & timestamp.
             * If the current tuple exists in the map with these ids and time
             * then we have a
             * multi-versioned commit (bad). Store a copy of each of these so
             * they can be logged,
             * after iterating each version of this description/relationship,
             * and then fail the build once all
             * description/relationship have been copied (so we get to log all
             * bad data)
             */

            TupleKey key = new TupleKey(tuple.getRelId(), tuple.getPathId(), tuple.getVersion());
            List<I_RelTuple> versions = new ArrayList<I_RelTuple>();

            if (versionsMap.containsKey(key)) {
                versions = versionsMap.get(key);
            }
            versions.add(tuple);
            versionsMap.put(key, versions);
            duplicateRelTuples.put(key, versions);
        }

        boolean datachanged = processEntity(relVersioned, relVersioned.getTuples(true));

        if (datachanged) {
            directInterface.writeRel(relVersioned);
        }
    }

    private <T extends I_AmTuple> Collection<T> getLatestTuples(List<T> tuples) {
        return getLatestTuples(tuples, null).values();
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

    private boolean isValidForCopy(I_AmPart part) {
        for (I_Position srcPosition : this.sourcePositions) {
            if (srcPosition.getPath().getConceptId() == part.getPathId()) {
                if (srcPosition.getVersion() >= part.getVersion()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCopyOnlyLatestState(boolean copyOnlyLatestState) {
        this.copyOnlyLatestState = copyOnlyLatestState;
    }

    public void setReadLatestPartOnly(boolean readLatestPartOnly) {
        this.readLatestPartOnly = readLatestPartOnly;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public void setTargetPathId(int targetPathId) {
        this.targetPathId = targetPathId;
    }

    public void setVersionTime(int versionTime) {
        this.versionTime = versionTime;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public boolean hasDuplicateVersionDescriptions() {
        return duplicateVersionDescriptions;
    }

    public boolean hasDuplicateVersionRelationships() {
        return duplicateVersionRelationships;
    }

    public void setSourcePositions(Collection<? extends I_Position> srcPositions) {
        this.sourcePositions.addAll(srcPositions);
    }

    public void addSourcePosition(I_Position position) {
        this.sourcePositions.add(position);
    }
}
