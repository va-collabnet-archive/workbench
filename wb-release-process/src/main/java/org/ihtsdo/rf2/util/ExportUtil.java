package org.ihtsdo.rf2.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.dwfa.ace.api.DatabaseSetupConfig;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceDb;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.cs.ChangeSetWriterThreading;
import org.dwfa.ace.commitlog.CommitLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.cs.econcept.EConceptChangeSetWriter;
import org.ihtsdo.db.bdb.BdbTermFactory;
import org.ihtsdo.idgeneration.IdAssignmentImpl;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.core.dao.ModuleIDDAO;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.ConceptSpec;

//import org.ihtsdo.tk.api.Precedence;

public class ExportUtil {

	// store the handle for the log files
	private static I_ConfigAceFrame aceConfig;

	// log4j logging
	private static Logger logger = Logger.getLogger(ExportUtil.class.getName());
	private static int userAuthorNid;

	private static UUID paths[] = null;
	private static HashSet<String> metaHier;

	private static Set<I_GetConceptData> metaConceptList;

	public static String TIMEFORMAT = I_Constants.TimeFormat;
	public static SimpleDateFormat DATEFORMAT = new SimpleDateFormat(TIMEFORMAT);

	private static String moduleId = "";
	private static Set<I_GetConceptData> inactiveConceptList;
	private static boolean inActiveRelationshipState = false;
	private static HashSet<ModuleIDDAO> metaHierDAO;


