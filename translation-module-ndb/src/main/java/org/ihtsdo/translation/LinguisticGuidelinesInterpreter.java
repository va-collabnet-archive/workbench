package org.ihtsdo.translation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DecisionTableConfiguration;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.workflow.api.SimpleKindOfComputer;

/**
 * The Class WorkflowInterpreter.
 */
public class LinguisticGuidelinesInterpreter {
	/** The h wf i. */
	private static HashMap<String, LinguisticGuidelinesInterpreter> hWfI = new HashMap<String, LinguisticGuidelinesInterpreter>();

	/** The kbase. */
	private KnowledgeBase kbase;

	/** The ksession. */
	private StatelessKnowledgeSession ksession;

	/** The actions. */
	private List<String> guidelines;

	private LinguistcGuidelineFacts linguisticGuidelinesFacts;

	private static final String linguisticGuidelinesRulesFile = "drools-rules/linguistic-guidelines.xls";

	/**
	 * Instantiates a new workflow interpreter.
	 * 
	 * @param wfDefinition
	 *            the wf definition
	 */
	public LinguisticGuidelinesInterpreter(I_ContextualizeDescription sourcePreferredDescription, I_ContextualizeDescription sourceFsnDescription) {
		super();
		// kbase and ksession are singletons
		if (kbase == null || ksession == null) {
			// Crate knowledge base with decision table
			DecisionTableConfiguration dtableconfiguration = KnowledgeBuilderFactory.newDecisionTableConfiguration();
			dtableconfiguration.setInputType(DecisionTableInputType.XLS);

			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);

			Resource xlsRes = ResourceFactory.newFileResource(linguisticGuidelinesRulesFile);
			kbuilder.add(xlsRes, ResourceType.DTABLE, dtableconfiguration);

			if (kbuilder.hasErrors()) {
				System.err.print(kbuilder.getErrors());
			}

			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
			ksession = kbase.newStatelessKnowledgeSession();
			hWfI.put(sourcePreferredDescription.getText(), this);

			this.linguisticGuidelinesFacts = new LinguistcGuidelineFacts(sourceFsnDescription.getText(), 
					sourcePreferredDescription.getText(), sourceFsnDescription.getConcept().getPrimUuid());

		}
	}

	public static LinguisticGuidelinesInterpreter createLinguisticGuidelinesInterpreter(I_ContextualizeDescription sourcePreferredDescription, I_ContextualizeDescription sourceFsnDescription) {
		if (hWfI.containsKey(sourcePreferredDescription.getText())) {
			return hWfI.get(sourcePreferredDescription.getText());
		}

		return new LinguisticGuidelinesInterpreter(sourcePreferredDescription, sourceFsnDescription);
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
	public List<String> getLinguisticGuidelines() {
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
