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
    private HashSet<UUID> skipUuidSet;
    private HashSet<UUID> refsetsToFilter;
    // Statistics
    private transient int totalMembersConverted = 0;
    private int totalZeros;
    private int totalSingletonsDropped;
    private int totalSnomedCoreRedunancy;
    private int totalSnomedCoreRetired;
    private int totalSupressedTypeZero;
    private int totalSupressedTypeOne;
    private int totalNonCoreBothActive;
    private int totalExtensionBothActive;
    private int totalExtensionCoreInactive;
    private int totalSuppressNonSnomedId;
    private int totalNotProcessedKeptBoth;
    private int totalResidual;
    // private final StringBuilder report;
    private final BufferedWriter reportWriter;

    /**
     * Instantiates a new transformer.
     */
    public FilterRedundantStatedRelsTransformer()
            throws ValidationException, IOException, TerminologyException {
        activeUuid = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getPrimUuid();
        statedUuid = SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getPrimUuid();
        snomedIntId = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getPrimoridalUid();
        snomedCorePathUuid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();
        extensionPath = UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a");
        developmentPath = UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf");

        totalZeros = 0;
        totalSingletonsDropped = 0;
        totalSnomedCoreRedunancy = 0;
        totalSnomedCoreRetired = 0;
        totalSupressedTypeZero = 0;
        totalSupressedTypeOne = 0;
        totalNonCoreBothActive = 0;
        totalExtensionBothActive = 0;
        totalExtensionCoreInactive = 0;
        totalSuppressNonSnomedId = 0;
        totalNotProcessedKeptBoth = 0;

        totalResidual = 0;

        ts = Ts.get();
        kpExtensionPathNid = ts.getNidForUuids(extensionPath);
        snomedCorePathNid = ts.getNidForUuids(snomedCorePathUuid);

        // report = new StringBuilder();
        File reportFile = new File("report.txt");
        reportWriter = new BufferedWriter(new FileWriter(reportFile));
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
        // setup check for skips UUIDs of concepts to be exclude from export
        // parameters.skipUuidList.uuid
        skipUuidSet = new HashSet();
        TransformersConfigApi api = new TransformersConfigApi(xmlFile);
        for (String loopValue : api.getCollectionAt(api.getIntId(id),
                "parameters.skipUuidList.uuid")) {
            skipUuidSet.add(UUID.fromString(loopValue));
        }
        refsetsToFilter = new HashSet();
        for (String loopValue : api.getCollectionAt(api.getIntId(id),
                "parameters.refsetsToFilter.uuid")) {
            refsetsToFilter.add(UUID.fromString(loopValue));
        }
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
        if (skipUuidSet.contains(eConcept.primordialUuid)) {
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
        for (TkRefexAbstractMember<?> member : eConcept.refsetMembers) {
            if (!skipUuidSet.contains(member.componentUuid)) {
                keepMemberList.add(member);
            }
        }
        eConcept.refsetMembers = keepMemberList;
    }
    
    //    private static final UUID watchUuid = UUID.fromString("80047350-83f2-3d0b-812c-90912af86ba7");
    private void filterRedundantStatedRelationships(TkConcept eConcept)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {

        ArrayList<LogicalRel> keepRels = new ArrayList<>();
        ArrayList<LogicalRel> statedRels = new ArrayList<>();
        List<TkRelationship> rels = eConcept.getRelationships();

        for (TkRelationship tkr : rels) {
            if (tkr.characteristicUuid.compareTo(statedUuid) == 0) {
                statedRels.add(new LogicalRel(tkr));
            } else {
                keepRels.add(new LogicalRel(tkr));
            }
        }

//        if (eConcept.getPrimordialUuid().compareTo(watchUuid) == 0) {
//            System.out.println(":DEBUG: found watch concept");
//        }
        statedRels = LogicalRelComputer.addLogicalUuidsWithSort(statedRels);
        if (statedRels.size() <= 1) {
            return;
        }

        int i = 0;
        ArrayList<LogicalRel> sameLogicalIdRels = new ArrayList<>();
        while (i < statedRels.size()) {
            // gather relationships of same logical rel uuid
            sameLogicalIdRels.clear();
            sameLogicalIdRels.add(statedRels.get(i));
            UUID uuidToTest = statedRels.get(i).logicalRelUuid;
            i++;
            while (i < statedRels.size()
                    && uuidToTest.compareTo(statedRels.get(i).logicalRelUuid) == 0) {
                sameLogicalIdRels.add(statedRels.get(i));
                i++;
            }

            if (sameLogicalIdRels.isEmpty()) {
                totalZeros++;
                continue;
            }

            if (sameLogicalIdRels.size() == 1) {
                LogicalRel a = sameLogicalIdRels.get(0);
                if (a.relSctIdPath != null
                        && isExtensionSctId(a.relSctId)) {
                    totalSingletonsDropped++;
                    keepRels.addAll(sameLogicalIdRels);
                    reportWriter.append(toStr("singleton with KP SCTID kept", eConcept, a));
                } else {
                    keepRels.addAll(sameLogicalIdRels);
                }
                continue;
            }

            if (sameLogicalIdRels.size() > 2) {
                if (findOnPathCount(sameLogicalIdRels, developmentPath) == 1) {
                    int idx = findOnPathIdx(sameLogicalIdRels, developmentPath);
                    sameLogicalIdRels.remove(idx);
                } else {
                    throw new UnsupportedOperationException("\n:!!!: sameLogicalIdRels.size() > 2 not expected");
                }

                if (sameLogicalIdRels.size() != 2) {
                    throw new UnsupportedOperationException("\n:!!!: sameLogicalIdRels.size() != 2");
                }
            }
            // detect presence of redundant active non-core sctid on snomed core rel
            LogicalRel a = sameLogicalIdRels.get(0);
            LogicalRel b = sameLogicalIdRels.get(1);

            if (a.relSctIdPath != null && b.relSctIdPath == null) {
                keepRels.add(b);
                totalSupressedTypeZero++;
                totalMembersConverted++;
                reportWriter.append(toStr("one redundancy missing sctid", eConcept, b));
            } else if (a.relSctIdPath == null && b.relSctIdPath != null) {
                keepRels.add(a);
                totalSupressedTypeZero++;
                totalMembersConverted++;
                reportWriter.append(toStr("one redundancy missing sctid", eConcept, a));
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) == 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) == 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                keepRels.add(a);
                keepRels.add(b);
                reportWriter.append(toStr("SNOMED Core Redundancy", eConcept, a));
                totalSnomedCoreRedunancy++;
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) != 0
                    && b.statusUuid.compareTo(activeUuid) == 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) == 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                keepRels.add(a);
                keepRels.add(b);
                reportWriter.append(toStr("SNOMED Core Retired", eConcept, a));
                totalSnomedCoreRetired++;
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) != 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) == 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                keepRels.add(a);
                keepRels.add(b);
                reportWriter.append(toStr("SNOMED Core Retired", eConcept, a));
                totalSnomedCoreRetired++;
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                keepRels.add(a);
                totalMembersConverted++;
                totalSupressedTypeOne++;
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) == 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                keepRels.add(b);
                totalMembersConverted++;
                totalSupressedTypeOne++;
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) != 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) != 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) == 0) {
                keepRels.add(a);
                keepRels.add(b);
                totalNonCoreBothActive++;
                reportWriter.append(toStr("both non-core, both active", eConcept, a));
            } else if (a.pathUuid.compareTo(extensionPath) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) == 0) {
                // keepRels.add(a);
                keepRels.add(b);
                totalExtensionBothActive++;
                reportWriter.append(toStr("extension, both active", eConcept, a));
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(extensionPath) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) == 0) {
                keepRels.add(a);
                // keepRels.add(b);
                totalExtensionBothActive++;
                reportWriter.append(toStr("extension, both active", eConcept, a));
            } else if (a.pathUuid.compareTo(extensionPath) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.statusUuid.compareTo(activeUuid) == 0
                    && b.statusUuid.compareTo(activeUuid) != 0) {
                keepRels.add(a);
                keepRels.add(b);
                totalExtensionCoreInactive++;
                reportWriter.append(toStr("extension, core inactive", eConcept, a));
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(extensionPath) == 0
                    && a.statusUuid.compareTo(activeUuid) != 0
                    && b.statusUuid.compareTo(activeUuid) == 0) {
                keepRels.add(a);
                keepRels.add(b);
                totalExtensionCoreInactive++;
                reportWriter.append(toStr("extension, core inactive", eConcept, a));
            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) == 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) != 0) {
                // most recent edits of *both* rels are on snomed core path
                // however, one of the ids is not on snomed core
                // keep only the one on with id on snomed core
                keepRels.add(a);
                totalSuppressNonSnomedId++;
                reportWriter.append(toStr("suppressed non-snomed id", eConcept, a));

            } else if (a.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && b.pathUuid.compareTo(snomedCorePathUuid) == 0
                    && a.relSctIdPath.compareTo(snomedCorePathUuid) != 0
                    && b.relSctIdPath.compareTo(snomedCorePathUuid) == 0) {
                // most recent edits of *both* rels are on snomed core path
                // however, one of the ids is not on snomed core
                // keep only the one on with id on snomed core
                keepRels.add(b);
                totalSuppressNonSnomedId++;
                reportWriter.append(toStr("suppressed non-snomed id", eConcept, b));
            } else {
                keepRels.add(a);
                keepRels.add(b);
                totalNotProcessedKeptBoth++;
                reportWriter.append(toStr("some other case", eConcept, a));
                if (totalNotProcessedKeptBoth < 10) {
                    if (a.tkr != null && b.tkr != null) {
                        reportWriter.append("\n:::some other case DETAIL_A " + a.tkr.toString());
                        reportWriter.append("\n:::some other case DETAIL_B " + b.tkr.toString());
                    }
                }
            }

        }

        // keep the rels of interest
        List<TkRelationship> relList = new ArrayList<>();
        for (LogicalRel logicalRel : keepRels) {
            relList.add(logicalRel.tkr);
            if (logicalRel.relSctIdPath != null
                    && isExtensionSctId(logicalRel.relSctId)) {
                totalResidual++;
                reportWriter.append(toStr("residual", eConcept, logicalRel));
            }
        }
        eConcept.setRelationships(relList);
    }

    // isExtensionSctId (sameLogicalIdRels.get(2).relSctId)
    private boolean isExtensionSctId(Long sctid) {
        String sctidStr = Long.toString(sctid);
        int length = sctidStr.length();
        if (length < 10) {
            return false;
        }
        String nameSpaceIdentifier = sctidStr.substring(length - 10, length - 3);
        if (nameSpaceIdentifier.equalsIgnoreCase("1000119")) {
            // :NYI: "1000119" needs to be generalized for broader applications
            return true;
        }
        return false;
    }

    private String toStr(String s, TkConcept eConcept, LogicalRel rel)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("\n:: ");
        sb.append(s);
        sb.append(" :: ");
        sb.append(eConcept.primordialUuid.toString());
        sb.append(" :: ");
        sb.append(ts.getConcept(eConcept.primordialUuid).toUserString());
        
        if (rel != null) {
            sb.append(" | ");
            sb.append(ts.getConcept(rel.roleTypeSnoId).toUserString());
            sb.append(" | ");
            sb.append(ts.getConcept(rel.c2SnoId).toUserString());
        }

        return sb.toString();
    }

    /*
     * (non-Javadoc) @see
     * org.ihtsdo.mojo.schema.AbstractTransformer#postProcessIteration()
     */
    @Override
    public void preProcessIteration() {
        // nothing to do
    }

    @Override
    public List<TkConcept> postProcessIteration() {
            StringBuilder sb = new StringBuilder();
            sb.append("**** Final, total converted ");
            sb.append(totalMembersConverted);
            sb.append(" members");

            sb.append("\n::: totalZeros ");
            sb.append(totalZeros);
            sb.append("\n::: totalSingletons ");
            sb.append(totalSingletonsDropped);

            sb.append("\n::: totalSnomedCoreRedunancy ");
            sb.append(totalSnomedCoreRedunancy);
            sb.append("\n");
            sb.append("\n::: totalSnomedCoreRetired ");
            sb.append(totalSnomedCoreRetired);

            sb.append("\n::: totalSupressedTypeZero ");
            sb.append(totalSupressedTypeZero);
            sb.append("\n::: totalSupressedTypeOne ");
            sb.append(totalSupressedTypeOne);
            sb.append("\n::: totalNonCoreBothActive ");
            sb.append(totalNonCoreBothActive);
            sb.append("\n::: totalExtensionBothActive ");
            sb.append(totalExtensionBothActive);
            sb.append("\n::: totalExtensionCoreInactive ");
            sb.append(totalExtensionCoreInactive);
            sb.append("\n::: totalSuppressNonSnomedId ");
            sb.append(totalSuppressNonSnomedId);
            sb.append("\n::: totalNotProcessedKeptBoth ");
            sb.append(totalNotProcessedKeptBoth);

            sb.append("\n::: totalResidual ");
            sb.append(totalResidual);
            sb.append("\n");
            System.out.println(sb.toString());

        try {
            reportWriter.append(sb.toString());

            reportWriter.flush();
            reportWriter.close();

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
