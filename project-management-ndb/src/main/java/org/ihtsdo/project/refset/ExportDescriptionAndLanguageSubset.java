package org.ihtsdo.project.refset;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

public class ExportDescriptionAndLanguageSubset implements I_ProcessConcepts{
	BufferedWriter outputDescFileWriter;
	BufferedWriter reportFileWriter;
	int lineCount;
	I_GetConceptData refsetConcept;
	UUID refsetUUID;
	I_HelpRefsets refsetHelper ;
	I_TermFactory termFactory;
	I_Identify id;
	SimpleDateFormat formatter;
	private BufferedWriter outputSubsFileWriter;
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
			CONCEPT_RETIRED=SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getLenient().getNid();
			LIMITED=SnomedMetadataRf2.LIMITED_COMPONENT_RF2.getLenient().getNid();
			FSN=SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid();
			//TODO change logic for detect preferred from language refset
			PREFERRED=SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid();

			snomedRoot = Terms.get().getConcept(UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8"));

			reportFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reportFile),"UTF8"));
			outputDescFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportDescFile),"UTF8"));
			outputSubsFileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(exportSubsFile),"UTF8"));

			Set<? extends I_GetConceptData> promRefsets = termFactory.getRefsetHelper(config).getPromotionRefsetForRefset(refsetConcept, config);
			promoRefset = promRefsets.iterator().next();
			if (promoRefset==null){
				reportFileWriter.append("The promotion refset concept for target language refset " + refsetConcept + " doesn't exists." + "\r\n");
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
				outputDescFileWriter.append(begEnd + "\r\n");
				

				outputSubsFileWriter.append(begEnd);
				outputSubsFileWriter.append("SubsetId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberId");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("MemberStatus");
				outputSubsFileWriter.append(sep);
				outputSubsFileWriter.append("LinkedId");
				outputSubsFileWriter.append(begEnd + "\r\n");
				
				descLineCount = 0l;
				subsLineCount = 0l;

				myStaticIsACache = Ts.get().getCache(config.getViewCoordinate());


				rUtil=new RefsetUtilImpl();
				refsetSCTID=rUtil.getSnomedId(refsetConcept.getNid(),termFactory).toString();
				try{
					Long.parseLong(refsetSCTID);
				}catch(NumberFormatException e){
					refsetSCTID=refsetConcept.getUUIDs().iterator().next().toString();
					reportFileWriter.append("The refset UUID " + refsetSCTID + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

				}
			}

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

				reportFileWriter.append("The concept " + conceptId + " has not Snomed Concept ID, It will be replaced with its UUID." + "\r\n");

			}
			for (I_ContextualizeDescription cdescription : descriptions) {
				if (cdescription.getLanguageExtension()!=null){

					String did=rUtil.getSnomedId(cdescription.getDescId(),termFactory).toString();
					try{
						Long.parseLong(did);
					}catch(NumberFormatException e){
						did=cdescription.getDescriptionVersioned().getUUIDs().iterator().next().toString();
						reportFileWriter.append("The description " + did + " has not Snomed Description ID, It will be replaced with its UUID." + "\r\n");

					}
					String dStatus=ExportUtil.getStatusType(cdescription.getDescriptionStatusId());
					

						boolean bstop;

						if (	dStatus.equals("8")){
						bstop=true;
						}else if (!dStatus.equals("0")){
							bstop=true;
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


					outputDescFileWriter.append(begEnd + did + sep + dStatus + sep + conceptId + sep + term + sep + ics + sep + dType + sep + lang + begEnd + "\r\n");

					descLineCount++;

					if (dStatus=="0"){
						outputSubsFileWriter.append(begEnd + refsetSCTID + sep + did + sep + dType + sep + begEnd + "\r\n");

						subsLineCount++;
					}
					bwrite=true;
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
		try {
			reportFileWriter.append("Exported to UUID file " + exportDescFile.getName()  + " : " + descLineCount + " lines" + "\r\n");
			reportFileWriter.append("Exported to SCTID file " + exportSubsFile.getName()  + " : " + subsLineCount + " lines" + "\r\n");
			reportFileWriter.flush();
			reportFileWriter.close();
			outputDescFileWriter.flush();
			outputDescFileWriter.close();
			outputSubsFileWriter.flush();
			outputSubsFileWriter.close();	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}