package org.dwfa.maven;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;

/**
 * Goal which executes derby sql commands to generate a database or perform
 * other such tasks.
 * 
 * @goal export-cement
 * @phase process-resources
 */
public class ExportCement extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the build directory.
	 * 
	 * @parameter
	 */
	private String output;

	public void execute() throws MojoExecutionException, MojoFailureException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			String prefix;
			if (output == null) {
				prefix = outputDirectory.getCanonicalPath() + File.separatorChar + 
					"generated-resources" + File.separatorChar + 
					"concrete" + File.separatorChar + dateFormat.format(new Date()) + File.separatorChar;
			} else {
				output = output.replace('/', File.separatorChar);
				prefix = output + File.separator + dateFormat.format(new Date()) + File.separatorChar;
			}
			ArchitectonicAuxiliary.main(new String[] { prefix });
		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

 
}
