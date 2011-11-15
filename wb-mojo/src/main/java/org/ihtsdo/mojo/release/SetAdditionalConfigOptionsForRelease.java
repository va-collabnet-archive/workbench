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
package org.ihtsdo.mojo.release;

import java.io.IOException;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * 
 * @goal set-additional-config-options-for-release
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class SetAdditionalConfigOptionsForRelease extends AbstractMojo {


    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            I_ConfigAceFrame aceConfig = Terms.get().getActiveAceFrameConfig();
            I_TermFactory tf = Terms.get();
            
            aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());//Fully specified name	
	
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_SYNONYM_NID());
		    
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());
			
			ConceptSpec definition = new ConceptSpec("Definition (core metadata concept)", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));
			aceConfig.getDescTypes().add(tf.uuidToNative(definition.getLenient().getUUIDs().get(0)));
			
			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());
			
			ConceptSpec isa = new ConceptSpec("Is a (attribute)", UUID.fromString(I_Constants.IS_A_UID));
			aceConfig.getDescTypes().add(tf.uuidToNative(isa.getLenient().getUUIDs().get(0)));
			aceConfig.setDefaultStatus(tf.getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID())); // Current
		
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_CURRENT_NID()); // Current
			
			aceConfig.getAllowedStatus().add(tf.uuidToNative(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"))); //Retired
			aceConfig.getAllowedStatus().add(tf.uuidToNative(UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"))); //Concept non-current (foundation metadata concept)
			aceConfig.getAllowedStatus().add(tf.uuidToNative(UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"))); //Pending
			aceConfig.getAllowedStatus().add(tf.uuidToNative(UUID.fromString("95028943-b11c-3509-b1c0-c4ae16aaad5c"))); //Component Moved elsewhere	900000000000487009
			
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_LIMITED_NID()); //Limited	900000000000486000
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid()); //Outdated	900000000000483008
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid()); //Erroneous component (foundation metadata concept)	900000000000485001
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid());  //Ambiguous component (foundation metadata concept)	900000000000484002
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid());  //Dups	900000000000482003
			
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());
			
			aceConfig.setPrecedence(Precedence.TIME);
            
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
