package org.ihtsdo.rf2;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;
import org.ihtsdo.rf2.refset.factory.RF2SimpleMapRefsetFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.util.LoggerUtil;

/**
 * Title: ProcessDataExtraction Description: Main Program calling all the different export routine Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class ProcessDataExtraction {

	private static Logger logger = Logger.getLogger(ProcessDataExtraction.class);
 
	public static void main(String[] args) {

		logger.error("**************Starting Process Data Extraction Program************" + (new Timestamp(new Date().getTime())));
		try {
			/*
			 * RF2IdentifierFactory identifierExport = new RF2IdentifierFactory(); identifierExport.createIdentifierFile();
			 * 
			 * RF2ConceptFactory conceptExport = new RF2ConceptFactory(); I_TermFactory conTermFactory = conceptExport.getTermFactory(); conceptExport.conceptExport(); conTermFactory.close();
			 * 
			 * RF2DescriptionFactory descriptionExport = new RF2DescriptionFactory(); I_TermFactory descTermFactory = descriptionExport.getTermFactory(); descriptionExport.descriptionExport();
			 * descTermFactory.close();
			 * 
			 * RF2RelationshipFactory relationshipExport = new RF2RelationshipFactory(); I_TermFactory relTermFactory = relationshipExport.getTermFactory(); relationshipExport.relationshipExport();
			 * relTermFactory.close();
			 * 
			 * RF2StatedRelationshipFactory statedRelationshipExport = new RF2StatedRelationshipFactory(); I_TermFactory statedRelTermFactory = statedRelationshipExport.getTermFactory();
			 * statedRelationshipExport.statedRelationshipExport(); statedRelTermFactory.close();
			 * 
			 * RF2ConceptInactivationRefsetFactory rf2ConceptInactivationRefsetExport = new RF2ConceptInactivationRefsetFactory(); I_TermFactory conceptInactivationRefsetTermFactory =
			 * rf2ConceptInactivationRefsetExport.getTermFactory(); rf2ConceptInactivationRefsetExport.conceptInactivationRefsetExport(); conceptInactivationRefsetTermFactory.close();
			 * 
			 * RF2DescriptionInactivationRefsetFactory rf2DescInactivationRefsetExport = new RF2DescriptionInactivationRefsetFactory(); I_TermFactory descInactivationRefsetTermFactory =
			 * rf2DescInactivationRefsetExport.getTermFactory(); rf2DescInactivationRefsetExport.descriptionInactivationRefsetExport(); descInactivationRefsetTermFactory.close();
			 * 
			 * RF2RefinabilityRefsetFactory rf2RefinabilityRefsetExport = new RF2RefinabilityRefsetFactory(); I_TermFactory refinabilityRefsetTermFactory =rf2RefinabilityRefsetExport.getTermFactory();
			 * rf2RefinabilityRefsetExport.refinabilityExport(); refinabilityRefsetTermFactory.close();
			 * 
			 * DataExtraction dataExtraction = new DataExtraction(); I_TermFactory tf = dataExtraction.setUp(); dataExtraction.conceptExport(); dataExtraction.descriptionExport();
			 * 
			 * RF2SimpleMapRefsetFactory rf2SimpleMapRefsetExport = new RF2SimpleMapRefsetFactory(); I_TermFactory simpleMapRefsetTermFactory = rf2SimpleMapRefsetExport.getTermFactory();
			 * rf2SimpleMapRefsetExport.snomedIdRefsetExport(); simpleMapRefsetTermFactory.close();
			 * 
			 * RF2HistoricalAssociationRefsetFactory rf2HistAssociationRefsetExport = new RF2HistoricalAssociationRefsetFactory(); I_TermFactory histAssociationRefsetTermFactory =
			 * rf2HistAssociationRefsetExport.getTermFactory(); rf2HistAssociationRefsetExport.historicalAssociationRefsetExport(); histAssociationRefsetTermFactory.close();
			 * 
			 * RF2AttributeValueRefsetFactory rf2AttributeValueRefsetExport = new RF2AttributeValueRefsetFactory(); I_TermFactory attributeRefsetTermFactory =
			 * rf2AttributeValueRefsetExport.getTermFactory(); rf2AttributeValueRefsetExport.attributeValueRefsetExport(); attributeRefsetTermFactory.close();
			 * 
			 * RF2TextDefinitionFactory textDefExport = new RF2TextDefinitionFactory(); I_TermFactory textDefTermFactory = textDefExport.getTermFactory(); textDefExport.textDefExport();
			 * textDefTermFactory.close();
			 * 
			 * RF2LanguageRefsetFactory usLangRefsetExport = new RF2LanguageRefsetFactory(); I_TermFactory usLangTermFactory = usLangRefsetExport.getTermFactory();
			 * usLangRefsetExport.languageRefsetExport(); usLangTermFactory.close();
			 */

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/core/config/simplemaprefsetrefset.xml");

			config = ProcessSingleDataExtraction.setCommandLineParams(config, args);

			RF2SimpleMapRefsetFactory factory = new RF2SimpleMapRefsetFactory(config);
			factory.export();

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		logger.error("Ending ProcessDataExtraction Program " + (new Timestamp(new Date().getTime())));

		System.exit(0);
	}
}
