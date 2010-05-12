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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.mojo.comparator.TupleComparator;
import org.dwfa.mojo.refset.ExportSpecification;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;

public class ExportIterator implements I_ProcessConcepts {

    private static final String DATE_FORMAT = "yyyy.mm.dd hh:mm:ss";

    private int totalConcepts = 0;

    private int conceptsMatched = 0;

    private int conceptsUnmatched = 0;

    private int conceptsSuppressed = 0;

    private int maxSuppressed = Integer.MAX_VALUE;

    private Writer errorWriter;

    private Set<I_Position> positions;

    private I_IntSet allowedStatus;

    private Writer conceptsWriter;
    private Writer descriptionsWriter;
    private Writer relationshipsWriter;
    private Writer idsWriter;

    private Log log;

    private String releaseDate;

    private boolean validatePositions = true;

    private ExportSpecification[] specs;

    private I_TermFactory termFactory;

    private I_IntList nameOrder;

    private final Writer idMapWriter;

    private Collection<UUID> snomedIdUuids;

    private I_GetConceptData activeConcept;

    private I_GetConceptData inactiveConcept;

    private HashMap<Integer, String> pathReleaseVersions = new HashMap<Integer, String>();

    /**
     * This attribute indicates if the concepts, descriptions, relationships and identifiers exported
     * i.e. matching the export specification, must be a cohesive self supporting set or not. If this
     * is set to true (and by default it is) then if a component that matches the export specification
     * depends upon a component that doesn't then it won't be exported. For example if a relationship
     * matches the export specification, however the refinability concept it refers to does not then the
     * relationship will not be exported.
     * If this attribute is set to false then this will be ignored, and the exported content may reference
     * identifiers of content that is not represented within the exported files.
     */
    private boolean exportCohesiveSet = true;

    public void setExportCohesiveSet(boolean exportCohesiveSet) {
        this.exportCohesiveSet = exportCohesiveSet;
    }

