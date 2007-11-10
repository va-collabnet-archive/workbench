package org.dwfa.ace.query;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;

public abstract class Query implements I_ProcessConcepts {

   public enum JOIN {
      AND, OR
   };

   private JOIN queryType;

   private I_ConfigAceFrame profile;

   private List<I_MatchConcept> matchers = new ArrayList<I_MatchConcept>();
   
   

   public Query(JOIN queryType, I_ConfigAceFrame profile) {
      super();
      this.queryType = queryType;
      this.profile = profile;
   }

   public void processConcept(I_GetConceptData concept) throws Exception {
      switch (queryType) {
      case AND:
         for (I_MatchConcept m : matchers) {
            if (m.matchConcept(concept, profile) == false) {
               return;
            }
            processMatch(concept);
         }
         break;
      case OR:
         for (I_MatchConcept m : matchers) {
            if (m.matchConcept(concept, profile)) {
               processMatch(concept);
               break;
            }
         }
         break;

      default:
         throw new Exception("Don't know how to handle: " + queryType);
      }

   }

   public abstract void processMatch(I_GetConceptData concept) throws Exception;

   public List<I_MatchConcept> getMatchers() {
      return matchers;
   }

}
