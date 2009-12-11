/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace;

import java.io.IOException;
import java.util.Comparator;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.tree.I_GetConceptDataForTree;

public class CompareConceptBeansForTree implements Comparator<I_GetConceptDataForTree> {

   private I_ConfigAceFrame aceConfig;

   public CompareConceptBeansForTree(I_ConfigAceFrame aceConfig) {
      super();
      this.aceConfig = aceConfig;
   }

   public int compare(I_GetConceptDataForTree cb1, I_GetConceptDataForTree cb2) {
      try {
         if (cb1 == cb2) {
            return 0;
         }
         if (cb1 == null) {
            return 1;
         }
         if (cb2 == null) {
            return -1;
         }
         I_DescriptionTuple cb1dt = cb1.getDescTuple(aceConfig);
         I_DescriptionTuple cb2dt = cb2.getDescTuple(aceConfig);
         
         if (cb1dt == cb2dt) {
            return cb1.getConceptId() - cb2.getConceptId();
         }
         if (cb1dt == null || cb1dt.getText() == null) {
            return 1;
         }
         if (cb2dt == null || cb2dt.getText() == null) {
            return -1;
         }
         int comparison = cb1dt.getText().toLowerCase().compareTo(cb2dt.getText().toLowerCase());
         if (comparison == 0) {
            comparison = cb1dt.getText().compareTo(cb2dt.getText());
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
