package org.ihtsdo.rf2.file.delta.snapshot.flow;
//
//import org.apache.commons.configuration.CompositeConfiguration;
//import org.apache.commons.configuration.SystemConfiguration;
//import org.apache.commons.configuration.XMLConfiguration;
//import org.drools.KnowledgeBase;
//import org.drools.builder.KnowledgeBuilder;
//import org.drools.builder.KnowledgeBuilderFactory;
//import org.drools.builder.ResourceType;
//import org.drools.io.ResourceFactory;
//import org.drools.runtime.StatefulKnowledgeSession;
//import org.ihtsdo.rf2.file.delta.snapshot.configuration.MetadataConfig;
//import org.ihtsdo.rf2.file.delta.snapshot.utils.RF2FileRetrieve;

public class ProcessTest {

//	/**
//	 * @param args
//	 */
//	public static final void main(String[] args) {
//		try {
//			CompositeConfiguration config = new CompositeConfiguration();
//			config.addConfiguration(new SystemConfiguration());
//			config.addConfiguration(new XMLConfiguration("config/config.xml"));
//			System.out.println(config.getString("releasefolder"));
//
//			// load up the knowledge base
//			KnowledgeBase kbase = readKnowledgeBase();
//			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//			//KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
//			// start a new process instance
//			String releaseFolder=config.getString("releasefolder");
//			if (releaseFolder!=null && !releaseFolder.trim().equals("")){
//				getFilesFromRelease(ksession,releaseFolder);
//
//			}else{
//
//				ksession.setGlobal("rf2ConceptsFile", config.getString("rf2inputfiles.concepts"));
//				ksession.setGlobal("rf2DescriptionsFile", config.getString("rf2inputfiles.descriptions"));
//				ksession.setGlobal("rf2RelationshipsFile", config.getString("rf2inputfiles.relationships"));
//				ksession.setGlobal("rf2LanguagesFile", config.getString("rf2inputfiles.languages"));
//				ksession.setGlobal("rf2AttributesFile", config.getString("rf2inputfiles.attributes"));
//				ksession.setGlobal("rf2SimpleMapsFile", config.getString("rf2inputfiles.simplemaps"));
//				ksession.setGlobal("rf2AssociationsFile", config.getString("rf2inputfiles.associations"));
//			}
//			ksession.setGlobal("rf1Generate",config.getString("parameters.rf1.generate"));
//			ksession.setGlobal("rf1Date", config.getString("parameters.rf1.date"));
//			ksession.setGlobal("rf1Validate",config.getString("parameters.rf1.validate"));
//
//			ksession.setGlobal("snapshotGenerate",config.getString("parameters.snapshot.generate"));
//			ksession.setGlobal("snapshotDate",config.getString("parameters.snapshot.date"));
//			
//			ksession.setGlobal("deltaGenerate",config.getString("parameters.delta.generate"));
//			ksession.setGlobal("deltaInitialDate",config.getString("parameters.delta.initialdate"));
//			ksession.setGlobal("deltaFinalDate",config.getString("parameters.delta.finaldate"));
//		
//			
//			MetadataConfig cfg;
//			cfg = new MetadataConfig();
//
//			ksession.setGlobal("RF2_REFINABILITY_REFSETID",cfg.getRF2_REFINABILITY_REFSETID());
//
//			ksession.setGlobal("RF2_ENUS_REFSET",cfg.getRF2_ENUS_REFSET());
//
//			ksession.setGlobal("RF2_ENGB_REFSET",cfg.getRF2_ENGB_REFSET());
//
//			ksession.setGlobal("RF2_INACT_CPT_REFSET",cfg.getRF2_INACT_CONCEPT_REFSET());
//
//			ksession.setGlobal("RF2_INACT_DESC_REFSET",cfg.getRF2_INACT_DESCRIPTION_REFSET());
//
//			ksession.startProcess("org.ihtsdo.convertToRf1");
//			//logger.close();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//	}
//
//	private static void getFilesFromRelease(StatefulKnowledgeSession ksession,
//			String releaseFolder) throws Exception {
//
//		RF2FileRetrieve fRet=new RF2FileRetrieve(releaseFolder);
//		ksession.setGlobal("rf2AssociationsFile",fRet.getAssociationFile());
//		ksession.setGlobal("rf2AttributesFile", fRet.getAttributeValueFile());
//		ksession.setGlobal("rf2ConceptsFile", fRet.getConceptFile());
//		ksession.setGlobal("rf2DescriptionsFile", fRet.getDescriptionFile());
//		ksession.setGlobal("rf2LanguagesFile",fRet.getLanguageFile());
//		ksession.setGlobal("rf2SimpleMapsFile",fRet.getSimpleMapFile());
//		ksession.setGlobal("rf2RelationshipsFile",fRet.getRelationshipFile());
//		fRet=null;
//
//	}
//
//	private static KnowledgeBase readKnowledgeBase() throws Exception {
//		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//		kbuilder.add(ResourceFactory.newFileResource("flows/conversion-to-rf1.rf"), ResourceType.DRF);
//		return kbuilder.newKnowledgeBase();
//	}


}
