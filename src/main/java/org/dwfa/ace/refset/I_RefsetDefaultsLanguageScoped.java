package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsLanguageScoped extends I_RefsetDefaultsLanguage {
   public I_GetConceptData getDefaultScopeForScopedLanguageRefset();
   public I_IntList getScopePopupIds();

   public I_GetConceptData getDefaultTagForScopedLanguageRefset();
   public I_IntList getTagPopupIds();

   public int getDefaultPriorityForScopedLanguageRefset();
   public Integer[] getPriorityPopupItems();

}
