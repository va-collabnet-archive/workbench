package org.ihtsdo.rf2.module.derivatives.factory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.module.derivatives.impl.RF2TextDefinitionImpl;
import org.ihtsdo.rf2.module.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.module.util.Config;
import org.ihtsdo.rf2.module.util.ExportUtil;

// TODO: Auto-generated Javadoc
/**
 * Title: RF2TextDefinitionFactory Description: Creating TextDefinitionFactory Specific methods required by DescriptionIterator Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class RF2TextDefinitionFactory extends RF2AbstractFactory {

	/** The logger. */
	private static Logger logger = Logger.getLogger(RF2TextDefinitionFactory.class);

	/**
	 * Instantiates a new r f2 text definition factory.
	 *
	 * @param config the config
	 */
	public RF2TextDefinitionFactory(Config config) {
		super(config);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.rf2.module.factory.RF2AbstractFactory#export()
	 */
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
			logger.info("Finished Textdefinition Snapshot Export...");
		} catch (MalformedURLException e) {
			logger.error("Message :", e);
		} catch (IOException e) {
			logger.error("Message :", e);
		} catch (Exception e) {
			logger.error("Message :", e);
		}
	}
}
