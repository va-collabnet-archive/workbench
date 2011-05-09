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
import org.dwfa.ace.api.*;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.cement.ArchitectonicAuxiliary;

import java.io.IOException;
import java.util.*;

/**
 * Copies from all the specified paths and their children to the new path. Note that this
 * mojo will only copy content that is explicitly on the origin paths, not inherited from
 * a parent path.
 *
 * @goal copy-from-path-to-path
 */
public class CopyFromPathToPath extends AbstractMojo implements I_ProcessConcepts {

    /**
     * Paths to copy the data from
     *
     * @parameter
     * @required
     */
    ConceptDescriptor[] fromPaths;

    /**
     * Path to copy the data to
     *
     * @parameter
     * @required
     */
    ConceptDescriptor toPath;

    /**
     * This status will be used to change all content to if set, otherwise the status of the
     * components on the origin path will be used
     *
     * @parameter
     */
    ConceptDescriptor status = null;

    /**
     * The release time to stamp all copies with, otherwise NOW will be used
     *
     * @parameter
     */
    Date releaseTime = null;

    /**
     * Indicate if all history or only the latest state of the objects should be copied - defaults to false
     *
     * @parameter
     */
    boolean copyOnlyLatestState = false;

    /**
     * Indicates whether to read all parts of the object and copy any found, or only the very latest part in time sequence across all paths
     *
     * @parameter
     */
    boolean readLatestPartOnly = false;

    /**
     * Indicates whether to include child paths
     *
     * @parameter
     */
    boolean includeChildPaths = false;

    /**
     * Indicates whether to include the SNOMED Is A
     *
     * @parameter
     */
    boolean includeSnomedIsA = true;

    /**
     * Indicates whether to require a a concept to have attributes
     *
     * @parameter
     */
    boolean validate = true;

    private I_IntSet fromPathIds;
    private int toPathId;
    private int versionTime;
    private int statusId = 0;
    private I_TermFactory tf;

    private I_WriteDirectToDb directInterface;

    private int conceptAttributeCount;

    private int descriptionCount;

    private int extCount;

    private int idCount;

    private int imageCount;

    private int relCount;

    private int conceptCount;

    private boolean duplicateVersionDescriptions = Boolean.FALSE;
    private boolean duplicateVersionRelationships = Boolean.FALSE;

    List<I_DescriptionTuple> duplicateDescTuples = new ArrayList<I_DescriptionTuple>();
    List<I_RelTuple> duplicateRelTuples = new ArrayList<I_RelTuple>();

