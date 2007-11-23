package org.dwfa.mojo;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationFile;
import net.jini.config.ConfigurationProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ReadUuidListListFromUrl;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.assignment.LaunchBatchGenAssignmentProcess;
import org.dwfa.ace.task.queue.OpenAllInboxes;
import org.dwfa.ace.task.queue.OpenQueuesInFolder;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.tasks.util.Complete;



/**
 * Automated process to perform the creation of the review assignments required.
 * 
 * @goal duplicate-review-assignments
 * 
 */
public class CreateDuplicateReviewAssignments extends AbstractMojo {
	
	/**
	 * Location of the input file to use.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File inputDirectory;
	
	/**
	 * Location of the queue file to use.
	 * 
	 * @parameter expression="${project.build.directory}\\..\\dev\\src\\main\\profiles\\users\\keith.dev\\inbox"
	 * @required
	 */
	private File queueDirectory;
	
	
	/**
	 * Name of file to read uuids from
	 * 
	 * @parameter expression="default.txt"
     * @required
	 * 
	 */
	private String fileName;
	
	/**
	 * Name of workflow manager, as found in ace address book
	 * 
	 * @parameter expression="admin"
     * @required
	 * 
	 */
	private String workFlowManager;
	
	/**
	 * Name of business process to assign
	 * 
	 * @parameter expression="dupReviewAssignment.bp"
     * @required
	 * 
	 */
	private String businessProcessToAssign;
	
	private int listListSize = 250;
	private String processFileStr = "dup_check_processes/assignmentGenFromUuidList_InProperty.bp";
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		System.out.println(">>>>>>>>>>>>>>>>> start execute method <<<<<<<<<<<<<<<<<<<<<<<");
		System.out.println("inputDirectory >>" + inputDirectory.getAbsolutePath());
		try{		
			if(inputDirectory.exists()){
				File inputFile = new File(inputDirectory, fileName);
				System.out.println(">>>>>>>>>>>>>>>>> input dir exists <<<<<<<<<<<<<<<<<<<<<<<");
				System.out.println("inputFile >>" + fileName);
				if(inputFile.exists()){
					System.out.println(">>>>>>>>>>>>>>>>> input file exists <<<<<<<<<<<<<<<<<<<<<<<");
		    		/*
		    		 * execute business process
		    		 */
					System.out.println(">>>>>>>>>>>>>>>>> creating config <<<<<<<<<<<<<<<<<<<<<<<");
		    		I_ConfigAceFrame config = LocalVersionedTerminology.get().getActiveAceFrameConfig();
		    		
		    		System.out.println(">>>>>>>>>>>>>>>>> config created <<<<<<<<<<<<<<<<<<<<<<<");
		    		
		    		
		    		
		    		
		    		String[] entries = new String[]{ queueDirectory.getAbsolutePath() };
		    	    
		    		
//		    		ConfigurationFile cf = new ConfigurationFile(entries);
		    		
		    		 Configuration configuration = 
		                 ConfigurationProvider.getInstance(entries,
		                                                   getClass().getClassLoader());
		    		
		    		 System.out.println(">>>>>>>>>>>>>>>>> configuration created <<<<<<<<<<<<<<<<<<<<<<<");
		    		MojoWorker mw = new MojoWorker(configuration,UUID.randomUUID() ,"whatisthis", "keith.dev" );
//		    		MojoWorker mw = new MojoWorker(config.getWorker().getJiniConfig(),UUID.randomUUID() ,"whatisthis" );
		    		System.out.println(">>>>>>>>>>>>>>>>> worker created <<<<<<<<<<<<<<<<<<<<<<<");
//		    		ReadUuidListListFromUrl readUuid = new ReadUuidListListFromUrl();
//		    		readUuid.setUuidFileNamePropName("C:\\_working\\au-ct\\ace-au-ct\\dev\\test-dup\\target\\classes\\dupPotMatchResults\\container-type-dups\\dwfaDups.txt");
//		    		readUuid.setUuidListListPropName("potDupUuidList");
//		    		mw.addTask(readUuid,1);
		    		
		    		List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
		             BufferedReader br = new BufferedReader( new FileReader( inputFile ) );
		             String uuidLineStr;
		             while ((uuidLineStr = br.readLine()) != null) { // while loop begins here
		            	 List<UUID> uuidList = new ArrayList<UUID>();
		            	 for (String uuidStr: uuidLineStr.split("\t")){
		         			 mw.getLogger().info("uuidStrs: " + uuidStr); 	
		            		 UUID uuid = UUID.fromString(uuidStr);
		            		 uuidList.add(uuid);
		            	 }
		            	 uuidListOfLists.add(uuidList);
		             } // end while 
		    		
		    		
		    		
//		    		List<Collection<UUID>> tempListList = uuidListOfLists;
		             List<List<UUID>> tempListList = uuidListOfLists;
		    		int sizeOfList = tempListList.size();
		    		
		            List<Collection<UUID>> uuidListList = null;
		            if (sizeOfList > listListSize){
		            	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, listListSize));
		            } else {
		            	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, sizeOfList));
		            }
		            System.out.println(">>>>>>>>>>>>>>>>> list edited <<<<<<<<<<<<<<<<<<<<<<<");
		    		mw.setProcessProperty( "potDupUuidList", uuidListList);
		            
		    		if (tempListList.removeAll(uuidListList)){
		            	//do nothing
		            } else {
		            	mw.getLogger().info("error encountered in removing uuid collection from list");
		            }
		            

		    		
		    		/*
		    		 * Set workflow manager
		    		 */
		    		List<String> addresses = new ArrayList<String>();
		    		addresses.add( workFlowManager );
		    		mw.setProcessProperty("workFlowManager", addresses);
		    		
		    		I_TermFactory termFact = LocalVersionedTerminology.get();
		    		I_ConfigAceFrame cf = termFact.getActiveAceFrameConfig();
		    		cf.setUsername("bootstrap");
		    		mw.writeAttachment( WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), cf );
		    		mw.setProcessProperty(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), cf );
		    		
		    		System.out.println(">>>>>>>>>>>>>>>>> wf man added <<<<<<<<<<<<<<<<<<<<<<<");
		    		/*
		    		 * Set business process 
		    		 */		    			    		
		    		mw.setProcessProperty("processFileName", "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment.bp");
		    		System.out.println(">>>>>>>>>>>>>>>>> bp added <<<<<<<<<<<<<<<<<<<<<<<");
		    		
		    		/*
		    		 * Set instruction file details
		    		 */
		    		mw.setProcessProperty("instructionFileName", "instructions_dup.html");
		    		mw.setProcessProperty("directoryName", "C:\\AMT Processes\\instructions");
		    		
		    		System.out.println(">>>>>>>>>>>>>>>>> instruction added <<<<<<<<<<<<<<<<<<<<<<<");
		    		
		    		
