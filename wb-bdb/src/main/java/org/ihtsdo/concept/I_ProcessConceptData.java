package org.ihtsdo.concept;

import java.io.IOException;

import org.dwfa.ace.api.I_TrackContinuation;
import org.ihtsdo.tk.api.NidBitSetBI;

public interface I_ProcessConceptData extends I_TrackContinuation {

	public void processConceptData(Concept concept) throws Exception;
	
    public NidBitSetBI getNidSet() throws IOException;


}