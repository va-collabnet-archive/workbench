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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_ProcessPaths;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.mojo.compare.CompareOperator;
import org.dwfa.mojo.compare.Match;
import org.dwfa.mojo.compare.MonitorComponents;

/**
 * Goal which monitors two branches for changes. Agreed changes are copied
 * to a new branch. Any encountered conflicts result in a html summary report
 * and text file containing a list of the conflicting concept identifiers.
 * 
 * Optionally can check for flagged concept status (exclude
 * components from being copied if they are flagged).
 * 
 * @goal vodb-create-paths-from-base
 * 
 * @requiresDependencyResolution compile
 * @author Tore Fjellheim
 */
public class VodbCreatePathsFromChildrenOfConcept extends AbstractMojo {

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
        I_TermFactory termFactory = LocalVersionedTerminology.get();
        try {

            List<I_Path> paths = termFactory.getPaths();
            for (I_Path path : paths) {
                I_GetConceptData pathConcept = termFactory.getConcept(path.getConceptId());
                List<I_RelVersioned> rels = pathConcept.getDestRels();
                for (int i = 0; i < rels.size(); i++) {
                    int c1 = rels.get(i).getC1Id();
                    int c2 = rels.get(i).getC2Id();
                    if (c1 != path.getConceptId() && c2 == path.getConceptId()) {
                        Set<I_Position> origins = new HashSet<I_Position>();
                        origins.add(termFactory.newPosition(path, Integer.MAX_VALUE));
                        termFactory.newPath(origins, termFactory.getConcept(c1));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
