package org.ihtsdo.rf2.module.refset.factory;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.module.constant.I_Constants;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.refset.impl.RF2Ctv3IdImpl;
import org.ihtsdo.rf2.module.refset.impl.RF2SimpleMapOpenImpl;
import org.ihtsdo.rf2.module.refset.impl.RF2SnomedIdImpl;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.RefSetParam;

/**
 * Title: RF2SimpleMapRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez
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
				setBufferedWriter();
				if (refsetData.refsetParam!=null && refsetData.refsetParam.size()>0){

					for (RefSetParam refsetData2:refsetData.refsetParam){

						getConfig().setRefsetUuid(refsetData2.refsetUuid);
						getConfig().setRefsetSCTID(refsetData2.refsetSCTId);

						logger.info("Started Simple Map Refset Export for [" + refsetData2.refsetUuid + "]...");
						if (refsetData2.refsetSCTId.equals(I_Constants.SNOMED_REFSET_ID)){
							RF2SnomedIdImpl snomedIdIterator = new RF2SnomedIdImpl(getConfig());
							Terms.get().iterateConcepts(snomedIdIterator);
						}else if (refsetData2.refsetSCTId.equals(I_Constants.CTV3_REFSET_ID)){
							RF2Ctv3IdImpl ctv3IdIterator = new RF2Ctv3IdImpl(getConfig());
							Terms.get().iterateConcepts(ctv3IdIterator);
						}else{
							RF2SimpleMapOpenImpl refsetImpl=new RF2SimpleMapOpenImpl(getConfig());
							Terms.get().iterateConcepts(refsetImpl);
						}
						logger.info("Finished Simple Map Refset Export for [" + refsetData2.refsetUuid + "].");
					}
				}else{
					getConfig().setRefsetUuid(refsetData.refsetUuid);
					getConfig().setRefsetSCTID(refsetData.refsetSCTId);

					logger.info("Started Simple Map Refset Export for [" + refsetData.refsetUuid + "]...");
					if (refsetData.refsetSCTId.equals(I_Constants.SNOMED_REFSET_ID)){
						RF2SnomedIdImpl snomedIdIterator = new RF2SnomedIdImpl(getConfig());
						Terms.get().iterateConcepts(snomedIdIterator);
					}else if (refsetData.refsetSCTId.equals(I_Constants.CTV3_REFSET_ID)){
						RF2Ctv3IdImpl ctv3IdIterator = new RF2Ctv3IdImpl(getConfig());
						Terms.get().iterateConcepts(ctv3IdIterator);
					}else{
						RF2SimpleMapOpenImpl refsetImpl=new RF2SimpleMapOpenImpl(getConfig());
						Terms.get().iterateConcepts(refsetImpl);
					}
					logger.info("Finished Simple Map Refset Export for [" + refsetData.refsetUuid + "].");

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
