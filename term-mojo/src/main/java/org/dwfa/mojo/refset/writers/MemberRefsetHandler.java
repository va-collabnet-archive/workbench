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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.ArchitectonicAuxiliary.Concept;
import org.dwfa.maven.sctid.UuidSnomedDbMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.NAMESPACE;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PathReleaseDateConfig;
import org.dwfa.mojo.file.AceIdentifierRow;
import org.dwfa.mojo.file.AceIdentifierWriter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.AceDateFormat;
import org.dwfa.vodb.bind.ThinVersionHelper;

public abstract class MemberRefsetHandler extends IterableFileReader<I_ThinExtByRefPart> {

    UUID activeNid = null;
    UUID inactiveNid = null;
    protected static final String COMPONENT_ID = "COMPONENT_ID";
    protected static final String STATUS_ID = "STATUS_ID";
    protected static final String VERSION = "VERSION";
    protected static final String PATH_ID = "PATH_ID";
    protected static final String REFSET_ID = "REFSET_ID";

    private static final String SNOMED_CT_ID_REG_EXP = "^[0-9]{6,18}$";
    private static final String UUID_REG_EXP = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
    private static final String ID = "MEMBER_ID";
    private static UuidSnomedDbMapHandler sctGenerator = null;
    private DateFormat dateFormat = AceDateFormat.getRf2DateFormat();
    String DATE_FORMAT = "yyyy.mm.dd hh:mm:ss";
    private I_TermFactory tf;
    private StringTokenizer st;
    private Map<String, Object> currentRow;
    private static I_GetConceptData module;
    private Logger logger = Logger.getLogger(MemberRefsetHandler.class.getName());
    private Map<Integer, String> toIdCache = new HashMap<Integer, String>();
    private AceIdentifierWriter aceIdentifierWriter = null;
    private ConceptDescriptor activeStatus = new ConceptDescriptor("32dc7b19-95cc-365e-99c9-5095124ebe72", "active");
    private DateFormat exportDateFormat = AceDateFormat.getRf2TimezoneDateFormat();

    /**
     * Used to configure the release date/version to use for given paths,
     * paths not specified will be exported with the version recorded in the
     * database.
     * Note this configuration will be ignored and overridden by the releaseDate
     * parameter
     */
    private static PathReleaseDateConfig[] pathReleaseDateConfig;

    public PathReleaseDateConfig[] getPathReleaseDateConfig() {
        return pathReleaseDateConfig;
    }

    public static void setPathReleaseDateConfig(PathReleaseDateConfig[] pathReleaseDateConfigToSet) {
        pathReleaseDateConfig = pathReleaseDateConfigToSet;
    }

    private static Integer snomedIdNid = null;
    private static HashMap<Integer, String> pathReleaseVersions = new HashMap<Integer, String>();

    /**
     * Column delimiter used to separate data fields into columns in reference
     * set records
     */
    public static final String COLUMN_DELIMITER = "\t";

    /**
     * Base header for all RF2 refset files
     */
    public static final String RF2_HEADER = "id" + COLUMN_DELIMITER + "effectiveTime" + COLUMN_DELIMITER + "active"
        + COLUMN_DELIMITER + "moduleId" + COLUMN_DELIMITER + "refSetId" + COLUMN_DELIMITER + "referencedComponentId";

    /**
     * Basic header used for all ARF reference sets - additional types append to
     * this
     */
    public static final String BASIC_REFSET_HEADER = "ID" + COLUMN_DELIMITER + PATH_ID + COLUMN_DELIMITER
        + "EFFECTIVE_DATE" + COLUMN_DELIMITER + "ACTIVE" + COLUMN_DELIMITER + "REFSET_ID" + COLUMN_DELIMITER
        + COMPONENT_ID;

    /**
     * @return the header line for the refset file of this type
     */
    public String getHeaderLine() {
        return BASIC_REFSET_HEADER;
    }

