package org.ihtsdo.rf2.refset.factory;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.factory.RF2AbstractFactory;
import org.ihtsdo.rf2.refset.impl.RF2LanguageImpl;
import org.ihtsdo.rf2.refset.impl.RF2LanguageUSImpl;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;

/**
 * Title: RF2LanguageRefsetFactory Description: Creating SnomedId & Ctv3Id Refset Specific methods Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class RF2LanguageRefsetFactory extends RF2AbstractFactory {

	private static Logger logger = Logger.getLogger(RF2LanguageRefsetFactory.class);

	public RF2LanguageRefsetFactory(Config config) {
		super(config);
	} 

	public void export() {

		logger.info("Started Language Refset Export ...");

		try {
			int refsetId=Terms.get().getConcept(UUID.fromString("bca0a686-3516-3daf-8fcf-fe396d13cfad")).getNid();
			RF2LanguageImpl languageUSIterator = new RF2LanguageImpl(getConfig(),refsetId,I_Constants.US_LANG_REFSET_ID,I_Constants.CORE_MODULE_ID);
			Terms.get().iterateConcepts(languageUSIterator);
			
			refsetId=Terms.get().getConcept(UUID.fromString("eb9a5e42-3cba-356d-b623-3ed472e20b30")).getNid();
			RF2LanguageImpl languageGBIterator = new RF2LanguageImpl(getConfig(),refsetId,I_Constants.GB_LANG_REFSET_ID,I_Constants.CORE_MODULE_ID);
			Terms.get().iterateConcepts(languageGBIterator);

			closeExportFileWriter();

			logger.info("Finished Language Refset Export.");

		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
}
