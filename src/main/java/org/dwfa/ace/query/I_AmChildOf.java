package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public interface I_AmChildOf {
   public boolean childOf(I_GetConceptData concept, I_ConfigAceFrame profile);

}
