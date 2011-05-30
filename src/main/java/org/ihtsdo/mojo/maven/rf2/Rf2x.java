/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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

import java.io.IOException;
import java.util.UUID;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;


public class Rf2x {

    static String convertEffectiveTimeToDate(String date) {
        return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8)
                + " 00:00:00";
    }

    static boolean convertStringToBoolean(String s) {
        if (s.startsWith("1")) {
            return true;
        } else {
            return false;
        }
    }

    static String convertActiveToStatusUuid(boolean active) throws IOException, TerminologyException {
        // RF1 "0" current, "1" noncurrent
        // RF2 "0" inactive, "1" active
        // ARF Status UUID <-- most general
        if (active) {
            return ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid().toString();
        } else {
            return ArchitectonicAuxiliary.Concept.RETIRED.getPrimoridalUid().toString();
        }
    }

    static boolean convertDefinitionStatusToIsPrimitive(String defStatus) {
        // RF1 "0" Fully defined, "1" Primitive
        // RF2 Set to a child of |Definition status| in the metadata hierarchy.
        //     900000000000074008 == primitive
        // ARF  boolean string 0 (false == defined) or 1 (true == primitive)
        if (defStatus.equalsIgnoreCase("900000000000074008")) {
            return true;
        } else {
            return false;
        }
    }

    static String convertIdToUuidStr(String idStr) {
        long id = Long.parseLong(idStr);
        return Type3UuidFactory.fromSNOMED(id).toString();
    }

    static String convertIdToUuidStr(long id) {
        return Type3UuidFactory.fromSNOMED(id).toString();
    }

    static boolean convertCaseSignificanceIdToCapStatus(String caseSignifcanceId) {
        // Case Significant RF2==900000000000017005, RF1=="1" or true
        // Case Not Significant RF2==900000000000020002, RF1=="0" or false
        if (caseSignifcanceId.equalsIgnoreCase("900000000000017005")) {
            return true;
        } else {
            return false;
        }
    }
}
