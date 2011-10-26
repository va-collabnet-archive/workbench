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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCid;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.partition.IsDescendantOfPartitioner;
import org.ihtsdo.project.refset.partition.QuantityPartitioner;
import org.ihtsdo.project.refset.partition.RefsetSplitter;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestWorksetsAndWorklistsCRUD extends TestCase {

	/** The vodb directory. */
	File vodbDirectory;

	/** The read only. */
	boolean readOnly = false;

	/** The cache size. */
	Long cacheSize = Long.getLong("600000000");

	/** The db setup config. */
	DatabaseSetupConfig dbSetupConfig;

	/** The config. */
	I_ConfigAceFrame config;

	/** The tf. */
	I_TermFactory tf;

	/** The new project concept. */
	I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	TranslationProject project = null;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/project/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);

	}

	public void testCreateNewWorkSet() {
		try {

			project = TerminologyProjectDAO.createNewTranslationProject("Test project", config);

			I_GetConceptData rootConcept = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			I_GetConceptData procedure = tf.getConcept(UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
			I_GetConceptData colonoscopicPolypectomy = tf.getConcept(UUID.fromString("bd2ce7f7-20a5-3ce4-b044-141284395579"));
			I_GetConceptData pneumonitis = tf.getConcept(UUID.fromString("975420b8-e706-3744-a9f6-54180b5a5469"));
			I_GetConceptData english = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
			I_GetConceptData spanish = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_ES.getUids());
			I_GetConceptData danish = tf.getConcept(RefsetAuxiliary.Concept.LANGUAGE_REFSET_DA.getUids());
			
			//Setup language refsets
			LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(english, "EN", config);
			LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(spanish, "ES", config);
			LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(danish, "DA", config);
			
			I_IntSet allowedTypes = Terms.get().newIntSet();
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			allowedTypes.add(tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));

			List<? extends I_RelTuple> relationships = rootConcept.getDestRelTuples(config.getAllowedStatus(), 
					allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME, config.getConflictResolutionStrategy());
			
			I_GetConceptData refset = tf.getConcept(RefsetAuxiliary.Concept.DESCRIPTION_TYPE_PURPOSE.getUids());
			for (I_RelTuple rel : relationships) {
				I_GetConceptData member = tf.getConcept(rel.getC1Id());
				tf.getRefsetHelper(config).newRefsetExtension(
						refset.getConceptNid(), 
						member.getConceptNid(), 
						EConcept.REFSET_TYPES.CID, 
						new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, member.getConceptNid()),config); 
				tf.addUncommittedNoChecks(refset);
				tf.addUncommittedNoChecks(member);
			}
			tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					pneumonitis.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, 
							pneumonitis.getConceptNid()),config);
			tf.addUncommittedNoChecks(refset);
			tf.addUncommittedNoChecks(pneumonitis);
			tf.commit();
			
			assertEquals(19, tf.getRefsetExtensionMembers(refset.getConceptNid()).size());
			
			assertEquals(0, project.getExclusionRefsets().size());
			project.addRefsetAsExclusion(refset);
			sleep(1);
			assertEquals(1, project.getExclusionRefsets().size());
			project.addRefsetAsExclusion(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(2, project.getExclusionRefsets().size());
			project.removeRefsetAsExclusion(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(1, project.getExclusionRefsets().size());
			project.removeRefsetAsExclusion(refset);
			tf.commit();
			sleep(1);
			assertEquals(0, project.getExclusionRefsets().size());
			project.addRefsetAsExclusion(refset);
			sleep(1);
			assertEquals(1, project.getExclusionRefsets().size());
			project.addRefsetAsExclusion(rootConcept);
			sleep(1);
			assertEquals(2, project.getExclusionRefsets().size());
			
			assertEquals(0, project.getCommonRefsets().size());
			project.addRefsetAsCommon(refset);
			sleep(1);
			assertEquals(1, project.getCommonRefsets().size());
			project.addRefsetAsCommon(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(2, project.getCommonRefsets().size());
			project.removeRefsetAsCommon(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(1, project.getCommonRefsets().size());
			project.removeRefsetAsCommon(refset);
			tf.commit();
			sleep(1);
			assertEquals(0, project.getCommonRefsets().size());
			project.addRefsetAsCommon(refset);
			sleep(1);
			assertEquals(1, project.getCommonRefsets().size());
			project.addRefsetAsCommon(rootConcept);
			sleep(1);
			assertEquals(2, project.getCommonRefsets().size());
			
			assertEquals(0, project.getSourceLanguageRefsets().size());
			project.addRefsetAsSourceLanguage(english);
			sleep(1);
			assertEquals(1, project.getSourceLanguageRefsets().size());
			project.addRefsetAsSourceLanguage(spanish);
			tf.commit();
			sleep(1);
			assertEquals(2, project.getSourceLanguageRefsets().size());
			project.removeRefsetAsSourceLanguage(spanish);
			tf.commit();
			sleep(1);
			assertEquals(1, project.getSourceLanguageRefsets().size());
			project.removeRefsetAsSourceLanguage(english);
			tf.commit();
			sleep(1);
			assertEquals(0, project.getSourceLanguageRefsets().size());
			project.addRefsetAsSourceLanguage(english);
			sleep(1);
			assertEquals(1, project.getSourceLanguageRefsets().size());
			project.addRefsetAsSourceLanguage(danish);
			tf.commit();
			sleep(1);
			assertEquals(2, project.getSourceLanguageRefsets().size());
			
			assertNull(project.getTargetLanguageRefset());
			project.setTargetLanguageRefset(danish);
			tf.commit();
			sleep(1);
			assertNotNull(project.getTargetLanguageRefset());
			assertEquals(danish.getConceptNid(), project.getTargetLanguageRefset().getConceptNid());
			project.setTargetLanguageRefset(spanish);
			sleep(1);
			assertNotNull(project.getTargetLanguageRefset());
			assertEquals(spanish.getConceptNid(), project.getTargetLanguageRefset().getConceptNid());

			assertEquals(0, TerminologyProjectDAO.getAllWorkSetsForProject(project, config).size());

			WorkSet workSet1 = TerminologyProjectDAO.createNewWorkSet("WorkSet 1", 
					project.getUids().iterator().next(), config);
			
			tf.commit();

			assertEquals(1, TerminologyProjectDAO.getAllWorkSetsForProject(project, config).size());
			
			assertEquals(0, workSet1.getExclusionRefsets().size());
			workSet1.addRefsetAsExclusion(refset);
			sleep(1);
			assertEquals(1, workSet1.getExclusionRefsets().size());
			workSet1.addRefsetAsExclusion(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(2, workSet1.getExclusionRefsets().size());
			workSet1.removeRefsetAsExclusion(rootConcept);
			tf.commit();
			sleep(1);
			assertEquals(1, workSet1.getExclusionRefsets().size());
			workSet1.removeRefsetAsExclusion(refset);
			tf.commit();
			sleep(1);
			assertEquals(0, workSet1.getExclusionRefsets().size());

			assertNull(workSet1.getSourceRefset());
			workSet1.setSourceRefset(rootConcept);
			tf.commit();
			sleep(1);
			assertNotNull(workSet1.getSourceRefset());
			assertEquals(rootConcept.getConceptNid(), workSet1.getSourceRefset().getConceptNid());
			workSet1.setSourceRefset(refset);
			tf.commit();
			sleep(1);
			assertNotNull(workSet1.getSourceRefset());
			assertEquals(refset.getConceptNid(), workSet1.getSourceRefset().getConceptNid());

			assertEquals(0, workSet1.getWorkSetMembers().size());
			workSet1.sync(config);
			tf.commit();
			sleep(1);
			assertEquals(19, workSet1.getWorkSetMembers().size());

			I_GetConceptData additionalMember = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					colonoscopicPolypectomy.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, additionalMember.getConceptNid()),config);
			tf.addUncommittedNoChecks(refset);
			tf.addUncommittedNoChecks(colonoscopicPolypectomy);
			tf.commit();
			sleep(1);
			assertEquals(20, tf.getRefsetExtensionMembers(refset.getConceptNid()).size());
			workSet1.sync(config);
			tf.commit();
			sleep(1);
			assertEquals(20, workSet1.getWorkSetMembers().size());

			Collection<? extends I_ExtendByRef> extensions = additionalMember.getExtensions();
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == refset.getConceptNid()) {
					long lastVersion = Long.MIN_VALUE;
					I_ExtendByRefPart lastPart = null;
					for (I_ExtendByRefPart loopPart : extension.getMutableParts()) {
						if (loopPart.getTime() > lastVersion) {
							lastVersion = loopPart.getTime();
							lastPart = loopPart;
						}
					}
					I_ExtendByRefPartCidCid part = (I_ExtendByRefPartCidCid) 
					lastPart.makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
							config.getEditingPathSet().iterator().next().getConceptNid(),
							Long.MAX_VALUE);
					extension.addVersion(part);
					tf.addUncommittedNoChecks(extension);
					tf.commit();
				}
			}
			tf.commit(); sleep(1);
			workSet1.sync(config);
			assertEquals(20, workSet1.getWorkSetMembers().size());

			assertEquals(0, workSet1.getPartitionSchemes(config).size());
			PartitionScheme partitionScheme1 = TerminologyProjectDAO.createNewPartitionScheme(
					"partition Scheme 1", 
					workSet1.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(1, workSet1.getPartitionSchemes(config).size());
			assertEquals(0, partitionScheme1.getPartitions().size());
			
			IsDescendantOfPartitioner isDescendantOfPartitioner = new IsDescendantOfPartitioner(procedure);
			List<I_GetConceptData> previewPartitionMembers = 
				isDescendantOfPartitioner.getMembersToInclude(partitionScheme1, "partition 0", config);
			
			assertEquals(2, previewPartitionMembers.size());
			
			QuantityPartitioner quantityPartitioner = new QuantityPartitioner(7);
			previewPartitionMembers = 
				quantityPartitioner.getMembersToInclude(partitionScheme1, "partition 0", config);
			
			assertEquals(7, previewPartitionMembers.size());
			
			Partition partition0 = isDescendantOfPartitioner.createPartition(partitionScheme1, "partition 0", config);
			tf.commit(); sleep(1);
			assertEquals(1, partitionScheme1.getPartitions().size());
			assertEquals(2, partition0.getPartitionMembers().size());
			
			RefsetSplitter refsetSplitter = new RefsetSplitter();
			
			List<Integer> portions = new ArrayList<Integer>();
			portions.add(50);
			portions.add(50);
			
			List<Partition> splitPartitions = refsetSplitter.splitRefset(partitionScheme1, 
					portions, "splitted partition ", config);
			tf.commit(); sleep(1);
			
			Partition partition1 = splitPartitions.get(0);
			Partition partition2 = splitPartitions.get(1);
			
			/*
			assertEquals(2, partition0.getPartitionMembers().size());
			
			Partition partition1 = TerminologyProjectDAO.createNewPartition(
					"partition 1", partitionScheme1.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(2, partitionScheme1.getPartitions().size());
			Partition partition2 = TerminologyProjectDAO.createNewPartition(
					"partition 2", partitionScheme1.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(3, partitionScheme1.getPartitions().size());

			relationships = rootConcept.getDestRelTuples(config.getAllowedStatus(), 
					allowedTypes, config.getViewPositionSetReadOnly(), true, true);
			List<I_GetConceptData> remainingMembers = 
				TerminologyProjectDAO.getMembersNotPartitioned(partitionScheme1, config);
			int i = 0;
			for (I_GetConceptData loopMember : remainingMembers) {
				i++;
				if (i <= 9) {
					TerminologyProjectDAO.addConceptAsPartitionMember(
							loopMember, partition1.getUids().iterator().next(), config);
				} else {
					TerminologyProjectDAO.addConceptAsPartitionMember(
							loopMember, partition2.getUids().iterator().next(), config);
				}
			}
			*/
			tf.commit(); sleep(1);
			
			assertEquals(9, partition1.getPartitionMembers().size());
			assertEquals(9, partition2.getPartitionMembers().size());
			
			assertEquals(3, partitionScheme1.getPartitions().size());
			List<Partition> partitionsToCombine = new ArrayList<Partition>();
			partitionsToCombine.add(partition0);
			partitionsToCombine.add(partition1);
			Partition partition3 = TerminologyProjectDAO.combinePartitions(partitionsToCombine, 
					"combined partition", config);
			tf.commit(); sleep(1);
			assertEquals(11, partition3.getPartitionMembers().size());
			assertEquals(2, partitionScheme1.getPartitions().size());
			
			BusinessProcess bp = getBusinessProcess(new File("src/test/java/org/ihtsdo/project/sample.bp"));
			
			WorkList workList3 = TerminologyProjectDAO.generateWorkListFromPartition(
					partition3, "user.inbox", bp, "worklist 1", config);
			tf.commit(); sleep(1);
			assertEquals(11, workList3.getWorkListMembers().size());
			
			assertEquals(0, partition2.getSubPartitionSchemes(config).size());
			PartitionScheme partitionScheme2 = TerminologyProjectDAO.createNewPartitionScheme(
					"partition sub Scheme 2", 
					partition2.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(1, partition2.getSubPartitionSchemes(config).size());
			
			assertEquals(0, partitionScheme2.getPartitions().size());
			Partition partition21 = TerminologyProjectDAO.createNewPartition(
					"partition 2-1", partitionScheme2.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(1, partitionScheme2.getPartitions().size());
			Partition partition22 = TerminologyProjectDAO.createNewPartition(
					"partition 2-2", partitionScheme2.getUids().iterator().next(), config);
			tf.commit(); sleep(1);
			assertEquals(2, partitionScheme2.getPartitions().size());
			
			int i = 0;
			for (PartitionMember member : partition2.getPartitionMembers()) {
				i++;
				if (i<=5) {
					TerminologyProjectDAO.addConceptAsPartitionMember(
							member.getConcept(), partition21.getUids().iterator().next(), config);
				} else {
					TerminologyProjectDAO.addConceptAsPartitionMember(
							member.getConcept(), partition22.getUids().iterator().next(), config);
				}
			}
			
			tf.commit(); sleep(1);
			assertEquals(5, partition21.getPartitionMembers().size());
			assertEquals(4, partition22.getPartitionMembers().size());
			
			WorkList workList21 = TerminologyProjectDAO.generateWorkListFromPartition(
					partition21, "user.inbox", bp, "worklist 2-1", config);
			tf.commit(); sleep(1);
			assertEquals(5, workList21.getWorkListMembers().size());
			
			WorkList workList22 = TerminologyProjectDAO.generateWorkListFromPartition(
					partition22, "user.inbox", bp, "worklist 2-2", config);
			tf.commit(); sleep(1);
			assertEquals(4, workList22.getWorkListMembers().size());
			
			I_TerminologyProject retrievedProject = TerminologyProjectDAO.getProjectForWorklist(workList3, config);
			assertEquals(retrievedProject.getId(), project.getId());
			retrievedProject = TerminologyProjectDAO.getProjectForWorklist(workList22, config);
			assertEquals(retrievedProject.getId(), project.getId());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		Terms.close(tf);

	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("5e51196f-903e-5dd4-8b3e-658f7e0a4fe6")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.addPromotionPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			config.getDestRelTypes().add(tf.uuidToNative(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25")));
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			//			I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
			//	        newDbProfile.setUsername("username");
			//	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			//	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			//	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			//	        config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation , File targetLocation)
	throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i=0; i<children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]),
						new File(targetLocation, children[i]));
			}
		} else {

			InputStream in = new FileInputStream(sourceLocation);
			OutputStream out = new FileOutputStream(targetLocation);

			// Copy the bits from instream to outstream
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
	}

	public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}

	private static BusinessProcess getBusinessProcess(File f) {
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
}


