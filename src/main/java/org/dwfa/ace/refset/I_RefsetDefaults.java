package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaults {
   public I_GetConceptData getDefaultRefset();
   public I_IntList getRefsetPopupIds();
   
   public I_GetConceptData getDefaultStatusForRefset();
   public I_IntList getStatusPopupIds();
}
