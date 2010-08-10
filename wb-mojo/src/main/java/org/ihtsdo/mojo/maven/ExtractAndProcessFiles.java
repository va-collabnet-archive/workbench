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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.ihtsdo.mojo.maven.ExtractAndProcessSpec.SubstutionSpec;

/**
 * Goal which writes configuration files to the output directory.
 * 
 * @goal process-config
 * @requiresDependencyResolution compile
 * 
 */

public class ExtractAndProcessFiles extends AbstractMojo {

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

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
    private ArtifactRepository localRepository;

    /**
     * @parameter
     * @required
     */
    ExtractAndProcessSpec[] specs;

    /**
     * The execution information for this commit operation.
     * 
     * @parameter expression="${mojoExecution}"
     */
    private MojoExecution execution;

    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File targetDirectory;

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

        UUID randomId = UUID.randomUUID();
        for (ExtractAndProcessSpec spec : specs) {
            Pattern filePattern = Pattern.compile(spec.getFilePatternStr());
            File rootDir = new File(outputDirectory, spec.getDestDir());
            List<RegexReplace> replacers = null;
            char[] cbuf = new char[1024];
            byte[] bbuf = new byte[1024];
            for (Artifact d : artifacts) {
                if (d.getScope().equals(Artifact.SCOPE_PROVIDED)) {
                    continue;
                }
                if (d.getScope().equals(Artifact.SCOPE_SYSTEM)) {
                    continue;
                }
                if (d.getScope().equals("runtime-directory")) {
                    l.error( "DEPRECATED: runtime-directory scope should be converted to ZIP artifact for " + d );
                    continue;
                }

                File dependencyFile = d.getFile();
                if (dependencyFile.exists()) {
                    try {

                        ZipFile jf = new ZipFile(dependencyFile);

                        Enumeration<? extends ZipEntry> jarEnum = jf.entries();
                        while (jarEnum.hasMoreElements()) {
                            ZipEntry je = jarEnum.nextElement();

                            Matcher m = filePattern.matcher(je.getName());
                            if (m.find()) {

                                File destFile = new File(rootDir, je.getName().substring(
                                    je.getName().lastIndexOf('/') + 1));
                                if (spec.getRetainDirStructure()) {
                                    if (je.getName().startsWith("/")) {
                                        destFile = new File(rootDir, je.getName().substring(1));
                                    } else {
                                        destFile = new File(rootDir, je.getName());
                                    }
                                }
                                destFile.getParentFile().mkdirs();
                                if (je.isDirectory()) {
                                    destFile.mkdirs();
                                } else if (spec.getSubstitutions().length > 0) {

                                    if (replacers == null) {
                                        replacers = new ArrayList<RegexReplace>(spec.getSubstitutions().length);
                                        for (SubstutionSpec s : spec.getSubstitutions()) {
                                            String replacementStr;
                                            switch (s.getNullAction()) {
                                            case MAKE_UUID:
                                                replacementStr = randomId.toString();
                                                break;
                                            case EMPTY_STRING:
                                                replacementStr = "";
                                                break;
                                            case PROMPT:
                                                throw new UnsupportedOperationException();
                                            case REPLACE_LITERAL:
                                                replacementStr = s.getReplacementStr();
                                                break;
                                            default:
                                                throw new UnsupportedOperationException();
                                            }
                                            if (s.getPropertyName() != null) {
                                                String propValue = System.getProperty(s.getPropertyName());
                                                if (propValue != null) {
                                                    replacementStr = propValue;
                                                }
                                            }
                                            replacers.add(new RegexReplace(s.getPatternStr(), replacementStr));
                                        }
                                    }
                                    InputStreamReader isr = new InputStreamReader(jf.getInputStream(je));
                                    StringBuffer buff = new StringBuffer();
                                    int read = isr.read(cbuf);
                                    while (read > 0) {
                                        buff.append(cbuf, 0, read);
                                        read = isr.read(cbuf);
                                    }
                                    isr.close();

                                    FileWriter fw = new FileWriter(destFile);
                                    String fileAsString = buff.toString();
                                    for (RegexReplace r : replacers) {
                                        fileAsString = r.execute(fileAsString);
                                    }

                                    Reader substuted = new StringReader(fileAsString);
                                    read = substuted.read(cbuf);
                                    while (read > 0) {
                                        fw.write(cbuf, 0, read);
                                        read = substuted.read(cbuf);
                                    }
                                    fw.close();
                                } else {
                                    InputStream is = jf.getInputStream(je);
                                    destFile.getParentFile().mkdirs();
                                    OutputStream os = new FileOutputStream(destFile);
                                    int read = is.read(bbuf);
                                    while (read > 0) {
                                        os.write(bbuf, 0, read);
                                        read = is.read(bbuf);
                                    }
                                    is.close();
                                    os.close();
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new MojoExecutionException(e.getMessage() + " path:" + dependencyFile + " spec" + spec, e);
                    }
                } else {
                    getLog().info("Warning. Dependency file does not exist: " + dependencyFile);
                }

            }
        }
    }
}
