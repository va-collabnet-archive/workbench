package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.dao.RefsetConceptDAO;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2DescriptionInactivationImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 ConceptInactivation Refset File Copyright: Copyright
 * (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 * 
 */

public class RF2DescriptionInactivationImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2DescriptionInactivationImpl.class);

	public RF2DescriptionInactivationImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		try {
			String effectiveTime = "";
			String valueId = "";

			int descInactivationRefsetNid = getNid(I_Constants.DESCRIPTION_INACTIVATION_REFSET_UID);
			String refsetId = getSctId(descInactivationRefsetNid, getSnomedCorePathNid());
			String moduleId = I_Constants.CORE_MODULE_ID;
			List<? extends I_DescriptionTuple> descriptions = concept.getDescriptionTuples(allStatuses, 
					allDescTypes, currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());

			for (I_DescriptionTuple description: descriptions) {
				String sDescType = getSnomedDescriptionType(description.getTypeNid());
				Date descriptionEffectiveDate = new Date(getTermFactory().convertToThickVersion(description.getVersion()));
				effectiveTime = getDateFormat().format(descriptionEffectiveDate);

				if (!sDescType.equals("4") && !description.getLang().equals("es") && !effectiveTime.contains("1031") && !effectiveTime.contains("0430"))  { // Ignore text-defination & spanish description
					String referencedComponentId = getDescriptionId(description.getDescId(), ExportUtil.getSnomedCorePathNid());

					if (referencedComponentId==null || referencedComponentId.equals("")){
						referencedComponentId=Terms.get().getUids(description.getDescId()).iterator().next().toString();
					}
					UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);

					valueId = getDescInactivationValueId(description.getStatusNid());
					
					if ( !valueId.equals("XXX")) {
						writeRF2TypeLine(uuid, effectiveTime, "1", moduleId, refsetId, referencedComponentId, valueId);

					} else {
						writeRF2TypeLine(uuid, effectiveTime, "0", moduleId, refsetId, referencedComponentId, "");

					} 
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			logger.error("==========conceptid==========" + conceptid);
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void writeRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {

		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");

	}
}
