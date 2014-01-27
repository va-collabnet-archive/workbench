
package org.ihtsdo.mojo.maven.classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;

public class GetDescendants {

	public static final String ISA_SCTID = "116680003";
	private File relationshipFile;
	private HashSet<String> parentConcepts;
	private File descendantsFile;
	private HashSet<String> descendants;


	public HashSet<String> getDescendants() {
		return descendants;
	}


	public GetDescendants(HashSet<String>parentConcepts, File relationshipFile, File descendantsFile) {
		super();
		this.parentConcepts=parentConcepts;
		this.relationshipFile=relationshipFile;
		this.descendantsFile=descendantsFile;
		descendants=new HashSet<String>();
	}


	public void execute() throws Exception{

		long start1 = System.currentTimeMillis();

		String nextLine;
		String[] splittedLine;

		if (descendantsFile!=null && descendantsFile.exists()){
			descendantsFile.delete();
		}
		HashSet<String> toFile = new HashSet<String>();
//		for (String key:parentConcepts){
//			toFile.add(key);
//		}
		while (parentConcepts.size()>0){

			HashSet<String> child = new HashSet<String>();

			FileInputStream rfis = new FileInputStream(relationshipFile	);
			InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
			BufferedReader rbr = new BufferedReader(risr);


			rbr.readLine();

			nextLine=null;
			splittedLine=null;

			while ((nextLine= rbr.readLine()) != null) {
				splittedLine = nextLine.split("\t",-1);
				if (splittedLine[7].compareTo(ISA_SCTID)==0
						&& splittedLine[2].compareTo("1")==0
						&& !toFile.contains(splittedLine[4])
						&& parentConcepts.contains(splittedLine[5])){
					toFile.add(splittedLine[4]);
					child.add(splittedLine[4]);
				}
			}
			rbr.close();
			rfis=null;
			risr=null;
			System.gc();
			parentConcepts=child;
		}
		if (descendantsFile!=null){

			FileOutputStream fos = new FileOutputStream( descendantsFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			bw.append("COMPONENTID");
			bw.append("\r\n");
			for (String desc:toFile){
				bw.append(desc);
				bw.append("\r\n");
			}
			bw.close();
			bw=null;
			osw=null;
			fos=null;
		}else{

			for (String desc:toFile){
				descendants.add(desc);
			}
		}
		long end1 = System.currentTimeMillis();
		long elapsed1 = (end1 - start1);
		System.out.println("Completed in " + elapsed1 + " ms");
	} 

}
