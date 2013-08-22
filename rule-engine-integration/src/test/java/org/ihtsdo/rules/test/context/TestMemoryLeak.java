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
package org.ihtsdo.rules.test.context;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.log.AceLog;

/**
 * The Class TestMemoryLeak.
 */
public class TestMemoryLeak extends TestCase {
	
	/** The session. */
	StatefulKnowledgeSession session;

	/**
	 * Test case.
	 *
	 * @throws InterruptedException the interrupted exception
	 */
	public void testCase() throws InterruptedException

    {

           KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

           KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

           kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

                        

           for(int i = 0; i < 1000; i++) {

                  AceLog.getAppLog().info("Run " + i);

                  session = kbase.newStatefulKnowledgeSession();

                  for(int j = 0; j < 100; j++) {

                        session.insert(new byte[10240]);

                  }

                 

                  session.dispose();

                  session = null;

                  System.gc();

           }     

    }
}
