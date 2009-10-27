package org.dwfa.ace.api.ebr;

public interface I_ThinExtByRefPartConceptConceptString extends I_ThinExtByRefPart {

	   public int getC1id();
	   public void setC1id(int c1id);

	   public int getC2id();
	   public void setC2id(int c2id);

	   /** @deprecated Use {@link #getStringValue()} */
	   @Deprecated
	   public String getStr();
	   
	   /** @deprecated Use {@link #setStringValue(String)} */
	   @Deprecated
	   public void setStr(String str);
	   
	   public String getStringValue();
	   public void setStringValue(String value);

}