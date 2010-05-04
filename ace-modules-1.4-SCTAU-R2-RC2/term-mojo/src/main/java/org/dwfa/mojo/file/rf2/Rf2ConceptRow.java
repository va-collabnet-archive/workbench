package org.dwfa.mojo.file.rf2;

public class Rf2ConceptRow {
    /** Default column delimiter. */
    String COLUMN_DELIMITER = "\t";

    String conceptSctId;
    String effectiveTime;
    String active;
    String moduleSctId;
    String definiationStatusSctId;

    private int CONCEPT_COLUMNS = 5;

    public Rf2ConceptRow() {
    }

    public Rf2ConceptRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS) {
            throw new Exception("Invalid file format. rf2 concept file must have " + CONCEPT_COLUMNS + " columns");
        }

        conceptSctId = columns[0];
        effectiveTime = columns[1];
        active = columns[2];
        moduleSctId = columns[3];
        definiationStatusSctId = columns[4];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "id" + "\t" + "effectiveTime" + "\t" + "active" + "\t" + "moduleId" + "\t" + "definitionStatusId";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return conceptSctId + "\t" + effectiveTime + "\t" + active + "\t" + moduleSctId + "\t" + definiationStatusSctId;
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
     * @return the definiationStatus
     */
    public final String getDefiniationStatusSctId() {
        return definiationStatusSctId;
    }

    /**
     * @param definiationStatus the definiationStatus to set
     */
    public final void setDefiniationStatusSctId(String definiationStatusSctId) {
        this.definiationStatusSctId = definiationStatusSctId;
    }
}