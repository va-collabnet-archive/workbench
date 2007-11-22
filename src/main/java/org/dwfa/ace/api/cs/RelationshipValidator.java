package org.dwfa.ace.api.cs;

import java.io.IOException;
import java.util.List;

import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalAceRelationship;
import org.dwfa.ace.utypes.UniversalAceRelationshipPart;
import org.dwfa.tapi.TerminologyException;

public class RelationshipValidator extends SimpleValidator {

    I_TermFactory termFactory;
    @Override
    protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
        throws IOException, TerminologyException {
        /*
         * The universal bean relationships must be converted and compared with a thin relationships
         * from the term factory.
         * This validator will return false if, for each relationship in the UniversalAceBean:
         *
         * 1. The number of starting source relationships doesn't equals the number of
         *          source relationships in term factory.
         * 2. There isn't a corresponding relationship in the database with the same
         *       relationship ID.
         * 3. There isn't a corresponding relationship in the database with the same source
         *       relationship ID.
         * 4. There isn't a corresponding relationship in the database with the same destination
         *       relationship ID.
         * 5. One of the starting relationships (relationships whose time is not Long.MAX_VALUE)
         *       doesn't match one of the thin relationships in term factory
         *
         */

        termFactory = tf;

        I_GetConceptData concept = tf.getConcept(tf.uuidToNative(bean.getId().getUIDs()));
        List<I_RelVersioned> databaseRelationships = concept.getSourceRels();
        List<UniversalAceRelationship> beanRelationships = bean.getSourceRels();

        if (databaseRelationships.size() != beanRelationships.size()) {
            System.out.println("number of relationships different");
            return false; // test 1
        }

        for (UniversalAceRelationship beanRelationship : beanRelationships) {

            boolean foundMatch = false;

            for (I_RelVersioned databaseRelationship : databaseRelationships) {
                if (relationshipsEqual(beanRelationship, databaseRelationship)) {
                    foundMatch = true;
                }
            }

            if (!foundMatch) {
                System.out.println("Failed to find matching relationship");
                return false;
            }
        }
        return true;
    }

    public boolean relationshipsEqual(UniversalAceRelationship beanRelationship,
            I_RelVersioned databaseRelationship)  throws IOException, TerminologyException {

        if (termFactory.uuidToNative(beanRelationship.getRelId()) !=
                databaseRelationship.getRelId()) {
            //System.out.println("rel ids different");
            return false; // Test 2
        }

        if (termFactory.uuidToNative(beanRelationship.getC1Id()) !=
                databaseRelationship.getC1Id()) {
            //System.out.println("c1 id diff");
            return false; // Test 3
         }

        if (termFactory.uuidToNative(beanRelationship.getC2Id()) !=
                databaseRelationship.getC2Id()) {
            //System.out.println("c2 id diff");
            return false; // Test 4
        }

        for (UniversalAceRelationshipPart part : beanRelationship.getVersions()) {
            if (part.getTime() != Long.MAX_VALUE) {
                I_RelPart newPart = termFactory.newRelPart();
                newPart.setVersion(termFactory.convertToThinVersion(part.getTime()));
                newPart.setPathId(termFactory.uuidToNative(part.getPathId()));
                newPart.setCharacteristicId(termFactory.uuidToNative(part.getCharacteristicId()));
                newPart.setGroup(part.getGroup());
                newPart.setRefinabilityId(termFactory.uuidToNative(part.getRefinabilityId()));
                newPart.setRelTypeId(termFactory.uuidToNative(part.getRelTypeId()));
                newPart.setStatusId(termFactory.uuidToNative(part.getStatusId()));

                if (databaseRelationship.getVersions().contains(newPart) == false) {
                    System.out.println("parts different");

                    System.out.println(databaseRelationship.getVersions());
                    System.out.println("..........");
                    System.out.println("new part: " + newPart);
                    return false; // Test 5
                }
            }
        }

        return true;
    }

}
