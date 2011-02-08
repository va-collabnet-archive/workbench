package org.ihtsdo.tk.api.amend;

import java.io.IOException;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;

public interface TerminologyAmendmentBI {

	/**
	 *  
	 * @param res
	 * @return A <code>RefexChronicleBI</code> if the <code>res</code> regardless of if the Refex was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RefexChronicleBI<?> amend(RefexAmendmentSpec res) throws IOException, InvalidAmendmentSpec;
	
	/**
	 *  This method incurs an extra cost to determine if a current version already meets the specification. 
	 * @param res
	 * @return A <code>RefexChronicleBI</code> if the <code>res</code> regardless of if the Refex was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RefexChronicleBI<?> amendIfNotCurrent(RefexAmendmentSpec res) throws IOException, InvalidAmendmentSpec;

}
