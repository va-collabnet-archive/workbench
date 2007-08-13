package org.dwfa.vodb.types;

public class ThinExtPartLanguage extends ThinExtPart {
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
         if (ThinExtPartLanguage.class.isAssignableFrom(obj.getClass())) {
            ThinExtPartLanguage another = (ThinExtPartLanguage) obj;
            return acceptabilityId == another.acceptabilityId &&
            correctnessId == another.correctnessId &&
            degreeOfSynonymyId == another.degreeOfSynonymyId;
         }
      }
      return false;
   }
}