	public static int activeId; 
	public static int inactId; 
	public static int conRetId;
	public static int retId ;
	public static int dupId ;
	public static int curId ;
	public static int outdatedId ;
	public static int ambiguousId;
	public static int errId ;
	public static int limId;
	public static int movId ;
	public static int pendId ;
	public static int inappropriateId ;
	static{
		try {

			activeId = SnomedMetadataRfx.getSTATUS_CURRENT_NID();
			inactId =  getTermFactory().uuidToNative(UUID.fromString("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"));
			conRetId = getTermFactory().uuidToNative(UUID.fromString("6cc3df26-661e-33cd-a93d-1c9e797c90e3"));
			retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
			dupId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getUUIDs().get(0));
			curId =ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();

			outdatedId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getUUIDs().get(0));
			ambiguousId =getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getUUIDs().get(0));
			errId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getUUIDs().get(0));
			limId = getTermFactory().uuidToNative(SnomedMetadataRfx.getSTATUS_LIMITED().getLenient().getUUIDs().get(0));
			movId = getTermFactory().uuidToNative(UUID.fromString("95028943-b11c-3509-b1c0-c4ae16aaad5c"));
			pendId = getTermFactory().uuidToNative(UUID.fromString("9906317a-f50f-30f6-8b59-a751ae1cdeb9"));
			inappropriateId = getTermFactory().uuidToNative(UUID.fromString("bcb2ccda-d62a-3fc8-b158-10ad673823b6"));
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void init() {
		// create ace framework
		//createAceConfig();

		// create the meta hierarchy
		//metaConceptList = initMetaHierarchyIsAList(); // 127;

		inactiveConceptList = getInactiveDecendentList();
		InitializeModuleID();		
	}



	//Temporary solution for module id...
	public static void InitializeModuleID() {
		InputStream is =ExportUtil.class.getResourceAsStream("/fileconfig.properties"); 

		try { 
			// Load properties file for FileNames 
			Properties propsFileName = new Properties();
			propsFileName.load(is); 
			String metaHierarchy = propsFileName.getProperty("metaHierarchy"); 			 
			metaHierDAO = new HashSet<ModuleIDDAO>();
			BufferedReader in = null;
			InputStream isData = ExportUtil.class.getResourceAsStream(metaHierarchy);
			InputStreamReader isR = new InputStreamReader(isData);
			try {
				in = new BufferedReader(isR);
				String str;
				while ((str = in.readLine()) != null) {
					str = str.trim();
					String[] part= str.split("\t");
					ModuleIDDAO ModuleIDDAO = new ModuleIDDAO(part[0] , part[1], part[2]);
					metaHierDAO.add(ModuleIDDAO);
				}
			} catch (FileNotFoundException e1) {
				logger.error(e1.getMessage());
			} catch (IOException e1) {
				logger.error(e1.getMessage());
			} catch (Exception e) {
				logger.error(" Exceptions in getHier : " + e.getMessage());
			} finally {
				if (in != null)
					try {
						in.close();
					} catch (IOException e) {		 				
						e.printStackTrace();
					}
			}
		} catch (FileNotFoundException e){ 
			logger.error(e.getMessage()); 
		} catch (IOException e) { 
			logger.error(e.getMessage()); 
		}
	}
	private static File qualStartStopFile;

	private static HashMap<Integer, String> type;

	private static HashMap<Integer, ArrayList<Integer[]>> domain;

	private static HashMap<Integer, ArrayList<String[]>> range;

	private static HashMap<Integer, Integer> sctidNidMap;

	private static HashMap<String, String> order;

	private static HashMap<Integer,ArrayList<Integer[]>> domaex;

	private static File SCTID_Code_mapFile;

	private static HashMap<Integer, String> sctidCodeMap;

	private static HashMap<String, Integer> refList;

	private static HashMap<String, String> qualIds;

	private static File previousQualIdsFile;

	private static File currentInferRelsFile;

	private static HashMap<String, String> infRels;

	public static void setQualStartStopFile(File qualStartStop){
		qualStartStopFile=qualStartStop;
	}

	public static void setPreviousQualIdsFile(File previousQualIds){
		previousQualIdsFile=previousQualIds;
	}
	public static void setCurrentInferRelsFile(File currentInferRels){
		currentInferRelsFile=currentInferRels;
	}

	public static void setSCTID_Code_MapFile(File SCTID_Code_map){
		SCTID_Code_mapFile=SCTID_Code_map;
	}

	public static void loadConceptForQualStartStop( )throws IOException {

		refList=new HashMap<String,Integer>();

		FileInputStream rfis = new FileInputStream(qualStartStopFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;
		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
//			if (!spl[5].equals("Self")){
				refList.put(spl[6], -1);
//			}
		}
		rbr.close();
		rbr=null;
	}
	public enum hierCondition{Self,DescendantsOrSelf,Descendants};

	public static void loadPreviousQualIds() throws IOException{

		FileInputStream rfis = new FileInputStream(previousQualIdsFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		qualIds=new HashMap<String, String>();
		String line;
		String[] spl;
		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			if (spl[4].equals("1")){
				qualIds.put(spl[1] + "-" + spl[2] + "-" + spl[3], spl[0]);
			}
		}
		rbr.close();
		rbr=null;
	}

	public static void loadCurrentInferRels() throws IOException{

		FileInputStream rfis = new FileInputStream(currentInferRelsFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;
		infRels=new HashMap<String,String>();
		String types=null;
		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			if (spl[4].equals("0") && !spl[2].equals("116680003")){
				String key=spl[1];
				if (infRels.containsKey(key)){
					types=infRels.get(key);
					types+=spl[2] + "-";
				}else{
					types="-" + spl[2] + "-";
				}
				infRels.put( spl[1],types);
			}
		}
		rbr.close();
		rbr=null;
	}

	public static void loadQualStartStop( )throws IOException {

		FileInputStream rfis = new FileInputStream(qualStartStopFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;

		type=new HashMap<Integer,String>();
		//		order=new HashMap<String,String>();
		domain=new HashMap<Integer,ArrayList<Integer[]>>();
		domaex=new HashMap<Integer,ArrayList<Integer[]>>();
		range=new HashMap<Integer,ArrayList<String[]>>();
		ArrayList<Integer[]> inte=new ArrayList<Integer[]>();
		ArrayList<String[]> stri=null;
		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			Integer order=Integer.valueOf(spl[2]);
			type.put(order, spl[0]);
			if (spl[6].equals("0")){
				boolean bstop=true;
			}
			if (spl[3].equals("Domain")){
				if (spl[4].equals("Include")){
					if (domain.containsKey(order)){
						inte=domain.get(order);
					}else{
						inte=new ArrayList<Integer[]>();
					}

					if (refList.get(spl[6])==null){
						boolean bstop=true;
					}
					inte.add( new Integer[]{hierCondition.valueOf(spl[5]).ordinal(),refList.get(spl[6])});
					domain.put(order,inte);

				}else if (spl[4].equals("Exclude")){

					if (domaex.containsKey(order)){
						inte=domaex.get(order);
					}else{
						inte=new ArrayList<Integer[]>();
					}
					if (refList.get(spl[6])==null){
						boolean bstop=true;
					}
					inte.add( new Integer[]{hierCondition.valueOf(spl[5]).ordinal(),refList.get(spl[6])});
					domaex.put(order,inte);
				}
			}

			if (spl[3].equals("Range")){
				if (range.containsKey(order)){
					stri=range.get(order);
				}else{
					stri=new ArrayList<String[]>();
				}
				stri.add( new String[]{spl[5],spl[6]});
				range.put(order,stri);
			}
		}
		rbr.close();
		rbr=null;
	}
	public static HashMap<Integer, String> getTypeForQualifiers(){
		return type;
	}



	public static String getQualifierRF1Row(Integer order2, 
			String conceptId,Config config) {
		StringBuffer sb=new StringBuffer("");
		String typeId=type.get(order2);

		List<String[]> stri=range.get(order2);
		String refinability = null;
		String strTypes;
		for (String[] str:stri){

			String conceptId2 = str[1];
			if (infRels.containsKey(conceptId)){
				strTypes=infRels.get(conceptId);
				if (strTypes.contains("-" + typeId + "-")){
					continue;
				}
			}
			hierCondition hc=hierCondition.valueOf(str[0]);
			switch(hc){
			case Descendants:
				refinability="2";
				break;
			case DescendantsOrSelf:
				refinability="1";
				break;
			case Self:
				refinability="0";

			}
			String relationshipId = getPreviousQualId(conceptId,typeId,conceptId2);
			if (relationshipId==null){
				UUID uuid=UUID.randomUUID();
				relationshipId=getSCTId(config,uuid,Integer.valueOf(config.getNamespaceId()),config.getPartitionId(),config.getReleaseId(),config.getExecutionId(),config.getModuleId());
			}
			sb.append(relationshipId);
			sb.append("\t");
			sb.append(conceptId);
			sb.append("\t");
			sb.append(typeId);
			sb.append("\t");
			sb.append(conceptId2);
			sb.append("\t");
			sb.append("1");
			sb.append("\t");
			sb.append(refinability);
			sb.append("\t");
			sb.append("0");
			sb.append("\r\n");
		}
		return sb.toString();
	}


	private static String getPreviousQualId(String conceptId, String typeId,
			String conceptId2) {
		String strKey=conceptId + "-" + typeId + "-" + conceptId2;

		return qualIds.get(strKey);
	}



	public static boolean testDomain(Integer order2,I_GetConceptData testCpt,I_ConfigAceFrame currenAceConfig) throws TerminologyException, IOException, ContradictionException {

		List<Integer[]> domaexc=domaex.get(order2);
		hierCondition[] hierCondValues = hierCondition.values();
		boolean ret=true;
		if (domaexc!=null){

			for(Integer[] inte:domaexc){

				hierCondition hc=hierCondValues[inte[0]];
				if (inte[1]==null){
					boolean bstop=true;
				}
				I_GetConceptData cpt=Terms.get().getConcept(inte[1]);
				switch(hc){
				case Descendants:
					ret=cpt.isParentOf(testCpt, currenAceConfig.getAllowedStatus(),
							currenAceConfig.getDestRelTypes(), 
							currenAceConfig.getViewPositionSetReadOnly(), 
							currenAceConfig.getPrecedence(), 
							currenAceConfig.getConflictResolutionStrategy());
					if (ret){
						return false;
					}
					break;
				case DescendantsOrSelf:

					ret=cpt.isParentOfOrEqualTo(testCpt, currenAceConfig.getAllowedStatus(),
							currenAceConfig.getDestRelTypes(), 
							currenAceConfig.getViewPositionSetReadOnly(), 
							currenAceConfig.getPrecedence(), 
							currenAceConfig.getConflictResolutionStrategy());
					if (ret){
						return false;
					}
					break;
				case Self:
					if ((cpt.getNid()==testCpt.getNid())){
						return false;
					}
				}
			}
		}
		ret=false;
		List<Integer[]> domains=domain.get(order2);
		for(Integer[] inte:domains){

			hierCondition hc=hierCondValues[inte[0]];
			if (inte[1]==null){
				boolean bstop=true;
			}
			I_GetConceptData cpt=Terms.get().getConcept(inte[1]);
			switch(hc){
			case Descendants:
				ret=cpt.isParentOf(testCpt, currenAceConfig.getAllowedStatus(),
						currenAceConfig.getDestRelTypes(), 
						currenAceConfig.getViewPositionSetReadOnly(), 
						currenAceConfig.getPrecedence(), 
						currenAceConfig.getConflictResolutionStrategy());
				if (ret){
					return true;
				}
				break;
			case DescendantsOrSelf:

				ret=cpt.isParentOfOrEqualTo(testCpt, currenAceConfig.getAllowedStatus(),
						currenAceConfig.getDestRelTypes(), 
						currenAceConfig.getViewPositionSetReadOnly(), 
						currenAceConfig.getPrecedence(), 
						currenAceConfig.getConflictResolutionStrategy());
				if (ret){
					return true;
				}
				break;
			case Self:
				if (cpt.getNid()==testCpt.getNid()){
					return true;
				}
			}
		}
		return ret;
	}



	public static void loadSCTID_map( )throws IOException, TerminologyException {


		FileInputStream rfis = new FileInputStream(SCTID_Code_mapFile	);
		InputStreamReader risr = new InputStreamReader(rfis,"UTF-8");
		BufferedReader rbr = new BufferedReader(risr);

		String line;
		String[] spl;

		while((line=rbr.readLine())!=null){

			spl=line.split("\t",-1);
			if (refList.containsKey(spl[1])){
				Integer nid=Terms.get().uuidToNative(UUID.fromString(spl[0]));
				if (nid==null){

					boolean bstop=true;
				}
				refList.put(spl[1], nid);
			}
		}
		rbr.close();
		rbr=null;
	}

	public static String getParentSnomedId(I_GetConceptData concept) throws Exception{		
		Set<I_GetConceptData> parents = new HashSet<I_GetConceptData>();
		parents = getParentLocal(parents, concept); // check size		
		String parentSnomedId="";
		boolean findParentSnomedId = true;

		for (I_GetConceptData loopConcept : parents) {
			if(findParentSnomedId){
				parentSnomedId = getSnomedId(loopConcept, getSnomedCorePathNid());
				if(!parentSnomedId.isEmpty()){
					findParentSnomedId = false;
				}
			}
		}

		/*	if(findParentSnomedId){
			parentSnomedId="R-10000"; //Default Value
		}*/	

		return parentSnomedId;
	}

	public static boolean insertSctId(int componentNid , Config config, String wsSctId , int pathNid , int statusNid , long effectiveDate) throws IOException {

		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);		
			BdbTermFactory tfb = (BdbTermFactory) getTermFactory();
			I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
			newDbProfile.setUsername("susan-test");
			newDbProfile.setUserConcept(getTermFactory().getConcept(UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c")));
			newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
			newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.INCREMENTAL);
			newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
			File changeSetRoot = new File("profiles" + File.separator + "susan-test" + File.separator + "changesets");
			String changeSetWriterFileName = "susan-test" + "." + "#" + 0 + "#" + UUID.randomUUID().toString() + ".eccs"; 
			newDbProfile.setChangeSetRoot(changeSetRoot);
			newDbProfile.setChangeSetWriterFileName(changeSetWriterFileName);

			ChangeSetWriterHandler.addWriter(newDbProfile.getUsername()
					+ ".eccs", new EConceptChangeSetWriter(new File(newDbProfile.getChangeSetRoot(), newDbProfile.getChangeSetWriterFileName()), 
							new File(newDbProfile.getChangeSetRoot(), "."
									+ newDbProfile.getChangeSetWriterFileName()), 
									ChangeSetGenerationPolicy.INCREMENTAL, true));

			ChangeSetWriterHandler.addWriter(newDbProfile.getUsername() + ".commitLog.xls",
					new CommitLog(new File(newDbProfile.getChangeSetRoot(),
							"commitLog.xls"), new File(newDbProfile.getChangeSetRoot(),
									"." + "commitLog.xls")));	

			flag = i_Identify.addLongId(Long.parseLong(wsSctId), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
					statusNid,
					effectiveDate,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);
			I_GetConceptData commitedConcept = getTermFactory().getConceptForNid(componentNid);

			getTermFactory().addUncommitted(commitedConcept);
			getTermFactory().commit();
		} catch (NullPointerException ne) {
			ne.printStackTrace();
			logger.error("NullPointerException " +ne.getMessage());
		} catch (NumberFormatException e) {
			e.printStackTrace();
			logger.error("NumberFormatException " +e.getMessage());
		} catch (TerminologyException e) {
			e.printStackTrace();
			logger.error("TerminologyException " +e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("IOException " +e.getMessage());;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception " +e.getMessage());
		}	
		return flag;
	}




	public static void setupProfile(Config config) throws TerminologyException, IOException{
		//File changeSetRoot = new File("profiles" + File.separator + "susan-test" + File.separator + "changesets");
		BdbTermFactory tfb = (BdbTermFactory) getTermFactory();
		I_ConfigAceDb newDbProfile = tfb.newAceDbConfig();
		newDbProfile.setUsername(config.getChangesetUserName());
		newDbProfile.setUserConcept(getTermFactory().getConcept(UUID.fromString(config.getChangesetUserConcept())));
		newDbProfile.setClassifierChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		newDbProfile.setRefsetChangesChangeSetPolicy(ChangeSetPolicy.OFF);
		newDbProfile.setUserChangesChangeSetPolicy(ChangeSetPolicy.MUTABLE_ONLY);
		newDbProfile.setChangeSetWriterThreading(ChangeSetWriterThreading.SINGLE_THREAD);
		File changeSetRoot = new File(config.getChangesetRoot(), "changesets");
		//File changeSetRoot = new File("profiles" + File.separator + newDbProfile.getUsername() + File.separator + "changesets");				

		changeSetRoot.mkdirs();
		String changeSetWriterFileName = config.getChangesetUserName() + "." + "#" + 1 + "#" + UUID.randomUUID().toString() + ".eccs"; 
		newDbProfile.setChangeSetRoot(changeSetRoot);
		newDbProfile.setChangeSetWriterFileName(changeSetWriterFileName);
		String tempKey = UUID.randomUUID().toString();

		ChangeSetGeneratorBI generator = Ts.get().createDtoChangeSetGenerator(
				new File(newDbProfile.getChangeSetRoot(),
						newDbProfile.getChangeSetWriterFileName()), 
						new File(newDbProfile.getChangeSetRoot(), "#1#"
								+ newDbProfile.getChangeSetWriterFileName()),
								ChangeSetGenerationPolicy.MUTABLE_ONLY);

		ChangeSetWriterHandler.addWriter(newDbProfile.getUsername() + ".commitLog.xls",
				new CommitLog(new File(newDbProfile.getChangeSetRoot(),
						"commitLog.xls"), new File(newDbProfile.getChangeSetRoot(),
								"." + "commitLog.xls")));	

		Ts.get().addChangeSetGenerator(tempKey, generator);
	}




	public static boolean insertSnomedId(int componentNid , Config config, String wsSnomedId , int pathNid , int statusNid) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);
			flag = i_Identify.addStringId(wsSnomedId, ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid(),
					statusNid,
					Long.MAX_VALUE,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);
			I_GetConceptData commitedConcept = getTermFactory().getConcept(componentNid);

			getTermFactory().addUncommitted(commitedConcept);

		} catch (NumberFormatException e) {
			logger.error("NumberFormatException" +e);
		} catch (TerminologyException e) {
			logger.error("TerminologyException" +e);
		} catch (IOException e) {
			logger.error("IOException" +e);
		} catch (Exception e) {
			logger.error("Exception" +e);
		}	
		return flag;
	}

	public static boolean insertCtv3Id(int componentNid , Config config, String wsCtv3Id , int pathNid , int statusNid) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);
			if (getAceConfig() == null) {
				createAceConfig();
			}

			flag = i_Identify.addStringId(wsCtv3Id, ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid(), statusNid,
					Long.MAX_VALUE,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);
			I_GetConceptData commitedConcept = getTermFactory().getConcept(componentNid);

			getTermFactory().addUncommitted(commitedConcept);

		} catch (NumberFormatException e) {
			logger.error("NumberFormatException" +e);
		} catch (TerminologyException e) {
			logger.error("TerminologyException" +e);
		} catch (IOException e) {
			logger.error("IOException" +e);
		} catch (Exception e) {
			logger.error("Exception" +e);
		}
		return flag;
	}



	public static boolean insertSnomedId(int componentNid , Config config, String wsSnomedId , int pathNid , int statusNid , long effectiveDate) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);
			if (getAceConfig() == null) {
				createAceConfig();
			}

			flag = i_Identify.addStringId(wsSnomedId, ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid(),
					statusNid,
					effectiveDate,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);

			I_GetConceptData commitedConcept = getTermFactory().getConcept(componentNid);
			//getTermFactory().addUncommitted(commitedConcept);
			//getTermFactory().commit();

		} catch (NumberFormatException e) {
			logger.error("NumberFormatException" +e);
		} catch (TerminologyException e) {
			logger.error("TerminologyException" +e);
		} catch (IOException e) {
			logger.error("IOException" +e);
		} catch (Exception e) {
			logger.error("Exception" +e);
		}	
		return flag;
	}

	public static boolean insertCtv3Id(int componentNid , Config config, String wsCtv3Id , int pathNid , int statusNid , long effectiveDate) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);
			if (getAceConfig() == null) {
				createAceConfig();
			}

			flag = i_Identify.addStringId(wsCtv3Id, ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid(),
					statusNid,
					effectiveDate,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);

			I_GetConceptData commitedConcept = getTermFactory().getConcept(componentNid);
			//getTermFactory().addUncommitted(commitedConcept);
			//getTermFactory().commit();

			/*	I_Identify i_Identify_after = getTermFactory().getId(componentNid);
						List<? extends I_IdVersion> i_IdentifyAfterList = i_Identify_after.getIdVersions();

						if (i_IdentifyAfterList.size() > 0) {
							for (int j = 0; j < i_IdentifyAfterList.size(); j++) {
								I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyAfterList.get(j);
								Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
								logger.info("====Final Id List==="+denotion.toString());
							}							
						}*/
		} catch (NumberFormatException e) {
			logger.error("NumberFormatException" +e);
		} catch (TerminologyException e) {
			logger.error("TerminologyException" +e);
		} catch (IOException e) {
			logger.error("IOException" +e);
		} catch (Exception e) {
			logger.error("Exception" +e);
		}
		return flag;
	}

	/*
	public static String getConceptMetaModuleID(I_GetConceptData snomedConcept , String conEffectiveTime) throws IOException, TerminologyException {
		String snomedIntegerId = getConceptId(snomedConcept, getSnomedCorePathNid());
		System.out.println(snomedConcept.getInitialText() + " & " + conEffectiveTime);

		moduleId = I_Constants.CORE_MODULE_ID; 
		if (snomedIntegerId!=null){
			if (metaHierDAO.isEmpty()) { 
				logger.error("Meta Hierarchy DAO Set is empty"); 
			} else {  
				Iterator iter = metaHierDAO.iterator();
				String prevET="00000000";
				while (iter.hasNext()) {
					ModuleIDDAO  moduleIdDAO = ( ModuleIDDAO ) iter.next();
					String conceptid = moduleIdDAO.getConceptid();
					String effectivetime = moduleIdDAO.getEffectiveTime();
					String active = moduleIdDAO.getActive();
					if(snomedIntegerId.equals(conceptid) 
							&& effectivetime.compareTo(conEffectiveTime)<=0 
							&& active.equals("0")
							&& prevET.compareTo(effectivetime)<0){
						moduleId = I_Constants.CORE_MODULE_ID;
						prevET=effectivetime;
					}else if(snomedIntegerId.equals(conceptid) 
							&& effectivetime.compareTo(conEffectiveTime)<=0 
							&& active.equals("1")
							&& prevET.compareTo(effectivetime)<0){
						moduleId = I_Constants.META_MOULE_ID;					
						prevET=effectivetime;
					}

				} 
			}
		}
		return moduleId; 
	}*/

	public static Long getLatestActivePart(List<I_RelPart> parts)
			throws Exception {
		long latestVersion = Integer.MIN_VALUE;
		for (I_RelPart rel : parts) {
			if (rel.getTime() > latestVersion && rel.getStatusNid()==getNid("d12702ee-c37f-385f-a070-61d56d4d0f1f")) {
				latestVersion = rel.getTime();
			}
		}
		if (latestVersion>Integer.MIN_VALUE)
			return latestVersion;

		return null;	  
	}

	public static String getConceptMetaModuleID(I_GetConceptData snomedConcept , String conEffectiveTime) throws IOException, TerminologyException {
		String snomedIntegerId = getConceptId(snomedConcept, getSnomedCorePathNid());
		moduleId = I_Constants.CORE_MODULE_ID; 
		if (snomedIntegerId!=null){
			if (metaHierDAO.isEmpty()) { 
				logger.error("Meta Hierarchy DAO Set is empty"); 
			} else {  
				Iterator iter = metaHierDAO.iterator();
				String prevET="00000000";
				while (iter.hasNext()) {
					ModuleIDDAO  moduleIdDAO = ( ModuleIDDAO ) iter.next();
					String conceptid = moduleIdDAO.getConceptid();
					String effectivetime = moduleIdDAO.getEffectiveTime();
					String active = moduleIdDAO.getActive();
					if(snomedIntegerId.equals(conceptid) 
							&& effectivetime.compareTo(conEffectiveTime)<=0 
							&& active.equals("0")
							&& prevET.compareTo(effectivetime)<0){
						moduleId = I_Constants.CORE_MODULE_ID;
						prevET=effectivetime;
					}else if(snomedIntegerId.equals(conceptid) 
							&& effectivetime.compareTo(conEffectiveTime)<=0 
							&& active.equals("1")
							&& prevET.compareTo(effectivetime)<0){
						moduleId = I_Constants.META_MODULE_ID;     
						prevET=effectivetime;
					}
				} 
			}
		}
		return moduleId; 
	}

	/*
	public static String getConceptMetaModuleID(I_GetConceptData snomedConcept , String conEffectiveTime) throws IOException, TerminologyException {
		String snomedIntegerId = getConceptId(snomedConcept, getSnomedCorePathNid());

		if (metaHierDAO.isEmpty()) { 
			logger.error("Meta Hierarchy DAO Set is empty"); 
		} else {  
			Iterator iter = metaHierDAO.iterator();
			while (iter.hasNext()) {
				ModuleIDDAO  moduleIdDAO = ( ModuleIDDAO ) iter.next();
				String conceptid = moduleIdDAO.getConceptid();
				String effectivetime = moduleIdDAO.getEffectiveTime();
				//need to sort effectivetime issue

				if(snomedIntegerId.equals(conceptid) && effectivetime.compareTo(conEffectiveTime)<=0
				){
					moduleId = I_Constants.META_MOULE_ID;
					break;
				}
				else {
					moduleId = I_Constants.CORE_MODULE_ID; 
				}
			} 
		}

		return moduleId; 
	}
	 */


	public static boolean getInactiveConceptList(I_GetConceptData destinationConcept , String destinationId) throws IOException, TerminologyException {	
		inActiveRelationshipState= false;
		if (inactiveConceptList.isEmpty()) {
			logger.error("No inactive concept in the list"); 
		} else if (inactiveConceptList.contains(destinationConcept)) {			 
			inActiveRelationshipState = true;
		}
		return inActiveRelationshipState;
	}

	public static Set<I_GetConceptData> getInactiveDecendentList() {
		Set<I_GetConceptData> inactiveConceptSet = new HashSet<I_GetConceptData>();
		try {
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();

			//I_GetConceptData inactiveConcept = getTermFactory().getConcept(UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a")); // Special concept --Inactive concept
			//inactiveConceptSet = getDescendants(descendants, inactiveConcept);

			I_GetConceptData ambiguousConcept = getTermFactory().getConcept(UUID.fromString("5adbed85-55d8-3304-a404-4bebab660fff")); // Ambiguous concept
			//inactiveConceptSet = getDescendants(descendants, ambiguousConcept); 

			I_GetConceptData duplicateConcept = getTermFactory().getConcept(UUID.fromString("a5db42d4-6d94-33b7-92e7-d4a1d0f0d814")); // 
			//inactiveConceptSet = getDescendants(descendants, duplicateConcept);

			I_GetConceptData erroneousConcept = getTermFactory().getConcept(UUID.fromString("d4227098-db7a-331e-8f00-9d1e27626fc5")); // 
			//inactiveConceptSet = getDescendants(descendants, erroneousConcept);

			I_GetConceptData limitedConcept = getTermFactory().getConcept(UUID.fromString("0c7b717a-3e41-372b-be57-621befb9b3ee")); //
			//inactiveConceptSet = getDescendants(descendants, limitedConcept);

			I_GetConceptData noncurrentConcept = getTermFactory().getConcept(UUID.fromString("e730d11f-e155-3482-a423-9637db3bc1a2")); // 
			//inactiveConceptSet = getDescendants(descendants, noncurrentConcept);

			I_GetConceptData outdatedConcept = getTermFactory().getConcept(UUID.fromString("d8a42cc5-05dd-3fcf-a1f7-62856e38874a")); //
			//inactiveConceptSet = getDescendants(descendants, outdatedConcept);

			I_GetConceptData reasonnotstatedConcept = getTermFactory().getConcept(UUID.fromString("a0db7e17-c6b2-3acc-811d-8a523274e869")); // 
			//inactiveConceptSet = getDescendants(descendants, reasonnotstatedConcept);

			//inactiveConceptSet.add(inactiveConcept);
			inactiveConceptSet.add(ambiguousConcept);
			inactiveConceptSet.add(duplicateConcept);
			inactiveConceptSet.add(erroneousConcept);
			inactiveConceptSet.add(limitedConcept);
			inactiveConceptSet.add(noncurrentConcept);
			inactiveConceptSet.add(outdatedConcept);
			inactiveConceptSet.add(reasonnotstatedConcept);

			//System.out.println("=====================================" + inactiveConceptSet.size());

			Iterator iter = inactiveConceptSet.iterator();		

			while (iter.hasNext()) {
				I_GetConceptData concept = (I_GetConceptData) iter.next();
				String conceptId = getConceptId(concept, getSnomedCorePathNid());		    	
			}

		} catch (StackOverflowError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Exception e) {
			logger.error(" Exceptions in getMetaHierarchyIsAList : " + e.getMessage());
		}
		return inactiveConceptSet;
	}


	public static  Set<I_GetConceptData> getDescendantsLocal(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendantsLocal(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return descendants;
	}


	//Add specific parent who has closest snomedid
	public static  Set<I_GetConceptData> getParentLocal(Set<I_GetConceptData> parent, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = Terms.get();
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));			
			//will have to check if this is sufficient
			//allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));

			Set<I_GetConceptData> parentSet = new HashSet<I_GetConceptData>();
			parentSet.addAll(concept.getSourceRelTargets(config.getAllowedStatus(), allowedDestRelTypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy()));

			boolean findParentSnomedId = true;
			String parentSnomedId="";

			for (I_GetConceptData loopConcept : parentSet) {
				if(findParentSnomedId){
					parentSnomedId = getSnomedId(loopConcept, getSnomedCorePathNid());	
					if(!parentSnomedId.isEmpty()){
						parent.addAll(parentSet);
						findParentSnomedId = false;
					}
				}

				if(findParentSnomedId){
					parent = getParentLocal(parent, loopConcept);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return parent;
	}

	public static void createTermFactory(Database db) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {

		File vodbDirectory = new File(db.getLocation());

		DatabaseSetupConfig dbSetupConfig = new DatabaseSetupConfig();
		Terms.createFactory(vodbDirectory, I_Constants.readOnly, I_Constants.cacheSize, dbSetupConfig);
	}

	public static I_TermFactory getTermFactory() {
		// since we are using mojo this handles the return of the opened database
		I_TermFactory termFactory = Terms.get();
		return termFactory;
	}

	public static I_ConfigAceFrame getAceConfig() {
		return aceConfig;
	}

	public static void createAceConfig() {
		try {

			aceConfig = getTermFactory().newAceFrameConfig();

			DateFormat df = new SimpleDateFormat("yyyy.mm.dd hh:mm:ss zzz");
			// config.addViewPosition(termFactory.newPosition(termFactory.getPath(new UUID[] { UUID.fromString(test_path_uuid) }),
			// termFactory.convertToThinVersion(df.parse(test_time).getTime())));

			// Added inferred promotion template to catch the context relationships
			aceConfig.addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString("b4f0899d-39db-5c3d-ae03-2bac05433162") }), Integer.MAX_VALUE)); //b4f0899d-39db-5c3d-ae03-2bac05433162
			aceConfig.addEditingPath(getTermFactory().getPath(new UUID[] { UUID.fromString("b4f0899d-39db-5c3d-ae03-2bac05433162") })); //b4f0899d-39db-5c3d-ae03-2bac05433162
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());//Fully specified name	

			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_SYNONYM_NID());

			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());

			ConceptSpec definition = new ConceptSpec("Definition (core metadata concept)", UUID.fromString("700546a3-09c7-3fc2-9eb9-53d318659a09"));
			aceConfig.getDescTypes().add(getNid(definition.getLenient().getUUIDs().get(0).toString()));

			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			aceConfig.getDestRelTypes().add(ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid());

			ConceptSpec isa = new ConceptSpec("Is a (attribute)", UUID.fromString(I_Constants.IS_A_UID));
			aceConfig.getDescTypes().add(getNid(isa.getLenient().getUUIDs().get(0).toString()));
			aceConfig.setDefaultStatus(getTermFactory().getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID())); // Current

			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_CURRENT_NID()); // Current

			aceConfig.getAllowedStatus().add(getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724")); //Retired
			aceConfig.getAllowedStatus().add(getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3")); //Concept non-current (foundation metadata concept)
			aceConfig.getAllowedStatus().add(getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9")); //Pending
			aceConfig.getAllowedStatus().add(getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c")); //Component Moved elsewhere	900000000000487009

			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_LIMITED_NID()); //Limited	900000000000486000
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid()); //Outdated	900000000000483008
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid()); //Erroneous component (foundation metadata concept)	900000000000485001
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid());  //Ambiguous component (foundation metadata concept)	900000000000484002
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid());  //Dups	900000000000482003

			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			aceConfig.setPrecedence(Precedence.TIME);
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/*public static String getMetaModuleID(I_GetConceptData snomedConcept) {
		if (metaConceptList.isEmpty()) {
			logger.error("Meta Hierarchy List is empty");
		} else if (metaConceptList.contains(snomedConcept)) {
			moduleId = I_Constants.META_MOULE_ID;
		} else {
			moduleId = I_Constants.CORE_MODULE_ID;
		}
		return moduleId;
	}*/


	public static String getMetaModuleID(I_GetConceptData snomedConcept) throws IOException, TerminologyException {
		String snomedIntegerId = getConceptId(snomedConcept, getSnomedCorePathNid());
		if (metaHierDAO.isEmpty()) { 
			logger.error("Meta Hierarchy DAO Set is empty"); 
		} else {  
			Iterator iter = metaHierDAO.iterator();
			while (iter.hasNext()) {
				ModuleIDDAO  moduleIdDAO = ( ModuleIDDAO ) iter.next();
				String conceptid = moduleIdDAO.getConceptid();
				String effectivetime = moduleIdDAO.getEffectiveTime();
				//need to sort effectivetime issue
				if(snomedIntegerId.equals(conceptid) //&& effectivetime.equals(effectivetime)
						){
					moduleId = I_Constants.META_MODULE_ID;
					break;
				}
				else {
					moduleId = I_Constants.CORE_MODULE_ID; 
				}
			} 
		}

		return moduleId; 
	}

	@SuppressWarnings("unchecked")
	public static Set<I_GetConceptData> initMetaHierarchyIsAList() {
		Set<I_GetConceptData> metaConceptSet = null;
		try {
			I_GetConceptData coreMetadataConcept = getTermFactory().getConcept(UUID.fromString("4c6d8b0b-774a-341e-b0e5-1fc2deedb5a5")); // Core metadata concept 27
			I_GetConceptData foundationMetadataConcept = getTermFactory().getConcept(UUID.fromString("f328cdec-6198-36c4-9c55-d7f4f5b30922")); // Foundation metadata concept 99
			I_GetConceptData snomedMetadataConcept = getTermFactory().getConcept(UUID.fromString("a60bd881-9010-3260-9653-0c85716b4391")); // Snomed meta root concept 99
			Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
			descendants = getDescendantsLocal(descendants, coreMetadataConcept); // 27
			descendants = getDescendantsLocal(descendants, foundationMetadataConcept); // 9?
			descendants = getDescendantsLocal(descendants, snomedMetadataConcept); // ?
			metaConceptSet = descendants;
			// metaConceptSet=getDescendants(descendants, snomedMetadataConcept);
			metaConceptSet.add(coreMetadataConcept);
			metaConceptSet.add(foundationMetadataConcept);
			metaConceptSet.add(snomedMetadataConcept);
		} catch (StackOverflowError e) {
			e.printStackTrace();
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (Exception e) {
			logger.error(" Exceptions in getMetaHierarchyIsAList : " + e.getMessage());
			e.printStackTrace();
		}
		return metaConceptSet;
	}

	public static HashSet<String> getMetaHierarchyList() {
		return metaHier;
	}

	public static HashSet<String> initHierarchyList(String metaHierarchy) {
		metaHier = new HashSet<String>();

		BufferedReader in = null;
		InputStream is = ExportUtil.class.getResourceAsStream(metaHierarchy);
		InputStreamReader isR = new InputStreamReader(is);
		try {
			// InitializeFileName();
			in = new BufferedReader(isR);
			String str;
			while ((str = in.readLine()) != null) {
				str = str.trim();
				metaHier.add(str);
			}
		} catch (FileNotFoundException e1) {
			logger.error(e1.getMessage());
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		} catch (Exception e) {
			logger.error(" Exceptions in getHier : " + e.getMessage());
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return metaHier;
	}

	public static String getRefinabilityStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();

		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();		
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int dupId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Dups	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007

		if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";		
		else if (status == dupId_Term_Aux)
			statusType = "2";
		else if (status == outdatedId_Term_Aux)
			statusType = "3";
		else if (status == ambiguousId_Term_Aux)
			statusType = "4";
		else if (status == errId_Term_Aux)
			statusType = "5";
		else if (status == limId_Term_Aux)
			statusType = "6";
		else if (status == inappropriateId_Term_Aux)
			statusType = "7";
		else if (status == conRetId_Term_Aux)
			statusType = "8";
		else if (status == movId_Term_Aux)
			statusType = "10";
		else if (status == pendId_Term_Aux)
			statusType = "11";
		return statusType;
	}

	public static String getConceptInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";
		//int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		//int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();

		int curId_Term_Aux = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId_Term_Aux = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();		
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();

		int retId = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006
		int curId = SnomedMetadataRfx.getSTATUS_CURRENT_NID(); //Active value	900000000000545005
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int dupId =SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); //Dups	900000000000482003
		int outdatedId =SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007

		if (status == retId_Term_Aux) 
			statusType = "1";
		else if (status == curId_Term_Aux) 
			statusType = "0";
		else if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";		
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";		
		else if (status == dupId_Term_Aux)
			statusType = "2";
		else if (status == outdatedId_Term_Aux)
			statusType = "3";
		else if (status == ambiguousId_Term_Aux)
			statusType = "4";
		else if (status == errId_Term_Aux)
			statusType = "5";
		else if (status == limId_Term_Aux)
			statusType = "6";
		else if (status == inappropriateId_Term_Aux)
			statusType = "7";
		else if (status == conRetId_Term_Aux)
			statusType = "8";
		else if (status == movId_Term_Aux)
			statusType = "10";
		else if (status == pendId_Term_Aux)
			statusType = "11";
		return statusType;
	}

	public static String getConceptInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();		
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();		
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); 			//Concept non-current (foundation metadata concept)	900000000000495008
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); 	//In-appropriate	900000000000494007

		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); 			//Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); 		//Outdated	900000000000483008
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); 		//Ambiguous component (foundation metadata concept)	900000000000484002
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); 			//Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); 	//Component Moved elsewhere	900000000000487009

		// if (status==retId) valueId="Don'tknow value"; //1 Retired without reason (foundation metadata concept)
		if (status == dupId)
			valueId = I_Constants.DUPLICATE; 		// 2 Duplicate component
		else if (status == outdatedId)
			valueId = I_Constants.OUTDATED; 		// 3 Outdated component
		else if (status == ambiguousId)
			valueId = I_Constants.AMBIGUOUS; 		// 4 Ambiguous component
		else if (status == errId)
			valueId = I_Constants.ERRONEOUS; 		// 5 Erroneous component
		else if (status == limId)
			valueId = I_Constants.LIMITED; 			// 6 Limited component
		else if (status == movId)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId)
			valueId = I_Constants.PENDING_MOVE; 	// 11 Pending move
		if (status == dupId_Term_Aux)
			valueId = I_Constants.DUPLICATE; 		// 2 Duplicate component
		else if (status == outdatedId_Term_Aux)
			valueId = I_Constants.OUTDATED; 		// 3 Outdated component
		else if (status == ambiguousId_Term_Aux)
			valueId = I_Constants.AMBIGUOUS; 		// 4 Ambiguous component
		else if (status == errId_Term_Aux)
			valueId = I_Constants.ERRONEOUS; 		// 5 Erroneous component
		else if (status == limId_Term_Aux)
			valueId = I_Constants.LIMITED; 			// 6 Limited component
		else if (status == movId_Term_Aux)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId_Term_Aux)
			valueId = I_Constants.PENDING_MOVE; 	// 11 Pending move
		return valueId;
	}


	public static String getDescInactivationValueId(int status) throws TerminologyException, IOException {
		String valueId = "XXX";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();

		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();

		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); 			//Concept non-current (foundation metadata concept)	900000000000495008
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); 	//In-appropriate	900000000000494007
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); 			//Pending move (foundation metadata concept)	900000000000492006	
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); 			//Dups	900000000000482003
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); 		//Outdated	900000000000483008
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); 			//Erroneous component (foundation metadata concept)	900000000000485001
		int limId = SnomedMetadataRfx.getSTATUS_LIMITED_NID(); 			//Limited	900000000000486000
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); 			//Component Moved elsewhere	900000000000487009

		// if (status==retId) valueId="Don'tknow value"; //1 Retired without reason (foundation metadata concept)
		if (status == dupId)
			valueId = I_Constants.DUPLICATE; // 2 Duplicate component
		else if (status == outdatedId)
			valueId = I_Constants.OUTDATED; // 3 Outdated component
		else if (status == errId)
			valueId = I_Constants.ERRONEOUS; // 5 Erroneous component
		else if (status == limId)
			valueId = I_Constants.LIMITED; // 6 Limited component
		else if (status == inappropriateId)
			valueId = I_Constants.INAPPROPRIATE; // 7 Inappropriate component
		else if (status == conRetId)
			valueId = I_Constants.CONCEPT_NON_CURRENT; // 8 Concept non-current
		else if (status == movId)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId)
			valueId = I_Constants.PENDING_MOVE; // 11 Pending move
		else if (status == dupId_Term_Aux)
			valueId = I_Constants.DUPLICATE; // 2 Duplicate component
		else if (status == outdatedId_Term_Aux)
			valueId = I_Constants.OUTDATED; // 3 Outdated component
		else if (status == errId_Term_Aux)
			valueId = I_Constants.ERRONEOUS; // 5 Erroneous component
		else if (status == limId_Term_Aux)
			valueId = I_Constants.LIMITED; // 6 Limited component
		else if (status == inappropriateId_Term_Aux)
			valueId = I_Constants.INAPPROPRIATE; // 7 Inappropriate component
		else if (status == conRetId_Term_Aux)
			valueId = I_Constants.CONCEPT_NON_CURRENT; // 8 Concept non-current
		else if (status == movId_Term_Aux)
			valueId = I_Constants.MOVED_ELSE_WHERE; // 10 Component moved elsewhere
		else if (status == pendId_Term_Aux)
			valueId = I_Constants.PENDING_MOVE; // 11 Pending move
		return valueId;
	}

	public static String getDescInactivationStatusType(int status) throws TerminologyException, IOException {
		String statusType = "50";
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();
		int conRetId = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();
		int retId = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int dupId = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int curId = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int outdatedId = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int ambiguousId = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();
		int errId = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int limId = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int movId = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int pendId = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int inappropriateId = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		if (status == curId)
			statusType = "0";
		else if (status == retId)
			statusType = "1";
		else if (status == dupId)
			statusType = "2";
		else if (status == outdatedId)
			statusType = "3";
		else if (status == ambiguousId)
			statusType = "4";
		else if (status == errId)
			statusType = "5";
		else if (status == limId)
			statusType = "6";
		else if (status == inappropriateId)
			statusType = "7";
		else if (status == conRetId)
			statusType = "8";
		else if (status == movId)
			statusType = "10";
		else if (status == pendId)
			statusType = "11";
		return statusType;
	}

	public static String getRefinabilityValueId(int charType) throws IOException, Exception {
		String refinabilityType = "99";
		if (charType == 0)
			refinabilityType = I_Constants.NON_REFINABLE;
		else if (charType == 1)
			refinabilityType = I_Constants.OPTIONAL_REFINABLE;
		else if (charType == 2)
			refinabilityType = I_Constants.MANTOTARY;
		return refinabilityType;
	}

	public static String getRefinabilityType(int type) throws IOException, Exception {
		String charType = "99";
		int notRefiniableNid_Term_Aux = ArchitectonicAuxiliary.Concept.NOT_REFINABLE.localize().getNid();
		int optionalNid_Term_Aux = ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.localize().getNid();
		int mandatoryNid_Term_Aux = ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.localize().getNid();
		int notRefiniableNid =getNid("ce30636d-bfc9-3a70-9678-abc6b542ab4c"); //Not refinable	900000000000007000	
		int optionalNid= getNid("7d2d6cd0-c727-397e-9bc8-da65562e9350"); //Optional refinability	900000000000216007
		int mandatoryNid =getNid("67a79b5a-d56e-37f6-ad66-da712e39c453"); //Mandatory refinability	900000000000218008	

		if (type == notRefiniableNid_Term_Aux)
			charType = "0";
		else if (type == optionalNid_Term_Aux)
			charType = "1";
		else if (type == mandatoryNid_Term_Aux)
			charType = "2";
		else if (type == notRefiniableNid)
			charType = "0";
		else if (type == optionalNid)
			charType = "1";
		else if (type == mandatoryNid)
			charType = "2";
		return charType;
	}



	public static String getStatusType(int status) throws TerminologyException, IOException {
		String statusType = "99";		
		int activeId = ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid();
		int inactId = ArchitectonicAuxiliary.Concept.INACTIVE.localize().getNid();

		int curId_Term_Aux = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
		int retId_Term_Aux = ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid();
		int pendId_Term_Aux = ArchitectonicAuxiliary.Concept.PENDING_MOVE.localize().getNid();
		int dupId_Term_Aux = ArchitectonicAuxiliary.Concept.DUPLICATE.localize().getNid();
		int inappropriateId_Term_Aux = ArchitectonicAuxiliary.Concept.INAPPROPRIATE.localize().getNid();
		int limId_Term_Aux = ArchitectonicAuxiliary.Concept.LIMITED.localize().getNid();
		int outdatedId_Term_Aux = ArchitectonicAuxiliary.Concept.OUTDATED.localize().getNid();
		int movId_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int conRetId_Term_Aux = ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.localize().getNid();
		int errId_Term_Aux = ArchitectonicAuxiliary.Concept.ERRONEOUS.localize().getNid();
		int ambiguousId_Term_Aux = ArchitectonicAuxiliary.Concept.AMBIGUOUS.localize().getNid();

		int retId = getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724"); //Inactive value	900000000000546006
		int curId = SnomedMetadataRfx.getSTATUS_CURRENT_NID(); //Active value	900000000000545005
		int pendId = getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9"); //Pending	900000000000492006	
		int inappropriateId = SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID(); //In-appropriate	900000000000494007
		int limId =SnomedMetadataRfx.getSTATUS_LIMITED_NID(); //Limited	900000000000486000
		int outdatedId = SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid(); //Outdated	900000000000483008
		int movId = getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c"); //Component Moved elsewhere	900000000000487009
		int conRetId = getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3"); //Concept non-current (foundation metadata concept)	900000000000495008
		int errId = SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid(); //Erroneous component (foundation metadata concept)	900000000000485001
		int ambiguousId = SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid(); //Ambiguous component (foundation metadata concept)	900000000000484002
		int dupId = SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid(); //Dups	900000000000482003

		if (status == retId) {
			statusType = "1";
		} else if (status == retId_Term_Aux) {
			statusType = "1";		
		} else if (status == curId) {
			statusType = "0";
		} else if (status == curId_Term_Aux) {
			statusType = "0";
		}  else if (status == dupId) {
			statusType = "2";
		} else if (status == dupId_Term_Aux) {
			statusType = "2";
		} else if (status == outdatedId) {
			statusType = "3";
		} else if (status == outdatedId_Term_Aux) {
			statusType = "3";
		} else if (status == ambiguousId) {
			statusType = "4";
		} else if (status == ambiguousId_Term_Aux) {
			statusType = "4";
		} else if (status == errId) {
			statusType = "5";
		} else if (status == errId_Term_Aux) {
			statusType = "5";
		} else if (status == limId) {
			statusType = "6";
		} else if (status == limId_Term_Aux) {
			statusType = "6";
		} else if (status == inappropriateId) {
			statusType = "7";
		} else if (status == inappropriateId_Term_Aux) {
			statusType = "7";
		} else if (status == conRetId) {
			statusType = "8";
		} else if (status == conRetId_Term_Aux) {
			statusType = "8";
		} else if (status == movId) {
			statusType = "10";
		} else if (status == movId_Term_Aux) {
			statusType = "10";
		} else if (status == pendId) {
			statusType = "11";
		} else if (status == pendId_Term_Aux) {
			statusType = "11";
		}
		return statusType;
	}
	private static IdAssignmentImpl stIdGen =null;
	private static IdAssignmentImpl getIdGeneratorClient(Config config){
		if (stIdGen==null ){
			stIdGen = new IdAssignmentImpl(config.getEndPoint(), config.getUsername(), config.getPassword());
		}
		return stIdGen;
	}
	// get the snomedID for the given UUID 
	public static String getSNOMEDID(Config config, UUID uuid, String parentSnomedId) {
		String snomedId = null;
		try {
			IdAssignmentImpl idGen = getIdGeneratorClient( config);
			snomedId = idGen.getSNOMEDID(uuid);

			if(snomedId!=null && !snomedId.equals("") ){
				return snomedId;
			}

			snomedId = idGen.createSNOMEDID(uuid, parentSnomedId);

			logger.info("===SnomedId Created: " + snomedId);
		} catch (Exception cE) {
			logger.error("Message : SnomedId creation error for UUID :" + uuid, cE);
		}
		return snomedId;
	}





	public static String getSnomedId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		String snomedId = "";
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation(); // Actual value for identifier
				int snomedRTNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedRTNid = ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedRTNid == arcAuxSnomedRTNid) {
					snomedId = (String) denotion;
				}else if (snomedId.equals("") && snomedRTNid == arcAuxSnomedRTNid) {
					snomedId = (String) denotion;
				}
			}
		}

		return snomedId.toString();
	}



	public static String getCtv3Id(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		String ctv3Id = ""; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<?> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				// Actual value for identifier
				int ctv3Nid = i_IdVersion.getAuthorityNid();
				int arcAuxCtv3Nid = ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && ctv3Nid == arcAuxCtv3Nid) {
					ctv3Id = (String) denotion;
					// logger.error("=======ctv3id value=======" + ctv3Id);
				}else if (ctv3Id.equals("") && ctv3Nid == arcAuxCtv3Nid){
					ctv3Id = (String) denotion;
				}
			}
		}
		return ctv3Id.toString();
	}


	// get the Ctv3Id for the given UUID
	public static String getCTV3ID(Config config, UUID uuid) {
		String ctv3Id = null;

		try {
			IdAssignmentImpl idGen = getIdGeneratorClient( config);
			ctv3Id = idGen.getCTV3ID(uuid);
			if( ctv3Id !=null && !ctv3Id.equals("")  ){ 
				return ctv3Id;
			}
			ctv3Id = idGen.createCTV3ID(uuid);
			logger.info("===Ctv3Id Created Successfully " + ctv3Id);

		}
		catch (Exception cE) {
			logger.error("Message : Ctv3Id creation error for UUID :" + uuid, cE);
		}
		return ctv3Id;
	}


	public static boolean IsConceptInActive(I_GetConceptData concept, String effectiveTimeRelStr) throws ParseException, TerminologyException, IOException {
		String conceptStatus = "";
		boolean isInActive = false;
		// set time when Limited concept is no longer current/active
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		List<? extends I_ConceptAttributeTuple> attributes = concept.getConceptAttributeTuples(null, config.getViewPositionSetReadOnly(), 
				Precedence.PATH, config.getConflictResolutionStrategy());
		if (attributes != null && !attributes.isEmpty()) {
			I_ConceptAttributeTuple attribute = attributes.iterator().next();
			conceptStatus = getStatusType(attribute.getStatusNid());
			Date et = new Date(attribute.getTime());
			if (conceptStatus.equals("0")) {
				isInActive = false;
			} else if (effectiveTimeRelStr.compareTo(I_Constants.limited_policy_change)<0 && conceptStatus.equals("6")) {
				isInActive = false;
			} else {
				isInActive = true;
			}
		}
		return isInActive;
	}
	public static boolean IsConceptInActiveNew(I_GetConceptData concept, String effectiveTimeRelStr) throws ParseException, TerminologyException, IOException {
		String conceptStatus = "";
		Date effectiveTimeRelDt = ExportUtil.DATEFORMAT.parse(effectiveTimeRelStr);
		boolean isInActive = false;
		Date LIMITED = ExportUtil.DATEFORMAT.parse(I_Constants.limited_policy_change); // 20100131T000100Z
		// set time when Limited concept is no longer current/active
		I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = concept.getConAttrs();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
		String conceptPriorStatus="";
		if (conceptAttributeTupleList.size() > 0 && conceptAttributeTupleList != null) {
			Date effectiveTimeConDt = null;
			for (int i = 0; i < conceptAttributeTupleList.size(); i++) {
				I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributeTupleList.get(i);
				conceptStatus = getStatusType(i_ConceptAttributeTuple.getConceptStatus());
				Date et = new Date(ExportUtil.getTermFactory().convertToThickVersion(i_ConceptAttributeTuple.getVersion()));
				String effectiveTimeConStr = ExportUtil.DATEFORMAT.format(et);
				effectiveTimeConDt = ExportUtil.DATEFORMAT.parse(effectiveTimeConStr);
				//System.out.println(" iteration et  " + et + " & LIMITED " + LIMITED + "effectiveTimeConStr=" + effectiveTimeConStr + "I_Constants.limited_policy_change =" +  I_Constants.limited_policy_change);

				if (effectiveTimeConDt.before(effectiveTimeRelDt) || effectiveTimeConDt.equals(effectiveTimeRelDt)) {
					if (conceptStatus.equals("0")) {
						isInActive = false;
					}else if (et.before(LIMITED) && conceptStatus.equals("6")) {
						isInActive = false;
					} else {
						isInActive = true;
					}
					conceptPriorStatus = conceptStatus;
				}
			}			

			if (conceptStatus.equals("6")  && effectiveTimeConDt.before(effectiveTimeRelDt) 
					&& (effectiveTimeRelDt.equals(LIMITED) || effectiveTimeRelDt.after(LIMITED))
					&&  effectiveTimeConDt != null) {
				isInActive = true;
			} else if (conceptPriorStatus.equals("6")  
					&& (effectiveTimeRelDt.equals(LIMITED) || effectiveTimeRelDt.after(LIMITED))
					&&  effectiveTimeConDt != null && !conceptPriorStatus.equals("")) {
				isInActive = true;
			}
		}
		return isInActive;
	}


	public static String getRefsetId(String typeId) throws IOException, Exception {
		String refsetId = "99";
		if (typeId.equals("149016008"))
			refsetId = I_Constants.MAY_BE_REFSET_ID;
		else if (typeId.equals("384598002"))
			refsetId = I_Constants.MOVED_FROM_REFSET_ID;
		else if (typeId.equals("370125004"))
			refsetId = I_Constants.MOVED_TO_REFSET_ID;
		else if (typeId.equals("370124000"))
			refsetId = I_Constants.REPLACED_BY_REFSET_ID;
		else if (typeId.equals("168666000"))
			refsetId = I_Constants.SAME_AS_REFSET_ID;
		else if (typeId.equals("159083000"))
			refsetId = I_Constants.WAS_A_REFSET_ID;
		return refsetId;
	}

	public static int getNid(String struuid) throws TerminologyException, IOException {
		int nid = 0;
		ArrayList<UUID> uuidList = new ArrayList<UUID>();
		UUID uuid = UUID.fromString(struuid); // SNOMED Core Inferred 5e51196f-903e-5dd4-8b3e-658f7e0a4fe6
		uuidList.add(uuid);
		I_GetConceptData findPathCon = getTermFactory().getConcept(uuidList);
		nid = findPathCon.getConceptNid();
		return nid;
	}

	public static int getSnomedCorePathNid() {
		int snomedCorePathNid = 0;
		try {
			snomedCorePathNid = getNid(I_Constants.SNOMED_CORE_PATH_UID);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedCorePathNid;
	}

	public static int getSnomedMetaPathNid() {
		int snomedMetaPathNid = 0;
		try {
			snomedMetaPathNid = getNid(I_Constants.SNOMED_META_PATH_UID);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedMetaPathNid;
	}

	public static int getSnorocketAuthorNid() {
		int snorocketAuthorNid = 0;
		try {
			snorocketAuthorNid = getNid(I_Constants.SNOROCKET_AUTHOR_UID); // Snorocket Author Nid (Classifier) Inferred Rels
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snorocketAuthorNid;
	}

	public static int getUserAuthorNid() {
		try {
			userAuthorNid = getNid(I_Constants.USER_AUTHOR_UID); // User Author Nid (Stated Rels)
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return userAuthorNid;
	}

	public static int getSnomedInferredPathNid() {
		int snomedInferredPathNid = 0;
		try {
			snomedInferredPathNid = getNid(I_Constants.SNOMED_INFERRED_PATH_UID); // SNOMED Core Inferred
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedInferredPathNid;
	}

	public static int getSnomedStatedPathNid() {
		int snomedStatedPathNid = 0;
		try {
			snomedStatedPathNid = getNid(I_Constants.SNOMED_STATED_PATH_UID); // SNOMED Stated
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (TerminologyException te) {
			logger.error(te.getMessage());
		}
		return snomedStatedPathNid;
	}

	public static boolean isOnPath(int onPath, int nid) throws IOException, TerminologyException {
		boolean snomedPathFlag = false;
		int pathId = 0;
		I_GetConceptData con = getTermFactory().getConcept(nid);
		I_ConceptAttributeVersioned<?> i_ConceptAttributeVersioned = con.getConAttrs();
		List<? extends I_ConceptAttributeTuple> conceptAttributeTupleList = i_ConceptAttributeVersioned.getTuples();
		for (int i = 0; i < conceptAttributeTupleList.size(); i++) {
			I_ConceptAttributeTuple<?> i_ConceptAttributeTuple = (I_ConceptAttributeTuple<?>) conceptAttributeTupleList.get(i);
			pathId = i_ConceptAttributeTuple.getPathNid();
			if (pathId == onPath) {
				snomedPathFlag = true;
			}
		}
		return snomedPathFlag;
	}

	public static String getSctId(int nid, int pathNid) throws IOException, TerminologyException {
		Long sctId = null;
		I_Identify identify = getTermFactory().getId(nid);
		if (identify==null) return null;
		List<? extends I_IdVersion> i_IdentifyList = identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int sctNid = i_IdVersion.getPathNid();
				if (sctNid == pathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					sctId = (Long) denotion;
				} else if (sctNid == getSnomedInferredPathNid() && snomedIntegerNid == arcAuxSnomedIntegerNid) { // -2147480867
					sctId = (Long) denotion; // Inferred Path
				} else if (sctNid == getSnomedStatedPathNid() && snomedIntegerNid == arcAuxSnomedIntegerNid) { // -2147480865
					sctId = (Long) denotion; // Stated Path
				} else if (sctId==null && snomedIntegerNid == arcAuxSnomedIntegerNid) { // -2147480865
					sctId = (Long) denotion; 
				}
			}
		}
		if (sctId == null)
			return null;
		return sctId.toString();
	}

	public static String getRelationshipIdVersion(Object denotion, int snomedAuthorityNid) throws IOException, TerminologyException {
		Long sctId = null;

		int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();

		if (snomedAuthorityNid == arcAuxSnomedIntegerNid) {
			sctId = (Long) denotion;
		} else {
			// System.out.println("===this is an uuid not relationship id"); //Special handling for uuid
			sctId = 1L;
		}

		if (sctId == null)
			sctId = 0L; // This is temporary fix to avoid null pointer exception...

		return sctId.toString();

	}

	//Get the sctid for the given UUID
	public static String getSCTId(Config config, UUID componentUuid, Integer namespaceId, String partitionId, String releaseId, String executionId, String moduleId) {

		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		Long sctId = 0L;

		try {
			sctId = idGen.getSCTID(componentUuid);
			if (sctId!=null && !sctId.equals("") ){
				return String.valueOf(sctId);
			}
			sctId = idGen.createSCTID(componentUuid, namespaceId, partitionId, releaseId, executionId, moduleId);
		} catch (Exception cE) {
			logger.error("Message : SCTID creation error for UUID :" + componentUuid, cE);
		}
		return String.valueOf(sctId);
	}


	// get the conceptid for the given UUID (Hardcoded values)
	public static String getSCTId(Config config, UUID uuid) {
		try{
			String namespaceId = null;
			String partitionId = null;
			String releaseId = null;
			String executionId = null;
			String sctModuleId = null;
			if(!config.getNamespaceId().equals(null))
			{
				namespaceId = config.getNamespaceId();
				partitionId = config.getPartitionId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
				sctModuleId = config.getModuleId();
				return getSCTId(config,uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);
			}	
		} catch (Exception cE) {
			logger.error("Message : SCTID creation error for UUID :" + uuid, cE);
		}
		return null;
	}

	public static String getConceptId(I_GetConceptData concept, int snomedCorePathNid) throws IOException, TerminologyException {
		Long conceptId = null; // ConceptId
		I_Identify i_Identify = concept.getIdentifier();
		List<? extends I_IdVersion> i_IdentifyList = i_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					try {
						conceptId = (Long) denotion;
					} catch (java.lang.ClassCastException e) {
						// e.printStackTrace();
						// This is all the subset which gets imported and subsetoriginalid becomes conceptid (not need to extracted)
						// System.out.println("ClassCastException ===>" + concept.getInitialText());
					}
				}else if(conceptId==null && snomedIntegerNid == arcAuxSnomedIntegerNid){
					try {
						conceptId = (Long) denotion;
					} catch (java.lang.ClassCastException e) {
						// e.printStackTrace();
						// This is all the subset which gets imported and subsetoriginalid becomes conceptid (not need to extracted)
						// System.out.println("ClassCastException ===>" + concept.getInitialText());
					}	
				}
			}
		}

		/*	
	   if(conceptId.toString().equals("0")){
			System.out.println("==conceptId==" + conceptId.toString());
		}*/

		if (conceptId==null) return null;

		return conceptId.toString();
	}


	// get the conceptid for the given UUID using Specific namespace and partition values
	public static String getConceptId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		long conceptId = 0L;

		String partitionId = "";
		String sctModuleId = "Concept Component";

		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {

			conceptId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no conceptid in the repository so we are getting NULL
			if (logger.isDebugEnabled())
				logger.debug("getSCTID for UUID : " + uuid + " returned NULL calling create to generate a new SCTID");

			try{
				if(!config.getReleaseId().equals(null)) {
					namespaceId = config.getNamespaceId();
					releaseId	= config.getReleaseId();
					executionId = config.getExecutionId();
				}
				conceptId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);

			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}
		return String.valueOf(conceptId);
	}



	// get the descriptionid for the given UUID using Specific namespace and partition values
	public static String getDescriptionId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);
		long descriptionId = 0L;

		String partitionId = "";
		String sctModuleId = "Description Component";		
		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {
			if(!config.getReleaseId().equals(null)) {
				namespaceId = config.getNamespaceId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
			}		
			descriptionId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no descriptionid in the repository so we are getting NULL
			try{
				descriptionId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);

			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}

		return String.valueOf(descriptionId);
	}





	// get the relationshipId for the given UUID using Specific namespace and partition values
	public static String getRelationshipId(Config config, UUID uuid) {
		IdAssignmentImpl idGen = getIdGeneratorClient( config);

		long relationshipId = 0L;
		String partitionId = "";
		String sctModuleId = "Relationship Component";
		String namespaceId = "";
		String releaseId = "";
		String executionId = "";

		try {
			if(!config.getReleaseId().equals(null)) {
				namespaceId = config.getNamespaceId();
				releaseId	= config.getReleaseId();
				executionId = config.getExecutionId();
			}		
			relationshipId = idGen.getSCTID(uuid);
		} catch (NullPointerException Ne) {
			// There is no relationshipId in the repository so we are getting NULL
			try{
				relationshipId = idGen.createSCTID(uuid, Integer.parseInt(namespaceId) , partitionId, releaseId, executionId, sctModuleId);
			} catch (Exception Ex) {
				logger.error("Message : SCTID creation error for UUID :"  + uuid, Ex);
			}

		} catch (Exception e) {
			logger.error("Exception Message : "  + uuid, e);
		}

		return String.valueOf(relationshipId);
	}

	public static boolean insertSctId(int componentNid , Config config, String wsSctId , int pathNid , int statusNid) throws IOException {
		boolean flag = false;
		try {	
			I_Identify i_Identify = getTermFactory().getId(componentNid);	
			I_GetConceptData commitedConcept = getTermFactory().getConceptForNid(componentNid);
			flag = i_Identify.addLongId(Long.parseLong(wsSctId), ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid(),
					statusNid,
					Long.MAX_VALUE,
					aceConfig.getEditCoordinate().getAuthorNid(),
					aceConfig.getEditCoordinate().getModuleNid(),
					pathNid);
			getTermFactory().addUncommitted(commitedConcept);

		} catch (Exception ex) {
			logger.error(ex);
		}
		return flag;
	}

	public static String getPartitionId(String sctId) {
		int slength = sctId.length();
		String partId = sctId.substring(slength - 3, slength - 1);
		return partId;
	}

	public static String getSnomedDescriptionType(int wbDescriptionType) {
		String snomedDescType = "99";
		try {
			int fsnId_Term_Aux = ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid();
			int symId_Term_Aux = ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid();
			int textDefId_Term_Aux = ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid();
			int ptId = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			int fsnId =SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID(); //Fully specified name	
			int symId = getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"); //Synonym
			int textDefId = getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"); //Definition

			// Note: for RF2, There will not be any PT only Sym or FSN (text definition will be exported separately)
			if (wbDescriptionType == ptId) // Preferred Term
				snomedDescType = "2";
			else if (wbDescriptionType == symId) // Synonym
				snomedDescType = "2";
			else if (wbDescriptionType == symId_Term_Aux) // Synonym
				snomedDescType = "2";
			else if (wbDescriptionType == fsnId) // FSN
				snomedDescType = "3";
			else if (wbDescriptionType == fsnId_Term_Aux) // FSN
				snomedDescType = "3";
			else if (wbDescriptionType == textDefId) // Text Definition
				snomedDescType = "4";
			else if (wbDescriptionType == textDefId_Term_Aux) // Text Definition
				snomedDescType = "4";

		} catch (IOException e) {
			logger.error("IOExceptions: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Exceptions: " + e.getMessage());
		}
		return snomedDescType;
	}

	public static String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {

		Long descriptionId = null; //If description is new then descriptionid doesn't exist in workbench so use dummy value.
		I_Identify desc_Identify = getTermFactory().getId(descriptionNid);
		List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					descriptionId = (Long) denotion;
				}else if (descriptionId==null && snomedIntegerNid == arcAuxSnomedIntegerNid){
					descriptionId = (Long) denotion;
				}
			}
		}
		/*if(descriptionId.toString().equals("0")){
			System.out.println("==descriptionId==" + descriptionId.toString());
		}*/
		if (descriptionId==null)return null;

		return descriptionId.toString();
	}

	public static String getCharacteristicType(int type) throws IOException, TerminologyException {
		String charType = "99";

		int defId =getNid("e607218d-7027-3058-ae5f-0d4ccd148fd0"); //	
		int qualId = getNid("569dac14-a8a5-3cf0-b608-5ae2f1c89461"); //
		int hisId = getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"); //
		int addId = getNid("85aba419-17fe-3033-a7c2-21df0af84176"); //
		int statedId = getNid("3b0dbd3b-2e53-3a30-8576-6c7fa7773060"); //statedId
		int inferredId = getNid("1290e6ba-48d0-31d2-8d62-e133373c63f5"); //inferredId

		int defId_Term_Aux  = ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid();
		int qualId_Term_Aux  = ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.localize().getNid();
		int hisId_Term_Aux  = ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.localize().getNid();
		int addId_Term_Aux = ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.localize().getNid();
		int statedId_Term_Aux = ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.localize().getNid();

		if (type == inferredId) {
			charType = "0";
		} else if (type == defId) {
			charType = "0";
		} else if (type == defId_Term_Aux) {
			charType = "0";
		} else if (type == qualId) {
			charType = "1";
		} else if (type == qualId_Term_Aux) {
			charType = "1";
		} else if (type == hisId) {
			charType = "2";
		} else if (type == hisId_Term_Aux) {
			charType = "2";
		} else if (type == addId) {
			charType = "3";
		} else if (type == addId_Term_Aux) {
			charType = "3";
		} else if (type == statedId) {
			charType = "4";
		} else if (type == statedId_Term_Aux) {
			charType = "4";
		}
		return charType;
	}


	public static String getSNOMEDrelationshipType(int type) throws TerminologyException, IOException {
		// Real RF2 data will no longer have PT just Synonym (needs modification)
		String relType = "99";
		int isA_Term_Aux = ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid();
		int movedElsewhere_Term_Aux = ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.localize().getNid();
		int isSameAs_Term_Aux = ArchitectonicAuxiliary.Concept.IS_SAME_AS_REL.localize().getNid();
		int dup_Term_Aux = ArchitectonicAuxiliary.Concept.DUP_REL_TYPE.localize().getNid();
		int isADup_Term_Aux = ArchitectonicAuxiliary.Concept.IS_A_DUP_REL.localize().getNid();

		List<? extends I_DescriptionTuple> descVersions = getTermFactory().getConcept(type).getDescriptionTuples();
		for (I_DescriptionTuple<?> descTuple : descVersions) {
			I_DescriptionPart<?> descPart = descTuple.getMutablePart();
			UUID uuidDesc = getTermFactory().getUids(descTuple.getDescVersioned().getDescId()).iterator().next();
			String descriptionText = descTuple.getText();
			int descriptionNid = descPart.getTypeNid();
			int prefferedNid = ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid();
			if (descriptionNid == prefferedNid) {
				relType = descriptionText;
			}
		}
		return relType;
	}

	public static String getTypeId(String descType) {
		String typeId = "";

		if (descType.equals("4")) {
			typeId = I_Constants.DEFINITION;
		} else if (descType.equals("3")) {
			typeId = I_Constants.FSN;
		} else {
			typeId = I_Constants.SYN;
		}

		return typeId;
	}


	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			//getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID) }), Integer.MAX_VALUE));
			//getAceConfig().addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString(I_Constants.SNOMED_INFERRED_PATH_UID) }), Integer.MAX_VALUE));
			//PositionSetReadOnly o = aceConfig.getViewPositionSetReadOnly();
			aceConfig.addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c") }), Integer.MAX_VALUE));
			aceConfig.addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c")}));

			//getAceConfig().addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());//Fully specified name	
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"));

			aceConfig.setDefaultStatus(getTermFactory().getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID())); // Current
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_CURRENT_NID()); // Current
			aceConfig.getAllowedStatus().add(getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724")); //Retired
			aceConfig.getAllowedStatus().add(getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3")); //Concept non-current (foundation metadata concept)
			aceConfig.getAllowedStatus().add(getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9")); //Pending
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_LIMITED_NID()); //Limited	900000000000486000
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid()); //Outdated	900000000000483008

			aceConfig.getAllowedStatus().add(getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c")); //Component Moved elsewhere	900000000000487009
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid()); //Erroneous component (foundation metadata concept)	900000000000485001
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid());  //Ambiguous component (foundation metadata concept)	900000000000484002
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid());  //Dups	900000000000482003
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			aceConfig.setPrecedence(Precedence.TIME);

			I_IntSet allowedDestRelTypes = getTermFactory().newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			//uids.add(UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));	

			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();

			Set<? extends I_GetConceptData> testObj = concept.getDestRelOrigins(aceConfig.getAllowedStatus(), aceConfig.getDestRelTypes(), aceConfig.getViewPositionSetReadOnly(), aceConfig
					.getPrecedence(), aceConfig.getConflictResolutionStrategy());
			childrenSet.addAll(testObj);
			descendants.addAll(childrenSet);

			/*for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return descendants;

	}






	/*

	public static Set<I_GetConceptData> getParent(Set<I_GetConceptData> parent, I_GetConceptData concept) {
		try {

			//PositionSetReadOnly o = aceConfig.getViewPositionSetReadOnly();
			aceConfig.addViewPosition(getTermFactory().newPosition(getTermFactory().getPath(new UUID[] { UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c") }), Integer.MAX_VALUE));
			aceConfig.addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString("2b2035dd-9419-56e2-a472-636e8545742c")}));

			//getAceConfig().addEditingPath(getTermFactory().getPath(new UUID[] {UUID.fromString(I_Constants.SNOMED_CORE_PATH_UID)}));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID());//Fully specified name	
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("8bfba944-3965-3946-9bcb-1e80a5da63a2"));
			aceConfig.getDescTypes().add(ArchitectonicAuxiliary.Concept.TEXT_DEFINITION_TYPE.localize().getNid());
			aceConfig.getDescTypes().add(getNid("700546a3-09c7-3fc2-9eb9-53d318659a09"));

			aceConfig.setDefaultStatus(getTermFactory().getConcept(SnomedMetadataRfx.getSTATUS_CURRENT_NID())); // Current
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_CURRENT_NID()); // Current
			aceConfig.getAllowedStatus().add(getNid("a5daba09-7feb-37f0-8d6d-c3cadfc7f724")); //Retired
			aceConfig.getAllowedStatus().add(getNid("6cc3df26-661e-33cd-a93d-1c9e797c90e3")); //Concept non-current (foundation metadata concept)
			aceConfig.getAllowedStatus().add(getNid("9906317a-f50f-30f6-8b59-a751ae1cdeb9")); //Pending

			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_LIMITED_NID()); //Limited	900000000000486000
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_OUTDATED().getLenient().getNid()); //Outdated	900000000000483008
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_INAPPROPRIATE_NID()); //In-appropriate	900000000000494007
			aceConfig.getAllowedStatus().add(getNid("95028943-b11c-3509-b1c0-c4ae16aaad5c")); //Component Moved elsewhere	900000000000487009

			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_ERRONEOUS().getLenient().getNid()); //Erroneous component (foundation metadata concept)	900000000000485001
			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_AMBIGUOUS().getLenient().getNid());  //Ambiguous component (foundation metadata concept)	900000000000484002

			aceConfig.getAllowedStatus().add(SnomedMetadataRfx.getSTATUS_DUPLICATE().getLenient().getNid());  //Dups	900000000000482003
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			aceConfig.getAllowedStatus().add(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid());

			aceConfig.setPrecedence(Precedence.TIME);

			I_IntSet allowedDestRelTypes = getTermFactory().newIntSet();
			Collection<UUID> uids = new ArrayList<UUID>();
			UUID uuid = UUID.fromString(I_Constants.IS_A_UID);
			uids.add(uuid);
			//uids.add(UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
			allowedDestRelTypes.add(getTermFactory().uuidToNative(uids));	

			Set<I_GetConceptData> parentSet = new HashSet<I_GetConceptData>();

			Set<? extends I_GetConceptData> testObj = concept.getSourceRelTargets(aceConfig.getAllowedStatus(), aceConfig.getDestRelTypes(), aceConfig.getViewPositionSetReadOnly(), aceConfig
					.getPrecedence(), aceConfig.getConflictResolutionStrategy());
			parentSet.addAll(testObj);
			parent.addAll(parentSet);


			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getParent(descendants, loopConcept);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return parent;

	}*/

	public static String getCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "";

		if (characteristicType.equals("0")) {
			characteristicTypeId = I_Constants.DEFININGRELATION;
		} else if (characteristicType.equals("1")) {
			characteristicTypeId = I_Constants.QUALIFYRELATION;
		} else if (characteristicType.equals("2")) {
			characteristicTypeId = I_Constants.HISTORICAL;
		} else if (characteristicType.equals("3")) {
			characteristicTypeId = I_Constants.ADDITIONALRELATION;
		} else if (characteristicType.equals("4")) {
			characteristicTypeId = I_Constants.STATED;
		}

		return characteristicTypeId;
	}

	public static String getInferredCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "99";

		if (characteristicType.equals("0")) {
			characteristicTypeId = I_Constants.INFERRED;
		} else if (characteristicType.equals("3")) {
			characteristicTypeId = I_Constants.ADDITIONALRELATION;
		} 

		return characteristicTypeId;
	}


	public static String getStatedCharacteristicTypeId(String characteristicType) {
		String characteristicTypeId = "99";

		if (characteristicType.equals("4")) {
			characteristicTypeId = I_Constants.STATED;
		}

		return characteristicTypeId;
	}

}
