package org.ihtsdo.mojo.econcept;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refset.str.TkRefsetStrMember;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Export the workflow history to initialize the WfHx refset in the database
 * in eConcept format.
 *
 * @goal export-workflow-history-as-econcept
 * @phase process-resources
 */

public class ExportWorkflowHistoryAsEConcept extends AbstractMojo {

    /**
     *
     * @author Jesse Efron
     *
     */

    /**
     * Editing path UUIDs
     *
     * @parameter
     */
    private String editPathUuid;

    /**
     * Size of cache used by the database.
     *
     * @parameter
     */
    Long cacheSize = 600000000L;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private String projectDirectoryPath;

    /**
     * Location of the input Read-Only Database for analyzing and converting to EConcept.
     *
     * @parameter
     */
    private String generatedDirectory = "generated-resources";

    /**
     * Location of the input Read-Only Database for analyzing and converting to EConcept.
     *
     * @parameter
     */
    private String databaseDirectory = "berkeley-db";

    /**
     * Specifies whether to create new files (default) or append to existing
     * files.
     *
     * @required
     * @parameter
     */
    private String inputFileDirectory = "migration-wf-data";

    /**
     * Specifies name of jbin output file.
     *
     * @required
     * @parameter
     */
	private String inputFileName = "wfHx.txt";

	/**
     * Location of the build directory.
     *
     * @parameter
     * @required
     */
    private String outputDirectory = "jbin";

    /**
     * Specifies name of jbin output file.
     *
     * @required
     * @parameter
     */
	private String outputFileName = "wfHistory.jbin";


    private final int workflowIdPosition = 0;								// 0
    private final int conceptIdPosition = workflowIdPosition + 1;			// 1
    private final int useCaseIgnorePosition = conceptIdPosition + 1;			// 2
    private final int pathPosition = useCaseIgnorePosition + 1;				// 3
    private final int modelerPosition = pathPosition + 1;					// 4
    private final int actionPosition = modelerPosition + 1;					// 5
    private final int statePosition = actionPosition + 1;					// 6
    private final int fsnPosition = statePosition + 1;						// 7
    private final int refsetColumnTimeStampPosition = fsnPosition + 1;		// 8
    private final int timeStampPosition = refsetColumnTimeStampPosition + 1;	// 9
    
    private final int numberOfColumns = timeStampPosition + 1;				// 10

	private Scanner textScanner = null;
	private List<TkRefsetAbstractMember<?>> memberList = null;
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private UUID wfHxRefsetId = null;
	private UUID pathUuid = null;
	private UUID authorUuid = null;

	private DataOutputStream eConceptDOS = null;
	private DatabaseSetupConfig dbSetupConfig ;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		int conceptCounter = 0;
		String[] row = null;

		getLog().info("Exporting workflow history");

