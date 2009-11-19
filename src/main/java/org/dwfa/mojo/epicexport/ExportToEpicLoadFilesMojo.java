package org.dwfa.mojo.epicexport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.I_ConfigAceFrame.LANGUAGE_SORT_PREF;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartInteger;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartString;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.maven.MojoUtil;
import org.dwfa.mojo.ConceptDescriptor;
import org.dwfa.mojo.PositionDescriptor;
import org.dwfa.mojo.epicexport.kp.EpicLoadFileFactory;
import org.dwfa.mojo.refset.ExportSpecification;
// import org.dwfa.mojo.refset.RefsetType;
//import org.dwfa.mojo.vivisimo.IntSet;
//import org.dwfa.mojo.vivisimo.GenerateVivisimoThesaurus.ExportIterator;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;


/**
 * * GenerateVivisimoThesaurus <br/>
 * <p>
 * The <code>ExportToEpicLoadFilesMojo</code> class generates Epic load files
 * used to populate and update the Epic terminology master files.
 * </p>
 * <p>
 * </p>
 *
 *
 *
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Steven Neiner
 * @goal generate-epic-loadfiles
 */
public class ExportToEpicLoadFilesMojo extends AbstractMojo {
	/**
	 * Location of the directory to output data files to.
	 *
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String outputDirectory;
	
	/**
	 * If true, do not create thesaurus entries for preferred terms with no synonyms.
	 *
	 * @parameter
	 * @required
	 */
	private ExportSpecification[] specs;

	/**
	 * Positions to export data.
	 *
	 * @parameter
	 * @required
	 */
	private PositionDescriptor[] positionsForExport;

	/**
	 * Status values to include in export
	 *
	 * @parameter
	 * @required
	 */
	private ConceptDescriptor[] statusValuesForExport;

    /**
     * @parameter default-value="${project.build.directory}"
     * @required
     * @readonly
     */
     private File buildDirectory;

     /**
      * Location of the build directory.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     private File targetDirectory;

	private HashSet<I_Position> positions;

	private I_IntSet statusValues;
	
	private I_TermFactory termFactory;
	
	//private HashMap<Integer, RefsetType> refsetTypeMap = new HashMap<Integer, RefsetType>();

     public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			if (MojoUtil.alreadyRun(getLog(), outputDirectory,
					this.getClass(), targetDirectory)) {
				return;
			}

			termFactory = LocalVersionedTerminology.get();

			positions = new HashSet<I_Position>(
					positionsForExport.length);
			for (PositionDescriptor pd : positionsForExport) {
				positions.add(pd.getPosition());
			}
			
			statusValues = termFactory.newIntSet();
			List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
			for (ConceptDescriptor status : statusValuesForExport) {
				I_GetConceptData statusConcept = status.getVerifiedConcept();
				statusValues.add(statusConcept.getConceptId());
				statusValueList.add(statusConcept);
			}
			getLog().info(
					" processing concepts for positions: " + positions
							+ " with status: " + statusValueList);

			if (outputDirectory.endsWith("/") == false) {
				outputDirectory = outputDirectory + "/";
			}
			
			/* File outputDirFile = new File(outputDirectory);
			outputDirFile.mkdirs();
			File dataFile = new File(outputDirectory
					+ dataFileName);
			outputDirFile.getParentFile().mkdirs();
			Writer dataWriter = new BufferedWriter(new FileWriter(
					dataFile));
			*/
			// getLog().info("About to iterate through concepts");
			ExportIterator expItr = new ExportIterator();
			
			
			I_GetConceptData concept = termFactory.getConcept(UUID.fromString("a7130b8c-e6c1-57d8-986a-0d88552c12e4"));
			expItr.processConcept(concept);
			
			expItr.close();
            // LocalVersionedTerminology.get().iterateConcepts(expItr);
			//dataWriter.write("</thesaurus>\n");

