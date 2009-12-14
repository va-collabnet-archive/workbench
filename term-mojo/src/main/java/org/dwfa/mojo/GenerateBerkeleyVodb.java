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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.maven.MojoUtil;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Import SNOMED format data files from a jar and load into an ACE Berkeley
 * database.
 *
 * @goal berkley-vodb
 *
 * @phase generate-resources
 * @requiresDependencyResolution compile
 */
public class GenerateBerkeleyVodb extends AbstractMojo {
    private static final String fileSep = System.getProperty("file.separator", "/");

    /**
     * Location of the build directory.
     *
     * @parameter expression="${project.build.directory}/classes/berkeley-db"
     * @required
     */
    File outputDirectory;

    /**
     * This is the target directory to unjar files to . We need this because of the multimodule build.
     * We can't simply reference "target" anymore.
     * @parameter default-value="${project.build.directory}/unjar"
     */
    File unjaringDir;

    /**
     * @parameter expression="${project.dependencies}"
     * @required
     */
    private List<Dependency> dependencies;

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private String localRepository;

    /**
     * A list of dependencies in the following order:
     * <ol>
     * <li>Dependency containing the <a href='../dataimport.html'>ACE format</a>
     * metadata necessary to import the SNOMED data. The data files are expected
     * in the "org/jehri/cement/" directory inside the jar file</li>
     * <li>Dependency containing the SNOMED format data to import. The data
     * files are expected in the "org/snomed/yyyy-MM-dd/" directory, where the
     * date is used to provide a status date for the relationship status (since
     * SNOMED does not provide them)</li>
     * </ol>
     *
     * @parameter
     * @required
     */
    private List<DependencySpec> dependenciesToProcess;

    /**
     * @parameter
     */
    private String dataLocationInJar;

    /**
     * @parameter
     */
    private DatabaseSetupConfig dbSetupConfig;

    private String[] allowedGoals = new String[] { "install", "deploy" };

    /**
     * The maven session
     *
     * @parameter expression="${session}"
     * @required
     */
    private MavenSession session;

    public void execute() throws MojoExecutionException {
        getLog().info(" data prefix: " + dataLocationInJar);
        if (MojoUtil.allowedGoal(getLog(), session.getGoals(), allowedGoals)) {
            if ((dataLocationInJar != null) && dependenciesToProcess.size() > 1) {
                throw new MojoExecutionException("Only one dependency is allowed with a data prefix specification");
            }
            String[] args = new String[3];
            int argIndex = 0;
            args[argIndex++] = outputDirectory.getAbsolutePath();
            outputDirectory.mkdirs();
            try {
                Set<String> processSet = new HashSet<String>(dependenciesToProcess.size());
                for (DependencySpec ds : dependenciesToProcess) {
                    /*
                     * Check added to see if the version of the dependecy to be
                     * process has been specified.
                     * If not, we look for a matching dependecy in the
                     * dependecies list for the POM and use the version
                     * specified there.
                     * This is to cater for the automated resolution of SNAPSHOT
                     * versions by maven.
                     */
                    if (ds.getVersion() == null) {
                        for (Dependency d : dependencies) {
                            if (d.getGroupId().equalsIgnoreCase(ds.getGroupId())
                                && d.getArtifactId().equalsIgnoreCase(ds.artifactId)) {
                                System.out.println("Setting version to >>> " + d.getVersion());
                                ds.setVersion(d.getVersion());
                            }
                        }
                    }

                    processSet.add(ds.getGroupId() + ds.getArtifactId() + ds.getVersion());
                }
                getLog().info("Here are the dependencies...");

                for (Dependency d : dependencies) {
                    getLog().info("Project Dependency: " + d);
                    if (processSet.contains(d.getGroupId() + d.getArtifactId() + d.getVersion())) {
                        getLog().info("    Process this one: " + d);
                        String path = dependencyToPath(d);
                        args[argIndex++] = path;
                        JarFile jf = new JarFile(path);

                        Enumeration<JarEntry> jarEnum = jf.entries();
                        while (jarEnum.hasMoreElements()) {
                            JarEntry je = jarEnum.nextElement();
                            getLog().info(je.toString());
                        }
                    }
                }

                if (dbSetupConfig == null) {
                    dbSetupConfig = new DatabaseSetupConfig();
                }
                if (dataLocationInJar == null) {
                    LocalVersionedTerminology.createFactory(outputDirectory, false, 600000000L, dbSetupConfig);
                    LocalVersionedTerminology.get().loadFromMultipleJars(args, unjaringDir);
                } else {
                    LocalVersionedTerminology.createFactory(outputDirectory, false, 600000000L, dbSetupConfig);
                    LocalVersionedTerminology.get().loadFromSingleJar(args[1], dataLocationInJar, unjaringDir);
                }

            } catch (Exception ex) {
                throw new MojoExecutionException("Error processing dependency. Reason: " + ex.getMessage(), ex);
            }
        }
    }

    public String dependencyToPath(Dependency dep) {
        StringBuffer buff = new StringBuffer();
        buff.append(localRepository);
        buff.append(fileSep);
        buff.append(dep.getGroupId().replace('.', fileSep.charAt(0)));
        buff.append(fileSep);
        buff.append(dep.getArtifactId());
        buff.append(fileSep);
        buff.append(dep.getVersion());
        buff.append(fileSep);
        buff.append(dep.getArtifactId());
        buff.append("-");
        buff.append(dep.getVersion());
        buff.append(".jar");
        return buff.toString();
    }

    public String getDataLocationInJar() {
        return dataLocationInJar;
    }

    public void setDataLocationInJar(String dataLocationInJar) {
        this.dataLocationInJar = dataLocationInJar;
    }
}
