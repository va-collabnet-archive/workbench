/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.mojo;


import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.ace.task.queue.OpenQueuesInFolder;
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.tasks.log.LogMessageOnWorkerLog;
import org.dwfa.bpa.tasks.util.Complete;
import org.dwfa.jini.JiniManager;
import org.dwfa.jini.TransactionParticipantAggregator;
import org.dwfa.tapi.TerminologyException;

import com.sun.jini.mahalo.LocalTransactionManager;



/**
 * <h1>AutomatedWorkflowAssignment</h1>
 * <br>
 * <p>This mojo is intended as a generic automation process for the generation and assignment of a business process to a particular user profile.</p>
 * <p>The parameters listed below refer to the configuration parameters of the mojos goal, passed from the executing pom file.</p>
 *
 * 
 * @param assigneeProfile - (required) has default value.
 * @param 2 - (optional)
 * <br><br> 
 * @goal assign-workflow-process
 * 
 * @see org.apache.maven.plugin.AbstractMojo
 * 
 */
public class AutomatedWorkflowAssignment extends AbstractMojo {
	
	/**
	 * <h2>assigneeProfile</h2>
	 * 
	 * <p>Private property of type <strong>"String"</strong>, used to hold the name of user profile<br>
	 * to assign generated workflow process to (usually the workflow manager).</p> 
	 * <p>This property is <strong>required</strong> to always have a value set. For this reason, <br>
	 * a default value is set by way of the parameter annotation expression value.</p>
	 * 
	 * @see java.lang.String
	 * 
	 * 
	 * @parameter expression="tore.dev"
     * @required
	 * 
	 */
	private String assigneeProfile;
	
	
	/**
	 * <h2>queueConfigFile</h2>
	 * 
	 * <p>Private property of type <strong>"File"</strong>, used to hold the Location of the assignee's queue config file.</p>
	 * <p>This property is <strong>required</strong> to always have a value set. For this reason, <br>
	 * a default value is set by way of the parameter annotation expression value.</p>
	 * 
	 * @see java.io.File
	 * 
	 * @parameter expression="${project.build.directory}\\..\\src\\main\\profiles\\users\\tore.dev\\inbox\\queue.config"
	 * @required
	 */
	private File queueConfigFile;
	
	
	/**
	 * <h2>processAttachments</h2>
	 * <p>Private property of type <strong>"Map"</strong>, used to hold a map of propertyName/Value pair for an attachment required by the workflow process.</p>
	 * <p>This property is <strong>optional</strong>, so no default need be set.</p>
	 * <p>If you require a pameter value to be a list of values, seperate the values using a ";"</p>
	 * <p><ul>i.e: <br><ul>value1;value2;value3</ul></ul></p>
	 * <p>You can also pas a list of lists by seperating each list of values with a ":"</p>
	 * <p><ul>i.e: <br><ul>value1;value2;value3:val1;val2;val3</ul></ul></p>
	 * @see java.util.Map
	 * 
	 * @parameter
	 * 
	 */
	private Map<String, String> processAttachments;
	
	
	/**
	 * <h2>businessProcess</h2>
	 * <p>Private property of type <strong>"String"</strong>, used to hold a file path to the business process to be assigned to the workflow manager.</p>
	 * <p>This property is <strong>required</strong> to always have a value set. For this reason, <br>
	 * a default value is set by way of the parameter annotation expression value.</p>
	 * 
	 * @see java.lang.String
	 * 
	 * @parameter expression="${project.build.directory}\\..\\src\\main\\bp\\assignment generators\\assignmentGenFromUuidList_InProperty.bp"
     * @required
	 * 
	 */
	private String businessProcess;
	
	/**
	 * <h2>uuidFile</h2>
	 * <p>Private property of type <strong>"String"</strong>, used to hold the path to a file containing uuids to be actioned by the workflow process.</p>
	 * <p>This property is <strong>optional</strong>, so no default need be set.</p>
	 * 
	 * @see java.lang.String
	 * 
	 * @parameter
     * 
	 * 
	 * 
	 */
	private File uuidFile;
	

