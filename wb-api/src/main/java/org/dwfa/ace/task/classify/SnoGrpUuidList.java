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
import java.util.HashSet;

/**
 *
 * @author marc
 */
public class SnoGrpUuidList extends ArrayList<SnoGrpUuid> {

    private static final long serialVersionUID = 1L;

    public SnoGrpUuidList(SnoGrpList sgl) throws Exception {
        super();

        for (SnoGrp sg : sgl) {
            SnoGrpUuid sgu = new SnoGrpUuid(sg);
            this.add(sgu);
        }
    }

    public void calcNewRoleGroupNumbers(HashSet<Integer> inUse) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        int offset = 0;
        // HashSet<Integer> numInUse = (HashSet<Integer>) inUse.clone();
        HashSet<Integer> numInThisConcept = new HashSet<Integer>();

        boolean notDone = true;
        while (notDone) {
            // UPDATE GROUP NUMBER FOR EACH ROLE GROUP
            for (SnoGrpUuid sgu : this) {
                sgu.setRoleGroupNumber(sgu.calcRoleGroupNumber(offset));
            }

            // verify that role groups numbers do not overlap
            boolean ok = true;
            for (SnoGrpUuid sgu : this) {
                if (sgu.get(0).group < 10) {
                    // 0-9 not allowed as a computed logical role group number
                    ok = false;
                    break;
                }
                if (numInThisConcept.add(sgu.get(0).group) == false) {
                    // no change to set
                    ok = false;
                    break;
                }
            }

            if (ok == false) {
                offset++;
                // numInUse = (HashSet<Integer>) inUse.clone();
                numInThisConcept = new HashSet<Integer>();
                // :!!!: change to logger
                // logger.log(Level.INFO, "calcNewRoleGroupNumbers offset={0}", offset);
                System.out.println("calcNewRoleGroupNumbers offset=" + offset);
            } else {
                notDone = false;
            }
        }
    }
}
