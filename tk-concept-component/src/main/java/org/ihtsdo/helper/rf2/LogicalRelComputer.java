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

    public LogicalRelComputer(BufferedWriter reportGroupAdditionsWriter, BufferedWriter reportListRoleGroupAdditionsWriter) {
        this.ts = Ts.get();
        this.reportItemList = new ArrayList<>();
        this.reportGroupAdditionsWriter = reportGroupAdditionsWriter;
        this.reportListRoleGroupAdditionsWriter = reportListRoleGroupAdditionsWriter;
    }

    private enum RelFlavor {

        REL_FROM_SNOMED,
        REL_FROM_SNOMED_WITH_LEGACY_FILLER_SCTID,
        REL_FROM_EXTENSION,
        REL_FROM_ECCS;
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
        } else {
            return RelFlavor.REL_FROM_EXTENSION;
        }
    }

    // exceptions list:  SCTID or UUID <tab> short name <\n>
    public ArrayList<LogicalRel> processRelsGroup0(ArrayList<LogicalRel> a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
            ArrayList<LogicalRel> keepList) {
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
                    tempList.add(logicalRel);
                    break;
            }
        }

        if (tempList.isEmpty()) {
            System.out.printf("\n>>>>> :LogicalRelComputer: filter tempList == 0 :: %s equivList size = %s <<<<<",
                    equivalentRelList.get(0).c1SnoId.toString(),
                    equivalentRelList.size());
            System.out.print(processRelFlavor(equivalentRelList.get(0)).toString());
            System.out.printf(" #### %s\n", equivalentRelList.get(0).tkr.toString());
            keepList.addAll(tempList);
        } else if (tempList.size() == 1) {
            keepList.addAll(tempList);
        } else if (tempList.size() == 2) {
            System.out.printf("\n>>>>> :LogicalRelComputer: filter tempList == %d :: %s ", 
                    tempList.size(),
                    equivalentRelList.get(0).c1SnoId.toString());
            for (LogicalRel logicalRel : tempList) {
                System.out.printf("####  c1 : %s", logicalRel.c1SnoId);
                System.out.printf(" type : %s", logicalRel.typeSnoId);
                System.out.printf(" c2 : %s", logicalRel.c2SnoId);
                System.out.printf(" sctid : %s ", logicalRel.relSctId);
                System.out.print(processRelFlavor(logicalRel));
                System.out.printf(" <<<<< %s ", logicalRel.tkr.toString());
            }
            System.out.printf("\n");
            keepList.addAll(tempList);
        } else { // tempList.size() > 2
            System.out.printf("\n>>>>> :LogicalRelComputer: filter tempList == %d :: %s ", 
                    tempList.size(),
                    equivalentRelList.get(0).c1SnoId.toString());
            for (LogicalRel logicalRel : tempList) {
                System.out.printf(">>>>>  c1 : %s", logicalRel.c1SnoId);
                System.out.printf(" type : %s", logicalRel.typeSnoId);
                System.out.printf(" c2 : %s", logicalRel.c2SnoId);
                System.out.printf(" sctid : %s ", logicalRel.relSctId);
                System.out.print(processRelFlavor(logicalRel));
                System.out.printf(" <<<<< %s ", logicalRel.tkr.toString());
            }
            System.out.printf("\n");
            keepList.addAll(tempList);
        }
    }

    // exceptions list:  SCTID or UUID <tab> short name <\n>
    public ArrayList<LogicalRel> processRelsGroup0Was(ArrayList<LogicalRel> a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        ArrayList<LogicalRel> keepList = new ArrayList<>();
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(a);

        LogicalRel relSnomed = null;
        LogicalRel relExtension = null;
        LogicalRel relEccs = null;

        for (int i = 0; i < a.size(); i++) {
            LogicalRel thisRel = a.get(i);

            if (thisRel.relSctIdPath != null
                    && thisRel.relSctIdPath.compareTo(snomedPathUuid) != 0
                    && thisRel.pathLastRevisionUuid.compareTo(snomedPathUuid) == 0) {
                // disregard snomed relationships to which KPs only change is to add an id 
            } else if (thisRel.relSctIdPath != null
                    && thisRel.relSctIdPath.compareTo(snomedPathUuid) == 0) {
                if (relSnomed == null) {
                    relSnomed = thisRel;
                } else {
                    addReportItem(relSnomed, relExtension, relEccs, "processGroup0Rels snomedPathUuid");
                }
            } else if (thisRel.relSctIdPath != null
                    && thisRel.relSctIdPath.compareTo(extensionPathUuid) == 0) {
                if (relExtension == null) {
                    relExtension = thisRel;
                } else {
                    addReportItem(relSnomed, relExtension, relEccs, "processGroup0Rels extensionPathUuid");
                }
            } else {
                if (relEccs == null) {
                    relEccs = thisRel;
                } else {
                    addReportItem(relSnomed, relExtension, relEccs, "processGroup0Rels development");
                }
            }

            // 
            boolean doKeepCheck = false;
            if (i == a.size() - 1) {
                doKeepCheck = true;
            } else {
                LogicalRel nextRel = a.get(i + 1);
                if (thisRel.c2SnoId.compareTo(nextRel.c2SnoId) != 0
                        || thisRel.typeSnoId.compareTo(nextRel.typeSnoId) != 0) {
                    doKeepCheck = true;
                }
            }

            // 
            if (doKeepCheck) {
                if (relSnomed == null && relExtension == null && relEccs == null) {
                    // nothing to keep
                } else if (relSnomed != null && relExtension == null && relEccs == null) {
                    keepList.add(relSnomed);
                } else if (relSnomed == null && relExtension != null && relEccs == null) {
                    keepList.add(relExtension);
                } else if (relSnomed == null && relExtension == null && relEccs != null) {
                    keepList.add(relEccs);
                } else if (relSnomed != null && relExtension == null && relEccs != null) {
                    if (isSnomedCloseEnough(relSnomed, relEccs)) {
                        keepList.add(relSnomed);
                    } else if (isEccsNonConflictingRevision(relSnomed, relEccs)) {
                        keepList.add(relEccs);
                    } else {
                        addReportItem(relSnomed, relExtension, relEccs, "not isSnomedCloseEnough, isEccsNonConflictingRevision");
                    }
                } else if (relSnomed != null && relExtension != null && relEccs == null) {
                    if (isSnomedCloseEnough(relSnomed, relExtension)) {
                        keepList.add(relSnomed);
                    } else {
                        addReportItem(relSnomed, relExtension, relEccs, "not isSnomedCloseEnough() Extension");
                    }
                } else {
                    addReportItem(relSnomed, relExtension, relEccs, "doKeepCheck");
                }

                relSnomed = null;
                relExtension = null;
                relEccs = null;
            }
        }

        if (keepList.isEmpty()) {
            System.out.println(":WARNING: empty stated rels list :: " + a.get(0).c1SnoId);
        }
        return keepList;
    }

    /**
     * does the snomedRel match all critical files where b is redundant
     */
    private boolean isSnomedCloseEnough(LogicalRel snomedRel, LogicalRel b) {
        if (snomedRel.statusUuid.compareTo(b.statusUuid) == 0
                && snomedRel.refinabilityUuid.compareTo(b.refinabilityUuid) == 0) {
            return true;
        }
        return false;
    }

    /**
     * is eccsRel a non-conflicting revision? if yes, keep eccsRel with
     * snomedIdentifier
     */
    private boolean isEccsNonConflictingRevision(LogicalRel snomedRel, LogicalRel eccsRel) {
        if (snomedRel.tkr.revisions == null
                && eccsRel.tkr.revisions != null
                && snomedRel.tkr.typeUuid.compareTo(eccsRel.tkr.typeUuid) == 0
                && snomedRel.tkr.refinabilityUuid.compareTo(eccsRel.tkr.refinabilityUuid) == 0
                && eccsRel.tkr.pathUuid.compareTo(snomedPathUuid) == 0
                && eccsRel.time > snomedRel.time) {

            eccsRel.relSctId = snomedRel.relSctId;
            eccsRel.relSctIdPath = snomedRel.relSctIdPath;
            eccsRel.relSctIdTime = snomedRel.relSctIdTime;
            snomedRel.tkr.revisions = eccsRel.tkr.revisions; // move over revisions
            eccsRel.tkr = snomedRel.tkr;
            return true;
        }
        return false;
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
        if (nameSpaceIdentifier.equalsIgnoreCase("1000119")) {
            // :NYI: "1000119" needs to be generalized for broader applications
            return true;
        }
        return false;
    }

    private ArrayList<LogicalRelGroup> convertToRelGroups(ArrayList<LogicalRel> a, boolean keepHistory)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        ArrayList<LogicalRelGroup> logicalRelGroupList = new ArrayList<>();
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(a);

        int prevRelGroup = -1;
        LogicalRelGroup thisRelGroup = null;
        for (LogicalRel thisRel : a) {
            // check role group number
            if (thisRel.group != prevRelGroup) {
                if (thisRelGroup != null) {
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
        if (thisRelGroup != null && !thisRelGroup.isEmpty()) {
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
        ArrayList<LogicalRel> relsViaKpList = new ArrayList<>();
        for (LogicalRel thisRel : a) {
            // must be a SNOMED rel with a SNOMED released sctid, otherwise skip
            if (thisRel.pathLastRevisionUuid.compareTo(snomedPathUuid) == 0
                    && thisRel.relSctIdPath != null
                    && thisRel.relSctIdPath.compareTo(snomedPathUuid) == 0
                    && isExtensionSctId(thisRel.relSctId) == false) {
                relsSnomedReleaseList.add(thisRel);
            } else {
                relsViaKpList.add(thisRel);
            }
        }

        // most trivial case, rels all on one side
        if (!relsSnomedReleaseList.isEmpty() && relsViaKpList.isEmpty()) {
            return relsSnomedReleaseList;
        }
        if (relsSnomedReleaseList.isEmpty() && !relsViaKpList.isEmpty()) {
            return relsViaKpList;
        }

        // CREATE REL GROUPS
        ArrayList<LogicalRelGroup> groupsSnomedReleaseList = convertToRelGroups(relsSnomedReleaseList, true);
        ArrayList<LogicalRelGroup> groupsViaKpList = convertToRelGroups(relsViaKpList, false);

        // trivial case, groups all on one side
        if (!groupsSnomedReleaseList.isEmpty() && groupsViaKpList.isEmpty()) {
            for (LogicalRelGroup rg : groupsSnomedReleaseList) {
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
            }
            return keepRels;
        }
        if (groupsSnomedReleaseList.isEmpty() && !groupsViaKpList.isEmpty()) {
            for (LogicalRelGroup rg : groupsViaKpList) {
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
            }
            return keepRels;
        }

        // HANDLE LOGICAL EQUIVALENT GROUPS
        HashSet<UUID> groupsSnomedReleaseSet = new HashSet<>();
        for (LogicalRelGroup logicalRelGroup : groupsSnomedReleaseList) {
            groupsSnomedReleaseSet.add(logicalRelGroup.groupListStrHash);
        }

        ArrayList<LogicalRelGroup> groupsViaKpUnMatchedList = new ArrayList<>();
        for (LogicalRelGroup groupKp : groupsViaKpList) {
            if (groupsSnomedReleaseSet.contains(groupKp.groupListStrHash)) {
                // System.out.println("matched group");
            } else {
                groupsViaKpUnMatchedList.add(groupKp);
            }
        }
        // easy case, everything matched
        if (groupsViaKpUnMatchedList.isEmpty()) {
            for (LogicalRelGroup rg : groupsSnomedReleaseList) {
                for (LogicalRel r : rg.logicalRels) {
                    keepRels.add(r);
                }
            }
            return keepRels;
        }

        // >>>>>>>> DOES RESIDUAL CONTAIN ??? <<<<<<<<<<<<<
        UUID c1Uuid = groupsSnomedReleaseList.get(0).logicalRels.get(0).c1SnoId;
        reportListRoleGroupAdditionsWriter.append(c1Uuid.toString());
        reportListRoleGroupAdditionsWriter.append("\t");
        reportListRoleGroupAdditionsWriter.append(ts.getConcept(c1Uuid).toUserString());
        reportListRoleGroupAdditionsWriter.append("\n");
        reportGroupAdditionsWriter.append("\n############################\n");
        for (LogicalRelGroup rg : groupsSnomedReleaseList) {
            for (LogicalRel r : rg.logicalRels) {
                keepRels.add(r);
            }
            reportGroupAdditionsWriter.append(rg.toStringUser());
        }
        reportGroupAdditionsWriter.append("\n......\n");
        for (LogicalRelGroup rg : groupsViaKpUnMatchedList) {
            for (LogicalRel r : rg.logicalRels) {
                r.group += 90;
                r.setGroup(r.group);
                keepRels.add(r);
            }
            reportGroupAdditionsWriter.append(rg.toStringUser());
        }
        return keepRels;
    }
    private ArrayList<ReportItem> reportItemList;

    public String toStringReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("#############################\n");
        sb.append("# LOGICAL RELATIONSHIP REPORT\n");
        sb.append("#############################\n");
        for (ReportItem reportItem : reportItemList) {
            sb.append(reportItem.comment);
            sb.append("\n");
            if (reportItem.relSnomed != null) {
                sb.append("# snomed core # \n");
                sb.append(reportItem.relSnomed.tkr.toString());
                sb.append("\n");
            }
            if (reportItem.relExtension != null) {
                sb.append("# extension # \n");
                sb.append(reportItem.relExtension.tkr.toString());
                sb.append("\n");
            }
            if (reportItem.relEccs != null) {
                sb.append("# change set # \n");
                sb.append(reportItem.relEccs.tkr.toString());
                sb.append("\n");
            }
            sb.append("#########\n");
        }
        return sb.toString();
    }

    public String toStringReportList() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ReportItem reportItem : reportItemList) {
            UUID c1Uuid;
            if (reportItem.relSnomed != null) {
                c1Uuid = reportItem.relSnomed.tkr.c1Uuid;
            } else if (reportItem.relExtension != null) {
                c1Uuid = reportItem.relExtension.tkr.c1Uuid;
            } else {
                c1Uuid = reportItem.relEccs.tkr.c1Uuid;
            }
            sb.append(c1Uuid.toString());
            sb.append("\t");
            sb.append(Ts.get().getConcept(c1Uuid).toUserString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private void addReportItem(LogicalRel relSnomed,
            LogicalRel relExtension,
            LogicalRel relEccs,
            String comment) {
        ReportItem item;
        item = new ReportItem(relSnomed, relExtension, relEccs, comment);
        reportItemList.add(item);
    }

    private class ReportItem {

        LogicalRel relSnomed;
        LogicalRel relExtension;
        LogicalRel relEccs;
        String comment;

        public ReportItem(LogicalRel relSnomed, LogicalRel relExtension, LogicalRel relEccs, String comment) {
            this.relSnomed = relSnomed;
            this.relExtension = relExtension;
            this.relEccs = relEccs;
            this.comment = comment;
        }
    }
}
