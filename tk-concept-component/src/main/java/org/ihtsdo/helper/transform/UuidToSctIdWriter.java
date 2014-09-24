/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.transform;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * The Class UuidToSctIdWriter converts UUID based Release Format 2 (RF2) files
 * into SCT ID based RF2 files.
 */
public class UuidToSctIdWriter {

    private String namespace;
    private String module;
    private File directory;
    private Writer conceptsWriter;
    private Writer descriptionsWriter;
    private Writer identifiersWriter;
    private Writer privateIdentifiersWriter;
    private Writer relationshipsWriter;
    private Writer relationshipsStatedWriter;
    private Writer langRefsetsWriter;
    private Writer otherLangRefsetsWriter;
    private Writer modDependWriter;
    private Writer descTypeWriter;
    private Writer refsetDescWriter;
    private Writer uuidToSctMapWriter;
    private Writer associationWriter;
    private Writer attributeValueWriter;
    private Writer jifReactantsWriter;
    private BufferedReader conceptsReader;
    private BufferedReader descriptionsReader;
    private BufferedReader identifiersReader;
    private BufferedReader privateIdentifiersReader;
    private BufferedReader relationshipsReader;
    private BufferedReader relationshipsStatedReader;
    private BufferedReader langRefsetsReader;
    private BufferedReader otherLangRefsetsReader;
    private BufferedReader modDependReader;
    private BufferedReader descTypeReader;
    private BufferedReader refsetDescReader;
    private BufferedReader uuidSctMapReader;
    private BufferedReader associationReader;
    private BufferedReader attributeValueReader;
    private BufferedReader jifReactantsReader;
    private TerminologyStoreDI store;
    private HashMap<UUID, String> uuidToSctMap = new HashMap<>();
    private HashMap<UUID, String> uuidToExistingSctMap = new HashMap<>();
    private boolean makePrivateAltIdsFile;
    private UuidSnomedMapHandler handler;
    File[] uuidFiles;
    private ReleaseType releaseType;
    private COUNTRY_CODE countryCode;
    private Date effectiveDate;
    private File content;
    private String uuidIdScheme = "900000000000002006";
    private ViewCoordinate vc;
    private ArrayList<Long> idFileList = new ArrayList<>();
    private HashSet<Long> idFileSet = new HashSet<>();
    private ArrayList<Long> combinedIdList = new ArrayList<>();
    private HashSet<Long> combinedIdSet = new HashSet<>();
    private HashSet<Long> conceptIds = new HashSet<>();
    private HashMap<Long, ArrayList<Long>> conceptInfRelMap = new HashMap<>();
    private HashMap<Long, ArrayList<Long>> conceptStatedRelMap = new HashMap<>();
    private HashSet<Long> inactives = new HashSet<>();
    private HashSet<Long> actives = new HashSet<>();
    private HashSet<Long> activeDups = new HashSet<>();
    private HashSet<Long> activeDescriptionIdSet = new HashSet<>();
    private HashSet<Long> inactiveDescriptionIdSet = new HashSet<>();
    private HashSet<Long> langRefsetIdSet = new HashSet<>();

    /**
     * Instantiates a new uuid to sct id writer for release files from the
     * specified
     * <code>namespace</code> and
     * <code>module</code>.
     *
     * @param namespace the namespace responsible for the release
     * @param module the module which is being released
     * @param directory the directory contain the uuid based RF2 files
     * @param handler the uuid-snomed map handler to use for converting to SCT
     * IDs
     * @param releaseType the release type of the files
     */
    public UuidToSctIdWriter(String namespace, String module,
            File directory, UuidSnomedMapHandler handler,
            ReleaseType releaseType, COUNTRY_CODE countryCode,
            Date effectiveDate,
            ViewCoordinate releaseViewCoordinate) {
        this.namespace = namespace;
        this.module = module;
        this.directory = directory;
        this.store = Ts.get();
        this.handler = handler;
        this.releaseType = releaseType;
        this.countryCode = countryCode;
        this.effectiveDate = effectiveDate;
        this.vc = releaseViewCoordinate;
    }

    /**
     * The Enum exisitingSctIds lists metadata SCT IDs needed for the release
     * files.
     */
    private enum exisitingSctIds {

        /**
         * The SCT ID for the concept: primitive.
         */
        PRIMITIVE("900000000000074008"),
        /**
         * The SCT ID for the concept: defined.
         */
        DEFINED("900000000000073002"),
        /**
         * The SCT ID for the concept: case sensitive.
         */
        CASE_SENSITIVE("900000000000017005"),
        /**
         * The SCT ID for the concept: case insensitive.
         */
        CASE_INSENSITIVE("900000000000448009");
        /**
         * The value.
         */
        public final String value;

        /**
         * Instantiates a new exisiting sct ids for the give SCT ID
         * <code>value</code>.
         *
         * @param value the SCT ID value to add
         */
        private exisitingSctIds(String value) {
            this.value = value;
        }
    }

