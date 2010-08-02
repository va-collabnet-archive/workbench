package org.ihtsdo.rules.context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;

public class RulesDeploymentPackageReferenceHelper {

	I_ConfigAceFrame config;

	public RulesDeploymentPackageReferenceHelper(I_ConfigAceFrame config) {
		this.config = config;

	}

	public RulesDeploymentPackageReference createNewRulesDeploymentPackage(String name, String url) {
		try {
			RulesDeploymentPackageReference rulesPackage = new RulesDeploymentPackageReference();
			rulesPackage.setName(name);
			rulesPackage.setUrl(url);

			I_TermFactory termFactory = Terms.get();
			I_HelpRefsets refsetHelper;
			refsetHelper = termFactory.getRefsetHelper(config);
			refsetHelper.setAutocommitActive(true);

			I_GetConceptData newConcept = null;

			I_GetConceptData rulesPackagesRoot = termFactory.getConcept(
					RefsetAuxiliary.Concept.RULES_DEPLOYMENT_PKG.getUids());

			I_GetConceptData rulesPackagesRefset = termFactory.getConcept(
					RefsetAuxiliary.Concept.RULES_DEPLOYMENT_PKG_METADATA_REFSET.getUids());

			I_GetConceptData fsnType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			I_GetConceptData preferredType = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name + " (rules deployment package)",
					fsnType, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name,
					preferredType, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					rulesPackagesRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			rulesPackage.setUuids(newConcept.getUids());
			String stringExtValue = url;
			refsetHelper.newRefsetExtension(rulesPackagesRefset.getConceptId(), newConcept.getConceptId(), 
					REFSET_TYPES.STR, 
					new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, stringExtValue), config); 

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.addUncommittedNoChecks(rulesPackagesRefset);

			termFactory.commit();

			return rulesPackage;

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public RulesDeploymentPackageReference getRulesDeploymentPackageReference(I_GetConceptData rulesPackageConcept) {
		try {
			RulesDeploymentPackageReference rulesPackage = new RulesDeploymentPackageReference();
			rulesPackage.setName(rulesPackageConcept.toString());
			rulesPackage.setUuids(rulesPackageConcept.getUids());

			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_HelpRefsets refsetHelper;
			refsetHelper = termFactory.getRefsetHelper(config);
			refsetHelper.setAutocommitActive(true);


			I_GetConceptData rulesPackageRefset = termFactory.getConcept(
					new UUID[] {RefsetAuxiliary.Concept.RULES_DEPLOYMENT_PKG_METADATA_REFSET.getUids().iterator().next()});
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			I_IntSet descriptionTypes =  termFactory.newIntSet();
			descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

			List<I_ExtendByRefPart> currentExtensionParts = 
				refsetHelper.getAllCurrentRefsetExtensions(rulesPackageRefset.getConceptId(), 
						rulesPackageConcept.getConceptId());

			for (I_ExtendByRefPart loopPart : currentExtensionParts) {
				I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr) loopPart;
				String metadata = strPart.getStringValue();
				rulesPackage.setUrl(metadata);
			}

			return rulesPackage;

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public RulesDeploymentPackageReference updateDeploymentPackageReference(
			RulesDeploymentPackageReference rulesPackageNewVersion) {
		I_TermFactory termFactory = Terms.get();

		try {

			I_GetConceptData rulesPackageConcept = termFactory.getConcept(rulesPackageNewVersion.getUuids());
			RulesDeploymentPackageReference oldVersionOfRepository = getRulesDeploymentPackageReference(rulesPackageConcept);

			if (!oldVersionOfRepository.getName().equals(rulesPackageNewVersion.getName())) {
				updatePreferredTerm(rulesPackageConcept, rulesPackageNewVersion.getName(), config);
			}

			I_GetConceptData rulesPackageRefset = termFactory.getConcept(
					new UUID[] {RefsetAuxiliary.Concept.RULES_DEPLOYMENT_PKG_METADATA_REFSET.getUids().iterator().next()});

			String metadata = rulesPackageNewVersion.getUrl();

			List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
			extensions.addAll(termFactory.getAllExtensionsForComponent(rulesPackageConcept.getConceptId()));
			for (I_ExtendByRef extension : extensions) {
				for (I_Path editPath : config.getEditingPathSet()) {
					if (extension.getRefsetId() == rulesPackageRefset.getConceptId()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptId(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
						termFactory.addUncommittedNoChecks(extension);
					}
				}
			}

			termFactory.addUncommittedNoChecks(rulesPackageConcept);
			termFactory.addUncommittedNoChecks(rulesPackageRefset);

			termFactory.commit();

			rulesPackageNewVersion = getRulesDeploymentPackageReference(rulesPackageConcept);
			return rulesPackageNewVersion;

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}
	public List<RulesDeploymentPackageReference> getAllRulesDeploymentPackages() throws Exception {
		I_TermFactory termFactory = Terms.get();
		List<RulesDeploymentPackageReference> rulesPackages = new ArrayList<RulesDeploymentPackageReference>();
		try {
			I_GetConceptData rulesPackagesRoot = termFactory.getConcept(
					new UUID[] {RefsetAuxiliary.Concept.RULES_DEPLOYMENT_PKG.getUids().iterator().next()});
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			I_IntSet allowedStatuses =  termFactory.newIntSet();
			allowedStatuses.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.RETIRED.getUids()));
			allowedStatuses.addAll(config.getAllowedStatus().getSetValues());
			Set<? extends I_GetConceptData> children = rulesPackagesRoot.getDestRelOrigins(allowedStatuses,
					allowedDestRelTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				if (child.getConceptAttributeTuples(allowedStatuses, config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), 
						config.getConflictResolutionStrategy()).iterator().next().getStatusId() !=
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()) {
					rulesPackages.add(getRulesDeploymentPackageReference(child));
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rulesPackages;

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
					for (I_Path editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) tuple.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptId(),
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

	public void retireRulesDeploymentPackageReference(RulesDeploymentPackageReference rulesPackage) {
		I_TermFactory termFactory = Terms.get();
		I_GetConceptData conceptToRetireUpdatedFromDB = null;
		try {
			conceptToRetireUpdatedFromDB = termFactory.getConcept(rulesPackage.getUuids());
			I_ConceptAttributePart lastAttributePart = getLastestAttributePart(conceptToRetireUpdatedFromDB);
			for (I_Path editPath : config.getEditingPathSet()) {
				I_ConceptAttributePart newAttributeVersion = 
					(I_ConceptAttributePart) lastAttributePart.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							editPath.getConceptId(), 
							Long.MAX_VALUE);
				conceptToRetireUpdatedFromDB.getConceptAttributes().addVersion(newAttributeVersion);
			}
			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			termFactory.commit();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData concept) throws IOException {
		List<? extends I_ConceptAttributePart> refsetAttibuteParts = concept.getConceptAttributes().getMutableParts();
		I_ConceptAttributePart latestAttributePart = null;
		for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
			if (latestAttributePart == null || attributePart.getVersion() >= latestAttributePart.getVersion()) {
				latestAttributePart = attributePart;
			}
		}

		if (latestAttributePart == null) {
			throw new IOException("No parts on this viewpositionset.");
		}

		return latestAttributePart;
	}
}
