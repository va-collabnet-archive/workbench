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
package org.ihtsdo.issue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.api.cs.ChangeSetWriterThreading;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;

/**
 * The Class .
 */
public class TestIssueAttachments extends TestCase {

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
	I_ImplementTermFactory tf;

	/** The new project concept. */
	I_GetConceptData newProjectConcept;

	/** The allowed statuses with retired. */
	I_IntSet allowedStatusesWithRetired;

	I_GetConceptData issueRepoConcept;
	IssueRepository newRepository;
	String issueTitle;
	public static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	Issue newIssue;

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Deleting test fixture");
		deleteDirectory(new File("berkeley-db"));
		System.out.println("Creating test fixture");
		copyDirectory(new File("src/test/java/org/ihtsdo/issue/berkeley-db"), new File("berkeley-db"));
		vodbDirectory = new File("berkeley-db");
		dbSetupConfig = new DatabaseSetupConfig();
		System.out.println("Opening database");
		Terms.createFactory(vodbDirectory, readOnly, cacheSize, dbSetupConfig);
		tf = (I_ImplementTermFactory) Terms.get();
		config = getTestConfig();
		tf.setActiveAceFrameConfig(config);
	}

	public void testDependencies() throws Exception{
		Integer repositoryTypeInt = IssueRepository.REPOSITORY_TYPE.WEB_SITE.ordinal();
		newRepository = new IssueRepository("tracker1264", "https://csfe.aceworkspace.net", 
				"Test repo", repositoryTypeInt);
		issueRepoConcept = IssueRepositoryDAO.addIssueRepoToMetahier(newRepository, 
				config);

		newRepository = IssueRepositoryDAO.getIssueRepository(issueRepoConcept);
		
		assertEquals("tracker1264", newRepository.getRepositoryId());
		assertEquals("https://csfe.aceworkspace.net", newRepository.getUrl());

		IssueRepoRegistration issueRepoReg = new IssueRepoRegistration(issueRepoConcept.getUids().iterator().next(),
				"alopez", "snomed", null);
		IssueRepositoryDAO.addRepositoryToProfile(issueRepoReg);
		
		config = Terms.get().getActiveAceFrameConfig();
		
		assertEquals ("alopez", IssueRepositoryDAO.getRepositoryRegistration(newRepository.getUuid(), config).getUserId());
		assertEquals ("snomed", IssueRepositoryDAO.getRepositoryRegistration(newRepository.getUuid(), config).getPassword());
		//Create issue
		issueTitle = "Issue with attachment for " + now();
		newIssue= new Issue();

		newIssue.setCategory("");
		newIssue.setComponent("");
		newIssue.setComponentId("2faa9260-8fb2-11db-b606-0800200c9a66");
		newIssue.setExternalUser("External user");
		newIssue.setDescription("Test component");
		newIssue.setPriority("0");
		newIssue.setProjectId("");
		newIssue.setWorkflowStatus("");
		newIssue.setTitle(issueTitle);
		newIssue.setUser("alopez");
		newIssue.setFieldMap(new HashMap<String,Object>());
		newIssue.setDownloadStatus("Open");

		IssueDAO jwto=new IssueDAO();

		newIssue=jwto.createIssue(newRepository, newIssue);
		
		assertEquals(issueTitle, newIssue.getTitle());

		
		File file=new File("src/test/java/org/ihtsdo/issue/test.txt");
		
		jwto.addAttachment(newRepository, newIssue, file, "text");		
		
		
		List<IssueAttachmentRef> issueAttList = jwto.getAttachmentList(newRepository, newIssue);
		
		assertEquals(issueAttList.size(),1);
		
		assertEquals(issueAttList.get(0).getFileName(),"test.txt");
		
		
		
		File folder=new File("src/test/java");
		
		jwto.getAttachmentFile(newRepository, issueAttList.get(0), folder );
		
		File testFile=new File(folder.getAbsolutePath() + File.separator + issueAttList.get(0).getFileName());
		
		assertTrue(testFile.exists());
		
		
		
		jwto.delAttachment(newRepository, issueAttList.get(0));

		issueAttList = jwto.getAttachmentList(newRepository, newIssue);
		
		assertEquals(issueAttList.size(),0);
		
		
		
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
			
			I_ConfigAceDb newDbProfile = tf.newAceDbConfig();
	        newDbProfile.setUsername("username");
	        newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
	        newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
	        newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
	        newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
	        config.setDbConfig(newDbProfile);
	        
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

	public static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		return sdf.format(cal.getTime());

	}
}


