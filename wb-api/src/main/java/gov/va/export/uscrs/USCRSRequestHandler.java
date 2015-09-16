package gov.va.export.uscrs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * Open todo list
 *  1) request id & globalRequestCounter?
 *  5) Final comparison against Vas's stuff

 *  3) Mojo
 *  6) Find US EXtension Module-->Done
 *  7) Multithreaded--> Non-isse?
 */
public class USCRSRequestHandler implements I_ProcessConcepts {
	/** logging */
	private static final Logger LOG = LoggerFactory.getLogger(USCRSRequestHandler.class);

	/** The request id counter. */
	private static AtomicInteger globalRequestCounter = new AtomicInteger(1);

	private USCRSInitiator initiator = null;
	private USCRSProcessor processor;
	private ViewCoordinate vc;


	private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
	private final long emptyPreviousExportTime = -1;
	private long previousRequestedExportTime;
	private String fileDateStamp;
	private String filename;
	private boolean isFromExport = false;
	private boolean badSetup = false;
	private ViewCoordinate configVC;

	private final int MAX_EXAMINED_CONCEPT_COUNT = 0;
	private final int MAX_PROCESSED_CONCEPT_COUNT = 60000;
	private int processedConcepts = 0;
	private int examinedConCount = 0;

	private Set<Integer> conceptsToInvestigate = null;
	