    public void execute() throws MojoExecutionException, MojoFailureException {
        tf = LocalVersionedTerminology.get();
        try {
            List<I_GetConceptData> fromPathConcepts = new ArrayList<I_GetConceptData>();
            for (ConceptDescriptor fromPath : fromPaths) {
                addToFromPaths(fromPathConcepts, fromPath.getVerifiedConcept());
            }

            fromPathIds = tf.newIntSet();
            getFromPathIds(fromPathConcepts);

            toPathId = toPath.getVerifiedConcept().getConceptId();

            if (status != null) {
                statusId = status.getVerifiedConcept().getConceptId();
            }

            if (releaseTime != null) {
                versionTime = tf.convertToThinVersion(releaseTime.getTime());
            } else {
                versionTime = tf.convertToThinVersion(System.currentTimeMillis());
            }

            directInterface = tf.getDirectInterface();

            getLog().info("Starting to iterate concept attributes to copy from " + fromPaths + " to " + toPath);
            tf.iterateConcepts(this);

            String duplicateVersionError = "";
            if (duplicateVersionDescriptions) {
                duplicateVersionError += "One or more descriptions were found with multiple versions on the same path and with the same timestamp. ";
            }
            if (duplicateVersionRelationships) {
                duplicateVersionError += "One or more relationships were found with multiple versions on the same path and with the same timestamp. ";
            }
            if (duplicateVersionDescriptions ||
                    duplicateVersionRelationships) {
                throw new MojoExecutionException(duplicateVersionError);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("failed copying from paths "
                    + fromPaths + " to path " + toPath, e);
        }

    }

    private void addToFromPaths(List<I_GetConceptData> fromPathConcepts, I_GetConceptData verifiedConcept) throws IOException {
        fromPathConcepts.add(verifiedConcept);

        Set<I_GetConceptData> children = verifiedConcept.getDestRelOrigins(null, null, null, false);
        if (includeChildPaths) {
            for (I_GetConceptData concept : children) {
                addToFromPaths(fromPathConcepts, concept);
            }
        }
    }

    public void processConcept(I_GetConceptData arg0) throws Exception {
        if (++conceptCount % 1000 == 0) {
            getLog().info("processed concept " + conceptCount);
        }

        duplicateDescTuples = new ArrayList<I_DescriptionTuple>();
        duplicateRelTuples = new ArrayList<I_RelTuple>();

        if (validate ||
                (arg0.getConceptAttributes() != null &&
                arg0.getConceptAttributes().versionCount() > 0)) {
            processConceptAttributes(arg0.getConceptAttributes());
        }
        processDescription(arg0.getDescriptions());
        for (I_ThinExtByRefVersioned extension : tf.getAllExtensionsForComponent(arg0.getConceptId())) {
            processExtensionByReference(extension);
        }
        processId(arg0.getId());
        processImages(arg0.getImages());
        processRelationship(arg0.getSourceRels());

        // Log all Duplicate-versioned descriptions and relationships on the same path and timestamp here
        if (!duplicateDescTuples.isEmpty()) {
            duplicateVersionDescriptions = Boolean.TRUE;
            getLog().error("***** Duplicate-versioned description tuple(s) in concept: " + arg0.getUids().get(0));
            for (I_DescriptionTuple tuple : duplicateDescTuples) {
                getLog().error("Desc id: " + tuple.getDescId() + ", Path id: " + tuple.getPathId() + ", Version: " + tuple.getVersion() + "\n\t(" + tuple.getText() + ")");
            }
        }
        if (!duplicateRelTuples.isEmpty()) {
            duplicateVersionRelationships = Boolean.TRUE;
            getLog().error("***** Duplicate-versioned relationship tuple(s) in concept: " + arg0.getUids().get(0));
            for (I_RelTuple tuple : duplicateRelTuples) {
                getLog().error("Rel id: " + tuple.getRelId() + ", Path id: " + tuple.getPathId() + ", Version: " + tuple.getVersion() + "\n\t(" + tuple.getRelTypeId() + ")");
            }
        }
    }

    private void getFromPathIds(Collection<I_GetConceptData> pathDescriptors) throws Exception {
        for (I_GetConceptData fromPath : pathDescriptors) {
            fromPathIds.add(fromPath.getConceptId());

            I_IntSet isAIntSet = tf.newIntSet();
            if (includeSnomedIsA) {
                isAIntSet.add(ConceptConstants.SNOMED_IS_A.localize().getNid());
            }
            isAIntSet.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
            getFromPathIds(fromPath.getSourceRelTargets(null, isAIntSet, null, true));
        }
    }

    public void processConceptAttributes(
            I_ConceptAttributeVersioned conceptAttributeVersioned)
            throws Exception {

        if (++conceptAttributeCount % 1000 == 0) {
            getLog().info("processed concept attribute " + conceptAttributeCount);
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
            if (fromPathIds.contains(t.getPathId())) {
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

    private I_ConceptAttributeTuple getLatestAttributes(
            List<I_ConceptAttributeTuple> tuples) {
        I_ConceptAttributeTuple latest = null;
        for (I_ConceptAttributeTuple conceptAttributeTuple : tuples) {
            if (latest == null || latest.getVersion() < conceptAttributeTuple.getVersion()) {
                latest = conceptAttributeTuple;
            }
        }
        return latest;
    }

    private void duplicateConceptAttributeTuple(
            I_ConceptAttributeTuple latestPart) {
        I_ConceptAttributePart newPart = latestPart.duplicatePart();
        newPart.setPathId(toPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setConceptStatus(statusId);
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

    public void processDescription(I_DescriptionVersioned descriptionVersioned)
            throws Exception {

        if (++descriptionCount % 1000 == 0) {
            getLog().info("processed description " + descriptionCount);
        }

        Collection<I_DescriptionTuple> allTuples = descriptionVersioned.getTuples();
        Map<TupleKey, List<I_DescriptionTuple>> versionsMap =
                new HashMap<TupleKey, List<I_DescriptionTuple>>();

        // Check for multi-versioned descriptions, with the same path and timestamp
        for (I_DescriptionTuple tuple : allTuples) {

            /**
             * add each tuple to a hashmap with a composite key of <tupletype>id, pathid & timestamp.
             * If the current tuple exists in the map with these ids and time then we have a
             * multi-versioned commit (bad). Store a copy of each of these so they can be logged,
             * after iterating each version of this description/relationship, and then fail the build once all
             * description/relationship have been copied (so we get to log all bad data)
             */

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

        Collection<I_DescriptionTuple> descriptions;

        if (readLatestPartOnly) {
            descriptions = getLatestDescriptions((List<I_DescriptionTuple>) allTuples);
        } else {
            descriptions = allTuples;
        }

        boolean datachanged = false;
        I_DescriptionTuple latestPart = null;
        for (I_DescriptionTuple t : descriptions) {

            if (fromPathIds.contains(t.getPathId())) {
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
        I_DescriptionPart newPart = t.duplicatePart();
        newPart.setPathId(toPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatusId(statusId);
        }
        t.getDescVersioned().addVersion(newPart);
    }

    public void processExtensionByReference(I_ThinExtByRefVersioned extByRef)
            throws Exception {

        if (++extCount % 1000 == 0) {
            getLog().info("processed extension " + extCount);
        }

        Collection<I_ThinExtByRefTuple> extensions;
        if (readLatestPartOnly) {
            extensions = getLatestExtensions(extByRef.getTuples(null, null, true));
        } else {
            extensions = extByRef.getTuples(null, null, true);
        }

        boolean datachanged = false;
        I_ThinExtByRefTuple latestPart = null;
        for (I_ThinExtByRefTuple t : extensions) {
            if (fromPathIds.contains(t.getPathId())) {
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
        I_ThinExtByRefPart newPart = t.duplicatePart();
        newPart.setPathId(toPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setStatus(statusId);
        }
        t.getCore().addVersion(newPart);
    }

    public void processId(I_IdVersioned idVersioned) throws Exception {

        if (++idCount % 1000 == 0) {
            getLog().info("processed id " + idCount);
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

            if (fromPathIds.contains(t.getPathId())) {
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
        I_IdPart newPart = t.duplicatePart();
        newPart.setPathId(toPathId);
        newPart.setVersion(versionTime);
        if (statusId != 0) {
            newPart.setIdStatus(statusId);
        }
        t.getIdVersioned().addVersion(newPart);
    }

    private void processImages(List<I_ImageVersioned> images) throws Exception {
        for (I_ImageVersioned imageVersioned : images) {
            processImages(imageVersioned);
        }
    }

    public void processImages(I_ImageVersioned imageVersioned) throws Exception {

        if (++imageCount % 1000 == 0) {
            getLog().info("processed image " + imageCount);
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

            if (fromPathIds.contains(t.getPathId())) {
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
        I_ImagePart newPart = t.duplicatePart();
        newPart.setPathId(toPathId);
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

    public void processRelationship(I_RelVersioned relVersioned)
            throws Exception {

        if (++relCount % 1000 == 0) {
            getLog().info("processed relationship " + relCount);
        }

        Collection<I_RelTuple> allTuples = relVersioned.getTuples();
        Map<TupleKey, List<I_RelTuple>> versionsMap =
                new HashMap<TupleKey, List<I_RelTuple>>();

        // Check for multi-versioned relationships, with the same path and timestamp
        for (I_RelTuple tuple : allTuples) {

            /**
             * add each tuple to a hashmap with a composite key of <tupletype>id, pathid & timestamp.
             * If the current tuple exists in the map with these ids and time then we have a
             * multi-versioned commit (bad). Store a copy of each of these so they can be logged,
             * after iterating each version of this description/relationship, and then fail the build once all
             * description/relationship have been copied (so we get to log all bad data)
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

            if (fromPathIds.contains(t.getPathId())) {
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
        I_RelPart newPart = t.duplicatePart();
        newPart.setPathId(toPathId);
        newPart.setVersion(versionTime);
		if (statusId != 0) {
			newPart.setStatusId(statusId);
		}
		t.getRelVersioned().addVersion(newPart);
	}

}
