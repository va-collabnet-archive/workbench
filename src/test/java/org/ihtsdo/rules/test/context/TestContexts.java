package org.ihtsdo.rules.test.context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.drools.definition.rule.Rule;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.context.RulesContextHelper;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;
import org.ihtsdo.rules.testmodel.ResultsCollectorWorkBench;
import org.ihtsdo.tk.api.Precedence;

public class TestContexts extends TestCase {

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
		copyDirectory(new File("src/test/java/org/ihtsdo/rules/test/context/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	public void testContexts() throws Exception {
		RulesDeploymentPackageReferenceHelper rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);
		RulesContextHelper contextHelper = new RulesContextHelper(config);

		I_GetConceptData includeClause = tf.getConcept(RefsetAuxiliary.Concept.INCLUDE_INDIVIDUAL.getUids());
		I_GetConceptData excludeClause = tf.getConcept(RefsetAuxiliary.Concept.EXCLUDE_INDIVIDUAL.getUids());
		I_GetConceptData testConcept = tf.getConcept(RefsetAuxiliary.Concept.REFSET_SPEC.getUids());

		assertEquals(0, rulesPackageHelper.getAllRulesDeploymentPackages().size());
		List<I_GetConceptData> contexts = contextHelper.getAllContexts();
		assertEquals(3, contexts.size());
		Iterator<I_GetConceptData> contextsIterator = contexts.iterator();
		I_GetConceptData context1 = contextsIterator.next();
		I_GetConceptData context2 = contextsIterator.next();
		I_GetConceptData context3 = contextsIterator.next();

		rulesPackageHelper.createNewRulesDeploymentPackage("Package reference one", 
		"http://127.0.0.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/org.ihtsdo/TESTING");

		List<RulesDeploymentPackageReference> packages = rulesPackageHelper.getAllRulesDeploymentPackages();
		assertEquals(1, packages.size());

		RulesDeploymentPackageReference package1 = packages.iterator().next();
		assertTrue(package1.validate());
		Collection<Rule> rules = package1.getRules();
		assertEquals(4, rules.size());

		for (Rule loopRule : rules) {
			String ruleUid = loopRule.getMetaAttribute("UID");
			assertNull(contextHelper.getRoleInContext(ruleUid, context1));
		}
		
		System.out.println("Checking contexts...");
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2, includeClause);
		
		assertEquals(includeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2, null);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2, includeClause);
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2, excludeClause);
		
		assertEquals(includeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(excludeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2, null);
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2, includeClause);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3, includeClause);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3, null);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(null, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3, includeClause);
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3, excludeClause);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(excludeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3, null);
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3, includeClause);
		
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(null, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));
		
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2, excludeClause);
		contextHelper.setRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3, excludeClause);
		
		assertEquals(excludeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context2));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context2));
		assertEquals(excludeClause, contextHelper.getRoleInContext("f7bd3b50-9c1e-11df-981c-0800200c9a66", context3));
		assertEquals(includeClause, contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context3));

		for (Rule loopRule : rules) {
			String ruleUid = loopRule.getMetaAttribute("UID");
			if (ruleUid.equals("f7bd3b50-9c1e-11df-981c-0800200c9a66")) {
				assertTrue(true);
				contextHelper.setRoleInContext(ruleUid, context1, includeClause);
			}
			if (ruleUid.equals("7feea960-9c23-11df-981c-0800200c9a66")) {
				assertTrue(true);
				contextHelper.setRoleInContext(ruleUid, context1, excludeClause);
			}
		}
		
		for (Rule loopRule : rules) {
			String ruleUid = loopRule.getMetaAttribute("UID");
			if (ruleUid.equals("f7bd3b50-9c1e-11df-981c-0800200c9a66")) {
				assertEquals(includeClause.getConceptNid(), contextHelper.getRoleInContext(ruleUid, context1).getConceptNid());
			} else {
				if (ruleUid.equals("7feea960-9c23-11df-981c-0800200c9a66")) {
					assertEquals(excludeClause.getConceptNid(), contextHelper.getRoleInContext(ruleUid, context1).getConceptNid());
				} else {
					assertNull(contextHelper.getRoleInContext(ruleUid, context1));
				}
			}
		}
		
		ResultsCollectorWorkBench results = RulesLibrary.checkConcept(testConcept, context1, false, config);
		assertEquals(1,results.getResultsItems().size());
		
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context1, includeClause);
		assertEquals(includeClause.getConceptNid(), 
				contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context1).getConceptNid());

		results = RulesLibrary.checkConcept(testConcept, context1, false, config);
		assertEquals(2,results.getResultsItems().size());
		
		contextHelper.setRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context1, excludeClause);
		assertEquals(excludeClause.getConceptNid(), 
				contextHelper.getRoleInContext("7feea960-9c23-11df-981c-0800200c9a66", context1).getConceptNid());
		
		results = RulesLibrary.checkConcept(testConcept, context1, false, config);
		assertEquals(1,results.getResultsItems().size());
		
		System.out.println("End!");

	}

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

}
