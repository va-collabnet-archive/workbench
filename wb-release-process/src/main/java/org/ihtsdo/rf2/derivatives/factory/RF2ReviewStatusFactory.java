package org.ihtsdo.rf2.derivatives.factory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.derivatives.impl.RF2ReviewStatusImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2TextDefinitionFactory Description: Creating TextDefinitionFactory Specific methods required by DescriptionIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2ReviewStatusFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2ReviewStatusFactory.class);

	public RF2ReviewStatusFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		try {
			RF2ReviewStatusImpl iterator = new RF2ReviewStatusImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			logger.info("==========Total number of metadata RF2 Review Status records======" + iterator.getMetaDataCount());
		
			
			closeExportFileWriter();
			logger.info("Finished Review Status Snapshot Export...");
		} catch (MalformedURLException e) {
			logger.error("Message :", e);
		} catch (IOException e) {
			logger.error("Message :", e);
		} catch (Exception e) {
			logger.error("Message :", e);
		}
	}
}
