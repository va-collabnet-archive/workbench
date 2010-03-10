/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.mojo.file;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.file.GenericFileWriter;
import org.dwfa.tapi.TerminologyException;

public class Rf2ConceptWriter extends GenericFileWriter<Rf2ConceptWriter.Rf2ConceptRow> {

    public Rf2ConceptWriter(File conceptFile) throws IOException {
        open(conceptFile, false);
    }

    /**
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(Rf2ConceptWriter.Rf2ConceptRow conceptRow) throws IOException, TerminologyException {
        return conceptRow.toString();
    }

    public class Rf2ConceptRow extends GenericFileWriter<Rf2ConceptRow> {
        String conceptSctId;
        String effectiveTime;
        String active;
        String moduleSctId;
        String definiationStatusSctId;

        public Rf2ConceptRow() {

        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return conceptSctId + "\t" + effectiveTime + "\t" + active + "\t" + moduleSctId + "\t"
                + definiationStatusSctId;
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
}
