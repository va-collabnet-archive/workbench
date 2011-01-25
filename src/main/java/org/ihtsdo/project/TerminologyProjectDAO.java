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
package org.ihtsdo.project;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.PrivilegedActionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.jini.config.ConfigurationException;
import net.jini.core.entry.Entry;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.lookup.ServiceItemFilter;
import net.jini.lookup.entry.Name;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.RefsetPropertyMap.REFSET_PROPERTY;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_QueueProcesses;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.ProcessID;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.model.WorkSetMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TerminologyProjectDAO.
 */
public class TerminologyProjectDAO {

	//I_TerminologyProjects CRUD **********************************

	/**
	 * Gets the all projects.
	 * 
	 * @param config the config
	 * 
	 * @return the all projects
	 */
	public static List<I_TerminologyProject> getAllProjects(I_ConfigAceFrame config) {
		List<I_TerminologyProject> projects = new ArrayList<I_TerminologyProject>();
		projects.addAll(getAllTranslationProjects(config));
		return projects;
	}

	public static List<TranslationProject> getAllTranslationProjects(I_ConfigAceFrame config) {

		I_TermFactory termFactory = Terms.get();
		List<TranslationProject> projects = new ArrayList<TranslationProject>();
		try {
			I_GetConceptData projectsRoot = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.getUids());
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<? extends I_GetConceptData> children = projectsRoot.getDestRelOrigins(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(child);
				if (isActive(lastAttributePart.getStatusNid())) {
					projects.add(getTranslationProject(child, config));
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return projects;
	}

	public static TranslationProject createNewTranslationProject(String name, I_ConfigAceFrame config) {
		TranslationProject project = new TranslationProject(name,0, null);
		return createNewTranslationProject(project, config);
	}

	/**
	 * Creates the new project.
	 * 
	 * @param projectWithMetadata the project with metadata
	 * @param config the config
	 * 
	 * @return the i_ terminology project
	 */
	public static TranslationProject createNewTranslationProject(TranslationProject projectWithMetadata,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		TranslationProject project = null;
		I_GetConceptData newConcept = null;
		String projectName = projectWithMetadata.getName() + " (translation project)";
		try {
			if(isConceptDuplicate(projectName)){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated project name", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Project with the same name allready exists.");
			}

			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData projectsRoot = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()),
					config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), 
					config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					projectsRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			project = new TranslationProject(projectWithMetadata.getName(), 
					newConcept.getConceptNid(), newConcept.getUids());

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.commit();

//			promote(newConcept, config);
//
//			termFactory.addUncommittedNoChecks(newConcept);
//			termFactory.commit();

			project = getTranslationProject(newConcept, config);

			String nacWorkSetName = "Maintenance - " + project.getName().replace("(translation project)", "");
			WorkSet nacWorkSet = createNewWorkSet(nacWorkSetName, project, config);
			createNewPartitionScheme("Maintenance - " + project.getName().replace("(translation project)", ""), nacWorkSet.getUids().iterator().next(), config);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return project;
	}

	/**
	 * Gets the project.
	 * 
	 * @param projectConcept the project concept
	 * @param config the config
	 * 
	 * @return the project
	 * @throws Exception 
	 */
	public static TranslationProject getTranslationProject(I_GetConceptData projectConcept, I_ConfigAceFrame config) throws Exception {
		TranslationProject project = null;
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = projectConcept.getSourceRelTargets(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1 ) {
				throw new Exception("Error: Wrong number of parents in project...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.localize().getNid()) {
				String name = projectConcept.toString();
				List<? extends I_DescriptionTuple> descTuples = projectConcept.getDescriptionTuples(
						config.getAllowedStatus(), 
						config.getDescTypes(), config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				project = new TranslationProject(name, projectConcept.getConceptNid(),
						projectConcept.getUids());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return project;
	}

	public static void updatePreferredTerm(I_GetConceptData concept, String newString,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet localDescTypes = null;
			if (config.getDescTypes().getSetValues().length > 0) {
				localDescTypes = config.getDescTypes();
			}
			List<? extends I_DescriptionTuple> descTuples = concept.getDescriptionTuples(
					config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

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
					//					termFactory.commit();
					termFactory.addUncommittedNoChecks(concept);
					//					termFactory.commit();
				}
			}
//			promote(concept, config);
//			termFactory.addUncommittedNoChecks(concept);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static WorkSet updateWorkSetMetadata(WorkSet workSetWithMetadata, I_ConfigAceFrame config) {
		WorkSet workSet = null;

		WorkSet currentVersionOfWorkSet = getWorkSet(
				workSetWithMetadata.getConcept(), config);

		if (!currentVersionOfWorkSet.getName().equals(workSetWithMetadata.getName())) {
			updatePreferredTerm(workSetWithMetadata.getConcept(), workSetWithMetadata.getName(), config);
		}

		workSet = getWorkSet(workSetWithMetadata.getConcept(), config);

		return workSet;
	}

	public static PartitionScheme updatePartitionSchemeMetadata(PartitionScheme partitionSchemeWithMetadata, 
			I_ConfigAceFrame config) {
		PartitionScheme partitionScheme = null;

		PartitionScheme currentVersionOfPartitionScheme = getPartitionScheme(
				partitionSchemeWithMetadata.getConcept(), config);

		if (!currentVersionOfPartitionScheme.getName().equals(partitionSchemeWithMetadata.getName())) {
			updatePreferredTerm(partitionSchemeWithMetadata.getConcept(), partitionSchemeWithMetadata.getName(), 
					config);
		}

		partitionScheme = getPartitionScheme(partitionSchemeWithMetadata.getConcept(), config);

		return partitionScheme;
	}

	public static Partition updatePartitionMetadata(Partition partitionWithMetadata, 
			I_ConfigAceFrame config) {
		Partition partition = null;

		Partition currentVersionOfPartition = getPartition(partitionWithMetadata.getConcept(), config);
		if (!currentVersionOfPartition.getName().equals(partitionWithMetadata.getName())) {
			updatePreferredTerm(partitionWithMetadata.getConcept(), partitionWithMetadata.getName(),config);
		}
		partition = getPartition(partitionWithMetadata.getConcept(), config);
		return partition;
	}

	/**
	 * Update project metadata.
	 * 
	 * @param projectWithMetadata the project with metadata
	 * @param config the config
	 * 
	 * @return the i_ terminology project
	 */
	public static TranslationProject updateTranslationProjectMetadata(TranslationProject projectWithMetadata, 
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		TranslationProject project = null;

		try {

			TranslationProject currentVersionOfProject = getTranslationProject(
					projectWithMetadata.getConcept(), config);

			if (!currentVersionOfProject.getName().equals(projectWithMetadata.getName())) {
				updatePreferredTerm(projectWithMetadata.getConcept(), projectWithMetadata.getName(), config);
			}

			I_GetConceptData projectsRefset = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PROJECT_EXTENSION_REFSET.getUids());

			I_GetConceptData projectConcept = termFactory.getConcept(projectWithMetadata.getUids());

			String metadata = serialize(projectWithMetadata);

			List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
			extensions.addAll(termFactory.getAllExtensionsForComponent(projectConcept.getConceptNid()));
			for (I_ExtendByRef extension : extensions) {
				for (PathBI editPath : config.getEditingPathSet()) {
					if (extension.getRefsetId() == projectsRefset.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
						termFactory.addUncommittedNoChecks(projectsRefset);
						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(projectsRefset);
//						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
					}
				}
			}
			project = getTranslationProject(projectConcept, config);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return project;
	}

	public static WorkSet createNewWorkSet(String name, I_TerminologyProject project, I_ConfigAceFrame config) {
		WorkSet workSet = new WorkSet(name, project.getUids().iterator().next());
		return createNewWorkSet(workSet, config);
	}

	public static WorkSet createNewWorkSet(String name, UUID projectUUID, I_ConfigAceFrame config) {
		WorkSet workSet = new WorkSet(name, projectUUID);
		return createNewWorkSet(workSet, config);
	}

	/**
	 * Creates the new work set.
	 * 
	 * @param worksetWithMetadata the workset with metadata
	 * @param config the config
	 * 
	 * @return the work set
	 */
	public static WorkSet createNewWorkSet(WorkSet worksetWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkSet workSet = null;
		I_GetConceptData newConcept = null;
		String name = worksetWithMetadata.getName();
		String workSetName = worksetWithMetadata.getName() + " (workset)";
		try {
			if(isConceptDuplicate(workSetName)){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated workSet name", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Workset with the same name allready exists.");
			}

			I_GetConceptData worksetsRoot = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.getUids());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData project = termFactory.getConcept(worksetWithMetadata.getProjectUUID());

			I_GetConceptData commentsRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData promotionRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData purposeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData defining = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
			I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
			I_GetConceptData current = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());


			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", workSetName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", workSetName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					worksetsRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					includesFromAttribute, 
					project, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			I_GetConceptData newCommentsConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newCommentsConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetsRoot, defining, refinability, 
					current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, commentsRelConcept, newCommentsConcept, defining, refinability, 
					current, 0, config);

			I_GetConceptData newPromotionConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newPromotionConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetsRoot, defining, refinability, 
					current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, promotionRelConcept, newPromotionConcept, defining, refinability, 
					current, 0, config);

			workSet = new WorkSet(worksetWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(),
					worksetWithMetadata.getProjectUUID());

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.addUncommittedNoChecks(newCommentsConcept);
			termFactory.addUncommittedNoChecks(newPromotionConcept);
			termFactory.commit();
