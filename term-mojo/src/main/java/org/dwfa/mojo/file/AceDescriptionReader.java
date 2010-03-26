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
package org.dwfa.mojo.file;

import java.io.File;
import java.util.logging.Logger;

import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Read ace description file rows into AceConceptRow.
 * 
 * @author ean
 */
public class AceDescriptionReader extends IterableFileReader<AceDescriptionReader.AceDescriptionRow> {
    /** class logger. */
    private Logger logger = Logger.getLogger(AceDescriptionReader.class.getName());

    /**
     * create a file reader for the File.
     * 
     * @param file File.
     */
    public AceDescriptionReader(File file) {
        setSourceFile(file);
    }

    /**
     * @see org.dwfa.ace.file.IterableFileReader#processLine(java.lang.String)
     * @throws TerminologyRuntimeException if the row is invalid.
     * @return AceDescriptionRow object.
     */
    @Override
    protected AceDescriptionRow processLine(String line) {
        AceDescriptionRow aceDescriptionRow;

        try {
            aceDescriptionRow = new AceDescriptionRow(line);
        } catch (IndexOutOfBoundsException ex) {
            logger.info("Invalid file format");
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            logger.info("Cannot process line:" + ex);
            throw new TerminologyRuntimeException(ex);
        }

        return aceDescriptionRow;
    }

    /**
     * Ace description file row.
     */
    public class AceDescriptionRow {
        String descriptionId;
        String descriptionStatus;
        String conceptId;
        String term;
        String casesensitivityId;
        String initCap;
        String descriptionType;
        String languageCode;
        String languageId;
        String descriptionUuid;
        String descriptionStatusId;
        String descriptionTypeId;
        String conceptUuid;
        String effectiveTime;
        String pathUuid;
        String statusUuid;

        /**
         * Default constructor.
         */
        public AceDescriptionRow() {
        }

        /**
         * Creates a AceDescriptionRow from an ace line.
         * 
         * @param line From an ace concept file
         * @throws TerminologyException if not in the correct format.
         */
        public AceDescriptionRow(String line) throws TerminologyException {
            String[] columns = line.split("\t");
            if (columns.length != 16) {
                throw new TerminologyException("Invalid file format. Ace concept file must have 16 columns");
            }

            descriptionId = columns[0];
            descriptionStatus = columns[1];
            conceptId = columns[2];
            term = columns[3];
            casesensitivityId = columns[4];
            initCap = columns[5];
            descriptionType = columns[6];
            languageCode = columns[7];
            languageId = columns[8];
            descriptionUuid = columns[9];
            descriptionStatusId = columns[10];
            descriptionTypeId = columns[11];
            conceptUuid = columns[12];
            effectiveTime = columns[13];
            pathUuid = columns[14];
            statusUuid = columns[15];
        }

        /**
         * @return the descriptionId
         */
        public final String getDescriptionId() {
            return descriptionId;
        }

        /**
         * @param descriptionId the descriptionId to set
         */
        public final void setDescriptionId(String descriptionId) {
            this.descriptionId = descriptionId;
        }

        /**
         * @return the descriptionStatus
         */
        public final String getDescriptionStatus() {
            return descriptionStatus;
        }

