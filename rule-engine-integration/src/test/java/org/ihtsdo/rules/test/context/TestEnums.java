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
package org.ihtsdo.rules.test.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.query.helper.RefsetHelper;

/**
 * The Class TestEnums.
 */
public class TestEnums extends TestCase {

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
		AceLog.getAppLog().info("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		AceLog.getAppLog().info("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/rules/test/context/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		AceLog.getAppLog().info("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	/**
	 * Test contexts.
	 *
	 * @throws Exception the exception
	 */
	public void testContexts() throws Exception {
		RulesDeploymentPackageReferenceHelper rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);
		RulesContextHelper contextHelper = new RulesContextHelper(config);
		
		I_GetConceptData rootConcept = tf.getConcept(RefsetAuxiliary.Concept.ADDED_CONCEPT.getUids());
		I_GetConceptData procedure = tf.getConcept(RefsetAuxiliary.Concept.ADDED_DESCRIPTION.getUids());
		I_GetConceptData colonoscopicPolypectomy = tf.getConcept(RefsetAuxiliary.Concept.ADDED_RELATIONSHIP.getUids());
		I_GetConceptData pneumonitis = tf.getConcept(RefsetAuxiliary.Concept.ALTERNATE_MAP_PURPOSE.getUids());
		
		I_IntSet allowedTypes = Terms.get().newIntSet();
		allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

		I_GetConceptData refset = tf.getConcept(RefsetAuxiliary.Concept.DESCRIPTION_TYPE_PURPOSE.getUids());
		
		I_GetConceptData guvnorType = tf.getConcept(
				ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.getUids());
		tf.newDescription(UUID.randomUUID(), refset, "en", "DrConcept.primordialUuid",
				guvnorType, config);
		RefsetHelper helper = new RefsetHelper(config.getViewCoordinate(), config.getEditCoordinate());
                helper.newConceptRefsetExtension(refset.getConceptNid(), 
				pneumonitis.getConceptNid(), pneumonitis.getConceptNid());
                helper.newConceptRefsetExtension(refset.getConceptNid(), 
				procedure.getConceptNid(), procedure.getConceptNid());
                helper.newConceptRefsetExtension(refset.getConceptNid(), 
				colonoscopicPolypectomy.getConceptNid(), colonoscopicPolypectomy.getConceptNid());
		tf.addUncommittedNoChecks(refset);
		tf.addUncommittedNoChecks(colonoscopicPolypectomy);
		tf.addUncommittedNoChecks(pneumonitis);
		tf.addUncommittedNoChecks(procedure);
		tf.commit();


		I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
		I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
		I_GetConceptData testConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_SPEC.getUids());

		assertEquals(0, rulesPackageHelper.getAllRulesDeploymentPackages().size());
		List<I_GetConceptData> contexts = contextHelper.getAllContexts();
		assertEquals(3, contexts.size());

		rulesPackageHelper.createNewRulesDeploymentPackage("Package reference one", 
		"http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/qa4/qa4Demo");

		List<RulesDeploymentPackageReference> packages = rulesPackageHelper.getAllRulesDeploymentPackages();
		assertEquals(1, packages.size());

		RulesDeploymentPackageReference package1 = packages.iterator().next();
		assertTrue(package1.validate());
		
		RulesLibrary.updateGuvnorEnumerations(refset, package1, config);
		
		AceLog.getAppLog().info("End!");

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
					Long.MAX_VALUE));
			config.addEditingPath( tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}));
			config.addPromotionPath( tf.getPath(new UUID[] {UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66")}));

			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			config.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

//			BdbTermFactory tfb = (BdbTermFactory) tf;
//			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
//			newDbProfile.setUsername("username");
//			newDbProfile.setUserConcept(tf.getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
//			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
//			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
//			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
//			config.setDbConfig(newDbProfile);

			config.setPrecedence(Precedence.TIME);

		} catch (TerminologyException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

		return config;
	}

	// If targetLocation does not exist, it will be created.
	/**
	 * Copy directory.
	 *
	 * @param sourceLocation the source location
	 * @param targetLocation the target location
	 * @throws IOException signals that an I/O exception has occurred.
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

}
