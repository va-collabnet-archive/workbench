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
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.dwfa.util.id.Type3UuidFactory;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal create-rf2-textdef-uuid
 * 
 * @phase install
 */
public class RF2TextDefinitionUUIDCreationMojo extends AbstractMojo {

	/**
	 * Location of the relationship file base file and final file
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<Append> rF2Files;

	public void execute() throws MojoExecutionException {

		getLog().info("Creating uuid for newly created text-definition in 20110731 release");		
		
		for (int i = 0; i < rF2Files.size(); i++) {
			getLog().info("RF2 File     :" + rF2Files.get(i).firstFile);
			
			// check if the RF1 Base file exist
			File rf2BaseFile = new File(rF2Files.get(i).firstFile);
			if (!rf2BaseFile.exists())
				throw new MojoExecutionException("RF2 Text-definition Base File : " + rF2Files.get(i).firstFile + " doesn't exist, exiting ..");

			File rf2FinalFile = new File(rF2Files.get(i).finalFile);
			
			if (!rf2FinalFile.exists())
				try {
					rf2FinalFile.createNewFile();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}	
			
			// open rf2 relationship file and  update characteristictypeId wherever applicable...
			try {
				BufferedReader rf2FileReader = new BufferedReader(new FileReader(rf2BaseFile));
				BufferedWriter rf2FileWriter = new BufferedWriter(new FileWriter(rf2FinalFile));
				String line;
				while ((line = rf2FileReader.readLine()) != null ) {
	 				String[] part= line.split("\t");
 			    	if(part[1].contains("NA")){
			    	  System.out.println("===part[1]===" + part[1] + "===part[3]===" + part[3]);
			    	  String UUID_NEW = Type3UuidFactory.fromSNOMED(part[3]).toString();
			    	  line =line.replace("NA", UUID_NEW);	
			    	  //System.out.println("===New Line===" + line);
			    	} 
 			    	rf2FileWriter.append(line);
 			    	rf2FileWriter.append("\r\n");
				}
				
				System.out.println("======RF2 Text-definition File updated with new UUID==========");		
				
				rf2FileReader.close();
				rf2FileWriter.close();
				
			} catch (IOException e) {
				getLog().error(e);
			}			
		}	
		getLog().info("Done.");
	}
}
