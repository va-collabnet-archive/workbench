package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPart {

   public int getStatus();

   public void setStatus(int idStatus);

   public int getPathId();

   public void setPathId(int pathId);

   public int getVersion();

   public void setVersion(int version);

   public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

   public I_ThinExtByRefPart duplicatePart();

}