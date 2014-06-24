package org.ihtsdo.mojo.mojo.exporter.publishers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;


public class BasicPublisher implements I_PublishConcepts {
	
	private static final Logger log = Logger.getLogger(BasicPublisher.class.getName());
	
	public String id_uuid = null;
	public int id_int = -1;
	public PublisherDTO pubDTO;
	public boolean pdtoOK = false;


	public I_TermFactory tf = null;
	public int foundConCount = 0;
	
	public int con_id;
	public String uuid_s = null;
	
	public Hashtable<String,Integer>uidI = new Hashtable();
	
	public Integer getIntKey(){
		 return Integer.valueOf(id_int);
		
	}
	
	
	public int getfoundConceptCount(){
		return foundConCount;
	}
	
	public int getConID(){
		return con_id;
	}
	
	public String getConUUID(){
		return uuid_s;
	}
	
	public boolean init(PublisherDTO pdto,I_TermFactory tf,Hashtable<String,Integer>uidI){
		/*String sep = System.getProperty("file.separator");
		if (outputDirectory.endsWith(sep) == false) {
			outputDirectory = outputDirectory + sep;
		}*/
		this.tf = tf;
		this.uidI = uidI;
		this.pubDTO = pdto;
		checkPDTO();
		try {
			getId_int();
		} catch (Exception e) {
			log.log(Level.SEVERE,"getId_int error thrown err = ", e);
			pdtoOK = false;
		}
		return pdtoOK;
	}
	
	
	public void checkPDTO(){
		pdtoOK = pubDTO.isPdtoOK();
		if(pdtoOK){
		pdtoOK = localSetUpProps();
		}
		
	}

	/**
	 * Possibly to be over ridden. Basically get the concept bean and calls localProcessConceptBean
	 * @param concept
	 * @throws Exception
	 */
	public void localProcessConcept(I_GetConceptData concept) throws Exception {
		con_id = concept.getConceptNid();
		if(con_id != -1){
			foundConCount++;
		 try {
			 I_GetConceptData cb = Terms.get().getConcept(con_id);	 
			 uuid_s = cb.getUids().iterator().next().toString();
			 localProcessConceptBean(cb);
		    } catch (IOException e) {
		    	e.printStackTrace();
			}
		}
	}
	
	/**
	 * Designed to be over ridden by a subclass
	 * @param cb
	 */
	public void localProcessConceptBean(I_GetConceptData cb){
		log.severe("Basic Publisher localProcessConceptBean called. This should be subclassed!!!");
	}
	

	/**
	 * Designed to be over ridden by a subclass
	 * This is where the designer of the specific publisher would check configProps to see that everything has been set OK
	 * @return
	 */
	
	public boolean localSetUpProps(){
		boolean localPropsOK = false;
		return localPropsOK;
	}

	public int getId_int() throws Exception{	
		
		if(pubDTO.getId_uuidS() != null){
			log.severe("getId_int Publisher name ="+pubDTO.getName() +" pubDTO.getId_uuidS() = "+pubDTO.getId_uuidS());
			id_int = getConceptUUID_S(pubDTO.getId_uuidS()).getConceptNid();	
			log.severe("getId_int id_int = "+id_int);
		}
		return id_int;
	}

	public void setId_int(int idInt) {
		id_int = idInt;
	}
	
	public String getErrorMsg(){
		
		String BasicPubErrS = " This is the BasicPublisher error message";
		
		return BasicPubErrS;
		
	}
	
	//Convenience Concept getting methods
	
	public I_GetConceptData getConceptUUID_S(String uuidS)throws Exception{
		if(uidI.containsKey(uuidS)){
			return getConceptInt(uidI.get(uuidS).intValue()) ;
		}
		else{UUID uid = UUID.fromString(uuidS);
		I_GetConceptData con = getConceptUUID(uid);
		uidI.put(uuidS, new Integer(con.getConceptNid()));
		log.severe("uidI adding "+uuidS +" size = "+uidI.size());
		return con;
		}
	}
	public I_GetConceptData getConceptUUID(UUID uuid)throws Exception{
		if(tf == null){
			log.severe("tf is NULL");
		}
	return tf.getConcept(uuid);
	}
	
	public I_GetConceptData getConceptInt(int conId)throws Exception{
		return tf.getConcept(conId);
		}
	public I_GetConceptData getConceptInt_S(String conIdS)throws Exception{
		int conId = Integer.parseInt(conIdS);
		return tf.getConcept(conId);
		}
	
	public int getConId_UUID(String uuidS) throws Exception{
		
		if(uidI.containsKey(uuidS)){
			return uidI.get(uuidS).intValue();
		}
		else{
		return getConceptUUID_S(uuidS).getConceptNid();
		}
	}

	public String getId_uuid() {
		return id_uuid;
	}

	public void setId_uuid(String idUuid) {
		id_uuid = idUuid;
	}

	public I_TermFactory getTf() {
		return tf;
	}

	public void setTf(I_TermFactory tf) {
		this.tf = tf;
	}
	
	public PublisherDTO getPubDTO() {
		return pubDTO;
	}

	public void setPubDTO(PublisherDTO pubDTO) {
		this.pubDTO = pubDTO;
	}	
	
	public boolean isPdtoOK() {
		return pdtoOK;
	}

	public void setPdtoOK(boolean pdtoOK) {
		this.pdtoOK = pdtoOK;
	}
	
	

}
