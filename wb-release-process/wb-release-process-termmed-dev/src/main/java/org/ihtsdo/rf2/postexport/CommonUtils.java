package org.ihtsdo.rf2.postexport;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CommonUtils {
	private static String newLine="\r\n";
	public static String[] getSmallestArray(HashMap<BufferedReader, String[]> passedMap, int[] sortColumns) {

		List<String[]> mapValues = new ArrayList<String[]>(passedMap.values());
		//
		Collections.sort(mapValues, new ArrayComparator(sortColumns, false));

		return mapValues.get(0);
	}

	public static String[] getRefsetIds(File file) {
		String[] result = new String[]{};
		HashSet<String> hashRefset = new HashSet<String>();

		FileInputStream ifis;
		try {
			ifis = new FileInputStream(file);
			InputStreamReader iisr = new InputStreamReader(ifis, "UTF-8");
			BufferedReader ibr = new BufferedReader(iisr);

			ibr.readLine();
			String line;
			String[] splittedLine;
			String refsetId;
			while ((line = ibr.readLine()) != null) {
				splittedLine = line.split("\t", -1);
				refsetId = splittedLine[4];
				hashRefset.add(refsetId);
			}
			ibr.close();
			ifis = null;
			iisr = null;
			ibr = null;
			System.gc();
			result = hashRefset.toArray(new String[] {});
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	public static void MergeFile(HashSet<File> hFile, File outputfile) {

		try{
			if (outputfile.exists())
				outputfile.delete();

			outputfile.createNewFile();

			String fileName=outputfile.getName();
			File fTmp = new File(outputfile.getParentFile()  + "/tmp_" + fileName);


			boolean first = true;
			String nextLine;
			for (File file:hFile){


				if (fTmp.exists())
					fTmp.delete();

				FileOutputStream fos = new FileOutputStream( fTmp);
				OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
				BufferedWriter bw = new BufferedWriter(osw);

				FileInputStream fis = new FileInputStream(file	);
				InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
				BufferedReader br = new BufferedReader(isr);

				FileInputStream ofis = new FileInputStream(outputfile	);
				InputStreamReader oisr = new InputStreamReader(ofis,"UTF-8");
				BufferedReader obr = new BufferedReader(oisr);


				nextLine=br.readLine();
				if (first && nextLine!=null){
					bw.append(nextLine);
					bw.append(newLine);
					first=false;
				}

				while ((nextLine=obr.readLine())!=null){
					bw.append(nextLine);
					bw.append(newLine);

				}
				while ((nextLine=br.readLine())!=null){
					bw.append(nextLine);
					bw.append(newLine);

				}
				bw.close();
				br.close();


				if (outputfile.exists())
					outputfile.delete();
				fTmp.renameTo(outputfile) ;
			}

			if (fTmp.exists())
				fTmp.delete();

		} catch (IOException e) {
			e.printStackTrace();
		}finally{

		}
	}

	public static void FilterFile(File file,File outputFile, int filterColumnIndex,ValueAnalyzer valueAnalyzer) {

		try{
			if (outputFile.exists())
				outputFile.delete();

			outputFile.createNewFile();
			String nextLine;


			FileOutputStream fos = new FileOutputStream( outputFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);

			FileInputStream fis = new FileInputStream(file	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader br = new BufferedReader(isr);


			nextLine=br.readLine();
			String[] splittedLine=null;
			bw.append(nextLine);
			bw.append(newLine);

			while ((nextLine=br.readLine())!=null){
				splittedLine=nextLine.split("\t",-1);
				if (valueAnalyzer.StringAnalyze( splittedLine[filterColumnIndex])){
					bw.append(nextLine);
					bw.append(newLine);
				}
			}
			bw.close();
			br.close();



		} catch (IOException e) {
			e.printStackTrace();
		}finally{

		}
	}
}
