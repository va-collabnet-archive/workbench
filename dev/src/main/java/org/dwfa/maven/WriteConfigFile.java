
/*
 * Created on Jan 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.maven;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which writes a default startup config file.
 *
 * @goal write-config
 * @requiresDependencyResolution compile
 */
public class WriteConfigFile extends AbstractMojo {

    /**
     * Location of the build directory.
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
     * @parameter expression="${project.dependencies}"
     * @required
     */
    private List<Dependency> dependencies;
    
    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private String localRepository;

    public WriteConfigFile() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
		List<Dependency> dependencyWithoutProvided = new ArrayList<Dependency>();
		for (Dependency d: dependencies) {
			if (d.getScope().equals("provided")) {
				//don't add
			} else {
				dependencyWithoutProvided.add(d);
			}
		}
        this.outputDirectory.mkdirs();
        for (int i = 0; i < specs.length; i++) {
            System.out.println(specs[i]);
            try {
                URLClassLoader libLoader = MojoUtil.getProjectClassLoader(
                		dependencyWithoutProvided, localRepository, this.outputDirectory + "/classes/");
                Class specClass = libLoader.loadClass(specs[i].getClassName());
                Constructor specConstructor = specClass.getConstructor(new Class[] {});
                Object obj = specConstructor.newInstance(new Object[] {});
                Method m = specClass.getMethod(specs[i].getMethodName(), new Class[] {File.class});
                m.invoke(obj, new Object[] {new File(outputDirectory, specs[i].getConfigFileName())});
                
            } catch (Exception e) {
                throw new MojoExecutionException("Problem writing config file: " + specs[i].getClassName(), e);
            }
        }

    }

}
