package org.dwfa.mojo.file.ace;

public class AceRelationshipRow {
    String relationshipUuid;
    String relationshipstatusUuid;
    String conceptUuid1;
    String relationshiptypeUuid;
    String conceptUuid2;
    String characteristicTypeUuid;
    String refinabilityUuid;
    String relationshipGroup;
    String effectiveTime;
    String pathUuid;
    private int CONCEPT_COLUMNS = 10;

    public AceRelationshipRow() {

    }

    public AceRelationshipRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. Ace relationship file must have " + CONCEPT_COLUMNS + " columns");
        }

        relationshipUuid = columns[0];
        relationshipstatusUuid = columns[1];
        conceptUuid1 = columns[2];
        relationshiptypeUuid = columns[3];
        conceptUuid2 = columns[4];
        characteristicTypeUuid = columns[5];
        refinabilityUuid = columns[6];
        relationshipGroup = columns[7];
        effectiveTime = columns[8];
        pathUuid = columns[9];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "RELATIONSHIP_UUID" + "\t" + "RELATIONSHIPSTATUS_UUID" + "\t" + "CONCEPT_UUID_1" + "\t"
            + "RELATIONSHIPTYPE_UUID" + "\t" + "CONCEPT_UUID_2" + "\t" + "CHARACTERISTIC_TYPE_UUID" + "\t"
            + "REFINABILITY_UUID" + "\t" + "RELATIONSHIP_GROUP" + "\t" + "EFFECTIVE_TIME" + "\t" + "PATH_UUID";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return relationshipUuid + "\t" + relationshipstatusUuid + "\t" + conceptUuid1 + "\t" + relationshiptypeUuid
            + "\t" + conceptUuid2 + "\t" + characteristicTypeUuid + "\t" + refinabilityUuid + "\t" + relationshipGroup
            + "\t" + effectiveTime + "\t" + pathUuid;
    }

    /**
     * @return the relationshipUuid
     */
    public final String getRelationshipUuid() {
        return relationshipUuid;
    }

    /**
     * @param relationshipUuid the relationshipUuid to set
     */
    public final void setRelationshipUuid(String relationshipUuid) {
        this.relationshipUuid = relationshipUuid;
    }

    /**
     * @return the relationshipstatusUuid
     */
    public final String getRelationshipstatusUuid() {
        return relationshipstatusUuid;
    }

    /**
     * @param relationshipstatusUuid the relationshipstatusUuid to set
     */
    public final void setRelationshipstatusUuid(String relationshipstatusUuid) {
        this.relationshipstatusUuid = relationshipstatusUuid;
    }

    /**
     * @return the conceptUuid1
     */
    public final String getConceptUuid1() {
        return conceptUuid1;
    }

    /**
     * @param conceptUuid1 the conceptUuid1 to set
     */
    public final void setConceptUuid1(String conceptUuid1) {
        this.conceptUuid1 = conceptUuid1;
    }

    /**
     * @return the relationshiptypeUuid
     */
    public final String getRelationshiptypeUuid() {
        return relationshiptypeUuid;
    }

    /**
     * @param relationshiptypeUuid the relationshiptypeUuid to set
     */
    public final void setRelationshiptypeUuid(String relationshiptypeUuid) {
        this.relationshiptypeUuid = relationshiptypeUuid;
    }

    /**
     * @return the conceptUuid2
     */
    public final String getConceptUuid2() {
        return conceptUuid2;
    }

    /**
     * @param conceptUuid2 the conceptUuid2 to set
     */
    public final void setConceptUuid2(String conceptUuid2) {
        this.conceptUuid2 = conceptUuid2;
    }

    /**
     * @return the characteristicTypeUuid
     */
    public final String getCharacteristicTypeUuid() {
        return characteristicTypeUuid;
    }

    /**
     * @param characteristicTypeUuid the characteristicTypeUuid to set
     */
    public final void setCharacteristicTypeUuid(String characteristicTypeUuid) {
        this.characteristicTypeUuid = characteristicTypeUuid;
    }

    /**
     * @return the refinabilityUuid
     */
    public final String getRefinabilityUuid() {
        return refinabilityUuid;
    }

    /**
     * @param refinabilityUuid the refinabilityUuid to set
     */
    public final void setRefinabilityUuid(String refinabilityUuid) {
        this.refinabilityUuid = refinabilityUuid;
    }

    /**
     * @return the relationshipGroup
     */
    public final String getRelationshipGroup() {
        return relationshipGroup;
    }

    /**
     * @param relationshipGroup the relationshipGroup to set
     */
    public final void setRelationshipGroup(String relationshipGroup) {
        this.relationshipGroup = relationshipGroup;
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
