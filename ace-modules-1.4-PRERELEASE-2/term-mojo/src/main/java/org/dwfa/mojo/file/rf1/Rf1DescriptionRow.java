package org.dwfa.mojo.file.rf1;

public class Rf1DescriptionRow {
    String descriptionSctId;
    String descriptionStatus;
    String conceptSctId;
    String lanaguageCode;
    String descriptionType;
    String term;
    String initialCapitalStatus;
    private int CONCEPT_COLUMNS = 7;

    public Rf1DescriptionRow() {

    }

    public Rf1DescriptionRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. rf2 description file must have " + CONCEPT_COLUMNS + " columns");
        }

        descriptionSctId = columns[0];
        descriptionStatus = columns[1];
        conceptSctId = columns[2];
        term = columns[3];
        initialCapitalStatus = columns[4];
        descriptionType = columns[5];
        lanaguageCode = columns[6];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "DESCRIPTIONID" + "\t" + "DESCRIPTIONSTATUS" + "\t" + "CONCEPTID" + "\t" + "TERM" + "\t" + "INITIALCAPITALSTATUS" + "\t"
            + "DESCRIPTIONTYPE" + "\t" + "LANGUAGECODE";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return descriptionSctId + "\t" + descriptionStatus + "\t" + conceptSctId + "\t" + term + "\t" + initialCapitalStatus
            + "\t" + descriptionType + "\t" + lanaguageCode;
    }

    /**
     * @return the descriptionSctId
     */
    public final String getDescriptionSctId() {
        return descriptionSctId;
    }

    /**
     * @param descriptionSctId the descriptionSctId to set
     */
    public final void setDescriptionSctId(String descriptionSctId) {
        this.descriptionSctId = descriptionSctId;
    }

    /**
     * @return the descriptionStatus
     */
    public final String getDescriptionStatus() {
        return descriptionStatus;
    }

    /**
     * @param descriptionStatus the descriptionStatus to set
     */
    public final void setDescriptionStatus(String descriptionStatus) {
        this.descriptionStatus = descriptionStatus;
    }

    /**
     * @return the conceptSctId
     */
    public final String getConceptSctId() {
        return conceptSctId;
    }

    /**
     * @param conceptSctId the conceptSctId to set
     */
    public final void setConceptSctId(String conceptSctId) {
        this.conceptSctId = conceptSctId;
    }

    /**
     * @return the lanaguageCode
     */
    public final String getLanaguageCode() {
        return lanaguageCode;
    }

    /**
     * @param lanaguageCode the lanaguageCode to set
     */
    public final void setLanaguageCode(String lanaguageCode) {
        this.lanaguageCode = lanaguageCode;
    }

    /**
     * @return the descriptionType
     */
    public final String getDescriptionType() {
        return descriptionType;
    }

    /**
     * @param descriptionType the descriptionType to set
     */
    public final void setDescriptionType(String descriptionType) {
        this.descriptionType = descriptionType;
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
     * @return the initialCapitalStatus
     */
    public final String getInitialCapitalStatus() {
        return initialCapitalStatus;
    }

    /**
     * @param initialCapitalStatus the initialCapitalStatus to set
     */
    public final void setInitialCapitalStatus(String initialCapitalStatus) {
        this.initialCapitalStatus = initialCapitalStatus;
    }
}
