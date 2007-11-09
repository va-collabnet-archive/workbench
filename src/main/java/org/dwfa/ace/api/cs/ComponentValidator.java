package org.dwfa.ace.api.cs;

import java.io.IOException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

/**
 * Validates a component - it must match exactly with a component in the database.
 * @author Christine Hill
 *
 */
public class ComponentValidator extends SimpleValidator {

   @Override
   protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
       throws IOException, TerminologyException {
       /*
        * Compares a bean to the associated term factory component.
        * 1) all descriptions must match
        * 2) all source relationships must match
        */

       DescriptionValidator descriptionValidator = new DescriptionValidator();
       RelationshipValidator relationshipValidator = new RelationshipValidator();

       boolean validDescription = descriptionValidator.validateAceBean(bean, tf);
       boolean validRelationship = relationshipValidator.validateAceBean(bean, tf);

       if (!validDescription) {
           System.out.println("Invalid description: " + bean);
       }

       if (!validRelationship) {
           System.out.println("Invalid relationship: " + bean);
       }

       return validDescription && validRelationship;

   }

}
