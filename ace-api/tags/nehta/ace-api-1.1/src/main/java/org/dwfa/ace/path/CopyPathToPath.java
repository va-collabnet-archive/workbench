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
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ImagePart;
import org.dwfa.ace.api.I_ImageTuple;
import org.dwfa.ace.api.I_ImageVersioned;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_WriteDirectToDb;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TupleKey;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

import java.util.ArrayList;
import java.util.Collection;
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

    private List<I_RelTuple> duplicateRelTuples = new ArrayList<I_RelTuple>();

    private Logger logger = Logger.getLogger(CopyPathToPath.class.getName());

    public CopyPathToPath() {
        tf = LocalVersionedTerminology.get();
        directInterface = tf.getDirectInterface();
    }

    public void processConcept(I_GetConceptData concept) throws Exception {

        if (versionTime == Integer.MAX_VALUE) {
            setVersionTime(tf.convertToThinVersion(System.currentTimeMillis()));
        }

        if (++conceptCount % 1000 == 0) {
            logger.info("processed concept " + conceptCount);
        }

        duplicateDescTuples = new ArrayList<I_DescriptionTuple>();
        duplicateRelTuples = new ArrayList<I_RelTuple>();

        if (validate || (concept.getConceptAttributes() != null && concept.getConceptAttributes().versionCount() > 0)) {
            processConceptAttributes(concept.getConceptAttributes());
        }
        processDescription(concept.getDescriptions());
        for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(concept.getConceptId())) {
            processExtensionByReference(extension);
        }
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
        if (!duplicateRelTuples.isEmpty()) {
            duplicateVersionRelationships = Boolean.TRUE;
            logger.severe("***** Duplicate-versioned relationship tuple(s) in concept: " + concept.getUids().get(0));
            for (I_RelTuple tuple : duplicateRelTuples) {
                logger.severe("Rel id: " + tuple.getRelId() + ", Path id: " + tuple.getPathId() + ", Version: "
                    + tuple.getVersion() + "\n\t(" + tuple.getTypeId() + ")");
            }
        }
    }

    private void processConceptAttributes(I_ConceptAttributeVersioned conceptAttributeVersioned) throws Exception {

        if (++conceptAttributeCount % 1000 == 0) {
            logger.info("processed concept attribute " + conceptAttributeCount);
        }

        boolean datachanged = false;
        I_ConceptAttributeTuple latestPart = null;

        Collection<I_ConceptAttributeTuple> conceptAttributes;
        if (readLatestPartOnly) {
            conceptAttributes = new ArrayList<I_ConceptAttributeTuple>();
            conceptAttributes.add(getLatestAttributes(conceptAttributeVersioned.getTuples()));
        } else {
            conceptAttributes = conceptAttributeVersioned.getTuples();
        }

        for (I_ConceptAttributeTuple t : conceptAttributes) {
            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateConceptAttributeTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateConceptAttributeTuple(latestPart);
            datachanged = true;
        }
        if (datachanged) {
            directInterface.writeConceptAttributes(conceptAttributeVersioned);
        }
    }

    private I_ConceptAttributeTuple getLatestAttributes(List<I_ConceptAttributeTuple> tuples) {
        I_ConceptAttributeTuple latest = null;
        for (I_ConceptAttributeTuple conceptAttributeTuple : tuples) {
            if (latest == null || latest.getVersion() < conceptAttributeTuple.getVersion()) {
                latest = conceptAttributeTuple;
            }
        }
        return latest;
    }

    private void duplicateConceptAttributeTuple(I_ConceptAttributeTuple latestPart) {
        I_ConceptAttributePart newPart = latestPart.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        latestPart.getConVersioned().addVersion(newPart);
    }

    private void processDescription(List<I_DescriptionVersioned> descriptions) throws Exception {
        for (I_DescriptionVersioned descriptionVersioned : descriptions) {
            processDescription(descriptionVersioned);
            for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(descriptionVersioned.getDescId())) {
                processExtensionByReference(extension);
            }
        }
    }

    private void processDescription(I_DescriptionVersioned descriptionVersioned) throws Exception {

        if (++descriptionCount % 1000 == 0) {
            logger.info("processed description " + descriptionCount);
        }

        Collection<I_DescriptionTuple> allTuples = descriptionVersioned.getTuples(true);

        Map<TupleKey, List<I_DescriptionTuple>> versionsMap = new HashMap<TupleKey, List<I_DescriptionTuple>>();

        // Check for multi-versioned descriptions, with the same path and
        // timestamp
        for (I_DescriptionTuple tuple : allTuples) {

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

        Collection<I_DescriptionTuple> descriptions;

        if (readLatestPartOnly) {
            descriptions = getLatestDescriptions((List<I_DescriptionTuple>) allTuples);
        } else {
            descriptions = allTuples;
        }

        boolean datachanged = false;
        I_DescriptionTuple latestPart = null;
        for (I_DescriptionTuple t : descriptions) {

            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateDescriptionTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateDescriptionTuple(latestPart);
            datachanged = true;
        }

        if (datachanged) {
            directInterface.writeDescription(descriptionVersioned);
        }
    }

    private Collection<I_DescriptionTuple> getLatestDescriptions(List<I_DescriptionTuple> tuples) {
        Map<Integer, I_DescriptionTuple> map = new HashMap<Integer, I_DescriptionTuple>();
        for (I_DescriptionTuple description : tuples) {
            if (map.containsKey(description.getDescId())) {
                if (map.get(description.getDescId()).getVersion() < description.getVersion()) {
                    map.put(description.getDescId(), description);
                }
            } else {
                map.put(description.getDescId(), description);
            }
        }
        return map.values();
    }

    private void duplicateDescriptionTuple(I_DescriptionTuple t) {
        I_DescriptionPart newPart = t.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getDescVersioned().addVersion(newPart);
    }

    private void processExtensionByReference(I_ThinExtByRefVersioned extByRef) throws Exception {

        if (++extCount % 1000 == 0) {
            logger.info("processed extension " + extCount);
        }

        Collection<I_ThinExtByRefTuple> extensions;
        if (readLatestPartOnly) {
            extensions = getLatestExtensions(extByRef.getTuples(null, null, true, false));
        } else {
            extensions = extByRef.getTuples(null, null, true, false);
        }

        boolean datachanged = false;
        I_ThinExtByRefTuple latestPart = null;
        for (I_ThinExtByRefTuple t : extensions) {
            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateExtensionTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateExtensionTuple(latestPart);
            datachanged = true;
        }

        if (datachanged) {
            directInterface.writeExt(extByRef);
        }
    }

    private Collection<I_ThinExtByRefTuple> getLatestExtensions(List<I_ThinExtByRefTuple> tuples) {
        Map<Integer, I_ThinExtByRefTuple> map = new HashMap<Integer, I_ThinExtByRefTuple>();
        for (I_ThinExtByRefTuple extension : tuples) {
            if (map.containsKey(extension.getMemberId())) {
                if (map.get(extension.getMemberId()).getVersion() < extension.getVersion()) {
                    map.put(extension.getMemberId(), extension);
                }
            } else {
                map.put(extension.getMemberId(), extension);
            }
        }
        return map.values();
    }

    private void duplicateExtensionTuple(I_ThinExtByRefTuple t) {
        I_ThinExtByRefPart newPart = t.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getCore().addVersion(newPart);
    }

    private void processId(I_IdVersioned idVersioned) throws Exception {

        if (++idCount % 1000 == 0) {
            logger.info("processed id " + idCount);
        }

        Collection<I_IdTuple> ids;
        if (readLatestPartOnly) {
            ids = getLatest(idVersioned.getTuples());
        } else {
            ids = idVersioned.getTuples();
        }

        boolean datachanged = false;
        I_IdTuple latestPart = null;
        for (I_IdTuple t : ids) {

            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateIdTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateIdTuple(latestPart);
            datachanged = true;
        }

        if (datachanged) {
            directInterface.writeId(idVersioned);
        }
    }

    private Collection<I_IdTuple> getLatest(List<I_IdTuple> tuples) {
        Map<Integer, I_IdTuple> map = new HashMap<Integer, I_IdTuple>();
        for (I_IdTuple ids : tuples) {
            if (map.containsKey(ids.getNativeId())) {
                if (map.get(ids.getNativeId()).getVersion() < ids.getVersion()) {
                    map.put(ids.getNativeId(), ids);
                }
            } else {
                map.put(ids.getNativeId(), ids);
            }
        }
        return map.values();
    }

    private void duplicateIdTuple(I_IdTuple t) {
        I_IdPart newPart = t.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getIdVersioned().addVersion(newPart);
    }

    private void processImages(List<I_ImageVersioned> images) throws Exception {
        for (I_ImageVersioned imageVersioned : images) {
            processImages(imageVersioned);
        }
    }

    private void processImages(I_ImageVersioned imageVersioned) throws Exception {

        if (++imageCount % 1000 == 0) {
            logger.info("processed image " + imageCount);
        }

        Collection<I_ImageTuple> images;
        if (readLatestPartOnly) {
            images = getLatest(imageVersioned.getTuples());
        } else {
            images = imageVersioned.getTuples();
        }

        boolean datachanged = false;
        I_ImageTuple latestPart = null;
        for (I_ImageTuple t : images) {

            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateImageTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateImageTuple(latestPart);
            datachanged = true;
        }

        if (datachanged) {
            directInterface.writeImage(imageVersioned);
        }
    }

    private Collection<I_ImageTuple> getLatest(Collection<I_ImageTuple> tuples) {
        Map<Integer, I_ImageTuple> map = new HashMap<Integer, I_ImageTuple>();
        for (I_ImageTuple image : tuples) {
            if (map.containsKey(image.getImageId())) {
                if (map.get(image.getImageId()).getVersion() < image.getVersion()) {
                    map.put(image.getImageId(), image);
                }
            } else {
                map.put(image.getImageId(), image);
            }
        }
        return map.values();
    }

    private void duplicateImageTuple(I_ImageTuple t) {
        I_ImagePart newPart = t.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getVersioned().addVersion(newPart);
    }

    private void processRelationship(List<I_RelVersioned> sourceRels) throws Exception {
        for (I_RelVersioned relVersioned : sourceRels) {
            processRelationship(relVersioned);
            for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(relVersioned.getRelId())) {
                processExtensionByReference(extension);
            }
        }
    }

    private void processRelationship(I_RelVersioned relVersioned) throws Exception {

        if (++relCount % 1000 == 0) {
            logger.info("processed relationship " + relCount);
        }

        Collection<I_RelTuple> allTuples = relVersioned.getTuples();
        Map<TupleKey, List<I_RelTuple>> versionsMap = new HashMap<TupleKey, List<I_RelTuple>>();

        // Check for multi-versioned relationships, with the same path and
        // timestamp
        for (I_RelTuple tuple : allTuples) {

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

                if (!duplicateRelTuples.contains(tuple)) {
                    duplicateRelTuples.add(tuple);
                }
            }
            versions.add(tuple);
            versionsMap.put(key, versions);
        }

        Collection<I_RelTuple> rels;
        if (readLatestPartOnly) {
            rels = getLatestRelationships((List<I_RelTuple>) allTuples);
        } else {
            rels = allTuples;
        }

        boolean datachanged = false;
        I_RelTuple latestPart = null;
        for (I_RelTuple t : rels) {

            if (isValidForCopy(t)) {
                if (copyOnlyLatestState) {
                    if (latestPart == null || t.getVersion() > latestPart.getVersion()) {
                        latestPart = t;
                    }
                } else {
                    duplicateRelationshipTuple(t);
                    datachanged = true;
                }
            }
        }
        if (copyOnlyLatestState && latestPart != null) {
            duplicateRelationshipTuple(latestPart);
            datachanged = true;
        }

        if (datachanged) {
            directInterface.writeRel(relVersioned);
        }
    }

    private Collection<I_RelTuple> getLatestRelationships(List<I_RelTuple> tuples) {
        Map<Integer, I_RelTuple> map = new HashMap<Integer, I_RelTuple>();
        for (I_RelTuple rel : tuples) {
            if (map.containsKey(rel.getRelId())) {
                if (map.get(rel.getRelId()).getVersion() < rel.getVersion()) {
                    map.put(rel.getRelId(), rel);
                }
            } else {
                map.put(rel.getRelId(), rel);
            }
        }
        return map.values();
    }

    private void duplicateRelationshipTuple(I_RelTuple t) {
        I_RelPart newPart = t.duplicate();
        newPart.setPathId(targetPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getRelVersioned().addVersion(newPart);
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
