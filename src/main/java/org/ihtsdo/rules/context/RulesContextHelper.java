package org.ihtsdo.rules.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;

public class RulesContextHelper {

	I_ConfigAceFrame config;
	HashMap<Integer, KnowledgeBase> kbCache;
	long lastCacheUpdateTime = 0;

	public RulesContextHelper(I_ConfigAceFrame config) {
		this.config = config;
		this.kbCache = new HashMap<Integer, KnowledgeBase>();
	}

	//	public void updateKbCacheFromFiles() {
	//		this.kbCache = new HashMap<Integer, KnowledgeBase>();
	//		File dir = new File("rules");
	//		for (File loopFile : dir.listFiles()) {
	//			if (loopFile.getName().endsWith(".bkb")) {
	//				try {
	//					ObjectInputStream in = new ObjectInputStream(new FileInputStream(loopFile));
	//					// The input stream might contain an individual
	//					// package or a collection.
	//					@SuppressWarnings( "unchecked" )
	//					//Collection<KnowledgePackage> kpkgs = (Collection<KnowledgePackage>)in.readObject();
	//					KnowledgeBase kbase = (KnowledgeBase) in.readObject();
	//					in.close();
	//					//KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
	//					//kbase.addKnowledgePackages(kpkgs);
	//
	//					Integer contextId = Integer.valueOf(
	//							loopFile.getName().substring(0, loopFile.getName().indexOf(".")));
	//					kbCache.put(contextId, kbase);
	//
	//				} catch (FileNotFoundException e) {
	//					e.printStackTrace();
	//				} catch (IOException e) {
	//					e.printStackTrace();
	//				} catch (ClassNotFoundException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//	}

	public KnowledgeBase getKnowledgeBaseForContext(I_GetConceptData context, I_ConfigAceFrame config) throws Exception {
		File serializedKbFile = new File("rules/" + context.getConceptNid() + ".bkb");
		if (kbCache.containsKey(context.getConceptNid())) {
			return kbCache.get(context.getConceptNid());
		} else if (serializedKbFile.exists()){
			KnowledgeBase kbase = null;
			try {
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(serializedKbFile));
				kbase = (KnowledgeBase) in.readObject();
				in.close();
				kbCache.put(context.getConceptNid(), kbase);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			return kbase;
		} else {
			//RulesDeploymentPackageReferenceHelper rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);

			KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

			// **Flow test start**
			KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			File flow = new File("rules/qa-execution2.rf");
			if (flow.exists()) {
				kbuilder.add(ResourceFactory.newClassPathResource("rules/qa-execution2.rf"), ResourceType.DRF);
				kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
				// **Flow test end**

				for (RulesDeploymentPackageReference deploymentPackage : getPackagesForContext(context)) {
					if (deploymentPackage.validate()) {
						KnowledgeBase loopKBase = deploymentPackage.getKnowledgeBase(false);
						loopKBase = filterForContext(loopKBase, context, config);
						kbase.addKnowledgePackages(loopKBase.getKnowledgePackages());
					}
				}
				try {
					ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( serializedKbFile ) );
					out.writeObject( kbase );
					//out.writeObject( kbase.getKnowledgePackages() );
					out.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				kbCache.put(context.getConceptNid(), kbase);
				lastCacheUpdateTime = Calendar.getInstance().getTimeInMillis();
			}

			return kbase;
		}
	}

