package org.ihtsdo.mojo.econcept;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.refex.type_string.TkRefsetStrMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

/**
 * Export the workflow history to initialize the WfHx refset in the database
 * in eConcept format.
 *
 * @goal export-workflow-history-as-econcept
 */

public class ExportWorkflowHistoryAsEConcept extends AbstractMojo {

    /**
     *
     * @author Jesse Efron
     *
     */

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

	private BufferedReader inputFile = null;
	private List<TkRefexAbstractMember<?>> memberList = null;
	private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private UUID wfHxRefsetId = null;
	private UUID snomedPathUid = null;
	private UUID authorUuid = null;
        private UUID moduleUuid = null;
	private UUID currentStatus = null;
	private UUID snomedPath = null;

	private DataOutputStream eConceptDOS = null;

	private WorkflowHistoryRefsetWriter writer;

	public void execute() throws MojoExecutionException, MojoFailureException
	{
		int conceptCounter = 0;
		String[] row = null;

		getLog().info("Exporting workflow history");

    	int counter = 0;
    	String currentRow = "";

    	try
        {
        	// Initialize Loop
	    	initializeExport();
	    	EConcept econcept = initializeEConcept();
			
			// Read Each Line of txt file
	    	System.out.print("\n\n1-");
	    	
	    	while ((currentRow = inputFile.readLine()) != null)
			{
				row = currentRow.split("\t");

				TkRefsetStrMember member = createTkMember(row);
				
				if (++counter % 100 == 0) {
                    System.out.print('.');
				}

				if (counter % 8000 == 0) {
                    System.out.print("\n" + counter + "-");
				}
				memberList.add(member);
				conceptCounter++;
			} 
			System.out.println("Total rows imported: " + counter);	
			System.out.print("\n\n");

	        econcept.setRefsetMembers(memberList);
	    	econcept.writeExternal(eConceptDOS);
	    	eConceptDOS.close();
        } catch (Exception e) {
                        e.printStackTrace();
			AceLog.getAppLog().log(Level.WARNING, "Failure to import: " + currentRow + " at row# " + counter + "\nwith error: " + e);
        }
	}

	private void initializeExport() throws IOException, TerminologyException
	{
		writer = new WorkflowHistoryRefsetWriter(); 

		initializeOutputFile();

        initializeConstantUuids();

        // Open Input File
        File txtFile = getInputTextFile();
        inputFile =  new BufferedReader(new FileReader(txtFile));
        
        snomedPath = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();
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
		String[] row = ((String)inputFile.readLine()).split("\t");

		//I_GetConceptData con = Terms.get().getConcept();
		//EConcept eC = new EConcept(con);
		EConcept eC = makeEConcept(wfHxRefsetId);

		TkRefsetStrMember initialMember = createTkMember(row);
		memberList = new ArrayList<TkRefexAbstractMember<?>>();
		memberList.add(initialMember);

		return eC;
	}

	private TkRefsetStrMember createTkMember(String[] row) throws IOException, TerminologyException, ParseException
	{
		TkRefsetStrMember member = new TkRefsetStrMember();

		// Refset member Id
		member.setPrimordialComponentUuid(UUID.randomUUID());

		// Ref Comp Id
		member.setComponentUuid(UUID.fromString(row[WorkflowHelper.conceptIdPosition]));

		// Refset Id
		member.setRefsetUuid(wfHxRefsetId);

		// Member Status Act/Inact
		member.statusUuid = currentStatus;

		// Author of WfHx
		member.authorUuid = authorUuid;
                
                // Module for authoring
		member.moduleUuid = moduleUuid;

		// Path writing on
		member.pathUuid = snomedPathUid;

                try{
                    member.time = format.parse(row[WorkflowHelper.timeStampPosition]).getTime();
                }catch(IndexOutOfBoundsException e){
                    System.out.println("WFID: " + row[WorkflowHelper.workflowIdPosition]);
                }

		member.setString1(toXml(row, member.time));

		return member;
	}

	private void initializeConstantUuids() throws IOException, TerminologyException
	{
        wfHxRefsetId = RefsetAuxiliary.Concept.WORKFLOW_HISTORY.getPrimoridalUid();

        snomedPathUid = ArchitectonicAuxiliary.Concept.SNOMED_CORE.getPrimoridalUid();

        authorUuid = ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid();
        
        moduleUuid = TkRevision.unspecifiedModuleUuid;

        currentStatus = SnomedMetadataRf2.ACTIVE_VALUE_RF2.getUuids()[0];
	}


