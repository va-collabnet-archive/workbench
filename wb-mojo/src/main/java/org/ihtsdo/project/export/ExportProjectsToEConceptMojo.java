package org.ihtsdo.project.export;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.WorkflowRefset;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.dto.concept.TkConcept;

/**
 * Goal exports the concepts, worksets and worklists included in a database, creating a binary EConcepts file as a result.
 *
 * @goal export-projects
 * 
 * @phase process-sources
 */
public class ExportProjectsToEConceptMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	private final Semaphore writeSemaphore = new Semaphore(1);

	private DataOutputStream eConceptDOS;

	private Set<TkConcept> conceptsToWrite;

	I_TermFactory tf;

	TerminologyStoreDI ts;

	I_ConfigAceFrame config;

	@Override
	public void execute() throws MojoExecutionException {
		try {

			conceptsToWrite =  new HashSet<TkConcept>();
			tf = Terms.get();
			ts = Ts.get();
			config = tf.getActiveAceFrameConfig();

			config.getViewPositionSet().clear();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("f9711e44-4f1f-5655-990d-0fe996c4fcc9")}), 
					Long.MAX_VALUE));

			File eConceptsFile = new File(targetDirectory, "/eConcepts.jbin");
			eConceptsFile.getParentFile().mkdirs();
			BufferedOutputStream eConceptsBos = new BufferedOutputStream(new FileOutputStream(eConceptsFile));
			eConceptDOS = new DataOutputStream(eConceptsBos);

			List<TranslationProject> transProjects = TerminologyProjectDAO.getAllTranslationProjects(config);

			for (TranslationProject  loopProject : transProjects) {
				addWorkflowRefset(loopProject.getId());
				for (I_GetConceptData loopRefset : loopProject.getCommonRefsets()) {
					addRefsetSpec(loopRefset);
				}
				for (I_GetConceptData loopRefset : loopProject.getExclusionRefsets()) {
					addRefsetSpec(loopRefset);
				}

				if (loopProject.getProjectIssueRepo() != null) {
					addToWriteList(loopProject.getProjectIssueRepo());
				}

				if (loopProject.getSourceIssueRepo() != null) {
					addToWriteList(loopProject.getSourceIssueRepo());
				}

				for (I_GetConceptData loopRefset : loopProject.getSourceLanguageRefsets()) {
					addToWriteList(loopRefset);
				}

				if (loopProject.getTargetLanguageRefset() != null) {
					addToWriteList(loopProject.getTargetLanguageRefset());
				}

				for (WorkSet loopWorkSet : loopProject.getWorkSets(config)) {
					addToWriteList(loopWorkSet.getRefsetConcept());
					if (loopWorkSet.getSourceRefset() != null) {
						addRefsetSpec(loopWorkSet.getSourceRefset());
					}

					for (PartitionScheme ps : loopWorkSet.getPartitionSchemes(config)) {
						writePartitionScheme(ps);
					}

					for (I_GetConceptData loopRefset : loopWorkSet.getExclusionRefsets()) {
						addRefsetSpec(loopRefset);
					}

				}
			}

			List<TerminologyProject> termProjects = TerminologyProjectDAO.getAllTerminologyProjects(config);

			for (TerminologyProject  loopProject : termProjects) {
				addWorkflowRefset(loopProject.getId());

				for (WorkSet loopWorkSet : loopProject.getWorkSets(config)) {
					addToWriteList(loopWorkSet.getRefsetConcept());
					if (loopWorkSet.getSourceRefset() != null) {
						addRefsetSpec(loopWorkSet.getSourceRefset());
					}

					for (PartitionScheme ps : loopWorkSet.getPartitionSchemes(config)) {
						writePartitionScheme(ps);
					}

					for (I_GetConceptData loopRefset : loopWorkSet.getExclusionRefsets()) {
						addRefsetSpec(loopRefset);
					}

				}
			}

			addToWriteList(ArchitectonicAuxiliary.Concept.WORKLISTS_EXTENSION_REFSET.localize().getNid());
			
			for (TkConcept loopEConcept : conceptsToWrite) {
				write (loopEConcept);
			}

			eConceptDOS.close();
			eConceptsBos.close();

		} catch (Exception e1) {
			throw new MojoExecutionException( "Error in export", e1 );
		}
	}

	private void writePartitionScheme(PartitionScheme ps) throws Exception{
		if (ps != null && ps.getPartitions() != null) {
			for (Partition loopPartition : ps.getPartitions()) {
				addToWriteList(loopPartition.getConcept());
				for (WorkList loopWlist : loopPartition.getWorkLists()) {
					addWorkflowRefset(loopWlist.getId());
				}
				for (PartitionScheme ps2 : loopPartition.getSubPartitionSchemes(config)) {
					writePartitionScheme(ps2);
				}
			}
		}
	}


	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	/**
	 * Write a single Econcept to the DataOutputStream.
	 *
	 * @param eConcept the e concept
	 * @throws IOException signals that an I/O exception has occurred.
	 */
	public void addToWriteList(TkConcept eConcept) throws IOException {
		conceptsToWrite.add(eConcept);
	}

	private void write(TkConcept eConcept) throws IOException {
		System.out.println("Writing: " + eConcept.getDescriptions().iterator().next().getText());
		writeSemaphore.acquireUninterruptibly();
		try {
			eConcept.writeExternal(eConceptDOS);
		} finally {
			writeSemaphore.release();
		}
	}

	public void addToWriteList(int conceptNid) throws Exception {
		EConcept ec = new EConcept(tf.getConcept(conceptNid));
		addToWriteList(ec);
	}


	public void addToWriteList(I_GetConceptData concept) throws Exception {
		EConcept ec = new EConcept(concept);
		addToWriteList(ec);
	}

	public void addWorkflowRefset(int conceptNid) throws Exception {
		I_GetConceptData concept = tf.getConcept(conceptNid);
		addToWriteList(concept);
		WorkflowRefset wfr = new WorkflowRefset(concept);
		if (wfr.getCommentsRefset(config) != null) {
			addToWriteList(wfr.getCommentsRefset(config).getRefsetConcept());
		}
		if (wfr.getPromotionRefset(config) != null) {
			addToWriteList(wfr.getPromotionRefset(config).getRefsetConcept());
		}
	}

	public void addRefsetSpec(I_GetConceptData refset)  throws Exception {
		RefsetSpec refsetSpecHelper = new RefsetSpec(refset, config);
		I_GetConceptData memberRefset = refsetSpecHelper.getMemberRefsetConcept();
		I_GetConceptData markedParentRefset = refsetSpecHelper.getMarkedParentRefsetConcept();
		I_GetConceptData commentsRefset = refsetSpecHelper.getCommentsRefsetConcept();
		I_GetConceptData promotionRefset = refsetSpecHelper.getPromotionRefsetConcept();
		I_GetConceptData editTimeRefset = refsetSpecHelper.getEditConcept();
		I_GetConceptData computeTimeRefset = refsetSpecHelper.getComputeConcept();
		if (memberRefset == null) {
			// "No member spec found. Please put the refset to be exported in the refset spec panel."
			addToWriteList(refset);
			return;
		}
		if (markedParentRefset == null) {
			//"No marked parent refset found - the member refset should have a 'marked parent refset' relationship to the marked parent refset."
			addToWriteList(refset);
			return;
		}

		addToWriteList(refset);
		addToWriteList(memberRefset);
		addToWriteList(markedParentRefset);
		addToWriteList(commentsRefset);
		addToWriteList(promotionRefset);
		addToWriteList(editTimeRefset);
		addToWriteList(computeTimeRefset);
	}

}