	/* 
	 * This case used for single concept processing via the Programmers Popup Menu
	 */
	public USCRSRequestHandler(I_GetConceptData concept)  {
		int namespace = getNamespace();

		if (namespace > 0) {
			Date previousReleaseDate = null;
			
			File outputPath = getOutputDirectory();
	
			if (outputPath == null) {
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Need Output Path",
						"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			} else {
	        	try {
	        		previousReleaseDate = getPreviousReleaseDate();
				} catch (ParseException e) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Incorrectly entered date: " + previousReleaseDate + ". Relaunch process and enter proper date",
							"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
					return;
				}
		
		        if (previousReleaseDate != null) {
		        	setup(previousReleaseDate.getTime(), outputPath, namespace);
		        } else {
		        	setup(emptyPreviousExportTime, outputPath, namespace);
		        }
		        
		        if (badSetup) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "General Database Error in this process",
							"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
		        } else {
			        try {
						processConcept(concept);
				        completeProcess();
					} catch (Exception e) {
						JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Error in processing concept: " + concept.getPrimUuid() + "/" + concept.getConceptNid() + " with error: " + e.getCause(),
								"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
						return;
					}
		        }		        
			}
		}
	}

	
	/* 
	 * This case used during release process where variable is not specified
	 */
	public USCRSRequestHandler(ViewCoordinate vc, File outputPath, int namespace)  {
		isFromExport = true;
		configVC = vc;
		setup(emptyPreviousExportTime, outputPath, namespace);
	}
	
	/* 
	 * This case used during release process
	 */
	public USCRSRequestHandler(ViewCoordinate vc, File outputFile, int namespace, long previousExportTime)  {
		isFromExport = true;
		configVC = vc;
		setup(previousExportTime, outputFile, namespace);
	}
	

	public void setConceptsToInvestigate(Set<Integer> nids) {
		conceptsToInvestigate  = nids;
	}
	
	private void setup(long previousExportTime, File outputPath, int namespace) {
		previousRequestedExportTime = previousExportTime;
		
		try {
			setupVC();
			initiator = new USCRSInitiator(vc);

			// create filename Date
			fileDateStamp = new SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(new Date());
			filename = "VA_USCRS_Submission_File_" + fileDateStamp + ".xls";
			outputPath.mkdirs();
			File outputFile = new File(outputPath + "\\" + filename);		

			setupInitiator(namespace, outputFile);
		} catch (Exception e) {
			badSetup = true;
		}	
		
	}

	
	private int getNamespace() {
		String response = JOptionPane.showInputDialog ( "Enter the namespace identifier to use during export (must be an integer)" );
		try {
			if (response == null || response.isEmpty()) {
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Must specify an integer for the namespace in order to move forward.  Value entered: " + response,
						"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
				return -1;
			}
			
			int namespace = Integer.parseInt (response);
			if (namespace <= 0) {
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Namespace must be a positive integer.  Value entered: " + response,
						"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
				return -1;
			} 
			
			return namespace;
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Must specify an integer for the namespace in order to move forward.  Value entered: " + response,
					"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			return -1;
		}
	}
	
	private Date getPreviousReleaseDate() throws ParseException {
		int haveExportTime = JOptionPane.showConfirmDialog(
	            null,
	            "Do you only want to identify changes against a previous release date?",
	            "Previous Release Time",
	            JOptionPane.YES_NO_OPTION);

        if (haveExportTime == JOptionPane.YES_OPTION) {
    		String exportTimeString = JOptionPane.showInputDialog(
    	            null,
    	            "Enter the export time to use during export (in mm/dd/yyyy format) or press cancel to continue without a previous date",
    	            "Enter Release Date",
    	            JOptionPane.YES_NO_OPTION);
    		
	        if(exportTimeString != null && !exportTimeString.isEmpty()) {
	        	return sdf.parse(exportTimeString);
	        }
        }
		return null;
	}

	private File getOutputDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select folder");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(null) == JFileChooser.OPEN_DIALOG) {
			return chooser.getSelectedFile();
		}
		return null;
	}

	private void setupInitiator(int namespace, File outputFile) throws IOException {
		LOG.debug("Create Batch Request File");

		// Ideally this would connect to a request submission instance and dynamically create the request. In lieu
		// of that, we simply create a spreadsheet.

		// Create workbook
		File f = new File("documents/USCRS_Batch_Template-2015-01-27.xls");

		InputStream is = new FileInputStream(f);
		USCRSBatchTemplate bt = new USCRSBatchTemplate(is);

		UscrsContentRequestTrackingInfo info = new UscrsContentRequestTrackingInfo();
		processor = new USCRSProcessor(bt, info, outputFile, vc, namespace);
	}

	private void setupVC() throws TerminologyException, IOException  {
		LOG.debug("Initialize View Coordinate");

		NidSetBI allowedStatuses;
		
		// Create new VC
		if (configVC == null) {
			configVC = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
			allowedStatuses = configVC.getAllowedStatusNids();
		} else {
			allowedStatuses = configVC.getAllowedStatusNids();
		}
		
		vc = new ViewCoordinate(configVC);
		PositionBI[] currentPosSet = configVC.getPositionSet().getPositionArray();
		
		if (currentPosSet.length > 1) {
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Cannot have more than one View Position for this process",
					"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
		}
		
		// Update VC's Position Time
		PositionBI viewPos = currentPosSet[0];
		
		PositionBI newViewPos = Terms.get().newPosition(viewPos.getPath(), System.currentTimeMillis());
        Set<PositionBI> pSet = new HashSet<PositionBI>();
        pSet.add(newViewPos);
        PositionSetBI posSet = new PositionSet(pSet);

		vc.setPositionSet(posSet);
		
		vc.setAllowedStatusNids(allowedStatuses);
		vc.setRelationshipAssertionType(RelAssertionType.STATED);


	}

	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {
/*		
 * if (!concept.getPrimUuid().equals(UUID.fromString("1b475397-3c49-3222-b27f-94016283e99d"))) {
			return;
		}
		
*/		
		if(examinedConCount++  % 50000 == 0) { //TODO: Modify this based on what options are passed in above
			LOG.debug("Uscrs Content Request Exported " + examinedConCount + " concepts");
			System.out.println("Uscrs Content Request Exported " + examinedConCount + " concepts");
		}
		
		if (conceptsToInvestigate != null && !conceptsToInvestigate.contains(concept.getNid())) {
			return;
		}
		
		if ((MAX_EXAMINED_CONCEPT_COUNT > 0 && examinedConCount > MAX_EXAMINED_CONCEPT_COUNT) || 
				(MAX_PROCESSED_CONCEPT_COUNT > 0 && processedConcepts > MAX_PROCESSED_CONCEPT_COUNT)) {
				return;
			}

		if(examinedConCount  % 500 == 0) { //TODO: Modify this based on what options are passed in above
			processor.getBt().saveFile(processor.getInfo().getFile());
		}

		ConceptVersionBI con = Ts.get().getConceptVersion(vc, concept.getConceptNid());

		if (con == null)
		{
			if (!isFromExport) {
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Unable to load concept for " + concept.getPrimUuid(),
						"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			} else {
				LOG.debug("Unable to load concept for " + concept.getPrimUuid());
			}
			return;
		}
		
		// Actual processing of concepts
		try {
			if (initiator.examineConcept(concept, previousRequestedExportTime) == true) {
				processedConcepts ++;
			}
		} catch (Exception e) {
			if (!isFromExport) {
				JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "Failed in looking at " + concept.getPrimUuid(),
						"USCRS Content Request", JOptionPane.ERROR_MESSAGE);
			} else {
				LOG.debug("Failed in looking at " + concept.getPrimUuid());
			}
		}
		
	}
	
	public void completeProcess() {
		LOG.info("USCRS Export Operation Successful. Processed " + processedConcepts + " concepts while examined " + examinedConCount + " concepts");
		System.out.println("USCRS Export Operation Successful. Processed " + processedConcepts + " concepts while examined " + examinedConCount + " concepts");

		if (!isFromExport && processor.getInfo().isSuccessful())
		{
			JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null),
					"Content request submission successful.\n\nUpload " + processor.getInfo().getFile().getAbsolutePath() + " to here: " + processor.getInfo().getUrl(),
					"USCRS Content Request",
					JOptionPane.INFORMATION_MESSAGE);
		}

		try {
			processor.getBt().saveFile(processor.getInfo().getFile());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean failedSetupStatus() {
		return badSetup;
	}
}
