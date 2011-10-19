package org.ihtsdo.rules.filesources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListener;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.rules.ConsoleSystemEventListener;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.testmodel.DrConcept;

public class GuvnorUtils {

	KnowledgeBase kbase;
	private BufferedWriter resultWriter;
	private TerminologyHelperDroolsWorkbench termHelper;
	private ResultsCollectorWorkBench results;
	private int counter;
	
	public void setupDrools(){
		kbase = KnowledgeBaseFactory.newKnowledgeBase();

		counter=0;
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

		File flow = new File("rules/qa-execution3.bpmn");

		if (flow.exists()) {

		kbuilder.add(ResourceFactory.newFileResource("rules/qa-execution3.bpmn"), ResourceType.BPMN2);

		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

		KnowledgeAgentConfiguration kaconf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();

		kaconf.setProperty( "drools.resource.urlcache","rules" );

		kaconf.setProperty( "drools.agent.newInstance","false" );

		KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "Agent", kaconf );

		SystemEventListener sysEvenListener = new ConsoleSystemEventListener();

		kagent.setSystemEventListener(sysEvenListener);

		kagent.applyChangeSet( ResourceFactory.newByteArrayResource(getChangeSetXmlBytes()) );

		kbase.addKnowledgePackages(kagent.getKnowledgeBase().getKnowledgePackages());
		}
		results = new ResultsCollectorWorkBench();
		try {
			Terms.createFactory(new File("/Volumes/Macintosh HD2/CompartidoServer/berkeley-db"), false, new Long(500L), new DatabaseSetupConfig());
		} catch (Exception e) {
			e.printStackTrace();
		}
		termHelper= new TerminologyHelperDroolsWorkbench();
	}
		
		
	public void checkConcept(DrConcept concept){
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        ksession.setGlobal("resultsCollector", results);
        ksession.setGlobal("terminologyHelper",termHelper );
        ksession.insert(concept);
    	ksession.fireAllRules();
    	try {
    		counter++;
    		if(results.getResultsItems().size()>0)
			resultWriter.write(counter+" Concept " + concept.getPrimordialUuid() + "\tResults size: " + results.getResultsItems().size());
			resultWriter.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ksession.dispose();

	}
	
	
	public GuvnorUtils() {
		super();
		try {
			FileWriter results= new FileWriter(new File("Results.txt"));
			resultWriter= new BufferedWriter(results);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public byte[] getChangeSetXmlBytes() {

		StringBuffer buff = new StringBuffer();

		buff.append("<change-set xmlns='http://drools.org/drools-5.0/change-set'");

		buff.append(" xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'");

		buff.append(" xs:schemaLocation='http://drools.org/drools-5.0/change-set drools-change-set-5.0.xsd' >");

		buff.append(" <add>");

		buff.append(" <resource source='");

		buff.append("http://mgr.servers.aceworkspace.net:50002/drools-guvnor/org.drools.guvnor.Guvnor/package/ihtsdo-qa/qa-package");

		//TODO: implement full authentication

		buff.append("' type='PKG' basicAuthentication='enabled' username='alopez' password='snomed'/>");

		buff.append(" </add>");

		buff.append("</change-set>");

		//System.out.println(buff.toString());

		return buff.toString().getBytes();

		}
}
