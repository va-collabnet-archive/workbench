/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.mojo.schema.transformer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.ERefsetCidMember;
import org.ihtsdo.helper.rf2.LogicalRel;
import org.ihtsdo.helper.rf2.LogicalRelComputer;
import org.ihtsdo.mojo.schema.AbstractTransformer;
import org.ihtsdo.mojo.schema.config.TransformersConfigApi;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.TkConcept;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.spec.ValidationException;

/**
 * This transformer checks for logically redundant relationship. This first
 * implementation applied to KP instance where the RF2 content has change sets
 * and extensions. This transformer still need to be generalized.
 *
 * @author Marc E. Campbell
 */
public class FilterRedundantStatedRelsTransformer extends AbstractTransformer {

    private static final Logger logger =
            Logger.getLogger(FilterRedundantStatedRelsTransformer.class.getSimpleName());
    /**
     * The id.
     */
    private final String id = "filter-redundant-stated-rels";
    /**
     * The concept count.
     */
    private final TerminologyStoreDI ts;
    private final UUID activeUuid;
    private final UUID statedUuid;
    private final UUID snomedIntId;
    private final UUID snomedCorePathUuid;
    private final int snomedCorePathNid;
    private final UUID extensionPath;
    private final int kpExtensionPathNid;
    private final UUID developmentPath;
    private final UUID terminologyAuxiliaryModule;
    private HashSet<UUID> skipUuidSet;
    private HashSet<UUID> skipPathUuidSet;
    private HashSet<UUID> refsetsToFilter;
    private HashSet<UUID> watchUuidSet;
    // REPORTS
    private final BufferedWriter reportWriter;
    private final BufferedWriter reportListGroup0AdditionsWriter;
    private final BufferedWriter reportGroupAdditionsWriter;
    private final BufferedWriter reportListRoleGroupAdditionsWriter;
    private LogicalRelComputer logicalRelComputer;

