package org.dwfa.mojo.file.rf1;

public class Rf1RelationshipRow {
    String relationshipSctId;
    String sourceSctId;
    String relationshipType;
    String destinationSctId;
    String relationshipGroup;
    String characteristicType;
    String refinability;
    private int CONCEPT_COLUMNS = 7;

    public Rf1RelationshipRow() {

    }

    public Rf1RelationshipRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS ) {
            throw new Exception("Invalid file format. rf2 description file must have " + CONCEPT_COLUMNS + " columns");
        }

        relationshipSctId = columns[0];
        sourceSctId = columns[1];
        relationshipType = columns[2];
        destinationSctId = columns[3];
        characteristicType = columns[4];
        refinability = columns[5];
        relationshipGroup = columns[6];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "RELATIONSHIPID" + "\t" + "CONCEPTID1" + "\t" + "RELATIONSHIPTYPE" + "\t" + "CONCEPTID2" + "\t"
            + "CHARACTERISTICTYPE" + "\t" + "REFINABILITY" + "\t" + "RELATIONSHIPGROUP";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return relationshipSctId + "\t" + sourceSctId + "\t" + relationshipType + "\t" + destinationSctId + "\t"
            + characteristicType + "\t" + refinability + "\t" + relationshipGroup;
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
     * @return the relationshipType
     */
    public final String getRelationshipType() {
        return relationshipType;
    }

    /**
     * @param relationshipType the relationshipType to set
     */
    public final void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
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
     * @return the characteristicType
     */
    public final String getCharacteristicType() {
        return characteristicType;
    }

    /**
     * @param characteristicType the characteristicType to set
     */
    public final void setCharacteristicType(String characteristicType) {
        this.characteristicType = characteristicType;
    }

    /**
     * @return the refinability
     */
    public final String getRefinability() {
        return refinability;
    }

    /**
     * @param refinability the refinability to set
     */
    public final void setRefinability(String refinability) {
        this.refinability = refinability;
    }
}
