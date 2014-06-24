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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;

/**
 *This goal will add all the given concept as path.
 * 
 * @goal vodb-create-new-path-from-exist-concept
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 * @author Ming Zhang
 * 
 */

public class VodbCreateNewPathFromParentConcept extends AbstractMojo {

    /**
     * Path origins
     * 
     * @parameter
     */
    SimpleUniversalAcePosition[] origins;

    /**
     * Parent of the new pathes.
     * 
     * @parameter
     * @required
     */
    private ConceptDescriptor pathConcept;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        // Use the architectonic branch for all path editing.
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + pathConcept.getDescription(),
                    this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            I_TermFactory tf = Terms.get();
            Set<PositionBI> pathOrigins = null;
            if (origins != null) {
                pathOrigins = new HashSet<PositionBI>(origins.length);
                for (SimpleUniversalAcePosition pos : origins) {
                    PathBI originPath = tf.getPath(pos.getPathId());
                    pathOrigins.add(tf.newPosition(originPath, pos.getTime()));
                }
            }
            I_GetConceptData path = pathConcept.getVerifiedConcept();

            tf.newPath(pathOrigins, path);

        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (ParseException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
