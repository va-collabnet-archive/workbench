package com.termmed.drools_test;

import java.util.HashMap;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;

public class Test_drools_interfaces extends TestCase {
	
	public void test1() {
		
		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
		resources.put( ResourceFactory.newFileResource("src/test/java/com/termmed/drools_test/sample-rules.drl"), 
				ResourceType.DRL );
		resources.put( ResourceFactory.newFileResource("src/test/java/com/termmed/drools_test/sample-declaration.drl"), 
				ResourceType.DRL );
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
		for (Resource resource : resources.keySet()) {
			kbuilder.add(resource, resources.get(resource));
		}
		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
//		KnowledgeRuntimeLogger logger =
//			KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		Concept concept = new Concept();
		concept.setId(1);
		concept.setLast(true);
		concept.setName("My concept");
		ksession.insert(concept);
		System.out.println("*******First fire....");
		ksession.fireAllRules();
//		logger.close();
//		Long stoptime = 4000L; //2 Seconds
//		System.out.println("Going to sleep...");
//		try {
//		Thread.sleep(stoptime);
//		} catch (InterruptedException e) {
//		e.printStackTrace();
//		}
//		System.out.println("Woke up again!");
//		System.out.println("*******Second fire....");
//		ksession.fireAllRules();
	}

}