    /**
     * @param tuple
     *            extension part to format
     * @param sctId
     *            true if the identifier should be a SNOMED ID, false otherwise
     * @return string representation of the part fit for a file of this
     *         handler's type
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple, boolean sctId, boolean useRf2)
            throws SQLException, ClassNotFoundException, Exception {
        return formatRefsetLine(tf, tuple, getMemberUuid(tuple.getMemberId(), tuple.getComponentId(),
            tuple.getRefsetId()), tuple.getRefsetId(), tuple.getComponentId(), sctId, useRf2);
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
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public String formatRefsetLineRF2(I_TermFactory tf, I_ThinExtByRefPart part, UUID memberUuid, int refsetNid,
            int componentId, boolean sctId, boolean useRf2, TYPE type) throws SQLException, ClassNotFoundException,
            Exception {
        String formattedLine = getRefsetAndReferencePart(tf, part, memberUuid, refsetNid, componentId, sctId, useRf2,
            type);

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
     * @throws Exception
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private String getRefsetAndReferencePart(I_TermFactory tf, I_ThinExtByRefPart part, UUID memberUuid, int refsetNid,
            int componentId, boolean useSctId, boolean useRf2, TYPE type) throws SQLException, ClassNotFoundException,
            Exception {
        StringBuffer formattedLine = new StringBuffer();
        String memberid = getMemberId(memberUuid, componentId, refsetNid, useRf2);
        String effectiveDate = createReleaseVersion(part.getPathId(), part.getVersion());

        if (aceIdentifierWriter != null) {
            AceIdentifierRow aceIdentifierRow = new AceIdentifierRow();

            aceIdentifierRow.setEffectiveTime(effectiveDate);
            aceIdentifierRow.setPathUuid(getModule().getUids().get(0).toString());
            aceIdentifierRow.setPrimaryUuid(memberUuid.toString());
            aceIdentifierRow.setSourceId(memberid);
            aceIdentifierRow.setSourceSystemUuid(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()
                .iterator()
                .next()
                .toString());
            aceIdentifierRow.setStatusUuid(activeStatus.getUuid());

            aceIdentifierWriter.write(aceIdentifierRow);
        }

        Collection<UUID> statusUuids = tf.getUids(part.getStatusId());
        boolean active = isActiveStatus(statusUuids);
        String moduleId = toIdWithCache(tf, getModule().getConceptId(), useSctId, TYPE.CONCEPT);
        String refsetId = toIdWithCache(tf, refsetNid, useSctId, TYPE.CONCEPT);
        String referencedComponentId = toId(tf, componentId, useSctId, type);

        formattedLine.append(memberid);
        formattedLine.append(COLUMN_DELIMITER);
        formattedLine.append(effectiveDate);
        formattedLine.append(COLUMN_DELIMITER);
        formattedLine.append(((active) ? 1 : 0));
        formattedLine.append(COLUMN_DELIMITER);
        formattedLine.append(moduleId);
        formattedLine.append(COLUMN_DELIMITER);
        formattedLine.append(refsetId);
        formattedLine.append(COLUMN_DELIMITER);
        formattedLine.append(referencedComponentId);

        return formattedLine.toString();
    }

    private synchronized String toIdWithCache(I_TermFactory tf, int id, boolean useSctId, TYPE type)
            throws SQLException, ClassNotFoundException, Exception {
        String result = toIdCache.get(id);
        if (result == null) {
            result = toId(tf, id, useSctId, type);
            toIdCache.put(id, result);
        }
        return result;
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
    public String formatRefsetAsSubset(I_TermFactory tf, I_ThinExtByRefPart part, Integer memberId, int refsetNid,
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

                String refsetId = toIdWithCache(tf, refsetNid, sctId, TYPE.SUBSET);
                String componentId = toId(tf, componentNid, sctId, TYPE.CONCEPT);

                return refsetId + COLUMN_DELIMITER + componentId + COLUMN_DELIMITER + statusInt;
            } else {
                throw new Exception("Part is not of type concept ext " + part);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public String getRefsetSctID(I_TermFactory tf, int refsetNid, TYPE type) throws SQLException,
            ClassNotFoundException, Exception {
        return toIdWithCache(tf, refsetNid, true, type);
    }

    /**
     * @return the header line for the refset file of this type in RF2
     */
    public String getRF2HeaderLine() {
        return RF2_HEADER;
    }

