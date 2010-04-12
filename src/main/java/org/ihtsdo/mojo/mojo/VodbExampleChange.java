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
package org.ihtsdo.mojo.mojo;

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Flag a concept to demonstrate how to change make changes to components...
 * 
 * @goal vodb-example-change
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbExampleChange extends AbstractMojo {

    /**
     * Location of the directory to output data files to.
     * KEC: I added this field, because the maven plugin plugin would
     * crash unless there was at least one commented field. This field is
     * not actually used by the plugin.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    @SuppressWarnings("unused")
    private String outputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_TermFactory termFactory = Terms.get();
        try {
            I_GetConceptData architectonicRoot = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_ROOT_CONCEPT.getUids());
            I_ConceptAttributeVersioned conceptAttributes = architectonicRoot.getConceptAttributes();
            I_GetConceptData architectonicBranch = termFactory.getConcept(ArchitectonicAuxiliary.Concept.ARCHITECTONIC_BRANCH.getUids());
            I_Path architectonicPath = termFactory.getPath(architectonicBranch.getUids());
            I_Position latestOnArchitectonicPath = termFactory.newPosition(architectonicPath, Integer.MAX_VALUE);
            Set<I_Position> positions = new HashSet<I_Position>();
            positions.add(latestOnArchitectonicPath);
            I_GetConceptData flaggedStatus = termFactory.getConcept(ArchitectonicAuxiliary.Concept.FLAGGED_FOR_REVIEW.getUids());
            // TODO replace with passed in config...
            I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();

            for (I_ConceptAttributeTuple tuple : architectonicRoot.getConceptAttributeTuples(null, new PositionSetReadOnly(positions), 
                config.getPrecedence(), config.getConflictResolutionStrategy())) {
                I_ConceptAttributePart part = (I_ConceptAttributePart) tuple.makeAnalog(flaggedStatus.getConceptId(), tuple.getPathId(), Long.MAX_VALUE);

                conceptAttributes.addVersion(part);
                termFactory.addUncommitted(architectonicRoot);
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
