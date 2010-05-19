/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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

/**
 * @author Adam Flinton
 */

package org.ihtsdo.mojo.maven.importer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Writer;

public class ImportFileWriters {
	
	public String effectiveDate;
	public String path;
	public String path_UUID_S;
	
	/**
	 * Sets up the class with the common values of the effective date and the path UUID as a String
	 * @param effDate - The effective Date
	 * @param pathFSUUID_S - The Path UUID as a String
	 */
	public void init(String effDate, String path_S){
		setEffectiveDate(effDate);
		setPath(path_S);
		System.out.println("ImportFileWriters init effectiveDate "+effectiveDate +" path = "+path);
		
	}
	/**
	 * Sets up the class if the Path is already a UUID (e.g. if getting a Concept Path from ArchitectonicAuxillary)
	 * @param effDate
	 * @param path_UUID_Str
	 */
	
	public void initUUID_S(String effDate, String path_UUID_Str){
		setEffectiveDate(effDate);
		setPath_UUID_S(path_UUID_Str);
		System.out.println("ImportFileWriters initUUID_S effectiveDate "+effectiveDate +" path = "+path_UUID_S);
		
	}
	
	/**
	 * Writes a line to the relationships file (relationships.txt)
	 * 
	 * @param writer - the Writer class set up and pointing to relationships.txt
	 * @param relUUID_S - The Relationship UUID as a string
	 * @param status_UUID_S - The Status UUID as a String
	 * @param sourceConcept_UUID_S
	 * @param reltype_UUID_S
	 * @param destConcept_UUID_S
	 * @param charType_UUID_S
	 * @param refin_UUID_S
	 * @param relGroup_S
	 */
	