        /**
         * @param descriptionStatus the descriptionStatus to set
         */
        public final void setDescriptionStatus(String descriptionStatus) {
            this.descriptionStatus = descriptionStatus;
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
         * @return the casesensitivity
         */
        public final String getCasesensitivityId() {
            return casesensitivityId;
        }

        /**
         * @param casesensitivityId the casesensitivityId to set
         */
        public final void setCasesensitivityId(String casesensitivityId) {
            this.casesensitivityId = casesensitivityId;
        }

        /**
         * @return the initCap
         */
        public final String getInitCap() {
            return initCap;
        }

        /**
         * @param initCap the initCap to set
         */
        public final void setInitCap(String initCap) {
            this.initCap = initCap;
        }

        /**
         * @return the descriptionType
         */
        public final String getDescriptionType() {
            return descriptionType;
        }

        /**
         * @param descriptionType the descriptionType to set
         */
        public final void setDescriptionType(String descriptionType) {
            this.descriptionType = descriptionType;
        }

        /**
         * @return the languageCode
         */
        public final String getLanguageCode() {
            return languageCode;
        }

        /**
         * @param languageCode the languageCode to set
         */
        public final void setLanguageCode(String languageCode) {
            this.languageCode = languageCode;
        }

        /**
         * @return the languageId
         */
        public final String getLanguageId() {
            return languageId;
        }

        /**
         * @param languageId the languageId to set
         */
        public final void setLanguageId(String languageId) {
            this.languageId = languageId;
        }

        /**
         * @return the descriptionUuid
         */
        public final String getDescriptionUuid() {
            return descriptionUuid;
        }

        /**
         * @param descriptionUuid the descriptionUuid to set
         */
        public final void setDescriptionUuid(String descriptionUuid) {
            this.descriptionUuid = descriptionUuid;
        }

        /**
         * @return the descriptionStatusId
         */
        public final String getDescriptionStatusId() {
            return descriptionStatusId;
        }

        /**
         * @param descriptionStatusId the descriptionStatusId to set
         */
        public final void setDescriptionStatusId(String descriptionStatusId) {
            this.descriptionStatusId = descriptionStatusId;
        }

        /**
         * @return the descriptionTypeId
         */
        public final String getDescriptionTypeId() {
            return descriptionTypeId;
        }

        /**
         * @param descriptionTypeId the descriptionTypeId to set
         */
        public final void setDescriptionTypeId(String descriptionTypeId) {
            this.descriptionTypeId = descriptionTypeId;
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

        /**
         * 
         <!-- 0 -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctDescIdWithGeneration">
         * <name>
         * DESCRIPTIONID
         * </name>
         * </transform>
         * <!-- 1 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * DESCRIPTIONSTATUS
         * </name>
         * </transform>
         * <!-- 2 -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * CONCEPTID
         * </name>
         * </transform>
         * <!-- 3 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>TERM</name>
         * </transform>
         * <!-- 4 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * CASESENSITIVITY
         * </name>
         * </transform>
         * <!-- 4 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.CaseSensitivityToUuidTransform">
         * <name>
         * CASESENSITIVITY_UUID
         * </name>
         * <columnId>
         * 4
         * </columnId>
         * </transform>
         * <!-- 4 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.CaseSensitivityToUuidTransform">
         * <name>
         * CASESENSITIVITY_SCTID
         * </name>
         * <columnId>
         * 4
         * </columnId>
         * <chainedTransform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * LANGUAGEID
         * </name>
         * <columnId>
         * 4
         * </columnId>
         * </chainedTransform>
         * </transform>
         * <!-- 5 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * INITIALCAPITALSTATUS
         * </name>
         * </transform>
         * <!-- 6 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * DESCRIPTIONTYPE
         * </name>
         * </transform>
         * <!-- 7 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * LANGUAGECODE
         * </name>
         * </transform>
         * <!-- 8 -->
         * <transform implementation=
         * "org.dwfa.maven.transform.LanguageCodeToUuidTransform">
         * <name>
         * LANGUAGEUUID
         * </name>
         * </transform>
         * <!-- 8 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.LanguageCodeToUuidTransform">
         * <name>
         * LANGUAGEID
         * </name>
         * <columnId>
         * 8
         * </columnId>
         * <chainedTransform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * LANGUAGEID
         * </name>
         * <columnId>
         * 8
         * </columnId>
         * </chainedTransform>
         * </transform>
         * <!-- 9 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * DESCRIPTIONUUID
         * </name>
         * </transform>
         * <!-- 10 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * DESCRIPTIONSTATUSUUID
         * </name>
         * </transform>
         * <!-- 10 -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * DESCRIPTIONSTATUSID
         * </name>
         * <columnId>
         * 10
         * </columnId>
         * </transform>
         * <!-- 11 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * DESCRIPTIONTYPEUUID
         * </name>
         * </transform>
         * <!-- 11 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * DESCRIPTIONTYPEID
         * </name>
         * <columnId>
         * 11
         * </columnId>
         * </transform>
         * <!-- 12 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * CONCEPTUUID
         * </name>
         * </transform>
         * <!-- 13 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * EFFECTIVETIME
         * </name>
         * </transform>
         * <!-- 14 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>
         * PATHUUID
         * </name>
         * </transform>
         * <!-- 14 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>PATHID</name>
         * <columnId>
         * 14
         * </columnId>
         * </transform>
         * <!-- 15 -->
         * <transform
         * implementation="org.dwfa.maven.transform.IdentityTransform">
         * <name>STATUS_UUID</name>
         * </transform>
         * <!-- 15 again -->
         * <transform implementation=
         * "org.dwfa.maven.transform.UuidToSctConIdWithGeneration">
         * <name>STATUS_SCTID</name>
         * <columnId>
         * 15
         * </columnId> *
         */
    }
}
