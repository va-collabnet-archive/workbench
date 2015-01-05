package org.ihtsdo.rf2.module.refset.factory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.refset.impl.RF2LanguageImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.RefSetParam;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2LanguageRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO.
 *
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2LanguageOpenRefsetFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2LanguageOpenRefsetFactory.class);

	/**
	 * Instantiates a new r f2 language open refset factory.
	 *
	 * @param config the config
	 */
	public RF2LanguageOpenRefsetFactory(Config config) {
		super();
		setConfig(config);
	} 

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
	public void export() {

		logger.info("Started Language Refset Export ...");

		try {
			List<RefSetParam> refsets=getConfig().getRefsetData();

			for (RefSetParam refsetData:refsets){

				getConfig().setExportFileName(refsetData.refsetFileName);
				setBufferedWriter();
				if (refsetData.refsetParam!=null && refsetData.refsetParam.size()>0){

					for (RefSetParam refsetData2:refsetData.refsetParam){

						getConfig().setRefsetUuid(refsetData2.refsetUuid);
						getConfig().setRefsetSCTID(refsetData2.refsetSCTId);

						logger.info("Started Language Refset Export for [" + refsetData2.refsetUuid + "]...");
						int refsetNid=Terms.get().getConcept(UUID.fromString(refsetData2.refsetUuid)).getNid();

						RF2LanguageImpl refsetImpl=new RF2LanguageImpl(getConfig(),refsetNid,refsetData2.refsetSCTId);
						Terms.get().iterateConcepts(refsetImpl);
						logger.info("Finished Language Refset Export for [" + refsetData2.refsetUuid + "].");
					}
				}else{
					getConfig().setRefsetUuid(refsetData.refsetUuid);
					getConfig().setRefsetSCTID(refsetData.refsetSCTId);

					logger.info("Started Language Refset Export for [" + refsetData.refsetUuid + "]...");
					int refsetNid=Terms.get().getConcept(UUID.fromString(refsetData.refsetUuid)).getNid();

					RF2LanguageImpl refsetImpl=new RF2LanguageImpl(getConfig(),refsetNid,refsetData.refsetSCTId);
					Terms.get().iterateConcepts(refsetImpl);
					logger.info("Finished Language Refset Export for [" + refsetData.refsetUuid + "].");

				}
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
