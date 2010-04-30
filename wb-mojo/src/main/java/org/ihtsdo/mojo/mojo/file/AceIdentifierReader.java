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
 * Read ace identifier file rows into AceIdentifierRow
 * 
 * @author Ean Dungey
 */
public class AceIdentifierReader extends IterableFileReader<AceIdentifierReader.AceIdentifierRow> {
    /** Class logger. */
    private Logger logger = Logger.getLogger(AceIdentifierReader.class.getName());

    /**
     * Create the reader for the file.
     * 
     * @param file File to read.
     */
    public AceIdentifierReader(File file) {
        setSourceFile(file);
    }

    /**
     * @see org.dwfa.ace.file.IterableFileReader#processLine(java.lang.String)
     * @throws TerminologyRuntimeException if the row is invalid.
     * @return AceIdentifierRow object.
     */
    @Override
    protected AceIdentifierRow processLine(String line) {
        AceIdentifierRow aceIdentifierRow;

        try {
            aceIdentifierRow = new AceIdentifierRow(line);
        } catch (IndexOutOfBoundsException ex) {
            logger.info("Invalid file format");
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            logger.info("Cannot process line:" + ex);
            throw new TerminologyRuntimeException(ex);
        }

        return aceIdentifierRow;
    }

    /**
     * 
     * Ace file identifier row.
     */
    public class AceIdentifierRow {
        /** number of columns in a valid ace identifier file */
        private static final int IDENTIFIER_COLUMNS = 6;
        /** concept UUID. */
        private String primaryUuid;
        /** status UUID. */
        private String sourceSystemUuid;
        /** Source UUID or SCTID. */
        private String sourceId;
        /** Status UUID */
        private String statusUuid;
        /** time string NB yyyyMMdd or yyyyMMddThhmmssZ format. */
        private String effectiveTime;
        /** edit path UUID */
        private String pathUuid;

        /**
         * Default constructor.
         */
        public AceIdentifierRow() {
        }

        /**
         * Creates a AceIdentifierRow from an ace line.
         * 
         * @param line From an ace concept file
         * @throws TerminologyException if not in the correct format.
         */
        public AceIdentifierRow(String line) throws TerminologyException {
            String[] columns = line.split("\t");
            if (columns.length != IDENTIFIER_COLUMNS) {
                throw new TerminologyException("Invalid file format. Ace identifier file must have "
                    + IDENTIFIER_COLUMNS + " columns");
            }

            primaryUuid = columns[0];
            sourceSystemUuid = columns[1];
            sourceId = columns[2];
            statusUuid = columns[3];
            effectiveTime = columns[4];
            pathUuid = columns[5];
        }

        /**
         * @return the primaryUuid
         */
        public final String getPrimaryUuid() {
            return primaryUuid;
        }

        /**
         * @param primaryUuid the primaryUuid to set
         */
        public final void setPrimaryUuid(String primaryUuid) {
            this.primaryUuid = primaryUuid;
        }

        /**
         * @return the sourceSystemUuid
         */
        public final String getSourceSystemUuid() {
            return sourceSystemUuid;
        }

        /**
         * @param sourceSystemUuid the sourceSystemUuid to set
         */
        public final void setSourceSystemUuid(String sourceSystemUuid) {
            this.sourceSystemUuid = sourceSystemUuid;
        }

        /**
         * This may be a Uuid or a SctId.
         * 
         * @return the sourceId
         */
        public final String getSourceId() {
            return sourceId;
        }

        /**
         * @param sourceId the sourceId to set
         */
        public final void setSourceId(String sourceId) {
            this.sourceId = sourceId;
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

        /**
         * either yyyyMMdd or yyyyMMdd'T'hhmmss'Z'
         * 
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
}
