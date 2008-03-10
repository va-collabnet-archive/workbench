package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.bpa.process.Condition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
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
    		
	        
	        MyMemberRefsetCalculator mrc = new MyMemberRefsetCalculator();
    		
	        I_TermFactory termFactory = LocalVersionedTerminology.get();
	        List<Integer> allowedRefsets = new ArrayList<Integer>();
	        allowedRefsets.add(refSetSpecDescriptor.getVerifiedConcept().getConceptId());
	        mrc.setAllowedRefsets(allowedRefsets);
	        
	        //mrc.setOutputDirectory(conflictsOutputFile);
    		mrc.setValidateOnly(true);
    		mrc.run();
	            		
    		List<String> details = mrc.conflictDetails;
    		BufferedWriter writer = new BufferedWriter(new FileWriter(conflictsOutputFile));
    		for(String conflict : details){
    			writer.write(conflict);
    			writer.newLine();
    		}
	        
    		writer.close();
    		if(details.size() > 0){
    			getLog().info("Conflicts logged in " + conflictsOutputFile.getAbsolutePath());
    			throw new ConflictFailure("Conflicts found during ref set spec verification. See report file for more details.");
    		}

    	}
    	catch(ConflictFailure e){
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

class MyMemberRefsetCalculator extends MemberRefsetCalculator{
    	
    	private boolean conflicts = false;
    	protected List<String> conflictDetails = new ArrayList<String>();
    	
    	
    	public boolean hasConflicts(){
    		return conflicts;
    	}
    	
    	    	
    	protected void setMembers() throws Exception {
    		
    		for (Integer refset : newRefsetMembers.keySet()) {
				Set<ConceptRefsetInclusionDetails> exclusions = new HashSet<ConceptRefsetInclusionDetails>();

				conflictDetails.add("Conflicts in refset " + termFactory.getConcept(refset) + " are: ");

				Set<ConceptRefsetInclusionDetails> newMembers = newRefsetMembers.get(refset);
				Set<ConceptRefsetInclusionDetails> oldMembers = newRefsetExclusion.get(refset);
				if (newMembers!=null) {
					
					for (ConceptRefsetInclusionDetails i: newMembers) {					
						if (oldMembers!=null && oldMembers.contains(i)) {
							List<Integer> addedConcepts = new ArrayList<Integer>();
							for (ConceptRefsetInclusionDetails old: oldMembers) {
								//Show only first level conflict
								I_IntSet isARel = termFactory.newIntSet();
								isARel.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
								isARel.add(termFactory.uuidToNative(SNOMED.Concept.IS_A.getUids()));
								if (old.equals(i)) {
									for(I_GetConceptData c :termFactory.getConcept(i.getConceptId()).getSourceRelTargets(null, isARel, null, false)){
										int conceptId = c.getConceptId();
										if(conceptId == termFactory.getConcept(i.getInclusionReasonId()).getConceptId() ||
											conceptId == termFactory.getConcept(old.getInclusionReasonId()).getConceptId()){
											
											if(!addedConcepts.contains(new Integer(conceptId))){
											
												StringBuffer sb = new StringBuffer();
												sb.append(termFactory.getConcept(i.getConceptId()).toString());
												sb.append(" because of " + termFactory.getConcept(i.getInclusionReasonId()).toString());
												sb.append(" conflicts with " +termFactory.getConcept(old.getInclusionReasonId()).toString());
												
												conflictDetails.add(sb.toString());
												addedConcepts.add(new Integer(conceptId));
											}
										}
									}//End inner for loop :termFactory.getConcept(i.getConceptId()).getSourceRelTargets
								}
								//Show all levels conflicts
//								if (old.equals(i)) {							
//									StringBuffer sb = new StringBuffer();
//									sb.append(termFactory.getConcept(i.getConceptId()).toString());
//									sb.append(" because of " + termFactory.getConcept(i.getInclusionReasonId()).toString());
//									sb.append(" conflicts with " +termFactory.getConcept(old.getInclusionReasonId()).toString());
//									
//									conflictDetails.add(sb.toString());
//									
//								}
							}//End inner for loop :oldMembers
							conflicts = true;
						}
					}//End inner for loop :newMembers
				}
			}//End for loop
		}//End method setMembers
    }//End nested class MyMemberRefsetCalculator
    
    
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
