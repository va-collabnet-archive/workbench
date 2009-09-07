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
	 * @parameter expression="${project.build.directory}/generated-sources/main/java"
	 */
	private File outputDirectory;

	/*
	 * Example use:
	 * 
	 * <br>&lt;execution&gt;
	 * 
	 * <br>&lt;id&gt;generate-concept-spec&lt;/id&gt;
	 * 
	 * <br>&lt;phase&gt;generate-sources&lt;/phase&gt;
	 * 
	 * <br>&lt;goals&gt;
	 * 
	 * <br>&lt;goal&gt;generate-concept-spec&lt;/goal&gt;
	 * 
	 * <br>&lt;/goals&gt;
	 * 
	 * <br>&lt;configuration&gt;
	 * 
	 * <br>&lt;packageName&gt;org.ihtsdo&lt;/packageName&gt;
	 * 
	 * <br>&lt;className&gt;IhtsdoConceptSpec&lt;/className&gt;
	 * 
	 * <br>&lt;refsetName&gt;spec&lt;/refsetName&gt;
	 * 
	 * <br>&lt;/configuration&gt;
	 * 
	 * <br>&lt;/execution&gt;
	 */
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
