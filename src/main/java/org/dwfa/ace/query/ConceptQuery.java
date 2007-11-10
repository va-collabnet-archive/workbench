package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public class ConceptQuery implements I_MatchConcept {

   public void addChildOfConstraint(I_AmChildOf parentMatcher) {
      // TODO Auto-generated method stub

   }

   public void addDescConstraint(I_MatchDescription descMatcher) {
      // TODO Auto-generated method stub

   }

   public void addRefsetConstraint(I_MatchRefset refsetMatcher) {
      // TODO Auto-generated method stub

   }

   public void addRelConstraint(I_MatchRelationship relMatcher) {
      // TODO Auto-generated method stub

   }

   public void addStatusConstraint(I_MatchConcept conceptMatcher) {
      // TODO Auto-generated method stub

   }

   public boolean matchConcept(I_GetConceptData concept, I_ConfigAceFrame profile) {
      // TODO Auto-generated method stub
      return false;
   }

}