//		    		OpenAllInboxes oal = new OpenAllInboxes();
//		    		
//		    		mw.addTask(oal, 0);
		    		
		    		File file = new File("profiles\\users\\peter.dev\\inbox");
		    		if(file.exists()){
		    			System.out.println("[file path] >> "+file.getAbsolutePath());
		    		}
		    		
		    		OpenQueuesInFolder oqf = new OpenQueuesInFolder();
		    		oqf.setQueueDir("profiles\\users\\peter.dev\\inbox");
		    		mw.addTask(oqf, 0);
		    		
		    		LaunchBatchGenAssignmentProcess genAssign = new LaunchBatchGenAssignmentProcess();
		    		genAssign.setAssigneeAddrPropName( "assignee" );
		    		genAssign.setProcessFileStr( "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment.bp" );
		    		genAssign.setProcessToAssignPropName( "processFileName" );
		    		genAssign.setBatchGenAssigneePropName("workFlowManager");
		    		genAssign.setUuidListListPropName( "potDupUuidList" );
		    		genAssign.setId(1);
		    		
		    		
		    		
		    		mw.addTask(genAssign, 1);
		    		
		    		
		    		
		    		Complete comp = new Complete();
		    		comp.setId(2);
		    		mw.addTask(comp, 2);
		    		
		    		mw.addBranch(oqf, genAssign, Condition.CONTINUE);
		    		mw.addBranch(genAssign, comp, Condition.CONTINUE);
		    		
		    		
		    		System.out.println(">>>>>>>>>>>>>>>>>>>>>   about to run worker <<<<<<<<<<<<<<<<<<<<<<<");
		    		mw.run();
		    		System.out.println(">>>>>>>>>>>>>>>>>>>>>   worker done <<<<<<<<<<<<<<<<<<<<<<<");
		    		
		    		
		    		
		    		
				}//End if
			}//End if
			
			
			
		}
		catch( Exception e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
		
	}//End method execute
}//End class 