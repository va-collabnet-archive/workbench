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
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

/**
 * The Class Test_tk_model.
 */
public class Test_tk_model extends TestCase {
	
	/**
	 * Test1.
	 */
	public void test1() {
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
		kbuilder.add(ResourceFactory.newFileResource("src/test/java/org/ihtsdo/rules/test/test-tk-model-rules.drl"), 
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
	
	/**
	 * Gets the facts.
	 *
	 * @return the facts
	 */
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
