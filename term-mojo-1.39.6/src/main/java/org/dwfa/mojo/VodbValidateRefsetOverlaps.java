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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.RefsetOverlapValidator;
import org.dwfa.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;

/**
 *
 * @goal vodb-validate-refset-overlaps
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class VodbValidateRefsetOverlaps extends AbstractMojo {

    /**
     * First refset descriptor to use for overlap validation
     * @parameter
     * @optional
     */
    private ConceptDescriptor refSetSpecDescriptor1;

    /**
     * Second refset descriptor to use for overlap validation
     * @parameter
     * @optional
     */
    private ConceptDescriptor refSetSpecDescriptor2;

    /**
     * Location to write list of conflicts - uuid and fsn.
     * @parameter expression="${project.build.directory}/refsetOverlaps.txt"
     * @required
     */
    private File overlapFile;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
		try{
			
			System.out.println("vodb-validate-refset-overlaps...........................................");
			
    		try {
				if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName(), 
						this.getClass(), targetDirectory)) {
					return;
	            }
	        } catch (NoSuchAlgorithmException e) {
	        	throw new MojoExecutionException(e.getLocalizedMessage(), e);
	        } 
	        
	        /*
	         * Perform validation
	         */
	        boolean compareAll = (refSetSpecDescriptor1 == null || refSetSpecDescriptor2 == null) ? true:false;
	        
	        I_TermFactory termFactory = LocalVersionedTerminology.get();
	        
	        RefsetOverlapValidator rov = new RefsetOverlapValidator();
	        if(!compareAll){
	        	rov.setFirstRefsetId(refSetSpecDescriptor1.getVerifiedConcept().getConceptId());
	        	rov.setSecondRefsetId(refSetSpecDescriptor2.getVerifiedConcept().getConceptId());
	        }
			rov.validate();
						
			if(rov.hasOverlaps()){
				BufferedWriter writer = new BufferedWriter(new FileWriter(overlapFile));
				
				writer.write("The following meber sets have been found to have refset ocverlaps:");
				writer.newLine();
				for(Integer i : rov.getOverlapedMemberComopnentIds()){
					writer.write(termFactory.getConcept(i.intValue()).getInitialText());
					writer.newLine();
				}//End for loop	
			}//End if
	        
		}
		catch(TerminologyException e){ throw new MojoExecutionException( e.getMessage() ); }
		catch(IOException e){ throw new MojoExecutionException( e.getMessage() ); }
		catch(Exception e){ throw new MojoExecutionException( e.getMessage() ); }
	}//End method execute
}//End class 
