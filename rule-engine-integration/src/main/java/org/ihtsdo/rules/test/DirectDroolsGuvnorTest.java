package org.ihtsdo.rules.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.rules.ConsoleSystemEventListener;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.testmodel.DrComponent;
import org.ihtsdo.testmodel.DrDescription;
import org.ihtsdo.tk.helper.ResultsItem;
import org.ihtsdo.tk.helper.ResultsItem.Severity;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;
import org.kie.api.io.ResourceType;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.SystemEventListener;
import org.kie.internal.agent.KnowledgeAgent;
import org.kie.internal.agent.KnowledgeAgentConfiguration;
import org.kie.internal.agent.KnowledgeAgentFactory;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class DirectDroolsGuvnorTest {

	public DirectDroolsGuvnorTest() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		List<UUID> uuids = new ArrayList<UUID>();
		uuids.add(UUID.randomUUID());
		RulesDeploymentPackageReference pkfRef = new RulesDeploymentPackageReference("ihtsdoBaseRules-dev",
				"http://mgr.servers.aceworkspace.net:50002/drools-guvnor550-final/org.drools.guvnor.Guvnor/package/ihtsdoBaseRules-dev/ihtsdoBaseRules-dev", 
				uuids);
		
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		File flow = new File("rules/qa-execution3.bpmn");
		if (flow.exists()) {
			kbuilder.add(ResourceFactory.newFileResource("rules/qa-execution3.bpmn"), ResourceType.BPMN2);
			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			
			KnowledgeAgentConfiguration kaconf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
			// kaconf.setProperty( "drools.resource.urlcache","rules" );
			kaconf.setProperty("drools.agent.newInstance", "false");
			KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("Agent", kaconf);
			SystemEventListener sysEvenListener = new ConsoleSystemEventListener();
			kagent.setSystemEventListener(sysEvenListener);
			kagent.applyChangeSet(ResourceFactory.newByteArrayResource(pkfRef.getChangeSetXmlBytes()));
			kbase.addKnowledgePackages(kagent.getKnowledgeBase().getKnowledgePackages());
		}
		
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

		// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		ResultsCollectorWorkBench results = new ResultsCollectorWorkBench();
		ksession.setGlobal("resultsCollector", results);
		ksession.setGlobal("terminologyHelper", new TerminologyHelperDrools());
		
		DrDescription testDescription = new DrDescription();
		testDescription.setText("New Description");
		testDescription.setExtensionId("module1");
		
		ksession.insert(testDescription);

		ksession.startProcess("org.ihtsdo.qa-execution3");
		ksession.fireAllRules();

		for (ResultsItem resultsItem : results.getResultsItems()) {
			System.out.println("ErrorCode: " + resultsItem.getErrorCode() + " Message: " + resultsItem.getMessage());
			System.out.println("Severity: " + resultsItem.getSeverity());
			System.out.println("Severity: " + Severity.getSeverityByUuid(UUID.fromString(resultsItem.getSeverity())));
			System.out.println("Suspects: " + resultsItem.getSuspects().size());
			for (DrComponent loopComponent : resultsItem.getSuspects()) {
				if (loopComponent instanceof DrDescription) {
					DrDescription loopDescription = (DrDescription) loopComponent;
					System.out.println("Suspect Description: " + loopDescription.getText());
					System.out.println("Suspect ExtensionId: " + loopDescription.getExtensionId());
				}
			}
		}
		
	}

}
