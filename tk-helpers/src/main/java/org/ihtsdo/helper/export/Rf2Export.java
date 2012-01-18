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
import java.util.*;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid.RefexCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_cnid.RefexCnidCnidCnidVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_cnid_str.RefexCnidCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid_str.RefexCnidStrVersionBI;
import org.ihtsdo.tk.api.refex.type_str.RefexStrVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ConceptSpec;

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
    LANG_CODE language;
    String namespace;
    String module;
    Writer relationshipsWriter;
    ReleaseType releaseType;
    Set<Integer> sapNids;
    TerminologyStoreDI store;
    ViewCoordinate vc;
    Writer conRefsetsWriter;
    Writer conConRefsetsWriter;
    Writer conConConRefsetsWriter;
    Writer conConStrRefsetsWriter;
    Writer stringRefsetsWriter;
    Set<Integer> excludedRefsetIds;
    ConceptSpec uuidIdScheme = new ConceptSpec("SNOMED CT universally unique identifier (core metadata concept)",
            UUID.fromString("680f3f6c-7a2a-365d-b527-8c9a96dd1a94"));

    //~--- constructors --------------------------------------------------------
    public Rf2Export(File directory, ReleaseType releaseType, LANG_CODE language, COUNTRY_CODE country,
            String namespace, String module, Date effectiveDate, Set<Integer> sapNids, ViewCoordinate vc,
            Set<Integer> excludedRefsetIds, NidBitSetBI conceptsToProcess)
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
        this.effectiveDateString = TimeHelper.formatDateForFile(effectiveDate.getTime());
        this.excludedRefsetIds = excludedRefsetIds;

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
        File identifiersFile = new File(directory,
                "sct2_Identifier_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File conRefsetsFile = new File(directory,
                "sct2_ConceptRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File conConRefsetsFile = new File(directory,
                "sct2_ConceptConceptRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File conConConRefsetsFile = new File(directory,
                "sct2_ConceptConceptConceptRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File conConStrRefsetsFile = new File(directory,
                "sct2_ConceptConceptStringRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");
        File stringRefsetsFile = new File(directory,
                "sct2_StringRefset_UUID_" + releaseType.suffix + "_"
                + country.getFormatedCountryCode() + namespace + "_"
                + TimeHelper.getShortFileDateFormat().format(effectiveDate) + ".txt");

        conceptsWriter = new BufferedWriter(new FileWriter(conceptsFile));
        descriptionsWriter = new BufferedWriter(new FileWriter(descriptionsFile));
        relationshipsWriter = new BufferedWriter(new FileWriter(relationshipsFile));
        identifiersWriter = new BufferedWriter(new FileWriter(identifiersFile));
        conRefsetsWriter = new BufferedWriter(new FileWriter(conRefsetsFile));
        conConRefsetsWriter = new BufferedWriter(new FileWriter(conConRefsetsFile));
        conConConRefsetsWriter = new BufferedWriter(new FileWriter(conConConRefsetsFile));
        conConStrRefsetsWriter = new BufferedWriter(new FileWriter(conConStrRefsetsFile));
        stringRefsetsWriter = new BufferedWriter(new FileWriter(stringRefsetsFile));


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
    }

    @Override
    public boolean continueWork() {
        return true;
    }

    public void process(ConceptChronicleBI c) throws Exception {
        processRefset(c);

        ConAttrChronicleBI ca = c.getConAttrs();
        processConceptAttribute(ca);
        processIdentifiers(ca.getPrimUuid(), ca.getPrimordialVersion().getSapNid());
        if (ca.getAnnotations() != null) {
            processAnnotations((Collection<RefexVersionBI<?>>) ca.getAnnotations());
        }

        if (c.getDescs() != null) {
            for (DescriptionChronicleBI d : c.getDescs()) {
                processDescription(d);
                processIdentifiers(d.getPrimUuid(), d.getPrimordialVersion().getSapNid());
                if (d.getAnnotations() != null) {
                    processAnnotations((Collection<RefexVersionBI<?>>) d.getAnnotations());
                }
            }
        }

        if (c.getRelsOutgoing() != null) {
            for (RelationshipChronicleBI r : c.getRelsOutgoing()) {
                processRelationship(r);
                processIdentifiers(r.getPrimUuid(), r.getPrimordialVersion().getSapNid());
                if (r.getAnnotations() != null) {
                    processAnnotations((Collection<RefexVersionBI<?>>) r.getAnnotations());
                }
            }
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

    private void processAnnotations(Collection<RefexVersionBI<?>> annotations) throws IOException {
        for (RefexChronicleBI annot : annotations) {
            if (!excludedRefsetIds.contains(annot.getCollectionNid())) {
                Collection<RefexVersionBI<?>> versions = annot.getVersions(vc);
                for (RefexVersionBI r : versions) {
                    if (!sapNids.contains(r.getSapNid())) {
                        break;
                    } else {
                        Class<? extends RefexVersionBI> aClass = r.getClass();
                        if (RefexCnidCnidCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConConRefsets((RefexCnidCnidCnidVersionBI) r);
                        } else if (RefexCnidCnidStrVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConStrRefsets((RefexCnidCnidStrVersionBI) r);
                        } else if (RefexCnidCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConRefsets((RefexCnidCnidVersionBI) r);
                        } else if (RefexCnidStrVersionBI.class.isAssignableFrom(r.getClass())) {
                            processStringRefsets((RefexCnidStrVersionBI) r);
                        } else if (RefexStrVersionBI.class.isAssignableFrom(r.getClass())) {
                            processStringRefsets((RefexStrVersionBI) r);
                        } else if (RefexCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConRefsets((RefexCnidVersionBI) r);
                        }
                    }
                }
                if (annot.getAnnotations() != null) {
                    processAnnotations((Collection<RefexVersionBI<?>>) annot.getAnnotations());
                }
            }
        }
    }

    private void processRefset(ConceptChronicleBI concept) throws IOException {
        Collection<? extends RefexChronicleBI<?>> refsetMembers = concept.getRefsetMembers();
        if (refsetMembers != null) {
            NEXT_MEMBER: for (RefexChronicleBI member : refsetMembers) {
                if (excludedRefsetIds.contains(member.getCollectionNid())) {
                    break NEXT_MEMBER;
                }
                Collection<RefexVersionBI> versions = member.getVersions(vc);
                NEXT_VERSION: for (RefexVersionBI r : versions) {
                    if (!sapNids.contains(r.getSapNid())) {
                        break NEXT_VERSION;
                    } else {
                        if (RefexCnidCnidCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConConRefsets((RefexCnidCnidCnidVersionBI) r);
                        } else if (RefexCnidCnidStrVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConStrRefsets((RefexCnidCnidStrVersionBI) r);
                        } else if (RefexCnidCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConConRefsets((RefexCnidCnidVersionBI) r);
                        } else if (RefexStrVersionBI.class.isAssignableFrom(r.getClass())) {
                            processStringRefsets((RefexStrVersionBI) r);
                        } else if (RefexCnidVersionBI.class.isAssignableFrom(r.getClass())) {
                            processConRefsets((RefexCnidVersionBI) r);
                        }
                    }
                }
            }
        }
    }

    private void processConRefsets(RefexCnidVersionBI r) throws IOException {
        if (r != null) {
            for (Rf2File.ConRefsetFileFields field : Rf2File.ConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        conRefsetsWriter.write(r.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        conRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        conRefsetsWriter.write(store.getComponent(r.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        conRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        conRefsetsWriter.write(store.getComponent(r.getCollectionNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        conRefsetsWriter.write(store.getComponent(r.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT:
                        conRefsetsWriter.write(store.getComponent(r.getCnid1()).getPrimUuid() + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConRefsets(RefexCnidCnidVersionBI r) throws IOException {
        if (r != null) {
            for (Rf2File.ConConRefsetFileFields field : Rf2File.ConConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        conConRefsetsWriter.write(r.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        conConRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        conConRefsetsWriter.write(store.getComponent(r.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        conConRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        conConRefsetsWriter.write(store.getComponent(r.getCollectionNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        conConRefsetsWriter.write(store.getComponent(r.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT1:
                        conConRefsetsWriter.write(store.getComponent(r.getCnid1()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT2:
                        conConRefsetsWriter.write(store.getComponent(r.getCnid2()).getPrimUuid() + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConConRefsets(RefexCnidCnidCnidVersionBI r) throws IOException {
        if (r != null) {
            for (Rf2File.ConConConRefsetFileFields field : Rf2File.ConConConRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        conConConRefsetsWriter.write(r.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        conConConRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        conConConRefsetsWriter.write(store.getComponent(r.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        conConConRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        conConConRefsetsWriter.write(store.getComponent(r.getCollectionNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        conConConRefsetsWriter.write(store.getComponent(r.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT1:
                        conConConRefsetsWriter.write(store.getComponent(r.getCnid1()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT2:
                        conConConRefsetsWriter.write(store.getComponent(r.getCnid2()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT3:
                        conConConRefsetsWriter.write(store.getComponent(r.getCnid3()).getPrimUuid() + field.seperator);

                        break;
                }
            }
        }
    }

    private void processConConStrRefsets(RefexCnidCnidStrVersionBI r) throws IOException {
        if (r != null) {
            for (Rf2File.ConConStrRefsetFileFields field : Rf2File.ConConStrRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        conConStrRefsetsWriter.write(r.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        conConStrRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        conConStrRefsetsWriter.write(store.getComponent(r.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        conConStrRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        conConStrRefsetsWriter.write(store.getComponent(r.getCollectionNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        conConStrRefsetsWriter.write(store.getComponent(r.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT1:
                        conConStrRefsetsWriter.write(store.getComponent(r.getCnid1()).getPrimUuid() + field.seperator);

                        break;

                    case CONCEPT2:
                        conConStrRefsetsWriter.write(store.getComponent(r.getCnid2()).getPrimUuid() + field.seperator);

                        break;

                    case STRING:
                        conConStrRefsetsWriter.write(r.getStr1() + field.seperator);

                        break;
                }
            }
        }
    }

    private void processStringRefsets(RefexStrVersionBI r) throws IOException {
        if (r != null) {
            for (Rf2File.StringRefsetFileFields field : Rf2File.StringRefsetFileFields.values()) {
                switch (field) {
                    case ID:
                        stringRefsetsWriter.write(r.getPrimUuid() + field.seperator);

                        break;

                    case EFFECTIVE_TIME:
                        stringRefsetsWriter.write(effectiveDateString + field.seperator);

                        break;

                    case ACTIVE:
                        stringRefsetsWriter.write(store.getComponent(r.getStatusNid()).getPrimUuid() + field.seperator);

                        break;

                    case MODULE_ID:
                        stringRefsetsWriter.write(module + field.seperator);

                        break;

                    case REFSET_ID:
                        stringRefsetsWriter.write(store.getComponent(r.getCollectionNid()).getPrimUuid() + field.seperator);

                        break;

                    case REFERENCED_COMPONENT_ID:
                        stringRefsetsWriter.write(store.getComponent(r.getReferencedComponentNid()).getPrimUuid() + field.seperator);

                        break;

                    case STRING:
                        stringRefsetsWriter.write(r.getStr1() + field.seperator);

                        break;
                }
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
