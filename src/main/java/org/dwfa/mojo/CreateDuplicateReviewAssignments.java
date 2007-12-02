package org.dwfa.mojo;


import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import net.jini.config.Configuration;
import net.jini.config.ConfigurationProvider;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.ace.task.assignment.LaunchBatchGenAssignmentProcess;
import org.dwfa.ace.task.queue.OpenQueuesInFolder;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.tasks.log.LogMessageOnWorkerLog;
import org.dwfa.bpa.tasks.util.Complete;
import org.dwfa.tapi.TerminologyException;



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
	 * @parameter expression="${project.build.directory}\\..\\src\\main\\profiles\\users\\keith.dev\\inbox\\queue.config"
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
	 * @parameter expression="keith.dev"
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
//	private String processFileStr = "dup_check_processes/assignmentGenFromUuidList_InProperty.bp";
	private String processFileStr = "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment1.bp";
	
	
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
		    		
		    		
		    		queueDirectory = new File("target/myConfig.config");
		    		queueDirectory.getParentFile().mkdirs();
		    		FileWriter fw = new FileWriter(queueDirectory);
		    		fw.append(" org.dwfa.mojo.MojoWorker {	tranDurLong = new Long(300000); ");
		    		fw.append("    serviceDiscovery = new net.jini.lookup.ServiceDiscoveryManager(new net.jini.discovery.LookupDiscovery(groups, this), null, this);");
		    		fw.append("    groups = org.dwfa.queue.QueueServer.groups; ");
		    		fw.append(" }");
		    		fw.close();
		    		
		    		
//		    		queueDirectory = new File("C:\\_working\\au-ct\\amt-edit-bundle-branches\\branches\\0.2-Development\\dev\\src\\main\\profiles\\users\\keith.dev\\inbox\\queue.config");
		    		String[] entries = new String[]{ queueDirectory.getAbsolutePath() };
		    	    
		    		
