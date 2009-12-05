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
package org.dwfa.mojo.refset.writers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.UuidSnomedMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

public abstract class MemberRefsetHandler extends IterableFileReader<I_ThinExtByRefPart> {

    protected static final String COMPONENT_ID = "COMPONENT_ID";
    protected static final String STATUS_ID = "STATUS_ID";
    protected static final String VERSION = "VERSION";
    protected static final String PATH_ID = "PATH_ID";
    protected static final String REFSET_ID = "REFSET_ID";
    protected static final String FILE_DELIMITER = "\t";
    private static final String ID = "MEMBER_ID";
    private static UuidSnomedMapHandler sctGenerator = null;
    private static File fixedMapDirectory;
    private static File readWriteMapDirectory;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'hhmmss'Z'");
    private I_TermFactory tf;
    private StringTokenizer st;
    private Map<String, Object> currentRow;
    private static I_GetConceptData module;

    /**
     * @return the header line for the refset file of this type
     */
    public String getHeaderLine() {
        return "ID" + FILE_DELIMITER + PATH_ID + FILE_DELIMITER + "EFFECTIVE_DATE" + FILE_DELIMITER + "ACTIVE"
            + FILE_DELIMITER + "REFSET_ID" + FILE_DELIMITER + COMPONENT_ID;
    }

