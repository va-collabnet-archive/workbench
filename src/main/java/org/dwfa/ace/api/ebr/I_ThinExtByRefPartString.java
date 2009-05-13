package org.dwfa.ace.api.ebr;


public interface I_ThinExtByRefPartString extends I_ThinExtByRefPart {

   public I_ThinExtByRefPartString duplicate();

   public String getStringValue();

   public void setStringValue(String stringValue);

}