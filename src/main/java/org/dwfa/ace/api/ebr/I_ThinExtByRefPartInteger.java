package org.dwfa.ace.api.ebr;


public interface I_ThinExtByRefPartInteger extends I_ThinExtByRefPart {

   /** @deprecated Use {@link #getIntValue()} */
   @Deprecated
   public int getValue();

   /** @deprecated Use {@link #setIntValue(int)} */
   @Deprecated
   public void setValue(int value);

   public int getIntValue();
   
   public void setIntValue(int value);
   
}