    /**
     * @param tuple
     *            extension part to format
     * @param sctId
     *            true if the identifier should be a SNOMED ID, false otherwise
     * @return string representation of the part fit for a file of this
     *         handler's type
     * @throws IOException
     * @throws TerminologyException
     */
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctId)
            throws TerminologyException, IOException {
        return formatRefsetLine(tf, tuple, tuple.getMemberId(), tuple.getRefsetId(), tuple.getComponentId(), sctId);
    }

    /**
     * String representation of the tuple in Release Format 2.
     * 
     * @param tf
     * @param tuple
     *            extension part to format
     * @param sctId
     *            true if the identifier should be a SNOMED ID, false for UUID
     * @return string representation of the part fit for a file of this
     *         handler's type
     * @throws TerminologyException
     * @throws IOException
     */
    public String formatRefsetLineRF2(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetNid,
            int componentId, boolean sctId) throws TerminologyException, IOException {
        String formattedLine = getRefsetAndReferencePart(tf, part, memberId, refsetNid, componentId, sctId);

        try {
            if (part instanceof I_ThinExtByRefPartConcept) {
                I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part;

                formattedLine += toId(tf, conceptPart.getC1id(), sctId);
            } else if (part instanceof I_ThinExtByRefPartString) {
                I_ThinExtByRefPartString stringPart = (I_ThinExtByRefPartString) part;

                formattedLine += stringPart.getStringValue();
            } else if (part instanceof I_ThinExtByRefPartInteger) {
                I_ThinExtByRefPartInteger stringPart = (I_ThinExtByRefPartInteger) part;

                formattedLine += stringPart.getValue();
            } else if (part instanceof I_ThinExtByRefPartBoolean) {
                I_ThinExtByRefPartBoolean stringPart = (I_ThinExtByRefPartBoolean) part;

                formattedLine += stringPart.getValue();
            } else {
                throw new Exception("No known refset format type for this extension " + part.getClass().getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }

        return formattedLine;
    }

    /**
     * Returns the first 6 columns for a refset file.
     * 
     * @param tf I_TermFactory DB access
     * @param part I_ThinExtByRefPart the concept extension
     * @param memberId Integer member id, may be null or not exists in the DB.
     * @param refsetNid int refset id
     * @param componentId int referenced component id
     * @param useSctId boolean use a new sct ids
     * 
     * @return RF2 formatted refset line except the annotation/concept
     * @throws TerminologyException DB errors
     * @throws IOException DB errors
     */
    private String getRefsetAndReferencePart(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId,
            int refsetNid, int componentId, boolean useSctId) throws TerminologyException, IOException {
        StringBuffer formattedLine = new StringBuffer();

        Collection<UUID> statusUuids = tf.getUids(part.getStatusId());
        String id = getMemberId(memberId, useSctId, componentId, refsetNid);
        String effectiveDate = getDate(tf, part.getVersion());
        boolean active = isActiveStatus(statusUuids);
        String moduleId = toId(tf, getModule().getConceptId(), useSctId);
        String refsetId = toId(tf, refsetNid, useSctId);
        String referencedComponentId = toId(tf, componentId, useSctId);

        formattedLine.append(id);
        formattedLine.append(FILE_DELIMITER);
        formattedLine.append(effectiveDate);
        formattedLine.append(FILE_DELIMITER);
        formattedLine.append(((active) ? 1 : 0));
        formattedLine.append(FILE_DELIMITER);
        formattedLine.append(moduleId);
        formattedLine.append(FILE_DELIMITER);
        formattedLine.append(refsetId);
        formattedLine.append(FILE_DELIMITER);
        formattedLine.append(referencedComponentId);
        formattedLine.append(FILE_DELIMITER);

        return formattedLine.toString();
    }

    public static boolean isActiveStatus(Collection<UUID> statusUuids) {
        boolean active;
        int activeId = ArchitectonicAuxiliary.getSnomedConceptStatusId(statusUuids);
        if (activeId == 0) {
            active = true;
        } else {
            active = false;
        }
        return active;
    }

    /**
     * String representation of the refset tuple as a subset.
     * 
     * @param tf
     * @param tuple
     *            extension part to format
     * @param sctId
     *            true if the identifier should be a SNOMED ID, false for UUID
     * @return string representation of the part fit for a file of this
     *         handler's type
     * @throws TerminologyException
     * @throws IOException
     */
    public String formatRefsetAsSubset(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, String subsetId,
            int componentNid, boolean sctId) throws TerminologyException, IOException {

        try {

            if (part instanceof I_ThinExtByRefPartConcept) {
                I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part;
                Collection<UUID> statusUuids = tf.getUids(conceptPart.getStatusId());

                int statusInt;
                if (isActiveStatus(statusUuids)) {
                    statusInt = 1;
                } else {
                    statusInt = 0;
                }

                // String refsetId = toId(tf, refsetNid, sctId);
                String componentId = toId(tf, componentNid, sctId);

                // return refsetId + FILE_DELIMITER + componentId + FILE_DELIMITER + statusInt;
                return subsetId + FILE_DELIMITER + componentId + FILE_DELIMITER + statusInt;
            } else {
                throw new Exception("Part is not of type concept ext " + part);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public String getRefsetSctID(I_TermFactory tf, int refsetNid) throws TerminologyException, IOException {
        return toId(tf, refsetNid, true);
    }

    /**
     * @return the header line for the refset file of this type in RF2
     */
    public String getRF2HeaderLine() {
        return "Id" + FILE_DELIMITER + "effectiveTime" + FILE_DELIMITER + "active" + FILE_DELIMITER + "moduleId"
            + FILE_DELIMITER + "refSetId" + FILE_DELIMITER + "referencedComponentId" + FILE_DELIMITER + "conceptId";
    }

    /**
     * @return the header line for the refset file of this type in subset format
     */
    public String getSubsetFormatHeaderLine() {
        return "SUBSETID" + FILE_DELIMITER + "MEMBERID" + FILE_DELIMITER + "MEMBERSTATUS" + FILE_DELIMITER + "LINKEDID";
    }

    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart tuple, Integer memberId, int refsetId,
            int componentId, boolean sctId) throws TerminologyException, IOException {
        return getMemberId(memberId, sctId, componentId, refsetId) + FILE_DELIMITER
            + toId(tf, tuple.getPathId(), sctId) + FILE_DELIMITER + getDate(tf, tuple.getVersion()) + FILE_DELIMITER
            + toId(tf, tuple.getStatusId(), sctId) + FILE_DELIMITER + toId(tf, refsetId, sctId) + FILE_DELIMITER
            + toId(tf, componentId, sctId);
    }

    private String getMemberId(Integer memberId, boolean sctId, int componentNid, int refsetNid)
            throws UnsupportedEncodingException, TerminologyException, IOException {
        UUID uuid;
        if (memberId == null) {
            // generate new id
            uuid =
                    UUID.nameUUIDFromBytes(("org.dwfa." + getTermFactory().getUids(componentNid) + getTermFactory()
                        .getUids(refsetNid)).getBytes("8859_1"));
        } else {
            if (getTermFactory().getUids(memberId) == null) {
                System.out.println("Member id " + memberId + " has no UUIDs!!! for refset "
                    + getTermFactory().getConcept(refsetNid) + " for component "
                    + getTermFactory().getConcept(componentNid));

                uuid =
                        UUID.nameUUIDFromBytes(("org.dwfa." + getTermFactory().getUids(componentNid) + getTermFactory()
                            .getUids(refsetNid)).getBytes("8859_1"));
            } else {
                uuid = getTermFactory().getUids(memberId).iterator().next();
            }
        }

        if (sctId) {
            return Long.toString(getSctGenerator().getWithGeneration(uuid, TYPE.SUBSET));
        } else {
            return uuid.toString();
        }
    }

    private String getDate(I_TermFactory tf, int version) {
        return dateFormat.format(tf.convertToThickVersion(version));
    }

    public String toId(I_TermFactory tf, int componentId, boolean sctId) throws TerminologyException, IOException {
        if (sctId) {
            // TODO this assumes that the componentId is a concept! it might be
            // a description or relationship

            // check if there's an existing snomed ID to use first
            String snomedId = getSnomedIntegerId(tf, componentId);
            if (snomedId != null) {
                return snomedId; // TODO need to add to map?
            } else {
                return Long.toString(getSctGenerator().getWithGeneration(tf.getUids(componentId).iterator().next(),
                    TYPE.CONCEPT));
            }
        } else { // uuid
            return tf.getUids(componentId).iterator().next().toString();
        }
    }

    public String generateNewSctId(int refsetId, int subsetVersion) throws TerminologyException, IOException {
        UUID refsetUuid = tf.getUids(refsetId).iterator().next();

        // generate a new UUID from refset ID + subset version
        UUID versionedRefsetUuid;
        try {
            versionedRefsetUuid = Type5UuidFactory.get(refsetUuid, subsetVersion + "");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new TerminologyException(e.getLocalizedMessage());
        }

        // use this new UUID in the SCT generator
        return Long.toString(getSctGenerator().getWithGeneration(versionedRefsetUuid, TYPE.CONCEPT));
    }

    public String getSnomedIntegerId(I_TermFactory tf, int componentId) throws TerminologyException, IOException {

        I_IdVersioned idVersioned = tf.getId(componentId);
        int snomedIntegerId =
                tf.getId(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()).getNativeId();

        List<I_IdPart> parts = idVersioned.getVersions();
        I_IdPart latestPart = null;
        for (I_IdPart part : parts) {
            if (latestPart == null || part.getVersion() >= latestPart.getVersion()) {
                if (part.getSource() == snomedIntegerId) {
                    latestPart = part;
                }
            }
        }

        if (latestPart != null) {
            return latestPart.getSourceId().toString();
        } else {
            return null;
        }

    }

    private static synchronized UuidSnomedMapHandler getSctGenerator() throws IOException {
        if (sctGenerator == null) {
            sctGenerator = new UuidSnomedMapHandler(fixedMapDirectory, readWriteMapDirectory);
        }
        return sctGenerator;
    }

    @Override
    protected abstract I_ThinExtByRefPart processLine(String line);

    protected Map<String, Object> parseLine(String line) throws ParseException {
        st = new StringTokenizer(line, FILE_DELIMITER);
        currentRow = new HashMap<String, Object>();

        currentRow.put(ID, UUID.fromString(st.nextToken()));
        currentRow.put(PATH_ID, UUID.fromString(st.nextToken()));
        currentRow.put(VERSION, getAceVersionFromDateString(st.nextToken()));
        currentRow.put(STATUS_ID, UUID.fromString(st.nextToken()));
        currentRow.put(REFSET_ID, UUID.fromString(st.nextToken()));
        currentRow.put(COMPONENT_ID, UUID.fromString(st.nextToken()));
        return currentRow;
    }

    protected int getAceVersionFromDateString(String string) throws ParseException {
        Date parsedDate = dateFormat.parse(string);
        return getTermFactory().convertToThinVersion(parsedDate.getTime());
    }

    protected I_TermFactory getTermFactory() {
        if (tf == null) {
            tf = LocalVersionedTerminology.get();
        }

        return tf;
    }

    protected String getNextCurrentRowToken() {
        return st.nextToken();
    }

    protected void setGenericExtensionPartFields(I_ThinExtByRefPart part) throws Exception {
        part.setPathId(getNid((UUID) currentRow.get(MemberRefsetHandler.PATH_ID)));
        part.setStatusId(getNid((UUID) currentRow.get(MemberRefsetHandler.STATUS_ID)));
        part.setVersion((Integer) currentRow.get(MemberRefsetHandler.VERSION));
    }

    protected I_ThinExtByRefVersioned getExtensionVersioned(String line, RefsetAuxiliary.Concept refsetType)
            throws Exception {
        Map<String, Object> currentRow = parseLine(line);

        UUID refsetUuid = (UUID) currentRow.get(MemberRefsetHandler.REFSET_ID);
        UUID componentUuid = (UUID) currentRow.get(MemberRefsetHandler.COMPONENT_ID);
        UUID memberUuid = (UUID) currentRow.get(MemberRefsetHandler.ID);
        int componentNid = getNid(componentUuid);
        List<I_ThinExtByRefVersioned> extensions = getTermFactory().getAllExtensionsForComponent(componentNid, true);

        I_ThinExtByRefVersioned versioned = null;
        int refsetNid = getNid(refsetUuid);

        Integer memberNid = null;
        if (getTermFactory().hasId(memberUuid)) {
            memberNid = getNid(memberUuid);

            for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
                if (thinExtByRefVersioned.getMemberId() == memberNid) {
                    versioned = thinExtByRefVersioned;
                    break;
                }
            }
        }

        if (versioned == null) {
            memberNid =
                    getTermFactory().uuidToNativeWithGeneration(memberUuid,
                        ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(),
                        getTermFactory().getPaths(), Integer.MAX_VALUE);

            if (isTransactional()) {
                versioned =
                        getTermFactory().newExtension(refsetNid, memberNid, componentNid,
                            getTermFactory().uuidToNative(refsetType.getUids()));
            } else {
                versioned =
                        getTermFactory().getDirectInterface().newExtensionBypassCommit(refsetNid, memberNid,
                            componentNid, getTermFactory().uuidToNative(refsetType.getUids()));
            }

        }

        return versioned;
    }

    protected int getNid(UUID id) throws TerminologyException, IOException {
        return getTermFactory().uuidToNative(id);
    }

    public File getFixedMapDirectory() {
        return fixedMapDirectory;
    }

    public static synchronized void setFixedMapDirectory(File fixedMapDirectory) {
        if (MemberRefsetHandler.fixedMapDirectory != null) {
            throw new RuntimeException("Fixed map directory can only be set once! Current value is "
                + fixedMapDirectory + " - please call MemberRefsetHandler.cleanup() to reset maps and files");
        }
        MemberRefsetHandler.fixedMapDirectory = fixedMapDirectory;
    }

    public File getReadWriteMapDirectory() {
        return readWriteMapDirectory;
    }

    public static synchronized void setReadWriteMapDirectory(File readWriteMapDirectory) {
        if (MemberRefsetHandler.readWriteMapDirectory != null) {
            throw new RuntimeException("Read write map directory can only be set once! Current value is "
                + readWriteMapDirectory + " - please call MemberRefsetHandler.cleanup() to reset maps and files");
        }
        MemberRefsetHandler.readWriteMapDirectory = readWriteMapDirectory;
    }

    public static synchronized void cleanup() throws IOException {
        if (sctGenerator != null) {
            sctGenerator.writeMaps();
            sctGenerator = null;
        }
        fixedMapDirectory = null;
        readWriteMapDirectory = null;
    }

    public static void setModule(ConceptDescriptor moduleDescriptor) throws Exception {
        module = moduleDescriptor.getVerifiedConcept();
    }

    public static I_GetConceptData getModule() {
        return module;
    }
}
