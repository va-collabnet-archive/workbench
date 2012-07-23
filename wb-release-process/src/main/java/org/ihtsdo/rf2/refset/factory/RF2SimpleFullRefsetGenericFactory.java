package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2GenericNonAnnotaRefsetImpl;
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
	private List<SctidUuid> sctidUuidList;
	private String uuid;
	private String moduleid;
	private Config config;

	public RF2SimpleFullRefsetGenericFactory(List<SctidUuid> sctidUuidList, Config config, String moduleid) {
		super(config);
		this.config = config;
		this.sctidUuidList = sctidUuidList;
		this.moduleid = moduleid;
	}

	public void export() {

		try {
			logger.info("Started Simple Refset Export ...");
			logger.info("List size: " + sctidUuidList.size());
			for (SctidUuid sctiduuid : sctidUuidList) {
				RF2GenericNonAnnotaRefsetImpl exporter = new RF2GenericNonAnnotaRefsetImpl(this.config, sctiduuid, moduleid);
				exporter.export();
			}
			logger.info("Finished Simple Refset Export.");
			closeExportFileWriter();
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
