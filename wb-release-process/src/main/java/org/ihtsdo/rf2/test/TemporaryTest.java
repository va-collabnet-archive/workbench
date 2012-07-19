package org.ihtsdo.rf2.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.idgeneration.IdAssignmentImpl;

public class TemporaryTest {
	private static final Logger log = Logger.getLogger(TemporaryTest.class);
	private static File resFolder = new File("Rf2_20120731_IDCHANGED");
	private static File sourceFolder = new File("Rf2_20120731_6");

	public static void main(String[] args) {
		String endpointURL = "";
		String username = "termmed";
		String password = "termmed";

		final IdAssignmentImpl idGen = new IdAssignmentImpl(endpointURL, username, password);
		long sctId = 0L;


		//processRecursivly(sourceFolder);
		
		File descriptionsFile = new File(sourceFolder.getPath()+  "/rf2/Full/sct2_Description_Full-en-GB_GMDN_20120731.txt");
		File variantsFile = new File("variants_UK.txt");
		//FileInputStream fis = new FileInputStream(variantsFile);
		//InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		//BufferedReader rf2FileReader = new BufferedReader(isr);
		
	}

	private static void processRecursivly(File file) {
		try {

			if (file.isDirectory()) {
				processRecursivly(file);
			} else {
				FileInputStream fis = new FileInputStream(file);
				InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
				BufferedReader rf2FileReader = new BufferedReader(isr);
				
				
				File result = new File(new File(file.getPath().replace(sourceFolder.getName(), resFolder.getName())), file.getName());
				FileOutputStream fos = new FileOutputStream(result);
	            OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF8");
	            BufferedWriter rf2FileWriter = new BufferedWriter(osw);
				
	            
	            while(rf2FileReader.ready()){
	            	String line = rf2FileReader.readLine();
	            	String[] splited = line.split("\\t", -1);
	            	//sctId = idGen.getSCTID(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));
	            }
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