//			promote(newConcept, config);
//			promote(newCommentsConcept, config);
//			promote(newPromotionConcept, config);
//			termFactory.addUncommittedNoChecks(newConcept);
//			termFactory.addUncommittedNoChecks(newCommentsConcept);
//			termFactory.addUncommittedNoChecks(newPromotionConcept);
			termFactory.commit();


		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workSet;
	}

	/**
	 * Gets the work set.
	 * 
	 * @param workSetConcept the work set concept
	 * @param config the config
	 * 
	 * @return the work set
	 */
	public static WorkSet getWorkSet(I_GetConceptData workSetConcept, I_ConfigAceFrame config) {
		WorkSet workSet = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = workSetConcept.getSourceRelTargets(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1 ) {
				throw new Exception("Error: Wrong number of parents in workset...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.localize().getNid()) {
				String name = workSetConcept.toString();
				List<? extends I_DescriptionTuple> descTuples = workSetConcept.getDescriptionTuples(
						config.getAllowedStatus(), 
						(config.getDescTypes().getSetValues().length == 0)?null:config.getDescTypes(), 
								config.getViewPositionSetReadOnly(), 
								Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> projects = workSetConcept.getSourceRelTargets(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (projects.size() != 1 ) {
					throw new Exception("Error: Wrong number of projects in workset...");
				}

				I_GetConceptData project = projects.iterator().next();

				workSet = new WorkSet(name, 
						workSetConcept.getConceptNid(),
						workSetConcept.getUids(),
						project.getUids().iterator().next());
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workSet;
	}

	public static List<PartitionScheme> getAllPartitionSchemesForRefsetConcept(I_GetConceptData workSetConcept, 
			I_ConfigAceFrame config) {
		List<PartitionScheme> partitionSchemes = new ArrayList<PartitionScheme>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			List<? extends I_RelTuple> partionSchemeTuples = workSetConcept.getDestRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple loopTuple : partionSchemeTuples) {
				I_GetConceptData loopConcept = termFactory.getConcept(loopTuple.getC1Id());
				I_ConceptAttributePart latestAttributePart = getLastestAttributePart(loopConcept);
				if (isActive(latestAttributePart.getStatusNid())) {
					partitionSchemes.add(getPartitionScheme(loopConcept, config));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		return partitionSchemes;
	}

	public static List<I_GetConceptData> getMembersNotPartitioned(PartitionScheme scheme, 
			I_ConfigAceFrame config) {
		List<I_GetConceptData> refsetMembersNotPartitioned = new ArrayList<I_GetConceptData>();
		List<PartitionMember> partitionMembersAlreadyPartitioned = new ArrayList<PartitionMember>();
		List<Integer> partitionedMembersIds = new ArrayList<Integer>();
		I_TermFactory termFactory = Terms.get();

		try {
			for (Partition loopPartition : scheme.getPartitions()) {
				partitionMembersAlreadyPartitioned.addAll(loopPartition.getPartitionMembers());
			}

			for (PartitionMember loopPartitionMember : partitionMembersAlreadyPartitioned) {
				partitionedMembersIds.add(loopPartitionMember.getId());
			}

			I_GetConceptData refset = termFactory.getConcept(scheme.getSourceRefsetUUID());

			Collection<? extends I_ExtendByRef> refsetMembers  = termFactory.getRefsetExtensionMembers(
					refset.getConceptNid());

			for (I_ExtendByRef refsetMember : refsetMembers) {
				if (isActive(getLastExtensionPart(refsetMember).getStatusNid())) {
					I_GetConceptData memberConcept = termFactory.getConcept(refsetMember.getComponentNid());
					if (!partitionedMembersIds.contains(memberConcept.getConceptNid())) {
						refsetMembersNotPartitioned.add(memberConcept);
					}
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return refsetMembersNotPartitioned;
	}

	public static List<Partition> getAllPartitionsForScheme(PartitionScheme scheme, 
			I_ConfigAceFrame config) {
		List<Partition> partitions = new ArrayList<Partition>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			Set<? extends I_GetConceptData> origins = scheme.getConcept().getDestRelOrigins(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData origin : origins) {
				I_ConceptAttributePart latestAttributePart = getLastestAttributePart(origin);
				if (isActive(latestAttributePart.getStatusNid())) {
					partitions.add(getPartition(origin, config));
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return partitions;
	}

	public static void removeRefsetAsExclusion(I_TerminologyProject project, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		removeRefsetAsExclusion(project.getConcept(), exclusion, config);
	}

	public static void removeRefsetAsExclusion(WorkSet workSet, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		removeRefsetAsExclusion(workSet.getConcept(), exclusion, config);
	}

	public static void removeRefsetAsExclusion(I_GetConceptData refset, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> exclusionRefsetRels = null;
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsetRels = refset.getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : exclusionRefsetRels) {
				if (rel.getC1Id() ==refset.getConceptNid() && rel.getC2Id() == exclusion.getConceptNid()
						&& rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(refset);
					termFactory.commit();
//					//promote(relVersioned, config);
//					termFactory.addUncommittedNoChecks(refset);
//					termFactory.commit();
				}
			}
//			promote(refset, config);
//			termFactory.addUncommittedNoChecks(refset);
			termFactory.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void removeRefsetAsSourceLanguage(I_TerminologyProject project, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> languageSourceRefsetRels = null;
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			languageSourceRefsetRels = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : languageSourceRefsetRels) {
				if (rel.getC1Id() == project.getId() && rel.getC2Id() == concept.getConceptNid()
						&& rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(project.getConcept());
					termFactory.commit();
					//promote(relVersioned, config);
					//					termFactory.addUncommittedNoChecks(project.getConcept());
					//					termFactory.commit();
				}
			}
//			promote(project.getConcept(), config);
//			termFactory.addUncommittedNoChecks(project.getConcept());
//			termFactory.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void removeRefsetAsCommon(I_TerminologyProject project, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> exclusionRefsetRels = null;
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsetRels = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : exclusionRefsetRels) {
				if (rel.getC1Id() == project.getId() && rel.getC2Id() == concept.getConceptNid()
						&& rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(project.getConcept());
					termFactory.commit();
					//promote(relVersioned, config);
					//					termFactory.addUncommittedNoChecks(project.getConcept());
					//					termFactory.commit();
				}
			}
//			promote(project.getConcept(),config);
//			termFactory.addUncommittedNoChecks(project.getConcept());
//			termFactory.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void addRefsetAsExclusion(I_GetConceptData refset, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		List<I_GetConceptData> currentExclusions = getExclusionRefsetsForConcept(refset, config);
		boolean alreadyExclusion = false;
		for (I_GetConceptData currentExclusion : currentExclusions) {
			if (currentExclusion.getConceptNid() == exclusion.getConceptNid()) {
				alreadyExclusion = true;
			}
		}

		if (!alreadyExclusion) {
			try {
				I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), refset, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.getUids()), 
						exclusion, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(refset);
				termFactory.commit();
//				promote(refset, config);
//				termFactory.addUncommittedNoChecks(refset);
//				termFactory.commit();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void addRefsetAsExclusion(I_TerminologyProject project, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		addRefsetAsExclusion(project.getConcept(), exclusion, config);
	}

	public static void addRefsetAsExclusion(WorkSet workSet, I_GetConceptData exclusion,
			I_ConfigAceFrame config) {
		addRefsetAsExclusion(workSet.getConcept(), exclusion, config);
	}

	public static void addRefsetAsCommon(I_TerminologyProject project, I_GetConceptData common,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		List<I_GetConceptData> currentCommons = getCommonRefsetsForProject(project, config);
		boolean alreadyExclusion = false;
		for (I_GetConceptData currentCommon : currentCommons) {
			if (currentCommon.getConceptNid() == common.getConceptNid()) {
				alreadyExclusion = true;
			}
		}

		if (!alreadyExclusion) {
			try {
				I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.getUids()), 
						common, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				termFactory.commit();
//				promote(project.getConcept(), config);
//				termFactory.addUncommittedNoChecks(project.getConcept());
//				termFactory.commit();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void addRefsetAsSourceLanguage(I_TerminologyProject project, I_GetConceptData language,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<I_GetConceptData> currentSourceLanguages = getSourceLanguageRefsetsForProject(project, config);
		boolean alreadySource = false;
		for (I_GetConceptData currentSourceLanguage : currentSourceLanguages) {
			if (currentSourceLanguage.getConceptNid() == language.getConceptNid()) {
				alreadySource = true;
			}
		}

		if (!alreadySource) {
			try {
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.getUids()), 
						language, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				termFactory.commit();
				Thread.sleep(100);
//				promote(project.getConcept(), config);
//				termFactory.addUncommittedNoChecks(project.getConcept());
//				termFactory.commit();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void setSourceRefset(WorkSet workSet, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> sourceRefsetRels = null;
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			sourceRefsetRels = workSet.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (sourceRefsetRels.size() > 0) {
				for (I_RelTuple rel : sourceRefsetRels) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(workSet.getConcept());
					termFactory.commit();
					//					promote(relVersioned, config);
					//					termFactory.addUncommittedNoChecks(workSet.getConcept());
					//					termFactory.commit();
				}
//				termFactory.addUncommittedNoChecks(workSet.getConcept());
//				termFactory.commit();
				//promote(workSet.getConcept(), config);
			}
			I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), workSet.getConcept(), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.getUids()), 
					concept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.addUncommittedNoChecks(workSet.getConcept());
			termFactory.commit();
//			promote(workSet.getConcept(), config);
//			termFactory.addUncommittedNoChecks(workSet.getConcept());
//			termFactory.commit();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setLanguageTargetRefset(TranslationProject project, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getTargetLanguageRefset() == null) || 
					(concept.getConceptNid() != project.getTargetLanguageRefset().getConceptNid())) {
				List<? extends I_RelTuple> targetRefsetRels = null;
				I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
				targetRefsetRels = project.getConcept().getSourceRelTuples(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (targetRefsetRels.size() > 0) {
					for (I_RelTuple rel : targetRefsetRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
									ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						termFactory.commit();
//						Thread.sleep(100);
//						//						promote(project.getConcept(), config);
//						//						termFactory.addUncommittedNoChecks(project.getConcept());
//						//						termFactory.commit();
					}
//					promote(project.getConcept(), config);
//					termFactory.addUncommittedNoChecks(project.getConcept());
//					termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.getUids()), 
						concept, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				termFactory.commit();
//				Thread.sleep(100);
//				promote(project.getConcept(), config);
//				termFactory.addUncommittedNoChecks(project.getConcept());
//				termFactory.commit();
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static I_GetConceptData getSourceIssueRepoForProject(
			TranslationProject project, 
			I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> repos = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> reposTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.localize().getNid());
			reposTuples = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			reposTuples = cleanRelTuplesList(reposTuples);

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (I_RelTuple rel : reposTuples) {
			if (isActive(rel.getStatusNid())) {
				repos.add(rel);
			}
		}

		if (repos.size() == 0) {
			return null;
		} else if (repos.size() == 1) {
			return termFactory.getConcept(repos.iterator().next().getC2Id());
		} else throw new Exception("Wrong number of source repos.");
	}

	public static void setSourceIssueRepo(TranslationProject project, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getSourceIssueRepo() == null) || 
					(concept.getConceptNid() != project.getSourceIssueRepo().getConceptNid())) {
				List<? extends I_RelTuple> reposRels = null;
				I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.localize().getNid());
				reposRels = project.getConcept().getSourceRelTuples(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (reposRels.size() > 0) {
					for (I_RelTuple rel : reposRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
									ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						termFactory.commit();
						//						promote(relVersioned, config);
						//						termFactory.addUncommittedNoChecks(project.getConcept());
						//						termFactory.commit();
					}
//					promote(project.getConcept(), config);
//					termFactory.addUncommittedNoChecks(project.getConcept());
//					termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.getUids()), 
						concept, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				termFactory.commit();
//				promote(project.getConcept(), config);
//				termFactory.addUncommittedNoChecks(project.getConcept());
//				termFactory.commit();
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static I_GetConceptData getProjectIssueRepoForProject(
			TranslationProject project, 
			I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> repos = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> reposTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.localize().getNid());
			reposTuples = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			reposTuples = cleanRelTuplesList(reposTuples);

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (I_RelTuple rel : reposTuples) {
			if (isActive(rel.getStatusNid())) {
				repos.add(rel);
			}
		}

		if (repos.size() == 0) {
			return null;
		} else if (repos.size() == 1) {
			return termFactory.getConcept(repos.iterator().next().getC2Id());
		} else throw new Exception("Wrong number of source repos.");
	}

	public static void setProjectIssueRepo(TranslationProject project, I_GetConceptData concept,
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getProjectIssueRepo() == null) || 
					(concept.getConceptNid() != project.getProjectIssueRepo().getConceptNid())) {
				List<? extends I_RelTuple> reposRels = null;
				I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.localize().getNid());
				reposRels = project.getConcept().getSourceRelTuples(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (reposRels.size() > 0) {
					for (I_RelTuple rel : reposRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(
									ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						termFactory.commit();
						//						promote(relVersioned, config);
						//						termFactory.addUncommittedNoChecks(project.getConcept());
						//						termFactory.commit();
					}
//					promote(project.getConcept(), config);
//					termFactory.addUncommittedNoChecks(project.getConcept());
//					termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.getUids()), 
						concept, 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
						0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				termFactory.commit();
//				promote(project.getConcept(), config);
//				termFactory.addUncommittedNoChecks(project.getConcept());
//				termFactory.commit();
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static I_GetConceptData getSourceRefsetForWorkSet(
			WorkSet workSet, 
			I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> sourceRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> sourceRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			sourceRefsetsTuples = workSet.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (I_RelTuple rel : sourceRefsetsTuples) {
			if (isActive(rel.getStatusNid())) {
				sourceRefsets.add(rel);
			}
		}

		if (sourceRefsets.size() == 0) {
			return null;
		} else if (sourceRefsets.size() == 1) {
			return termFactory.getConcept(sourceRefsets.iterator().next().getC2Id());
		} else throw new Exception("Wrong number of source refsets.");
	}

	public static I_GetConceptData getTargetLanguageRefsetForProject(
			TranslationProject project, 
			I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
			targetRefsetsTuples = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (I_RelTuple rel : targetRefsetsTuples) {
			if (isActive(rel.getStatusNid())) {
				targetRefsets.add(rel);
			}
		}

		if (targetRefsets.size() == 0) {
			return null;
		} else if (targetRefsets.size() == 1) {
			return termFactory.getConcept(targetRefsets.iterator().next().getC2Id());
		} else throw new Exception("Wrong number of target refsets.");
	}

	public static List<I_GetConceptData> getExclusionRefsetsForProject(
			I_TerminologyProject project, 
			I_ConfigAceFrame config) {

		return getExclusionRefsetsForConcept(project.getConcept(), config);

	}

	public static List<I_GetConceptData> getExclusionRefsetsForWorkSet(
			WorkSet workSet, 
			I_ConfigAceFrame config) {

		return getExclusionRefsetsForConcept(workSet.getConcept(), config);

	}

	public static List<I_GetConceptData> getExclusionRefsetsForConcept(
			I_GetConceptData refset, 
			I_ConfigAceFrame config) {
		List<? extends I_RelTuple> exclusionRefsets = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsets = refset.getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			exclusionRefsets = cleanRelTuplesList(exclusionRefsets);

			if (exclusionRefsets != null) {
				for (I_RelTuple loopTuple : exclusionRefsets) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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

	public static List<I_GetConceptData> getSourceLanguageRefsetsForProject(
			I_TerminologyProject project, 
			I_ConfigAceFrame config) {
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());

			List<? extends I_RelTuple> sourceLanguageRefsetsTuples = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			sourceLanguageRefsetsTuples = cleanRelTuplesList(sourceLanguageRefsetsTuples);

			if (sourceLanguageRefsetsTuples != null) {
				for (I_RelTuple loopTuple : sourceLanguageRefsetsTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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

	public static List<I_GetConceptData> getCommonRefsetsForProject(
			I_TerminologyProject project, 
			I_ConfigAceFrame config) {
		List<? extends I_RelTuple> commonRefsetsRelTuples = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid());
			commonRefsetsRelTuples = project.getConcept().getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			commonRefsetsRelTuples = cleanRelTuplesList(commonRefsetsRelTuples);

			if (commonRefsetsRelTuples != null) {
				for (I_RelTuple loopTuple : commonRefsetsRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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

	public static PartitionScheme getPartitionScheme(I_GetConceptData partitionSchemeConcept, 
			I_ConfigAceFrame config) {
		PartitionScheme partitionScheme = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = partitionSchemeConcept.getSourceRelTargets(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1 ) {
				throw new Exception("Error: Partition scheme wrong nomber of worksets...");
			}

			I_GetConceptData parent = parents.iterator().next();

			String name = partitionSchemeConcept.toString();
			List<? extends I_DescriptionTuple> descTuples = partitionSchemeConcept.getDescriptionTuples(
					config.getAllowedStatus(), 
					config.getDescTypes(), config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_DescriptionTuple tuple : descTuples) {
				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					name = tuple.getText();
				}
			}

			if (getWorkSet(parent,config) != null || getPartition(parent,config) != null) {
				partitionScheme = new PartitionScheme(name, 
						partitionSchemeConcept.getConceptNid(),
						partitionSchemeConcept.getUids(),
						parent.getUids().iterator().next());
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return partitionScheme;
	}

	public static Partition getPartition(I_GetConceptData partitionConcept, 
			I_ConfigAceFrame config) {
		Partition partition = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = partitionConcept.getSourceRelTargets(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1 ) {
				throw new Exception("Error: Wrong number of parents in partition...");
			}

			I_GetConceptData parent = parents.iterator().next();


			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.localize().getNid()) {
				allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> schemes = partitionConcept.getSourceRelTargets(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (schemes.size() != 1 ) {
					throw new Exception("Error: Wrong number of schemes in partition...");
				}

				String name = partitionConcept.toString();
				List<? extends I_DescriptionTuple> descTuples = partitionConcept.getDescriptionTuples(
						config.getAllowedStatus(), 
						(config.getDescTypes().getSetValues().length == 0)?null:config.getDescTypes(),
								config.getViewPositionSetReadOnly(),
								Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}

				I_GetConceptData scheme = schemes.iterator().next();

				partition = new Partition(name, 
						partitionConcept.getConceptNid(),
						partitionConcept.getUids(),
						scheme.getUids().iterator().next());
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return partition;
	}

	/**
	 * Gets the all work sets for project.
	 * 
	 * @param project the project
	 * @param config the config
	 * 
	 * @return the all work sets for project
	 */
	public static List<WorkSet> getAllWorkSetsForProject(I_TerminologyProject project, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkSet> workSets = new ArrayList<WorkSet>();
		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());
			Set<? extends I_GetConceptData> children = project.getConcept().getDestRelOrigins(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(child);
				if (isActive(lastAttributePart.getStatusNid())) {
					WorkSet workSet = getWorkSet(child, config);
					if (project.getUids().contains(workSet.getProjectUUID())) {
						workSets.add(workSet);
					}
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workSets;
	}

	public static void addConceptAsWorkSetMember(I_GetConceptData workSetMemberConcept, 
			UUID workSetUUID, I_ConfigAceFrame config) {
		try {
			WorkSetMember member = new WorkSetMember(workSetMemberConcept.toString(), 
					workSetMemberConcept.getConceptNid(), workSetMemberConcept.getUids(),
					workSetUUID);
			addConceptAsWorkSetMember(member, config); 
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adds the concept as work set member.
	 * 
	 * @param member the member
	 * @param config the config
	 */
	public static void addConceptAsWorkSetMember(WorkSetMember member, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = termFactory.getConcept(member.getUids());
			boolean alreadyMember = false;
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(newMemberConcept.getConceptNid());
			I_GetConceptData workSetConcept = termFactory.getConcept(member.getWorkSetUUID());
			for (I_ExtendByRef extension : extensions) {
				if (workSetConcept.getConceptNid() == extension.getRefsetId()) {
					alreadyMember = true;
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					if (isInactive(part.getStatusNid())) {
						for (PathBI editPath : config.getEditingPathSet()) {
							I_ExtendByRefPartStr newStringPart = (I_ExtendByRefPartStr) part.makeAnalog(
									ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							extension.addVersion(newStringPart);
						}
						termFactory.addUncommittedNoChecks(workSetConcept);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(workSetConcept);
						//						termFactory.commit();
					}
				}
			}

			if (!alreadyMember) {
				//				String metadata = serialize(member);
				termFactory.getRefsetHelper(config).newRefsetExtension(
						workSetConcept.getConceptNid(), 
						newMemberConcept.getConceptNid(),
						EConcept.REFSET_TYPES.STR,
						new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""), config);
				for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workSetConcept.getConceptNid())) {
					if (extension.getComponentNid() == newMemberConcept.getConceptNid() &&
							extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
						termFactory.addUncommittedNoChecks(workSetConcept);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(workSetConcept);
						//						termFactory.commit();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static WorkList updateWorkListMetadata(WorkList workListWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkList workList = null;

		try {
			I_GetConceptData workListExtensionRefset = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());

			I_GetConceptData workListConcept = termFactory.getConcept(workListWithMetadata.getUids());

			WorkList currentVersionOfWorkList = getWorkList(
					workListWithMetadata.getConcept(), config);

			if (!currentVersionOfWorkList.getName().equals(workListWithMetadata.getName())) {
				updatePreferredTerm(workListWithMetadata.getConcept(), workListWithMetadata.getName(), config);
			}

			String metadata = serialize(workListWithMetadata);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					workListConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workListExtensionRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workListExtensionRefset);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(workListExtensionRefset);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
				}
			}
			waiting(1);
			workList = getWorkList(workListConcept, config);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workList;
	}

	public static WorkSet deleteWorkSetMembers(WorkSet workSetWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkSet workSet = null;

		try {
			I_GetConceptData workSetExtensionRefset = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.WORKSET_EXTENSION_REFSET.getUids());

			I_GetConceptData workSetConcept = termFactory.getConcept(workSetWithMetadata.getUids());

			String metadata = serialize(workSetWithMetadata);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					workSetConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workSetExtensionRefset.getConceptNid()) {
					Collection<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(
							config.getConflictResolutionStrategy());
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						extTuples.iterator().next().makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workSetExtensionRefset);
					termFactory.addUncommittedNoChecks(extension);
					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(workSetExtensionRefset);
//					termFactory.addUncommittedNoChecks(extension);
					termFactory.commit();
				}
			}
			workSet = getWorkSet(workSetConcept, config);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workSet;
	}

	public static void retireWorkSetMember(WorkSetMember workSetMember) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_GetConceptData worksetConcept = termFactory.getConcept(workSetMember.getWorkSetUUID());
			//WorkSet workSet = getWorkSet(worksetConcept, config);
			I_GetConceptData workSetMemberConcept = termFactory.getConcept(workSetMember.getId());

			String metadata = serialize(workSetMember);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					workSetMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == worksetConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(worksetConcept);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(worksetConcept);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
				}
			}

			//TODO: implement recursive retiring??

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}
	public static void retireWorkListMember(WorkListMember workListMember) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_GetConceptData workListConcept = termFactory.getConcept(workListMember.getWorkListUUID());
			I_GetConceptData workListMemberConcept = termFactory.getConcept(workListMember.getId());

			String metadata = serialize(workListMember);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					workListMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workListConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workListConcept);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(workListConcept);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}
	/**
	 * Update work set member metadata.
	 * 
	 * @param workSetMemberWithMetadata the work set member with metadata
	 * @param config the config
	 * 
	 * @return the work set member
	 */
	public static WorkSetMember updateWorkSetMemberMetadata(WorkSetMember workSetMemberWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkSetMember workSetMember = null;

		try {
			I_GetConceptData workSetConcept = termFactory.getConcept(workSetMemberWithMetadata.getWorkSetUUID());
			String metadata = serialize(workSetMemberWithMetadata);
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workSetMemberWithMetadata.getId());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workSetConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workSetConcept);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(workSetConcept);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
				}
			}
			waiting(1);
			workSetMember = getWorkSetMember(workSetMemberWithMetadata.getConcept(), workSetConcept.getConceptNid(), config);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workSetMember;
	}

	/**
	 * Gets the work set member.
	 * 
	 * @param workSetMemberConcept the work set member concept
	 * @param worksetId the workset id
	 * @param config the config
	 * 
	 * @return the work set member
	 */
	public static WorkSetMember getWorkSetMember(I_GetConceptData workSetMemberConcept, int worksetId, I_ConfigAceFrame config) {
		WorkSetMember workSetMember = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData workSetRefset = termFactory.getConcept(worksetId);

			termFactory.getAllExtensionsForComponent(workSetMemberConcept.getConceptNid());

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workSetMemberConcept.getConceptNid());//workSetMemberConcept.getExtensions();
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workSetRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					I_GetConceptData component = termFactory.getConcept(extension.getComponentNid());
					workSetMember = new WorkSetMember(component.toString(), component.getConceptNid(),
							component.getUids(), workSetRefset.getUids().iterator().next());
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workSetMember;
	}

	public static PartitionMember getPartitionMember(I_GetConceptData partitionMemberConcept, int partitionId, I_ConfigAceFrame config) {
		PartitionMember partitionMember = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData partitionRefset = termFactory.getConcept(partitionId);
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					partitionMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == partitionRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					I_GetConceptData component = termFactory.getConcept(extension.getComponentNid());
					partitionMember = new PartitionMember(component.toString(), 
							component.getConceptNid(),
							component.getUids(),
							partitionRefset.getUids().iterator().next());
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return partitionMember;
	}

	/**
	 * Gets the all work set members.
	 * 
	 * @param workset the workset
	 * @param config the config
	 * 
	 * @return the all work set members
	 */
	public static List<WorkSetMember> getAllWorkSetMembers(WorkSet workset, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkSetMember> workSetMembers = new ArrayList<WorkSetMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions  = 
				termFactory.getRefsetExtensionMembers(workset.getId());
			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					members.add(termFactory.getConcept(extension.getComponentNid()));
				}
			}
			for (I_GetConceptData member : members) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(member);
				if (isActive(lastAttributePart.getStatusNid())) {
					workSetMembers.add(getWorkSetMember(member, workset.getId(), config));
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workSetMembers;
	}

	public static List<PartitionMember> getAllPartitionMembers(Partition partition, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<PartitionMember> partitionMembers = new ArrayList<PartitionMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions  = 
				termFactory.getRefsetExtensionMembers(partition.getId());
			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					members.add(termFactory.getConcept(extension.getComponentNid()));
				}
			}
			for (I_GetConceptData member : members) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(member);
				if (isActive(lastAttributePart.getStatusNid())) {
					partitionMembers.add(getPartitionMember(member, partition.getId(), config));
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return partitionMembers;
	}

	//WorkLists CRUD **********************************

	/**
	 * Gets the all work lists for work set.
	 * 
	 * @param workSet the work set
	 * @param config the config
	 * 
	 * @return the all work lists for work set
	 */
	public static List<WorkList> getAllWorkListsForRefset(I_GetConceptData refset, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkList> workLists = new ArrayList<WorkList>();
		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			Set<? extends I_GetConceptData> origins = refset.getDestRelOrigins(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData origin : origins) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(origin);
				if (isActive(lastAttributePart.getStatusNid())) {
					WorkList workList = getWorkList(origin, config);
					workLists.add(workList);
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workLists;
	}

	public static List<WorkList> getAllWorklistForWorkset(WorkSet workset, I_ConfigAceFrame config){
		List<WorkList> result = new ArrayList<WorkList>();
		List<PartitionScheme> partitionSchemes = getAllPartitionSchemesForRefsetConcept(workset.getConcept(), config);
		for (PartitionScheme partitionScheme : partitionSchemes) {
			result.addAll(getAllPartitionsRecursive(partitionScheme, config));
			//			List<PartitionScheme> subPartitionSchemes = getAllPartitionSchemes(partitionScheme, config);
			//			for (PartitionScheme subPartitionScheme : subPartitionSchemes) {
			//				List<Partition> partitions = getAllPartitionsForScheme(subPartitionScheme, config);
			//				for (Partition partition : partitions) {
			//					List<WorkList> worklists = getAllWorkListsForRefset(partition.getConcept(), config);
			//					for (WorkList workList : worklists) {
			//						result.add(workList);
			//					}
			//				}
			//			}

		}
		return result;
	}

	private static List<WorkList> getAllPartitionsRecursive(PartitionScheme partitionScheme, I_ConfigAceFrame config){
		List<WorkList> result = new ArrayList<WorkList>();
		List<Partition> partitions = getAllPartitionsForScheme(partitionScheme, config);
		for (Partition partition : partitions) {
			List<WorkList> wls = getAllWorkListsForRefset(partition.getConcept(), config);
			for (WorkList workList : wls) {
				if(workList != null){
					result.add(workList);
				}
			}
			List<PartitionScheme> subPartitionSchemes = getAllPartitionSchemesForRefsetConcept(partition.getConcept(), config);
			for (PartitionScheme subPartitionScheme : subPartitionSchemes) {
				if(subPartitionScheme != null){
					result.addAll(getAllPartitionsRecursive(subPartitionScheme, config));
				}
			}
		}
		return result;
	}

	private static I_TerminologyProject getProjectFromParent(I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TerminologyProject project = null;
		try {
			//System.out.println(concept.toString());
			project = getTranslationProject(concept, config);
		} catch (Exception e1) {
			//do nothing
			//System.out.println(concept.toString());
		}
		if (project == null) {
			try {
				I_TermFactory termFactory = Terms.get();
				I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> parents = concept.getSourceRelTargets(
						config.getAllowedStatus(), 
						allowedDestRelTypes, config.getViewPositionSetReadOnly(),
						Precedence.TIME, config.getConflictResolutionStrategy());

				if (parents.size() > 1 ) {
					throw new Exception("Error: Wrong number of parents...");
				}

				if (parents.size() == 0 ) {
					allowedDestRelTypes =  termFactory.newIntSet();
					allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
					parents = concept.getSourceRelTargets(
							config.getAllowedStatus(), 
							allowedDestRelTypes, config.getViewPositionSetReadOnly(),
							Precedence.TIME, config.getConflictResolutionStrategy());
				}

				I_GetConceptData parent = parents.iterator().next();
				project = getProjectFromParent(parent, config);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return project;
	}

	public static I_TerminologyProject getProjectForWorklist(WorkList workList, I_ConfigAceFrame config) {

		return getProjectFromParent(workList.getConcept(), config);
	}

	/**
	 * Gets the work list.
	 * 
	 * @param workListConcept the work list concept
	 * @param config the config
	 * 
	 * @return the work list
	 */
	public static WorkList getWorkList(I_GetConceptData workListConcept, I_ConfigAceFrame config) {
		WorkList workList = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = workListConcept.getSourceRelTargets(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1 ) {
				throw new Exception("Error: Wrong number of parents in workList...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.localize().getNid()) {
				String name = workListConcept.toString();
				List<? extends I_DescriptionTuple> descTuples = workListConcept.getDescriptionTuples(
						config.getAllowedStatus(), 
						(config.getDescTypes().getSetValues().length == 0)?null:config.getDescTypes(), 
								config.getViewPositionSetReadOnly(), 
								Precedence.TIME, config.getConflictResolutionStrategy());

				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}

				I_GetConceptData workListRefset = termFactory.getConcept(
						ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());
				allowedDestRelTypes =  termFactory.newIntSet();
				allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
				I_IntSet descriptionTypes =  termFactory.newIntSet();
				descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
				WorkList deserializedWorkListWithMetadata = null;
				for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workListRefset.getConceptNid())) {
					if (extension.getComponentNid() == workListConcept.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
						String metadata = part.getStringValue();
						deserializedWorkListWithMetadata = (WorkList) deserialize(metadata);
					}
				}
				if (deserializedWorkListWithMetadata != null) {
					workList = new WorkList(name, workListConcept.getConceptNid(), 
							workListConcept.getUids(),
							deserializedWorkListWithMetadata.getPartitionUUID(), 
							deserializedWorkListWithMetadata.getDestination(), deserializedWorkListWithMetadata.getBusinessProcess());
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return workList;
	}

	public static WorkSet getNonAssignedChangesWorkSet(I_TerminologyProject project, I_ConfigAceFrame config) {
		WorkSet nacWorkSet = null;
		String nacWorkSetName = "Maintenance - " + project.getName().replace("(translation project)", "");

		for (WorkSet loopWorkSet : project.getWorkSets(config)) {
			if (loopWorkSet.getName().startsWith(nacWorkSetName)) {
				nacWorkSet = loopWorkSet;
			}
		}

		return nacWorkSet;
	}

	public static WorkList createNewNacWorkList(String name, BusinessProcess bp, 
			I_TerminologyProject project, I_ConfigAceFrame config) throws TerminologyException, IOException {
		WorkList newNacWorkList = null;
		WorkSet nacWorkSet = getNonAssignedChangesWorkSet(project, config);

		Partition nacWorkListPartition = createNewPartition(name, 
				nacWorkSet.getPartitionSchemes(config).iterator().next().getUids().iterator().next(), config);

		newNacWorkList = new WorkList(name,
				0, null, nacWorkListPartition.getUids().iterator().next(), "no destination", bp);

		WorkList returnWorkList = createNewWorkList(newNacWorkList, config);

		return returnWorkList;
	}

	public static List<WorkList> getAllNacWorkLists(I_TerminologyProject project, I_ConfigAceFrame config) {
		List<WorkList> nacWorkLists = new ArrayList<WorkList>();
		WorkSet nacWorkSet = getNonAssignedChangesWorkSet(project, config);
		if (nacWorkSet != null && !nacWorkSet.getPartitionSchemes(config).isEmpty()) {
			for (Partition loopPartition : nacWorkSet.getPartitionSchemes(config).iterator().next().getPartitions()) {
				nacWorkLists.addAll(loopPartition.getWorkLists());
			}
		}
		return nacWorkLists;
	}

	public static WorkListMember addConceptAsNacWorklistMember(WorkList workList,
			I_GetConceptData concept, String destination, I_ConfigAceFrame config) throws IOException {
		WorkSet nacWorkSet = getNonAssignedChangesWorkSet(getProjectForWorklist(workList, config), config);
		addConceptAsWorkSetMember(concept, nacWorkSet.getUids().iterator().next(), config);
		addConceptAsPartitionMember(concept, workList.getPartitionUUID(), config);
		WorkListMember workListMember = new WorkListMember(concept.toString(), 
				concept.getConceptNid(),
				concept.getUids(), workList.getUids().iterator().next(), destination, 
				ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next(),new java.util.Date().getTime() );
		addConceptAsWorkListMember(workListMember, config);
		try {
			Terms.get().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getWorkListMember(concept, workList.getId(), config);
	}

	/**
	 * Creates the new work list.
	 * 
	 * @param workListWithMetadata the work list with metadata
	 * @param config the config
	 * 
	 * @return the work list
	 */
	public static WorkList createNewWorkList(WorkList workListWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkList workList = null;
		I_GetConceptData newConcept = null;
		String name = workListWithMetadata.getName();
		workListWithMetadata.getBusinessProcess().setDestination(
				workListWithMetadata.getDestination());

		String worklistName = workListWithMetadata.getName() + " (worklist)";

		try {
			if(isConceptDuplicate(worklistName)){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated worklist name", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Worklist with the same name allready exists.");
			}
			I_GetConceptData workListsRoot = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.getUids());

			I_GetConceptData workListRefset = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData commentsRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData promotionRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData purposeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData defining = termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids());
			I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
			I_GetConceptData current = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());


			I_GetConceptData partition = termFactory.getConcept(workListWithMetadata.getPartitionUUID());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", worklistName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", worklistName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					workListsRoot, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					includesFromAttribute, 
					partition, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			I_GetConceptData newCommentsConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en",
					name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newCommentsConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), workListsRoot, defining, refinability, 
					current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, commentsRelConcept, newCommentsConcept, defining, refinability, 
					current, 0, config);

			I_GetConceptData newPromotionConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en",
					name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newPromotionConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), workListsRoot, defining, refinability, 
					current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, promotionRelConcept, newPromotionConcept, defining, refinability, 
					current, 0, config);

			workList = new WorkList(workListWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(),
					workListWithMetadata.getPartitionUUID(), 
					workListWithMetadata.getDestination(), workListWithMetadata.getBusinessProcess());

			String metadata = serialize(workList);

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.addUncommittedNoChecks(newCommentsConcept);
			termFactory.addUncommittedNoChecks(newPromotionConcept);
			termFactory.commit();
//			promote(newConcept, config);
//			promote(newCommentsConcept, config);
//			promote(newPromotionConcept, config);
//			termFactory.addUncommittedNoChecks(newConcept);
//			termFactory.addUncommittedNoChecks(newCommentsConcept);
//			termFactory.addUncommittedNoChecks(newPromotionConcept);
//			termFactory.commit();

			termFactory.getRefsetHelper(config).newRefsetExtension(workListRefset.getConceptNid(), 
					newConcept.getConceptNid(), EConcept.REFSET_TYPES.STR, 
					new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, metadata), config);

			for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workListRefset.getConceptNid())) {
				if (extension.getComponentNid() == newConcept.getConceptNid() &&
						extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
					termFactory.addUncommittedNoChecks(workListRefset);
					termFactory.addUncommittedNoChecks(extension);
					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(workListRefset);
//					termFactory.addUncommittedNoChecks(extension);
//					termFactory.commit();
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workList;
	}

	public static Partition createNewPartition(String name, UUID partitionSchemeUUID, 
			I_ConfigAceFrame config) {
		Partition newPartition = new Partition(name, 0, null, partitionSchemeUUID);
		return createNewPartition(newPartition, config);
	}

	public static Partition combinePartitions(List<Partition> partitions, 
			String name, I_ConfigAceFrame config) throws Exception {

		PartitionScheme partitionScheme = partitions.get(0).getPartitionScheme(config);

		if (partitions.size() < 2) {
			throw new Exception("At least two partitions are required to combine.");
		}

		for (Partition loopPartition : partitions) {
			if (loopPartition.getPartitionScheme(config).getId() != partitionScheme.getId()) {
				throw new Exception("All partitions must belong to the same scheme.");
			}

			if (loopPartition.getWorkLists().size() > 0) {
				throw new Exception("Partitions to combine should have no associated worlist, delete worlists first.");
			}

			if (loopPartition.getSubPartitionSchemes(config).size() > 0) {
				throw new Exception("Partitions to combine should have no sub-partition schemes.");
			}

		}

		Partition newPartition = TerminologyProjectDAO.createNewPartition(name, 
				partitionScheme.getUids().iterator().next(), config);

		for (Partition loopPartition : partitions) {
			for (PartitionMember loopMember : loopPartition.getPartitionMembers()) {
				addConceptAsPartitionMember(loopMember.getConcept(), 
						newPartition.getUids().iterator().next(), config);
			}

		}

		for (Partition loopPartition : partitions) {
			retirePartition(loopPartition, config);
		}

		List<PartitionMember> members = newPartition.getPartitionMembers();
		if (members.size()==0) {
			//sas
		}
		return newPartition;
	}

	private static boolean isConceptDuplicate(String description) {
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			SearchResult results = tf.doLuceneSearch(description);
			for (int i = 0 ; i <results.topDocs.scoreDocs.length  ; i++) {
				try{
					Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					//System.out.println(doc);
					I_DescriptionVersioned<?> foundDescription = tf.getDescription(dnid);
					if (foundDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getText().equals(description)) {
						result = true;
					}
				}catch(Exception e){
					//Do Nothing
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Partition createNewPartitionAndMembersFromWorkSet(String name, 
			WorkSet workSet, I_ConfigAceFrame config) throws TerminologyException, IOException {

		Partition newPartition = null;
		try{
			if(isConceptDuplicate(name  + " (partition)")){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated partition name", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Partition name allready exists.");
			}
			PartitionScheme newPartitionScheme = createNewPartitionScheme(name,
					workSet.getUids().iterator().next(), config);
			newPartition = TerminologyProjectDAO.createNewPartition(name, 
					newPartitionScheme.getUids().iterator().next(), config);
			for (WorkSetMember loopMember : workSet.getWorkSetMembers()) {
				TerminologyProjectDAO.addConceptAsPartitionMember(loopMember.getConcept(), 
						newPartition.getUids().iterator().next(), config);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return newPartition;

	}
	private static Partition createNewPartition(Partition partitionWithMetadata, 
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		Partition partition = null;
		I_GetConceptData newConcept = null;

		String partitionName = partitionWithMetadata.getName() + " (partition)";


		try {
			if(isConceptDuplicate(partitionName)){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated partition name", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Partition name allready exists.");
			}
			I_GetConceptData parentConcept = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.getUids());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData partitionScheme = termFactory.getConcept(partitionWithMetadata.getPartitionSchemeUUID());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);


			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					parentConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					includesFromAttribute, 
					partitionScheme, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			partition = new Partition(partitionWithMetadata.getName(), newConcept.getConceptNid(), 
					newConcept.getUids(), partitionWithMetadata.getPartitionSchemeUUID());

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.commit();
//			promote(newConcept, config);
//			termFactory.addUncommittedNoChecks(newConcept);
//			termFactory.commit();

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return partition;
	}

	public static PartitionScheme createNewPartitionScheme(String name, UUID sourceRefsetUUID,
			I_ConfigAceFrame config) {
		PartitionScheme newPartitionScheme = new PartitionScheme(name, 0, null, sourceRefsetUUID);
		return createNewPartitionScheme(newPartitionScheme, config);
	}

	private static PartitionScheme createNewPartitionScheme(PartitionScheme partitionSchemeWithMetadata, 
			I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		PartitionScheme partitionScheme = null;
		I_GetConceptData newConcept = null;
		String partitionSchemeName = partitionSchemeWithMetadata.getName() + " (partition scheme)";
		try {
			if(isConceptDuplicate(partitionSchemeName)){
				JOptionPane.showMessageDialog(new JDialog(), "Duplicated partition sheme name", "Error", JOptionPane.WARNING_MESSAGE);
				throw new Exception("Partition scheme name allready exists.");
			}

			I_GetConceptData worksetConcept = termFactory.getConcept(partitionSchemeWithMetadata.getSourceRefsetUUID());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(
					ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData workSet = termFactory.getConcept(partitionSchemeWithMetadata.getSourceRefsetUUID());

			WorkSet parentWorkSet = getWorkSet(workSet, config);
			if (!parentWorkSet.getName().startsWith("Maintenance") && getAllWorkSetMembers(parentWorkSet, config).isEmpty()) {
				JOptionPane.showMessageDialog(new JDialog(), "WorkSet empty, please specify source refset and Sync.", 
						"Error", JOptionPane.WARNING_MESSAGE);
				throw new Exception("WorkSet empty.");
			}

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionSchemeName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionSchemeName,
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()),
					config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
					worksetConcept, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, 
					includesFromAttribute, 
					workSet, 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
					0, config);

			partitionScheme = new PartitionScheme(partitionSchemeWithMetadata.getName(), newConcept.getConceptNid(), 
					newConcept.getUids(), partitionSchemeWithMetadata.getSourceRefsetUUID());

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.commit();
//			promote(newConcept, config);
//			termFactory.addUncommittedNoChecks(newConcept);
//			termFactory.commit();

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return partitionScheme;
	}

	/**
	 * Adds the concept as work list member.
	 * 
	 * @param member the member
	 * @param config the config
	 */
	public static void addConceptAsWorkListMember(WorkListMember member, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = termFactory.getConcept(member.getUids());
			I_GetConceptData workListConcept = termFactory.getConcept(member.getWorkListUUID());
			//			member.getBusinessProcessWithAttachments().writeAttachment(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey(), member);
			//			member.getBusinessProcessWithAttachments().setSubject(member.getConcept().toString());

			//String metadata = serialize(member);

			boolean alreadyMember = false;
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(newMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				I_GetConceptData refset = termFactory.getConcept(extension.getRefsetId());
				if (refset.getUids().contains(member.getWorkListUUID())) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					if (isActive(part.getStatusNid())) {
						alreadyMember = true;
					} else if (isInactive(part.getStatusNid())) {
						for (PathBI editPath : config.getEditingPathSet()) {
							part.makeAnalog(
									ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							//part.setStringValue(metadata); //Removed to minimize changeset footprint
							extension.addVersion(part);
						}
						termFactory.addUncommittedNoChecks(workListConcept);
						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(workListConcept);
//						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
					}
				}
			}

			if (!alreadyMember) {
				WorkList workList = getWorkList(workListConcept, config);
				PromotionRefset promotionRefset = workList.getPromotionRefset(config);
				termFactory.getRefsetHelper(config).newRefsetExtension(
						workListConcept.getConceptNid(), 
						newMemberConcept.getConceptNid(), 
						EConcept.REFSET_TYPES.STR, 
						new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""), //metadata Removed to minimize changeset footprint
						config); 
				for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workListConcept.getConceptNid())) {
					if (extension.getComponentNid() == newMemberConcept.getConceptNid() &&
							extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
						termFactory.addUncommittedNoChecks(workListConcept);
						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(workListConcept);
//						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
					}
				}
				I_GetConceptData activityStatusConcept = termFactory.getConcept(member.getActivityStatus());
				promotionRefset.setPromotionStatus(member.getId(), activityStatusConcept.getConceptNid());
				//Translation specific concept level promotion refset
				TranslationProject transProject = (TranslationProject) getProjectForWorklist(workList, config);
				LanguageMembershipRefset targetLanguage = new LanguageMembershipRefset(
						transProject.getTargetLanguageRefset(), config);
				PromotionRefset languagePromotionRefset = targetLanguage.getPromotionRefset(config);
				languagePromotionRefset.setPromotionStatus(member.getId(), activityStatusConcept.getConceptNid());
				// end	
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addConceptAsPartitionMember(I_GetConceptData partitionMemberConcept, 
			UUID partitionUUID, I_ConfigAceFrame config) {
		try {
			PartitionMember partitionMember = new PartitionMember(partitionMemberConcept.toString(), 
					partitionMemberConcept.getConceptNid(),
					partitionMemberConcept.getUids(),
					partitionUUID);
			addConceptAsPartitionMember(partitionMember, config);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public static void addConceptAsPartitionMember(PartitionMember member, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = termFactory.getConcept(member.getUids());
			I_GetConceptData partitionConcept = termFactory.getConcept(member.getPartitionUUID());

			boolean alreadyMember = false;
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(newMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				I_GetConceptData refset = termFactory.getConcept(extension.getRefsetId());
				if (refset.getUids().contains(member.getPartitionUUID())) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					if (isActive(part.getStatusNid())) {
						alreadyMember = true;
					} else if (isInactive(part.getStatusNid())) {
						for (PathBI editPath : config.getEditingPathSet()) {
							part.makeAnalog(
									ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
									editPath.getConceptNid(),
									Long.MAX_VALUE);
							extension.addVersion(part);
						}
						termFactory.addUncommittedNoChecks(partitionConcept);
						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(partitionConcept);
//						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
					}
				}
			}

			if (!alreadyMember) {
				termFactory.getRefsetHelper(config).newRefsetExtension(
						partitionConcept.getConceptNid(), 
						newMemberConcept.getConceptNid(), 
						EConcept.REFSET_TYPES.STR, 
						new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""),
						config); 
				for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(partitionConcept.getConceptNid())) {
					if (extension.getComponentNid() == newMemberConcept.getConceptNid() &&
							extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
						termFactory.addUncommittedNoChecks(partitionConcept);
						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
//						promote(extension, config);
//						termFactory.addUncommittedNoChecks(partitionConcept);
//						termFactory.addUncommittedNoChecks(extension);
						//						termFactory.commit();
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Execute worklist business process.
	 * 
	 * @param worklist the worklist
	 * @param worker the worker
	 * @param config the config
	 * 
	 * @throws TaskFailedException the task failed exception
	 * 
	 * @deprecated use deliverWorklistBusinessProcessToOutbox instead
	 */
	@SuppressWarnings("unchecked")
	public static void executeWorklistBusinessProcess(WorkList worklist, I_Work worker,I_ConfigAceFrame config,I_EncodeBusinessProcess processToLunch) throws TaskFailedException {

		List<WorkListMember> workListMembers=getAllWorkListMembers(worklist, config);

		Stack ps=worker.getProcessStack();
		for (WorkListMember workListMember : workListMembers) {

			worker.setProcessStack(new Stack());
			//			BusinessProcess processToLunch= workListMember.getBusinessProcessWithAttachments();
			processToLunch.execute(worker);
		}
		worker.setProcessStack(ps);
	}

	public static void deliverWorklistBusinessProcessToOutbox(WorkList worklist, I_Work worker) throws TerminologyException, IOException, TaskFailedException, UnknownTransactionException, CannotCommitException, LeaseDeniedException, InterruptedException, PrivilegedActionException {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		List<WorkListMember> workListMembers=getAllWorkListMembers(worklist, config);
		boolean bSent=false;
		ServiceID serviceID = null;
		Class<?>[] serviceTypes = new Class[] { I_QueueProcesses.class };
		String queueName = config.getUsername().trim() + ".outbox";
		Entry[] attrSetTemplates = new Entry[] { new Name(queueName) };
		ServiceTemplate template = new ServiceTemplate(serviceID, serviceTypes, attrSetTemplates);
		ServiceItemFilter filter = null;
		ServiceItem service = null;
		try {
			service = worker.lookup(template, filter);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		if (service == null) {
			throw new TaskFailedException("No queue with the specified name could be found: "
					+ "OUTBOX");
		}
		I_QueueProcesses q = (I_QueueProcesses) service.service;
		I_EncodeBusinessProcess process=(I_EncodeBusinessProcess)worklist.getBusinessProcess();
		String destination=worklist.getDestination();
		process.setDestination(destination);
		I_TerminologyProject project = getProjectForWorklist(worklist, config);
		List<I_GetConceptData> souLanRefsets = ((TranslationProject)project).getSourceLanguageRefsets();
		Integer langRefset = null;
		for (I_GetConceptData lCon:souLanRefsets){
			if (lCon.getConceptNid()==enRefset.getConceptNid()){
				langRefset=lCon.getConceptNid();
				break;
			}
		}
		if (langRefset==null && souLanRefsets!=null){
			langRefset=souLanRefsets.get(0).getConceptNid();
		}
		Long statusTime=new java.util.Date().getTime();
		int statusId = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids()).getConceptNid();
		PromotionRefset promoRefset = worklist.getPromotionRefset(config);
		for (WorkListMember workListMember : workListMembers) {
			if (ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().contains(workListMember.getActivityStatus())) {
				//				I_EncodeBusinessProcess process= workListMember.getBusinessProcessWithAttachments();
				try {
					if (workListMember.getDestination() != null && !workListMember.getDestination().isEmpty()) {
						process.setDestination(workListMember.getDestination());
					}
					workListMember.setActivityStatus(
							ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_DELIVERED_STATUS.getUids().iterator().next());
					process.writeAttachment(ProcessAttachmentKeys.WORKLIST_MEMBER.getAttachmentKey(), workListMember);
					updateWorkListMemberMetadata(workListMember, config);
					//					Due commit is missing take date from variable
					//					statusTime=promoRefset.getLastStatusTime(workListMember.getId(), config);
					String subj=getItemSubject(workListMember,worklist,project,promoRefset,langRefset,statusId,statusTime);

					process.setSubject(subj);
					process.setProcessID(new ProcessID(UUID.randomUUID()));
					worker.getLogger().info(
							"Moving process " + process.getProcessID() + " to Queue named: " + queueName);
					q.write(process, worker.getActiveTransaction());
					bSent = true;
					worker.getLogger()
					.info("Moved process " + process.getProcessID() + " to queue: " + q.getNodeInboxAddress());
					updateWorkListMemberMetadata(workListMember, config);
					//TODO: move to a more generic promotion, not language specific
//					promoteLanguageContent(workListMember, config);
				} catch (Exception e) {
					throw new TaskFailedException(e);
				}
			}
		}
		if (bSent){
			worker.getActiveTransaction().commit();
		}
		try {
			Terms.get().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String subjectSeparator = "-@-";

	public enum subjectIndexes{WORKLIST_MEMBER_ID,WORKLIST_MEMBER_SOURCE_NAME,WORKLIST_ID,WORKLIST_NAME,PROJECT_ID,WORKLIST_MEMBER_SOURCE_PREF,PROMO_REFSET_ID,TAGS_ARRAY,STATUS_ID,STATUS_TIME};

	public static String getItemSubject(WorkListMember workListMember,
			WorkList worklist, I_TerminologyProject project, PromotionRefset promotionRefset, Integer langRefset, int statusId, Long statusTime) {

		String[] terms=getSourceTerms(workListMember.getId(), langRefset);
		String subject=workListMember.getId()  + subjectSeparator + terms[0] +
		subjectSeparator + worklist.getId() + subjectSeparator + worklist.getName() + 
		subjectSeparator + project.getId() + subjectSeparator + terms[1] + 
		subjectSeparator + promotionRefset.getRefsetId() + 
		subjectSeparator + subjectSeparator + statusId +
		subjectSeparator + statusTime;

		return subject;
	}

	private static I_GetConceptData fsn;
	private static I_GetConceptData preferred;
	private static I_GetConceptData notAcceptable;
	private static I_GetConceptData inactive;
	private static I_GetConceptData retired;
	private static I_GetConceptData enRefset;
	static {

		try {
			fsn = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			preferred =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());

			notAcceptable =  Terms.get().getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
			inactive =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.INACTIVE.getUids());
			retired =Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
			enRefset=Terms.get().getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static String[] getSourceTerms(Integer worklistmemberId,
			Integer sourceLang) {
		String [] retString={"",""};
		String sFsn="";
		String sPref="";
		if (sourceLang!=null){

			List<ContextualizedDescription> descriptions;
			try {
				descriptions = ContextualizedDescription.getContextualizedDescriptions(
						worklistmemberId, sourceLang, true);

				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension()!=null && description.getLanguageRefsetId()==sourceLang){

						if (!(description.getAcceptabilityId()==notAcceptable.getConceptNid() ||
								description.getExtensionStatusId()==inactive.getConceptNid() ||
								description.getDescriptionStatusId()==retired.getConceptNid())){

							if (description.getTypeId() == fsn.getConceptNid() ) {
								sFsn= description.getText();
								if (!sPref.equals("")){
									break;
								}
							}else{
								if ( description.getAcceptabilityId() == preferred.getConceptNid() ) {
									sPref=description.getText();
									if (!sFsn.equals("")){
										break;
									}
								}
							}
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
			retString[0]=sFsn;
			retString[1]=sPref;
		}

		return retString;

	}

	public static String[] getParsedItemSubject(String subject){
		if (subject==null){
			return new String[]{""};
		}
		return subject.split(subjectSeparator,-1);
	}


	public static List<Integer> getSourceLanguageRefsetIdsForProjectId(
			int projectId, 
			I_ConfigAceFrame config) {
		ArrayList<Integer> returnData = new ArrayList<Integer>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());

			List<? extends I_RelTuple> sourceLanguageRefsetsTuples = termFactory.getConcept(projectId).getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			sourceLanguageRefsetsTuples = cleanRelTuplesList(sourceLanguageRefsetsTuples);

			if (sourceLanguageRefsetsTuples != null) {
				for (I_RelTuple loopTuple : sourceLanguageRefsetsTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(loopTuple.getC2Id());
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

	public static String getSubjectFromArray(String[] parsedSubj){
		StringBuffer ret=new StringBuffer("");
		if (parsedSubj.length==TerminologyProjectDAO.subjectIndexes.values().length){
			for(int i =0;i<parsedSubj.length;i++ ){
				ret.append(parsedSubj[i]);
				if (i<parsedSubj.length-1)
					ret.append(subjectSeparator);
			}
		}
		return ret.toString();
	}

	public static Integer getPromotionStatusIdForRefsetId(int refsetId, int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {

		I_TermFactory termFactory = Terms.get();
		List<? extends I_ExtendByRef> members = termFactory.getAllExtensionsForComponent(componentId);
		for (I_ExtendByRef promotionMember : members) {
			if (promotionMember.getRefsetId() == refsetId) {
				List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(config.getAllowedStatus(), 
						config.getViewPositionSetReadOnly(), 
						Precedence.TIME, 
						config.getConflictResolutionStrategy());
				if (tuples != null && !tuples.isEmpty()) {
					I_ExtendByRefVersion lastTuple = null;
					for (I_ExtendByRefVersion loopTuple : tuples) {
						if (lastTuple == null || lastTuple.getTime() < loopTuple.getTime()) {
							lastTuple = loopTuple;
						}
					}
					I_ExtendByRefPartCid promotionExtensionPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
					return promotionExtensionPart.getC1id();
				}
			}
		}
		return null;
	}

	public static Integer getTargetLanguageRefsetIdForProjectId(
			int projectId, 
			I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
			targetRefsetsTuples = termFactory.getConcept(projectId).getSourceRelTuples(
					config.getAllowedStatus(), 
					allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			e.printStackTrace();
		}

		for (I_RelTuple rel : targetRefsetsTuples) {
			if (isActive(rel.getStatusNid())) {
				targetRefsets.add(rel);
			}
		}

		if (targetRefsets.size() == 0) {
			return null;
		} else if (targetRefsets.size() == 1) {
			return targetRefsets.iterator().next().getC2Id();
		} else throw new Exception("Wrong number of target refsets.");
	}

	/**
	 * Gets the work list member.
	 * 
	 * @param workListMemberConcept the work list member concept
	 * @param workListId the work list id
	 * @param config the config
	 * 
	 * @return the work list member
	 */
	public static WorkListMember getWorkListMember(I_GetConceptData workListMemberConcept, int workListId, I_ConfigAceFrame config) {
		WorkListMember workListMember = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData workListRefset = termFactory.getConcept(workListId);

			I_IntSet descriptionTypes =  termFactory.newIntSet();
			descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

			List<? extends I_DescriptionTuple> descTuples = workListMemberConcept.getDescriptionTuples(
					config.getAllowedStatus(), 
					descriptionTypes, config.getViewPositionSetReadOnly(),
					Precedence.TIME, config.getConflictResolutionStrategy());
			String name;
			if (!descTuples.isEmpty()) {
				name = descTuples.iterator().next().getText();
			} else {
				name = "No FSN!";
			}
			//WorkListMember deserializedWorkListMemberWithMetadata = null;
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					workListMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workListRefset.getConceptNid()) {
					WorkList workList = getWorkList(workListRefset, config);
					PromotionRefset promotionRefset = workList.getPromotionRefset(config);
					I_GetConceptData status = promotionRefset.getPromotionStatus(workListMemberConcept.getConceptNid(), 
							config);
					Long statusDate = promotionRefset.getLastStatusTime(workListMemberConcept.getConceptNid(), 
							config);
					workListMember = new WorkListMember(name, workListMemberConcept.getConceptNid(), 
							workListMemberConcept.getUids(), workList.getUids().iterator().next(), 
							workList.getDestination(), status.getUids().iterator().next(),statusDate);
					//					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					//					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					//					String metadata = part.getStringValue();
					//					deserializedWorkListMemberWithMetadata = (WorkListMember) deserialize(metadata);
				}
			}
			//			if (deserializedWorkListMemberWithMetadata != null) {
			//				workListMember = deserializedWorkListMemberWithMetadata;
			//				workListMember.setName(name);
			//				WorkList workList = getWorkList(workListRefset, config);
			//				PromotionRefset promotionRefset = workList.getPromotionRefset(config);
			//				I_GetConceptData status = promotionRefset.getPromotionStatus(workListMember.getId(), config);
			//				workListMember.setActivityStatus(status.getUids().iterator().next());
			//
			//			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workListMember;
	}

	/**
	 * Gets the all work list members.
	 * 
	 * @param workList the work list
	 * @param config the config
	 * 
	 * @return the all work list members
	 */
	public static List<WorkListMember> getAllWorkListMembers(WorkList workList, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkListMember> workListMembers = new ArrayList<WorkListMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions  = 
				termFactory.getRefsetExtensionMembers(workList.getId());
			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					members.add(termFactory.getConcept(extension.getComponentNid()));
				}
			}
			for (I_GetConceptData member : members) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(member);
				if (isActive(lastAttributePart.getStatusNid())) {
					workListMembers.add(getWorkListMember(member, workList.getId(), config));
				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return workListMembers;
	}

	public static List<WorkListMember> getWorkListMembersWithStatus(WorkList workList, I_GetConceptData activityStatus,
			I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_TermFactory termFactory = Terms.get();
		List<WorkListMember> workListMembers = new ArrayList<WorkListMember>();

		List<WorkListMember> members = getAllWorkListMembers(workList, config);

		for (WorkListMember loopMember : members) {
			I_GetConceptData loopActivityStatus = termFactory.getConcept(loopMember.getActivityStatus());
			if (activityStatus.getConceptNid() == loopActivityStatus.getConceptNid()) {
				workListMembers.add(loopMember);
			}
		}

		return workListMembers;
	}

	public static HashMap<I_GetConceptData, Integer> getWorkListMemberStatuses(WorkList workList, 
			I_ConfigAceFrame config) throws TerminologyException, IOException {
		HashMap<I_GetConceptData, Integer> workListMembersStatuses = new HashMap<I_GetConceptData, Integer>();
		I_TermFactory tf = Terms.get();

		List<WorkListMember> members = getAllWorkListMembers(workList, config);

		for (WorkListMember loopMember : members) {
			I_GetConceptData activityStatus = tf.getConcept(loopMember.getActivityStatus());
			Integer currentCount = workListMembersStatuses.get(activityStatus);
			if (currentCount == null) currentCount = 0;
			workListMembersStatuses.put(activityStatus, currentCount + 1);
		}

		return workListMembersStatuses;
	}

	/**
	 * Update component metadata.
	 * 
	 * @param component the component
	 * @param objectWithMetadata the object with metadata
	 * @param refsetUUIDs the refset uui ds
	 * @param config the config
	 */
	public static void updateComponentMetadata(I_GetConceptData component, Object objectWithMetadata, 
			UUID[] refsetUUIDs, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_GetConceptData componentsRefset = termFactory.getConcept(refsetUUIDs);

			I_GetConceptData componentConcept = termFactory.getConcept(component.getUids());

			String metadata = serialize(objectWithMetadata);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					componentConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == componentsRefset.getConceptNid()) {
					Collection<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(
							config.getConflictResolutionStrategy());
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						extTuples.iterator().next().makeAnalog(
								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(componentsRefset);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(componentsRefset);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
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

	/**
	 * Update worklist and members.
	 * 
	 * @param worklist the worklist
	 * @param config the config
	 */
	public static void updateWorklistAndMembers(WorkList worklist, I_ConfigAceFrame config){
		try {
			I_GetConceptData worklistConcept = worklist.getConcept();
			updateComponentMetadata(worklistConcept, worklist, 
					new UUID[]{ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids().iterator().next()},
					config);
			List<WorkListMember> workListMembers=getAllWorkListMembers(worklist, config);
			for (WorkListMember workListMember:workListMembers){
				workListMember.setDestination(worklist.getDestination());
				updateComponentMetadata(workListMember.getConcept(), workListMember, 
						(UUID[]) worklist.getUids().toArray(),
						config);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update worklist member metadata.
	 * 
	 * @param workListMemberWithMetadata the work list member with metadata
	 * @param config the config
	 * 
	 * @return the work list member
	 */
	public static WorkListMember updateWorkListMemberMetadata(WorkListMember workListMemberWithMetadata, I_ConfigAceFrame config){
		I_TermFactory termFactory = Terms.get();
		WorkListMember workListMember = null;

		try {
			I_GetConceptData workListConcept = termFactory.getConcept(workListMemberWithMetadata.getWorkListUUID());
			//			String metadata = serialize(workListMemberWithMetadata);
			//
			//			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workListMemberWithMetadata.getId());
			//			for (I_ExtendByRef extension : extensions) {
			//				if (workListConcept.getConceptId() == extension.getRefsetId()) {
			//					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
			//					for (PathBI editPath : config.getEditingPathSet()) {
			//						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
			//						lastPart.makeAnalog(
			//								ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
			//								editPath.getConceptId(),
			//								Long.MAX_VALUE);
			//						part.setStringValue(metadata);
			//						extension.addVersion(part);
			//					}
			//					termFactory.addUncommittedNoChecks(extension);
			//					promote(extension, config);
			//					//					termFactory.addUncommitted(extension);
			//					//					termFactory.commit();
			//				}
			//			}
			//			waiting(1);
			WorkList workList = getWorkList(workListConcept, config);
			PromotionRefset promotionRefset = workList.getPromotionRefset(config);
			I_GetConceptData activityStatusConcept = termFactory.getConcept(workListMemberWithMetadata.getActivityStatus());
			promotionRefset.setPromotionStatus(workListMemberWithMetadata.getId(), activityStatusConcept.getConceptNid());

			//Translation specific concept level promotion refset
			TranslationProject transProject = (TranslationProject) getProjectForWorklist(workList, config);
			LanguageMembershipRefset targetLanguage = new LanguageMembershipRefset(
					transProject.getTargetLanguageRefset(), config);
			PromotionRefset languagePromotionRefset = targetLanguage.getPromotionRefset(config);
			languagePromotionRefset.setPromotionStatus(workListMemberWithMetadata.getId(), activityStatusConcept.getConceptNid());
			// end	

			workListMember = getWorkListMember(workListMemberWithMetadata.getConcept(), workListConcept.getConceptNid(), config);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return workListMember;
	}

	/**
	 * Retire concept.
	 * 
	 * @param conceptToRetire the concept to retire
	 * @param config the config
	 * 
	 * @return the i_ get concept data
	 * @throws Exception 
	 */

	public static void retireProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		if (!project.getWorkSets(config).isEmpty()) {
			throw new Exception("Not empty!, delete worksets first...");
		} else {
			retireConcept(project.getConcept(), config);
		}
	}

	public static void retireWorkSet(WorkSet workSet, I_ConfigAceFrame config) throws Exception {
		if (!workSet.getPartitionSchemes(config).isEmpty()) {
			throw new Exception("Not empty!, delete partition schemes first...");
		} else {
			for (WorkSetMember member : workSet.getWorkSetMembers()) {
				retireWorkSetMember(member);
			}
			retireConcept(workSet.getConcept(), config);
		}
	}

	public static void retireWorkList(WorkList workList, I_ConfigAceFrame config) throws Exception {
		for (WorkListMember member : workList.getWorkListMembers()) {
			if (!ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().contains(member.getActivityStatus()) &&
					!ArchitectonicAuxiliary.Concept.RETIRED.getUids().contains(member.getActivityStatus())) {
				throw new Exception("WorkList cannot be retired, some members have been delivered and are still active.");
			}
		}

		for (WorkListMember member : workList.getWorkListMembers()) {
			retireWorkListMember(member);
		}
		retireConcept(workList.getConcept(), config);
	}

	public static void retirePartition(Partition partition, I_ConfigAceFrame config) throws Exception {
		if (!partition.getWorkLists().isEmpty()) {
			throw new Exception("Not empty!, delete worklists first...");
		} else {
			for (PartitionMember member : partition.getPartitionMembers()) {
				retirePartitionMember(member, config);
			}
			retireConcept(partition.getConcept(), config);
		}
	}

	public static void retirePartitionScheme(PartitionScheme partitionScheme, I_ConfigAceFrame config) throws Exception {
		if (!partitionScheme.getPartitions().isEmpty()) {
			throw new Exception("Not empty!, delete partitions first...");
		} else {
			retireConcept(partitionScheme.getConcept(), config);
		}
	}

	public static void retirePartitionMember(PartitionMember partitionMember, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_TermFactory termFactory = Terms.get();
		try {
			I_GetConceptData partitionConcept = termFactory.getConcept(partitionMember.getPartitionUUID());
			I_GetConceptData partitionMemberConcept = partitionMember.getConcept();

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(
					partitionMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == partitionConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) 
						lastPart.makeAnalog(
								ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
								editPath.getConceptNid(),
								Long.MAX_VALUE);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(partitionConcept);
					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
//					promote(extension, config);
//					termFactory.addUncommittedNoChecks(partitionConcept);
//					termFactory.addUncommittedNoChecks(extension);
					//					termFactory.commit();
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return;
	}

	public static I_GetConceptData retireConcept(I_GetConceptData conceptToRetire, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		I_GetConceptData conceptToRetireUpdatedFromDB = null;
		try {
			conceptToRetireUpdatedFromDB = termFactory.getConcept(conceptToRetire.getUids());
			I_ConceptAttributePart lastAttributePart = getLastestAttributePart(conceptToRetireUpdatedFromDB);
			for (PathBI editPath : config.getEditingPathSet()) {
				I_ConceptAttributePart newAttributeVersion = 
					(I_ConceptAttributePart) lastAttributePart.makeAnalog(
							ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							editPath.getConceptNid(), 
							Long.MAX_VALUE);
				conceptToRetireUpdatedFromDB.getConceptAttributes().addVersion(newAttributeVersion);
			}
			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			termFactory.commit();
			//promote(conceptToRetireUpdatedFromDB.getConceptAttributes(), config);
//			promote(conceptToRetire, config);
//			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
//			termFactory.commit();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conceptToRetireUpdatedFromDB;
	}

	/**
	 * Serialize.
	 * 
	 * @param object the object
	 * 
	 * @return the string
	 */
	private static String serialize(Object object) {
		String serializedForm = "";
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			serializedForm = Base64.encodeBase64String(baos.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return serializedForm;
	}

	/**
	 * Deserialize.
	 * 
	 * @param string the string
	 * 
	 * @return the object
	 */
	private static Object deserialize(String string) {
		Object object = null;
		try {
			ByteArrayInputStream bios = new ByteArrayInputStream(Base64.decodeBase64(string));
			ObjectInputStream ois = new ObjectInputStream(bios);
			object = ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		return object;
	}

	/**
	 * Waiting.
	 * 
	 * @param n the n
	 */
	public static void waiting(int n){

		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}

	public static I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType)
	throws Exception {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		I_GetConceptData latestTarget = null;
		long latestVersion = Integer.MIN_VALUE;

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(relationshipType.getConceptNid());

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(
				config.getAllowedStatus(), 
				allowedTypes, config.getViewPositionSetReadOnly(),
				Precedence.TIME, config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getTime() > latestVersion) {
				latestVersion = rel.getTime();
				latestTarget = Terms.get().getConcept(rel.getC2Id());
			}
		}

		return latestTarget;
	}

	public static void syncWorksetWithRefsetSpec(WorkSet workSet, I_ConfigAceFrame config) throws Exception {
		if (!workSet.getPartitionSchemes(config).isEmpty()) {
			JOptionPane.showMessageDialog(new JDialog(), "Not empty!, can't sync a workset once it has partition schemes...", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			throw new Exception("Not empty!, can't sync a workset once it has partition schemes...");
		}
		I_TermFactory termFactory = Terms.get();
		I_GetConceptData refsetConc = getSourceRefsetForWorkSet(workSet, config);
		if (refsetConc == null) {
			throw new Exception("No Refset Spec associated with this WorkSet");
		}

		List<Integer> excludedConcepts = new ArrayList<Integer>();
		List<Integer> includedConcepts = new ArrayList<Integer>();

		List<I_GetConceptData> exclusionRefsets = workSet.getExclusionRefsets();

		for (I_GetConceptData loopRefset : exclusionRefsets) {
			Collection<? extends I_ExtendByRef> loopRefsetMembers  = 
				termFactory.getRefsetExtensionMembers(
						loopRefset.getConceptNid());
			for (I_ExtendByRef loopRefsetMember : loopRefsetMembers) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(loopRefsetMember);
				if (isActive(lastPart.getStatusNid())) {
					excludedConcepts.add(loopRefsetMember.getComponentNid());
				}
			}
		}

		Collection<? extends I_ExtendByRef> refsetMembers  = termFactory.getRefsetExtensionMembers(
				refsetConc.getConceptNid());

		for (I_ExtendByRef refsetMember : refsetMembers) {
			I_ExtendByRefPart lastPart = getLastExtensionPart(refsetMember);
			if (isActive(lastPart.getStatusNid())) {
				includedConcepts.add(refsetMember.getComponentNid());
				if (!excludedConcepts.contains(refsetMember.getComponentNid())) {
					I_GetConceptData concept = termFactory.getConcept(refsetMember.getComponentNid());
					WorkSetMember newWorkSetMember;
					newWorkSetMember = new WorkSetMember(concept.getInitialText(),
							concept.getConceptNid(),
							concept.getUids(),
							workSet.getUids().iterator().next());
					addConceptAsWorkSetMember(newWorkSetMember, config);
				}
			}
		}

		List<WorkSetMember> workSetMembers = workSet.getWorkSetMembers();

		for (WorkSetMember loopMember : workSetMembers) {
			if (excludedConcepts.contains(loopMember.getId())) {
				retireWorkSetMember(loopMember);
			} else if (!includedConcepts.contains(loopMember.getId())) {
				retireWorkSetMember(loopMember);
			}
		}

		//		for (WorkSetMember loopMember : workSetMembers) {
		//			boolean isCurrentMemberOfRefset = false;
		//			if (!excludedConcepts.contains(loopMember.getId())) {
		//				I_GetConceptData loopConcept = termFactory.getConcept(loopMember.getId());
		//				Collection<? extends I_ExtendByRef> conceptExtensions = loopConcept.getExtensions();
		//				for (I_ExtendByRef extension : conceptExtensions) {
		//					if (refsetConc.getConceptId() == extension.getRefsetId()) {
		//						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
		//						if (isActive(lastPart.getStatusNid())) {
		//							isCurrentMemberOfRefset = true;
		//						}
		//					}
		//				}
		//			}
		//			if (!isCurrentMemberOfRefset) {
		//				retireWorkSetMember(loopMember);
		//			}
		//		}

		termFactory.commit();

	}

	public static I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Integer.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = Terms.get().newIntSet();
		allowedStatus.addAll(config.getAllowedStatus().getSetValues());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(
				allowedStatus, config.getViewPositionSetReadOnly(), Precedence.TIME,
				config.getConflictResolutionStrategy())) {
			if (loopTuple.getTime() > lastVersion) {
				lastVersion = loopTuple.getTime();
				lastPart = loopTuple.getMutablePart();
			}
		}

		if (lastPart == null) {
			lastPart = (I_ExtendByRefPart) extension;
			//throw new TerminologyException("No parts on this viewpositionset.");
		}

		return lastPart;
	}

	private static I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData concept) throws IOException {
		List<? extends I_ConceptAttributePart> refsetAttibuteParts = concept.getConceptAttributes().getMutableParts();
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

	public static boolean isActive(int statusId) {
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

	private static boolean isInactive(int statusId) {
		List<Integer> inactiveStatuses = new ArrayList<Integer>();
		try {
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (inactiveStatuses.contains(statusId));
	}

	/**
	 * Check permission for hierarchy.
	 * 
	 * @param user the user
	 * @param target the target
	 * @param permission the permission
	 * @param config the config
	 * 
	 * @return true, if successful
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * 
	 * @deprecated moved to ProjectPermissionsAPI
	 */
	public static boolean checkPermissionForHierarchy(I_GetConceptData user, I_GetConceptData target, 
			I_GetConceptData permission, I_ConfigAceFrame config) throws IOException, TerminologyException {

		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.checkPermissionForHierarchy(user, target, permission);
	}

	/**
	 * Check permission for project.
	 * 
	 * @param user the user
	 * @param target the target
	 * @param permission the permission
	 * @param config the config
	 * 
	 * @return true, if successful
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * 
	 * @deprecated moved to ProjectPermissionsAPI
	 */
	public static boolean checkPermissionForProject(I_GetConceptData user, I_GetConceptData target, 
			I_GetConceptData permission, I_ConfigAceFrame config) throws IOException, TerminologyException {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.checkPermissionForProject(user, target, permission);
	}

	public static BusinessProcess getBusinessProcess(File f) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			BusinessProcess processToLunch=(BusinessProcess) in.readObject();
			in.close();
			return processToLunch;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static WorkList generateWorkListFromPartition(Partition partition, String destination,
			BusinessProcess businessProcess, String name, I_ConfigAceFrame config) throws Exception {
		WorkList workList = new WorkList(name,
				0, null, partition.getUids().iterator().next(), destination, businessProcess);
		List<WorkListMember> workListMembers = new ArrayList<WorkListMember>();
		List<PartitionMember> partitionMembers = partition.getPartitionMembers();
		for (PartitionMember partitionMember: partitionMembers) {
			BusinessProcess businessProcessForAttachments = businessProcess;
			if (businessProcessForAttachments != null) {
				businessProcessForAttachments.setOriginator(config.getUsername());
			}
			WorkListMember workListMember = new WorkListMember(partitionMember.getName(), 
					partitionMember.getId(),
					partitionMember.getUids(), null, destination, 
					ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().iterator().next(),
					new java.util.Date().getTime() );
			workListMembers.add(workListMember);
		}
		if (workListMembers.size() == 0) {
			throw new Exception("No concepts found for the worklist!");
		} else {
			workList = createNewWorkList(workList, config);
			if(workList != null){
				for (WorkListMember workListMember: workListMembers) {
					workListMember.setWorkListUUID(workList.getUids().iterator().next());
					addConceptAsWorkListMember(workListMember, config);
//					promoteLanguageContent(workListMember, config);
				}
			}
		}
		Terms.get().commit();
		return workList;
	}

	//	public static void promoteWorkSetMember(WorkSetMember workSetMember, 
	//			PositionBI positionToExport) throws TerminologyException, IOException {
	//		I_TermFactory tf = Terms.get();
	//		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
	//		I_GetConceptData memberConcept = workSetMember.getConcept();
	//		memberConcept.promote(positionToExport, config.getPromotionPathSetReadOnly(), 
	//				config.getAllowedStatus(), Precedence.TIME);
	//	}
	//
	//	public static void promoteWorkSetMembers(WorkSet workSet, int statusIdToPromote, 
	//			PositionBI positionToExport) throws TerminologyException, IOException {
	//		I_TermFactory tf = Terms.get();
	//		for (WorkSetMember member : workSet.getWorkSetMembers()) {
	//			//			I_GetConceptData statusConcept = tf.getConcept(member.getActivityStatus());
	//			//			if (statusConcept.getConceptId() == statusIdToPromote) {
	//			//				promoteWorkSetMember(member, positionToExport);
	//			//			}
	//		}
	//	}

	private static List<? extends I_RelTuple> cleanRelTuplesList(List<? extends I_RelTuple> tuples) {
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

	public static boolean validateConceptAsRefset(I_GetConceptData concept, I_ConfigAceFrame config)
	throws IOException, TerminologyException {
		I_TermFactory tf = Terms.get();
		if (tf.getConcept(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids()).isParentOf(
				concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.getUids()).isParentOf(
				concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.getUids()).isParentOf(
				concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.getUids()).isParentOf(
				concept)) {
			return true;

		}
		return false;
	}

	public static void promote(I_GetConceptData termComponent, I_ConfigAceFrame config) {
//		PositionBI viewPosition = config.getViewPositionSetReadOnly().iterator().next();
//		I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
//		allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
//		try {
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
//			//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
//			//			}
//			//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
//			//			}
//			//			
//			//			Terms.get().commit();

//			termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//					allowedStatusWithRetired, Precedence.TIME);
//			Terms.get().addUncommittedNoChecks(termComponent);
//
//			//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
//			//			}
//			//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
//			//			}
//			//
//			//			Terms.get().commit();
//
//			for (I_ExtendByRef loopExtension : Terms.get().getAllExtensionsForComponent(termComponent.getConceptNid())) {
//				loopExtension.promote(config.getViewPositionSet().iterator().next(), 
//						config.getPromotionPathSetReadOnly(), allowedStatusWithRetired, Precedence.TIME);
//				Terms.get().addUncommittedNoChecks(loopExtension);
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//		//		for (PathBI path : config.getEditingPathSetReadOnly()) {
//		//			try {
//		//				PositionBI viewPosition = Terms.get().newPosition(path, Integer.MAX_VALUE);
//		//				termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//						config.getAllowedStatus(), Precedence.TIME);
//		//			} catch (TerminologyException e) {
//		//				e.printStackTrace();
//		//			} catch (IOException e) {
//		//				e.printStackTrace();
//		//			}
//		//		}

	}
	public static void promote(I_ExtendByRef termComponent, I_ConfigAceFrame config) {
//		PositionBI viewPosition = config.getViewPositionSetReadOnly().iterator().next();
//		I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
//		allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
//		try {
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
//			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
//			//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
//			//			}
//			//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
//			//			}
//			//			
//			//			Terms.get().commit();
//
//			termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//					allowedStatusWithRetired, Precedence.TIME);
//			Terms.get().addUncommittedNoChecks(termComponent);
//
//			//			if (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
//			//			}
//			//			if (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
//			//				Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
//			//			}
//			//
//			//			Terms.get().commit();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//
//		//		for (PathBI path : config.getEditingPathSetReadOnly()) {
//		//			try {
//		//				PositionBI viewPosition = Terms.get().newPosition(path, Integer.MAX_VALUE);
//		//				termComponent.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//						config.getAllowedStatus(), Precedence.TIME);
//		//			} catch (TerminologyException e) {
//		//				e.printStackTrace();
//		//			} catch (IOException e) {
//		//				e.printStackTrace();
//		//			}
//		//		}

	}

	/**
	 * Gets the users for role.
	 * 
	 * @param role the role
	 * @param project the project
	 * @param config the config
	 * 
	 * @return the users for role
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws TerminologyException the terminology exception
	 * 
	 *  @deprecated moved to ProjectPermissionAPI
	 */
	public static Set<I_GetConceptData> getUsersForRole(I_GetConceptData role, I_GetConceptData project, 
			I_ConfigAceFrame config) throws IOException, TerminologyException {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.getUsersForRole(role, project);
	}

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of the User concept in the top hierarchy,
	 * and have a description of type "user inbox".
	 * 
	 * @return The set of valid users.
	 * 
	 * @deprecated moved to ProjectPermissionAPI
	 */
	public static Set<I_GetConceptData> getUsers(I_ConfigAceFrame config) {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.getUsers();
	}

	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}

	public static boolean promoteWorkListContentToReleaseCandidatePath(I_GetConceptData conflictResolutionPathConcept,
			I_GetConceptData releaseCandidatePathConcept,
			WorkList workList, I_ConfigAceFrame config) throws TerminologyException, IOException {
		int promoted = 0;
		I_TermFactory tf = Terms.get();
		PathSetReadOnly promotionPathSet = new PathSetReadOnly(tf.getPath(releaseCandidatePathConcept.getConceptNid()));
		PositionBI viewPosition = tf.newPosition(tf.getPath(conflictResolutionPathConcept.getConceptNid()), 
				Integer.MAX_VALUE);
		for (WorkListMember loopMember : workList.getWorkListMembers()) {
			if (loopMember.getConcept().promote(viewPosition, promotionPathSet, 
					config.getAllowedStatus(), Precedence.TIME)) {
				promoted++;
			}
		}
		return (promoted > 0);
	}

	public static void promoteLanguageContent(WorkListMember member, I_ConfigAceFrame config) throws Exception {
		// PROMOTION REMOVED, No promotion path will be used
//		I_TermFactory tf = Terms.get();
//		I_GetConceptData concept = member.getConcept();
//
//		// Policy: Only one viewposition set, only one promotion path, only one edit path
//		//TODO: promote only language content
//
//		concept.promote(config.getViewPositionSetReadOnly().iterator().next(), 
//				config.getPromotionPathSetReadOnly(),
//				config.getAllowedStatus(), config.getPrecedence());
//		tf.addUncommittedNoChecks(concept);
//
//		for (I_ExtendByRef loopExtension : tf.getAllExtensionsForComponent(concept.getConceptNid())) {
//			loopExtension.promote(config.getViewPositionSetReadOnly().iterator().next(), 
//					config.getPromotionPathSetReadOnly(),
//					config.getAllowedStatus(), config.getPrecedence());
//			tf.addUncommittedNoChecks(loopExtension);
//		}
//
//		// END OF NEW PROMOTION ALGORITHM
//
//		//		WorkList workList = TerminologyProjectDAO.getWorkList(
//		//				tf.getConcept(member.getWorkListUUID()), config);
//		//		CommentsRefset commentsRefset = workList.getCommentsRefset(config);
//		//		PromotionRefset promotionRefset = workList.getPromotionRefset(config);
//		//		TranslationProject project = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(workList, config);
//		//		if (project.getTargetLanguageRefset() == null) {
//		//			JOptionPane.showMessageDialog(new JDialog(), "Target language refset cannot be retrieved\nCheck project details", 
//		//					"Error", JOptionPane.ERROR_MESSAGE);
//		//			throw new Exception("Target language refset cannot be retrieved.");
//		//		}
//		//		int targetLanguageRefsetId = project.getTargetLanguageRefset().getConceptNid();
//		//		LanguageMembershipRefset langRefset = new LanguageMembershipRefset(project.getTargetLanguageRefset(), config);
//		//		int commentsLanguageRefsetId = langRefset.getCommentsRefset(config).getRefsetId();
//		//
//		//		I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
//		//		for (int id : config.getAllowedStatus().getSetValues()) {
//		//			allowedStatusWithRetired.add(id);
//		//		}
//		//		allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
//		//		//TODO: validate only one viewposition, editPath and promotePath
//		//		int editPathId = config.getEditingPathSet().iterator().next().getConceptNid();
//		//		int promotePathId = config.getPromotionPathSet().iterator().next().getConceptNid();
//		//		PositionBI viewPosition = config.getViewPositionSetReadOnly().iterator().next();
//		//		Set<PositionBI> viewPositions= new HashSet<PositionBI>();
//		//		viewPositions.add(viewPosition);
//		//		PositionSetReadOnly originPositionsReadOnly = new PositionSetReadOnly(viewPositions);
//		//
//		//		Set<PositionBI> targetPositions= new HashSet<PositionBI>();
//		//		for (PathBI loopPath : config.getPromotionPathSet()) {
//		//			targetPositions.add(tf.newPosition(loopPath, Integer.MAX_VALUE));
//		//		}
//		//		PositionSetReadOnly targetPositionsReadOnly = new PositionSetReadOnly(targetPositions);
//		//		
//		//		// Check for changes in descriptions
//		//		List<? extends I_DescriptionTuple> originDescriptions = concept.getDescriptionTuples(allowedStatusWithRetired, config.getDescTypes(), 
//		//				originPositionsReadOnly, Precedence.TIME, config.getConflictResolutionStrategy());
//		//
//		//		List<? extends I_DescriptionTuple> targetDescriptions = concept.getDescriptionTuples(allowedStatusWithRetired, config.getDescTypes(), 
//		//				targetPositionsReadOnly, Precedence.TIME, config.getConflictResolutionStrategy());
//		//
//		//		Set<Integer> originDescIds = new HashSet<Integer>();
//		//		for (I_DescriptionTuple loopTuple : originDescriptions) {
//		//			originDescIds.add(loopTuple.getDescId());
//		//		}
//		//		Set<Integer> targetDescIds = new HashSet<Integer>();
//		//		for (I_DescriptionTuple loopTuple : targetDescriptions) {
//		//			targetDescIds.add(loopTuple.getDescId());
//		//		}
//		//		// new Descriptions
//		//		for (Integer loopDescId : originDescIds) {
//		//			if (!targetDescIds.contains(loopDescId)) {
//		//				tf.getDescription(loopDescId).promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//						allowedStatusWithRetired, Precedence.TIME);
//		//			}
//		//		}
//		//		// retired Descriptions
//		//		for (Integer loopDescId : targetDescIds) {
//		//			if (!originDescIds.contains(loopDescId)) {
//		//				tf.getDescription(loopDescId).promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//						allowedStatusWithRetired, Precedence.TIME);
//		//			}
//		//		}
//		//		// Check for new versions of descriptions
//		//		for (I_DescriptionTuple originLoopTuple : originDescriptions) {
//		//			for (I_DescriptionTuple targetLoopTuple : targetDescriptions) {
//		//				if (originLoopTuple.getDescId() == targetLoopTuple.getDescId()) {
//		//					if (!originLoopTuple.getText().equals(targetLoopTuple.getText()) ||
//		//							originLoopTuple.getTypeNid() != targetLoopTuple.getTypeNid() ||
//		//							originLoopTuple.isInitialCaseSignificant() != targetLoopTuple.isInitialCaseSignificant() ||
//		//							originLoopTuple.getStatusNid() != targetLoopTuple.getStatusNid()) {
//		//						originLoopTuple.getDescVersioned().promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//								allowedStatusWithRetired, Precedence.TIME);
//		//					}
//		//				}
//		//			}
//		//		}
//		//		
//		//		Collection<? extends I_ExtendByRef> langExtensions = project.getTargetLanguageRefset().getExtensions();
//		//		for (I_ExtendByRef iExtendByRef : langExtensions) {
//		//			iExtendByRef.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//									allowedStatusWithRetired, Precedence.TIME);
//		//		}
//		//		
//		////		Collection<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(concept.getConceptNid());
//		////		for (I_ExtendByRef loopExtension : extensions) {
//		////			if (loopExtension.getRefsetId() == commentsRefset.getRefsetId() ||
//		////					loopExtension.getRefsetId() == promotionRefset.getRefsetId() ||
//		////					loopExtension.getRefsetId() == commentsLanguageRefsetId ||
//		////					loopExtension.getRefsetId() == targetLanguageRefsetId) {
//		////				List<? extends I_ExtendByRefVersion> tuples = loopExtension.getTuples(config.getAllowedStatus(), 
//		////						config.getViewPositionSetReadOnly(), Precedence.TIME, 
//		////						config.getConflictResolutionStrategy());
//		////				if (tuples != null && !tuples.isEmpty()) {
//		////					I_ExtendByRefPart lastPart = tuples.iterator().next().getMutablePart();
//		////					
//		////					if (lastPart.getPathNid() == editPathId) {
//		////						boolean promoted = false;
//		////						for (I_ExtendByRefPart loop2Part : loopExtension.getMutableParts()) {
//		////							if (loop2Part.getPathNid() == promotePathId && lastPart.compareTo(loop2Part) == 0) {
//		////								promoted = true;
//		////							}
//		////						}
//		////						if (!promoted) {
//		////							loopExtension.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		////									allowedStatusWithRetired, Precedence.TIME);
//		////						}
//		////					}
//		////					
//		////				}
//		////			}
//		////		}
//		//		//TODO: extension.getTuples is always size =1 or 0??
//		//		//			I_ExtendByRefVersion originVersion = loopExtension.getTuples(allowedStatusWithRetired, 
//		//		//					originPositionsReadOnly, 
//		//		//					Precedence.TIME, config.getConflictResolutionStrategy()).iterator().next();
//		//		//			I_ExtendByRefVersion targetVersion = loopExtension.getTuples(allowedStatusWithRetired, 
//		//		//					targetPositionsReadOnly, 
//		//		//					Precedence.TIME, config.getConflictResolutionStrategy()).iterator().next();
//		//		//			
//		//		//			if ((originVersion != null && targetVersion == null) || 
//		//		//					(originVersion == null && targetVersion != null)) {
//		//		//				loopExtension.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//		//						allowedStatusWithRetired, Precedence.TIME);
//		//		//				tf.addUncommitted(tf.getConcept(loopExtension.getRefsetId()));
//		//		//			} else if ((originVersion != null && targetVersion != null)) {
//		//		//				if (originVersion.getVersion() != targetVersion.getVersion()) {
//		//		//					tf.addUncommitted(tf.getConcept(loopExtension.getRefsetId()));
//		//		//					loopExtension.promote(viewPosition, config.getPromotionPathSetReadOnly(), 
//		//		//							allowedStatusWithRetired, Precedence.TIME);
//		//		//				}
//		//		//			}
//		//		tf.commit();
	}

}
