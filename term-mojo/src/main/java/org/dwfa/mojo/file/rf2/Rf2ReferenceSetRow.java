package org.dwfa.mojo.file.rf2;

import org.dwfa.maven.sctid.SctIdValidator;

public class Rf2ReferenceSetRow {
    /** Default column delimiter. */
    String COLUMN_DELIMITER = "\t";

    String memberId;
    String effectiveTime;
    String active;
    String moduleId;
    String refsetId;
    String referencedComponentId;
    String referencedComponentId2;
    String referencedComponentId3;
    String value;

    private int CONCEPT_COLUMNS = 6;

    public Rf2ReferenceSetRow() {

    }

    public Rf2ReferenceSetRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length < CONCEPT_COLUMNS  ) {
            throw new Exception("Invalid file format. rf2 reference set file must have atlest" + CONCEPT_COLUMNS + " columns");
        }

        memberId = columns[0];
        effectiveTime = columns[1];
        active = columns[2];
        moduleId = columns[3];
        refsetId = columns[4];
        referencedComponentId = columns[5];

        for(int i = 6; i <= columns.length - 1; i++ ){
            if(isSctId(columns[i])){
                switch(i) {
                case 6 :
                    referencedComponentId2 = columns[i];
                    break;
                case 7 :
                    referencedComponentId3 = columns[i];
                    break;
                case 8 :
                    value = columns[i];
                    break;
                }
            } else {
                value = columns[i];
            }
        }
    }

    private boolean isSctId(String sctId) {
        boolean isSctId = true;

        try{
            SctIdValidator.getInstance().isValidSctId(sctId);
        } catch (NumberFormatException nfe) {
            isSctId = false;
        }

        return isSctId;
    }
    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "id" + COLUMN_DELIMITER + "effectiveTime" + COLUMN_DELIMITER + "active" + COLUMN_DELIMITER + "moduleId"
            + COLUMN_DELIMITER + "refSetId" + COLUMN_DELIMITER + "referencedComponentId"
            + ((referencedComponentId2 == null) ? "" : COLUMN_DELIMITER + "referencedComponentId2")
            + ((referencedComponentId3 == null) ? "" : COLUMN_DELIMITER + "referencedComponentId3")
            + ((value == null) ? "" : COLUMN_DELIMITER + "valueId");
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return memberId + COLUMN_DELIMITER + effectiveTime + COLUMN_DELIMITER + active + COLUMN_DELIMITER + moduleId
            + COLUMN_DELIMITER + refsetId + COLUMN_DELIMITER + referencedComponentId + COLUMN_DELIMITER
            + ((referencedComponentId2 == null) ? "" : referencedComponentId2) + COLUMN_DELIMITER
            + ((referencedComponentId3 == null) ? "" : referencedComponentId3) + COLUMN_DELIMITER
            + ((value == null) ? "" : value);
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
     * @return the memberId
     */
    public String getMemberId() {
        return memberId;
    }

    /**
     * @param memberId the memberId to set
     */
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    /**
     * @return the moduleId
     */
    public String getModuleId() {
        return moduleId;
    }

    /**
     * @param moduleId the moduleId to set
     */
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    /**
     * @return the refsetId
     */
    public String getRefsetId() {
        return refsetId;
    }

    /**
     * @param refsetId the refsetId to set
     */
    public void setRefsetId(String refsetId) {
        this.refsetId = refsetId;
    }

    /**
     * @return the referencedComponentId
     */
    public String getReferencedComponentId() {
        return referencedComponentId;
    }

    /**
     * @param referencedComponentId the referencedComponentId to set
     */
    public void setReferencedComponentId(String referencedComponentId) {
        this.referencedComponentId = referencedComponentId;
    }

    /**
     * @return the referencedComponentId2
     */
    public String getReferencedComponentId2() {
        return referencedComponentId2;
    }

    /**
     * @param referencedComponentId2 the referencedComponentId2 to set
     */
    public void setReferencedComponentId2(String referencedComponentId2) {
        this.referencedComponentId2 = referencedComponentId2;
    }

    /**
     * @return the referencedComponentId3
     */
    public String getReferencedComponentId3() {
        return referencedComponentId3;
    }

    /**
     * @param referencedComponentId3 the referencedComponentId3 to set
     */
    public void setReferencedComponentId3(String referencedComponentId3) {
        this.referencedComponentId3 = referencedComponentId3;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
