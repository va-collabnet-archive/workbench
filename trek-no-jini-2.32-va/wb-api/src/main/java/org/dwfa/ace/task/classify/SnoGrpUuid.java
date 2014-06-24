/*
 * Copyright 2012 International Health Terminology Standards Development Organisation.
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
package org.dwfa.ace.task.classify;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author marc
 */
public class SnoGrpUuid extends ArrayList<SnoRelUuid> {

    private static final long serialVersionUID = 1L;

    public SnoGrpUuid(SnoGrp sg) throws Exception {
        super();
        for (SnoRel snoRel : sg) {
            this.add(new SnoRelUuid(snoRel));
        }
    }

    int calcRoleGroupNumber(int offset)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        Collections.sort(this);
        // Type5UuidFactory.get(toStringLogicalGroup(offset));
        UUID uuid = UuidT5Generator.get(toStringLogicalGroup(offset));
        for (SnoRelUuid snoRelUuid : this) {
            snoRelUuid.group = uuid.hashCode();
        }
        return Math.abs(uuid.hashCode());
    }

    void setRoleGroupNumber(int groupNum) {
        for (SnoRelUuid snoRelUuid : this) {
            snoRelUuid.group = groupNum;
        }
    }

    String toStringLogicalGroup() {
        return toStringLogicalGroup(0);
    }

    String toStringLogicalGroup(int offset) {

        StringBuilder sb = new StringBuilder();
        // REL_ROLE_GROUP_NUMSPACE_UUID_TYPE1 = "5c31afa0-76af-11e1-b0c4-0800200c9a66";
        sb.append("5c31afa0-76af-11e1-b0c4-0800200c9a66::");
        for (SnoRelUuid sru : this) {
            sb.append(sru.c1.toString());
            sb.append(":");
            sb.append(String.valueOf(offset));
            sb.append(":");
            sb.append(sru.role.toString());
            sb.append(":");
            sb.append(String.valueOf(offset));
            sb.append(":");
            sb.append(sru.c2.toString());
        }

        return sb.toString();
    }
}
