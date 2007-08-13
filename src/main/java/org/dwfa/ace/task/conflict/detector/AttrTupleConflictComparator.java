package org.dwfa.ace.task.conflict.detector;

import java.util.Comparator;

import org.dwfa.ace.api.I_ConceptAttributeTuple;

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
