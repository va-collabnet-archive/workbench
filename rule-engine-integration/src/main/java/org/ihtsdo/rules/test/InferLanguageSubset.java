package org.ihtsdo.rules.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class InferLanguageSubset {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(
			        new File("")), "UTF-8"));
			
			PrintWriter pw = new PrintWriter(new FileWriter("", true));
			pw.println("SUBSETID\tMEMBERID\tMEMBERSTATUS\tLINKEDID");

			int DESCRIPTIONID = 0;
			int DESCRIPTIONSTATUS = 1;
			int CONCEPTID = 2;
			int TERM = 3;
			int INITIALCAPITALSTATUS = 4;
			int DESCRIPTIONTYPE = 5;
			int LANGUAGECODE = 6;

			// Header row
			r.readLine();

			while (r.ready()) {
			    String[] line = r.readLine().split("	");

			    // DESCRIPTIONID
			    long descriptionId = Long.parseLong(line[DESCRIPTIONID]);
			    // DESCRIPTIONSTATUS
			    int status = Integer.parseInt(line[DESCRIPTIONSTATUS]);
			    // CONCEPTID
			    long conSnoId = Long.parseLong(line[CONCEPTID]);
			    // TERM
			    String text = line[TERM];
			    // INITIALCAPITALSTATUS
			    int capStatus = Integer.parseInt(line[INITIALCAPITALSTATUS]);
			    // DESCRIPTIONTYPE
			    int typeInt = Integer.parseInt(line[DESCRIPTIONTYPE]);
			    // LANGUAGECODE
			    String lang = line[LANGUAGECODE];
			    
			    String subsetMembersRow = "121" + "\t" + descriptionId + "\t" + typeInt + "\t";
			    pw.println(subsetMembersRow);

			}
			pw.close();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