        try
        {
			// Initialize Loop
        	openAndInitializeReadOnlyWfHxDatabase();
        	initializeExport();
        	EConcept econcept = initializeEConcept();
			
			// Read Each Line of txt file
			while (textScanner.hasNext())
			{
				row = ((String)textScanner.nextLine()).split("\t");
				TkRefsetStrMember member = createTkMember(row);

				memberList.add(member);
				conceptCounter++;
			}

			// Finalize
			econcept.setRefsetMembers(memberList);
			econcept.writeExternal(eConceptDOS);
			eConceptDOS.close();
	        Terms.get().close();

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

	private void initializeExport() throws IOException, TerminologyException
	{
        initializeOutputFile();

        initializeConstantUuids();

        // Open Input File
        File txtFile = getInputTextFile();
        textScanner = new Scanner(txtFile);
	}

	private void initializeOutputFile() throws FileNotFoundException {
    	// Open Output jbin File

		File eConceptsFile = getOutputFile();         

		BufferedOutputStream eConceptsBos = new BufferedOutputStream(
				new FileOutputStream(eConceptsFile));

		eConceptDOS = new DataOutputStream(eConceptsBos);
	}

	private EConcept initializeEConcept() throws TerminologyException, IOException, ParseException
	{
		String[] row = ((String)textScanner.nextLine()).split("\t");

		I_GetConceptData con = Terms.get().getConcept(wfHxRefsetId);
		EConcept eC = new EConcept(con);

		TkRefsetStrMember initialMember = createTkMember(row);
		memberList = new ArrayList<TkRefsetAbstractMember<?>>();
		memberList.add(initialMember);

		return eC;
	}

	private TkRefsetStrMember createTkMember(String[] row) throws IOException, TerminologyException, ParseException
	{
		TkRefsetStrMember member = new TkRefsetStrMember();

		// Refset member Id
		member.setPrimordialComponentUuid(UUID.randomUUID());

		// Ref Comp Id
		member.setComponentUuid(UUID.fromString(row[conceptIdPosition]));

		// Refset Id
		member.setRefsetUuid(wfHxRefsetId);

		// Member Status Act/Inact
		member.statusUuid = ArchitectonicAuxiliary.Concept.CURRENT.getPrimoridalUid();

		// Author of WfHx
		member.authorUuid = authorUuid;

		// Path writing on
		member.pathUuid = pathUuid;

		member.time = format.parse(row[timeStampPosition]).getTime();

		member.setStrValue(toXml(row, member.time));

		return member;
	}

	private void initializeConstantUuids() throws IOException, TerminologyException
	{
        wfHxRefsetId = RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid();

        pathUuid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();

        authorUuid = ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid();
	}

	private String toXml(String[] row, long effectiveTimestamp) throws IOException, TerminologyException
	{
		try 
		{
			UUID modeler = WorkflowHelper.lookupModelerUid(row[modelerPosition]);
			UUID action = WorkflowHelper.lookupAction(row[actionPosition]).getPrimUuid();
			UUID state = WorkflowHelper.lookupState(row[statePosition]).getPrimUuid();

			long wfTimestamp = format.parse(row[refsetColumnTimeStampPosition]).getTime();

        	WorkflowHistoryRefsetWriter writer = new WorkflowHistoryRefsetWriter(); 
			
			writer.setWorkflowUid(UUID.fromString(row[workflowIdPosition]));
			writer.setPathUid(UUID.fromString(row[pathPosition]));
			writer.setModelerUid(modeler);
			writer.setActionUid(action);
			writer.setStateUid(state);
        	writer.setWorkflowTime(wfTimestamp);
			writer.setEffectiveTime(effectiveTimestamp);
			writer.setAutoApproved(false);
			writer.setOverride(false);
			writer.setFSN(row[fsnPosition]);

			return writer.fieldsToRefsetString();
			
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, row.toString(), new Exception("Failure in creating Workflow History EConcepts"));
		}

		return "";
	}


	private I_TermFactory openAndInitializeReadOnlyWfHxDatabase() throws Exception
	{
        	 File inputDatabase = getDatabaseFile();

            // Create database
            dbSetupConfig = new DatabaseSetupConfig();
            getLog().info("vodb dir: " + inputDatabase.getAbsolutePath());
            I_TermFactory origTF = Terms.get();

            try
            {
            	Terms.createFactory(inputDatabase, false, cacheSize, dbSetupConfig);

	            // Setup Config
	            I_TermFactory tf = Terms.get();
	
	            I_ConfigAceFrame activeConfig = null;
	
	            if (Terms.get().getActiveAceFrameConfig() == null) {
	            	activeConfig = NewDefaultProfile.newProfile(null, null, null, null, null);
	            	tf.setActiveAceFrameConfig(activeConfig);
            	}
	
	        } catch (Exception e) {
            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into workflow history refset");
            }

            return origTF;
	}

	private File getOutputFile() {
		String outputPath = projectDirectoryPath + File.separatorChar + generatedDirectory + File.separatorChar + outputDirectory + File.separatorChar;

		File directory = new File(outputPath);
        directory.mkdirs();
        
        return new File(directory, outputFileName);
	}

	private File getDatabaseFile() {
		String databasePath = projectDirectoryPath + File.separatorChar + databaseDirectory + File.separatorChar;

		return new File(databasePath);
	}

	private File getInputTextFile() 
	{
		String inputTextFilePath = projectDirectoryPath + File.separatorChar + generatedDirectory + File.separatorChar + inputFileDirectory + File.separatorChar;

        return new File(inputTextFilePath, inputFileName);
	}

}