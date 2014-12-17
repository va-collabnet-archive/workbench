/*
 * Copyright 2014 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.maven.rf2;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.etypes.*;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_boolean.TkRefexBooleanRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_int.TkRefexIntRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrRevision;
import org.ihtsdo.tk.dto.concept.component.refex.type_uuid.TkRefexUuidRevision;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 *
 * checklist. <br>
 * _√_ path added to all new objects <br>
 * _√_ module added to all new objects <br>
 * _√_ remove ' for description text <br>
 * _√_ add KP UUID<br>
 * _√_ add EIdentifierUuid ___ connect components to concepts at transition
 *
 * @author code
 *
 * @goal longform-to-eccs
 * @requiresDependencyResolution compile
 * @requiresProject false
 */
public class LongformToEccs extends AbstractMojo {

    private static final String FILE_SEPARATOR = File.separator;
    /**
     * Module UUID.
     *
     * Default value from TkRevision.unspecifiedModuleUuid<br>
     * 40d1c869-b509-32f8-b735-836eac577a67
     *
     * KP Extension module (core metadata concept)<br>
     * 815d5052-4fd5-599f-b996-4640eb166eeb
     *
     * @parameter default-value="815d5052-4fd5-599f-b996-4640eb166eeb"
     */
    private UUID uuidModule;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}/generated-resources/longform"
     * @required
     */
    private File longformDirectory;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDir;

    //
    private UUID uuidSourceKpUuid;
    private UUID uuidActive;
    private UUID uuidInactive;

    public void setUuidModule(String uuidStr) {
        uuidModule = UUID.fromString(uuidStr);
    }

    /**
     * Path UUID.
     *
     * KPET CMT Project development path 3770e517-7adc-5a24-a447-77a9daa3eedf
     *
     * @parameter default-value="3770e517-7adc-5a24-a447-77a9daa3eedf"
     */
    private UUID uuidPath;

    public void setUuidPath(String uuidStr) {
        uuidPath = UUID.fromString(uuidStr);
    }

    private HashMap<String, UUID> authorUuidMap;
    private HashMap<String, UUID> conceptUuidMap;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        uuidActive = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
        uuidInactive = SnomedMetadataRf2.INACTIVE_VALUE_RF2.getUuids()[0];
        uuidSourceKpUuid = UUID.fromString("a27dce27-22d8-5729-8d3c-10b2708ef366");

        setupAuthorUuidMap();
        setupConceptUuidMap();

        // DataOutputStream dos
        File[] fileArray = longformDirectory.listFiles();
        // File outputFile = new File(outputDir.getAbsolutePath() + FILE_SEPARATOR + "Alisa Papotto#4#" + UUID.randomUUID().toString() + ".eccs");
        try {
            for (File f : fileArray) {
                File outputFile = new File(outputDir.getAbsolutePath() + FILE_SEPARATOR + UUID.randomUUID().toString() + ".eccs");
                System.out.println("\nOUTPUT FILE: " + outputFile.getAbsolutePath() + "\n");
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
                        new FileOutputStream(outputFile)
                ));
                System.out.println("\nPROCESSING FILE: " + f.getAbsolutePath() + "\n");

                EConcept ec = parseFile(f);

                dos.writeLong(ec.conceptAttributes.time);
                ec.writeExternal(dos);

                StringBuilder sb = new StringBuilder();
                sb.append("\n:::::::::::");
                sb.append("\n::CONCEPT::");
                sb.append("\n:::::::::::");
                sb.append("\n::TIME=");
                sb.append(Long.toString(ec.conceptAttributes.time));
                sb.append("\n");
                sb.append(ec.toString());
                sb.append("\n");
                getLog().info(sb.toString());
                dos.flush();
                dos.close();
            }

        } catch (IOException | ParseException ex) {
            Logger.getLogger(LongformToEccs.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoFailureException("LongformToEccs FAILED\n", ex);
        }

    }

    public enum States {

        CONCEPT,
        CONCEPT_ATTRIBUTE,
        CONCEPT_ATTRIBUTE_VERSION,
        CONCEPT_EXTRA_IDS,
        CONCEPT_ANNOTATION,
        DESCRIPTION,
        DESCRIPTION_EXTRA_IDS,
        DESCRIPTION_ANNOTATION,
        RELATIONSHIP,
        RELATIONSHIP_EXTRA_IDS,
        PROTOTYPE
    }

    private EConcept parseFile(File f) throws IOException, ParseException, MojoFailureException {
        FileInputStream is = new FileInputStream(f);
        InputStreamReader isr = new InputStreamReader(is, "UTF-8");
        ArrayList<Longform_ConRecord> conList = new ArrayList<>();
        ArrayList<Longform_DesRecord> desList = new ArrayList<>();
        ArrayList<Longform_RelRecord> relList = new ArrayList<>();
        ArrayList<Longform_AnnotationRecord> rsByConList = new ArrayList<>();
        ArrayList<Longform_AnnotationRecord> rsByRsList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(isr)) {
            States currentState = States.CONCEPT;
            States nextState = States.CONCEPT;
            Longform_ConRecord concept = null;
            Longform_IdRecord id = null;
            Longform_DesRecord description = null;
            Longform_RelRecord relationship = null;
            Longform_AnnotationRecord member = null;
            UUID uuid;
            UUID kpUuid = null;
            Longform_DesRecord primordialDescription = null;
            Longform_AnnotationRecord primordialMember = null;

            while (br.ready()) {
                String[] line = br.readLine().split(":");
                if (line.length == 1) {
                    System.out.print(line[0] + "\n");

                } else if (line.length >= 2) {
                    System.out.print(line[0] + ":" + line[1] + "\n");
                }
                if (line.length > 0) {
                    switch (currentState) {
                        case CONCEPT:
                            switch (line[0]) {
                                case "Concept":
                                    concept = new Longform_ConRecord();
                                    concept.annotations = new ArrayList<>();
                                    conList.add(concept);
                                    break;
                                case "annotationRefset":
                                    break;
                                case "annotationIndex":
                                    break;
                                case "ConceptAttributes":
                                    nextState = States.CONCEPT_ATTRIBUTE;
                                    break;
                            }
                            break;

                        case CONCEPT_ATTRIBUTE:
                            switch (line[0]) {
                                case "defined": // true | false
                                    assert (concept != null);
                                    concept.isPrimitive = 0; // defined is not primitive
                                    if (line[1].compareToIgnoreCase("false") == 0) {
                                        concept.isPrimitive = 1; // not defined is primitive
                                    }
                                    break;
                                case "pUuid": //
                                    assert (concept != null);
                                    uuid = UUID.fromString(line[1]);
                                    kpUuid = uuid;
                                    concept.conUuidLsb = uuid.getLeastSignificantBits();
                                    concept.conUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "status": // current | retired
                                    assert (concept != null);
                                    concept.status = processStatus(line[1]);
                                    break;
                                case "author": //
                                    assert (concept != null);
                                    concept.authorUuid = authorUuidMap.get(line[1]);
                                    break;
                                case "tm": // 1363457441966
                                    assert (concept != null);
                                    concept.revTime = processTime(line[1]);
                                    break;
                                case "extraVersions": // IGNORED
                                    if (line[1].compareToIgnoreCase("null") != 0) {
                                        throw new UnsupportedOperationException("Not supported yet.");
                                    }
                                    break;
                                case "xtraIds":
                                    assert (concept != null);
                                    assert (kpUuid != null);
                                    assert (kpUuid.getLeastSignificantBits() == concept.conUuidLsb);
                                    assert (kpUuid.getMostSignificantBits() == concept.conUuidMsb);
                                    // KP UUID matches primordial UUID
                                    Longform_IdRecord kpId = new Longform_IdRecord();
                                    kpId.primaryUuidLsb = concept.conUuidLsb;
                                    kpId.primaryUuidMsb = concept.conUuidMsb;
                                    kpId.authorUuid = concept.authorUuid;
                                    kpId.denotationUuid = kpUuid;
                                    kpId.status = uuidActive;
                                    kpId.srcSystemUuid = uuidSourceKpUuid;
                                    kpId.revTime = concept.revTime;
                                    if (concept.addedIds == null) {
                                        concept.addedIds = new ArrayList<>();
                                    }
                                    concept.addedIds.add(kpId);
                                    nextState = States.CONCEPT_EXTRA_IDS;
                                    break;
                            }
                            break;

                        case CONCEPT_EXTRA_IDS:
                            switch (line[0]) {
                                // other Identifer* not implemented
                                case "IdentifierVersionLong":
                                    assert (concept != null);
                                    id = new Longform_IdRecord();
                                    id.primaryUuidLsb = concept.conUuidLsb;
                                    id.primaryUuidMsb = concept.conUuidMsb;
                                    id.authorUuid = concept.authorUuid;
                                    if (concept.addedIds == null) {
                                        concept.addedIds = new ArrayList<>();
                                    }
                                    concept.addedIds.add(id);
                                    break;
                                case "denotation":
                                    assert (id != null);
                                    assert (concept != null);
                                    id.denotationLong = Long.parseLong(line[1]);
                                    concept.conSnoId = id.denotationLong;
                                    break;
                                case "authority":
                                    assert (id != null);
                                    id.srcSystemUuid = processIdSystem(line[1]);
                                    break;
                                case "tm":
                                    assert (id != null);
                                    id.revTime = processTime(line[1]);
                                    break;
                                case "status":
                                    assert (id != null);
                                    id.status = processStatus(line[1]);
                                    break;
                                case "annotations":
                                    nextState = States.CONCEPT_ANNOTATION;
                                    break;
                            }
                            break;

                        case CONCEPT_ANNOTATION:
                            switch (line[0]) {
                                case "CidMember":
                                case "BooleanMember":
                                case "StrMember":
                                    assert (concept != null);
                                    assert (concept.annotations != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.CONCEPT;
                                    concept.annotations.add(member);
                                    break;
                                case "IntMember": // type: INT
                                    assert (concept != null);
                                    assert (concept.annotations != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.CONCEPT;
                                    member.valueInt = Integer.parseInt(line[1]);
                                    concept.annotations.add(member);
                                    break;

                                case "c1Nid": // type: CID
                                    assert (member != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    member.valueConUuidLsb = uuid.getLeastSignificantBits();
                                    member.valueConUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "booleanValue": // type: BOOLEAN
                                    assert (member != null);
                                    member.valueBoolean = true;
                                    if (line[1].compareTo("false") == 0) {
                                        member.valueBoolean = false;
                                    }
                                    break;
                                case "stringValue": // type: STR
                                    assert (member != null);
                                    member.valueString = line[1];
                                    break;

                                case "refset":
                                    assert (member != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    member.refsetUuidLsb = uuid.getLeastSignificantBits();
                                    member.refsetUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "type":
                                    assert (member != null);
                                    switch (line[1]) {
                                        case "BOOLEAN":
                                            member.valueType = ValueType.BOOLEAN;
                                            break;
                                        case "CID":
                                            member.valueType = ValueType.CONCEPT;
                                            break;
                                        case "INT":
                                            member.valueType = ValueType.INTEGER;
                                            break;
                                        case "STR":
                                            member.valueType = ValueType.STRING;
                                            break;
                                    }
                                    break;
                                case "rcNid":
                                    assert (member != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    member.referencedComponentUuidLsb = uuid.getLeastSignificantBits();
                                    member.referencedComponentUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "pUuid":
                                    assert (member != null);
                                    uuid = UUID.fromString(line[1]);
                                    member.refsetMemberUuidLsb = uuid.getLeastSignificantBits();
                                    member.refsetMemberUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "status":
                                    assert (member != null);
                                    member.status = processStatus(line[1]);
                                    break;
                                case "author":
                                    assert (member != null);
                                    member.authorUuid = authorUuidMap.get(line[1]);
                                    break;
                                case "tm":
                                    assert (member != null);
                                    member.revTime = processTime(line[1]);
                                    break;
                                case "extraVersions":
                                    if (line[1].compareToIgnoreCase("null") != 0) {
                                        throw new UnsupportedOperationException("Not supported yet.");
                                    }
                                    break;
                                case "descriptions":
                                    nextState = States.DESCRIPTION;
                                    break;
                            }
                            break;

                        case DESCRIPTION:
                            switch (line[0]) {
                                case "Description":
                                    assert (concept != null);
                                    description = new Longform_DesRecord();
                                    description.conUuidLsb = concept.conUuidLsb;
                                    description.conUuidMsb = concept.conUuidMsb;
                                    desList.add(description);
                                    primordialDescription = description;
                                    primordialDescription.annotations = new ArrayList<>();
                                    System.out.println("::::CREATE PRIMORDIAL DESCRIPTION::::");
                                    break;
                                case "DescriptionRevision":
                                    assert (description != null);
                                    Longform_DesRecord d = new Longform_DesRecord();
                                    d.conUuidLsb = description.conUuidLsb;
                                    d.conUuidMsb = description.conUuidMsb;
                                    d.desUuidLsb = description.desUuidLsb;
                                    d.desUuidMsb = description.desUuidMsb;
                                    description = d;
                                    desList.add(description);
                                    break;
                                case "text":
                                    assert (description != null);
                                    description.termText = line[1];
                                    break;
                                case "caseSig":
                                case "initialCaseSignificant": // DescriptionRevision
                                    assert (description != null);
                                    description.capStatus = 0;
                                    if (line[1].compareToIgnoreCase("true") == 0) {
                                        description.capStatus = 1;
                                    }
                                    break;
                                case "type":
                                case "typeNid": // DescriptionRevision
                                    assert (description != null);
                                    description.descriptionType = conceptUuidMap.get(line[1]);
                                    break;
                                case "lang":
                                    assert (description != null);
                                    description.languageCode = line[1];
                                    break;
                                case "pUuid":
                                    assert (description != null);
                                    assert (primordialDescription != null);
                                    uuid = UUID.fromString(line[1]);
                                    kpUuid = uuid;
                                    description.desUuidLsb = uuid.getLeastSignificantBits();
                                    description.desUuidMsb = uuid.getMostSignificantBits();
                                    System.out.println("::::ADD PUUID::::");
                                    System.out.println("::::DESCRIPTION PUUID:::: " + new UUID(description.desUuidMsb, description.desUuidMsb));
                                    System.out.println("::::DESCRIPTION PUUID:::: " + new UUID(primordialDescription.desUuidMsb, primordialDescription.desUuidMsb));
                                    break;
                                case "status":
                                    assert (description != null);
                                    description.status = processStatus(line[1]);
                                    break;
                                case "author":
                                    assert (description != null);
                                    description.authorUuid = authorUuidMap.get(line[1]);
                                    break;
                                case "tm":
                                    assert (description != null);
                                    description.revTime = processTime(line[1]);
                                    break;
                                case "extraVersions":
                                    break;
                                case "xtraIds":
                                    assert (description != null);
                                    assert (desList.size() > 0);
                                    assert (primordialDescription != null);
                                    Longform_DesRecord desc = primordialDescription;
                                    assert (kpUuid != null);
                                    if (!(kpUuid.getLeastSignificantBits() == desc.desUuidLsb)) {
                                        System.out.println(":DEBUG:");
                                    }
                                    assert (kpUuid.getLeastSignificantBits() == desc.desUuidLsb);
                                    assert (kpUuid.getMostSignificantBits() == desc.desUuidMsb);
                                    Longform_IdRecord kpId = new Longform_IdRecord();
                                    kpId.primaryUuidLsb = desc.desUuidLsb;
                                    kpId.primaryUuidMsb = desc.desUuidMsb;
                                    kpId.authorUuid = desc.authorUuid;
                                    kpId.denotationUuid = kpUuid;
                                    kpId.status = uuidActive;
                                    kpId.srcSystemUuid = uuidSourceKpUuid;
                                    kpId.revTime = description.revTime;
                                    if (desc.addedIds == null) {
                                        desc.addedIds = new ArrayList<>();
                                    }
                                    desc.addedIds.add(kpId);
                                    nextState = States.DESCRIPTION_EXTRA_IDS;
                                    break;
                                case "annotations":
                                    nextState = States.DESCRIPTION_ANNOTATION;
                                    break;
                                case "srcRels":
                                    nextState = States.RELATIONSHIP;
                                    break;
                            }
                            break;

                        case DESCRIPTION_EXTRA_IDS:
                            switch (line[0]) {
                                // other Identifer* not implemented
                                case "IdentifierVersionLong":
                                    assert (description != null);
                                    id = new Longform_IdRecord();
                                    id.primaryUuidLsb = description.desUuidLsb;
                                    id.primaryUuidMsb = description.desUuidMsb;
                                    id.authorUuid = description.authorUuid;
                                    if (description.addedIds == null) {
                                        description.addedIds = new ArrayList<>();
                                    }
                                    description.addedIds.add(id);
                                    break;
                                case "denotation":
                                    assert (id != null);
                                    assert (description != null);
                                    id.denotationLong = Long.parseLong(line[1]);
                                    description.desSnoId = id.denotationLong;
                                    break;
                                case "authority":
                                    assert (id != null);
                                    id.srcSystemUuid = processIdSystem(line[1]);
                                    break;
                                case "tm":
                                    assert (id != null);
                                    id.revTime = processTime(line[1]);
                                    break;
                                case "status":
                                    assert (id != null);
                                    id.status = processStatus(line[1]);
                                    break;

                                case "annotations":
                                    nextState = States.DESCRIPTION_ANNOTATION;
                                    break;
                                case "xtraIds":
                                    nextState = States.DESCRIPTION_EXTRA_IDS;
                                    break;
                                case "Description":
                                    assert (concept != null);
                                    description = new Longform_DesRecord();
                                    description.conUuidLsb = concept.conUuidLsb;
                                    description.conUuidMsb = concept.conUuidMsb;
                                    desList.add(description);
                                    primordialDescription = description;
                                    primordialDescription.annotations = new ArrayList<>();
                                    System.out.println("::::CREATE PRIMORDIAL DESCRIPTION::::");
                                    nextState = States.DESCRIPTION;
                                    break;
                                case "srcRels":
                                    nextState = States.RELATIONSHIP;
                                    break;
                            }
                            break;

                        case DESCRIPTION_ANNOTATION:
                            switch (line[0]) {
                                case "CidMember":
                                case "BooleanMember":
                                case "StrMember":
                                    assert (primordialDescription != null);
                                    assert (primordialDescription.annotations != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.DESCRIPTION;
                                    primordialMember = member;
                                    // :!!!: primordialMember.revisions = new ArrayList<>();
                                    primordialDescription.annotations.add(member);
                                    break;
                                case "IntMember": // type: INT
                                    assert (primordialDescription != null);
                                    assert (primordialDescription.annotations != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.DESCRIPTION;
                                    member.valueInt = Integer.parseInt(line[1]);
                                    primordialMember = member;
                                    // :!!!: primordialMember.revisions = new ArrayList<>();
                                    primordialDescription.annotations.add(member);
                                    break;

                                case "CidRevision":
                                case "BooleanRevision":
                                case "StrRevision":
                                    assert (primordialMember != null);
                                    assert (primordialDescription != null);
                                    assert (primordialDescription.annotations != null);
                                    // :!!!: assert (primordialMember.revisions != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.DESCRIPTION;
                                    member.valueType = primordialMember.valueType;
                                    member.referencedComponentUuidLsb = primordialMember.referencedComponentUuidLsb;
                                    member.referencedComponentUuidMsb = primordialMember.referencedComponentUuidMsb;
                                    member.refsetUuidLsb = primordialMember.refsetUuidLsb;
                                    member.refsetUuidMsb = primordialMember.refsetUuidMsb;
                                    member.refsetMemberUuidLsb = primordialMember.refsetMemberUuidLsb;
                                    member.refsetMemberUuidMsb = primordialMember.refsetMemberUuidMsb;
                                    // :!!!: primordialMember.revisions.add(member);
                                    primordialDescription.annotations.add(member);
                                    break;
                                case "IntRevision": // type: INT
                                    assert (primordialMember != null);
                                    assert (primordialDescription != null);
                                    assert (primordialDescription.annotations != null);
                                    // :!!!: assert (primordialMember.revisions != null);
                                    member = new Longform_AnnotationRecord();
                                    member.componentType = ComponentType.DESCRIPTION;
                                    member.valueType = primordialMember.valueType;
                                    member.referencedComponentUuidLsb = primordialMember.referencedComponentUuidLsb;
                                    member.referencedComponentUuidMsb = primordialMember.referencedComponentUuidMsb;
                                    member.refsetUuidLsb = primordialMember.refsetUuidLsb;
                                    member.refsetUuidMsb = primordialMember.refsetUuidMsb;
                                    member.refsetMemberUuidLsb = primordialMember.refsetMemberUuidLsb;
                                    member.refsetMemberUuidMsb = primordialMember.refsetMemberUuidMsb;
                                    member.valueInt = Integer.parseInt(line[1]);
                                    // primordialMember.revisions.add(member);
                                    primordialDescription.annotations.add(member);
                                    break;

                                case "c1Nid": // type: CID
                                    assert (member != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    member.valueConUuidLsb = uuid.getLeastSignificantBits();
                                    member.valueConUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "booleanValue": // type: BOOLEAN
                                    assert (member != null);
                                    member.valueBoolean = true;
                                    if (line[1].compareTo("false") == 0) {
                                        member.valueBoolean = false;
                                    }
                                    break;
                                case "stringValue": // type: STR
                                    assert (member != null);
                                    member.valueString = line[1];
                                    break;

                                case "refset":
                                    assert (member != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    member.refsetUuidLsb = uuid.getLeastSignificantBits();
                                    member.refsetUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "type":
                                    assert (member != null);
                                    switch (line[1]) {
                                        case "BOOLEAN":
                                            member.valueType = ValueType.BOOLEAN;
                                            break;
                                        case "CID":
                                            member.valueType = ValueType.CONCEPT;
                                            break;
                                        case "INT":
                                            member.valueType = ValueType.INTEGER;
                                            break;
                                        case "STR":
                                            member.valueType = ValueType.STRING;
                                            break;
                                    }
                                    break;
                                case "rcNid":
                                    assert (member != null);
                                    assert (primordialDescription != null);
                                    member.referencedComponentUuidLsb = primordialDescription.desUuidLsb;
                                    member.referencedComponentUuidMsb = primordialDescription.desUuidMsb;
                                    break;
                                case "pUuid":
                                    assert (member != null);
                                    uuid = UUID.fromString(line[1]);
                                    member.refsetMemberUuidLsb = uuid.getLeastSignificantBits();
                                    member.refsetMemberUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "status":
                                    assert (member != null);
                                    member.status = processStatus(line[1]);
                                    break;
                                case "author":
                                    assert (member != null);
                                    member.authorUuid = authorUuidMap.get(line[1]);
                                    break;
                                case "tm":
                                    assert (member != null);
                                    member.revTime = processTime(line[1]);
                                    break;
                                case "extraVersions":
                                    break;

                                case "xtraIds":
                                    if (line[1].compareToIgnoreCase("null") != 0) {
                                        throw new UnsupportedOperationException("Not supported yet.");
                                    }
                                    break;
                                case "Description":
                                    assert (concept != null);
                                    description = new Longform_DesRecord();
                                    description.conUuidLsb = concept.conUuidLsb;
                                    description.conUuidMsb = concept.conUuidMsb;
                                    desList.add(description);
                                    primordialDescription = description;
                                    primordialDescription.annotations = new ArrayList<>();
                                    System.out.println("::::CREATE PRIMORDIAL DESCRIPTION::::");
                                    nextState = States.DESCRIPTION;
                                    break;
                                case "srcRels":
                                    nextState = States.RELATIONSHIP;
                                    break;
                            }
                            break;

                        case RELATIONSHIP:
                            switch (line[0]) {
                                case "Relationship":
                                    assert (concept != null);
                                    relationship = new Longform_RelRecord();
                                    relationship.c1UuidLsb = concept.conUuidLsb;
                                    relationship.c1UuidMsb = concept.conUuidMsb;
                                    relList.add(relationship);
                                    break;
                                case "src":
                                    assert (relationship != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    relationship.c1UuidLsb = uuid.getLeastSignificantBits();
                                    relationship.c1UuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "t":
                                    assert (relationship != null);
                                    relationship.roleTypeUuid = conceptUuidMap.get(line[1]);
                                    break;
                                case "dest":
                                    assert (relationship != null);
                                    uuid = conceptUuidMap.get(line[1]);
                                    relationship.c2UuidLsb = uuid.getLeastSignificantBits();
                                    relationship.c2UuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "c":
                                    assert (relationship != null);
                                    relationship.characteristic = conceptUuidMap.get(line[1]);
                                    break;
                                case "g":
                                    assert (relationship != null);
                                    relationship.group = Integer.parseInt(line[1]);
                                    break;
                                case "r":
                                    assert (relationship != null);
                                    relationship.refinability = conceptUuidMap.get(line[1]);
                                    break;
                                case "pUuid":
                                    assert (relationship != null);
                                    uuid = UUID.fromString(line[1]);
                                    kpUuid = uuid;
                                    relationship.relUuidLsb = uuid.getLeastSignificantBits();
                                    relationship.relUuidMsb = uuid.getMostSignificantBits();
                                    break;
                                case "status":
                                    assert (relationship != null);
                                    relationship.status = processStatus(line[1]);
                                    break;
                                case "author":
                                    assert (relationship != null);
                                    relationship.authorUuid = authorUuidMap.get(line[1]);
                                    break;
                                case "tm":
                                    assert (relationship != null);
                                    relationship.revTime = processTime(line[1]);
                                    break;
                                case "extraVersions":
                                    if (line[1].compareToIgnoreCase("null") != 0) {
                                        throw new UnsupportedOperationException("Not supported yet.");
                                    }
                                    break;
                                case "xtraIds":
                                    nextState = States.RELATIONSHIP_EXTRA_IDS;
                                    break;
                                case "refset members":
                                    nextState = States.PROTOTYPE;
                                    break;
                            }
                            break;

                        case RELATIONSHIP_EXTRA_IDS:
                            switch (line[0]) {
                                // other Identifer* not implemented
                                case "IdentifierVersionLong":
                                    assert (relationship != null);
                                    id = new Longform_IdRecord();
                                    id.primaryUuidLsb = relationship.relUuidLsb;
                                    id.primaryUuidMsb = relationship.relUuidMsb;
                                    id.authorUuid = relationship.authorUuid;
                                    if (relationship.addedIds == null) {
                                        relationship.addedIds = new ArrayList<>();
                                    }
                                    relationship.addedIds.add(id);
                                    break;
                                case "denotation":
                                    assert (id != null);
                                    assert (relationship != null);
                                    id.denotationLong = Long.parseLong(line[1]);
                                    relationship.relSnoId = id.denotationLong;
                                    break;
                                case "authority":
                                    assert (id != null);
                                    id.srcSystemUuid = processIdSystem(line[1]);
                                    break;
                                case "tm":
                                    assert (id != null);
                                    id.revTime = processTime(line[1]);
                                    break;
                                case "status":
                                    assert (id != null);
                                    id.status = processStatus(line[1]);
                                    break;
                                case "]":
                                    assert (relationship != null);
                                    assert (kpUuid != null);
                                    assert (kpUuid.getLeastSignificantBits() == relationship.relUuidLsb);
                                    assert (kpUuid.getMostSignificantBits() == relationship.relUuidMsb);
                                    Longform_IdRecord kpId = new Longform_IdRecord();
                                    kpId.primaryUuidLsb = relationship.relUuidLsb;
                                    kpId.primaryUuidMsb = relationship.relUuidMsb;
                                    kpId.authorUuid = relationship.authorUuid;
                                    kpId.denotationUuid = kpUuid;
                                    kpId.status = uuidActive;
                                    kpId.srcSystemUuid = uuidSourceKpUuid;
                                    kpId.revTime = relationship.revTime;
                                    if (relationship.addedIds == null) {
                                        relationship.addedIds = new ArrayList<>();
                                    }
                                    relationship.addedIds.add(kpId);
                                    nextState = States.PROTOTYPE;
                                    break;
                                case "refset members":
                                    nextState = States.PROTOTYPE;
                                    break;
                            }
                            break;

                        case PROTOTYPE:
                            switch (line[0]) {
                                case "defined": // true | false
                                    break;
                            }
                            break;
                    }
                }

                currentState = nextState;
            }
        }

        processTimeAdjustment(conList, desList, relList);

        EConcept ec = createEConcept(conList, desList, relList, rsByConList, rsByRsList);
        return ec;
    }

    private UUID processIdSystem(String idSystem) {
        if (idSystem.compareToIgnoreCase("SNOMED integer id") == 0) {
            // RF2 SNOMED CT integer identifier (core metadata concept)
            return UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9");
        } else {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private UUID processStatus(String status) {
        if (status.compareToIgnoreCase("retired") == 0
                || status.compareToIgnoreCase("inactive") == 0) {
            return uuidInactive;
        }
        return uuidActive;
    }

    private long processTime(String time) throws ParseException {

        if (time.contains("-")) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
            Date date = formatter.parse(time);
            return date.getTime();
        }
        return Long.parseLong(time);
    }

    private void processTimeAdjustment(ArrayList<Longform_ConRecord> conList,
            ArrayList<Longform_DesRecord> desList,
            ArrayList<Longform_RelRecord> relList)
            throws ParseException {
        // 
        String baseTimeStr = "2014-12-15-00.00.00";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss");
        Date baseDate = formatter.parse(baseTimeStr);
        Long baseTime = baseDate.getTime();

        //
        HashSet<Long> timeSet = new HashSet<>();
        for (Longform_ConRecord c : conList) {
            timeSet.add(c.revTime);
            ArrayList<Longform_IdRecord> idl = c.addedIds;
            for (Longform_IdRecord id : idl) {
                timeSet.add(id.revTime);
            }
            ArrayList<Longform_AnnotationRecord> al = c.annotations;
            for (Longform_AnnotationRecord a : al) {
                timeSet.add(a.revTime);
                ArrayList<Longform_AnnotationRecord> arl = a.revisions;
                if (arl != null) {
                    for (Longform_AnnotationRecord ar : arl) {
                        timeSet.add(ar.revTime);
                    }
                }
            }
        }
        for (Longform_DesRecord d : desList) {
            timeSet.add(d.revTime);
            ArrayList<Longform_IdRecord> idl = d.addedIds;
            for (Longform_IdRecord id : idl) {
                timeSet.add(id.revTime);
            }
            ArrayList<Longform_AnnotationRecord> al = d.annotations;
            if (al != null) {
                for (Longform_AnnotationRecord a : al) {
                    timeSet.add(a.revTime);
                    ArrayList<Longform_AnnotationRecord> arl = a.revisions;
                    if (arl != null) {
                        for (Longform_AnnotationRecord ar : arl) {
                            timeSet.add(ar.revTime);
                        }
                    }
                }
            }
        }
        for (Longform_RelRecord r : relList) {
            timeSet.add(r.revTime);
            ArrayList<Longform_IdRecord> idl = r.addedIds;
            for (Longform_IdRecord id : idl) {
                timeSet.add(id.revTime);
            }
        }

        Long[] timeArray = timeSet.toArray(new Long[timeSet.size()]);
        Arrays.sort(timeArray);

        HashMap<Long, Long> timeLookup = new HashMap<>();

        for (Long t : timeArray) {
            timeLookup.put(t, baseTime);
            baseTime += 5000; // increment by 5 minutes
        }

        for (Longform_ConRecord c : conList) {
            c.revTime = timeLookup.get(c.revTime);
            ArrayList<Longform_IdRecord> idl = c.addedIds;
            for (Longform_IdRecord id : idl) {
                id.revTime = timeLookup.get(id.revTime);
            }
            ArrayList<Longform_AnnotationRecord> al = c.annotations;
            for (Longform_AnnotationRecord a : al) {
                a.revTime = timeLookup.get(a.revTime);
                ArrayList<Longform_AnnotationRecord> arl = a.revisions;
                if (arl != null) {
                    for (Longform_AnnotationRecord ar : arl) {
                        ar.revTime = timeLookup.get(ar.revTime);
                    }
                }
            }
        }
        for (Longform_DesRecord d : desList) {
            d.revTime = timeLookup.get(d.revTime);
            ArrayList<Longform_IdRecord> idl = d.addedIds;
            for (Longform_IdRecord id : idl) {
                id.revTime = timeLookup.get(id.revTime);
            }
            ArrayList<Longform_AnnotationRecord> al = d.annotations;
            if (al != null) {
                for (Longform_AnnotationRecord a : al) {
                    a.revTime = timeLookup.get(a.revTime);
                    ArrayList<Longform_AnnotationRecord> arl = a.revisions;
                    if (arl != null) {
                        for (Longform_AnnotationRecord ar : arl) {
                            ar.revTime = timeLookup.get(ar.revTime);
                        }
                    }
                }
            }
        }
        for (Longform_RelRecord r : relList) {
            r.revTime = timeLookup.get(r.revTime);
            ArrayList<Longform_IdRecord> idl = r.addedIds;
            for (Longform_IdRecord id : idl) {
                id.revTime = timeLookup.get(id.revTime);
            }
        }
    }

    private void setupAuthorUuidMap() {
        authorUuidMap = new HashMap<>();
        authorUuidMap.put(ROLE, uuidModule);
        // "Mary Gerard" KP Users fc1ade4c-9646-5291-8006-1a174207cd39
        // "Mary Gerard" IHTSDO user 471db693-66d0-38b2-bf95-72f5e680d478
        authorUuidMap.put("Mary Gerard", UUID.fromString("fc1ade4c-9646-5291-8006-1a174207cd39"));

        // "Peter Hender" KP Users (with KP UUID) 5aaf8f31-65e4-5eac-85db-bf83333547e0
        // "Peter Hendler" KP Users (without KP UUID) 00d80dd6-219e-5f0d-a243-932f04c008fc
        authorUuidMap.put("Peter Hender", UUID.fromString("5aaf8f31-65e4-5eac-85db-bf83333547e0"));

        // "Sarah Albo" KP Users 3b463a64-ccf0-5957-a49d-2f1c48e85995
        authorUuidMap.put("Sarah Albo", UUID.fromString("3b463a64-ccf0-5957-a49d-2f1c48e85995"));

    }

    private void setupConceptUuidMap() {
        conceptUuidMap = new HashMap<>();

        // current retired  Active Inactive
        // Description type:
        conceptUuidMap.put("fully specified name", UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf"));
        conceptUuidMap.put("synonym", UUID.fromString("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
        conceptUuidMap.put("kp description", UUID.fromString("ecfd4324-04de-5503-8274-3116f8f07217"));
        conceptUuidMap.put("patient friendly display name", UUID.fromString("084283a0-b7ca-5626-b604-6dd69fb5ff2d"));

        // Refset Member c1Nid:
        // "preferred acceptability" 15877c09-60d7-3464-bed8-635a98a7e5b2
        // maps to "Preferred" 266f1bc3-3361-39f3-bffe-69db9daea56e
        conceptUuidMap.put("preferred acceptability", UUID.fromString("266f1bc3-3361-39f3-bffe-69db9daea56e"));
        conceptUuidMap.put("unreviewed", UUID.fromString("854552b2-74b7-3f68-81fc-3211950d2ba9"));
        conceptUuidMap.put("Open fracture of multiple ribs, unspecified", UUID.fromString("90755f09-d05b-5a8b-874c-23d2f662fcab"));
        conceptUuidMap.put("Open fracture of greater tuberosity of humerus", UUID.fromString("7bb18dd9-26e1-5ffd-a68d-2863466569dc"));

        // ICD9 Concept ... not to be confused with SNOMED Concept 0b7bf4fd-f4bb-3108-8d44-ce1e7e8cad7a with the same descriptoin
        conceptUuidMap.put("Open fracture of lateral condyle of humerus", UUID.fromString("85c6261a-9c96-560b-816c-ccd225a37a96"));
        //
        conceptUuidMap.put("Open fracture of supracondylar humerus", UUID.fromString("f945e5f9-6759-5d00-9896-5bb8aaa125e1"));
        conceptUuidMap.put("DSPL FX LAT CONDYLE LT HUMERUS INIT ENC OPEN FX", UUID.fromString("15993294-eea7-5140-8531-6b63aca60d84"));
        conceptUuidMap.put("DSPL FX AVUL LAT EPICONDYLE LT HUM INIT ENC OPN", UUID.fromString("1d2b865d-547b-5ee0-aa63-178d43019cec"));
        conceptUuidMap.put("DSPL SMPL SC FX W/O IC FX LT HUM INIT ENC OP FX", UUID.fromString("dcaa1ac6-cc9d-550b-b396-f2b677722dff"));
        conceptUuidMap.put("DSPLCD FX GT TUBEROS LT HUM INIT ENC OPEN FX", UUID.fromString("b30f780a-ac32-5266-acaa-d1de6156e476"));
        conceptUuidMap.put("MX FX RIBS BILATERAL INIT ENC OPEN FRACTURE", UUID.fromString("a20ccf2e-c9d1-58a3-95ca-697109321077"));

        // refset:
        conceptUuidMap.put("ICD9 CM Code Extension 1", UUID.fromString("a4a58113-95df-5556-8520-7504bb088dc8"));
        conceptUuidMap.put("ICD10 CM Code Extension 1", UUID.fromString("dd38b9ca-9c9e-556d-910c-5f82e29311d8"));

        // US English Dialect Subset maps to
        // United States of America English language reference set (foundation metadata concept)
        conceptUuidMap.put("US English Dialect Subset", UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad"));

        // GB English Dialect Subset maps to 
        // Great Britain English language reference set (foundation metadata concept)
        conceptUuidMap.put("GB English Dialect Subset", UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30"));

        conceptUuidMap.put("EDG Clinical Description National", UUID.fromString("0b901015-b091-5556-9e77-e64fcbd63e49"));
        conceptUuidMap.put("EDG Clinical Diagnosis Id", UUID.fromString("0c1cf67d-7a74-54ee-b1f6-87770f604753"));
        conceptUuidMap.put("EDG Clinical Community Id", UUID.fromString("59c63e18-67fe-5725-8c61-40168e3166a2"));
        conceptUuidMap.put("EDG Clinical External Id", UUID.fromString("df73866c-df5f-5504-b9e9-600eb959503e"));
        conceptUuidMap.put("Submitted To NLM", UUID.fromString("846ef00d-6629-5270-bfe0-75338d125831"));

        // Used 'KP Status Types' not to be consufed with e37ab984-d123-5e03-ab84-8cc12110f584
        conceptUuidMap.put("SNOMED Review Status", UUID.fromString("c505d6d8-81ab-55a5-8afa-4c7bdc279cd6"));

        // src: from file
        conceptUuidMap.put("Open supracondylar fracture of left humerus (disorder)", UUID.fromString("2208eb52-6552-5440-974b-becaeee08d74"));
        conceptUuidMap.put("Open bilateral fracture of multiple ribs (disorder)", UUID.fromString("5480eac0-60e9-50e6-b4ea-5351a297d2be"));
        conceptUuidMap.put("Open fracture of lateral condyle of left humerus (disorder)", UUID.fromString("cd98a5ea-a17c-5d0a-b6db-b9c908c3c95b"));
        conceptUuidMap.put("Open fracture of lateral epicondyle of left humerus (disorder)", UUID.fromString("f173c3c4-53c5-597c-adec-cc618cba4df4"));

        conceptUuidMap.put("Open fracture of greater tuberosity of left humerus (disorder)", UUID.fromString("f6eed280-5059-5866-8626-7ee7ebcd5e19"));

        // t:
        conceptUuidMap.put("Is a (attribute)", UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));

        // dest:
        conceptUuidMap.put("Open supracondylar fracture of humerus (disorder)", UUID.fromString("0b05db43-b60b-3d9e-beca-63022c215886"));
        conceptUuidMap.put("Open fracture of rib (disorder)", UUID.fromString("1b2c61ac-38e6-3b78-8854-be85adc8a3b4"));
        conceptUuidMap.put("Open fracture of lateral condyle of humerus (disorder)", UUID.fromString("0b7bf4fd-f4bb-3108-8d44-ce1e7e8cad7a"));
        conceptUuidMap.put("Open fracture distal humerus, lateral epicondyle (disorder)", UUID.fromString("77f7e42e-7424-332c-a81f-993b64ecbe5b"));
        conceptUuidMap.put("Open fracture proximal humerus, greater tuberosity (disorder)", UUID.fromString("893a6776-82ee-3de2-a191-c6d59b130403"));
        //c:
        conceptUuidMap.put("stated", UUID.fromString("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"));

        // r:
        conceptUuidMap.put("not refinable", UUID.fromString("ce30636d-bfc9-3a70-9678-abc6b542ab4c"));

        // :!!!: src: is current concept
        // :!!!: rcNid: is current component
    }

    private EConcept createEConcept(ArrayList<Longform_ConRecord> conList,
            ArrayList<Longform_DesRecord> desList,
            ArrayList<Longform_RelRecord> relList,
            //ArrayList<Longform_RelDestRecord> relDestList, 
            ArrayList<Longform_AnnotationRecord> rsByConList,
            ArrayList<Longform_AnnotationRecord> rsByRsList)
            throws MojoFailureException, IOException {
        if (conList.size() < 1) {
            throw new MojoFailureException("createEConcept(), empty conList");
        }

        Collections.sort(conList);
        Longform_ConRecord cRec0 = conList.get(0);
        UUID theConUUID = new UUID(cRec0.conUuidMsb, cRec0.conUuidLsb);

//        if (theConUUID.compareTo(debugUuid01) == 0) {
//            System.out.println(":!!!:DEBUG:");
//        }
        EConcept ec = new EConcept();
        ec.setPrimordialUuid(theConUUID);

        if (cRec0.status == null) {
            ec.setConceptAttributes(null);
        } else {
            // ADD CONCEPT ATTRIBUTES
            EConceptAttributes ca = new EConceptAttributes();
            ca.primordialUuid = theConUUID;
            ca.setDefined(cRec0.isPrimitive == 0);
            ca.setAuthorUuid(cRec0.authorUuid);
            ca.setModuleUuid(uuidModule);

            ArrayList<TkIdentifier> tmpAdditionalIds = new ArrayList<>();

            if (cRec0.addedIds != null) {
                for (Longform_IdRecord eId : cRec0.addedIds) {
                    tmpAdditionalIds.add(createEIdentifier(eId));
                }
            }

            if (tmpAdditionalIds.size() > 0) {
                ca.additionalIds = tmpAdditionalIds;
            } else {
                ca.additionalIds = null;
            }

            if (cRec0.annotations.size() > 0) {
                ca.annotations = processAnnotations(cRec0.annotations);
            }
            // List<TkRefexAbstractMember<?>> a = ca.annotations;  <-- :!!!: add annotations

            ca.setStatusUuid(cRec0.status);
            ca.setPathUuid(uuidPath);
            ca.setTime(cRec0.revTime); // long

            int max = conList.size();
            List<TkConceptAttributesRevision> caRevisions = new ArrayList<>();
            for (int i = 1; i < max; i++) {
                EConceptAttributesRevision rev = new EConceptAttributesRevision();
                Longform_ConRecord cRec = conList.get(i);
                rev.setDefined(cRec.isPrimitive == 0);
                rev.setStatusUuid(cRec.status);
                rev.setPathUuid(uuidPath);
                rev.setTime(cRec.revTime);
                rev.authorUuid = cRec.authorUuid;
                rev.setModuleUuid(uuidModule);
                caRevisions.add(rev);
            }

            if (caRevisions.size() > 0) {
                ca.revisions = caRevisions;
            } else {
                ca.revisions = null;
            }
            ec.setConceptAttributes(ca);
        }

        // ADD DESCRIPTIONS
        if (desList != null) {
            Collections.sort(desList);
            List<TkDescription> eDesList = new ArrayList<>();
            // long theDesId = Long.MIN_VALUE;
            long theDesMsb = Long.MIN_VALUE;
            long theDesLsb = Long.MIN_VALUE;
            EDescription des = null;
            List<TkDescriptionRevision> revisions = new ArrayList<>();
            for (Longform_DesRecord dRec : desList) {
                if (dRec.desUuidMsb != theDesMsb || dRec.desUuidLsb != theDesLsb) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (des != null) {
                        if (revisions.size() > 0) {
                            des.revisions = revisions;
                            revisions = new ArrayList<>();
                        }
                        eDesList.add(des);
                    }

                    // CREATE NEW DESCRIPTION
                    des = new EDescription();
                    ArrayList<TkIdentifier> tmpDesAdditionalIds = new ArrayList<>();
                    if (dRec.addedIds != null) {
                        for (Longform_IdRecord eId : dRec.addedIds) {
                            tmpDesAdditionalIds.add(createEIdentifier(eId));
                        }
                    }
                    if (tmpDesAdditionalIds.size() > 0) {
                        des.additionalIds = tmpDesAdditionalIds;
                    } else {
                        des.additionalIds = null;
                    }

                    if (dRec.annotations.size() > 0) {
                        des.annotations = processAnnotations(dRec.annotations);
                    }

                    theDesMsb = dRec.desUuidMsb;
                    theDesLsb = dRec.desUuidLsb;
                    des.setPrimordialComponentUuid(new UUID(theDesMsb, theDesLsb));
                    des.setConceptUuid(theConUUID);
                    des.setText(dRec.termText);
                    des.setInitialCaseSignificant(dRec.capStatus == 1);
                    des.setLang(dRec.languageCode);
                    des.setTypeUuid(dRec.descriptionType);
                    des.setStatusUuid(dRec.status);
                    des.setPathUuid(uuidPath);
                    des.setTime(dRec.revTime);
                    des.authorUuid = dRec.authorUuid;
                    des.setModuleUuid(uuidModule);
                    des.revisions = null;
                } else {
                    EDescriptionRevision edv = new EDescriptionRevision();
                    edv.setText(dRec.termText);
                    edv.setTypeUuid(dRec.descriptionType);
                    edv.setInitialCaseSignificant(dRec.capStatus == 1);
                    edv.setLang(dRec.languageCode);
                    edv.setStatusUuid(dRec.status);
                    edv.setPathUuid(uuidPath);
                    edv.setTime(dRec.revTime);
                    edv.authorUuid = dRec.authorUuid;
                    edv.setModuleUuid(uuidModule);
                    revisions.add(edv);
                }
            }
            if (des != null && revisions.size() > 0) {
                des.revisions = revisions;
            }
            eDesList.add(des);
            ec.setDescriptions(eDesList);
        }

        // ADD RELATIONSHIPS
        if (relList != null) {
            Collections.sort(relList);
            List<TkRelationship> eRelList = new ArrayList<>();
            long theRelMsb = Long.MIN_VALUE;
            long theRelLsb = Long.MIN_VALUE;
            ERelationship rel = null;
            List<TkRelationshipRevision> revisions = new ArrayList<>();
            for (Longform_RelRecord rRec : relList) {
                if (rRec.relUuidMsb != theRelMsb || rRec.relUuidLsb != theRelLsb) {
                    // CLOSE OUT OLD RELATIONSHIP
                    if (rel != null) {
                        if (revisions.size() > 0) {
                            rel.revisions = revisions;
                            revisions = new ArrayList<>();
                        }
                        eRelList.add(rel);
                    }

                    // CREATE NEW RELATIONSHIP
                    rel = new ERelationship();

                    ArrayList<TkIdentifier> tmpRelAdditionalIds = new ArrayList<>(1);
                    if (rRec.addedIds != null) {
                        for (Longform_IdRecord eId : rRec.addedIds) {
                            tmpRelAdditionalIds.add(createEIdentifier(eId));
                        }
                    }
                    if (tmpRelAdditionalIds.size() > 0) {
                        rel.additionalIds = tmpRelAdditionalIds;
                    } else {
                        rel.additionalIds = null;
                    }

                    theRelMsb = rRec.relUuidMsb;
                    theRelLsb = rRec.relUuidLsb;
                    rel.setPrimordialComponentUuid(new UUID(theRelMsb, theRelLsb));
                    rel.setC1Uuid(theConUUID);
                    rel.setC2Uuid(new UUID(rRec.c2UuidMsb, rRec.c2UuidLsb));
                    rel.setTypeUuid(rRec.roleTypeUuid);
                    rel.setRelGroup(rRec.group);
                    rel.setCharacteristicUuid(rRec.characteristic);
                    rel.setRefinabilityUuid(rRec.refinability);
                    rel.setStatusUuid(rRec.status);
                    rel.setPathUuid(uuidPath);
                    rel.setTime(rRec.revTime);
                    rel.setAuthorUuid(rRec.authorUuid);
                    rel.setModuleUuid(uuidModule);
                    rel.revisions = null;
                } else {
                    ERelationshipRevision erv = new ERelationshipRevision();
                    erv.setTypeUuid(rRec.roleTypeUuid);
                    erv.setRelGroup(rRec.group);
                    erv.setCharacteristicUuid(rRec.characteristic);
                    erv.setRefinabilityUuid(rRec.refinability);
                    erv.setStatusUuid(rRec.status);
                    erv.setPathUuid(uuidPath);
                    erv.setTime(rRec.revTime);
                    erv.setAuthorUuid(rRec.authorUuid);
                    erv.setModuleUuid(uuidModule);
                    revisions.add(erv);
                }
            }
            if (rel != null && revisions.size() > 0) {
                rel.revisions = revisions;
            }
            eRelList.add(rel);
            ec.setRelationships(eRelList);
        }

        // ADD REFSET INDEX
        if (rsByConList != null && rsByConList.size() > 0) {
            List<UUID> listRefsetUuidMemberUuidForCon = new ArrayList<>();
            List<UUID> listRefsetUuidMemberUuidForDes = new ArrayList<>();
            List<UUID> listRefsetUuidMemberUuidForImage = new ArrayList<>();
            List<UUID> listRefsetUuidMemberUuidForRefsetMember = new ArrayList<>();
            List<UUID> listRefsetUuidMemberUuidForRel = new ArrayList<>();

            Collections.sort(rsByConList);
            int length = rsByConList.size();
            for (int rIdx = 0; rIdx < length; rIdx++) {
                Longform_AnnotationRecord r = rsByConList.get(rIdx);
                if (rIdx < length - 1) {
                    Longform_AnnotationRecord rNext = rsByConList.get(rIdx + 1);
                    if (r.refsetUuidMsb == rNext.refsetUuidMsb
                            && r.refsetUuidLsb == rNext.refsetUuidLsb
                            && r.refsetMemberUuidMsb == rNext.refsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == rNext.refsetMemberUuidLsb) {
                        continue;
                    }
                }

                if (r.componentType == ComponentType.CONCEPT) {
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForCon.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == ComponentType.DESCRIPTION) {
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForDes.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == ComponentType.IMAGE) {
                    listRefsetUuidMemberUuidForImage.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForImage.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == ComponentType.MEMBER) {
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.refsetUuidMsb,
                            r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRefsetMember.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else if (r.componentType == ComponentType.RELATIONSHIP) {
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    listRefsetUuidMemberUuidForRel.add(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                } else {
                    throw new UnsupportedOperationException("Cannot handle case");
                }
            }

        }

        // ADD REFSET MEMBER VALUES
        if (rsByRsList != null && rsByRsList.size() > 0) {
            List<TkRefexAbstractMember<?>> listErm = new ArrayList<>();
            Collections.sort(rsByRsList);

            int rsmMax = rsByRsList.size(); // NUMBER OF REFSET MEMBERS
            int rsmIdx = 0;
            long lastRefsetMemberUuidMsb;
            long lastRefsetMemberUuidLsb;
            Longform_AnnotationRecord r = null;
            boolean hasMembersToProcess = false;
            if (rsmIdx < rsmMax) {
                r = rsByRsList.get(rsmIdx++);
                hasMembersToProcess = true;
            }
            while (hasMembersToProcess) {

                if (r != null && r.valueType.compareTo(ValueType.BOOLEAN) == 0) {
                    ERefsetBooleanMember tmp = new ERefsetBooleanMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(r.status);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(uuidPath);
                    tmp.setBoolean1(r.valueBoolean);
                    tmp.setAuthorUuid(r.authorUuid);
                    tmp.setModuleUuid(uuidModule);

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefexBooleanRevision> revisionList = new ArrayList<>();
                            ERefsetBooleanRevision revision = new ERefsetBooleanRevision();
                            revision.setBoolean1(r.valueBoolean);
                            revision.setStatusUuid(r.status);
                            revision.setPathUuid(uuidPath);
                            revision.setTime(r.revTime);
                            revision.setAuthorUuid(r.authorUuid);
                            revision.setModuleUuid(uuidModule);
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetBooleanRevision();
                                        revision.setBoolean1(r.valueBoolean);
                                        revision.setStatusUuid(r.status);
                                        revision.setPathUuid(uuidPath);
                                        revision.setTime(r.revTime);
                                        revision.setAuthorUuid(r.authorUuid);
                                        revision.setModuleUuid(uuidModule);
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r != null && r.valueType.compareTo(ValueType.CONCEPT) == 0) {
                    ERefsetCidMember tmp = new ERefsetCidMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(r.status);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(uuidPath);
                    tmp.setUuid1(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                    tmp.setAuthorUuid(r.authorUuid);
                    tmp.setModuleUuid(uuidModule);

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefexUuidRevision> revisionList = new ArrayList<>();
                            ERefsetCidRevision revision = new ERefsetCidRevision();
                            revision.setUuid1(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                            revision.setStatusUuid(r.status);
                            revision.setPathUuid(uuidPath);
                            revision.setTime(r.revTime);
                            revision.setAuthorUuid(r.authorUuid);
                            revision.setModuleUuid(uuidModule);
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetCidRevision();
                                        revision.setUuid1(new UUID(r.valueConUuidMsb,
                                                r.valueConUuidLsb));
                                        revision.setStatusUuid(r.status);
                                        revision.setPathUuid(uuidPath);
                                        revision.setTime(r.revTime);
                                        revision.setAuthorUuid(r.authorUuid);
                                        revision.setModuleUuid(uuidModule);
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r != null && r.valueType.compareTo(ValueType.INTEGER) == 0) {
                    ERefsetIntMember tmp = new ERefsetIntMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(r.status);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(uuidPath);
                    tmp.setInt1(r.valueInt);
                    tmp.setAuthorUuid(r.authorUuid);
                    tmp.setModuleUuid(uuidModule);

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefexIntRevision> revisionList = new ArrayList<>();
                            ERefsetIntRevision revision = new ERefsetIntRevision();
                            revision.setInt1(r.valueInt);
                            revision.setStatusUuid(r.status);
                            revision.setPathUuid(uuidPath);
                            revision.setTime(r.revTime);
                            revision.setAuthorUuid(r.authorUuid);
                            revision.setModuleUuid(uuidModule);
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetIntRevision();
                                        revision.setInt1(r.valueInt);
                                        revision.setStatusUuid(r.status);
                                        revision.setPathUuid(uuidPath);
                                        revision.setTime(r.revTime);
                                        revision.setAuthorUuid(r.authorUuid);
                                        revision.setModuleUuid(uuidModule);
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else if (r != null && r.valueType.compareTo(ValueType.STRING) == 0) {
                    // :DEBUG:
                    //                    UUID debugUuid = new UUID(r.refsetMemberUuidMsb, r.refsetMemberUuidLsb);
                    //                    if (debugUuid.compareTo(debugUuid02) == 0) 
                    //                        System.out.println(":DEBUG:");

                    ERefsetStrMember tmp = new ERefsetStrMember();
                    tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                    tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                            r.refsetMemberUuidLsb));
                    tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                            r.referencedComponentUuidLsb));
                    tmp.setStatusUuid(r.status);
                    tmp.setTime(r.revTime);
                    tmp.setPathUuid(uuidPath);
                    tmp.setString1(r.valueString);
                    tmp.setAuthorUuid(r.authorUuid);
                    tmp.setModuleUuid(uuidModule);

                    if (rsmIdx < rsmMax) { // CHECK REVISIONS
                        lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                        lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                        r = rsByRsList.get(rsmIdx++);
                        if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                            // FIRST REVISION
                            List<TkRefsetStrRevision> revisionList = new ArrayList<>();
                            ERefsetStrRevision revision = new ERefsetStrRevision();
                            revision.setString1(r.valueString);
                            revision.setStatusUuid(r.status);
                            revision.setPathUuid(uuidPath);
                            revision.setTime(r.revTime);
                            revision.setAuthorUuid(r.authorUuid);
                            revision.setModuleUuid(uuidModule);
                            revisionList.add(revision);

                            boolean checkForMoreVersions = true;
                            do {
                                // SET UP NEXT MEMBER
                                if (rsmIdx < rsmMax) {
                                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                    r = rsByRsList.get(rsmIdx++);
                                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                        revision = new ERefsetStrRevision();
                                        revision.setString1(r.valueString);
                                        revision.setStatusUuid(r.status);
                                        revision.setPathUuid(uuidPath);
                                        revision.setTime(r.revTime);
                                        revision.setAuthorUuid(r.authorUuid);
                                        revision.setModuleUuid(uuidModule);
                                        revisionList.add(revision);
                                    } else {
                                        checkForMoreVersions = false;
                                    }
                                } else {
                                    checkForMoreVersions = false;
                                    hasMembersToProcess = false;
                                }

                            } while (checkForMoreVersions);

                            tmp.setRevisions(revisionList); // ADD REVISIONS
                        }
                    } else {
                        hasMembersToProcess = false;
                    }

                    // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                    listErm.add(tmp);
                } else {
                    throw new UnsupportedOperationException("Cannot handle case");
                }

            }

            ec.setRefsetMembers(listErm);
            //            if (conceptsToWatchMap.containsKey(ec.primordialUuid)) {
            //                getLog().info("Found watch concept after adding refset members: "
            //                        + ec);
            //            }
        }
        return ec;
    }

    List<TkRefexAbstractMember<?>> processAnnotations(ArrayList<Longform_AnnotationRecord> anotationList) {
        List<TkRefexAbstractMember<?>> listErm = new ArrayList<>();
        Collections.sort(anotationList);

        int rsmMax = anotationList.size(); // NUMBER OF REFSET MEMBERS
        int rsmIdx = 0;
        long lastRefsetMemberUuidMsb;
        long lastRefsetMemberUuidLsb;
        Longform_AnnotationRecord r = null;
        boolean hasMembersToProcess = false;
        if (rsmIdx < rsmMax) {
            r = anotationList.get(rsmIdx++);
            hasMembersToProcess = true;
        }
        while (hasMembersToProcess) {

            if (r != null && r.valueType.compareTo(ValueType.BOOLEAN) == 0) {
                ERefsetBooleanMember tmp = new ERefsetBooleanMember();
                tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                        r.refsetMemberUuidLsb));
                tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                        r.referencedComponentUuidLsb));
                tmp.setStatusUuid(r.status);
                tmp.setTime(r.revTime);
                tmp.setPathUuid(uuidPath);
                tmp.setBoolean1(r.valueBoolean);
                tmp.setAuthorUuid(r.authorUuid);
                tmp.setModuleUuid(uuidModule);

                if (rsmIdx < rsmMax) { // CHECK REVISIONS
                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                    r = anotationList.get(rsmIdx++);
                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                        // FIRST REVISION
                        List<TkRefexBooleanRevision> revisionList = new ArrayList<>();
                        ERefsetBooleanRevision revision = new ERefsetBooleanRevision();
                        revision.setBoolean1(r.valueBoolean);
                        revision.setStatusUuid(r.status);
                        revision.setPathUuid(uuidPath);
                        revision.setTime(r.revTime);
                        revision.setAuthorUuid(r.authorUuid);
                        revision.setModuleUuid(uuidModule);
                        revisionList.add(revision);

                        boolean checkForMoreVersions = true;
                        do {
                            // SET UP NEXT MEMBER
                            if (rsmIdx < rsmMax) {
                                lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                r = anotationList.get(rsmIdx++);
                                if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                        && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                    revision = new ERefsetBooleanRevision();
                                    revision.setBoolean1(r.valueBoolean);
                                    revision.setStatusUuid(r.status);
                                    revision.setPathUuid(uuidPath);
                                    revision.setTime(r.revTime);
                                    revision.setAuthorUuid(r.authorUuid);
                                    revision.setModuleUuid(uuidModule);
                                    revisionList.add(revision);
                                } else {
                                    checkForMoreVersions = false;
                                }
                            } else {
                                checkForMoreVersions = false;
                                hasMembersToProcess = false;
                            }

                        } while (checkForMoreVersions);

                        tmp.setRevisions(revisionList); // ADD REVISIONS
                    }
                } else {
                    hasMembersToProcess = false;
                }

                // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                listErm.add(tmp);
            } else if (r != null && r.valueType.compareTo(ValueType.CONCEPT) == 0) {
                ERefsetCidMember tmp = new ERefsetCidMember();
                tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                        r.refsetMemberUuidLsb));
                tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                        r.referencedComponentUuidLsb));
                tmp.setStatusUuid(r.status);
                tmp.setTime(r.revTime);
                tmp.setPathUuid(uuidPath);
                tmp.setUuid1(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                tmp.setAuthorUuid(r.authorUuid);
                tmp.setModuleUuid(uuidModule);

                if (rsmIdx < rsmMax) { // CHECK REVISIONS
                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                    r = anotationList.get(rsmIdx++);
                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                        // FIRST REVISION
                        List<TkRefexUuidRevision> revisionList = new ArrayList<>();
                        ERefsetCidRevision revision = new ERefsetCidRevision();
                        revision.setUuid1(new UUID(r.valueConUuidMsb, r.valueConUuidLsb));
                        revision.setStatusUuid(r.status);
                        revision.setPathUuid(uuidPath);
                        revision.setTime(r.revTime);
                        revision.setAuthorUuid(r.authorUuid);
                        revision.setModuleUuid(uuidModule);
                        revisionList.add(revision);

                        boolean checkForMoreVersions = true;
                        do {
                            // SET UP NEXT MEMBER
                            if (rsmIdx < rsmMax) {
                                lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                r = anotationList.get(rsmIdx++);
                                if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                        && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                    revision = new ERefsetCidRevision();
                                    revision.setUuid1(new UUID(r.valueConUuidMsb,
                                            r.valueConUuidLsb));
                                    revision.setStatusUuid(r.status);
                                    revision.setPathUuid(uuidPath);
                                    revision.setTime(r.revTime);
                                    revision.setAuthorUuid(r.authorUuid);
                                    revision.setModuleUuid(uuidModule);
                                    revisionList.add(revision);
                                } else {
                                    checkForMoreVersions = false;
                                }
                            } else {
                                checkForMoreVersions = false;
                                hasMembersToProcess = false;
                            }

                        } while (checkForMoreVersions);

                        tmp.setRevisions(revisionList); // ADD REVISIONS
                    }
                } else {
                    hasMembersToProcess = false;
                }

                // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                listErm.add(tmp);
            } else if (r != null && r.valueType.compareTo(ValueType.INTEGER) == 0) {
                ERefsetIntMember tmp = new ERefsetIntMember();
                tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                        r.refsetMemberUuidLsb));
                tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                        r.referencedComponentUuidLsb));
                tmp.setStatusUuid(r.status);
                tmp.setTime(r.revTime);
                tmp.setPathUuid(uuidPath);
                tmp.setInt1(r.valueInt);
                tmp.setAuthorUuid(r.authorUuid);
                tmp.setModuleUuid(uuidModule);

                if (rsmIdx < rsmMax) { // CHECK REVISIONS
                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                    r = anotationList.get(rsmIdx++);
                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                        // FIRST REVISION
                        List<TkRefexIntRevision> revisionList = new ArrayList<>();
                        ERefsetIntRevision revision = new ERefsetIntRevision();
                        revision.setInt1(r.valueInt);
                        revision.setStatusUuid(r.status);
                        revision.setPathUuid(uuidPath);
                        revision.setTime(r.revTime);
                        revision.setAuthorUuid(r.authorUuid);
                        revision.setModuleUuid(uuidModule);
                        revisionList.add(revision);

                        boolean checkForMoreVersions = true;
                        do {
                            // SET UP NEXT MEMBER
                            if (rsmIdx < rsmMax) {
                                lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                r = anotationList.get(rsmIdx++);
                                if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                        && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                    revision = new ERefsetIntRevision();
                                    revision.setInt1(r.valueInt);
                                    revision.setStatusUuid(r.status);
                                    revision.setPathUuid(uuidPath);
                                    revision.setTime(r.revTime);
                                    revision.setAuthorUuid(r.authorUuid);
                                    revision.setModuleUuid(uuidModule);
                                    revisionList.add(revision);
                                } else {
                                    checkForMoreVersions = false;
                                }
                            } else {
                                checkForMoreVersions = false;
                                hasMembersToProcess = false;
                            }

                        } while (checkForMoreVersions);

                        tmp.setRevisions(revisionList); // ADD REVISIONS
                    }
                } else {
                    hasMembersToProcess = false;
                }

                // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                listErm.add(tmp);
            } else if (r != null && r.valueType.compareTo(ValueType.STRING) == 0) {
                // :DEBUG:
                //                    UUID debugUuid = new UUID(r.refsetMemberUuidMsb, r.refsetMemberUuidLsb);
                //                    if (debugUuid.compareTo(debugUuid02) == 0) 
                //                        System.out.println(":DEBUG:");

                ERefsetStrMember tmp = new ERefsetStrMember();
                tmp.setRefsetUuid(new UUID(r.refsetUuidMsb, r.refsetUuidLsb));
                tmp.setPrimordialComponentUuid(new UUID(r.refsetMemberUuidMsb,
                        r.refsetMemberUuidLsb));
                tmp.setComponentUuid(new UUID(r.referencedComponentUuidMsb,
                        r.referencedComponentUuidLsb));
                tmp.setStatusUuid(r.status);
                tmp.setTime(r.revTime);
                tmp.setPathUuid(uuidPath);
                tmp.setString1(r.valueString);
                tmp.setAuthorUuid(r.authorUuid);
                tmp.setModuleUuid(uuidModule);

                if (rsmIdx < rsmMax) { // CHECK REVISIONS
                    lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                    lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                    r = anotationList.get(rsmIdx++);
                    if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                            && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                        // FIRST REVISION
                        List<TkRefsetStrRevision> revisionList = new ArrayList<>();
                        ERefsetStrRevision revision = new ERefsetStrRevision();
                        revision.setString1(r.valueString);
                        revision.setStatusUuid(r.status);
                        revision.setPathUuid(uuidPath);
                        revision.setTime(r.revTime);
                        revision.setAuthorUuid(r.authorUuid);
                        revision.setModuleUuid(uuidModule);
                        revisionList.add(revision);

                        boolean checkForMoreVersions = true;
                        do {
                            // SET UP NEXT MEMBER
                            if (rsmIdx < rsmMax) {
                                lastRefsetMemberUuidMsb = r.refsetMemberUuidMsb;
                                lastRefsetMemberUuidLsb = r.refsetMemberUuidLsb;
                                r = anotationList.get(rsmIdx++);
                                if (r.refsetMemberUuidMsb == lastRefsetMemberUuidMsb
                                        && r.refsetMemberUuidLsb == lastRefsetMemberUuidLsb) {
                                    revision = new ERefsetStrRevision();
                                    revision.setString1(r.valueString);
                                    revision.setStatusUuid(r.status);
                                    revision.setPathUuid(uuidPath);
                                    revision.setTime(r.revTime);
                                    revision.setAuthorUuid(r.authorUuid);
                                    revision.setModuleUuid(uuidModule);
                                    revisionList.add(revision);
                                } else {
                                    checkForMoreVersions = false;
                                }
                            } else {
                                checkForMoreVersions = false;
                                hasMembersToProcess = false;
                            }

                        } while (checkForMoreVersions);

                        tmp.setRevisions(revisionList); // ADD REVISIONS
                    }
                } else {
                    hasMembersToProcess = false;
                }

                // :NYI: tmp.setAdditionalIdComponents(additionalIdComponents);
                listErm.add(tmp);
            } else {
                throw new UnsupportedOperationException("Cannot handle case");
            }

        }

        return listErm;
        //            if (conceptsToWatchMap.containsKey(ec.primordialUuid)) {
        //                getLog().info("Found watch concept after adding refset members: "
        //                        + ec);
        //            }
    }

    class Longform_ConRecord implements Comparable<Longform_ConRecord> {

        UUID status; // CONCEPTSTATUS
        long conSnoId; //  CONCEPTID
        long conUuidMsb; // CONCEPTID
        long conUuidLsb; // CONCEPTID
        int isPrimitive;
        UUID authorUuid;
        long revTime;
        ArrayList<Longform_IdRecord> addedIds;
        ArrayList<Longform_AnnotationRecord> annotations;

        public long getRevTime2() {
            return revTime;
        }

        @Override
        public int compareTo(Longform_ConRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (this.conUuidMsb < o.conUuidMsb) {
                return thisLess; // instance less than received
            } else if (this.conUuidMsb > o.conUuidMsb) {
                return thisMore; // instance greater than received
            } else {
                if (conUuidLsb < o.conUuidLsb) {
                    return thisLess;
                } else if (conUuidLsb > o.conUuidLsb) {
                    return thisMore;
                } else {
                    if (this.revTime < o.revTime) {
                        return thisLess; // instance less than received
                    } else if (this.revTime > o.revTime) {
                        return thisMore; // instance greater than received
                    } else {
                        return 0; // instance == received
                    }
                }
            }
        }
    }

    class Longform_DesRecord implements Comparable<Longform_DesRecord> {

        long desSnoId; // DESCRIPTIONID
        long desUuidMsb;
        long desUuidLsb;
        UUID status; // DESCRIPTIONSTATUS
        // ArrayList<EIdentifier> additionalIds;
        ArrayList<Longform_IdRecord> addedIds;
        ArrayList<Longform_AnnotationRecord> annotations;
        long conSnoId; // CONCEPTID
        long conUuidMsb; // CONCEPTID
        long conUuidLsb; // CONCEPTID    
        String termText; // TERM
        int capStatus; // INITIALCAPITALSTATUS -- capitalization
        UUID descriptionType; // DESCRIPTIONTYPE
        String languageCode; // LANGUAGECODE
        long revTime;
        UUID authorUuid;

        @Override
        public int compareTo(Longform_DesRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (this.desUuidMsb < o.desUuidMsb) {
                return thisLess; // instance less than received
            } else if (this.desUuidMsb > o.desUuidMsb) {
                return thisMore; // instance greater than received
            } else {
                if (desUuidLsb < o.desUuidLsb) {
                    return thisLess;
                } else if (desUuidLsb > o.desUuidLsb) {
                    return thisMore;
                } else {
                    if (this.revTime < o.revTime) {
                        return thisLess; // instance less than received
                    } else if (this.revTime > o.revTime) {
                        return thisMore; // instance greater than received
                    } else {
                        return 0; // instance == received
                    }
                }
            }
        }
    }

    class Longform_RelRecord implements Comparable<Longform_RelRecord> {

        long relSnoId; // SNOMED RELATIONSHIPID, if applicable
        long relUuidMsb;
        long relUuidLsb;
        // List<EIdentifier> additionalIds;
        ArrayList<Longform_IdRecord> addedIds;
        UUID status; // status is computed for relationships
        long c1SnoId; // CONCEPTID1
        long c1UuidMsb;
        long c1UuidLsb;
        long roleTypeSnoId; // RELATIONSHIPTYPE .. SNOMED ID
        UUID roleTypeUuid; // RELATIONSHIPTYPE .. index
        long c2SnoId; // CONCEPTID2
        long c2UuidMsb;
        long c2UuidLsb;
        UUID characteristic; // CHARACTERISTICTYPE
        UUID refinability; // REFINABILITY
        int group; // RELATIONSHIPGROUP
        boolean exceptionFlag; // to handle Concept ID change exception
        long revTime;
        UUID authorUuid; // user: 0=unassigned, 1=inferred/classifier

        @Override
        public int compareTo(Longform_RelRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (this.relUuidMsb > o.relUuidMsb) {
                return thisMore;
            } else if (this.relUuidMsb < o.relUuidMsb) {
                return thisLess;
            } else {
                if (this.relUuidLsb > o.relUuidLsb) {
                    return thisMore;
                } else if (this.relUuidLsb < o.relUuidLsb) {
                    return thisLess;
                } else {
                    if (this.revTime > o.revTime) {
                        return thisMore;
                    } else if (this.revTime < o.revTime) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    class Longform_RelDestRecord implements Comparable<Longform_RelDestRecord> {

        long relUuidMsb; // RELATIONSHIPID
        long relUuidLsb; // RELATIONSHIPID
        long c2UuidMsb;
        long c2UuidLsb;
        UUID roleTypeUuid; // RELATIONSHIPTYPE

        @Override
        public int compareTo(Longform_RelDestRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (this.roleTypeUuid.compareTo(o.roleTypeUuid) > 0) {
                return thisMore;
            } else if (this.roleTypeUuid.compareTo(o.roleTypeUuid) < 0) {
                return thisLess;
            } else {
                return 0; // EQUAL
            }
        }
    }

    enum ComponentType {

        CONCEPT, DESCRIPTION, IMAGE, MEMBER, RELATIONSHIP, UNKNOWN
    };

    enum ValueType {

        BOOLEAN, CONCEPT, INTEGER, STRING
    };

    class Longform_AnnotationRecord implements Comparable<Longform_AnnotationRecord> {

        private static final long serialVersionUID = 1L;
        long conUuidMsb; // ENVELOP CONCEPTID (eConcept to which this concept belongs)
        long conUuidLsb; // ENVELOP CONCEPTID
        long referencedComponentUuidMsb;
        long referencedComponentUuidLsb;
        ComponentType componentType;
        long refsetUuidMsb;
        long refsetUuidLsb;
        long refsetMemberUuidMsb; // aka primordialComponentUuidMsb
        long refsetMemberUuidLsb; // aka primordialComponentUuidLsb
        boolean valueBoolean;
        long valueConUuidMsb;
        long valueConUuidLsb;
        int valueInt;
        String valueString;
        ValueType valueType;
        UUID status; // CONCEPTSTATUS
        private long revTime;
        UUID authorUuid;
        ArrayList<Longform_AnnotationRecord> revisions;

        @Override
        public int compareTo(Longform_AnnotationRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (this.referencedComponentUuidMsb < o.referencedComponentUuidMsb) {
                return thisLess; // instance less than received
            } else if (this.referencedComponentUuidMsb > o.referencedComponentUuidMsb) {
                return thisMore; // instance greater than received
            } else {
                if (this.referencedComponentUuidLsb < o.referencedComponentUuidLsb) {
                    return thisLess;
                } else if (this.referencedComponentUuidLsb > o.referencedComponentUuidLsb) {
                    return thisMore;
                } else {
                    if (this.refsetUuidMsb < o.refsetUuidMsb) {
                        return thisLess; // instance less than received
                    } else if (this.refsetUuidMsb > o.refsetUuidMsb) {
                        return thisMore; // instance greater than received
                    } else {
                        if (this.refsetUuidLsb < o.refsetUuidLsb) {
                            return thisLess;
                        } else if (this.refsetUuidLsb > o.refsetUuidLsb) {
                            return thisMore;
                        } else {
                            if (this.refsetMemberUuidMsb < o.refsetMemberUuidMsb) {
                                return thisLess; // instance less than received
                            } else if (this.refsetMemberUuidMsb > o.refsetMemberUuidMsb) {
                                return thisMore; // instance greater than received
                            } else {
                                if (this.refsetMemberUuidLsb < o.refsetMemberUuidLsb) {
                                    return thisLess;
                                } else if (this.refsetMemberUuidLsb > o.refsetMemberUuidLsb) {
                                    return thisMore;
                                } else {
                                    if (this.revTime < o.revTime) {
                                        return thisLess;
                                    } else if (this.revTime > o.revTime) {
                                        return thisMore;
                                    } else {
                                        return 0; // instance == received
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    class Longform_IdRecord implements Comparable<Longform_IdRecord> {

        long primaryUuidMsb; // CONCEPTID/PRIMARYID
        long primaryUuidLsb; // CONCEPTID/PRIMARYID

        //String idType;
        // SOURCE ID -- DENOTATION
        String denotation = null;
        long denotationLong;
        UUID denotationUuid = null;
        // SOURCE UUID
        // ArchitectonicAuxiliary.Concept.ICD_9.getUids().get(0)
        UUID srcSystemUuid;
        // STATUS UUID
        // ArchitectonicAuxiliary.Concept.CURRENT.getUids().get(0)
        UUID status;
        long revTime; // EFFECTIVE DATE
        UUID authorUuid; // USER

        @Override
        public int compareTo(Longform_IdRecord o) {
            int thisMore = 1;
            int thisLess = -1;
            if (primaryUuidMsb > o.primaryUuidMsb) {
                return thisMore;
            } else if (primaryUuidMsb < o.primaryUuidMsb) {
                return thisLess;
            } else {
                if (primaryUuidLsb > o.primaryUuidLsb) {
                    return thisMore;
                } else if (primaryUuidLsb < o.primaryUuidLsb) {
                    return thisLess;
                } else {
                    if (this.revTime > o.revTime) {
                        return thisMore;
                    } else if (this.revTime < o.revTime) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    private TkIdentifier createEIdentifier(Longform_IdRecord id) {
        if (id.denotation != null) {
            return createEIdentifierString(id);
        } else if (id.denotationUuid != null) {
            return createEIdentifierUuid(id);
        } else {
            return createEIdentifierLong(id);
        }
    }

    private TkIdentifier createEIdentifierString(Longform_IdRecord id) {
        EIdentifierString eId = new EIdentifierString();
        eId.setAuthorityUuid(id.srcSystemUuid);

        eId.setDenotation(id.denotation);

        // PATH
        long msb = uuidPath.getMostSignificantBits();
        long lsb = uuidPath.getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));

        // STATUS
        msb = id.status.getMostSignificantBits();
        lsb = id.status.getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));

        // VERSION (REVISION TIME)
        eId.setTime(id.revTime);

        // AUTHOR
        eId.setAuthorUuid(id.authorUuid);

        // MODULE UUID
        eId.setModuleUuid(uuidModule);

        return eId;
    }

    private TkIdentifier createEIdentifierLong(Longform_IdRecord id) {
        EIdentifierLong eId = new EIdentifierLong();
        eId.setAuthorityUuid(id.srcSystemUuid);

        eId.setDenotation(id.denotationLong);

        // PATH
        long msb = uuidPath.getMostSignificantBits();
        long lsb = uuidPath.getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));

        // STATUS
        msb = id.status.getMostSignificantBits();
        lsb = id.status.getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));

        // VERSION (REVISION TIME)
        eId.setTime(id.revTime);

        // AUTHOR
        eId.setAuthorUuid(id.authorUuid);

        // MODULE UUID
        eId.setModuleUuid(uuidModule);

        return eId;
    }

    private TkIdentifier createEIdentifierUuid(Longform_IdRecord id) {
        EIdentifierUuid eId = new EIdentifierUuid();
        eId.setAuthorityUuid(id.srcSystemUuid);

        eId.setDenotation(id.denotationUuid);

        // PATH
        long msb = uuidPath.getMostSignificantBits();
        long lsb = uuidPath.getLeastSignificantBits();
        eId.setPathUuid(new UUID(msb, lsb));

        // STATUS
        msb = id.status.getMostSignificantBits();
        lsb = id.status.getLeastSignificantBits();
        eId.setStatusUuid(new UUID(msb, lsb));

        // VERSION (REVISION TIME)
        eId.setTime(id.revTime);

        // AUTHOR
        eId.setAuthorUuid(id.authorUuid);

        // MODULE UUID
        eId.setModuleUuid(uuidModule);

        return eId;
    }
}
