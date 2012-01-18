/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.transform;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.spec.ValidationException;

/**
 *
 * @author akf
 */
public class UuidToSctIdMapper {

    String namespace;
    File directory;
    Writer conceptsWriter;
    Writer descriptionsWriter;
    Writer identifiersWriter;
    Writer relationshipsWriter;
    Writer conRefsetsWriter;
    Writer conConRefsetsWriter;
    Writer conConConRefsetsWriter;
    Writer conConStrRefsetsWriter;
    Writer stringRefsetsWriter;
    Writer uuidToSctMapWriter;
    BufferedReader conceptsReader;
    BufferedReader descriptionsReader;
    BufferedReader identifiersReader;
    BufferedReader relationshipsReader;
    BufferedReader conRefsetsReader;
    BufferedReader conConRefsetsReader;
    BufferedReader conConConRefsetsReader;
    BufferedReader conConStrRefsetsReader;
    BufferedReader stringRefsetsReader;
    TerminologyStoreDI store;
    HashMap<UUID, String> uuidToSctMap = new HashMap<UUID, String>();
    HashMap<UUID, String> uuidToExistingSctMap = new HashMap<UUID, String>();
    int conceptCounter = 1;
    int descCounter = 1;
    int relCounter = 1;

    public UuidToSctIdMapper(String namespace, File directory) {
        this.namespace = namespace;
        this.directory = directory;
        this.store = Ts.get();
    }

    private enum exisitingSctIds {

        PRIMITIVE("900000000000074008"), DEFINED("900000000000073002"),
        CASE_SENSITIVE("900000000000017005"), CASE_INSENSITIVE("900000000000448009");
        public final String value;

        private exisitingSctIds(String value) {
            this.value = value;
        }
    }

    public void map() throws IOException {
        setup();

        String conceptLine = conceptsReader.readLine();
        conceptLine = conceptsReader.readLine();
        while (conceptLine != null) {
            processConceptAttribute(conceptLine);
            conceptLine = conceptsReader.readLine();
        }

        String descLine = descriptionsReader.readLine();
        descLine = descriptionsReader.readLine();
        while (descLine != null) {
            processDescription(descLine);
            descLine = descriptionsReader.readLine();
        }

        String relLine = relationshipsReader.readLine();
        relLine = relationshipsReader.readLine();
        while (relLine != null) {
            processRelationship(relLine);
            relLine = relationshipsReader.readLine();
        }

        String idLine = identifiersReader.readLine();
        idLine = identifiersReader.readLine();
        while (idLine != null) {
            processIdentifiers(idLine);
            idLine = identifiersReader.readLine();
        }

        String conRefLine = conRefsetsReader.readLine();
        conRefLine = conRefsetsReader.readLine();
        while (conRefLine != null) {
            processConRefsets(conRefLine);
            conRefLine = conRefsetsReader.readLine();
        }

        String conConRefLine = conConRefsetsReader.readLine();
        conConRefLine = conConRefsetsReader.readLine();
        while (conConRefLine != null) {
            processConConRefsets(conConRefLine);
            conConRefLine = conConRefsetsReader.readLine();
        }

        String conConConRefLine = conConConRefsetsReader.readLine();
        conConConRefLine = conConConRefsetsReader.readLine();
        while (conConConRefLine != null) {
            processConConConRefsets(conConConRefLine);
            conConConRefLine = conConConRefsetsReader.readLine();
        }

        String conConStrRefLine = conConStrRefsetsReader.readLine();
        conConStrRefLine = conConStrRefsetsReader.readLine();
        while (conConStrRefLine != null) {
            processConConStrRefsets(conConStrRefLine);
            conConStrRefLine = conConStrRefsetsReader.readLine();
        }

        String strRefLine = stringRefsetsReader.readLine();
        strRefLine = stringRefsetsReader.readLine();
        while (strRefLine != null) {
            processStringRefsets(strRefLine);
            strRefLine = stringRefsetsReader.readLine();
        }
        
        processUuidToSctMap();
        
        close();
    }

