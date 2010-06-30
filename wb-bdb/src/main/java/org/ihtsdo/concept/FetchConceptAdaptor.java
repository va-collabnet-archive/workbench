package org.ihtsdo.concept;

import java.util.List;


public class FetchConceptAdaptor implements I_ProcessUnfetchedConceptData {

    private I_ProcessConceptData processor;

    public FetchConceptAdaptor(I_ProcessConceptData processor) {
        super();
        this.processor = processor;
    }

    @Override
    public void processUnfetchedConceptData(int cNid, I_FetchConceptFromCursor fcfc) throws Exception {
        processor.processConceptData(fcfc.fetch());
    }

    @Override
    public boolean continueWork() {
        return processor.continueWork();
    }

    @Override
    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
        // TODO Auto-generated method stub
        
    }

}
