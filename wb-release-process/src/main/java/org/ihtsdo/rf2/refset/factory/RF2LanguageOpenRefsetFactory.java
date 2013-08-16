package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.mojo.RefSetParam;
import org.ihtsdo.rf2.refset.impl.RF2LanguageImpl;
import org.ihtsdo.rf2.refset.impl.RF2SimpleRefsetImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2LanguageRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2LanguageOpenRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2LanguageOpenRefsetFactory.class);

	public RF2LanguageOpenRefsetFactory(Config config) {
		super();
		setConfig(config);
	} 

	public void export() {

		logger.info("Started Language Refset Export ...");

		try {
			List<RefSetParam> refsets=getConfig().getRefsetData();
			
			for (RefSetParam refsetData:refsets){
				
				getConfig().setExportFileName(refsetData.refsetFileName);
				getConfig().setRefsetUuid(refsetData.refsetUuid);
				getConfig().setRefsetSCTID(refsetData.refsetSCTId);

				setBufferedWriter();
				
				logger.info("Started Language Refset Export for [" + refsetData.refsetUuid + "]...");
				int refsetNid=Terms.get().getConcept(UUID.fromString(refsetData.refsetUuid)).getNid();
				
				RF2LanguageImpl refsetImpl=new RF2LanguageImpl(getConfig(),refsetNid,refsetData.refsetSCTId);
				Terms.get().iterateConcepts(refsetImpl);
				logger.info("Finished Language Refset Export for [" + refsetData.refsetUuid + "].");
				
				closeExportFileWriter();
			}

			logger.info("Finished Language Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
