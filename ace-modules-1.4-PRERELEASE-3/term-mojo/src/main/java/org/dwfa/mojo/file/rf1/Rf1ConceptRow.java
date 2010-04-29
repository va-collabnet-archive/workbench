package org.dwfa.mojo.file.rf1;

public class Rf1ConceptRow {
    /** Default column delimiter. */
    String COLUMN_DELIMITER = "\t";

    String conceptSctId;
    String conceptStatus;
    String fullySpecifiedName;
    String ctv3Id;
    String snomedId;
    String isPrimitve;

    private int CONCEPT_COLUMNS = 6;

    public Rf1ConceptRow() {
    }

    public Rf1ConceptRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS) {
            throw new Exception("Invalid file format. rf1 concept file must have " + CONCEPT_COLUMNS + " columns");
        }

        conceptSctId = columns[0];
        conceptStatus = columns[1];
        fullySpecifiedName = columns[2];
        ctv3Id = columns[3];
        snomedId = columns[4];
        isPrimitve = columns[5];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "CONCEPTID" + "\t" + "CONCEPTSTATUS" + "\t" + "FULLYSPECIFIEDNAME" + "\t" + "CTV3ID" + "\t" + "SNOMEDID" + "\t" + "ISPRIMITVE";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return conceptSctId + "\t" + conceptStatus + "\t" + fullySpecifiedName + "\t" + ctv3Id + "\t" + snomedId + "\t" + isPrimitve;
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
     * @return the conceptStatus
     */
    public final String getConceptStatus() {
        return conceptStatus;
    }

    /**
     * @param conceptStatus the conceptStatus to set
     */
    public final void setConceptStatus(String conceptStatus) {
        this.conceptStatus = conceptStatus;
    }

    /**
     * @return the fullySpecifiedName
     */
    public final String getFullySpecifiedName() {
        return fullySpecifiedName;
    }

    /**
     * @param fullySpecifiedName the fullySpecifiedName to set
     */
    public final void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
    }

    /**
     * @return the ctv3Id
     */
    public final String getCtv3Id() {
        return ctv3Id;
    }

    /**
     * @param ctv3Id the ctv3Id to set
     */
    public final void setCtv3Id(String ctv3Id) {
        this.ctv3Id = ctv3Id;
    }

    /**
     * @return the snomedId
     */
    public final String getSnomedId() {
        return snomedId;
    }

    /**
     * @param snomedId the snomedId to set
     */
    public final void setSnomedId(String snomedId) {
        this.snomedId = snomedId;
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
}
