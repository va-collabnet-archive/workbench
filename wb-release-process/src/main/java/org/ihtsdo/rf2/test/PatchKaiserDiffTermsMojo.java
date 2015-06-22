package org.ihtsdo.rf2.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;


/**
 * Goal which patch kaiser different term with new description ids.
 * 
 * @goal patch-kaiser-descriptions
 * 
 */
public class PatchKaiserDiffTermsMojo extends AbstractMojo  {
	/**
	 * Input File
	 * 
	 * @parameter
	 * @required
	 */
	public String inputFile;
	
	/**
	 * File with string-tab-string map to replace
	 * 
	 * @parameter
	 * @required
	 */
	public String mapFile;
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		File mapF=new File(mapFile);
		try {
			HashMap<String, String> hashMap = getMap(mapF);
			File inFile=new File(inputFile);
			File replacFile=replaceInFile(inFile,hashMap);
			
			inFile.delete();
			replacFile.renameTo(inFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private File replaceInFile(File origFile,HashMap<String, String> replaceMap ) throws IOException{
	
		File tempFile=createTempFile(origFile,null);
		
		FileInputStream rfis = new FileInputStream(origFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		FileOutputStream rfos = new FileOutputStream(tempFile	);
		OutputStreamWriter rosw = new OutputStreamWriter(rfos,"UTF-8");
		BufferedWriter bw = new BufferedWriter(rosw);
		
		String line;
		String[] spl;
		StringBuffer lineOut = new StringBuffer("");
		while((line=rbr.readLine())!=null){
			spl=line.split("\t",-1);
			lineOut = new StringBuffer("");
			for(int i=0;i<spl.length;i++){
				
				if (replaceMap.containsKey(spl[i])){
					lineOut.append(replaceMap.get(spl[i]));
				}else{
					lineOut.append(spl[i]);
				}
				if (i==spl.length-1){
					lineOut.append("\r\n");
				}else{
					lineOut.append("\t");
				}
			}
			bw.append(lineOut);
		}
		rbr.close();
		bw.close();
		
		return tempFile;
		
	}

	private File createTempFile(File origFile, File folder) {
		File ret;
		if (folder!=null ){
			if (folder.exists()){
				 if (!folder.isDirectory()){
					 ret=new File(origFile.getParent(),"tmp_" + origFile.getName());
				 }else{
					 ret=new File(folder,"tmp_" + origFile.getName());
				 }
			}else{
				folder.mkdirs();
				ret=new File(folder,"tmp_" + origFile.getName());
			}
		}else{
			ret=new File(origFile.getParent(),"tmp_" + origFile.getName());
		}
		return ret;
	}

	private HashMap<String, String> getMap(File mapFile) throws IOException {

		HashMap<String,String> descList=new HashMap<String,String>();

		FileInputStream rfis = new FileInputStream(mapFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;
		while((line=rbr.readLine())!=null){
			spl=line.split("\t",-1);
			descList.put(spl[2],spl[1]);
		}
		rbr.close();

		return descList;
	}

}
