package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_RelVersioned;

public interface I_MatchRelationship {
   public boolean matchRelationship(I_RelVersioned rel, I_ConfigAceFrame profile);

}
