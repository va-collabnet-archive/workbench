package org.dwfa.ace.task.conflict.detector;

import java.util.Comparator;

import org.dwfa.ace.api.I_RelTuple;

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

