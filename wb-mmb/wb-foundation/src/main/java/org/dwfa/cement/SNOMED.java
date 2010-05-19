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
package org.dwfa.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.util.id.Type3UuidFactory;

public class SNOMED {
    public enum Description implements I_DescribeConceptUniversally {
        PATIENT_FIRST_NAME(UUID.fromString("ad7e2614-6443-3d0e-90c5-4dc9306d362b"), UUID.fromString("99291540-0d13-3b2b-bff9-e82bf26596a5")), PATIENT_MIDDLE_NAME(UUID.fromString("2cda065a-766b-3106-9ad2-e3753d601323"), UUID.fromString("c485c13a-f797-3104-9b28-603af195f54d")), PATIENT_NAME(UUID.fromString("1afab883-1f3e-30b3-8be5-a01e51c3bc76"), UUID.fromString("f7b5cba7-6558-3277-a108-7eb66198cd9a")), PATIENT_DOB(UUID.fromString("2a10c4ce-84a5-383f-9438-1b65d57314b9"), UUID.fromString("40584511-f4fa-33f5-9120-e74f69f1db36")), LINKAGE_CONCEPT(UUID.fromString("a8c7ba58-48a5-3d02-a5cf-751613a9cb88"), UUID.fromString("1a3399bc-e6b5-3dea-8058-4e08012ff00f")), SNOMED_CT_CONCEPT(UUID.fromString("5fdbd08a-f7e5-311a-b9f6-3f27e6f43a14"), UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8")), COUNT_OF_ENTITIES(UUID.fromString("5dcf26c5-b01a-36b2-b70f-608f93a85512"), UUID.fromString("55134b14-bf07-3181-84a5-36a66b695d74")), NUCLEATED_RED_BLOOD_CELL_COUNT(UUID.fromString("2e3a9b4b-047b-3e6d-b889-a8a2e269377f"), UUID.fromString("73f16b60-b808-397d-a289-702388e6d993")), RACIAL_GROUP(UUID.fromString("e1cb0296-db69-3924-8b05-a0e918bb9cc4"), UUID.fromString("2faee888-c9c7-3dec-9ba9-c652923c9c84")), HISPANIC(UUID.fromString("fdc4c2a8-713a-3193-b885-1e0ef3a36731"), UUID.fromString("53f633c1-0116-3db2-b787-9224acc7db7b")), CAUCASIAN(UUID.fromString("7f7b0d72-db19-36a6-a33a-d72dbc8ece1c"), UUID.fromString("76cd3085-772f-3abc-ac6c-e32602b23c3a")), EAST_INDIAN(UUID.fromString("a62a93c0-2f90-3d38-8d25-f1f29662c34e"), UUID.fromString("d4a952de-3c65-33da-9247-1eb944a887b8")), GENDER(UUID.fromString("4a8a6062-fd13-35c7-88dd-c8e1e9c4ce84"), UUID.fromString("49e978c8-81a4-3ea6-8bed-e7275ef20cc5")), MALE(UUID.fromString("e643c68b-73a8-304f-8d1b-ab04b540ee92"), UUID.fromString("cf312e35-e5e9-36e0-8ede-720a6e7aad72")), FEMALE(UUID.fromString("96c6aea5-d8c1-3d65-8d10-1d19420f36f1"), UUID.fromString("47bd3fa7-e605-3185-bf2c-4fa5e9c5c469")), PATIENT_FOLLOW_UP_PLANNED_AND_SCHEDULED(UUID.fromString("9a9065c1-ad2e-3647-8742-86cf3bd31b48"), UUID.fromString("d585c18a-1e4d-3f30-919e-41513a56b4dd")), DIABETES_EDUCATION(UUID.fromString("c9243864-53b3-330d-b9a6-737848d0785b"), UUID.fromString("f2da06ab-dff5-33c5-a712-aca093e95d4a")), RENAL_CARE_EDUCATION(UUID.fromString("b4ce3ba9-9fe9-3d8e-b0e5-120447bdc168"), UUID.fromString("39b126d8-ce14-35ab-b491-5533ace42640")), SMOKING_EFFECTS_EDUCATION(UUID.fromString("4a31192c-0eab-3e1e-848f-b89374959fff"), UUID.fromString("9cb478dd-6c9f-3dbf-b668-19d3eec2a1e2")), EDUCATION(UUID.fromString("7db4a35a-63f7-3c38-bd84-9ba0a8d3fef6"), UUID.fromString("c153ee83-35d0-3734-b089-faddbe7afcae")), IBUPROFEN_200_MG_TAB(UUID.fromString("3b04fb89-17b7-36a8-a5b3-d0ede749573e"), UUID.fromString("3828b663-7ff9-3482-ac0c-fc3794f16a4b")), TABLET(UUID.fromString("9d2792ea-8a9a-3781-ab1e-3fde29182b01"), UUID.fromString("48c7b424-f8d5-39e2-8956-b787568e264c")), ORAL_ROUTE(UUID.fromString("98980243-b6ac-3781-a0ef-eb554dc925dd"), UUID.fromString("8de36878-0d4d-3c96-9260-61eff787660a")), QID(UUID.fromString("2f1b104c-cec6-3f31-9d1a-abf8b5708b91"), UUID.fromString("3f943d75-e8eb-3341-951e-2be451c15abe")), BID(UUID.fromString("77e3cc73-b280-3d31-af8d-600afd9367a2"), UUID.fromString("4fada9ea-5e1b-3af3-ac2a-ddd42e313c06")), Examination_of_retina(UUID.fromString("b8660ec6-8901-3d3f-be3e-6862e6517857"), UUID.fromString("e2f396ad-17a8-35f1-a12a-5a70855597a3")), Glyburide_5mg_tablet(UUID.fromString("dcc42417-af4a-333c-8590-38d473087d6a"), UUID.fromString("69c8cf4c-2055-3cc0-9454-af8327237bfe")), Wound_debridement(UUID.fromString("722208c9-2c58-3836-92c5-cb6f859e4f51"), UUID.fromString("8fc6c161-4371-31d6-8025-ca6c759cd38a")), Power_of_sphere(UUID.fromString("5d177900-ebdc-3bde-9703-2cb63dc20792"), UUID.fromString("ec2b0b4b-127b-3568-b173-6d7db4e3bed3")), diopters(UUID.fromString("5761de7d-8988-3abc-9c95-931bab8e06d7"), UUID.fromString("c30be543-84ed-334d-9a4c-88c1058e3dd5")),

        Disability_evaluation_procedure(UUID.fromString("cc9aefd8-7c9e-38bf-a978-4f412fae7389"), UUID.fromString("b7de93d6-2944-3606-b072-649dfe3ca2d9")), Physician_visit_with_evaluation_AND_OR_management_service(UUID.fromString("0e223090-800b-350b-909d-98dcd5d6124a"), UUID.fromString("50161b46-590d-3f79-a65b-7af8c0d83aaf")),

        Retinal_surgeon(UUID.fromString("0f37415c-d017-3cde-bf86-f6702232c01e"), UUID.fromString("d5b735e2-1173-3a59-80d6-4fc2ee8ef540")), Medical_ophthalmologist(UUID.fromString("66e8f4e0-bc0a-300d-9a74-92d68ebf3e2d"), UUID.fromString("a26a40be-b280-3e7e-98f7-773798b802cb")), General_practioner(UUID.fromString("e0dd5075-9a57-3452-9a05-46d8b3d0ee63"), UUID.fromString("a1f66d56-15ac-3a95-ab25-fd360d7c4da1")), Internal_medicine_specialist(UUID.fromString("129b1304-784b-3780-a6f2-f95dc28f3290"), UUID.fromString("24250060-2c4a-3193-9a6d-98abeee4077f")), Family_medicine_specialist(UUID.fromString("f81f4ae8-28dd-3851-be97-cece9707cb82"), UUID.fromString("a1f66d56-15ac-3a95-ab25-fd360d7c4da1")), ;

        private Collection<UUID> descUids = new ArrayList<UUID>();
        private Collection<UUID> concUids = new ArrayList<UUID>();

        private Description(UUID id, UUID conId) {
            this.descUids.add(id);
            this.concUids.add(conId);
        }

        public I_ConceptualizeUniversally getConcept() throws IOException, TerminologyException {
            throw new UnsupportedOperationException();
        }

        public I_ConceptualizeUniversally getDescType() throws IOException, TerminologyException {
            throw new UnsupportedOperationException();
        }

        public I_ConceptualizeUniversally getStatus() throws IOException, TerminologyException {
            throw new UnsupportedOperationException();
        }

        public I_DescribeConceptLocally localize() throws IOException, TerminologyException {
            return LocalFixedTerminology.getStore().getDescription(LocalFixedTerminology.getStore().getNid(descUids),
                LocalFixedTerminology.getStore().getNid(concUids));
        }

        public String getLangCode() {
            throw new UnsupportedOperationException();
        }

        public String getText() {
            throw new UnsupportedOperationException();
        }

        public boolean isInitialCapSig() {
            throw new UnsupportedOperationException();
        }

        public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
                I_StoreUniversalFixedTerminology extensionServer) {
            throw new UnsupportedOperationException();
        }

