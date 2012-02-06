/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.rules.test;

import java.util.HashMap;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * The Class Test_drools_interfaces.
 */
public class Test_drools_interfaces extends TestCase {
	
	/**
	 * Test1.
	 */
	public void test1() {
		
		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
		resources.put( ResourceFactory.newFileResource("src/test/java/org/ihtsdo/rules/test/sample-rules.drl"), 
				ResourceType.DRL );
		resources.put( ResourceFactory.newFileResource("src/test/java/org/ihtsdo/rules/test/sample-declaration.drl"), 
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
//		AceLog.getAppLog().alertAndLogException(e);
//		}
//		System.out.println("Woke up again!");
//		System.out.println("*******Second fire....");
//		ksession.fireAllRules();
	}

}
