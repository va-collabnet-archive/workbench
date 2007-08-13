package org.dwfa.vodb.types;

public class ThinExtPartBoolean extends ThinExtPart {
   private boolean value;

   public boolean getValue() {
      return value;
   }

   public void setValue(boolean value) {
      this.value = value;
   }
   
   @Override
   public boolean equals(Object obj) {
      if (super.equals(obj)) {
         if (ThinExtPartBoolean.class.isAssignableFrom(obj.getClass())) {
            ThinExtPartBoolean another = (ThinExtPartBoolean) obj;
            return value == another.value;
         }
      }
      return false;
   }

}