    /**
     * Converts and writes the SCT ID based RF2 files.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    public void write() throws IOException, ContradictionException {
        setup();

        String moduleId = module;
        module = getExistingSctId(module);
        if (module == null) {
            module = handler.getWithGeneration(UUID.fromString(moduleId), SctIdGenerator.TYPE.CONCEPT).toString();
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
        
        String assocationLine = associationReader.readLine();
        assocationLine = associationReader.readLine();
        while (assocationLine != null) {
            processAssociationRefset(assocationLine);
            assocationLine = associationReader.readLine();
        }
        
        String attribValueLine = attributeValueReader.readLine();
        attribValueLine = attributeValueReader.readLine();
        while(attribValueLine != null){
            processAttributeValueRefset(attribValueLine);
            attribValueLine = attributeValueReader.readLine();
        }

        String jifReactantsLine = jifReactantsReader.readLine();
        jifReactantsLine = jifReactantsReader.readLine();
        while(jifReactantsLine != null){
            processJifReactantsRefset(jifReactantsLine);
            jifReactantsLine = jifReactantsReader.readLine();
        }

        String langRefLine = langRefsetsReader.readLine();
        langRefLine = langRefsetsReader.readLine();
        while (langRefLine != null) {
            processLangRefsets(langRefLine);
            langRefLine = langRefsetsReader.readLine();
        }
        if(otherLangRefsetsReader != null){
            String otherLangRefLine = otherLangRefsetsReader.readLine();
            otherLangRefLine = otherLangRefsetsReader.readLine();
            while (otherLangRefLine != null) {
                processOtherLangRefsets(otherLangRefLine);
                otherLangRefLine = otherLangRefsetsReader.readLine();
            }
        }
        
        String modDependLine = modDependReader.readLine();
        modDependLine = modDependReader.readLine();
        while (modDependLine != null) {
            processModuleDependency(modDependLine);
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

        if (privateIdentifiersReader != null && privateIdentifiersWriter != null) {
            String privateIdLine = privateIdentifiersReader.readLine();
            privateIdLine = privateIdentifiersReader.readLine();
            while (privateIdLine != null) {
                processIdentifiers(privateIdLine, privateIdentifiersWriter);
                privateIdLine = privateIdentifiersReader.readLine();
            }
        }
        processSimpleRefsets();
        processConNumRefsets();
        processUuidToSctMap();
        sanityCheck();
    }

    /**
     * Sets up the file readers and writers.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
    private void setup() throws IOException {
        uuidFiles = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String string) {
                return string.endsWith(".txt");
            }
        });
        File releaseFolder = new File(directory, "SnomedCT_Release_" + countryCode 
                + namespace +"_"+ TimeHelper.getShortFileDateFormat().format(effectiveDate));
        releaseFolder.mkdir();
        File rf2 = new File(releaseFolder, "RF2Release");
        rf2.mkdir();
        File releaseType = new File(rf2, this.releaseType.suffix);
        releaseType.mkdir();
        File terminology = new File(releaseType, "Terminology");
        terminology.mkdir();
        File refset = new File(releaseType, "Refset");
        refset.mkdir();
        content = new File(refset, "Content");
        content.mkdir();
        File languageDir = new File(refset, "Language");
        languageDir.mkdir();
        File metadata = new File(refset, "Metadata");
        metadata.mkdir();

        File conceptsFileUuid = null;
        File descriptionsFileUuid = null;
        File relationshipsFileUuid = null;
        File identifiersFileUuid = null;
        File privateIdentifiersFileUuid = null;
        File statedRelFileUuid = null;
        File associationFileUuid = null;
        File attributeValueFileUuid = null;
        File jifReactantsFileUuid = null;
        File langRefsetsFileUuid = null;
        File otherLangRefsetsFileUuid = null;
        File modDependFileUuid = null;
        File descTypeFileUuid = null;
        File refsetDescFileUuid = null;

        for (File inputFile : uuidFiles) {
            if (inputFile.getName().startsWith("sct2_Concept_UUID_" + this.releaseType.suffix)) {
                conceptsFileUuid = inputFile;
                conceptsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Description_UUID_" + this.releaseType.suffix)) {
                descriptionsFileUuid = inputFile;
                descriptionsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Relationship_UUID_" + this.releaseType.suffix)) {
                relationshipsFileUuid = inputFile;
                relationshipsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_Identifier_UUID_" + this.releaseType.suffix)) {
                identifiersFileUuid = inputFile;
                identifiersReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_IdentifierAuxiliary_UUID_" + this.releaseType.suffix)) {
                privateIdentifiersFileUuid = inputFile;
                privateIdentifiersReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("sct2_StatedRelationship_UUID_" + this.releaseType.suffix)) {
                statedRelFileUuid = inputFile;
                relationshipsStatedReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("der2_cRefset_AssociationReference_UUID" + this.releaseType.suffix)) {
                associationFileUuid = inputFile;
                associationReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            }else if (inputFile.getName().startsWith("der2_cRefset_AttributeValue_UUID" + this.releaseType.suffix)) {
                attributeValueFileUuid = inputFile;
                attributeValueReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            }else if (inputFile.getName().startsWith("der2_cRefset_JifReactants_UUID" + this.releaseType.suffix)) {
                jifReactantsFileUuid = inputFile;
                jifReactantsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            }else if (inputFile.getName().startsWith("der2_cRefset_Language_UUID" + this.releaseType.suffix)
                    && inputFile.getName().contains(LANG_CODE.EN.getFormatedLanguageCode())) {
                langRefsetsFileUuid = inputFile;
                langRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("der2_cRefset_Language_UUID" + this.releaseType.suffix)
                    && !inputFile.getName().contains(LANG_CODE.EN.getFormatedLanguageCode())) {
                otherLangRefsetsFileUuid = inputFile;
                otherLangRefsetsReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("der2_ssRefset_ModuleDependency_UUID" + this.releaseType.suffix)) {
                modDependFileUuid = inputFile;
                modDependReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("der2_ciRefset_DescriptionType_UUID" + this.releaseType.suffix)) {
                descTypeFileUuid = inputFile;
                descTypeReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            } else if (inputFile.getName().startsWith("der2_cciRefset_RefsetDescriptor_UUID" + this.releaseType.suffix)) {
                refsetDescFileUuid = inputFile;
                refsetDescReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
            }
        }

        File conceptsFile = new File(terminology,
                conceptsFileUuid.getName().replace("sct2_Concept_UUID_", "sct2_Concept_"));
        File descriptionsFile = new File(terminology,
                descriptionsFileUuid.getName().replace("sct2_Description_UUID_", "sct2_Description_"));
        File relationshipsFile = new File(terminology,
                relationshipsFileUuid.getName().replace("sct2_Relationship_UUID_", "sct2_Relationship_"));
        File identifiersFile = new File(terminology,
                identifiersFileUuid.getName().replace("sct2_Identifier_UUID_", "sct2_Identifier_"));
        File privateIdentifiersFile = null;
        if (privateIdentifiersFileUuid != null) {
            privateIdentifiersFile = new File(terminology,
                    privateIdentifiersFileUuid.getName().replace("sct2_IdentifierAuxiliary_UUID_", "sct2_IdentifierAuxiliary_"));
        }
        File statedRelFile = new File(terminology,
                statedRelFileUuid.getName().replace("sct2_StatedRelationship_UUID_", "sct2_StatedRelationship_"));
        File associationFile = new File(content,
                associationFileUuid.getName().replace("der2_cRefset_AssociationReference_UUID", "der2_cRefset_AssociationReference"));
        File attributeValueFile = new File(content,
                attributeValueFileUuid.getName().replace("der2_cRefset_AttributeValue_UUID", "der2_cRefset_AttributeValue"));
        File jifReactantsFile = new File(content,
                jifReactantsFileUuid.getName().replace("der2_cRefset_JifReactants_UUID", "der2_cRefset_JifReactants"));
        File langRefsetsFile = new File(languageDir,
                langRefsetsFileUuid.getName().replace("der2_cRefset_Language_UUID", "der2_cRefset_Language"));
        File otherLangRefsetsFile = null;
        if(otherLangRefsetsFileUuid != null){
            otherLangRefsetsFile = new File(languageDir,
                otherLangRefsetsFileUuid.getName().replace("der2_cRefset_Language_UUID", "der2_cRefset_Language"));
        }
        File modDependFile = new File(metadata,
                modDependFileUuid.getName().replace("der2_ssRefset_ModuleDependency_UUID", "der2_ssRefset_ModuleDependency"));
        File descTypeFile = new File(metadata,
                descTypeFileUuid.getName().replace("der2_ciRefset_DescriptionType_UUID", "der2_ciRefset_DescriptionType"));
        File refsetDescFile = new File(metadata,
                refsetDescFileUuid.getName().replace("der2_cciRefset_RefsetDescriptor_UUID", "der2_cciRefset_RefsetDescriptor"));
        File uuidToSctIdsFile = new File(directory,
                refsetDescFileUuid.getName().replace("der2_cciRefset_RefsetDescriptor_UUID", "sct2_to_uuid_map"));

        FileOutputStream conceptOs = new FileOutputStream(conceptsFile);
        conceptsWriter = new BufferedWriter(new OutputStreamWriter(conceptOs, "UTF8"));
        FileOutputStream descriptionOs = new FileOutputStream(descriptionsFile);
        descriptionsWriter = new BufferedWriter(new OutputStreamWriter(descriptionOs, "UTF8"));
        FileOutputStream relOs = new FileOutputStream(relationshipsFile);
        relationshipsWriter = new BufferedWriter(new OutputStreamWriter(relOs, "UTF8"));
        FileOutputStream pubIdOs = new FileOutputStream(identifiersFile);
        identifiersWriter = new BufferedWriter(new OutputStreamWriter(pubIdOs, "UTF8"));
        if (privateIdentifiersFile != null) {
            FileOutputStream privIdOs = new FileOutputStream(privateIdentifiersFile);
            privateIdentifiersWriter = new BufferedWriter(new OutputStreamWriter(privIdOs, "UTF8"));
            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
                privateIdentifiersWriter.write(field.headerText + field.seperator);
            }
        }
        FileOutputStream relStatedOs = new FileOutputStream(statedRelFile);
        relationshipsStatedWriter = new BufferedWriter(new OutputStreamWriter(relStatedOs, "UTF8"));
        FileOutputStream associationOs = new FileOutputStream(associationFile);
        associationWriter = new BufferedWriter(new OutputStreamWriter(associationOs, "UTF8"));
        FileOutputStream attributeValueOs = new FileOutputStream(attributeValueFile);
        attributeValueWriter = new BufferedWriter(new OutputStreamWriter(attributeValueOs, "UTF8"));
        FileOutputStream jifReactantsOs = new FileOutputStream(jifReactantsFile);
        jifReactantsWriter = new BufferedWriter(new OutputStreamWriter(jifReactantsOs, "UTF8"));
        FileOutputStream langRefOs = new FileOutputStream(langRefsetsFile);
        langRefsetsWriter = new BufferedWriter(new OutputStreamWriter(langRefOs, "UTF8"));
        if(otherLangRefsetsFile != null){
            FileOutputStream langOs = new FileOutputStream(otherLangRefsetsFile);
            otherLangRefsetsWriter = new BufferedWriter(new OutputStreamWriter(langOs, "UTF8"));
        }
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
        
        for (Rf2File.AssociationRefsetFileFields field : Rf2File.AssociationRefsetFileFields.values()) {
            associationWriter.write(field.headerText + field.seperator);
        }
        
        for (Rf2File.AttribValueRefsetFileFields field : Rf2File.AttribValueRefsetFileFields.values()) {
            attributeValueWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.JifReactantsRefsetFileFields field : Rf2File.JifReactantsRefsetFileFields.values()) {
            jifReactantsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
            langRefsetsWriter.write(field.headerText + field.seperator);
        }
        
        if (otherLangRefsetsWriter != null) {
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                otherLangRefsetsWriter.write(field.headerText + field.seperator);
            }
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

    /**
     * Processes the concepts file and converts to SCT IDs.
     *
     * @param line a line of the concept file
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConceptAttribute(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            int status = 100;
            long conceptId = 0;
            for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.ConceptsFileFields.ACTIVE.ordinal()];
                        status = convertStatus(statusUuid);
                        conceptsWriter.write(status
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
                            conceptSctId = handler.getWithGeneration(UUID.fromString(concept), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(concept), conceptSctId);
                        }
                        conceptsWriter.write(conceptSctId + field.seperator);
                        combinedIdList.add(Long.parseLong(conceptSctId));
                        combinedIdSet.add(Long.parseLong(conceptSctId));
                        conceptIds.add(Long.parseLong(conceptSctId));
                        conceptId = Long.parseLong(conceptSctId);
                        break;

                    case MODULE_ID:
                        conceptsWriter.write(module + field.seperator);

                        break;
                }
            }
            if (status == 0) {
                inactives.add(conceptId);
            } else {
                if (!actives.contains(conceptId)) {
                    actives.add(conceptId);
                } else {
                    activeDups.add(conceptId);
                }

            }
        }
    }

    /**
     * Processes the descriptions file and converts to SCT IDs.
     *
     * @param line a line of the description file
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processDescription(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            int status = 100;
            long descId = 0;
            for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.DescriptionsFileFields.ACTIVE.ordinal()];
                        status = convertStatus(statusUuid);
                        descriptionsWriter.write(status
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
                            descSctId = handler.getWithGeneration(UUID.fromString(desc), SctIdGenerator.TYPE.DESCRIPTION).toString();
                            this.uuidToSctMap.put(UUID.fromString(desc), descSctId);
                        }

                        descriptionsWriter.write(descSctId + field.seperator);
                        combinedIdList.add(Long.parseLong(descSctId));
                        combinedIdSet.add(Long.parseLong(descSctId));
                        statusUuid = parts[Rf2File.DescriptionsFileFields.ACTIVE.ordinal()];
                        status = convertStatus(statusUuid);
                        if(status == 1){
                            activeDescriptionIdSet.add(Long.parseLong(descSctId));
                        }else{
                            inactiveDescriptionIdSet.add(Long.parseLong(descSctId));
                        }
                        descId = Long.parseLong(descSctId);

                        break;

                    case MODULE_ID:
                        descriptionsWriter.write(module + field.seperator);

                        break;

                    case CONCEPT_ID:
                        String conceptId = parts[Rf2File.DescriptionsFileFields.CONCEPT_ID.ordinal()];
                        String conceptSctId = getExistingSctId(conceptId);
                        if (conceptSctId == null) {
                            conceptSctId = handler.getWithGeneration(UUID.fromString(conceptId), SctIdGenerator.TYPE.CONCEPT).toString();
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
                            descTypeSctId = handler.getWithGeneration(UUID.fromString(descType), SctIdGenerator.TYPE.CONCEPT).toString();
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
            if (status == 0) {
                inactives.add(descId);
            } else {
                if (!actives.contains(descId)) {
                    actives.add(descId);
                } else {
                    activeDups.add(descId);
                }

            }
        }
    }

    /**
     * Processes the inferred relationships and converts to SCT IDs.
     *
     * @param line a line of the inferred relationship file
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processRelationship(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            long relId = 0;
            long conceptId = 0;
            int status = 100;
            for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.RelationshipsFileFields.ACTIVE.ordinal()];
                        status = convertStatus(statusUuid);
                        relationshipsWriter.write(status + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.RelationshipsFileFields.EFFECTIVE_TIME.ordinal()];
                        relationshipsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ID:
                        String rel = parts[Rf2File.RelationshipsFileFields.ID.ordinal()];
                        String relSctId = getExistingSctId(rel);
                        if (relSctId == null) {
                            relSctId = handler.getWithGeneration(UUID.fromString(rel), SctIdGenerator.TYPE.RELATIONSHIP).toString();
                            this.uuidToSctMap.put(UUID.fromString(rel), relSctId);
                        }
                        relationshipsWriter.write(relSctId + field.seperator);
                        combinedIdList.add(Long.parseLong(relSctId));
                        combinedIdSet.add(Long.parseLong(relSctId));
                        relId = Long.parseLong(relSctId);
                        break;

                    case MODULE_ID:
                        relationshipsWriter.write(module + field.seperator);

                        break;

                    case SOURCE_ID:
                       String source = parts[Rf2File.RelationshipsFileFields.SOURCE_ID.ordinal()];
                        String sourceSctId = getExistingSctId(source);
                        if (sourceSctId == null) {
                            sourceSctId = handler.getWithGeneration(UUID.fromString(source), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(source), sourceSctId);
                        }
                        relationshipsWriter.write(sourceSctId + field.seperator);
                        conceptId = Long.parseLong(sourceSctId);

                        break;

                    case DESTINATION_ID:
                        String dest = parts[Rf2File.RelationshipsFileFields.DESTINATION_ID.ordinal()];
                        String destSctId = getExistingSctId(dest);
                        if (destSctId == null) {
                            destSctId = handler.getWithGeneration(UUID.fromString(dest), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(dest), destSctId);
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
                            typeSctId = handler.getWithGeneration(UUID.fromString(type), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
                        }
                        relationshipsWriter.write(typeSctId + field.seperator);

                        break;

                    case CHARCTERISTIC_ID:
                        String relChar = parts[Rf2File.RelationshipsFileFields.CHARCTERISTIC_ID.ordinal()];
                        String relCharSctId = getExistingSctId(relChar);
                        if (relCharSctId == null) {
                            relCharSctId = handler.getWithGeneration(UUID.fromString(relChar), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(relChar), relCharSctId);
                        }
                        relationshipsWriter.write(relCharSctId + field.seperator);

                        break;


                    case MODIFIER_ID:
                        String modifier = parts[Rf2File.RelationshipsFileFields.MODIFIER_ID.ordinal()];
                        String modifierSctId = getExistingSctId(modifier);
                        if (modifierSctId == null) {
                            modifierSctId = handler.getWithGeneration(UUID.fromString(modifier), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(modifier), modifierSctId);
                        }
                        relationshipsWriter.write(modifierSctId
                                + field.seperator);

                        break;
                }
            }
            if (conceptInfRelMap.containsKey(conceptId)) {
                conceptInfRelMap.get(conceptId).add(relId);
            } else {
                ArrayList<Long> list = new ArrayList<>();
                list.add(relId);
                conceptInfRelMap.put(conceptId, list);
            }
            if (status == 0) {
                inactives.add(relId);
            } else {
                if (!actives.contains(relId)) {
                    actives.add(relId);
                } else {
                    activeDups.add(relId);
                }

            }
        }
    }

    /**
     * Processes the stated relationships file and converts to SCT IDs.
     *
     * @param line a line of the stated relationships file
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processStatedRelationship(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            long relId = 0;
            long conceptId = 0;
            int status = 100;
            for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
                switch (field) {
                    case ACTIVE:
                        String statusUuid = parts[Rf2File.RelationshipsFileFields.ACTIVE.ordinal()];
                        status = convertStatus(statusUuid);
                        relationshipsStatedWriter.write(status + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.RelationshipsFileFields.EFFECTIVE_TIME.ordinal()];
                        relationshipsStatedWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ID:
                        String rel = parts[Rf2File.RelationshipsFileFields.ID.ordinal()];
                        String relSctId = getExistingSctId(rel);
                        if (relSctId == null) {
                            relSctId = handler.getWithGeneration(UUID.fromString(rel), SctIdGenerator.TYPE.RELATIONSHIP).toString();
                            this.uuidToSctMap.put(UUID.fromString(rel), relSctId);
                        }
                        relationshipsStatedWriter.write(relSctId + field.seperator);
                        combinedIdList.add(Long.parseLong(relSctId));
                        combinedIdSet.add(Long.parseLong(relSctId));
                        relId = Long.parseLong(relSctId);
                        break;

                    case MODULE_ID:
                        relationshipsStatedWriter.write(module + field.seperator);

                        break;

                    case SOURCE_ID:
                        String source = parts[Rf2File.RelationshipsFileFields.SOURCE_ID.ordinal()];
                        String sourceSctId = getExistingSctId(source);
                        if (sourceSctId == null) {
                            sourceSctId = handler.getWithGeneration(UUID.fromString(source), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(source), sourceSctId);
                        }
                        relationshipsStatedWriter.write(sourceSctId + field.seperator);
                        conceptId = Long.parseLong(sourceSctId);

                        break;

                    case DESTINATION_ID:
                        String dest = parts[Rf2File.RelationshipsFileFields.DESTINATION_ID.ordinal()];
                        String destSctId = getExistingSctId(dest);
                        if (destSctId == null) {
                            destSctId = handler.getWithGeneration(UUID.fromString(dest), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(dest), destSctId);
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
                            typeSctId = handler.getWithGeneration(UUID.fromString(type), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
                        }
                        relationshipsStatedWriter.write(typeSctId + field.seperator);

                        break;

                    case CHARCTERISTIC_ID:
                        String relChar = parts[Rf2File.RelationshipsFileFields.CHARCTERISTIC_ID.ordinal()];
                        String relCharSctId = getExistingSctId(relChar);
                        if (relCharSctId == null) {
                            relCharSctId = handler.getWithGeneration(UUID.fromString(relChar), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(relChar), relCharSctId);
                        }
                        relationshipsStatedWriter.write(relCharSctId + field.seperator);

                        break;

                    case MODIFIER_ID:
                        String modifier = parts[Rf2File.RelationshipsFileFields.MODIFIER_ID.ordinal()];
                        String modifierSctId = getExistingSctId(modifier);
                        if (modifierSctId == null) {
                            modifierSctId = handler.getWithGeneration(UUID.fromString(modifier), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(modifier), modifierSctId);
                        }
                        relationshipsStatedWriter.write(modifierSctId
                                + field.seperator);

                        break;
                }
            }
            if(conceptStatedRelMap.containsKey(conceptId)){
                conceptStatedRelMap.get(conceptId).add(relId);
            }else{
                ArrayList<Long> list = new ArrayList<>();
                list.add(relId);
                conceptStatedRelMap.put(conceptId, list);
            }
            if (status == 0) {
                inactives.add(relId);
            } else {
                if (!actives.contains(relId)) {
                    actives.add(relId);
                } else {
                    activeDups.add(relId);
                }

            }
        }
    }

    /**
     * Processes the identifiers file and converts to SCT IDs.
     *
     * @param line a line of the identifiers file
     * @param writer the output file writer
     * @throws IOException signals that an I/O exception has occurred
     */
private void processIdentifiers(String line, Writer writer) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {

                switch (field) {
                    case IDENTIFIER_SCHEME_ID:
                        String schemeId = parts[Rf2File.IdentifiersFileFields.IDENTIFIER_SCHEME_ID.ordinal()];
                        String schemeSctId = getExistingSctId(schemeId);
                        if (schemeSctId == null) {
                            schemeSctId = handler.getWithGeneration(UUID.fromString(schemeId), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(schemeId), schemeSctId);
                        }
                        writer.write(schemeSctId + field.seperator);

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
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        writer.write(rcSctId + field.seperator);
                        idFileList.add(Long.parseLong(rcSctId));
                        idFileSet.add(Long.parseLong(rcSctId));

                        break;
                }
            }
        }
    }
    
    /**
     * Must happen after all components have been processed.
     * @param line
     * @throws IOException 
     */
    private void processAssociationRefset(String line) throws IOException {
        if (line != null) {
            String[] parts = line.split("\t");
            for (Rf2File.AssociationRefsetFileFields field : Rf2File.AssociationRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        String memberUuid = parts[Rf2File.AssociationRefsetFileFields.ID.ordinal()];
                        associationWriter.write(memberUuid + field.seperator);

                        break;
                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.AssociationRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        associationWriter.write(effectiveDateString + field.seperator);

                        break;
                    case ACTIVE:
                        String status = parts[Rf2File.AssociationRefsetFileFields.ACTIVE.ordinal()];
                        associationWriter.write(convertStatus(status) + field.seperator);

                        break;
                    case MODULE_ID:
                        associationWriter.write(module + field.seperator);
                        break;
                    case REFSET_ID:
                        String refsetId = parts[Rf2File.AssociationRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        associationWriter.write(refsetSctId + field.seperator);

                        break;
                    case REFERENCED_COMPONENT_ID:
                        //TODO assuming this is already assigned
                        String rcId = parts[Rf2File.AssociationRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rcId);
                        associationWriter.write(rcSctId + field.seperator);
                        break;
                    case TARGET:
                        String targetId = parts[Rf2File.AssociationRefsetFileFields.TARGET.ordinal()];
                        String targetSctId = getExistingSctId(targetId);
                        if (targetSctId == null) {
                            targetSctId = handler.getWithGeneration(UUID.fromString(targetId), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(targetId), targetSctId);
                        }
                        associationWriter.write(targetSctId + field.seperator);
                        break;
                }
            }
        }
    }
    
    /**
     * Must happen after all components have been processed.
     * @param line
     * @throws IOException 
     */
    private void processAttributeValueRefset(String line) throws IOException{
        if (line != null) {
            String[] parts = line.split("\t");
        for (Rf2File.AttribValueRefsetFileFields field : Rf2File.AttribValueRefsetFileFields.values()) {
            switch (field) {
                case ID:
                        String memberUuid = parts[Rf2File.AttribValueRefsetFileFields.ID.ordinal()];
                        attributeValueWriter.write(memberUuid + field.seperator);

                        break;
                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.AttribValueRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        attributeValueWriter.write(effectiveDateString + field.seperator);

                        break;
                    case ACTIVE:
                        String status = parts[Rf2File.AttribValueRefsetFileFields.ACTIVE.ordinal()];
                        attributeValueWriter.write(convertStatus(status) + field.seperator);

                        break;
                    case MODULE_ID:
                        attributeValueWriter.write(module + field.seperator);

                        break;
                    case REFSET_ID:
                        String refsetId = parts[Rf2File.AttribValueRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        attributeValueWriter.write(refsetSctId + field.seperator);

                        break;
                case REFERENCED_COMPONENT_ID:
                    //TODO assuming this is already assigned
                    String rcId = parts[Rf2File.AssociationRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rcId);
                        attributeValueWriter.write(rcSctId + field.seperator);
                    break;
                case VALUE_ID:
                    //concept
                   String valueId = parts[Rf2File.AttribValueRefsetFileFields.VALUE_ID.ordinal()];
                        String valueSctId = getExistingSctId(valueId);
                        if (valueSctId == null) {
                            valueSctId = handler.getWithGeneration(UUID.fromString(valueId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(valueId), valueSctId);
                        }
                        attributeValueWriter.write(valueSctId + field.seperator);

                        break;
            }
        }
        }
    }

    /**
     * Must happen after all components have been processed.
     * @param line
     * @throws IOException 
     */
    private void processJifReactantsRefset(String line) throws IOException{
        if (line != null) {
            String[] parts = line.split("\t");
        for (Rf2File.JifReactantsRefsetFileFields field : Rf2File.JifReactantsRefsetFileFields.values()) {
            switch (field) {
                case ID:
                        String memberUuid = parts[Rf2File.JifReactantsRefsetFileFields.ID.ordinal()];
                        jifReactantsWriter.write(memberUuid + field.seperator);

                        break;
                    case EFFECTIVE_TIME:
                        String effectiveDateString = parts[Rf2File.JifReactantsRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                        jifReactantsWriter.write(effectiveDateString + field.seperator);

                        break;
                    case ACTIVE:
                        String status = parts[Rf2File.JifReactantsRefsetFileFields.ACTIVE.ordinal()];
                        jifReactantsWriter.write(convertStatus(status) + field.seperator);

                        break;
                    case MODULE_ID:
                        jifReactantsWriter.write(module + field.seperator);

                        break;
                    case REFSET_ID:
                        String refsetId = parts[Rf2File.JifReactantsRefsetFileFields.REFSET_ID.ordinal()];
                        String refsetSctId = getExistingSctId(refsetId);
                        if (refsetSctId == null) {
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        jifReactantsWriter.write(refsetSctId + field.seperator);

                        break;
                case REFERENCED_COMPONENT_ID:
                    //TODO assuming this is already assigned
                    String rcId = parts[Rf2File.JifReactantsRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rcId);
                        jifReactantsWriter.write(rcSctId + field.seperator);
                    break;
                case REACTANTS:
                    //concept
                   String valueId = parts[Rf2File.JifReactantsRefsetFileFields.REACTANTS.ordinal()];
                        String valueSctId = getExistingSctId(valueId);
                        if (valueSctId == null) {
                            valueSctId = handler.getWithGeneration(UUID.fromString(valueId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(valueId), valueSctId);
                        }
                        jifReactantsWriter.write(valueSctId + field.seperator);

                        break;
            }
        }
        }
    }

    /**
     * Processes an English language refsets file.
     *
     * @param line a line from the language refsets file
     * @throws IOException signals that an I/O exception has occurred
     */
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
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        langRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.LanguageRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        langRefsetsWriter.write(rcSctId + field.seperator);
                        langRefsetIdSet.add(Long.parseLong(rcSctId));

                        break;

                    case ACCEPTABILITY:
                        String accept = parts[Rf2File.LanguageRefsetFileFields.ACCEPTABILITY.ordinal()];
                        String acceptSctId = getExistingSctId(accept);
                        if (acceptSctId == null) {
                            acceptSctId = handler.getWithGeneration(UUID.fromString(accept), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(accept), acceptSctId);
                        }
                        langRefsetsWriter.write(acceptSctId + field.seperator);

                        break;
                }
            }
        }
    }

    /**
     * Processes any other language refsets file and converts to SCT IDs.
     *
     * @param line a line from the language refset file
     * @throws IOException signals that an I/O exception has occurred
     */
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
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        otherLangRefsetsWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.LanguageRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        otherLangRefsetsWriter.write(rcSctId + field.seperator);
                        langRefsetIdSet.add(Long.parseLong(rcSctId));

                        break;

                    case ACCEPTABILITY:
                        String accept = parts[Rf2File.LanguageRefsetFileFields.ACCEPTABILITY.ordinal()];
                        String acceptSctId = getExistingSctId(accept);
                        if (acceptSctId == null) {
                            acceptSctId = handler.getWithGeneration(UUID.fromString(accept), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(accept), acceptSctId);
                        }
                        otherLangRefsetsWriter.write(acceptSctId + field.seperator);

                        break;
                }
            }
        }
    }

    /**
     * Processes the module dependency file and converts to SCT IDs.
     *
     * @param line a line from the module dependency file
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processModuleDependency(String line) throws IOException {
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
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId),
                                    SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        modDependWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.ModuleDependencyFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
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

    /**
     * Processes the description type file and converts to SCT IDs.
     *
     * @param line a line from the description type file
     * @throws IOException signals that an I/O exception has occurred
     */
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
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        descTypeWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.DescTypeFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        descTypeWriter.write(rcSctId + field.seperator);

                        break;

                    case DESC_FORMAT:
                        String format = parts[Rf2File.DescTypeFileFields.DESC_FORMAT.ordinal()];
                        String formatSctId = getExistingSctId(format);
                        if (formatSctId == null) {
                            formatSctId = handler.getWithGeneration(UUID.fromString(format), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(format), formatSctId);
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

    /**
     * Processes the refset description file and converts to SCT IDs.
     *
     * @param line a line from the refset description file
     * @throws IOException signals that an I/O exception has occurred
     */
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
                            refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                            this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                        }
                        refsetDescWriter.write(refsetSctId + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        String rc = parts[Rf2File.RefsetDescriptorFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                        String rcSctId = getExistingSctId(rc);
                        if (rcSctId == null) {
                            rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                        }
                        refsetDescWriter.write(rcSctId + field.seperator);

                        break;

                    case ATTRIB_DESC:
                        String desc = parts[Rf2File.RefsetDescriptorFileFields.ATTRIB_DESC.ordinal()];
                        String descSctId = getExistingSctId(desc);
                        if (descSctId == null) {
                            descSctId = handler.getWithGeneration(UUID.fromString(desc), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(desc), descSctId);
                        }
                        refsetDescWriter.write(descSctId + field.seperator);

                        break;

                    case ATTRIB_TYPE:
                        String type = parts[Rf2File.RefsetDescriptorFileFields.ATTRIB_TYPE.ordinal()];
                        String typeSctId = getExistingSctId(type);
                        if (typeSctId == null) {
                            typeSctId = handler.getWithGeneration(UUID.fromString(type), SctIdGenerator.TYPE.CONCEPT).toString();
                            this.uuidToSctMap.put(UUID.fromString(type), typeSctId);
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
    /**
     * Converts uuid-based simple refset files to SCT ID based files.
     * 
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processSimpleRefsets() throws  IOException {
        for (File inputFile : uuidFiles) {
            if (inputFile.getName().toLowerCase().contains("simplerefset_uuid" + releaseType.toString().toLowerCase())) {
                BufferedReader simpleRefsetReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
                File simpleRefsetFile = new File(content,
                        inputFile.getName().replace("_UUID", ""));
                FileOutputStream outputStream = new FileOutputStream(simpleRefsetFile);
                BufferedWriter simpleRefsetWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8"));

                for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                    simpleRefsetWriter.write(field.headerText + field.seperator);
                }

                String refsetLine = simpleRefsetReader.readLine();
                refsetLine = simpleRefsetReader.readLine();
                while (refsetLine != null) {
                    String[] parts = refsetLine.split("\t");
                    for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                        switch (field) {
                            case ID:
                                String memberUuid = parts[Rf2File.SimpleRefsetFileFields.ID.ordinal()];
                                simpleRefsetWriter.write(memberUuid + field.seperator);

                                break;

                            case EFFECTIVE_TIME:
                                String effectiveDateString = parts[Rf2File.SimpleRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                                simpleRefsetWriter.write(effectiveDateString + field.seperator);

                                break;

                            case ACTIVE:
                                String status = parts[Rf2File.SimpleRefsetFileFields.ACTIVE.ordinal()];
                                simpleRefsetWriter.write(convertStatus(status) + field.seperator);

                                break;

                            case MODULE_ID:
                                simpleRefsetWriter.write(module + field.seperator);

                                break;

                            case REFSET_ID:
                                String refsetId = parts[Rf2File.SimpleRefsetFileFields.REFSET_ID.ordinal()];
                                String refsetSctId = getExistingSctId(refsetId);
                                if (refsetSctId == null) {
                                    refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString(); //TODO akf: subset?
                                    this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                                }
                                simpleRefsetWriter.write(refsetSctId + field.seperator);

                                break;

                            case REFERENCED_COMPONENT_ID:
                                String rc = parts[Rf2File.SimpleRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                                String rcSctId = getExistingSctId(rc);
                                if (rcSctId == null) {
                                    rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                                    this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                                }
                                simpleRefsetWriter.write(rcSctId + field.seperator);

                                break;
                        }
                    }

                    refsetLine = simpleRefsetReader.readLine();
                }

                simpleRefsetReader.close();
                simpleRefsetWriter.close();
            }
        }
    }
    
       /**
     * Converts uuid-based concept number refset files to SCT ID based files.
     * 
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processConNumRefsets() throws  IOException {
        for (File inputFile : uuidFiles) {
            if (inputFile.getName().toLowerCase().contains("conceptnumberrefset_uuid" + releaseType.toString().toLowerCase())) {
                BufferedReader conNumRefsetReader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF8"));
                File conNumRefsetFile = new File(content,
                        inputFile.getName().replace("_UUID", ""));
                FileOutputStream outputStream = new FileOutputStream(conNumRefsetFile);
                BufferedWriter conNumRefsetWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF8"));

                for (Rf2File.ConNumRefsetFileFields field : Rf2File.ConNumRefsetFileFields.values()) {
                    conNumRefsetWriter.write(field.headerText + field.seperator);
                }

                String refsetLine = conNumRefsetReader.readLine();
                refsetLine = conNumRefsetReader.readLine();
                while (refsetLine != null) {
                    String[] parts = refsetLine.split("\t");
                    for (Rf2File.ConNumRefsetFileFields field : Rf2File.ConNumRefsetFileFields.values()) {
                        switch (field) {
                            case ID:
                                String memberUuid = parts[Rf2File.ConNumRefsetFileFields.ID.ordinal()];
                                conNumRefsetWriter.write(memberUuid + field.seperator);

                                break;

                            case EFFECTIVE_TIME:
                                String effectiveDateString = parts[Rf2File.ConNumRefsetFileFields.EFFECTIVE_TIME.ordinal()];
                                conNumRefsetWriter.write(effectiveDateString + field.seperator);

                                break;

                            case ACTIVE:
                                String status = parts[Rf2File.ConNumRefsetFileFields.ACTIVE.ordinal()];
                                conNumRefsetWriter.write(convertStatus(status) + field.seperator);

                                break;

                            case MODULE_ID:
                                conNumRefsetWriter.write(module + field.seperator);

                                break;

                            case REFSET_ID:
                                String refsetId = parts[Rf2File.ConNumRefsetFileFields.REFSET_ID.ordinal()];
                                String refsetSctId = getExistingSctId(refsetId);
                                if (refsetSctId == null) {
                                    refsetSctId = handler.getWithGeneration(UUID.fromString(refsetId), SctIdGenerator.TYPE.CONCEPT).toString();
                                    this.uuidToSctMap.put(UUID.fromString(refsetId), refsetSctId);
                                }
                                conNumRefsetWriter.write(refsetSctId + field.seperator);

                                break;

                            case REFERENCED_COMPONENT_ID:
                                String rc = parts[Rf2File.ConNumRefsetFileFields.REFERENCED_COMPONENT_ID.ordinal()];
                                String rcSctId = getExistingSctId(rc);
                                if (rcSctId == null) {
                                    rcSctId = handler.getWithGeneration(UUID.fromString(rc), SctIdGenerator.TYPE.CONCEPT).toString();
                                    this.uuidToSctMap.put(UUID.fromString(rc), rcSctId);
                                }
                                conNumRefsetWriter.write(rcSctId + field.seperator);

                                break;
                                
                             case ADDITIONAL_CONCEPT_ID:
                                String con1 = parts[Rf2File.ConNumRefsetFileFields.ADDITIONAL_CONCEPT_ID.ordinal()];
                                String con1Id = getExistingSctId(con1);
                                if (con1Id == null) {
                                    con1Id = handler.getWithGeneration(UUID.fromString(con1), SctIdGenerator.TYPE.CONCEPT).toString();
                                    this.uuidToSctMap.put(UUID.fromString(con1), con1Id);
                                }
                                conNumRefsetWriter.write(con1Id + field.seperator);

                                break;
                                 
                             case NUMBER:
                                String number = parts[Rf2File.ConNumRefsetFileFields.NUMBER.ordinal()];
                                conNumRefsetWriter.write(number + field.seperator);

                                break;
                        }
                    }

                    refsetLine = conNumRefsetReader.readLine();
                }

                conNumRefsetReader.close();
                conNumRefsetWriter.close();
            }
        }
    }

    /**
     * Creates a uuid to SCT ID map file.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Convert status to either active or inactive.
     *
     * @param statusString a String representing a description of the status to
     * convert
     * @return the integer representation of the status as either active, 1, or
     * inactive, 0
     * @throws ValidationException indicates a validation exception has occurred
     * @throws IOException signals that an I/O exception has occurred
     */
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

    /**
     * Checks to see of the component, specified by the
     * <code>uuidString</code>, already has an associated uuid.
     *
     * @param uuidString the uuid string associated with the component in question
     * @return a string representing the SCT ID, <code>null</code> if not found
     * @throws IOException signals that an I/O exception has occurred
     */
    private String getExistingSctId(String uuidString) throws IOException {
        UUID componentUuid = UUID.fromString(uuidString);
        boolean idExists = false;
        String componentSctId = null;
        if (this.uuidToExistingSctMap.containsKey(componentUuid)) {
            componentSctId = this.uuidToExistingSctMap.get(componentUuid);
            return componentSctId;
        }
        if (this.uuidToSctMap.containsKey(componentUuid)) {
            componentSctId = this.uuidToSctMap.get(componentUuid);
            return componentSctId;
        }
        if (!idExists) {
            ComponentChronicleBI component = store.getComponent(componentUuid);
            if (component != null) {
                Collection<IdBI> ids = (Collection<IdBI>) component.getAdditionalIds();
                if (ids != null) {
                    for (IdBI id : ids) {
                        if (id.getAuthorityNid() == TermAux.SCT_ID_AUTHORITY.getLenient().getNid()) {
                            componentSctId = id.getDenotation().toString();
                            this.uuidToExistingSctMap.put(componentUuid, componentSctId);
                            return componentSctId;
                        }
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
        return componentSctId;
    }

    /**
     * Closes the file writers.
     *
     * @throws IOException signals that an I/O exception has occurred
     */
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
        
        if(associationWriter != null){
            associationWriter.close();
        }
        
        if(attributeValueWriter != null){
            attributeValueWriter.close();
        }

        if(jifReactantsWriter != null){
        	jifReactantsWriter.close();
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
        
        if(associationReader != null){
            associationReader.close();
        }
        
        if(attributeValueReader != null){
            attributeValueReader.close();
        }
        
        if(jifReactantsReader != null){
        	jifReactantsReader.close();
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
    
    private void sanityCheck() {
        System.out.println("### CHECKING Identifiers file for duplicates.");
        if (idFileList.size() != idFileSet.size()) {
            ArrayList checkedIds = new ArrayList();
            for (Long id : idFileList) {
                if (!checkedIds.contains(id)) {
                    checkedIds.add(id);
                } else {
                    System.out.println("Duplicate ID: " + id);
                }
            }
        } else {
            System.out.println("Passed.");
        }

        System.out.println("### CHECKING for at least one stated relationship for each concept.");
        boolean passed = true;
        Set<Long> statedRelConcepts = conceptStatedRelMap.keySet();
        for(Long id : conceptIds){
            if(!statedRelConcepts.contains(id)){
                passed = false;
                System.out.println("Found concept ID with no stated relationship. ID: " + id);
            }
        }
        if(passed){
            System.out.println("Passed.");
        }
                
        System.out.println("### CHECKING for at least one inferred relationship for each concept.");
        passed = true;
        Set<Long> statedInfConcepts = conceptInfRelMap.keySet();
        for(Long id : conceptIds){
            if(!statedInfConcepts.contains(id)){
                passed = false;
                System.out.println("Found concept ID with no inferred relationship. ID: " + id);
            }
        }
        if(passed){
            System.out.println("Passed.");
        }
        
        System.out.println("### CHECKING for one active version of each component.");
        for(long id : activeDups){
            System.out.println("Found component with more than one active value. ID: " + id);
        }
        if(activeDups.isEmpty()){
            System.out.println("Passed.");
        }
        
        System.out.println("### Found " + inactives.size() + " inactive versions.");

        System.out.println("### CHECKING Identifiers file against component IDs.");
        passed = true;
        for (Long id : idFileSet) {
            if (!combinedIdSet.contains(id)) {
                passed = false;
                System.out.println("Found ID in Indentifiers file not present in data. ID: " + id);
            }
        }
        for (Long id : combinedIdSet) {
            if (!idFileSet.contains(id)) {
                passed = false;
                System.out.println("Found ID in data not present in Identifiers file. ID: " + id);
            }
        }
        if (passed) {
            System.out.println("Passed.");
        }

        if (releaseType.equals(ReleaseType.DELTA) || releaseType.equals(ReleaseType.SNAPSHOT)) {
            System.out.println("### CHECKING for one version of each component.");
            if (combinedIdList.size() != combinedIdSet.size()) {
                ArrayList checkedIds = new ArrayList();
                for (Long id : combinedIdList) {
                    if (!checkedIds.contains(id)) {
                        checkedIds.add(id);
                    } else {
                        System.out.println("More than one version exists for ID: " + id);
                    }
                }
            } else {
                System.out.println("Passed.");
            }
        }
        System.out.println("### CHECKING for one language refset member for each description.");
        int count = 0;
        activeDescriptionIdSet.removeAll(inactiveDescriptionIdSet);
        for(Long id : activeDescriptionIdSet){
            if(!langRefsetIdSet.contains(id)){
                count++;
                System.out.println("Found description ID not present in a language refset file. ID: " + id);
            }
        }
        if(count > 0){
            System.out.println("Found " + count + " descriptions not present in language refsets.");
        }else{
            System.out.println("Passed.");
        }
    }
}
