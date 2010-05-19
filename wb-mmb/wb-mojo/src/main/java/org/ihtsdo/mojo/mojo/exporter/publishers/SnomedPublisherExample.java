package org.ihtsdo.mojo.mojo.exporter.publishers;

/**
 * An Example publisher with just the 3 basic methods you would have to implement
 */

import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;

public class SnomedPublisherExample extends BasicPublisher {
	private static final Logger log = Logger.getLogger(SnomedPublisherExample.class.getName());
	
	private String errMsg = "";

	public boolean localSetUpProps(){
		boolean localPropsOK = true;
		
		return localPropsOK;
	}
	
	public void localProcessConceptBean(I_GetConceptData cb){
		if(foundConCount == 1000){
		log.severe("SnoMed num found = "+foundConCount);	
		}
		
	}
	
	public String getErrorMsg(){
		return errMsg;
	}

	

}
