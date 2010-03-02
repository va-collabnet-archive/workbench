package org.ihtsdo.concept;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.task.conflict.detector.AttrTupleConflictComparator;
import org.dwfa.ace.task.conflict.detector.DescriptionTupleConflictComparator;
import org.dwfa.ace.task.conflict.detector.RelTupleConflictComparator;

public class ConflictHelper {

   
   public static Set<I_ConceptAttributeTuple> getCommonConceptAttributeTuples(Concept cb, I_ConfigAceFrame config)
         throws IOException {
      Set<I_ConceptAttributeTuple> commonTuples = null;
      for (I_Position p : config.getViewPositionSet()) {
          PositionSetReadOnly positionSet = new PositionSetReadOnly(p);
         List<? extends I_ConceptAttributeTuple> tuplesForPosition = cb.getConceptAttributeTuples(config.getAllowedStatus(),
               positionSet, false);
         if (commonTuples == null) {
            commonTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
            commonTuples.addAll(tuplesForPosition);
         } else {
            commonTuples.retainAll(tuplesForPosition);
         }
      }
      if (commonTuples == null) {
         commonTuples = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
      }
      return commonTuples;
   }

   public static Set<I_RelTuple> getCommonRelTuples(Concept cb, I_ConfigAceFrame config) throws IOException {
      Set<I_RelTuple> commonTuples = null;
      for (I_Position p : config.getViewPositionSet()) {
         PositionSetReadOnly positionSet = new PositionSetReadOnly(p);
         List<? extends I_RelTuple> tuplesForPosition = cb
               .getSourceRelTuples(config.getAllowedStatus(), null, positionSet, false);
         if (commonTuples == null) {
            commonTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
            commonTuples.addAll(tuplesForPosition);
         } else {
            commonTuples.retainAll(tuplesForPosition);
         }
      }
      if (commonTuples == null) {
         commonTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
      }
      return commonTuples;
   }

   public static Set<I_DescriptionTuple> getCommonDescTuples(Concept cb, I_ConfigAceFrame config)
         throws IOException {
      Set<I_DescriptionTuple> commonTuples = null;
      for (I_Position p : config.getViewPositionSet()) {
          PositionSetReadOnly positionSet = new PositionSetReadOnly(p);
         List<I_DescriptionTuple> tuplesForPosition = cb.getDescriptionTuples(config.getAllowedStatus(), null,
               positionSet, false);
         if (commonTuples == null) {
            commonTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
            commonTuples.addAll(tuplesForPosition);
         } else {
            commonTuples.retainAll(tuplesForPosition);
         }
      }
      if (commonTuples == null) {
         commonTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
      }
      return commonTuples;
   }

}
