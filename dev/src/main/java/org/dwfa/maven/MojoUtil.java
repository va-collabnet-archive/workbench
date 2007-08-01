/*
 * Created on Dec 11, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

public class MojoUtil {

	private static final String fileSep = System.getProperty("file.separator",
			"/");

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

	/**
	 * @param cpBuff
	 * @return
	 * @throws MojoExecutionException
	 */
	public static URLClassLoader getProjectClassLoader(
			List<Dependency> dependencies, String localRepository)
			throws IOException {
		List<URL> libs = addDependencies(dependencies, localRepository);
		// System.out.println("URLClassLoader libs: " + libs);
		return new URLClassLoader(libs.toArray(new URL[libs.size()]));
	}

	/**
	 * @param cpBuff
	 * @return
	 * @throws MojoExecutionException
	 */
	public static URLClassLoader getProjectClassLoader(List<Artifact> artifacts)
			throws IOException {
		List<URL> libs = new ArrayList<URL>(artifacts.size());
		for (Artifact a : artifacts) {
			libs.add(a.getFile().toURI().toURL());
		}
		System.out.println("URLClassLoader(List<Artifact>) libs: " + libs);
		return new URLClassLoader(libs.toArray(new URL[libs.size()]));
	}

	private static List<URL> addDependencies(List<Dependency> dependencies,
			String localRepository) throws MalformedURLException {
		List<URL> libs = new ArrayList<URL>();
		for (Dependency d : dependencies) {
			String dependencyPath = MojoUtil.dependencyToPath(localRepository,
					d);
			libs.add(new File(dependencyPath).toURI().toURL());
		}
		return libs;
	}

	/**
	 * @param cpBuff
	 * @return
	 * @throws MojoExecutionException
	 */
	public static URLClassLoader getProjectClassLoader(
			List<Dependency> dependencies, String localRepository,
			String classesDir) throws IOException {
		List<URL> libs = addDependencies(dependencies, localRepository);
		libs.add(new File(classesDir).toURI().toURL());
		// System.out.println("URLClassLoader libs: " + libs);
		return new URLClassLoader(libs.toArray(new URL[libs.size()]));
	}

	public static URLClassLoader getProjectClassLoaderWithoutProvided(
			List<Dependency> dependencies, String localRepository,
			String classesDir) throws IOException {

		List<Dependency> dependencyWithoutProvided = new ArrayList<Dependency>();
		for (Dependency d : dependencies) {
			if (d.getScope().equals("provided")) {
				// don't add
			} else if (d.getScope().equals("runtime-directory")) {
				// don't add
			} else {
				dependencyWithoutProvided.add(d);
			}
		}
		return getProjectClassLoader(dependencyWithoutProvided,
				localRepository, classesDir);
	}

	public static boolean allowedGoal(Log log, List sessionGoals,
			String[] allowedGoals) {
		boolean allowedGoal = false;
		for (String goal : allowedGoals) {
			if (sessionGoals.contains(goal)) {
				allowedGoal = true;
				break;
			}
		}
		if (allowedGoal == false) {
			log.info("Skipping execution since session goals: " + sessionGoals
					+ " do not contain one of the following allowed goals: "
					+ Arrays.asList(allowedGoals));
		}
		return allowedGoal;
	}
    public static boolean alreadyRun(Log l, String input) throws NoSuchAlgorithmException, IOException {
        Sha1HashCodeGenerator generator = new Sha1HashCodeGenerator();
        generator.add(input);
        String hashCode = generator.getHashCode();

        File goalFileDirectory = new File("target" + File.separator + "completed-mojos");
        File goalFile = new File(goalFileDirectory, hashCode);

        // check to see if this goal has been executed previously
        if (!goalFile.exists()) {
            // create a new file to indicate this execution has completed
                goalFileDirectory.mkdirs();
                goalFile.createNewFile();
        } else {
            l.info("Previously executed. Now stopping.");
            return true;
        }
        return false;
    }

}
