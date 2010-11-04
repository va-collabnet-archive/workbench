/**
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
package org.ihtsdo.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetComputeType;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.app.DwfaEnv;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.id.Type3UuidFactory;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.testmodel.DrComponentHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.helper.ResultsItem;
import org.ihtsdo.tk.helper.templates.AbstractTemplate;
import org.ihtsdo.tk.helper.templates.DescriptionTemplate;
import org.ihtsdo.tk.helper.templates.RelationshipTemplate;
import org.ihtsdo.tk.helper.templates.AbstractTemplate.TemplateType;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.tk.spec.SpecFactory;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;

/**
 * The Class RulesLibrary.
 */
public class RulesLibrary {

	/** The CONCEPT MODEL knowledge package identifier */
	public static int CONCEPT_MODEL_PKG = 0;
	public static int LINGUISTIC_GUIDELINES_PKG = 1;


	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, 
			boolean onlyUncommittedContent, I_ConfigAceFrame config) 
	throws Exception {
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		return checkConcept(concept, context, onlyUncommittedContent, config, contextHelper);
	}


	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, 
			boolean onlyUncommittedContent, I_ConfigAceFrame config, RulesContextHelper contextHelper) 
	throws Exception {
		KnowledgeBase kbase = contextHelper.getKnowledgeBaseForContext(context, config);
		ResultsCollectorWorkBench results = new ResultsCollectorWorkBench();
		if (kbase != null) {
			int a1 = kbase.getKnowledgePackages().size();
			int a2 = kbase.getKnowledgePackages().iterator().next().getRules().size();
			if (!(kbase.getKnowledgePackages().size() == 1 &&
					kbase.getKnowledgePackages().iterator().next().getRules().size() == 0)) { 

				StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

				//KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

				ksession.setGlobal("resultsCollector", results);
				ksession.setGlobal("terminologyHelper", new TerminologyHelperDroolsWorkbench());

				ConceptVersionBI conceptBi = Ts.get().getConceptVersion(config.getCoordinate(), concept.getNid());

				DrConcept testConcept = DrComponentHelper.getDrConcept(conceptBi, "Last version");

				ksession.insert(testConcept);

				ksession.startProcess("org.ihtsdo.qa-execution2");
				ksession.fireAllRules();

				//ResultsCollectorWorkBench results = (ResultsCollectorWorkBench) ksession.getGlobal("resultsCollector");

				for (ResultsItem resultsItem : results.getResultsItems() ) {
					results.getAlertList().add(new AlertToDataConstraintFailure(
							AlertToDataConstraintFailure.ALERT_TYPE.ERROR, 
							resultsItem.getErrorCode() + " - " + resultsItem.getMessage(), 
							concept));
				}

				List<String> relTypesList = new ArrayList<String>();
				List<String> textList = new ArrayList<String>();
				for (AbstractTemplate template : results.getTemplates()) {
					if (template.getType().equals(TemplateType.DESCRIPTION)) {
						DescriptionTemplate dtemplate = (DescriptionTemplate) template;
						if (!textList.contains(dtemplate.getText())) {
							textList.add(dtemplate.getText());
							DescriptionVersionBI description = (DescriptionVersionBI) Ts.get().getComponentVersion(config.getCoordinate(),
									UUID.fromString(dtemplate.getComponentUuid()));
							DescriptionSpec dSpec = SpecFactory.get(description);
							if (dtemplate.getText() != null) {
								dSpec.setDescText(dtemplate.getText());
							}
							//TODO: implement other properties
							results.getWbTemplates().put(dSpec, description.getNid());
						}
					}

					if (template.getType().equals(TemplateType.RELATIONSHIP)) {
						RelationshipTemplate rtemplate = (RelationshipTemplate) template;
						if (!relTypesList.contains(rtemplate.getTypeUuid().trim())) {
							relTypesList.add(rtemplate.getTypeUuid().trim());
							ConceptSpec sourceConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getSourceUuid())).toString(),
									UUID.fromString(rtemplate.getSourceUuid()));
							ConceptSpec typeConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getTypeUuid())).toString(),
									UUID.fromString(rtemplate.getTypeUuid()));
							ConceptSpec targetConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getTargetUuid())).toString(),
									UUID.fromString(rtemplate.getTargetUuid()));
							RelSpec relSpec = new RelSpec(sourceConceptSpec, typeConceptSpec, targetConceptSpec);
							//TODO: implement other properties
							results.getWbTemplates().put(relSpec, concept.getConceptNid());
						}
					}
					//TODO: implement other templates
				}

				ksession.dispose();
			}
		}

		return results;
	}

	/**
	 * Check concept.
	 * 
	 * @param concept the concept
	 * @param config the config
	 * 
	 * @return the array list< alert to data constraint failure>
	 * @throws Exception 
	 */
	@Deprecated
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, int kbId, boolean onlyUncommittedContent
	) throws Exception {
		return checkConcept(concept, kbId, null, onlyUncommittedContent);
	}

	@Deprecated
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, int kbId, 
			I_GetConceptData languageRefset, boolean onlyUncommittedContent) 
	throws Exception {
		KnowledgeBase kbase = getKnowledgeBase(kbId);

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		RulesAgenda agenda = (RulesAgenda) config.getDbConfig().getProperty("RulesAgenda");

		if (agenda == null) {
			agenda = new RulesAgenda();
		}

		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			//System.out.println("** " + kpackg.getName());
			for (Rule rule : kpackg.getRules()) {
				//System.out.println("**** " + rule.getName());
				boolean excluded = false;
				String ruleUid = rule.getMetaAttribute("UID");
				if (ruleUid != null) {
					if (agenda.getExcludedRules().containsKey(UUID.fromString(ruleUid))) {
						excluded = true;
					}
				}
				if (agenda.getExcludedRules().containsValue(rule.getName())) {
					excluded = true;
				}
				if (excluded) {
					kbase.removeRule(kpackg.getName(), rule.getName());
				}
			}
		}

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		ksession.setGlobal("resultsCollector", new ResultsCollectorWorkBench());
		ksession.setGlobal("terminologyHelper", new TerminologyHelperDroolsWorkbench());

		//TODO: convert to tk model
		//		List<TerminologyComponent> termComponents =  new ArrayList<TerminologyComponent>();
		//		if (onlyUncommittedContent) {
		//			termComponents.addAll(TestModelUtil.convertUncommittedToTestModel(concept, true, true, true, true));
		//		} else {
		//			termComponents.addAll(TestModelUtil.convertToTestModel(concept, true, true, true, true));
		//		}
		//
		//		for (TerminologyComponent termComponent : termComponents) {
		//			ksession.insert(termComponent);
		//		}

		//		if (languageRefset != null) {
		//			termComponents = TestModelUtil.convertContextualizedDescriptionsToTestModel(concept, languageRefset);
		//			for (TerminologyComponent termComponent : termComponents) {
		//				ksession.insert(termComponent);
		//			}
		//		}

		ksession.fireAllRules();

		ResultsCollectorWorkBench results = (ResultsCollectorWorkBench) ksession.getGlobal("resultsCollector");

		//		for (int errorCode : results.getErrorCodes().keySet() ) {
		//			results.getAlertList().add(new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.ERROR, 
		//					results.getErrorCodes().get(errorCode), 
		//					concept));
		//		}

		ksession.dispose();

		return results;
	}

	@Deprecated
	public static ResultsCollectorWorkBench checkObjects(List<Object> objects, int kbId) throws Exception {
		KnowledgeBase kbase = getKnowledgeBase(kbId);

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		FactHandle resultsHandle = ksession.insert(new ResultsCollectorWorkBench());

		for (Object object : objects) {
			ksession.insert(object);
		}

		ksession.fireAllRules();

		ResultsCollectorWorkBench results = (ResultsCollectorWorkBench) ksession.getObject(resultsHandle);

		ksession.dispose();

		return results;
	}

	@Deprecated
	public static ResultsCollectorWorkBench checkObjectsTestModel(List<Object> objects, int kbId) throws Exception {
		KnowledgeBase kbase = getKnowledgeBase(kbId);

		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			System.out.println("** " + kpackg.getName());
			for (Rule rule : kpackg.getRules()) {
				System.out.println("**** " + rule.getName());
				if (rule.getName().trim().equals("Check for double spaces")) {
					System.out.println("****** " + rule.getMetaAttribute("UID"));
					//kbase.removeRule(kpackg.getName(), rule.getName());
				}
			}
		}

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		ksession.setGlobal("resultsCollector", new ResultsCollectorWorkBench());
		ksession.setGlobal("terminologyHelper", new TerminologyHelperDroolsWorkbench());

		//ksession.insert(new TransitiveClosureHelperMock());

		for (Object object : objects) {
			ksession.insert(object);
		}

		ksession.fireAllRules();

		ResultsCollectorWorkBench results = (ResultsCollectorWorkBench) ksession.getGlobal("resultsCollector");

		ksession.dispose();

		return results;
	}	

	/**
	 * Gets the descriptions.
	 * 
	 * @param concept the concept
	 * 
	 * @return the descriptions
	 */
	public static List<? extends I_DescriptionTuple> getDescriptions(I_GetConceptData concept) {
		I_TermFactory tf = Terms.get();
		List<? extends I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		try {
			//TODO add config as parameter
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_IntSet descriptionTypes =  tf.newIntSet();
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
			descriptions = concept.getDescriptionTuples(tf.getActiveAceFrameConfig().getAllowedStatus(), 
					descriptionTypes, tf.getActiveAceFrameConfig().getViewPositionSetReadOnly(),
					config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return descriptions;
	}

	/**
	 * Gets the knowledge base from the Guvnor deployment URL.
	 * 
	 * @param kbId the kb id
	 * @param url the url
	 * @param recreate the recreate
	 * 
	 * @return the knowledge base
	 * 
	 * @throws Exception the exception
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, String url, boolean recreate) throws Exception {
		KnowledgeBase kbase= null;
		File serializedKbFile = new File("rules/knowledge_packages-" + kbId + ".pkg");
		if (kbId == RulesLibrary.CONCEPT_MODEL_PKG) {
			if (serializedKbFile.exists() && !recreate) {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
					// The input stream might contain an individual
					// package or a collection.
					@SuppressWarnings( "unchecked" )
					Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>)in.readObject();
					in.close();
					kbase = KnowledgeBaseFactory.newKnowledgeBase();
					kbase.addKnowledgePackages(kpkgs);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "Agent" );
				kagent.applyChangeSet( ResourceFactory.newFileResource( url ) );
				kbase = kagent.getKnowledgeBase();
				try {
					ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( serializedKbFile ) );
					out.writeObject( kbase.getKnowledgePackages() );
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return kbase;
	}

	/**
	 * Gets the knowledge base from the Guvnor deployment URL retrieved from a byteArray, not a file.
	 * 
	 * @param kbId the kb id
	 * @param url the url
	 * @param recreate the recreate
	 * 
	 * @return the knowledge base
	 * 
	 * @throws Exception the exception
	 */
	public static KnowledgeBase getKnowledgeBase(UUID referenceUuid, byte[] bytes, boolean recreate) throws Exception {
		KnowledgeBase kbase= null;

		if (recreate) {
			try {
				kbase = getKnowledgeBaseWithAgent(referenceUuid, bytes);
			} catch (Exception e) {
				// agent base not available
				System.out.println("WARNING: Agent based connection with guvnor not available, using trying to load from cache...");
				kbase = getKnowledgeBaseFromFileCache(referenceUuid);
				if (kbase != null) {
					System.out.println("WARNING: Cache load OK.");
				} else {
					System.out.println("WARNING: Cache loading failed, No knowledgebase.");
				}
			}
		} else  {
			kbase = getKnowledgeBaseFromFileCache(referenceUuid);
			if (kbase != null) {
				System.out.println("WARNING: Cache loading failed, terying Guvnor...");
				kbase = getKnowledgeBaseWithAgent(referenceUuid, bytes);
				if (kbase != null) {
					System.out.println("WARNING: Guvnor load OK.");
				} else {
					System.out.println("WARNING: Guvnor loading failed, No knowledgebase.");
				}
			}
		}

		return kbase;
	}

	private static KnowledgeBase getKnowledgeBaseFromFileCache(UUID referenceUuid) {
		KnowledgeBase kbase= null;
		File rulesDirectory = new File("rules");
		if (!rulesDirectory.exists())
		{
			rulesDirectory.mkdir();
		}
		File serializedKbFile = new File(rulesDirectory, "knowledge_packages-" + referenceUuid.toString() + ".pkg");

		if (serializedKbFile.exists()) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
				// The input stream might contain an individual
				// package or a collection.
				kbase = (KnowledgeBase)in.readObject();
				in.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return kbase;
	}
	private static KnowledgeBase getKnowledgeBaseWithAgent(UUID referenceUuid, byte[] bytes) {
		KnowledgeBase kbase= null;
		File rulesDirectory = new File("rules");
		if (!rulesDirectory.exists())
		{
			rulesDirectory.mkdir();
		}
		File serializedKbFile = new File(rulesDirectory, "knowledge_packages-" + referenceUuid.toString() + ".pkg");
		KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "Agent" );
		kagent.applyChangeSet( ResourceFactory.newByteArrayResource(bytes) );
		kbase = kagent.getKnowledgeBase();
		try {
			ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( serializedKbFile ) );
			out.writeObject( kbase );
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return kbase;
	}

	public static boolean validateDeploymentPackage(UUID referenceUuid, byte[] bytes) {
		KnowledgeBase kbase= null;
		try {
			kbase = getKnowledgeBase(referenceUuid, bytes, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (kbase != null);
	}

	public static boolean isPackageOnLine(byte[] bytes) {
		KnowledgeBase kbase= null;
		try {
			KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent( "Agent" );
			kagent.applyChangeSet( ResourceFactory.newByteArrayResource(bytes) );
			kbase = kagent.getKnowledgeBase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (kbase != null);
	}

	/**
	 * Gets the knowledge base.
	 * 
	 * @param kbId the kb id
	 * 
	 * @return the knowledge base
	 * @throws Exception 
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId) throws Exception {
		return getKnowledgeBase(kbId, false, null);
	}

	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, HashMap<Resource, ResourceType> resources) throws Exception {
		return getKnowledgeBase(kbId, false, resources);
	}

	/**
	 * Gets the knowledge base.
	 * 
	 * @param kbId the kb id
	 * @param recreate the recreate
	 * 
	 * @return the knowledge base
	 * @throws Exception 
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, boolean recreate, 
			HashMap<Resource, ResourceType> resources) throws Exception {
		KnowledgeBase kbase= null;
		File serializedKbFile = new File("rules/knowledge_packages-" + kbId + ".pkg");
		if (serializedKbFile.exists() && !recreate) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
				// The input stream might contain an individual
				// package or a collection.
				@SuppressWarnings( "unchecked" )
				Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>)in.readObject();
				in.close();
				kbase = KnowledgeBaseFactory.newKnowledgeBase();
				kbase.addKnowledgePackages(kpkgs);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			if (resources == null || resources.isEmpty()) {
				//
				if (kbId == RulesLibrary.CONCEPT_MODEL_PKG) {
					resources = new HashMap<Resource, ResourceType>();
					resources.put( ResourceFactory.newFileResource("rules/sample-descriptions-rules.drl"), ResourceType.DRL );
				} else if (kbId == RulesLibrary.LINGUISTIC_GUIDELINES_PKG) {
					resources = new HashMap<Resource, ResourceType>();
					resources.put( ResourceFactory.newFileResource("rules/sample-guidelines-rules.drl"), ResourceType.DRL );
				} else {
					throw new Exception("No rules resources to process");
				}
			}
			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
			for (Resource resource : resources.keySet()) {
				kbuilder.add(resource, resources.get(resource));
			}

			if ( kbuilder.hasErrors() ) {
				System.err.println(kbuilder.getErrors().toString() );
			}
			kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

			try {
				ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( serializedKbFile ) );
				out.writeObject( kbuilder.getKnowledgePackages() );
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		//		}
		return kbase;
	}


	/**
	 * Gets the snomed concept id.
	 * 
	 * @param concept the concept
	 * 
	 * @return the snomed concept id
	 */
	static public String getSnomedConceptId(I_GetConceptData concept) {
		String id = null;
		I_TermFactory tf = Terms.get();
		try {
			List<? extends I_IdPart> idParts = tf.getId(concept.getConceptNid()).getMutableIdParts();
			for (I_IdPart idPart : idParts) {
				if (idPart.getAuthorityNid() == ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid()) {
					id = (String) idPart.getDenotation();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return id;

	}

	/**
	 * Gets the concept.
	 * 
	 * @param snomedConceptId the snomed concept id
	 * 
	 * @return the concept
	 */
	static public I_GetConceptData getConcept(String snomedConceptId) {
		I_TermFactory tf = Terms.get();
		I_GetConceptData concept = null;
		try {
			UUID elementUuid = Type3UuidFactory.fromSNOMED(snomedConceptId);
			concept = tf.getConcept(new UUID[] {elementUuid});
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return concept;
	}

	/**
	 * Gets the integer id for uuid.
	 * 
	 * @param uuid the uuid
	 * 
	 * @return the integer id for uuid
	 */
	public static Integer getIntegerIdForUUID(UUID uuid) {
		I_TermFactory tf = Terms.get();
		I_GetConceptData concept = null;
		try {
			concept = tf.getConcept(new UUID[] {uuid});
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return concept.getConceptNid();
	}

	/**
	 * Gets the integer id for concept spec.
	 * 
	 * @param conceptSpec the concept spec
	 * 
	 * @return the integer id for concept spec
	 */
	public static Integer getIntegerIdForConceptSpec(String conceptSpec) {
		UUID uuid = UUID.fromString(conceptSpec.substring(conceptSpec.indexOf("|") + 1, conceptSpec.indexOf(">")));
		//TODO: verify text integrity
		return getIntegerIdForUUID(uuid);
	}

	/**
	 * Gets the alert with text change fix up.
	 * 
	 * @param concept the concept
	 * @param description the description
	 * @param newText the new text
	 * 
	 * @return the alert with text change fix up
	 */
	public static AlertToDataConstraintFailure getAlertWithTextChangeFixUp(I_GetConceptData concept, I_DescriptionTuple description,
			String newText) {
		AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.ERROR, 
				description.getText(), concept);
		alert.getFixOptions().add(new ChangeDescriptionTextFixUp(concept, description.getMutablePart(), newText));				  
		return alert;
	}


	/**
	 * Gets the rel tuple details.
	 * 
	 * @param rel the rel
	 * 
	 * @return the rel tuple details
	 */
	public static String getRelTupleDetails(I_RelTuple rel) {
		String details = "";
		I_TermFactory tf = Terms.get();

		try {
			I_GetConceptData sourceConcept = tf.getConcept(rel.getC1Id());
			I_GetConceptData attributeConcept = tf.getConcept(rel.getTypeId());
			I_GetConceptData targetConcept = tf.getConcept(rel.getC2Id());
			I_GetConceptData charType = tf.getConcept(rel.getCharacteristicId());

			details = "Source: " + sourceConcept.toString() + " - Type: " + attributeConcept.toString() + " - Target: " +
			targetConcept.toString() + " - CharType: " + charType.toString() + " - Group: " + rel.getGroup();

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return details;
	}

	public static void updateGuvnorEnumerations(I_GetConceptData refset, RulesDeploymentPackageReference kPack, I_ConfigAceFrame config) {
		try {
			ConceptVersionBI refsetBI = Ts.get().getConceptVersion(config.getCoordinate(), refset.getUids());
			String propertyName = "";
			int guvnorDescriptionsSize = refsetBI.getDescsActive(ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.localize().getNid()).size();
			if (guvnorDescriptionsSize < 1 || guvnorDescriptionsSize > 1) {
				throw new Exception("Wrong number of guvnor property descriptions: " + guvnorDescriptionsSize);
			}
			propertyName = refsetBI.getDescsActive(ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.localize().getNid()).iterator().next().getText();

			String guvnorEnumerationText = "'" + propertyName + "' : [";
			for (I_ExtendByRef loopMember : Terms.get().getRefsetExtensionMembers(refset.getConceptNid())) {

				I_ExtendByRefPartCid lastPart = (I_ExtendByRefPartCid) loopMember.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next().getMutablePart();
				ConceptVersionBI loopConcept = Ts.get().getConceptVersion(config.getCoordinate(),lastPart.getC1id());

				String name = "";

				if (loopConcept.getDescsActive(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()).size() > 0) {
					name = loopConcept.getDescsActive(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()).iterator().next().getText();
				} else if (loopConcept.getDescsActive().size() > 0){
					name = loopConcept.getDescsActive().iterator().next().getText();
				} else {
					name = "no description found";
				}

				guvnorEnumerationText = guvnorEnumerationText + "'" + loopConcept.getPrimUuid();
				guvnorEnumerationText = guvnorEnumerationText + "=" + name;
				guvnorEnumerationText = guvnorEnumerationText + "',";
			}
			guvnorEnumerationText = guvnorEnumerationText.substring(0, guvnorEnumerationText.length()-1) + "]";
			System.out.println(guvnorEnumerationText);

			Sardine sardine = SardineFactory.begin("username", "password");

			String pkgUrl = kPack.getUrl().substring(0, kPack.getUrl().indexOf(".Guvnor") + 7) + "/webdav/packages/";
			pkgUrl = pkgUrl + kPack.getUrl().substring(kPack.getUrl().indexOf("/package/") + 9, kPack.getUrl().indexOf("/", kPack.getUrl().indexOf("/package/") + 10));
			pkgUrl = pkgUrl + "/" + propertyName + ".enumeration";
			//"http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/qa4/qa4Demo"
			sardine.put(pkgUrl, guvnorEnumerationText.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ContraditionException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean isIncludedInRefsetSpec(I_GetConceptData refset, I_GetConceptData candidateConcept, I_ConfigAceFrame config) {
		boolean result = false;

		try {
			RefsetSpec refsetSpecHelper = new RefsetSpec(refset, true, config);
			I_GetConceptData refsetSpec = refsetSpecHelper.getRefsetSpecConcept();
			AceLog.getAppLog().info("Refset: " + refset.getInitialText() + " " + refset.getUids().get(0));
			AceLog.getAppLog().info("Refset spec: " + refsetSpec.getInitialText() + " " + refsetSpec.getUids().get(0));
			RefsetComputeType computeType = RefsetComputeType.CONCEPT; // default
			if (refsetSpecHelper.isDescriptionComputeType()) {
				computeType = RefsetComputeType.DESCRIPTION;
			} else if (refsetSpecHelper.isRelationshipComputeType()) {
				AceLog.getAppLog().info("Invalid refset spec to compute - relationship compute types not supported.");
				if (!DwfaEnv.isHeadless()) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
							"Invalid refset spec to compute - relationship compute types not supported.", "",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			// verify a valid refset spec construction
			if (refsetSpec == null) {
				AceLog.getAppLog().info(
				"Invalid refset spec to compute - unable to get spec from the refset currently in the spec panel.");
				if (!DwfaEnv.isHeadless()) {
					JOptionPane
					.showMessageDialog(
							LogWithAlerts.getActiveFrame(null),
							"Invalid refset spec to compute - unable to get spec from the refset currently in the spec panel.",
							"", JOptionPane.ERROR_MESSAGE);
				}
			}
			// Step 1: create the query object, based on the refset spec
			RefsetSpecQuery query =
				RefsetQueryFactory.createQuery(config, Terms.get(), refsetSpec, refset, computeType);

			// check validity of query
			if (!query.isValidQuery() && query.getTotalStatementCount() != 0) {
				AceLog.getAppLog().info("Refset spec has dangling AND/OR. These must have sub-statements.");
				if (!DwfaEnv.isHeadless()) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
							"Refset spec has dangling AND/OR. These must have sub-statements.", "",
							JOptionPane.ERROR_MESSAGE);
				}
			}

			I_GetConceptData selectedConcept = candidateConcept;

			System.out.println("************ Starting test computation *****************");
			System.out.println("Refset spec = " + refsetSpec.toString());
			System.out.println("Refset = " + refset.toString());
			System.out.println("Concept to test = " + selectedConcept.toString());

			List<I_ShowActivity> activities = new ArrayList<I_ShowActivity>();
			result = query.execute(selectedConcept, activities);

			System.out.println("++++++++++++++ Result = " + result);
			System.out.println("************ Finished test computation *****************");
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
			try {
				Terms.get().cancel();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}

		return result;
	}
}
