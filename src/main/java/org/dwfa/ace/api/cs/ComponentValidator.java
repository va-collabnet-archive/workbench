package org.dwfa.ace.api.cs;

import java.io.IOException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;

/**
 * Validates a component - it must match exactly with a component in the database.
 * @author Christine Hill
 *
 */
public class ComponentValidator extends SimpleValidator {

   private final RelationshipValidator relationshipValidator = new RelationshipValidator();
   private final DescriptionValidator descriptionValidator = new DescriptionValidator();

   @Override
   protected boolean validateAceBean(UniversalAceBean bean, I_TermFactory tf)
       throws IOException, TerminologyException {
       /*
        * Compares a bean to the associated term factory component.
        * 1) all descriptions must match
        * 2) all source relationships must match
        */

       boolean validDescription = descriptionValidator.validateAceBean(bean, tf);

       if (!validDescription) {
           AceLog.getEditLog().info("Invalid description: " + bean);
           return false;
       }

       boolean validRelationship = relationshipValidator.validateAceBean(bean, tf);

       if (!validRelationship) {
           AceLog.getEditLog().info("Invalid relationship: " + bean);
           return false;
       }

       return true;

   }

}
