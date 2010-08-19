package org.ihtsdo.arena.drools;

import java.io.IOException;
import java.util.HashMap;

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
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.drools.IsKindOfEvaluatorDefinition;
import org.ihtsdo.tk.drools.SatisfiesConstraintEvaluatorDefinition;

public class EditPanelKb {

	private KnowledgeBase kbase;
	private I_ConfigAceFrame config;

	public EditPanelKb(I_ConfigAceFrame config) {
		super();
		this.config = config;
		// setupKb();
	}

	private void setupKb(String testResource) {

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
		kbase = KnowledgeBaseFactory.newKnowledgeBase();

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory
				.newKnowledgeBuilder(kbase, builderConfig);
		for (Resource resource : resources.keySet()) {
			kbuilder.add(resource, resources.get(resource));
		}
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
	}

	public void setConcept(I_GetConceptData c) {
		try {
			setupKb("org/ihtsdo/arena/drools/TkApiRules.drl");
			StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
			boolean uselogger = false;
			
			KnowledgeRuntimeLogger logger = null;
			if (uselogger) {
				logger = KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
			}
			try {
				Coordinate coordinate = new Coordinate(config.getPrecedence(),
						config.getViewPositionSetReadOnly(), config
								.getAllowedStatus(), config.getDestRelTypes(),
						config.getConflictResolutionStrategy());
				ksession.insert(Ts.get().getConceptVersion(coordinate, c.getNid()));
				ksession.fireAllRules();
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} finally {
				if (logger != null) {
					logger.close();
				}
			}
		} catch (Throwable e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public void setConceptOld(I_GetConceptData c) {
		setupKb("org/ihtsdo/arena/drools/ConceptRules.drl");
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		KnowledgeRuntimeLogger logger = KnowledgeRuntimeLoggerFactory
				.newConsoleLogger(ksession);

		ksession.setGlobal("editPaths", config.getEditingPathSetReadOnly());
		ksession
				.setGlobal("viewPositions", config.getViewPositionSetReadOnly());
		ksession.setGlobal("precedence", config.getPrecedence());
		ksession.setGlobal("contradictionMgr", config
				.getConflictResolutionStrategy());
		ksession.setGlobal("allowedStatus", config.getAllowedStatus());
		ksession.setGlobal("allowedDescTypes", null);
		ksession.setGlobal("allowedSrcRelTypes", null);
		ksession.setGlobal("allowedMediaTypes", null);
		ksession.insert(c);
		ksession.fireAllRules();
		logger.close();

	}

}
