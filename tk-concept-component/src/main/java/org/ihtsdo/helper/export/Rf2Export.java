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
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
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
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 * The Class Rf2Export generates a Release Format 2 style release files for a
 * specified set of concepts in the database. This class implements
 * <code>ProcessUnfetchedConceptDataBI</code> and can be "run" using the
 * terminology store method iterateConceptDataInParallel.
 *
 * @see
 * TerminologyStoreDI#iterateConceptDataInParallel(org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI)
 * @see <a href="http://www.snomed.org/tig?t=tig_release_files">IHTSDO Technical
 * Implementation Guide - Release File Specifications</a>
 */
public class Rf2Export implements ProcessUnfetchedConceptDataBI {

    /**
     * The set of nids representing the concepts to include in this release.
     */
    public NidBitSetBI conceptsToProcess;
    private Writer conceptsWriter;
    private COUNTRY_CODE country;
    private Writer descriptionsWriter;
    private Date effectiveDate;
    private String effectiveDateString;
    private Writer identifiersWriter;
    private Writer publicIdentifiersWriter;
    private LANG_CODE language;
    private String namespace;
    private String module;
    private Writer relationshipsWriter;
    private Writer relationshipsStatedWriter;
    private ReleaseType releaseType;
    private Set<Integer> stampNids;
    private TerminologyStoreDI store;
    private ViewCoordinate viewCoordinate;
    private Writer langRefsetsWriter;
    private Writer otherLangRefsetsWriter;
    private Writer modDependWriter;
    private Writer descTypeWriter;
    private Writer refsetDescWriter;
    private Set<Integer> excludedRefsetIds;
    private ConceptSpec uuidIdScheme = new ConceptSpec("SNOMED CT universally unique identifier (core metadata concept)",
            UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));
    private Set<Integer> langRefexNids = new HashSet<Integer>();
    private Set<Integer> possibleLangRefexNids = new HashSet<Integer>();
    private Set<ConceptSpec> descTypes = new HashSet<ConceptSpec>();
    private ViewCoordinate newVc;
    private ConceptVersionBI parentCv;
    private static UUID REFSET_DESC_NAMESPACE = UUID.fromString("d1871eb0-8a47-11e1-b0c4-0800200c9a66");
    private static UUID MODULE_DEPEND_NAMESPACE = UUID.fromString("d1871eb2-8a47-11e1-b0c4-0800200c9a66");
    private static UUID DESC_TYPE_NAMESPACE = UUID.fromString("d1871eb3-8a47-11e1-b0c4-0800200c9a66");
    private boolean makePrivateIdFile;
    private ConceptVersionBI refsetParentConcept;
    private File directory;

    //~--- constructors --------------------------------------------------------
    /**
     * Instantiates a new rf2 export.
     *
     * @param directory specifying where to write the files
     * @param releaseType the <code>ReleaseType</code> representing the type of
     * release
     * @param language the <code>LANG_CODE</code> representing the language of
     * the release
     * @param country the <code>COUNTRY_CODE</code> representing the country
     * associated with the release
     * @param namespace the String representing the namespace associated with
     * the released content
     * @param module the String representing the module associated with the
     * released content
     * @param effectiveDate specifying the official release date
     * @param stampNids the valid stamp nids associated with content for this
     * release
     * @param viewCoordinate specifying which versions of the concepts to
     * include in this release
     * @param excludedRefsetIds the set of nids associated with refexes which
     * should not be included in the release (for example, workflow refsets)
     * @param conceptsToProcess the set of nids representing the concepts to be
     * included in this release
     * @param makePrivateIdFile set to <code>true</code> to make an auxiliary
     * identifier file
     * @throws IOException signals that an I/O exception has occurred
     */
    public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
            String namespace, String module, Date effectiveDate, Set<Integer> stampNids, ViewCoordinate viewCoordinate,
            Set<Integer> excludedRefsetIds, NidBitSetBI conceptsToProcess, boolean makePrivateIdFile, int refsetParentConceptNid)
            throws IOException, ContradictionException {
        this.directory = directory;
        directory.mkdirs();
        this.releaseType = releaseType;
        this.effectiveDate = effectiveDate;
        this.language = language;
        this.country = country;
        this.namespace = namespace;
        this.module = module;
        this.stampNids = stampNids;
        this.store = Ts.get();
        this.viewCoordinate = viewCoordinate;
        this.conceptsToProcess = conceptsToProcess;
        this.effectiveDateString = TimeHelper.getShortFileDateFormat().format(effectiveDate);
        this.excludedRefsetIds = excludedRefsetIds;
        this.makePrivateIdFile = makePrivateIdFile;
        if(refsetParentConceptNid != 0){
            this.refsetParentConcept = Ts.get().getConceptVersion(viewCoordinate, refsetParentConceptNid);
        }
        
        File conceptsFile = new File(directory,
                "sct2_Concept_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descriptionsFile = new File(directory,
                "sct2_Description_UUID_" + releaseType.suffix + "_"
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
        if (makePrivateIdFile) {
            privateIdentifiersFile = new File(directory,
                    "sct2_Identifier_Auxiliary_UUID_" + releaseType.suffix + "_"
                    + country.getFormatedCountryCode() + namespace + "_"
                    + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
            identifiersFile = new File(directory,
                    "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                    + country.getFormatedCountryCode() + namespace + "_"
                    + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        } else {
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
        if (makePrivateIdFile) {
            FileOutputStream idOs = new FileOutputStream(identifiersFile);
            publicIdentifiersWriter = new BufferedWriter(new OutputStreamWriter(idOs, "UTF8"));
            FileOutputStream privIdOs = new FileOutputStream(privateIdentifiersFile);
            identifiersWriter = new BufferedWriter(new OutputStreamWriter(privIdOs, "UTF8"));

            for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
                publicIdentifiersWriter.write(field.headerText + field.seperator);
            }
        } else {
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
        newVc = Ts.get().getMetadataViewCoordinate();
        newVc.setPositionSet(viewCoordinate.getPositionSet());
        NidSetBI isaNids = newVc.getIsaTypeNids();
        isaNids.add(Snomed.IS_A.getLenient().getNid());

        ConceptSpec langRefexParent = new ConceptSpec("Language type reference set",
                UUID.fromString("84a0b03b-220c-3d69-8487-2e019c933687"));
        parentCv = Ts.get().getConceptVersion(newVc, langRefexParent.getLenient().getNid());
        for(ConceptVersionBI child : parentCv.getRelationshipsTargetSourceConceptsActiveIsa()){
            possibleLangRefexNids.add(child.getConceptNid());
            for (ConceptVersionBI childOfChild : child.getRelationshipsTargetSourceConceptsActiveIsa()) {
                possibleLangRefexNids.add(childOfChild.getConceptNid());
            }
        }
    }

    //~--- methods -------------------------------------------------------------
    /**
     * Closes the release file writers.
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

    /**
     *
     * @return <code>true</code>
     */
    @Override
    public boolean continueWork() {
        return true;
    }

    /**
     * Processes the components, annotations, and language refset members of the
     * specified
     * <code>concept</code>. Any components that has a stamp nid in the set of
     * specified stamp nids will be written to the release file.
     *
     * @param concept the concept to process
     * @throws Exception indicates an exception has occurred
     */
    private void process(ConceptChronicleBI concept) throws Exception {
        ConceptAttributeChronicleBI ca = concept.getConceptAttributes();
        processConceptAttribute(ca);
        processIdentifiers(ca.getPrimUuid(), ca.getPrimordialVersion().getStampNid());

        if (concept.getDescriptions() != null) {
            for (DescriptionChronicleBI d : concept.getDescriptions()) {
                processDescription(d);
                processIdentifiers(d.getPrimUuid(), d.getPrimordialVersion().getStampNid());
                if (d.getAnnotations() != null) {
                    for (RefexChronicleBI annot : d.getAnnotations()) {
                        int refexNid = annot.getRefexNid();
                        if(possibleLangRefexNids.contains(annot.getRefexNid())){
                            langRefexNids.add(annot.getRefexNid());
                            processLangRefsets(annot);
                        }
                    }
                }
            }
        }

        if (concept.getRelationshipsSource() != null) {
            for (RelationshipChronicleBI r : concept.getRelationshipsSource()) {
                processRelationship(r);
                processIdentifiers(r.getPrimUuid(), r.getPrimordialVersion().getStampNid());
            }
        }
    }

    /**
     * Writes the modular dependency, description types, and refset descriptions
     * files.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     */
    public void writeOneTimeFiles() throws IOException, NoSuchAlgorithmException, ContradictionException {

        processModularDependency();

        for (ConceptSpec descType : descTypes) {
            processDescType(descType);
        }
        
        for (Integer refexNid : langRefexNids) {
            processLanguageRefsetDesc(refexNid);
            processRefsetDescAttribute(refexNid);
        }
        if(refsetParentConcept != null){
            writeSimpleRefsetFiles();
        }
    }
    /**
     * Writes the simple refset files for all of the refsets which are a child
     * of the
     * <code>refsetParentConcept</code> and that were written in the module
     * being released.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for the
     * specified view coordinate
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     */
    private void writeSimpleRefsetFiles() throws IOException, ContradictionException, NoSuchAlgorithmException {
        Collection<? extends ConceptVersionBI> childRefsetConcepts = refsetParentConcept.getRelationshipsTargetSourceConceptsActiveIsa();
        for (ConceptVersionBI childRefset : childRefsetConcepts) {
            if (childRefset.getModuleNid() == Ts.get().getNidForUuids(UUID.fromString(module))) {
                String refsetName = childRefset.getDescriptionPreferred().getText();
                refsetName = refsetName.replace(" ", "-");
                if(!refsetName.toLowerCase().contains("simple-refset")){
                    refsetName = refsetName.concat("-simple-refset");
                }
                File simpleRefsetFile = new File(directory,
                        "sct2_" + refsetName + "_UUID_" + releaseType.suffix + "_"
                        + language.getFormatedLanguageCode() + namespace + "_"
                        + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
                FileOutputStream simpleRefsetOs = new FileOutputStream(simpleRefsetFile);
                Writer simpleRefsetWriter = new BufferedWriter(new OutputStreamWriter(simpleRefsetOs, "UTF8"));

                for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                    simpleRefsetWriter.write(field.headerText + field.seperator);
                }

                for (RefexVersionBI refexVersion : childRefset.getRefsetMembersActive()) {
                    for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                        switch (field) {
                            case ID:
                                simpleRefsetWriter.write(refexVersion.getPrimUuid() + field.seperator);

                                break;

                            case EFFECTIVE_TIME:
                                simpleRefsetWriter.write(effectiveDateString + field.seperator);

                                break;

                            case ACTIVE:
                                simpleRefsetWriter.write(store.getComponent(refexVersion.getStatusNid()).getPrimUuid() + field.seperator);

                                break;

                            case MODULE_ID:
                                simpleRefsetWriter.write(module + field.seperator);

                                break;

                            case REFSET_ID:
                                simpleRefsetWriter.write(store.getComponent(refexVersion.getRefexNid()).getPrimUuid() + field.seperator);

                                break;

                            case REFERENCED_COMPONENT_ID:
                                simpleRefsetWriter.write(store.getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                                break;
                        }
                    }

                }
                processSimpleRefsetDesc(childRefset.getNid());
                simpleRefsetWriter.close();
            }
        }
    }

    /**
     * Writes the concepts file according to the fields specified in
     * <code>
     * Rf2File.ConceptsFileFields</code>. Only the versions which have a stamp
     * nid in the specified collection of stamp nids will be written.
     *
     * @param conceptAttributeChronicle the concept attribute to process
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.ConceptsFileFields
     */
    private void processConceptAttribute(ConceptAttributeChronicleBI conceptAttributeChronicle) throws IOException {
        if (conceptAttributeChronicle != null) {
            for (ConceptAttributeVersionBI car : conceptAttributeChronicle.getVersions(viewCoordinate)) {
                if (stampNids.contains(car.getStampNid())) {
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

    /**
     * Writes the descriptions file according to the fields specified in
     * <code>
     * Rf2File.DescriptionsFileFields</code>. Only the versions which have a
     * stamp nid in the specified collection of stamp nids will be written.
     *
     *
     * @param descriptionChronicle the description to process
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.DescriptionsFileFields
     */
    private void processDescription(DescriptionChronicleBI descriptionChronicle) throws IOException {
        if (descriptionChronicle != null) {
            for (DescriptionVersionBI descr : descriptionChronicle.getVersions(viewCoordinate)) {
                if (stampNids.contains(descr.getStampNid())) {
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
                                descriptionsWriter.write(store.getUuidPrimordialForNid(descriptionChronicle.getNid()) + field.seperator);

                                break;

                            case MODULE_ID:
                                descriptionsWriter.write(module + field.seperator);

                                break;

                            case CONCEPT_ID:
                                descriptionsWriter.write(store.getUuidPrimordialForNid(descriptionChronicle.getConceptNid())
                                        + field.seperator);

                                break;

                            case LANGUAGE_CODE:
                                String lang = descr.getLang();
                                if(lang.length() > 2){
                                    lang = lang.substring(0, 2);
                                }
                                descriptionsWriter.write(lang + field.seperator);

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

    /**
     * Determines if a relationship is stated or inferred and processes the
     * relationship accordingly. Only the versions which have a stamp nid in the
     * specified collection of stamp nids will be written.
     *
     * @param relationshipChronicle the relationship to process
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processRelationship(RelationshipChronicleBI relationshipChronicle) throws IOException {
        if (relationshipChronicle != null) {
            for (RelationshipVersionBI rv : relationshipChronicle.getVersions(viewCoordinate)) {
                if (stampNids.contains(rv.getStampNid())) {
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

    /**
     * Writes the inferred relationships file according to the fields specified
     * in
     * <code>
     * Rf2File.RelationshipsFileFields</code>.
     *
     * @param relationshipVersion the relationship version to write
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.RelationshipsFileFields
     */
    private void processInferredRelationship(RelationshipVersionBI relationshipVersion) throws IOException {
        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            switch (field) {
                case ACTIVE:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getStatusNid())
                            + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    relationshipsWriter.write(effectiveDateString + field.seperator);

                    break;

                case ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getNid()) + field.seperator);

                    break;

                case MODULE_ID:
                    relationshipsWriter.write(module + field.seperator);

                    break;

                case SOURCE_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getSourceNid())
                            + field.seperator);

                    break;

                case DESTINATION_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getTargetNid())
                            + field.seperator);

                    break;

                case RELATIONSHIP_GROUP:
                    relationshipsWriter.write(relationshipVersion.getGroup() + field.seperator);

                    break;

                case TYPE_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getTypeNid())
                            + field.seperator);

                    break;

                case CHARCTERISTIC_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getCharacteristicNid())
                            + field.seperator);

                    break;

                case MODIFIER_ID:
                    relationshipsWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getRefinabilityNid())
                            + field.seperator);

                    break;
            }
        }
    }

    /**
     * Writes the stated relationships file according to the fields specified in
     * <code>
     * Rf2File.RelationshipsFileFields</code>.
     *
     * @param relationshipVersion the relationship version to write
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.RelationshipsFileFields
     */
    private void processStatedRelationship(RelationshipVersionBI relationshipVersion) throws IOException {
        for (Rf2File.RelationshipsFileFields field : Rf2File.RelationshipsFileFields.values()) {
            switch (field) {
                case ACTIVE:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getStatusNid())
                            + field.seperator);

                    break;

                case EFFECTIVE_TIME:
                    relationshipsStatedWriter.write(effectiveDateString + field.seperator);

                    break;

                case ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getNid()) + field.seperator);

                    break;

                case MODULE_ID:
                    relationshipsStatedWriter.write(module + field.seperator);

                    break;

                case SOURCE_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getSourceNid())
                            + field.seperator);

                    break;

                case DESTINATION_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getTargetNid())
                            + field.seperator);

                    break;

                case RELATIONSHIP_GROUP:
                    relationshipsStatedWriter.write(relationshipVersion.getGroup() + field.seperator);

                    break;

                case TYPE_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getTypeNid())
                            + field.seperator);

                    break;

                case CHARCTERISTIC_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getCharacteristicNid())
                            + field.seperator);

                    break;

                case MODIFIER_ID:
                    relationshipsStatedWriter.write(store.getUuidPrimordialForNid(relationshipVersion.getRefinabilityNid())
                            + field.seperator);

                    break;
            }
        }
    }

    /**
     * Writes the alternate identifiers file according to the fields specified
     * in
     * <code>
     * Rf2File.IdentifiersFileFields</code>. Only the versions which have a
     * stamp nid in the specified collection of stamp nids will be written.
     *
     * @param primordialUuid the alternate identifier
     * @param primordialStampNid the primordial stamp nid associated with the
     * primordial uuid
     * @throws IOException signals that an I/O exception has occurred
     * @see Rf2File.IdentifiersFileFields
     */
    private void processIdentifiers(UUID primordialUuid, int primordialStampNid) throws IOException {
        if (primordialUuid != null) {
            if (stampNids.contains(primordialStampNid)) {
                for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {

                    switch (field) {
                        case IDENTIFIER_SCHEME_ID:
                            identifiersWriter.write(uuidIdScheme.getLenient().getPrimUuid() + field.seperator);

                            break;

                        case ALTERNATE_IDENTIFIER:
                            identifiersWriter.write(primordialUuid + field.seperator);

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
                            identifiersWriter.write(primordialUuid + field.seperator);

                            break;
                    }
                }
            }
        }
    }

    /**
     * Processes a language refex member to determine if the language is English
     * or another language. Only the versions which have a stamp nid in the
     * specified collection of stamp nids will be written.
     *
     * @param refexChronicle refex member to process
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processLangRefsets(RefexChronicleBI refexChronicle) throws IOException {
        if (refexChronicle != null) {
            if (!excludedRefsetIds.contains(refexChronicle.getRefexNid())) {
                Collection<RefexVersionBI<?>> versions = refexChronicle.getVersions(viewCoordinate);
                for (RefexVersionBI rv : versions) {
                    if (!stampNids.contains(rv.getStampNid())) {
                        break;
                    } else {
                        if (refexChronicle.getRefexNid() == RefsetAux.EN_GB_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else if (refexChronicle.getRefexNid() == RefsetAux.EN_US_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else {
                            processOtherLang(rv);
                        }
                    }

                }
            }
        }
    }

    /**
     * Writes the English language refset file according to the fields specified
     * in
     * <code>
     * Rf2File.LanguageRefsetFileFields</code>.
     *
     * @param refexVersion the refex member to write
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processLang(RefexVersionBI refexVersion) throws IOException {
        if (refexVersion != null) {
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        langRefsetsWriter.write(refexVersion.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        langRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        langRefsetsWriter.write(store.getComponent(refexVersion.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        langRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        langRefexNids.add(refexVersion.getRefexNid());
                        langRefsetsWriter.write(store.getComponent(refexVersion.getRefexNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        langRefsetsWriter.write(store.getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case ACCEPTABILITY:
                        RefexNidVersionBI refexNidVersion = (RefexNidVersionBI) refexVersion;
                        langRefsetsWriter.write(store.getComponent(refexNidVersion.getNid1()).getPrimUuid() + field.seperator);

                        break;
                }
            }
        }
    }

    /**
     * Writes the language refset file according to the fields specified in
     * <code>
     * Rf2File.LanguageRefsetFileFields</code>. Uses the language specified in
     * the constructor.
     *
     * @param refexVersion the refex member to write
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processOtherLang(RefexVersionBI refexVersion) throws IOException {
        if (refexVersion != null) {
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        otherLangRefsetsWriter.write(refexVersion.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        otherLangRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        otherLangRefsetsWriter.write(store.getComponent(refexVersion.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        otherLangRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        langRefexNids.add(refexVersion.getRefexNid());
                        otherLangRefsetsWriter.write(store.getComponent(refexVersion.getRefexNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        otherLangRefsetsWriter.write(store.getComponent(refexVersion.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case ACCEPTABILITY:
                        RefexNidVersionBI refexNidVersion = (RefexNidVersionBI) refexVersion;
                        otherLangRefsetsWriter.write(store.getComponent(refexNidVersion.getNid1()).getPrimUuid() + field.seperator);

                        break;
                }
            }
        }
    }

    /**
     * Writes the refset description file for the language refexes according to
     * the
     * <code>Rf2File.RefsetDescriptorFileFields</code>.
     *
     * @param refexNid the nid associated with a language refex
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates that a no such algorithm
     * exception has occurred
     * @see Rf2File.RefsetDescriptorFileFields
     */
    private void processLanguageRefsetDesc(int refexNid) throws IOException, NoSuchAlgorithmException {
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(0 + field.seperator);

                    break;
            }
        }
    }
    
    /**
     * Writes the refset description file for simple refexes according to
     * the
     * <code>Rf2File.RefsetDescriptorFileFields</code>.
     *
     * @param refexNid the nid associated with a language refex
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates that a no such algorithm
     * exception has occurred
     * @see Rf2File.RefsetDescriptorFileFields
     */
    private void processSimpleRefsetDesc(int refexNid) throws IOException, NoSuchAlgorithmException {
        ConceptSpec refsetDescriptor = new ConceptSpec("Reference set descriptor reference set (foundation metadata concept)",
                UUID.fromString("5ddff82f-5aee-3b16-893f-6b7aa726cc4b"));
        ConceptSpec attributeDescriptor = new ConceptSpec("Concept type component (foundation metadata concept)",
                UUID.fromString("78f69fb6-410c-3b5a-9120-53954592a80d"));
        ConceptSpec attributeType = new ConceptSpec("Concept type component (foundation metadata concept)",
                UUID.fromString("78f69fb6-410c-3b5a-9120-53954592a80d"));
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attributeDescriptor.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attributeType.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(0 + field.seperator);

                    break;
            }
        }
    }

    /**
     * Writes the refset attributes for the refset description file for the
     * language refexes according to the
     * <code>Rf2File.RefsetDescriptorFileFields</code>.
     *
     * @param refexNid the nid associated with a language refex
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates that a no such algorithm
     * exception has occurred
     * @see Rf2File.RefsetDescriptorFileFields
     */
    private void processRefsetDescAttribute(int refexNid) throws IOException, NoSuchAlgorithmException {
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(1 + field.seperator);

                    break;
            }
        }
    }

    /**
     * Writes the modular dependency file according to the
     * <code>Rf2File.ModuleDependencyFileFields</code>.
     *
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception
     * has occurred
     * @see Rf2File.ModuleDependencyFileFields
     */
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
                            + coreModule.getStrict(viewCoordinate).getPrimUuid()
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
                    modDependWriter.write(modDepenRefex.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    modDependWriter.write(coreModule.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

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

    /**
     * Writes the description type file according to the
     * <code>Rf2File.DescTypeFileFields</code>.
     *
     * @param descType a <code>ConceptSpec</code> representing the description
     * type to write
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates a no such algorithm exception has occurred
     * @see Rf2File.DescTypeFileFields
     */
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
                            descType.getStrict(viewCoordinate).getPrimUuid().toString());
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
                    descTypeWriter.write(descFormatRefex.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    descTypeWriter.write(descType.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

                    break;

                case DESC_FORMAT:
                    descTypeWriter.write(descFormat.getStrict(viewCoordinate).getPrimUuid() + field.seperator);

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

    /**
     * Processes each concept to determine if the concept or any of its
     * components should be written to the release files.
     *
     * @param cNid the nid of the concept to process
     * @param fetcher the fetcher for getting the concept associated with * *
     * the <code>cNid</code> from the database
     * @throws Exception indicates an exception has occurred
     */
    @Override
    public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        process(fetcher.fetch());
    }

    //~--- get methods ---------------------------------------------------------
    /**
     *
     * @return the set of nids associated with concept to be included in this
     * release
     * @throws IOException signals that an I/O exception has occurred
     */
    @Override
    public NidBitSetBI getNidSet() throws IOException {
        return conceptsToProcess;
    }
}
