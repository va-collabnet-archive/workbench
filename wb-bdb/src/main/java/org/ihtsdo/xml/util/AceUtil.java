package org.ihtsdo.xml.util;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_TermFactory;

public class AceUtil {

	private static final Logger log = Logger.getLogger(AceUtil.class.getName());
	public static I_TermFactory tf = null;
	//public Hashtable<String,Integer>uidI = null;

	public static void init(I_TermFactory tf1) {
		tf = tf1;
	}

	public static I_TermFactory getTf() {
		if (tf == null) {
			log.severe("TF IS NULL!!!!!");
		}
		return tf;
	}

	public static void setTf(I_TermFactory tf1) {
		tf = tf1;
	}
	
	public static I_GetConceptData getConceptUUID_S_Check(String uuidS)throws Exception{
		if(AceXMLUtil.getUuidInt().containsKey(uuidS)){
			return getConceptInt(Integer.parseInt(AceXMLUtil.getUuidInt().get(uuidS))) ;
		}
		else {
			return getConceptUUID_S(uuidS);
		}
	}
	
	public static I_GetConceptData getConceptUUID_S(String uuidS)throws Exception{
		UUID uid = UUID.fromString(uuidS);
		return getConceptUUID(uid);
		
	}

	public static I_GetConceptData getConceptUUID(UUID uuid) throws Exception {
		getTf();
		I_GetConceptData icd = tf.getConcept(uuid);
		AceXMLUtil.addtoUuidInt(uuid.toString(),icd.getConceptNid());
		//getUidI().put(uuid.toString(), new Integer(icd.getConceptNid()));
		return icd;
	}

	public static I_GetConceptData getConceptInt(int conId) throws Exception {
		getTf();
		return tf.getConcept(conId);
	}

	public static I_GetConceptData getConceptInt_S(String conIdS) throws Exception {
		int conId = Integer.parseInt(conIdS);
		return getConceptInt(conId);
	}
	
	public static int getConId_UUID(String uuidS) throws Exception{
		if(AceXMLUtil.getUuidInt().containsKey(uuidS)){
			return Integer.parseInt(AceXMLUtil.getUuidInt().get(uuidS));
		}
		else{
		return getConceptUUID_S(uuidS).getConceptNid();	
		}
	}

	public static String getIdValById(int id, I_GetConceptData igcd ) {
	String idval = null;
	
	try {
		for(I_IdVersion idv: igcd.getIdentifier().getIdVersions()){
			int id_id = idv.getAuthorityNid();
			if (id_id == id) {	
				idval = idv.getDenotation().toString();
			}
		}
	}
	catch(Exception E) {
		System.out.println("Exception in AceUtil.getIdValById");
		log.severe("Exception in AceUtil.getIdValById");
		E.printStackTrace();
	}
	return idval;
		
	}
	
	public static String getUuidAsString(I_GetConceptData igcd) {
		
		String uuid_S = null;
		try {
			uuid_S = igcd.getUids().iterator().next().toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return uuid_S;
		
	}

}
