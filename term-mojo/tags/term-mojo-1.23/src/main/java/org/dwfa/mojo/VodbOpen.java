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

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

/**
 * 
 * @goal vodb-open
 * 
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbOpen extends AbstractMojo {

    /**
     * Location of the vodb directory.
     * 
     * @parameter default-value="${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    File vodbDirectory;

    /**
     * True if the database is readonly.
     * 
     * @parameter 
     */
    Boolean readOnly = false;

    /**
     * Size of cache used by the database.
     * 
     * @parameter 
     */
    Long cacheSize = 600000000L;

    /**
     * Use existing if it is already open
     * 
     * @parameter 
     * 
     */
    boolean useExistingDb = false;

    /**
     * @parameter 
     */
    private DatabaseSetupConfig dbSetupConfig;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (useExistingDb && LocalVersionedTerminology.get() != null) {
                return;
            }
            try {
                if (MojoUtil.alreadyRun(getLog(), vodbDirectory
                    .getCanonicalPath())) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            if (dbSetupConfig == null) {
                dbSetupConfig = new DatabaseSetupConfig();
            }
            LocalVersionedTerminology.createFactory(vodbDirectory, readOnly,
                cacheSize, dbSetupConfig);
        } catch (InstantiationException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IllegalAccessException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }

}
