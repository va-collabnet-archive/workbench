package org.dwfa.ace;

import java.io.IOException;
import java.util.Comparator;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.tree.I_GetConceptDataForTree;

public class CompareConceptBeansForTree implements Comparator<I_GetConceptDataForTree> {

   private I_ConfigAceFrame aceConfig;

   public CompareConceptBeansForTree(I_ConfigAceFrame aceConfig) {
      super();
      this.aceConfig = aceConfig;
   }

   public int compare(I_GetConceptDataForTree cb1, I_GetConceptDataForTree cb2) {
      try {
         int comparison = cb1.getDescTuple(aceConfig).getText().toLowerCase().compareTo(cb2.getDescTuple(aceConfig).getText().toLowerCase());
         if (comparison == 0) {
            comparison = cb1.getDescTuple(aceConfig).getText().compareTo(cb2.getDescTuple(aceConfig).getText());
         }
         if (comparison == 0) {
            comparison = cb1.getConceptId() - cb2.getConceptId();
         }
         return comparison;
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}
