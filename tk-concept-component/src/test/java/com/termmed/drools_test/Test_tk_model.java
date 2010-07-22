package com.termmed.drools_test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.ihtsdo.tk.concept.component.description.TkDescription;
import org.ihtsdo.tk.concept.component.description.TkDescriptionRevision;

public class Test_tk_model extends TestCase {
	
	public void test1() {
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
		kbuilder.add(ResourceFactory.newFileResource("src/test/java/com/termmed/drools_test/test-tk-model-rules.drl"), 
				ResourceType.DRL);
		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//		KnowledgeRuntimeLogger logger =
//			KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		for (Object fact : getFacts()) {
			ksession.insert(fact);
		}
		ksession.fireAllRules();
//		logger.close();
	}
	
	public List<Object> getFacts() {
		List<Object> objects = new ArrayList<Object>();
		
		UUID conceptUuid = UUID.randomUUID();
		UUID currentUuid = UUID.randomUUID();
		UUID retiredUuid = UUID.randomUUID();
		UUID prefUuid = UUID.randomUUID();
		UUID fsnUuid = UUID.randomUUID();
		UUID synonymUuid = UUID.randomUUID();
		
		TkDescription description = new TkDescription();
		description.setPrimordialComponentUuid(UUID.randomUUID());
		description.setAdditionalIdComponents(null);
		description.setTime(System.currentTimeMillis() - 10000);
		description.setPathUuid(UUID.randomUUID());
		description.setStatusUuid(currentUuid);
		description.setAuthorUuid(UUID.randomUUID());
		description.setTypeUuid(fsnUuid);
		description.setText("body structure (body structure)");
		description.setLang("en");
		description.setInitialCaseSignificant(false);
		description.setConceptUuid(conceptUuid);
		description.setRevisions(new ArrayList<TkDescriptionRevision>());
		
		TkDescriptionRevision revision = new TkDescriptionRevision();
		revision.setTime(System.currentTimeMillis() - 5000);
		revision.setPathUuid(UUID.randomUUID());
		revision.setStatusUuid(currentUuid);
		revision.setAuthorUuid(UUID.randomUUID());
		revision.setTypeUuid(fsnUuid);
		revision.setText("body structure rev 1 (body structure)");
		revision.setLang("en");
		revision.setInitialCaseSignificant(false);
		
		description.getRevisionList().add(revision);
		
		TkDescriptionRevision revision2 = new TkDescriptionRevision();
		revision2.setTime(System.currentTimeMillis());
		revision2.setPathUuid(UUID.randomUUID());
		revision2.setStatusUuid(currentUuid);
		revision2.setAuthorUuid(UUID.randomUUID());
		revision2.setTypeUuid(fsnUuid);
		revision2.setText("body structure rev 2 (body structure)");
		revision2.setLang("en");
		revision2.setInitialCaseSignificant(false);
		
		description.getRevisionList().add(revision2);
		
		objects.add(description);
		
		return objects;
	}

}
