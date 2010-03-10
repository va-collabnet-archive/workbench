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
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.ihtsdo.mojo.maven.MojoUtil;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type5UuidFactory;

/**
 * 
 * @goal vodb-set-edit-paths
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbSetEditPaths extends AbstractMojo {

    /**
     * Editing path UUIDs
     * 
     * @parameter
     * @required
     */
    private List<ConceptDescriptor> editPaths;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            try {
                if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + editPaths, this.getClass(),
                    targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            I_ConfigAceFrame activeConfig = LocalVersionedTerminology.get().getActiveAceFrameConfig();
            I_TermFactory tf = LocalVersionedTerminology.get();

            activeConfig.getEditingPathSet().clear();
            for (ConceptDescriptor pathConcept : editPaths) {
                if (pathConcept.getUuid() == null) {
                    pathConcept.setUuid(Type5UuidFactory.get(Type5UuidFactory.PATH_ID_FROM_FS_DESC,
                        pathConcept.getDescription()).toString());
                }
                activeConfig.addEditingPath(tf.getPath(pathConcept.getVerifiedConcept().getUids()));
            }
        } catch (TerminologyException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

    public List<ConceptDescriptor> getEditPaths() {
        return editPaths;
    }

    public void setEditPaths(List<ConceptDescriptor> editPaths) {
        this.editPaths = editPaths;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}
