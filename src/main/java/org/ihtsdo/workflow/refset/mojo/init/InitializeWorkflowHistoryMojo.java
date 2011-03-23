package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.example.binding.Taxonomies;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * @author Jesse Efron
 *
 * @goal initialize-workflow-history-refset
 * @requiresDependencyResolution compile
 */

public class InitializeWorkflowHistoryMojo extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * The name of the database to create. All sql inserts will be against this
     * database.
     *
     * @parameter
     * @required
     */
    private String filePath;
    
    /**
     * Whether to alert user of a bad row that can't be imported into the database
     * 
     * @parameter
     * default-value=true
     * @required
     */
    private boolean reportErrors;

    private I_GetConceptData snomedConcept = null;

    private static final int workflowIdPosition = 0;								// 0
    private static final int conceptIdPosition = workflowIdPosition + 1;			// 1
    private static final int useCaseIgnorePosition = conceptIdPosition + 1;			// 2
    private static final int pathPosition = useCaseIgnorePosition + 1;				// 3
    private static final int modelerPosition = pathPosition + 1;					// 4
    private static final int actionPosition = modelerPosition + 1;					// 5
    private static final int statePosition = actionPosition + 1;					// 6
    private static final int fsnPosition = statePosition + 1;						// 7
    private static final int refsetColumnTimeStampPosition = fsnPosition + 1;		// 8
    private static final int timeStampPosition = refsetColumnTimeStampPosition + 1;	// 9
    
    private static final int numberOfColumns = timeStampPosition + 1;				// 10

    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Map<String, UUID> releases = new HashMap<String, UUID>();

   @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");
        String line = null;
        WorkflowHistoryRefsetWriter writer = null;
        
        try {
        	snomedConcept = Terms.get().getConcept(Taxonomies.SNOMED.getUuids());
        	
            writer = new WorkflowHistoryRefsetWriter();
            Scanner scanner = new Scanner(new File(filePath));
            int lineCounter = 1;
            
            while (scanner.hasNextLine())
            {
            	line = scanner.nextLine();
            	String[] columns = line.split("\t");
            	
            	if (lineCounter++ % 2500 == 0)
            	{
                    System.out.println("At: " + (lineCounter - 1));
            	}

            	if (columns.length == numberOfColumns)
            	{
            		UUID releaseDescription = identifyReleaseDescription(columns[timeStampPosition]);
            		writer.setReleaseDescriptionUid(releaseDescription);
            		writer.setWorkflowUid(UUID.fromString(columns[workflowIdPosition]));
	            	writer.setConceptUid(UUID.fromString(columns[conceptIdPosition]));
	            	writer.setPathUid(UUID.fromString(columns[pathPosition]));
	            	writer.setModelerUid(WorkflowHelper.lookupModeler(columns[modelerPosition]).getPrimUuid());
	            	writer.setActionUid(WorkflowHelper.lookupAction(columns[actionPosition]).getPrimUuid());
	            	writer.setStateUid(WorkflowHelper.lookupState(columns[statePosition]).getPrimUuid());
	            	writer.setFSN(columns[fsnPosition]);
	            	
        			long timestamp = format.parse(columns[timeStampPosition]).getTime();
	            	writer.setEffectiveTime(timestamp);
	
        			timestamp = format.parse(columns[refsetColumnTimeStampPosition]).getTime();
	            	writer.setWorkflowTime(timestamp);
	
	            	writer.addMember();
            	} else if (reportErrors) {
                	AceLog.getAppLog().log(Level.WARNING, line, new Exception("Unable to import this row into workflow history refset"));
            	}
            	
            	if (lineCounter % 100 == 0)
            	{
            		Terms.get().addUncommitted(writer.getRefsetConcept());
            		Terms.get().commit();
            	}

            }	
        } catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Exception: " + e.getMessage() + " at line: " + line);
		}
	}

    private UUID identifyReleaseDescription(String timestamp) 
    {
    	if (!releases.containsKey(timestamp))
    	{
	    	// timestamp format: 2008-07-31 00:00:00
	    	UUID retId = null;
			String searchStringPrefix = "version: ";
			String searchDate = timestamp.substring(0, 4) + timestamp.substring(5,7) + timestamp.substring(8, 10);
			
			try {
				for ( I_DescriptionVersioned desc : snomedConcept.getDescriptions())
				{
					I_DescriptionTuple tuple = desc.getLastTuple();
					int currentState = tuple.getStatusNid();
					
					if (tuple.getText().contains(searchStringPrefix + searchDate))
					{
						releases.put(timestamp, desc.getPrimUuid());
						return desc.getPrimUuid();
					}
				}
				
				// TODO Fix this with new solution &&&& InitWfHxOnCommit as will be using this RefCompId
				// If here, means that the first time for which a member is being added for the 
				// current release which for current testing is 2011-01-31
				releases.put(timestamp, snomedConcept.getPrimUuid());
			} catch (Exception e) {
		    	AceLog.getAppLog().log(Level.SEVERE, "Unable to identify the release description for timestamp: " + timestamp);
			}
    	} 

   		return releases.get(timestamp);
	}

	public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}
}