	public KnowledgeBase filterForContext(KnowledgeBase kbase, I_GetConceptData context, I_ConfigAceFrame config) throws TerminologyException, IOException {
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		I_GetConceptData excludeClause = Terms.get().getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
		for (KnowledgePackage kpackg : kbase.getKnowledgePackages()) {
			for (Rule rule : kpackg.getRules()) {
				boolean excluded = false;
				String ruleUid = (String) rule.getMetaData().get("UUID");
				//String ruleUid = (String) rule.getMetaAttribute("UID");
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

	public I_GetConceptData createContext(String name) {
		try {
			I_TermFactory termFactory = Terms.get();

			I_GetConceptData newConcept = null;

			I_GetConceptData contextRoot = termFactory.getConcept(
					RefsetAuxiliary.Concept.RULES_CONTEXT.getUids());

			I_GetConceptData fsnType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			I_GetConceptData preferredType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name + " (rules context)",
					fsnType, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name,
					preferredType, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					contextRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.addUncommittedNoChecks(newConcept);

			termFactory.commit();
			
			promote(newConcept);

			return newConcept;

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<I_GetConceptData> getAllContexts() throws Exception {
		List<I_GetConceptData> contexts = new ArrayList<I_GetConceptData>();
		I_TermFactory tf = Terms.get();
		try {
			I_GetConceptData contextsParent = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT.getUids());
			contexts.addAll(contextsParent.getDestRelOrigins(config.getAllowedStatus(), 
					config.getDestRelTypes(), config.getViewPositionSetReadOnly(), 
					config.getPrecedence(), config.getConflictResolutionStrategy()));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contexts;

	}

	public void setRoleInContext(String ruleUid, I_GetConceptData context, I_GetConceptData newRole) {
		try {
			I_GetConceptData currentRole = getRoleInContext(ruleUid, context);
			I_ExtendByRefPartCidString currentRolePart = getLastStringPartForRule(ruleUid, context);
			I_TermFactory tf = Terms.get();
			I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			I_GetConceptData contextRefset = tf.getConcept(
					RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
			I_HelpRefsets refsetHelper = tf.getRefsetHelper(config);
			refsetHelper.setAutocommitActive(true);

			if (currentRolePart == null && newRole != null) {
				//new member in context refset
				RefsetPropertyMap propertyMap = new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ruleUid);
				propertyMap.put(REFSET_PROPERTY.CID_ONE, newRole.getConceptNid());
				refsetHelper.newRefsetExtension(contextRefset.getConceptNid(), context.getConceptNid(), 
						REFSET_TYPES.CID_STR, propertyMap, config);
				tf.addUncommittedNoChecks(contextRefset);
				tf.addUncommittedNoChecks(context);
				tf.commit();
				promote(context);
			} else if (currentRolePart != null && newRole != null){
				if (currentRolePart.getC1id() != newRole.getConceptNid() || currentRolePart.getStatusNid() != currentStatus.getConceptNid()) {
					// update existing role
					for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
						if (extension.getComponentNid() == context.getConceptNid()) {
							List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
							for (I_ExtendByRefPart part : extension.getMutableParts()) {
								I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
								if (strPart.getStringValue().equals(ruleUid)) {
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
									I_ExtendByRefPartCidString newPart = (I_ExtendByRefPartCidString) 
									lastPart.makeAnalog(
											ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
											editPath.getConceptNid(),
											Long.MAX_VALUE);
									newPart.setC1id(newRole.getConceptNid());
									extension.addVersion(newPart);
									tf.addUncommittedNoChecks(extension);
								}
								tf.addUncommittedNoChecks(contextRefset);
								tf.commit();
								promote(context);
							}
						}
					}
				} else {
					// same role, do nothing
				}
			} else if (currentRole != null && newRole == null) {
				// retire latest version of role
				for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
					if (extension.getComponentNid() == context.getConceptNid()) {
						List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
						for (I_ExtendByRefPart part : extension.getMutableParts()) {
							I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
							if (strPart.getStringValue().equals(ruleUid)) {
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
								I_ExtendByRefPartCidString newPart = (I_ExtendByRefPartCidString) 
								lastPart.makeAnalog(
										ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
										editPath.getConceptNid(),
										Long.MAX_VALUE);
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
			e.printStackTrace();
		}
	}

	public I_ExtendByRefPartCidString getLastStringPartForRule(String ruleUid, I_GetConceptData context) {
		try {
			I_ExtendByRefPartCidString lastPart = null;
			I_TermFactory tf = Terms.get();
			I_GetConceptData agendaMetadataRefset = tf.getConcept(RefsetAuxiliary.Concept.RULES_CONTEXT_METADATA_REFSET.getUids());
			//I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			//I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
			//I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
			//I_GetConceptData role = null;
			for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(agendaMetadataRefset.getConceptNid())) {
				if (extension.getComponentNid() == context.getConceptNid()) {
					List<I_ExtendByRefPartCidString> ruleParts = new ArrayList<I_ExtendByRefPartCidString>();
					for (I_ExtendByRefPart part : extension.getMutableParts()) {
						I_ExtendByRefPartCidString strPart = (I_ExtendByRefPartCidString) part;
						if (strPart.getStringValue().equals(ruleUid)) {
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public I_GetConceptData getRoleInContext(String ruleUid, I_GetConceptData context) {
		try {
			I_TermFactory tf = Terms.get();
			I_GetConceptData role = null;
			I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			I_ExtendByRefPartCidString lastPart = getLastStringPartForRule(ruleUid, context);
			if (lastPart != null) {
				if (lastPart.getStatusNid() == currentStatus.getConceptNid()) {
					role = tf.getConcept(lastPart.getC1id());
				}
			}
			return role;
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void updatePreferredTerm(I_GetConceptData concept, String newString,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			//			I_IntSet localDescTypes = null;
			//			if (config.getDescTypes().getSetValues().length > 0) {
			//				localDescTypes = config.getDescTypes();
			//			}
			List<? extends I_DescriptionTuple> descTuples = concept.getDescriptionTuples(
					config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(),
					config.getPrecedence(), config.getConflictResolutionStrategy());

			for (I_DescriptionTuple tuple : descTuples) {

				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					I_DescriptionVersioned description = tuple.getDescVersioned();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) tuple.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						newPart.setText(newString);
						description.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(concept);
					termFactory.commit();
					promote(concept);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = config.getAllowedStatus();
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(
				allowedStatus, config.getViewPositionSetReadOnly(), config.getPrecedence(),
				config.getConflictResolutionStrategy())) {
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

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	public HashMap<Integer, KnowledgeBase> getKbCache() {
		return kbCache;
	}

	public void setKbCache(HashMap<Integer, KnowledgeBase> kbCache) {
		this.kbCache = kbCache;
	}

	public void clearCache() {
		File dir = new File("rules");
		for (File loopFile : dir.listFiles()) {
			if (loopFile.getName().endsWith(".bkb")) {
				loopFile.delete();
			}
		}
		kbCache = new HashMap<Integer, KnowledgeBase>();
		//updateKbCacheFromFiles();
	}

	public List<RulesDeploymentPackageReference> getPackagesForContext(I_GetConceptData context) {
		List<? extends I_RelTuple> pkgRelTuples = null;
		RulesDeploymentPackageReferenceHelper refHelper = new RulesDeploymentPackageReferenceHelper(config);
		ArrayList<RulesDeploymentPackageReference> returnData = 
			new ArrayList<RulesDeploymentPackageReference>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			pkgRelTuples = context.getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			pkgRelTuples = cleanRelTuplesList(pkgRelTuples);

			if (pkgRelTuples != null) {
				for (I_RelTuple loopTuple : pkgRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(refHelper.getRulesDeploymentPackageReference(
								termFactory.getConcept(loopTuple.getC2Id())));
					}
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnData;
	}

	public List<I_GetConceptData> getContextsForPackage(RulesDeploymentPackageReference refPkg) {
		List<? extends I_RelTuple> pkgRelTuples = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			I_GetConceptData pkgConcept = termFactory.getConcept(refPkg.getUuids());
			pkgRelTuples = pkgConcept.getDestRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			pkgRelTuples = cleanRelTuplesList(pkgRelTuples);

			if (pkgRelTuples != null) {
				for (I_RelTuple loopTuple : pkgRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC1Id()));
					}
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return returnData;
	}

	public void addPkgReferenceToContext(RulesDeploymentPackageReference pkgReference, 
			I_GetConceptData context) {
		I_TermFactory termFactory = Terms.get();

		List<RulesDeploymentPackageReference> currentPkgs = getPackagesForContext(context);
		Boolean alreadyThere = false;
		for (RulesDeploymentPackageReference loopPkg : currentPkgs) {
			if (loopPkg.getUuids().containsAll(pkgReference.getUuids())) {
				//System.out.println("Setting to true...");
				alreadyThere = true;
			}
		}

		if (!alreadyThere) {
			try {
				boolean retiredAndReactivated = false;
				I_IntSet allowedStatus =  termFactory.newIntSet();
				allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
				I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
				I_GetConceptData pkgConcept = termFactory.getConcept(pkgReference.getUuids());
				List<? extends I_RelTuple> pkgRelTuples = pkgConcept.getDestRelTuples(
						allowedStatus, 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());
				pkgRelTuples = cleanRelTuplesList(pkgRelTuples);
				if (pkgRelTuples != null) {
					for (I_RelTuple loopTuple : pkgRelTuples) {
						if (loopTuple.getC1Id() == context.getConceptNid() && 
								loopTuple.getC2Id() == pkgConcept.getConceptNid()) {
							I_RelPart newPart = (I_RelPart) loopTuple.getMutablePart().makeAnalog(
									ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
									config.getEditingPathSetReadOnly().iterator().next().getConceptNid(),
									Long.MAX_VALUE);
							loopTuple.getFixedPart().addVersion(newPart);
							retiredAndReactivated = true;
							termFactory.addUncommittedNoChecks(context);
							termFactory.commit();
							promote(context);
						}
					}
				}
				if (!retiredAndReactivated) {
					//I_RelVersioned relVersioned = 
					termFactory.newRelationship(UUID.randomUUID(), 
							context, 
							termFactory.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids()), 
							termFactory.getConcept(pkgReference.getUuids()), 
							termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
							termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
							termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
							0, config);
					termFactory.addUncommittedNoChecks(context);
					termFactory.commit();
					promote(context);
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void removePkgReferenceFromContext(RulesDeploymentPackageReference pkgReference, 
			I_GetConceptData context) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> pkgRels = null;
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid());
			pkgRels = context.getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			I_GetConceptData pkgConcept = termFactory.getConcept(pkgReference.getUuids());
			for (I_RelTuple rel : pkgRels) {
				if (rel.getC1Id() == context.getConceptNid() && rel.getC2Id() == pkgConcept.getConceptNid()
						&& rel.getTypeNid() == RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(context);
					termFactory.commit();
					promote(context);
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private List<? extends I_RelTuple> cleanRelTuplesList(List<? extends I_RelTuple> tuples) {
		HashMap<Integer, I_RelTuple> cleanMap = new HashMap<Integer, I_RelTuple>();
		for (I_RelTuple loopTuple : tuples) {
			if (cleanMap.get(loopTuple.getRelId()) ==  null) {
				cleanMap.put(loopTuple.getRelId(), loopTuple);
			} else if (cleanMap.get(loopTuple.getRelId()).getTime() < loopTuple.getTime()) {
				cleanMap.put(loopTuple.getRelId(), loopTuple);
			}
		}
		List<I_RelTuple> cleanList = new ArrayList<I_RelTuple>();
		cleanList.addAll(cleanMap.values());
		return cleanList;
	}

	public boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		try {
			activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (activeStatuses.contains(statusId));
	}
	
	public void promote(I_GetConceptData concept) {
		try {
			I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
			allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			
			I_TermFactory termFactory = Terms.get();
			
			concept.promote(config.getViewPositionSet().iterator().next(), 
					config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME);
			
			for (I_ExtendByRef loopExtension : termFactory.getAllExtensionsForComponent(concept.getConceptNid())) {
				loopExtension.promote(config.getViewPositionSet().iterator().next(), 
						config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME);
			}
			
			termFactory.commit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
