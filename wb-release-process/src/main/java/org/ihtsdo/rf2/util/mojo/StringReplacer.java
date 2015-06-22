package org.ihtsdo.rf2.util.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
/**
 * Replace string on all files
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author Alejandro Rodriguez
 * @goal string-replace-on-all-files
 * @phase compile
 */
public class StringReplacer extends AbstractMojo {

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
	private File filesTargetFolder;

	/**
	 * string to replace.
	 * 
	 * @parameter
	 *
	 */
	private String stringToReplace;

	/**
	 * string replacement.
	 * 
	 * @parameter
	 * 
	 */
	private String stringReplacement;
	

	public void processFolderRec(File coreFolder) {
		if (coreFolder.isDirectory()) {
			File[] list = coreFolder.listFiles();
			for (File file : list) {
				processFolderRec(file);
			}
		} else {
			changedFileDate(coreFolder);
		}
	}

	private void changedFileDate(File coreFolder) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(coreFolder), "UTF-8"));
			File destFile = new File(coreFolder.getParentFile(),coreFolder.getName() + "-copy");
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8"));
			while (br.ready()) {
				String line = br.readLine();
				line = line.replaceAll( stringToReplace ,  stringReplacement );
				bw.write(line+"\r\n");
			}
			br.close();
			bw.close();
			
			String name = coreFolder.getName();

			if(coreFolder.delete()){
				destFile.renameTo(new File(destFile.getParentFile(), name));
			}
			bw=null;
			br=null;
			System.gc();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		processFolderRec(filesTargetFolder);
	}
}
