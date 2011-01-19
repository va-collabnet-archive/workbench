package org.dwfa.mojo.file.rf2;

import java.util.UUID;

import org.dwfa.ace.file.GenericFileWriter;

public class Rf2IdentifierRow extends GenericFileWriter<Rf2IdentifierRow> {
    public static UUID SCT_ID_IDENTIFIER_SCHEME = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");
    private static final int CONCEPT_COLUMNS = 6;
    String identifierSchemeSctId;
    String alternateIdentifier;
    String effectiveTime;
    String active;
    String moduleSctId;
    String referencedComponentSctId;

    public Rf2IdentifierRow() {

    }

    public Rf2IdentifierRow(String line) throws Exception {
        String[] columns = line.split("\t");
        if (columns.length != CONCEPT_COLUMNS) {
            throw new Exception("Invalid file format. rf2 concept file must have " + CONCEPT_COLUMNS + " columns");
        }

        identifierSchemeSctId = columns[0];
        alternateIdentifier = columns[1];
        effectiveTime = columns[2];
        active = columns[3];
        moduleSctId = columns[4];
        referencedComponentSctId = columns[5];
    }

    /**
     * Header row for this type.
     *
     * @return String header.
     */
    public String getHeader() {
        return "identifierSchemeId" + "\t" + "alternateIdentifier" + "\t" + "effectiveTime" + "\t" + "active"
            + "\t" + "moduleId" + "\t" + "referencedComponentId";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return identifierSchemeSctId + "\t" + alternateIdentifier + "\t" + effectiveTime + "\t" + active + "\t"
            + moduleSctId + "\t" + referencedComponentSctId;
    }

    /**
     * @return the identifierSchemeSctId
     */
    public final String getIdentifierSchemeSctId() {
        return identifierSchemeSctId;
    }

    /**
     * @param identifierSchemeSctId the identifierSchemeSctId to set
     */
    public final void setIdentifierSchemeSctId(String identifierSchemeSctId) {
        this.identifierSchemeSctId = identifierSchemeSctId;
    }

    /**
     * @return the alternateIdentifier
     */
    public final String getAlternateIdentifier() {
        return alternateIdentifier;
    }

    /**
     * @param alternateIdentifier the alternateIdentifier to set
     */
    public final void setAlternateIdentifier(String alternateIdentifier) {
        this.alternateIdentifier = alternateIdentifier;
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
     * @return the referencedComponentSctId
     */
    public final String getReferencedComponentSctId() {
        return referencedComponentSctId;
    }

    /**
     * @param referencedComponentSctId the referencedComponentSctId to set
     */
    public final void setReferencedComponentSctId(String referencedComponentSctId) {
        this.referencedComponentSctId = referencedComponentSctId;
    }
}