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

public class Rf2DescriptionWriter extends GenericFileWriter<Rf2DescriptionWriter.Rf2DescriptionRow> {

    public Rf2DescriptionWriter(File file) throws IOException {
        open(file, false);
    }

    /**
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(Rf2DescriptionWriter.Rf2DescriptionRow descriptionRow) throws IOException,
            TerminologyException {
        return descriptionRow.toString();
    }

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

        public Rf2DescriptionRow() {

        }

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

}
