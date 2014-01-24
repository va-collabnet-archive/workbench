package org.ihtsdo.rf2.module.refset.factory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.refset.impl.RF2SimpleRefsetImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.RefSetParam;

/**
 * Title: RF2SimpleRefsetFactory Description: Creating Simple Refset Specific methods Copyright: Copyright (c) 2013 Company: IHTSDO
 * 
 * @author Alejandro Rodriguez
 * @version 1.0
 */

public class RF2SimpleRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2SimpleRefsetFactory.class);

	public RF2SimpleRefsetFactory(Config config) {
		super();
		setConfig(config);
	}

	public void export() {

		try {
			List<RefSetParam> refsets=getConfig().getRefsetData();

			for (RefSetParam refsetData:refsets){

				getConfig().setExportFileName(refsetData.refsetFileName);
				setBufferedWriter();
				if (refsetData.refsetParam!=null && refsetData.refsetParam.size()>0){

					for (RefSetParam refsetData2:refsetData.refsetParam){
						getConfig().setRefsetUuid(refsetData2.refsetUuid);
						getConfig().setRefsetSCTID(refsetData2.refsetSCTId);

						logger.info("Started Simple Refset Export for [" + refsetData2.refsetUuid + "]...");
						RF2SimpleRefsetImpl refsetImpl=new RF2SimpleRefsetImpl(getConfig());
						Terms.get().iterateConcepts(refsetImpl);
						logger.info("Finished Simple Refset Export for [" + refsetData2.refsetUuid + "].");
					}
				}else{

					getConfig().setRefsetUuid(refsetData.refsetUuid);
					getConfig().setRefsetSCTID(refsetData.refsetSCTId);

					logger.info("Started Simple Refset Export for [" + refsetData.refsetUuid + "]...");
					RF2SimpleRefsetImpl refsetImpl=new RF2SimpleRefsetImpl(getConfig());
					Terms.get().iterateConcepts(refsetImpl);
					logger.info("Finished Simple Refset Export for [" + refsetData.refsetUuid + "].");
				}

				closeExportFileWriter();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
