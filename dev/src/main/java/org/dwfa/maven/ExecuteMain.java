package org.dwfa.maven;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which executes derby sql commands to generate a database or perform
 * other such tasks.
 * 
 * @goal execute-main
 * @phase process-resources
 */
public class ExecuteMain extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * Class to execute.
	 * @parameter
	 * @required
	 */
	private String className;
	
	/**
	 * Arguments to pass to class.
	 * @parameter
	 * @requiresDependencyResolution compile
	 */
	private String[] args;
	
	/**
	 * List of project's dependencies.
	 * @parameter expression="${project.dependencies}"
	 * @required
	 */
	private List<Dependency> dependencies;
	
	/**
	 * Location of local repository.
	 * @parameter expression="${settings.localRepository}"
	 * @required
	 */
	private String localRepository;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			URLClassLoader libLoader = 
					MojoUtil.getProjectClassLoaderWithoutProvided(
					dependencies, localRepository, outputDirectory
							+ "/classes/");
			
			Class c = libLoader.loadClass(className);
			Method m = c.getMethod("main", new Class[]{String[].class});
			m.invoke(null, new Object[]{ args });
			
	//		Class c = Class.forName(className);
	//		Method m = c.getMethod("main", new Class[]{String[].class});
	//		m.invoke(null, args);
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

 
}
