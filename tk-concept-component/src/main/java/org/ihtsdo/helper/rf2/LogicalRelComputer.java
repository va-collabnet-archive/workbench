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
package org.ihtsdo.helper.rf2;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @author code
 */
public class LogicalRelComputer {

    private final UUID snomedPathUuid = UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
    private final UUID extensionPathUuid = UUID.fromString("2bfc4102-f630-5fbe-96b8-625f2a6b3d5a");
    private final UUID developmentPathUuid = UUID.fromString("3770e517-7adc-5a24-a447-77a9daa3eedf");
    private final static UUID SNOMED_RF2_ACTIVE_UUID = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
    private final TerminologyStoreDI ts;
    // 
    private final BufferedWriter reportGroupAdditionsWriter;
    private final BufferedWriter reportListRoleGroupAdditionsWriter;
    private final StringBuffer sbType0List;
    private final StringBuffer sbType2List;
    private final StringBuffer sbType3List;
    private final StringBuffer sbType0Verbose;
    private final StringBuffer sbType2Verbose;
    private final StringBuffer sbType3Verbose;

    public LogicalRelComputer(BufferedWriter reportGroupAdditionsWriter,
            BufferedWriter reportListRoleGroupAdditionsWriter) {
        this.ts = Ts.get();
        this.reportGroupAdditionsWriter = reportGroupAdditionsWriter;
        this.reportListRoleGroupAdditionsWriter = reportListRoleGroupAdditionsWriter;
        this.sbType0List = new StringBuffer("\n##############\n### Case 0 ###\n##############\n");
        this.sbType2List = new StringBuffer("\n##############\n### Case 2 ###\n##############\n");
        this.sbType3List = new StringBuffer("\n##############\n### Case 3 ###\n##############\n");
        this.sbType0Verbose = new StringBuffer("\n####################\n### Case 0 LISTS ###\n####################\n");
        this.sbType2Verbose = new StringBuffer("\n####################\n### Case 2 LISTS ###\n####################\n");
        this.sbType3Verbose = new StringBuffer("\n####################\n### Case 3 LISTS ###\n####################\n");
    }

    private enum RelFlavor {

        REL_FROM_SNOMED,
        REL_FROM_SNOMED_WITH_LEGACY_FILLER_SCTID,
        REL_FROM_EXTENSION,
        REL_FROM_ECCS,
        REL_FROM_US_EXTENSION;
    }

    private RelFlavor processRelFlavor(LogicalRel rel) {
        if (rel.relSctIdPath != null
                && rel.relSctIdPath.compareTo(snomedPathUuid) != 0
                && rel.pathLastRevisionUuid.compareTo(snomedPathUuid) == 0) {
            // disregard snomed relationships to which KPs only change is to add an id 
            return RelFlavor.REL_FROM_SNOMED_WITH_LEGACY_FILLER_SCTID;
        } else if (rel.relSctIdPath != null
                && rel.relSctIdPath.compareTo(snomedPathUuid) == 0) {
            return RelFlavor.REL_FROM_SNOMED;
        } else if (rel.relSctIdPath != null
                && rel.relSctIdPath.compareTo(extensionPathUuid) == 0) {
            return RelFlavor.REL_FROM_EXTENSION;
        } else if (rel.relSctIdPath != null
                && rel.relSctIdPath.compareTo(SnomedMetadataRf2.US_EXTENSION_PATH.getUuids()[0]) == 0) {
            return RelFlavor.REL_FROM_US_EXTENSION;
        } else {
            return RelFlavor.REL_FROM_EXTENSION;
        }
    }

