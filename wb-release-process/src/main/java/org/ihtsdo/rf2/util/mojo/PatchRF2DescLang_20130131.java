package org.ihtsdo.rf2.util.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class PatchRF2DescLang_20130131  {

	private File rf2Desc;
	private File rf2Lang;

	public PatchRF2DescLang_20130131(File RF2Description, File RF2Language) {
		super();
		this.rf2Desc=RF2Description;
		this.rf2Lang=RF2Language;	
	}


	public void execute(){

		try {
			long start1 = System.currentTimeMillis();

			String nextLine;
			String[] splittedLine;
			double lines = 0;

			
				File RF2OutputDescFile= new File("destination/tmp","pch_" + rf2Desc.getName());

				FileOutputStream fos = new FileOutputStream( RF2OutputDescFile);
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				FileInputStream rfis = new FileInputStream(rf2Desc	);
				InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
				BufferedReader rbr = new BufferedReader(risr);


				String header=rbr.readLine();
				bw.append(header);
				bw.append("\r\n");

				nextLine=null;
				splittedLine=null;
				while ((nextLine= rbr.readLine()) != null) {
					splittedLine = nextLine.split("\t",-1);
					bw.append(splittedLine[0]);
					bw.append("\t");
					bw.append(splittedLine[1]);
					bw.append("\t");
					bw.append(splittedLine[2]);
					bw.append("\t");
					bw.append(splittedLine[3]);
					bw.append("\t");
					bw.append(splittedLine[4]);
					bw.append("\t");
					bw.append(splittedLine[5]);
					bw.append("\t");
					bw.append(splittedLine[6]);
					bw.append("\t");
					if (splittedLine[0].equals("2952212013")){
						bw.append("Chest drain tip submitted as specimen");
					}else if (splittedLine[0].equals("2956704018")){
						bw.append("Congenital obstruction of coronary sinus");
					}else if (splittedLine[0].equals("2957185017")){
						bw.append("SNOMED Clinical Terms version: 20130131 [R] (January 2013 Release)");
					}else{
						bw.append(splittedLine[7]);						
					}
					bw.append("\t");
					bw.append(splittedLine[8]);
					bw.append("\r\n");

				}
				rbr.close();
				bw.close();

				if (rf2Desc.exists())
					rf2Desc.delete();
				RF2OutputDescFile.renameTo(rf2Desc) ;
				
				RF2OutputDescFile= new File("destination/tmp","pch_" + rf2Lang.getName());

				fos = new FileOutputStream( RF2OutputDescFile);
				osw = new OutputStreamWriter(fos,"UTF-8");
				bw = new BufferedWriter(osw);

				rfis = new FileInputStream(rf2Lang	);
				risr = new InputStreamReader(rfis,"UTF-8");
				rbr = new BufferedReader(risr);


				header=rbr.readLine();
				bw.append(header);
				bw.append("\r\n");

				nextLine=null;
				splittedLine=null;
				while ((nextLine= rbr.readLine()) != null) {
					splittedLine = nextLine.split("\t",-1);
					if (splittedLine[0].equals("1b8f3ad6-9d9e-5692-a0b9-4d04b2dc9332")
							 || splittedLine[0].equals("66c4eb52-1fe6-5879-9f70-649d3c4b03c6")
							 || splittedLine[0].equals("fa4cbe6f-61d7-537b-a639-167445a45560")
							 || splittedLine[0].equals("2773b04c-ec8c-54f2-a605-b6c928215a33")
							 || splittedLine[0].equals("69eb7211-5e6c-5cab-a89e-a9bfd6ab1beb")
							 || splittedLine[0].equals("d8e0bf7c-1831-5afc-a867-4e0e6f9006a4")
							 || splittedLine[0].equals("17f2ce90-57e5-50e8-8c0b-2319916c387d")
							 || splittedLine[0].equals("d9cfcabd-1239-5ff2-aeae-fce76409c971")
							 ){
						continue;
					}
					bw.append(splittedLine[0]);
					bw.append("\t");
					bw.append(splittedLine[1]);
					bw.append("\t");
					bw.append(splittedLine[2]);
					bw.append("\t");
					bw.append(splittedLine[3]);
					bw.append("\t");
					bw.append(splittedLine[4]);
					bw.append("\t");
					bw.append(splittedLine[5]);
					bw.append("\t");
					bw.append(splittedLine[6]);
					bw.append("\r\n");

				}
				rbr.close();
				bw.close();

				if (rf2Lang.exists())
					rf2Lang.delete();
				RF2OutputDescFile.renameTo(rf2Lang) ;
				
				rfis=null;
				risr=null;
				System.gc();
				long end1 = System.currentTimeMillis();
				long elapsed1 = (end1 - start1);
				System.out.println(lines + " lines in output file  : " + RF2OutputDescFile.getAbsolutePath());
				System.out.println("Completed in " + elapsed1 + " ms");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
