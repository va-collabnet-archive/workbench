/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.dwfa.ace.api.I_ConceptAttributePart;
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
import org.dwfa.ace.api.I_ShowActivity;
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
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.helper.promote.TerminologyPromoterBI;
import org.ihtsdo.lucene.SearchResult;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.MappingProject;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.model.WorkSetMember;
import org.ihtsdo.project.model.WorklistMetadata;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.refset.PromotionRefset;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfMembership;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.conceptattribute.ConceptAttributeVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid_string.RefexNidStringVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf1;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.workflow.api.WfFilterBI;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

/**
 * The Class TerminologyProjectDAO.
 */
public class TerminologyProjectDAO {

	/**
	 * The work list cache.
	 */
	public static Map<UUID, WorkList> workListCache = new HashMap<UUID, WorkList>();
	/**
	 * The partition cache.
	 */
	public static Map<UUID, Partition> partitionCache = new HashMap<UUID, Partition>();
	/**
	 * The target languages cache.
	 */
	public static HashMap<Integer, List<TranslationProject>> targetLanguagesCache = null;
	public static HashMap<Integer, List<Integer>> languageWorksetsMap = null;

	// I_TerminologyProjects CRUD **********************************
	/**
	 * Gets the all projects.
	 * 
	 * @param config
	 *            the config
	 * 
	 * @return the all projects
	 */
	public static List<I_TerminologyProject> getAllProjects(I_ConfigAceFrame config) {
		List<I_TerminologyProject> projects = new ArrayList<I_TerminologyProject>();
		projects.addAll(getAllTranslationProjects(config));
		projects.addAll(getAllTerminologyProjects(config));
		projects.addAll(getAllMappingProjects(config));
		return projects;
	}

