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
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
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
    String module;
    File directory;
    Writer conceptsWriter;
    Writer descriptionsWriter;
    Writer identifiersWriter;
    Writer privateIdentifiersWriter;
    Writer relationshipsWriter;
    Writer relationshipsStatedWriter;
    Writer langRefsetsWriter;
    Writer otherLangRefsetsWriter;
    Writer modDependWriter;
    Writer descTypeWriter;
    Writer refsetDescWriter;
    Writer uuidToSctMapWriter;
    BufferedReader conceptsReader;
    BufferedReader descriptionsReader;
    BufferedReader identifiersReader;
    BufferedReader  privateIdentifiersReader;
    BufferedReader relationshipsReader;
    BufferedReader relationshipsStatedReader;
    BufferedReader langRefsetsReader;
    BufferedReader otherLangRefsetsReader;
    BufferedReader modDependReader;
    BufferedReader descTypeReader;
    BufferedReader refsetDescReader;
    BufferedReader uuidSctMapReader;
    TerminologyStoreDI store;
    HashMap<UUID, String> uuidToSctMap = new HashMap<UUID, String>();
    HashMap<UUID, String> uuidToExistingSctMap = new HashMap<UUID, String>();
    int conceptCounter = 1;
    int descCounter = 1;
    int relCounter = 1;
    boolean makePrivateAltIdsFile;

    public UuidToSctIdMapper(String namespace, String module,
            File directory) {
        this.namespace = namespace;
        this.module = module;
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

        String moduleId = module;
        module = getExistingSctId(module);
        if (module == null) {
            module = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
            conceptCounter++;
            uuidToSctMap.put(UUID.fromString(moduleId), module);
        }
        
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

        String statedRelLine = relationshipsStatedReader.readLine();
        statedRelLine = relationshipsStatedReader.readLine();
        while (statedRelLine != null) {
            processStatedRelationship(statedRelLine);
            statedRelLine = relationshipsStatedReader.readLine();
        }

        String langRefLine = langRefsetsReader.readLine();
        langRefLine = langRefsetsReader.readLine();
        while (langRefLine != null) {
            processLangRefsets(langRefLine);
            langRefLine = langRefsetsReader.readLine();
        }
        
        String otherLangRefLine = otherLangRefsetsReader.readLine();
        otherLangRefLine = otherLangRefsetsReader.readLine();
        while (otherLangRefLine != null) {
            processOtherLangRefsets(otherLangRefLine);
            otherLangRefLine = otherLangRefsetsReader.readLine();
        }

        String modDependLine = modDependReader.readLine();
        modDependLine = modDependReader.readLine();
        while (modDependLine != null) {
            processModuleDepedency(modDependLine);
            modDependLine = modDependReader.readLine();
        }

        String descTypeLine = descTypeReader.readLine();
        descTypeLine = descTypeReader.readLine();
        while (descTypeLine != null) {
            processDescType(descTypeLine);
            descTypeLine = descTypeReader.readLine();
        }

        String refsetDescLine = refsetDescReader.readLine();
        refsetDescLine = refsetDescReader.readLine();
        while (refsetDescLine != null) {
            processRefsetDesc(refsetDescLine);
            refsetDescLine = refsetDescReader.readLine();
        }
        
        String idLine = identifiersReader.readLine();
        idLine = identifiersReader.readLine();
        while (idLine != null) {
            processIdentifiers(idLine, identifiersWriter);
            idLine = identifiersReader.readLine();
        }
        
        if(privateIdentifiersReader != null && privateIdentifiersWriter != null){
            String privateIdLine = privateIdentifiersReader.readLine();
        privateIdLine = privateIdentifiersReader.readLine();
        while (privateIdLine != null) {
            processIdentifiers(privateIdLine, privateIdentifiersWriter);
            privateIdLine = privateIdentifiersReader.readLine();
        }
        }
        
        processUuidToSctMap();
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
        File privateIdentifiersFileUuid = null;
        File statedRelFileUuid = null;
        File langRefsetsFileUuid = null;
        File otherLangRefsetsFileUuid = null;
        File modDependFileUuid = null;
        File descTypeFileUuid = null;
        File refsetDescFileUuid = null;

        for (File inputFile : uuidFiles) {
            if (inputFile.getName().startsWith("sct2_Concept_UUID_")) {
                conceptsFileUuid = inputFile;
                        
                conceptsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Description_UUID_")) {
                descriptionsFileUuid = inputFile;
                descriptionsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Relationship_UUID_")) {
                relationshipsFileUuid = inputFile;
                relationshipsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Identifier_UUID_")) {
                identifiersFileUuid = inputFile;
                identifiersReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Identifier_Auxiliary_UUID_")) {
                privateIdentifiersFileUuid = inputFile;
                privateIdentifiersReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            }else if (inputFile.getName().startsWith("sct2_StatedRelationships_UUID_")) {
                statedRelFileUuid = inputFile;
                relationshipsStatedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_LangRefset_UUID_") &&
                    inputFile.getName().contains(LANG_CODE.EN.getFormatedLanguageCode())) {
                langRefsetsFileUuid = inputFile;
                langRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_LangRefset_UUID_")&&
                    !inputFile.getName().contains(LANG_CODE.EN.getFormatedLanguageCode())) {
                otherLangRefsetsFileUuid = inputFile;
                otherLangRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_ModuleDependency_UUID_")) {
                modDependFileUuid = inputFile;
                modDependReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_DescriptionType_UUID_")) {
                descTypeFileUuid = inputFile;
                descTypeReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_RefsetDescriptor_UUID_")) {
                refsetDescFileUuid = inputFile;
                refsetDescReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
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
        File privateIdentifiersFile = null;
        if(privateIdentifiersFileUuid != null){
            privateIdentifiersFile = new File(directory,
                privateIdentifiersFileUuid.getName().replace("sct2_Identifier_Auxiliary_UUID_", "sct2_Identifier_Auxiliary_"));
        }
        File statedRelFile = new File(directory,
                statedRelFileUuid.getName().replace("sct2_StatedRelationships_UUID_", "sct2_StatedRelationships_"));
        File langRefsetsFile = new File(directory,
                langRefsetsFileUuid.getName().replace("sct2_LangRefset_UUID_", "sct2_LangRefset_"));
        File otherLangRefsetsFile = new File(directory,
                otherLangRefsetsFileUuid.getName().replace("sct2_LangRefset_UUID_", "sct2_LangRefset_"));
        File modDependFile = new File(directory,
                modDependFileUuid.getName().replace("sct2_ModuleDependency_UUID_", "sct2_ModuleDependency_"));
        File descTypeFile = new File(directory,
                descTypeFileUuid.getName().replace("sct2_DescriptionType_UUID_", "sct2_DescriptionType_"));
        File refsetDescFile = new File(directory,
                refsetDescFileUuid.getName().replace("sct2_RefsetDescriptor_UUID_", "sct2_RefsetDescriptor_"));
        File uuidToSctIdsFile = new File(directory,
                refsetDescFileUuid.getName().replace("sct2_RefsetDescriptor_UUID_", "sct2_to_uuid_map"));
        
        FileOutputStream conceptOs = new FileOutputStream(conceptsFile);
        conceptsWriter = new BufferedWriter(new OutputStreamWriter(conceptOs, "UTF8"));
        FileOutputStream descriptionOs = new FileOutputStream(descriptionsFile);
        descriptionsWriter = new BufferedWriter(new OutputStreamWriter(descriptionOs, "UTF8"));
        FileOutputStream relOs = new FileOutputStream(relationshipsFile);
        relationshipsWriter = new BufferedWriter(new OutputStreamWriter(relOs, "UTF8"));
        FileOutputStream pubIdOs = new FileOutputStream(identifiersFile);
            identifiersWriter = new BufferedWriter(new OutputStreamWriter(pubIdOs, "UTF8"));
        if(privateIdentifiersFile != null){
            FileOutputStream privIdOs = new FileOutputStream(privateIdentifiersFile);
            privateIdentifiersWriter = new BufferedWriter(new OutputStreamWriter(privIdOs, "UTF8"));
        }
        FileOutputStream relStatedOs = new FileOutputStream(statedRelFile);
        relationshipsStatedWriter = new BufferedWriter(new OutputStreamWriter(relStatedOs, "UTF8"));
        FileOutputStream langRefOs = new FileOutputStream(langRefsetsFile);
        langRefsetsWriter = new BufferedWriter(new OutputStreamWriter(langRefOs, "UTF8"));
        FileOutputStream langOs = new FileOutputStream(otherLangRefsetsFile);
        otherLangRefsetsWriter = new BufferedWriter(new OutputStreamWriter(langOs, "UTF8"));
        FileOutputStream modDependOs = new FileOutputStream(modDependFile);
        modDependWriter = new BufferedWriter(new OutputStreamWriter(modDependOs, "UTF8"));
        FileOutputStream descTypeOs = new FileOutputStream(descTypeFile);
        descTypeWriter = new BufferedWriter(new OutputStreamWriter(descTypeOs, "UTF8"));
        FileOutputStream refDescOs = new FileOutputStream(refsetDescFile);
        refsetDescWriter = new BufferedWriter(new OutputStreamWriter(refDescOs, "UTF8"));
        FileOutputStream uuidSctMapOs = new FileOutputStream(uuidToSctIdsFile);
        uuidToSctMapWriter = new BufferedWriter(new OutputStreamWriter(uuidSctMapOs, "UTF8"));

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

        for (Rf2File.StatedRelationshipsFileFields field : Rf2File.StatedRelationshipsFileFields.values()) {
            relationshipsStatedWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
            langRefsetsWriter.write(field.headerText + field.seperator);
        }
        
        for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
            otherLangRefsetsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.ModuleDependencyFileFields field : Rf2File.ModuleDependencyFileFields.values()) {
            modDependWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.DescTypeFileFields field : Rf2File.DescTypeFileFields.values()) {
            descTypeWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields.values()) {
            refsetDescWriter.write(field.headerText + field.seperator);
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
                        conceptsWriter.write(module + field.seperator);

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
                        descriptionsWriter.write(module + field.seperator);

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
                        relationshipsWriter.write(module + field.seperator);

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
    
    private void processStatedRelationship(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.RelationshipsFileFields.ACTIVE.ordinal()];
                        relationshipsStatedWriter.write(convertStatus(statusUuid) + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.RelationshipsFileFields.EFFECTIVE_TIME.ordinal()];
                        relationshipsStatedWriter.write(effectiveDateString + field.seperator);

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
                        relationshipsStatedWriter.write(relSctId + field.seperator);

                        break;

                    case MODULE_ID:
                        relationshipsStatedWriter.write(module + field.seperator);

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
                        relationshipsStatedWriter.write(sourceSctId + field.seperator);


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
                        relationshipsStatedWriter.write(destSctId + field.seperator);

                        break;

                    case RELATIONSHIP_GROUP:
                        String group = parts[Rf2File.RelationshipsFileFields.RELATIONSHIP_GROUP.ordinal()];
                        relationshipsStatedWriter.write(group + field.seperator);

                        break;

                    case TYPE_ID:
                        String type = parts[Rf2File.RelationshipsFileFields.TYPE_ID.ordinal()];
                        String typeSctId = getExistingSctId(type);
                        if (typeSctId == null) {
                            typeSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
                        }
                        relationshipsStatedWriter.write(typeSctId + field.seperator);

                        break;

                    case CHARCTERISTIC_ID:
                        String relChar = parts[Rf2File.RelationshipsFileFields.CHARCTERISTIC_ID.ordinal()];
                        String relCharSctId = getExistingSctId(relChar);
                        if (relCharSctId == null) {
                            relCharSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(relChar), relCharSctId);
                        }
                        relationshipsStatedWriter.write(relCharSctId + field.seperator);

                        break;

                    case MODIFIER_ID:
                        String modifier = parts[Rf2File.RelationshipsFileFields.MODIFIER_ID.ordinal()];
                        String modifierSctId = getExistingSctId(modifier);
                        if (modifierSctId == null) {
                            modifierSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(modifier), modifierSctId);
                        }
                        relationshipsStatedWriter.write(modifierSctId
                                + field.seperator);

                        break;
                }
            }
        }
    }

    private void processIdentifiers(String line, Writer writer) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {

                switch (field) {
                    case IDENTIFIER_SCHEME_ID:
                        String schemeId = parts[Rf2File.IdentifiersFileFields.IDENTIFIER_SCHEME_ID.ordinal()];
                        writer.write(getExistingSctId(schemeId) + field.seperator);

                        break;

                    case ALTERNATE_IDENTIFIER:
                        String primUuid = parts[Rf2File.IdentifiersFileFields.ALTERNATE_IDENTIFIER.ordinal()];
                        writer.write(primUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.IdentifiersFileFields.EFFECTIVE_TIME.ordinal()];
                        writer.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.IdentifiersFileFields.ACTIVE.ordinal()];
                        writer.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        writer.write(module + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.IdentifiersFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        writer.write(rcSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processLangRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.LanguageRefsetFileFields.ID.ordinal()];
                        langRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.LanguageRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        langRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.LanguageRefsetFileFields.ACTIVE.ordinal()];
                        langRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        langRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.LanguageRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        langRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.LanguageRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        langRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case ACCEPTABILITY:
                        String accept = parts[Rf2File.LanguageRefsetFileFields.ACCEPTABILITY.ordinal()];
                        String acceptSctId = getExistingSctId(accept);
                        if (acceptSctId == null) {
                            acceptSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(accept), acceptSctId);
                            conceptCounter++;
                        }
                        langRefsetsWriter.write(acceptSctId + field.seperator);

                        break;
                }
            }
        }
    }
    
    private void processOtherLangRefsets(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.LanguageRefsetFileFields.ID.ordinal()];
                        otherLangRefsetsWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.LanguageRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        otherLangRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.LanguageRefsetFileFields.ACTIVE.ordinal()];
                        otherLangRefsetsWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        otherLangRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.LanguageRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        otherLangRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.LanguageRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        otherLangRefsetsWriter.write(rcSctId + field.seperator);

                        break;

                    case ACCEPTABILITY:
                        String accept = parts[Rf2File.LanguageRefsetFileFields.ACCEPTABILITY.ordinal()];
                        String acceptSctId = getExistingSctId(accept);
                        if (acceptSctId == null) {
                            acceptSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(accept), acceptSctId);
                            conceptCounter++;
                        }
                        otherLangRefsetsWriter.write(acceptSctId + field.seperator);

                        break;
                }
            }
        }
    }

    private void processModuleDepedency(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.ModuleDependencyFileFields field : Rf2File.ModuleDependencyFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.ModuleDependencyFileFields.ID.ordinal()];
                        modDependWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.ModuleDependencyFileFields.EFFECTIVE_TIME.ordinal()];
                        modDependWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.ModuleDependencyFileFields.ACTIVE.ordinal()];
                        modDependWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        modDependWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.ModuleDependencyFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        modDependWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ModuleDependencyFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        modDependWriter.write(rcSctId + field.seperator);

                        break;

                    case SOURCE_TIME:
                        String sourceTime = parts[Rf2File.ModuleDependencyFileFields.SOURCE_TIME.ordinal()];
                        modDependWriter.write(sourceTime + field.seperator);

                        break;

                    case TARGET_TIME:
                        String targetTime = parts[Rf2File.ModuleDependencyFileFields.TARGET_TIME.ordinal()];
                        modDependWriter.write(targetTime + field.seperator);

                        break;
                }
            }
        }
    }

    private void processDescType(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.DescTypeFileFields field : Rf2File.DescTypeFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.DescTypeFileFields.ID.ordinal()];
                        descTypeWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.DescTypeFileFields.EFFECTIVE_TIME.ordinal()];
                        descTypeWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.DescTypeFileFields.ACTIVE.ordinal()];
                        descTypeWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        descTypeWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.DescTypeFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        descTypeWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.DescTypeFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        descTypeWriter.write(rcSctId + field.seperator);

                        break;

                    case DESC_FORMAT:
                        String format = parts[Rf2File.DescTypeFileFields.DESC_FORMAT.ordinal()];
                        String formatSctId = getExistingSctId(format);
                        if (formatSctId == null) {
                            formatSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(format), formatSctId);
                            conceptCounter++;
                        }
                        descTypeWriter.write(formatSctId + field.seperator);

                        break;

                    case DESC_LENGTH:
                        String length = parts[Rf2File.DescTypeFileFields.DESC_LENGTH.ordinal()];
                        descTypeWriter.write(length + field.seperator);

                        break;
                }
            }
        }
    }

    private void processRefsetDesc(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.RefsetDescriptorFileFields.ID.ordinal()];
                        refsetDescWriter.write(memberUuid + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.RefsetDescriptorFileFields.EFFECTIVE_TIME.ordinal()];
                        refsetDescWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        String status = parts[Rf2File.RefsetDescriptorFileFields.ACTIVE.ordinal()];
                        refsetDescWriter.write(convertStatus(status) + field.seperator);

                        break;

                    case MODULE_ID:
                        refsetDescWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        String refsetId = parts[Rf2File.RefsetDescriptorFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT); //@afk: subset?
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        refsetDescWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.RefsetDescriptorFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            conceptCounter++;
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        refsetDescWriter.write(rcSctId + field.seperator);

                        break;

                    case ATTRIB_DESC:
                        String desc = parts[Rf2File.RefsetDescriptorFileFields.ATTRIB_DESC.ordinal()];
                        String descSctId = getExistingSctId(desc);
                        if (descSctId == null) {
                            descSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(desc), descSctId);
                            conceptCounter++;
                        }
                        refsetDescWriter.write(descSctId + field.seperator);

                        break;

                    case ATTRIB_TYPE:
                        String type = parts[Rf2File.RefsetDescriptorFileFields.ATTRIB_TYPE.ordinal()];
                        String typeSctId = getExistingSctId(type);
                        if (typeSctId == null) {
                            typeSctId = SctIdGenerator.generate(conceptCounter, namespace, SctIdGenerator.TYPE.CONCEPT);
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
                            conceptCounter++;
                        }
                        refsetDescWriter.write(typeSctId + field.seperator);

                        break;

                    case ATTRIB_ORDER:
                        String order = parts[Rf2File.RefsetDescriptorFileFields.ATTRIB_ORDER.ordinal()];
                        refsetDescWriter.write(order + field.seperator);

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
    
    private void writeUuidToSctMapFile() throws IOException {
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
        } else if (status.equals(SnomedMetadataRf2.ERRONEOUS_COMPONENT_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.AMBIGUOUS_COMPONENT_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.COMPONENT_MOVED_ELSEWHERE_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getPrimUuid())) {
            return inactive;
        } else if (status.equals(SnomedMetadataRf2.OUTDATED_COMPONENT_RF2.getLenient().getPrimUuid())) {
            return inactive;
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
            ComponentChronicleBI component = store.getComponent(conceptUuid);
            if (component != null) {
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
        
        if (privateIdentifiersWriter != null) {
            privateIdentifiersWriter.close();
        }

        if (relationshipsStatedWriter != null) {
            relationshipsStatedWriter.close();
        }

        if (langRefsetsWriter != null) {
            langRefsetsWriter.close();
        }
        
        if (otherLangRefsetsWriter != null) {
            otherLangRefsetsWriter.close();
        }

        if (modDependWriter != null) {
            modDependWriter.close();
        }

        if (descTypeWriter != null) {
            descTypeWriter.close();
        }

        if (refsetDescWriter != null) {
            refsetDescWriter.close();
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

        if (relationshipsStatedReader != null) {
            relationshipsStatedReader.close();
        }

        if (langRefsetsReader != null) {
            langRefsetsReader.close();
        }
        
        if (otherLangRefsetsReader != null) {
            otherLangRefsetsReader.close();
        }

        if (modDependReader != null) {
            modDependReader.close();
        }

        if (descTypeReader != null) {
            descTypeReader.close();
        }

        if (refsetDescReader != null) {
            refsetDescReader.close();
        }
    }
}
