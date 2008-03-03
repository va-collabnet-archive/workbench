package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.RefSetConflictValidator;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;


/**
 * Validates a specified reference set.
 * Illegal for a concept to inherit conflicting rules from parents.
 * e.g. if one parent says include children, and other parent has
 * exclude children.
 * @author Christine Hill
 *
 */

/**
 *
 * @goal vodb-validate-ref-set
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbValidateRefSet extends AbstractMojo {

    public final int ROOT = -1;
    public final int NO_TYPE_DEFINED = 0;
    public final int CONFLICT = -2;
    public final int VALID = -3;

    /**
     * @parameter
     * The concept descriptor for the ref set spec.
     */
    private ConceptDescriptor refSetSpecDescriptor;


    /**
     * Location to write list of conflicts - uuid and fsn.
     * @parameter expression="${project.build.directory}/conflicts.txt"
     * @required
     */
    private File conflictsOutputFile;

   
    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
    	
    	try{
    		try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName())) {
					return;
	            }
	        } catch (NoSuchAlgorithmException e) {
	        	throw new MojoExecutionException(e.getLocalizedMessage(), e);
	        } 
    		
	        RefSetConflictValidator validator = null;
	        
	        if(refSetSpecDescriptor == null){
	        	validator = new RefSetConflictValidator();
	        }
	        else{
	        	I_GetConceptData specConcept = refSetSpecDescriptor.getVerifiedConcept();
	        	validator = new RefSetConflictValidator(specConcept);
	        }
	        
    		
    		getLog().info("Checking for conflicts...");
    		validator.validate();
    		    	  
    		BufferedWriter writer = new BufferedWriter(new FileWriter(conflictsOutputFile));
    		if(!validator.hasConflicts()){
    			writer.write("No refset conflicts found.");
    			writer.close();
    		}
    		else{
    			I_TermFactory termFactory = LocalVersionedTerminology.get();
    			
    			getLog().info("Conflicts found!");
    			
    			HashMap<Integer, RefSetConflictValidator.Conflicts> conflictedConcepts = validator.getConflicts();
    			
    			
    			
    			writer.write("While verifying ref set specs [" + conflictedConcepts.size() +
                  			 "] concepts were found to have conflicts.");
    			writer.newLine();
    			
    			
    			for(Integer conceptId : conflictedConcepts.keySet()){
    				I_GetConceptData concept = termFactory.getConcept(conceptId);
    				
    				StringBuffer stringBuffer = new StringBuffer();
    				
    				stringBuffer.append("Concept ");
    				stringBuffer.append(concept.getInitialText());

    				stringBuffer.append(" has the following conflicts:");
    				stringBuffer.append(System.getProperty("line.separator"));
    				    				
    				HashMap<Integer, Integer> conflicts = conflictedConcepts.get(conceptId).getConflictDetails(); 
    				for(Integer parentConceptId : conflicts.keySet()){
    					I_GetConceptData parentConcept = termFactory.getConcept(parentConceptId);
    					
    					int refsetTypeId = conflicts.get(parentConceptId);
    					I_GetConceptData refSetTypeConcept = termFactory.getConcept(refsetTypeId);
    					
    					stringBuffer.append("\t Parent concept ");
    					stringBuffer.append(parentConcept.getInitialText());
    					stringBuffer.append(" is using ");
    					stringBuffer.append(refSetTypeConcept.getInitialText());
    					stringBuffer.append(" inheritance.");
    					stringBuffer.append(System.getProperty("line.separator"));
    					
    				}//End inner for loop
    				stringBuffer.append(System.getProperty("line.separator"));
    				stringBuffer.append("\t \t ............... ");
    				stringBuffer.append(System.getProperty("line.separator"));
    				
    				writer.write(stringBuffer.toString());
    				writer.newLine();
 
    			}//End outer for loop
    			writer.close();
    			getLog().info("Conflicts logged in " + conflictsOutputFile.getAbsolutePath());
    			throw new ConflictFailure("Conflicts found during ref set spec verification. See report file for more details.");
    		}
    		
    	}
    	catch(ConflictFailure e){
    		throw new MojoExecutionException( e.getMessage() );
    	}	
    	catch(TerminologyException e){
    		throw new MojoExecutionException( e.getMessage() );
    	}
    	catch(IOException e){
    		throw new MojoExecutionException( e.getMessage() );
    	}
    	catch(Exception e){
    		throw new MojoExecutionException( e.getMessage() );
    	}
    }//End method execute
    
    public ConceptDescriptor getRefSetSpecDescriptor() {
        return refSetSpecDescriptor;
    }

    public void setRefSetSpecDescriptor(ConceptDescriptor descriptor) {
        this.refSetSpecDescriptor = descriptor;
    }

    /*
	 * Custom exception so we can exit and notify of file comparison failure
	 */
	private class ConflictFailure extends Exception
	{
	   public ConflictFailure( String message )
	   { 
	      super( message );
	   }
	}//End class ComparisonFailure
}//End class VodbValidateRefSet
