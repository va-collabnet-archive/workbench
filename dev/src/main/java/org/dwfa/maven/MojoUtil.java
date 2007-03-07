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
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;

public class MojoUtil {

    private static final String fileSep = System.getProperty("file.separator", "/");

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
    public static URLClassLoader getProjectClassLoader(List<Dependency> dependencies, String localRepository) throws IOException {
        List<URL> libs = addDependencies(dependencies, localRepository);
        //System.out.println("URLClassLoader libs: " + libs);
        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }

    /**
     * @param cpBuff
     * @return
     * @throws MojoExecutionException
     */
    public static URLClassLoader getProjectClassLoader(List<Artifact> artifacts) throws IOException {
        List<URL> libs = new ArrayList<URL>(artifacts.size());
        for (Artifact a: artifacts) {
        	libs.add(a.getFile().toURL());
        }
        System.out.println("URLClassLoader(List<Artifact>) libs: " + libs);
        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }

	private static List<URL> addDependencies(List<Dependency> dependencies, String localRepository) throws MalformedURLException {
		List<URL> libs = new ArrayList<URL>();
        for (Dependency d: dependencies) {
            String dependencyPath = MojoUtil.dependencyToPath(localRepository, d);
            libs.add(new File(dependencyPath).toURL());
        }
		return libs;
	}
    
    /**
     * @param cpBuff
     * @return
     * @throws MojoExecutionException
     */
    public static URLClassLoader getProjectClassLoader(List<Dependency> dependencies, String localRepository, String classesDir) throws IOException {
        List<URL> libs = addDependencies(dependencies, localRepository);
        libs.add(new File(classesDir).toURL());
        //System.out.println("URLClassLoader libs: " + libs);
        return new URLClassLoader(libs.toArray(new URL[libs.size()]));
    }
    
}
