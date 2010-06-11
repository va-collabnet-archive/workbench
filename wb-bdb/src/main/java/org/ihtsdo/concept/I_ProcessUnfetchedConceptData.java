package org.ihtsdo.concept;

import org.dwfa.ace.api.I_TrackContinuation;

public interface I_ProcessUnfetchedConceptData extends I_TrackContinuation {

    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception;

}
