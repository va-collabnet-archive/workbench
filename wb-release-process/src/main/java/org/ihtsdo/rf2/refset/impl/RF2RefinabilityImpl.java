package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.dao.RefsetConceptDAO;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;

/**
 * Title: RF2RefinabilityImpl Description: Iterating over all the concept in workbench and fetching all the components required by RF2 Refinability Refset File Copyright: Copyright (c) 2010
 * Company:IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2RefinabilityImpl extends RF2AbstractImpl implements I_ProcessConcepts {

	private static Logger logger = Logger.getLogger(RF2RefinabilityImpl.class);

	public RF2RefinabilityImpl(Config config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.dwfa.ace.api.I_ProcessConcepts#processConcept(org.dwfa.ace.api. I_GetConceptData)
	 */
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		process(concept);

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) throws IOException {
		try {
			String effectiveTime = "";
			String referencedComponentId = "";
			String refinability = "";
			String status = "";
			String relTypeId = "";
			String active = "";
			String characteristicTypeId = "";
			String valueId = "";

			int refinabilityRefsetNid = getNid(I_Constants.REFINIBILITY_REFSET_UID);
			String refsetId = getSctId(refinabilityRefsetNid, getSnomedCorePathNid());

			// UUID uuid = UUID.randomUUID();
			//String moduleId = getMetaModuleID(concept);
			String moduleId = I_Constants.CORE_MODULE_ID;
			Collection<? extends I_RelVersioned> rels = concept.getSourceRels();
			if (!rels.isEmpty()) {
				for (I_RelVersioned<?> rel : rels) {
					if (rel.getAuthorNid() == getSnorocketAuthorNid()) {
						List<? extends I_RelPart> relVers = rel.getMutableParts();
						if (!relVers.isEmpty()) {
							String priorActive = "X";
							String priorValueId = "XX";
							String priorQualifier = "0";
							int firstRowInserted = 0;
							for (I_RelPart relVer : relVers) {
								// int relPathId = relVer.getPathNid();
								// if (relPathId == getSnomedCorePathNid() || relPathId == getSnomedInferredPathNid()) {
								referencedComponentId = getSctId(rel.getRelId(), relVer.getPathNid());
								UUID uuid = Type5UuidFactory.get(refsetId + referencedComponentId);
								String characteristicType = getCharacteristicType(relVer.getCharacteristicId());

								characteristicTypeId = getCharacteristicTypeId(characteristicType);

								refinability = getRefinabilityType(relVer.getRefinabilityId());
								valueId = getRefinabilityValueId(Integer.parseInt(refinability));
								status = getRefinabilityStatusType(relVer.getStatusNid());

								if (status.equals("0")) {
									active = "1";
								} else {
									active = "0";
								}
								Date date = new Date(getTermFactory().convertToThickVersion(relVer.getVersion()));
								effectiveTime = getDateFormat().format(date);
								relTypeId = getSctId(relVer.getTypeNid(), getSnomedCorePathNid());
								if (relTypeId.equals(I_Constants.MAY_BE) || relTypeId.equals(I_Constants.WAS_A) || relTypeId.equals(I_Constants.SAME_AS) || relTypeId.equals(I_Constants.REPLACED_BY)
										|| relTypeId.equals(I_Constants.MOVED_FROM) || relTypeId.equals(I_Constants.MOVED_TO)) {
									characteristicTypeId = I_Constants.HISTORICAL;
								}

								// We need to capture relationshipid's state only when they are qualifier
								// Do not output Hisorical or any other relationships
								if (getConfig().getRf2Format().equals("true") && firstRowInserted == 0 && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)) {
									insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
									priorValueId = valueId;
									priorQualifier = "1";
									priorActive = active;
									firstRowInserted = 1;
								} else if (getConfig().getRf2Format().equals("true") && firstRowInserted == 1) {
									if (priorActive.equals("1") && active.equals("0") && priorQualifier.equals("1") && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)
											&& valueId == priorValueId) {
										// Inactivation row
										insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, priorValueId);
										priorActive = active;
									} else if (priorActive.equals("1") && active.equals("1") && priorQualifier.equals("1") && !characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)) {
										// Inactivation row
										priorQualifier = "0";
										priorActive = "0";
										insertRefsetRow(uuid, effectiveTime, priorActive, moduleId, refsetId, referencedComponentId, priorValueId);
									} else if (priorActive.equals("1") && active.equals("1") && priorQualifier.equals("1") && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)
											&& valueId != priorValueId) {
										// Insert new row
										insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
										priorValueId = valueId;
										priorQualifier = "1";
									} else if (priorActive.equals("0") && active.equals("1") && priorQualifier.equals("0") && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)) {
										// Insert new row
										insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
										priorValueId = valueId;
										priorQualifier = "1";
										priorActive = active;
									} else if (priorActive.equals("0") && active.equals("1") && priorQualifier.equals("1") && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)) {
										// Insert new row
										insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
										priorValueId = valueId;
										priorActive = active;
										priorQualifier = "1";
									} else if (priorActive.equals("1") && active.equals("0")) {
										// Inactivation row
										insertRefsetRow(uuid, effectiveTime, active, moduleId, refsetId, referencedComponentId, valueId);
										priorValueId = valueId;
										priorActive = active;
									} else if (priorActive.equals("1") && active.equals("0") && priorQualifier.equals("1") && characteristicTypeId.equalsIgnoreCase(I_Constants.QUALIFYRELATION)
											&& valueId != priorValueId) {
										logger.error("<====================Illegal State========================>");
									}
									// if( active != priorActive || valueId != priorValueId ){ }
								}
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("IOExceptions: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exceptions in exportRefinability: " + e.getMessage());
			logger.error("==========sourceid==========" + conceptid);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void insertRefsetRow(UUID uuid, String effectiveTime, String active, String moduleId, String refsetId, String referencedComponentId, String valueId) throws IOException {
		// Inactivation row
		WriteUtil.write(getConfig(), uuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + referencedComponentId + "\t" + valueId);
		WriteUtil.write(getConfig(), "\r\n");
	}

	private void writeRF2TypeLine(RefsetConceptDAO dao) throws IOException {

		WriteUtil.write(getConfig(), dao.getUuid() + "\t" + dao.getEffectiveTime() + "\t" + dao.getActive() + "\t" + dao.getModuleId() + "\t" + dao.getRefsetId() + "\t"
				+ dao.getReferencedComponentId() + "\t" + dao.getValueId());
		WriteUtil.write(getConfig(), "\r\n");

	}
}
