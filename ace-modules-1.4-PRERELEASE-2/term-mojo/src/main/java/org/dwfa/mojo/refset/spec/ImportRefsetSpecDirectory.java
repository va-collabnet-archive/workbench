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
package org.dwfa.mojo.refset.spec;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.file.TupleFileUtil;
import org.dwfa.maven.MojoUtil;

/**
 * Imports all the refset specs in a specified directory.
 * 
 * @goal import-refset-spec-directory
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */

public class ImportRefsetSpecDirectory extends AbstractMojo {

    /**
     * The refset spec directory.
     * 
     * @parameter default-value=
     *            "${project.build.directory}/generated-resources/refsetspec/"
     */
    File dir;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + dir.getCanonicalPath(),
                this.getClass(), targetDirectory)) {
                return;
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        try {
            TupleFileUtil tupleImporter = new TupleFileUtil();
            if (!dir.isDirectory()) {
                throw new Exception("Directory has not been configured : " + dir.getPath());
            } else {
                getLog().info("Importing refset specs from " + dir.getPath());
                for (File f : dir.listFiles()) {
                    getLog().info("Beginning import of refset spec :" + f.getPath());
                    // tupleImporter.importFile(f);
                    getLog().info("Finished importing refset spec from " + f.getPath());
                }

                LocalVersionedTerminology.get().commit();
            }

        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }
}