//		    		ConfigurationFile cf = new ConfigurationFile(entries);
		    		
		    		 Configuration configuration = 
		                 ConfigurationProvider.getInstance(entries,	                		 
		                                                   getClass().getClassLoader());
		    		 
		    		 Configuration workerConfiguration = 
		                 ConfigurationProvider.getInstance(new String[] { "myConfig.config" },	                		 
		                                                   getClass().getClassLoader());
		    		
		    		getLog().info(">>>>>>>>>>>>>>>>> configuration created <<<<<<<<<<<<<<<<<<<<<<<");
		    		MojoWorker mw = new MojoWorker( workerConfiguration, UUID.randomUUID() ,"MoJo worker" );
		    		mw.getLogger().setLevel(Level.FINE);
		    		getLog().info(">>>>>>>>>>>>>>>>> worker created <<<<<<<<<<<<<<<<<<<<<<<");

		    		mw.execute(createTestRunLogProcess());
		    		mw.execute(createAssignmentProcess(inputFile));
		    		getLog().info(">>>>>>>>>>>>>>>>>>>>>   worker done <<<<<<<<<<<<<<<<<<<<<<<");
		    		
				}//End if
			}//End if
			
			
			
		}
		catch( Exception e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
		
	}//End method execute


	private BusinessProcess createTestRunLogProcess() throws PropertyVetoException {
		BusinessProcess testLogProcess = new BusinessProcess(
		        "Test logging process", Condition.CONTINUE, false);
		LogMessageOnWorkerLog log1task = (LogMessageOnWorkerLog) testLogProcess.addTask(new LogMessageOnWorkerLog());
		log1task.setMessage("********* A long time ago....");

		LogMessageOnWorkerLog log2task = (LogMessageOnWorkerLog) testLogProcess.addTask(new LogMessageOnWorkerLog());
		log2task.setMessage("******** in a galaxy far, far away....");
		testLogProcess.addBranch(log1task, log2task, Condition.CONTINUE);
		
		I_DefineTask completeTask = testLogProcess.addTask(new Complete());
		testLogProcess.addBranch(log2task, completeTask, Condition.CONTINUE);
		return testLogProcess;
	}
	
	private BusinessProcess createAssignmentProcess(File inputFile) throws PropertyVetoException, IOException, TerminologyException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		// Construct the process. 
		BusinessProcess assignmentProcess = new BusinessProcess(
		        "Assignment process", Condition.CONTINUE, false);
		LogMessageOnWorkerLog log1task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
		log1task.setMessage("-------- Starting assignment process...");

		OpenQueuesInFolder oqf = new OpenQueuesInFolder();
		oqf.setQueueDir("profiles\\users\\peter.dev\\inbox");
		assignmentProcess.addTask(oqf);
		assignmentProcess.addBranch(log1task, oqf, Condition.CONTINUE);
		
		LaunchBatchGenAssignmentProcess genAssign = new LaunchBatchGenAssignmentProcess();
		genAssign.setProcessFileStr( "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment1.bp" );		
		assignmentProcess.addTask(genAssign);
		assignmentProcess.addBranch(oqf, genAssign, Condition.CONTINUE);
		
		LogMessageOnWorkerLog log2task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
		log2task.setMessage("-------- Finished assignment process....");
		assignmentProcess.addBranch(genAssign, log2task, Condition.CONTINUE);

		assignmentProcess.addBranch(log2task, assignmentProcess.addTask(new Complete()), Condition.CONTINUE);		

		
		
		
		List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
         BufferedReader br = new BufferedReader( new FileReader( inputFile ) );
         String uuidLineStr;
         while ((uuidLineStr = br.readLine()) != null) { // while loop begins here
        	 List<UUID> uuidList = new ArrayList<UUID>();
        	 for (String uuidStr: uuidLineStr.split("\t")){
     			 assignmentProcess.getLogger().info("uuidStrs: " + uuidStr); 	
        		 UUID uuid = UUID.fromString(uuidStr);
        		 uuidList.add(uuid);
        	 }
        	 uuidListOfLists.add(uuidList);
         } // end while 
		
		
		
         List<List<UUID>> tempListList = uuidListOfLists;
		int sizeOfList = tempListList.size();
		
        List<Collection<UUID>> uuidListList = null;
        if (sizeOfList > listListSize){
        	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, listListSize));
        } else {
        	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, sizeOfList));
        }
        getLog().info(">>>>>>>>>>>>>>>>> list edited <<<<<<<<<<<<<<<<<<<<<<<");
		assignmentProcess.writeAttachment( "potDupUuidList", uuidListList);
        
		if (tempListList.removeAll(uuidListList)){
        	//do nothing
        } else {
        	assignmentProcess.getLogger().info("error encountered in removing uuid collection from list");
        }
        

		
		/*
		 * Set workflow manager
		 */
		List<String> addresses = new ArrayList<String>();
		addresses.add( workFlowManager );
		assignmentProcess.writeAttachment("workFlowManager", addresses);
		
		    		
		
		I_TermFactory termFact = LocalVersionedTerminology.get();
		I_ConfigAceFrame cf = termFact.getActiveAceFrameConfig();
		cf.setUsername("keith.dev");
		assignmentProcess.writeAttachment( WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), cf );
		
		getLog().info(">>>>>>>>>>>>>>>>> wf man added <<<<<<<<<<<<<<<<<<<<<<<");
		/*
		 * Set business process 
		 */		    			    		
		assignmentProcess.writeAttachment("processFileName", "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment1.bp");
		getLog().info(">>>>>>>>>>>>>>>>> bp added <<<<<<<<<<<<<<<<<<<<<<<");
		
		/*
		 * Set instruction file details
		 */
		assignmentProcess.writeAttachment("instructionFileName", "instructions_dup.html");
		assignmentProcess.writeAttachment("directoryName", "C:\\AMT Processes\\instructions");
		
		getLog().info(">>>>>>>>>>>>>>>>> instruction added <<<<<<<<<<<<<<<<<<<<<<<");
		
				
		File file = new File("profiles\\users\\peter.dev\\inbox");
		if(file.exists()){
			getLog().info("[file path] >> "+file.getAbsolutePath());
		}
		
		
		// You could use set property here instead of writeAttachment if you prepend and A: as in "A: assigneeAddrPropName"
		assignmentProcess.writeAttachment("assigneeAddrPropName", addresses);
		assignmentProcess.writeAttachment("batchGenAssigneePropName", addresses);
		assignmentProcess.writeAttachment("processFileStr", "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment1.bp");
		assignmentProcess.writeAttachment("processToAssignPropName", "processFileName");
		assignmentProcess.writeAttachment("newDestinationProp", "keith.dev");
		assignmentProcess.writeAttachment("destinationProp", "keith.dev");
		
		// the getAttachmentKey method prepends an A: to the key, thus providing the information necessary to read and write
		// the attachment as a property... Thus we use the get/setProperty methods. If you want to use
		// the read/writeAttachments methods, call ProcessAttachmentKeys.ASSIGNEE.getName() instead of getAttachment key. 
		
		assignmentProcess.setProperty(ProcessAttachmentKeys.ASSIGNEE.getAttachmentKey(), "keith.dev");
		assignmentProcess.setProperty(ProcessAttachmentKeys.BATCH_UUID_LIST2.getAttachmentKey(), uuidListList);
		assignmentProcess.setProperty(ProcessAttachmentKeys.DESTINATION_ADR.getAttachmentKey(), "keith.dev");
		assignmentProcess.setProperty(ProcessAttachmentKeys.SELECTED_ADDRESSES.getAttachmentKey(), addresses);
		assignmentProcess.setProperty(ProcessAttachmentKeys.TO_ASSIGN_PROCESS.getAttachmentKey(), "C:\\AMT Processes\\TA assignment processes\\dupReviewAssignment1.bp");
		
		return assignmentProcess;
	}
}//End class 