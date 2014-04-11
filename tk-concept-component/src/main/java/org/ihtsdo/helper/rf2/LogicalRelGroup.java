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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author Marc Campbell
 */
public class LogicalRelGroup {

    private static final String REL_ID_NAMESPACE_UUID_TYPE1 = "84fd0460-2270-11df-8a39-0800200c9a66";
    private final static UUID SNOMED_RF2_ACTIVE_UUID = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
    String groupListStr;
    UUID groupListStrHash;
    public ArrayList<LogicalRel> logicalRels;

    LogicalRelGroup() {
        logicalRels = new ArrayList<>();
    }

    public void add(LogicalRel thisRel) {
        logicalRels.add(thisRel);
    }

    public boolean isEmpty() {
        return logicalRels.isEmpty();
    }

    public int size() {
        return logicalRels.size();
    }

    void updateLogicalIds() 
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Collections.sort(logicalRels);
        StringBuilder sb = new StringBuilder();
        for (LogicalRel r : logicalRels) {
            if (r.statusUuid.compareTo(SNOMED_RF2_ACTIVE_UUID) == 0) {
                sb.append(r.c1SnoId).append("|");
                sb.append(r.typeSnoId).append("|");
                sb.append(r.c2SnoId).append(";");
            }
        }
        groupListStr = sb.toString();

        // SET RELATIONSHIP LOGICAL UUID
        for (LogicalRel r : logicalRels) {
            UUID uuidComputed = UuidT5Generator.get(REL_ID_NAMESPACE_UUID_TYPE1
                    + r.c1SnoId
                    + r.typeSnoId
                    + r.c2SnoId
                    + groupListStr);

            r.logicalRelUuid = uuidComputed;
        }

        groupListStrHash = UuidT5Generator.get(REL_ID_NAMESPACE_UUID_TYPE1 + groupListStr);
    }
    
    public String toStringUser() {
        TerminologyStoreDI ts = Ts.get();
        
        StringBuilder sb = new StringBuilder();
        
        for (LogicalRel r : logicalRels) {
            if (r.statusUuid.compareTo(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0]) != 0) {
                continue;
            }
            sb.append(r.c1SnoId.toString());
            sb.append(" :: ");
            sb.append(r.typeSnoId.toString());
            sb.append(" :: ");
            sb.append(r.c2SnoId.toString());
            sb.append(" :: (");
            sb.append(r.group);
            sb.append(") || ");
            try {
                sb.append(ts.getConcept(r.c1SnoId).toUserString());
            } catch (IOException ex) {
                sb.append("ERROR");
            }
            sb.append(" || ");
            try {
                sb.append(ts.getConcept(r.typeSnoId).toUserString());
            } catch (IOException ex) {
                sb.append("ERROR");
            }
            sb.append(" || ");
            try {
                sb.append(ts.getConcept(r.c2SnoId).toUserString());
            } catch (IOException ex) {
                sb.append("ERROR");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

}
