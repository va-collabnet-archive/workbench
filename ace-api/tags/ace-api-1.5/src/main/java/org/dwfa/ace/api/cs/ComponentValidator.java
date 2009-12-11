/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
           AceLog.getEditLog().info("Invalid description: " + bean);
       }

       if (!validRelationship) {
           AceLog.getEditLog().info("Invalid relationship: " + bean);
       }

       return validDescription && validRelationship;

   }

}
