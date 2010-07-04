package org.dwfa.mojo.file.ace;

import java.util.UUID;

import org.dwfa.ace.file.GenericFileWriter;

public class AceIdentifierRow extends GenericFileWriter<AceIdentifierRow> {
    public static UUID SCT_ID_IDENTIFIER_SCHEME = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");

    private static final int CONCEPT_COLUMNS = 6;
    String primaryUuid;
    String sourceSystemUuid;
    String sourceId;
    String statusUuid;
    String effectiveDate;
    String pathUuid;

    public AceIdentifierRow() {

    }

    public AceIdentifierRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS) {
            throw new Exception("Invalid file format. rf2 concept file must have " + CONCEPT_COLUMNS + " columns");
        }

        primaryUuid = columns[0];
        sourceSystemUuid = columns[1];
        sourceId = columns[2];
        statusUuid = columns[3];
        effectiveDate = columns[4];
        pathUuid = columns[5];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "PRIMARY_UUID" + "\t" + "SOURCE_SYSTEM_UUID" + "\t" + "SOURCE_ID" + "\t" + "STATUS_UUID" + "\t"
            + "EFFECTIVE_DATE" + "\t" + "PATH_UUID";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return primaryUuid + "\t" + sourceSystemUuid + "\t" + sourceId + "\t" + statusUuid + "\t" + effectiveDate
            + "\t" + pathUuid;
    }

    /**
     * @return the primaryUuid
     */
    public final String getPrimaryUuid() {
        return primaryUuid;
    }

    /**
     * @param primaryUuid the primaryUuid to set
     */
    public final void setPrimaryUuid(String primaryUuid) {
        this.primaryUuid = primaryUuid;
    }

    /**
     * @return the sourceSystemUuid
     */
    public final String getSourceSystemUuid() {
        return sourceSystemUuid;
    }

    /**
     * @param sourceSystemUuid the sourceSystemUuid to set
     */
    public final void setSourceSystemUuid(String sourceSystemUuid) {
        this.sourceSystemUuid = sourceSystemUuid;
    }

    /**
     * @return the sourceId
     */
    public final String getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public final void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the statusUuid
     */
    public final String getStatusUuid() {
        return statusUuid;
    }

    /**
     * @param statusUuid the statusUuid to set
     */
    public final void setStatusUuid(String statusUuid) {
        this.statusUuid = statusUuid;
    }

    /**
     * @return the effectiveDate
     */
    public final String getEffectiveDate() {
        return effectiveDate;
    }

    /**
     * @param effectiveDate the effectiveDate to set
     */
    public final void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * @return the pathUuid
     */
    public final String getPathUuid() {
        return pathUuid;
    }

    /**
     * @param pathUuid the pathUuid to set
     */
    public final void setPathUuid(String pathUuid) {
        this.pathUuid = pathUuid;
    }
}