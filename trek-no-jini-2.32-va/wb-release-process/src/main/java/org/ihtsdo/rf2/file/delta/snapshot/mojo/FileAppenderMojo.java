package org.ihtsdo.rf2.file.delta.snapshot.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal file-appender
 * 
 * @phase install
 */
public class FileAppenderMojo extends AbstractMojo {

	/**
	 * Location of the first and second file and final files
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<Append> rF2Files;

	

	public void execute() throws MojoExecutionException {

		getLog().info("Running the RF2 File Appender ");
		
		for (int i = 0; i < rF2Files.size(); i++) {
			getLog().info("First file     :" + rF2Files.get(i).firstFile);
			getLog().info("Second file       :" + rF2Files.get(i).secondFile);
			getLog().info("Second file       :" + rF2Files.get(i).finalFile);
			
			// check if the First file exist
			File firstFile = new File(rF2Files.get(i).firstFile);
			if (!firstFile.exists())
				throw new MojoExecutionException("First file : " + rF2Files.get(i).firstFile + " doesn't exist, exiting ..");

			// check if the Second file exist
			File secondFile = new File(rF2Files.get(i).secondFile);
			if (!secondFile.exists())
				throw new MojoExecutionException("Second file : " + rF2Files.get(i).secondFile + " doesn't exist, exiting ..");

			// check if the Final file exist
			File finalFile = new File(rF2Files.get(i).finalFile);
			if (!finalFile.exists())
				try {
					finalFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			// merged first file and  second file to create final file...
			try {
				//FileWriter firstFileWriter = new FileWriter(firstFile) ;
				BufferedWriter finalFileWriter = new BufferedWriter(new FileWriter(finalFile));
				BufferedReader firstFileReader = new BufferedReader(new FileReader(firstFile));
				BufferedReader secondFileReader = new BufferedReader(new FileReader(secondFile));
				
				String line;
				
				while ((line = firstFileReader.readLine()) != null ) {	
					
					finalFileWriter.append(line);
					finalFileWriter.append("\r\n");
				}			
				System.out.println("======First file writing finished==========");
				
				while ((line = secondFileReader.readLine()) != null ) {					
					if(!line.contains("effectiveTime") && !line.contains("ID")){						
						finalFileWriter.append(line);
						finalFileWriter.append("\r\n");
					}					
				}
				System.out.println("======Second file writing finished==========");
				
				firstFileReader.close();
				secondFileReader.close();
				finalFileWriter.close();
			} catch (IOException e) {
				getLog().error(e);
			}			
		}	
		getLog().info("Done.");
	}

	
}
