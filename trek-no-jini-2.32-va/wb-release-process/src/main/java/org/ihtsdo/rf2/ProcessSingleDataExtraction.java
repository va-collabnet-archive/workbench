package org.ihtsdo.rf2;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.ihtsdo.rf2.core.factory.RF2ConceptFactory;
import org.ihtsdo.rf2.core.factory.RF2DescriptionFactory;
import org.ihtsdo.rf2.core.factory.RF2IdentifierFactory;
import org.ihtsdo.rf2.core.factory.RF2RelationshipFactory;
import org.ihtsdo.rf2.core.factory.RF2StatedRelationshipFactory;
import org.ihtsdo.rf2.derivatives.factory.RF2TextDefinitionFactory;
import org.ihtsdo.rf2.refset.factory.RF2AttributeValueRefsetFactory; 
import org.ihtsdo.rf2.refset.factory.RF2HistoricalAssociationRefsetFactory;
import org.ihtsdo.rf2.refset.factory.RF2LanguageRefsetFactory;
import org.ihtsdo.rf2.refset.factory.RF2SimpleFullRefsetFactory;
import org.ihtsdo.rf2.refset.factory.RF2SimpleMapRefsetFactory;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.Database;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;

/**
 * Title: ProcessDataExtraction Description: Main Program calling all the different export routine Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */
enum ExtractType {
	CONCEPT, DESCRIPTION, IDENTIFIER, RELATIONSHIP, STATEDRELATIONSHIP, INVALID, ATTRIBUTEVALUEREFSET, HISTORICALASSOCIATIONREFSET, SIMPLEMAPREFSET, TEXTDEF, LANGREFSET, SIMPLEFULLREFSET
};

public class ProcessSingleDataExtraction {

	private static Logger logger = Logger.getLogger(ProcessSingleDataExtraction.class);
 
	public static ExtractType getExtractType(String type) {
		ExtractType extractType = ExtractType.INVALID;

		if (type.toUpperCase().equals("CONCEPT"))
			extractType = ExtractType.CONCEPT;
		else if (type.toUpperCase().equals("DESCRIPTION"))
			extractType = ExtractType.DESCRIPTION;		
		else if (type.toUpperCase().equals("RELATIONSHIP"))
			extractType = ExtractType.RELATIONSHIP;
		else if (type.toUpperCase().equals("STATEDRELATIONSHIP"))			
			extractType = ExtractType.STATEDRELATIONSHIP;
		else if (type.toUpperCase().equals("TEXTDEF"))
			extractType = ExtractType.TEXTDEF;
		else if (type.toUpperCase().equals("IDENTIFIER"))
			extractType = ExtractType.IDENTIFIER;
		else if (type.toUpperCase().equals("ATTRIBUTEVALUEREFSET"))
			extractType = ExtractType.ATTRIBUTEVALUEREFSET;
		else if (type.toUpperCase().equals("HISTORICALASSOCIATIONREFSET"))
			extractType = ExtractType.HISTORICALASSOCIATIONREFSET;
		else if (type.toUpperCase().equals("SIMPLEMAPREFSET"))
			extractType = ExtractType.SIMPLEMAPREFSET;
		else if (type.toUpperCase().equals("SIMPLEFULLREFSET"))
			extractType = ExtractType.SIMPLEFULLREFSET;
		else if (type.toUpperCase().equals("LANGREFSET"))
			extractType = ExtractType.LANGREFSET;		
	
		return extractType;
	}

