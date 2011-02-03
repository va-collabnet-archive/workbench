package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
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
    private static final int workflowIdPosition = 0;
    private static final int conceptIdPosition = 1;
    private static final int useCasePosition = 2;
    private static final int pathPosition = 3;
    private static final int modelerPosition = 4;
    private static final int actionPosition = 5;
    private static final int statePosition = 6;
    private static final int fsnPosition = 7;
    private static final int refsetColumnTimeStampPosition = 8;
    private static final int timeStampPosition = 9;

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        System.setProperty("java.awt.headless", "true");
        String line = null;
        WorkflowHistoryRefsetWriter writer = null;
        
        try {
            writer = new WorkflowHistoryRefsetWriter();

            Scanner scanner = new Scanner(new File(filePath));

            while (scanner.hasNextLine())
            {
            	line = scanner.nextLine();
            	String[] columns = line.split("\t");


            	UUID refConUid = UUID.fromString(columns[conceptIdPosition]);

            	if (Terms.get().hasId(refConUid))
            	{
            		writer.setWorkflowUid(UUID.fromString(columns[workflowIdPosition]));
	            	writer.setConceptUid(UUID.fromString(columns[conceptIdPosition]));
	            	writer.setUseCaseUid(getDummyUseCase());
	            	writer.setPathUid(UUID.fromString(columns[pathPosition]));
	            	writer.setModelerUid(WorkflowHelper.lookupModeler(columns[modelerPosition]).getPrimUuid());
	            	writer.setActionUid(WorkflowHelper.lookupAction(columns[actionPosition]).getPrimUuid());
	            	writer.setStateUid(WorkflowHelper.lookupState(columns[statePosition]).getPrimUuid());
	            	writer.setFSN(columns[fsnPosition]);
	            	
        			long timestamp = format.parse(columns[timeStampPosition]).getTime();
	            	writer.setTimeStamp(timestamp);
	
        			timestamp = format.parse(columns[refsetColumnTimeStampPosition]).getTime();
	            	writer.setRefsetColumnTimeStamp(timestamp);
	
	            	writer.addMember();
            	} 
            }	

            Terms.get().addUncommitted(writer.getRefsetConcept());
        } catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, line, e);
		}
	}

    public File getTargetDirectory() {
        return targetDirectory;
	}

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
	}

	private UUID getDummyUseCase()
	{
		return UUID.fromString("de6a2fcf-24b7-3a46-aa62-27d1958e3a16");
	}
}
