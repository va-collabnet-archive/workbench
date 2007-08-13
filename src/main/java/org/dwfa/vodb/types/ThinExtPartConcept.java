package org.dwfa.vodb.types;

public class ThinExtPartConcept extends ThinExtPart {
   private int conceptId;

   public int getConceptId() {
      return conceptId;
   }

   public void setConceptId(int conceptId) {
      this.conceptId = conceptId;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtPartConcept.class.isAssignableFrom(obj.getClass())) {
            ThinExtPartConcept another = (ThinExtPartConcept) obj;
            return conceptId == another.conceptId;
         }
      }
      return false;
   }
}
