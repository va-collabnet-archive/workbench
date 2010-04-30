package org.ihtsdo.concept;

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

}
