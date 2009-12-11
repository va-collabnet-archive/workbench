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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ImplementTermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

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
     * Property is set to true to use existing LocalVersionedTerminology.
     *
     * @parameter default-value=false
     *
     */
    Boolean useExistingDb;

    /**
     * This parameter specifies whether to rerun this mojo even if it has run before.
     * @parameter default-value=false
     */
    boolean forceRerun;

    /**
     * @parameter
     */
    private DatabaseSetupConfig dbSetupConfig;

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        System.setProperty("java.awt.headless", "true");
        getLog().info("useExistingDb: " + useExistingDb);
        if (dbSetupConfig == null) {
            dbSetupConfig = new DatabaseSetupConfig();
        }
        try {
            if (useExistingDb != null && useExistingDb
                && LocalVersionedTerminology.get() != null) {
                LocalVersionedTerminology.createFactory(vodbDirectory,
                    readOnly, cacheSize, dbSetupConfig, useExistingDb);
                return;
            }

            if (!forceRerun) {
                try {
                    if (MojoUtil.alreadyRun(getLog(), vodbDirectory
                        .getCanonicalPath(), this.getClass(), targetDirectory)) {
                        return;
                    }
                } catch (NoSuchAlgorithmException e) {
                    throw new MojoExecutionException(e.getLocalizedMessage(), e);
                }
            }

            getLog().info("vodb dir: " + vodbDirectory);
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

    public File getVodbDirectory() {
        return vodbDirectory;
    }

    public void setVodbDirectory(File vodbDirectory) {
        this.vodbDirectory = vodbDirectory;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public Boolean getUseExistingDb() {
        return useExistingDb;
    }

    public void setUseExistingDb(Boolean useExistingDb) {
        this.useExistingDb = useExistingDb;
    }

    public boolean isForceRerun() {
        return forceRerun;
    }

    public void setForceRerun(boolean forceRerun) {
        this.forceRerun = forceRerun;
    }

    public DatabaseSetupConfig getDbSetupConfig() {
        return dbSetupConfig;
    }

    public void setDbSetupConfig(DatabaseSetupConfig dbSetupConfig) {
        this.dbSetupConfig = dbSetupConfig;
    }

    public File getTargetDirectory() {
        return targetDirectory;
    }

    public void setTargetDirectory(File targetDirectory) {
        this.targetDirectory = targetDirectory;
    }

}
