/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

import org.dwfa.ace.log.AceLog;

/**
 * The Class InferLanguageSubset.
 */
public class InferLanguageSubset {

	/**
	 * The main method.
	 *
	 * @param args the arguments
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
			AceLog.getAppLog().alertAndLogException(e);
		} catch (UnsupportedEncodingException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (FileNotFoundException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (IOException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}

	}

}