	/**
	 * Gets the all translation projects.
	 * 
	 * @param config
	 *            the config
	 * @return the all translation projects
	 */
	public static List<TranslationProject> getAllTranslationProjects(I_ConfigAceFrame config) {

		I_TermFactory termFactory = Terms.get();
		List<TranslationProject> projects = new ArrayList<TranslationProject>();
		try {
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<? extends I_GetConceptData> children = projectsRoot.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(child);
				if (isActive(lastAttributePart.getStatusNid())) {
					projects.add(getTranslationProject(child, config));
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return projects;
	}

	/**
	 * Gets the all terminology projects.
	 * 
	 * @param config
	 *            the config
	 * @return all terminology projects
	 */
	public static List<TerminologyProject> getAllTerminologyProjects(I_ConfigAceFrame config) {

		I_TermFactory termFactory = Terms.get();
		List<TerminologyProject> projects = new ArrayList<TerminologyProject>();
		try {
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.TERMINOLOGY_PROJECTS_ROOT.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<? extends I_GetConceptData> children = projectsRoot.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(child);
				if (isActive(lastAttributePart.getStatusNid())) {
					projects.add(getTerminologyProject(child, config));
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return projects;
	}

	/**
	 * Gets the all terminology projects.
	 * 
	 * @param config
	 *            the config
	 * @return all terminology projects
	 */
	public static List<MappingProject> getAllMappingProjects(I_ConfigAceFrame config) {

		I_TermFactory termFactory = Terms.get();
		List<MappingProject> projects = new ArrayList<MappingProject>();
		try {
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.MAPPING_PROJECTS_ROOT.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<? extends I_GetConceptData> children = projectsRoot.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData child : children) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(child);
				if (isActive(lastAttributePart.getStatusNid())) {
					projects.add(getMappingProject(child, config));
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return projects;
	}

	/**
	 * Gets the all workset target languages map.
	 * 
	 * @param config
	 *            the config
	 * @return the all workset target languages map
	 * @throws Exception
	 * @throws IOException
	 * @throws TerminologyException
	 */
	public static HashMap<Integer, List<Integer>> getAllWorksetTargetLanguages(I_ConfigAceFrame config) throws TerminologyException, IOException, Exception {
		if (languageWorksetsMap != null) {
			return languageWorksetsMap;
		}
		HashMap<Integer, List<TranslationProject>> tgtLangs = getAllTranslationProjectTargetLanguages(config);
		languageWorksetsMap = new HashMap<Integer, List<Integer>>();
		for (Integer tgtLang : tgtLangs.keySet()) {
			List<Integer> wsetList = new ArrayList<Integer>();
			List<TranslationProject> transPrjs = tgtLangs.get(tgtLang);
			for (TranslationProject tPrj : transPrjs) {
				List<WorkSet> wsts = tPrj.getWorkSets(config);
				for (WorkSet wSet : wsts) {
					PromotionRefset pRefset = wSet.getPromotionRefset(config);
					wsetList.add(pRefset.getRefsetId());
				}
			}
			languageWorksetsMap.put(tgtLang, wsetList);
		}
		return languageWorksetsMap;
	}

	public static HashMap<Integer, List<TranslationProject>> getAllTranslationProjectTargetLanguages(I_ConfigAceFrame config) throws TerminologyException, IOException, Exception {

		if (targetLanguagesCache != null) {
			return targetLanguagesCache;
		}
		targetLanguagesCache = new HashMap<Integer, List<TranslationProject>>();
		config = Terms.get().getActiveAceFrameConfig();
		List<TranslationProject> translProjects = getAllTranslationProjects(config);
		List<TranslationProject> tmpTransProjList = new ArrayList<TranslationProject>();
		for (TranslationProject translationProject : translProjects) {
			I_GetConceptData refConcept = translationProject.getTargetLanguageRefset();
			if (targetLanguagesCache != null && refConcept != null && targetLanguagesCache.containsKey(refConcept.getNid())) {
				tmpTransProjList = targetLanguagesCache.get(refConcept.getNid());
				targetLanguagesCache.put(refConcept.getNid(), tmpTransProjList);
			} else {
				tmpTransProjList = new ArrayList<TranslationProject>();
			}
			tmpTransProjList.add(translationProject);
		}

		return targetLanguagesCache;

	}

	/**
	 * Creates the new translation project.
	 * 
	 * @param name
	 *            the name
	 * @param config
	 *            the config
	 * @return the translation project
	 */
	public static TranslationProject createNewTranslationProject(String name, I_ConfigAceFrame config) {
		TranslationProject project = new TranslationProject(name, 0, null);
		return createNewTranslationProject(project, config);
	}

	/**
	 * Creates the new terminology project.
	 * 
	 * @param name
	 *            the name
	 * @param config
	 *            the config
	 * @return the terminology project
	 */
	public static TerminologyProject createNewTerminologyProject(String name, I_ConfigAceFrame config) {
		TerminologyProject project = new TerminologyProject(name, 0, null);
		return createNewTerminologyProject(project, config);
	}

	/**
	 * Creates the new project.
	 * 
	 * @param projectWithMetadata
	 *            the project with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the translation project
	 */
	public static TranslationProject createNewTranslationProject(TranslationProject projectWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		TranslationProject project = null;
		I_GetConceptData newConcept = null;
		String projectName = projectWithMetadata.getName() + " (translation project)";
		try {
			if (isConceptDuplicate(projectName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}

			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), projectsRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			project = new TranslationProject(projectWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids());

			termFactory.addUncommittedNoChecks(newConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

			// promote(newConcept, config);
			//
			// termFactory.addUncommittedNoChecks(newConcept);
			// termFactory.commit();

			project = getTranslationProject(newConcept, config);

			String nacWorkSetName = "Maintenance - " + project.getName().replace("(translation project)", "");
			WorkSet nacWorkSet = createNewWorkSet(nacWorkSetName, project, config);
			createNewPartitionScheme("Maintenance - " + project.getName().replace("(translation project)", ""), nacWorkSet.getUids().iterator().next(), config);
			getTranslationProjectDefaultConfigFile(project);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return project;
	}

	/**
	 * This method returns the parameter projects default configuration file<br>
	 * if the file does not exists, it creates a new default configuration file<br>
	 * .
	 * 
	 * @param project
	 *            the project
	 * @return The project file
	 */
	public static File getTranslationProjectDefaultConfigFile(TranslationProject project) {
		File configFile = null;
		File sharedFolder = new File("profiles/shared");
		if (!sharedFolder.exists()) {
			sharedFolder.mkdirs();
		} else {
			List<UUID> uids = project.getUids();
			for (UUID uuid : uids) {
				File tmpFile = new File("profiles/shared/" + uuid + "-translation-config.cfg");
				if (tmpFile.exists()) {
					configFile = tmpFile;
				}
			}
		}
		if (configFile == null) {
			configFile = new File("profiles/shared/" + project.getUids().get(0) + "-translation-config.cfg");
			if (!configFile.exists()) {
				try {
					configFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return configFile;
	}

	/**
	 * Creates the new project.
	 * 
	 * @param projectWithMetadata
	 *            the project with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the terminology project
	 */
	public static TerminologyProject createNewTerminologyProject(TerminologyProject projectWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		TerminologyProject project = null;
		I_GetConceptData newConcept = null;
		String projectName = projectWithMetadata.getName() + " (terminology project)";
		try {
			if (isConceptDuplicate(projectName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}

			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.TERMINOLOGY_PROJECTS_ROOT.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), projectsRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			project = new TerminologyProject(projectWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids());

			termFactory.addUncommittedNoChecks(newConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

			// promote(newConcept, config);
			//
			// termFactory.addUncommittedNoChecks(newConcept);
			// termFactory.commit();

			project = getTerminologyProject(newConcept, config);

			String nacWorkSetName = "Maintenance - " + project.getName().replace("(terminology project)", "");
			WorkSet nacWorkSet = createNewWorkSet(nacWorkSetName, project, config);
			createNewPartitionScheme("Maintenance - " + project.getName().replace("(terminology project)", ""), nacWorkSet.getUids().iterator().next(), config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return project;
	}

	/**
	 * Creates the new project.
	 * 
	 * @param projectWithMetadata
	 *            the project with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the terminology project
	 */
	public static MappingProject createNewMappingProject(MappingProject projectWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		I_TerminologyProject project = null;
		I_GetConceptData newConcept = null;
		String projectName = projectWithMetadata.getName() + " (mapping project)";
		try {
			if (isConceptDuplicate(projectName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}

			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData projectsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.MAPPING_PROJECTS_ROOT.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", projectName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), projectsRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			project = new MappingProject(projectWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids());

			termFactory.addUncommittedNoChecks(newConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

			// promote(newConcept, config);
			//
			// termFactory.addUncommittedNoChecks(newConcept);
			// termFactory.commit();

			project = getMappingProject(newConcept, config);

			String nacWorkSetName = "Maintenance - " + project.getName().replace("(mapping project)", "");
			WorkSet nacWorkSet = createNewWorkSet(nacWorkSetName, project, config);
			createNewPartitionScheme("Maintenance - " + project.getName().replace("(mapping project)", ""), nacWorkSet.getUids().iterator().next(), config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return (MappingProject) project;
	}

	/**
	 * Gets the project.
	 * 
	 * @param projectConcept
	 *            the project concept
	 * @param config
	 *            the config
	 * @return the project
	 * @throws Exception
	 *             the exception
	 */
	public static TranslationProject getTranslationProject(I_GetConceptData projectConcept, I_ConfigAceFrame config) throws Exception {
		TranslationProject project = null;
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = projectConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in project...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.TRANSLATION_PROJECTS_ROOT.localize().getNid()) {
				String name = getConceptString(projectConcept);
				List<? extends I_DescriptionTuple> descTuples = projectConcept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				project = new TranslationProject(name, projectConcept.getConceptNid(), projectConcept.getUids());
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return project;
	}

	/**
	 * Temporary method replacing I_Getconceptdata toString method
	 * 
	 * @param concept
	 * @return
	 */
	private static String getConceptString(I_GetConceptData concept) {
		ConceptVersionBI conceptVersion = null;
		try {
			conceptVersion = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), concept.getUUIDs());
			return conceptVersion.getDescriptionPreferred().getText();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
		return concept.toString();
	}

	/**
	 * Gets the project.
	 * 
	 * @param projectConcept
	 *            the project concept
	 * @param config
	 *            the config
	 * @return the project
	 * @throws Exception
	 *             the exception
	 */
	public static MappingProject getMappingProject(I_GetConceptData projectConcept, I_ConfigAceFrame config) throws Exception {

		MappingProject project = null;
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = projectConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in project...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.MAPPING_PROJECTS_ROOT.localize().getNid()) {
				String name = getConceptString(projectConcept);
				List<? extends I_DescriptionTuple> descTuples = projectConcept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				project = new MappingProject(name, projectConcept.getConceptNid(), projectConcept.getUids());
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return project;
	}

	/**
	 * Gets the project.
	 * 
	 * @param projectConcept
	 *            the project concept
	 * @param config
	 *            the config
	 * @return the project
	 * @throws Exception
	 *             the exception
	 */
	public static TerminologyProject getTerminologyProject(I_GetConceptData projectConcept, I_ConfigAceFrame config) throws Exception {

		TerminologyProject project = null;
		I_TermFactory termFactory = Terms.get();
		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = projectConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in project...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.TERMINOLOGY_PROJECTS_ROOT.localize().getNid()) {
				String name = getConceptString(projectConcept);
				List<? extends I_DescriptionTuple> descTuples = projectConcept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				project = new TerminologyProject(name, projectConcept.getConceptNid(), projectConcept.getUids());
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return project;
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
	public static void updatePreferredTerm(I_GetConceptData concept, String newString, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet localDescTypes = null;
			if (config.getDescTypes().getSetValues().length > 0) {
				localDescTypes = config.getDescTypes();
			}
			List<? extends I_DescriptionTuple> descTuples = concept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			for (I_DescriptionTuple tuple : descTuples) {

				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					I_DescriptionVersioned description = tuple.getDescVersioned();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_DescriptionPart newPart = (I_DescriptionPart) tuple.getMutablePart().makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						newPart.setText(newString);
						description.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(concept);
					// termFactory.commit();
					termFactory.addUncommittedNoChecks(concept);
					// termFactory.commit();
				}
			}
			// promote(concept, config);
			// termFactory.addUncommittedNoChecks(concept);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Update work set metadata.
	 * 
	 * @param workSetWithMetadata
	 *            the work set with metadata
	 * @param config
	 *            the config
	 * @return the work set
	 */
	public static WorkSet updateWorkSetMetadata(WorkSet workSetWithMetadata, I_ConfigAceFrame config) {
		WorkSet workSet = null;

		WorkSet currentVersionOfWorkSet = getWorkSet(workSetWithMetadata.getConcept(), config);

		if (!currentVersionOfWorkSet.getName().equals(workSetWithMetadata.getName())) {
			updatePreferredTerm(workSetWithMetadata.getConcept(), workSetWithMetadata.getName(), config);
		}

		workSet = getWorkSet(workSetWithMetadata.getConcept(), config);

		return workSet;
	}

	/**
	 * Update partition scheme metadata.
	 * 
	 * @param partitionSchemeWithMetadata
	 *            the partition scheme with metadata
	 * @param config
	 *            the config
	 * @return the partition scheme
	 */
	public static PartitionScheme updatePartitionSchemeMetadata(PartitionScheme partitionSchemeWithMetadata, I_ConfigAceFrame config) {
		PartitionScheme partitionScheme = null;

		PartitionScheme currentVersionOfPartitionScheme = getPartitionScheme(partitionSchemeWithMetadata.getConcept(), config);

		if (!currentVersionOfPartitionScheme.getName().equals(partitionSchemeWithMetadata.getName())) {
			updatePreferredTerm(partitionSchemeWithMetadata.getConcept(), partitionSchemeWithMetadata.getName(), config);
		}

		partitionScheme = getPartitionScheme(partitionSchemeWithMetadata.getConcept(), config);

		return partitionScheme;
	}

	/**
	 * Update partition metadata.
	 * 
	 * @param partitionWithMetadata
	 *            the partition with metadata
	 * @param config
	 *            the config
	 * @return the partition
	 */
	public static Partition updatePartitionMetadata(Partition partitionWithMetadata, I_ConfigAceFrame config) {
		Partition partition = null;

		Partition currentVersionOfPartition = getPartition(partitionWithMetadata.getConcept(), config);
		if (!currentVersionOfPartition.getName().equals(partitionWithMetadata.getName())) {
			updatePreferredTerm(partitionWithMetadata.getConcept(), partitionWithMetadata.getName(), config);
		}
		partition = getPartition(partitionWithMetadata.getConcept(), config);
		return partition;
	}

	/**
	 * Update project metadata.
	 * 
	 * @param projectWithMetadata
	 *            the project with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the i_ terminology project
	 */
	public static TranslationProject updateTranslationProjectMetadata(TranslationProject projectWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		TranslationProject project = null;

		try {

			TranslationProject currentVersionOfProject = getTranslationProject(projectWithMetadata.getConcept(), config);

			if (!currentVersionOfProject.getName().equals(projectWithMetadata.getName())) {
				updatePreferredTerm(projectWithMetadata.getConcept(), projectWithMetadata.getName(), config);
			}

			I_GetConceptData projectsRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROJECT_EXTENSION_REFSET.getUids());

			I_GetConceptData projectConcept = termFactory.getConcept(projectWithMetadata.getUids());

			String metadata = serialize(projectWithMetadata);

			List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
			extensions.addAll(termFactory.getAllExtensionsForComponent(projectConcept.getConceptNid()));
			for (I_ExtendByRef extension : extensions) {
				for (PathBI editPath : config.getEditingPathSet()) {
					if (extension.getRefsetId() == projectsRefset.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
						termFactory.addUncommittedNoChecks(projectsRefset);
						termFactory.addUncommittedNoChecks(extension);
						// termFactory.commit();
						// promote(extension, config);
						// termFactory.addUncommittedNoChecks(projectsRefset);
						// termFactory.addUncommittedNoChecks(extension);
						// termFactory.commit();
					}
				}
			}
			project = getTranslationProject(projectConcept, config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return project;
	}

	/**
	 * Update project metadata.
	 * 
	 * @param projectWithMetadata
	 *            the project with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the i_ terminology project
	 */
	public static I_TerminologyProject updateProjectMetadata(I_TerminologyProject projectWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		I_TerminologyProject project = null;

		try {

			I_TerminologyProject currentVersionOfProject = getProject(projectWithMetadata.getConcept(), config);

			if (!currentVersionOfProject.getName().equals(projectWithMetadata.getName())) {
				updatePreferredTerm(projectWithMetadata.getConcept(), projectWithMetadata.getName(), config);
			}

			I_GetConceptData projectsRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROJECT_EXTENSION_REFSET.getUids());

			I_GetConceptData projectConcept = termFactory.getConcept(projectWithMetadata.getUids());

			String metadata = serialize(projectWithMetadata);

			List<I_ExtendByRef> extensions = new ArrayList<I_ExtendByRef>();
			extensions.addAll(termFactory.getAllExtensionsForComponent(projectConcept.getConceptNid()));
			for (I_ExtendByRef extension : extensions) {
				for (PathBI editPath : config.getEditingPathSet()) {
					if (extension.getRefsetId() == projectsRefset.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
						termFactory.addUncommittedNoChecks(projectsRefset);
						termFactory.addUncommittedNoChecks(extension);
						// termFactory.commit();
						// promote(extension, config);
						// termFactory.addUncommittedNoChecks(projectsRefset);
						// termFactory.addUncommittedNoChecks(extension);
						// termFactory.commit();
					}
				}
			}
			project = getProject(projectConcept, config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return project;
	}

	/**
	 * Creates the new work set.
	 * 
	 * @param name
	 *            the name
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the work set
	 */
	public static WorkSet createNewWorkSet(String name, I_TerminologyProject project, I_ConfigAceFrame config) {
		WorkSet workSet = new WorkSet(name, project.getUids().iterator().next());
		return createNewWorkSet(workSet, config);
	}

	/**
	 * Creates the new work set.
	 * 
	 * @param name
	 *            the name
	 * @param projectUUID
	 *            the project uuid
	 * @param config
	 *            the config
	 * @return the work set
	 */
	public static WorkSet createNewWorkSet(String name, UUID projectUUID, I_ConfigAceFrame config) {
		WorkSet workSet = new WorkSet(name, projectUUID);
		return createNewWorkSet(workSet, config);
	}

	/**
	 * Creates the new work set.
	 * 
	 * @param worksetWithMetadata
	 *            the workset with metadata
	 * @param config
	 *            the config
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
			if (isConceptDuplicate(workSetName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}

			I_GetConceptData worksetsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.getUids());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData project = termFactory.getConcept(worksetWithMetadata.getProjectUUID());

			I_GetConceptData commentsRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
			I_GetConceptData promotionRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
			I_GetConceptData purposeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
			I_GetConceptData typeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
			I_GetConceptData defining = termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
			I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
			I_GetConceptData current = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", workSetName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", workSetName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetsRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, includesFromAttribute, project, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			I_GetConceptData newCommentsConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en", name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en", name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newCommentsConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetsRoot, defining, refinability, current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, commentsRelConcept, newCommentsConcept, defining, refinability, current, 0, config);

			I_GetConceptData newPromotionConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
			newPromotionConcept.setAnnotationStyleRefex(true);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en", name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en", name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
			termFactory.newRelationship(UUID.randomUUID(), newPromotionConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetsRoot, defining, refinability, current, 0, config);
			termFactory.newRelationship(UUID.randomUUID(), newConcept, promotionRelConcept, newPromotionConcept, defining, refinability, current, 0, config);

			workSet = new WorkSet(worksetWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(), worksetWithMetadata.getProjectUUID());

			termFactory.addUncommittedNoChecks(newConcept);
			termFactory.addUncommittedNoChecks(newCommentsConcept);
			termFactory.addUncommittedNoChecks(newPromotionConcept);
			// promote(newConcept, config);
			// promote(newCommentsConcept, config);
			// promote(newPromotionConcept, config);
			// termFactory.addUncommittedNoChecks(newConcept);
			// termFactory.addUncommittedNoChecks(newCommentsConcept);
			// termFactory.addUncommittedNoChecks(newPromotionConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			newCommentsConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			newPromotionConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return workSet;
	}

	/**
	 * Gets the work set.
	 * 
	 * @param workSetConcept
	 *            the work set concept
	 * @param config
	 *            the config
	 * 
	 * @return the work set
	 */
	public static WorkSet getWorkSet(I_GetConceptData workSetConcept, I_ConfigAceFrame config) {
		WorkSet workSet = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = workSetConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in workset...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.localize().getNid()) {
				String name = getConceptString(workSetConcept);
				List<? extends I_DescriptionTuple> descTuples = workSetConcept.getDescriptionTuples(config.getAllowedStatus(), (config.getDescTypes().getSetValues().length == 0) ? null : config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME,
						config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}
				allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> projects = workSetConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (projects.size() != 1) {
					throw new Exception("Error: Wrong number of projects in workset...");
				}

				I_GetConceptData project = projects.iterator().next();

				workSet = new WorkSet(name, workSetConcept.getConceptNid(), workSetConcept.getUids(), project.getUids().iterator().next());
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workSet;
	}

	/**
	 * Gets the all partition schemes for refset concept.
	 * 
	 * @param workSetConcept
	 *            the work set concept
	 * @param config
	 *            the config
	 * @return the all partition schemes for refset concept
	 */
	public static List<PartitionScheme> getAllPartitionSchemesForRefsetConcept(I_GetConceptData workSetConcept, I_ConfigAceFrame config) {
		List<PartitionScheme> partitionSchemes = new ArrayList<PartitionScheme>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			List<? extends I_RelTuple> partionSchemeTuples = workSetConcept.getDestRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple loopTuple : partionSchemeTuples) {
				I_GetConceptData loopConcept = termFactory.getConcept(loopTuple.getC1Id());
				I_ConceptAttributePart latestAttributePart = getLastestAttributePart(loopConcept);
				if (isActive(latestAttributePart.getStatusNid())) {
					PartitionScheme loopPartScheme = getPartitionScheme(loopConcept, config);
					if (loopPartScheme != null) {
						partitionSchemes.add(loopPartScheme);
					}
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return partitionSchemes;
	}

	/**
	 * Gets the members not partitioned.
	 * 
	 * @param scheme
	 *            the scheme
	 * @param config
	 *            the config
	 * @return the members not partitioned
	 */
	public static List<I_GetConceptData> getMembersNotPartitioned(PartitionScheme scheme, I_ConfigAceFrame config) {
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

			Collection<? extends I_ExtendByRef> refsetMembers = termFactory.getRefsetExtensionMembers(refset.getConceptNid());

			for (I_ExtendByRef refsetMember : refsetMembers) {
				if (isActive(getLastExtensionPart(refsetMember).getStatusNid())) {
					I_GetConceptData memberConcept = termFactory.getConcept(refsetMember.getComponentNid());
					if (!partitionedMembersIds.contains(memberConcept.getConceptNid())) {
						refsetMembersNotPartitioned.add(memberConcept);
					}
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return refsetMembersNotPartitioned;
	}

	/**
	 * Gets the all partitions for scheme.
	 * 
	 * @param scheme
	 *            the scheme
	 * @param config
	 *            the config
	 * @return the all partitions for scheme
	 */
	public static List<Partition> getAllPartitionsForScheme(PartitionScheme scheme, I_ConfigAceFrame config) {
		List<Partition> partitions = new ArrayList<Partition>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			Set<? extends I_GetConceptData> origins = scheme.getConcept().getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData origin : origins) {
				I_ConceptAttributePart latestAttributePart = getLastestAttributePart(origin);
				if (isActive(latestAttributePart.getStatusNid())) {
					partitions.add(getPartition(origin, config));
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return partitions;
	}

	/**
	 * Removes the refset as exclusion.
	 * 
	 * @param project
	 *            the project
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void removeRefsetAsExclusion(I_TerminologyProject project, I_GetConceptData exclusion, I_ConfigAceFrame config) {
		removeRefsetAsExclusion(project.getConcept(), exclusion, config);
	}

	/**
	 * Removes the refset as exclusion.
	 * 
	 * @param workSet
	 *            the work set
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void removeRefsetAsExclusion(WorkSet workSet, I_GetConceptData exclusion, I_ConfigAceFrame config) {
		removeRefsetAsExclusion(workSet.getConcept(), exclusion, config);
	}

	/**
	 * Removes the refset as exclusion.
	 * 
	 * @param refset
	 *            the refset
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void removeRefsetAsExclusion(I_GetConceptData refset, I_GetConceptData exclusion, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> exclusionRefsetRels = null;
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsetRels = refset.getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : exclusionRefsetRels) {
				if (rel.getC1Id() == refset.getConceptNid() && rel.getC2Id() == exclusion.getConceptNid() && rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(refset);
					// //promote(relVersioned, config);
					// termFactory.addUncommittedNoChecks(refset);
					// termFactory.commit();
				}
			}
			// promote(refset, config);
			// termFactory.addUncommittedNoChecks(refset);
			refset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Removes the refset as source language.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void removeRefsetAsSourceLanguage(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> languageSourceRefsetRels = null;
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			languageSourceRefsetRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : languageSourceRefsetRels) {
				if (rel.getC1Id() == project.getId() && rel.getC2Id() == concept.getConceptNid() && rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(project.getConcept());
					// promote(relVersioned, config);
					// termFactory.addUncommittedNoChecks(project.getConcept());
					// termFactory.commit();
				}
			}
			// promote(project.getConcept(), config);
			// termFactory.addUncommittedNoChecks(project.getConcept());
			project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Removes the refset as common.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void removeRefsetAsCommon(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> exclusionRefsetRels = null;
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsetRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_RelTuple rel : exclusionRefsetRels) {
				if (rel.getC1Id() == project.getId() && rel.getC2Id() == concept.getConceptNid() && rel.getTypeNid() == ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid()) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(project.getConcept());
					// promote(relVersioned, config);
					// termFactory.addUncommittedNoChecks(project.getConcept());
					// termFactory.commit();
				}
			}
			// promote(project.getConcept(),config);
			// termFactory.addUncommittedNoChecks(project.getConcept());
			project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Adds the refset as exclusion.
	 * 
	 * @param refset
	 *            the refset
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void addRefsetAsExclusion(I_GetConceptData refset, I_GetConceptData exclusion, I_ConfigAceFrame config) {
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
				I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), refset, termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.getUids()), exclusion, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(refset);
				refset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// promote(refset, config);
				// termFactory.addUncommittedNoChecks(refset);
				refset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
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
	 * Adds the refset as exclusion.
	 * 
	 * @param project
	 *            the project
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void addRefsetAsExclusion(I_TerminologyProject project, I_GetConceptData exclusion, I_ConfigAceFrame config) {
		addRefsetAsExclusion(project.getConcept(), exclusion, config);
	}

	/**
	 * Adds the refset as exclusion.
	 * 
	 * @param workSet
	 *            the work set
	 * @param exclusion
	 *            the exclusion
	 * @param config
	 *            the config
	 */
	public static void addRefsetAsExclusion(WorkSet workSet, I_GetConceptData exclusion, I_ConfigAceFrame config) {
		addRefsetAsExclusion(workSet.getConcept(), exclusion, config);
	}

	/**
	 * Adds the refset as common.
	 * 
	 * @param project
	 *            the project
	 * @param common
	 *            the common
	 * @param config
	 *            the config
	 */
	public static void addRefsetAsCommon(I_TerminologyProject project, I_GetConceptData common, I_ConfigAceFrame config) {
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
				I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.getUids()), common,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// promote(project.getConcept(), config);
				// termFactory.addUncommittedNoChecks(project.getConcept());
				// termFactory.commit();
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
	 * Adds the refset as source language.
	 * 
	 * @param project
	 *            the project
	 * @param language
	 *            the language
	 * @param config
	 *            the config
	 */
	public static void addRefsetAsSourceLanguage(I_TerminologyProject project, I_GetConceptData language, I_ConfigAceFrame config) {
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
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.getUids()), language,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// promote(project.getConcept(), config);
				// termFactory.addUncommittedNoChecks(project.getConcept());
				// termFactory.commit();
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
	 * Sets the source refset.
	 * 
	 * @param workSet
	 *            the work set
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setSourceRefset(WorkSet workSet, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			List<? extends I_RelTuple> sourceRefsetRels = null;
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			sourceRefsetRels = workSet.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (sourceRefsetRels.size() > 0) {
				for (I_RelTuple rel : sourceRefsetRels) {
					I_RelVersioned relVersioned = rel.getFixedPart();
					for (PathBI editPath : config.getEditingPathSet()) {
						I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						relVersioned.addVersion(newPart);
					}
					termFactory.addUncommittedNoChecks(workSet.getConcept());
					// promote(relVersioned, config);
					// termFactory.addUncommittedNoChecks(workSet.getConcept());
					// termFactory.commit();
				}
				// termFactory.addUncommittedNoChecks(workSet.getConcept());
				// termFactory.commit();
				// promote(workSet.getConcept(), config);
			}
			I_RelVersioned relVersioned = termFactory.newRelationship(UUID.randomUUID(), workSet.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.getUids()), concept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			termFactory.addUncommittedNoChecks(workSet.getConcept());
			workSet.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			// promote(workSet.getConcept(), config);
			// termFactory.addUncommittedNoChecks(workSet.getConcept());
			// termFactory.commit();
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Sets the module id refset.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setModuleIdRefset(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			if ((concept != null && project != null) && (project.getReleasePath() == null) || (concept != null && concept.getConceptNid() != project.getReleasePath().getConceptNid())) {
				List<? extends I_RelTuple> targetRefsetRels = null;
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.localize().getNid());
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.localize().getNid());
				targetRefsetRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (targetRefsetRels.size() > 0) {
					for (I_RelTuple rel : targetRefsetRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						// termFactory.commit();
					}
				}
				// I_RelVersioned newRelationship =
				// termFactory.newRelationship(UUID.randomUUID(),
				// project.getConcept(),
				// termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_MODULE_ID_REFSET_ATTRIBUTE.getUids()),
				// concept,
				// termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
				// termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
				// termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()),
				// 0, config);
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.getUids()), concept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
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
	 * Sets the namespace refset.
	 * 
	 * @param project
	 *            the project
	 * @param namespaceText
	 *            the namespace text
	 * @param config
	 *            the config
	 */
	public static void setNamespaceRefset(I_TerminologyProject project, String namespaceText, I_ConfigAceFrame config) {
		try {
			I_TermFactory termFactory = Terms.get();

			I_GetConceptData projectConcept = project.getConcept();
			// I_GetConceptData namespaceRefset =
			// termFactory.getConcept(ArchitectonicAuxiliary.Concept.PROJECT_NAMESPACE_REFSET.localize().getNid());
			I_GetConceptData namespaceRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ALEJANDRO_RODRIGUEZ.localize().getNid());

			RefexCAB newBluePrint = new RefexCAB(TK_REFEX_TYPE.CID_STR, projectConcept.getNid(), namespaceRefset.getNid());
			newBluePrint.put(RefexProperty.CNID1, ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			newBluePrint.put(RefexProperty.STRING1, namespaceText);
			Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate()).constructIfNotCurrent(newBluePrint);
			termFactory.addUncommittedNoChecks(namespaceRefset);
			namespaceRefset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Sets the release path refset.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setReleasePathRefset(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			if ((concept != null) && (project.getReleasePath() == null) || (concept != null && concept.getConceptNid() != project.getReleasePath().getConceptNid())) {
				List<? extends I_RelTuple> targetRefsetRels = null;
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_RELEASE_PATH_REFSET_ATTRIBUTE.localize().getNid());
				targetRefsetRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (targetRefsetRels.size() > 0) {
					for (I_RelTuple rel : targetRefsetRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
					}
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_RELEASE_PATH_REFSET_ATTRIBUTE.getUids()), concept,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
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
	 * Sets the language target refset.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setLanguageTargetRefset(TranslationProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getTargetLanguageRefset() == null) || (concept.getConceptNid() != project.getTargetLanguageRefset().getConceptNid())) {
				List<? extends I_RelTuple> targetRefsetRels = null;
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
				targetRefsetRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (targetRefsetRels.size() > 0) {
					for (I_RelTuple rel : targetRefsetRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						// Thread.sleep(100);
						// // promote(project.getConcept(), config);
						// //
						// termFactory.addUncommittedNoChecks(project.getConcept());
						// // termFactory.commit();
					}
					// promote(project.getConcept(), config);
					// termFactory.addUncommittedNoChecks(project.getConcept());
					// termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.getUids()), concept,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// Thread.sleep(100);
				// promote(project.getConcept(), config);
				// termFactory.addUncommittedNoChecks(project.getConcept());
				// termFactory.commit();
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
	 * Gets the source issue repo for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the source issue repo for project
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getSourceIssueRepoForProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> repos = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> reposTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.localize().getNid());
			reposTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			reposTuples = cleanRelTuplesList(reposTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of source repos.");
		}
	}

	/**
	 * Sets the source issue repo.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setSourceIssueRepo(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getSourceIssueRepo() == null) || (concept.getConceptNid() != project.getSourceIssueRepo().getConceptNid())) {
				List<? extends I_RelTuple> reposRels = null;
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.localize().getNid());
				reposRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (reposRels.size() > 0) {
					for (I_RelTuple rel : reposRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						// promote(relVersioned, config);
						// termFactory.addUncommittedNoChecks(project.getConcept());
						// termFactory.commit();
					}
					// promote(project.getConcept(), config);
					// termFactory.addUncommittedNoChecks(project.getConcept());
					// termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_SOURCE_DEFECTS_ISSUE_REPO.getUids()), concept,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// promote(project.getConcept(), config);
				// termFactory.addUncommittedNoChecks(project.getConcept());
				// termFactory.commit();
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
	 * Gets the project issue repo for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the project issue repo for project
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getProjectIssueRepoForProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> repos = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> reposTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.localize().getNid());
			reposTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			reposTuples = cleanRelTuplesList(reposTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of source repos.");
		}
	}

	/**
	 * Sets the project issue repo.
	 * 
	 * @param project
	 *            the project
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 */
	public static void setProjectIssueRepo(I_TerminologyProject project, I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();

		try {
			if ((concept != null) && (project.getProjectIssueRepo() == null) || (concept.getConceptNid() != project.getProjectIssueRepo().getConceptNid())) {
				List<? extends I_RelTuple> reposRels = null;
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.localize().getNid());
				reposRels = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (reposRels.size() > 0) {
					for (I_RelTuple rel : reposRels) {
						I_RelVersioned relVersioned = rel.getFixedPart();
						for (PathBI editPath : config.getEditingPathSet()) {
							I_RelPart newPart = (I_RelPart) rel.getMutablePart().makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							relVersioned.addVersion(newPart);
						}
						termFactory.addUncommittedNoChecks(project.getConcept());
						// promote(relVersioned, config);
						// termFactory.addUncommittedNoChecks(project.getConcept());
						// termFactory.commit();
					}
					// promote(project.getConcept(), config);
					// termFactory.addUncommittedNoChecks(project.getConcept());
					// termFactory.commit();
				}
				I_RelVersioned newRelationship = termFactory.newRelationship(UUID.randomUUID(), project.getConcept(), termFactory.getConcept(ArchitectonicAuxiliary.Concept.HAS_PROJECT_ISSUE_REPO.getUids()), concept,
						termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

				termFactory.addUncommittedNoChecks(project.getConcept());
				project.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				// promote(project.getConcept(), config);
				// termFactory.addUncommittedNoChecks(project.getConcept());
				// termFactory.commit();
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
	 * Gets the source refset for work set.
	 * 
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @return the source refset for work set
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getSourceRefsetForWorkSet(WorkSet workSet, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> sourceRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> sourceRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_SOURCE_REFSET_ATTRIBUTE.localize().getNid());
			sourceRefsetsTuples = workSet.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of source refsets.");
		}
	}

	/**
	 * Gets the release path refset for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the release path refset for project
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getReleasePathForProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_RELEASE_PATH_REFSET_ATTRIBUTE.localize().getNid());
			targetRefsetsTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of target refsets.");
		}

	}

	/**
	 * Gets the module id refset for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the module id refset for project
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getModuleIdRefsetForProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_MODULE_ID_REFSET_ATTRIBUTE.localize().getNid());
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.localize().getNid());
			targetRefsetsTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of target refsets.");
		}
	}

	/**
	 * Gets the namespace refset for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the namespace refset for project
	 * @throws Exception
	 *             the exception
	 */
	public static String getNamespaceRefsetForProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		String namespaceStr = "";
		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			// allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.PROJECT_NAMESPACE_REFSET.localize().getNid());
			ConceptChronicleBI rodriguez = Ts.get().getConcept(ArchitectonicAuxiliary.Concept.ALEJANDRO_RODRIGUEZ.localize().getNid());
			Collection<? extends RefexVersionBI<?>> members = rodriguez.getRefsetMembersActive(config.getViewCoordinate());
			for (RefexVersionBI<?> member : members) {
				if (member.getReferencedComponentNid() == project.getConcept().getNid()) {
					RefexNidStringVersionBI namespaceBi = (RefexNidStringVersionBI) member;
					namespaceStr = namespaceBi.getString1();
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return namespaceStr;
	}

	/**
	 * Gets the target language refset for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the target language refset for project
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getTargetLanguageRefsetForProject(TranslationProject project, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
			targetRefsetsTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of target refsets.");
		}
	}

	/**
	 * Gets the exclusion refsets for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the exclusion refsets for project
	 */
	public static List<I_GetConceptData> getExclusionRefsetsForProject(I_TerminologyProject project, I_ConfigAceFrame config) {

		return getExclusionRefsetsForConcept(project.getConcept(), config);

	}

	/**
	 * Gets the exclusion refsets for work set.
	 * 
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @return the exclusion refsets for work set
	 */
	public static List<I_GetConceptData> getExclusionRefsetsForWorkSet(WorkSet workSet, I_ConfigAceFrame config) {

		return getExclusionRefsetsForConcept(workSet.getConcept(), config);

	}

	/**
	 * Gets the exclusion refsets for concept.
	 * 
	 * @param refset
	 *            the refset
	 * @param config
	 *            the config
	 * @return the exclusion refsets for concept
	 */
	public static List<I_GetConceptData> getExclusionRefsetsForConcept(I_GetConceptData refset, I_ConfigAceFrame config) {
		List<? extends I_RelTuple> exclusionRefsets = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_EXCLUSION_REFSET_ATTRIBUTE.localize().getNid());
			exclusionRefsets = refset.getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			exclusionRefsets = cleanRelTuplesList(exclusionRefsets);

			if (exclusionRefsets != null) {
				for (I_RelTuple loopTuple : exclusionRefsets) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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
	 * Gets the source language refsets for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the source language refsets for project
	 */
	public static List<I_GetConceptData> getSourceLanguageRefsetsForProject(I_TerminologyProject project, I_ConfigAceFrame config) {
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());

			List<? extends I_RelTuple> sourceLanguageRefsetsTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			sourceLanguageRefsetsTuples = cleanRelTuplesList(sourceLanguageRefsetsTuples);

			if (sourceLanguageRefsetsTuples != null) {
				for (I_RelTuple loopTuple : sourceLanguageRefsetsTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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
	 * Gets the common refsets for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the common refsets for project
	 */
	public static List<I_GetConceptData> getCommonRefsetsForProject(I_TerminologyProject project, I_ConfigAceFrame config) {
		List<? extends I_RelTuple> commonRefsetsRelTuples = null;
		ArrayList<I_GetConceptData> returnData = new ArrayList<I_GetConceptData>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_COMMON_REFSET_ATTRIBUTE.localize().getNid());
			commonRefsetsRelTuples = project.getConcept().getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			commonRefsetsRelTuples = cleanRelTuplesList(commonRefsetsRelTuples);

			if (commonRefsetsRelTuples != null) {
				for (I_RelTuple loopTuple : commonRefsetsRelTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(termFactory.getConcept(loopTuple.getC2Id()));
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
	 * Gets the partition scheme.
	 * 
	 * @param partitionSchemeConcept
	 *            the partition scheme concept
	 * @param config
	 *            the config
	 * @return the partition scheme
	 */
	public static PartitionScheme getPartitionScheme(I_GetConceptData partitionSchemeConcept, I_ConfigAceFrame config) {
		PartitionScheme partitionScheme = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = partitionSchemeConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Partition scheme wrong nomber of worksets...");
			}

			I_GetConceptData parent = parents.iterator().next();

			String name = getConceptString(partitionSchemeConcept);
			List<? extends I_DescriptionTuple> descTuples = partitionSchemeConcept.getDescriptionTuples(config.getAllowedStatus(), config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_DescriptionTuple tuple : descTuples) {
				if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
					name = tuple.getText();
				}
			}

			if (getWorkSet(parent, config) != null || getPartition(parent, config) != null) {
				partitionScheme = new PartitionScheme(name, partitionSchemeConcept.getConceptNid(), partitionSchemeConcept.getUids(), parent.getUids().iterator().next());
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return partitionScheme;
	}

	/**
	 * Gets the partition.
	 * 
	 * @param partitionConcept
	 *            the partition concept
	 * @param config
	 *            the config
	 * @return the partition
	 */
	public static Partition getPartition(I_GetConceptData partitionConcept, I_ConfigAceFrame config) {
		Partition partition = TerminologyProjectDAO.partitionCache.get(partitionConcept.getPrimUuid());
		if (partition != null) {
			return partition;
		}
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			Set<? extends I_GetConceptData> parents = partitionConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in partition...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.localize().getNid()) {
				allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> schemes = partitionConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (schemes.size() != 1) {
					throw new Exception("Error: Wrong number of schemes in partition...");
				}

				String name = getConceptString(partitionConcept);
				List<? extends I_DescriptionTuple> descTuples = partitionConcept.getDescriptionTuples(config.getAllowedStatus(), (config.getDescTypes().getSetValues().length == 0) ? null : config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME,
						config.getConflictResolutionStrategy());
				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}

				I_GetConceptData scheme = schemes.iterator().next();

				partition = new Partition(name, partitionConcept.getConceptNid(), partitionConcept.getUids(), scheme.getUids().iterator().next());
				partition.setConcept(partitionConcept);
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		if (partition != null) {
			TerminologyProjectDAO.partitionCache.put(partitionConcept.getPrimUuid(), partition);
		}
		return partition;
	}

	/**
	 * Gets the all work sets for project.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * 
	 * @return the all work sets for project
	 */
	public static List<WorkSet> getAllWorkSetsForProject(I_TerminologyProject project, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkSet> workSets = new ArrayList<WorkSet>();
		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());
			Set<? extends I_GetConceptData> children = project.getConcept().getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workSets;
	}

	/**
	 * Adds the concept as work set member.
	 * 
	 * @param workSetMemberConcept
	 *            the work set member concept
	 * @param workSetUUID
	 *            the work set uuid
	 * @param config
	 *            the config
	 */
	public static void addConceptAsWorkSetMember(I_GetConceptData workSetMemberConcept, UUID workSetUUID, I_ConfigAceFrame config) {
		try {
			WorkSetMember member = new WorkSetMember(getConceptString(workSetMemberConcept), workSetMemberConcept.getConceptNid(), workSetMemberConcept.getUids(), workSetUUID);
			addConceptAsWorkSetMember(member, config);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * Adds the concept as work set member.
	 * 
	 * @param member
	 *            the member
	 * @param config
	 *            the config
	 */
	public static void addConceptAsWorkSetMember(WorkSetMember member, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = termFactory.getConcept(member.getUids());
			I_GetConceptData workSetConcept = termFactory.getConcept(member.getWorkSetUUID());
			boolean alreadyMember = false;
			Collection<? extends RefexChronicleBI<?>> members = newMemberConcept.getAnnotations();
			for (RefexChronicleBI<?> promotionMember : members) {
				if (promotionMember.getRefexNid() == workSetConcept.getConceptNid()) {
					alreadyMember = true;
					I_ExtendByRef extension = termFactory.getExtension(promotionMember.getNid());
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					if (isInactive(part.getStatusNid())) {
						for (PathBI editPath : config.getEditingPathSet()) {
							I_ExtendByRefPartStr newStringPart = (I_ExtendByRefPartStr) part.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							extension.addVersion(newStringPart);
						}
						termFactory.addUncommittedNoChecks(workSetConcept);
						// WorkSet workset =
						// TerminologyProjectDAO.getWorkSet(workSetConcept,
						// config);
						// PromotionRefset promRef =
						// workset.getPromotionRefset(config);
						// promRef.setPromotionStatus(newMemberConcept.getNid(),
						// SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
						// termFactory.addUncommittedNoChecks(newMemberConcept);
					}
				}
			}

			if (!alreadyMember) {
				if (refsetHelper == null) {
					refsetHelper = termFactory.getRefsetHelper(config);
				}
				refsetHelper.newRefsetExtension(workSetConcept.getConceptNid(), newMemberConcept.getConceptNid(), EConcept.REFSET_TYPES.STR, new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""), config);
				// WorkSet workset =
				// TerminologyProjectDAO.getWorkSet(workSetConcept, config);
				termFactory.addUncommittedNoChecks(workSetConcept);
				// PromotionRefset promRef = workset.getPromotionRefset(config);
				// promRef.setPromotionStatus(newMemberConcept.getNid(),
				// SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
				// termFactory.addUncommittedNoChecks(newMemberConcept);
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Initialize work set.
	 * 
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @param updater
	 *            the updater
	 * @throws Exception
	 *             the exception
	 */
	public static void initializeWorkSet(WorkSet workSet, I_ConfigAceFrame config, ActivityUpdater updater) throws Exception {
		WorksetInitializerProcessor initWorker = new WorksetInitializerProcessor(workSet, workSet.getSourceRefset(), config, updater);
		Ts.get().iterateConceptDataInParallel(initWorker);
		updater.setTaskMessage("Included: " + initWorker.getIncludedCounter() + " Excluded: " + initWorker.getExcludedByPolicyCounter());
		updater.finish();
	}

	/**
	 * Initialize work list.
	 * 
	 * @param partition
	 *            the partition
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @param updater
	 *            the updater
	 * @throws Exception
	 *             the exception
	 */
	public static void initializeWorkList(Partition partition, WorkList workList, I_ConfigAceFrame config, ActivityUpdater updater) throws Exception {
		Ts.get().iterateConceptDataInParallel(new WorklistInitializerProcessor(partition, workList, config, updater));
		if (updater != null) {
			updater.finish();
		}
	}

	/**
	 * Update work list metadata.
	 * 
	 * @param workListWithMetadata
	 *            the work list with metadata
	 * @param config
	 *            the config
	 * @return the work list
	 */
	public static WorkList updateWorkListMetadata(WorkList workListWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkList workList = null;

		try {
			I_GetConceptData workListExtensionRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());

			I_GetConceptData workListConcept = termFactory.getConcept(workListWithMetadata.getUids());

			WorkList currentVersionOfWorkList = getWorkList(workListWithMetadata.getConcept(), config);

			if (!currentVersionOfWorkList.getName().equals(workListWithMetadata.getName())) {
				updatePreferredTerm(workListWithMetadata.getConcept(), workListWithMetadata.getName(), config);
			}

			WorklistMetadata worklistMetadata = new WorklistMetadata(workListWithMetadata.getName(), workListWithMetadata.getUids(), workListWithMetadata.getPartitionUUID(), workListWithMetadata.getWorkflowDefinitionFileName(),
					convertToStringListOfMembers(workListWithMetadata.getWorkflowUserRoles()));
			String metadata = serialize(worklistMetadata);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workListConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workListExtensionRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workListExtensionRefset);
					termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(workListExtensionRefset);
					// termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
				}
			}
			waiting(1);
			workList = getWorkList(workListConcept, config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		TerminologyProjectDAO.workListCache.put(workList.getUids().iterator().next(), workList);
		return workList;
	}

	/**
	 * Delete work set members.
	 * 
	 * @param workSetWithMetadata
	 *            the work set with metadata
	 * @param config
	 *            the config
	 * @return the work set
	 */
	public static WorkSet deleteWorkSetMembers(WorkSet workSetWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkSet workSet = null;

		try {
			I_GetConceptData workSetExtensionRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKSET_EXTENSION_REFSET.getUids());

			I_GetConceptData workSetConcept = termFactory.getConcept(workSetWithMetadata.getUids());

			String metadata = serialize(workSetWithMetadata);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workSetConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workSetExtensionRefset.getConceptNid()) {
					Collection<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(config.getConflictResolutionStrategy());
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) extTuples.iterator().next()
								.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workSetExtensionRefset);
					termFactory.addUncommittedNoChecks(extension);
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(workSetExtensionRefset);
					// termFactory.addUncommittedNoChecks(extension);
					workSetExtensionRefset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				}
			}
			workSet = getWorkSet(workSetConcept, config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return workSet;
	}

	/**
	 * Retire work set member.
	 * 
	 * @param workSetMember
	 *            the work set member
	 */
	public static void retireWorkSetMember(WorkSetMember workSetMember) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_GetConceptData worksetConcept = termFactory.getConcept(workSetMember.getWorkSetUUID());
			// WorkSet workSet = getWorkSet(worksetConcept, config);
			I_GetConceptData workSetMemberConcept = termFactory.getConcept(workSetMember.getId());

			String metadata = serialize(workSetMember);

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workSetMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == worksetConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(worksetConcept);
					termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(worksetConcept);
					// termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
				}
			}

			// TODO: implement recursive retiring??

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return;
	}

	/**
	 * Retire work list member.
	 * 
	 * @param workListMember
	 *            the work list member
	 */
	public static void retireWorkListMember(WorkListMember workListMember) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_GetConceptData workListConcept = termFactory.getConcept(workListMember.getWorkListUUID());
			I_GetConceptData workListMemberConcept = termFactory.getConcept(workListMember.getId());

			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(workListMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == workListConcept.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPart part = (I_ExtendByRefPart) lastPart.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workListConcept);
					termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(workListConcept);
					// termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return;
	}

	/**
	 * Update work set member metadata.
	 * 
	 * @param workSetMemberWithMetadata
	 *            the work set member with metadata
	 * @param config
	 *            the config
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
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(workSetConcept);
					termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(workSetConcept);
					// termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
				}
			}
			waiting(1);
			workSetMember = getWorkSetMember(workSetMemberWithMetadata.getConcept(), workSetConcept.getConceptNid(), config);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return workSetMember;
	}

	/**
	 * Gets the work set member.
	 * 
	 * @param workSetMemberConcept
	 *            the work set member concept
	 * @param worksetId
	 *            the workset id
	 * @param config
	 *            the config
	 * 
	 * @return the work set member
	 */
	public static WorkSetMember getWorkSetMember(I_GetConceptData workSetMemberConcept, int worksetId, I_ConfigAceFrame config) {
		WorkSetMember workSetMember = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData workSetRefset = termFactory.getConcept(worksetId);
			WorkSet workset = TerminologyProjectDAO.getWorkSet(workSetRefset, config);
			PromotionRefset promRefset = workset.getPromotionRefset(config);

			Collection<? extends RefexChronicleBI<?>> members = workSetMemberConcept.getAnnotations();
			for (RefexChronicleBI<?> promotionMember : members) {
				if (promotionMember.getRefexNid() == promRefset.getRefsetId()) {
					workSetMember = new WorkSetMember(getConceptString(workSetMemberConcept), workSetMemberConcept.getConceptNid(), workSetMemberConcept.getUids(), workSetRefset.getUids().iterator().next());
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		if (workSetMember == null) {
			AceLog.getAppLog().log(Level.WARNING, "NULL workset member: " + workSetMemberConcept.toString() + " UUID: " + workSetMemberConcept.getPrimUuid());
		}
		return workSetMember;
	}

	/**
	 * Gets the partition member.
	 * 
	 * @param partitionMemberConcept
	 *            the partition member concept
	 * @param partitionId
	 *            the partition id
	 * @param config
	 *            the config
	 * @return the partition member
	 */
	public static PartitionMember getPartitionMember(I_GetConceptData partitionMemberConcept, int partitionId, I_ConfigAceFrame config) {
		PartitionMember partitionMember = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_GetConceptData partitionRefset = termFactory.getConcept(partitionId);
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(partitionMemberConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == partitionRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_GetConceptData component = termFactory.getConcept(extension.getComponentNid());
					partitionMember = new PartitionMember(getConceptString(component), component.getConceptNid(), component.getUids(), partitionRefset.getUids().iterator().next());
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return partitionMember;
	}

	/**
	 * Gets the all work set members.
	 * 
	 * @param workset
	 *            the workset
	 * @param config
	 *            the config
	 * 
	 * @return the all work set members
	 */
	public static List<WorkSetMember> getAllWorkSetMembers(WorkSet workset, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkSetMember> workSetMembers = new ArrayList<WorkSetMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions = termFactory.getRefsetExtensionMembers(workset.getId());
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					WorkSetMember workSetMember = getWorkSetMember(termFactory.getConcept(extension.getComponentNid()), workset.getId(), config);
					if (workSetMember != null) {
						workSetMembers.add(workSetMember);
					}
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workSetMembers;
	}

	/**
	 * Gets the all work set members.
	 * 
	 * @param workset
	 *            the workset
	 * @param config
	 *            the config
	 * 
	 * @return the all work set members
	 */
	public static List<WorkSetMember> getWorkSetMembersByCount(WorkSet workset, I_ConfigAceFrame config, int count) {
		I_TermFactory termFactory = Terms.get();
		List<WorkSetMember> workSetMembers = new ArrayList<WorkSetMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions = termFactory.getRefsetExtensionMembers(workset.getId());
			int i = 0;
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					workSetMembers.add(getWorkSetMember(termFactory.getConcept(extension.getComponentNid()), workset.getId(), config));
					i++;
				}
				if (i == count) {
					break;
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workSetMembers;
	}

	/**
	 * Gets the all partition members.
	 * 
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 * @return the all partition members
	 */
	public static List<PartitionMember> getAllPartitionMembers(Partition partition, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<PartitionMember> partitionMembers = new ArrayList<PartitionMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions = termFactory.getRefsetExtensionMembers(partition.getId());
			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
			for (I_ExtendByRef extension : membersExtensions) {
				I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
				if (isActive(lastPart.getStatusNid())) {
					members.add(termFactory.getConcept(extension.getComponentNid()));
				}
			}
			for (I_GetConceptData member : members) {
				partitionMembers.add(getPartitionMember(member, partition.getId(), config));

			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return partitionMembers;
	}

	// WorkLists CRUD **********************************
	/**
	 * Gets the all work lists for work set.
	 * 
	 * @param refset
	 *            the refset
	 * @param config
	 *            the config
	 * @return the all work lists for work set
	 */
	public static List<WorkList> getAllWorkListsForRefset(I_GetConceptData refset, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		List<WorkList> workLists = new ArrayList<WorkList>();
		try {
			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(includesFromAttribute.getConceptNid());

			Set<? extends I_GetConceptData> origins = refset.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			for (I_GetConceptData origin : origins) {
				I_ConceptAttributePart lastAttributePart = getLastestAttributePart(origin);
				if (isActive(lastAttributePart.getStatusNid())) {
					WorkList workList = getWorkList(origin, config);
					workLists.add(workList);
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workLists;
	}

	/**
	 * Gets the all worklist for workset.
	 * 
	 * @param workset
	 *            the workset
	 * @param config
	 *            the config
	 * @return the all worklist for workset
	 */
	public static List<WorkList> getAllWorklistForWorkset(WorkSet workset, I_ConfigAceFrame config) {
		List<WorkList> result = new ArrayList<WorkList>();
		List<PartitionScheme> partitionSchemes = getAllPartitionSchemesForRefsetConcept(workset.getConcept(), config);
		for (PartitionScheme partitionScheme : partitionSchemes) {
			result.addAll(getAllPartitionsRecursive(partitionScheme, config));
			// List<PartitionScheme> subPartitionSchemes =
			// getAllPartitionSchemes(partitionScheme, config);
			// for (PartitionScheme subPartitionScheme : subPartitionSchemes) {
			// List<Partition> partitions =
			// getAllPartitionsForScheme(subPartitionScheme, config);
			// for (Partition partition : partitions) {
			// List<WorkList> worklists =
			// getAllWorkListsForRefset(partition.getConcept(), config);
			// for (WorkList workList : worklists) {
			// result.add(workList);
			// }
			// }
			// }

		}
		return result;
	}

	/**
	 * Gets the all partitions recursive.
	 * 
	 * @param partitionScheme
	 *            the partition scheme
	 * @param config
	 *            the config
	 * @return the all partitions recursive
	 */
	private static List<WorkList> getAllPartitionsRecursive(PartitionScheme partitionScheme, I_ConfigAceFrame config) {
		List<WorkList> result = new ArrayList<WorkList>();
		List<Partition> partitions = getAllPartitionsForScheme(partitionScheme, config);
		for (Partition partition : partitions) {
			List<WorkList> wls = getAllWorkListsForRefset(partition.getConcept(), config);
			for (WorkList workList : wls) {
				if (workList != null) {
					result.add(workList);
				}
			}
			List<PartitionScheme> subPartitionSchemes = getAllPartitionSchemesForRefsetConcept(partition.getConcept(), config);
			for (PartitionScheme subPartitionScheme : subPartitionSchemes) {
				if (subPartitionScheme != null) {
					result.addAll(getAllPartitionsRecursive(subPartitionScheme, config));
				}
			}
		}
		return result;
	}

	/**
	 * Gets the project from parent.
	 * 
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 * @return the project from parent
	 */
	private static I_TerminologyProject getProjectFromParent(I_GetConceptData concept, I_ConfigAceFrame config) {
		I_TerminologyProject project = null;
		try {
			// System.out.println(concept.toString());
			project = getProject(concept, config);
		} catch (Exception e1) {
			// do nothing
			// System.out.println(concept.toString());
		}
		if (project == null) {
			try {
				I_TermFactory termFactory = Terms.get();
				I_IntSet allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.localize().getNid());
				Set<? extends I_GetConceptData> parents = concept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

				if (parents.size() > 1) {
					throw new Exception("Error: Wrong number of parents...");
				}

				if (parents.size() == 0) {
					allowedDestRelTypes = termFactory.newIntSet();
					allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
					parents = concept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
				}

				I_GetConceptData parent = parents.iterator().next();
				project = getProjectFromParent(parent, config);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}

		return project;
	}

	/**
	 * Gets the project for worklist.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @return the project for worklist
	 */
	public static I_TerminologyProject getProjectForWorklist(WorkList workList, I_ConfigAceFrame config) {

		return getProjectFromParent(workList.getConcept(), config);
	}

	/**
	 * Gets the work list.
	 * 
	 * @param workListConcept
	 *            the work list concept
	 * @param config
	 *            the config
	 * 
	 * @return the work list
	 */
	public static WorkList getWorkList(I_GetConceptData workListConcept, I_ConfigAceFrame config) {
		WorkList workList = TerminologyProjectDAO.workListCache.get(workListConcept.getPrimUuid());
		if (workList != null) {
			return workList;
		}

		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			I_IntSet allowedStatuses = config.getAllowedStatus();
			allowedStatuses.remove(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			Set<? extends I_GetConceptData> parents = workListConcept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			if (parents.size() != 1) {
				throw new Exception("Error: Wrong number of parents in workList...");
			}

			I_GetConceptData parent = parents.iterator().next();

			if (parent.getConceptNid() == ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.localize().getNid()) {
				String name = getConceptString(workListConcept);
				List<? extends I_DescriptionTuple> descTuples = workListConcept.getDescriptionTuples(config.getAllowedStatus(), (config.getDescTypes().getSetValues().length == 0) ? null : config.getDescTypes(), config.getViewPositionSetReadOnly(), Precedence.TIME,
						config.getConflictResolutionStrategy());

				for (I_DescriptionTuple tuple : descTuples) {
					if (tuple.getTypeNid() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()) {
						name = tuple.getText();
					}
				}

				I_GetConceptData workListRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());
				allowedDestRelTypes = termFactory.newIntSet();
				allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
				I_IntSet descriptionTypes = termFactory.newIntSet();
				descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
				WorklistMetadata deserializedWorkListMetadata = null;
				for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workListRefset.getConceptNid())) {
					if (extension.getComponentNid() == workListConcept.getConceptNid()) {
						I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
						String metadata = part.getStringValue();
						// deserializedWorkListWithMetadata = (WorkList)
						// deserialize(metadata);
						deserializedWorkListMetadata = (WorklistMetadata) deserialize(metadata);
					}
				}
				if (deserializedWorkListMetadata != null) {
					workList = WorkList.getInstanceFromMetadata(deserializedWorkListMetadata);
				}
			}
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		if (workList != null) {
			TerminologyProjectDAO.workListCache.put(workList.getUids().iterator().next(), workList);
		}
		return workList;
	}

	/**
	 * Gets the non assigned changes work set.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the non assigned changes work set
	 */
	public static WorkSet getNonAssignedChangesWorkSet(I_TerminologyProject project, I_ConfigAceFrame config) {
		WorkSet nacWorkSet = null;
		String nacWorkSetName = "Maintenance - ";

		for (WorkSet loopWorkSet : project.getWorkSets(config)) {
			if (loopWorkSet.getName().startsWith(nacWorkSetName)) {
				nacWorkSet = loopWorkSet;
			}
		}

		return nacWorkSet;
	}

	/**
	 * Creates the new nac work list.
	 * 
	 * @param name
	 *            the name
	 * @param bp
	 *            the bp
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the work list
	 * @throws Exception
	 *             the exception
	 */
	@Deprecated
	public static WorkList createNewNacWorkList(String name, BusinessProcess bp, I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		WorkList newNacWorkList = null;
		WorkSet nacWorkSet = getNonAssignedChangesWorkSet(project, config);

		Partition nacWorkListPartition = createNewPartition(name, nacWorkSet.getPartitionSchemes(config).iterator().next().getUids().iterator().next(), config);

		newNacWorkList = new WorkList(name, 0, null, nacWorkListPartition.getUids().iterator().next());

		WorkList returnWorkList = createNewWorkList(newNacWorkList, config);

		return returnWorkList;
	}

	public static void createNewNacWorkList(I_TerminologyProject project, WorkflowDefinition workflowDefinition, ArrayList<WfMembership> workflowUserRoles, String name, I_ConfigAceFrame config, I_ShowActivity activity) throws Exception {
		WorkList newNacWorkList = null;
		WorkSet nacWorkSet = getNonAssignedChangesWorkSet(project, config);

		Partition nacWorkListPartition = createNewPartition(name, nacWorkSet.getPartitionSchemes(config).iterator().next().getUids().iterator().next(), config);

		newNacWorkList = new WorkList(name, 0, null, nacWorkListPartition.getUids().iterator().next());
		newNacWorkList.setWorkflowDefinition(workflowDefinition);
		newNacWorkList.setWorkflowUserRoles(workflowUserRoles);

		WorkList returnWorkList = createNewWorkList(newNacWorkList, config);
		ActivityUpdater updater = null;
		if (activity != null) {
			updater = new ActivityUpdater(activity, "Generating WorkList");
		}
		if (returnWorkList != null) {
			initializeWorkList(nacWorkListPartition, returnWorkList, config, updater);
		}
		Terms.get().addUncommittedNoChecks(returnWorkList.getConcept());
		returnWorkList.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

		TerminologyProjectDAO.workListCache.put(returnWorkList.getUids().iterator().next(), returnWorkList);

	}

	/**
	 * Gets the all nac work lists.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the all nac work lists
	 */
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

	public static WorkListMember addConceptAsNacWorklistMember(WorkList workList, I_GetConceptData concept, I_ConfigAceFrame config) throws IOException {
		return addConceptAsNacWorklistMember(workList, concept, config, true);
	}

	/**
	 * Adds the concept as nac worklist member.
	 * 
	 * @param workList
	 *            the work list
	 * @param concept
	 *            the concept
	 * @param destination
	 *            the destination
	 * @param config
	 *            the config
	 * @param commit
	 * @return the work list member
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static WorkListMember addConceptAsNacWorklistMember(WorkList workList, I_GetConceptData concept, I_ConfigAceFrame config, boolean commit) throws IOException {
		try {
			WorkSet nacWorkSet = getNonAssignedChangesWorkSet(getProjectForWorklist(workList, config), config);
			addConceptAsWorkSetMember(concept, nacWorkSet.getUids().iterator().next(), config);
			Partition partition = workList.getPartition();
			addConceptAsPartitionMember(concept, partition, config);
			Terms.get().addUncommittedNoChecks(partition.getConcept());
			//I_GetConceptData assingStatus = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids());
			I_GetConceptData assingStatus;
	        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
	        	assingStatus = Terms.get().getConcept(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"));
	        } else {
	        	assingStatus = Terms.get().getConcept(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"));
	        }
			WorkListMember workListMember = new WorkListMember(getConceptString(concept), concept.getConceptNid(), concept.getUids(), workList.getUids().iterator().next(), assingStatus, new java.util.Date().getTime());
			WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
			WfComponentProvider provider = new WfComponentProvider();
			WfInstance wfInstance = new WfInstance();
			wfInstance.setComponentId(Terms.get().nidToUuid(concept.getNid()));
			wfInstance.setWfDefinition(workList.getWorkflowDefinition());
			wfInstance.setWorkList(workList);
			wfInstance.setComponentName(getConceptString(concept));
			wfInstance.setState(provider.statusConceptToWfState(assingStatus));
			wfInstance.setLastChangeTime(System.currentTimeMillis());
			WfUser destination = interpreter.getNextDestination(wfInstance, workList);
			if (destination == null) {
				destination = new WfUser("user", ArchitectonicAuxiliary.Concept.USER.getPrimoridalUid());
			}
			addConceptAsWorkListMember(workListMember, Terms.get().uuidToNative(destination.getId()), config);
			if (commit) {
				if (concept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD)) {
					nacWorkSet.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
					partition.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
					workList.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return getWorkListMember(concept, workList, config);
	}

	/**
	 * Creates the new work list.
	 * 
	 * @param workListWithMetadata
	 *            the work list with metadata
	 * @param config
	 *            the config
	 * @return the work list
	 * @throws Exception
	 *             the exception
	 */
	public static WorkList createNewWorkList(WorkList workListWithMetadata, I_ConfigAceFrame config) throws Exception {
		I_TermFactory termFactory = Terms.get();
		WorkList workList = null;
		I_GetConceptData newConcept = null;
		String name = workListWithMetadata.getName();
		// workListWithMetadata.getBusinessProcess().setDestination(
		// workListWithMetadata.getDestination());

		String worklistName = workListWithMetadata.getName() + " (worklist)";

		if (isConceptDuplicate(worklistName)) {
			JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
			throw new Exception("This name is already in use");
		}
		I_GetConceptData workListsRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.getUids());

		I_GetConceptData workListRefset = termFactory.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids());

		I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

		I_GetConceptData commentsRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.COMMENTS_REL.getUids());
		I_GetConceptData promotionRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.PROMOTION_REL.getUids());
		I_GetConceptData purposeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.getUids());
		I_GetConceptData typeRelConcept = termFactory.getConcept(RefsetAuxiliary.Concept.REFSET_TYPE_REL.getUids());
		I_GetConceptData defining = termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids());
		I_GetConceptData refinability = termFactory.getConcept(ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids());
		I_GetConceptData current = termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());

		I_GetConceptData partition = termFactory.getConcept(workListWithMetadata.getPartitionUUID());

		newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

		termFactory.newDescription(UUID.randomUUID(), newConcept, "en", worklistName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);

		termFactory.newDescription(UUID.randomUUID(), newConcept, "en", worklistName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);

		termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), workListsRoot, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

		termFactory.newRelationship(UUID.randomUUID(), newConcept, includesFromAttribute, partition, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

		I_GetConceptData newCommentsConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
		termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en", name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
		termFactory.newDescription(UUID.randomUUID(), newCommentsConcept, "en", name + " - comments refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
		termFactory.newRelationship(UUID.randomUUID(), newCommentsConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), workListsRoot, defining, refinability, current, 0, config);
		termFactory.newRelationship(UUID.randomUUID(), newConcept, commentsRelConcept, newCommentsConcept, defining, refinability, current, 0, config);

		I_GetConceptData newPromotionConcept = termFactory.newConcept(UUID.randomUUID(), false, config);
		newPromotionConcept.setAnnotationStyleRefex(true);
		termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en", name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()), config);
		termFactory.newDescription(UUID.randomUUID(), newPromotionConcept, "en", name + " - promotion refset", termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), config);
		termFactory.newRelationship(UUID.randomUUID(), newPromotionConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), workListsRoot, defining, refinability, current, 0, config);
		termFactory.newRelationship(UUID.randomUUID(), newConcept, promotionRelConcept, newPromotionConcept, defining, refinability, current, 0, config);
		newPromotionConcept.setAnnotationStyleRefex(true);

		workList = new WorkList(workListWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(), workListWithMetadata.getPartitionUUID());
		workList.setWorkflowDefinition(workListWithMetadata.getWorkflowDefinition());

		workList.setWorkflowUserRoles(workListWithMetadata.getWorkflowUserRoles());
		// String metadata = serialize(workList);
		WorklistMetadata worklistMetadata = new WorklistMetadata(workList.getName(), workList.getUids(), workList.getPartitionUUID(), workList.getWorkflowDefinitionFileName(), convertToStringListOfMembers(workList.getWorkflowUserRoles()));
		String metadata = serialize(worklistMetadata);

		termFactory.addUncommittedNoChecks(newConcept);
		termFactory.addUncommittedNoChecks(workListRefset);
		termFactory.addUncommittedNoChecks(newCommentsConcept);
		termFactory.addUncommittedNoChecks(newPromotionConcept);
		// promote(newConcept, config);
		// promote(newCommentsConcept, config);
		// promote(newPromotionConcept, config);
		// termFactory.addUncommittedNoChecks(newConcept);
		// termFactory.addUncommittedNoChecks(newCommentsConcept);
		// termFactory.addUncommittedNoChecks(newPromotionConcept);
		// termFactory.commit();

		termFactory.getRefsetHelper(config).newRefsetExtension(workListRefset.getConceptNid(), newConcept.getConceptNid(), EConcept.REFSET_TYPES.STR, new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, metadata), config);

		for (I_ExtendByRef extension : termFactory.getRefsetExtensionMembers(workListRefset.getConceptNid())) {
			if (extension.getComponentNid() == newConcept.getConceptNid() && extension.getMutableParts().iterator().next().getTime() == Long.MAX_VALUE) {
				termFactory.addUncommittedNoChecks(workListRefset);
				termFactory.addUncommittedNoChecks(extension);
				// promote(extension, config);
				// termFactory.addUncommittedNoChecks(workListRefset);
				// termFactory.addUncommittedNoChecks(extension);
				// termFactory.commit();
			}
		}

		workListRefset.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		newPromotionConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		newCommentsConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		TerminologyProjectDAO.workListCache.put(workList.getUids().iterator().next(), workList);
		return workList;
	}

	/**
	 * Creates the new partition.
	 * 
	 * @param name
	 *            the name
	 * @param partitionSchemeUUID
	 *            the partition scheme uuid
	 * @param config
	 *            the config
	 * @return the partition
	 */
	public static Partition createNewPartition(String name, UUID partitionSchemeUUID, I_ConfigAceFrame config) {
		Partition newPartition = new Partition(name, 0, null, partitionSchemeUUID);
		return createNewPartition(newPartition, config);
	}

	/**
	 * Gets the j son form.
	 * 
	 * @param obj
	 *            the obj
	 * @return the j son form
	 */
	public static String getJSonForm(Object obj) {

		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		String str = xstream.toXML(obj);
		return str;
		// //xstream.setMode(XStream.NO_REFERENCES);
		// //xstream.alias("action", WfAction.class);
		// System.out.println("JSON Len: " + xstream.toXML(role2).length());
		// System.out.println(xstream.toXML(role2));
	}

	/**
	 * Gets the object from j son.
	 * 
	 * @param jSon
	 *            the j son
	 * @return the object from j son
	 */
	public static Object getObjectFromJSon(String jSon) {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		Object obj = xstream.fromXML(jSon);
		return obj;
	}

	/**
	 * Combine partitions.
	 * 
	 * @param partitions
	 *            the partitions
	 * @param name
	 *            the name
	 * @param config
	 *            the config
	 * @return the partition
	 * @throws Exception
	 *             the exception
	 */
	public static Partition combinePartitions(List<Partition> partitions, String name, I_ConfigAceFrame config) throws Exception {

		PartitionScheme partitionScheme = partitions.get(0).getPartitionScheme(config);

		if (partitions.size() < 2) {
			throw new Exception("At least two partitions are required to combine.");
		}

		for (Partition loopPartition : partitions) {
			if (loopPartition.getPartitionScheme(config).getId() != partitionScheme.getId()) {
				throw new Exception("All partitions must belong to the same scheme.");
			}

			if (loopPartition.getWorkLists().size() > 0) {
				throw new Exception("Partitions to be combined should have no associated worklists, delete worklists first");
			}

			if (loopPartition.getSubPartitionSchemes(config).size() > 0) {
				throw new Exception("Partitions to be combined should have no sub-partition schemes.");
			}

		}

		Partition newPartition = TerminologyProjectDAO.createNewPartition(name, partitionScheme.getUids().iterator().next(), config);

		for (Partition loopPartition : partitions) {
			for (PartitionMember loopMember : loopPartition.getPartitionMembers()) {
				addConceptAsPartitionMember(loopMember.getConcept(), newPartition, config);
			}

		}
		Terms.get().addUncommittedNoChecks(newPartition.getConcept());
		newPartition.getConcept().commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(), ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

		for (Partition loopPartition : partitions) {
			retirePartition(loopPartition, config);
		}

		List<PartitionMember> members = newPartition.getPartitionMembers();
		if (members.size() == 0) {
			// sas
		}
		return newPartition;
	}

	/**
	 * Checks if is concept duplicate.
	 * 
	 * @param description
	 *            the description
	 * @return true, if is concept duplicate
	 */
	private static boolean isConceptDuplicate(String description) {
		boolean result = false;
		I_TermFactory tf = Terms.get();
		try {
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			String escapedDescription = QueryParser.escape(description);
			SearchResult results = tf.doLuceneSearch(escapedDescription);
			for (int i = 0; i < results.topDocs.scoreDocs.length; i++) {
				try {
					Document doc = results.searcher.doc(results.topDocs.scoreDocs[i].doc);
					int cnid = Integer.parseInt(doc.get("cnid"));
					int dnid = Integer.parseInt(doc.get("dnid"));
					// System.out.println(doc);
					I_DescriptionVersioned<?> foundDescription = tf.getDescription(dnid);
					I_GetConceptData foundConcept = tf.getConcept(foundDescription.getConceptNid());
					if (foundDescription.getTuples(config.getConflictResolutionStrategy()).iterator().next().getText().equalsIgnoreCase(description)
							&& foundConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next().getStatusNid() == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {
						result = true;
					}
				} catch (Exception e) {
					// Do Nothing
				}
			}
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ParseException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return result;
	}

	/**
	 * Creates the new partition and members from work set.
	 * 
	 * @param name
	 *            the name
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @param activity
	 *            the activity
	 * @return the partition
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static Partition createNewPartitionAndMembersFromWorkSet(String name, WorkSet workSet, I_ConfigAceFrame config, I_ShowActivity activity) throws TerminologyException, IOException {

		Partition newPartition = null;
		ActivityUpdater updater = new ActivityUpdater(activity, "Creating partition");
		refsetHelper = Terms.get().getRefsetHelper(config);
		updater.startActivity();
		try {
			if (isConceptDuplicate(name + " (partition)")) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}
			PartitionScheme newPartitionScheme = createNewPartitionScheme(name, workSet.getUids().iterator().next(), config);
			newPartition = TerminologyProjectDAO.createNewPartition(name, newPartitionScheme.getUids().iterator().next(), config);
			List<WorkSetMember> members = workSet.getWorkSetMembers();
			updater.startCount(members.size());
			for (WorkSetMember loopMember : members) {
				TerminologyProjectDAO.addConceptAsPartitionMember(loopMember.getConcept(), newPartition, config);
				updater.incrementCount();
			}
			Terms.get().addUncommittedNoChecks(newPartition.getConcept());
			newPartition.getConcept().commit(config.getDbConfig().getUserChangesChangeSetPolicy().convert(), ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		updater.finish();
		refsetHelper = null;
		return newPartition;

	}

	/**
	 * Creates the new partition.
	 * 
	 * @param partitionWithMetadata
	 *            the partition with metadata
	 * @param config
	 *            the config
	 * @return the partition
	 */
	private static Partition createNewPartition(Partition partitionWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		Partition partition = null;
		I_GetConceptData newConcept = null;

		String partitionName = partitionWithMetadata.getName() + " (partition)";

		try {
			if (isConceptDuplicate(partitionName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Warning", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}
			I_GetConceptData parentConcept = termFactory.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.getUids());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData partitionScheme = termFactory.getConcept(partitionWithMetadata.getPartitionSchemeUUID());

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), parentConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, includesFromAttribute, partitionScheme, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			partition = new Partition(partitionWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(), partitionWithMetadata.getPartitionSchemeUUID());
			partition.setConcept(newConcept);

			termFactory.addUncommittedNoChecks(newConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		if (partition != null) {
			partitionCache.put(newConcept.getPrimUuid(), partition);
		}

		return partition;
	}

	/**
	 * Creates the new partition scheme.
	 * 
	 * @param name
	 *            the name
	 * @param sourceRefsetUUID
	 *            the source refset uuid
	 * @param config
	 *            the config
	 * @return the partition scheme
	 */
	public static PartitionScheme createNewPartitionScheme(String name, UUID sourceRefsetUUID, I_ConfigAceFrame config) {
		PartitionScheme newPartitionScheme = new PartitionScheme(name, 0, null, sourceRefsetUUID);
		return createNewPartitionScheme(newPartitionScheme, config);
	}

	/**
	 * Creates the new partition scheme.
	 * 
	 * @param partitionSchemeWithMetadata
	 *            the partition scheme with metadata
	 * @param config
	 *            the config
	 * @return the partition scheme
	 */
	private static PartitionScheme createNewPartitionScheme(PartitionScheme partitionSchemeWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		PartitionScheme partitionScheme = null;
		I_GetConceptData newConcept = null;
		String partitionSchemeName = partitionSchemeWithMetadata.getName() + " (partition scheme)";
		try {
			if (isConceptDuplicate(partitionSchemeName)) {
				JOptionPane.showMessageDialog(new JDialog(), "This name is already in use", "Error", JOptionPane.WARNING_MESSAGE);
				throw new Exception("This name is already in use");
			}

			I_GetConceptData worksetConcept = termFactory.getConcept(partitionSchemeWithMetadata.getSourceRefsetUUID());

			I_GetConceptData includesFromAttribute = termFactory.getConcept(ArchitectonicAuxiliary.Concept.INCLUDES_FROM_ATTRIBUTE.getUids());

			I_GetConceptData workSet = termFactory.getConcept(partitionSchemeWithMetadata.getSourceRefsetUUID());

			WorkSet parentWorkSet = getWorkSet(workSet, config);
			if (!parentWorkSet.getName().startsWith("Maintenance") && getAllWorkSetMembers(parentWorkSet, config).isEmpty()) {
				JOptionPane.showMessageDialog(new JDialog(), "WorkSet empty, please specify source refset and Sync.", "Error", JOptionPane.WARNING_MESSAGE);
				throw new Exception("WorkSet empty.");
			}

			newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionSchemeName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newDescription(UUID.randomUUID(), newConcept, "en", partitionSchemeName, termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid()), config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), worksetConcept, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			termFactory.newRelationship(UUID.randomUUID(), newConcept, includesFromAttribute, workSet, termFactory.getConcept(ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()), termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
					termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 0, config);

			partitionScheme = new PartitionScheme(partitionSchemeWithMetadata.getName(), newConcept.getConceptNid(), newConcept.getUids(), partitionSchemeWithMetadata.getSourceRefsetUUID());

			termFactory.addUncommittedNoChecks(newConcept);
			newConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			// promote(newConcept, config);
			// termFactory.addUncommittedNoChecks(newConcept);
			// termFactory.commit();

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return partitionScheme;
	}

	/**
	 * Adds the concept as work list member.
	 * 
	 * @param member
	 *            the member
	 * @param assignedUserId
	 *            the assigned user id
	 * @param config
	 *            the config
	 */
	public static void addConceptAsWorkListMember(WorkListMember member, int assignedUserId, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = termFactory.getConcept(member.getUids());
			I_GetConceptData workListConcept = termFactory.getConcept(member.getWorkListUUID());
			// String metadata = serialize(member);

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
							part.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							extension.addVersion(part);
						}
						termFactory.addUncommittedNoChecks(workListConcept);
					}
				}
			}

			if (!alreadyMember) {
				WorkList workList = getWorkList(workListConcept, config);
				PromotionAndAssignmentRefset promotionRefset = workList.getPromotionRefset(config);
				if (refsetHelper == null) {
					refsetHelper = termFactory.getRefsetHelper(config);
				}
				refsetHelper.newRefsetExtension(workListConcept.getConceptNid(), newMemberConcept.getConceptNid(), EConcept.REFSET_TYPES.STR, new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""), // metadata
						config);
				termFactory.addUncommittedNoChecks(workListConcept);
				I_GetConceptData activityStatusConcept = member.getActivityStatus();
				promotionRefset.setDestinationAndPromotionStatus(member.getId(), assignedUserId, activityStatusConcept.getConceptNid());
				I_TerminologyProject project = getProjectForWorklist(workList, config);
				if (project.getProjectType().equals(I_TerminologyProject.Type.TRANSLATION)) {
					TranslationProject transProject = (TranslationProject) project;
					if (transProject.getTargetLanguageRefset() != null) {
						LanguageMembershipRefset targetLanguage = new LanguageMembershipRefset(transProject.getTargetLanguageRefset(), config);
						PromotionRefset languagePromotionRefset = targetLanguage.getPromotionRefset(config);
						languagePromotionRefset.setPromotionStatus(member.getId(), activityStatusConcept.getConceptNid());
					}
				}
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Adds the concept as partition member.
	 * 
	 * @param partitionMemberConcept
	 *            the partition member concept
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 */
	public static void addConceptAsPartitionMember(I_GetConceptData partitionMemberConcept, Partition partition, I_ConfigAceFrame config) {
		try {
			PartitionMember partitionMember = new PartitionMember(getConceptString(partitionMemberConcept), partitionMemberConcept.getConceptNid(), partitionMemberConcept.getUids(), partition.getConcept().getPrimUuid());
			partitionMember.setMemberConcept(partitionMemberConcept);
			addConceptAsPartitionMember(partitionMember, partition, config);

		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

	/**
	 * The refset helper.
	 */
	private static I_HelpRefsets refsetHelper = null;

	/**
	 * Adds the concept as partition member.
	 * 
	 * @param member
	 *            the member
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 */
	public static void addConceptAsPartitionMember(PartitionMember member, Partition partition, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			termFactory.setActiveAceFrameConfig(config);
			I_GetConceptData newMemberConcept = member.getMemberConcept();
			I_GetConceptData partitionConcept = partition.getConcept();

			boolean alreadyMember = false;

			Collection<? extends RefexChronicleBI<?>> extensions = newMemberConcept.getRefexes();
			for (RefexChronicleBI<?> extensionBI : extensions) {
				if (extensionBI.getRefexNid() == partitionConcept.getNid()) {
					I_ExtendByRef extension = termFactory.getExtension(extensionBI.getNid());
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					if (isActive(part.getStatusNid())) {
						alreadyMember = true;
					} else if (isInactive(part.getStatusNid())) {
						for (PathBI editPath : config.getEditingPathSet()) {
							part.makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
							extension.addVersion(part);
						}
					}
				}
			}

			if (!alreadyMember) {
				if (refsetHelper == null) {
					refsetHelper = termFactory.getRefsetHelper(config);
				}
				refsetHelper.newRefsetExtension(partitionConcept.getConceptNid(), newMemberConcept.getConceptNid(), EConcept.REFSET_TYPES.STR, new RefsetPropertyMap().with(REFSET_PROPERTY.STRING_VALUE, ""), config);
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Execute worklist business process.
	 * 
	 * @param worklist
	 *            the worklist
	 * @param worker
	 *            the worker
	 * @param config
	 *            the config
	 * @param processToLunch
	 *            the process to lunch
	 * @throws TaskFailedException
	 *             the task failed exception
	 * @deprecated use deliverWorklistBusinessProcessToOutbox instead
	 */
	@SuppressWarnings("unchecked")
	public static void executeWorklistBusinessProcess(WorkList worklist, I_Work worker, I_ConfigAceFrame config, I_EncodeBusinessProcess processToLunch) throws TaskFailedException {

		List<WorkListMember> workListMembers = getAllWorkListMembers(worklist, config);

		Stack ps = worker.getProcessStack();
		for (WorkListMember workListMember : workListMembers) {

			worker.setProcessStack(new Stack());
			// BusinessProcess processToLunch=
			// workListMember.getBusinessProcessWithAttachments();
			processToLunch.execute(worker);
		}
		worker.setProcessStack(ps);
	}

	/**
	 * Deliver worklist business process to outbox.
	 * 
	 * @param worklist
	 *            the worklist
	 * @param worker
	 *            the worker
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TaskFailedException
	 *             the task failed exception
	 * @throws UnknownTransactionException
	 *             the unknown transaction exception
	 * @throws CannotCommitException
	 *             the cannot commit exception
	 * @throws LeaseDeniedException
	 *             the lease denied exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws PrivilegedActionException
	 *             the privileged action exception
	 */
	public static void deliverWorklistBusinessProcessToOutbox(WorkList worklist, I_Work worker) throws TerminologyException, IOException, TaskFailedException, InterruptedException, PrivilegedActionException {
		throw new UnsupportedOperationException("TODO: JINI REMOVAL");
	}

	/**
	 * The subject separator.
	 */
	private static String subjectSeparator = "-@-";

	/**
	 * The Enum subjectIndexes.
	 */
	public enum subjectIndexes {

		/**
		 * The WORKLIS t_ membe r_ id.
		 */
		WORKLIST_MEMBER_ID,
		/**
		 * The WORKLIS t_ membe r_ sourc e_ name.
		 */
		WORKLIST_MEMBER_SOURCE_NAME,
		/**
		 * The WORKLIS t_ id.
		 */
		WORKLIST_ID,
		/**
		 * The WORKLIS t_ name.
		 */
		WORKLIST_NAME,
		/**
		 * The PROJEC t_ id.
		 */
		PROJECT_ID,
		/**
		 * The WORKLIS t_ membe r_ sourc e_ pref.
		 */
		WORKLIST_MEMBER_SOURCE_PREF,
		/**
		 * The PROM o_ refse t_ id.
		 */
		PROMO_REFSET_ID,
		/**
		 * The TAG s_ array.
		 */
		TAGS_ARRAY,
		/**
		 * The STATU s_ id.
		 */
		STATUS_ID,
		/**
		 * The STATU s_ time.
		 */
		STATUS_TIME
	};

	/**
	 * Gets the item subject.
	 * 
	 * @param workListMember
	 *            the work list member
	 * @param worklist
	 *            the worklist
	 * @param project
	 *            the project
	 * @param promotionRefset
	 *            the promotion refset
	 * @param langRefset
	 *            the lang refset
	 * @param statusId
	 *            the status id
	 * @param statusTime
	 *            the status time
	 * @return the item subject
	 */
	public static String getItemSubject(WorkListMember workListMember, WorkList worklist, I_TerminologyProject project, PromotionRefset promotionRefset, Integer langRefset, int statusId, Long statusTime) {
		String[] terms = getSourceTerms(workListMember.getId(), langRefset);
		String subject = "";
		try {
			subject = workListMember.getUids().iterator().next() + subjectSeparator + terms[0] + subjectSeparator + worklist.getUids().iterator().next() + subjectSeparator + worklist.getName() + subjectSeparator + project.getUids().iterator().next() + subjectSeparator + terms[1] + subjectSeparator
					+ promotionRefset.getRefsetConcept().getUids().iterator().next() + subjectSeparator + subjectSeparator + Terms.get().nidToUuid(statusId) + subjectSeparator + statusTime;
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return subject;
	}

	/**
	 * The fsn.
	 */
	private static I_GetConceptData fsn;
	/**
	 * The preferred.
	 */
	private static I_GetConceptData preferred;
	/**
	 * The not acceptable.
	 */
	private static I_GetConceptData notAcceptable;
	/**
	 * The inactive.
	 */
	private static I_GetConceptData inactive;
	/**
	 * The retired.
	 */
	private static I_GetConceptData retired;
	/**
	 * The en refset.
	 */
	private static I_GetConceptData enRefset;

	static {

		try {
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());

			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());

			inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			enRefset = Terms.get().getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Gets the source terms.
	 * 
	 * @param worklistmemberId
	 *            the worklistmember id
	 * @param sourceLang
	 *            the source lang
	 * @return the source terms
	 */
	private static String[] getSourceTerms(Integer worklistmemberId, Integer sourceLang) {
		String[] retString = { "", "" };
		String sFsn = "";
		String sRetFsn = "";
		String sPref = "";
		String sRetPref = "";
		if (sourceLang != null) {

			List<ContextualizedDescription> descriptions;
			try {
				descriptions = ContextualizedDescription.getContextualizedDescriptions(worklistmemberId, sourceLang, true);

				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == sourceLang) {

						if (description.getExtensionStatusId() != inactive.getConceptNid() && description.getDescriptionStatusId() != inactive.getConceptNid()) {

							if (description.getTypeId() == fsn.getConceptNid()) {
								sFsn = description.getText();
								if (!sPref.equals("")) {
									break;
								}
							} else {
								if (description.getAcceptabilityId() == preferred.getConceptNid()) {
									sPref = description.getText();
									if (!sFsn.equals("")) {
										break;
									}
								}
							}
						} else {
							if (description.getTypeId() == fsn.getConceptNid()) {
								sRetFsn = description.getText();
							} else {
								sRetPref = description.getText();
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
			if (sFsn.equals("")) {
				retString[0] = sRetFsn;
			} else {
				retString[0] = sFsn;
			}

			if (sPref.equals("")) {
				retString[1] = sRetPref;
			} else {
				retString[1] = sPref;
			}
		}

		return retString;

	}

	/**
	 * Gets the parsed item subject.
	 * 
	 * @param subject
	 *            the subject
	 * @return the parsed item subject
	 */
	public static String[] getParsedItemSubject(String subject) {
		if (subject == null) {
			return new String[] { "" };
		}
		return subject.split(subjectSeparator, -1);
	}

	/**
	 * Gets the source language refset ids for project id.
	 * 
	 * @param projectId
	 *            the project id
	 * @param config
	 *            the config
	 * @return the source language refset ids for project id
	 */
	public static List<Integer> getSourceLanguageRefsetIdsForProjectId(int projectId, I_ConfigAceFrame config) {
		ArrayList<Integer> returnData = new ArrayList<Integer>();
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE.localize().getNid());

			List<? extends I_RelTuple> sourceLanguageRefsetsTuples = termFactory.getConcept(projectId).getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			sourceLanguageRefsetsTuples = cleanRelTuplesList(sourceLanguageRefsetsTuples);

			if (sourceLanguageRefsetsTuples != null) {
				for (I_RelTuple loopTuple : sourceLanguageRefsetsTuples) {
					if (isActive(loopTuple.getStatusNid())) {
						returnData.add(loopTuple.getC2Id());
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
	 * Gets the subject from array.
	 * 
	 * @param parsedSubj
	 *            the parsed subj
	 * @return the subject from array
	 */
	public static String getSubjectFromArray(String[] parsedSubj) {
		StringBuffer ret = new StringBuffer("");
		if (parsedSubj.length == TerminologyProjectDAO.subjectIndexes.values().length) {
			for (int i = 0; i < parsedSubj.length; i++) {
				ret.append(parsedSubj[i]);
				if (i < parsedSubj.length - 1) {
					ret.append(subjectSeparator);
				}
			}
		}
		return ret.toString();
	}

	/**
	 * Gets the promotion status id for refset id.
	 * 
	 * @param refsetId
	 *            the refset id
	 * @param componentId
	 *            the component id
	 * @param config
	 *            the config
	 * @return the promotion status id for refset id
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	public static Integer getPromotionStatusIdForRefsetId(int refsetId, int componentId, I_ConfigAceFrame config) throws IOException, TerminologyException {

		I_TermFactory termFactory = Terms.get();
		List<? extends I_ExtendByRef> members = termFactory.getAllExtensionsForComponent(componentId);
		for (I_ExtendByRef promotionMember : members) {
			if (promotionMember.getRefsetId() == refsetId) {
				List<? extends I_ExtendByRefVersion> tuples = promotionMember.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
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

	/**
	 * Gets the target language refset id for project id.
	 * 
	 * @param projectId
	 *            the project id
	 * @param config
	 *            the config
	 * @return the target language refset id for project id
	 * @throws Exception
	 *             the exception
	 */
	public static Integer getTargetLanguageRefsetIdForProjectId(int projectId, I_ConfigAceFrame config) throws Exception {
		List<I_RelTuple> targetRefsets = new ArrayList<I_RelTuple>();
		List<? extends I_RelTuple> targetRefsetsTuples = null;
		I_TermFactory termFactory = Terms.get();

		try {
			I_IntSet allowedDestRelTypes = termFactory.newIntSet();
			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE.localize().getNid());
			targetRefsetsTuples = termFactory.getConcept(projectId).getSourceRelTuples(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());

			targetRefsetsTuples = cleanRelTuplesList(targetRefsetsTuples);

		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
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
		} else {
			throw new Exception("Wrong number of target refsets.");
		}
	}

	/**
	 * Gets the work list member.
	 * 
	 * @param workListMemberConcept
	 *            the work list member concept
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @return the work list member
	 */
	public static WorkListMember getWorkListMember(I_GetConceptData workListMemberConcept, WorkList workList, I_ConfigAceFrame config) {
		WorkListMember workListMember = null;
		I_TermFactory termFactory = Terms.get();

		PromotionAndAssignmentRefset promotionRefset = workList.getPromotionRefset(config);

		// I_IntSet descriptionTypes = termFactory.newIntSet();
		// descriptionTypes.add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
		//
		// List<? extends I_DescriptionTuple> descTuples =
		// workListMemberConcept.getDescriptionTuples(
		// config.getAllowedStatus(),
		// descriptionTypes, config.getViewPositionSetReadOnly(),
		// Precedence.TIME, config.getConflictResolutionStrategy());
		// String name;
		// if (!descTuples.isEmpty()) {
		// name = descTuples.iterator().next().getText();
		// } else {
		// name = "No FSN!";
		// }
		String name = "";
		try {
			ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(config.getViewCoordinate(), workListMemberConcept.getNid());
			name = conceptVersion.getDescriptionPreferred().getText();

			Collection<? extends RefexChronicleBI<?>> members = workListMemberConcept.getAnnotations();
			for (RefexChronicleBI<?> promotionMember : members) {
				if (promotionMember.getRefexNid() == promotionRefset.getRefsetId()) {
					I_GetConceptData status = promotionRefset.getPromotionStatus(workListMemberConcept.getConceptNid(), config);
					I_GetConceptData destination = promotionRefset.getDestination(workListMemberConcept.getConceptNid(), config);
					Long statusDate = promotionRefset.getLastStatusTime(workListMemberConcept.getConceptNid(), config);
					workListMember = new WorkListMember(name, workListMemberConcept.getConceptNid(), workListMemberConcept.getUids(), workList.getUids().iterator().next(), status, statusDate);
				}
			}
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Worlist member corrupted");
			return null;
		}
		return workListMember;
	}

	/**
	 * Gets the all work list members.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * 
	 * @return the all work list members
	 */
	public static List<WorkListMember> getAllWorkListMembers(WorkList workList, I_ConfigAceFrame config) {

		I_TermFactory termFactory = Terms.get();
		List<WorkListMember> workListMembers = new ArrayList<WorkListMember>();
		try {
			Collection<? extends I_ExtendByRef> membersExtensions = termFactory.getRefsetExtensionMembers(workList.getId());
			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();
			for (I_ExtendByRef extension : membersExtensions) {
				members.add(termFactory.getConcept(extension.getComponentNid()));
			}
			for (I_GetConceptData member : members) {
				WorkListMember workListMember = getWorkListMember(member, workList, config);
				if (workListMember != null) {
					workListMembers.add(workListMember);
				}
			}

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return workListMembers;
	}

	/**
	 * Gets the work list members with status.
	 * 
	 * @param workList
	 *            the work list
	 * @param activityStatus
	 *            the activity status
	 * @param config
	 *            the config
	 * @return the work list members with status
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static List<WorkListMember> getWorkListMembersWithStatus(WorkList workList, I_GetConceptData activityStatus, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_TermFactory termFactory = Terms.get();
		List<WorkListMember> workListMembers = new ArrayList<WorkListMember>();

		List<WorkListMember> members = getAllWorkListMembers(workList, config);

		for (WorkListMember loopMember : members) {
			I_GetConceptData loopActivityStatus = loopMember.getActivityStatus();
			if (activityStatus.getConceptNid() == loopActivityStatus.getConceptNid()) {
				workListMembers.add(loopMember);
			}
		}

		return workListMembers;
	}

	/**
	 * Gets the work list member statuses.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @return the work list member statuses
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static HashMap<I_GetConceptData, Integer> getWorkListMemberStatuses(WorkList workList, I_ConfigAceFrame config, List<WfFilterBI> filters) throws TerminologyException, IOException {
		HashMap<I_GetConceptData, Integer> workListMembersStatuses = new HashMap<I_GetConceptData, Integer>();
		I_TermFactory tf = Terms.get();

		List<WorkListMember> members = getAllWorkListMembers(workList, config);

		for (WorkListMember loopMember : members) {
			if (filters != null) {
				for (WfFilterBI filter : filters) {
					if (!filter.evaluateInstance(loopMember.getWfInstance())) {
						continue;
					}
				}
			}
			I_GetConceptData activityStatus = loopMember.getActivityStatus();
			Integer currentCount = workListMembersStatuses.get(activityStatus);
			if (currentCount == null) {
				currentCount = 0;
			}
			workListMembersStatuses.put(activityStatus, currentCount + 1);
		}

		return workListMembersStatuses;
	}

	/**
	 * Update component metadata.
	 * 
	 * @param component
	 *            the component
	 * @param objectWithMetadata
	 *            the object with metadata
	 * @param refsetUUIDs
	 *            the refset uui ds
	 * @param config
	 *            the config
	 */
	public static void updateComponentMetadata(I_GetConceptData component, Object objectWithMetadata, UUID[] refsetUUIDs, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		try {
			I_GetConceptData componentsRefset = termFactory.getConcept(refsetUUIDs);

			I_GetConceptData componentConcept = termFactory.getConcept(component.getUids());

			String metadata;
			// String metadata = serialize(objectWithMetadata);
			if (objectWithMetadata instanceof WorkList) {
				WorkList workList = (WorkList) objectWithMetadata;
				WorklistMetadata worklistMetadata = new WorklistMetadata(workList.getName(), workList.getUids(), workList.getPartitionUUID(), workList.getWorkflowDefinitionFileName(), convertToStringListOfMembers(workList.getWorkflowUserRoles()));
				metadata = serialize(worklistMetadata);
			} else {
				metadata = serialize(objectWithMetadata);
			}
			Collection<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(componentConcept.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == componentsRefset.getConceptNid()) {
					Collection<? extends I_ExtendByRefVersion> extTuples = extension.getTuples(config.getConflictResolutionStrategy());
					for (PathBI editPath : config.getEditingPathSet()) {
						Object makeAnalog = extTuples.iterator().next().makeAnalog(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) makeAnalog;
						part.setStringValue(metadata);
						extension.addVersion(part);
					}
					termFactory.addUncommittedNoChecks(componentsRefset);
					termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
					// promote(extension, config);
					// termFactory.addUncommittedNoChecks(componentsRefset);
					// termFactory.addUncommittedNoChecks(extension);
					// termFactory.commit();
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
	 * Update worklist and members.
	 * 
	 * @param worklist
	 *            the worklist
	 * @param config
	 *            the config
	 */
	public static void updateWorklistAndMembers(WorkList worklist, I_ConfigAceFrame config) {
		try {
			I_GetConceptData worklistConcept = worklist.getConcept();
			updateComponentMetadata(worklistConcept, worklist, new UUID[] { ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.getUids().iterator().next() }, config);
			List<WorkListMember> workListMembers = getAllWorkListMembers(worklist, config);
			// for (WorkListMember workListMember : workListMembers) {
			// workListMember.setDestination(worklist.getDestination());
			// UUID[] uuidArray = new UUID[worklist.getUids().size()];
			// updateComponentMetadata(workListMember.getConcept(),
			// workListMember,worklist.getUids().toArray(uuidArray), config);
			// }
			TerminologyProjectDAO.workListCache.remove(worklist.getUids().iterator().next());
			ConceptChronicleBI extRefsetConcept = Ts.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.localize().getNid());
			extRefsetConcept.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Update worklist member metadata.
	 * 
	 * @param workListMemberWithMetadata
	 *            the work list member with metadata
	 * @param config
	 *            the config
	 * 
	 * @return the work list member
	 */
	public static WorkListMember updateWorkListMemberMetadata(WorkListMember workListMemberWithMetadata, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		WorkListMember workListMember = null;

		try {
			I_GetConceptData workListConcept = termFactory.getConcept(workListMemberWithMetadata.getWorkListUUID());
			// String metadata = serialize(workListMemberWithMetadata);
			//
			// Collection<? extends I_ExtendByRef> extensions =
			// termFactory.getAllExtensionsForComponent(workListMemberWithMetadata.getId());
			// for (I_ExtendByRef extension : extensions) {
			// if (workListConcept.getConceptId() == extension.getRefsetId()) {
			// I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
			// for (PathBI editPath : config.getEditingPathSet()) {
			// I_ExtendByRefPartStr part = (I_ExtendByRefPartStr)
			// lastPart.makeAnalog(
			// ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
			// editPath.getConceptId(),
			// Long.MAX_VALUE);
			// part.setStringValue(metadata);
			// extension.addVersion(part);
			// }
			// termFactory.addUncommittedNoChecks(extension);
			// promote(extension, config);
			// // termFactory.addUncommittedNoChecks(extension);
			// // termFactory.commit();
			// }
			// }
			// waiting(1);
			WorkList workList = getWorkList(workListConcept, config);
			PromotionRefset promotionRefset = workList.getPromotionRefset(config);
			I_GetConceptData activityStatusConcept = workListMemberWithMetadata.getActivityStatus();
			promotionRefset.setPromotionStatus(workListMemberWithMetadata.getId(), activityStatusConcept.getConceptNid());

			// Translation specific concept level promotion refset
			I_TerminologyProject project = getProjectForWorklist(workList, config);
			if (project.getProjectType().equals(I_TerminologyProject.Type.TRANSLATION)) {
				TranslationProject transProject = (TranslationProject) project;
				LanguageMembershipRefset targetLanguage = new LanguageMembershipRefset(transProject.getTargetLanguageRefset(), config);
				PromotionRefset languagePromotionRefset = targetLanguage.getPromotionRefset(config);
				languagePromotionRefset.setPromotionStatus(workListMemberWithMetadata.getId(), activityStatusConcept.getConceptNid());
				// end
			}

			workListMember = getWorkListMember(workListMemberWithMetadata.getConcept(), workList, config);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return workListMember;
	}

	/**
	 * Retire concept.
	 * 
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * @return the i_ get concept data
	 * @throws Exception
	 *             the exception
	 */
	public static void retireProject(I_TerminologyProject project, I_ConfigAceFrame config) throws Exception {
		boolean retire = true;

		for (WorkSet loopWorkSet : project.getWorkSets(config)) {
			if (!loopWorkSet.getName().startsWith("Maintenance - ")) {
				retire = false;
			}
		}
		if (!retire) {
			throw new Exception("Not empty!, delete worksets first...");
		} else {
			retireConcept(project.getConcept(), config);
		}
	}

	/**
	 * Retire work set.
	 * 
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @throws Exception
	 *             the exception
	 */
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

	/**
	 * Retire work list.
	 * 
	 * @param workList
	 *            the work list
	 * @param config
	 *            the config
	 * @throws Exception
	 *             the exception
	 */
	public static void retireWorkList(WorkList workList, I_ConfigAceFrame config) throws Exception {
		for (WorkListMember member : workList.getWorkListMembers()) {
//			if (!ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids().contains(member.getActivityStatus()) && !ArchitectonicAuxiliary.Concept.RETIRED.getUids().contains(member.getActivityStatus())
//					&& !SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getUUIDs().contains(member.getActivityStatus())) {
			if (!UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b").equals(member.getActivityStatus()) && !UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42").equals(member.getActivityStatus()) 
					&& !ArchitectonicAuxiliary.Concept.RETIRED.getUids().contains(member.getActivityStatus())
					&& !SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getUUIDs().contains(member.getActivityStatus())) {
				throw new Exception("WorkList cannot be retired, some members have been delivered and are still active.");
			}
		}

		for (WorkListMember member : workList.getWorkListMembers()) {
			retireWorkListMember(member);
		}
		retireConcept(workList.getConcept(), config);
	}

	/**
	 * Retire partition.
	 * 
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 * @throws Exception
	 *             the exception
	 */
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

	/**
	 * Retire partition scheme.
	 * 
	 * @param partitionScheme
	 *            the partition scheme
	 * @param config
	 *            the config
	 * @throws Exception
	 *             the exception
	 */
	public static void retirePartitionScheme(PartitionScheme partitionScheme, I_ConfigAceFrame config) throws Exception {
		if (!partitionScheme.getPartitions().isEmpty()) {
			throw new Exception("Not empty!, delete partitions first...");
		} else {
			retireConcept(partitionScheme.getConcept(), config);
		}
	}

	/**
	 * Retire partition member.
	 * 
	 * @param partitionMember
	 *            the partition member
	 * @param config
	 *            the config
	 * @throws TerminologyException
	 *             the terminology exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void retirePartitionMember(PartitionMember partitionMember, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_TermFactory termFactory = Terms.get();
		try {
			I_GetConceptData partitionConcept = termFactory.getConcept(partitionMember.getPartitionUUID());
			I_GetConceptData partitionMemberConcept = partitionMember.getConcept();
			ViewCoordinate vc = config.getViewCoordinate();
			TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());

			Collection<? extends RefexChronicleBI<?>> extensions = partitionMemberConcept.getRefexes();
			for (RefexChronicleBI<?> extensionBI : extensions) {
				if (extensionBI.getRefexNid() == partitionConcept.getNid()) {
					I_ExtendByRef extension = termFactory.getExtension(extensionBI.getNid());
					I_ExtendByRefPart lastPart = getLastExtensionPart(extension);
					I_ExtendByRefPartStr part = (I_ExtendByRefPartStr) lastPart;
					for (PathBI editPath : config.getEditingPathSet()) {
						part.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(), editPath.getConceptNid());
						if (part instanceof RefexVersionBI && !part.getClass().toString().contains("StrMember")) {
							extension.addVersion(part);
						}
					}
				}
			}

			termFactory.addUncommittedNoChecks(partitionConcept);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return;
	}

	/**
	 * Retire concept.
	 * 
	 * @param conceptToRetire
	 *            the concept to retire
	 * @param config
	 *            the config
	 * @return the i_ get concept data
	 */
	public static I_GetConceptData retireConcept(I_GetConceptData conceptToRetire, I_ConfigAceFrame config) {
		I_TermFactory termFactory = Terms.get();
		I_GetConceptData conceptToRetireUpdatedFromDB = null;
		try {
			conceptToRetireUpdatedFromDB = termFactory.getConcept(conceptToRetire.getUids());
			I_ConceptAttributePart lastAttributePart = getLastestAttributePart(conceptToRetireUpdatedFromDB);
			for (PathBI editPath : config.getEditingPathSet()) {
				I_ConceptAttributePart newAttributeVersion = (I_ConceptAttributePart) lastAttributePart.makeAnalog(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(),
						editPath.getConceptNid());
				conceptToRetireUpdatedFromDB.getConAttrs().addVersion(newAttributeVersion);
			}
			termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			conceptToRetireUpdatedFromDB.commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			// promote(conceptToRetireUpdatedFromDB.getConceptAttributes(),
			// config);
			// promote(conceptToRetire, config);
			// termFactory.addUncommittedNoChecks(conceptToRetireUpdatedFromDB);
			// termFactory.commit();

		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return conceptToRetireUpdatedFromDB;
	}

	/**
	 * Serialize.
	 * 
	 * @param object
	 *            the object
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
			AceLog.getAppLog().alertAndLogException(e);
			return null;
		}
		return serializedForm;
	}

	/**
	 * Deserialize.
	 * 
	 * @param string
	 *            the string
	 * 
	 * @return the object
	 */
	public static Object deserialize(String string) {
		Object object = null;
		try {
			ByteArrayInputStream bios = new ByteArrayInputStream(Base64.decodeBase64(string));
			ObjectInputStream ois = new ObjectInputStream(bios);
			object = ois.readObject();
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
			return null;
		} catch (ClassNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
			return null;
		}
		return object;
	}

	/**
	 * Waiting.
	 * 
	 * @param n
	 *            the n
	 */
	public static void waiting(int n) {

		long t0, t1;
		t0 = System.currentTimeMillis();
		do {
			t1 = System.currentTimeMillis();
		} while ((t1 - t0) < (n * 1000));
	}

	/**
	 * Gets the latest source relationship target.
	 * 
	 * @param concept
	 *            the concept
	 * @param relationshipType
	 *            the relationship type
	 * @return the latest source relationship target
	 * @throws Exception
	 *             the exception
	 */
	public static I_GetConceptData getLatestSourceRelationshipTarget(I_GetConceptData concept, I_GetConceptData relationshipType) throws Exception {

		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

		I_GetConceptData latestTarget = null;
		long latestVersion = Integer.MIN_VALUE;

		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(relationshipType.getConceptNid());

		List<? extends I_RelTuple> relationships = concept.getSourceRelTuples(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
		for (I_RelTuple rel : relationships) {
			if (rel.getTime() > latestVersion) {
				latestVersion = rel.getTime();
				latestTarget = Terms.get().getConcept(rel.getC2Id());
			}
		}

		return latestTarget;
	}

	/**
	 * Sync workset with refset spec.
	 * 
	 * @param workSet
	 *            the work set
	 * @param config
	 *            the config
	 * @param activity
	 *            the activity
	 * @throws Exception
	 *             the exception
	 */
	public static void syncWorksetWithRefsetSpec(WorkSet workSet, I_ConfigAceFrame config, I_ShowActivity activity) throws Exception {

		ActivityUpdater updater = new ActivityUpdater(activity, "Synchronizing WorkSet");
		updater.startActivity();
		refsetHelper = Terms.get().getRefsetHelper(config);
		initializeWorkSet(workSet, config, updater);
		Terms.get().addUncommittedNoChecks(workSet.getConcept());
		workSet.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		refsetHelper = null;
		updater.finish();
		return;
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
	 *             Signals that an I/O exception has occurred.
	 */
	public static I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = Terms.get().newIntSet();
		allowedStatus.addAll(config.getAllowedStatus().getSetValues());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		allowedStatus.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(allowedStatus, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy())) {
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
	 * Gets the lastest attribute part.
	 * 
	 * @param concept
	 *            the concept
	 * @return the lastest attribute part
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static I_ConceptAttributePart getLastestAttributePart(final I_GetConceptData concept) throws IOException {
		Collection<? extends ConceptAttributeVersionBI> refsetAttibuteParts = concept.getConceptAttributes().getVersions();
		ConceptAttributeVersionBI latestAttributePart = null;
		for (ConceptAttributeVersionBI attributePart : refsetAttibuteParts) {
			if (latestAttributePart == null || attributePart.getTime() >= latestAttributePart.getTime()) {
				latestAttributePart = attributePart;
			}
		}

		if (latestAttributePart == null) {
			throw new IOException("No parts on this viewpositionset.");
		}

		return (I_ConceptAttributePart) latestAttributePart;
	}

	/**
	 * Checks if is active.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is active
	 */
	public static boolean isActive(int statusId) {
		List<Integer> activeStatuses = new ArrayList<Integer>();
		try {
			activeStatuses.add(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			activeStatuses.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
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
	 * Checks if is inactive.
	 * 
	 * @param statusId
	 *            the status id
	 * @return true, if is inactive
	 */
	private static boolean isInactive(int statusId) {
		List<Integer> inactiveStatuses = new ArrayList<Integer>();
		try {
			inactiveStatuses.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
			inactiveStatuses.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return (inactiveStatuses.contains(statusId));
	}

	/**
	 * Check permission for hierarchy.
	 * 
	 * @param user
	 *            the user
	 * @param target
	 *            the target
	 * @param permission
	 *            the permission
	 * @param config
	 *            the config
	 * 
	 * @return true, if successful
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 * 
	 * @deprecated moved to ProjectPermissionsAPI
	 */
	public static boolean checkPermissionForHierarchy(I_GetConceptData user, I_GetConceptData target, I_GetConceptData permission, I_ConfigAceFrame config) throws IOException, TerminologyException, ContradictionException {

		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.checkPermissionForHierarchy(user, target, permission);
	}

	/**
	 * Check permission for project.
	 * 
	 * @param user
	 *            the user
	 * @param target
	 *            the target
	 * @param permission
	 *            the permission
	 * @param config
	 *            the config
	 * 
	 * @return true, if successful
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 * 
	 * @deprecated moved to ProjectPermissionsAPI
	 */
	public static boolean checkPermissionForProject(I_GetConceptData user, I_GetConceptData target, I_GetConceptData permission, I_ConfigAceFrame config) throws IOException, TerminologyException {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.checkPermissionForProject(user, target, permission);
	}

	/**
	 * Gets the business process.
	 * 
	 * @param f
	 *            the f
	 * @return the business process
	 */
	public static BusinessProcess getBusinessProcess(File f) {
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
			BusinessProcess processToLunch = (BusinessProcess) in.readObject();
			in.close();
			return processToLunch;

		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ClassNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		return null;
	}

	/**
	 * Generate work list from partition.
	 * 
	 * @param partition
	 *            the partition
	 * @param workflowDefinition
	 *            the workflow definition
	 * @param workflowUserRoles
	 *            the workflow user roles
	 * @param name
	 *            the name
	 * @param config
	 *            the config
	 * @param activity
	 *            the activity
	 * @return the work list
	 * @throws Exception
	 *             the exception
	 */
	public static WorkList generateWorkListFromPartition(Partition partition, WorkflowDefinition workflowDefinition, List<WfMembership> workflowUserRoles, String name, I_ConfigAceFrame config, I_ShowActivity activity) throws Exception {
		WorkList workList = new WorkList(name, 0, null, partition.getUids().iterator().next());
		workList.setWorkflowDefinition(workflowDefinition);
		workList.setWorkflowUserRoles(workflowUserRoles);
		workList = createNewWorkList(workList, config);
		ActivityUpdater updater = null;
		if (activity != null) {
			updater = new ActivityUpdater(activity, "Generating WorkList");
		}
		if (workList != null) {
			initializeWorkList(partition, workList, config, updater);
		}
		Terms.get().addUncommittedNoChecks(workList.getConcept());
		workList.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);

		TerminologyProjectDAO.workListCache.put(workList.getUids().iterator().next(), workList);
		JOptionPane.showMessageDialog(null, "WorkList created!", "Success", JOptionPane.INFORMATION_MESSAGE);
		return workList;
	}

	/**
	 * Clean rel tuples list.
	 * 
	 * @param tuples
	 *            the tuples
	 * @return the list<? extends i_ rel tuple>
	 */
	private static List<? extends I_RelTuple> cleanRelTuplesList(List<? extends I_RelTuple> tuples) {
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
	 * Validate concept as refset.
	 * 
	 * @param concept
	 *            the concept
	 * @param config
	 *            the config
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 */
	public static boolean validateConceptAsRefset(I_GetConceptData concept, I_ConfigAceFrame config) throws IOException, TerminologyException, ContradictionException {
		I_TermFactory tf = Terms.get();
		if (tf.getConcept(RefsetAuxiliary.Concept.REFSET_AUXILIARY.getUids()).isParentOf(concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONS_ROOT.getUids()).isParentOf(concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.WORKSETS_ROOT.getUids()).isParentOf(concept)) {
			return true;

		} else if (tf.getConcept(ArchitectonicAuxiliary.Concept.WORKLISTS_ROOT.getUids()).isParentOf(concept)) {
			return true;

		}
		return false;
	}

	/**
	 * Promote.
	 * 
	 * @param termComponent
	 *            the term component
	 * @param config
	 *            the config
	 */
	public static void promote(I_GetConceptData termComponent, I_ConfigAceFrame config) {
		// PositionBI viewPosition =
		// config.getViewPositionSetReadOnly().iterator().next();
		// I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
		// allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
		// try {
		// allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		// allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		// // if
		// (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		// //
		// Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		// // }
		// // if
		// (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		// // Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		// // }
		// //
		// // Terms.get().commit();
		// termComponent.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// allowedStatusWithRetired, Precedence.TIME);
		// Terms.get().addUncommittedNoChecks(termComponent);
		//
		// // if
		// (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		// //
		// Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		// // }
		// // if
		// (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		// // Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		// // }
		// //
		// // Terms.get().commit();
		//
		// for (I_ExtendByRef loopExtension :
		// Terms.get().getAllExtensionsForComponent(termComponent.getConceptNid()))
		// {
		// loopExtension.promote(config.getViewPositionSet().iterator().next(),
		// config.getPromotionPathSetReadOnly(), allowedStatusWithRetired,
		// Precedence.TIME);
		// Terms.get().addUncommittedNoChecks(loopExtension);
		// }
		//
		// } catch (Exception e) {
		// AceLog.getAppLog().alertAndLogException(e);
		// }
		//
		//
		// // for (PathBI path : config.getEditingPathSetReadOnly()) {
		// // try {
		// // PositionBI viewPosition = Terms.get().newPosition(path,
		// Integer.MAX_VALUE);
		// // termComponent.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // config.getAllowedStatus(), Precedence.TIME);
		// // } catch (TerminologyException e) {
		// // AceLog.getAppLog().alertAndLogException(e);
		// // } catch (IOException e) {
		// // AceLog.getAppLog().alertAndLogException(e);
		// // }
		// // }
	}

	/**
	 * Promote.
	 * 
	 * @param termComponent
	 *            the term component
	 * @param config
	 *            the config
	 */
	public static void promote(I_ExtendByRef termComponent, I_ConfigAceFrame config) {
		// PositionBI viewPosition =
		// config.getViewPositionSetReadOnly().iterator().next();
		// I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
		// allowedStatusWithRetired.addAll(config.getAllowedStatus().getSetValues());
		// try {
		// allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		// allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		// // if
		// (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		// //
		// Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		// // }
		// // if
		// (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		// // Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		// // }
		// //
		// // Terms.get().commit();
		//
		// termComponent.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// allowedStatusWithRetired, Precedence.TIME);
		// Terms.get().addUncommittedNoChecks(termComponent);
		//
		// // if
		// (I_GetConceptData.class.isAssignableFrom(termComponent.getClass())) {
		// //
		// Terms.get().addUncommittedNoChecks((I_GetConceptData)termComponent);
		// // }
		// // if
		// (I_ExtendByRef.class.isAssignableFrom(termComponent.getClass())) {
		// // Terms.get().addUncommittedNoChecks((I_ExtendByRef)termComponent);
		// // }
		// //
		// // Terms.get().commit();
		// } catch (Exception e) {
		// AceLog.getAppLog().alertAndLogException(e);
		// }
		//
		//
		// // for (PathBI path : config.getEditingPathSetReadOnly()) {
		// // try {
		// // PositionBI viewPosition = Terms.get().newPosition(path,
		// Integer.MAX_VALUE);
		// // termComponent.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // config.getAllowedStatus(), Precedence.TIME);
		// // } catch (TerminologyException e) {
		// // AceLog.getAppLog().alertAndLogException(e);
		// // } catch (IOException e) {
		// // AceLog.getAppLog().alertAndLogException(e);
		// // }
		// // }
	}

	/**
	 * Gets the users for role.
	 * 
	 * @param role
	 *            the role
	 * @param project
	 *            the project
	 * @param config
	 *            the config
	 * 
	 * @return the users for role
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws TerminologyException
	 *             the terminology exception
	 * 
	 * @deprecated moved to ProjectPermissionAPI
	 */
	public static Set<I_GetConceptData> getUsersForRole(I_GetConceptData role, I_GetConceptData project, I_ConfigAceFrame config) throws IOException, TerminologyException {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.getUsersForRole(role, project);
	}

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of
	 * the User concept in the top hierarchy, and have a description of type
	 * "user inbox".
	 * 
	 * @param config
	 *            the config
	 * @return The set of valid users.
	 * @deprecated moved to ProjectPermissionAPI
	 */
	public static Set<I_GetConceptData> getUsers(I_ConfigAceFrame config) {
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);

		return permissionsApi.getUsers();
	}

	/**
	 * Sleep.
	 * 
	 * @param n
	 *            the n
	 */
	public static void sleep(int n) {
		try {
			Thread.sleep(n);
		} catch (InterruptedException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	@Deprecated
	/**
	 * Use the concept by concept promotion
	 * promoteTargetContentToReleasePath(...)
	 */
	public static boolean promoteWorkListContentToReleaseCandidatePath(I_GetConceptData conflictResolutionPathConcept, I_GetConceptData releaseCandidatePathConcept, WorkList workList, I_ConfigAceFrame config) throws TerminologyException, IOException {
		int promoted = 0;
		I_TermFactory tf = Terms.get();
		PathSetReadOnly promotionPathSet = new PathSetReadOnly(tf.getPath(releaseCandidatePathConcept.getConceptNid()));
		PositionBI viewPosition = tf.newPosition(tf.getPath(conflictResolutionPathConcept.getConceptNid()), Integer.MAX_VALUE);
		for (WorkListMember loopMember : workList.getWorkListMembers()) {
			if (loopMember.getConcept().promote(viewPosition, promotionPathSet, config.getAllowedStatus(), Precedence.TIME, config.getDbConfig().getUserConcept().getNid())) {
				promoted++;
			}
		}
		return (promoted > 0);
	}

	@Deprecated
	/**
	 * Use the concept by concept promotion
	 * promoteTargetContentToReleasePath(...)
	 */
	public static void promoteLanguageContent(WorkListMember member, I_ConfigAceFrame config) throws Exception {
		// PROMOTION REMOVED, No promotion path will be used
		// I_TermFactory tf = Terms.get();
		// I_GetConceptData concept = member.getConcept();
		//
		// // Policy: Only one viewposition set, only one promotion path, only
		// one edit path
		// //TODO: promote only language content
		//
		// concept.promote(config.getViewPositionSetReadOnly().iterator().next(),
		// config.getPromotionPathSetReadOnly(),
		// config.getAllowedStatus(), config.getPrecedence());
		// tf.addUncommittedNoChecks(concept);
		//
		// for (I_ExtendByRef loopExtension :
		// tf.getAllExtensionsForComponent(concept.getConceptNid())) {
		// loopExtension.promote(config.getViewPositionSetReadOnly().iterator().next(),
		// config.getPromotionPathSetReadOnly(),
		// config.getAllowedStatus(), config.getPrecedence());
		// tf.addUncommittedNoChecks(loopExtension);
		// }
		//
		// // END OF NEW PROMOTION ALGORITHM
		//
		// // WorkList workList = TerminologyProjectDAO.getWorkList(
		// // tf.getConcept(member.getWorkListUUID()), config);
		// // CommentsRefset commentsRefset =
		// workList.getCommentsRefset(config);
		// // PromotionRefset promotionRefset =
		// workList.getPromotionRefset(config);
		// // TranslationProject project = (TranslationProject)
		// TerminologyProjectDAO.getProjectForWorklist(workList, config);
		// // if (project.getTargetLanguageRefset() == null) {
		// // JOptionPane.showMessageDialog(new JDialog(),
		// "Target language refset cannot be retrieved\nCheck project details",
		// // "Error", JOptionPane.ERROR_MESSAGE);
		// // throw new
		// Exception("Target language refset cannot be retrieved.");
		// // }
		// // int targetLanguageRefsetId =
		// project.getTargetLanguageRefset().getConceptNid();
		// // LanguageMembershipRefset langRefset = new
		// LanguageMembershipRefset(project.getTargetLanguageRefset(), config);
		// // int commentsLanguageRefsetId =
		// langRefset.getCommentsRefset(config).getRefsetId();
		// //
		// // I_IntSet allowedStatusWithRetired = Terms.get().newIntSet();
		// // for (int id : config.getAllowedStatus().getSetValues()) {
		// // allowedStatusWithRetired.add(id);
		// // }
		// //
		// allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		// // //TODO: validate only one viewposition, editPath and promotePath
		// // int editPathId =
		// config.getEditingPathSet().iterator().next().getConceptNid();
		// // int promotePathId =
		// config.getPromotionPathSet().iterator().next().getConceptNid();
		// // PositionBI viewPosition =
		// config.getViewPositionSetReadOnly().iterator().next();
		// // Set<PositionBI> viewPositions= new HashSet<PositionBI>();
		// // viewPositions.add(viewPosition);
		// // PositionSetReadOnly originPositionsReadOnly = new
		// PositionSetReadOnly(viewPositions);
		// //
		// // Set<PositionBI> targetPositions= new HashSet<PositionBI>();
		// // for (PathBI loopPath : config.getPromotionPathSet()) {
		// // targetPositions.add(tf.newPosition(loopPath, Integer.MAX_VALUE));
		// // }
		// // PositionSetReadOnly targetPositionsReadOnly = new
		// PositionSetReadOnly(targetPositions);
		// //
		// // // Check for changes in descriptions
		// // List<? extends I_DescriptionTuple> originDescriptions =
		// concept.getDescriptionTuples(allowedStatusWithRetired,
		// config.getDescTypes(),
		// // originPositionsReadOnly, Precedence.TIME,
		// config.getConflictResolutionStrategy());
		// //
		// // List<? extends I_DescriptionTuple> targetDescriptions =
		// concept.getDescriptionTuples(allowedStatusWithRetired,
		// config.getDescTypes(),
		// // targetPositionsReadOnly, Precedence.TIME,
		// config.getConflictResolutionStrategy());
		// //
		// // Set<Integer> originDescIds = new HashSet<Integer>();
		// // for (I_DescriptionTuple loopTuple : originDescriptions) {
		// // originDescIds.add(loopTuple.getDescId());
		// // }
		// // Set<Integer> targetDescIds = new HashSet<Integer>();
		// // for (I_DescriptionTuple loopTuple : targetDescriptions) {
		// // targetDescIds.add(loopTuple.getDescId());
		// // }
		// // // new Descriptions
		// // for (Integer loopDescId : originDescIds) {
		// // if (!targetDescIds.contains(loopDescId)) {
		// // tf.getDescription(loopDescId).promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // allowedStatusWithRetired, Precedence.TIME);
		// // }
		// // }
		// // // retired Descriptions
		// // for (Integer loopDescId : targetDescIds) {
		// // if (!originDescIds.contains(loopDescId)) {
		// // tf.getDescription(loopDescId).promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // allowedStatusWithRetired, Precedence.TIME);
		// // }
		// // }
		// // // Check for new versions of descriptions
		// // for (I_DescriptionTuple originLoopTuple : originDescriptions) {
		// // for (I_DescriptionTuple targetLoopTuple : targetDescriptions) {
		// // if (originLoopTuple.getDescId() == targetLoopTuple.getDescId()) {
		// // if (!originLoopTuple.getText().equals(targetLoopTuple.getText())
		// ||
		// // originLoopTuple.getTypeNid() != targetLoopTuple.getTypeNid() ||
		// // originLoopTuple.isInitialCaseSignificant() !=
		// targetLoopTuple.isInitialCaseSignificant() ||
		// // originLoopTuple.getStatusNid() != targetLoopTuple.getStatusNid())
		// {
		// // originLoopTuple.getDescVersioned().promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // allowedStatusWithRetired, Precedence.TIME);
		// // }
		// // }
		// // }
		// // }
		// //
		// // Collection<? extends I_ExtendByRef> langExtensions =
		// project.getTargetLanguageRefset().getExtensions();
		// // for (I_ExtendByRef iExtendByRef : langExtensions) {
		// // iExtendByRef.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // allowedStatusWithRetired, Precedence.TIME);
		// // }
		// //
		// //// Collection<? extends I_ExtendByRef> extensions =
		// tf.getAllExtensionsForComponent(concept.getConceptNid());
		// //// for (I_ExtendByRef loopExtension : extensions) {
		// //// if (loopExtension.getRefsetId() == commentsRefset.getRefsetId()
		// ||
		// //// loopExtension.getRefsetId() == promotionRefset.getRefsetId() ||
		// //// loopExtension.getRefsetId() == commentsLanguageRefsetId ||
		// //// loopExtension.getRefsetId() == targetLanguageRefsetId) {
		// //// List<? extends I_ExtendByRefVersion> tuples =
		// loopExtension.getTuples(config.getAllowedStatus(),
		// //// config.getViewPositionSetReadOnly(), Precedence.TIME,
		// //// config.getConflictResolutionStrategy());
		// //// if (tuples != null && !tuples.isEmpty()) {
		// //// I_ExtendByRefPart lastPart =
		// tuples.iterator().next().getMutablePart();
		// ////
		// //// if (lastPart.getPathNid() == editPathId) {
		// //// boolean promoted = false;
		// //// for (I_ExtendByRefPart loop2Part :
		// loopExtension.getMutableParts()) {
		// //// if (loop2Part.getPathNid() == promotePathId &&
		// lastPart.compareTo(loop2Part) == 0) {
		// //// promoted = true;
		// //// }
		// //// }
		// //// if (!promoted) {
		// //// loopExtension.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// //// allowedStatusWithRetired, Precedence.TIME);
		// //// }
		// //// }
		// ////
		// //// }
		// //// }
		// //// }
		// // //TODO: extension.getTuples is always size =1 or 0??
		// // // I_ExtendByRefVersion originVersion =
		// loopExtension.getTuples(allowedStatusWithRetired,
		// // // originPositionsReadOnly,
		// // // Precedence.TIME,
		// config.getConflictResolutionStrategy()).iterator().next();
		// // // I_ExtendByRefVersion targetVersion =
		// loopExtension.getTuples(allowedStatusWithRetired,
		// // // targetPositionsReadOnly,
		// // // Precedence.TIME,
		// config.getConflictResolutionStrategy()).iterator().next();
		// // //
		// // // if ((originVersion != null && targetVersion == null) ||
		// // // (originVersion == null && targetVersion != null)) {
		// // // loopExtension.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // // allowedStatusWithRetired, Precedence.TIME);
		// // //
		// tf.addUncommittedNoChecks(tf.getConcept(loopExtension.getRefsetId()));
		// // // } else if ((originVersion != null && targetVersion != null)) {
		// // // if (originVersion.getVersion() != targetVersion.getVersion()) {
		// // //
		// tf.addUncommittedNoChecks(tf.getConcept(loopExtension.getRefsetId()));
		// // // loopExtension.promote(viewPosition,
		// config.getPromotionPathSetReadOnly(),
		// // // allowedStatusWithRetired, Precedence.TIME);
		// // // }
		// // // }
		// // tf.commit();
	}

	public static void initializeWorkflowForMember(WorkListMember member, WorkList workList, I_ConfigAceFrame config) throws Exception {
		WfInstance instance = new WfInstance();
		WfComponentProvider prov = new WfComponentProvider();
		instance.setComponentId(SNOMED.Concept.ROOT.getPrimoridalUid());
//		instance.setState(prov.statusConceptToWfState(Terms.get().getConcept(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids())));
        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
			instance.setState(prov.statusConceptToWfState(Terms.get().getConcept(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"))));
		} else {
			instance.setState(prov.statusConceptToWfState(Terms.get().getConcept(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"))));
		}
		instance.setWfDefinition(workList.getWorkflowDefinition());
		instance.setWorkList(workList);
		instance.setLastChangeTime(System.currentTimeMillis());
		WorkflowInterpreter interpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());

		WfUser user = interpreter.getNextDestination(instance, workList);
		// if (user == null) {
		// throw new Exception("Cannot set next destination\n");
		// }

		TerminologyStoreDI ts = Ts.get();

		PromotionAndAssignmentRefset promRef = workList.getPromotionRefset(config);
		ConceptVersionBI concept = ts.getConceptVersion(config.getViewCoordinate(), ts.getNidForUuids(member.getUids()));

		int userNid = promRef.getDestination(concept.getNid(), config).getNid();

		if (user != null) {
			userNid = ts.getNidForUuids(user.getId());
		}

		RefexCAB newSpec = new RefexCAB(TK_REFEX_TYPE.CID, concept.getNid(), ts.getNidForUuids(workList.getUids()));
		int activeNid = SnomedMetadataRf1.CURRENT_RF1.getLenient().getNid();
//		int assignedNid = Terms.get().uuidToNative(ArchitectonicAuxiliary.Concept.WORKLIST_ITEM_ASSIGNED_STATUS.getUids());
		int assignedNid = -1;
        if (Terms.get().getActiveAceFrameConfig().isVaProject()) {
			assignedNid = Terms.get().uuidToNative(UUID.fromString("904447b8-d079-5167-8bf0-928bbdcb9e5b"));
		} else {
			assignedNid = Terms.get().uuidToNative(UUID.fromString("2cd075aa-fa92-5aa5-9f3d-d68c1c241d42"));
		}
		newSpec.put(RefexProperty.CNID1, activeNid);

		TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), config.getViewCoordinate());
		RefexChronicleBI<?> newRefex = tc.constructIfNotCurrent(newSpec);

		RefexCAB newSpecForProm = new RefexCAB(TK_REFEX_TYPE.CID_CID, concept.getNid(), promRef.getRefsetId());
		newSpecForProm.put(RefexProperty.CNID1, assignedNid);
		newSpecForProm.put(RefexProperty.CNID2, userNid);
		RefexChronicleBI<?> newRefexForProm = tc.constructIfNotCurrent(newSpecForProm);
		concept.addAnnotation(newRefexForProm);

	}

	private static I_IntSet allowedStatusWithRetired;

	static {

		allowedStatusWithRetired = Terms.get().newIntSet();
		try {
			allowedStatusWithRetired.addAll(Terms.get().getActiveAceFrameConfig().getAllowedStatus().getSetValues());
			allowedStatusWithRetired.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			allowedStatusWithRetired.add(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public static void promoteTargetContentToReleasePath(TranslationProject project, ConceptChronicleBI concept, I_ConfigAceFrame config) {
		try {
			// TerminologyStoreDI ts = Ts.get();
			I_TermFactory tf = Terms.get();

			I_IntSet status = config.getAllowedStatus();
			config.setAllowedStatus(allowedStatusWithRetired);
			int targetLangNid = project.getTargetLanguageRefset().getNid();
			I_GetConceptData pathConcept = project.getReleasePath();

			// EditCoordinate ec = new
			// EditCoordinate(config.getDbConfig().getUserConcept().getNid(),
			// pathConcept.getNid());

			// TerminologyBuilderBI tc = ts.getTerminologyBuilder(ec,
			// config.getViewCoordinate());

			for (DescriptionChronicleBI loopDescription : concept.getDescriptions()) {

				DescriptionVersionBI lastDescVersion = loopDescription.getVersion(config.getViewCoordinate());
				if (lastDescVersion != null) {
					// DescCAB dcab =
					// lastDescVersion.makeBlueprint(config.getViewCoordinate());
					// dcab.setComponentUuidNoRecompute(loopDescription.getPrimUuid());
					boolean written = false;
					for (RefexChronicleBI<?> loopAnnotChronicle : loopDescription.getAnnotations()) {
						if (loopAnnotChronicle.getRefexNid() == targetLangNid) {
							if (!written) {
								I_DescriptionVersioned descriptionVersioned = tf.getDescription(loopDescription.getNid());
								I_DescriptionTuple descriptionTuple = (I_DescriptionTuple) descriptionVersioned.getLastTuple();
								I_DescriptionPart newDescriptionPart = (I_DescriptionPart) descriptionTuple.getMutablePart().makeAnalog(descriptionTuple.getMutablePart().getStatusNid(), Long.MAX_VALUE, config.getDbConfig().getUserConcept().getNid(), config.getEditCoordinate().getModuleNid(),
										pathConcept.getNid());
								newDescriptionPart.setText(lastDescVersion.getText());
								newDescriptionPart.setAuthorNid(config.getDbConfig().getUserConcept().getConceptNid());
								newDescriptionPart.setLang(lastDescVersion.getLang());
								newDescriptionPart.setTypeNid(lastDescVersion.getTypeNid());
								descriptionVersioned.addVersion(newDescriptionPart);
								// DescriptionChronicleBI desc =
								// tc.constructIfNotCurrent(dcab);
								// written = true;
							}
							I_ExtendByRef langExtension = tf.getExtension(loopAnnotChronicle.getNid());
							I_ExtendByRefVersion langExtV = langExtension.getTuples(allowedStatusWithRetired, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();
							I_ExtendByRefPart newLangExtV = (I_ExtendByRefPart) langExtV.makeAnalog(langExtV.getStatusNid(), Long.MAX_VALUE, langExtV.getAuthorNid(), config.getEditCoordinate().getModuleNid(), pathConcept.getNid());
							langExtension.addVersion(newLangExtV);
							// RefexVersionBI loopAnnotV =
							// loopAnnotChronicle.getVersion(config.getViewCoordinate());
							// RefexNidVersionBI loopAnnotC =
							// (RefexNidVersionBI) loopAnnotV;
							// RefexCAB acab =
							// loopAnnotC.makeBlueprint(config.getViewCoordinate());
							// RefexChronicleBI<?> newRefexForProm =
							// tc.constructIfNotCurrent(acab);
							// concept.addAnnotation(newRefexForProm);
						}
					}
				}
			}

			Terms.get().addUncommittedNoChecks((I_GetConceptData) concept);
			config.setAllowedStatus(status);
		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ContradictionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (Exception e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public static I_TerminologyProject getProject(I_GetConceptData projectConcept, I_ConfigAceFrame aceFrameConfig) {
		I_TerminologyProject project = null;
		try {
			project = getTranslationProject(projectConcept, aceFrameConfig);
		} catch (Exception e) {

		}
		if (project == null) {
			try {
				project = getTerminologyProject(projectConcept, aceFrameConfig);
			} catch (Exception e) {

			}
		}
		if (project == null) {
			try {
				project = getMappingProject(projectConcept, aceFrameConfig);
			} catch (Exception e) {

			}
		}
		return project;
	}

	public static List<WfMembership> convertToMembershipList(List<String> list) throws Exception {
		List<WfMembership> members = new ArrayList<WfMembership>();
		for (String line : list) {
			String[] fields = line.split("\\|");
			I_GetConceptData userConcept = Terms.get().getConcept(UUID.fromString(fields[1]));
			if (userConcept.getDescriptions().size() > 0) {
				WfUser user = new WfUser(getConceptString(userConcept), userConcept.getPrimUuid());
	
				I_GetConceptData roleConcept = Terms.get().getConcept(UUID.fromString(fields[2]));
				WfRole role = new WfRole(getConceptString(roleConcept), roleConcept.getPrimUuid());
	
				WfMembership member = new WfMembership(UUID.fromString(fields[0]), user, role, Boolean.parseBoolean(fields[3]));
				members.add(member);
			}
		}

		return members;
	}

	public static List<String> convertToStringListOfMembers(List<WfMembership> list) {
		List<String> members = new ArrayList<String>();
		for (WfMembership line : list) {
			members.add(line.getId().toString() + "|" + line.getUser().getId().toString() + "|" + line.getRole().getId().toString() + "|" + line.isDefaultAssignment());
		}

		return members;
	}

	public static I_TerminologyProject createNewMappingProject(String projectName, I_ConfigAceFrame config) {
		// TODO Auto-generated method stub
		MappingProject project = new MappingProject(projectName, 0, null);
		return createNewMappingProject(project, config);
	}

	public static void promoteContent(int conceptNid, ViewCoordinate sourceViewCoordinate, int targetPathNid) throws Exception {
		EditCoordinate editCoord = Terms.get().getActiveAceFrameConfig().getEditCoordinate();

		PositionBI[] positionSet = sourceViewCoordinate.getPositionSet().getPositionArray();
		PositionBI originPosition = positionSet[0].getAllOrigins().iterator().next();

		TerminologyPromoterBI promoter = Ts.get().getTerminologyPromoter(sourceViewCoordinate, editCoord, targetPathNid, originPosition);

		NidBitSetBI promotionNids = Ts.get().getEmptyNidSet();
		promotionNids.setMember(conceptNid);

		promoter.promote(promotionNids, false);
	}

}
