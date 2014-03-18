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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept.REFSET_TYPES;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 * The Class RulesDeploymentPackageReferenceHelper.
 */
public class RulesDeploymentPackageReferenceHelper {

	/** The config. */
	I_ConfigAceFrame config;

	/**
	 * Instantiates a new rules deployment package reference helper.
	 *
	 * @param config the config
	 */
	public RulesDeploymentPackageReferenceHelper(I_ConfigAceFrame config) {
		this.config = config;

	}

	/**
	 * Creates the new rules deployment package.
	 *
	 * @param name the name
	 * @param url the url
	 * @return the rules deployment package reference
	 */
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
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			rulesPackage.setUuids(newConcept.getUids());
			String stringExtValue = url;
			refsetHelper.newRefsetExtension(rulesPackagesRefset.getConceptNid(), newConcept.getConceptNid(), 
					REFSET_TYPES.STR, 
					new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, stringExtValue), config); 

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.addUncommittedNoChecks(rulesPackagesRefset);

			newConcept.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(),
					config.getDbConfig().getChangeSetWriterThreading().convert());
			rulesPackagesRefset.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(),
					config.getDbConfig().getChangeSetWriterThreading().convert());
			
//			I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
//			allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
//			
//			newConcept.promote(config.getViewPositionSet().iterator().next(), 
//					config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME);
//			
//			for (I_ExtendByRef loopExtension : termFactory.getAllExtensionsForComponent(newConcept.getConceptNid())) {
//				loopExtension.promote(config.getViewPositionSet().iterator().next(), 
//						config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME);
//				termFactory.addUncommittedNoChecks(loopExtension);
//			}
//			newConcept.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(),
//					config.getDbConfig().getChangeSetWriterThreading().convert());
//			rulesPackagesRefset.commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(),
//					config.getDbConfig().getChangeSetWriterThreading().convert());

			return rulesPackage;

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
	 * Gets the rules deployment package reference.
	 *
	 * @param rulesPackageConcept the rules package concept
	 * @return the rules deployment package reference
	 */
	public RulesDeploymentPackageReference getRulesDeploymentPackageReference(ConceptChronicleBI rulesPackageConcept) {
		try {
			RulesDeploymentPackageReference rulesPackage = new RulesDeploymentPackageReference();
			rulesPackage.setName(rulesPackageConcept.toString());
			rulesPackage.setUuids(rulesPackageConcept.getUUIDs());

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
				refsetHelper.getAllCurrentRefsetExtensions(rulesPackageRefset.getConceptNid(), 
						rulesPackageConcept.getConceptNid());

			for (I_ExtendByRefPart loopPart : currentExtensionParts) {
				I_ExtendByRefPartStr strPart = (I_ExtendByRefPartStr) loopPart;
				String metadata = strPart.getStringValue();
				rulesPackage.setUrl(metadata);
			}

			return rulesPackage;

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
	 * Update deployment package reference.
	 *
	 * @param rulesPackageNewVersion the rules package new version
	 * @return the rules deployment package reference
	 */
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
			extensions.addAll(termFactory.getAllExtensionsForComponent(rulesPackageConcept.getConceptNid()));
			for (I_ExtendByRef extension : extensions) {
				for (PathBI editPath : config.getEditingPathSet()) {
					if (extension.getRefsetId() == rulesPackageRefset.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                                                                Long.MAX_VALUE,
                                                                config.getEditCoordinate().getAuthorNid(),
                                                                config.getEditCoordinate().getModuleNid(),
								editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
						termFactory.addUncommittedNoChecks(extension);
					}
				}
			}

			termFactory.addUncommittedNoChecks(rulesPackageConcept);
			termFactory.addUncommittedNoChecks(rulesPackageRefset);

			termFactory.commit();
			
			I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
			allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			
			rulesPackageConcept.promote(config.getViewPositionSet().iterator().next(), 
					config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, 
                                        Precedence.TIME, config.getEditCoordinate().getAuthorNid());
			
			for (I_ExtendByRef loopExtension : termFactory.getAllExtensionsForComponent(rulesPackageConcept.getConceptNid())) {
				loopExtension.promote(config.getViewPositionSet().iterator().next(), 
						config.getPromotionPathSetReadOnly(), allowedStatusWithRetired,
                                                Precedence.TIME, config.getEditCoordinate().getAuthorNid());
				termFactory.addUncommittedNoChecks(loopExtension);
			}
			termFactory.addUncommittedNoChecks(rulesPackageConcept);
			termFactory.addUncommittedNoChecks(rulesPackageRefset);
			termFactory.commit();

			rulesPackageNewVersion = getRulesDeploymentPackageReference(rulesPackageConcept);
			return rulesPackageNewVersion;

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
	 * Gets the all rules deployment packages.
	 *
	 * @return the all rules deployment packages
	 * @throws Exception the exception
	 */
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
				List<? extends I_ConceptAttributeTuple> attrs = child.getConceptAttributeTuples(allowedStatuses, config.getViewPositionSetReadOnly(), 
						config.getPrecedence(), 
						config.getConflictResolutionStrategy());
				if (!attrs.isEmpty() && attrs.iterator().next().getStatusNid() !=
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()) {
					rulesPackages.add(getRulesDeploymentPackageReference(child));
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return rulesPackages;

	}

	/**
	 * Update preferred term.
	 *
	 * @param concept the concept
	 * @param newString the new string
	 * @param config the config
	 */
	public void updatePreferredTerm(I_GetConceptData concept, String newString,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			//			I_IntSet localDescTypes = null;
			//			if (config.getDescTypes().getSetValues().length > 0) {
			//				localDescTypes = config.getDescTypes();
			//			}
			int preferred = termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_IntSet types = termFactory.newIntSet();
			types.add(preferred);
			List<? extends I_DescriptionTuple> descTuples = concept.getDescriptionTuples(
					config.getAllowedStatus(), 
					types, config.getViewPositionSetReadOnly(),
					config.getPrecedence(), config.getConflictResolutionStrategy());

			for (I_DescriptionTuple tuple : descTuples) {

				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					I_DescriptionVersioned description = tuple.getDescVersioned();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) tuple.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								Long.MAX_VALUE,
                                                                config.getEditCoordinate().getAuthorNid(),
                                                                config.getEditCoordinate().getModuleNid(),
								editPath.getConceptNid());
						newPart.setText(newString);
						description.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(concept);
					termFactory.commit();
					
					I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
					allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
					allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
					allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
					
					concept.promote(config.getViewPositionSet().iterator().next(), 
							config.getPromotionPathSetReadOnly(), allowedStatusWithRetired,
                                                        Precedence.TIME, config.getEditCoordinate().getAuthorNid());
					termFactory.addUncommittedNoChecks(concept);
					termFactory.commit();
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
	 * @param extension the extension
	 * @return the last extension part
	 * @throws TerminologyException the terminology exception
	 * @throws IOException signals that an I/O exception has occurred.
	 */
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

	/**
	 * Retire rules deployment package reference.
	 *
	 * @param rulesPackage the rules package
	 */
	public void retireRulesDeploymentPackageReference(RulesDeploymentPackageReference rulesPackage) {
		I_TermFactory termFactory = Terms.get();
		I_GetConceptData conceptToRetireUpdatedFromDB = null;
		try {
			conceptToRetireUpdatedFromDB = termFactory.getConcept(rulesPackage.getUuids());
			I_ConceptAttributePart lastAttributePart = getLastestAttributePart(conceptToRetireUpdatedFromDB);
			for (PathBI editPath : config.getEditingPathSet()) {
				I_ConceptAttributePart newAttributeVersion = 
					(I_ConceptAttributePart) lastAttributePart.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							Long.MAX_VALUE,
                                                        config.getEditCoordinate().getAuthorNid(),
                                                        config.getEditCoordinate().getModuleNid(),
							editPath.getConceptNid());
				conceptToRetireUpdatedFromDB.getConAttrs().addVersion(newAttributeVersion);
			}
			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			termFactory.commit();
			
			I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
			allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			
			conceptToRetireUpdatedFromDB.promote(config.getViewPositionSet().iterator().next(), 
					config.getPromotionPathSetReadOnly(), allowedStatusWithRetired,
                                        Precedence.TIME, config.getEditCoordinate().getAuthorNid());
			
			for (I_ExtendByRef loopExtension : termFactory.getAllExtensionsForComponent(conceptToRetireUpdatedFromDB.getConceptNid())) {
				loopExtension.promote(config.getViewPositionSet().iterator().next(), 
						config.getPromotionPathSetReadOnly(), allowedStatusWithRetired,
                                                Precedence.TIME, config.getEditCoordinate().getAuthorNid());
				termFactory.addUncommittedNoChecks(loopExtension);
			}
			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			termFactory.commit();
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the lastest attribute part.
	 *
	 * @param concept the concept
	 * @return the lastest attribute part
	 * @throws IOException signals that an I/O exception has occurred.
	 */
	private static I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData concept) throws IOException {
		List<? extends I_ConceptAttributePart> refsetAttibuteParts = concept.getConAttrs().getMutableParts();
		I_ConceptAttributePart latestAttributePart = null;
		for (I_ConceptAttributePart attributePart : refsetAttibuteParts) {
			if (latestAttributePart == null || attributePart.getTime() >= latestAttributePart.getTime()) {
				latestAttributePart = attributePart;
			}
		}

		if (latestAttributePart == null) {
			throw new IOException("No parts on this viewpositionset.");
		}

		return latestAttributePart;
	}
}
