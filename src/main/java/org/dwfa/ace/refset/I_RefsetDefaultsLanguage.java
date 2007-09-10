package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsLanguage extends I_RefsetDefaults {

   public I_GetConceptData getDefaultAcceptabilityForLanguageRefset();
   public I_IntList getAcceptabilityPopupIds();

   public I_GetConceptData getDefaultCorrectnessForLanguageRefset();
   public I_IntList getCorrectnessPopupIds();

   public I_GetConceptData getDefaultDegreeOfSynonymyForLanguageRefset();
   public I_IntList getDegreeOfSynonymyPopupIds();

   

}
