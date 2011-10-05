package org.ihtsdo.rules.test.context;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.runtime.StatefulKnowledgeSession;

public class TestMemoryLeak extends TestCase {
	
	StatefulKnowledgeSession session;

	public void testCase() throws InterruptedException

    {

           KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

           KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

           kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

                        

           for(int i = 0; i < 1000; i++) {

                  System.out.println("Run " + i);

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
