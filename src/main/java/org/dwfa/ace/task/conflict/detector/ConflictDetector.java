package org.dwfa.ace.task.conflict.detector;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;

public class ConflictDetector {
   /**
    * @todo implement conflict for images.
    * @param concept
    * @return
    * @throws IOException
    */
   public static boolean conflict(I_GetConceptData concept, I_ConfigAceFrame profileForConflictDetection) throws IOException {
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
            TreeSet<I_ConceptAttributeTuple> positionTupleSet = new TreeSet<I_ConceptAttributeTuple>(new AttrTupleConflictComparator());
            positionTupleSet.addAll(concept.getConceptAttributeTuples(null, viewPositionSet));
            if (positionTupleSet.containsAll(attributeTuples) == false) {
               return true;
            }
            if (attributeTuples.containsAll(positionTupleSet) == false) {
               return true;
            }
         }

         if (descTuples == null) {
            descTuples = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
            descTuples.addAll(concept.getDescriptionTuples(null, null, viewPositionSet));
         } else {
            TreeSet<I_DescriptionTuple> positionTupleSet = new TreeSet<I_DescriptionTuple>(new DescriptionTupleConflictComparator());
            positionTupleSet.addAll(concept.getDescriptionTuples(null, null, viewPositionSet));
            if (positionTupleSet.containsAll(descTuples) == false) {
               return true;
            }
            if (descTuples.containsAll(positionTupleSet) == false) {
               return true;
            }
         }

         if (relTuples == null) {
            relTuples = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
            relTuples.addAll(concept.getSourceRelTuples(null, null, viewPositionSet, false));
         } else {
            TreeSet<I_RelTuple> positionTupleSet = new TreeSet<I_RelTuple>(new RelTupleConflictComparator());
            positionTupleSet.addAll(concept.getSourceRelTuples(null, null, viewPositionSet, false));
            if (positionTupleSet.containsAll(relTuples) == false) {
               return true;
            }
            if (relTuples.containsAll(positionTupleSet) == false) {
               return true;
            }
         }
      }

      return false;
   }

}
