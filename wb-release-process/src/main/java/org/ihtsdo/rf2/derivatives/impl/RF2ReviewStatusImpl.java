package org.ihtsdo.rf2.derivatives.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.concept.component.refsetmember.cid.CidMember;
import org.ihtsdo.concept.component.refsetmember.cid.CidRevision;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.api.Precedence;

/**
 * Title: RF2ConceptInactivationImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 ConceptInactivation Refset File Copyright: Copyright (c)
 * 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 * 
 */

public class RF2ReviewStatusImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2ReviewStatusImpl.class);
	private static int recordCounter = 0;

	public RF2ReviewStatusImpl(Config config) {
		super(config);
	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String referencedComponentId) {
		try {
			String effectiveTime = "";
			String conceptStatus = "";
			String valueId = "";
			String active = "";
			CidMember extensionPart;
			int extensionStatusId = 0;


			String refsetId = I_Constants.GMDN_REVIEW_STATUS_REFSET_ID;
			String moduleId = I_Constants.GMDN_MODULE_ID;
			int reviewStatusRefsetNid = getNid(I_Constants.GMDN_REVIEW_STATUS_REFSET_UID);

			UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);
			Date PREVIOUSRELEASEDATE = getDateFormat().parse(I_Constants.inactivation_policy_change);

			List<? extends I_ConceptAttributeTuple> conceptAttributes = concept.getConceptAttributeTuples(
					allStatuses, 
					currenAceConfig.getViewPositionSetReadOnly(), 
					Precedence.PATH, currenAceConfig.getConflictResolutionStrategy());
			active="0";
			if (conceptAttributes.size() > 0) {
				for (int i = 0; i < conceptAttributes.size(); i++) {
					I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributes.get(i);
					conceptStatus = getStatusType(i_ConceptAttributeTuple.getStatusNid());
					Date et = new Date(getTermFactory().convertToThickVersion(i_ConceptAttributeTuple.getVersion()));
					effectiveTime = getDateFormat().format(et);
					if (conceptStatus.equals("0")) {
						active = "1";
					} else if (getConfig().getReleaseDate().compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
						active = "1";
					} else {
						active = "0";
					}
				}
			}
			List<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getNid(), true);

			if (!extensions.isEmpty()) {
				for (I_ExtendByRef extension : extensions) {
					if (extension.getRefsetId() == reviewStatusRefsetNid) {
						long lastVersion = Long.MIN_VALUE;
						extensionPart=null;
						int conceptVal=-1;
						for (I_ExtendByRefVersion loopTuple : extension.getTuples(allStatusSet,currenAceConfig.getViewPositionSetReadOnly(),
								Precedence.PATH,currenAceConfig.getConflictResolutionStrategy())) {

							if (loopTuple.getTime() >= lastVersion) {
								lastVersion = loopTuple.getTime();
								if (loopTuple.getMutablePart() instanceof CidMember){
									extensionPart = (CidMember) loopTuple.getMutablePart();
									extensionStatusId = extensionPart.getStatusNid();
									conceptVal= extensionPart.getC1Nid();
								}else if (loopTuple.getMutablePart() instanceof CidRevision){
									CidRevision extensionRevPart = (CidRevision) loopTuple.getMutablePart();
									extensionStatusId = extensionRevPart.getStatusNid();
									conceptVal= extensionRevPart.getC1id();
								}
							}
						}
						if (conceptVal==-1) {
							if (logger.isDebugEnabled()) {
								logger.debug("Refset extension part not found!");
							}
						}else{								
							if (extensionStatusId == activeNid && active.equals("1")) { 														
								active = "1";
							} else if (extensionStatusId == inactiveNid) { 														
								active = "0";
							} else {
								active = "0";
								logger.error("refset member active and concept inactive : =====>" + concept);
							}
							valueId= tf.getConcept(conceptVal).getUUIDs().iterator().next().toString();

							WriteRF2TypeLine(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportConcept: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void WriteRF2TypeLine(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");
	}

}
