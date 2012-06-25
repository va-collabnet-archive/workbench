/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.helper.export;

//~--- non-JDK imports --------------------------------------------------------
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.conattr.ConAttrChronicleBI;
import org.ihtsdo.tk.api.conattr.ConAttrVersionBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author kec
 */
public class Rf2Export implements ProcessUnfetchedConceptDataBI {

    NidBitSetBI conceptsToProcess;
    Writer conceptsWriter;
    COUNTRY_CODE country;
    Writer descriptionsWriter;
    Date effectiveDate;
    String effectiveDateString;
    Writer identifiersWriter;
    Writer publicIdentifiersWriter;
    LANG_CODE language;
    String namespace;
    String module;
    Writer relationshipsWriter;
    Writer relationshipsStatedWriter;
    ReleaseType releaseType;
    Set<Integer> sapNids;
    TerminologyStoreDI store;
    ViewCoordinate vc;
    Writer langRefsetsWriter;
    Writer otherLangRefsetsWriter;
    Writer modDependWriter;
    Writer descTypeWriter;
    Writer refsetDescWriter;
    Set<Integer> excludedRefsetIds;
    ConceptSpec uuidIdScheme = new ConceptSpec("SNOMED CT universally unique identifier (core metadata concept)",
            UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));
    Set<Integer> langRefexNids = new HashSet<Integer>();
    Set<ConceptSpec> descTypes = new HashSet<ConceptSpec>();
    ViewCoordinate newVc;
    ConceptVersionBI parentCv;
    public static UUID REFSET_DESC_NAMESPACE = UUID.fromString("d1871eb0-8a47-11e1-b0c4-0800200c9a66");
    public static UUID MODULE_DEPEND_NAMESPACE = UUID.fromString("d1871eb2-8a47-11e1-b0c4-0800200c9a66");
    public static UUID DESC_TYPE_NAMESPACE = UUID.fromString("d1871eb3-8a47-11e1-b0c4-0800200c9a66");
    boolean makePrivateIdFile;

    //~--- constructors --------------------------------------------------------
    public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
            String namespace, String module, Date effectiveDate, Set<Integer> sapNids, ViewCoordinate vc,
            Set<Integer> excludedRefsetIds, NidBitSetBI conceptsToProcess, boolean makePrivateIdFile)
            throws IOException {
        directory.mkdirs();
        this.releaseType = releaseType;
        this.effectiveDate = effectiveDate;
        this.language = language;
        this.country = country;
        this.namespace = namespace;
        this.module = module;
        this.sapNids = sapNids;
        this.store = Ts.get();
        this.vc = vc;
        this.conceptsToProcess = conceptsToProcess;
        this.effectiveDateString = TimeHelper.getShortFileDateFormat().format(effectiveDate);
        this.excludedRefsetIds = excludedRefsetIds;
        this.makePrivateIdFile = makePrivateIdFile;

        File conceptsFile = new File(directory,
                "sct2_Concept_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descriptionsFile = new File(directory,
                "sct2_Description_UUID_" + releaseType.suffix + "-"
                + language.getFormatedLanguageCode() + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File relationshipsFile = new File(directory,
                "sct2_Relationship_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File relationshipsStatedFile = new File(directory,
                "sct2_StatedRelationships_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File privateIdentifiersFile = null;
        File identifiersFile = null;
        if(makePrivateIdFile){
            privateIdentifiersFile = new File(directory,
                "sct2_Identifier_Auxiliary_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
            identifiersFile = new File(directory,
                "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        }else{
            identifiersFile = new File(directory,
                "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        }
        File langRefsetsFile = new File(directory,
                "sct2_LangRefset_UUID_" + releaseType.suffix + "_"
                + LANG_CODE.EN.getFormatedLanguageCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File otherLangRefsetsFile = new File(directory,
                "sct2_LangRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File modDependFile = new File(directory,
                "sct2_ModuleDependency_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descTypeFile = new File(directory,
                "sct2_DescriptionType_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File refsetDescFile = new File(directory,
                "sct2_RefsetDescriptor_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");

        FileOutputStream conceptOs = new FileOutputStream(conceptsFile);
        conceptsWriter = new BufferedWriter(new OutputStreamWriter(conceptOs, "UTF8"));
        FileOutputStream descriptionOs = new FileOutputStream(descriptionsFile);
        descriptionsWriter = new BufferedWriter(new OutputStreamWriter(descriptionOs, "UTF8"));
        FileOutputStream relOs = new FileOutputStream(relationshipsFile);
        relationshipsWriter = new BufferedWriter(new OutputStreamWriter(relOs, "UTF8"));
        FileOutputStream relStatedOs = new FileOutputStream(relationshipsStatedFile);
        relationshipsStatedWriter = new BufferedWriter(new OutputStreamWriter(relStatedOs, "UTF8"));
        if(makePrivateIdFile){
            FileOutputStream idOs = new FileOutputStream(identifiersFile);
            publicIdentifiersWriter = new BufferedWriter(new OutputStreamWriter(idOs, "UTF8"));
            FileOutputStream privIdOs = new FileOutputStream(privateIdentifiersFile);
            identifiersWriter = new BufferedWriter(new OutputStreamWriter(privIdOs, "UTF8"));
            
            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
                publicIdentifiersWriter.write(field.headerText + field.seperator);
            }
        }else{
            FileOutputStream pubIdOs = new FileOutputStream(identifiersFile);
            identifiersWriter = new BufferedWriter(new OutputStreamWriter(pubIdOs, "UTF8"));
        }
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


        for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
            conceptsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
            descriptionsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            relationshipsWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.StatedRelationshipsFileFields field : Rf2File.StatedRelationshipsFileFields.values()) {
            relationshipsStatedWriter.write(field.headerText + field.seperator);
        }


        for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
            identifiersWriter.write(field.headerText + field.seperator);
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

        ConceptSpec fsnDesc = new ConceptSpec("Fully specified name (core metadata concept)",
                UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));
        ConceptSpec synDesc = new ConceptSpec("Synonym (core metadata concept)",
                UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
        descTypes.add(synDesc);
        descTypes.add(fsnDesc);

        //setting up view coordinate with specific active values, and adding correct Is a
        newVc = Ts.get().getMetadataVC();
        newVc.setPositionSet(vc.getPositionSet());
        NidSetBI isaNids = newVc.getIsaTypeNids();
        isaNids.add(Snomed.IS_A.getLenient().getNid());

        ConceptSpec langRefexParent = new ConceptSpec("Language type reference set",
                UUID.fromString("84a0b03b-220c-3d69-8487-2e019c933687"));
        parentCv = Ts.get().getConceptVersion(newVc, langRefexParent.getLenient().getNid());
    }

    //~--- methods -------------------------------------------------------------
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

        if (relationshipsStatedWriter != null) {
            relationshipsStatedWriter.close();
        }

        if (identifiersWriter != null) {
            identifiersWriter.close();
        }
        
        if (publicIdentifiersWriter != null) {
            publicIdentifiersWriter.close();
        }

        if (langRefsetsWriter != null) {
            langRefsetsWriter.close();
        }

        if (otherLangRefsetsWriter != null) {
            otherLangRefsetsWriter.flush();
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
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    public void process(ConceptChronicleBI c) throws Exception {
        if (c.getPrimUuid().equals(UUID.fromString("568cd2a4-bcac-4cf7-a6af-29587639a354"))) {
            System.out.println(c);
        }
        ConAttrChronicleBI ca = c.getConAttrs();
        processConceptAttribute(ca);
        processIdentifiers(ca.getPrimUuid(), ca.getPrimordialVersion().getSapNid());

        if (c.getDescs() != null) {
            for (DescriptionChronicleBI d : c.getDescs()) {
                processDescription(d);
                processIdentifiers(d.getPrimUuid(), d.getPrimordialVersion().getSapNid());
                if (d.getAnnotations() != null) {
                    for (RefexChronicleBI annot : d.getAnnotations()) {
                        ConceptVersionBI cv = Ts.get().getConceptVersion(newVc, annot.getCollectionNid());
                            langRefexNids.add(annot.getCollectionNid());
                            processLangRefsets(annot);
                    }
                }
            }
        }

        if (c.getRelsOutgoing() != null) {
            for (RelationshipChronicleBI r : c.getRelsOutgoing()) {
                processRelationship(r);
                processIdentifiers(r.getPrimUuid(), r.getPrimordialVersion().getSapNid());
            }
        }
    }

    public void writeOneTimeFiles() throws IOException, NoSuchAlgorithmException {

        processModularDependency();

        for (ConceptSpec descType : descTypes) {
            processDescType(descType);
        }

        for (Integer refexNid : langRefexNids) {
            processRefsetDesc(refexNid);
            processRefsetDescAttribute(refexNid);
        }
    }

    private void processConceptAttribute(ConAttrChronicleBI ca) throws IOException {
        if (ca != null) {
            for (ConAttrVersionBI car : ca.getVersions(vc)) {
                if (sapNids.contains(car.getSapNid())) {
                    for (Rf2File.ConceptsFileFields field : Rf2File.ConceptsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                conceptsWriter.write(store.getUuidPrimordialForNid(car.getStatusNid())
                                        + field.seperator);

                                break;

                            case DEFINITION_STATUS_ID:
                                conceptsWriter.write(car.isDefined() + field.seperator);

                                break;

                            case EFFECTIVE_TIME:
                                conceptsWriter.write(effectiveDateString + field.seperator);

                                break;

                            case ID:
                                conceptsWriter.write(store.getUuidPrimordialForNid(car.getNid()) + field.seperator);

                                break;

                            case MODULE_ID:
                                conceptsWriter.write(module + field.seperator);

                                break;
                        }
                    }
                }
            }
        }
    }

    private void processDescription(DescriptionChronicleBI desc) throws IOException {
        if (desc != null) {
            for (DescriptionVersionBI descr : desc.getVersions(vc)) {
                if (sapNids.contains(descr.getSapNid())) {
                    for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
                        switch (field) {
                            case ACTIVE:
                                descriptionsWriter.write(store.getUuidPrimordialForNid(descr.getStatusNid())
                                        + field.seperator);

                                break;

                            case EFFECTIVE_TIME:
                                descriptionsWriter.write(effectiveDateString + field.seperator);

                                break;

                            case ID:
                                descriptionsWriter.write(store.getUuidPrimordialForNid(desc.getNid()) + field.seperator);

                                break;

                            case MODULE_ID:
                                descriptionsWriter.write(module + field.seperator);

                                break;

                            case CONCEPT_ID:
                                descriptionsWriter.write(store.getUuidPrimordialForNid(desc.getConceptNid())
                                        + field.seperator);

                                break;

                            case LANGUAGE_CODE:
                                descriptionsWriter.write(descr.getLang() + field.seperator);

                                break;

                            case TYPE_ID:
                                descriptionsWriter.write(store.getUuidPrimordialForNid(descr.getTypeNid())
                                        + field.seperator);

                                break;

                            case TERM:
                                descriptionsWriter.write(descr.getText() + field.seperator);

                                break;

                            case CASE_SIGNIFICANCE_ID:
                                descriptionsWriter.write(descr.isInitialCaseSignificant() + field.seperator);

                                break;
                        }
                    }
                }
            }
        }
    }

    private void processRelationship(RelationshipChronicleBI r) throws IOException {
        if (r != null) {
            for (RelationshipVersionBI rv : r.getVersions(vc)) {
                if (sapNids.contains(rv.getSapNid())) {
                    if (rv.getCharacteristicNid()
                            == SnomedMetadataRfx.getREL_CH_INFERRED_RELATIONSHIP_NID()) {
                        processInferredRelationship(rv);
                    } else if (rv.getCharacteristicNid()
                            == SnomedMetadataRfx.getREL_CH_STATED_RELATIONSHIP_NID()) {
                        processStatedRelationship(rv);
                    }
                }
            }
        }
    }

    private void processInferredRelationship(RelationshipVersionBI rv) throws IOException {
        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            switch (field) {
                case ACTIVE:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getStatusNid())
                            + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    relationshipsWriter.write(effectiveDateString + field.seperator);

                    break;

                case ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getNid()) + field.seperator);

                    break;

                case MODULE_ID:
                    relationshipsWriter.write(module + field.seperator);

                    break;

                case SOURCE_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getOriginNid())
                            + field.seperator);

                    break;

                case DESTINATION_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getDestinationNid())
                            + field.seperator);

                    break;

                case RELATIONSHIP_GROUP:
                    relationshipsWriter.write(rv.getGroup() + field.seperator);

                    break;

                case TYPE_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getTypeNid())
                            + field.seperator);

                    break;

                case CHARCTERISTIC_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getCharacteristicNid())
                            + field.seperator);

                    break;

                case MODIFIER_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(rv.getRefinabilityNid())
                            + field.seperator);

                    break;
            }
        }
    }

    private void processStatedRelationship(RelationshipVersionBI rv) throws IOException {
        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            switch (field) {
                case ACTIVE:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getStatusNid())
                            + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    relationshipsStatedWriter.write(effectiveDateString + field.seperator);

                    break;

                case ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getNid()) + field.seperator);

                    break;

                case MODULE_ID:
                    relationshipsStatedWriter.write(module + field.seperator);

                    break;

                case SOURCE_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getOriginNid())
                            + field.seperator);

                    break;

                case DESTINATION_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getDestinationNid())
                            + field.seperator);

                    break;

                case RELATIONSHIP_GROUP:
                    relationshipsStatedWriter.write(rv.getGroup() + field.seperator);

                    break;

                case TYPE_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getTypeNid())
                            + field.seperator);

                    break;

                case CHARCTERISTIC_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getCharacteristicNid())
                            + field.seperator);

                    break;

                case MODIFIER_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(rv.getRefinabilityNid())
                            + field.seperator);

                    break;
            }
        }
    }

    private void processIdentifiers(UUID primUuid, int primSapNid) throws IOException {
        if (primUuid != null) {
            if (sapNids.contains(primSapNid)) {
                for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {

                    switch (field) {
                        case IDENTIFIER_SCHEME_ID:
                            identifiersWriter.write(uuidIdScheme.getLenient().getPrimUuid() + field.seperator);

                            break;

                        case ALTERNATE_IDENTIFIER:
                            identifiersWriter.write(primUuid + field.seperator);

                            break;

                        case EFFECTIVE_TIME:
                            identifiersWriter.write(effectiveDateString + field.seperator);

                            break;

                        case ACTIVE:
                            identifiersWriter.write(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid() + field.seperator);

                            break;

                        case MODULE_ID:
                            identifiersWriter.write(module + field.seperator);

                            break;

                        case REFERENCED_COMPONENT_ID:
                            identifiersWriter.write(primUuid + field.seperator);

                            break;
                    }
                }
            }
        }
    }

    private void processLangRefsets(RefexChronicleBI r) throws IOException {
        if (r != null) {
            if (!excludedRefsetIds.contains(r.getCollectionNid())) {
                Collection<RefexVersionBI<?>> versions = r.getVersions(vc);
                for (RefexVersionBI rv : versions) {
                    if (!sapNids.contains(rv.getSapNid())) {
                        break;
                    } else {
                        if (r.getCollectionNid() == RefsetAux.EN_GB_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else if (r.getCollectionNid() == RefsetAux.EN_US_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else {
                            processOtherLang(rv);
                        }
                    }

                }
            }
        }
    }

    private void processLang(RefexVersionBI rv) throws IOException {
        if (rv != null) {
            if(RefexCnidVersionBI.class.isAssignableFrom(rv.getClass())){
                RefexCnidVersionBI rvc = (RefexCnidVersionBI) rv;
                for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                    switch (field) {
                        case ID:
                            langRefsetsWriter.write(rvc.getPrimUuid() + field.seperator);

                            break;

                        case EFFECTIVE_TIME:
                            langRefsetsWriter.write(effectiveDateString + field.seperator);

                            break;

                        case ACTIVE:
                            langRefsetsWriter.write(store.getComponent(rvc.getStatusNid()).getPrimUuid() + field.seperator);

                            break;

                        case MODULE_ID:
                            langRefsetsWriter.write(module + field.seperator);

                            break;

                        case REFSET_ID:
                            langRefexNids.add(rv.getCollectionNid());
                            langRefsetsWriter.write(store.getComponent(rvc.getCollectionNid()).getPrimUuid() + field.seperator);

                            break;

                        case REFERENCED_COMPONENT_ID:
                            langRefsetsWriter.write(store.getComponent(rvc.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                            break;

                        case ACCEPTABILITY:
                            langRefsetsWriter.write(store.getComponent(rvc.getCnid1()).getPrimUuid() + field.seperator);

                            break;
                    }
                }
            }
        }
    }

    private void processOtherLang(RefexVersionBI rv) throws IOException {
       if (rv != null) {
           if(RefexCnidVersionBI.class.isAssignableFrom(rv.getClass())){
                RefexCnidVersionBI rvc = (RefexCnidVersionBI) rv;
                for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                    switch (field) {
                        case ID:
                            otherLangRefsetsWriter.write(rvc.getPrimUuid() + field.seperator);

                            break;

                        case EFFECTIVE_TIME:
                            otherLangRefsetsWriter.write(effectiveDateString + field.seperator);

                            break;

                        case ACTIVE:
                            otherLangRefsetsWriter.write(store.getComponent(rvc.getStatusNid()).getPrimUuid() + field.seperator);

                            break;

                        case MODULE_ID:
                            otherLangRefsetsWriter.write(module + field.seperator);

                            break;

                        case REFSET_ID:
                            langRefexNids.add(rv.getCollectionNid());
                            otherLangRefsetsWriter.write(store.getComponent(rvc.getCollectionNid()).getPrimUuid() + field.seperator);

                            break;

                        case REFERENCED_COMPONENT_ID:
                            otherLangRefsetsWriter.write(store.getComponent(rvc.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                            break;

                        case ACCEPTABILITY:
                            otherLangRefsetsWriter.write(store.getComponent(rvc.getCnid1()).getPrimUuid() + field.seperator);

                            break;
                    }
               }
        }
      }
    }

    private void processRefsetDesc(int refexNid) throws IOException, NoSuchAlgorithmException {
        ConceptSpec refsetDescriptor = new ConceptSpec("Reference set descriptor reference set (foundation metadata concept)",
                UUID.fromString("5ddff82f-5aee-3b16-893f-6b7aa726cc4b"));
        ConceptSpec attribDesc = new ConceptSpec("Description in dialect (foundation metadata concept)",
                UUID.fromString("db73d522-612f-3dcd-a793-f62d4f0c41fe"));
        ConceptSpec attribType = new ConceptSpec("Description type component (foundation metadata concept)",
                UUID.fromString("4ea66278-f8a7-37d3-90fa-88c19cc107a6"));
        for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields.values()) {
            switch (field) {
                case ID:
                    //referenced component, attribute order
                    UUID uuid = UuidT5Generator.get(REFSET_DESC_NAMESPACE,
                            store.getUuidPrimordialForNid(refexNid).toString()
                            + 0);
                    refsetDescWriter.write(uuid + field.seperator);
                    
                    break;

                case EFFECTIVE_TIME:
                    refsetDescWriter.write(effectiveDateString + field.seperator);

                    break;

                case ACTIVE:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(SnomedMetadataRfx.getSTATUS_CURRENT_NID()) + field.seperator);

                    break;

                case MODULE_ID:
                    refsetDescWriter.write(module + field.seperator);

                    break;

                case REFSET_ID:
                    refsetDescWriter.write(refsetDescriptor.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(0 + field.seperator);

                    break;
            }
        }
    }
    
    private void processRefsetDescAttribute (int refexNid) throws IOException, NoSuchAlgorithmException {
        ConceptSpec refsetDescriptor = new ConceptSpec("Reference set descriptor reference set (foundation metadata concept)",
                UUID.fromString("5ddff82f-5aee-3b16-893f-6b7aa726cc4b"));
        ConceptSpec attribDesc = new ConceptSpec("Acceptability (foundation metadata concept)",
                UUID.fromString("26bac151-c2cf-3223-b417-c0a703cffff1"));
        ConceptSpec attribType = new ConceptSpec("Concept type component (foundation metadata concept)",
                UUID.fromString("78f69fb6-410c-3b5a-9120-53954592a80d"));
        for (Rf2File.RefsetDescriptorFileFields field : Rf2File.RefsetDescriptorFileFields.values()) {
            switch (field) {
                case ID:
                    UUID uuid = UuidT5Generator.get(REFSET_DESC_NAMESPACE,
                            store.getUuidPrimordialForNid(refexNid).toString()
                            + 1);
                    refsetDescWriter.write(uuid + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    refsetDescWriter.write(effectiveDateString + field.seperator);

                    break;

                case ACTIVE:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(SnomedMetadataRfx.getSTATUS_CURRENT_NID()) + field.seperator);

                    break;

                case MODULE_ID:
                    refsetDescWriter.write(module + field.seperator);

                    break;

                case REFSET_ID:
                    refsetDescWriter.write(refsetDescriptor.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(1 + field.seperator);

                    break;
            }
        }
    }

    private void processModularDependency() throws IOException, NoSuchAlgorithmException {
        ConceptSpec modDepenRefex = new ConceptSpec("Module dependency reference set (foundation metadata concept)",
                UUID.fromString("19076bfe-661f-39c2-860c-8706a37073b0"));
        ConceptSpec coreModule = new ConceptSpec("SNOMED CT core module (core metadata concept)",
                UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
        for (Rf2File.ModuleDependencyFileFields field : Rf2File.ModuleDependencyFileFields.values()) {
            switch (field) {
                case ID:
            //use module, referenced component, source time, destination time
                    UUID uuid = UuidT5Generator.get(MODULE_DEPEND_NAMESPACE,
                               module
                               + coreModule.getStrict(vc).getPrimUuid()
                               + effectiveDateString
                               + effectiveDateString);
                    modDependWriter.write(uuid + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    modDependWriter.write(effectiveDateString + field.seperator);

                    break;

                case ACTIVE:
                    modDependWriter.write(store.getUuidPrimordialForNid(SnomedMetadataRfx.getSTATUS_CURRENT_NID()) + field.seperator);

                    break;

                case MODULE_ID:
                    modDependWriter.write(module + field.seperator);

                    break;

                case REFSET_ID:
                    modDependWriter.write(modDepenRefex.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    modDependWriter.write(coreModule.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case SOURCE_TIME:
                    modDependWriter.write(effectiveDateString + field.seperator);

                    break;

                case TARGET_TIME:
                    modDependWriter.write(effectiveDateString + field.seperator);

                    break;
            }
        }
    }

    private void processDescType(ConceptSpec descType) throws IOException, NoSuchAlgorithmException {
        ConceptSpec descFormatRefex = new ConceptSpec("Description format reference set (foundation metadata concept)",
                UUID.fromString("c3467f1f-c0d5-3865-b169-61712ca03072"));
        ConceptSpec descFormat = new ConceptSpec("Plain text (foundation metadata concept)",
                UUID.fromString("9ec1bbb1-80f7-3db6-96b8-a942b0673db3"));
        for (Rf2File.DescTypeFileFields field : Rf2File.DescTypeFileFields.values()) {
            switch (field) {
                case ID:
                    //each referenced component should be unique in file
                    UUID uuid = UuidT5Generator.get(DESC_TYPE_NAMESPACE,
                            descType.getStrict(vc).getPrimUuid().toString());
                    descTypeWriter.write(uuid + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    descTypeWriter.write(effectiveDateString + field.seperator);

                    break;

                case ACTIVE:
                    descTypeWriter.write(store.getUuidPrimordialForNid(SnomedMetadataRfx.getSTATUS_CURRENT_NID()) + field.seperator);

                    break;

                case MODULE_ID:
                    descTypeWriter.write(module + field.seperator);

                    break;

                case REFSET_ID:
                    descTypeWriter.write(descFormatRefex.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    descTypeWriter.write(descType.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case DESC_FORMAT:
                    descTypeWriter.write(descFormat.getStrict(vc).getPrimUuid() + field.seperator);

                    break;

                case DESC_LENGTH:
                    int length = 0;
                    if (descType.getLenient().getNid()
                            == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()) {
                        length = 255;
                    }
                    if (descType.getLenient().getNid()
                            == SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid()) {
                        length = 1024;
                    }
                    descTypeWriter.write(length + field.seperator);

                    break;
            }
        }
    }

    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        process(fetcher.fetch());
    }

    //~--- get methods ---------------------------------------------------------
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptsToProcess;
    }
}