    private void setup() throws IOException {
        File[] uuidFiles = directory.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".txt");
            }
        });
        File conceptsFileUuid = null;
        File descriptionsFileUuid = null;
        File relationshipsFileUuid = null;
        File identifiersFileUuid = null;
        File conRefsetsFileUuid = null;
        File conConRefsetsFileUuid = null;
        File conConConRefsetsFileUuid = null;
        File conConStrRefsetsFileUuid = null;
        File stringRefsetsFileUuid = null;

        for (File inputFile : uuidFiles) {
            if (inputFile.getName().startsWith("sct2_Concept_UUID_")) {
                conceptsFileUuid = inputFile;
                conceptsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_Description_UUID_")) {
                descriptionsFileUuid = inputFile;
                descriptionsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_Relationship_UUID_")) {
                relationshipsFileUuid = inputFile;
                relationshipsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_Identifier_UUID_")) {
                identifiersFileUuid = inputFile;
                identifiersReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_ConceptRefset_UUID_")) {
                conRefsetsFileUuid = inputFile;
                conRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_ConceptConceptRefset_UUID_")) {
                conConRefsetsFileUuid = inputFile;
                conConRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_ConceptConceptConceptRefset_UUID_")) {
                conConConRefsetsFileUuid = inputFile;
                conConConRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_ConceptConceptStringRefset_UUID_")) {
                conConStrRefsetsFileUuid = inputFile;
                conConStrRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            } else if (inputFile.getName().startsWith("sct2_StringRefset_UUID_")) {
                stringRefsetsFileUuid = inputFile;
                stringRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            }
        }

        File conceptsFile = new File(directory,
                conceptsFileUuid.getName().replace("sct2_Concept_UUID_", "sct2_Concept_"));
        File descriptionsFile = new File(directory,
                descriptionsFileUuid.getName().replace("sct2_Description_UUID_", "sct2_Description_"));
        File relationshipsFile = new File(directory,
                relationshipsFileUuid.getName().replace("sct2_Relationship_UUID_", "sct2_Relationship_"));
        File identifiersFile = new File(directory,
                identifiersFileUuid.getName().replace("sct2_Identifier_UUID_", "sct2_Identifier_"));
        File conRefsetsFile = new File(directory,
                conRefsetsFileUuid.getName().replace("sct2_ConceptRefset_UUID_", "sct2_ConceptRefset_"));
        File conConRefsetsFile = new File(directory,
                conConRefsetsFileUuid.getName().replace("sct2_ConceptConceptRefset_UUID_", "sct2_ConceptConceptRefset_"));
        File conConConRefsetsFile = new File(directory,
                conConConRefsetsFileUuid.getName().replace("sct2_ConceptConceptConceptRefset_UUID_", "sct2_ConceptConceptConceptRefset_"));
        File conConStrRefsetsFile = new File(directory,
                conConStrRefsetsFileUuid.getName().replace("sct2_ConceptConceptStringRefset_UUID_", "sct2_ConceptConceptStringRefset_"));
        File stringRefsetsFile = new File(directory,
                stringRefsetsFileUuid.getName().replace("sct2_StringRefset_UUID_", "sct2_StringRefset_"));
        File uuidToSctIdsFile = new File(directory,
                stringRefsetsFileUuid.getName().replace("sct2_StringRefset_UUID_", "sct2_to_uuid_map"));

        conceptsWriter = new BufferedWriter(new FileWriter(conceptsFile));
        descriptionsWriter = new BufferedWriter(new FileWriter(descriptionsFile));
        relationshipsWriter = new BufferedWriter(new FileWriter(relationshipsFile));
        identifiersWriter = new BufferedWriter(new FileWriter(identifiersFile));
        conRefsetsWriter = new BufferedWriter(new FileWriter(conRefsetsFile));
        conConRefsetsWriter = new BufferedWriter(new FileWriter(conConRefsetsFile));
        conConConRefsetsWriter = new BufferedWriter(new FileWriter(conConConRefsetsFile));
        conConStrRefsetsWriter = new BufferedWriter(new FileWriter(conConStrRefsetsFile));
        stringRefsetsWriter = new BufferedWriter(new FileWriter(stringRefsetsFile));
        uuidToSctMapWriter = new BufferedWriter(new FileWriter(uuidToSctIdsFile));


        for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
            conceptsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
            descriptionsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            relationshipsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
            identifiersWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.ConRefsetFileFields field : Rf2File.ConRefsetFileFields.values()) {
            conRefsetsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.ConConRefsetFileFields field : Rf2File.ConConRefsetFileFields.values()) {
            conConRefsetsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.ConConConRefsetFileFields field : Rf2File.ConConConRefsetFileFields.values()) {
            conConConRefsetsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.ConConStrRefsetFileFields field : Rf2File.ConConStrRefsetFileFields.values()) {
            conConStrRefsetsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.StringRefsetFileFields field : Rf2File.StringRefsetFileFields.values()) {
            stringRefsetsWriter.write(field.headerText + field.seperator);
        }
        for (Rf2File.UuidToSctMapFileFields field : Rf2File.UuidToSctMapFileFields.values()) {
            uuidToSctMapWriter.write(field.headerText + field.seperator);
        }
    }

    private void processConceptAttribute(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");

            for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.ConceptsFileFields.ACTIVE.ordinal()];
                        conceptsWriter.write(convertStatus(statusUuid)
                                + field.seperator);

                        break;

                    case DEFINITION_STATUS_ID:
                        String defStatus = parts[Rf2File.ConceptsFileFields.DEFINITION_STATUS_ID.ordinal()];
                        String defStatusSctId = null;
                        if (defStatus.equals("false")) {
                            defStatusSctId = exisitingSctIds.PRIMITIVE.value;
                        } else if (defStatus.equals("true")) {
                            defStatusSctId = exisitingSctIds.DEFINED.value;
                        }
                        conceptsWriter.write(defStatusSctId + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ConceptsFileFields.EFFECTIVE_TIME.ordinal()];
                        conceptsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ID:
                        String concept = parts[Rf2File.ConceptsFileFields.ID.ordinal()];
                        String conceptSctId = getExistingSctId(concept);
                        if (conceptSctId == null) {
                            conceptSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(concept), conceptSctId);
                        }
                        conceptsWriter.write(conceptSctId + field.seperator);

                        break;

                    case MODULE_ID:
                        conceptsWriter.write(namespace + field.seperator);

                        break;
                }
            }
        }
    }

    private void processDescription(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.DescriptionsFileFields.ACTIVE.ordinal()];
                        descriptionsWriter.write(convertStatus(statusUuid)
                                + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.DescriptionsFileFields.EFFECTIVE_TIME.ordinal()];
                        descriptionsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ID:
                        String desc = parts[Rf2File.DescriptionsFileFields.ID.ordinal()];
                        String descSctId = getExistingSctId(desc);
                        if (descSctId == null) {
                            descSctId = SctIdGenerator.generate(
                                    descCounter, namespace, SctIdGenerator.TYPE.DESCRIPTION);
                            this.uuidToSctMap.put(UUID.fromString(desc), descSctId);
                            descCounter++;
                        }

                        descriptionsWriter.write(descSctId + field.seperator);

                        break;

                    case MODULE_ID:
                        descriptionsWriter.write(namespace + field.seperator);

                        break;

                    case CONCEPT_ID:
                        String conceptId = parts[Rf2File.DescriptionsFileFields.CONCEPT_ID.ordinal()];
                        String conceptSctId = getExistingSctId(conceptId);
                        if (conceptSctId == null) {
                            conceptSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(conceptId), conceptSctId);
                        }
                        descriptionsWriter.write(conceptSctId + field.seperator);

                        break;

                    case LANGUAGE_CODE:
                        String langCode = parts[Rf2File.DescriptionsFileFields.LANGUAGE_CODE.ordinal()];
                        descriptionsWriter.write(langCode + field.seperator);

                        break;

                    case TYPE_ID:
                        String descType = parts[Rf2File.DescriptionsFileFields.TYPE_ID.ordinal()];
                        String descTypeSctId = getExistingSctId(descType);
                        if (descTypeSctId == null) {
                            descTypeSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(descType), descTypeSctId);
                        }

                        descriptionsWriter.write(descTypeSctId + field.seperator);

                        break;

                    case TERM:
                        String term = parts[Rf2File.DescriptionsFileFields.TERM.ordinal()];
                        descriptionsWriter.write(term + field.seperator);

                        break;

                    case CASE_SIGNIFICANCE_ID:
                        String cs = parts[Rf2File.DescriptionsFileFields.CASE_SIGNIFICANCE_ID.ordinal()];
                        String csSctId = null;
                        if (cs.equals("false")) {
                            csSctId = exisitingSctIds.CASE_INSENSITIVE.value;
                        } else {
                            csSctId = exisitingSctIds.CASE_SENSITIVE.value;
                        }
                        descriptionsWriter.write(csSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processRelationship(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.RelationshipsFileFields.ACTIVE.ordinal()];
                        relationshipsWriter.write(convertStatus(statusUuid) + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.RelationshipsFileFields.EFFECTIVE_TIME.ordinal()];
                        relationshipsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ID:
                        String rel = parts[Rf2File.RelationshipsFileFields.ID.ordinal()];
                        String relSctId = getExistingSctId(rel);
                        if (relSctId == null) {
                            relSctId = SctIdGenerator.generate(
                                    relCounter, namespace, SctIdGenerator.TYPE.RELATIONSHIP);
                            this.uuidToSctMap.put(UUID.fromString(rel), relSctId);
                            relCounter++;
                        }
                        relationshipsWriter.write(relSctId + field.seperator);

                        break;

                    case MODULE_ID:
                        relationshipsWriter.write(namespace + field.seperator);

                        break;

                    case SOURCE_ID:
                        String source = parts[Rf2File.RelationshipsFileFields.SOURCE_ID.ordinal()];
                        String sourceSctId = getExistingSctId(source);
                        if (sourceSctId == null) {
                            sourceSctId = SctIdGenerator.generate(
                                    conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(source), sourceSctId);
                            conceptCounter++;
                        }
                        relationshipsWriter.write(sourceSctId + field.seperator);


                        break;

                    case DESTINATION_ID:
                        String dest = parts[Rf2File.RelationshipsFileFields.DESTINATION_ID.ordinal()];
                        String destSctId = getExistingSctId(dest);
                        if (destSctId == null) {
                            destSctId = SctIdGenerator.generate(
                                    conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(dest), destSctId);
                            conceptCounter++;
                        }
                        relationshipsWriter.write(destSctId + field.seperator);

                        break;

                    case RELATIONSHIP_GROUP:
                        String group = parts[Rf2File.RelationshipsFileFields.RELATIONSHIP_GROUP.ordinal()];
                        relationshipsWriter.write(group + field.seperator);

                        break;

                    case TYPE_ID:
                        String type = parts[Rf2File.RelationshipsFileFields.TYPE_ID.ordinal()];
                        String typeSctId = getExistingSctId(type);
                        if (typeSctId == null) {
                            typeSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
                        }
                        relationshipsWriter.write(typeSctId + field.seperator);

                        break;

                    case CHARCTERISTIC_ID:
                        String relChar = parts[Rf2File.RelationshipsFileFields.CHARCTERISTIC_ID.ordinal()];
                        String relCharSctId = getExistingSctId(relChar);
                        if (relCharSctId == null) {
                            relCharSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(relChar), relCharSctId);
                        }
                        relationshipsWriter.write(relCharSctId + field.seperator);

                        break;

                    case MODIFIER_ID:
                        String modifier = parts[Rf2File.RelationshipsFileFields.MODIFIER_ID.ordinal()];
                        String modifierSctId = getExistingSctId(modifier);
                        if (modifierSctId == null) {
                            modifierSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(modifier), modifierSctId);
                        }
                        relationshipsWriter.write(modifierSctId
                                + field.seperator);

                        break;
                }
            }
        }
    }

    private void processIdentifiers(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {

                switch (field) {
                    case IDENTIFIER_SCHEME_ID:
                        String schemeId = parts[Rf2File.IdentifiersFileFields.IDENTIFIER_SCHEME_ID.ordinal()];
                        identifiersWriter.write(getExistingSctId(schemeId) + field.seperator);

                        break;

                    case ALTERNATE_IDENTIFIER:
                        String primUuid = parts[Rf2File.IdentifiersFileFields.ALTERNATE_IDENTIFIER.ordinal()];
                        identifiersWriter.write(primUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.IdentifiersFileFields.EFFECTIVE_TIME.ordinal()];
                        identifiersWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.IdentifiersFileFields.ACTIVE.ordinal()];
                        identifiersWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        identifiersWriter.write(namespace + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.IdentifiersFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        identifiersWriter.write(rcSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.ConRefsetFileFields field : Rf2File.ConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.ConRefsetFileFields.ID.ordinal()];
                        conRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ConRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        conRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.ConRefsetFileFields.ACTIVE.ordinal()];
                        conRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        conRefsetsWriter.write(namespace + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.ConRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        conRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ConRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        conRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case CONCEPT:
                        String con1 = parts[Rf2File.ConRefsetFileFields.CONCEPT.ordinal()];
                        String con1SctId = getExistingSctId(con1);
                        if (con1SctId == null) {
                            con1SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con1), con1SctId);
                            conceptCounter++;
                        }
                        conRefsetsWriter.write(con1SctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.ConConRefsetFileFields field : Rf2File.ConConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.ConConRefsetFileFields.ID.ordinal()];
                        conConRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ConConRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        conConRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.ConConRefsetFileFields.ACTIVE.ordinal()];
                        conConRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        conConRefsetsWriter.write(namespace + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.ConConRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        conConRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ConConRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        conConRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case CONCEPT1:
                        String con1 = parts[Rf2File.ConConRefsetFileFields.CONCEPT1.ordinal()];
                        String con1SctId = getExistingSctId(con1);
                        if (con1SctId == null) {
                            con1SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con1), con1SctId);
                            conceptCounter++;
                        }
                        conConRefsetsWriter.write(con1SctId + field.seperator);

                        break;

                    case CONCEPT2:
                        String con2 = parts[Rf2File.ConConRefsetFileFields.CONCEPT2.ordinal()];
                        String con2SctId = getExistingSctId(con2);
                        if (con2SctId == null) {
                            con2SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con2), con2SctId);
                            conceptCounter++;
                        }
                        conConRefsetsWriter.write(con2SctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConConRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.ConConConRefsetFileFields field : Rf2File.ConConConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.ConConConRefsetFileFields.ID.ordinal()];
                        conConConRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ConConConRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        conConConRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.ConConConRefsetFileFields.ACTIVE.ordinal()];
                        conConConRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        conConConRefsetsWriter.write(namespace + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.ConConConRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        conConConRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ConConConRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        conConConRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case CONCEPT1:
                        String con1 = parts[Rf2File.ConConConRefsetFileFields.CONCEPT1.ordinal()];
                        String con1SctId = getExistingSctId(con1);
                        if (con1SctId == null) {
                            con1SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con1), con1SctId);
                            conceptCounter++;
                        }
                        conConConRefsetsWriter.write(con1SctId + field.seperator);

                        break;

                    case CONCEPT2:
                        String con2 = parts[Rf2File.ConConConRefsetFileFields.CONCEPT2.ordinal()];
                        String con2SctId = getExistingSctId(con2);
                        if (con2SctId == null) {
                            con2SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con2), con2SctId);
                            conceptCounter++;
                        }
                        conConConRefsetsWriter.write(con2SctId + field.seperator);

                        break;

                    case CONCEPT3:
                        String con3 = parts[Rf2File.ConConConRefsetFileFields.CONCEPT3.ordinal()];
                        String con3SctId = getExistingSctId(con3);
                        if (con3SctId == null) {
                            con3SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con3), con3SctId);
                            conceptCounter++;
                        }
                        conConConRefsetsWriter.write(con3SctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConStrRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.ConConStrRefsetFileFields field : Rf2File.ConConStrRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.ConConStrRefsetFileFields.ID.ordinal()];
                        conConStrRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ConConStrRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        conConStrRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.ConConStrRefsetFileFields.ACTIVE.ordinal()];
                        conConStrRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        conConStrRefsetsWriter.write(namespace + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.ConConStrRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        conConStrRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ConConStrRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        conConStrRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case CONCEPT1:
                        String con1 = parts[Rf2File.ConConStrRefsetFileFields.CONCEPT1.ordinal()];
                        String con1SctId = getExistingSctId(con1);
                        if (con1SctId == null) {
                            con1SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con1), con1SctId);
                            conceptCounter++;
                        }
                        conConStrRefsetsWriter.write(con1SctId + field.seperator);

                        break;

                    case CONCEPT2:
                        String con2 = parts[Rf2File.ConConStrRefsetFileFields.CONCEPT2.ordinal()];
                        String con2SctId = getExistingSctId(con2);
                        if (con2SctId == null) {
                            con2SctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(con2), con2SctId);
                            conceptCounter++;
                        }
                        conConStrRefsetsWriter.write(con2SctId + field.seperator);

                        break;

                    case STRING:
                        String str = parts[Rf2File.ConConStrRefsetFileFields.STRING.ordinal()];
                        String strSctId = getExistingSctId(str);
                        if (strSctId == null) {
                            strSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(str), strSctId);
                            conceptCounter++;
                        }
                        conConStrRefsetsWriter.write(strSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processStringRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.StringRefsetFileFields field : Rf2File.StringRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.StringRefsetFileFields.ID.ordinal()];
                        stringRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.StringRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        stringRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.StringRefsetFileFields.ACTIVE.ordinal()];
                        stringRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        stringRefsetsWriter.write(namespace + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.StringRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        stringRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.StringRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        stringRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case STRING:
                        String str = parts[Rf2File.StringRefsetFileFields.STRING.ordinal()];
                        String strSctId = getExistingSctId(str);
                        if (strSctId == null) {
                            strSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(str), strSctId);
                            conceptCounter++;
                        }
                        conRefsetsWriter.write(strSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processUuidToSctMap() throws IOException {
        Set<UUID> keySet = uuidToSctMap.keySet();
        for (UUID uuid : keySet) {
            String sctId = uuidToSctMap.get(uuid);
            for (Rf2File.UuidToSctMapFileFields field : Rf2File.UuidToSctMapFileFields.values()) {

                switch (field) {
                    case UUID:
                        uuidToSctMapWriter.write(uuid + field.seperator);

                        break;

                    case SCT:
                        uuidToSctMapWriter.write(sctId + field.seperator);

                        break;
                }
            }
        }
    }

    private Integer convertStatus(String statusString) throws ValidationException, IOException {
        UUID status = UUID.fromString(statusString);
        int inactive = 0;
        int active = 1;
        if (status.equals(SnomedMetadataRf1.INAPPROPRIATE_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf1.RETIRED_INACTIVE_STATUS_RF1.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf1.CURRENT_RF1.getLenient().getPrimUuid())) {
            return active;
        } else if (status.equals(SnomedMetadataRf2.INAPPROPRIATE_COMPONENT_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid())) {
            return active;
        } 
        
        return null;
    }

    private String getExistingSctId(String uuidString) throws IOException {
        UUID conceptUuid = UUID.fromString(uuidString);
        boolean idExists = false;
        String conceptSctId = null;
        if (this.uuidToExistingSctMap.containsKey(conceptUuid)) {
            conceptSctId = this.uuidToExistingSctMap.get(conceptUuid);
            return conceptSctId;
        }
        if (this.uuidToSctMap.containsKey(conceptUuid)) {
            conceptSctId = this.uuidToSctMap.get(conceptUuid);
            return conceptSctId;
        }
        if (!idExists) {
            ComponentChroncileBI component = store.getComponent(conceptUuid);
            Collection<IdBI> ids = (Collection<IdBI>) component.getAdditionalIds();
            if (ids != null) {
                for (IdBI id : ids) {
                    if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                        conceptSctId = id.getDenotation().toString();
                        this.uuidToExistingSctMap.put(conceptUuid, conceptSctId);
                        return conceptSctId;
                    }
                }
            } else {
                return null;
            }
        }
        return conceptSctId;
    }

    public void close() throws IOException {
        if (conceptsWriter != null) {
            conceptsWriter.close();
        }

        if (descriptionsWriter != null) {
            descriptionsWriter.close();
        }

        if (relationshipsWriter != null) {
            relationshipsWriter.close();
        }

        if (identifiersWriter != null) {
            identifiersWriter.close();
        }

        if (conRefsetsWriter != null) {
            conRefsetsWriter.close();
        }

        if (conConRefsetsWriter != null) {
            conConRefsetsWriter.close();
        }

        if (conConConRefsetsWriter != null) {
            conConConRefsetsWriter.close();
        }

        if (conConStrRefsetsWriter != null) {
            conConStrRefsetsWriter.close();
        }

        if (stringRefsetsWriter != null) {
            stringRefsetsWriter.close();
        }

        if (uuidToSctMapWriter != null) {
            uuidToSctMapWriter.close();
        }

        if (conceptsReader != null) {
            conceptsReader.close();
        }

        if (descriptionsReader != null) {
            descriptionsReader.close();
        }

        if (relationshipsReader != null) {
            relationshipsReader.close();
        }

        if (identifiersReader != null) {
            identifiersReader.close();
        }

        if (conRefsetsReader != null) {
            conRefsetsReader.close();
        }

        if (conConRefsetsReader != null) {
            conConRefsetsReader.close();
        }

        if (conConConRefsetsReader != null) {
            conConConRefsetsReader.close();
        }

        if (conConStrRefsetsReader != null) {
            conConStrRefsetsReader.close();
        }

        if (stringRefsetsReader != null) {
            stringRefsetsReader.close();
        }
    }
}
