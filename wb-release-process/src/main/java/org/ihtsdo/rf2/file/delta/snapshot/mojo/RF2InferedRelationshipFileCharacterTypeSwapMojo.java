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
 * @goal rf2-inferred-relationship-characteristictypeid
 * 
 * @phase install
 */
public class RF2InferedRelationshipFileCharacterTypeSwapMojo extends AbstractMojo {

	/**
	 * Location of the relationship file base file and final file
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<Append> rF2Files;

	public void execute() throws MojoExecutionException {

		getLog().info("Swapping CharacteristicType in the RF2 Relationship File to be more specific");		
		
		for (int i = 0; i < rF2Files.size(); i++) {
			getLog().info("RF2 File     :" + rF2Files.get(i).firstFile);
			
			// check if the RF1 Base file exist
			File rf2BaseFile = new File(rF2Files.get(i).firstFile);
			if (!rf2BaseFile.exists())
				throw new MojoExecutionException("RF2 Relationship Base File : " + rF2Files.get(i).firstFile + " doesn't exist, exiting ..");

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
		 				String newline = line;						
		 				String[] part= line.split("\t");
		 			
				    	if(part[8].equals("900000000000006009")){				    	
				    		//System.out.println("==========================================");
				    		String newChar= part[8].replace("900000000000006009", "900000000000011006");
				    		part[8] = newChar;	
				    		newline="";
				    		int totalcolumn = part.length;
				    		for(int j=0; j <totalcolumn ; j++ ){
				    			if(j < (totalcolumn -1)){
				    			newline=newline + part[j] +"\t";
				    			}else{
				    				newline=newline + part[j];
				    			}
				    		}
				    		//System.out.println(newline);
					    }
				    	
				    	
				   	rf2FileWriter.append(newline);
					rf2FileWriter.append("\r\n");
				 }	
				
				System.out.println("======RF2 Inferred Relationship Characteristic Type File updated==========");		
				
				rf2FileReader.close();
				rf2FileWriter.close();
				
			} catch (IOException e) {
				getLog().error(e);
			}			
		}	
		getLog().info("Done.");
	}

	
}
