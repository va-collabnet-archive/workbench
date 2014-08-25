/*
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
package org.ihtsdo.rules.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.swing.JOptionPane;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class RulesContextHelper.
 */
public class RulesContextHelper {

	/** The config. */
	I_ConfigAceFrame config;

	/** The kb cache. */
	HashMap<Integer, KnowledgeBase> kbCache;

	/** The last cache update time. */
	long lastCacheUpdateTime = 0;

	/** The no rules alert shown. */
	static boolean noRulesAlertShown = false;

	/**
	 * Instantiates a new rules context helper.
	 * 
	 * @param config
	 *            the config
	 */
	public RulesContextHelper(I_ConfigAceFrame config) {
		this.config = config;
		if (Terms.get().getKnowledgeBaseCache() != null) {
			this.kbCache = Terms.get().getKnowledgeBaseCache();
		} else {
			this.kbCache = new HashMap<Integer, KnowledgeBase>();
		}
	}

	// public void updateKbCacheFromFiles() {
	// this.kbCache = new HashMap<Integer, KnowledgeBase>();
	// File dir = new File("rules");
	// for (File loopFile : dir.listFiles()) {
	// if (loopFile.getName().endsWith(".bkb")) {
	// try {
	// ObjectInputStream in = new ObjectInputStream(new
	// FileInputStream(loopFile));
	// // The input stream might contain an individual
	// // package or a collection.
	// @SuppressWarnings( "unchecked" )
	// //Collection<KnowledgePackage> kpkgs =
	// (Collection<KnowledgePackage>)in.readObject();
	// KnowledgeBase kbase = (KnowledgeBase) in.readObject();
	// in.close();
	// //KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
	// //kbase.addKnowledgePackages(kpkgs);
	//
	// Integer contextId = Integer.valueOf(
	// loopFile.getName().substring(0, loopFile.getName().indexOf(".")));
	// kbCache.put(contextId, kbase);
	//
	// } catch (FileNotFoundException e) {
	// AceLog.getAppLog().alertAndLogException(e);
	// } catch (IOException e) {
	// AceLog.getAppLog().alertAndLogException(e);
	// } catch (ClassNotFoundException e) {
	// AceLog.getAppLog().alertAndLogException(e);
	// }
	// }
	// }
	// }

	/**
	 * Gets the knowledge base for context.
	 * 
	 * @param context
	 *            the context
	 * @param config
	 *            the config
	 * @return the knowledge base for context
	 * @throws Exception
	 *             the exception
	 */
	public KnowledgeBase getKnowledgeBaseForContext(I_GetConceptData context, I_ConfigAceFrame config) throws Exception {
		return getKnowledgeBaseForContext(context, config, false);
	}

