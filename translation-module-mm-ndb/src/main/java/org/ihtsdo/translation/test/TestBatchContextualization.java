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
package org.ihtsdo.translation.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.translation.tasks.PutsDescriptionsInLanguageRefset;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestBatchContextualization extends TestCase {

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
		//vodbDirectory = new File("berkeley-db");
		vodbDirectory = new File("/Users/alo/Documents/TermMed/Eclipse/WorkbenchMarch2010Version/ihtsdo-sab-ndb/target/wb-bundle/target/berkeley-db");
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

			I_GetConceptData preferred = tf.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
			I_GetConceptData synonym = tf.getConcept(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids());
			I_GetConceptData acceptable = tf.getConcept(ArchitectonicAuxiliary.Concept.ACCEPTABLE.getUids());
			I_GetConceptData fsn = tf.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());

			config.getViewPositionSet().clear();
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), Integer.MAX_VALUE));
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_ES_PATH.getUids()), Integer.MAX_VALUE));
			config.getViewPositionSet().add(tf.newPosition(
					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_LANGUAGE_SE_PATH.getUids()), Integer.MAX_VALUE));

			config.getEditingPathSet().clear();
			config.getEditingPathSet().add(tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));

			config.getDescTypes().clear();
			config.getDescTypes().add(preferred.getConceptNid());
			config.getDescTypes().add(synonym.getConceptNid());
			config.getDescTypes().add(fsn.getConceptNid());

			config.getAllowedStatus().clear();
			config.getAllowedStatus().add(tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptNid());

			tf.setActiveAceFrameConfig(config);

			I_GetConceptData englishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_EN.getUids());
			I_GetConceptData englishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.EN.getUids());
			String englishLCode=ArchitectonicAuxiliary.getLanguageCode(englishLanguageCodeConcept.getUids());

			LanguageMembershipRefset englishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						englishLanguageRefsetConcept, englishLCode, config);

			I_GetConceptData spanishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_ES.getUids());
			I_GetConceptData spanishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.ES.getUids());
			String spanishLCode=ArchitectonicAuxiliary.getLanguageCode(spanishLanguageCodeConcept.getUids());

			LanguageMembershipRefset spanishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						spanishLanguageRefsetConcept, spanishLCode, config);

			I_GetConceptData swedishLanguageRefsetConcept = tf.getConcept(
					RefsetAuxiliary.Concept.LANGUAGE_REFSET_SV_SE.getUids());
			I_GetConceptData swedishLanguageCodeConcept = tf.getConcept(
					ArchitectonicAuxiliary.Concept.SV_SE.getUids());
			String swedishLCode=ArchitectonicAuxiliary.getLanguageCode(swedishLanguageCodeConcept.getUids());

			LanguageMembershipRefset swedishLangMemberRefset = 
				LanguageMembershipRefset.createLanguageMembershipRefsetFromConcept(
						swedishLanguageRefsetConcept, swedishLCode, config);

			I_GetConceptData snomedRoot = tf.getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			I_IntSet isaType = tf.newIntSet();
			isaType.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

			tf.iterateConcepts(new PutsDescriptionsInLanguageRefset( snomedRoot,  config, 
					isaType,  englishLanguageRefsetConcept,
					spanishLanguageRefsetConcept, swedishLanguageRefsetConcept,
					preferred,  acceptable,  fsn,
					synonym));
			
			tf.commit();

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private I_ConfigAceFrame getTestConfig() {
		I_ConfigAceFrame config = null;
		try {
			config = tf.newAceFrameConfig();
//			config.addViewPosition(tf.newPosition(
//					tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()), 
//					Integer.MAX_VALUE));
//			config.addEditingPath(tf.getPath(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()));
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
}
