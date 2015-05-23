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
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import org.ihtsdo.country.COUNTRY_CODE;
import org.ihtsdo.helper.rf2.Rf2File;
import org.ihtsdo.helper.rf2.Rf2File.ReleaseType;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_member.RefexMemberVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_float.RefexNidFloatVersionBI;
import org.ihtsdo.tk.api.refex.type_string_string.RefexStringStringVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.binding.snomed.ConceptInactivationType;
import org.ihtsdo.tk.binding.snomed.HistoricalRelType;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.Snomed;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.ValidationException;
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
    private Date snomedCoreReleaseDate;
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
    private ViewCoordinate viewCoordinateAllStatus;
    private ViewCoordinate viewCoordinateAllStatusTime;
    private ViewCoordinate viewCoordinate;
    private Writer langRefsetsWriter;
    private Writer otherLangRefsetsWriter;
    private Writer modDependWriter;
    private Writer descTypeWriter;
    private Writer refsetDescWriter;
    private Writer associationWriter;
    private Writer attributeValueWriter;
    private Set<Integer> excludedRefsetIds;
    private ConceptSpec uuidIdScheme = new ConceptSpec("SNOMED CT universally unique identifier (core metadata concept)",
            UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));
    private Set<Integer> langRefexNids = new HashSet<Integer>();
    private Set<Integer> possibleLangRefexNids = new HashSet<Integer>();
    private Set<Integer> associationRefexNids = new HashSet<Integer>();
    private Set<Integer> attribValueRefexNids = new HashSet<Integer>();
    private Set<ConceptSpec> descTypes = new HashSet<ConceptSpec>();
    private ViewCoordinate newVc;
    private ConceptVersionBI parentCv;
    private static UUID REFSET_DESC_NAMESPACE = UUID.fromString("d1871eb0-8a47-11e1-b0c4-0800200c9a66");
    private static UUID MODULE_DEPEND_NAMESPACE = UUID.fromString("d1871eb2-8a47-11e1-b0c4-0800200c9a66");
    private static UUID DESC_TYPE_NAMESPACE = UUID.fromString("d1871eb3-8a47-11e1-b0c4-0800200c9a66");
    private boolean makePrivateIdFile;
    private ConceptVersionBI refsetParentConcept;
    private File directory;
    private Set<Integer> sameCycleStampNids;
    private Collection<Integer> taxonomyParentNids;
    private ConceptVersionBI conNumRefsetParentConcept;
    private Date previousReleaseDate;
    private ArrayList<UUID> uuidsToSkip = new ArrayList<>();

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
     * @param snomedCoreReleaseDate specifying the release date of SNOMED core content used by this release
     * @param stampNids the valid stamp nids associated with content for this
     * release
     * @param viewCoordinate specifying which versions of the concepts to
     * include in this release. Should not include all status values.
     * @param excludedRefsetIds the set of nids associated with refexes which
     * should not be included in the release (for example, workflow refsets)
     * @param conceptsToProcess the set of nids representing the concepts to be
     * included in this release
     * @param makePrivateIdFile set to <code>true</code> to make an auxiliary
     * identifier file
     * @param refsetParentConceptNid an integer representing the parent concept of simple refsets
     * @param previousReleaseDate the date of the previous release
     * @param stampsToRemove stamps to not include in the release
     * @param taxonomyParentNids an integer representing the parent of taxonomy to release
     * @throws IOException signals that an I/O exception has occurred
     */
    public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
            String namespace, String module, Date effectiveDate, Date snomedCoreReleaseDate, Set<Integer> stampNids, ViewCoordinate viewCoordinate,
            Set<Integer> excludedRefsetIds, NidBitSetBI conceptsToProcess, boolean makePrivateIdFile, int refsetParentConceptNid,
            Date previousReleaseDate, Set<Integer> stampsToRemove, Collection taxonomyParentNids)
            throws IOException, ContradictionException {
        this.directory = directory;
        directory.mkdirs();
        this.releaseType = releaseType;
        this.effectiveDate = effectiveDate;
        this.snomedCoreReleaseDate = snomedCoreReleaseDate;
        this.language = language;
        this.country = country;
        this.namespace = namespace;
        this.module = module;
        this.stampNids = stampNids;
        this.store = Ts.get();
        this.viewCoordinate = viewCoordinate;
        this.viewCoordinateAllStatus = viewCoordinate.getViewCoordinateWithAllStatusValues();
        this.viewCoordinateAllStatusTime = viewCoordinate.getViewCoordinateWithAllStatusValues();
        this.conceptsToProcess = conceptsToProcess;
        this.effectiveDateString = TimeHelper.getShortFileDateFormat().format(effectiveDate);
        this.excludedRefsetIds = excludedRefsetIds;
        this.makePrivateIdFile = makePrivateIdFile;
        this.sameCycleStampNids = stampsToRemove;
        if(refsetParentConceptNid != 0){
            this.refsetParentConcept = Ts.get().getConceptVersion(viewCoordinate, refsetParentConceptNid);
        }
        this.taxonomyParentNids = taxonomyParentNids;
        if(releaseType.equals(ReleaseType.DELTA) && previousReleaseDate != null){
            for(int stamp : stampNids){
                long time = Ts.get().getTimeForStampNid(stamp);
                if(time < previousReleaseDate.getTime()){
                    stampNids.remove(stamp);
                }
            }
        }
        viewCoordinateAllStatusTime.setPrecedence(Precedence.TIME);
        setup();
    }
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
     * @param snomedCoreReleaseDate specifying the release date of SNOMED core content used by this release
     * @param stampNids the valid stamp nids associated with content for this
     * release
     * @param viewCoordinate specifying which versions of the concepts to
     * include in this release. Should not include all status values.
     * @param excludedRefsetIds the set of nids associated with refexes which
     * should not be included in the release (for example, workflow refsets)
     * @param conceptsToProcess the set of nids representing the concepts to be
     * included in this release
     * @param makePrivateIdFile set to <code>true</code> to make an auxiliary
     * identifier file
     * @param refsetParentConceptNid an integer representing the parent concept of simple refsets
     * @param previousReleaseDate the date of the previous release
     * @param stampsToRemove stamps to not include in the release
     * @param taxonomyParentNids an integer representing the parent of taxonomy to release
     * @param conNumRefesetParentConceptNid an integer representing the parent concept of number float refsets
     * @throws IOException signals that an I/O exception has occurred
     */
    public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
            String namespace, String module, Date effectiveDate, Date snomedCoreReleaseDate,Set<Integer> stampNids, ViewCoordinate viewCoordinate,
            Set<Integer> excludedRefsetIds, NidBitSetBI conceptsToProcess, boolean makePrivateIdFile, int refsetParentConceptNid,
            Date previousReleaseDate, Set<Integer> stampsToRemove, Collection taxonomyParentNids, Integer conNumRefesetParentConceptNid)
            throws IOException, ContradictionException {
        this.directory = directory;
        directory.mkdirs();
        this.releaseType = releaseType;
        this.effectiveDate = effectiveDate;
        this.snomedCoreReleaseDate = snomedCoreReleaseDate;
        this.language = language;
        this.country = country;
        this.namespace = namespace;
        this.module = module;
        this.stampNids = stampNids;
        this.store = Ts.get();
        this.viewCoordinate = viewCoordinate;
        this.viewCoordinateAllStatus = viewCoordinate.getViewCoordinateWithAllStatusValues();
        this.viewCoordinateAllStatusTime = viewCoordinate.getViewCoordinateWithAllStatusValues();
        this.conceptsToProcess = conceptsToProcess;
        this.effectiveDateString = TimeHelper.getShortFileDateFormat().format(effectiveDate);
        this.excludedRefsetIds = excludedRefsetIds;
        this.makePrivateIdFile = makePrivateIdFile;
        this.sameCycleStampNids = stampsToRemove;
        if(refsetParentConceptNid != 0){
            this.refsetParentConcept = Ts.get().getConceptVersion(viewCoordinate, refsetParentConceptNid);
        }
        this.taxonomyParentNids = taxonomyParentNids;
        if(conNumRefesetParentConceptNid != null){
            this.conNumRefsetParentConcept = Ts.get().getConceptVersion(viewCoordinate, conNumRefesetParentConceptNid);
        }
        this.previousReleaseDate = previousReleaseDate;
        if(releaseType.equals(ReleaseType.DELTA) && previousReleaseDate != null){
            for(int stamp : stampNids){
                long time = Ts.get().getTimeForStampNid(stamp);
                if(time < previousReleaseDate.getTime()){
                    stampNids.remove(stamp);
                }
            }
        }
        viewCoordinateAllStatusTime.setPrecedence(Precedence.TIME);
        setup();
    }
    
    private void setup() throws FileNotFoundException, UnsupportedEncodingException, IOException, ContradictionException{
                File conceptsFile = new File(directory,
                "sct2_Concept_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descriptionsFile = new File(directory,
                "sct2_Description_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File relationshipsFile = new File(directory,
                "sct2_Relationship_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File relationshipsStatedFile = new File(directory,
                "sct2_StatedRelationship_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File privateIdentifiersFile = null;
        File identifiersFile = null;
        if (makePrivateIdFile) {
            privateIdentifiersFile = new File(directory,
                    "sct2_IdentifierAuxiliary_UUID_" + releaseType.suffix + "_"
                    + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                    + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
            identifiersFile = new File(directory,
                    "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                    + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                    + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        } else {
            identifiersFile = new File(directory,
                    "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                    + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                    + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        }
        File associationFile = new File(directory,
                "der2_cRefset_AssociationReference_UUID" + releaseType.suffix + "_"
                +  country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File attributeValueFile = new File(directory,
                "der2_cRefset_AttributeValue_UUID" + releaseType.suffix + "_"
                +  country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File langRefsetsFile = new File(directory,
                "der2_cRefset_Language_UUID" + releaseType.suffix + "-"
                + LANG_CODE.EN.getFormatedLanguageCode() + "_" 
                + country.getFormatedCountryCode().toUpperCase() 
                + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File otherLangRefsetsFile = null;
        if(!language.getFormatedLanguageNoDialectCode().equals(LANG_CODE.EN.getFormatedLanguageCode())){
             otherLangRefsetsFile = new File(directory,
                "der2_cRefset_Language_UUID" + releaseType.suffix + "-"
                + language.getFormatedLanguageNoDialectCode() + "_" 
                + country.getFormatedCountryCode().toUpperCase()
                + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        }
        File modDependFile = new File(directory,
                "der2_ssRefset_ModuleDependency_UUID" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File descTypeFile = new File(directory,
                "der2_ciRefset_DescriptionType_UUID" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File refsetDescFile = new File(directory,
                "der2_cciRefset_RefsetDescriptor_UUID" + releaseType.suffix + "_"
                + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
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
        FileOutputStream associationOs = new FileOutputStream(associationFile);
        associationWriter = new BufferedWriter(new OutputStreamWriter(associationOs, "UTF8"));
        FileOutputStream attributeValueOs = new FileOutputStream(attributeValueFile);
        attributeValueWriter = new BufferedWriter(new OutputStreamWriter(attributeValueOs, "UTF8"));
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
        
        for (Rf2File.AssociationRefsetFileFields field : Rf2File.AssociationRefsetFileFields.values()) {
            associationWriter.write(field.headerText + field.seperator);
        }
        
        for (Rf2File.AttribValueRefsetFileFields field : Rf2File.AttribValueRefsetFileFields.values()) {
            attributeValueWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.IdentifiersFileFields field : Rf2File.IdentifiersFileFields.values()) {
            identifiersWriter.write(field.headerText + field.seperator);
        }

        for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
            langRefsetsWriter.write(field.headerText + field.seperator);
        }
        if(otherLangRefsetsWriter != null){
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
        for(ConceptVersionBI child : parentCv.getRelationshipsIncomingSourceConceptsActiveIsa()){
            possibleLangRefexNids.add(child.getConceptNid());
            for (ConceptVersionBI childOfChild : child.getRelationshipsIncomingSourceConceptsActiveIsa()) {
                possibleLangRefexNids.add(childOfChild.getConceptNid());
            }
        }
        //for association refset file
        associationRefexNids.add(SnomedMetadataRf2.POSSIBLY_EQUIVALENT_TO_REFSET_RF2.getLenient().getConceptNid());
        associationRefexNids.add(SnomedMetadataRf2.SAME_AS_REFSET_RF2.getLenient().getConceptNid());
        associationRefexNids.add(SnomedMetadataRf2.WAS_A_REFSET_RF2.getLenient().getConceptNid());
        associationRefexNids.add(SnomedMetadataRf2.REPLACED_BY_REFSET_RF2.getLenient().getConceptNid());
        associationRefexNids.add(SnomedMetadataRf2.REFERS_TO_REFSET_RF2.getLenient().getConceptNid());
        //for attribute value refset file
        attribValueRefexNids.add(SnomedMetadataRf2.DESC_INACTIVE_REFSET.getLenient().getConceptNid());
        attribValueRefexNids.add(SnomedMetadataRf2.CONCEPT_INACTIVE_REFSET.getLenient().getConceptNid());
        
        uuidsToSkip.add(UUID.fromString("6e23b5e8-5d71-326c-b16d-30a9f92baefe"));
        uuidsToSkip.add(UUID.fromString("d403e857-eb52-4c9c-883f-19ae04dfc039"));
        uuidsToSkip.add(UUID.fromString("5eb17274-2ecb-4169-bf64-09e710536b3f"));
        uuidsToSkip.add(UUID.fromString("20719df2-3625-3135-a61a-f9ff6ecee307"));
        uuidsToSkip.add(UUID.fromString("a54dbce6-98d8-11e3-a5e2-0800200c9a66"));
        uuidsToSkip.add(UUID.fromString("decec67f-1d4f-3fe1-97fe-99e3b83e6296"));
        uuidsToSkip.add(UUID.fromString("47f754d2-2fb7-45c4-997d-18315fea2334"));
        uuidsToSkip.add(UUID.fromString("52f78895-d74a-3dcf-b3b5-a9a14019fbd7"));
        uuidsToSkip.add(UUID.fromString("2603c150-f846-30cd-9a41-e71c6afc2c72"));
        uuidsToSkip.add(UUID.fromString("b4fa7fd9-4e4b-389d-b35a-2b082daec319"));
        uuidsToSkip.add(UUID.fromString("bdbdcebc-b82c-4924-97e2-8e2ae32a176e"));
        uuidsToSkip.add(UUID.fromString("d9a07cce-ad66-36af-9396-06313c490497"));
        uuidsToSkip.add(UUID.fromString("3194910f-aa5c-35a7-8cc6-57aab307d975"));
        uuidsToSkip.add(UUID.fromString("36ebb8dd-406b-3cf6-8752-fca4fd3e5fc6"));
        uuidsToSkip.add(UUID.fromString("9ef7fe8c-6b00-360f-be75-576f03c57a01"));
        uuidsToSkip.add(UUID.fromString("3d69e72b-3703-33c8-a4b0-46cfc0af265a"));
        uuidsToSkip.add(UUID.fromString("0dbb2ca9-e399-31fe-81bd-6c0d04186841"));
        uuidsToSkip.add(UUID.fromString("17db3434-db4a-3ba1-b2f8-a2169c9defd9"));
        uuidsToSkip.add(UUID.fromString("50f92e0e-128a-4383-8199-47cb5a778762"));
        uuidsToSkip.add(UUID.fromString("6936a025-65fa-3581-abec-ff43ffd7e7fb"));
        uuidsToSkip.add(UUID.fromString("5f5a8494-24f6-11e3-b2c7-10bb7e44da78"));
        uuidsToSkip.add(UUID.fromString("d45ed8db-9457-3e54-963e-4bf44920878d"));
        uuidsToSkip.add(UUID.fromString("f13fc93c-fca3-3aa4-9206-2eff33e9cc53"));
        uuidsToSkip.add(UUID.fromString("d147b304-5d71-47f7-bc5a-dfce97ca8973"));
        uuidsToSkip.add(UUID.fromString("2ea80d81-36d0-3c30-9f7f-3f4d2afe272c"));
        uuidsToSkip.add(UUID.fromString("2d910fc2-b4e9-3863-a9ad-2b3f2bfeaafe"));
        uuidsToSkip.add(UUID.fromString("0c9d65cf-0289-3eef-862d-052a338ccb91"));
        uuidsToSkip.add(UUID.fromString("f7d2d450-00e0-3264-bc8e-da6f04924b70"));
        uuidsToSkip.add(UUID.fromString("419ace76-8bf3-395b-b17c-94fa28f3134f"));
        uuidsToSkip.add(UUID.fromString("5e34bdea-4548-3777-a64e-f758c90c834f"));
        uuidsToSkip.add(UUID.fromString("ea4e0e1d-2ac9-3bc4-94b7-aa01201c3617"));
        uuidsToSkip.add(UUID.fromString("ce76531b-c9e2-42ec-86ce-933b06ab26f0"));
        uuidsToSkip.add(UUID.fromString("d60abc94-ab2e-34ca-b4dc-2a8f58900f49"));
        uuidsToSkip.add(UUID.fromString("8ea4e7b6-9761-3ded-aaba-d7a63f3a97ba"));
        uuidsToSkip.add(UUID.fromString("dc2eaf33-e282-4892-a49e-9c37ca3f6fa0"));
        uuidsToSkip.add(UUID.fromString("a54dbce8-98d8-11e3-a5e2-0800200c9a66"));
        uuidsToSkip.add(UUID.fromString("06fdab86-7805-3edf-89e7-1dc1e1f777e7"));
        uuidsToSkip.add(UUID.fromString("ccf16924-3318-3a2b-882e-325c78332366"));
        uuidsToSkip.add(UUID.fromString("7713d598-ceb2-386a-b9e1-1ff307ec1801"));
        uuidsToSkip.add(UUID.fromString("58eb48a8-1419-3190-aaa4-5133a31d0f50"));
        uuidsToSkip.add(UUID.fromString("79c39774-2ae7-33c0-89cc-7711c4497fe3"));
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
        
        if(associationWriter != null){
            associationWriter.close();
        }
        
        if(attributeValueWriter != null){
            attributeValueWriter.close();
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
        boolean write = true;
        if(sameCycleStampNids.contains(concept.getConceptAttributes().getPrimordialVersion().getStampNid())){
            if(!concept.getVersion(viewCoordinate).isActive(viewCoordinate)){
                //concept was created and retired in same cycle, don't write any part
                write = false;
            }
        }
        if (write) {
            ConceptAttributeChronicleBI ca = concept.getConceptAttributes();
            if (!uuidsToSkip.contains(ca.getPrimUuid())) {
                processConceptAttribute(ca);
                for (RefexChronicleBI annot : ca.getAnnotations()) {
                    if (associationRefexNids.contains(annot.getRefexNid())) {
                        processAssociationRefset(annot);
                    } else if (attribValueRefexNids.contains(annot.getRefexNid())) {
                        processAttributeValueRefset(annot);
                    }
                }
            }

            if (concept.getDescriptions() != null) {
                for (DescriptionChronicleBI d : concept.getDescriptions()) {
                    processDescription(d);
                    if (d.getAnnotations() != null) {
                        for (RefexChronicleBI annot : d.getAnnotations()) {
                            int refexNid = annot.getRefexNid();
                            if (possibleLangRefexNids.contains(annot.getRefexNid())) {
                                langRefexNids.add(annot.getRefexNid());
                                processLangRefsets(annot);
                            } else if (associationRefexNids.contains(annot.getRefexNid())) {
                                processAssociationRefset(annot);
                            } else if (attribValueRefexNids.contains(annot.getRefexNid())) {
                                processAttributeValueRefset(annot);
                            }
                        }
                    }
                }
            }

            if (concept.getRelationshipsOutgoing() != null) {
                for (RelationshipChronicleBI r : concept.getRelationshipsOutgoing()) {
                    if(r.getPrimUuid().equals(UUID.fromString("824c4941-bf10-3fdd-a23f-c6a41c55f30c"))){
                        System.out.println("### DEBUG");
                    }
                    processRelationship(r);
                }
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
        
        if(conNumRefsetParentConcept != null){
            writeConFloatRefsetFiles();
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
        Collection<? extends ConceptVersionBI> childRefsetConcepts = refsetParentConcept.getRelationshipsIncomingSourceConceptsActiveIsa();
        for (ConceptVersionBI childRefset : childRefsetConcepts) {
            if (childRefset.getModuleNid() == Ts.get().getNidForUuids(UUID.fromString(module))) {
                String refsetNameOld = childRefset.getDescriptionPreferred().getText();
                String[] parts = refsetNameOld.split(" ");
                String refsetName = "";
                for(String part : parts){
                    String first = part.substring(0, 1);
                    String rest = part.substring(1);
                    first = first.toUpperCase();
                    part = first + rest;
                    refsetName = refsetName + part;
                }
                if(!refsetName.toLowerCase().contains("SimpleRefset")){
                    refsetName = refsetName.concat("SimpleRefset");
                }
                File simpleRefsetFile = new File(directory,
                        "der2_cRefset_" + refsetName + "_UUID" + releaseType.suffix + "_"
                        + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                        + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
                FileOutputStream simpleRefsetOs = new FileOutputStream(simpleRefsetFile);
                Writer simpleRefsetWriter = new BufferedWriter(new OutputStreamWriter(simpleRefsetOs, "UTF8"));

                for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                    simpleRefsetWriter.write(field.headerText + field.seperator);
                }
                
                Collection<RefexVersionBI> versions = new ArrayList<>();
                if (releaseType.equals(ReleaseType.FULL)) {
                    Collection<? extends RefexChronicleBI<?>> refsetMembers = childRefset.getRefsetMembers();
                    for (RefexChronicleBI member : refsetMembers) {
                        RefexVersionBI latest = (RefexVersionBI) member.getVersion(viewCoordinateAllStatus);
                        Collection<RefexVersionBI> refexVersions = member.getVersions();
                        for (RefexVersionBI r : refexVersions) {
                            if (!sameCycleStampNids.contains(r.getStampNid()) || (latest != null && r.getStampNid() == latest.getStampNid())) {
                                    versions.add(r);
                            }
                        }
                    }
                } else {
                    Collection<? extends RefexChronicleBI<?>> refsetMembers = childRefset.getRefsetMembers();
                    for (RefexChronicleBI member : refsetMembers) {
                        RefexVersionBI version = (RefexVersionBI) member.getVersion(viewCoordinateAllStatus);
                        if(version != null){
                            versions.add(version);
                        }
                    }
                }
                    for (RefexVersionBI refexVersion : versions) {
                        boolean write = true;
                        RefexChronicleBI chronicle = (RefexChronicleBI) refexVersion.getChronicle();
                        if (sameCycleStampNids.contains(refexVersion.getStampNid())) {
                            RefexVersionBI version = (RefexVersionBI) chronicle.getVersion(viewCoordinateAllStatus);
//                            if (!version.isActive(viewCoordinate)) {
//                                write = false;
//                            }
                        }
                        if (write) {
                            if (stampNids.contains(refexVersion.getStampNid())) {
                                for (Rf2File.SimpleRefsetFileFields field : Rf2File.SimpleRefsetFileFields.values()) {
                                    switch (field) {
                                        case ID:
                                            simpleRefsetWriter.write(refexVersion.getPrimUuid() + field.seperator);

                                            break;

                                        case EFFECTIVE_TIME:
                                            if (sameCycleStampNids.contains(refexVersion.getStampNid())) {
                                                simpleRefsetWriter.write(effectiveDateString + field.seperator);
                                            } else {
                                                simpleRefsetWriter.write(TimeHelper.getShortFileDateFormat().format(refexVersion.getTime()) + field.seperator);
                                            }
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
                        }
                    }
                processSimpleRefsetDesc(childRefset.getNid());
                simpleRefsetWriter.close();
            }
        }
    }
    
        /**
     * Writes the con float refset files for all of the refsets which are a child
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
    private void writeConFloatRefsetFiles() throws IOException, ContradictionException, NoSuchAlgorithmException {
        Collection<? extends ConceptVersionBI> childRefsetConcepts = conNumRefsetParentConcept.getRelationshipsIncomingSourceConceptsActiveIsa();
        for (ConceptVersionBI childRefset : childRefsetConcepts) {
            if (childRefset.getModuleNid() == Ts.get().getNidForUuids(UUID.fromString(module))) {
                String refsetNameOld = childRefset.getDescriptionPreferred().getText();
                String[] parts = refsetNameOld.split(" ");
                String refsetName = "";
                for(String part : parts){
                    String first = part.substring(0, 1);
                    String rest = part.substring(1);
                    first = first.toUpperCase();
                    part = first + rest;
                    refsetName = refsetName + part;
                }
                if(!refsetName.toLowerCase().contains("ConceptNumberRefset")){
                    refsetName = refsetName.concat("ConceptNumberRefset");
                }
                File conNumRefsetFile = new File(directory,
                        "der2_ciRefset_" + refsetName + "_UUID" + releaseType.suffix + "_"
                        + country.getFormatedCountryCode().toUpperCase() + namespace + "_"
                        + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
                FileOutputStream conNumRefsetOs = new FileOutputStream(conNumRefsetFile);
                Writer conNumRefsetWriter = new BufferedWriter(new OutputStreamWriter(conNumRefsetOs, "UTF8"));

                for (Rf2File.ConNumRefsetFileFields field : Rf2File.ConNumRefsetFileFields.values()) {
                    conNumRefsetWriter.write(field.headerText + field.seperator);
                }
                
                Collection<RefexVersionBI> versions = new ArrayList<>();
                if (releaseType.equals(ReleaseType.FULL)) {
                    Collection<? extends RefexChronicleBI<?>> refsetMembers = childRefset.getRefsetMembers();
                    for (RefexChronicleBI member : refsetMembers) {
                        RefexVersionBI latest = (RefexVersionBI) member.getVersion(viewCoordinateAllStatus);
                        Collection<RefexVersionBI> refexVersions = member.getVersions();
                        for (RefexVersionBI r : refexVersions) {
                            if (!sameCycleStampNids.contains(r.getStampNid()) || r.equals(latest)) {
                                versions.add(r);
                            }
                        }
                    }
                } else {
                    Collection<? extends RefexChronicleBI<?>> refsetMembers = childRefset.getRefsetMembers();
                    for (RefexChronicleBI member : refsetMembers) {
                        RefexVersionBI version = (RefexVersionBI) member.getVersion(viewCoordinateAllStatus);
                        versions.add(version);
                    }
                }
                boolean write = true;
                    for (RefexVersionBI refexVersion : versions) {
                        RefexNidFloatVersionBI nfVersion = (RefexNidFloatVersionBI) refexVersion;
                        RefexChronicleBI chronicle = (RefexChronicleBI) nfVersion.getChronicle();
                        if (sameCycleStampNids.contains(refexVersion.getStampNid())) {
                            RefexVersionBI version = (RefexVersionBI) chronicle.getVersion(viewCoordinateAllStatus);
                            if (!version.isActive(viewCoordinate)) {
                                write = false;
                            }
                        }
                        if (write) {
                            if (stampNids.contains(refexVersion.getStampNid())) {
                                for (Rf2File.ConNumRefsetFileFields field : Rf2File.ConNumRefsetFileFields.values()) {
                                    switch (field) {
                                        case ID:
                                            conNumRefsetWriter.write(nfVersion.getPrimUuid() + field.seperator);

                                            break;

                                        case EFFECTIVE_TIME:
                                            if (sameCycleStampNids.contains(nfVersion.getStampNid())) {
                                                conNumRefsetWriter.write(effectiveDateString + field.seperator);
                                            } else {
                                                conNumRefsetWriter.write(TimeHelper.getShortFileDateFormat().format(nfVersion.getTime()) + field.seperator);
                                            }
                                            break;

                                        case ACTIVE:
                                            conNumRefsetWriter.write(store.getComponent(nfVersion.getStatusNid()).getPrimUuid() + field.seperator);

                                            break;

                                        case MODULE_ID:
                                            conNumRefsetWriter.write(module + field.seperator);

                                            break;

                                        case REFSET_ID:
                                            conNumRefsetWriter.write(store.getComponent(nfVersion.getRefexNid()).getPrimUuid() + field.seperator);

                                            break;

                                        case REFERENCED_COMPONENT_ID:
                                            conNumRefsetWriter.write(store.getComponent(nfVersion.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                                            break;

                                        case ADDITIONAL_CONCEPT_ID:
                                            conNumRefsetWriter.write(store.getComponent(nfVersion.getNid1()).getPrimUuid() + field.seperator);
                                            break;

                                        case NUMBER:
                                            conNumRefsetWriter.write(nfVersion.getFloat1() + field.seperator);
                                    }
                                }
                            }
                        }
                    }
                processConNumRefsetDesc(childRefset.getNid());
                conNumRefsetWriter.close();
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
    private void processConceptAttribute(ConceptAttributeChronicleBI conceptAttributeChronicle) throws IOException, Exception {
        if (conceptAttributeChronicle != null) {
            ConceptAttributeVersionBI primordialVersion = conceptAttributeChronicle.getPrimordialVersion();
            Collection<ConceptAttributeVersionBI> versions = new HashSet<>();
            if (releaseType.equals(ReleaseType.FULL)) {
                //if not previously released or latest version remove
                ConceptAttributeVersionBI latest = conceptAttributeChronicle.getVersion(viewCoordinateAllStatus);
                for(ConceptAttributeVersionBI ca : conceptAttributeChronicle.getVersions()){
                    if(!sameCycleStampNids.contains(ca.getStampNid()) || (latest != null && ca.getStampNid() == latest.getStampNid())){
                        versions.add(ca);
                    }
                }
            } else {
                ConceptAttributeVersionBI version = conceptAttributeChronicle.getVersion(viewCoordinateAllStatus);
                if(version != null){
                    versions.add(version);
                }
            }
            boolean write = true;
            boolean writeIds = true;

            for (ConceptAttributeVersionBI car : versions) {
                if (sameCycleStampNids.contains(primordialVersion.getStampNid()) && sameCycleStampNids.contains(car.getStampNid())) {
                    if (car == null || !car.isActive(viewCoordinate)) { //concept has been created and inactivated in the same release cycle
                        write = false;
                    }
                }
                if (write) {
                    if (stampNids.contains(car.getStampNid())) {
                        if (writeIds) {
                            writeIds = false;
                            if (sameCycleStampNids.contains(car.getStampNid())) {
                                processIdentifiers(conceptAttributeChronicle.getPrimUuid(),
                                        car.getStampNid(),
                                        effectiveDate.getTime());
                            } else {
                                processIdentifiers(conceptAttributeChronicle.getPrimUuid(),
                                        car.getStampNid(),
                                        conceptAttributeChronicle.getPrimordialVersion().getTime());
                            }
                        }
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
                                    if (sameCycleStampNids.contains(car.getStampNid())) {
                                        conceptsWriter.write(effectiveDateString + field.seperator);
                                    } else {
                                        conceptsWriter.write(TimeHelper.getShortFileDateFormat().format(car.getTime()) + field.seperator);
                                    }
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
    private void processDescription(DescriptionChronicleBI descriptionChronicle) throws IOException, Exception {
        if (descriptionChronicle != null) {
            Collection<DescriptionVersionBI> versions = new HashSet<>();
            DescriptionVersionBI primordialVersion = descriptionChronicle.getPrimordialVersion();
            if (releaseType.equals(ReleaseType.FULL)) {
                //if not previously released or latest version remove
                DescriptionVersionBI latest = descriptionChronicle.getVersion(viewCoordinateAllStatus);
                for(DescriptionVersionBI d : descriptionChronicle.getVersions()){
                    if(!sameCycleStampNids.contains(d.getStampNid()) || (latest != null &&  d.getStampNid() == latest.getStampNid())){
                        versions.add(d);
                    }
                }
            } else {
                DescriptionVersionBI version = descriptionChronicle.getVersion(viewCoordinateAllStatus);
                if(version != null){
                    versions.add(version);
                }
            }
            boolean write = true;
            boolean writeIds = true;


            for (DescriptionVersionBI descr : versions) {
                if (sameCycleStampNids.contains(primordialVersion.getStampNid()) && sameCycleStampNids.contains(descr.getStampNid())) {
                    if (descr == null || !descr.isActive(viewCoordinate)) { //description has been created and retired in the same release cycle
                        write = false;
                    }
                }
                if (write) {
                    if (stampNids.contains(descr.getStampNid())) {
                        if (writeIds) {
                            writeIds = false;
                            if (sameCycleStampNids.contains(descr.getStampNid())) {
                                processIdentifiers(descriptionChronicle.getPrimUuid(), descr.getStampNid(), effectiveDate.getTime());
                            } else {
                                processIdentifiers(descriptionChronicle.getPrimUuid(), descr.getStampNid(), descriptionChronicle.getPrimordialVersion().getTime());
                            }
                        }
                        for (Rf2File.DescriptionsFileFields field : Rf2File.DescriptionsFileFields.values()) {
                            switch (field) {
                                case ACTIVE:
                                    descriptionsWriter.write(store.getUuidPrimordialForNid(descr.getStatusNid())
                                            + field.seperator);

                                    break;

                                case EFFECTIVE_TIME:
                                    if (sameCycleStampNids.contains(descr.getStampNid())) {
                                        descriptionsWriter.write(effectiveDateString + field.seperator);
                                    } else {
                                        descriptionsWriter.write(TimeHelper.getShortFileDateFormat().format(descr.getTime()) + field.seperator);
                                    }
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
    }

    /**
     * Determines if a relationship is stated or inferred and processes the
     * relationship accordingly. Only the versions which have a stamp nid in the
     * specified collection of stamp nids will be written.
     *
     * @param relationshipChronicle the relationship to process
     * @throws IOException signals that an I/O exception has occurred
     */
    private void processRelationship(RelationshipChronicleBI relationshipChronicle) throws IOException, Exception {
        if (relationshipChronicle != null) {
            Collection<RelationshipVersionBI> versions = new HashSet<>();
            RelationshipVersionBI primordialVersion = relationshipChronicle.getPrimordialVersion();
            if (releaseType.equals(ReleaseType.FULL)) {
                //if not previously released or latest version remove
                RelationshipVersionBI latest = relationshipChronicle.getVersion(viewCoordinateAllStatus);
                for(RelationshipVersionBI r : relationshipChronicle.getVersions()){
                    if(!sameCycleStampNids.contains(r.getStampNid()) || (latest != null && r.getStampNid() == latest.getStampNid())){
                        versions.add(r);
                    }
                }
            } else {
                RelationshipVersionBI version = relationshipChronicle.getVersion(viewCoordinateAllStatus);
                if(version != null){
                    versions.add(version);
                }
            }
            boolean write = true;

            boolean writeIds = true;
            boolean inTaxonomy = false;
            for (RelationshipVersionBI rv : versions) {
                //TODO: need to support refset specs
                if (sameCycleStampNids.contains(primordialVersion.getStampNid()) && sameCycleStampNids.contains(rv.getStampNid())) {
                    if (!rv.isActive(viewCoordinate)) { //relationship has been created and retired in the same release cycle
                        write = false;
                    }
                }
                if (write) {
                    if (rv.getTypeNid() != RefsetAux.MARKED_PARENT_ISA.getLenient().getConceptNid()) {
                        if (stampNids.contains(rv.getStampNid())) {
                            for (int parentNid : taxonomyParentNids) {
                                if (Ts.get().wasEverKindOf(rv.getTargetNid(), parentNid, viewCoordinate)) {
                                    inTaxonomy = true;
                                }
                            }
                            if (inTaxonomy) {
                                if (writeIds) {
                                    writeIds = false;
                                    if (sameCycleStampNids.contains(rv.getStampNid())) {
                                        processIdentifiers(relationshipChronicle.getPrimUuid(),
                                                rv.getStampNid(),
                                                effectiveDate.getTime());
                                    } else {
                                        processIdentifiers(relationshipChronicle.getPrimUuid(),
                                                rv.getStampNid(),
                                                relationshipChronicle.getPrimordialVersion().getTime());
                                    }
                                }
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
                    if (sameCycleStampNids.contains(relationshipVersion.getStampNid())) {
                        relationshipsWriter.write(effectiveDateString + field.seperator);
                    }else{
                        relationshipsWriter.write(TimeHelper.getShortFileDateFormat().format(relationshipVersion.getTime()) + field.seperator);
                    }
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
                    relationshipsWriter.write(Snomed.SOME.getLenient().getPrimUuid()
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
                    if (sameCycleStampNids.contains(relationshipVersion.getStampNid())) {
                        relationshipsStatedWriter.write(effectiveDateString + field.seperator);
                    }else{
                        relationshipsStatedWriter.write(TimeHelper.getShortFileDateFormat().format(relationshipVersion.getTime()) + field.seperator);
                    }
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
                    relationshipsStatedWriter.write(Snomed.SOME.getLenient().getPrimUuid()
                            + field.seperator);

                    break;
            }
        }
    }
    
    private void processAssociationRefset(RefexChronicleBI refexChronicle) throws IOException, Exception {
        if (refexChronicle != null) {
            Collection<RefexVersionBI> versions = new HashSet<>();
            ComponentVersionBI primordialVersion = refexChronicle.getPrimordialVersion();
            if (releaseType.equals(ReleaseType.FULL)) {
                //if not previously released or latest version remove
                RefexVersionBI latest = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                for(Object o : refexChronicle.getVersions()){
                    RefexVersionBI r = (RefexVersionBI)o;
                    if(!sameCycleStampNids.contains(r.getStampNid()) || (latest != null && r.getStampNid() == latest.getStampNid())){
                        versions.add(r);
                    }
                }
            } else {
                RefexVersionBI version = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                if(version != null){
                    versions.add(version);
                }
            }
            boolean write = true;
                for (RefexVersionBI refex : versions) {
                    if (sameCycleStampNids.contains(primordialVersion.getStampNid()) && sameCycleStampNids.contains(refex.getStampNid())) {
                        RefexVersionBI version = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                        if (version == null || !version.isActive(viewCoordinate)) { //refex member has been created and inactivated during the same release cycle
                            write = false;
                        }
                    }
                    if (stampNids.contains(refex.getStampNid()) && write) {
                        RefexNidVersionBI member = (RefexNidVersionBI) refex;
                        for (Rf2File.AssociationRefsetFileFields field : Rf2File.AssociationRefsetFileFields.values()) {
                            switch (field) {
                                case ID:
                                    associationWriter.write(member.getPrimUuid().toString() + field.seperator);
                                    break;
                                case EFFECTIVE_TIME:
                                    if (sameCycleStampNids.contains(member.getStampNid())) {
                                        associationWriter.write(effectiveDateString + field.seperator);
                                    }else{
                                        associationWriter.write(TimeHelper.getShortFileDateFormat().format(member.getTime()) + field.seperator);
                                    }                                    break;
                                case ACTIVE:
                                    associationWriter.write(Ts.get().getUuidPrimordialForNid(member.getStatusNid())
                                            + field.seperator);
                                    break;
                                case MODULE_ID:
                                    associationWriter.write(module + field.seperator);
                                    break;
                                case REFSET_ID:
                                    associationWriter.write(Ts.get().getUuidPrimordialForNid(member.getRefexNid()) + field.seperator);
                                    break;

                                case REFERENCED_COMPONENT_ID:
                                    associationWriter.write(Ts.get().getUuidPrimordialForNid(member.getReferencedComponentNid())
                                            + field.seperator);
                                    break;

                                case TARGET:
                                    associationWriter.write(Ts.get().getUuidPrimordialForNid(member.getNid1())
                                            + field.seperator);
                                    break;
                            }
                        }
                    }
                }
        }
    }

      private void processAttributeValueRefset(RefexChronicleBI refexChronicle) throws IOException, Exception {
        if (refexChronicle != null) {
            ComponentVersionBI primordialVersion = refexChronicle.getPrimordialVersion();
            Collection<RefexVersionBI> versions = new HashSet<>();
            if (releaseType.equals(ReleaseType.FULL)) {
                //if not previously released or latest version remove
                RefexVersionBI latest = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                for(Object o : refexChronicle.getVersions()){
                    RefexVersionBI r = (RefexVersionBI)o;
                    if(!sameCycleStampNids.contains(r.getStampNid()) || (latest != null && r.getStampNid() == latest.getStampNid())){
                        versions.add(r);
                    }
                }
            } else {
                RefexVersionBI version = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                if(version != null){
                    versions.add(version);
                }
            }
            boolean write = true;
            
                for (RefexVersionBI refex : versions) {
                    if (sameCycleStampNids.contains(primordialVersion.getStampNid()) && sameCycleStampNids.contains(refex.getStampNid())) {
                        RefexVersionBI version = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                        if (!version.isActive(viewCoordinate)) { //refex member has been created and inactivated during the same release cycle
                            write = false;
                        }
                    }
                    if (stampNids.contains(refex.getStampNid()) && write) {
                        RefexNidVersionBI member = (RefexNidVersionBI) refex;
                        for (Rf2File.AttribValueRefsetFileFields field : Rf2File.AttribValueRefsetFileFields.values()) {
                            switch (field) {
                                case ID:
                                    attributeValueWriter.write(member.getPrimUuid().toString() + field.seperator);
                                    break;
                                case EFFECTIVE_TIME:
                                    if (sameCycleStampNids.contains(refex.getStampNid())) {
                                        attributeValueWriter.write(effectiveDateString + field.seperator);
                                    }else{
                                        attributeValueWriter.write(TimeHelper.getShortFileDateFormat().format(member.getTime()) + field.seperator);
                                    }                                    
                                    break;
                                case ACTIVE:
                                    attributeValueWriter.write(Ts.get().getUuidPrimordialForNid(member.getStatusNid())
                                            + field.seperator);
                                    break;
                                case MODULE_ID:
                                    attributeValueWriter.write(module + field.seperator);
                                    break;
                                case REFSET_ID:
                                    attributeValueWriter.write(Ts.get().getUuidPrimordialForNid(member.getRefexNid())
                                            + field.seperator);
                                    break;
                                case REFERENCED_COMPONENT_ID:
                                    attributeValueWriter.write(Ts.get().getUuidPrimordialForNid(member.getReferencedComponentNid()) + field.seperator);
                                    break;
                                case VALUE_ID:
                                    attributeValueWriter.write(Ts.get().getUuidPrimordialForNid(member.getNid1()) + field.seperator);
                                    break;
                            }
                        }
                    }
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
    private void processIdentifiers(UUID primordialUuid, int primordialStampNid, long time) throws IOException {
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
                            identifiersWriter.write(TimeHelper.getShortFileDateFormat().format(time) + field.seperator);

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
    private void processLangRefsets(RefexChronicleBI refexChronicle) throws IOException, ContradictionException {
        if (refexChronicle != null) {
            if (!excludedRefsetIds.contains(refexChronicle.getRefexNid())) {
                Collection<RefexVersionBI> versions = new HashSet<>();
                if (releaseType.equals(ReleaseType.FULL)) {
                    //if not previously released or latest version remove
                    RefexVersionBI latest = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatusTime); //CHANGE FOR DK, before merge back use viewCoordinateAllStatus
                    for (Object o : refexChronicle.getVersions()) {
                        RefexVersionBI r = (RefexVersionBI) o;
                        if (!sameCycleStampNids.contains(r.getStampNid()) || (latest != null && r.getStampNid() == latest.getStampNid())) {
                            versions.add(r);
                        }
                    }
                } else {
                    RefexVersionBI version = (RefexVersionBI) refexChronicle.getVersion(viewCoordinateAllStatus);
                    if (version != null) {
                        versions.add(version);
                    }
                }
                boolean write = true;
                for (RefexVersionBI rv : versions) {
                    if (sameCycleStampNids.contains(refexChronicle.getPrimordialVersion().getStampNid())) {
                        if (!rv.isActive(viewCoordinate)) {
                            //refex was created and retired in same cycle, don't write any part
                            write = false;
                        }
                    }
                    if (stampNids.contains(rv.getStampNid()) && write) {
                        if (refexChronicle.getRefexNid() == RefsetAux.EN_GB_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else if (refexChronicle.getRefexNid() == RefsetAux.EN_US_REFEX.getLenient().getNid()) {
                            processLang(rv);
                        } else {
                            if(otherLangRefsetsWriter != null){
                                processOtherLang(rv);
                            }
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
        RefexNidVersionBI refexNidVersion = (RefexNidVersionBI) refexVersion;
        if (refexVersion != null) {
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        langRefsetsWriter.write(refexVersion.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        if (sameCycleStampNids.contains(refexVersion.getStampNid())) {
                            langRefsetsWriter.write(effectiveDateString + field.seperator);
                        }else{
                            langRefsetsWriter.write(TimeHelper.getShortFileDateFormat().format(refexVersion.getTime()) + field.seperator);
                        }
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
            RefexNidVersionBI refexNidVersion = (RefexNidVersionBI) refexVersion;
            for (Rf2File.LanguageRefsetFileFields field : Rf2File.LanguageRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        otherLangRefsetsWriter.write(refexVersion.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        if (sameCycleStampNids.contains(refexVersion.getStampNid())) {
                            otherLangRefsetsWriter.write(effectiveDateString+ field.seperator);
                        }else{
                            otherLangRefsetsWriter.write(TimeHelper.getShortFileDateFormat().format(refexVersion.getTime()) + field.seperator);
                        }
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attributeDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attributeType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(0 + field.seperator);

                    break;
            }
        }
    }
    
    /**
     * Writes the refset description file for concept number refexes according to
     * the
     * <code>Rf2File.RefsetDescriptorFileFields</code>.
     *
     * @param refexNid the nid associated with a language refex
     * @throws IOException signals that an I/O exception has occurred
     * @throws NoSuchAlgorithmException indicates that a no such algorithm
     * exception has occurred
     * @see Rf2File.RefsetDescriptorFileFields
     */
    private void processConNumRefsetDesc(int refexNid) throws IOException, NoSuchAlgorithmException {
        ConceptSpec refsetDescriptor = new ConceptSpec("Reference set descriptor reference set (foundation metadata concept)",
                UUID.fromString("5ddff82f-5aee-3b16-893f-6b7aa726cc4b"));
        ConceptSpec attributeDescriptor = new ConceptSpec("Referenced component",
                UUID.fromString("b0d88038-7f87-31a5-873e-d023e2484d0e"));
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attributeDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attributeType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(0 + field.seperator);

                    break;
            }
        }
        //additional concept
        attributeDescriptor = new ConceptSpec("Attribute type",
                UUID.fromString("34e794d9-0405-3aa1-adf5-64801950c397")); //attribute description
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attributeDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attributeType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(1 + field.seperator);

                    break;
            }
        }
        
        //additional number
        attributeDescriptor = new ConceptSpec("Attribute type",
                UUID.fromString("34e794d9-0405-3aa1-adf5-64801950c397")); //attribute description
        attributeType = new ConceptSpec("Integer",
                UUID.fromString("42d9f81e-27e9-3b73-9c19-9de4e2346b44"));
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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attributeDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attributeType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_ORDER:
                    refsetDescWriter.write(2 + field.seperator);

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
                    refsetDescWriter.write(refsetDescriptor.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    refsetDescWriter.write(store.getUuidPrimordialForNid(refexNid) + field.seperator);

                    break;

                case ATTRIB_DESC:
                    refsetDescWriter.write(attribDesc.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case ATTRIB_TYPE:
                    refsetDescWriter.write(attribType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

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
    private void processModularDependency() throws IOException, NoSuchAlgorithmException, ContradictionException {
        ConceptSpec coreModule = new ConceptSpec("SNOMED CT core module (core metadata concept)",
                UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
        Collection<? extends RefexChronicleBI<?>> refsetMembers = Ts.get().getConcept(
                Snomed.MODULE_DEPENDENCY.getLenient().getPrimUuid()).getRefsetMembers();
        for (RefexChronicleBI refexChronicle : refsetMembers) {
            Collection<RefexVersionBI> versions = new HashSet<>();
            if (releaseType.equals(ReleaseType.DELTA)) { //This is for delta
                for (Object o : refexChronicle.getVersions()) {
                    RefexVersionBI version = (RefexVersionBI) o;
                    if (version.getTime() > previousReleaseDate.getTime()) {
                        versions.add(version);
                    }
                }
            } else {
                //if not previously released or latest version remove
                RefexVersionBI latest = (RefexVersionBI) refexChronicle.getVersions().iterator().next(); //will only be one, maintained programatically
                versions.add(latest);
            }
            boolean write = true;
            for (RefexVersionBI rv : versions) {
                if (!rv.isActive(viewCoordinate)) {
                    if (sameCycleStampNids.contains(rv.getStampNid())) {
                        write = false;
                    }
                }
                if (rv.isActive(viewCoordinate)) { //CHANGE FOR DK, source data incorrect, retired descriptions should also have retired lang refsets
                    ComponentVersionBI rc = Ts.get().getComponentVersion(viewCoordinate, rv.getReferencedComponentNid());
                    if (rc == null || !rc.isActive(viewCoordinate)) {
                        write = false;
                    }
                }
                RefexStringStringVersionBI rssv = (RefexStringStringVersionBI) rv;
                for (Rf2File.ModuleDependencyFileFields field : Rf2File.ModuleDependencyFileFields.values()) {
                    switch (field) {
                        case ID:
                            modDependWriter.write(rssv.getPrimUuid() + field.seperator);

                            break;

                        case EFFECTIVE_TIME:
                            if(rssv.getTime() > previousReleaseDate.getTime()){
                                modDependWriter.write(effectiveDateString + field.seperator);
                            }else{
                                modDependWriter.write(TimeHelper.getShortFileDateFormat().format(rssv.getTime()) + field.seperator);
                            }
                            break;

                        case ACTIVE:
                            modDependWriter.write(store.getUuidPrimordialForNid(rssv.getStatusNid()) + field.seperator);

                            break;

                        case MODULE_ID:
                            modDependWriter.write(module + field.seperator);

                            break;

                        case REFSET_ID:
                            modDependWriter.write(Snomed.MODULE_DEPENDENCY.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                            break;

                        case REFERENCED_COMPONENT_ID:
                            modDependWriter.write(store.getUuidPrimordialForNid(rssv.getReferencedComponentNid()) + field.seperator);

                            break;

                        case SOURCE_TIME:
                            modDependWriter.write(rssv.getString1() + field.seperator);

                            break;

                        case TARGET_TIME:
                            modDependWriter.write(rssv.getString2() + field.seperator);

                            break;
                    }
                }
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
                            descType.getStrict(viewCoordinateAllStatus).getPrimUuid().toString());
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
                    descTypeWriter.write(descFormatRefex.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case REFERENCED_COMPONENT_ID:
                    descTypeWriter.write(descType.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

                    break;

                case DESC_FORMAT:
                    descTypeWriter.write(descFormat.getStrict(viewCoordinateAllStatus).getPrimUuid() + field.seperator);

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
        if(conceptsToProcess.isMember(cNid)){
            process(fetcher.fetch());
        }
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