    public ExportIterator(Writer concepts, Writer descriptions, Writer relationships, Writer idsWriter, Writer idMapWriter,
                          Writer errorWriter, Set<I_Position> positions, I_IntSet allowedStatus, ExportSpecification[] specs,
                          Log log)
            throws IOException, TerminologyException {

        super();
        this.idsWriter = idsWriter;
        this.idMapWriter = idMapWriter;
        this.errorWriter = errorWriter;
        this.positions = positions;
        this.allowedStatus = allowedStatus;
        this.conceptsWriter = concepts;
        this.descriptionsWriter = descriptions;
        this.relationshipsWriter = relationships;
        this.log = log;
        this.specs = specs;
        this.termFactory = LocalVersionedTerminology.get();

        activeConcept = termFactory.getConcept(Concept.ACTIVE.getUids());
        inactiveConcept = termFactory.getConcept(Concept.INACTIVE.getUids());

        snomedIdUuids = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids();

        nameOrder = termFactory.newIntList();
        nameOrder.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
        nameOrder.add(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.localize().getNid());
        nameOrder.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
        nameOrder.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid());
    }

    public int getTotals() {
        return totalConcepts;
    }

    public int getmatched() {
        return conceptsMatched;
    }

    public int getUnmatched() {
        return conceptsUnmatched;
    }

    public void processConcept(I_GetConceptData concept) throws Exception {

        if (conceptsSuppressed > maxSuppressed) {
            return;
        }

        // I_IntSet allowedTypes = termFactory.newIntSet();
        // allowedTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

        totalConcepts++;

        if (totalConcepts % 1000 == 0) {
            log.info("Iterated " + totalConcepts);
        }

        try {
        if (isExportable(concept)) {
            /*
             * Get concept details
             */
            if (writeUuidBasedConceptDetails(concept, allowedStatus)) {
                writeUuidBasedRelDetails(concept, allowedStatus, null);
                writeUuidBasedDescriptionDetails(concept, allowedStatus, null);
                writeUuidBasedIdDetails(concept.getId(), allowedStatus, null);
            }
        } else {
            conceptsSuppressed++;
            log.info("Suppressing: " + concept);
        }
        } catch (Exception e) {
            throw new Exception("Exception caught processing concept " + concept.getUids() + " postions " + positions, e);
        }

    }// End method processConcept

    private void writeUuidBasedIdDetails(I_IdVersioned idVersioned, I_IntSet allowedStatus, Object object)
            throws TerminologyException, Exception {

        Object[] idTuples = idVersioned.getTuples().toArray();
        Arrays.sort(idTuples, new Comparator<Object>() {
            public int compare(Object o1, Object o2) {
                return ((I_IdTuple) o1).getVersion() - ((I_IdTuple) o2).getVersion();
            }
        });

        String uuidString = "";
        boolean firstRun = true;
        for (UUID uuid : idVersioned.getUIDs()) {
            if (!firstRun) {
                uuidString += "\t" + uuid;
            } else {
                firstRun = false;
                uuidString += uuid;
            }
        }

        String snomedId = "";

        for (Object obj : idTuples) {
            I_IdTuple tuple = (I_IdTuple) obj;
            I_IdPart part = tuple.getPart();
            I_IdVersioned id = tuple.getIdVersioned();
            if (allowedStatus.contains(part.getIdStatus())
                    && (!exportCohesiveSet || isExportable(ConceptBean.get(part.getSource())))
                    && (!validatePositions || validPosition(part.getPathId()))) {

                if (snomedSource(part) && !snomedId.equals(part.getSourceId())) {
                    snomedId = part.getSourceId().toString();
                    idMapWriter.write(uuidString);
                    idMapWriter.write(System.getProperty("line.separator"));
                    idMapWriter.write(snomedId);
                    idMapWriter.write(System.getProperty("line.separator"));
                }

                StringBuilder stringBuilder = new StringBuilder();
                // primary UUID
                createRecord(stringBuilder, id.getUIDs().iterator().next());

                // source system UUID
                createRecord(stringBuilder, getFirstUuid(part.getSource()));

                // source id
                createRecord(stringBuilder, part.getSourceId());

                // status UUID
                createRecord(stringBuilder, getBinaryStatusValue(part.getIdStatus()));

                // Effective time
                createVersion(stringBuilder, part.getVersion(), part.getPathId());

                //Path Id
                createRecord(stringBuilder, getFirstUuid(part.getPathId()));

                createRecord(stringBuilder, System.getProperty("line.separator"));

                idsWriter.write(stringBuilder.toString());
            }
        }
    }

    private boolean snomedSource(I_IdPart idvPart) throws TerminologyException, IOException {
        if (termFactory.hasConcept(idvPart.getSource())) {
            for (UUID uuid : termFactory.getUids(idvPart.getSource())) {
                if (snomedIdUuids.contains(uuid)) {
                    return true;
                }
            }
        } else {
            System.out.println("no concept for source, id was " + idvPart.getSourceId());
        }
        return false;
    }

    private boolean isExportable(I_GetConceptData concept) throws Exception {
        for (ExportSpecification spec : specs) {
            if (spec.test(concept)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        return "prepareConceptData";
    }

    private boolean writeUuidBasedConceptDetails(I_GetConceptData concept, I_IntSet allowedStatus)
            throws Exception {

        I_IntSet fsn = termFactory.newIntSet();
        fsn.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
        List<I_DescriptionTuple> descTuples = concept.getDescriptionTuples(null, fsn, positions);
        List<I_DescriptionTuple> latestDescTuples = getLatestTuples(descTuples);

        I_DescriptionTuple descForConceptFile = null;

        if (latestDescTuples != null) {

            // Get the latest, 'current', FSN
            for (I_DescriptionTuple descTuple : latestDescTuples) {
                if (isDescriptionStatusCurrent(descTuple.getStatusId())) {
                    if (descForConceptFile == null ||
                            descForConceptFile.getVersion() < descTuple.getVersion()) {
                        descForConceptFile = descTuple;
                    }
                }
            }

            // If no 'current' FSN is available, get the latest FSN (regardless of status)
            if (descForConceptFile == null) {
                for (I_DescriptionTuple descTuple : latestDescTuples) {
                    if (descForConceptFile == null ||
                            descForConceptFile.getVersion() < descTuple.getVersion()) {
                        descForConceptFile = descTuple;
                    }
                }
            }
        }

        if (descForConceptFile == null) {
            errorWriter.append("\n\nnull desc for: " + concept.getUids() + " " + concept.getDescriptions());
            return false;
        } else {

            StringBuilder stringBuilder = new StringBuilder("");

            List<I_ConceptAttributeTuple> firstMatches = concept.getConceptAttributeTuples(null, positions);
            List<I_ConceptAttributeTuple> matches = new LinkedList<I_ConceptAttributeTuple>();
            for (int i = 0; i < firstMatches.size(); i++) {
                if (allowedStatus.contains(firstMatches.get(i).getConceptStatus())) {
                    matches.add(firstMatches.get(i));
                }
            }

            if (matches == null || matches.size() == 0) {
                return false;
            }

            conceptsMatched++;

            I_ConceptAttributeTuple latestAttrib = null;
            for (I_ConceptAttributeTuple attribTup : matches) {
                if ((!validatePositions || validPosition(attribTup.getPathId()))
                        && (latestAttrib == null || attribTup.getVersion() >= latestAttrib.getVersion())) {
                    latestAttrib = attribTup;
                }
            }

            if (latestAttrib == null) {
                return false;
            }

            // ConceptId
            createRecord(stringBuilder, concept.getUids().get(0));

            // Concept status
            createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedConceptStatusId(
                    termFactory.getUids(latestAttrib.getConceptStatus())));

            // Fully specified name
            createRecord(stringBuilder, descForConceptFile.getText());

            // CTV3ID
            createRecord(stringBuilder, "null");

            // SNOMED 3 ID... We ignore this for now.
            createRecord(stringBuilder, "null");

            // IsPrimative value
            createRecord(stringBuilder, latestAttrib.isDefined() ? 0 : 1);

            // AMT added
            // Concept UUID
            createRecord(stringBuilder, concept.getUids().get(0));

            // ConceptStatusId
            createRecord(stringBuilder, getFirstUuid(latestAttrib.getConceptStatus()));

            // Effective time
            createVersion(stringBuilder, latestAttrib.getPart().getVersion(), latestAttrib.getPart().getPathId());

            //Path Id
            createRecord(stringBuilder, getFirstUuid(latestAttrib.getPart().getPathId()));

            //Status active/inactive value
            createRecord(stringBuilder, getBinaryStatusValue(latestAttrib.getPart().getStatusId()));

            // End record
            createRecord(stringBuilder, System.getProperty("line.separator"));

            conceptsWriter.write(stringBuilder.toString());

            return true;
        }// End method getUuidBasedConceptDetaiils
    }

    private List<I_DescriptionTuple> getLatestTuples(List<I_DescriptionTuple> descTuples) {

        // Sort and reverse the tuples so that the list is ordered by the latest description and then the latest version
        Collections.sort(descTuples, new TupleComparator());
        Collections.reverse(descTuples);

        List<I_DescriptionTuple> latestTuples = new ArrayList<I_DescriptionTuple>();
        I_DescriptionTuple previousTuple = null;

        for (I_DescriptionTuple tuple : descTuples) {

            // If this is the first loop, or this is a new description version
            if (previousTuple == null ||
                    previousTuple.getDescId() != tuple.getDescId()) {

                latestTuples.add(tuple);
                previousTuple = tuple;
            }
        }
        return latestTuples;
    }

    private boolean isDescriptionStatusCurrent(int statusId) throws IOException, TerminologyException {

        // If statusId matches a 'current'/'active' description status
        if (statusId == termFactory.getConcept(ArchitectonicAuxiliary.Concept.ACTIVE.getUids()).getConceptId() ||
            statusId == termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()).getConceptId() ||
            statusId == termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptId() ||
            statusId == termFactory.getConcept(ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE.getUids()).getConceptId()) {
            return Boolean.TRUE;
        }
        return false;
    }

    private boolean validPosition(int pathId) {
        for (I_Position position : positions) {
            if (position.getPath().getConceptId() == pathId) {
                return true;
            }
        }
        return false;
    }

    private void writeUuidBasedRelDetails(I_GetConceptData concept, I_IntSet allowedStatus, I_IntSet allowedTypes)
            throws Exception {

        List<I_RelTuple> tuples = concept.getSourceRelTuples(null, null,positions, false);

        HashMap<Integer, I_RelTuple> latestRel = new HashMap<Integer, I_RelTuple>();
        for (I_RelTuple tuple : tuples) {

            if (validatePositions && !validPosition(tuple.getPathId())) {
                continue;
            }

            if (!latestRel.containsKey(tuple.getRelId())) {
                latestRel.put(tuple.getRelId(), tuple);
            } else {
                I_RelTuple latestTuple = latestRel.get(tuple.getRelId());
                if (tuple.getVersion() >= latestTuple.getVersion()) {
                    latestRel.put(tuple.getRelId(), tuple);
                }
            }
        }

        int relId = 0;
        for (I_RelTuple tuple : latestRel.values()) {
            I_RelPart part = tuple.getPart();
            I_RelVersioned rel = tuple.getRelVersioned();
            if (allowedStatus.contains(part.getStatusId()) && isExportable(ConceptBean.get(rel.getC2Id()))
                    && (!exportCohesiveSet ||
                        (
                            isExportable(ConceptBean.get(part.getCharacteristicId()))
                            && isExportable(ConceptBean.get(part.getRefinabilityId()))
                            && isExportable(ConceptBean.get(part.getRelTypeId()))
                        )
                    )
                )
                {

                if (relId != tuple.getRelId()) {
                    relId = tuple.getRelId();
                    writeUuidBasedIdDetails(termFactory.getId(relId), allowedStatus, null);
                }

                StringBuilder stringBuilder = new StringBuilder();
                // Relationship ID
                createRecord(stringBuilder, getFirstUuid(rel.getRelId()));

                // Concept status
                createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedConceptStatusId(
                        termFactory.getUids(part.getStatusId())));

                // Concept Id 1 UUID
                createRecord(stringBuilder, getFirstUuid((rel.getC1Id())));

                // Relationship type UUID
                createRecord(stringBuilder, getFirstUuid((part.getRelTypeId())));

                // Concept Id 2 UUID
                createRecord(stringBuilder, getFirstUuid(rel.getC2Id()));

                // (Characteristict Type integer)
                int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(
                        termFactory.getUids(part.getCharacteristicId()));
                if (snomedCharacter == -1) {
                    errorWriter.append("\nNo characteristic mapping for: "
                            + termFactory.getConcept(part.getCharacteristicId()));
                }
                createRecord(stringBuilder, snomedCharacter);

                // Refinability integer
                createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(
                        termFactory.getUids(part.getRefinabilityId())));

                // Relationship Group
                createRecord(stringBuilder, part.getGroup());

                // Amt added
                // Relationship UUID
                createRecord(stringBuilder, getFirstUuid((rel.getRelId())));

                // Concept1 UUID
                createRecord(stringBuilder, getFirstUuid(rel.getC1Id()));

                // Relationship type UUID
                createRecord(stringBuilder, getFirstUuid(part.getRelTypeId()));

                // Concept2 UUID
                createRecord(stringBuilder, getFirstUuid(rel.getC2Id()));

                // Characteristic Type UUID
                createRecord(stringBuilder, getFirstUuid(part.getCharacteristicId()));

                // Refinability UUID
                createRecord(stringBuilder, getFirstUuid(part.getRefinabilityId()));

                // Relationship status UUID
                createRecord(stringBuilder, getFirstUuid(part.getStatusId()));

                // Effective Time
                createVersion(stringBuilder, part.getVersion(), part.getPathId());

                //Path Id
                createRecord(stringBuilder, getFirstUuid(part.getPathId()));

                //Status active/inactive value
                createRecord(stringBuilder, getBinaryStatusValue(part.getStatusId()));

                createRecord(stringBuilder, System.getProperty("line.separator"));

                relationshipsWriter.write(stringBuilder.toString());
            }
        }
    }// End method getUuidBasedRelDetails

    private UUID getBinaryStatusValue(int statusId) throws Exception {

        I_GetConceptData status = termFactory.getConcept(statusId);

        if (activeConcept.isParentOf(status, null, null, null, false)) {
            return activeConcept.getUids().iterator().next();
        } else if (inactiveConcept.isParentOf(status, null, null, null, false)) {
            return inactiveConcept.getUids().iterator().next();
        }
        throw new Exception("Status concept " + status + " is not a child of Active or Inactive - cannot be handled.");
    }


    private void createVersion(StringBuilder buffer, int version, int pathId) {
        if (releaseDate != null) {
            createRecord(buffer, releaseDate);
        } else {
            createReleaseVersion(buffer, pathId);
        }
    }

    /*
     * Get the defined "release" version for a specific path.
     * This is declared in the Path Version Reference Set (String).
     * The path concept must contain exactly one extensions for the path version refset.
     */
    private void createReleaseVersion(StringBuilder buffer, int pathId) {
        if (pathReleaseVersions.containsKey(pathId)) {
            createRecord(buffer, pathReleaseVersions.get(pathId));
        } else {
            String pathUuidStr = Integer.toString(pathId);
            try {
                String pathVersion = null;
                pathUuidStr = termFactory.getUids(pathId).iterator().next().toString();

                int pathVersionRefsetNid = termFactory.uuidToNative(ConceptConstants.PATH_VERSION_REFSET.getUuids()[0]);
                int currentStatusId = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
                for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(pathId)) {
                    if (extension.getRefsetId() == pathVersionRefsetNid) {
                        I_ThinExtByRefPart latestPart = getLatestVersion(extension);
                        if (latestPart.getStatusId() == currentStatusId) {

                            if (pathVersion != null) {
                                throw new TerminologyException("Concept contains multiple extensions for refset" +
                                        ConceptConstants.PATH_VERSION_REFSET.getDescription());
                            }

                            pathVersion = ((I_ThinExtByRefPartString) latestPart).getStringValue();
                        }
                    }
                }

                if (pathVersion == null) {
                    throw new TerminologyException("Concept not a member of " +
                            ConceptConstants.PATH_VERSION_REFSET.getDescription());
                }

                createRecord(buffer, pathVersion);

            } catch (Exception e) {
                throw new RuntimeException("Failed to obtain the release version for the path " + pathUuidStr, e);
            }
        }
    }

    private void writeUuidBasedDescriptionDetails(I_GetConceptData concept, I_IntSet allowedStatus, I_IntSet allowedTypes)
            throws Exception {

        List<I_DescriptionTuple> tuples = concept.getDescriptionTuples(null,null, positions);

        int descId = 0;

        // Filter the tuples down to just the latest version for each unique description
        HashMap<Integer, I_DescriptionTuple> latestDesc = new HashMap<Integer, I_DescriptionTuple>();
        for (I_DescriptionTuple tuple : tuples) {

            if (validatePositions && !validPosition(tuple.getPathId())) {
                continue;
            }

            if (!latestDesc.containsKey(tuple.getDescId())) {
                latestDesc.put(tuple.getDescId(), tuple);
            } else {
                I_DescriptionTuple latestTuple = latestDesc.get(tuple.getDescId());
                if (tuple.getVersion() >= latestTuple.getVersion()) {
                    latestDesc.put(tuple.getDescId(), tuple);
                }
            }
        }

        for (I_DescriptionTuple desc : latestDesc.values()) {

            I_DescriptionPart part = desc.getPart();
            if ((!validatePositions || validPosition(part.getPathId()))
                    && allowedStatus.contains(part.getStatusId())
                    && (!exportCohesiveSet || isExportable(ConceptBean.get(part.getTypeId())))) {

                if (descId != desc.getDescId()) {
                    descId = desc.getDescId();
                    writeUuidBasedIdDetails(termFactory.getId(descId), allowedStatus, null);
                }

                StringBuilder stringBuilder = new StringBuilder("");
                createRecord(stringBuilder, termFactory.getConcept(desc.getDescVersioned().getDescId()).getUids().get(0));

                // Description Status
                createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionStatusId(
                        termFactory.getUids(part.getStatusId())));

                // ConceptId
                createRecord(stringBuilder, concept.getUids().get(0));

                // Term
                createRecord(stringBuilder, part.getText());

                // Case sensitivity
                createRecord(stringBuilder, part.getInitialCaseSignificant() ? 1 : 0);

                // Initial Capital Status
                createRecord(stringBuilder, part.getInitialCaseSignificant() ? 1 : 0);

                // Description Type
                createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionTypeId(
                        termFactory.getUids(part.getTypeId())));

                // Language code
                createRecord(stringBuilder, part.getLang());

                // Language code for UUID
                createRecord(stringBuilder, part.getLang());

                // AMT added
                // Description UUID
                createRecord(stringBuilder, getFirstUuid(desc.getDescVersioned().getDescId()));

                // Description status UUID
                createRecord(stringBuilder, getFirstUuid(part.getStatusId()));

                // Description type UUID
                createRecord(stringBuilder, getFirstUuid(part.getTypeId()));

                // ConceptId
                createRecord(stringBuilder, concept.getUids().get(0));

                // Effective time
                createVersion(stringBuilder, part.getVersion(), part.getPathId());

                //Path Id
                createRecord(stringBuilder, getFirstUuid(part.getPathId()));

                //Status active/inactive value
                createRecord(stringBuilder, getBinaryStatusValue(part.getStatusId()));

                // End record
                createRecord(stringBuilder, System.getProperty("line.separator"));

                descriptionsWriter.write(stringBuilder.toString());
            }
        }
    }// End method getUuidBasedDescriptionDetails

    private UUID getFirstUuid(int nid)
            throws TerminologyException, IOException {
        return termFactory.getUids(nid).iterator().next();
    }

    private I_ThinExtByRefPart getLatestVersion(I_ThinExtByRefVersioned extension) {
        I_ThinExtByRefPart latestPart = null;
        for (I_ThinExtByRefPart part : extension.getVersions()) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                latestPart = part;
            }
        }
        return latestPart;
    }

    public int getConceptsSuppressed() {
        return conceptsSuppressed;
    }

    public void setConceptsSuppressed(int conceptsSuppressed) {
        this.conceptsSuppressed = conceptsSuppressed;
    }

    private void createRecord(StringBuilder stringBuilder, Object fieldData) {
        stringBuilder.append(fieldData);
        if (fieldData != System.getProperty("line.separator"))
            stringBuilder.append("\t");
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setValidatePositions(boolean validatePositions) {
        this.validatePositions = validatePositions;
    }
}
