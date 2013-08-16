package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.identifier.mojo.RefSetParam;
import org.ihtsdo.rf2.refset.impl.RF2SimpleMapOpenImpl;
import org.ihtsdo.rf2.util.Config;

/**
 * Title: RF2SimpleMapRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

//This refset contains SnomedId and Ctv3Id and VTM-VMP and Non-Human and ICDO Refset.

public class RF2SimpleMapOpenRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2SimpleMapOpenRefsetFactory.class);

	public RF2SimpleMapOpenRefsetFactory(Config config) {
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
				
				logger.info("Started Simple Map Refset Export for [" + refsetData.refsetUuid + "]...");
				RF2SimpleMapOpenImpl refsetImpl=new RF2SimpleMapOpenImpl(getConfig());
				Terms.get().iterateConcepts(refsetImpl);
				logger.info("Finished Simple Map Refset Export for [" + refsetData.refsetUuid + "].");
				
				closeExportFileWriter();
			}

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
	}
}
