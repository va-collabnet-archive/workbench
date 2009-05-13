package org.dwfa.ace.api.ebr;


public interface I_ThinExtByRefPartLanguageScoped extends I_ThinExtByRefPartLanguage {

   public int getPriority();

   public void setPriority(int priority);

   public int getScopeId();

   public void setScopeId(int scopeId);

   public int getTagId();

   public void setTagId(int tagId);

}