			//dataWriter.close();

		} catch (Exception e) {
			getLog().error(e);
			throw new MojoExecutionException("Unable to export database due to exception", e);
		}

	}// End method execute
     
    private class ExportIterator implements I_ProcessConcepts {

		private Writer dataWriter;
		// private IntSet preferred = new IntSet();
		// private IntSet synonym = new IntSet();
		private int maxWords;
		private int minWords;
		private String currentItem;
		
		private String currentMasterFile;
		private EpicLoadFileFactory exportFactory;
		private EpicExportManager exportManager;

		public ExportIterator()
		{
			exportFactory = new EpicLoadFileFactory();
			exportManager = exportFactory.getExportManager("c:/temp");
			
		}

		public void close() throws Exception {
			this.exportManager.close();
		}
		
		public void processConcept(I_GetConceptData concept) throws Exception {
			// getLog().info("Checking concept id " + concept.getId());
			//if (isExportable(concept)) {
				getLog().info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> Processing concept: " + concept);
				exportRefsetsForConcept(concept);
				// getLog().info("**** Tested concept id " + concept.getId());
			//}
			List<I_DescriptionVersioned> descriptions = concept.getDescriptions();
			for (Iterator<I_DescriptionVersioned> i = descriptions.iterator(); i.hasNext();) {
				I_DescriptionVersioned desc = i.next();
				//getLog().info(" Description: " + desc);
				List<I_DescriptionTuple> dts = desc.getTuples();
				for (Iterator<I_DescriptionTuple> ti = dts.iterator(); ti.hasNext();) {
					I_DescriptionTuple descTuple = ti.next();
					//getLog().info(" Description tuple: " + descTuple.getText());
					//getLog().info("Description tuple concept: " + termFactory.getConcept(descTuple.getConceptId()));
				}
				
			}
				
			
		}

	    private void exportRefsetsForConcept(I_GetConceptData concept) throws TerminologyException, Exception {
	        List<I_ThinExtByRefVersioned> extensions = termFactory.getAllExtensionsForComponent(concept.getConceptId());
	        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extensions) {
	        	if (termFactory.hasConcept(thinExtByRefVersioned.getRefsetId())) {
	                for (I_ThinExtByRefTuple thinExtByRefTuple : thinExtByRefVersioned.getTuples(statusValues,
	                    positions, false, false)) {
	                	getLog().info("getTypeid()=" + thinExtByRefVersioned.getTypeId());
	                	export(thinExtByRefTuple, concept);
	                }
	        	}else {
	        		throw new Exception("No concept for ID " + thinExtByRefVersioned.getRefsetId());
	        	}
	        }
	        I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(this.currentMasterFile);
	        exportWriter.writeRecord("version");
	        exportWriter.newExportRecord();
	    }

	    void export(I_ThinExtByRefTuple thinExtByRefTuple, I_GetConceptData parentConcept) throws Exception {
	    	getLog().info("thinExtByRefTuple = " + thinExtByRefTuple);	    	
	        export(thinExtByRefTuple.getPart(), thinExtByRefTuple.getMemberId(), thinExtByRefTuple.getRefsetId(),
	            thinExtByRefTuple.getComponentId(), parentConcept);
	    }

	    void export(I_ThinExtByRefPart thinExtByRefPart, Integer memberId, int refsetId, int componentId, I_GetConceptData parentConcept) throws Exception {
    	
            I_GetConceptData refsetConcept = termFactory.getConcept(refsetId);
            String refsetName = refsetConcept.getInitialText();
	       exportRefset(refsetName, refsetConcept, thinExtByRefPart, parentConcept);
	    }
	    
	    private boolean isExportable(I_GetConceptData concept) throws Exception {
			 for (ExportSpecification spec : specs) {
				if (spec.test(concept)) {
					return true;
				}
			}
			return true;
		}


	    boolean testSpecification(I_GetConceptData concept) throws Exception {
	        for (ExportSpecification spec : specs) {
	            if (spec.test(concept)) {
	                return true;
	            }
	        }
	        
	    	return true;
	    }
	    boolean testSpecification(int id) throws TerminologyException, IOException, Exception {
	        return testSpecification(termFactory.getConcept(id));
	    }

	    
	    public void exportRefset(String refsetName, I_GetConceptData concept, I_ThinExtByRefPart thinExtByRefPart, I_GetConceptData parentConcept) throws Exception {
	    	getLog().info("** exportRefset: refsetName=" + refsetName);
	    	this.setCurrentItem(null, null);
	    	String stringValue = null;
	    	if(refsetName.equals("EDG Billing Item 207")) {
	    		this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "207");
	    	}
	    	if(refsetName.equals("EDG Billing Item 2000")) {
	    		this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "2000");
	    	}
	    	if(refsetName.equals("EDG Billing Item 2")) {
	    		this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "2");
	    		stringValue = getDisplayName(parentConcept);
	    	}
	    	if(refsetName.equals("EDG Billing Item 40")) {
	    		this.setCurrentItem(EpicExportManager.EPIC_MASTERFILE_NAME_EDG_BILLING, "40");
	    	}
	    	
	    	if (this.currentItem != null) {
	    		if (stringValue == null)
	    			stringValue = getValueAsString(thinExtByRefPart);
	    		I_EpicLoadFileBuilder exportWriter = exportManager.getLoadFileBuilder(this.currentMasterFile);
	    		getLog().info("Exporting item " + this.currentItem + " with a value of " + stringValue);	    		
	    		exportWriter.sendItemForExport(this.currentItem, stringValue, null);
	    	}
	    }
	    
	    public String getValueAsString(I_ThinExtByRefPart thinExtByRefPart) {
	    	String value = null;
	    	if (I_ThinExtByRefPartString.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartString str = (I_ThinExtByRefPartString) thinExtByRefPart;
	    		value = str.getStringValue();
	    	}
	    	if (I_ThinExtByRefPartInteger.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartInteger str = (I_ThinExtByRefPartInteger) thinExtByRefPart;
	    		value = new Integer(str.getIntValue()).toString();
	    	}
	    	if (I_ThinExtByRefPartConcept.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartConcept str = (I_ThinExtByRefPartConcept) thinExtByRefPart;
	    		value = "I_ThinExtByRefPartConcept";
	    	}
	    	if (I_ThinExtByRefPartConceptString.class.isAssignableFrom(thinExtByRefPart.getClass())) {
	    		I_ThinExtByRefPartConceptString str = (I_ThinExtByRefPartConceptString) thinExtByRefPart;
	    		value = "I_ThinExtByRefPartConceptString";
	    	}
	    	
	    	return value;

	    }
	    public void setCurrentItem(String masterFile, String item) {
	    	this.currentMasterFile = masterFile;
	    	this.currentItem = item;
	    }
	    
	    public String getDisplayName(I_GetConceptData conceptData) throws Exception {
	    	String ret = null;
	
            I_ConfigAceFrame newConfig = NewDefaultProfile.newProfile("", "", "", "", "");
            //I_GetConceptData refsetConcept = termFactory.getConcept(UUID.fromString("6fd32c1f-8096-40a1-9053-1cc204bc61e3"));
            if (newConfig != null) {
            	I_DescriptionTuple it = conceptData.getDescTuple(newConfig.getLongLabelDescPreferenceList(), newConfig);
            	if (it == null)
            		ret = "?";
            	else
            		ret = it.getText();
            }
	    	return ret;
	    }

    }


}
