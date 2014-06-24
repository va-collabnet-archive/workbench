package org.ihtsdo.mojo.mojo.exporter;

import java.io.IOException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.mojo.mojo.exporter.publishers.I_PublishConcepts;

/**
 * The job of the controller is to pass the concept to the relevant publisher
 */
public class publisherController implements I_ProcessConcepts{
	
	private static final Logger log = Logger.getLogger(publisherController.class.getName());
	public Hashtable<Integer,I_PublishConcepts> classCache = new Hashtable();
	public int conCount = 0;
	
	public I_PublishConcepts checkID(I_GetConceptData concept){
		I_PublishConcepts pubC = null;
		try {
			for(I_IdVersion idv: concept.getIdentifier().getIdVersions()){
				int id_id = idv.getAuthorityNid();
				if(classCache.containsKey(Integer.valueOf(id_id))){
					return classCache.get(Integer.valueOf(id_id));
			}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE,"checkID error thrown err = ", e);
		}
		return pubC;
	}

	
	public void processConcept(I_GetConceptData arg0) throws Exception {
		conCount++;
		I_PublishConcepts pubC = checkID(arg0);
		if(pubC != null){
			pubC.localProcessConcept(arg0);
			//logProgress(pubC);
		}
	}
	
	public void logProgress(I_PublishConcepts pubC){
		log.severe("Processed con id = "+pubC.getConID()+ " uuid = "+pubC.getConUUID() +" Type = "+pubC.getPubDTO().getName()+" foundConCount = "+pubC.getfoundConceptCount() + " Total processed so far = "+conCount);
	}


	public Hashtable<Integer, I_PublishConcepts> getClassCache() {
		return classCache;
	}
	public void setClassCache(Hashtable<Integer, I_PublishConcepts> classCache) {
		this.classCache = classCache;
	}

	public int getConCount() {
		return conCount;
	}

	public void setConCount(int conCount) {
		this.conCount = conCount;
	}

}
