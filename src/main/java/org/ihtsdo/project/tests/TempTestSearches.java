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
package org.ihtsdo.project.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidCidCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.task.refset.spec.compute.RefsetComputeType;
import org.dwfa.ace.task.refset.spec.compute.RefsetQueryFactory;
import org.dwfa.ace.task.refset.spec.compute.RefsetSpecQuery;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.LanguageSpecRefset;
import org.ihtsdo.tk.api.PathBI;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TempTestSearches extends TestCase {

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

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/project/berkeley-db"), 
				new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}


	public void testSearch() {
		try {
			I_GetConceptData rootConcept = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
			I_GetConceptData procedure = tf.getConcept(UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
			I_GetConceptData administrativeProcedure = tf.getConcept(UUID.fromString("5ee78031-c76d-3b01-8df7-3d5243ba7876"));
			I_GetConceptData colonoscopicPolypectomy = tf.getConcept(UUID.fromString("bd2ce7f7-20a5-3ce4-b044-141284395579"));
			I_GetConceptData pneumonitis = tf.getConcept(UUID.fromString("975420b8-e706-3744-a9f6-54180b5a5469"));
			I_GetConceptData allergicAsthma = tf.getConcept(UUID.fromString("531abe20-8324-3db9-9104-8bcdbf251ac7"));

			I_IntSet allowedTypes = Terms.get().newIntSet();
			allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

			List<? extends I_RelTuple> relationships = rootConcept.getDestRelTuples(config.getAllowedStatus(), 
					allowedTypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
			I_GetConceptData refset = tf.getConcept(RefsetAuxiliary.Concept.GB_UKTC_CAB.getUids());
			for (I_RelTuple rel : relationships) {
				I_GetConceptData member = tf.getConcept(rel.getC1Id());
				 tf.getRefsetHelper(config).newRefsetExtension(
						refset.getConceptNid(), 
						member.getConceptNid(), 
						EConcept.REFSET_TYPES.CID, 
						new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, member.getConceptNid()),config); 
			}
			 tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					colonoscopicPolypectomy.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, colonoscopicPolypectomy.getConceptNid()),config);

			 tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					pneumonitis.getConceptNid(), 
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, pneumonitis.getConceptNid()),config);

			 tf.getRefsetHelper(config).newRefsetExtension(
					refset.getConceptNid(), 
					allergicAsthma.getConceptNid(),
					EConcept.REFSET_TYPES.CID, 
					new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, allergicAsthma.getConceptNid()),config);


			tf.commit();
			
			I_GetConceptData stringMatchSpecForAsthma = tf.getConcept(
					UUID.fromString("45175cb0-189d-4a36-8274-ec9a7c17e3e6"));
			I_GetConceptData descendantSpecForProcedures = tf.getConcept(
					UUID.fromString("8328bc6d-70e3-4fdd-8768-3b0a6d501844"));
			
			LanguageMembershipRefset refsetmember = LanguageMembershipRefset.createNewLanguageMembershipRefset(
					"target 1", refset.getConceptNid(), "en", config);

			LanguageMembershipRefset targetrefset = LanguageMembershipRefset.createNewLanguageMembershipRefset(
					"member 1", refset.getConceptNid(), "en", config);
			
			LanguageSpecRefset refsetspec = LanguageSpecRefset.createNewLanguageSpecRefset(
					"spec 1", refset.getConceptNid(), refsetmember.getRefsetId(), refset.getConceptNid(), config);

		

			int typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_EXTENSION.localize().getNid();
			UUID memberId =UUID.randomUUID();
//				tf.uuuidToNativeWithGeneration(UUID.randomUUID(),
//						ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), config
//						.getEditingPathSet(), Integer.MAX_VALUE);
			
			I_ExtendByRef ext = tf.newExtension(refsetspec.getRefsetId(), memberId, 
					refsetspec.getRefsetId(), typeId);
			Date date=new Date();
			for (PathBI p : config.getEditingPathSet()) {
				I_ExtendByRefPartCidCid specPart =tf.newExtensionPart(I_ExtendByRefPartCidCid.class);
				specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid());
				specPart.setC2id(RefsetAuxiliary.Concept.REFSET_OR_GROUPING.localize().getNid());
				specPart.setPathNid(p.getConceptNid());
				specPart.setStatusNid(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
				specPart.setTime(date.getTime());
				ext.addVersion(specPart);
			}
			tf.addUncommitted(ext);

			typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_CONCEPT_EXTENSION.localize().getNid();
			UUID memberId2 =UUID.randomUUID();
