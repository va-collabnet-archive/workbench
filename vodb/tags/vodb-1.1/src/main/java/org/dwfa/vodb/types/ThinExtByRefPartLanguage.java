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
package org.dwfa.vodb.types;

import java.io.IOException;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPartLanguage;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

public class ThinExtByRefPartLanguage extends ThinExtByRefPart {
   private int acceptabilityId;
   private int correctnessId;
   private int degreeOfSynonymyId;
   
   public int getAcceptabilityId() {
      return acceptabilityId;
   }
   public void setAcceptabilityId(int acceptabilityId) {
      this.acceptabilityId = acceptabilityId;
   }
   public int getCorrectnessId() {
      return correctnessId;
   }
   public void setCorrectnessId(int correctnessId) {
      this.correctnessId = correctnessId;
   }
   public int getDegreeOfSynonymyId() {
      return degreeOfSynonymyId;
   }
   public void setDegreeOfSynonymyId(int degreeOfSynonymyId) {
      this.degreeOfSynonymyId = degreeOfSynonymyId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtByRefPartLanguage.class.isAssignableFrom(obj.getClass())) {
            ThinExtByRefPartLanguage another = (ThinExtByRefPartLanguage) obj;
            return acceptabilityId == another.acceptabilityId &&
            correctnessId == another.correctnessId &&
            degreeOfSynonymyId == another.degreeOfSynonymyId;
         }
      }
      return false;
   }
   
   @Override
   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException {
      I_TermFactory tf = LocalVersionedTerminology.get();
      UniversalAceExtByRefPartLanguage universalPart = new UniversalAceExtByRefPartLanguage();
      universalPart.setAcceptabilityUids(tf.getUids(getAcceptabilityId()));
      universalPart.setCorrectnessUids(tf.getUids(getCorrectnessId()));
      universalPart.setDegreeOfSynonymyUids(tf.getUids(getDegreeOfSynonymyId()));
      universalPart.setPathUid(tf.getUids(getPathId()));
      universalPart.setStatusUid(tf.getUids(getStatus()));
      universalPart.setTime(ThinVersionHelper.convert(getVersion()));
      return universalPart;
   }
   @Override
   public ThinExtByRefPart duplicatePart() {
      return new ThinExtByRefPartLanguage(this);
   }
   public ThinExtByRefPartLanguage(ThinExtByRefPartLanguage another) {
      super(another);
      this.acceptabilityId = another.acceptabilityId;
      this.correctnessId = another.correctnessId;
      this.degreeOfSynonymyId = another.degreeOfSynonymyId;
   }
   public ThinExtByRefPartLanguage() {
      super();
   }

}
