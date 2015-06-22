package org.ihtsdo.rf2.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class RedundantRowsFix {

	private int[] idColumns;
	private File sortedfile;
	private Integer[] fieldsToCompare;
	private File outputFile;

	public static enum FILE_TYPE{
		RF1_CONCEPT(new int[]{0},new Integer[]{1,2,3,4,5}),
		RF1_DESCRIPTION(new int[]{0},new Integer[]{1,2,3,4,5,6}),
		RF1_RELATIONSHIP(new int[]{0},new Integer[]{1,2,3,4,5,6}),
		RF1_SUBSET(new int[]{1},new Integer[]{2}),
		RF1_COMPONENT_HISTORY(new int[]{0,1},new Integer[]{2,3}),

		RF2_CONCEPT(new int[]{0,1},new Integer[]{2,3,4}),
		RF2_DESCRIPTION(new int[]{0,1},new Integer[]{2,3,4,5,6,7,8}),
		RF2_RELATIONSHIP(new int[]{0,1},new Integer[]{2,3,4,5,6,7,8,9}), 
		RF2_STATED_RELATIONSHIP(new int[]{0,1},new Integer[]{2,3,4,5,6,7,8,9}), 
		RF2_QUALIFIER(new int[]{0,1},new Integer[]{2,4,5,6,7,8,9}), 
		RF2_LANGUAGE_REFSET(new int[]{0,1},new Integer[]{2,3,4,5,6}), 
		RF2_ATTRIBUTE_VALUE(new int[]{0,1},new Integer[]{2,3,4,5,6}),
		RF2_ASSOCIATION(new int[]{0,1},new Integer[]{2,3,4,5,6}),
		RF2_SIMPLE_MAP(new int[]{0,1},new Integer[]{2,3,4,5,6}),
		RF2_SIMPLE(new int[]{0,1},new Integer[]{2,3,4,5}),
		RF2_TEXTDEFINITION(new int[]{0,1},new Integer[]{2,3,4,5,6,7,8}),
		RF2_ICD9_MAP(new int[]{0,1},new Integer[]{2,3,4,5,6,7,8,9,10,11});


		private int[] columnIndexes;
		private Integer[] columnsToCompare;

		public Integer[] getColumnsToCompare() {
			return columnsToCompare;
		}

		FILE_TYPE(int[] columnIndexes,Integer[] columnsToCompare){
			this.columnIndexes=columnIndexes;
			this.columnsToCompare=columnsToCompare;
		}

		public int[] getColumnIndexes() {
			return columnIndexes;
		}
	};
	public RedundantRowsFix(File sortedfile,FILE_TYPE fileType, int[] idColumns,Integer[]fieldsToCompare, File outputFile) {
		super();
		this.sortedfile = sortedfile;
		if (idColumns!=null){
			this.idColumns = idColumns;
		}else
			this.idColumns=fileType.getColumnIndexes();
		
		if (fieldsToCompare!=null)
			this.fieldsToCompare=fieldsToCompare;
		else
			this.fieldsToCompare=fileType.getColumnsToCompare();
		
		this.outputFile=outputFile;
	}

	public boolean Fix(){
		boolean sortedSnap=true;
		boolean same=false;
		try {
			long start1 = System.currentTimeMillis();

			File fTmp = new File(sortedfile.getParentFile()  + "/tmpf_" + sortedfile.getName());

			if (fTmp.exists()){
				fTmp.delete();
			}
			FileOutputStream fos = new FileOutputStream( fTmp);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			FileInputStream fis = new FileInputStream(sortedfile);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);

			double lines = 0;
			String nextLine;
			String header=br.readLine();
			bw.append(header);
			bw.append("\r\n");
			
			String[] splittedLine;
			String[] prevSplittedLine=null;
			int comp;

			String prevValues[]=new String[idColumns.length];

			if ((nextLine= br.readLine()) != null) {
				prevSplittedLine =nextLine.split("\t",-1);

				for (int i=0;i<idColumns.length;i++){
					prevValues[i]=prevSplittedLine[idColumns[i]];
				}
				writeLine(bw,nextLine);
				lines++;
			}
			if ( nextLine!=null) {
				while ((nextLine= br.readLine()) != null ) {
					splittedLine = nextLine.split("\t",-1);

					if (splittedLine[1].equals("245417002")){
						boolean bstop=true;
					}

					for (int i=0;i<idColumns.length;i++){
						same=false;
						comp=prevValues[i].compareTo(splittedLine[idColumns[i]]);
						if (comp<0){
							break;
						}
						if (comp>0){
							break;
						}
						same=true;
					}
					if (same){

						if (fieldsCompare(prevSplittedLine,splittedLine)!=0){
							writeLine(bw,nextLine);
							lines++;
						}else{
							sortedSnap=false;
						}
					}else{
						writeLine(bw,nextLine);
						lines++;
					}
					for (int i=0;i<idColumns.length;i++){
						prevValues[i]=splittedLine[idColumns[i]];
					}
					prevSplittedLine=splittedLine;
				}

			}
			br.close();
			bw.close();
			fos.close();
			osw.close();
			fis.close();
			isr.close();
			br=null;
			bw=null;
			fos=null;
			osw=null;
			fis=null;
			isr=null;
			

			if (outputFile.exists()){
				outputFile.delete();
			}
			if (fTmp.exists()){
				fTmp.renameTo(outputFile);
			}
			if (sortedSnap){
				System.out.println("No duplicated rows has been found in file " + sortedfile.getName());
			}else{
				System.out.println("Duplicated rows has been found in file " + sortedfile.getName() + ".\n They were deleted in output file " + outputFile.getName());
			}
			long end1 = System.currentTimeMillis();
			long elapsed1 = (end1 - start1);
			System.out.println("Completed in " + elapsed1 + " ms");
			return sortedSnap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void writeLine(BufferedWriter bw, String nextLine) {
		try {
			bw.append(nextLine);
			bw.append("\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	private int fieldsCompare(String[] splittedLine1, String[] splittedLine2) {
		int iComp;
		for (int i : fieldsToCompare){
			iComp=splittedLine1[i].compareTo(splittedLine2[i]);
			if (iComp!=0)
				return iComp;
		}
		return 0;
	}
}
