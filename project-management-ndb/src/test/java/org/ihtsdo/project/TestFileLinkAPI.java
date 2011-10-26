package org.ihtsdo.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.tk.api.Precedence;

public class TestFileLinkAPI extends TestCase {
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

	protected void setUp() throws Exception {
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
	
	

	public void testAPI() throws Exception {
		FileLinkAPI flApi = new FileLinkAPI(config);

		
		
		I_GetConceptData category1 = tf.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids());
		I_GetConceptData category2 = tf.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.getUids());
		
		try{
			//I_GetConceptData categoriesRoot = tf.getConcept(ArchitectonicAuxiliary.Concept.FILE_LINK_CATEGORY.getUids());
			I_GetConceptData categoriesRoot = tf.getConcept(ArchitectonicAuxiliary.Concept.STATUS.getUids());
			Iterator<I_GetConceptData> s = flApi.getCategories(categoriesRoot).iterator();
			while (s.hasNext()) {
				I_GetConceptData iGetConceptData = (I_GetConceptData) s.next();
				System.out.println(iGetConceptData.getInitialText());
				for (FileLink fileLink : flApi.getLinksForCategory(iGetConceptData)) {
					System.out.println("\t" + fileLink.getName());
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		assertEquals(0, flApi.getLinksForCategory(category1).size());
		assertEquals(0, flApi.getLinksForCategory(category2).size());

		FileLink link1 = new FileLink(new File("src/test/java/org/ihtsdo/project/sample.bp"), category1);

		//Add link
		flApi.putLinkInConfig(link1);

		assertEquals(1, flApi.getLinksForCategory(category1).size());
		assertEquals(0, flApi.getLinksForCategory(category2).size());

		FileLink link1returned = flApi.getLinksForCategory(category1).iterator().next();
		

		//Test retrieve link
		assertEquals(link1,link1returned);

		//Remove link
		flApi.removeLinkFromConfig(link1returned);

		assertEquals(0, flApi.getLinksForCategory(category1).size());
		assertEquals(0, flApi.getLinksForCategory(category2).size());

		flApi.putLinkInConfig(link1);

		assertEquals(1, flApi.getLinksForCategory(category1).size());
		assertEquals(0, flApi.getLinksForCategory(category2).size());

		link1returned = flApi.getLinksForCategory(category1).iterator().next();

		assertEquals(link1,link1returned);
		
		//Add folder
		flApi.addFolderAsFileLinksToConfig(new File("src/test/java/org/ihtsdo/project/sampleProcesses"), category2);
		
		assertEquals(1, flApi.getLinksForCategory(category1).size());
		assertEquals(6, flApi.getLinksForCategory(category2).size());
		
		File sample2 = new File("sample2.bp");
		sample2.delete();
		
		copyDirectory(new File("src/test/java/org/ihtsdo/project/sample.bp"), 
				new File("sample2.bp"));
		
		FileLink link2 = new FileLink(new File("sample2.bp"), category1);
		
		flApi.putLinkInConfig(link2);

		assertEquals(2, flApi.getLinksForCategory(category1).size());
		assertEquals(6, flApi.getLinksForCategory(category2).size());
		
		sample2.delete();

		FileLink link2returned = new FileLink();
		
		for (FileLink loopLink : flApi.getLinksForCategory(category1)) {
			if (loopLink.getUuid().equals(link2.getUuid())) {
				link2returned = loopLink;
			}
		}

		assertEquals(link2returned.isFoundOnDisk(),false);
		
		flApi.cleanup();
		
		assertEquals(1, flApi.getLinksForCategory(category1).size());
		assertEquals(6, flApi.getLinksForCategory(category2).size());

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
			config.addEditingPath(tf.getPath(new UUID[] {UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2")}));
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			config.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			config.setDefaultStatus(tf.getConcept((ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid())));
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			config.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());

			BdbTermFactory bdbFactory = (BdbTermFactory) tf;
			I_ConfigAceDb newDbProfile = bdbFactory.newAceDbConfig();
			newDbProfile.setUsername("username");
			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			config.setDbConfig(newDbProfile);

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
