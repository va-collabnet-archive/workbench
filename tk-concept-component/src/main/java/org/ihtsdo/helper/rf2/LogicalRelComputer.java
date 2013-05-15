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

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author code
 */
public class LogicalRelComputer {

    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";

    public static ArrayList<LogicalRel> addLogicalUuidsWithSort(ArrayList<LogicalRel> a)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // SORT BY [C1-Group-RoleType-C2]
        Collections.sort(a);

        UUID lastC1 = a.get(0).c1SnoId;
        int lastGroup = a.get(0).group;
        String GroupListStr = getGroupListString(a, 0);
        int max = a.size();
        for (int i = 0; i < max; i++) {
            // DETERMINE IF NEW GroupListStr IS NEEDED
            if (lastC1.compareTo(a.get(i).c1SnoId) != 0
                    || lastGroup != a.get(i).group) {
                GroupListStr = getGroupListString(a, i);
            }

            // SET RELATIONSHIP UUID
            UUID uuidComputed = UuidT5Generator.get(REL_ID_NAMESPACE_UUID_TYPE1
                    + a.get(i).c1SnoId
                    + a.get(i).roleTypeSnoId
                    + a.get(i).c2SnoId
                    + GroupListStr);

            a.get(i).logicalRelUuid = uuidComputed;

            lastC1 = a.get(i).c1SnoId;
            lastGroup = a.get(i).group;
        }

        LogicalRel.sortByLogicalRelUuid(a);

        return a;
    }

    private static String getGroupListString(ArrayList<LogicalRel> a, int startIdx) {
        StringBuilder sb = new StringBuilder();

        int max = a.size();
        if (a.get(startIdx).group > 0) {
            UUID keepC1 = a.get(startIdx).c1SnoId;
            int keepGroup = a.get(startIdx).group;
            int i = startIdx;
            while ((i < max - 1)
                    && (a.get(i).c1SnoId.compareTo(keepC1) == 0)
                    && (a.get(i).group == keepGroup)) {
                sb.append(a.get(i).c1SnoId).append("|");
                sb.append(a.get(i).roleTypeSnoId).append("|");
                sb.append(a.get(i).c2SnoId).append(";");
                i++;
            }
        }
        return sb.toString();
    }
}
