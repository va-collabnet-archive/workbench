package org.dwfa.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.binding.java.GenerateClassFromRefset;

/**
 * Generate ConceptSpec Java file from a given Refset
 * 
 * @goal generate-concept-spec
 * 
 * @phase generate-resources
 * 
 * @requiresDependencyResolution compile
 */
public class GenerateConceptSpecFromRefset extends AbstractMojo {

	/**
	 * The name of the RefSet.
	 * 
	 * @parameter
	 */
	private String refsetName;

	/**
	 * The Uuid of the RefSet.
	 * 
	 * @parameter
	 */
	private String refsetUuid;

	/**
	 * The Java package name.
	 * 
	 * @parameter
	 */
	private String packageName;

	/**
	 * The Java class name.
	 * 
	 * @parameter expression="ConceptSpecFromRefset"
	 */
	private String className;

	/**
	 * The Java file output location.
	 * 
	 * @parameter expression="${project.build.sourceDirectory}"
	 */
	private File outputDirectory;

	// <execution>
	// <id>generate-concept-spec</id>
	// <phase>generate-sources</phase>
	// <goals>
	// <goal>generate-concept-spec</goal>
	// </goals>
	// <configuration>
	// <packageName>org.ihtsdo</packageName>
	// <className>IhtsdoConceptSpec</className>
	// <refsetName>spec</refsetName>
	// </configuration>
	// </execution>

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			GenerateClassFromRefset gcfr = new GenerateClassFromRefset();
			gcfr.setPackageName(packageName);
			gcfr.setClassName(className);
			gcfr.setOutputDirectory(outputDirectory);
			gcfr.setRefsetName(refsetName);
			gcfr.setRefsetUuid(refsetUuid);
			gcfr.run();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}
	}
}
