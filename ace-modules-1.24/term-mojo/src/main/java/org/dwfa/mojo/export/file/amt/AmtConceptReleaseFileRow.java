package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;
import java.util.Map.Entry;
import java.util.UUID;
import org.dwfa.dto.ConceptDto;

/**
 * The {@code AmtConceptReleaseFileRow} class holds all the necessary information for writing AMT Concepts to Release
 * Files.
 * @author Matthew Edwards
 */
public final class AmtConceptReleaseFileRow implements ReleaseFileRow {

    /** The Title of the Concept ID Column.*/
    private static final String CONCEPTID_HEADER = "CONCEPTID";
    /** The Title of the Concept Status Column.*/
    private static final String CONCEPTSTATUS_HEADER = "CONCEPTSTATUS";
    /** The Title of the Fully Specified Name Column.*/
    private static final String FSN_HEADER = "FULLYSPECIFIEDNAME";
    /** The Title of the CTV3ID Column.*/
    private static final String CTV3ID_HEADER = "CTV3ID";
    /** The Title of the Snomed Id Column.*/
    private static final String SNOMEDID_HEADER = "SNOMEDID";
    /** The Title of the Is Primitive Column.*/
    private static final String ISPRIMITIVE_HEADER = "ISPRIMITIVE";
    /** The Title of the Concept Uuid Column.*/
    private static final String CONCEPTUUID_HEADER = "CONCEPTUUID";
    /** The Title of the Concept Status Uuid Column.*/
    private static final String CONCEPTSTATUSUUID_HEADER = "CONCEPTSTATUSUUID";
    /** The Title of the Effective Time Column.*/
    private static final String EFFECTIVETIME_HEADER = "EFFECTIVETIME";
    /** The String format for each Row. (9 tab separated columns). */
    private static final String ROW_FORMAT = "%1$s\t%2$s\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\t%9$s";
    /** The value of the Header Row. */
    public static final String HEADER_ROW = String.format(ROW_FORMAT,
            CONCEPTID_HEADER, CONCEPTSTATUS_HEADER, FSN_HEADER, CTV3ID_HEADER, SNOMEDID_HEADER, ISPRIMITIVE_HEADER,
            CONCEPTUUID_HEADER, CONCEPTSTATUSUUID_HEADER, EFFECTIVETIME_HEADER);
    /** The Default Effective Time for an AMT concept. */
    private static final String DEFAULT_EFFECTIVE_TIME = "20070928 00:00:00";
    /** The final Release Formatted String for a Concept. */
    private final transient String outputRow;

    /**
     * Constructs an instance of {@code AmtConceptReleaseFileRow} for writing an AMT concept to a concepts release file
     * in the AMT Release File Format, using the default effective time of {@code 20070928 00:00:00}, and no value for
     * ctv3Id or snomedId.
     * @param conceptId The id of the concept for this row.
     * @param conceptStatus The Concept Status for this row.
     * @param fsn The fsn String for this Row.
     * @param isPrimitiveFlag A binary (0 or 1) indicating whether this is a primitive concept.
     * @param conceptUuid The Uuid of this concept.
     * @param conceptStatusUuid The Uuid of this concepts status.
     */
    public AmtConceptReleaseFileRow(final String conceptId, final String conceptStatus, final String fsn,
            final String isPrimitiveFlag, final String conceptUuid,
            final String conceptStatusUuid) {
        this(conceptId, conceptStatus, fsn, "", "", isPrimitiveFlag, conceptUuid, conceptStatusUuid,
                DEFAULT_EFFECTIVE_TIME);
    }

    /**
     * Constructs an instance of {@code AmtConceptReleaseFileRow} for writing an AMT concept to a concepts release file
     * in the AMT Release File Format.
     * @param conceptId The id of the concept for this row.
     * @param conceptStatus The Concept Status for this row.
     * @param fsn The fsn String for this Row.
     * @param ctv3Id The ctv3Id for this row.
     * @param snomedId The snomed id for this row.
     * @param isPrimitiveFlag A binary (0 or 1) indicating whether this is a primitive concept.
     * @param conceptUuid The Uuid of this concept.
     * @param conceptStatusUuid The Uuid of this concepts status.
     * @param effectiveTime The time value denoting when this concept is effective from.
     */
    public AmtConceptReleaseFileRow(final String conceptId, final String conceptStatus, final String fsn,
            final String ctv3Id, final String snomedId, final String isPrimitiveFlag, final String conceptUuid,
            final String conceptStatusUuid, final String effectiveTime) {
        this.outputRow = String.format(ROW_FORMAT, conceptId, conceptStatus, fsn, ctv3Id, snomedId, isPrimitiveFlag,
                conceptUuid, conceptStatusUuid, effectiveTime);
    }

    /**
     * Constructs an instance of {@code AmtConceptReleaseFileRow} for writing an AMT concept to a concepts release file
     * in the AMT Release File Format, using the default effective time of {@code 20070928 00:00:00}, and no value for
     * ctv3Id or snomedId.
     * @param conceptDto The concept DTO containing all the information for this Concept Row.
     */
    public AmtConceptReleaseFileRow(final ConceptDto conceptDto) {
        final Entry<UUID, Long> idMapEntry = conceptDto.getConceptId().entrySet().iterator().next();
        final Long conceptId = idMapEntry.getValue();
        final UUID conceptUuid = idMapEntry.getKey();

        final String conceptIdString = conceptId.toString();
        final String conceptStatus = conceptDto.getStatusCode();
        final String fsn = conceptDto.getFullySpecifiedName();
        final String isPrimitive = conceptDto.isPrimative() ? "1" : "0";
        final String conceptUuidString = conceptUuid.toString();
        final String conceptStatusUUID = conceptDto.getStatusId().toString();

        this.outputRow = String.format(ROW_FORMAT, conceptIdString, conceptStatus,
                fsn, "", "", isPrimitive, conceptUuidString,
                conceptStatusUUID, DEFAULT_EFFECTIVE_TIME);
    }

    @Override
    public String getOutputRow() {
        return this.outputRow;
    }
}
