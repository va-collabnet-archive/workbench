package org.dwfa.mojo.refset.writers;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.maven.transform.UuidSnomedMapHandler;
import org.dwfa.maven.transform.SctIdGenerator.TYPE;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.tapi.TerminologyException;

public abstract class MemberRefsetHandler extends
        IterableFileReader<I_ThinExtByRefPart> {

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
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyyMMdd'T'hhmmss'Z'");
    private I_TermFactory tf;
    private StringTokenizer st;
    private Map<String, Object> currentRow;
    private static I_GetConceptData module;

    /**
     * @return the header line for the refset file of this type
     */
    public String getHeaderLine() {
        return "ID" + FILE_DELIMITER + PATH_ID + FILE_DELIMITER
                + "EFFECTIVE_DATE" + FILE_DELIMITER + "ACTIVE" + FILE_DELIMITER
                + "REFSET_ID" + FILE_DELIMITER + COMPONENT_ID;
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
    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefTuple tuple,
            boolean sctId) throws TerminologyException, IOException {
        return formatRefsetLine(tf, tuple, tuple.getMemberId(), tuple
                .getRefsetId(), tuple.getComponentId(), sctId);
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
    public String formatRefsetLineRF2(I_TermFactory tf,
            I_ThinExtByRefPart part, Integer memberId, int refsetNid,
            int componentId, boolean sctId) throws TerminologyException,
            IOException {

        try {

            if (part instanceof I_ThinExtByRefPartConcept) {
                I_ThinExtByRefPartConcept conceptPart = (I_ThinExtByRefPartConcept) part;
                Collection<UUID> statusUuids = tf.getUids(conceptPart
                        .getStatusId());

                String id = toId(tf, memberId, sctId);
                String effectiveDate = getDate(tf, conceptPart.getVersion());
                boolean active = isActiveStatus(statusUuids);
                String moduleId = toId(tf, getModule().getConceptId(), sctId);
                String refsetId = toId(tf, refsetNid, sctId);
                String referencedComponentId = toId(tf, componentId, sctId);
                String conceptId = toId(tf, conceptPart.getC1id(), sctId);

                return id + FILE_DELIMITER + effectiveDate + FILE_DELIMITER
                        + active + FILE_DELIMITER + moduleId + FILE_DELIMITER
                        + refsetId + FILE_DELIMITER + referencedComponentId
                        + FILE_DELIMITER + conceptId;
            } else {
                throw new Exception("Part is not of type concept ext " + part);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new TerminologyException(e.getMessage());
        }
    }

    public static boolean isActiveStatus(Collection<UUID> statusUuids) {
        boolean active;
        int activeId = ArchitectonicAuxiliary
                .getSnomedConceptStatusId(statusUuids);
        if (activeId == 0) {
            active = true;
        } else {
            active = false;
        }
        return active;
    }

    /**
     * @return the header line for the refset file of this type in RF2
     */
    public String getRF2HeaderLine() {
        return "Id" + FILE_DELIMITER + "effectiveTime" + FILE_DELIMITER
                + "active" + FILE_DELIMITER + "moduleId" + FILE_DELIMITER
                + "refSetId" + FILE_DELIMITER + "referencedComponentId"
                + FILE_DELIMITER + "conceptId";
    }

    public String formatRefsetLine(I_TermFactory tf, I_ThinExtByRefPart tuple,
            Integer memberId, int refsetId, int componentId, boolean sctId)
            throws TerminologyException, IOException {
        return getMemberId(memberId, sctId, componentId, refsetId)
                + FILE_DELIMITER + toId(tf, tuple.getPathId(), sctId)
                + FILE_DELIMITER + getDate(tf, tuple.getVersion())
                + FILE_DELIMITER + toId(tf, tuple.getStatus(), sctId)
                + FILE_DELIMITER + toId(tf, refsetId, sctId) + FILE_DELIMITER
                + toId(tf, componentId, sctId);
    }

    private String getMemberId(Integer memberId, boolean sctId,
            int componentNid, int refsetNid)
            throws UnsupportedEncodingException, TerminologyException,
            IOException {
        UUID uuid;
        if (memberId == null) {
            // generate new id
            uuid = UUID.nameUUIDFromBytes(("org.dwfa."
                    + getTermFactory().getUids(componentNid) + getTermFactory()
                    .getUids(refsetNid)).getBytes("8859_1"));
        } else {
            if (getTermFactory().getUids(memberId) == null) {
                System.out.println("Member id " + memberId
                        + " has no UUIDs!!! for refset "
                        + getTermFactory().getConcept(refsetNid)
                        + " for component "
                        + getTermFactory().getConcept(componentNid));

                uuid = UUID
                        .nameUUIDFromBytes(("org.dwfa."
                                + getTermFactory().getUids(componentNid) + getTermFactory()
                                .getUids(refsetNid)).getBytes("8859_1"));
            } else {
                uuid = getTermFactory().getUids(memberId).iterator().next();
            }
        }

        if (sctId) {
            return Long.toString(getSctGenerator().getWithGeneration(uuid,
                    TYPE.SUBSET));
        } else {
            return uuid.toString();
        }
    }

    private String getDate(I_TermFactory tf, int version) {
        return dateFormat.format(tf.convertToThickVersion(version));
    }

    protected String toId(I_TermFactory tf, int componentId, boolean sctId)
            throws TerminologyException, IOException {
        if (sctId) {
            // TODO this assumes that the componentId is a concept! it might be
            // a description or relationship
            return Long.toString(getSctGenerator().getWithGeneration(
                    tf.getUids(componentId).iterator().next(), TYPE.CONCEPT));
        } else { // uuid
            return tf.getUids(componentId).iterator().next().toString();
        }
    }

    private static synchronized UuidSnomedMapHandler getSctGenerator()
            throws IOException {
        if (sctGenerator == null) {
            sctGenerator = new UuidSnomedMapHandler(fixedMapDirectory,
                    readWriteMapDirectory);
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

    protected int getAceVersionFromDateString(String string)
            throws ParseException {
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

    protected void setGenericExtensionPartFields(I_ThinExtByRefPart part)
            throws Exception {
        part.setPathId(getNid((UUID) currentRow
                .get(MemberRefsetHandler.PATH_ID)));
        part.setStatus(getNid((UUID) currentRow
                .get(MemberRefsetHandler.STATUS_ID)));
        part.setVersion((Integer) currentRow.get(MemberRefsetHandler.VERSION));
    }

    protected I_ThinExtByRefVersioned getExtensionVersioned(String line,
            RefsetAuxiliary.Concept refsetType) throws Exception {
        Map<String, Object> currentRow = parseLine(line);

        UUID refsetUuid = (UUID) currentRow.get(MemberRefsetHandler.REFSET_ID);
        UUID componentUuid = (UUID) currentRow
                .get(MemberRefsetHandler.COMPONENT_ID);
        UUID memberUuid = (UUID) currentRow.get(MemberRefsetHandler.ID);
        int componentNid = getNid(componentUuid);
        List<I_ThinExtByRefVersioned> extensions = getTermFactory()
                .getAllExtensionsForComponent(componentNid, true);

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
            memberNid = getTermFactory().uuidToNativeWithGeneration(
                    memberUuid,
                    ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize()
                            .getNid(), getTermFactory().getPaths(),
                    Integer.MAX_VALUE);

            if (isTransactional()) {
                versioned = getTermFactory().newExtension(refsetNid, memberNid,
                        componentNid,
                        getTermFactory().uuidToNative(refsetType.getUids()));
            } else {
                versioned = getTermFactory().getDirectInterface()
                        .newExtensionBypassCommit(
                                refsetNid,
                                memberNid,
                                componentNid,
                                getTermFactory().uuidToNative(
                                        refsetType.getUids()));
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
            throw new RuntimeException(
                    "Fixed map directory can only be set once! Current value is "
                            + fixedMapDirectory
                            + " - please call MemberRefsetHandler.cleanup() to reset maps and files");
        }
        MemberRefsetHandler.fixedMapDirectory = fixedMapDirectory;
    }

    public File getReadWriteMapDirectory() {
        return readWriteMapDirectory;
    }

    public static synchronized void setReadWriteMapDirectory(
            File readWriteMapDirectory) {
        if (MemberRefsetHandler.readWriteMapDirectory != null) {
            throw new RuntimeException(
                    "Read write map directory can only be set once! Current value is "
                            + readWriteMapDirectory
                            + " - please call MemberRefsetHandler.cleanup() to reset maps and files");
        }
        MemberRefsetHandler.readWriteMapDirectory = readWriteMapDirectory;
    }

    public static synchronized void cleanup() throws IOException {
        if (sctGenerator != null) {
            sctGenerator.writeMaps();
            sctGenerator = null;
            fixedMapDirectory = null;
            readWriteMapDirectory = null;
        }
    }

    public static void setModule(ConceptDescriptor moduleDescriptor)
            throws Exception {
        module = moduleDescriptor.getVerifiedConcept();
    }

    public static I_GetConceptData getModule() {
        return module;
    }
}
