package org.ihtsdo.rules.test.context;

import java.io.File;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderErrors;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.ihtsdo.rules.filesources.DrComponentFromFile;
import org.ihtsdo.rules.filesources.GuvnorUtils;
import org.ihtsdo.testmodel.DrConcept;

/**
 * This is a sample class to launch a rule.
 */
public class DrComponentFromFileRuleTest {

    public static final void main(String[] args) {
        try {
            DrComponentFromFile comp = new DrComponentFromFile(new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedConcept.txt"),
    				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedDescription.txt"), 
    				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedRelationship.txt"),
    				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedAssociations.txt"),
    				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedENLanguage.txt"),
    				new File("/Users/termmed/Downloads/RF2_Snapshot/snapshotSortedGBLanguage.txt"));
            DrConcept concept;
            int i=0;
            GuvnorUtils utils= new GuvnorUtils();
            utils.setupDrools();
            while((concept=comp.getNextDrConcept())!=null){
            	utils.checkConcept(concept);
            }
            System.out.println("RULES");
//            logger.close();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

//    private static KnowledgeBase readKnowledgeBase() throws Exception {
//        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
//        kbuilder.add(ResourceFactory.newFileResource("/Users/termmed/Documents/workspace/workbench/rules-engine-cesar-september/rules/DrComponentFromFileRules.drl"), ResourceType.DRL);
//        KnowledgeBuilderErrors errors = kbuilder.getErrors();
//        if (errors.size() > 0) {
//            for (KnowledgeBuilderError error: errors) {
//                System.err.println(error);
//            }
//            throw new IllegalArgumentException("Could not parse knowledge.");
//        }
//        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
//        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//        return kbase;
//    }

}
