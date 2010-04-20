/**
 *  Copyright (c) 2009 International Health Terminology Standards Development Organisation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dwfa.mojo;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.maven.MojoUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.dwfa.builder.BuilderException;
import org.dwfa.builder.itermfactory.LocalVersionedTerminologyBuilder;

/**
 * @goal vodb-open
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class VodbOpen extends AbstractMojo {

    /**
     * Location of the vodb directory.
     *
     * Only required if <code>useExistingDb</code> is null or set to false
     *
    Not    * @parameter default-value=
     *            "${project.build.directory}/generated-resources/berkeley-db"
     * @required
     */
    private File vodbDirectory;
    /**
     * True if the database is readonly.
     *
     * @parameter
     */
    private Boolean readOnly;
    /**
     * Size of cache used by the database.
     *
     * @parameter
     */
    private Long cacheSize;
    /**
     * Property is set to true to use existing LocalVersionedTerminology.
     *
     * @parameter default-value=false
     *
     */
    private boolean useExistingDb;
    /**
     * This parameter specifies whether to rerun this mojo even if it has run
     * before.
     *
     * @parameter default-value=false
     */
    private boolean forceRerun;
    /**
     * @parameter
     */
    private DatabaseSetupConfig dbSetupConfig;
    /**
     * Location of the build directory.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty("java.awt.headless", "true");
        getLog().info("useExistingDb: " + useExistingDb);

        if (!forceRerun) {
            try {
                if (MojoUtil.alreadyRun(getLog(), vodbDirectory.getCanonicalPath(), this.getClass(), targetDirectory)) {
                    return;
                }
            } catch (NoSuchAlgorithmException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
        }
        try {
            new LocalVersionedTerminologyBuilder(vodbDirectory, readOnly, cacheSize,
                    useExistingDb, dbSetupConfig).build();
        } catch (BuilderException ex) {
            Logger.getLogger(VodbOpen.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex.getCause());
        }
    }
}