    /**
     * Instantiates a new transformer.
     * @throws org.ihtsdo.tk.spec.ValidationException
     * @throws org.dwfa.tapi.TerminologyException
     */
    public FilterRedundantStatedRelsTransformer()
            throws ValidationException, IOException, TerminologyException {
        activeUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid();
        statedUuid = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid();
        snomedIntId = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid();
        snomedCorePathUuid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();
        extensionPath = UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a");
        developmentPath = UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf");
        terminologyAuxiliaryModule = UUID.fromString("dacb22ed-b2df-3667-88b8-2c17a545d37e");
        

        ts = Ts.get();
        kpExtensionPathNid = ts.getNidForUuids(extensionPath);
        snomedCorePathNid = ts.getNidForUuids(snomedCorePathUuid);

        // report = new StringBuilder();
        File reportFile = new File("report.txt");
        reportWriter = new BufferedWriter(new FileWriter(reportFile));
        reportFile = new File("reportListGroup0Additions.txt");
        reportListGroup0AdditionsWriter = new BufferedWriter(new FileWriter(reportFile));
        // 
        reportFile = new File("reportGroupAdditions.txt");
        reportGroupAdditionsWriter = new BufferedWriter(new FileWriter(reportFile));
        reportFile = new File("reportListRoleGroupAdditions.txt");
        reportListRoleGroupAdditionsWriter = new BufferedWriter(new FileWriter(reportFile));
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#setupFromXml(java.lang.String)
     */
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setupFromXml(String xmlFile) throws Exception {
        TransformersConfigApi api = new TransformersConfigApi(xmlFile);
        // setup check for skips UUIDs of concepts to be exclude from export
        // parameters.UuidList.uuid
        skipUuidSet = new HashSet();
        for (String loopValue : api.getCollectionAt(api.getIntId(id),
                "parameters.skipUuidList.uuid")) {
            skipUuidSet.add(UUID.fromString(loopValue));
        }
        // parameters.skipPathUuidList.uuid
        skipPathUuidSet = new HashSet();
        for (String loopValue : api.getCollectionAt(api.getIntId(id),
                "parameters.skipPathUuidList.uuid")) {
            skipPathUuidSet.add(UUID.fromString(loopValue));
        }
        refsetsToFilter = new HashSet();
        for (String loopValue : api.getCollectionAt(api.getIntId(id),
                "parameters.refsetsToFilter.uuid")) {
            refsetsToFilter.add(UUID.fromString(loopValue));
        }
        watchUuidSet = new HashSet();
        // RF2 "Ischemia, viscera"
        // watchUuidSet.add(UUID.fromString("ae72b717-f028-766d-91ef-0216c2f5b505"));
        // RF1 "Ischemia, viscera"
        // watchUuidSet.add(UUID.fromString("e245603d-309e-567d-be11-647786e61a08"));
        // watchUuidSet.add(UUID.fromString("1adb3f25-66be-47a8-a024-efe8b31146e6")); // KPET CMT Project release candidate path
        // watchUuidSet.add(UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf")); // KPET CMT Project development path
        // watchUuidSet.add(UUID.fromString("098eed03-204c-5bf0-91c0-3c9610beec6b")); // KPET CMT Project development origin path
        // watchUuidSet.add(UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a")); // KPET Extension Path
        // watchUuidSet.add(UUID.fromString("43409306-c304-559b-9440-de06ece1eda7")); // US Extension path
        // watchUuidSet.add(UUID.fromString("7b6ace72-4604-5ff1-b8c0-48e8f6204e3d")); // source baseline
        // watchUuidSet.add(UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")); // SNOMED Core
        // watchUuidSet.add(UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")); // Workbench Auxiliary
    }

    @Override
    public void transformAttributes(TkConceptAttributes attributes, TkConcept concept) {
        // nothing to do
    }

    @Override
    public void transformDescription(TkDescription description, TkConcept concept) {
        // nothing to do
    }

    @Override
    public void transformRelationship(TkRelationship relationship, TkConcept concept) {
        // nothing to do
    }

    @Override
    public void transformAnnotation(TkRefexAbstractMember<?> annotation, TkComponent<?> component) {
        // nothing to do
    }

    @Override
    public void transformMember(TkRefexAbstractMember<?> member, TkConcept concept) {
        // nothing to do
    }

    @Override
    public boolean postProcessConcept(TkConcept eConcept) {
        if (watchUuidSet.contains(eConcept.primordialUuid)) {
            System.out.println("FilterRedundantStatedRelsTransformer :: watch found :: " + eConcept.primordialUuid);
        }
        if (skipUuidSet.contains(eConcept.primordialUuid)) {
            return false;
        }
        if (skipPathUuidSet.contains(eConcept.primordialUuid)) {
            return false;
        }
        if (refsetsToFilter.contains(eConcept.primordialUuid)) {
            filterMembersByComponentUuid(eConcept);
        }
        try {
            if (eConcept.hasAuxIsa() && eConcept.hasSnomedIsa()) {
                reportWriter.append(toStr("DATA CHECK: eConcept hasActiveAuxIsa && hasActiveSnomedIsa ", eConcept, null));
            }
            if (eConcept.hasAuxIsa() == false && eConcept.hasSnomedIsa() == false) {
                reportWriter.append(toStr("DATA CHECK: eConcept is root or orphan ", eConcept, null));
            }
            if (eConcept.hasSnomedIsa()) {
                filterRedundantStatedRelationships(eConcept);
            }
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            logger.severe(ex.toString());
        } catch (IOException ex) {
            logger.severe(ex.toString());
        }
        return true;
    }

    private void filterMembersByComponentUuid(TkConcept eConcept) {
        ArrayList<TkRefexAbstractMember<?>> keepMemberList = new ArrayList<>();
        // Path origin reference set
        if (eConcept.primordialUuid.compareTo(UUID.fromString("1239b874-41b4-32a1-981f-88b448829b4b")) == 0) {
            for (TkRefexAbstractMember<?> member : eConcept.refsetMembers) {
                UUID uuid = member.componentUuid;
                if (uuid != null && !skipPathUuidSet.contains(uuid)) {
                    member.moduleUuid = terminologyAuxiliaryModule;
                    keepMemberList.add(member);
                }
            }
        }
        // Path reference set
        if (eConcept.primordialUuid.compareTo(UUID.fromString("fd9d47b7-c0a4-3eea-b3ab-2b5a3f9e888f")) == 0) {
            for (TkRefexAbstractMember<?> member : eConcept.refsetMembers) {
                if (ERefsetCidMember.class.isAssignableFrom(member.getClass())) {
                    UUID uuid = ((ERefsetCidMember) member).uuid1;
                    if (uuid != null && !skipPathUuidSet.contains(uuid)) {
                        member.moduleUuid = terminologyAuxiliaryModule;
                        keepMemberList.add(member);
                    }
                }
            }
        }
        eConcept.refsetMembers = keepMemberList;
    }

    //    private static final UUID watchUuid = UUID.fromString("80047350-83f2-3d0b-812c-90912af86ba7");
    private void filterRedundantStatedRelationships(TkConcept eConcept)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        List<TkRelationship> finalRelList = new ArrayList<>();
        // INTERMEDIATE LISTS
        ArrayList<LogicalRel> statedNonGroup0LogicalRels = new ArrayList<>();
        ArrayList<LogicalRel> statedGroup0LogicalRels = new ArrayList<>();
        List<TkRelationship> rels = eConcept.getRelationships();

        for (TkRelationship tkr : rels) {
            if (tkr.characteristicUuid.compareTo(statedUuid) == 0) {
                if (tkr.relGroup == 0) {
                    statedGroup0LogicalRels.add(new LogicalRel(tkr));
                } else {
                    statedNonGroup0LogicalRels.add(new LogicalRel(tkr));
                }
            } else {
                finalRelList.add(tkr);
            }
        }

        // PROCESS GROUP0 RELS
        ArrayList<LogicalRel> list = logicalRelComputer.processRelsGroup0(statedGroup0LogicalRels);
        for (LogicalRel logicalRel : list) {
            finalRelList.add(logicalRel.tkr);
            if (logicalRel.relSctIdPath != null
                    && isExtensionSctId(logicalRel.relSctId)
                    && eConcept.getConceptAttributes().pathUuid.compareTo(snomedCorePathUuid) == 0) {
                reportWriter.append(toStr("group0 addition to snomed", eConcept, logicalRel));
                reportListGroup0AdditionsWriter.append(toStrList(eConcept));
            }
        }

        // PROCESS REL GROUPS
        list = logicalRelComputer.processRelGroups(statedNonGroup0LogicalRels);
        for (LogicalRel logicalRel : list) {
            finalRelList.add(logicalRel.tkr);
        }

        // SET ECONCEPT RELS 
        eConcept.setRelationships(finalRelList);
    }