	public void writeRelationshipLine(Writer writer, String relUUID_S,String status_UUID_S,String sourceConcept_UUID_S,String reltype_UUID_S,
			String destConcept_UUID_S,String charType_UUID_S,String refin_UUID_S,String relGroup_S){

		//relationship UUID
		writeLine(writer,relUUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//source Concept UUID
		writeLine(writer,sourceConcept_UUID_S);
		//relationship type UUID
		writeLine(writer,reltype_UUID_S);		
		//destination Concept UUID
		if(destConcept_UUID_S == null){
			System.out.println("destConcept_UUID_S is null ");
		}
		writeLine(writer,destConcept_UUID_S);
		//characteristic type UUID
		writeLine(writer,charType_UUID_S);
		//refinability UUID
		writeLine(writer,refin_UUID_S);
		//relationship group usually 0
		writeLine(writer,relGroup_S);
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLastLine(writer,path_UUID_S);		
	}
	
	/**
	 * 
	 * @param writer
	 * @param conceptUUID_S
	 * @param primitive_S
	 * @param status_UUID_S
	 */
	
	public void writeConceptLine(Writer writer, String conceptUUID_S, String primitive_S, String status_UUID_S){
		
		//conceptUUID
		writeLine(writer,conceptUUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//primitive
		writeLine(writer,primitive_S);
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLastLine(writer,path_UUID_S);		
	}
	
	/**
	 * 
	 * @param writer
	 * @param descUUID_S
	 * @param status_UUID_S
	 * @param concept_UUID_S
	 * @param term_S
	 * @param capStatus_S
	 * @param descType_UUID_S
	 * @param langCode_S
	 */
	
	public void writeDescriptionLine(Writer writer, String descUUID_S,String status_UUID_S,String concept_UUID_S,String term_S,
			String capStatus_S,String descType_UUID_S,String langCode_S){

		//Description UUID
		writeLine(writer,descUUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//Concept UUID
		writeLine(writer,concept_UUID_S);
		//term
		writeLine(writer,term_S);		
		//capitalization status boolean assume 1 unless known
		writeLine(writer,capStatus_S);
		//description type uuid
		writeLine(writer,descType_UUID_S);
		//language code
		writeLine(writer,langCode_S);
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLastLine(writer,path_UUID_S);		
	}
	
	/**
	 * 
	 * @param writer
	 * @param primaryUUID_S
	 * @param status_UUID_S
	 * @param sourceSystem_UUID_S
	 * @param source_id_S
	 */
	
	public void writeIDLine(Writer writer, String primaryUUID_S,String status_UUID_S,String sourceSystem_UUID_S,String source_id_S){

		//Primary UUID
		writeLine(writer,primaryUUID_S);
		//source system uuid
		writeLine(writer,sourceSystem_UUID_S);
		//source id
		writeLine(writer,source_id_S);		
		//statusUUID
		writeLine(writer,status_UUID_S);
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLastLine(writer,path_UUID_S);		
	}
	
	/**
	 * 
	 * @param writer
	 * @param illicitWord_S
	 */
	
	public void writeIllicitLine(Writer writer, String illicitWord_S){
		//Illicit word
		writeLastLine(writer,illicitWord_S);		
	}
	
	/**
	 * 
	 * @param writer
	 * @param licitWord_S
	 */
	
	public void writeLicitLine(Writer writer, String licitWord_S){
		//Licit word
		writeLastLine(writer,licitWord_S);		
	}
	
	//RefSets
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param bl_ext_S
	 */
	
	public void writeBooleanRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,String bl_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//boolean extension value
		writeLastLine(writer,bl_ext_S);	
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param concept_ext_S
	 */
	
	public void writeConceptRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,String concept_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//concept extension value
		writeLastLine(writer,concept_ext_S);	
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param concept_ext_S
	 * @param int_ext_S
	 */
	
	public void writeConceptIntRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,String concept_ext_S,
			String int_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//concept extension value
		writeLine(writer,concept_ext_S);
		//integer extension value
		writeLastLine(writer,int_ext_S);
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param dp_ext_S
	 * @param uom_ext_S
	 */
	
	public void writeMeasurementefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S
			,String dp_ext_S,String uom_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//Double precision floating point number representing the measurement extension value
		writeLine(writer,dp_ext_S);
		//units of measure concept
		writeLastLine(writer,uom_ext_S);
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param int_ext_S
	 */
	
	public void writeIntRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,String int_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//integer extension value
		writeLastLine(writer,int_ext_S);
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param string_ext_S
	 */
	
	public void writeStringRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,String string_ext_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//String extension value
		writeLastLine(writer,string_ext_S);
	}
	
	/**
	 * 
	 * @param writer
	 * @param refSet_UUID_S
	 * @param status_UUID_S
	 * @param member_UUID_S
	 * @param component_UUID_S
	 * @param accept_ext_UUID_S
	 * @param correct_ext_UUID_S
	 * @param synonymy_ext_UUID_S
	 */

	public void writeLangRefSetLine(Writer writer, String refSet_UUID_S,String status_UUID_S,String member_UUID_S,String component_UUID_S,
			String accept_ext_UUID_S,String correct_ext_UUID_S,String synonymy_ext_UUID_S){
		//refset uuid
		writeLine(writer,refSet_UUID_S);
		//member uuid
		writeLine(writer,member_UUID_S);
		//statusUUID
		writeLine(writer,status_UUID_S);
		//component uuid
		writeLine(writer,component_UUID_S);	
		// effective date
		writeLine(writer,effectiveDate);
		// pathUUID
		writeLine(writer,path_UUID_S);	
		//acceptability extension value UUID
		writeLine(writer,accept_ext_UUID_S);
		//correctness extension value UUID
		writeLine(writer,correct_ext_UUID_S);
		//synonymy value UUID
		writeLastLine(writer,synonymy_ext_UUID_S);
	}
	
	/**
	 * 
	 * @param writer
	 * @param content
	 */
	
	public void writeLine(Writer writer, String content){
	
		try {
			writer.append(content);
			writer.append("\t");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * 
	 * @param writer
	 * @param content
	 */
	
	public void writeLastLine(Writer writer, String content){
		
		try {
			writer.append(content);
			writer.append("\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
		setPath_UUID_S(ImportStatics.getPathUUID_S(path));
	}

	public String getPath_UUID_S() {
		
		if(path_UUID_S == null || path_UUID_S.length() == 0){
		if(getPath() == null || getPath().length() == 0){
			setPath("DefaultPath");
			System.out.println("Path not set so setting to default of "+getPath());
		}
		}
		return path_UUID_S;
	}

	public void setPath_UUID_S(String pathUUIDS) {
		path_UUID_S = pathUUIDS;
	}

	public String getEffectiveDate() {
		
		if(effectiveDate == null || effectiveDate.length() == 0){
			effectiveDate = "20091012 00:00:00";
			System.out.println("effectiveDate not set so setting to default of "+effectiveDate);
		}
		return effectiveDate;
	}

	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	/**
	 * A debug method for counting the number of lines in a file (e.g. doing a before and after debug method)
	 * @param file
	 * @return
	 * @throws Exception
	 */
	
	public int getLineCount(File file)throws Exception{
		int count = 0;
		if (file.exists()){
	        FileReader fr = new FileReader(file);
	     LineNumberReader ln = new LineNumberReader(fr);
	        while (ln.readLine() != null){
	          count++;
	   }	 
	        ln.close();
	 	      }
	      else{
	       throw new Exception("File does not exist!");
	     }
		return count;
	}

	
}
