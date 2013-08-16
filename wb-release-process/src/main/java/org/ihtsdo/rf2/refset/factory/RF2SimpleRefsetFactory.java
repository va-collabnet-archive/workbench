package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.mojo.RefSetParam;
import org.ihtsdo.rf2.refset.impl.RF2NonHumanImpl;
import org.ihtsdo.rf2.refset.impl.RF2SimpleRefsetImpl;
import org.ihtsdo.rf2.refset.impl.RF2VMPImpl;
import org.ihtsdo.rf2.refset.impl.RF2VTMImpl;
import org.ihtsdo.rf2.util.Config;

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
				getConfig().setRefsetUuid(refsetData.refsetUuid);
				getConfig().setRefsetSCTID(refsetData.refsetSCTId);

				setBufferedWriter();
				
				logger.info("Started Simple Refset Export for [" + refsetData.refsetUuid + "]...");
				RF2SimpleRefsetImpl refsetImpl=new RF2SimpleRefsetImpl(getConfig());
				Terms.get().iterateConcepts(refsetImpl);
				logger.info("Finished Simple Refset Export for [" + refsetData.refsetUuid + "].");
				
				closeExportFileWriter();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
}
