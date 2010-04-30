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
import java.util.logging.Logger;

import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Read ace concept file rows into AceConceptRow
 * 
 * @author ean
 */
public class AceConceptReader extends IterableFileReader<AceConceptReader.AceConceptRow> {
    /** Class logger. */
    private Logger logger = Logger.getLogger(AceConceptReader.class.getName());

    /**
     * Create the reader for the file.
     * 
     * @param file File to read.
     */
    public AceConceptReader(File file) {
        setSourceFile(file);
    }

    /**
     * @see org.dwfa.ace.file.IterableFileReader#processLine(java.lang.String)
     * @throws TerminologyRuntimeException if the row is invalid.
     * @return AceConceptRow object.
     */
    @Override
    protected AceConceptRow processLine(String line) {
        AceConceptRow aceConceptRow;

        try {
            aceConceptRow = new AceConceptRow(line);
        } catch (IndexOutOfBoundsException ex) {
            logger.info("Invalid file format");
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            logger.info("Cannot process line:" + ex);
            throw new TerminologyRuntimeException(ex);
        }

        return aceConceptRow;
    }

    /**
     * Ace file concept row.
     */
    public class AceConceptRow {
        /** number of columns in a valid ace concepts file */
        private static final int CONCEPT_COLUMNS = 11;
        /** UUID. */
        private String conceptId;
        /** status code. */
        private String conceptStatus;
        /** concepts fully specified name. */
        private String fullySpecifiedName;
        /** ctv3 id.. */
        private String ctv3Id;
        /** snomed id. */
        private String snomedId;
        /** primative flag. */
        private String isPrimative;
        /** concept UUID. */
        private String conceptUuid;
        /** status concept UUID. */
        private String conceptStatusId;
        /** time string eg 20090226T000000Z */
        private String effectiveTime;
        /** edit path uuid */
        private String pathUuid;
        /** status concept UUID. */
        private String statusUuid;

        /**
         * Default constructor.
         */
        public AceConceptRow() {
        }

        /**
         * Creates a AceConceptRow from an ace line.
         * 
         * @param line From an ace concept file
         * @throws TerminologyException if not in the correct format.
         */
        public AceConceptRow(String line) throws TerminologyException {
            String[] columns = line.split("\t");
            if (columns.length != CONCEPT_COLUMNS) {
                throw new TerminologyException("Invalid file format. Ace concept file must have " + CONCEPT_COLUMNS
                    + " columns");
            }

            conceptId = columns[0];
            conceptStatus = columns[1];
            fullySpecifiedName = columns[2];
            ctv3Id = columns[3];
            snomedId = columns[4];
            isPrimative = columns[5];
            conceptUuid = columns[6];
            conceptStatusId = columns[7];
            effectiveTime = columns[8];
            pathUuid = columns[9];
            statusUuid = columns[10];
        }

        /**
         * @return the conceptId
         */
        public final String getConceptId() {
            return conceptId;
        }

        /**
         * @param conceptId the conceptId to set
         */
        public final void setConceptId(String conceptId) {
            this.conceptId = conceptId;
        }

        /**
         * @return the conceptStatus
         */
        public final String getConceptStatus() {
            return conceptStatus;
        }

        /**
         * @param conceptStatus the conceptStatus to set
         */
        public final void setConceptStatus(String conceptStatus) {
            this.conceptStatus = conceptStatus;
        }

        /**
         * @return the fullySpecifiedName
         */
        public final String getFullySpecifiedName() {
            return fullySpecifiedName;
        }

        /**
         * @param fullySpecifiedName the fullySpecifiedName to set
         */
        public final void setFullySpecifiedName(String fullySpecifiedName) {
            this.fullySpecifiedName = fullySpecifiedName;
        }

        /**
         * @return the ctv3Id
         */
        public final String getCtv3Id() {
            return ctv3Id;
        }

        /**
         * @param ctv3Id the ctv3Id to set
         */
        public final void setCtv3Id(String ctv3Id) {
            this.ctv3Id = ctv3Id;
        }

        /**
         * @return the snomedId
         */
        public final String getSnomedId() {
            return snomedId;
        }

        /**
         * @param snomedId the snomedId to set
         */
        public final void setSnomedId(String snomedId) {
            this.snomedId = snomedId;
        }

        /**
         * @return the isPrimative
         */
        public final String getIsPrimative() {
            return isPrimative;
        }

        /**
         * @param isPrimative the isPrimative to set
         */
        public final void setIsPrimative(String isPrimative) {
            this.isPrimative = isPrimative;
        }

        /**
         * @return the conceptUuid
         */
        public final String getConceptUuid() {
            return conceptUuid;
        }

        /**
         * @param conceptUuid the conceptUuid to set
         */
        public final void setConceptUuid(String conceptUuid) {
            this.conceptUuid = conceptUuid;
        }

        /**
         * @return the conceptStatusId
         */
        public final String getConceptStatusId() {
            return conceptStatusId;
        }

        /**
         * @param conceptStatusId the conceptStatusId to set
         */
        public final void setConceptStatusId(String conceptStatusId) {
            this.conceptStatusId = conceptStatusId;
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

        /**
         * @return the statusUuid
         */
        public final String getStatusUuid() {
            return statusUuid;
        }

        /**
         * @param statusUuid the statusUuid to set
         */
        public final void setStatusUuid(String statusUuid) {
            this.statusUuid = statusUuid;
        }
    }
}