	private String separator = System.getProperty("file.separator");
	private int listListSize = 250;
	private String inboxPath = null; 
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		JiniManager.setLocalOnly(true);
		
		inboxPath = "src" + separator + "main" + separator + "profiles" + separator + "users" + separator + assigneeProfile + separator + "inbox";

		try{		
			TransactionParticipantAggregator tpa = new TransactionParticipantAggregator(new String[] { "src/main/config/transactionAggregator.config"}, null);
			LocalTransactionManager ltm = new LocalTransactionManager(new String[] { "src/main/config/transactionManager.config"}, null);
					    		
	    	I_ConfigAceFrame config = LocalVersionedTerminology.get().getActiveAceFrameConfig();
	    			    		
//		    		queueDirectory = new File("target/myConfig.config");
//		    		queueDirectory.getParentFile().mkdirs();
//		    		FileWriter fw = new FileWriter(queueDirectory);
//		    		fw.append(" org.dwfa.mojo.MojoWorker {	tranDurLong = new Long(300000); ");
//		    		fw.append("    serviceDiscovery = new net.jini.lookup.ServiceDiscoveryManager(new net.jini.discovery.LookupDiscovery(groups, this), null, this);");
//		    		fw.append("    groups = org.dwfa.queue.QueueServer.groups; ");
//		    		fw.append(" }");
//		    		fw.close();
	    		
//	    		queueConfigFile = new File(inboxPath + separator + "queue.config");

	    	String[] entries = new String[]{ queueConfigFile.getAbsolutePath() };
	    	    
	    				    		
	    	Configuration configuration = 
	                 ConfigurationProvider.getInstance( entries,	                		 
	                                                    getClass().getClassLoader() );
	    		 
	    	Configuration workerConfiguration = 
	                 ConfigurationProvider.getInstance( new String[] { "myConfig.config" },	                		 
	                                                    getClass().getClassLoader() );
	    				    		
	    	MojoWorker mw = new MojoWorker( workerConfiguration, UUID.randomUUID() ,"MoJo worker" );
	    		
	    	I_ConfigAceFrame configFrame = NewDefaultProfile.newProfile( assigneeProfile, assigneeProfile, assigneeProfile, assigneeProfile );
	    	mw.writeAttachment( WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), configFrame );
	    	mw.getLogger().setLevel( Level.FINE );
	    		    	
	    	BusinessProcess assignmentProcess = createAssignmentProcess();
	    	
