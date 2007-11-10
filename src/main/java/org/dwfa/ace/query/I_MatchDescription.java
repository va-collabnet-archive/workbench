package org.dwfa.ace.query;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;

public interface I_MatchDescription {
   public boolean matchDescription(I_DescriptionVersioned desc, I_ConfigAceFrame profile);

}
