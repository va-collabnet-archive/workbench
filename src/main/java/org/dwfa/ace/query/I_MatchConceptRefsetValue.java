package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;

public interface I_MatchConceptRefsetValue {
   public boolean matchConceptRefsetValue(I_ThinExtByRefVersioned extension, I_ConfigAceFrame profile);
}