//				tf.uuidToNativeWithGeneration(UUID.randomUUID(),
//						ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid(), config
//						.getEditingPathSet(), Integer.MAX_VALUE);

			I_ExtendByRef ext2 = tf.newExtension(refsetspec.getRefsetId(), memberId2, 
				tf.uuidToNative(memberId), typeId);
			I_ExtendByRefPartCidCidCid specPart =
				tf.newExtensionPart(I_ExtendByRefPartCidCidCid.class);
			for (PathBI p : config.getEditingPathSet()) {
				specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid());
				specPart.setC2id(RefsetAuxiliary.Concept.CONCEPT_IS_KIND_OF.localize().getNid());
				specPart.setC3id(procedure.getConceptNid());
				specPart.setPathNid(p.getConceptNid());
				specPart.setStatusNid(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
				specPart.setTime(date.getTime());
				ext2.addVersion(specPart);
			}
			tf.addUncommitted(ext2);


			//			I_ThinExtByRefVersioned ext2 = tf.newExtension(refsetspec.getRefsetId(), memberId2, 
			//					memberId, typeId);
			//			for (I_Path p : config.getEditingPathSet()) {
			//				I_ThinExtByRefPartConceptConceptString specPart =
			//					tf.newExtensionPart(I_ThinExtByRefPartConceptConceptString.class);
			//				specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid());
			//				specPart.setC2id(RefsetAuxiliary.Concept.DESC_REGEX_MATCH.localize().getNid());
			//				specPart.setStringValue(".*"); // TODO
			//				specPart.setPathId(p.getConceptId());
			//				specPart.setStatusId(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			//				specPart.setVersion(Integer.MAX_VALUE);
			//				ext2.addVersion(specPart);
			//			}
			//			tf.addUncommitted(ext2);

			tf.commit();
			
			System.out.println(stringMatchSpecForAsthma);
			I_GetConceptData spec = getSpecRefsetConcept(stringMatchSpecForAsthma, config);
			System.out.println(spec);

			RefsetSpecQuery query =
				RefsetQueryFactory.createQuery(config, tf,  
						spec,
						null,
						RefsetComputeType.CONCEPT);

