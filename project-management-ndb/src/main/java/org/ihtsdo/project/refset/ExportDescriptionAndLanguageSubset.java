package org.ihtsdo.project.refset;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HelpRefsets;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.task.refset.members.RefsetUtilImpl;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.KindOfCacheBI;

public class ExportDescriptionAndLanguageSubset implements I_ProcessConcepts{
	PrintWriter outputDescFileWriter;
	PrintWriter reportFileWriter;
	int lineCount;
	I_GetConceptData refsetConcept;
	UUID refsetUUID;
	I_HelpRefsets refsetHelper ;
	I_TermFactory termFactory;
	I_Identify id;
	SimpleDateFormat formatter;
	private PrintWriter outputSubsFileWriter;
	private int CONCEPT_RETIRED;
	private int LIMITED;
	private int FSN;
	private int PREFERRED;
	private RefsetUtilImpl rUtil;
	private String sep;
	private String begEnd;
	private long descLineCount;
	private long subsLineCount;
	private String refsetSCTID;
	private File exportDescFile;
	private File exportSubsFile;
	private HashSet<Integer> excludedStatus;
	private boolean completeWithCoreTems;
	private I_GetConceptData promoRefset;
	private I_ConfigAceFrame config;
	private I_GetConceptData snomedRoot;
	private I_GetConceptData sourceRefset;
	public static KindOfCacheBI myStaticIsACache;
	public static KindOfCacheBI myStaticIsACacheRefsetSpec;