    // exceptions list:  SCTID or UUID <tab> short name <\n>
    public ArrayList<LogicalRel> processRelsGroup0(ArrayList<LogicalRel> a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        ArrayList<LogicalRel> keepList = new ArrayList<>();
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(a);

        ArrayList<LogicalRel> equivalentRelList = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            LogicalRel currentRel = a.get(i);
            equivalentRelList.add(currentRel);
            if (i == a.size() - 1) {
                // BOUNDARY: last rel on list
                processRelsGroup0EquivList(equivalentRelList, keepList);
            } else {
                LogicalRel nextRel = a.get(i + 1);  // LOOK AHEAD
                if (currentRel.c2SnoId.compareTo(nextRel.c2SnoId) != 0
                        || currentRel.typeSnoId.compareTo(nextRel.typeSnoId) != 0) {
                    processRelsGroup0EquivList(equivalentRelList, keepList);
                    equivalentRelList.clear();
                }
            }
        }
        return keepList;
    }

    private void processRelsGroup0EquivList(ArrayList<LogicalRel> equivalentRelList,
            ArrayList<LogicalRel> keepList) throws IOException {
        ArrayList<LogicalRel> tempList = new ArrayList<>();
        for (LogicalRel logicalRel : equivalentRelList) {
            RelFlavor relFlavor = processRelFlavor(logicalRel);
            switch (relFlavor) {
                case REL_FROM_SNOMED_WITH_LEGACY_FILLER_SCTID:
                    //do not keep 
                    break;
                case REL_FROM_SNOMED:
                case REL_FROM_EXTENSION:
                case REL_FROM_ECCS:
                case REL_FROM_US_EXTENSION:
                    tempList.add(logicalRel);
                    break;
            }
        }

        if (tempList.isEmpty()) {
            // REPORT CONCEPT LIST FOR WORKBENCH LIST VIEW
            sbType0List.append(equivalentRelList.get(0).c1SnoId.toString());
            sbType0List.append("\t");
            ConceptChronicleBI cb = ts.getConcept(equivalentRelList.get(0).c1SnoId);
            if (cb != null) {
                sbType0List.append(cb.toUserString());
            } else {
                sbType0List.append("NULL CONCEPT");
            }
            sbType0List.append("\n");

            // EXPANDED REPORT
            sbType0Verbose.append("###  tempList.size() relationship would not have been kept.\n");
            sbType0Verbose.append("Concept: ");
            sbType0Verbose.append(equivalentRelList.get(0).c1SnoId.toString());
            sbType0Verbose.append("\n");
            sbType0Verbose.append("  ## equivalentRelList ... kept, however, should be retired ##\n");
            for (LogicalRel logicalRel : equivalentRelList) {
                sbType0Verbose.append("  Relationship: ");
                if (logicalRel.tkr != null) {
                    sbType0Verbose.append(logicalRel.tkr.primordialUuid.toString());
                } else {
                    sbType0Verbose.append(" (null) ");
                }
                sbType0Verbose.append(" ");
                sbType0Verbose.append(processRelFlavor(logicalRel).toString());
                if (logicalRel.statusUuid != null
                        && logicalRel.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) == 0) {
                    sbType0Verbose.append(" Active  ");
                } else {
                    sbType0Verbose.append(" Inactive");
                }
                sbType0Verbose.append(" time:");
                sbType0Verbose.append(logicalRel.time);
                sbType0Verbose.append(" ");
                sbType0Verbose.append(logicalRel.toStringUser());
                sbType0Verbose.append("\n");
            }
            sbType0Verbose.append("  ## tempList ##\n");
            for (LogicalRel logicalRel : tempList) {
                sbType0Verbose.append("  Relationship: ");
                if (logicalRel.tkr != null) {
                    sbType0Verbose.append(logicalRel.tkr.primordialUuid.toString());
                } else {
                    sbType0Verbose.append(" (null) ");
                }
                sbType0Verbose.append(" ");
                sbType0Verbose.append(processRelFlavor(logicalRel).toString());
                sbType0Verbose.append("\n");
            }
            // KEEP ORIGINAL LIST
            keepList.addAll(equivalentRelList);
        } else if (tempList.size() == 1) {
            keepList.addAll(tempList);
        } else { // tempList.size() > 1
            int activeCount = 0;
            for (LogicalRel lr : tempList) {
                if (lr.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) == 0) {
                    activeCount++;
                }
            }
            if (activeCount < 2) {
                keepList.addAll(tempList);
                return;
            }
            // REPORT CONCEPT LIST FOR WORKBENCH LIST VIEW
            sbType3List.append(equivalentRelList.get(0).c1SnoId.toString());
            sbType3List.append("\t");
            ConceptChronicleBI cb = ts.getConcept(equivalentRelList.get(0).c1SnoId);
            if (cb != null) {
                sbType3List.append(cb.toUserString());
            } else {
                sbType3List.append("NULL CONCEPT");
            }
            sbType3List.append("\n");

            // EXPANDED REPORT
            sbType3Verbose.append("###  ACTIVE tempList.size()==");
            sbType3Verbose.append(activeCount);
            sbType3Verbose.append("\n");
            sbType3Verbose.append("Concept: ");
            sbType3Verbose.append(equivalentRelList.get(0).c1SnoId.toString());
            sbType3Verbose.append("\n");
            sbType3Verbose.append("  ## equivalentRelList ... processed ##\n");
            for (LogicalRel logicalRel : equivalentRelList) {
                if (logicalRel.statusUuid != null
                        && logicalRel.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) != 0) {
                    continue; // do not report Inactive
                }
                sbType3Verbose.append("  Relationship: ");
                if (logicalRel.tkr != null) {
                    sbType3Verbose.append(logicalRel.tkr.primordialUuid.toString());
                } else {
                    sbType3Verbose.append(" (null) ");
                }
                sbType3Verbose.append(" ");
                sbType3Verbose.append(processRelFlavor(logicalRel).toString());
                sbType3Verbose.append(" time:");
                sbType3Verbose.append(logicalRel.time);
                sbType3Verbose.append(" ");
                sbType3Verbose.append(logicalRel.toStringUser());
                sbType3Verbose.append("\n");
            }
            sbType3Verbose.append("  ## tempList ... kept ##\n");
            for (LogicalRel logicalRel : tempList) {
                if (logicalRel.statusUuid != null
                        && logicalRel.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) != 0) {
                    continue; // do not report Inactive
                }
                sbType3Verbose.append("  Relationship: ");
                if (logicalRel.tkr != null) {
                    sbType3Verbose.append(logicalRel.tkr.primordialUuid.toString());
                } else {
                    sbType3Verbose.append(" (null) ");
                }
                sbType3Verbose.append(" ");
                sbType3Verbose.append(processRelFlavor(logicalRel).toString());
                sbType3Verbose.append(" time:");
                sbType3Verbose.append(logicalRel.time);
                sbType3Verbose.append(" ");
                sbType3Verbose.append(logicalRel.toStringUser());
                sbType3Verbose.append("\n");
            }

            // KEEP ORIGINAL LIST MINUS REL_FROM_SNOMED_WITH_LEGACY_FILLER_SCTID
            keepList.addAll(tempList);
        }
    }

    /**
     * checks name space of sctid
     */
    private boolean isExtensionSctId(Long sctid) {
        String sctidStr = Long.toString(sctid);
        int length = sctidStr.length();
        if (length < 10) {
            return false;
        }
        String nameSpaceIdentifier = sctidStr.substring(length - 10, length - 3);
        return nameSpaceIdentifier.equalsIgnoreCase("1000119");
    }

    private ArrayList<LogicalRelGroup> convertToRelGroups(ArrayList<LogicalRel> relsNonGroup0List,
            boolean keepHistory)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        ArrayList<LogicalRelGroup> logicalRelGroupList = new ArrayList<>();
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(relsNonGroup0List);

        int prevRelGroup = -1;
        LogicalRelGroup thisRelGroup = new LogicalRelGroup();
        for (LogicalRel thisRel : relsNonGroup0List) {
            // check role group number
            if (thisRel.group != prevRelGroup) {
                if (!thisRelGroup.isEmpty()) {
                    thisRelGroup.updateLogicalIds();
                    logicalRelGroupList.add(thisRelGroup);
                }
                thisRelGroup = new LogicalRelGroup();
                prevRelGroup = thisRel.group;
            }
            if (keepHistory) {
                thisRelGroup.add(thisRel);
            } else if (thisRel.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) == 0) {
                thisRelGroup.add(thisRel);
            }
        }
        if (!thisRelGroup.isEmpty()) {
            thisRelGroup.updateLogicalIds();
            logicalRelGroupList.add(thisRelGroup);
        }
        return logicalRelGroupList;
    }

    public ArrayList<LogicalRel> processRelGroups(ArrayList<LogicalRel> a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, IOException {
        ArrayList<LogicalRel> keepRels = new ArrayList<>();
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(a);

        // SPLIT RELS INTO CATEGORIES
        ArrayList<LogicalRel> relsSnomedReleaseList = new ArrayList<>();
        ArrayList<LogicalRel> relsUsReleaseList = new ArrayList<>();
        ArrayList<LogicalRel> relsViaKpList = new ArrayList<>();
        for (LogicalRel thisRel : a) {
            // must be a SNOMED rel with a SNOMED released sctid, otherwise skip
            if (thisRel.pathLastRevisionUuid.compareTo(snomedPathUuid) == 0
                    && thisRel.relSctIdPath != null
                    && thisRel.relSctIdPath.compareTo(snomedPathUuid) == 0
                    && isExtensionSctId(thisRel.relSctId) == false) {
                relsSnomedReleaseList.add(thisRel);
            } else if (thisRel.pathLastRevisionUuid.compareTo(
                    SnomedMetadataRf2.US_EXTENSION_PATH.getUuids()[0]) == 0) {
                relsUsReleaseList.add(thisRel);
            } else {
                relsViaKpList.add(thisRel);
            }
        }

        // most trivial case, rels all on one list
        if (!relsSnomedReleaseList.isEmpty()
                && relsUsReleaseList.isEmpty()
                && relsViaKpList.isEmpty()) {
            return relsSnomedReleaseList;
        }
        if (relsSnomedReleaseList.isEmpty()
                && !relsUsReleaseList.isEmpty()
                && relsViaKpList.isEmpty()) {
            return relsUsReleaseList;
        }
        if (relsSnomedReleaseList.isEmpty()
                && relsUsReleaseList.isEmpty()
                && !relsViaKpList.isEmpty()) {
            return relsViaKpList;
        }

        // CREATE REL GROUPS
        ArrayList<LogicalRelGroup> groupsSnomedReleaseList = convertToRelGroups(relsSnomedReleaseList, true);
        ArrayList<LogicalRelGroup> groupsUsReleaseList = convertToRelGroups(relsUsReleaseList, true);
        ArrayList<LogicalRelGroup> groupsViaKpList = convertToRelGroups(relsViaKpList, false);

        // HANDLE LOGICAL EQUIVALENT GROUPS
        // setup and check SCT hash set
        HashSet<UUID> groupsSnomedReleaseSet = new HashSet<>();
        int countNotRetired = 0;
        for (LogicalRelGroup lrg : groupsSnomedReleaseList) {
            if (!lrg.groupListStr.isEmpty()) {
                groupsSnomedReleaseSet.add(lrg.groupListStrHash);
                countNotRetired++;
            }
        }
        if (groupsSnomedReleaseSet.size() != countNotRetired) {
            reportGroupAdditionsWriter.append("\n######_# SCT CONTAINS LOGICALLY REDUNDANT ROLE GROUP\n");
            reportGroupAdditionsWriter.append(groupsSnomedReleaseList.get(0).logicalRels.get(0).tkr.c1Uuid.toString());
        }
        // setup and check US hash set
        HashSet<UUID> groupsUsReleaseSet = new HashSet<>();
        countNotRetired = 0;
        for (LogicalRelGroup lrg : groupsUsReleaseList) {
            if (!lrg.groupListStr.isEmpty()) {
                groupsUsReleaseSet.add(lrg.groupListStrHash);
                countNotRetired++;
            }
        }
        if (groupsUsReleaseSet.size() != countNotRetired) {
            reportGroupAdditionsWriter.append("\n######_# US CONTAINS LOGICALLY REDUNDANT ROLE GROUP\n");
            reportGroupAdditionsWriter.append(groupsUsReleaseList.get(0).logicalRels.get(0).tkr.c1Uuid.toString());
        }

        ArrayList<LogicalRelGroup> groupsViaKpUnMatchedList = new ArrayList<>();
        for (LogicalRelGroup groupKp : groupsViaKpList) {
            if (groupsSnomedReleaseSet.contains(groupKp.groupListStrHash)) {
                // System.out.println("matched SNOMED release group");
            } else if (groupsUsReleaseSet.contains(groupKp.groupListStrHash)) {
                // System.out.println("matched US release group");
            } else {
                groupsViaKpUnMatchedList.add(groupKp);
            }
        }
        // CASE: no role groups outside the release
        if (groupsViaKpUnMatchedList.isEmpty()) {
            for (LogicalRelGroup rg : groupsSnomedReleaseList) {
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
            }
            for (LogicalRelGroup rg : groupsUsReleaseList) {
                if (groupsSnomedReleaseSet.contains(rg.groupListStrHash)) {
                    reportGroupAdditionsWriter.append("\n######_# SCT/US CONTAINS LOGICALLY REDUNDANT ROLE GROUP \n");
                    for (LogicalRel r : rg.logicalRels) {
                        r.group += 80;
                        r.setGroup(r.group);
                    }
                    reportGroupAdditionsWriter.append(rg.toStringUser());
                }
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
            }
            return keepRels;
        }

        // >>>>>>>> WHAT DOES RESIDUAL CONTAIN ??? <<<<<<<<<<<<<
        UUID c1Uuid = groupsSnomedReleaseList.get(0).logicalRels.get(0).c1SnoId;
        reportListRoleGroupAdditionsWriter.append(c1Uuid.toString());
        reportListRoleGroupAdditionsWriter.append("\t");
        reportListRoleGroupAdditionsWriter.append(ts.getConcept(c1Uuid).toUserString());
        reportListRoleGroupAdditionsWriter.append("\n");
        reportGroupAdditionsWriter.append("\n###### SCT RELEASE ######\n");
        for (LogicalRelGroup rg : groupsSnomedReleaseList) {
            for (LogicalRel r : rg.logicalRels) {
                keepRels.add(r);
            }
            if (!rg.groupListStr.isEmpty()) {
                reportGroupAdditionsWriter.append(rg.toStringUser());
            }
        }
        if (groupsUsReleaseList.size() > 0) {
            reportGroupAdditionsWriter.append("\n... US RELEASE ...\n");
            for (LogicalRelGroup rg : groupsUsReleaseList) {
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
                if (!rg.groupListStr.isEmpty()) {
                    reportGroupAdditionsWriter.append(rg.toStringUser());
                }
            }
        }
        reportGroupAdditionsWriter.append("\n... KP role group without logical match in release ...\n");
        for (LogicalRelGroup rg : groupsViaKpUnMatchedList) {
            for (LogicalRel r : rg.logicalRels) {
                r.group += 90;
                r.setGroup(r.group);
                keepRels.add(r);
            }
            reportGroupAdditionsWriter.append(rg.toStringUser());
            ArrayList<LogicalRel> lRels = rg.logicalRels;
            for (LogicalRel logicalRel : lRels) {
                reportGroupAdditionsWriter.append(processRelFlavor(logicalRel).name());
                reportGroupAdditionsWriter.append("\n");
            }
        }
        return keepRels;
    }

    public String toStringReport() {
//        System.out.print(sbType0List.toString());
//        System.out.print(sbType2List.toString());
//        System.out.print(sbType3List.toString());
//        System.out.print(sbType0Verbose.toString());
//        System.out.print(sbType2Verbose.toString());
//        System.out.print(sbType3Verbose.toString());

        StringBuilder sb = new StringBuilder();
        sb.append(sbType0List.toString());
        sb.append(sbType2List.toString());
        sb.append(sbType3List.toString());
        sb.append(sbType0Verbose.toString());
        sb.append(sbType2Verbose.toString());
        sb.append(sbType3Verbose.toString());
        sb.append("\n");
        return sb.toString();
    }
}
