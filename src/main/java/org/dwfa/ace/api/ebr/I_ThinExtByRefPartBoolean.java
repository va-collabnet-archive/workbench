package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartBoolean extends I_ThinExtByRefPart {

   public boolean getValue();

   public void setValue(boolean value);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

}