	/**
	 * Gets the knowledge base for context.
	 * 
	 * @param context
	 *            the context
	 * @param config
	 *            the config
	 * @param recreate
	 *            the recreate
	 * @return the knowledge base for context
	 * @throws Exception
	 *             the exception
	 */
	public KnowledgeBase getKnowledgeBaseForContext(I_GetConceptData context, I_ConfigAceFrame config, boolean recreate) throws Exception {
		KnowledgeBase returnBase = null;
		HashSet<I_ShowActivity> activities = new HashSet<I_ShowActivity>();
		I_ShowActivity activity = Terms.get().newActivityPanel(true, config, "<html>Generating KnowledgeBase for context...", true);
		long startTime = System.currentTimeMillis();
		try {
			activities.add(activity);
			activity.setValue(0);
			activity.setIndeterminate(true);
			activity.setProgressInfoLower("Generating KnowledgeBase for context...");
			File serializedKbFile = new File("rules/" + context.getConceptNid() + ".bkb");
			if (kbCache.containsKey(context.getConceptNid()) && !recreate) {
				returnBase = kbCache.get(context.getConceptNid());
			}

			if (returnBase == null && serializedKbFile.exists() && !recreate) {
				KnowledgeBase kbase = null;
				try {
					ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
					kbase = (KnowledgeBase) in.readObject();
					in.close();
					kbCache.put(context.getConceptNid(), kbase);
					Terms.get().setKnowledgeBaseCache(kbCache);
				} catch (StreamCorruptedException e0) {
					serializedKbFile.delete();
				} catch (FileNotFoundException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (Exception e) {
					serializedKbFile.delete();
					// AceLog.getAppLog().alertAndLogException(e);
				}
				returnBase = kbase;
			}

			if (returnBase == null) {
				// RulesDeploymentPackageReferenceHelper rulesPackageHelper =
				// new RulesDeploymentPackageReferenceHelper(config);

				KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

				KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
				File flow = new File("rules/qa-execution3.bpmn");
				if (flow.exists()) {
					kbuilder.add(ResourceFactory.newFileResource("rules/qa-execution3.bpmn"), ResourceType.BPMN2);
					kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

					for (RulesDeploymentPackageReference deploymentPackage : getPackagesForContext(context)) {
						try {
							KnowledgeBase loopKBase = deploymentPackage.getKnowledgeBase(recreate);
							if (loopKBase != null) {
								loopKBase = filterForContext(loopKBase, context, config);
								kbase.addKnowledgePackages(loopKBase.getKnowledgePackages());
							}
						} catch (Exception e) {
							// ignoring exception during rules regeneration,
							// errors will be logged
							// and context build will continue
						}
					}

					if (kbase.getKnowledgePackages().size() == 0 && !noRulesAlertShown && RefsetAuxiliary.Concept.REALTIME_QA_CONTEXT.getUids().containsAll(context.getUids())) {
						noRulesAlertShown = true;
						JOptionPane.showMessageDialog(null, "Rules base is empty, you might need to update from Guvnor.", "Rules base empty", JOptionPane.WARNING_MESSAGE);
					}

					try {
						ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(serializedKbFile));
						out.writeObject(kbase);
						out.writeObject(kbase.getKnowledgePackages());
						out.close();
					} catch (FileNotFoundException e) {
						AceLog.getAppLog().alertAndLogException(e);
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}

					kbCache.put(context.getConceptNid(), kbase);
					Terms.get().setKnowledgeBaseCache(kbCache);
					lastCacheUpdateTime = Calendar.getInstance().getTimeInMillis();
				} else {
					AceLog.getAppLog().info("ERROR: Required flow file is missing in rules folder: " + flow.getName());
				}
				returnBase = kbase;
			}

			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			String result = "Done";
			activity.setProgressInfoLower("Elapsed: " + elapsedStr + "; " + result);
			try {
				activity.complete();
				activity.removeActivityFromViewer();
			} catch (ComputationCanceled e) {
				// Nothing to do
			}
		} catch (Exception e1) {
			long endTime = System.currentTimeMillis();
			long elapsed = endTime - startTime;
			String elapsedStr = TimeHelper.getElapsedTimeString(elapsed);
			String result = "Done";
			activity.setProgressInfoLower("Error - Elapsed: " + elapsedStr + "; " + result);
			try {
				activity.complete();
				activity.removeActivityFromViewer();
			} catch (ComputationCanceled e) {
				// Nothing to do
			}
			AceLog.getAppLog().alertAndLogException(e1);
		}
		return returnBase;
	}

