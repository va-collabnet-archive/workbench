package org.ihtsdo.workflow.refset.mojo.init;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;


/**
 * @author Jesse Efron
 * 
 */
  
public class InitializeEditorCategoryRefset implements I_InitializeWorkflowRefset {

    private static final int editorPosition = 0;
    private static final int semanticAreaPosition = 1;
    private static final int categoryPosition = 2;
    private static final int numberOfColumns = 3;

    private String fileName = "edCatRefset.txt";

    private EditorCategoryRefsetWriter writer = null;
    
    public InitializeEditorCategoryRefset()  
    {
        try {
            writer = new EditorCategoryRefsetWriter();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Unable to initialize semantic tag refset with error: " + e.getMessage());
		}
	}
    
    
   @Override
   public boolean initializeRefset(String resourceFilePath)  {
		String line = null;
        
        try {
    		File f = new File(resourceFilePath + File.separatorChar + fileName);
	    	BufferedReader inputFile = new BufferedReader(new FileReader(f));    	

        	while ((line = inputFile.readLine()) != null)
            {

        		if (line.trim().length() == 0) {
        			continue;
        		}

        		String[] columns = line.split("\t");

        		if (columns.length == numberOfColumns)
        		{
        			int ss = columns.length;
        			String s = columns[editorPosition];
        			UUID mod = lookupModeler(columns[editorPosition]);
        			writer.setEditor(mod);
	            	writer.setSemanticArea(columns[semanticAreaPosition]);
	            	writer.setCategory(lookupRole(columns[categoryPosition]));

	            	writer.addMember();
        		} else {
	            	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
    			}
            }

        	Terms.get().addUncommitted(writer.getRefsetConcept());

        	return true;
    	} catch (Exception e) {
        	AceLog.getAppLog().log(Level.WARNING, "Unable to import this row into editor category refset: " + line);
        	return false;
    	}
	}

   private I_GetConceptData getRole(String role) throws TerminologyException, IOException {
   		return Terms.get().getConcept(lookupRole(role));
   }

   private UUID lookupRole(String role) throws IOException, TerminologyException {
	   	if (role.equalsIgnoreCase("Clinical editor role A")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_A.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role B")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_B.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role C")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_C.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role D")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_D.getPrimoridalUid();
	   	} else if (role.equalsIgnoreCase("Clinical editor role All")) {
	   		return ArchitectonicAuxiliary.Concept.WORKFLOW_ROLE_ALL.getPrimoridalUid();
	   	}
	   	
	   	return null;
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
		} else if (modeler.equalsIgnoreCase("jogo")) {
			return ArchitectonicAuxiliary.Concept.JO_GOULDING.getPrimoridalUid();
		} else if (modeler.equalsIgnoreCase("sspence")) {
			return ArchitectonicAuxiliary.Concept.SAMANTHA_SPENCER.getPrimoridalUid();
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
		}
		
		return null;
    }
}