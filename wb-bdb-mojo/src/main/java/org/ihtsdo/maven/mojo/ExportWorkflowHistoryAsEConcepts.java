package org.ihtsdo.maven.mojo;

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

/**
 * Export the workflow history to initialize the WfHx refset in the database
 * in eConcept format.
 *
 * @goal export-workflow-history-as-econcept
 * @phase process-resources
 */

public class ExportWorkflowHistoryAsEConcepts extends AbstractMojo {

    /**
     *
     * @author Jesse Efron
     *
     */

    /**
     * Location of the build directory.
     *
     * @parameter
     * @required
     */
    private String targetDirectoryPath;

    /**
     * Editing path UUIDs
     *
     * @parameter
     * @required
     */
    private String editPathUuid;

    /**
     * @parameter
     */
    private DatabaseSetupConfig dbSetupConfig;

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
    private File projectDirectory;

    /**
     * Location of the input Read-Only Database for analyzing and converting to EConcept.
     *
     * @parameter
     */
    private String inputDatabasePath;

    /**
     * Specifies whether to create new files (default) or append to existing
     * files.
     *
     * @required
     * @parameter
     */
    private String inputFilePath;

	private static final int workflowIdPosition = 0;
	private static final int conceptIdPosition = 1;
	private static final int useCasePosition = 2;
	private static final int pathPosition = 3;
	private static final int modelerPosition = 4;
	private static final int actionPosition = 5;
	private static final int statePosition = 6;
	private static final int fsnPosition = 7;
	private static final int timeStampPosition = 8;

	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private DataOutputStream eConceptDOS = null;
	private Scanner textScanner = null;

	private UUID wfHxRefsetId = null;
	private UUID pathUuid = null;
	private UUID authorUuid = null;
	private String currentConceptUuid = null;

	private int conceptCounter = 0;
	private int conceptGroupCounter = 0;
	private List<TkRefsetAbstractMember<?>> memberList = null;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		EConcept currentEConcept = null;
		String[] row = null;

		getLog().info("Exporting workflow history");

