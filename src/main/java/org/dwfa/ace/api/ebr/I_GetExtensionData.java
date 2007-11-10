package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.utypes.UniversalAceExtByRefBean;
import org.dwfa.tapi.TerminologyException;

public interface I_GetExtensionData {

   public abstract I_ThinExtByRefVersioned getExtension() throws IOException;

   public abstract UniversalAceExtByRefBean getUniversalAceBean() throws TerminologyException, IOException;

   public abstract int getMemberId();

}