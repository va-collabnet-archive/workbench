package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPart extends Comparable<I_ThinExtByRefPart>, I_AmPart {

   public int getStatus();

   public void setStatus(int idStatus);

   public void setPathId(int pathId);

   public void setVersion(int version);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPart duplicatePart();

}