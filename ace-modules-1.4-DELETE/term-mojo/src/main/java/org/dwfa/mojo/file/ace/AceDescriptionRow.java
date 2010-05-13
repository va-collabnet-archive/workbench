package org.dwfa.mojo.file.ace;

public class AceDescriptionRow {
    String descriptionUuid;
    String descriptionstatusUuid;
    String conceptUuid;
    String term;
    String initialCapitalStatusCode;
    String descriptiontypeUuid;
    String languageCode;
    String effectiveTime;
    String pathUuid;
    private int CONCEPT_COLUMNS = 9;

    public AceDescriptionRow() {

    }

    public AceDescriptionRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. Ace description file must have " + CONCEPT_COLUMNS + " columns");
        }

        descriptionUuid = columns[0];
        descriptionstatusUuid = columns[1];
        conceptUuid = columns[2];
        term = columns[3];
        initialCapitalStatusCode = columns[4];
        descriptiontypeUuid = columns[5];
        languageCode = columns[6];
        effectiveTime = columns[7];
        pathUuid = columns[8];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "DESCRIPTIONUUID" + "\t" + "DESCRIPTIONSTATUSUUID" + "\t" + "CONCEPTUUID" + "\t" + "TERM" + "\t" + "INITIALCAPITALSTATUSCODE" + "\t"
            + "DESCRIPTIONTYPEUUID" + "\t" + "LANGUAGECode" + "\t" + "EFFECTIVETIME" + "\t" + "PATHUUID";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return descriptionUuid + "\t" + descriptionstatusUuid + "\t" + conceptUuid + "\t" + term + "\t" + initialCapitalStatusCode
            + "\t" + descriptiontypeUuid + "\t" + languageCode + "\t" + effectiveTime + "\t" + pathUuid;
    }

    /**
     * @return the descriptionUuid
     */
    public final String getDescriptionUuid() {
        return descriptionUuid;
    }

    /**
     * @param descriptionUuid the descriptionUuid to set
     */
    public final void setDescriptionUuid(String descriptionUuid) {
        this.descriptionUuid = descriptionUuid;
    }

    /**
     * @return the descriptionstatusUuid
     */
    public final String getDescriptionstatusUuid() {
        return descriptionstatusUuid;
    }

    /**
     * @param descriptionstatusUuid the descriptionstatusUuid to set
     */
    public final void setDescriptionstatusUuid(String descriptionstatusUuid) {
        this.descriptionstatusUuid = descriptionstatusUuid;
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
     * @return the term
     */
    public final String getTerm() {
        return term;
    }

    /**
     * @param term the term to set
     */
    public final void setTerm(String term) {
        this.term = term;
    }

    /**
     * @return the casesensitivityUuid
     */
    public final String getInitialCapitalStatusCode() {
        return initialCapitalStatusCode;
    }

    /**
     * @param initialCapitalStatusCode the initialCapitalStatusCode to set
     */
    public final void setInitialCapitalStatusCode(String initialCapitalStatusCode) {
        this.initialCapitalStatusCode = initialCapitalStatusCode;
    }

    /**
     * @return the descriptiontypeUuid
     */
    public final String getDescriptiontypeUuid() {
        return descriptiontypeUuid;
    }

    /**
     * @param descriptiontypeUuid the descriptiontypeUuid to set
     */
    public final void setDescriptiontypeUuid(String descriptiontypeUuid) {
        this.descriptiontypeUuid = descriptiontypeUuid;
    }

    /**
     * @return the languageUuid
     */
    public final String getLanguageCode() {
        return languageCode;
    }

    /**
     * @param languageCode the languageCode to set
     */
    public final void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
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
