package org.dwfa.mojo.file.ace;

public class AceConceptRow {
    /** Default column delimiter. */
    String COLUMN_DELIMITER = "\t";

    String conceptUuid;
    String conceptStatusUuid;
    String isPrimitve;
    String effectiveTime;
    String pathUuid;

    private int CONCEPT_COLUMNS = 5;

    public AceConceptRow() {
    }

    public AceConceptRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS) {
            throw new Exception("Invalid file format. Ace concept file must have " + CONCEPT_COLUMNS + " columns");
        }

        conceptUuid = columns[0];
        conceptStatusUuid = columns[1];
        isPrimitve = columns[2];
        effectiveTime = columns[3];
        pathUuid = columns[4];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "CONCEPTUUID" + "\t" + "CONCEPTSTATUSUUID" + "\t" + "ISPRIMITVE" + "\t" + "EFFECTIVETIME" + "\t" + "PATHUUID";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return conceptUuid + "\t" + conceptStatusUuid + "\t" + isPrimitve + "\t" + effectiveTime + "\t" + pathUuid;
    }

    /**
     * @return the conceptUuid
     */
    public final String getConceptUuid() {
        return conceptUuid;
    }

    /**
     * @param conceptUuid the conceptUuid to set
     */
    public final void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    /**
     * @return the conceptStatusUuid
     */
    public final String getConceptStatusUuid() {
        return conceptStatusUuid;
    }

    /**
     * @param conceptStatusUuid the conceptStatusUuid to set
     */
    public final void setConceptStatusUuid(String conceptStatusUuid) {
        this.conceptStatusUuid = conceptStatusUuid;
    }

    /**
     * @return the isPrimitve
     */
    public final String getIsPrimitve() {
        return isPrimitve;
    }

    /**
     * @param isPrimitve the isPrimitve to set
     */
    public final void setIsPrimitve(String isPrimitve) {
        this.isPrimitve = isPrimitve;
    }

    /**
     * @return the effectiveTime
     */
    public final String getEffectiveTime() {
        return effectiveTime;
    }

    /**
     * @param effectiveTime the effectiveTime to set
     */
    public final void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
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
