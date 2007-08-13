package org.dwfa.vodb.types;

public class ThinExtPartInteger extends ThinExtPart {
   public int value;

   public int getValue() {
      return value;
   }

   public void setValue(int value) {
      this.value = value;
   }
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtPartInteger.class.isAssignableFrom(obj.getClass())) {
            ThinExtPartInteger another = (ThinExtPartInteger) obj;
            return value == another.value;
         }
      }
      return false;
   }
}
