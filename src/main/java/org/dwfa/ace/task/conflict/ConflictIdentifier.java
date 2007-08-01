package org.dwfa.ace.task.conflict;

import java.util.List;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ProcessConcepts;

public class ConflictIdentifier implements I_ProcessConcepts {
   
   private I_IntSet conflictsNids;
   
   private int conceptsProcessed = 0;
   private int conceptsProcessedWithNoTuples = 0;
   private int conceptsProcessedWithOneTuple = 0;
   private int conceptsProcessedWithTwoTuples = 0;
   private int conceptsProcessedWithThreeTuples = 0;
   private int conceptsProcessedWithFourTuples = 0;
   
   I_ConfigAceFrame profileForConflictDetection;

   public ConflictIdentifier(I_IntSet conflictsNids, I_ConfigAceFrame profileForConflictDetection) {
      super();
      this.conflictsNids = conflictsNids;
      this.profileForConflictDetection = profileForConflictDetection;
   }

   public int getConceptsProcessed() {
      return conceptsProcessed;
   }

   public I_IntSet getConflictsNids() {
      return conflictsNids;
   }

   public void processConcept(I_GetConceptData concept) throws Exception {
      conceptsProcessed++;
      List<I_ConceptAttributeTuple> attrTupels;
      if (profileForConflictDetection == null) {
         attrTupels = concept.getConceptAttributeTuples(null,
               null);
      } else {
         attrTupels = concept.getConceptAttributeTuples(profileForConflictDetection.getAllowedStatus(),
               profileForConflictDetection.getViewPositionSet());
      }
      switch (attrTupels.size()) {
      case 0:
         conceptsProcessedWithNoTuples++;
         break;
      case 1:
         conceptsProcessedWithOneTuple++;
         break;
      case 2:
         conceptsProcessedWithTwoTuples++;
         conflictsNids.add(concept.getConceptId());
         break;
      case 3:
         conceptsProcessedWithThreeTuples++;
         conflictsNids.add(concept.getConceptId());
         break;
      case 4:
         conceptsProcessedWithFourTuples++;
         conflictsNids.add(concept.getConceptId());
         break;

      default:
         conflictsNids.add(concept.getConceptId());
         break;
      }
   }
   
   
   
   public String toString() {
      return "conceptsProcessed: " + conceptsProcessed + " conflicts: " + conflictsNids.getSetValues().length + 
      " conceptsProcessedWithNoTuples: " + conceptsProcessedWithNoTuples  + 
      " conceptsProcessedWithOneTuple: " + conceptsProcessedWithOneTuple  + 
      " conceptsProcessedWithTwoTuples: " + conceptsProcessedWithTwoTuples  + 
      " conceptsProcessedWithThreeTuples: " + conceptsProcessedWithThreeTuples  + 
      " conceptsProcessedWithFourTuples: " + conceptsProcessedWithFourTuples;
   }

}
