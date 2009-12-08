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
package org.dwfa.mojo.refset.migrate;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConcept;
import org.dwfa.ace.api.ebr.I_ThinExtByRefTuple;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.refset.ConceptConstants;
import org.dwfa.ace.refset.MemberRefsetHelper;
import org.dwfa.ace.task.profile.NewDefaultProfile;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * @goal retire-all-marked-parents
 */
public class RetireAllMarkedParents extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    public ConceptDescriptor editPath;

    protected I_TermFactory termFactory;

    protected HashMap<String, I_GetConceptData> concepts = new HashMap<String, I_GetConceptData>();

    public RetireAllMarkedParents() throws Exception {
        termFactory = LocalVersionedTerminology.get();
        if (termFactory == null) {
            throw new RuntimeException("The LocalVersionedTerminology is not available. Please check the database.");
        }
    }

    public void init() throws Exception {
        concepts.put("CURRENT", termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid()));
        concepts.put("RETIRED", termFactory.getConcept(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()));
        concepts.put("PARENT_MARKER", termFactory.getConcept(ConceptConstants.PARENT_MARKER.localize().getNid()));
        concepts.put("NORMAL_MEMBER",
            new ConceptDescriptor("cc624429-b17d-4ac5-a69e-0b32448aaf3c", "normal member").getVerifiedConcept());

        I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
        if (config == null) {
            config = NewDefaultProfile.newProfile(null, null, null, null, null);
            termFactory.setActiveAceFrameConfig(config);
        }
        config.getEditingPathSet().clear();
        config.addEditingPath(termFactory.getPath(editPath.getVerifiedConcept().getUids()));

        config.setViewPositions(null);
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            init();
            Set<Integer> memberRefsets = MemberRefsetHelper.getMemberRefsets();

            for (Integer memberRefsetId : memberRefsets) {
                I_GetConceptData memberRefsetConcept = termFactory.getConcept(memberRefsetId);
                retireExistingMarkedParentMembers(memberRefsetConcept);
            }
        } catch (Exception ex) {
            throw new MojoExecutionException("Unable to migrate specification refsets", ex);
        }

    }

    private void retireExistingMarkedParentMembers(I_GetConceptData memberRefsetConcept) throws Exception {

        int refsetId = memberRefsetConcept.getConceptId();

        List<I_ThinExtByRefVersioned> extVersions = termFactory.getRefsetExtensionMembers(refsetId);

        for (I_ThinExtByRefVersioned thinExtByRefVersioned : extVersions) {

            List<I_ThinExtByRefTuple> extensions = thinExtByRefVersioned.getTuples(null, null, true, false);

            for (I_ThinExtByRefTuple thinExtByRefTuple : extensions) {
                if (thinExtByRefTuple.getRefsetId() == refsetId) {

                    I_ThinExtByRefPartConcept part = (I_ThinExtByRefPartConcept) thinExtByRefTuple.getPart();
                    if (part.getConceptId() == concepts.get("PARENT_MARKER").getConceptId()
                        && part.getStatusId() == concepts.get("CURRENT").getConceptId()) {

                        I_ThinExtByRefPart clone = part.duplicate();
                        clone.setStatusId(concepts.get("RETIRED").getConceptId());
                        clone.setVersion(Integer.MAX_VALUE);
                        thinExtByRefVersioned.addVersion(clone);

                        termFactory.addUncommitted(thinExtByRefVersioned);
                    }
                }
            }
        }
    }

}
