/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.mojo.db;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.tk.Ts;

/**
 *
 * @goal vodb-close
 *
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbClose extends AbstractMojo {

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;
    /**
     * watch concepts
     *
     * @parameter
     */
    private List<ConceptDescriptor> watchConcepts;

    public void execute() throws MojoExecutionException, MojoFailureException {
        I_ImplementTermFactory termFactoryImpl = (I_ImplementTermFactory) Terms.get();
        try {
            try {
                getLog().info("Watches: " + watchConcepts);
                if (watchConcepts != null) {
                    for (ConceptDescriptor cd : watchConcepts) {
                        Concept c =
                                (Concept) Ts.get().getConcept(UUID.fromString(cd.getUuid()));

                        getLog().info("Watch: " + c.toLongString());
                    }
                }
                if (MojoUtil.alreadyRun(getLog(), "VodbClose", this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            termFactoryImpl.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }
}
