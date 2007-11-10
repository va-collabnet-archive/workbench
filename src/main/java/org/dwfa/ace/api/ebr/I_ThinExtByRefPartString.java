package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPartString extends I_ThinExtByRefPart {

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPartString duplicatePart();

   public String getStringValue();

   public void setStringValue(String stringValue);

}