        try
        {
        	I_TermFactory origTF = openAndInitializeReadOnlyWfHxDatabase();

        	initializeExport();

			// Initialize Loop
			currentEConcept = processFirstRow();
			// Read Each Line of txt file
			while (textScanner.hasNext())
			{
				row = ((String)textScanner.nextLine()).split("\t");
				conceptCounter++;

				if (!currentConceptUuid.equals(row[conceptIdPosition]))
				{
					finalizeCurrentEConcept(currentEConcept, memberList);
					I_GetConceptData c = Terms.get().getConcept(wfHxRefsetId);
					EConcept eC  = new EConcept(c);
					currentEConcept = eC;
					currentConceptUuid = row[conceptIdPosition];
					memberList.clear();
				}

				TkRefsetStrMember member = createTkMember(row);
				memberList.add(member);
			}

			// Finalize
			finalizeCurrentEConcept(currentEConcept, memberList);
			eConceptDOS.close();

	        Terms.get().close();
	        Terms.set(origTF);

        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

	private void finalizeCurrentEConcept(EConcept eC, List<TkRefsetAbstractMember<?>> members) throws IOException
	{
		conceptGroupCounter++;
		eC.setRefsetMembers(members);
		eC.writeExternal(eConceptDOS);
	}

	private String getFilePrefix() throws IOException {
		String prefix = projectDirectory.getCanonicalPath() + File.separatorChar + targetDirectoryPath + File.separatorChar;

        return prefix;
	}

	private void initializeExport() throws IOException, TerminologyException
	{
        String prefix = getFilePrefix();

        initializeOutputFile(prefix);

        initializeConstantUuids();

        // Open Input File
        try
        {
        	textScanner = new Scanner(new File(inputFilePath));
        } catch (Exception e) {
        	textScanner = new Scanner(new File("ihtsdo-generate-econcepts-trek/" + inputFilePath));
        }
	}

	private void initializeOutputFile(String prefix) throws FileNotFoundException {
    	// Open Output jbin File
        File directory = new File(prefix);
        directory.mkdirs();

		File eConceptsFile = new File(directory, "wfHistory.jbin");
		eConceptsFile.getParentFile().mkdirs();

		BufferedOutputStream eConceptsBos = new BufferedOutputStream(
				new FileOutputStream(eConceptsFile));

		eConceptDOS = new DataOutputStream(eConceptsBos);
	}

	private EConcept processFirstRow() throws TerminologyException, IOException, ParseException
	{
		String[] row = ((String)textScanner.nextLine()).split("\t");
		currentConceptUuid = row[conceptIdPosition];

		I_GetConceptData con = Terms.get().getConcept(UUID.fromString(row[conceptIdPosition]));
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

	private String toXml(String[] row, long timeStamp) throws IOException, TerminologyException
	{
		try 
		{
			UUID modeler = lookupModelerUid(row[modelerPosition]);
			UUID action = lookupActionUid(row[actionPosition]);
			UUID state = lookupStateUid(row[statePosition]);

			String useCase = getCaseTypeUidStringByUseCase(row[useCasePosition]);
			if (state == null || action == null)
				throw new Exception("Can't identify action or state");

			return "<properties>\n" +
					   	"<property>" +
					   		"<key>workflowId</key>" +
					   		"<value>" + row[workflowIdPosition] + "</value>" +
					   	"</property>" +
					   	"<property>" +
				   			"<key>useCase</key>" +
				   			"<value>" + useCase + "</value>" +
			   			"</property>" +
					   	"<property>" +
				   			"<key>path</key>" +
				   			"<value>" + row[pathPosition] + "</value>" +
			   			"</property>" +
					   	"<property>" +
				   			"<key>modeler</key>" +
				   			"<value>" + modeler + "</value>" +
			   			"</property>" +
				   		"<property>" +
				   			"<key>state</key>" +
				   			"<value>" + state + "</value>" +
						"</property>" +
				   		"<property>" +
			   				"<key>action</key>" +
			   				"<value>" + action + "</value>" +
			   			"</property>" +
				   		"<property>" +
			   				"<key>refsetColumnTimeStamp</key>" +
			   				"<value>" + timeStamp + "</value>" +
			   			"</property>" +
				   		"<property>" +
				   			"<key>fsn</key>" +
				   			"<value>" + row[fsnPosition] + "</value>" +
			   			"</property>" +
				"</properties>";
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, row.toString(), new Exception("Failure in creating Workflow History EConcepts"));
		}

		return "";
	}

	private UUID lookupModelerUid(String string) throws IOException, TerminologyException 
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


		// TODO Auto-generated method stub
		return null;
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

	private String getCaseTypeUidStringByUseCase(String input)
	{
		if (input.equals("NEW"))
			return "05395fa5-d8ea-3f0f-8850-8795ea0f9aea";
		else
			return "de6a2fcf-24b7-3a46-aa62-27d1958e3a16";
	}

	private I_TermFactory openAndInitializeReadOnlyWfHxDatabase() throws Exception
	{
        	 File inputDatabase = new File(inputDatabasePath);

            // Create database
            dbSetupConfig = new DatabaseSetupConfig();
            getLog().info("vodb dir: " + inputDatabase.getAbsolutePath());
            I_TermFactory origTF = Terms.get();

            try
            {
            	Terms.createFactory(inputDatabase, false, cacheSize, dbSetupConfig);
            } catch (Exception e) {
            	inputDatabase = new File("ihtsdo-generate-econcepts-trek/" + inputDatabasePath);
                getLog().info("vodb dir: " + inputDatabase.getAbsolutePath());
            	Terms.createFactory(inputDatabase, false, cacheSize, dbSetupConfig);
            }

            // Setup Config
            I_TermFactory tf = Terms.get();

            I_ConfigAceFrame activeConfig = null;

            if (Terms.get().getActiveAceFrameConfig() == null)
            	activeConfig = NewDefaultProfile.newProfile(null, null, null, null, null);

            tf.setActiveAceFrameConfig(activeConfig);

            // Set Edit Path
            activeConfig = Terms.get().getActiveAceFrameConfig();
            tf = Terms.get();

            activeConfig.getEditingPathSet().clear();
            activeConfig.addEditingPath(tf.getPath(UUID.fromString(editPathUuid)));

            return origTF;
	}
}