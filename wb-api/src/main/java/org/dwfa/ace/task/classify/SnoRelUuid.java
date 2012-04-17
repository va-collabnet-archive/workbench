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

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;

/**
 *
 * @author marc
 */
public class SnoRelUuid implements Comparable<SnoRelUuid> {

    public UUID c1;
    public UUID role;
    public UUID c2;
    public int group;
    private static final TerminologyStoreDI ts = Ts.get();

    public SnoRelUuid(SnoRel sr) throws Exception {
        try {
            this.c1 = ts.getUuidPrimordialForNid(sr.c1Id);
            this.role = ts.getUuidPrimordialForNid(sr.typeId);
            this.c2 = ts.getUuidPrimordialForNid(sr.c2Id);
            this.group = sr.group;
        } catch (IOException ex) {
            StringBuilder sb = new StringBuilder("Primorial UUID not found c1: ");
            sb.append(this.c1);
            sb.append(" role: ");
            sb.append(this.role);
            sb.append(" c2: ");
            sb.append(this.c2);
            Logger.getLogger(SnorocketExTask.class.getName()).log(
                    Level.SEVERE, sb.toString(), ex);
            throw new Exception("Primorial UUID not found");
        }
    }

    @Override
    public int compareTo(SnoRelUuid o2) {
        int thisMore = 1;
        int thisLess = -1;
        // C1
        if (this.c1.compareTo(o2.c1) > 0) {
            return thisMore;
        } else if (this.c1.compareTo(o2.c1) < 0) {
            return thisLess;
        } else {
            // GROUP
            if (this.group > o2.group) {
                return thisMore;
            } else if (this.group < o2.group) {
                return thisLess;
            } else {
                // ROLE TYPE
                if (this.role.compareTo(o2.role) > 0) {
                    return thisMore;
                } else if (this.role.compareTo(o2.role) < 0) {
                    return thisLess;
                } else {
                    // C2
                    if (this.c2.compareTo(o2.c2) > 0) {
                        return thisMore;
                    } else if (this.c2.compareTo(o2.c2) < 0) {
                        return thisLess;
                    } else {
                        return 0; // EQUAL
                    }
                }
            }
        }
    }
}
