package org.ihtsdo.tk.api;

import java.util.concurrent.CountDownLatch;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface KindOfCacheBI {

	public abstract void setup(ViewCoordinate coordinate) throws Exception;

	public abstract boolean isKindOf(int childNid, int parentNid)
			throws Exception;
	
	public CountDownLatch getLatch();
        
        public void updateCache(ConceptChronicleBI c) throws Exception;
    

}