	public static void main(String[] args) {

		// error code
		int error = 0;

		// configure Log4j for logging with the lo4j.xml found in the classpath
		BasicConfigurator.configure();

		// Check for parameter to determine which process to execute
		if (args.length < 3) {
			logger.error("Invalid arguments");
			logger
					.error("Usage: ProcessData Extraction <typeOfExtraction> <database> <releaseDate> <exportFolder> [ OPTIONAL <rf2format> <invokeDrools> <incrementalRelease> <fromReleaseDate> <toReleaseDate> <exportFileExtension>]");
			logger.error("Example: java ProcessDataExtraction concept true D:\\database\berkeley-db 20100713 txt export 200201031 20100731 false false");
			logger
					.error("Valid Types of Extraction are: concept, description, identifier, relationship, STATEDRELATIONSHIP , ATTRIBUTEVALUEREFSET, HISTORICALASSOCIATIONREFSET,SIMPLEMAPREFSET , LANGREFSET ,NONHUMANREFSET ,VTMVMPREFSET, TEXTDEF");
			System.exit(1);
		}

		ExtractType[] extractTypes;

		if (args[0].toUpperCase().equals("ALL")) {
			extractTypes = new ExtractType[10];
			extractTypes = setAll();
		} else if (args[0].toUpperCase().equals("CORE")) {
			extractTypes = new ExtractType[5];
			extractTypes = setCore();
		} else if (args[0].toUpperCase().equals("REFSET")) {
			extractTypes = new ExtractType[7];
			extractTypes = setRefset();
		} else if (args[0].toUpperCase().equals("DERIVATIVES")) {
			extractTypes = new ExtractType[1];
			extractTypes = setDerivatives();
		} else {
			extractTypes = new ExtractType[1];
			extractTypes[0] = getExtractType(args[0]);
		}

		try {

			// get and set the database location from command line
			Database db = new Database();
			db.setName("IHTSDO");
			db.setLocation(args[1]);

			// initialize the database
			ExportUtil.createTermFactory(db);

			// initialize ace framework and meta hierarchy
			ExportUtil.init();

			for (int i = 0; i < extractTypes.length; i++) {

				switch (extractTypes[i]) {

				case CONCEPT: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/concept.xml");

					config = setCommandLineParams(config, args);

					RF2ConceptFactory factory = new RF2ConceptFactory(config);
					factory.export();

					break;
				}
				case DESCRIPTION: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/description.xml");

					config = setCommandLineParams(config, args);

					RF2DescriptionFactory factory = new RF2DescriptionFactory(config);
					factory.export();

					break;
				}
				case IDENTIFIER: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/identifier.xml");

					config = setCommandLineParams(config, args);

					RF2IdentifierFactory factory = new RF2IdentifierFactory(config);
					factory.export();

					break;
				}
				case RELATIONSHIP: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/relationship.xml");

					config = setCommandLineParams(config, args);

					RF2RelationshipFactory factory = new RF2RelationshipFactory(config);
					factory.export();

					break;
				}
				case STATEDRELATIONSHIP: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/statedrelationship.xml");

					config = setCommandLineParams(config, args);

					RF2StatedRelationshipFactory factory = new RF2StatedRelationshipFactory(config);
					factory.export();

