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
package org.ihtsdo.helper.descriptionlogic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.uuid.UuidT5Generator;

/**
 *
 * @author marc
 */
public class DescriptionLogic { // :SNOOWL:

    private static final String REFSET_ID_NAMESPACE_UUID_TYPE1 = "d0b3c9c0-e395-11df-bccf-0800200c9a66";
    // CONCEPT SPECS
    public static ConceptSpec DESCRIPTION_LOGIC_REFSET =
            new ConceptSpec("Description logic refset",
            genUuid("Description logic refset"));
    public static ConceptSpec DISJOINT_SETS_REFSET =
            new ConceptSpec("Disjoint sets refset",
            genUuid("Disjoint sets refset"));
    public static ConceptSpec NEGATION_REFSET =
            new ConceptSpec("Negation refset",
            genUuid("Negation refset"));
    public static ConceptSpec UNION_SETS_REFSET =
            new ConceptSpec("Union sets refset",
            genUuid("Union sets refset"));
    public static ConceptSpec CONDOR_REASONER =
            new ConceptSpec("ConDOR",
            genUuid("ConDOR Reasoner"));
    private static boolean isVisibleB = false;

    public DescriptionLogic() {
        isVisibleB = false;
    }

    public static boolean isPresent() {
        // Check for presence of both refset and attribute to be present
        UUID[] uuidsRefset = DESCRIPTION_LOGIC_REFSET.getUuids();
        if (uuidsRefset.length > 0 && Ts.get().hasUuid(uuidsRefset[0])) {
            return true;
        }

        return false;
    }

    public static boolean isVisible() {
        return isVisibleB;
    }

    public static void setVisible(boolean visible) {
        if (isPresent()) {
            isVisibleB = visible;
        } else {
            isVisibleB = false;
        }
    }

    private static UUID genUuid(String fsn) {
        try {
            // return Type5UuidFactory.get(REFSET_ID_NAMESPACE_UUID_TYPE1 + fsn);
            return UuidT5Generator.get(REFSET_ID_NAMESPACE_UUID_TYPE1 + fsn);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DescriptionLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DescriptionLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static int getDisjointSetsRefsetNid() throws IOException {
        return Ts.get().getNidForUuids(DISJOINT_SETS_REFSET.getUuids());
    }

    public static int getNegationRefsetNid() throws IOException {
        return Ts.get().getNidForUuids(NEGATION_REFSET.getUuids());
    }

    public static int getUnionSetsRefsetNid() throws IOException {
        return Ts.get().getNidForUuids(UNION_SETS_REFSET.getUuids());
    }

    public static boolean isNegatedRel(int relNid, ViewCoordinate vc) throws IOException {
//        if (relNid == -2145628237 || relNid == -2147479420 || relNid == -2143114520
//                || relNid == -2144096571 || relNid == -2144869271) {
//            System.out.println(":!!!:DEBUG: found relationship of interest");
//        }
        ComponentChroncileBI<?> component = Ts.get().getComponent(relNid);
        boolean isNegatedRel = false;
        try {
            Collection<? extends RefexChronicleBI> refexes = component.getCurrentRefexes(vc);
            int evalRefsetNid = getNegationRefsetNid();

            if (refexes != null) {
                for (RefexChronicleBI refex : refexes) {
                    if (refex.getCollectionNid() == evalRefsetNid) {
                        if (RefexVersionBI.class.isAssignableFrom(refex.getClass())) {
                            RefexVersionBI<?> rv = (RefexVersionBI<?>) refex;

                            if (RefexCnidVersionBI.class.isAssignableFrom(rv.getClass())) {
                                isNegatedRel = true;
                            } else {
                                System.out.println("Can't convert: RefexCnidVersionBI:  " + rv);
                            }
                        } else {
                            System.out.println("Can't convert: RefexVersionBI:  " + refex);
                        }
                    }
                }
            }
            return isNegatedRel;
        } catch (IOException e) {
            return isNegatedRel;

        }
    }

    public static UUID computeOrderedSetUuid(List<ConceptChronicleBI> concepts, String str)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {
        // UUID SORT ORDER -- order required for deterministic refset UUID
        Comparator<ConceptChronicleBI> comp = new Comparator<ConceptChronicleBI>() {
            @Override
            public int compare(ConceptChronicleBI o1, ConceptChronicleBI o2) {
                return o1.getPrimUuid().compareTo(o2.getPrimUuid());
            }
        };
        Collections.sort(concepts, comp);

        // :!!!:SNOOWL:NYI: reject non-unique UUIDS

        StringBuilder conceptsUuidStr = new StringBuilder(concepts.size() * (36 + 1));
        conceptsUuidStr.append(str);
        for (ConceptChronicleBI ccbi : concepts) {
            conceptsUuidStr.append(ccbi.getPrimUuid().toString()).append("|");
        }

        UUID uuid = UuidT5Generator.get(conceptsUuidStr.toString());

        return uuid;
    }
}
