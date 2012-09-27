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
 * Created on Dec 11, 2005
 */
package org.ihtsdo.mojo.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringOutputStream;

public class MojoUtil {

    private static final String fileSep = System.getProperty("file.separator", "/");

    // example snapshot pattern 2.0.2-20070925.004307-16
    private static Pattern snapshotPattern = Pattern.compile("[-]{1}[0-9]{8}\\.[0-9]{6}\\-[0-9]{1,3}$");

    // private static Pattern snapshotPattern =
    // Pattern.compile(".*[0-9]{1,3}$");

    public static String dependencyToPath(String localRepository, Dependency dep) {
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

    public static String artifactToPath(String localRepository, Artifact artifact) {
        StringBuffer buff = new StringBuffer();
        buff.append(localRepository);
        buff.append(fileSep);
        buff.append(artifact.getGroupId().replace('.', fileSep.charAt(0)));
        buff.append(fileSep);
        buff.append(artifact.getArtifactId());
        buff.append(fileSep);
        Matcher snapshotMatcher = snapshotPattern.matcher(artifact.getVersion());
        if (snapshotMatcher.find()) {
            buff.append(snapshotMatcher.replaceAll("-SNAPSHOT"));
        } else {
            buff.append(artifact.getVersion());
        }
        buff.append(fileSep);
        buff.append(artifact.getArtifactId());
        buff.append("-");
        buff.append(artifact.getVersion());
        buff.append(".jar");
        return buff.toString();
    }

    /**
     * @param cpBuff
     * @return
     * @throws MojoExecutionException
     */
    public static URLClassLoader getProjectClassLoader(List<Dependency> dependencies, String localRepository)
            throws IOException {
        List<URL> libs = addDependencies(dependencies, localRepository);
        return new URLClassLoader(libs.toArray(new URL[libs.size()]), dependencies.getClass().getClassLoader());
    }

    /**
     * @param cpBuff
     * @return
     * @throws MojoExecutionException
     */
    public static URLClassLoader getProjectClassLoader(List<Artifact> artifacts) throws IOException {
        List<URL> libs = new ArrayList<URL>(artifacts.size());
        for (Artifact a : artifacts) {
            libs.add(a.getFile().toURI().toURL());
        }
        return new URLClassLoader(libs.toArray(new URL[libs.size()]), artifacts.getClass().getClassLoader());
    }

    private static List<URL> addDependencies(List<Dependency> dependencies, String localRepository)
            throws MalformedURLException {
        List<URL> libs = new ArrayList<URL>();
        for (Dependency d : dependencies) {
            String dependencyPath = MojoUtil.dependencyToPath(localRepository, d);
            libs.add(new File(dependencyPath).toURI().toURL());
        }
        return libs;
    }

    /**
     * @param cpBuff
     * @return
     * @throws MojoExecutionException
     */
    public static URLClassLoader getProjectClassLoader(List<Dependency> dependencies, String localRepository,
            String classesDir) throws IOException {
        List<URL> libs = addDependencies(dependencies, localRepository);
        libs.add(new File(classesDir).toURI().toURL());
        return new URLClassLoader(libs.toArray(new URL[libs.size()]), dependencies.getClass().getClassLoader());
    }

    public static URLClassLoader getProjectClassLoaderWithoutProvided(List<Artifact> dependencies) throws IOException {
        List<Artifact> dependencyWithoutProvided = new ArrayList<Artifact>();
        for (Artifact d : dependencies) {
            if (d.getScope().equals(Artifact.SCOPE_PROVIDED)) {
                // don't add
            } else if (d.getScope().equals("runtime-directory")) {
                // don't add
                // DEPRECATED - but ignored here as method appears unused regardless
            } else if (d.getScope().equals(Artifact.SCOPE_SYSTEM)) {
                // don't add
            } else {
                dependencyWithoutProvided.add(d);
            }
        }
        return getProjectClassLoader(dependencyWithoutProvided);
    }

    public static URLClassLoader getProjectClassLoaderWithoutProvided(List<Dependency> dependencies,
            String localRepository, String classesDir) throws IOException {

        List<Dependency> dependencyWithoutProvided = new ArrayList<Dependency>();
        for (Dependency d : dependencies) {
            if (d.getScope().equals(Artifact.SCOPE_PROVIDED)) {
                // don't add
            } else if (d.getScope().equals("runtime-directory")) {
                // don't add
                // DEPRECATED - but ignored here as method appears unused regardless
            } else if (d.getScope().equals(Artifact.SCOPE_SYSTEM)) {
                // don't add
            } else {
                dependencyWithoutProvided.add(d);
            }
        }
        return getProjectClassLoader(dependencyWithoutProvided, localRepository, classesDir);
    }

    public static boolean allowedGoal(Log log, List<String> sessionGoals, String[] allowedGoals) {
        boolean allowedGoal = false;
        for (String goal : allowedGoals) {
            if (sessionGoals.contains(goal)) {
                allowedGoal = true;
                break;
            }
        }
        if (allowedGoal == false) {
            log.info("Skipping execution since session goals: " + sessionGoals
                + " do not contain one of the following allowed goals: " + Arrays.asList(allowedGoals));
        }
        return allowedGoal;
    }

    public static boolean alreadyRun(Log l, String input, Class<?> targetClass, File targetDir)
            throws NoSuchAlgorithmException, IOException {
        Sha1HashCodeGenerator generator = new Sha1HashCodeGenerator();
        if (input == null) {
            input = targetClass.getName();
            l.warn("Input is NULL. Using mojo class name instead...");
        }
        generator.add(input);
        String hashCode = generator.getHashCode();

        File goalFileDirectory = new File(targetDir, "completed-mojos");
        File goalFile = new File(goalFileDirectory, hashCode);

        // check to see if this goal has been executed previously
        if (!goalFile.exists()) {
            // create a new file to indicate this execution has completed
            goalFileDirectory.mkdirs();
            goalFile.createNewFile();
        } else {
            l.info("Previously executed: " + goalFile.getAbsolutePath() + "\nNow stopping.");
            StringOutputStream sos = new StringOutputStream();
            PrintStream ps = new PrintStream(sos);
            l.info("Properties: " + sos.toString());
            return true;
        }
        return false;
    }

}
