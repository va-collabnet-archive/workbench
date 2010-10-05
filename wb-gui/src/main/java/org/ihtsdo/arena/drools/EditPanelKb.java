package org.ihtsdo.arena.drools;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.drools.IsKindOfEvaluatorDefinition;
import org.ihtsdo.tk.drools.SatisfiesConstraintEvaluatorDefinition;
import org.ihtsdo.tk.helper.ResultsItem;
import org.ihtsdo.tk.spec.SpecBI;

import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;

public class EditPanelKb implements Runnable {

	private KnowledgeBase kbase;
	private I_ConfigAceFrame config;
	private String drlFileStr = "org/ihtsdo/arena/drools/TkApiRules.drl";
	private CountDownLatch kbLatch = new CountDownLatch(1);

	public EditPanelKb(I_ConfigAceFrame config) {
		super();
		this.config = config;
		ACE.threadPool.execute(this);
	}
	
	@Override
	public void run() {
		kbase = setupKb(drlFileStr);
		kbLatch.countDown();
	}


	public static KnowledgeBase setupKb(String testResource) {

		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();

		resources.put(ResourceFactory.newClassPathResource(testResource),
				ResourceType.DRL);

		KnowledgeBuilderConfiguration builderConfig = KnowledgeBuilderFactory
				.newKnowledgeBuilderConfiguration();
		builderConfig.setOption(EvaluatorOption.get(
				IsKindOfEvaluatorDefinition.IS_KIND_OF.getOperatorString(),
				new IsKindOfEvaluatorDefinition()));
		builderConfig.setOption(EvaluatorOption.get(
				SatisfiesConstraintEvaluatorDefinition.SATISFIES_CONSTRAINT.getOperatorString(),
				new SatisfiesConstraintEvaluatorDefinition()));
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder(kbase, builderConfig);
		for (Resource resource : resources.keySet()) {
			kbuilder.add(resource, resources.get(resource));
		}
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
		return kbase;
	}

	public Map<SpecBI, Integer> setConcept(I_GetConceptData c) {
		Map<SpecBI, Integer> templates = new TreeMap<SpecBI, Integer>(new Comparator<Object>() {

			@Override
			public int compare(Object o1, Object o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		if (c == null) {
			return templates;
		}
		try {
			kbLatch.await();
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			boolean uselogger = false;
			
			KnowledgeRuntimeLogger logger = null;
			if (uselogger) {
				logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
			}
			try {
				Coordinate coordinate = config.getCoordinate();
				ksession.setGlobal("templates", templates);
				ksession.insert(Ts.get().getConceptVersion(coordinate, c.getNid()));
				ksession.fireAllRules();
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} finally {
				if (logger != null) {
					logger.close();
				}
			}
			
			try {
				I_GetConceptData context = Terms.get().getConcept(RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT.getUids());
				ResultsCollectorWorkBench results = RulesLibrary.checkConcept(c, context, false, config);
				Map<SpecBI, Integer> guvnorTemplates = results.getWbTemplates();
				templates.putAll(guvnorTemplates);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} catch (Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return templates;
	}

	public void setConceptOld(I_GetConceptData c) {
		setupKb("org/ihtsdo/arena/drools/ConceptRules.drl");
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory
				.newConsoleLogger(ksession);

		ksession.setGlobal("editPaths", config.getEditingPathSetReadOnly());
		ksession.setGlobal("viewPositions", config.getViewPositionSetReadOnly());
		ksession.setGlobal("precedence", config.getPrecedence());
		ksession.setGlobal("contradictionMgr", config.getConflictResolutionStrategy());
		ksession.setGlobal("allowedStatus", config.getAllowedStatus());
		ksession.setGlobal("allowedDescTypes", null);
		ksession.setGlobal("allowedSrcRelTypes", null);
		ksession.setGlobal("allowedMediaTypes", null);
		ksession.insert(c);
		ksession.fireAllRules();
		logger.close();

	}


}