	    	if( assignmentProcess != null )
	    		mw.execute( createAssignmentProcess() );

		}
		catch( Exception e){
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
		
	}//End method execute

	private List<List<UUID>> createUuidListsFromFile( File inputFile ) throws IOException{
		
		List<List<UUID>> uuidListOfLists = new ArrayList<List<UUID>>();
        BufferedReader br = new BufferedReader( new FileReader( inputFile ) );
        String uuidLineStr;
       
        while ( ( uuidLineStr = br.readLine() ) != null ) { // while loop begins here
        	List<UUID> uuidList = new ArrayList<UUID>();
        	for (String uuidStr: uuidLineStr.split( "\t" )){
        			
        		UUID uuid = UUID.fromString( uuidStr );
        		uuidList.add( uuid );
        	}//End for loop
        	
        	uuidListOfLists.add(uuidList);
        	
        } //End while loop
		
		return uuidListOfLists;
		
	}//End createUuidListsFromFile
	
	private BusinessProcess createAssignmentProcess() throws PropertyVetoException, IOException, TerminologyException, IntrospectionException, IllegalAccessException, InvocationTargetException {
		// Construct the process. 
		BusinessProcess assignmentProcess = new BusinessProcess(
		        "Assignment process", Condition.CONTINUE, false);
		
		
		/*
		 * Add tasks to assignemt process
		 */
		LogMessageOnWorkerLog log1task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
		log1task.setMessage("-------- Starting assignment process...");

		
		OpenQueuesInFolder oqf = new OpenQueuesInFolder();
		oqf.setQueueDir(inboxPath);
		assignmentProcess.addTask(oqf);
		assignmentProcess.addBranch(log1task, oqf, Condition.CONTINUE);
		
		LaunchBatchGenAssignmentProcess genAssign = new LaunchBatchGenAssignmentProcess();
		genAssign.setProcessFileStr( businessProcess );		
		assignmentProcess.addTask(genAssign);
		assignmentProcess.addBranch(oqf, genAssign, Condition.CONTINUE);
		
		LogMessageOnWorkerLog log2task = (LogMessageOnWorkerLog) assignmentProcess.addTask(new LogMessageOnWorkerLog());
		log2task.setMessage("-------- Finished assignment process....");
		assignmentProcess.addBranch(genAssign, log2task, Condition.CONTINUE);

		assignmentProcess.addBranch(log2task, assignmentProcess.addTask(new Complete()), Condition.CONTINUE);		
		List<Collection<UUID>> uuidListList = null;
		if( uuidFile != null ){
			List<List<UUID>> tempListList = createUuidListsFromFile( uuidFile );
			int sizeOfList = tempListList.size();
	        
	        if (sizeOfList > listListSize){
	        	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, listListSize));
	        } else {
	        	uuidListList = new ArrayList<Collection<UUID>>(tempListList.subList(0, sizeOfList));
	        }
	        
	        
			if (tempListList.removeAll(uuidListList)){
	        	//do nothing
	        } else {
	        	assignmentProcess.getLogger().info("error encountered in removing uuid collection from list");
	        }
		}//End if uuidFile != null
		
		/*
		 * Set attachment values for assignment
		 */    		
		
		// Set workflow manager
		List<String> addresses = new ArrayList<String>();
		addresses.add( assigneeProfile );
		assignmentProcess.writeAttachment("workFlowManager", addresses);
		

		//Set config frame		
		I_TermFactory termFact = LocalVersionedTerminology.get();
		I_ConfigAceFrame cf = termFact.getActiveAceFrameConfig();
		cf.setUsername(assigneeProfile);
		assignmentProcess.writeAttachment( WorkerAttachmentKeys.ACE_FRAME_CONFIG.name(), cf );
		
		
		// the getAttachmentKey method prepends an A: to the key, thus providing the information necessary to read and write
		// the attachment as a property... Thus we use the get/setProperty methods. If you want to use
		// the read/writeAttachments methods, call ProcessAttachmentKeys.ASSIGNEE.getName() instead of getAttachment key. 
		
				
		//Set configuration attachements
		for( String key : processAttachments.keySet() ){
			String propertyLabel = ProcessAttachmentKeys.valueOf( key.toUpperCase() ).getAttachmentKey();
			
			if( processAttachments.get( key ).equalsIgnoreCase("uuidListList") ){
				/* This suggests that the uuid list is required and can not be empty/null.
				 * If the uuid list is empty or null we want to return a null business process so nothing gets executed.
				 */
				if( uuidListList == null || uuidListList.isEmpty() ) return null;
				
				assignmentProcess.setProperty( propertyLabel, uuidListList );
			}
			else{
				
				if( processAttachments.get( key ).contains(":") ){
					//Create list of list
					String[] array = processAttachments.get( key ).split( ":" );
					List<List> listOfLists = new ArrayList<List>();
					for( String listValues : array ){
						List<String> tempList = new ArrayList<String>();
						String[] array2 = listValues.split(";");
						for( String value : array ){
							getLog().info("value >> " + value);
							if( value != null && !value.equalsIgnoreCase("") ) tempList.add( value );
						}
						listOfLists.add( tempList );
					}
					assignmentProcess.setProperty( propertyLabel, listOfLists );
				}
				
				
				if( processAttachments.get( key ).contains(";") ){
					List<String> tempList = new ArrayList<String>();
					String[] array = processAttachments.get( key ).split( ";" );
					for( String value : array ){
						getLog().info("value >> " + value);
						if( value != null && !value.equalsIgnoreCase("") ) tempList.add( value );
					}
					assignmentProcess.setProperty( propertyLabel, tempList );
				}
				else{
					assignmentProcess.setProperty( propertyLabel, processAttachments.get( key ) );
				}
			}
		}//End for loop
		
		return assignmentProcess;
	}//End createAssignmentProcess
	
}//End class 
