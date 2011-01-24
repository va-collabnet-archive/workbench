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
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.LanguageSpecRefset;
import org.ihtsdo.project.refset.Refset;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.Precedence;

/**
 * The Class TestContextualizedDescriptions.
 */
public class TestCommentsRefset extends TestCase {

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

	/*
	 * (non-Javadoc)
	 * 
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

	/**
	 * Test state full.
	 */
	public void testStateFull() {
		try {
			Date timeStamp = new Date();
			I_GetConceptData concept = tf.getConcept(UUID.fromString("c265cf22-2a11-3488-b71e-296ec0317f96"));
			I_GetConceptData parentConcept = tf.getConcept(UUID.fromString("900042ba-b637-38ae-95d7-1974896da7fb"));

			Refset originLangMemberRefset = LanguageMembershipRefset.createNewLanguageMembershipRefset("Origin Language Members" + timeStamp, parentConcept.getConceptNid(), "en", config);
			tf.commit();
			Refset langMemberRefset = LanguageMembershipRefset.createNewLanguageMembershipRefset("Language Members for spec" + timeStamp, parentConcept.getConceptNid(), "en", config);
			tf.commit();
			LanguageSpecRefset langSpecRefset = LanguageSpecRefset.createNewLanguageSpecRefset("Language Spec" + timeStamp, parentConcept.getConceptNid(), langMemberRefset.getRefsetId(),
					originLangMemberRefset.getRefsetId(), config);
			tf.commit();

			langSpecRefset.getCommentsRefset(config).addComment(concept.getConceptNid(), "Test comment number one");
			//tf.commit();
			langSpecRefset.getCommentsRefset(config).addComment(concept.getConceptNid(), "Test comment number two");
			tf.commit();

			System.out.println(langSpecRefset.getCommentsRefset(config).getRefsetConcept().toLongString());

			List<String> comments = langSpecRefset.getCommentsRefset(config).getComments(concept.getConceptNid());
			assertTrue(comments.get(0).startsWith("Test comment number one"));
			assertTrue(comments.get(1).startsWith("Test comment number two"));

//			boolean isOnEditPath = false;
//			boolean isOnPromotePath = false;
//
//			for (I_ExtendByRef loopExtension : tf.getAllExtensionsForComponent(concept.getConceptNid())) {
//				if (loopExtension.getRefsetId() == langSpecRefset.getCommentsRefset(config).getRefsetId()) {
//					for (I_ExtendByRefPart loopPart : loopExtension.getMutableParts()) {
//						for (PathBI loopPath : config.getEditingPathSet()) {
//							if (loopPart.getPathNid() == loopPath.getConceptNid()) {
//								isOnEditPath = true;
//							}
//						}
//						for (PathBI loopPath : config.getPromotionPathSet()) {
//							if (loopPart.getPathNid() == loopPath.getConceptNid()) {
//								isOnPromotePath = true;
//							}
//						}
//					}
//				}
//			}
//
//			//			System.out.println("Before promote:");
//			//			System.out.println(concept.toLongString());
//
//			assertTrue(isOnEditPath);
//			assertFalse(isOnPromotePath);
//
//			langSpecRefset.getCommentsRefset(config).getRefsetConcept().promote(
//					tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("e0c9b55a-f349-420e-a85e-3957c4c81b82") }), Integer.MAX_VALUE), 
//					config.getPromotionPathSetReadOnly(),
//					config.getAllowedStatus(), config.getPrecedence());
//			concept.promote(
//					tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("e0c9b55a-f349-420e-a85e-3957c4c81b82") }), Integer.MAX_VALUE), 
//					config.getPromotionPathSetReadOnly(),
//					config.getAllowedStatus(), config.getPrecedence());
//			for (I_ExtendByRef loopExtension : tf.getAllExtensionsForComponent(concept.getConceptNid())) {
//				loopExtension.promote(
//					tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("e0c9b55a-f349-420e-a85e-3957c4c81b82") }), Integer.MAX_VALUE), 
//					config.getPromotionPathSetReadOnly(),
//					config.getAllowedStatus(), config.getPrecedence());
//				tf.addUncommittedNoChecks(loopExtension);
//			}
//
//			tf.commit();
//			
//			isOnEditPath = false;
//			isOnPromotePath = false;
//
//			for (I_ExtendByRef loopExtension : tf.getAllExtensionsForComponent(concept.getConceptNid())) {
//				if (loopExtension.getRefsetId() == langSpecRefset.getCommentsRefset(config).getRefsetId()) {
//					for (I_ExtendByRefPart loopPart : loopExtension.getMutableParts()) {
//						for (PathBI loopPath : config.getEditingPathSet()) {
//							if (loopPart.getPathNid() == loopPath.getConceptNid()) {
//								isOnEditPath = true;
//							}
//						}
//						for (PathBI loopPath : config.getPromotionPathSet()) {
//							if (loopPart.getPathNid() == loopPath.getConceptNid()) {
//								isOnPromotePath = true;
//							}
//						}
//					}
//				}
//			}
//
//			//			System.out.println("Before promote:");
//			//			System.out.println(concept.toLongString());
//
//			assertTrue(isOnEditPath);
//			assertTrue(isOnPromotePath);

			System.out.println("\n\n");

			System.out.println(langSpecRefset.getCommentsRefset(config).getRefsetConcept().toLongString());
			//System.out.println(langSpecRefset.getCommentsRefset(config).getRefsetConcept().toLongString());

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
			config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66") }), Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2") }), Integer.MAX_VALUE));
			config.addViewPosition(tf.newPosition(tf.getPath(new UUID[] { UUID.fromString("e0c9b55a-f349-420e-a85e-3957c4c81b82") }), Integer.MAX_VALUE));
			config.addEditingPath(tf.getPath(new UUID[] { UUID.fromString("e0c9b55a-f349-420e-a85e-3957c4c81b82") }));
			config.addPromotionPath(tf.getPath(new UUID[] { UUID.fromString("3416c77c-1a90-43f3-86ec-3a7bc2c07b63") }));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	public void copyDirectory(File sourceLocation, File targetLocation) throws IOException {

		if (sourceLocation.isDirectory()) {
			if (!targetLocation.exists()) {
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();
			for (int i = 0; i < children.length; i++) {
				copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
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
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}
}
