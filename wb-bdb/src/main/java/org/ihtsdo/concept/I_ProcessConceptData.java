package org.ihtsdo.concept;

import org.dwfa.ace.api.I_TrackContinuation;

public interface I_ProcessConceptData extends I_TrackContinuation {

	public void processConceptData(Concept concept) throws Exception;

}