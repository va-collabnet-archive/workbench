
/*
* To change this template, choose Tools | Templates and open the template in the editor.
 */
package org.ihtsdo.taxonomy.model.childfilters;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.taxonomy.model.ChildNodeFilterBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

/**
 *
 * @author kec
 */
public class RefsetMemberChildFilter implements ChildNodeFilterBI {
   ConceptVersionBI refsetConcept; 

   //~--- constructors --------------------------------------------------------

   public RefsetMemberChildFilter(ConceptVersionBI refsetConcept) {
      this.refsetConcept = refsetConcept;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean pass(ConceptVersionBI parent, ConceptVersionBI possibleChild) throws Exception {
      if (refsetConcept.isAnnotationStyleRefex()) {
         return possibleChild.hasAnnotationMemberActive(refsetConcept.getNid());
      }

      return refsetConcept.hasRefsetMemberActiveForComponent(possibleChild.getNid());
   }
}