					break;
				}
				case ATTRIBUTEVALUEREFSET: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/attributevaluerefset.xml");

					config = setCommandLineParams(config, args);

					RF2AttributeValueRefsetFactory factory = new RF2AttributeValueRefsetFactory(config);
					factory.export();

					break;
				}
				case HISTORICALASSOCIATIONREFSET: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/historicalassociationrefset.xml");

					config = setCommandLineParams(config, args);

					RF2HistoricalAssociationRefsetFactory factory = new RF2HistoricalAssociationRefsetFactory(config);
					factory.export();

					break;
				}
				case SIMPLEMAPREFSET: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/simplemaprefset.xml");

					config = setCommandLineParams(config, args);

					RF2SimpleMapRefsetFactory factory = new RF2SimpleMapRefsetFactory(config);
					factory.export();

					break;
				}
				case TEXTDEF: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/textDef.xml");

					config = setCommandLineParams(config, args);

					RF2TextDefinitionFactory factory = new RF2TextDefinitionFactory(config);
					factory.export();

					break;
				}
				case LANGREFSET: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/languageRefset.xml");

					config = setCommandLineParams(config, args);

					RF2LanguageRefsetFactory factory = new RF2LanguageRefsetFactory(config);
					factory.export();

					break;
				}
				case SIMPLEFULLREFSET: {

					Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/simpleFullRefset.xml");

					config = setCommandLineParams(config, args);

					RF2SimpleFullRefsetFactory factory = new RF2SimpleFullRefsetFactory(config);
					factory.export();

					break;
				}
				case INVALID: {
					logger.error("Invalid arguments passed");
					logger
							.error("Valid Types of Extraction are: concept, description, identifier, relationship, statedRelationship, ATTRIBUTEVALUEREFSET, HISTORICALASSOCIATIONREFSET, SIMPLEMAPREFSET, TEXTDEF, LANGREFSET");
				}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			error = 1;
		} finally {
			try {
				ExportUtil.getTermFactory().close();
			} catch (IOException e) {
				e.printStackTrace();
				error = 1;
			}
		}
		System.exit(error);
	}

	public static Config setCommandLineParams(Config config, String args[]) throws ParseException {

		// from command line
		config.setDatabase(args[1]);
		config.setReleaseDate(args[2]);
		config.setOutputFolderName(args[3]);

		// default if not set in command line
		config.setDefaults();

		// for web services
		if (args.length > 4) {
			if (args[4] != null)
				config.setEndPoint(args[4]);
		}

		if (args.length > 5) {
			if (args[5] != null)
				config.setUsername(args[5]);
		}

		if (args.length > 6) {
			if (args[6] != null)
				config.setPassword(args[6]);
		}

		// override defaults if specified in the command line
		if (args.length > 7) {
			if (args[4] != null)
				config.setRf2Format(args[7]);
		}

		if (args.length > 8) {
			if (args[5] != null)
				config.setInvokeDroolRules(args[8]);
		}

		if (args.length > 9) {
			if (args[6] != null)
				config.setIncrementalRelease(args[9]);
		}

		DateFormat df = ExportUtil.DATEFORMAT;

		if (args.length > 10) {
			if (args[7] != null)
				config.setFromReleaseDate(df.parse(args[10]));
		}

		if (args.length > 11) {
			if (args[8] != null)
				config.setToReleaseDate(df.parse(args[11]));
		}

		if (args.length > 12) {
			if (args[9] != null)
				config.setFileExtension(args[12]);
		}

		return config;
	}

	public static ExtractType[] setAll() {

		ExtractType[] extractTypes = { ExtractType.CONCEPT, ExtractType.DESCRIPTION, ExtractType.IDENTIFIER, ExtractType.RELATIONSHIP, ExtractType.STATEDRELATIONSHIP,
				ExtractType.ATTRIBUTEVALUEREFSET, ExtractType.HISTORICALASSOCIATIONREFSET, ExtractType.SIMPLEMAPREFSET, ExtractType.LANGREFSET, 
				ExtractType.SIMPLEFULLREFSET, ExtractType.TEXTDEF };

		return extractTypes;
	}

	public static ExtractType[] setCore() {

		ExtractType[] extractTypes = { ExtractType.CONCEPT, ExtractType.DESCRIPTION, ExtractType.IDENTIFIER, ExtractType.RELATIONSHIP, ExtractType.STATEDRELATIONSHIP };

		return extractTypes;
	}

	public static ExtractType[] setRefset() {

		ExtractType[] extractTypes = { ExtractType.ATTRIBUTEVALUEREFSET, ExtractType.HISTORICALASSOCIATIONREFSET, ExtractType.SIMPLEMAPREFSET, ExtractType.LANGREFSET, ExtractType.SIMPLEFULLREFSET };

		return extractTypes;
	}

	public static ExtractType[] setDerivatives() {

		ExtractType[] extractTypes = { ExtractType.TEXTDEF };

		return extractTypes;
	}

}
