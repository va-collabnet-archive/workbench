package org.dwfa.mojo.file;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.file.GenericFileWriter;
import org.dwfa.tapi.TerminologyException;

/**
 * File writer for RF2 identifier rows.
 *
 * @author Ean Dungey
 */
public class Rf2IdentifierWriter extends GenericFileWriter<Rf2IdentifierWriter.Rf2IdentifierRow> {

    public Rf2IdentifierWriter(File conceptFile) throws IOException {
        open(conceptFile, false);
    }

    /**
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(Rf2IdentifierWriter.Rf2IdentifierRow conceptRow) throws IOException,
            TerminologyException {
        return conceptRow.toString();
    }

    public class Rf2IdentifierRow extends GenericFileWriter<Rf2IdentifierRow> {
        String identifierSchemeSctId;
        String alternateIdentifier;
        String effectiveTime;
        String active;
        String moduleSctId;
        String referencedComponentSctId;

        public Rf2IdentifierRow() {

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
}