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

public class AceRelationshipReader extends IterableFileReader<AceRelationshipReader.AceRelationshipRow> {
    private Logger logger = Logger.getLogger(AceRelationshipReader.class.getName());

    public AceRelationshipReader(File file) {
        setSourceFile(file);
    }

    @Override
    protected AceRelationshipRow processLine(String line) {
        AceRelationshipRow aceDescriptionRow;

        try {
            aceDescriptionRow = new AceRelationshipRow(line);
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
     * Ace description file line.
     * 
     */
    public class AceRelationshipRow {
        String relationshipId;
        String relationshipStatus;
        String concept1Id;
        String relationshipTypeId;
        String concept2Id;
        String characteristicType;
        String refinability;
        String relationshipGroup;
        String relationshipUuid;
        String concept1Uuid;
        String relationshipTypeUuid;
        String concept2IdUuid;
        String characteristicTypeId;
        String refinabilityUuid;
        String relationshipStatusUuid;
        String effectiveTime;
        String pathUuid;
        String statusUuid;

        /**
         * Default constructor.
         */
        public AceRelationshipRow() {
        }

        /**
         * Creates a AceDescriptionRow from an ace line.
         * 
         * @param line From an ace concept file
         * @throws TerminologyException if not in the correct format.
         */
        public AceRelationshipRow(String line) throws TerminologyException {
            String[] columns = line.split("\t");
            if (columns.length != 18) {
                throw new TerminologyException("Invalid file format. Ace concept file must have 16 columns");
            }

            relationshipId = columns[0];
            relationshipStatus = columns[1];
            concept1Id = columns[2];
            relationshipTypeId = columns[3];
            concept2Id = columns[4];
            characteristicType = columns[5];
            refinability = columns[6];
            relationshipGroup = columns[7];
            relationshipUuid = columns[8];
            concept1Uuid = columns[9];
            relationshipTypeUuid = columns[10];
            concept2IdUuid = columns[11];
            characteristicTypeId = columns[12];
            refinabilityUuid = columns[13];
            relationshipStatusUuid = columns[14];
            effectiveTime = columns[15];
            pathUuid = columns[16];
            statusUuid = columns[17];
        }

        /**
         * @return the relationshipId
         */
        public final String getRelationshipId() {
            return relationshipId;
        }

        /**
         * @param relationshipId the relationshipId to set
         */
        public final void setRelationshipId(String relationshipId) {
            this.relationshipId = relationshipId;
        }

        /**
         * @return the relationshipStatus
         */
        public final String getRelationshipStatus() {
            return relationshipStatus;
        }

        /**
         * @param relationshipStatus the relationshipStatus to set
         */
        public final void setRelationshipStatus(String relationshipStatus) {
            this.relationshipStatus = relationshipStatus;
        }

        /**
         * @return the concept1Id
         */
        public final String getConcept1Id() {
            return concept1Id;
        }

        /**
         * @param concept1Id the concept1Id to set
         */
        public final void setConcept1Id(String concept1Id) {
            this.concept1Id = concept1Id;
        }

        /**
         * @return the relationshipTypeId
         */
        public final String getRelationshipTypeId() {
            return relationshipTypeId;
        }

        /**
         * @param relationshipTypeId the relationshipTypeId to set
         */
        public final void setRelationshipTypeId(String relationshipTypeId) {
            this.relationshipTypeId = relationshipTypeId;
        }

        /**
         * @return the concept2Id
         */
        public final String getConcept2Id() {
            return concept2Id;
        }

        /**
         * @param concept2Id the concept2Id to set
         */
        public final void setConcept2Id(String concept2Id) {
            this.concept2Id = concept2Id;
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
         * @return the relationshipUuid
         */
        public final String getRelationshipUuid() {
            return relationshipUuid;
        }

        /**
         * @param relationshipUuid the relationshipUuid to set
         */
        public final void setRelationshipUuid(String relationshipUuid) {
            this.relationshipUuid = relationshipUuid;
        }

        /**
         * @return the concept1Uuid
         */
        public final String getConcept1Uuid() {
            return concept1Uuid;
        }

        /**
         * @param concept1Uuid the concept1Uuid to set
         */
        public final void setConcept1Uuid(String concept1Uuid) {
            this.concept1Uuid = concept1Uuid;
        }

        /**
         * @return the relationshipTypeUuid
         */
        public final String getRelationshipTypeUuid() {
            return relationshipTypeUuid;
        }

        /**
         * @param relationshipTypeUuid the relationshipTypeUuid to set
         */
        public final void setRelationshipTypeUuid(String relationshipTypeUuid) {
            this.relationshipTypeUuid = relationshipTypeUuid;
        }

        /**
         * @return the concept2IdUuid
         */
        public final String getConcept2IdUuid() {
            return concept2IdUuid;
        }

        /**
         * @param concept2IdUuid the concept2IdUuid to set
         */
        public final void setConcept2IdUuid(String concept2IdUuid) {
            this.concept2IdUuid = concept2IdUuid;
        }

        /**
         * @return the characteristicTypeId
         */
        public final String getCharacteristicTypeId() {
            return characteristicTypeId;
        }

        /**
         * @param characteristicTypeId the characteristicTypeId to set
         */
        public final void setCharacteristicTypeId(String characteristicTypeId) {
            this.characteristicTypeId = characteristicTypeId;
        }

        /**
         * @return the refinabilityUuid
         */
        public final String getRefinabilityUuid() {
            return refinabilityUuid;
        }

        /**
         * @param refinabilityUuid the refinabilityUuid to set
         */
        public final void setRefinabilityUuid(String refinabilityUuid) {
            this.refinabilityUuid = refinabilityUuid;
        }

        /**
         * @return the relationshipStatusUuid
         */
        public final String getRelationshipStatusUuid() {
            return relationshipStatusUuid;
        }

        /**
         * @param relationshipStatusUuid the relationshipStatusUuid to set
         */
        public final void setRelationshipStatusUuid(String relationshipStatusUuid) {
            this.relationshipStatusUuid = relationshipStatusUuid;
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
         <columnSpecs>
         * <!-- 0 -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctRelIdWithGeneration">
         * <name>
         * RELATIONSHIPID
         * </name>
         * </transform>
         * <!-- 1 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * REL_STATUS
         * </name>
         * </transform>
         * <!-- 2 -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * CONCEPTID1
         * </name>
         * </transform>
         * <!-- 3 -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * RELATIONSHIPTYPE
         * </name>
         * </transform>
         * <!-- 4 -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * CONCEPTID2
         * </name>
         * </transform>
         * <!-- 5 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * CHARACTERISTICTYPE
         * </name>
         * </transform>
         * <!-- 6 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * REFINABILITY
         * </name>
         * </transform>
         * <!-- 7 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * RELATIONSHIPGROUP
         * </name>
         * </transform>
         * <!-- 8 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * RELATIONSHIPUUID
         * </name>
         * </transform>
         * <!-- 9 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * CONCEPTUUID1
         * </name>
         * </transform>
         * <!-- 10 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * RELATIONSHIPTYPEUUID
         * </name>
         * </transform>
         * <!-- 10 again -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * RELATIONSHIPTYPEID
         * </name>
         * <columnId>
         * 10
         * </columnId>
         * </transform>
         * <!-- 11 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * CONCEPTUUID2
         * </name>
         * </transform>
         * <!-- 12 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * CHARACTERISTICTYPEUUID
         * </name>
         * </transform>
         * <!-- 12 again -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>
         * CHARACTERISTICTYPEID
         * </name>
         * <columnId>
         * 12
         * </columnId>
         * </transform>
         * <!-- 13 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * REFINABILITYUUID
         * </name>
         * </transform>
         * <!-- 14 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * RELATIONSHIPSTATUSUUID
         * </name>
         * </transform>
         * <!-- 15 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * EFFECTIVETIME
         * </name>
         * </transform>
         * <!-- 16 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>
         * PATHUUID
         * </name>
         * </transform>
         * <!-- 16 again -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>PATHID</name>
         * <columnId>
         * 16
         * </columnId>
         * </transform>
         * <!-- 17 -->
         * <transform
         * implementation="org.ihtsdo.mojo.maven.transform.IdentityTransform">
         * <name>STATUS_UUID</name>
         * </transform>
         * <!-- 17 again -->
         * <transform implementation=
         * "org.ihtsdo.mojo.maven.transform.UuidToSctConIdWithGeneration">
         * <name>STATUS_SCTID</name>
         * <columnId>
         * 17
         * </columnId>
         * </transform>
         */
    }
}
