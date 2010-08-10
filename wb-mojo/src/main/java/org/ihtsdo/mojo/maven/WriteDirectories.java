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
package org.ihtsdo.mojo.maven;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal which writes configuration files to the output directory.
 * 
 * @goal write-directories
 * @requiresDependencyResolution compile
 * 
 */

public class WriteDirectories extends AbstractMojo {

    /**
     * Location of the source directory.
     * 
     * @parameter expression="${project.build.sourceDirectory}"
     * @required
     */
    private File sourceDirectory;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.dependencies}"
     * @required
     */
    private List<Dependency> dependencies;

    /**
     * The dependency artifacts of this project, for resolving
     * 
     * @pathOf(..)@ expressions. These are of type
     *              org.apache.maven.artifact.Artifact, and are keyed by
     *              groupId:artifactId, using
     *              org.apache.maven.artifact.ArtifactUtils.versionlessKey(..)
     *              for consistent formatting.
     * 
     * @parameter expression="${project.artifacts}"
     * @required
     * @readonly
     */
    private Set<Artifact> artifacts;

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    private DefaultArtifactRepository localRepository;

    /**
     * @parameter
     * 
     */
    private String targetSubDir;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

    /**
     * A list of groupId:artifactId combinations to include as runtime directories
     *
     * @parameter
     */
    private List<String> runtimeArtifacts;

    /**
     * A list of groupId:artifactId combinations to include as resource directories
     *
     * @parameter
     */
    private List<String> resourceArtifacts;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log l = getLog();
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + dependencies
                + outputDirectory.getCanonicalPath(), this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        ArtifactFilter legacyRuntimeFilter = new ScopeEqualsArtifactFilter( "runtime-directory" );
        ArtifactFilter runtimeFilter = new IncludesArtifactFilter( runtimeArtifacts != null ? runtimeArtifacts : Collections.emptyList() );

        ArtifactFilter legacyResourceFilter = new ScopeEqualsArtifactFilter( "resource-directory" );
        ArtifactFilter resourceFilter = new IncludesArtifactFilter( resourceArtifacts != null ? resourceArtifacts : Collections.emptyList() );

        for (Artifact a : artifacts) {
            boolean legacyRuntime = legacyRuntimeFilter.include(a);
            boolean legacyResource = legacyResourceFilter.include(a);
            if (legacyRuntime || runtimeFilter.include(a)) {
                if (legacyRuntime) {
                    l.error( "DEPRECATED: instead of runtime-directory, the artifact should be listed in the runtimeArtifacts configuration element" );
                }
                File rootDir = this.outputDirectory;
                if (targetSubDir != null) {
                    rootDir = new File(rootDir, targetSubDir);
                }
                extractArtifactDependencyToDir(l, rootDir, a);

            } else if (legacyResource || resourceFilter.include(a)) {
                if (legacyRuntime) {
                    l.error( "DEPRECATED: instead of resource-directory, the artifact should be listed in the resourceArtifacts configuration element" );
                }
                File rootDir = new File(this.sourceDirectory.getParentFile(), "resources");
                if (targetSubDir != null) {
                    rootDir = new File(rootDir, targetSubDir);
                }
                if (rootDir.exists() == false) {
                    extractArtifactDependencyToDir(l, rootDir, a);
                } else {
                    l.info("resource directory already exists: " + rootDir.getAbsolutePath());
                }
            }
        }
    }

    private void extractArtifactDependencyToDir(Log l, File rootDir, Artifact a) throws MojoExecutionException {
        l.info("Processing dependency artifact: " + a);
        l.info("   file: " + a.getFile());
        try {
            FileInputStream fis = new FileInputStream(a.getFile());
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream jis = new ZipInputStream(bis);
            ZipEntry je = jis.getNextEntry();
            while (je != null) {
                // l.info(" entry: " + je.getName());
                if (je.getName().contains("META-INF") == false) {
                    // l.info(" entry ok");

                    File destFile = new File(rootDir, je.getName());
                    destFile.getParentFile().mkdirs();
                    if (je.isDirectory()) {
                        destFile.mkdirs();
                    } else {
                        OutputStream fos = new FileOutputStream(destFile);
                        byte[] buffer = new byte[10240];
                        long bytesToRead = je.getSize();
                        while (bytesToRead > 0) { // write contents of
                            // 'is' to
                            // 'fos'
                            int bytesRead = jis.read(buffer);
                            fos.write(buffer, 0, bytesRead);
                            bytesToRead = bytesToRead - bytesRead;
                        }
                        fos.close();
                        destFile.setLastModified(je.getTime());
                    }
                }
                je = jis.getNextEntry();
            }
            jis.close();
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage() + " file:" + a.getFile(), e);
        }
    }

    private static class ScopeEqualsArtifactFilter
        implements ArtifactFilter
    {
        private String scope;

        public ScopeEqualsArtifactFilter( String scope )
        {
            this.scope = scope;
        }

        @Override
        public boolean include( Artifact artifact )
        {
            return artifact.getScope().equals( scope );
        }
    }
}
