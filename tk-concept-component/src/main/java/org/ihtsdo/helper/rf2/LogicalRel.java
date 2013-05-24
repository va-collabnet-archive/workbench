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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationshipRevision;

/**
 *
 * @author code
 */
public class LogicalRel implements Comparable<LogicalRel> {

    public UUID logicalRelUuid;
    public long relSctId;
    public long relSctIdTime;
    public UUID relSctIdPath;
    public UUID c1SnoId;
    public UUID c2SnoId;
    public UUID roleTypeSnoId;
    public UUID pathUuid;
    public UUID statusUuid;
    public int group;
    public long time;
    public final TkRelationship tkr;
    //
    private final static UUID SNOMED_CORE_PATH_UUID = UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2");
    private final static UUID SNOMED_INT_RF1_UUID = UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9");
    private final static UUID SNOMED_INT_RF2_UUID = UUID.fromString("87360947-e603-3397-804b-efd0fcc509b9");
    private final static UUID SNOMED_RF2_ACTIVE_UUID = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];

    public LogicalRel(TkRelationship tkr) {
        this.tkr = tkr;
        this.logicalRelUuid = null; // to be assigned later
        this.c1SnoId = tkr.c1Uuid; // not versioned
        this.c2SnoId = tkr.c2Uuid; // not versioned

        List<TkRelationshipRevision> revisions = tkr.revisions;
        if (revisions == null) {
            this.roleTypeSnoId = tkr.typeUuid;
            this.group = tkr.relGroup;
            this.statusUuid = tkr.statusUuid;
            this.time = tkr.time;
            this.pathUuid = tkr.getPathUuid();
        } else {
            int i = 0;
            TkRelationshipRevision rev = revisions.get(i);
            this.roleTypeSnoId = rev.typeUuid;
            this.group = rev.group;
            this.statusUuid = rev.statusUuid;
            this.time = rev.time;
            this.pathUuid = rev.getPathUuid();
            for (; i < revisions.size(); i++) {
                // get the most recent snapshot
                rev = revisions.get(i);
                if (rev.time > this.time) {
                    this.roleTypeSnoId = rev.typeUuid;
                    this.group = rev.group;
                    this.statusUuid = rev.statusUuid;
                    this.time = rev.time;
                    this.pathUuid = rev.getPathUuid();
                }
            }
        }

        List<TkIdentifier> ids = tkr.additionalIds;
        if (ids != null) {
            for (TkIdentifier tki : ids) {
                int longIdCount = 0;
                if (tki.authorityUuid.compareTo(SNOMED_INT_RF1_UUID) == 0
                        || tki.authorityUuid.compareTo(SNOMED_INT_RF2_UUID) == 0) {
                    relSctId = ((Long) tki.getDenotation());
                    relSctIdTime = tki.time;
                    relSctIdPath = tki.pathUuid;
                    longIdCount++;
                    if (longIdCount > 1) {
                        throw new UnsupportedOperationException(":!!!: multiple or versioned sctids not expected");
                    }
                }
            }
        }
    }

    @Override
    public int compareTo(LogicalRel o2) {
        int thisMore = 1;
        int thisLess = -1;
        // C1
        if (this.c1SnoId.compareTo(o2.c1SnoId) > 0) {
            return thisMore;
        } else if (this.c1SnoId.compareTo(o2.c1SnoId) < 0) {
            return thisLess;
        } else {
            // GROUP
            if (this.group > o2.group) {
                return thisMore;
            } else if (this.group < o2.group) {
                return thisLess;
            } else {
                // ROLE TYPE
                if (this.roleTypeSnoId.compareTo(o2.roleTypeSnoId) > 0) {
                    return thisMore;
                } else if (this.roleTypeSnoId.compareTo(o2.roleTypeSnoId) < 0) {
                    return thisLess;
                } else {
                    // C2
                    if (this.c2SnoId.compareTo(o2.c2SnoId) > 0) {
                        return thisMore;
                    } else if (this.c2SnoId.compareTo(o2.c2SnoId) < 0) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }

    /**
     *
     * @param a
     */
    public static void sortByLogicalRelUuid(ArrayList<LogicalRel> a) {

        Comparator<LogicalRel> byLogicalRelUuid = new Comparator<LogicalRel>() {
            @Override
            public int compare(LogicalRel o1, LogicalRel o2) {
                int more = 1;
                int less = -1;
                // relationship assigned sct id
                if (o1.logicalRelUuid.compareTo(o2.logicalRelUuid) > 0) {
                    return more;
                } else if (o1.logicalRelUuid.compareTo(o2.logicalRelUuid) > 0) {
                    return less;
                } else {
                    return 0; // equal
                }
            }
        };
        Collections.sort(a, byLogicalRelUuid);
    }
}
