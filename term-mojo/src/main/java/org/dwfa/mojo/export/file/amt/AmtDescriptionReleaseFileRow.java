package org.dwfa.mojo.export.file.amt;

import org.dwfa.mojo.file.ReleaseFileRow;
import java.util.Map.Entry;
import java.util.UUID;
import org.dwfa.dto.DescriptionDto;

/**
 * The {@code AmtDescriptionReleaseFileRow} class holds all the necessary information for writing AMT Descriptions to
 * Release Files.
 * @author Matthew Edwards
 */
public final class AmtDescriptionReleaseFileRow implements ReleaseFileRow {

    /** The Title of Description Id Column.*/
    private static final String DESC_ID_HEADER = "DESCRIPTIONID";
    /** The Title of the Description Status Column. */
    private static final String DESC_STAT_HEADER = "DESCRIPTIONSTATUS";
    /** The Title of the Concept Id Column. */
    private static final String CONCEPTID_HEADER = "CONCEPTID";
    /** The Title of the Term (Description) Column. */
    private static final String TERM_HEADER = "TERM";
    /** The Title of the Initial Capital Status Column. */
    private static final String INITIAL_CAP_STAT_HEADER = "INITIALCAPITALSTATUS";
    /** The Title of the Description Type Column. */
    private static final String DESC_TYPE_HEADER = "DESCRIPTIONTYPE";
    /** The Title of the Language Code Column. */
    private static final String LANG_CODE_HEADER = "LANGUAGECODE";
    /** The Title of the Description UUID Column. */
    private static final String DESC_UUID_HEADER = "DESCRIPTIONUUID";
    /** The Title of the Whatever that is Column. */
    private static final String DESCRIPTIONSTATUSUUID_HEADER = "DESCRIPTIONSTATUSUUID";
    /** The title of the Description Type UUID Column. */
    private static final String DESCTYPEUUID_HEADER = "DESCRIPTIONTYPEUUID";
    /** The Title of the Concept UUID Column. */
    private static final String CONCEPTUUID_HEADER = "CONCEPTUUID";
    /** The Title of the Language UUID Column. */
    private static final String LANGUUID_HEAD = "LANGUAGEUUID";
    /** The Title of the Case Sensitivity Column. */
    private static final String CASE_SENS_HEADER = "CASESENSITIVITY";
    /** The Title of the Effective Time Column.*/
    private static final String EFFECTIVETIME_HEADER = "EFFECTIVETIME";
    /** The String format for each Row. (14 tab separated columns). */
    private static final String ROW_FORMAT = "%1$s\t%2$s\t%3$s\t%4$s\t%5$s\t%6$s\t%7$s\t%8$s\t%9$s\t%10$s\t%11$s\t%12$s\t%13$s\t%14$s";
    /** The value of the Header Row. */
    public static final String HEADER_ROW = String.format(ROW_FORMAT,
            DESC_ID_HEADER, DESC_STAT_HEADER, CONCEPTID_HEADER, TERM_HEADER, INITIAL_CAP_STAT_HEADER, DESC_TYPE_HEADER,
            LANG_CODE_HEADER, DESC_UUID_HEADER, DESCRIPTIONSTATUSUUID_HEADER, DESCTYPEUUID_HEADER, CONCEPTUUID_HEADER,
            LANGUUID_HEAD, CASE_SENS_HEADER, EFFECTIVETIME_HEADER);
    /** The Default Effective Time for an AMT concept. */
    private static final String DEFAULT_EFFECTIVE_TIME = "20070928 00:00:00";
    /** The Default Case Sensitivity Flag  for an AMT description. */
    private static final String DEFAULT_CASE_SENSITIVITY = "1";
    /** The final Release Formatted String for a Concept. */
    private final transient String outputRow;

