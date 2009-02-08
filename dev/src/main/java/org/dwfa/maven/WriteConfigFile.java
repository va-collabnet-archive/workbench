
/*
 * Created on Jan 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.maven;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;

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
     * @parameter expression="${mojoExecution}"
     */
    MojoExecution execution;

    public WriteConfigFile() {
        super();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
		Log l = getLog();

		try {
			if (MojoUtil.alreadyRun(l, execution.getExecutionId())) {
				return;
			}
		} catch (NoSuchAlgorithmException e1) {
			throw new MojoExecutionException(e1.getMessage(), e1);
		} catch (IOException e1) {
			throw new MojoExecutionException(e1.getMessage(), e1);
		}
        this.outputDirectory.mkdirs();
        for (int i = 0; i < specs.length; i++) {
            l.info("writing config for: " + specs[i]);
            try {
                Class<?> specClass = this.getClass().getClassLoader().loadClass(specs[i].getClassName());
                Constructor<?> specConstructor = specClass.getConstructor(new Class[] {});
                Object obj = specConstructor.newInstance(new Object[] {});
                Method m = specClass.getMethod(specs[i].getMethodName(), new Class[] {File.class});
                m.invoke(obj, new Object[] {new File(outputDirectory, specs[i].getConfigFileName())});
                
            } catch (Exception e) {
                throw new MojoExecutionException("Problem writing config file: " + specs[i].getClassName(), e);
            }
        }

    }

}
