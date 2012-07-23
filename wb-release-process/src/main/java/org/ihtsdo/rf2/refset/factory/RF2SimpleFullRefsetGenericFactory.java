package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2GenericRefsetImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2LanguageRefsetFactory Description: Creating SnomedId & Ctv3Id
 * Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2SimpleFullRefsetGenericFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2SimpleFullRefsetGenericFactory.class);
	private String sctid;
	private String uuid;
	private String moduleid;

	public RF2SimpleFullRefsetGenericFactory(SctidUuid sctidUuid, Config config, String moduleid) {
		super(config);
		this.sctid = sctidUuid.getSctid();
		this.uuid = sctidUuid.getUuid();
		this.moduleid = moduleid;
	}

	public void export() {

		try {
			logger.info("Started Simple Refset Export ...");
			RF2GenericRefsetImpl vtmUSIterator = new RF2GenericRefsetImpl(getConfig(), sctid, uuid, moduleid);
			Terms.get().iterateConcepts(vtmUSIterator);
			logger.info("Finished Simple Refset Export.");
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
