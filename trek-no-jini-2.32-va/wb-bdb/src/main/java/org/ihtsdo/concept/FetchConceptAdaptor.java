package org.ihtsdo.concept;

import java.io.IOException;
import java.util.List;
import org.ihtsdo.tk.api.ConceptFetcherBI;

import org.ihtsdo.tk.api.NidBitSetBI;


public class FetchConceptAdaptor implements I_ProcessUnfetchedConceptData {

    private I_ProcessConceptData processor;

    public FetchConceptAdaptor(I_ProcessConceptData processor) {
        super();
        this.processor = processor;
    }

    @Override
    public boolean continueWork() {
        return processor.continueWork();
    }

    @Override
    public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
        // TODO Auto-generated method stub

    }

	@Override
	public NidBitSetBI getNidSet() throws IOException {
		return processor.getNidSet();
	}

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
        processor.processConceptData((Concept) fetcher.fetch());
   }


}
