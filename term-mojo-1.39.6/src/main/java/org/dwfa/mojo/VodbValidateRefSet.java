/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.ClosestDistanceHashSet;
import org.dwfa.ace.refset.ConceptRefsetInclusionDetails;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.SNOMED;
import org.dwfa.maven.MojoUtil;


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
	 * @parameter
	 * The number of items to add to the uncommitted list before committing
	 */
	private int commitSize = 1000;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;
   
    /**
     * Iterates over each concept and calculates the member set.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
    	
    	try{
    		try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), 
						this.getClass(), targetDirectory)) {
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
    		mrc.setCommitSize(commitSize);
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

				ClosestDistanceHashSet newMembers = newRefsetMembers.get(refset);
				ClosestDistanceHashSet oldMembers = newRefsetExclusion.get(refset);
				
				Set<Integer> keySet = new HashSet<Integer>();
				keySet.addAll(newMembers.keySet());
				keySet.retainAll(oldMembers.keySet());
				
				for (Integer integer : keySet) {
					//Show only first level conflict
					I_IntSet isARel = termFactory.newIntSet();
					isARel.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
					isARel.add(termFactory.uuidToNative(SNOMED.Concept.IS_A.getUids()));

					List<Integer> addedConcepts = new ArrayList<Integer>();
					
					ConceptRefsetInclusionDetails old = oldMembers.get(integer);
					ConceptRefsetInclusionDetails newMember = newMembers.get(integer);
					
					for(I_GetConceptData c :termFactory.getConcept(newMember.getConceptId()).getSourceRelTargets(null, isARel, null, false)){
						int conceptId = c.getConceptId();
						if(conceptId == termFactory.getConcept(newMember.getInclusionReasonId()).getConceptId() ||
							conceptId == termFactory.getConcept(old.getInclusionReasonId()).getConceptId()){
							
							if(!addedConcepts.contains(new Integer(conceptId))){
							
								StringBuffer sb = new StringBuffer();
								sb.append(termFactory.getConcept(newMember.getConceptId()).toString());
								sb.append(" because of " + termFactory.getConcept(newMember.getInclusionReasonId()).toString());
								sb.append(" conflicts with " +termFactory.getConcept(old.getInclusionReasonId()).toString());
								
								conflictDetails.add(sb.toString());
								addedConcepts.add(new Integer(conceptId));
							}
						}
					}
				}

				conflicts = true;
    		}
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
