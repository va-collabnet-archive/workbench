package org.dwfa.ace.task.conflict.detector;

import java.util.Comparator;

import org.dwfa.ace.api.I_DescriptionTuple;

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

