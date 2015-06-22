package org.ihtsdo.rf2.util.mojo;

import java.io.File;
import java.util.HashSet;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * Add differences with wb 20150131 release process
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Alejandro Rodriguez
 * @goal add-relationship-from-list-20150131
 * @phase compile
 */
public class AddRelationshipFromList extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;
	
	/**
	 * Location of the files target Folder.
	 * 
	 * @parameter
	 * 
	 */
	private File exportDirectory;

	/**
	 * Location of the resource Folder.
	 * 
	 * @parameter
	 * 
	 */
	private File resourceDirectory;

	private void processFiles(String exportedFile, String auxFile)  {
			
			File expFile=new File(exportDirectory,exportedFile);
			File fromFile=new File(resourceDirectory , auxFile);
			HashSet<File> hFile = new HashSet<File>();
			hFile.add(expFile);
			hFile.add(fromFile);
			org.ihtsdo.rf2.postexport.CommonUtils.concatFile(hFile, expFile);
			
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		processFiles("sct2_Relationship_Delta_20150131.txt","dif_rel_in_inf_wbrp.txt");
		processFiles("sct2_StatedRelationship_Delta_20150131.txt","dif_rel_in_wbrp.txt");
	}

}
