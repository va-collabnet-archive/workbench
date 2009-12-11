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
package org.dwfa.maven;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.maven.artifact.repository.DefaultArtifactRepository;
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
	 * @parameter expression="${localRepository}"
	 * @required
	 */
	private DefaultArtifactRepository localRepository;

    /**
     * @parameter
     * 
     */
    private String targetSubDir;

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log l = getLog();
        l.info("Now executing WriteDirectories: ");
        try {
            if (MojoUtil.alreadyRun(getLog(), this.getClass().getCanonicalName() + dependencies
                    + outputDirectory.getCanonicalPath())) {
                return;
            }
        } catch (NoSuchAlgorithmException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        File rootDir = this.outputDirectory;
        if (targetSubDir != null) {
            rootDir = new File(this.outputDirectory, targetSubDir);
        }
        for (Dependency d : dependencies) {
            if (d.getScope().equals("runtime-directory") == false) {
                // l.info("Skipping: " + d);
                continue;
            }
            l.info("Processing: " + d);

            String dependencyPath = MojoUtil.dependencyToPath(localRepository.getBasedir(), d);
            try {

                FileInputStream fis = new FileInputStream(dependencyPath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                JarInputStream jis = new JarInputStream(bis);
                JarEntry je = jis.getNextJarEntry();
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
                    je = jis.getNextJarEntry();
                }
                jis.close();
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage() + " path:" + dependencyPath, e);
            }
        }
    }
}