	private String toXml(String[] row, long effectiveTimestamp) throws IOException, TerminologyException
	{
		try 
		{	
                    UUID modeler = lookupModeler(row[WorkflowHelper.modelerPosition]);
			UUID action = lookupAction(row[WorkflowHelper.actionPosition]);
			UUID state = lookupState(row[WorkflowHelper.statePosition]);

			long wfTimestamp = format.parse(row[WorkflowHelper.refsetColumnTimeStampPosition]).getTime();

			
			writer.setWorkflowUid(UUID.fromString(row[WorkflowHelper.workflowIdPosition]));
			writer.setPathUid(snomedPath);
			writer.setModelerUid(modeler);
			writer.setActionUid(action);
			writer.setStateUid(state);
                        writer.setWorkflowTime(wfTimestamp);
			writer.setEffectiveTime(effectiveTimestamp);
			writer.setAutoApproved(false);
			writer.setOverride(false);
			writer.setFSN(row[WorkflowHelper.fsnPosition]);

			// Setting of Concept in writer is not needed for EConcept, but required for valuesExist() method
			writer.setConceptUid(UUID.fromString(row[WorkflowHelper.conceptIdPosition]));
			
			if (writer.getProperties().valuesExist())
				return writer.fieldsToRefsetString();
			else
				AceLog.getAppLog().log(Level.WARNING, "Couldn't identify all values in refset row");
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Error in processing row: " + row.toString() + " with error: " + e.getMessage());
		}

		return "";
	}




	private File getOutputFile() {
		String outputPath = projectDirectoryPath + File.separatorChar + generatedDirectory + File.separatorChar + outputDirectory + File.separatorChar;

		File directory = new File(outputPath);
        directory.mkdirs();
        
        return new File(directory, outputFileName);
	}

	private File getInputTextFile() 
	{
		String inputTextFilePath = projectDirectoryPath + File.separatorChar + generatedDirectory + File.separatorChar + inputFileDirectory + File.separatorChar;

        return new File(inputTextFilePath, inputFileName);
	}

    private EConcept makeEConcept(UUID primUuid) throws IOException, TerminologyException {
        MemoryTermServer mts = new MemoryTermServer();
        EConcept testConcept = null;
        RefsetAuxiliary ra = new RefsetAuxiliary();
        
        try {
            LocalFixedTerminology.setStore(mts);
            mts.setGenerateIds(true);

            ra.addToMemoryTermServer(mts);
			I_ConceptualizeLocally con = mts.getConcept(mts.getNid(primUuid));
	    
			testConcept = new EConcept(con, mts);
        } catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Failed creating EConcept with error: " + e.getMessage());
		}
                if(testConcept.conceptAttributes.moduleUuid == null){
                    System.out.println("1: " + testConcept);
                }
                
                for(TkDescription desc : testConcept.descriptions){
                    if(desc.moduleUuid == null){
                        System.out.println("2: " + testConcept);
                    }
                }
                
