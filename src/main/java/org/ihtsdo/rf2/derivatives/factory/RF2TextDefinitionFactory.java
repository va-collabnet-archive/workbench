package org.ihtsdo.rf2.derivatives.factory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.derivatives.impl.RF2TextDefinitionImpl;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2TextDefinitionFactory Description: Creating TextDefinitionFactory Specific methods required by DescriptionIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2TextDefinitionFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2TextDefinitionFactory.class);

	public RF2TextDefinitionFactory(Config config) {
		super(config);
	}

	public void export() throws IOException, Exception {

		try {

			// validate if the enPoint, user name and password is provided
//
//			if (getConfig().getEndPoint() == null || getConfig().getEndPoint().isEmpty())
//				throw new Exception("Please provide a valid endPoint to connect to IdGeneration API");
//
//			if ( getConfig().getUsername() == null || getConfig().getUsername().isEmpty())
//				throw new Exception("Please provide a valid USER  to connect to IdGeneration API");
//
//			if (getConfig().getPassword() == null || getConfig().getPassword().isEmpty())
//				throw new Exception("Please provide a valid PASS  to connect to IdGeneration API");
//
//			// validate if a valid URL is provided
//			URL url = new URL(getConfig().getEndPoint());
//			URLConnection conn = url.openConnection();
//			conn.connect();

			RF2TextDefinitionImpl iterator = new RF2TextDefinitionImpl(getConfig());

			ExportUtil.getTermFactory().iterateConcepts(iterator);

			logger.info("==========Total number of metadata textdefinition records======" + iterator.getMetaDataCount());
		
			
			closeExportFileWriter();

		} catch (MalformedURLException e) {
			logger.error("Message :", e);
		} catch (IOException e) {
			logger.error("Message :", e);
		} catch (Exception e) {
			logger.error("Message :", e);
		}
	}
}