    // isExtensionSctId (sameLogicalIdRels.get(2).relSctId)
    private boolean isExtensionSctId(Long sctid) {
        String sctidStr = Long.toString(sctid);
        int length = sctidStr.length();
        if (length < 10) {
            return false;
        }
        String nameSpaceIdentifier = sctidStr.substring(length - 10, length - 3);
        // :NYI: needs to be generalized.  "1000119" is KP namespace
        return nameSpaceIdentifier.equalsIgnoreCase("1000119");
    }

    private String toStr(String s, TkConcept eConcept, LogicalRel rel) throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("\n:: ");
        sb.append(s);
        sb.append(" :: ");
        sb.append(eConcept.primordialUuid.toString());

        if (rel != null) {
            // role group number
            sb.append(" group:");
            sb.append(rel.group);
            sb.append(" | c1: ");
        } else {
            sb.append(" :: ");
        }

        if (eConcept.primordialUuid.compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) == 0) {
            sb.append("00000000-0000-0000-c000-000000000046");
        } else {
            sb.append(ts.getConcept(eConcept.primordialUuid).toUserString());
        }

        if (rel != null) {
            sb.append(" | type: ");
            if (rel.typeSnoId.compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) == 0) {
                sb.append("00000000-0000-0000-c000-000000000046");
            } else {
                sb.append(ts.getConcept(rel.typeSnoId).toUserString());
            }

            sb.append(" | c2: ");
            if (rel.c2SnoId.compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) == 0) {
                sb.append("00000000-0000-0000-c000-000000000046");
            } else {
                sb.append(ts.getConcept(rel.c2SnoId).toUserString());
            }

            sb.append(" | latest path: ");
            if (rel.pathLastRevisionUuid.compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) == 0) {
                sb.append("00000000-0000-0000-c000-000000000046");
            } else {
                sb.append(ts.getConcept(rel.pathLastRevisionUuid).toUserString());
            }
        }

        return sb.toString();
    }

    private String toStrList(TkConcept eConcept) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append(eConcept.primordialUuid.toString());
        sb.append("\t");
        if (eConcept.primordialUuid.compareTo(UUID.fromString("00000000-0000-0000-c000-000000000046")) == 0) {
            sb.append("null");
        } else {
            sb.append(ts.getConcept(eConcept.primordialUuid).toUserString());
        }
        sb.append("\n");
        return sb.toString();
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
     */
    @Override
    public void preProcessIteration() {
        logicalRelComputer = new LogicalRelComputer(reportGroupAdditionsWriter,
            reportListRoleGroupAdditionsWriter);
    }

    @Override
    public List<TkConcept> postProcessIteration() {
        try {
            reportWriter.append("\n\n##############################\n\n");
            reportWriter.append(logicalRelComputer.toStringReport());

            reportWriter.flush();
            reportWriter.close();

            reportListGroup0AdditionsWriter.flush();
            reportListGroup0AdditionsWriter.close();
            
            reportGroupAdditionsWriter.flush();
            reportGroupAdditionsWriter.flush();
            reportListRoleGroupAdditionsWriter.flush();
            reportListRoleGroupAdditionsWriter.close();

        } catch (IOException ex) {
            Logger.getLogger(FilterRedundantStatedRelsTransformer.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<TkConcept> postProcessList = new ArrayList<>();
        return postProcessList;
    }

    private int findOnPathCount(ArrayList<LogicalRel> rels, UUID path) {
        int count = 0;
        for (LogicalRel rel : rels) {
            if (rel.relSctIdPath != null
                    && rel.relSctIdPath.compareTo(path) == 0) {
                count++;
            }
        }
        return count;
    }

    private int findOnPathIdx(ArrayList<LogicalRel> rels, UUID path) {
        for (int i = 0; i < rels.size(); i++) {
            if (rels.get(i).relSctIdPath != null
                    && rels.get(i).relSctIdPath.compareTo(path) == 0) {
                return i;
            }
        }
        return -1; // not found
    }
}
