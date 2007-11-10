package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartLanguageScoped extends I_ThinExtByRefPartLanguage {

   public int getPriority();

   public void setPriority(int priority);

   public int getScopeId();

   public void setScopeId(int scopeId);

   public int getTagId();

   public void setTagId(int tagId);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPart duplicatePart();

}