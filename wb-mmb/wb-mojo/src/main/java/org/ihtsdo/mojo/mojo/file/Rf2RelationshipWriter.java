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

public class Rf2RelationshipWriter extends GenericFileWriter<Rf2RelationshipWriter.Rf2RelationshipRow> {

    public Rf2RelationshipWriter(File file) throws IOException {
        open(file, false);
    }

    /**
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(Rf2RelationshipWriter.Rf2RelationshipRow replationshipRow) throws IOException,
            TerminologyException {
        return replationshipRow.toString();
    }

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

        public Rf2RelationshipRow() {

        }

        /**
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return relationshipSctId + "\t" + effectiveTime + "\t" + active + "\t" + moduleSctId + "\t" + sourceSctId
                + "\t" + destinationSctId + "\t" + relationshipGroup + "\t" + typeSctId + "\t" + characteristicSctId
                + "\t" + modifierSctId;
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
}
