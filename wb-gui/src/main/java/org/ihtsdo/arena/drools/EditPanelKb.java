package org.ihtsdo.arena.drools;

import java.util.HashMap;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public class EditPanelKb {

	private KnowledgeBase kbase;
	private I_ConfigAceFrame config;
	
	public EditPanelKb(I_ConfigAceFrame config) {
		super();
		this.config = config;
		//setupKb();
	}

	private void setupKb() {
		kbase = KnowledgeBaseFactory.newKnowledgeBase();

		HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
		
	    resources.put( ResourceFactory.newFileResource("/Users/kec/Documents/wb-trek/wb-toolkit_trek/wb-bdb/src/main/resources/org/ihtsdo/arena/drools/ConceptRules.drl"), ResourceType.DRL );

		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
		for (Resource resource : resources.keySet()) {
			kbuilder.add(resource, resources.get(resource));
		}
		if (kbuilder.hasErrors()) {
			throw new RuntimeException(kbuilder.getErrors().toString());
		}
		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
	}
	
	public void setConcept(I_GetConceptData c) {
		setupKb();
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		KnowledgeRuntimeLogger logger =
			KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
		
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
