package org.ihtsdo.workflow.refset.mojo.init;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;

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
	            	writer.setModelerUid(lookupEditorUid(columns[modelerPosition]));
	
	            	writer.setActionUid(lookupActionUid(columns[actionPosition]));
	            	writer.setStateUid(lookupStateUid(columns[statePosition]));
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

	private UUID lookupEditorUid(String string) throws IOException, TerminologyException 
	{
        if (string.equalsIgnoreCase("spackman"))
       		return ArchitectonicAuxiliary.Concept.KENT_SPACKMAN.getPrimoridalUid();
        else if (string.equalsIgnoreCase("jogo"))
       		return ArchitectonicAuxiliary.Concept.JO_GOULDING.getPrimoridalUid();
        else if (string.equalsIgnoreCase("msmith"))
       		return ArchitectonicAuxiliary.Concept.MIKE_SMITH.getPrimoridalUid();
        else if (string.equalsIgnoreCase("dkonice"))
           		return ArchitectonicAuxiliary.Concept.DEBORAH_KONICEK.getPrimoridalUid();
        else if (string.equalsIgnoreCase("alopez"))
       		return ArchitectonicAuxiliary.Concept.ALEJANDRO_LOPEZ.getPrimoridalUid();
        else if (string.equalsIgnoreCase("alejandro"))
       		return ArchitectonicAuxiliary.Concept.ALEJANDRO_LOPEZ.getPrimoridalUid();
        else if (string.equalsIgnoreCase("emme"))
           		return ArchitectonicAuxiliary.Concept.EMMA_MELHUISH.getPrimoridalUid();
        else if (string.equalsIgnoreCase("greynos"))
           		return ArchitectonicAuxiliary.Concept.GUILLERMO_REYNOSO.getPrimoridalUid();
        else if (string.equalsIgnoreCase("pbrottm"))
           		return ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.getPrimoridalUid();
        else if (string.equalsIgnoreCase("phought"))
           		return ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.getPrimoridalUid();
        else if (string.equalsIgnoreCase("mgerard"))
           		return ArchitectonicAuxiliary.Concept.MARY_GERARD.getPrimoridalUid();
        else if (string.equalsIgnoreCase("llivesa"))
           		return ArchitectonicAuxiliary.Concept.PENNY_LIVESAY.getPrimoridalUid();
        else if (string.equalsIgnoreCase("jmirza"))
           		return ArchitectonicAuxiliary.Concept.JALEH_MIZRA.getPrimoridalUid();
        else if (string.equalsIgnoreCase("khaake"))
           		return ArchitectonicAuxiliary.Concept.KIRSTEN_HAAKE.getPrimoridalUid();
        else if (string.equalsIgnoreCase("mvanber"))
           		return ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.getPrimoridalUid();
        else if (string.equalsIgnoreCase("rturnbu"))
           		return ArchitectonicAuxiliary.Concept.ROBERT_TURNBULL.getPrimoridalUid();
        else if (string.equalsIgnoreCase("rmoldwi"))
           		return ArchitectonicAuxiliary.Concept.RICHARD_MOLDWIN.getPrimoridalUid();
        else if (string.equalsIgnoreCase("clundbe"))
           		return ArchitectonicAuxiliary.Concept.CYNDIE_LUNDBERG.getPrimoridalUid();
        else if (string.equalsIgnoreCase("nalbarr"))
           		return ArchitectonicAuxiliary.Concept.NARCISO_ALBARRACIN.getPrimoridalUid();
        else if (string.equalsIgnoreCase("vparekh"))
           		return ArchitectonicAuxiliary.Concept.VARSHA_PAREKH.getPrimoridalUid();
        else if (string.equalsIgnoreCase("cspisla"))
           		return ArchitectonicAuxiliary.Concept.CHRISTINE_SPISLA.getPrimoridalUid();

		return null;
	}

	private UUID getDummyUseCase()
	{
		return UUID.fromString("de6a2fcf-24b7-3a46-aa62-27d1958e3a16");
	}

	private UUID lookupStateUid(String string) throws IOException, TerminologyException {
		if (string.equalsIgnoreCase("Approved workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Changed workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Changed in batch workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_IN_BATCH_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("For Chief Terminologist review workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Concept having no prior workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_EMPTY_NO_WFHX_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Concept not previously existing workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_EMPTY_NOT_EXISTING_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Escalated workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATED_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("New workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("For review workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_STATE.getPrimoridalUid();
		else if (string.equalsIgnoreCase("For discussion workflow state"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSSION_STATE.getPrimoridalUid();
		else
			return null;
	}

	private UUID lookupActionUid(String string) throws IOException, TerminologyException {
		if (string.equalsIgnoreCase("Accept workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Chief Terminologist review workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Commit workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Commit in batch workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Discuss workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSS_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Escalate workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATE_ACTION.getPrimoridalUid();
		else if (string.equalsIgnoreCase("Review workflow action"))
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_ACTION.getPrimoridalUid();
		else
			return null;
	}
}