    /**
     * Constructs an instance of {@code AmtDescriptionReleaseFileRow} for writing an AMT Description to a descriptions
     * release file in the AMT Release File Format.
     * @param descId the Description Id.
     * @param descStatus the description Status.
     * @param conceptId the concept Id.
     * @param term the description or term for this description row.
     * @param initCapStat the initial capital status.
     * @param descType the description type.
     * @param langCode the language code.
     * @param descUuid the description UUID.
     * @param descStatUuid the descriptions Status UUID.
     * @param descTypeUuid the description Type UUID.
     * @param conceptUuid the concept UUID.
     * @param langUuid the language UUID.
     * @param caseSensitivity the case sensitivity status flag.
     * @param effectiveTime The time value denoting when this concept is effective from.
     */
    public AmtDescriptionReleaseFileRow(final String descId, final String descStatus, final String conceptId,
            final String term, final String initCapStat, final String descType, final String langCode,
            final String descUuid, final String descStatUuid, final String descTypeUuid, final String conceptUuid,
            final String langUuid, final String caseSensitivity, final String effectiveTime) {
        this.outputRow = String.format(ROW_FORMAT, descId, descStatus, conceptId, term, initCapStat, descType, langCode,
                descUuid, descStatUuid, descTypeUuid, conceptUuid, langUuid, caseSensitivity, effectiveTime);
    }

    /**
     * Constructs an instance of {@code AmtDescriptionReleaseFileRow} for writing an AMT Description to a descriptions
     * release file in the AMT Release File Format with the Default Effective Time.
     * @param descId the Description Id.
     * @param descStatus the description Status.
     * @param conceptId the concept Id.
     * @param term the description or term for this description row.
     * @param initCapStat the initial capital status.
     * @param descType the description type.
     * @param langCode the language code.
     * @param descUuid the description UUID.
     * @param descStatUuid the descriptions Status UUID.
     * @param descTypeUuid the description Type UUID.
     * @param conceptUuid the concept UUID.
     * @param langUuid the language UUID.
     * @param caseSensitivity the case sensitivity status flag.
     */
    public AmtDescriptionReleaseFileRow(final String descId, final String descStatus, final String conceptId,
            final String term, final String initCapStat, final String descType, final String langCode,
            final String descUuid, final String descStatUuid, final String descTypeUuid, final String conceptUuid,
            final String langUuid, final String caseSensitivity) {
        this(descId, descStatus, conceptId, term, initCapStat, descType, langCode, descUuid, descStatUuid, descTypeUuid,
                conceptUuid, langUuid, caseSensitivity, DEFAULT_EFFECTIVE_TIME);
    }

    /**
     * Constructs an instance of {@code AmtDescriptionReleaseFileRow} for writing an AMT Description to a descriptions
     * release file in the AMT Release File Format with the Default Effective Time, and Default case sensitivity flag.
     * @param descriptionDto The Description DTO containing all the information for this Description Row.
     */
    public AmtDescriptionReleaseFileRow(final DescriptionDto descriptionDto) {
        final Entry<UUID, Long> idMapEntry = descriptionDto.getConceptId().entrySet().iterator().next();
        final Long conceptId = idMapEntry.getValue();
        final UUID conceptUuid = idMapEntry.getKey();

        final String descId = descriptionDto.getSnomedId();
        final String descStatus = descriptionDto.getStatusCode();
        final String conceptIdString = conceptId.toString();
        final String term = descriptionDto.getDescription();
        final Character initCapStatus = descriptionDto.getInitialCapitalStatusCode();
        final Character descType = descriptionDto.getDescriptionTypeCode();
        final String langCode = descriptionDto.getLanguageCode();
        final String descUuid = descriptionDto.getDescriptionId().toString();
        final String descStatusUuid = descriptionDto.getStatusId().toString();
        final String descTypeUuid = descriptionDto.getTypeId().toString();
        final String conceptUuidString = conceptUuid.toString();
        final String langUuid = descriptionDto.getLanguageId().toString();

        this.outputRow = String.format(ROW_FORMAT,
                descId,
                descStatus,
                conceptIdString,
                term,
                initCapStatus,
                descType,
                langCode,
                descUuid,
                descStatusUuid,
                descTypeUuid,
                conceptUuidString,
                langUuid,
                DEFAULT_CASE_SENSITIVITY,
                DEFAULT_EFFECTIVE_TIME);
    }

    @Override
    public String getOutputRow() {
        return this.outputRow;
    }
}
