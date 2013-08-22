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
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.SystemEventListener;
import org.drools.agent.KnowledgeAgent;
import org.drools.agent.KnowledgeAgentConfiguration;
import org.drools.agent.KnowledgeAgentFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure.ALERT_TYPE;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.task.refset.spec.compute.RefsetComputeType;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.app.DwfaEnv;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.dwfa.util.id.Type3UuidFactory;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.testmodel.DrComponentHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.rules.testmodel.TerminologyHelperDroolsWorkbench;
import org.ihtsdo.testmodel.DrConcept;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.helper.ResultsItem;
import org.ihtsdo.tk.helper.ResultsItem.Severity;
import org.ihtsdo.tk.helper.templates.AbstractTemplate;
import org.ihtsdo.tk.helper.templates.AbstractTemplate.TemplateType;
import org.ihtsdo.tk.helper.templates.DescriptionTemplate;
import org.ihtsdo.tk.helper.templates.RelationshipTemplate;
import org.ihtsdo.tk.spec.ConceptSpec;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelationshipSpec;
import org.ihtsdo.tk.spec.SpecFactory;
import org.mvel2.ast.IsDef;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import org.ihtsdo.tk.api.ConceptFetcherSimple;

/**
 * The Class RulesLibrary.
 */
public class RulesLibrary {

	/** The CONCEPT MODEL knowledge package identifier. */
	public static int CONCEPT_MODEL_PKG = 0;

	/** The LINGUISTI c_ guideline s_ pkg. */
	public static int LINGUISTIC_GUIDELINES_PKG = 1;

	/** The terminology helper cache. */
	public static TerminologyHelperDroolsWorkbench terminologyHelperCache;

	/** The no realtime rules alert shown. */
	public static boolean noRealtimeRulesAlertShown = false;

	/** The rules disabled. */
	public static boolean rulesDisabled = false;

	/**
	 * The Enum INFERRED_VIEW_ORIGIN.
	 */
	public enum INFERRED_VIEW_ORIGIN {
		/** The STATED. */
		STATED, /** The CONSTRAIN t_ norma l_ form. */
		CONSTRAINT_NORMAL_FORM, /** The INFERRED. */
		INFERRED
	};

	/** The all rels. */
	public static I_IntSet allRels;

	/** The hist rels. */
	public static I_IntSet histRels;

	/** The Cpt model rels. */
	public static I_IntSet CptModelRels;

	/**
	 * Gets the terminology helper.
	 * 
	 * @return the terminology helper
	 */
	public static TerminologyHelperDroolsWorkbench getTerminologyHelper() {
		if (terminologyHelperCache == null) {
			terminologyHelperCache = new TerminologyHelperDroolsWorkbench();
			terminologyHelperCache.loadProperties();
			return terminologyHelperCache;
		} else {
			return terminologyHelperCache;
		}
	}

