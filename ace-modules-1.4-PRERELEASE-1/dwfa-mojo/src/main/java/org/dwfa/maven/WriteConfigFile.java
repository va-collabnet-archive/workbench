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

/*
 * Created on Jan 7, 2006
 */
package org.dwfa.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;

/**
 * Goal which writes a default startup config file.
 * 
 * @goal write-config
 * @requiresDependencyResolution compile
 */
public class WriteConfigFile extends AbstractMojo {

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
     */
    ConfigSpec[] specs;

    /**
     * The execution information for this commit operation.
     * 
     * @parameter expression="${mojoExecution}"
     */
    MojoExecution execution;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

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

    public WriteConfigFile() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        Log l = getLog();

        try {
            if (MojoUtil.alreadyRun(l, execution.getExecutionId(), this.getClass(), targetDirectory)) {
                return;
            }
        } catch (NoSuchAlgorithmException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        } catch (IOException e1) {
            throw new MojoExecutionException(e1.getMessage(), e1);
        }
        List<Artifact> dependencyWithoutProvided = new ArrayList<Artifact>();
        for (Artifact a : artifacts) {
            if (a.getScope().equals("provided")) {
                getLog().info("Not adding provided: " + a);
            } else if (a.getGroupId().endsWith("runtime-directory") || a.getScope().equals("runtime-directory")) {
                getLog().info("Not adding runtime-directory: " + a);
            } else {
                if (a.getScope().equals("system")) {
                    getLog().info("System dependency: " + a);
                }
                dependencyWithoutProvided.add(a);
            }
        }
        try {
            URLClassLoader libLoader = MojoUtil.getProjectClassLoader(dependencyWithoutProvided);
            this.outputDirectory.mkdirs();
            for (int i = 0; i < specs.length; i++) {
                l.info("writing config for: " + specs[i]);
                try {
                    Class<?> specClass = libLoader.loadClass(specs[i].getClassName());
                    Constructor<?> specConstructor = specClass.getConstructor(new Class[] {});
                    Object obj = specConstructor.newInstance(new Object[] {});
                    Method m = specClass.getMethod(specs[i].getMethodName(), new Class[] { File.class });
                    m.invoke(obj, new Object[] { new File(outputDirectory, specs[i].getConfigFileName()) });

                } catch (Exception e) {
                    throw new MojoExecutionException("Problem writing config file: " + specs[i].getClassName(), e);
                }
            }
        } catch (Exception e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

    }

}
