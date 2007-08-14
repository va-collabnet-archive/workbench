package org.dwfa.vodb.types;

public class ThinExtPartScopedLanguage extends ThinExtPart {
   private int scopeId;
   private int priority;
   private int tagId;
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
   public int getPriority() {
      return priority;
   }
   public void setPriority(int priority) {
      this.priority = priority;
   }
   public int getScopeId() {
      return scopeId;
   }
   public void setScopeId(int scopeId) {
      this.scopeId = scopeId;
   }
   public int getTagId() {
      return tagId;
   }
   public void setTagId(int tagId) {
      this.tagId = tagId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtPartScopedLanguage.class.isAssignableFrom(obj.getClass())) {
            ThinExtPartScopedLanguage another = (ThinExtPartScopedLanguage) obj;
            return acceptabilityId == another.acceptabilityId &&
            scopeId == another.scopeId &&
            priority == another.priority &&
            tagId == another.tagId &&
            correctnessId == another.correctnessId &&
            degreeOfSynonymyId == another.degreeOfSynonymyId;
         }
      }
      return false;
   }
}