    /**
     * @return the header line for the refset file of this type in subset format
     */
    public String getSubsetFormatHeaderLine() {
        return "SUBSETID" + COLUMN_DELIMITER + "MEMBERID" + COLUMN_DELIMITER + "MEMBERSTATUS" + COLUMN_DELIMITER
            + "LINKEDID";
    }

    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart tuple, UUID memberUuid, int refsetId,
            int componentId, boolean sctId, boolean useRf2) throws SQLException, ClassNotFoundException, Exception {
        return memberUuid + COLUMN_DELIMITER
            + toIdWithCache(tf, tuple.getPathId(), sctId, TYPE.CONCEPT) + COLUMN_DELIMITER
            + getDate(tf, tuple.getVersion()) + COLUMN_DELIMITER
            + toIdWithCache(tf, tuple.getStatusId(), sctId, TYPE.CONCEPT) + COLUMN_DELIMITER
            + toIdWithCache(tf, refsetId, sctId, TYPE.SUBSET) + COLUMN_DELIMITER
            + toId(tf, componentId, sctId, TYPE.SUBSET);
    }

    /**
     * HACK
     *
     * @param forConcept
     * @return
     * @throws IOException
     * @throws TerminologyException
     */
    private I_Path getLatestPath(I_GetConceptData forConcept) throws IOException, TerminologyException {
        I_Path path = null;

        if (forConcept.getConceptAttributes() != null && !forConcept.getConceptAttributes().getVersions().isEmpty()) {
            I_ConceptAttributePart part = forConcept.getConceptAttributes().getVersions().get(0);
            path = tf.getPath(part.getPathId());
        }

        return path;
    }

    /**
     * THIS IS A HACK REMOVE.
     *
     * Get the namespace for the I_Path.
     *
     * @param forPath I_path
     * @return NAMESPACE
     */
    private NAMESPACE getNamespace(I_Path forPath) {
        NAMESPACE namespace = NAMESPACE.NEHTA;

        if (forPath != null && forPath.toString().equals("SNOMED Core")) {
            namespace = NAMESPACE.SNOMED_META_DATA;
        }

        return namespace;
    }

    private String getMemberId(UUID memberUuid, int componentNid, int refsetNid, boolean useRf2) throws SQLException,
            ClassNotFoundException, Exception {
        I_Path refsetPath = getLatestPath(getTermFactory().getConcept(refsetNid));

        return Long.toString(getSctGenerator().getWithGeneration(memberUuid, getNamespace(refsetPath),
            getSctIdTypeForExtention(componentNid, useRf2)));
    }

    protected UUID getMemberUuid(Integer memberNid, int componentNid, int refsetNid) throws UnsupportedEncodingException,
            TerminologyException, IOException {
        UUID uuid;
        if (memberNid == null) {
            // generate new id
            uuid = UUID.nameUUIDFromBytes(("org.dwfa." + getTermFactory().getUids(componentNid) + getTermFactory().getUids(refsetNid)).getBytes("8859_1"));
        } else {
            if (getTermFactory().getUids(memberNid) == null) {
                logger.warning("No UUID for member, component and refset ids.");
                uuid = UUID.nameUUIDFromBytes(("org.dwfa." + getTermFactory().getUids(componentNid) + getTermFactory().getUids(refsetNid)).getBytes("8859_1"));
            } else {
                uuid = getTermFactory().getUids(memberNid).iterator().next();
            }
        }
        return uuid;
    }

    private TYPE getSctIdTypeForExtention(int componentNid, boolean useRf2) throws IOException {
        TYPE type = TYPE.SUBSET;

        if (useRf2) {
            type = TYPE.REFSET;
        }

        return type;
    }

    private String getDate(I_TermFactory tf, int version) {
        return dateFormat.format(tf.convertToThickVersion(version));
    }

    protected String toId(I_TermFactory tf, int componentId, boolean sctId, TYPE type) throws SQLException,
            ClassNotFoundException, Exception {
        String id = null;
        if (sctId) {
            I_Path refsetPath = getLatestPath(tf.getConcept(componentId));

            id = Long.toString(getSctGenerator().getWithGeneration(tf.getUids(componentId).iterator().next(),
                getNamespace(refsetPath), type));

            if (id == null) {
                logger.severe("no sct-id type for this component.");
                throw new TerminologyException("cannot create a sct id for this component: " + componentId);
            }

        } else { // uuid
            id = tf.getUids(componentId).iterator().next().toString();
        }

        return id;
    }

    private static synchronized UuidSnomedDbMapHandler getSctGenerator() throws Exception,
            ClassNotFoundException {
        if (sctGenerator == null) {
            sctGenerator = UuidSnomedDbMapHandler.getInstance();
        }
        return sctGenerator;
    }

    @Override
    protected abstract I_ThinExtByRefPart processLine(String line);

    protected Map<String, Object> parseLine(String line) throws ParseException, TerminologyException, IOException,
            org.apache.lucene.queryParser.ParseException {
        st = new StringTokenizer(line, COLUMN_DELIMITER);
        currentRow = new HashMap<String, Object>();

        try {
            currentRow.put(ID, componentUuidFromIdString(st.nextToken()));
            currentRow.put(VERSION, getAceVersionFromDateString(st.nextToken()));
            currentRow.put(STATUS_ID, getStatusFromString(st.nextToken()));
            currentRow.put(PATH_ID, componentUuidFromIdString(st.nextToken()));
            currentRow.put(REFSET_ID, componentUuidFromIdString(st.nextToken()));
            currentRow.put(COMPONENT_ID, componentUuidFromIdString(st.nextToken()));
            return currentRow;
        } catch (Exception e) {
            throw new TerminologyException("Failed to parse line '" + line + "'", e);
        }
    }

    private UUID getStatusFromString(String nextToken) throws Exception {

        if (activeNid == null || inactiveNid == null) {
            activeNid = ArchitectonicAuxiliary.Concept.ACTIVE.getUids().iterator().next();
            inactiveNid = ArchitectonicAuxiliary.Concept.INACTIVE.getUids().iterator().next();
        }
        if (nextToken.equals("0")) {
            return inactiveNid;
        } else if (nextToken.equals("1")) {
            return activeNid;
        }
        throw new Exception("Failed to parse '" + nextToken + "' as a status value - expected 1 or 0");
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
            memberNid = getTermFactory().uuidToNativeWithGeneration(memberUuid,
                ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), getTermFactory().getPaths(),
                Integer.MAX_VALUE);

            if (isTransactional()) {
                versioned = getTermFactory().newExtension(refsetNid, memberNid, componentNid,
                    getTermFactory().uuidToNative(refsetType.getUids()));
            } else {
                versioned = getTermFactory().getDirectInterface().newExtensionBypassCommit(refsetNid, memberNid,
                    componentNid, getTermFactory().uuidToNative(refsetType.getUids()));
            }

        }

        return versioned;
    }

    protected int getNid(UUID id) throws TerminologyException, IOException {
        return getTermFactory().uuidToNative(id);
    }

    /**
     * This method determines if the string passed is a UUID or SCTID and then
     * finds the NID of the matching concept using the passed identifier.
     *
     * @param id String representation of a UUID or SCTID
     * @return NID of the matching concept if found
     * @throws TerminologyException if not found
     * @throws IOException
     * @throws org.apache.lucene.queryParser.ParseException
     */
    public static int conceptFromIdString(String id) throws TerminologyException, IOException,
            org.apache.lucene.queryParser.ParseException {
        if (id.matches(UUID_REG_EXP)) {
            return LocalVersionedTerminology.get().getConcept(UUID.fromString(id)).getConceptId();
        } else if (id.matches(SNOMED_CT_ID_REG_EXP)) {
            if (snomedIdNid == null) {
                snomedIdNid = Concept.SNOMED_INT_ID.localize().getNid();
            }
            return LocalVersionedTerminology.get().getConcept(id, snomedIdNid).getConceptId();
        }
        throw new TerminologyException("Identifier '" + id + "' cannot be handled - not a UUID or an SCTID");
    }

    protected int componentFromIdString(String id) throws TerminologyException, IOException {

        return componentIdVersionedFromIdString(id).getNativeId();
    }

    protected UUID componentUuidFromIdString(String id) throws IOException, TerminologyException {
        return componentIdVersionedFromIdString(id).getUIDs().iterator().next();
    }

    protected I_IdVersioned componentIdVersionedFromIdString(String id) throws IOException, TerminologyException {
        if (id.matches(UUID_REG_EXP)) {
            return LocalVersionedTerminology.get().getId(UUID.fromString(id));
        } else if (id.matches(SNOMED_CT_ID_REG_EXP)) {
            if (snomedIdNid == null) {
                snomedIdNid = Concept.SNOMED_INT_ID.localize().getNid();
            }

            Collection<I_IdVersioned> idSearchResult = LocalVersionedTerminology.get().getId(id, snomedIdNid);
            if (idSearchResult.size() > 1) {
                I_IdVersioned result = null;
                for (I_IdVersioned idVersioned : idSearchResult) {
                    for (I_IdTuple tuple : idVersioned.getTuples()) {
                        if (tuple.getSource() == snomedIdNid && tuple.getSourceId().equals(id)) {
                            if (result == null) {
                                result = idVersioned;
                            } else if (result.getNativeId() != tuple.getNativeId()) {
                                throw new TerminologyException("Multiple matching tuples for '" + id
                                    + "' as a SNOMED CT ID with different NIDs " + idSearchResult);
                            }
                        }
                    }
                }
                if (result != null) {
                    return result;
                } else {
                    throw new TerminologyException("Cannot find identifier for SCTID '" + id
                        + "' in multiple matching results " + idSearchResult);
                }
            } else if (idSearchResult.size() == 0) {
                throw new TerminologyException("Cannot find identifier for SCTID " + id);
            }
            return idSearchResult.iterator().next();
        }
        throw new TerminologyException("Identifier '" + id + "' cannot be handled - not a UUID or an SCTID");
    }

    public static synchronized void cleanup() throws Exception {
        if (sctGenerator != null) {
            sctGenerator.writeMaps();
            sctGenerator = null;

        }
    }

    public static void setModule(ConceptDescriptor moduleDescriptor) throws Exception {
        module = moduleDescriptor.getVerifiedConcept();
    }

    public static I_GetConceptData getModule() {
        return module;
    }

    /**
     * Opens the id file for writing.
     *
     * @param aceIdentifierWriter the aceIdentifierWriter to set
     * @throws IOException cannot open file for writing.
     */
    public final void setAceIdentifierFile(AceIdentifierWriter aceIdentifierWriterToSet) throws IOException {
        aceIdentifierWriter = aceIdentifierWriterToSet;
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

    /*
     * Get the defined "release" version for a specific path.
     * This is declared in the Path Version Reference Set (String).
     * The path concept must contain exactly one extensions for the path version
     * refset.
     */
    private String createReleaseVersion(int pathId, int partVersion) throws Exception {
        String buffer = new String();

        I_TermFactory termFactory = LocalVersionedTerminology.get();
        if (pathReleaseVersions.containsKey(pathId)) {
            buffer = exportDateFormat.format(new Date(ThinVersionHelper.convert(partVersion)));
            for (PathReleaseDateConfig config : pathReleaseDateConfig) {
                if (config.getLastReleaseDate() != null
                    && config.getLastReleaseDate().getTime() < ThinVersionHelper.convert(partVersion)) {
                    buffer = pathReleaseVersions.get(pathId);
                }
            }
        } else if (pathReleaseDateConfig != null) {
            String version = null;
            for (PathReleaseDateConfig config : pathReleaseDateConfig) {
                if (config.getPath().getVerifiedConcept().getConceptId() == pathId) {
                    if (config.getLastReleaseDate() != null
                        && config.getLastReleaseDate().getTime() < ThinVersionHelper.convert(partVersion)) {
                        version = exportDateFormat.format(config.getReleaseDate());
                        pathReleaseVersions.put(pathId, version);
                    }
                }
            }
            if (version == null) {
                version = ThinVersionHelper.formatRf2(partVersion);
            }

            buffer = version;
        } else {
            String pathUuidStr = Integer.toString(pathId);
            int currentStatusNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();

            try {
                String pathVersion = null;
                pathUuidStr = termFactory.getUids(pathId).iterator().next().toString();

                int pathVersionRefsetNid = termFactory.uuidToNative(ConceptConstants.PATH_VERSION_REFSET.getUuids()[0]);
                int currentStatusId = currentStatusNid;
                for (I_ThinExtByRefVersioned extension : termFactory.getAllExtensionsForComponent(pathId)) {
                    if (extension.getRefsetId() == pathVersionRefsetNid) {
                        I_ThinExtByRefPart latestPart = getLatestVersion(extension);
                        if (latestPart.getStatusId() == currentStatusId) {

                            if (pathVersion != null) {
                                throw new TerminologyException("Concept contains multiple extensions for refset"
                                    + ConceptConstants.PATH_VERSION_REFSET.getDescription());
                            }

                            pathVersion = ((I_ThinExtByRefPartString) latestPart).getStringValue();
                        }
                    }
                }

                if (pathVersion == null) {
                    throw new TerminologyException("Concept not a member of "
                        + ConceptConstants.PATH_VERSION_REFSET.getDescription());
                }

                buffer = pathVersion;

            } catch (Exception e) {
                throw new RuntimeException("Failed to obtain the release version for the path " + pathUuidStr, e);
            }
        }

        return buffer;
    }
}
