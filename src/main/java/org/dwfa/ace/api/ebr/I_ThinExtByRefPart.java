package org.dwfa.ace.api.ebr;

import java.io.IOException;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.utypes.UniversalAceExtByRefPart;
import org.dwfa.tapi.TerminologyException;

public interface I_ThinExtByRefPart extends Comparable<I_ThinExtByRefPart>, I_AmPart {

	/**
	 * @deprecated Use {@link #getStatusId()}
	 */
	@Deprecated
	public int getStatus();

	/**
	 * @deprecated Use {@link #setStatusId(int)}
	 */
	@Deprecated
	public void setStatus(int idStatus);

	public UniversalAceExtByRefPart getUniversalPart() throws TerminologyException, IOException;

	/**
	 * @deprecated Use {@link #duplicate()}
	 */
	@Deprecated
	public I_ThinExtByRefPart duplicatePart();

	public I_ThinExtByRefPart duplicate();
   
}