	/**
	 * Filter for context.
	 * 
	 * @param kbase
	 *            the kbase
	 * @param context
	 *            the context
	 * @param config
	 *            the config
	 * @return the knowledge base
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             signals that an I/O exception has occurred.
	 */
	public KnowledgeBase filterForContext(KnowledgeBase kbase, I_GetConceptData context, I_ConfigAceFrame config) throws TerminologyException, IOException {
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		I_GetConceptData excludeClause = Terms.get().getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			for (Rule rule : kpackg.getRules()) {
				boolean excluded = false;
				String ruleUid = (String) rule.getMetaData().get("UUID");
				// String ruleUid = (String) rule.getMetaAttribute("UID");
				if (ruleUid != null) {
					I_GetConceptData role = contextHelper.getRoleInContext(ruleUid, context);
					if (role != null && role.getConceptNid() == excludeClause.getConceptNid()) {
						excluded = true;
					}
				}
				if (excluded) {
					kbase.removeRule(kpackg.getName(), rule.getName());
				}
			}
		}
		return kbase;
	}

	/**
	 * Creates the context.
	 * 
	 * @param name
	 *            the name
	 * @return the i_ get concept data
	 */
	public I_GetConceptData createContext(String name) {
		try {
			I_TermFactory termFactory = Terms.get();

			I_GetConceptData newConcept = null;

			I_GetConceptData contextRoot = termFactory.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT.getUids());

			I_GetConceptData fsnType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			I_GetConceptData preferredType = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name + " (rules context)", fsnType, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name, preferredType, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), contextRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			termFactory.addUncommittedNoChecks(newConcept);

			newConcept.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(), config.getDbConfig().getChangeSetWriterThreading().convert());

			promote(newConcept);

			return newConcept;

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return null;
	}

	/**
	 * Gets the all contexts.
	 * 
	 * @return the all contexts
	 * @throws Exception
	 *             the exception
	 */
	public List<I_GetConceptData> getAllContexts() throws Exception {
		List<I_GetConceptData> contexts = new ArrayList<I_GetConceptData>();
		I_TermFactory tf = Terms.get();
		try {
			I_GetConceptData contextsParent = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT.getUids());
			contexts.addAll(contextsParent.getDestRelOrigins(config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return contexts;

	}

	/**
	 * Sets the role in context.
	 * 
	 * @param ruleUid
	 *            the rule uid
	 * @param context
	 *            the context
	 * @param newRole
	 *            the new role
	 */
	public void setRoleInContext(String ruleUid, I_GetConceptData context, I_GetConceptData newRole) {
		try {
			I_GetConceptData currentRole = getRoleInContext(ruleUid, context);
			I_ExtendByRefPartCidString currentRolePart = getLastStringPartForRule(ruleUid, context);
			I_TermFactory tf = Terms.get();
			I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			I_GetConceptData contextRefset = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
//			refsetHelper.setAutocommitActive(true);

			if (currentRolePart == null && newRole != null) {
				// new member in context refset
                                RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                                helper.newConceptStringRefsetExtension(contextRefset.getConceptNid(), context.getConceptNid(),
                                        newRole.getConceptNid(), ruleUid);
				tf.addUncommittedNoChecks(contextRefset);
				tf.addUncommittedNoChecks(context);
				contextRefset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.MULTI_THREAD);
				context.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.MULTI_THREAD);
			} else if (currentRolePart != null && newRole != null) {
				if (currentRolePart.getC1id() != newRole.getConceptNid() || currentRolePart.getStatusNid() != currentStatus.getConceptNid()) {
					// update existing role
					for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
						if (extension.getComponentNid() == context.getConceptNid()) {
							List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
							for (I_ExtendByRefPart part : extension.getMutableParts()) {
								I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
								if (strPart.getString1Value().equals(ruleUid)) {
									ruleParts.add(strPart);
								}
							}
							if (!ruleParts.isEmpty()) {
								I_ExtendByRefPartCidString lastPart = ruleParts.iterator().next();
								for (I_ExtendByRefPartCidString loopPart : ruleParts) {
									if (loopPart.getTime() >= lastPart.getTime()) {
										lastPart = loopPart;
									}
								}
								for (PathBI editPath : config.getEditingPathSet()) {
									I_ExtendByRefPartCidString newPart = (I_ExtendByRefPartCidString) lastPart.makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), Long.MAX_VALUE, config.getEditCoordinate().getAuthorNid(), config.getEditCoordinate().getModuleNid(),
											editPath.getConceptNid());
									newPart.setC1id(newRole.getConceptNid());
									extension.addVersion(newPart);
									tf.addUncommittedNoChecks(extension);
								}
								tf.addUncommittedNoChecks(contextRefset);
								contextRefset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.MULTI_THREAD);
							}
						}
					}
				}
			} else if (currentRole != null && newRole == null) {
				// retire latest version of role
				for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
					if (extension.getComponentNid() == context.getConceptNid()) {
						List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
						for (I_ExtendByRefPart part : extension.getMutableParts()) {
							I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
							if (strPart.getString1Value().equals(ruleUid)) {
								ruleParts.add(strPart);
							}
						}
						if (!ruleParts.isEmpty()) {
							I_ExtendByRefPartCidString lastPart = ruleParts.iterator().next();
							for (I_ExtendByRefPartCidString loopPart : ruleParts) {
								if (loopPart.getTime() >= lastPart.getTime()) {
									lastPart = loopPart;
								}
							}
							for (PathBI editPath : config.getEditingPathSet()) {
								I_ExtendByRefPartCidString newPart = (I_ExtendByRefPartCidString) lastPart.makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), Long.MAX_VALUE, config.getEditCoordinate().getAuthorNid(), config.getEditCoordinate().getModuleNid(),
										editPath.getConceptNid());
								extension.addVersion(newPart);
								tf.addUncommittedNoChecks(extension);
							}
							tf.addUncommittedNoChecks(contextRefset);
							tf.commit();
							promote(context);
						}
					}
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the last string part for rule.
	 * 
	 * @param ruleUid
	 *            the rule uid
	 * @param context
	 *            the context
	 * @return the last string part for rule
	 */
	public I_ExtendByRefPartCidString getLastStringPartForRule(String ruleUid, I_GetConceptData context) {
		try {
			I_ExtendByRefPartCidString lastPart = null;
			I_TermFactory tf = Terms.get();
			I_GetConceptData agendaMetadataRefset = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
			// I_GetConceptData currentStatus =
			// tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			// I_GetConceptData includeClause =
			// tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
			// I_GetConceptData excludeClause =
			// tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
			// I_GetConceptData role = null;
			for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(agendaMetadataRefset.getConceptNid())) {
				if (extension.getComponentNid() == context.getConceptNid()) {
					List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
					for (I_ExtendByRefPart part : extension.getMutableParts()) {
						I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
						if (strPart.getString1Value().equals(ruleUid)) {
							ruleParts.add(strPart);
						}
					}
					if (!ruleParts.isEmpty()) {
						lastPart = ruleParts.iterator().next();
						for (I_ExtendByRefPartCidString loopPart : ruleParts) {
							if (loopPart.getTime() >= lastPart.getTime()) {
								lastPart = loopPart;
							}
						}
					}
				}
			}
			return lastPart;
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	/**
	 * Gets the role in context.
	 * 
	 * @param ruleUid
	 *            the rule uid
	 * @param context
	 *            the context
	 * @return the role in context
	 */
	public I_GetConceptData getRoleInContext(String ruleUid, I_GetConceptData context) {
		try {
			I_TermFactory tf = Terms.get();
			I_GetConceptData role = null;
			I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			I_GetConceptData activeStatus = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids());
			I_ExtendByRefPartCidString lastPart = getLastStringPartForRule(ruleUid, context);
			if (lastPart != null) {
				if (lastPart.getStatusNid() == currentStatus.getConceptNid() || lastPart.getStatusNid() == activeStatus.getConceptNid()) {
					role = tf.getConcept(lastPart.getC1id());
				}
			}
			return role;
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	/**
	 * Update preferred term.
	 * 
	 * @param concept
	 *            the concept
	 * @param newString
	 *            the new string
	 * @param config
	 *            the config
	 */
	public void updatePreferredTerm(I_GetConceptData concept, String newString, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			// I_IntSet localDescTypes = null;
			// if (config.getDescTypes().getSetValues().length > 0) {
			// localDescTypes = config.getDescTypes();
			// }
			int preferred = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_IntSet types = termFactory.newIntSet();
			types.add(preferred);

			List<? extends I_DescriptionTuple> descTuples = concept.getDescriptionTuples(config.getAllowedStatus(), types, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

			for (I_DescriptionTuple tuple : descTuples) {

				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					I_DescriptionVersioned description = tuple.getDescVersioned();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) tuple.getMutablePart().makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), Long.MAX_VALUE, config.getEditCoordinate().getAuthorNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newPart.setText(newString);
						description.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(concept);
					termFactory.commit();
					promote(concept);
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the last extension part.
	 * 
	 * @param extension
	 *            the extension
	 * @return the last extension part
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             signals that an I/O exception has occurred.
	 */
	public I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = config.getAllowedStatus();
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(allowedStatus, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {
			if (loopTuple.getTime() > lastVersion) {
				lastVersion = loopTuple.getTime();
				lastPart = loopTuple.getMutablePart();
			}
		}

		if (lastPart == null) {
			throw new TerminologyException("No parts on this viewpositionset.");
		}

		return lastPart;
	}

	/**
	 * Gets the config.
	 * 
	 * @return the config
	 */
	public I_ConfigAceFrame getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the new config
	 */
	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	/**
	 * Gets the kb cache.
	 * 
	 * @return the kb cache
	 */
	public HashMap<Integer, KnowledgeBase> getKbCache() {
		return kbCache;
	}

	/**
	 * Sets the kb cache.
	 * 
	 * @param kbCache
	 *            the kb cache
	 */
	public void setKbCache(HashMap<Integer, KnowledgeBase> kbCache) {
		this.kbCache = kbCache;
		Terms.get().setKnowledgeBaseCache(kbCache);
	}

	/**
	 * Clear cache.
	 */
	public void clearCache() {
		File dir = new File("rules");
		if (!dir.exists()) {
			dir.mkdir();
		}
		for (File loopFile : dir.listFiles()) {
			if (loopFile.getName().endsWith(".bkb") || loopFile.getName().endsWith(".pkg")) {
				loopFile.delete();
			}
		}
		kbCache = new HashMap<Integer, KnowledgeBase>();
		Terms.get().setKnowledgeBaseCache(kbCache);
		// updateKbCacheFromFiles();
	}

	/**
	 * Gets the packages for context.
	 * 
	 * @param context
	 *            the context
	 * @return the packages for context
	 */
	public List<RulesDeploymentPackageReference> getPackagesForContext(I_GetConceptData context) {
		List<? extends I_RelTuple> pkgRelTuples = null;
		RulesDeploymentPackageReferenceHelper refHelper = new RulesDeploymentPackageReferenceHelper(config);
		ArrayList<RulesDeploymentPackageReference> returnData = new ArrayList<RulesDeploymentPackageReference>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			pkgRelTuples = context.getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			pkgRelTuples = cleanRelTuplesList(pkgRelTuples);

			if (pkgRelTuples != null) {
				for (I_RelTuple loopTuple : pkgRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						ConceptChronicleBI targetPackage = termFactory.getConcept(loopTuple.getC2Id());
						Long time = Long.MIN_VALUE;
						int statusId = Integer.MIN_VALUE;
						for (ConceptAttributeVersionBI loopAttr : targetPackage.getConceptAttributes().getVersions()) {
							if (loopAttr.getTime() > time) {
								time = loopAttr.getTime();
								statusId = loopAttr.getStatusNid();
							}
						}
						if (isActive(statusId) && !targetPackage.getPrimUuid().equals(UUID.fromString("00000000-0000-0000-C000-000000000046"))) {
							returnData.add(refHelper.getRulesDeploymentPackageReference(targetPackage));
						}
					}
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return returnData;
	}

	/**
	 * Gets the contexts for package.
	 * 
	 * @param refPkg
	 *            the ref pkg
	 * @return the contexts for package
	 */
	public List<I_GetConceptData> getContextsForPackage(RulesDeploymentPackageReference refPkg) {
		List<? extends I_RelTuple> pkgRelTuples = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			I_GetConceptData pkgConcept = termFactory.getConcept(refPkg.getUuids());
			pkgRelTuples = pkgConcept.getDestRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			pkgRelTuples = cleanRelTuplesList(pkgRelTuples);

			if (pkgRelTuples != null) {
				for (I_RelTuple loopTuple : pkgRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC1Id()));
					}
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return returnData;
	}

	/**
	 * Adds the pkg reference to context.
	 * 
	 * @param pkgReference
	 *            the pkg reference
	 * @param context
	 *            the context
	 */
	public void addPkgReferenceToContext(RulesDeploymentPackageReference pkgReference, I_GetConceptData context) {
		I_TermFactory termFactory = Terms.get();

		List<RulesDeploymentPackageReference> currentPkgs = getPackagesForContext(context);
		Boolean alreadyThere = false;
		for (RulesDeploymentPackageReference loopPkg : currentPkgs) {
			if (loopPkg.getUuids().containsAll(pkgReference.getUuids())) {
				// AceLog.getAppLog().info("Setting to true...");
				alreadyThere = true;
			}
		}

		if (!alreadyThere) {
			try {
				boolean retiredAndReactivated = false;
				I_IntSet allowedStatus = termFactory.newIntSet();
				allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
				I_GetConceptData pkgConcept = termFactory.getConcept(pkgReference.getUuids());
				List<? extends I_RelTuple> pkgRelTuples = pkgConcept.getDestRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
				pkgRelTuples = cleanRelTuplesList(pkgRelTuples);
				if (pkgRelTuples != null) {
					for (I_RelTuple loopTuple : pkgRelTuples) {
						if (loopTuple.getC1Id() == context.getConceptNid() && loopTuple.getC2Id() == pkgConcept.getConceptNid()) {
							I_RelPart newPart = (I_RelPart) loopTuple.getMutablePart().makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), Long.MAX_VALUE, config.getEditCoordinate().getAuthorNid(), config.getEditCoordinate().getModuleNid(),
									config.getEditingPathSetReadOnly().iterator().next().getConceptNid());
							loopTuple.getFixedPart().addVersion(newPart);
							retiredAndReactivated = true;
							termFactory.addUncommittedNoChecks(context);
							termFactory.commit();
							promote(context);
						}
					}
				}
				if (!retiredAndReactivated) {
					// I_RelVersioned relVersioned =
					termFactory.newRelationship(UUID.randomUUID(), context, termFactory.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids()), termFactory.getConcept(pkgReference.getUuids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
							termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);
					termFactory.addUncommittedNoChecks(context);
					context.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(), config.getDbConfig().getChangeSetWriterThreading().convert());
					promote(context);
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}

	}

	/**
	 * Removes the pkg reference from context.
	 * 
	 * @param pkgReference
	 *            the pkg reference
	 * @param context
	 *            the context
	 */
	public void removePkgReferenceFromContext(RulesDeploymentPackageReference pkgReference, I_GetConceptData context) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> pkgRels = null;
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			pkgRels = context.getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			I_GetConceptData pkgConcept = termFactory.getConcept(pkgReference.getUuids());
			for (I_RelTuple rel : pkgRels) {
				if (rel.getC1Id() == context.getConceptNid() && rel.getC2Id() == pkgConcept.getConceptNid() && rel.getTypeNid() == RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), Long.MAX_VALUE, config.getEditCoordinate().getAuthorNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(context);
					context.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(), config.getDbConfig().getChangeSetWriterThreading().convert());
					promote(context);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Clean rel tuples list.
	 * 
	 * @param tuples
	 *            the tuples
	 * @return the list<? extends i_ rel tuple>
	 */
	private List<? extends I_RelTuple> cleanRelTuplesList(List<? extends I_RelTuple> tuples) {
		HashMap<Integer, I_RelTuple> cleanMap = new HashMap<Integer, I_RelTuple>();
		for (I_RelTuple loopTuple : tuples) {
			if (cleanMap.get(loopTuple.getRelId()) == null) {
				cleanMap.put(loopTuple.getRelId(), loopTuple);
			} else if (cleanMap.get(loopTuple.getRelId()).getTime() < loopTuple.getTime()) {
				cleanMap.put(loopTuple.getRelId(), loopTuple);
			}
		}
		List<I_RelTuple> cleanList = new ArrayList<I_RelTuple>();
		cleanList.addAll(cleanMap.values());
		return cleanList;
	}

	/**
	 * Checks if is active.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is active
	 */
	public boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		try {
			activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return (activeStatuses.contains(statusId));
	}

	/**
	 * Promote.
	 * 
	 * @param concept
	 *            the concept
	 */
	public void promote(I_GetConceptData concept) {
		try {
			I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
			allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			allowedStatusWithRetired.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());

			I_TermFactory termFactory = Terms.get();

			concept.promote(config.getViewPositionSet().iterator().next(), config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME, config.getEditCoordinate().getAuthorNid());
			termFactory.addUncommittedNoChecks(concept);

			for (I_ExtendByRef loopExtension : termFactory.getAllExtensionsForComponent(concept.getConceptNid())) {
				loopExtension.promote(config.getViewPositionSet().iterator().next(), config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME, config.getEditCoordinate().getAuthorNid());
				termFactory.addUncommittedNoChecks(loopExtension);
			}

			termFactory.commit();
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

}
