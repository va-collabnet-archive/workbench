package org.ihtsdo.project.refset;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
import org.ihtsdo.project.TerminologyProjectDAO;

public class ExportConceptMemberRefsetToRefset {
	PrintWriter outputFileWriter;
	PrintWriter reportFileWriter;
	int lineCount;
	ConceptMembershipRefset refsetConcept;
	UUID refsetUUID;
	I_HelpRefsets refsetHelper ;
	I_TermFactory termFactory;
	I_Identify id;
	SimpleDateFormat formatter;
	private PrintWriter outputFileWriter2;
	
	public ExportConceptMemberRefsetToRefset(){
		termFactory = Terms.get();
		formatter=new SimpleDateFormat("yyyyMMddHHmmss");

	}
	public Long[] exportFile(File exportFile, File exportFile2, File reportFile, I_GetConceptData refsetConcept, boolean exportToCsv) throws Exception {

		reportFileWriter = new PrintWriter(new FileWriter(reportFile));
		outputFileWriter = new PrintWriter(new FileWriter(exportFile));
		outputFileWriter2 = new PrintWriter(new FileWriter(exportFile2));

		String begEnd;
		String sep;
		if (exportToCsv){
			sep="\",\"";
			begEnd="\"";
		}else{
			sep="\t";
			begEnd="";
		}
		long UUIDlineCount = 0;
		long SCTIDlineCount = 0;
		String refsetId=refsetConcept.getUUIDs().iterator().next().toString();
		Collection<? extends I_ExtendByRef> extensions = Terms.get().getRefsetExtensionMembers(
				refsetConcept.getConceptNid());
		
		I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();

		RefsetUtilImpl rUtil=new RefsetUtilImpl();

		String refsetSCTID=rUtil.getSnomedId(refsetConcept.getNid(),Terms.get()).toString();
		try{
			Long.parseLong(refsetSCTID);
		}catch(NumberFormatException e){

			reportFileWriter.println("The refset UUID " + refsetId + " has not Snomed Concept ID, It will be replaced with its UUID.");

			refsetSCTID=refsetId;
		}
		I_HelpSpecRefset helper = termFactory.getSpecRefsetHelper(config);
		for (I_ExtendByRef ext : extensions) {

			I_ExtendByRefPart lastPart = TerminologyProjectDAO.getLastExtensionPart(ext);
			if (lastPart.getStatusNid()!=ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid()){
//
//			List<? extends I_ExtendByRefVersion> tuples = ext.getTuples(helper.getCurrentStatusIntSet(), null, 
//					config.getPrecedence(),
//					config.getConflictResolutionStrategy());
//
//			if (tuples.size() > 0) {
//				I_ExtendByRefVersion thinTuple = tuples.get(0);

				//UUID file
				String id=ext.getUUIDs().iterator().next().toString();
				String effectiveTime=formatter.format(lastPart.getTime());
				String compoID=Terms.get().getConcept(ext.getComponentNid()).getUUIDs().iterator().next().toString();
				String moduleId = Terms.get().getConcept(lastPart.getPathNid()).getUids().iterator().next().toString();
			
				outputFileWriter.println(begEnd + id + sep + effectiveTime + sep + "1" + sep + moduleId + sep + refsetId + sep + compoID + begEnd);

				UUIDlineCount++;
				
				//SCTID File
				String conceptId = rUtil.getSnomedId (ext.getComponentNid(),termFactory);
				boolean bSkip = false;
				try{
					Long.parseLong(conceptId);
				}catch(NumberFormatException e){
					reportFileWriter.println("The concept UUID " + compoID + " has not Snomed Concept ID.");

					bSkip=true;
				}
				if (!bSkip){
					String sctId_id = rUtil.getSnomedId (ext.getNid(),termFactory);
					try{
						Long.parseLong(sctId_id);
					}catch(NumberFormatException e){
						sctId_id=id;
					}
					String sctId_moduleId = rUtil.getSnomedId (lastPart.getPathNid(),termFactory);
					try{
						Long.parseLong(sctId_moduleId);
					}catch(NumberFormatException e){
						sctId_moduleId=moduleId;
					}
					outputFileWriter2.println(begEnd + sctId_id + sep + effectiveTime + sep + "1" + sep + sctId_moduleId + sep + refsetSCTID + sep + conceptId + begEnd);

					SCTIDlineCount++;
					
				}
			}
		}
		reportFileWriter.println("Exported to UUID file " + exportFile.getName()  + " : " + UUIDlineCount + " lines");
		reportFileWriter.println("Exported to SCTID file " + exportFile2.getName()  + " : " + SCTIDlineCount + " lines");
		reportFileWriter.flush();
		reportFileWriter.close();
		outputFileWriter.flush();
		outputFileWriter.close();
		outputFileWriter2.flush();
		outputFileWriter2.close();
		
		return new Long[]{UUIDlineCount,SCTIDlineCount};
	}
}