package org.ihtsdo.mojo.mojo.exporter.publishers;

import java.util.Hashtable;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;

public interface I_PublishConcepts  {
	
	public boolean init(PublisherDTO pdto,I_TermFactory tf,Hashtable<String,Integer>uidI);
	
	public String getErrorMsg();
	
	public boolean isPdtoOK();
	
	public int getfoundConceptCount();
	
	public int getConID();
	
	public String getConUUID();
	
	public PublisherDTO getPubDTO();
	
	public Integer getIntKey();
	
	public void localProcessConcept(I_GetConceptData concept)throws Exception ;

}
