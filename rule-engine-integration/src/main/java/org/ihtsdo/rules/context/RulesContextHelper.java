package org.ihtsdo.rules.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PathBI;

public class RulesContextHelper {

	I_ConfigAceFrame config;

	public RulesContextHelper(I_ConfigAceFrame config) {
		this.config = config;
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
			} else if (currentRolePart != null && newRole != null){
				if (currentRolePart.getC1id() != newRole.getConceptNid() || currentRolePart.getStatusId() != currentStatus.getConceptNid()) {
					// update existing role
					for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
						if (extension.getComponentId() == context.getConceptNid()) {
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
									if (loopPart.getVersion() >= lastPart.getVersion()) {
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
							}
						}
					}
				} else {
					// same role, do nothing
				}
			} else if (currentRole != null && newRole == null) {
				// retire latest version of role
				for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(contextRefset.getConceptNid())) {
					if (extension.getComponentId() == context.getConceptNid()) {
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
								if (loopPart.getVersion() >= lastPart.getVersion()) {
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
			I_GetConceptData currentStatus = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
			//I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
			//I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
			I_GetConceptData role = null;
			for (I_ExtendByRef extension : tf.getRefsetExtensionMembers(agendaMetadataRefset.getConceptNid())) {
				if (extension.getComponentId() == context.getConceptNid()) {
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
							if (loopPart.getVersion() >= lastPart.getVersion()) {
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
				if (lastPart.getStatusId() == currentStatus.getConceptNid()) {
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

				if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
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
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		int lastVersion = Integer.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = config.getAllowedStatus();
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(
				allowedStatus, config.getViewPositionSetReadOnly(), config.getPrecedence(),
				config.getConflictResolutionStrategy())) {
			if (loopTuple.getVersion() > lastVersion) {
				lastVersion = loopTuple.getVersion();
				lastPart = loopTuple.getMutablePart();
			}
		}

		if (lastPart == null) {
			throw new TerminologyException("No parts on this viewpositionset.");
		}

		return lastPart;
	}
}
