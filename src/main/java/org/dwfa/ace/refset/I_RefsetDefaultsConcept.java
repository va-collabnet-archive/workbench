package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsConcept extends I_RefsetDefaults {
   public I_GetConceptData getDefaultForConceptRefset();
   public I_IntList getConceptPopupIds();

}
