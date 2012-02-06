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
package org.ihtsdo.translation;

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
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.LanguageSpecRefset;
import org.ihtsdo.project.refset.Refset;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestLanguageRefsets extends TestCase {

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
		copyDirectory(new File("src/test/java/org/ihtsdo/translation/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			Date timeStamp = new Date();
			I_GetConceptData concept = tf.getConcept(UUID.fromString("c265cf22-2a11-3488-b71e-296ec0317f96"));
			I_GetConceptData parentConcept = tf.getConcept(UUID.fromString("900042ba-b637-38ae-95d7-1974896da7fb"));
			I_GetConceptData preferred = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_GetConceptData synonym = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			I_GetConceptData acceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
			I_GetConceptData notAcceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.getUids());
			I_GetConceptData fsn = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());


			System.out.println(concept + " - " + parentConcept);
			Refset originLangMemberRefset = LanguageMembershipRefset.createNewLanguageMembershipRefset(
					"Origin Language Members" + timeStamp, parentConcept.getConceptNid(), "en", config);

			Refset langMemberRefset = LanguageMembershipRefset.createNewLanguageMembershipRefset(
					"Language Members for spec" + timeStamp, parentConcept.getConceptNid(), "en", config);

			LanguageSpecRefset langSpecRefset = LanguageSpecRefset.createNewLanguageSpecRefset(
					"Language Spec" + timeStamp, parentConcept.getConceptNid(), 
					langMemberRefset.getRefsetId(), originLangMemberRefset.getRefsetId(), config);

			List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(
					concept.getConceptNid(), originLangMemberRefset.getRefsetId(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getTypeId() == fsn.getConceptNid()) {
					description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
							preferred.getConceptNid());
				} else if (description.getTypeId() == synonym.getConceptNid()) {
					description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
							acceptable.getConceptNid());
				} else {
					description.contextualizeThisDescription(originLangMemberRefset.getRefsetId(), 
							preferred.getConceptNid());
				}
			}

			for (I_ContextualizeDescription description : descriptions) {
				if (description.getText().trim().equals("Asthmatic")) {
					description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
							preferred.getConceptNid());
				} else if (description.getText().trim().equals("Asthma")) {
					description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
							acceptable.getConceptNid());
				} else if (description.getText().trim().equals("BHR - Bronchial hyperreactivity")) {
					description.contextualizeThisDescription(langSpecRefset.getRefsetId(), 
							notAcceptable.getConceptNid());
				}
			}

			langSpecRefset.computeLanguageRefsetSpec(config);

			tf.commit();

			descriptions = LanguageUtil.getContextualizedDescriptions(
					concept.getConceptNid(), originLangMemberRefset.getRefsetId(), true);
			for (I_ContextualizeDescription description : descriptions) {
//				if (description.getLanguageExtension() != null) {
//					I_GetConceptData acceptabilityConcept = tf.getConcept(description.getAcceptabilityId());
//					System.out.println("~~~~~~~~~ Language Refset 1 " + description.toString() + " - Acceptability: " + 
//							acceptabilityConcept.toString());
//				} else {
//					System.out.println("~~~~~~~~~ Language Refset 1 " + description.toString() + " - No Acceptability");
//				}
				if (description.getText().trim().equals("BHR - Bronchial hyperreactivity")) {
					assertEquals(description.getAcceptabilityId(), 
							ArchitectonicAuxiliary.Concept.ACCEPTABLE.localize().getNid());
				}
				if (description.getText().trim().equals("Asthmatic")) {
					assertEquals(description.getAcceptabilityId(), 
							ArchitectonicAuxiliary.Concept.ACCEPTABLE.localize().getNid());
				}
			}

			descriptions = LanguageUtil.getContextualizedDescriptions(
					concept.getConceptNid(), langSpecRefset.getRefsetId(), true);
			for (I_ContextualizeDescription description : descriptions) {
//				if (description.getLanguageExtension() != null) {
//					I_GetConceptData acceptabilityConcept = tf.getConcept(description.getAcceptabilityId());
//					System.out.println("~~~~~~~~~ Language Refset spec " + description.toString() + " - Acceptability: " + 
//							acceptabilityConcept.toString());
//				} else {
//					System.out.println("~~~~~~~~~ Language Refset spec " + description.toString() + " - No Acceptability");
//				}
				if (description.getText().trim().equals("BHR - Bronchial hyperreactivity")) {
					assertEquals(description.getAcceptabilityId(), 
							ArchitectonicAuxiliary.Concept.NOT_ACCEPTABLE.localize().getNid());
				}
				if (description.getText().trim().equals("Asthmatic")) {
					assertEquals(description.getAcceptabilityId(), 
							ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
				}
			}

			descriptions = LanguageUtil.getContextualizedDescriptions(
					concept.getConceptNid(), langMemberRefset.getRefsetId(), true);
			for (I_ContextualizeDescription description : descriptions) {
//				if (description.getLanguageExtension() != null) {
//					I_GetConceptData acceptabilityConcept = tf.getConcept(description.getAcceptabilityId());
//					System.out.println("~~~~~~~~~ Language Refset 2 " + description.toString() + " - Acceptability: " + 
//							acceptabilityConcept.toString());
//				} else {
//					System.out.println("~~~~~~~~~ Language Refset 2 " + description.toString() + " - No Acceptability");
//				}
				if (description.getText().trim().equals("BHR - Bronchial hyperreactivity")) {
					assertNull(description.getLanguageExtension());
				}
				if (description.getText().trim().equals("Asthmatic")) {
					assertEquals(description.getAcceptabilityId(), 
							ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
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
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
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
}
