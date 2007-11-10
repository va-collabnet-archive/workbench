package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartInteger extends I_ThinExtByRefPart {

   public int getValue();

   public void setValue(int value);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

}