        public boolean isUniversal() {
            return true;
        }

        public Collection<UUID> getUids() {
            return descUids;
        }

    }

    public enum Concept implements I_ConceptualizeUniversally {
        ROOT(Type3UuidFactory.SNOMED_ROOT_UUID), PATIENT_FIRST_NAME(UUID.fromString("99291540-0d13-3b2b-bff9-e82bf26596a5")), PATIENT_MIDDLE_NAME(UUID.fromString("c485c13a-f797-3104-9b28-603af195f54d")), PATIENT_NAME(UUID.fromString("f7b5cba7-6558-3277-a108-7eb66198cd9a")), PATIENT_DOB(UUID.fromString("40584511-f4fa-33f5-9120-e74f69f1db36")), IS_A(Type3UuidFactory.SNOMED_ISA_REL_UUID), UNIT_OF_TIME(UUID.fromString("3328b9a2-2936-3468-9b18-df6249c2add8")),

        Disability_evaluation_procedure(UUID.fromString("b7de93d6-2944-3606-b072-649dfe3ca2d9")), Physician_visit_with_evaluation_AND_OR_management_service(UUID.fromString("50161b46-590d-3f79-a65b-7af8c0d83aaf")), Retinal_surgeon(UUID.fromString("d5b735e2-1173-3a59-80d6-4fc2ee8ef540")), Medical_ophthalmologist(UUID.fromString("539ab168-f4be-38d9-ae44-e4dcf6171685")), Family_medicine_specialist(UUID.fromString("a1f66d56-15ac-3a95-ab25-fd360d7c4da1")), Internal_medicine_specialist(UUID.fromString("24250060-2c4a-3193-9a6d-98abeee4077f"));

        private Collection<UUID> conceptUids = new ArrayList<UUID>();

        private Concept(UUID id) {
            this.conceptUids.add(id);
        }

        public boolean isPrimitive(I_StoreUniversalFixedTerminology server) throws IOException, TerminologyException {
            return server.getConcept(conceptUids).isPrimitive(server);
        }

        public Collection<UUID> getUids() {
            return conceptUids;
        }

        public boolean isUniversal() {
            return true;
        }

        public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getDestRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology server) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType,
                I_StoreUniversalFixedTerminology extensionServer) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types,
                I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

        public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
            return LocalFixedConcept.get(getUids());
        }

    }
}
