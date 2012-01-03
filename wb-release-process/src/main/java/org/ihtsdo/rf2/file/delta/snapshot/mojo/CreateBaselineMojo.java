package org.ihtsdo.rf2.file.delta.snapshot.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal create-previous-baseline
 * 
 * @phase install
 */
public class CreateBaselineMojo extends AbstractMojo {

	/**
	 * Location of the relationship file base file and final file
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<Append> rF2Files;

	public void execute() throws MojoExecutionException {

		getLog().info("Creating RF2 Previous Baseline Using Next Baseline");		
		
		for (int i = 0; i < rF2Files.size(); i++) {
			getLog().info("RF2 File     :" + rF2Files.get(i).firstFile);
			
			// check if the RF1 Base file exist
			File rf2BaseFile = new File(rF2Files.get(i).firstFile);
			if (!rf2BaseFile.exists())
				throw new MojoExecutionException("RF2 Base File : " + rF2Files.get(i).firstFile + " doesn't exist, exiting ..");

			File rf2FinalFile = new File(rF2Files.get(i).finalFile);
			
			if (!rf2FinalFile.exists())
				try {
					rf2FinalFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
			
			// open rf2 file and  removed line with effective date  characteristictypeId wherever applicable...
			try {
				BufferedReader rf2FileReader = new BufferedReader(new FileReader(rf2BaseFile));
				BufferedWriter rf2FileWriter = new BufferedWriter(new FileWriter(rf2FinalFile));
				String currentBaselineDate = "20110131";
				String line;
				while ((line = rf2FileReader.readLine()) != null ) {
		 				String[] part= line.split("\t");		 			
				    	if(!part[1].equals(currentBaselineDate)){				    	
				    		rf2FileWriter.append(line);
				    		rf2FileWriter.append("\r\n");
					    }
				 }	
				
				System.out.println("======RF2 Previous Release File created==========");		
				
				rf2FileReader.close();
				rf2FileWriter.close();
				
			} catch (IOException e) {
				getLog().error(e);
			}			
		}	
		getLog().info("Done.");
	}

	
}
