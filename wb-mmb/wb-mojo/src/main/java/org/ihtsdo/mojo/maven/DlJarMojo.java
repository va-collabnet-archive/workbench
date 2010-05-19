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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import com.sun.jini.tool.ClassDep;

/**
 * Goal which writes the classnames to include in a &lt;service&gt;-dl.jar file.
 * 
 * @goal class-dep
 * @requiresDependencyResolution compile
 */
public class DlJarMojo extends AbstractMojo {
    /**
     * Location of the build directory.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String artifactId;

    /**
     * @parameter
     * @required
     */
    private String[] inSearch;

    /**
     * @parameter
     * @required
     */
    private String[] outOfSearch;

    /**
     * @parameter
     * @required
     */
    private String[] skipSearch;

    /**
     * @parameter
     * @required
     */
    private String[] searchRoot;

    /**
     * @parameter
     * @required
     */
    private String[] preferred;

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

    private static final String pathSep = System.getProperty("path.separator", ":");

    private static final String eol = System.getProperty("line.separator", "\n");

    public void execute() throws MojoExecutionException {
        FileWriter w = null;
        try {
            List<String> argList = new ArrayList<String>();
            argList.add("-files");
            argList.add("-cp");
            StringBuffer cpBuff = new StringBuffer();

            // System.out.println("Dependencies: " + dependencies);
            for (Dependency d : dependencies) {
                cpBuff.append(MojoUtil.dependencyToPath(localRepository, d));
                /*
                 * String dependencyPath = MojoUtil.dependencyToPath(
                 * localRepository, d);
                 * System.out.println("Dependency path: " + dependencyPath);
                 */
                cpBuff.append(pathSep);
            }
            URLClassLoader libLoader = MojoUtil.getProjectClassLoader(dependencies, localRepository);

            // cpBuff.append(outputDirectory);
            // cpBuff.append(fileSep);
            // cpBuff.append("classes");

            argList.add(cpBuff.toString());

            for (int i = 0; i < inSearch.length; i++) {
                argList.add("-in");
                argList.add(inSearch[i]);
            }

            for (int i = 0; i < outOfSearch.length; i++) {
                argList.add("-out");
                argList.add(outOfSearch[i]);
            }

            for (int i = 0; i < skipSearch.length; i++) {
                argList.add("-skip");
                argList.add(skipSearch[i]);
            }

            for (int i = 0; i < searchRoot.length; i++) {
                argList.add(searchRoot[i]);
            }

            String[] args = new String[argList.size()];
            ClassDep dep = new ClassDep();
            dep.setupOptions((String[]) argList.toArray(args));

            File f = new File(outputDirectory, "classes");

            if (!f.exists()) {
                f.mkdirs();
            }

            File deps = new File(outputDirectory, artifactId + ".deps");
            w = new FileWriter(deps);
            String[] vals = dep.compute();
            for (int i = 0; i < vals.length; i++) {
                if (dep.getFiles()) {
                    w.write(vals[i].replace('.', File.separatorChar) + ".class");
                } else {
                    w.write(vals[i]);
                }
                w.write(eol);
                File classFile = new File(f, vals[i].replace('.', File.separatorChar) + ".class");
                try {
                    Class theClass = Class.forName(vals[i]);
                    writeClass(classFile, theClass);
                } catch (ClassNotFoundException ex) {
                    Class theClass = libLoader.loadClass(vals[i]);
                    writeClass(classFile, theClass);
                }
            }
            w.close();

            f = new File(f, "META-INF");

            if (!f.exists()) {
                f.mkdirs();
            }
            File prefFile = new File(f, "PREFERRED.LIST");
            w = new FileWriter(prefFile);
            w.write("PreferredResources-Version: 1.0");
            w.write(eol);
            w.write(eol);
            for (int i = 0; i < preferred.length; i++) {
                w.write("Name: ");
                w.write(preferred[i]);
                if (preferred[i].endsWith(".class") == false) {
                    w.write(".class");
                }
                w.write(eol);
                w.write("Preferred: true");
                w.write(eol);
            }
            w.close();
        } catch (Exception e) {
            throw new MojoExecutionException("Error creating dl jar file.", e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * @param jarOut
     * @param theClass
     * @throws IOException
     */
    private void writeClass(File classFile, Class theClass) throws IOException {
        String classFileName = theClass.getName().replace('.', '/') + ".class";
        URL classUrl = theClass.getResource("/" + classFileName);
        InputStream classInputStream = classUrl.openStream();
        int size = classInputStream.available();
        byte[] data = new byte[size];
        classInputStream.read(data, 0, size);

        classFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(classFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        bos.write(data, 0, size);
        bos.close();
    }
}
