/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.descriptionlogic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 *
 * @author marc
 */
public class DescriptionLogic {

    private static final String REFSET_ID_NAMESPACE_UUID_TYPE1 = "d0b3c9c0-e395-11df-bccf-0800200c9a66";
    // CONCEPT SPECS
    public static ConceptSpec DESCRIPTION_LOGIC_REFSET =
            new ConceptSpec("Description Logic Refset",
            genUuid("Description Logic Refset"));
    public static ConceptSpec DISJUNTION =
            new ConceptSpec("Disjunction",
            genUuid("Disjunction Refset"));
    public static ConceptSpec NEGATION =
            new ConceptSpec("Negation",
            genUuid("Negation Refset"));

    private static UUID genUuid(String fsn) {
        try {
            return Type5UuidFactory.get(REFSET_ID_NAMESPACE_UUID_TYPE1 + fsn);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DescriptionLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(DescriptionLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static int getDisjunctionRefsetNid() throws IOException {
        return Ts.get().getNidForUuids(DISJUNTION.getUuids());
    }

    public static int getNegationRefsetNid() throws IOException {
        return Ts.get().getNidForUuids(NEGATION.getUuids());
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

    public static boolean isNegatedRel(I_RelTuple rel, ViewCoordinate vc) {
        boolean isNegatedRel = false;
        try {
            Collection<? extends RefexChronicleBI> refexes = rel.getCurrentRefexes(vc);
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
}