                for(TkRelationship rel : testConcept.relationships){
                    if(rel.moduleUuid == null){
                        System.out.println(": " + testConcept);
                    }
                }
        return testConcept;
    }
    
    private UUID lookupModeler(String modeler) throws IOException, TerminologyException {
    	if (modeler.equalsIgnoreCase("IHTSDO")) {
    		return ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("spackman")) {
    		return ArchitectonicAuxiliary.Concept.KENT_SPACKMAN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("mvanber")) {
    		return ArchitectonicAuxiliary.Concept.MONIQUE_VAN_BERKUM.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("khaake")) {
    		return ArchitectonicAuxiliary.Concept.KIRSTEN_HAAKE.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("jmirza")) {
    		return ArchitectonicAuxiliary.Concept.JALEH_MIZRA.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("llivesa")) {
    		return ArchitectonicAuxiliary.Concept.PENNY_LIVESAY.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("mgerard")) {
    		return ArchitectonicAuxiliary.Concept.MARY_GERARD.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("msmith")) {
    		return ArchitectonicAuxiliary.Concept.MIKE_SMITH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("rturnbu")) {
    		return ArchitectonicAuxiliary.Concept.ROBERT_TURNBULL.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("phought")) {
    		return ArchitectonicAuxiliary.Concept.PATRICIA_HOUGHTON.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("pbrottm")) {
    		return ArchitectonicAuxiliary.Concept.PHILLIP_BROTTMAN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("greynos")) {
    		return ArchitectonicAuxiliary.Concept.GUILLERMO_REYNOSO.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("alopez")) {
    		return ArchitectonicAuxiliary.Concept.ALEJANDRO_LOPEZ.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("emme")) {
    		return ArchitectonicAuxiliary.Concept.EMMA_MELHUISH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("dkonice")) {
    		return ArchitectonicAuxiliary.Concept.DEBORAH_KONICEK.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("skeni")) {
    		return ArchitectonicAuxiliary.Concept.SARITA_KENI.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("sspence")) {
    		return ArchitectonicAuxiliary.Concept.SAMANTHA_SPENCER.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("jogo")) {
    		return ArchitectonicAuxiliary.Concept.JO_GOULDING.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("clundbe")) {
    		return ArchitectonicAuxiliary.Concept.CYNDIE_LUNDBERG.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("rmoldwi")) {
    		return ArchitectonicAuxiliary.Concept.RICHARD_MOLDWIN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("nalbarr")) {
    		return ArchitectonicAuxiliary.Concept.NARCISO_ALBARRACIN.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("vparekh")) {
    		return ArchitectonicAuxiliary.Concept.VARSHA_PAREKH.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("cspisla")) {
    		return ArchitectonicAuxiliary.Concept.CHRISTINE_SPISLA.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("dmcginn")) {
    		return ArchitectonicAuxiliary.Concept.DORIS_MCGINNESS.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("asyed")) {
    		return ArchitectonicAuxiliary.Concept.ASIF_SYED.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("cvalles")) {
    		return ArchitectonicAuxiliary.Concept.CECILIA_VALLESE.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("alejandro")) {
    		return ArchitectonicAuxiliary.Concept.ALEJANDRO_RODRIGUEZ.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("NHS")) {
    		return ArchitectonicAuxiliary.Concept.NHS.getPrimoridalUid();
    	} else if (modeler.equalsIgnoreCase("phernan")) {
    		return ArchitectonicAuxiliary.Concept.PENNI_HERNANDEZ.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("jrogers")) {
    		return ArchitectonicAuxiliary.Concept.JEREMY_ROGERS.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("edcheetham")) {
    		return ArchitectonicAuxiliary.Concept.ED_CHEETHAME.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("bgoldberg")) {
    		return ArchitectonicAuxiliary.Concept.BRUCE_GOLDBERG.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("jcase")) {
    		return ArchitectonicAuxiliary.Concept.JAMES_CASE.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("crichardson")) {
    		return ArchitectonicAuxiliary.Concept.CATHY_RICHARDSON.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("kfung")) {
    		return ArchitectonicAuxiliary.Concept.KIN_WAH_FUNGA.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("rhausam")) {
    		return ArchitectonicAuxiliary.Concept.ROBERT_HAUSAM.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("sshayegani")) {
    		return ArchitectonicAuxiliary.Concept.SHAPOOR_SHAYEGANI.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("igreen")) {
    		return ArchitectonicAuxiliary.Concept.IAN_GREEN.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("hmuys")) {
    		return ArchitectonicAuxiliary.Concept.HERMAN_MUYS.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("gwade")) {
    		return ArchitectonicAuxiliary.Concept.GERALDINE_WADE.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("jhowarth")) {
    		return ArchitectonicAuxiliary.Concept.JANE_HOWARTH.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("pamos")) {
    		return ArchitectonicAuxiliary.Concept.PAUL_AMOS.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("ysgao")) {
    		return ArchitectonicAuxiliary.Concept.YONG_SHENG_GAO.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("mbraithwaite")) {
    		return ArchitectonicAuxiliary.Concept.MARIA_BRAITHWAITE.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("mharry")) {
    		return ArchitectonicAuxiliary.Concept.MONICA_HARRY.getPrimoridalUid();
    	}else if (modeler.equalsIgnoreCase("kcampbell")) {
    		return ArchitectonicAuxiliary.Concept.KEITH_CAMPBELL.getPrimoridalUid();
    	}
    	
    	return null;
    }
    	
	public  UUID lookupAction(String action) throws TerminologyException, IOException {
		if (action.equalsIgnoreCase("Accept workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ACCEPT_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Chief Terminologist review workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Commit workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Commit in batch workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_COMMIT_IN_BATCH_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Discuss workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSS_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Escalate workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATE_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Review workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_ACTION.getPrimoridalUid();
		} else if (action.equalsIgnoreCase("Override workflow action")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_OVERRIDE_ACTION.getPrimoridalUid();
		} else {
			return null;
		} 
				
	}

	public  UUID lookupState(String state) throws TerminologyException, IOException {
		if (state.equalsIgnoreCase("Approved workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_APPROVED_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Changed workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Changed in batch workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHANGED_IN_BATCH_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For Chief Terminologist review workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CHIEF_TERMINOLOGIST_REVIEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Initial history workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_INITIAL_HISTORY_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Concept creation workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_CONCEPT_CREATION_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("Escalated workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_ESCALATED_STATE.getPrimoridalUid();
		} else if ((state.equalsIgnoreCase("New workflow state")) || (state.equalsIgnoreCase("first review")))  {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_NEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For review workflow state") || state.equalsIgnoreCase("review chief term")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_REVIEW_STATE.getPrimoridalUid();
		} else if (state.equalsIgnoreCase("For discussion workflow state")) {
			return ArchitectonicAuxiliary.Concept.WORKFLOW_DISCUSSION_STATE.getPrimoridalUid();
		} else {
			return null;		
		}
	}
}