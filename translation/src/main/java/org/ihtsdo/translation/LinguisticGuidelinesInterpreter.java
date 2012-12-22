package org.ihtsdo.translation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.impl.InputStreamResource;
import org.drools.io.impl.ReaderInputStream;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.workflow.api.SimpleKindOfComputer;

/**
 * The Class WorkflowInterpreter.
 */
public class LinguisticGuidelinesInterpreter {

	/**
	 * The h wf i.
	 */
	private static LinguisticGuidelinesInterpreter cache;
	/**
	 * The kbase.
	 */
	private KnowledgeBase kbase;
	/**
	 * The ksession.
	 */
	private StatelessKnowledgeSession ksession;
	/**
	 * The actions.
	 */
	private List<String> guidelines;
	private static final String linguisticGuidelinesRulesFile = "drools-rules/linguistic-guidelines.xls";

	/**
	 * Instantiates a new workflow interpreter.
	 * 
	 * @param wfDefinition
	 *            the wf definition
	 */
	private LinguisticGuidelinesInterpreter() {
		super();
		// kbase and ksession are singletons
		if (kbase == null || ksession == null) {
			// Crate knowledge base with decision table
			DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
			dtableconfiguration.setInputType(DecisionTableInputType.XLS);

			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

			ReaderInputStream ris = null;
			try {
				ris = new ReaderInputStream(new FileReader(new File("linguisticGuidelinesRulesFile")), "UTF-8");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Resource xlsRes = null;
			xlsRes = new InputStreamResource(ris);
			kbuilder.add(xlsRes, ResourceType.DTABLE, dtableconfiguration);

			if (kbuilder.hasErrors()) {
				System.err.print(kbuilder.getErrors());
			}

			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			ksession = kbase.newStatelessKnowledgeSession();
		}
	}

	public static LinguisticGuidelinesInterpreter createLinguisticGuidelinesInterpreter() {
		if (cache != null) {
			return cache;
		}

		cache = new LinguisticGuidelinesInterpreter();
		return cache;
	}

	/**
	 * Gets the possible actions.
	 * 
	 * @param instance
	 *            the instance
	 * @param user
	 *            the user
	 * @return the possible actions
	 */
	public List<String> getLinguisticGuidelines(I_ContextualizeDescription sourcePreferredDescription, I_ContextualizeDescription sourceFsnDescription) {
		LinguistcGuidelineFacts linguisticGuidelinesFacts = new LinguistcGuidelineFacts(sourceFsnDescription.getText(), sourcePreferredDescription.getText(), sourceFsnDescription.getConcept()
				.getPrimUuid());
		KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		guidelines = new ArrayList<String>();
		ksession.setGlobal("guidelines", guidelines);
		ksession.setGlobal("kindOfComputer", new SimpleKindOfComputer());
		ArrayList<Object> facts = new ArrayList<Object>();
		facts.add(linguisticGuidelinesFacts);
		ksession.execute(facts);
		return guidelines;
	}
}
