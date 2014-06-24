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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class TestContextualizedDescriptions.
 */
public class GetIdsMapping extends TestCase {

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

	/** The project. */
	TranslationProject project = null;

	/** The writer. */
	PrintWriter writer;
	
	/** The snomed int id. */
	int snomedIntId;
	
	/** The inferred int id. */
	int inferredIntId;
	
	/** The stated int id. */
	int statedIntId;
	
	/** The counter. */
	int counter;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		//		System.out.println("Deleting test fixture");
		//		deleteDirectory(new File("berkeley-db"));
		//		System.out.println("Creating test fixture");
		//		copyDirectory(new File("src/test/java/org/ihtsdo/project/berkeley-db"), new File("berkeley-db"));
		//		vodbDirectory = new File("berkeley-db");
		vodbDirectory = new File("/Users/alo/Desktop/berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
		snomedIntId = tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids());
		inferredIntId = tf.uuidToNative(SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getUuids());
		statedIntId = tf.uuidToNative(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getUuids());
		counter = 0;

	}

	/**
	 * Test iterate.
	 */
	public void testIterate() {
		try {
			OutputStream outputStream = new FileOutputStream("ids-mapping.txt");
			writer       = new PrintWriter(outputStream);
			Ts.get().iterateConceptDataInParallel(new IdExtractorProcessor());
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The Class IdExtractorProcessor.
	 */
	private class IdExtractorProcessor implements ProcessUnfetchedConceptDataBI {

		/* (non-Javadoc)
		 * @see org.ihtsdo.tk.api.ContinuationTrackerBI#continueWork()
		 */
		@Override
		public boolean continueWork() {
			return true;
		}

		/* (non-Javadoc)
		 * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#getNidSet()
		 */
		@Override
		public NidBitSetBI getNidSet() throws IOException {
			return Ts.get().getAllConceptNids();
		}

		/* (non-Javadoc)
		 * @see org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI#processUnfetchedConceptData(int, org.ihtsdo.tk.api.ConceptFetcherBI)
		 */
		@Override
		public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher)
		throws Exception {
			ConceptChronicleBI concept = fetcher.fetch();
			writeId(concept);

			for ( DescriptionChronicleBI loopDescription : concept.getDescriptions()) {
				writeId(loopDescription);
			}
			for ( RelationshipChronicleBI loopRel : concept.getRelationshipsOutgoing()) {
				writeId(loopRel);
			}
		}
	}

	/**
	 * Write id.
	 *
	 * @param component the component
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void writeId (ComponentBI component) throws IOException {
		Long sctid = null;
		if (component.getAllIds() != null) {
			for (IdBI loopId : component.getAllIds()) {
				if (loopId.getAuthorityNid() == snomedIntId) {
					sctid = (Long)loopId.getDenotation();
				}
			}
			if (sctid != null) {
				String type = "X";
				if (component instanceof ConceptChronicleBI) {
					type = "C";
				} else if (component instanceof DescriptionChronicleBI) {
					type = "D";
				} else if (component instanceof RelationshipChronicleBI) {
					RelationshipChronicleBI rel = (RelationshipChronicleBI) component;
					if (rel.getPrimordialVersion().getCharacteristicNid() == inferredIntId) {
						type = "RI";
					} else if (rel.getPrimordialVersion().getCharacteristicNid() == statedIntId) {
						type = "RS";
					}
				}
				counter++;
				if (!type.equals("RI") && !type.equals("X")) {
					writer.println(component.getPrimUuid() + "\t" + sctid + "\t" + type);
				}
				//System.out.println(component.getPrimUuid() + "\t" + sctid);
				if (counter % 1000 == 0) {
					System.out.println(counter);
				}
			}
		}
	}

	/**
	 * Gets the test config.
	 *
	 * @return the test config
	 */
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
			//			config.addViewPosition(tf.newPosition(
			//					tf.getPath(new UUID[] {UUID.fromString("5e51196f-903e-5dd4-8b3e-658f7e0a4fe6")}), 
			//					Integer.MAX_VALUE));
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
	/**
	 * Copy directory.
	 *
	 * @param sourceLocation the source location
	 * @param targetLocation the target location
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Delete directory.
	 *
	 * @param path the path
	 * @return true, if successful
	 */
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

	/**
	 * Sleep.
	 *
	 * @param n the n
	 */
	public static void sleep(int n){
		long t0, t1;
		t0 =  System.currentTimeMillis();
		do{
			t1 = System.currentTimeMillis();
		}
		while ((t1 - t0) < (n * 1000));
	}

	/**
	 * Gets the business process.
	 *
	 * @param f the f
	 * @return the business process
	 */
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


