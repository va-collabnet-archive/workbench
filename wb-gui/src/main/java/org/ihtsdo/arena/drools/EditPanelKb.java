package org.ihtsdo.arena.drools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
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
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.drools.IsKindOfEvaluatorDefinition;
import org.ihtsdo.tk.drools.IsMemberOfEvaluatorDefinition; //TODO this
import org.ihtsdo.tk.drools.SatisfiesConstraintEvaluatorDefinition;
import org.ihtsdo.tk.spec.SpecBI;

public class EditPanelKb implements Runnable {

	private KnowledgeBase kbase;
	private I_ConfigAceFrame config;
	private CountDownLatch kbLatch = new CountDownLatch(1);

	public EditPanelKb(I_ConfigAceFrame config) {
		super();
		this.config = config;
		ACE.threadPool.execute(this);
	}
	
	@Override
	public void run() {
		try {
			kbase = setupKb(new File("drools-rules/TkApiRules.drl"));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		kbLatch.countDown();
	}


	public static KnowledgeBase setupKb(File kbFile) throws IOException {
		Collection<KnowledgePackage> kpkgs;
		File drlPkgFile = new File(kbFile.getParentFile(), kbFile.getName() + ".kpkgs");
		if (!drlPkgFile.exists() || drlPkgFile.lastModified() < kbFile.lastModified()) {
			HashMap<Resource, ResourceType> resources = new HashMap<Resource, ResourceType>();
			resources.put(ResourceFactory.newFileResource(kbFile), ResourceType.DRL);
			KnowledgeBuilderConfiguration builderConfig = KnowledgeBuilderFactory
					.newKnowledgeBuilderConfiguration();
			builderConfig.setOption(EvaluatorOption.get(
					IsKindOfEvaluatorDefinition.IS_KIND_OF.getOperatorString(),
					new IsKindOfEvaluatorDefinition()));
			builderConfig.setOption(EvaluatorOption.get(
					SatisfiesConstraintEvaluatorDefinition.SATISFIES_CONSTRAINT.getOperatorString(),
					new SatisfiesConstraintEvaluatorDefinition()));
			builderConfig.setOption(EvaluatorOption.get( //TODO this
					IsMemberOfEvaluatorDefinition.IS_MEMBER_OF.getOperatorString(),
					new IsMemberOfEvaluatorDefinition()));
			
			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase, builderConfig);
			for (Resource resource : resources.keySet()) {
				kbuilder.add(resource, resources.get(resource));
			}
			if (kbuilder.hasErrors()) {
				throw new RuntimeException(kbuilder.getErrors().toString());
			}
			
			kpkgs = kbuilder.getKnowledgePackages();
			ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( drlPkgFile ) );
			out.writeObject(kpkgs);
			out.close();
		} else {
			ObjectInputStream in = new ObjectInputStream( new FileInputStream( drlPkgFile ) );
			try {
				// The input stream might contain an individual
				// package or a collection.
				kpkgs = (Collection<KnowledgePackage>) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new IOException(e);
			} finally {
				in.close();
			}
		} 
		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
		kbase.addKnowledgePackages(kpkgs);
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
				ViewCoordinate coordinate = config.getViewCoordinate();
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
}
