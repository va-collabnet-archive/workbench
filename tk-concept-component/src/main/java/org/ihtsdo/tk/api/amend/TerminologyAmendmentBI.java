package org.ihtsdo.tk.api.amend;

import java.io.IOException;

import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public interface TerminologyAmendmentBI {

	/**
	 *  
	 * @param res
	 * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RefexChronicleBI<?> amend(RefexAmendmentSpec spec) throws IOException, InvalidAmendmentSpec;
	
	/**
	 *  This method incurs an extra cost to determine if a current version already meets the specification. 
	 * @param res
	 * @return A <code>RefexChronicleBI</code> if the <code>spec</code> regardless of if the Refex was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RefexChronicleBI<?> amendIfNotCurrent(RefexAmendmentSpec spec) throws IOException, InvalidAmendmentSpec;
        
        
	/**
	 *  
	 * @param res
	 * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RelationshipChronicleBI amend(RelAmendmentSpec spec) throws IOException, InvalidAmendmentSpec;
	
	/**
	 *  This method incurs an extra cost to determine if a current version already meets the specification. 
	 * @param res
	 * @return A <code>RelationshipChronicleBI</code> if the <code>spec</code> regardless of if the RelationshipChronicleBI was modified. 
	 * @throws IOException
	 * @throws InvalidAmendmentSpec
	 */
	RelationshipChronicleBI amendIfNotCurrent(RelAmendmentSpec spec) throws IOException, InvalidAmendmentSpec;
        
        

}
