package org.dwfa.mojo.file.rf2;

public class Rf2RelationshipRow {
    String relationshipSctId;
    String effectiveTime;
    String active;
    String moduleSctId;
    String sourceSctId;
    String destinationSctId;
    String relationshipGroup;
    String typeSctId;
    String characteristicSctId;
    String modifierSctId;
    private int CONCEPT_COLUMNS = 10;

    public Rf2RelationshipRow() {

    }

    public Rf2RelationshipRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. rf2 description file must have " + CONCEPT_COLUMNS + " columns");
        }

        relationshipSctId = columns[0];
        effectiveTime = columns[1];
        active = columns[2];
        moduleSctId = columns[3];
        sourceSctId = columns[4];
        destinationSctId = columns[5];
        relationshipGroup = columns[6];
        typeSctId = columns[7];
        characteristicSctId = columns[8];
        modifierSctId = columns[9];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "id" + "\t" + "effectiveTime" + "\t" + "active" + "\t" + "moduleId" + "\t" + "sourceId" + "\t"
            + "destinationId" + "\t" + "relationshipGroup" + "\t" + "typeId" + "\t" + "characteristicTypeId" + "\t"
            + "modifierId";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return relationshipSctId + "\t" + effectiveTime + "\t" + active + "\t" + moduleSctId + "\t" + sourceSctId
            + "\t" + destinationSctId + "\t" + relationshipGroup + "\t" + typeSctId + "\t" + characteristicSctId + "\t"
            + modifierSctId;
    }

    /**
     * @return the relationshipSctId
     */
    public final String getRelationshipSctId() {
        return relationshipSctId;
    }

    /**
     * @param relationshipSctId the relationshipSctId to set
     */
    public final void setRelationshipSctId(String relationshipSctId) {
        this.relationshipSctId = relationshipSctId;
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
     * @return the sourceSctId
     */
    public final String getSourceSctId() {
        return sourceSctId;
    }

    /**
     * @param sourceSctId the sourceSctId to set
     */
    public final void setSourceSctId(String sourceSctId) {
        this.sourceSctId = sourceSctId;
    }

    /**
     * @return the destinationSctId
     */
    public final String getDestinationSctId() {
        return destinationSctId;
    }

    /**
     * @param destinationSctId the destinationSctId to set
     */
    public final void setDestinationSctId(String destinationSctId) {
        this.destinationSctId = destinationSctId;
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
     * @return the characteristicSctId
     */
    public final String getCharacteristicSctId() {
        return characteristicSctId;
    }

    /**
     * @param characteristicSctId the characteristicSctId to set
     */
    public final void setCharacteristicSctId(String characteristicSctId) {
        this.characteristicSctId = characteristicSctId;
    }

    /**
     * @return the modifierSctId
     */
    public final String getModifierSctId() {
        return modifierSctId;
    }

    /**
     * @param modifierSctId the modifierSctId to set
     */
    public final void setModifierSctId(String modifierSctId) {
        this.modifierSctId = modifierSctId;
    }
}