	/**
	 * Setup is a cache.
	 * 
	 * @return the kind of cache bi
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws Exception
	 *             the exception
	 */
	//	public static KindOfCacheBI setupIsACache() throws TerminologyException, Exception {
	//		myStaticIsACache = Ts.get().getCache(Terms.get().getActiveAceFrameConfig().getViewCoordinate());
	//		RefsetSpecQuery.myStaticIsACache = myStaticIsACache;
	//		myStaticIsACacheRefsetSpec = RefsetSpecQuery.myStaticIsACache;
	//		return myStaticIsACache;
	//	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param context
	 *            the context
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @param config
	 *            the config
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, boolean checkUncommittedContent, I_ConfigAceFrame config) throws Exception {
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		return checkConcept(concept, context, checkUncommittedContent, config, contextHelper, INFERRED_VIEW_ORIGIN.STATED);
	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param context
	 *            the context
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @param config
	 *            the config
	 * @param contextHelper
	 *            the context helper
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, boolean checkUncommittedContent, I_ConfigAceFrame config, RulesContextHelper contextHelper) throws Exception {
		return checkConcept(concept, context, checkUncommittedContent, config, contextHelper, INFERRED_VIEW_ORIGIN.STATED);
	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param context
	 *            the context
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @param config
	 *            the config
	 * @param inferredOrigin
	 *            the inferred origin
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, boolean checkUncommittedContent, I_ConfigAceFrame config, INFERRED_VIEW_ORIGIN inferredOrigin) throws Exception {
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		return checkConcept(concept, context, checkUncommittedContent, config, contextHelper, inferredOrigin);
	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param context
	 *            the context
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @param config
	 *            the config
	 * @param contextHelper
	 *            the context helper
	 * @param inferredOrigin
	 *            the inferred origin
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, I_GetConceptData context, boolean checkUncommittedContent, I_ConfigAceFrame config, RulesContextHelper contextHelper, INFERRED_VIEW_ORIGIN inferredOrigin)
			throws Exception {
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		
		I_ShowActivity activity = null;
		if (!DwfaEnv.isHeadless()) {
			activity = Terms.get().newActivityPanel(true, config, "<html>Performing QA check on concept: " + concept.toString() + " for " + context.toString(), true);
			activities.add(activity);
			activity.setValue(0);
			activity.setIndeterminate(true);
			activity.setProgressInfoLower("Getting KnowledgeBase...");
			Terms.get().getActiveAceFrameConfig().setStatusMessage("Getting KnowledgeBase...");
		}
		long startTime = System.currentTimeMillis();
		// AceLog.getAppLog().info("Starting concept check...");
		KnowledgeBase kbase = contextHelper.getKnowledgeBaseForContext(context, config);
		if (!noRealtimeRulesAlertShown && context.getUids().containsAll(RefsetAuxiliary.Concept.REALTIME_PRECOMMIT_QA_CONTEXT.getUids()) && kbase.getKnowledgePackages().size() < 2) {
			noRealtimeRulesAlertShown = true;
			AceLog.getAppLog().alertAndLogException(new IOException("Warning! No rules in context" + context.toString() + ". QA is disabled."));
		}
		// AceLog.getAppLog().info("KBase: null = " + (kbase==null) + "; Size: "
		// + kbase.getKnowledgePackages().size());
		ResultsCollectorWorkBench results = new ResultsCollectorWorkBench();
		try {
			if (kbase != null) {
				// int a1 = kbase.getKnowledgePackages().size();
				// int a2 =
				// kbase.getKnowledgePackages().iterator().next().getRules().size();
				if (!(kbase.getKnowledgePackages().size() == 0) && !(kbase.getKnowledgePackages().size() == 1 && kbase.getKnowledgePackages().iterator().next().getRules().size() == 0)) {
					if (!DwfaEnv.isHeadless()) {
						activity.setProgressInfoLower("Creating session...");
						config.setStatusMessage("Creating session...");
					}

					StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

					// KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);

					ksession.setGlobal("resultsCollector", results);
					ksession.setGlobal("terminologyHelper", getTerminologyHelper());

					ConceptVersionBI conceptBi = Ts.get().getConceptVersion(config.getViewCoordinate(), concept.getNid());

					if (!DwfaEnv.isHeadless()) {
						activity.setProgressInfoLower("Converting concept...");
						config.setStatusMessage("Converting concept...");
					}

					DrConcept testConcept = DrComponentHelper.getDrConcept(conceptBi, "Last version", inferredOrigin);
					// AceLog.getAppLog().info("Test concept converted...");
					if (!DwfaEnv.isHeadless()) {
						activity.setProgressInfoLower("Testing concept...");
						config.setStatusMessage("Testing concept...");
					}

					ksession.insert(testConcept);

					ksession.startProcess("org.ihtsdo.qa-execution3");
					ksession.fireAllRules();

					// ResultsCollectorWorkBench results =
					// (ResultsCollectorWorkBench)
					// ksession.getGlobal("resultsCollector");
					// AceLog.getAppLog().info("Results size: " +
					// results.getResultsItems().size());
					for (ResultsItem resultsItem : results.getResultsItems()) {
						ALERT_TYPE alertType = ALERT_TYPE.ERROR;

						if (resultsItem.getSeverity() != null && !resultsItem.getSeverity().isEmpty()) {
							if (resultsItem.getSeverity().trim().equals(Severity.NOTIFICATION.getSeverityUuid().toString())) {
								alertType = ALERT_TYPE.INFORMATIONAL;
							} else if (resultsItem.getSeverity().trim().equals(Severity.WARNING.getSeverityUuid().toString())) {
								alertType = ALERT_TYPE.WARNING;
							}
						}

						results.getAlertList().add(new AlertToDataConstraintFailure(alertType, resultsItem.getErrorCode() + " - " + resultsItem.getMessage(), concept));
					}

					List<String> relTypesList = new ArrayList<String>();
					List<String> textList = new ArrayList<String>();
					for (AbstractTemplate template : results.getTemplates()) {
						if (template.getType().equals(TemplateType.DESCRIPTION)) {
							DescriptionTemplate dtemplate = (DescriptionTemplate) template;
							if (!textList.contains(dtemplate.getText())) {
								textList.add(dtemplate.getText());
								DescriptionVersionBI description = (DescriptionVersionBI) Ts.get().getComponentVersion(config.getViewCoordinate(), UUID.fromString(dtemplate.getComponentUuid()));
								DescriptionSpec dSpec = SpecFactory.get(description, config.getViewCoordinate());
								if (dSpec != null && dtemplate.getText() != null) {
									dSpec.setDescText(dtemplate.getText());
									// TODO: implement other properties
									results.getWbTemplates().put(dSpec, description.getNid());
								}

							}
						}

						if (template.getType().equals(TemplateType.RELATIONSHIP)) {
							RelationshipTemplate rtemplate = (RelationshipTemplate) template;
							if (!relTypesList.contains(rtemplate.getTypeUuid().trim())) {
								relTypesList.add(rtemplate.getTypeUuid().trim());
								ConceptSpec sourceConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getSourceUuid())).toString(), UUID.fromString(rtemplate.getSourceUuid()));
								ConceptSpec typeConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getTypeUuid())).toString(), UUID.fromString(rtemplate.getTypeUuid()));
								ConceptSpec targetConceptSpec = new ConceptSpec(Terms.get().getConcept(UUID.fromString(rtemplate.getTargetUuid())).toString(), UUID.fromString(rtemplate.getTargetUuid()));
								RelationshipSpec relSpec = new RelationshipSpec(sourceConceptSpec, typeConceptSpec, targetConceptSpec);
								// TODO: implement other properties
								results.getWbTemplates().put(relSpec, concept.getConceptNid());
							}
						}
						// TODO: implement other templates
					}

					ksession.dispose();
				}
			} else {
				AceLog.getAppLog().info("QA Skipped, kbase was null...");
			}
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			String result = "Error";
			if (!DwfaEnv.isHeadless()) {
				activity.setProgressInfoUpper("<html>Error in QA check on concept: " + concept.toString() + " for " + context.toString());
				activity.setProgressInfoLower("Error: elapsed: " + elapsed + " ms; " + result + " -  Rules fired:" + results.getResultsItems().size());
				try {
					activity.complete();
				} catch (ComputationCanceled e1) {
					// Nothing to do
				}
				AceLog.getAppLog().alertAndLogException(e);
			} else {
				System.out.println("Error in QA check on concept: " + concept.toString() + " for " + context.toString());
			}
			//throw(e);
		}
		long endTime = System.currentTimeMillis();
		long elapsed = endTime - startTime;
		String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
		String result = "Done";
		if (!DwfaEnv.isHeadless()) {
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			activity.setProgressInfoUpper("<html>Performed QA check on concept: " + concept.toString() + " for " + context.toString());
			activity.setProgressInfoLower("Elapsed: " + elapsed + " ms; " + result + " -  Rules fired:" + results.getResultsItems().size() + " - " + sdf.format(cal.getTime()));

			try {
				activity.complete();
			} catch (ComputationCanceled e) {
				// Nothing to do
			}
			config.setStatusMessage("");
		}

		return results;
	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param kbId
	 *            the kb id
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @return the array list< alert to data constraint failure>
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, int kbId, boolean checkUncommittedContent) throws Exception {
		return checkConcept(concept, kbId, null, checkUncommittedContent);
	}

	/**
	 * Check concept.
	 * 
	 * @param concept
	 *            the concept
	 * @param kbId
	 *            the kb id
	 * @param languageRefset
	 *            the language refset
	 * @param checkUncommittedContent
	 *            the only uncommitted content
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static ResultsCollectorWorkBench checkConcept(I_GetConceptData concept, int kbId, I_GetConceptData languageRefset, boolean checkUncommittedContent) throws Exception {
		KnowledgeBase kbase = getKnowledgeBase(kbId);

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		RulesAgenda agenda = (RulesAgenda) config.getDbConfig().getProperty("RulesAgenda");

		if (agenda == null) {
			agenda = new RulesAgenda();
		}

		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			// AceLog.getAppLog().info("** " + kpackg.getName());
			for (Rule rule : kpackg.getRules()) {
				// AceLog.getAppLog().info("**** " + rule.getName());
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
		ksession.setGlobal("terminologyHelper", getTerminologyHelper());

		// TODO: convert to tk model
		// List<TerminologyComponent> termComponents = new
		// ArrayList<TerminologyComponent>();
		// if (checkUncommittedContent) {
		// termComponents.addAll(TestModelUtil.convertUncommittedToTestModel(concept,
		// true, true, true, true));
		// } else {
		// termComponents.addAll(TestModelUtil.convertToTestModel(concept, true,
		// true, true, true));
		// }
		//
		// for (TerminologyComponent termComponent : termComponents) {
		// ksession.insert(termComponent);
		// }

		// if (languageRefset != null) {
		// termComponents =
		// TestModelUtil.convertContextualizedDescriptionsToTestModel(concept,
		// languageRefset);
		// for (TerminologyComponent termComponent : termComponents) {
		// ksession.insert(termComponent);
		// }
		// }

		ksession.fireAllRules();

		ResultsCollectorWorkBench results = (ResultsCollectorWorkBench) ksession.getGlobal("resultsCollector");

		// for (int errorCode : results.getErrorCodes().keySet() ) {
		// results.getAlertList().add(new
		// AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.ERROR,
		// results.getErrorCodes().get(errorCode),
		// concept));
		// }

		ksession.dispose();

		return results;
	}

	/**
	 * Check objects.
	 * 
	 * @param objects
	 *            the objects
	 * @param kbId
	 *            the kb id
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
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

	/**
	 * Check objects test model.
	 * 
	 * @param objects
	 *            the objects
	 * @param kbId
	 *            the kb id
	 * @return the results collector work bench
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static ResultsCollectorWorkBench checkObjectsTestModel(List<Object> objects, int kbId) throws Exception {
		KnowledgeBase kbase = getKnowledgeBase(kbId);

		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			AceLog.getAppLog().info("** " + kpackg.getName());
			for (Rule rule : kpackg.getRules()) {
				AceLog.getAppLog().info("**** " + rule.getName());
				if (rule.getName().trim().equals("Check for double spaces")) {
					AceLog.getAppLog().info("****** " + rule.getMetaAttribute("UID"));
					// kbase.removeRule(kpackg.getName(), rule.getName());
				}
			}
		}

		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		ksession.setGlobal("resultsCollector", new ResultsCollectorWorkBench());
		ksession.setGlobal("terminologyHelper", getTerminologyHelper());

		// ksession.insert(new TransitiveClosureHelperMock());

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
	 * @param concept
	 *            the concept
	 * 
	 * @return the descriptions
	 */
	public static List<? extends I_DescriptionTuple> getDescriptions(I_GetConceptData concept) {
		I_TermFactory tf = Terms.get();
		List<? extends I_DescriptionTuple> descriptions = new ArrayList<I_DescriptionTuple>();
		try {
			// TODO add config as parameter
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			I_IntSet descriptionTypes = tf.newIntSet();
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()));
			descriptionTypes.add(tf.uuidToNative(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()));
			descriptions = concept.getDescriptionTuples(tf.getActiveAceFrameConfig().getAllowedStatus(), descriptionTypes, tf.getActiveAceFrameConfig().getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return descriptions;
	}

	/**
	 * Gets the knowledge base from the Guvnor deployment URL.
	 * 
	 * @param kbId
	 *            the kb id
	 * @param url
	 *            the url
	 * @param recreate
	 *            the recreate
	 * 
	 * @return the knowledge base
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, String url, boolean recreate) throws Exception {
		KnowledgeBase kbase = null;
		File serializedKbFile = new File("rules/knowledge_packages-" + kbId + ".pkg");
		if (kbId == RulesLibrary.CONCEPT_MODEL_PKG) {
			if (serializedKbFile.exists() && !recreate) {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
					// The input stream might contain an individual
					// package or a collection.
					@SuppressWarnings("unchecked")
					Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>) in.readObject();
					in.close();
					kbase = KnowledgeBaseFactory.newKnowledgeBase();
					kbase.addKnowledgePackages(kpkgs);
				} catch (StreamCorruptedException e0) {
					serializedKbFile.delete();
				} catch (FileNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (ClassNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			} else {
				KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("Agent");
				kagent.applyChangeSet(ResourceFactory.newFileResource(url));
				kbase = kagent.getKnowledgeBase();
				try {
					ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedKbFile));
					out.writeObject(kbase.getKnowledgePackages());
					out.close();
				} catch (FileNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
		return kbase;
	}
	
	/**
	 * Gets the knowledge base from the Guvnor deployment URL retrieved from a
	 * byteArray, not a file.
	 * 
	 * @param referenceUuid
	 *            the reference uuid
	 * @param bytes
	 *            the bytes
	 * @param recreate
	 *            the recreate
	 * @return the knowledge base
	 * @throws Exception
	 *             the exception
	 */
	public static KnowledgeBase getKnowledgeBaseFromFolder(UUID referenceUuid, File folder) throws Exception {
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
		
		for (File loopFile : folder.listFiles()) {
			if (loopFile.getName().endsWith(".drl")) {
				kbuilder.add( ResourceFactory.newFileResource(loopFile), ResourceType.DRL );
			}
		}
		if ( kbuilder.hasErrors() ) {
		    AceLog.getAppLog().info( kbuilder.getErrors().toString() );
		}

		KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

		kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
		return kbase;
	}

	/**
	 * Gets the knowledge base from the Guvnor deployment URL retrieved from a
	 * byteArray, not a file.
	 * 
	 * @param referenceUuid
	 *            the reference uuid
	 * @param bytes
	 *            the bytes
	 * @param recreate
	 *            the recreate
	 * @return the knowledge base
	 * @throws Exception
	 *             the exception
	 */
	public static KnowledgeBase getKnowledgeBase(UUID referenceUuid, byte[] bytes, boolean recreate) throws Exception {
		KnowledgeBase kbase = null;

		if (recreate) {
			try {
				kbase = getKnowledgeBaseWithAgent(referenceUuid, bytes);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			if (kbase == null || kbase.getKnowledgePackages().size() == 0) {
				AceLog.getAppLog().info("WARNING: Agent based connection with guvnor not available, trying to load from cache...");
				kbase = getKnowledgeBaseFromFileCache(referenceUuid);
				if (kbase != null && kbase.getKnowledgePackages().size() > 0) {
					AceLog.getAppLog().info("INFO: Cache load OK.");
				} else {
					AceLog.getAppLog().info("WARNING: Cache loading failed.");
				}
			}
		} else {
			kbase = getKnowledgeBaseFromFileCache(referenceUuid);
			if (kbase == null || kbase.getKnowledgePackages().size() == 0) {
				AceLog.getAppLog().info("INFO: Trying Guvnor...");
				kbase = getKnowledgeBaseWithAgent(referenceUuid, bytes);
				if (kbase != null && kbase.getKnowledgePackages().size() > 0) {
					AceLog.getAppLog().info("INFO: Guvnor load OK.");
				} else {
					AceLog.getAppLog().info("WARNING: Guvnor loading failed, No knowledgebase.");
				}
			}
		}

		if (kbase == null || kbase.getKnowledgePackages().size() == 0)
			throw new Exception("Can't retrieve database...");
		return kbase;
	}

	/**
	 * Gets the knowledge base from file cache.
	 * 
	 * @param referenceUuid
	 *            the reference uuid
	 * @return the knowledge base from file cache
	 * @throws Exception
	 *             the exception
	 */
	private static KnowledgeBase getKnowledgeBaseFromFileCache(UUID referenceUuid) throws Exception {
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Retrieving rules data from cache", true);
		activities.add(activity);
		activity.setValue(0);
		activity.setIndeterminate(true);
		long startTime = System.currentTimeMillis();
		KnowledgeBase kbase = null;
		try {
			File rulesDirectory = new File("rules");
			if (!rulesDirectory.exists()) {
				rulesDirectory.mkdir();
			}
			File serializedKbFile = new File(rulesDirectory, "knowledge_packages-" + referenceUuid.toString() + ".pkg");

			if (serializedKbFile.exists()) {
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
					// The input stream might contain an individual
					// package or a collection.
					kbase = (KnowledgeBase) in.readObject();
					in.close();
				} catch (StreamCorruptedException e0) {
					serializedKbFile.delete();
				} catch (FileNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (ClassNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}

			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			String result = "";
			if (kbase != null) {
				result = "Sucess...";
			} else {
				result = "Cache not available...";
			}
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; " + result);
			try {
				activity.complete();
				activity.removeActivityFromViewer();
			} catch (ComputationCanceled e) {
				// Nothing to do
			}
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; Error");
			AceLog.getAppLog().alertAndLogException(e);
		}
		return kbase;
	}

	/**
	 * Gets the knowledge base with agent.
	 * 
	 * @param referenceUuid
	 *            the reference uuid
	 * @param bytes
	 *            the bytes
	 * @return the knowledge base with agent
	 * @throws Exception
	 *             the exception
	 */
	private static KnowledgeBase getKnowledgeBaseWithAgent(UUID referenceUuid, byte[] bytes) throws Exception {
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Retrieving rules data from repository", true);
		activities.add(activity);
		activity.setValue(0);
		activity.setIndeterminate(true);
		long startTime = System.currentTimeMillis();
		KnowledgeBase kbase = null;
		try {
			File rulesDirectory = new File("rules");
			if (!rulesDirectory.exists()) {
				rulesDirectory.mkdir();
			}
			File serializedKbFile = new File(rulesDirectory, "knowledge_packages-" + referenceUuid.toString() + ".pkg");
			KnowledgeAgentConfiguration kaconf = KnowledgeAgentFactory.newKnowledgeAgentConfiguration();
			// kaconf.setProperty( "drools.resource.urlcache","rules" );
			kaconf.setProperty("drools.agent.newInstance", "false");
			KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("Agent", kaconf);
			SystemEventListener sysEvenListener = new ConsoleSystemEventListener();
			kagent.setSystemEventListener(sysEvenListener);
			kagent.applyChangeSet(ResourceFactory.newByteArrayResource(bytes));
			kbase = kagent.getKnowledgeBase();
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedKbFile));
				out.writeObject(kbase);
				out.close();
			} catch (FileNotFoundException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			String result = "";
			if (kbase != null && kbase.getKnowledgePackages().size() > 0) {
				result = "Sucess...";
			} else {
				result = "Repository not available...";
			}
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; " + result);
			try {
				activity.complete();
				activity.removeActivityFromViewer();
			} catch (ComputationCanceled e) {
				// Nothing to do
			}
		} catch (Exception e) {
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; Error");
			AceLog.getAppLog().alertAndLogException(e);
		}
		return kbase;
	}

	@Deprecated
	// now is validated in the rules deplyment package itself
	public static boolean validateDeploymentPackage(UUID referenceUuid, byte[] bytes) {
		KnowledgeBase kbase = null;
		try {
			kbase = getKnowledgeBase(referenceUuid, bytes, false);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return (kbase != null);
	}

	/**
	 * Checks if is package on line.
	 * 
	 * @param bytes
	 *            the bytes
	 * @return true, if is package on line
	 */
	public static boolean isPackageOnLine(byte[] bytes) {
		KnowledgeBase kbase = null;
		try {
			KnowledgeAgent kagent = KnowledgeAgentFactory.newKnowledgeAgent("Agent");
			kagent.applyChangeSet(ResourceFactory.newByteArrayResource(bytes));
			kbase = kagent.getKnowledgeBase();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return (kbase != null);
	}

	/**
	 * Gets the knowledge base.
	 * 
	 * @param kbId
	 *            the kb id
	 * @return the knowledge base
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId) throws Exception {
		return getKnowledgeBase(kbId, false, null);
	}

	/**
	 * Gets the knowledge base.
	 * 
	 * @param kbId
	 *            the kb id
	 * @param resources
	 *            the resources
	 * @return the knowledge base
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, HashMap<Resource, ResourceType> resources) throws Exception {
		return getKnowledgeBase(kbId, false, resources);
	}

	/**
	 * Gets the knowledge base.
	 * 
	 * @param kbId
	 *            the kb id
	 * @param recreate
	 *            the recreate
	 * @param resources
	 *            the resources
	 * @return the knowledge base
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static KnowledgeBase getKnowledgeBase(int kbId, boolean recreate, HashMap<Resource, ResourceType> resources) throws Exception {
		KnowledgeBase kbase = null;
		File serializedKbFile = new File("rules/knowledge_packages-" + kbId + ".pkg");
		if (serializedKbFile.exists() && !recreate) {
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
				// The input stream might contain an individual
				// package or a collection.
				@SuppressWarnings("unchecked")
				Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>) in.readObject();
				in.close();
				kbase = KnowledgeBaseFactory.newKnowledgeBase();
				kbase.addKnowledgePackages(kpkgs);
			} catch (StreamCorruptedException e0) {
				serializedKbFile.delete();
			} catch (FileNotFoundException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (ClassNotFoundException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		} else {
			if (resources == null || resources.isEmpty()) {
				//
				if (kbId == RulesLibrary.CONCEPT_MODEL_PKG) {
					resources = new HashMap<Resource, ResourceType>();
					resources.put(ResourceFactory.newFileResource("rules/sample-descriptions-rules.drl"), ResourceType.DRL);
				} else if (kbId == RulesLibrary.LINGUISTIC_GUIDELINES_PKG) {
					resources = new HashMap<Resource, ResourceType>();
					resources.put(ResourceFactory.newFileResource("rules/sample-guidelines-rules.drl"), ResourceType.DRL);
				} else {
					throw new Exception("No rules resources to process");
				}
			}
			kbase = KnowledgeBaseFactory.newKnowledgeBase();
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbase);
			for (Resource resource : resources.keySet()) {
				kbuilder.add(resource, resources.get(resource));
			}

			if (kbuilder.hasErrors()) {
				System.err.println(kbuilder.getErrors().toString());
			}
			kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedKbFile));
				out.writeObject(kbuilder.getKnowledgePackages());
				out.close();
			} catch (FileNotFoundException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}

		}
		// }
		return kbase;
	}

	/**
	 * Gets the snomed concept id.
	 * 
	 * @param concept
	 *            the concept
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return id;

	}

	/**
	 * Gets the concept.
	 * 
	 * @param snomedConceptId
	 *            the snomed concept id
	 * 
	 * @return the concept
	 */
	static public I_GetConceptData getConcept(String snomedConceptId) {
		I_TermFactory tf = Terms.get();
		I_GetConceptData concept = null;
		try {
			UUID elementUuid = Type3UuidFactory.fromSNOMED(snomedConceptId);
			concept = tf.getConcept(new UUID[] { elementUuid });
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return concept;
	}

	/**
	 * Gets the integer id for uuid.
	 * 
	 * @param uuid
	 *            the uuid
	 * 
	 * @return the integer id for uuid
	 */
	public static Integer getIntegerIdForUUID(UUID uuid) {
		I_TermFactory tf = Terms.get();
		I_GetConceptData concept = null;
		try {
			concept = tf.getConcept(new UUID[] { uuid });
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return concept.getConceptNid();
	}

	/**
	 * Gets the integer id for concept spec.
	 * 
	 * @param conceptSpec
	 *            the concept spec
	 * 
	 * @return the integer id for concept spec
	 */
	public static Integer getIntegerIdForConceptSpec(String conceptSpec) {
		UUID uuid = UUID.fromString(conceptSpec.substring(conceptSpec.indexOf("|") + 1, conceptSpec.indexOf(">")));
		// TODO: verify text integrity
		return getIntegerIdForUUID(uuid);
	}

	/**
	 * Gets the alert with text change fix up.
	 * 
	 * @param concept
	 *            the concept
	 * @param description
	 *            the description
	 * @param newText
	 *            the new text
	 * 
	 * @return the alert with text change fix up
	 */
	public static AlertToDataConstraintFailure getAlertWithTextChangeFixUp(I_GetConceptData concept, I_DescriptionTuple description, String newText) {
		AlertToDataConstraintFailure alert = new AlertToDataConstraintFailure(AlertToDataConstraintFailure.ALERT_TYPE.ERROR, description.getText(), concept);
		alert.getFixOptions().add(new ChangeDescriptionTextFixUp(concept, description.getMutablePart(), newText));
		return alert;
	}

	/**
	 * Gets the rel tuple details.
	 * 
	 * @param rel
	 *            the rel
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

			details = "Source: " + sourceConcept.toString() + " - Type: " + attributeConcept.toString() + " - Target: " + targetConcept.toString() + " - CharType: " + charType.toString() + " - Group: " + rel.getGroup();

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return details;
	}

	/**
	 * Update guvnor enumerations.
	 * 
	 * @param refset
	 *            the refset
	 * @param kPack
	 *            the k pack
	 * @param config
	 *            the config
	 */
	public static void updateGuvnorEnumerations(I_GetConceptData refset, RulesDeploymentPackageReference kPack, I_ConfigAceFrame config) {
		try {
			ConceptVersionBI refsetBI = Ts.get().getConceptVersion(config.getViewCoordinate(), refset.getUids());
			String propertyName = "";
			int guvnorDescriptionsSize = refsetBI.getDescriptionsActive(ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.localize().getNid()).size();
			if (guvnorDescriptionsSize < 1 || guvnorDescriptionsSize > 1) {
				throw new Exception("Wrong number of guvnor property descriptions: " + guvnorDescriptionsSize);
			}
			propertyName = refsetBI.getDescriptionsActive(ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.localize().getNid()).iterator().next().getText();

			String guvnorEnumerationText = "'" + propertyName + "' : [";
			for (I_ExtendByRef loopMember : Terms.get().getRefsetExtensionMembers(refset.getConceptNid())) {

				// I_ExtendByRefPartCid lastPart = (I_ExtendByRefPartCid)
				// loopMember.getTuples(config.getAllowedStatus(),
				// config.getViewPositionSetReadOnly(),
				// config.getPrecedence(),
				// config.getConflictResolutionStrategy()).iterator().next().getMutablePart();
				ConceptVersionBI loopConcept = Ts.get().getConceptVersion(config.getViewCoordinate(), loopMember.getComponentNid());

				String name = "no description found";

				for (DescriptionVersionBI loopDescription : loopConcept.getDescriptionsActive()) {
					if (loopDescription.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid() && loopDescription.getLang().toLowerCase().startsWith("en")) {
						name = loopDescription.getText();
					}
				}

				// if
				// (loopConcept.getDescriptionsActive(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()).size()
				// > 0) {
				// name =
				// loopConcept.getDescriptionsActive(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()).iterator().next().getText();
				// } else if (loopConcept.getDescriptionsActive().size() > 0){
				// name =
				// loopConcept.getDescriptionsActive().iterator().next().getText();
				// } else {
				// name = "no description found";
				// }

				guvnorEnumerationText = guvnorEnumerationText + "'" + loopConcept.getPrimUuid();
				guvnorEnumerationText = guvnorEnumerationText + "=" + name;
				guvnorEnumerationText = guvnorEnumerationText + "',";
			}
			guvnorEnumerationText = guvnorEnumerationText.substring(0, guvnorEnumerationText.length() - 1) + "]";
			AceLog.getAppLog().info(guvnorEnumerationText);

			Sardine sardine = SardineFactory.begin("username", "password");

			String pkgUrl = kPack.getUrl().substring(0, kPack.getUrl().indexOf(".Guvnor") + 7) + "/webdav/packages/";
			pkgUrl = pkgUrl + kPack.getUrl().substring(kPack.getUrl().indexOf("/package/") + 9, kPack.getUrl().indexOf("/", kPack.getUrl().indexOf("/package/") + 10));
			pkgUrl = pkgUrl + "/" + propertyName + ".enumeration";
			// "http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/qa4/qa4Demo"
			sardine.put(pkgUrl, guvnorEnumerationText.getBytes());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ContradictionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Checks if is included in refset spec.
	 * 
	 * @param refset
	 *            the refset
	 * @param candidateConcept
	 *            the candidate concept
	 * @param config
	 *            the config
	 * @return true, if is included in refset spec
	 * @throws Exception 
	 */
	public static boolean isIncludedInRefsetSpec(I_GetConceptData refset, I_GetConceptData candidateConcept, I_ConfigAceFrame config) throws Exception {
		boolean result = false;
		// AceLog.getAppLog().info("************ Starting test computation *****************");
		Long start = System.currentTimeMillis();
		RefsetSpec refsetSpecHelper = new RefsetSpec(refset, true, config);
		I_GetConceptData refsetSpec = refsetSpecHelper.getRefsetSpecConcept();
		// AceLog.getAppLog().info("Refset: " + refset.getInitialText() +
		// " " + refset.getUids().get(0));
		// AceLog.getAppLog().info("Checking Refset spec: " +
		// refsetSpec.getInitialText() + " " + refsetSpec.getUids().get(0));
		RefsetComputeType computeType = RefsetComputeType.CONCEPT; // default
		if (refsetSpecHelper.isDescriptionComputeType()) {
			computeType = RefsetComputeType.DESCRIPTION;
		} else if (refsetSpecHelper.isRelationshipComputeType()) {
			throw new Exception("Invalid refset spec to compute - relationship compute types not supported.(" + refset.toUserString() + ")");
		}

		// verify a valid refset spec construction
		if (refsetSpec == null) {
			throw new Exception("Invalid refset spec to compute - refset is null. (" + refset.toUserString() + ")");
		}
		// Step 1: create the query object, based on the refset spec
		RefsetSpecQuery query = RefsetQueryFactory.createQuery(config, Terms.get(), refsetSpec, refset, computeType);

		// check validity of query
		if (!query.isValidQuery() && query.getTotalStatementCount() != 0) {
			throw new Exception("Invalid refset spec to compute - Query not valid. (" + refset.toUserString() + ")");
		}

		I_GetConceptData selectedConcept = candidateConcept;

		// AceLog.getAppLog().info("Refset spec = " +
		// refsetSpec.toString());
		// AceLog.getAppLog().info("Refset = " + refset.toString());
		// AceLog.getAppLog().info("Concept to test = " +
		// selectedConcept.toString());

		List<I_ShowActivity> activities = new ArrayList<I_ShowActivity>();
		result = query.execute(selectedConcept.getNid(), selectedConcept, activities);

		// ArrayList<RefsetSpecComponent> components =
		// query.getAllComponents();
		//
		// boolean resultByComponents = false;
		// for (RefsetSpecComponent loopComponent : components) {
		// try {
		// ConceptStatement loopConceptStatement = (ConceptStatement)
		// loopComponent;
		// //loopConceptStatement..getTokenEnum().equals(loopConceptStatement.getTokenEnum().CONCEPT_IS);
		// } catch (Exception e) {
		// // skip, unknown component
		// }
		//
		// }

		// AceLog.getAppLog().info("++++++++++++++ Result = " + result);
		// AceLog.getAppLog().info("************ Finished test computation in "
		// + (System.currentTimeMillis() - start) +
		// " ms. *****************");

		return result;
	}

	/**
	 * Gets the documentation url for rule uuid.
	 * 
	 * @param ruleUuid
	 *            the rule uuid
	 * @return the documentation url for rule uuid
	 * @throws ConfigurationException
	 *             the configuration exception
	 */
	public static URL getDocumentationUrlForRuleUUID(UUID ruleUuid) throws ConfigurationException {
		URL url = null;

		XMLConfiguration config = new XMLConfiguration("rules/rules-documentation-config.xml");
		String template = config.getString("urlTemplate");
		AceLog.getAppLog().info("Template: " + template);
		Object rules = config.getProperty("rules.rule.uuid");
		if (rules instanceof Collection) {
			AceLog.getAppLog().info("Number of rules: " + ((Collection) rules).size());
			for (int i = 0; i <= ((Collection) rules).size() - 1; i++) {
				AceLog.getAppLog().info(i + "- UUID: " + config.getString("rules.rule(" + i + ").uuid"));
				AceLog.getAppLog().info(i + "- Address: " + config.getString("rules.rule(" + i + ").address"));

				if (ruleUuid.equals(UUID.fromString(config.getString("rules.rule(" + i + ").uuid")))) {
					String urlString = template.replace("*", config.getString("rules.rule(" + i + ").address"));
					try {
						url = new URL(urlString);
					} catch (MalformedURLException e) {
						// do nothing, url = null
					}
				}
			}
		}
		return url;
	}

	/**
	 * Gets the historical rels.
	 * 
	 * @return the historical rels
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             signals that an I/O exception has occurred.
	 */
	public static I_IntSet getHistoricalRels() throws TerminologyException, IOException {

		if (histRels == null) {
			histRels = Terms.get().newIntSet();
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
			ConceptSpec spec = new ConceptSpec("Concept history attribute (attribute)", UUID.fromString("f323b5dd-1f97-3873-bcbc-3563663dda14"));
			descendants = getDescendants(descendants, Terms.get().getConcept(spec.getLenient().getNid()));
			for (I_GetConceptData loopConcept : descendants) {
				histRels.add(loopConcept.getNid());
			}
		}
		return histRels;
	}

	/**
	 * Gets the concept model rels.
	 * 
	 * @return the concept model rels
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             signals that an I/O exception has occurred.
	 */
	public static I_IntSet getConceptModelRels() throws TerminologyException, IOException {

		if (CptModelRels == null) {
			CptModelRels = Terms.get().newIntSet();
			CptModelRels = Terms.get().newIntSet();
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
			ConceptSpec spec = new ConceptSpec("Concept model attribute (attribute)", UUID.fromString("6155818b-09ed-388e-82ce-caa143423e99"));
			descendants = getDescendants(descendants, Terms.get().getConcept(spec.getLenient().getNid()));
			for (I_GetConceptData loopConcept : descendants) {
				CptModelRels.add(loopConcept.getNid());
			}
		}
		return CptModelRels;
	}

	/**
	 * Gets the all rels.
	 * 
	 * @return the all rels
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             signals that an I/O exception has occurred.
	 */
	public static I_IntSet getAllRels() throws TerminologyException, IOException {

		if (allRels == null) {
			allRels = Terms.get().newIntSet();
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			allRels.addAll(config.getDestRelTypes().getSetValues());
			allRels.addAll(getConceptModelRels().getSetValues());
			allRels.addAll(getHistoricalRels().getSetValues());
		}
		return allRels;
	}

	/**
	 * Gets the descendants.
	 * 
	 * @param descendants
	 *            the descendants
	 * @param concept
	 *            the concept
	 * @return the descendants
	 */
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			Set<I_GetConceptData> firstMetChildrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
			for (I_GetConceptData loopConcept : childrenSet) {
				if (!descendants.contains(loopConcept)) {
					firstMetChildrenSet.add(loopConcept);
				}
			}
			descendants.addAll(firstMetChildrenSet);
			for (I_GetConceptData loopConcept : firstMetChildrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return descendants;
	}

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of
	 * the User concept in the top hierarchy, and have a description of type
	 * "user inbox".
	 * 
	 * @return The set of valid users.
	 */
	public static Set<I_GetConceptData> getUsers() {
		HashSet<I_GetConceptData> validUsers = new HashSet<I_GetConceptData>();
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
		} catch (TerminologyException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		} catch (IOException e1) {
			AceLog.getAppLog().alertAndLogException(e1);
		}
		try {
			I_GetConceptData userParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER.getUids());
			I_GetConceptData ihtsdoUserParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getUids());
			I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();
			I_HelpSpecRefset helper = Terms.get().getSpecRefsetHelper(Terms.get().getActiveAceFrameConfig());
			Set<Integer> currentStatuses = helper.getCurrentStatusIds();
			Set<? extends I_GetConceptData> allUsers = userParent.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			Set<? extends I_GetConceptData> allIhtsdoUsrs = ihtsdoUserParent.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			I_GetConceptData descriptionType = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.USER_INBOX.getUids());
			I_IntSet descAllowedTypes = Terms.get().newIntSet();
			descAllowedTypes.add(descriptionType.getConceptNid());

			for (I_GetConceptData user : allUsers) {
				I_DescriptionVersioned latestTuple = null;
				Collection<? extends I_DescriptionVersioned> descriptions = user.getDescs();
				for (I_DescriptionVersioned descVersioned : descriptions) {
					if (descVersioned.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid()) {
						latestTuple = descVersioned;
					}
				}
				if (latestTuple != null) {
					for (int currentStatusId : currentStatuses) {
						if (latestTuple.getStatusNid() == currentStatusId) {
							validUsers.add(user);
						}
					}
				}
			}
			for (I_GetConceptData user : allIhtsdoUsrs) {
				I_DescriptionVersioned latestTuple = null;
				Collection<? extends I_DescriptionVersioned> descriptions = user.getDescs();
				for (I_DescriptionVersioned descVersioned : descriptions) {
					if (descVersioned.getTypeNid() == SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid() || descVersioned.getTypeNid() == ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()) {
						latestTuple = descVersioned;
					}
				}
				if (latestTuple != null) {
					for (int currentStatusId : currentStatuses) {
						if (latestTuple.getStatusNid() == currentStatusId) {
							validUsers.add(user);
						}
					}
				}
			}

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return validUsers;
	}
}
