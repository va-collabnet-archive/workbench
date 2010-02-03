package org.dwfa.mojo.file.rf2;

public class Rf2DescriptionRow {
    String descriptionSctId;
    String effectiveTime;
    String active;
    String moduleSctId;
    String conceptSctId;
    String lanaguageCode;
    String typeSctId;
    String term;
    String caseSignificaceSctId;
    private int CONCEPT_COLUMNS = 9;

    public Rf2DescriptionRow() {

    }

    public Rf2DescriptionRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. rf2 description file must have " + CONCEPT_COLUMNS + " columns");
        }

        descriptionSctId = columns[0];
        effectiveTime = columns[1];
        active = columns[2];
        moduleSctId = columns[3];
        conceptSctId = columns[4];
        lanaguageCode = columns[5];
        typeSctId = columns[6];
        term = columns[7];
        caseSignificaceSctId = columns[8];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "id" + "\t" + "effectiveTime" + "\t" + "active" + "\t" + "moduleId" + "\t" + "conceptId" + "\t"
            + "languageCode" + "\t" + "typeId" + "\t" + "term" + "\t" + "caseSignificanceId";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return descriptionSctId + "\t" + effectiveTime + "\t" + active + "\t" + moduleSctId + "\t" + conceptSctId
            + "\t" + lanaguageCode + "\t" + typeSctId + "\t" + term + "\t" + caseSignificaceSctId;
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
     * @return the active
     */
    public final String getActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public final void setActive(String active) {
        this.active = active;
    }

    /**
     * @return the moduleSctId
     */
    public final String getModuleSctId() {
        return moduleSctId;
    }

    /**
     * @param moduleSctId the moduleSctId to set
     */
    public final void setModuleSctId(String moduleSctId) {
        this.moduleSctId = moduleSctId;
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
     * @return the typeSctId
     */
    public final String getTypeSctId() {
        return typeSctId;
    }

    /**
     * @param typeSctId the typeSctId to set
     */
    public final void setTypeSctId(String typeSctId) {
        this.typeSctId = typeSctId;
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
     * @return the caseSignificaceSctId
     */
    public final String getCaseSignificaceSctId() {
        return caseSignificaceSctId;
    }

    /**
     * @param caseSignificaceSctId the caseSignificaceSctId to set
     */
    public final void setCaseSignificaceSctId(String caseSignificaceSctId) {
        this.caseSignificaceSctId = caseSignificaceSctId;
    }
}
