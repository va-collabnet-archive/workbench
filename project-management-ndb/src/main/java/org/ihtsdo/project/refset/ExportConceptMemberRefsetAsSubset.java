package org.ihtsdo.project.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.refset.spec.I_HelpSpecRefset;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;

public class ExportConceptMemberRefsetAsSubset {
	BufferedWriter outputFileWriter;
	BufferedWriter reportFileWriter;
	Long lineCount;
	ConceptMembershipRefset refsetConcept;
	UUID refsetUUID;
	I_HelpRefsets refsetHelper ;
	I_TermFactory termFactory;
	I_Identify id;
	public ExportConceptMemberRefsetAsSubset(){
		termFactory = Terms.get();

	}
	public Long[] exportFile(File exportFile, File reportFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

		reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile),"UTF8"));
		outputFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportFile),"UTF8"));

		String begEnd;
		String sep;
		if (exportToCsv){
			sep="\",\"";
			begEnd="\"";
		}else{
			sep="\t";
			begEnd="";
		}
		lineCount = 0l ;
		RefsetUtilImpl rUtil=new RefsetUtilImpl();
		String subsetId=rUtil.getSnomedId(refsetConcept.getNid(),Terms.get()).toString();
	
		String memberStatus="0";
		String linkedId="";
		Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
				refsetConcept.getConceptNid());
		
		//TODO: move config to parameter
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(config);
		for (I_ExtendByRef ext : extensions) {

			I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(ext);
			if (lastPart.getStatusNid()!=ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()){
//			List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, 
//					config.getPrecedence(),
//					config.getConflictResolutionStrategy());
//
//			if (tuples.size() > 0) {
//				I_ExtendByRefVersion thinTuple = lastPart.get(0);
				String conceptId = null;
				try {
					conceptId = rUtil.getSnomedId (ext.getComponentNid(),Terms.get());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
				try{
					Long.parseLong(conceptId);
				}catch(NumberFormatException e){
					conceptId=null;
				}
				if (conceptId ==null){

					reportFileWriter.append("The concept UUID " +  termFactory.getUids(ext.getComponentNid()).iterator().next() + " has not Snomed Concept ID." + "\r\n");
//					reportFileWriter.newLine();
				}else{
//				UUID typeUuid = termFactory.getUids(thinTuple.getTypeId()).iterator().next();
//				if (!typeUuid.equals(RefsetAuxiliary.Concept.CONCEPT_EXTENSION.getUids().iterator().next())) {
//
//					reportFileWriter.println("Error on line " + lineCount + " : ");
//					reportFileWriter.println("The concept UUID " + termFactory.getUids(thinTuple.getTypeId()).iterator().next() + " has not concept extension (CONCEPT_EXT).");
////					reportFileWriter.newLine();
//					reportFileWriter.flush();
//					reportFileWriter.close();
//					outputFileWriter.flush();
//					outputFileWriter.close();
//					throw new TerminologyException("Non concept ext tuple passed to export concepts to file .");
//				}
					outputFileWriter.append(begEnd + subsetId + sep + conceptId + sep + memberStatus + sep + linkedId + begEnd + "\r\n");
					lineCount++;
				}
			}
		}

		reportFileWriter.append("Exported to file " + exportFile.getName()  + " : " + lineCount + " lines" + "\r\n");
		reportFileWriter.flush();
		reportFileWriter.close();
		outputFileWriter.flush();
		outputFileWriter.close();
		
		return new Long[]{lineCount,0l};
	}
}