//			System.out.println("colonoscopicPolypectomy: " + query.execute(colonoscopicPolypectomy, config));
//			System.out.println("administrativeProcedure: " + query.execute(administrativeProcedure, config));
//			System.out.println("allergic asthma: " + query.execute(allergicAsthma, config));
//			
//			System.out.println(descendantSpecForProcedures);
//			spec = getSpecRefsetConcept(descendantSpecForProcedures, config);
//			System.out.println(spec);
//			
//			query =
//				RefsetQueryFactory.createQuery(config, tf,  
//						spec,
//						null,
//						RefsetComputeType.CONCEPT);
//			
//			System.out.println("colonoscopicPolypectomy: " + query.execute(colonoscopicPolypectomy, config));
//			System.out.println("administrativeProcedure: " + query.execute(administrativeProcedure, config));
//			System.out.println("allergic asthma: " + query.execute(allergicAsthma, config));

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//	public void testVisualizeRefsetSpecContent() {
	//		try {
	//			I_GetConceptData refsetSpecConcept = tf.getConcept(UUID.fromString("2ded1c1f-c676-42bf-9272-39320fc778fe"));
	//			//			I_IntSet allowedTypes = tf.newIntSet();
	//			//			allowedTypes.add(RefsetAuxiliary.Concept.SPECIFIES_REFSET.localize().getNid());
	//			//			Set<? extends I_GetConceptData> specConcepts = refsetSpecConcept.getDestRelOrigins(allowedTypes, true, true);
	//			//			I_GetConceptData specConcept = specConcepts.iterator().next();
	//
	//			for (I_ThinExtByRefVersioned extension : refsetSpecConcept.getExtensions()) {
	//				I_GetConceptData clausesExtConcept = tf.getConcept(extension.getRefsetId());
	//				I_GetConceptData typeConcept = tf.getConcept(extension.getTypeId());
	//				for (I_ThinExtByRefVersioned extensionMember :tf.getRefsetExtensionMembers(extension.getRefsetId())) {
	//					I_GetConceptData typeConceptLoop = tf.getConcept(extension.getTypeId());
	//					I_ThinExtByRefPart lastPart = getLastExtensionPart(extensionMember);
	//					try {
	//						ThinExtByRefPartConceptConceptString lastConceptPart= (ThinExtByRefPartConceptConceptString) lastPart;
	//						I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//						I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//						System.out.println("ThinExtByRefPartConceptConceptString c1id:" + lastComponentLoop);
	//						System.out.println("ThinExtByRefPartConceptConceptString c2id:" + lastComponent2Loop);
	//						System.out.println("ThinExtByRefPartConceptConceptString string:" + lastConceptPart.getStringValue());
	//					} catch (Exception e) {
	//						try {
	//							ThinExtByRefPartConceptConcept lastConceptPart= (ThinExtByRefPartConceptConcept) lastPart;
	//							I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//							I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//							System.out.println("ThinExtByRefPartConceptConcept c1id:" + lastComponentLoop);
	//							System.out.println("ThinExtByRefPartConceptConcept c2id:" + lastComponent2Loop);
	//						} catch (Exception e2) {
	//							ThinExtByRefPartConceptConceptConcept lastConceptPart= (ThinExtByRefPartConceptConceptConcept) lastPart;
	//							I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//							I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//							I_GetConceptData lastComponent3Loop = tf.getConcept(lastConceptPart.getC3id());
	//							System.out.println("ThinExtByRefPartConceptConceptConcept c1id:" + lastComponentLoop);
	//							System.out.println("ThinExtByRefPartConceptConceptConcept c2id:" + lastComponent2Loop);
	//							System.out.println("ThinExtByRefPartConceptConceptConcept c3id:" + lastComponent3Loop);
	//						}
	//					}
	//					
	//					for (I_ThinExtByRefVersioned extensionLoop2 : tf.getAllExtensionsForComponent(extensionMember.getComponentId())) {
	//						I_ThinExtByRefPart lastPart2 = getLastExtensionPart(extensionLoop2);
	//						try {
	//							ThinExtByRefPartConceptConceptString lastConceptPart= (ThinExtByRefPartConceptConceptString) lastPart2;
	//							I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//							I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//							System.out.println("-------ThinExtByRefPartConceptConceptString c1id:" + lastComponentLoop);
	//							System.out.println("-------ThinExtByRefPartConceptConceptString c2id:" + lastComponent2Loop);
	//							System.out.println("-------ThinExtByRefPartConceptConceptString string:" + lastConceptPart.getStringValue());
	//						} catch (Exception e) {
	//							try {
	//								ThinExtByRefPartConceptConcept lastConceptPart= (ThinExtByRefPartConceptConcept) lastPart2;
	//								I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//								I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//								System.out.println("-------ThinExtByRefPartConceptConcept c1id:" + lastComponentLoop);
	//								System.out.println("-------ThinExtByRefPartConceptConcept c2id:" + lastComponent2Loop);
	//							} catch (Exception e2) {
	//								ThinExtByRefPartConceptConceptConcept lastConceptPart= (ThinExtByRefPartConceptConceptConcept) lastPart2;
	//								I_GetConceptData lastComponentLoop = tf.getConcept(lastConceptPart.getC1id());
	//								I_GetConceptData lastComponent2Loop = tf.getConcept(lastConceptPart.getC2id());
	//								I_GetConceptData lastComponent3Loop = tf.getConcept(lastConceptPart.getC3id());
	//								System.out.println("-------ThinExtByRefPartConceptConceptConcept c1id:" + lastComponentLoop);
	//								System.out.println("-------ThinExtByRefPartConceptConceptConcept c2id:" + lastComponent2Loop);
	//								System.out.println("-------ThinExtByRefPartConceptConceptConcept c3id:" + lastComponent3Loop);
	//							}
	//						}
	//					}
	//				}
	//
	//
	//			}
	//
	//		} catch (TerminologyException e) {
	//			e.printStackTrace();
	//		} catch (IOException e) {
	//			e.printStackTrace();
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}

	//	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}), 
					Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(
					tf.getPath(new UUID[] {UUID.fromString("d638b00c-eda1-4f99-9440-5c22bf93f601")}), 
					Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("d638b00c-eda1-4f99-9440-5c22bf93f601")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.localize().getNid());
			config.getAllowedStatus().add(tf.getConcept(UUID.fromString("854552b2-74b7-3f68-81fc-3211950d2ba9")).getConceptNid());
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

	private static I_ExtendByRefPart getLastExtensionPart(I_ExtendByRef extension) throws TerminologyException, IOException {
		long lastVersion = Long.MIN_VALUE;
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		I_IntSet allowedStatus = config.getAllowedStatus();
		allowedStatus.add(ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid());
		allowedStatus.add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
		I_ExtendByRefPart lastPart = null;
		for (I_ExtendByRefVersion loopTuple : extension.getTuples(allowedStatus,  config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())) {
			if (loopTuple.getTime() > lastVersion) {
				lastVersion = loopTuple.getTime();
				lastPart = loopTuple.getMutablePart();
			}
		}
		return lastPart;
	}
	
	public I_GetConceptData getSpecRefsetConcept(I_GetConceptData refset, I_ConfigAceFrame config) {
		try {
			I_GetConceptData specifiesRefsetRel =
				tf.getConcept(RefsetAuxiliary.Concept.SPECIFIES_REFSET.getUids());
			return getLatestDestinationRelationshipSource(refset, specifiesRefsetRel, config);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public I_GetConceptData getLatestDestinationRelationshipSource(I_GetConceptData concept,
			I_GetConceptData relationshipType, I_ConfigAceFrame config) throws Exception {
			
				I_GetConceptData latestSource = null;
				long latestVersion = Long.MIN_VALUE;
			
				I_IntSet allowedTypes = Terms.get().newIntSet();
				allowedTypes.add(relationshipType.getConceptNid());
			
				List<? extends I_RelTuple> relationships = concept.getDestRelTuples(
						config.getAllowedStatus(), allowedTypes,  config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
				for (I_RelTuple rel : relationships) {
					if (rel.getTime() > latestVersion) {
						latestVersion = rel.getTime();
						latestSource = Terms.get().getConcept(rel.getC1Id());
					}
				}
			
				return latestSource;
			}

}


