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
package org.dwfa.mojo.refset;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.refset.MemberRefsetCalculator;
import org.dwfa.mojo.ConceptDescriptor;

/**
 * 
 * @author Tore Fjellheim
 *
 * @goal createMemberRefset
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class CreateMemberRefset extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter
     * @required
     * The concept descriptor for the member set path.
     */
    private ConceptDescriptor memberSetPathDescriptor;

    /**
     * @parameter
     * The number of items to add to the uncommitted list before committing
     */
    private int commitSize = 1000;

    /**
     * @parameter
     * Use the direct non-transaction ACE API (defaults to false)
     */
    private boolean useNonTxInterface = false;

    /**
     * Output location for the change sets
     *
     * @parameter
     * @required
     */
    private File changeSetOutputDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {

            MemberRefsetCalculator calc = new MemberRefsetCalculator();
            calc.setOutputDirectory(outputDirectory);
            calc.setChangeSetOutputDirectory(changeSetOutputDirectory);
            calc.setPathConcept(memberSetPathDescriptor.getVerifiedConcept());
            calc.setValidateOnly(false);
            calc.setCommitSize(commitSize);
            calc.setUseNonTxInterface(useNonTxInterface);
            calc.run();
        } catch (Exception e) {
            throw new MojoExecutionException(
                "member refset calculation failed with exception", e);
        }

    }
}
