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
package org.ihtsdo.mojo.maven.rf2;

import org.ihtsdo.helper.rf2.UuidUuidRecord;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;

/**
 *
 * @author marc
 */
public class SctRelLogicalUuidComputer {

    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";

    public static void computeRelationshipUuids() {
    }

    /**
     * requires that Sct1_RelRecord relSctId value be present
     *
     * @param a
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static ArrayList<UuidUuidRecord> createSctUuidToLogicalUuidList(Sct2_RelLogicalRecord[] a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        ArrayList<UuidUuidRecord> uuidUuidList = new ArrayList<>();

        // SORT BY [C1-Group-RoleType-C2]
        Arrays.sort(a);

        long lastC1 = a[0].c1SnoId;
        int lastGroup = a[0].group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.length;
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1 != a[i].c1SnoId
                    || lastGroup != a[i].group) {
                GroupListStr = getGroupListString(a, i);
            }

            // SET RELATIONSHIP UUID
            UUID uuidComputed = Type5UuidFactory.get(REL_ID_NAMESPACE_UUID_TYPE1 + a[i].c1SnoId
                    + a[i].roleTypeSnoId + a[i].c2SnoId + GroupListStr);
            
            UUID uuidFromSctId = UUID.fromString(Type3UuidFactory.fromSNOMED(a[i].relSctId).toString());
            
            uuidUuidList.add(new UuidUuidRecord(uuidComputed, uuidFromSctId));

            lastC1 = a[i].c1SnoId;
            lastGroup = a[i].group;
        }
        return uuidUuidList;
    }

    private static String getGroupListString(Sct2_RelLogicalRecord[] a, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int max = a.length;
        if (a[startIdx].group > 0) {
            long keepC1 = a[startIdx].c1SnoId;
            int keepGroup = a[startIdx].group;
            int i = startIdx;
            while ((i < max - 1) && (a[i].c1SnoId == keepC1) && (a[i].group == keepGroup)) {
                sb.append(a[i].c1SnoId).append("-");
                sb.append(a[i].roleTypeSnoId).append("-");
                sb.append(a[i].c2SnoId).append(";");
                i++;
            }
        }
        return sb.toString();
    }

}
