package org.ihtsdo.rf2.refset.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.rf2.impl.RF2AbstractImpl;
import org.ihtsdo.rf2.refset.factory.SctidUuid;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.WriteUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;

/**
 * Title: RF2DescriptionImpl Description: Iterating over all the concept in
 * workbench and fetching all the components required by RF2 Description File
 * Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2GenericNonAnnotaRefsetImpl extends RF2AbstractImpl {

	private static Logger logger = Logger.getLogger(RF2GenericNonAnnotaRefsetImpl.class);
	private SctidUuid sctidUuid;
	private String moduleid;

	public RF2GenericNonAnnotaRefsetImpl(Config config, SctidUuid sctidUuid, String moduleid) {
		super(config);
		this.sctidUuid = sctidUuid;
		this.moduleid = moduleid;
		tf = Terms.get();
	}

	public void export() throws IOException {
		String effectiveTime = "";
		UUID refsetuuid = null;

		try {
			tf.uuidToNative(UUID.fromString(sctidUuid.getUuid()));

			ConceptVersionBI concept = Ts.get().getConceptVersion(tf.getActiveAceFrameConfig().getViewCoordinate(), UUID.fromString(sctidUuid.getUuid()));
			Collection<? extends RefexVersionBI<?>> refsetMembers = concept.getCurrentRefsetMembers(tf.getActiveAceFrameConfig().getViewCoordinate());
			for (RefexVersionBI<?> refexVersionBI : refsetMembers) {
				logger.info("Enclosing concept " + refexVersionBI.getEnclosingConcept());
				logger.info("Enclosing concept " + refexVersionBI.getConceptNid());
				logger.info("Enclosing concept " + refexVersionBI.getNid());
				logger.info("Enclosing concept " + refexVersionBI.getEnclosingConcept().getConceptNid());
				ConceptChronicleBI concept2 = null;
				try {
					concept2 = Ts.get().getConceptForNid(refexVersionBI.getReferencedComponentNid());
				} catch (Exception e) {
				}
				if (concept2 == null) {
					I_DescriptionVersioned desc = tf.getDescription(refexVersionBI.getReferencedComponentNid());
					if (desc != null) {
						logger.info("Description: " + desc.getConceptNid());
						logger.info("Description: " + desc.getDescId());
					}
					concept2 = Ts.get().getConceptForNid(desc.getDescId());
				}
				if (concept2 != null) {
					String conceptid = getConceptId(concept2);
					refsetuuid = Type5UuidFactory.get(sctidUuid.getSctid() + conceptid);
					effectiveTime = getDateFormat().format(new Date(refexVersionBI.getTime()));
					writeRF2TypeLine(refsetuuid, effectiveTime, "1", moduleid, sctidUuid.getUuid(), conceptid);
				}else{
					logger.info("CONCEPT NOT FOUND");
				}
			}
		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.error("Exceptions in exportDescription: " + e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void writeRF2TypeLine(UUID refsetuuid, String effectiveTime, String active, String moduleId, String refsetId, String conceptid) throws IOException {

		WriteUtil.write(getConfig(), refsetuuid + "\t" + effectiveTime + "\t" + active + "\t" + moduleId + "\t" + refsetId + "\t" + conceptid);
		WriteUtil.write(getConfig(), "\r\n");

	}

	@Override
	public void export(I_GetConceptData concept, String conceptid) throws IOException {

	}
}