	public ExportDescriptionAndLanguageSubset(I_ConfigAceFrame config ,File exportDescFile, 
			File exportSubsFile, File reportFile, I_GetConceptData refsetConcept, 
			HashSet<Integer> excludedStatus,I_GetConceptData sourceRefset, boolean completeWithCoreTems) throws Exception{
		termFactory = Terms.get();
		this.config=config;
		this.sourceRefset=sourceRefset;
		termFactory.setActiveAceFrameConfig(config);
		formatter=new SimpleDateFormat("yyyyMMdd");
		this.exportDescFile=exportDescFile;
		this.exportSubsFile=exportSubsFile;
		this.refsetConcept=refsetConcept;
		this.excludedStatus=excludedStatus;
		this.completeWithCoreTems=completeWithCoreTems;
		try {
			CONCEPT_RETIRED=Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids()).getConceptNid();
			LIMITED=Terms.get().getConcept(ArchitectonicAuxiliary.Concept.LIMITED.getUids()).getConceptNid();
			FSN=Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getConceptNid();
			PREFERRED=Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()).getConceptNid();

			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));


			reportFileWriter = new PrintWriter(new FileWriter(reportFile));
			outputDescFileWriter = new PrintWriter(new FileWriter(exportDescFile));
			outputSubsFileWriter = new PrintWriter(new FileWriter(exportSubsFile));


			Set<? extends I_GetConceptData> promRefsets = termFactory.getRefsetHelper(config).getPromotionRefsetForRefset(refsetConcept, config);
			promoRefset = promRefsets.iterator().next();
			if (promoRefset==null){
				reportFileWriter.println("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists.");
				throw new Exception("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists.");
			}else{

				sep="\t";
				begEnd="";

				outputDescFileWriter.append(begEnd);
				outputDescFileWriter.append("DescriptionId");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("DescriptionStatus");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("ConceptId");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("Term");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("InitialCapitalStatus");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("DescriptionType");
				outputDescFileWriter.append(sep);
				outputDescFileWriter.append("LanguageCode");
				outputDescFileWriter.println(begEnd);
				

				outputSubsFileWriter.append(begEnd);
				outputSubsFileWriter.append("SubsetId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberStatus");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("LinkedId");
				outputSubsFileWriter.println(begEnd);
				
				descLineCount = 0l;
				subsLineCount = 0l;

				myStaticIsACache = Ts.get().getCache(config.getViewCoordinate());


				rUtil=new RefsetUtilImpl();
				refsetSCTID=rUtil.getSnomedId(refsetConcept.getNid(),termFactory).toString();
				try{
					Long.parseLong(refsetSCTID);
				}catch(NumberFormatException e){
					refsetSCTID=refsetConcept.getUUIDs().iterator().next().toString();
					reportFileWriter.println("The refset UUID " + refsetSCTID + " has not Snomed Concept ID, It will be replaced with its UUID.");

				}
			}

		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Long[] getResults() {
		return new Long[]{descLineCount,subsLineCount};
	}
	private boolean writeTerms(I_GetConceptData concept, I_GetConceptData refset) {
		List<ContextualizedDescription> descriptions;
		boolean bwrite=false;
		try {
			descriptions = ContextualizedDescription.getContextualizedDescriptions(
					concept.getConceptNid(), refset.getConceptNid(), true);

			I_ConceptAttributeVersioned attrib = concept.getConceptAttributes();
			int conceptStatus=attrib.getStatusNid();

			String conceptId=rUtil.getSnomedId(concept.getConceptNid(),termFactory).toString();
			try{
				Long.parseLong(conceptId);
			}catch(NumberFormatException e){
				conceptId=concept.getUUIDs().iterator().next().toString();

				reportFileWriter.println("The concept " + conceptId + " has not Snomed Concept ID, It will be replaced with its UUID.");

			}
			for (I_ContextualizeDescription cdescription : descriptions) {
				if (cdescription.getLanguageExtension()!=null){

					String did=rUtil.getSnomedId(cdescription.getDescId(),termFactory).toString();
					try{
						Long.parseLong(did);
					}catch(NumberFormatException e){
						did=cdescription.getDescriptionVersioned().getUUIDs().iterator().next().toString();
						reportFileWriter.println("The description " + did + " has not Snomed Description ID, It will be replaced with its UUID.");

					}
					String dStatus="";
					if (cdescription.getDescriptionStatusId()==ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid() ||
							cdescription.getDescriptionStatusId()==ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid()){
						dStatus="1";
					}else{
						dStatus="0";
					}
					if (dStatus=="0"){

						if (conceptStatus==CONCEPT_RETIRED){

							dStatus="8";
						}else if (conceptStatus==LIMITED){
							dStatus="6";
						}
					}


					String lang=cdescription.getLang();

					int typeId = cdescription.getTypeId();
					String dType="";
					if (typeId==FSN){
						dType="3";
					}else{
						int acceptId=cdescription.getAcceptabilityId();
						if (acceptId==PREFERRED){
							dType="1";
						}else{
							dType="2";
						}
					}
					String term=cdescription.toString();

					String ics= cdescription.isInitialCaseSignificant()? "1":"0";


					outputDescFileWriter.println(begEnd + did + sep + dStatus + sep + conceptId + sep + term + sep + ics + sep + dType + sep + lang + begEnd);

					descLineCount++;

					if (dStatus=="0"){
						outputSubsFileWriter.println(begEnd + refsetSCTID + sep + did + sep + dType + sep + begEnd);

						subsLineCount++;
					}
					bwrite=true;
				}
			}
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bwrite;
	}
	@Override
	public void processConcept(I_GetConceptData concept) throws Exception {

		if (myStaticIsACache.isKindOf(concept.getConceptNid(), snomedRoot.getConceptNid())){

			Integer statusId=TerminologyProjectDAO.getPromotionStatusIdForRefsetId(promoRefset.getConceptNid(), concept.getConceptNid(), config);

			if (statusId==null){
				if (!writeTerms(concept,refsetConcept)){
					if (completeWithCoreTems){
						writeTerms(concept, sourceRefset);
					}
				}
			}else if (excludedStatus.contains(statusId) && completeWithCoreTems){
				writeTerms(concept, sourceRefset);
			}else if (!excludedStatus.contains(statusId)){
				writeTerms(concept, refsetConcept);
			}

		}

	}
	public void closeFiles(){
		reportFileWriter.println("Exported to UUID file " + exportDescFile.getName()  + " : " + descLineCount + " lines");
		reportFileWriter.println("Exported to SCTID file " + exportSubsFile.getName()  + " : " + subsLineCount + " lines");
		reportFileWriter.flush();
		reportFileWriter.close();
		outputDescFileWriter.flush();
		outputDescFileWriter.close();
		outputSubsFileWriter.flush();
		outputSubsFileWriter.close();	
	}
}