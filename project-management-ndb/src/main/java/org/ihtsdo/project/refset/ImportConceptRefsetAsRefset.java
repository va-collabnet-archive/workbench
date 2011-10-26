package org.ihtsdo.project.refset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.id.Type3UuidFactory;

public class ImportConceptRefsetAsRefset {
	PrintWriter outputFileWriter;
	BufferedReader inputFileReader;
	int lineCount;
	int imported;
	int deleted;
	ConceptMembershipRefset refsetConcept;
	UUID refsetUUID;
	I_HelpRefsets refsetHelper;
	private RefsetMemberValueMgr memberValueMgr;
    I_TermFactory termFactory;
	private HashSet<Integer> conIdHash;
	private boolean incremental;
    
    public ImportConceptRefsetAsRefset(){
		termFactory = Terms.get();
    	
    }
	public Integer[] importFromFile(File importFile, File reportFile,String refsetName,int parentId) throws Exception{

		outputFileWriter = new PrintWriter(new FileWriter(reportFile));

		if (!fileDataControl(importFile)){
			outputFileWriter.close();
			throw new Exception("There are errors on input file.");
		}
		inputFileReader = new BufferedReader(new FileReader(importFile));

		String currentLine = inputFileReader.readLine();
		lineCount = 1;
		imported=0;
		refsetUUID=UUID.nameUUIDFromBytes(refsetName.getBytes());
		if (termFactory.hasId(refsetUUID))
			throw new Exception ("The refset already exists");
		else{
			refsetConcept=ConceptMembershipRefset.createNewConceptMembershipRefset(refsetName, parentId);
		}
		memberValueMgr =new RefsetMemberValueMgr(refsetConcept.getRefsetConcept());

		incremental=true;
		while (currentLine != null) {

			if (!currentLine.trim().equals("")) {
				importRefsetLine(currentLine);
			}

            currentLine = inputFileReader.readLine();
            lineCount++;
		}
		inputFileReader.close();

		termFactory.commit();
		outputFileWriter.println("Imported from file " + importFile.getName()  + " : " + imported + " concepts");
		outputFileWriter.flush();
		outputFileWriter.close();
		return new Integer[]{imported,0};
	}
	
	private boolean importRefsetLine (String inputLine) throws Exception {

		String memberId ;
		String[] lineParts = inputLine.split("\t");

		memberId = lineParts[5];

		int conceptMemberId=0;
		try {
			try{
				Long.parseLong(memberId);
				conceptMemberId = termFactory.getId( Type3UuidFactory.fromSNOMED(memberId)).getNid();
			}catch(NumberFormatException e){
				conceptMemberId = termFactory.getId( UUID.fromString(memberId)).getNid();
			}
			memberValueMgr.putConceptMember(conceptMemberId);
			imported++;
			if (!incremental){
				conIdHash.add(conceptMemberId);
			}
		} catch (TerminologyException e1) { 
			String errorMessage = "Cannot find concept for memberId: " + memberId
			+ e1.getLocalizedMessage();
			outputFileWriter.println("Error on line " + lineCount + " : ");
			outputFileWriter.println(errorMessage);
		} 
			
        return true;
	}
	public Integer[] importFromFileToExistRefset(File importFile, File reportFile,
			I_GetConceptData refset, boolean incremental) throws Exception {

		outputFileWriter = new PrintWriter(new FileWriter(reportFile));

		if (!fileDataControl(importFile)){
			outputFileWriter.close();
			throw new Exception("There are errors on input file.");
		}
		
		inputFileReader = new BufferedReader(new FileReader(importFile));
			
		this.incremental=incremental;
		conIdHash=new HashSet<Integer>();
		String currentLine = inputFileReader.readLine();
		lineCount = 1;
		imported=0;
		deleted=0;

//		refsetConcept=new ConceptMembershipRefset(refset);
//		memberValueMgr =new RefsetMemberValueMgr(refsetConcept.getRefsetConcept());
		
		memberValueMgr =new RefsetMemberValueMgr(refset);
		while (currentLine != null) {

			if (!currentLine.trim().equals("")) {
				importRefsetLine(currentLine);
			}
            currentLine = inputFileReader.readLine();
            lineCount++;
		}
		inputFileReader.close();
		if (!incremental){
			inactivateNotExistentMembers(refset);
		}
		termFactory.commit();
		outputFileWriter.println("Imported from file " + importFile.getName()  + " : " + imported + " concepts");

		if (!incremental){
			outputFileWriter.println("Deleted from Refset " + refset.toString() +  " : " + deleted + " concepts");
		}
		outputFileWriter.flush();
		outputFileWriter.close();

		return new Integer[]{imported,deleted};
		
	}
	private boolean fileDataControl(File importFile) throws IOException {
		BufferedReader inputFileReaderCtrl = new BufferedReader(new FileReader(importFile));
		
		boolean ret=true;
		HashSet<String> conUuidHash = new HashSet<String>();
		String currentLine = inputFileReaderCtrl.readLine();
		lineCount=1;
		String memberId ;
		while (currentLine != null) {

			if (!currentLine.trim().equals("")) {
				String[] lineParts = currentLine.split("\t");
				memberId = lineParts[5];
				if (conUuidHash.contains(memberId)){
					ret=false;
		            outputFileWriter.println("Error on line " + lineCount + " : ");
					outputFileWriter.println("Duplicated component " + memberId);
				}else{
					conUuidHash.add(memberId);
				}

			}
            currentLine = inputFileReaderCtrl.readLine();
            lineCount++;
		}
		inputFileReaderCtrl.close();
		outputFileWriter.flush();
		return ret;
		
	}
	private void inactivateNotExistentMembers(I_GetConceptData concept) throws Exception {
		
		Collection<? extends I_ExtendByRef> extensions=termFactory.getRefsetExtensionMembers(concept.getConceptNid());
		for (I_ExtendByRef extension : extensions) {
			if (!conIdHash.contains(extension.getComponentNid())){
				memberValueMgr.delConceptMember(extension.getComponentNid());
				deleted++;
			}

		}
		
	}
}
