package org.ihtsdo.rf2.identifier.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

public class RF2ConceptWOmapImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ConceptWOmapImpl.class);
	
	public RF2ConceptWOmapImpl (Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) {
		String conceptStatus = "";
		String active = "";
		
		try {
		
			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			if (conceptAttributes != null && !conceptAttributes.isEmpty()) {
				I_ConceptAttributeTuple attributes = conceptAttributes.iterator().next();
				
				conceptStatus = getStatusType(attributes.getStatusNid());
				if (conceptStatus.equals("0")) {
					active = "1";
				} else {
					active = "0";
				}
				String snomedid="";
				String ctv3id="";
				
				if (ExportUtil.isInKList(conceptid)){
					UUID uuid=concept.getUids().iterator().next();
					if (active.equals("1")){
						ctv3id=this.getCTV3ID(getConfig(), uuid);
						String parnSID=this.getParentSnomedId(concept);
						snomedid=this.getSNOMEDID(getConfig(), uuid, parnSID);
					}
					writeRF2TypeLine(uuid.toString(), conceptid,active,ctv3id,snomedid);
				}
				
			}
		}catch (NullPointerException ne) {
			ne.printStackTrace();
			logger.error("NullPointerException: " + ne.getMessage());
			logger.error(" NullPointerException " + conceptid);
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			logger.error("IOExceptions: " + conceptid);
		} catch (Exception e) {
			logger.error("Exceptions in exportConcept: " + e.getMessage());
			logger.error("Exceptions in exportConcept: " +conceptid);
		}

	}


	public static void writeRF2TypeLine(String uuid, String conceptid, String active, String ctv3id, String snomedid) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + conceptid  + "\t" + active + "\t" + ctv3id + "\t" + snomedid);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
