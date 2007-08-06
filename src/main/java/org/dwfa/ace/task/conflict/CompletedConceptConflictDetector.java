package org.dwfa.ace.task.conflict;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class CompletedConceptConflictDetector implements I_ProcessConcepts {

   private I_IntSet conflictsNids;

   private I_IntSet noConflictNids;

   private int conceptsProcessed = 0;

   private int conceptsProcessedWithNoTuples = 0;

   private int conceptsProcessedWithOneTuple = 0;

   private int conceptsProcessedWithTwoTuples = 0;

   private int conceptsProcessedWithThreeTuples = 0;

   private int conceptsProcessedWithFourTuples = 0;

   I_ConfigAceFrame profileForConflictDetection;

   HashMap<Integer, Integer> statusCount = new HashMap<Integer, Integer>();

   public CompletedConceptConflictDetector(I_IntSet conflictsNids, I_IntSet noConflictNids,
                                           I_ConfigAceFrame profileForConflictDetection) {
      super();
      this.conflictsNids = conflictsNids;
      this.noConflictNids = noConflictNids;
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
         attrTupels = concept.getConceptAttributeTuples(null, null);
      } else {
         attrTupels = concept.getConceptAttributeTuples(profileForConflictDetection.getAllowedStatus(),
               profileForConflictDetection.getViewPositionSet());
      }
      int tupleListSize = attrTupels.size();
      if (tupleListSize > 1) {
         AceLog.getAppLog().info(concept.getInitialText() + " has multiple tuples: " + attrTupels);
      }
      for (I_ConceptAttributeTuple tuple : attrTupels) {
         if (statusCount.containsKey(tuple.getConceptStatus())) {
            Integer count = statusCount.get(tuple.getConceptStatus());
            statusCount.put(tuple.getConceptStatus(), count.intValue() + 1);
         } else {
            statusCount.put(tuple.getConceptStatus(), 1);
         }
      }

      if (attrTupels.size() > 1) {
         if (conflict(concept)) {
            conflictsNids.add(concept.getConceptId());
         } else {
            noConflictNids.add(concept.getConceptId());
         }
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
         break;
      case 3:
         conceptsProcessedWithThreeTuples++;
         break;
      case 4:
         conceptsProcessedWithFourTuples++;
         break;
      default:
         break;
      }
   }

   public class AttrTupleConflictComparator implements Comparator<I_ConceptAttributeTuple> {
      public int compare(I_ConceptAttributeTuple t1, I_ConceptAttributeTuple t2) {
         if (t1.getConceptStatus() != t2.getConceptStatus()) {
            return t1.getConceptStatus() - t2.getConceptStatus();
         }
         if (t1.isDefined() != t2.isDefined()) {
            if (t1.isDefined()) {
               return -1;
            } else {
               return +1;
            }
         }
         return 0;
      }
   }

   public class DescriptionTupleConflictComparator implements Comparator<I_DescriptionTuple> {

      public int compare(I_DescriptionTuple t1, I_DescriptionTuple t2) {
         if (t1.getStatusId() != t2.getStatusId()) {
            return t1.getStatusId() - t2.getStatusId();
         }
         if (t1.getConceptId() != t2.getConceptId()) {
            return t1.getConceptId() - t2.getConceptId();
         }
         if (t1.getInitialCaseSignificant() != t2.getInitialCaseSignificant()) {
            if (t1.getInitialCaseSignificant()) {
               return -1;
            } else {
               return +1;
            }
         }
         if (t1.getLang().equals(t2.getLang()) == false) {
            return t1.getLang().compareTo(t2.getLang());
         }
         if (t1.getText().equals(t2.getText()) == false) {
            return t1.getText().compareTo(t2.getText());
         }
         if (t1.getTypeId() != t2.getTypeId()) {
            return t1.getTypeId() - t2.getTypeId();
         }
         return 0;
      }
   }

   public class RelTupleConflictComparator implements Comparator<I_RelTuple> {

      public int compare(I_RelTuple t1, I_RelTuple t2) {
         if (t1.getStatusId() != t2.getStatusId()) {
            return t1.getStatusId() - t2.getStatusId();
         }
         if (t1.getC1Id() != t2.getC1Id()) {
            return t1.getC1Id() - t2.getC1Id();
         }
         if (t1.getC2Id() != t2.getC2Id()) {
            return t1.getC2Id() - t2.getC2Id();
         }
         if (t1.getCharacteristicId() != t2.getCharacteristicId()) {
            return t1.getCharacteristicId() - t2.getCharacteristicId();
         }
         if (t1.getGroup() != t2.getGroup()) {
            return t1.getGroup() - t2.getGroup();
         }
         if (t1.getRefinabilityId() != t2.getRefinabilityId()) {
            return t1.getRefinabilityId() - t2.getRefinabilityId();
         }
         if (t1.getRelTypeId() != t2.getRelTypeId()) {
            return t1.getRelTypeId() - t2.getRelTypeId();
         }
         return 0;
      }
   }

   /**
    * @todo implement conflict for images.
    * @param concept
    * @return
    * @throws IOException
    */
   public boolean conflict(I_GetConceptData concept) throws IOException {
      Set<I_ConceptAttributeTuple> attributeTuples = null;
      Set<I_DescriptionTuple> descTuples = null;
      Set<I_RelTuple> relTuples = null;
      for (I_Position viewPos : profileForConflictDetection.getViewPositionSet()) {
         Set<I_Position> viewPositionSet = new HashSet<I_Position>();
         viewPositionSet.add(viewPos);
         if (attributeTuples == null) {
            attributeTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
            attributeTuples.addAll(concept.getConceptAttributeTuples(null, viewPositionSet));
         } else {
            for (I_ConceptAttributeTuple tuple : concept.getConceptAttributeTuples(null, viewPositionSet)) {
               if (attributeTuples.contains(tuple) == false) {
                  return true;
               }
            }
         }

         if (descTuples == null) {
            descTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
            descTuples.addAll(concept.getDescriptionTuples(null, null, viewPositionSet));
         } else {
            for (I_DescriptionTuple tuple : concept.getDescriptionTuples(null, null, viewPositionSet)) {
               if (descTuples.contains(tuple) == false) {
                  return true;
               }
            }
         }

         if (relTuples == null) {
            relTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
            relTuples.addAll(concept.getSourceRelTuples(null, null, viewPositionSet, false));
         } else {
            for (I_RelTuple tuple : concept.getSourceRelTuples(null, null, viewPositionSet, false)) {
               if (relTuples.contains(tuple) == false) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public String toString() {
      StringBuffer buff = new StringBuffer();
      if (profileForConflictDetection != null) {
         for (I_Position view : profileForConflictDetection.getViewPositionSet()) {
            buff.append("\nview: " + view);
         }
         for (int statusNid : profileForConflictDetection.getAllowedStatus().getSetValues()) {
            try {
               I_GetConceptData status = LocalVersionedTerminology.get().getConcept(statusNid);
               buff.append("\nallowed status: " + status.getInitialText() + " (" + status.getConceptId() + "): ");
            } catch (TerminologyException e) {
               AceLog.getAppLog().alertAndLogException(e);
            } catch (IOException e) {
               AceLog.getAppLog().alertAndLogException(e);
            }
         }
      } else {
         buff.append("\n null profile. ");
      }
      for (Integer key : statusCount.keySet()) {
         try {
            I_GetConceptData status = LocalVersionedTerminology.get().getConcept(key);
            Integer count = statusCount.get(key);
            buff.append("\nstatus: " + status.getInitialText() + " (" + status.getConceptId() + "); count: " + count);
         } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

      return "conceptsProcessed: " + conceptsProcessed + " conflicts: " + conflictsNids.getSetValues().length
            + " conceptsProcessedWithNoTuples: " + conceptsProcessedWithNoTuples + " conceptsProcessedWithOneTuple: "
            + conceptsProcessedWithOneTuple + " conceptsProcessedWithTwoTuples: " + conceptsProcessedWithTwoTuples
            + " conceptsProcessedWithThreeTuples: " + conceptsProcessedWithThreeTuples
            + " conceptsProcessedWithFourTuples: " + conceptsProcessedWithFourTuples + buff.toString();
   }

   public I_IntSet getNoConflictNids() {
      return noConflictNids;
   }

}
