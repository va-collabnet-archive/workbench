package org.ihtsdo.rf2.util.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.SnapshotGenerator;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal patch-snomedid-not-replaced
 * 
 */
public class PatchSnomedIdNotReplaced extends AbstractMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * sorted file
	 * 
	 * @parameter
	 * @required
	 */
	private String inputFile;
	/**
	 *  output File. (output in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String outputFile;

	/**
	 * Auxiliary file
	 * 
	 * @parameter
	 * @required
	 */
	private String auxiliaryFile;


	public void execute() throws MojoExecutionException {	

		try {


			FileOutputStream dfos = new FileOutputStream( outputFile);
			OutputStreamWriter dosw = new OutputStreamWriter(dfos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(dosw);

			FileInputStream fis = new FileInputStream(inputFile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br1 = new BufferedReader(isr);


			FileInputStream fis2 = new FileInputStream(auxiliaryFile);
			InputStreamReader isr2 = new InputStreamReader(fis2,"UTF-8");
			BufferedReader br2 = new BufferedReader(isr2);

			String line1;
			String header=br1.readLine();
			br2.readLine();
			bw.append(header);
			bw.append("\r\n");

			String[] splittedLine1;

			HashMap<String,String> hm=new HashMap<String,String>();

			while ((line1= br2.readLine()) != null) {
				splittedLine1 = line1.split("\t",-1);
				hm.put(splittedLine1[0],splittedLine1[1]);
			}
			br2.close();


			while ((line1= br1.readLine()) != null) {
				splittedLine1 = line1.split("\t",-1);
				if (splittedLine1[4].equals(I_Constants.SNOMED_REFSET_ID)){
					if (hm.containsKey(splittedLine1[5])  && (!(hm.get(splittedLine1[5]).equals(splittedLine1[6])))){
						bw.append(splittedLine1[0]);
						bw.append("\t");
						bw.append(splittedLine1[1]);
						bw.append("\t");
						bw.append(splittedLine1[2]);
						bw.append("\t");
						bw.append(splittedLine1[3]);
						bw.append("\t");
						bw.append(splittedLine1[4]);
						bw.append("\t");
						bw.append(splittedLine1[5]);
						bw.append("\t");
						bw.append(hm.get(splittedLine1[5]));
					}else{
						bw.append(line1);
					}
				}
				else{
					bw.append(line1);
				}
				bw.append("\r\n");
			}
			br1.close();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}