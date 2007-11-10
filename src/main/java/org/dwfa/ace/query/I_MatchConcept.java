package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public interface I_MatchConcept {
   public void addRelConstraint(I_MatchRelationship relMatcher);
   public void addDescConstraint(I_MatchDescription descMatcher);
   public void addStatusConstraint(I_MatchConcept conceptMatcher);
   public void addRefsetConstraint(I_MatchRefset refsetMatcher);
   public void addChildOfConstraint(I_AmChildOf parentMatcher);
   
   public boolean matchConcept(I_GetConceptData concept, I_ConfigAceFrame profile);
}
