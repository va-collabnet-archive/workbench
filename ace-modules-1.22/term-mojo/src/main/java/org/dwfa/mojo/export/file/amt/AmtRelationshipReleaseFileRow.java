package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;
import java.util.Map.Entry;
import java.util.UUID;
import org.dwfa.dto.RelationshipDto;

/**
 * The {@code AmtRelationshipReleaseFileRow} class holds all the necessary information for writing AMT Relationships to
 * Release Files.
 * @author Matthew Edwards
 */
public final class AmtRelationshipReleaseFileRow implements ReleaseFileRow {

    /** The Title of the Relationship ID Column.*/
    private static final String RELID_HEADER = "RELATIONSHIPID";
    /** The Title of the Concept ID 1 Column.*/
    private static final String CONCEPTID1_HEADER = "CONCEPTID1";
    /** The Title of the Relationship Type Column.*/
    private static final String RELTYPE_HEADER = "RELATIONSHIPTYPE";
    /** The Title of the Concept ID 2 Column.*/
    private static final String CONCEPTID2_HEADER = "CONCEPTID2";
    /** The Title of the Characteristic Type Column.*/
    private static final String CHARACTERISTICTYPE_HEADER = "CHARACTERISTICTYPE";
    /** The Title of the Refinability Column.*/
    private static final String REFINABILITY_HEADER = "REFINABILITY";
    /** The Title of the Relationship Group Column.*/
    private static final String RELGROUP_HEADER = "RELATIONSHIPGROUP";
    /** The Title of the Relationship UUID Column.*/
    private static final String RELUUID_HEADER = "RELATIONSHIPUUID";
    /** The Title of the Concept 1 UUID Column.*/
    private static final String CONCEPTUUID1_HEADER = "CONCEPTUUID1";
    /** The Title of the Relationship Type UUID Column.*/
    private static final String RELTYPEUUID_HEADER = "RELATIONSHIPTYPEUUID";
    /** The Title of the Concept 2 UUID Column.*/
    private static final String CONCEPTUUID2_HEADER = "CONCEPTUUID2";
    /** The Title of the Characteristic Type UUID Column.*/
    private static final String CHARACTERISTICTYPEUUID_HEADER = "CHARACTERISTICTYPEUUID";
    /** The Title of the Refinability UUID Column.*/
    private static final String REFINABILITYUUID_HEADER = "REFINABILITYUUID";
    /** The Title of the Relationship Status UUID Column.*/
    private static final String RELSTATUSUUID_HEADER = "RELATIONSHIPSTATUSUUID";
    /** The Title of the Effective Time Column.*/
    private static final String EFFECTIVETIME_HEADER = "EFFECTIVETIME";
    /** The Default Effective Time for an AMT concept. */
    private static final String DEFAULT_EFFECTIVE_TIME = "20070928 00:00:00";
    /** The String format for each Row. (15 tab separated columns). */
    private static final String ROW_FORMAT = "%1$s\t%2$s\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\t%9$s\t%10$s\t%11$s\t%12$s\t%13$s\t%14$s\t%15$s";
    /** The value of the Header Row. */
    public static final String HEADER_ROW = String.format(ROW_FORMAT,
            RELID_HEADER, CONCEPTID1_HEADER, RELTYPE_HEADER, CONCEPTID2_HEADER, CHARACTERISTICTYPE_HEADER,
            REFINABILITY_HEADER, RELGROUP_HEADER, RELUUID_HEADER, CONCEPTUUID1_HEADER, RELTYPEUUID_HEADER,
            CONCEPTUUID2_HEADER, CHARACTERISTICTYPEUUID_HEADER, REFINABILITYUUID_HEADER, RELSTATUSUUID_HEADER,
            EFFECTIVETIME_HEADER);
    /** The final Release Formatted String for a Concept. */
    private final transient String outputRow;

    /**
     * Constructs an instance of {@code AmtRelationshipReleaseFileRow} for writing an AMT Relationship to a
     * relationships release file in the AMT Release File Format, using the default effective time of
     * {@code 20070928 00:00:00}.
     * @param relDto The Relationship DTO containing all the information for this Relationship Row.
     */
    public AmtRelationshipReleaseFileRow(final RelationshipDto relDto) {
        final Entry<UUID, Long> relationshipInfo = relDto.getConceptId().entrySet().iterator().next();
        final Entry<UUID, Long> destinationInfo = relDto.getDestinationId().entrySet().iterator().next();
        final Entry<UUID, Long> sourceInfo = relDto.getSourceIdMap().entrySet().iterator().next();
        final Entry<UUID, Long> relTypeInfo = relDto.getRelTypeMap().entrySet().iterator().next();

        final String relationshipID = relationshipInfo.getValue().toString();
        final String conceptId1 = sourceInfo.getValue().toString();
        final String relType = relTypeInfo.getValue().toString();
        final String conceptId2 = destinationInfo.getValue().toString();
        final String characteristicType = relDto.getCharacteristicTypeCode().toString();
        final String refinabilityType = relDto.getRefinable().toString();
        final String relGroup = relDto.getRelationshipGroup().toString();
        final String relUUID = relationshipInfo.getKey().toString();
        final String concept1Uuid = sourceInfo.getKey().toString();
        final String relTypeUuid = relTypeInfo.getKey().toString();
        final String concept2Uuid = destinationInfo.getKey().toString();
        final String characteristicTypeUuid = relDto.getCharacteristicTypeId().toString();
        final String refinabilityUuid = relDto.getRefinabilityId().toString();
        final String relStatusUuid = relDto.getStatusId().toString();

        this.outputRow = String.format(ROW_FORMAT, relationshipID, conceptId1, relType, conceptId2, characteristicType,
                refinabilityType, relGroup, relUUID,
                concept1Uuid, relTypeUuid,
                concept2Uuid, characteristicTypeUuid, refinabilityUuid, relStatusUuid, DEFAULT_EFFECTIVE_TIME);
    }

    public AmtRelationshipReleaseFileRow(final String relationshipID, final String conceptId1, final String relType,
            final String conceptId2,
            final String characteristicType, final String refinabilityType, final String relGroup, final String relUUID,
            final String concept1Uuid, final String relTypeUuid, final String concept2Uuid,
            final String characteristicTypeUuid,
            final String refinabilityUuid, final String relStatusUuid) {
        this(relationshipID, conceptId1, relType, conceptId2, characteristicType, refinabilityType, relGroup, relUUID,
                concept1Uuid, relTypeUuid,
                concept2Uuid, characteristicTypeUuid, refinabilityUuid, relStatusUuid, DEFAULT_EFFECTIVE_TIME);
    }

    public AmtRelationshipReleaseFileRow(final String relationshipID, final String conceptId1, final String relType,
            final String conceptId2,
            final String characteristicType, final String refinabilityType, final String relGroup, final String relUUID,
            final String concept1Uuid, final String relTypeUuid, final String concept2Uuid,
            final String characteristicTypeUuid,
            final String refinabilityUuid, final String relStatusUuid, final String effectiveTime) {
        this.outputRow = String.format(ROW_FORMAT, relationshipID,
                conceptId1, relType, conceptId2, characteristicType, refinabilityType, relGroup, relUUID, concept1Uuid,
                relTypeUuid, concept2Uuid, characteristicTypeUuid, refinabilityUuid, relStatusUuid, effectiveTime);
    }

    @Override
    public String getOutputRow() {
        return this.outputRow;
    }
}
