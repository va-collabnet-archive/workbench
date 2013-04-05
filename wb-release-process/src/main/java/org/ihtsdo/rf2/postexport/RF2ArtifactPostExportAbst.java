package org.ihtsdo.rf2.postexport;

import java.io.File;

public abstract class RF2ArtifactPostExportAbst {
	
	private String endFile=".txt";
	private String fullSuffix="Full";
	private String deltaSuffix="Delta";
	private String snapshotSuffix="Snapshot";
	private String fullOutputFolder="Full";
	private String deltaOutputFolder="Delta";
	private String snapshotOutputFolder="Snapshot";

	private String tmpPostExport="tmppostexport";
	private String tmpSort="tmpsort";
	private String tmpTmpSort="tmp";

	private String tmpSnapShot="tmpsnapshot";

	public enum MODULE{
		CLINICAL_MODULE("999000011000000103","UKClinical"),
		DRUG_MODULE("999000011000001104","UKDrug");
		
		private String moduleSCTId;
		private String extensionName;

		MODULE(String moduleSCTId,String extensionName){
			this.moduleSCTId=moduleSCTId;
			this.extensionName=extensionName;
		}

		public String getModuleSCTId() {
			return moduleSCTId;
		}

		public String getExtensionName() {
			return extensionName;
		}
	}
	public static enum FILE_TYPE{

		RF2_CONCEPT(new int[]{0,1},new int[]{0},new Integer[]{2,3,4},"sct2_Concept_SUFFIX_Metadata",1),
		RF2_DESCRIPTION(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8},"sct2_Description_SUFFIX_Metadata",1),
		RF2_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_Relationship_SUFFIX_Metadata",1), 
		RF2_STATED_RELATIONSHIP(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"sct2_StatedRelationship_SUFFIX_Metadata",1), 
		RF2_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{3,4,5},"sct2_Identifier_SUFFIX_Metadata",2),
		RF2_COMPATIBILITY_IDENTIFIER(new int[]{1,2},new int[]{1},new Integer[]{5},"res2_Identifier_SUFFIX_Metadata",2),
		RF2_TEXTDEFINITION(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8},"sct2_TextDefinition_SUFFIX-en-GB_Metadata",1),
		RF2_LANGUAGE_REFSET(new int[]{5,1},new int[]{5},new Integer[]{2,4,6},"der2_cRefset_LanguageSUFFIX-en-GB_Metadata",1), 
		RF2_ATTRIBUTE_VALUE(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6},"der2_cRefset_AttributeValueSUFFIX_Metadata",1),
		RF2_SIMPLE_MAP(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_sRefset_SimpleMapSUFFIX_Metadata",1),
		RF2_SIMPLE(new int[]{4,5,1},new int[]{4,5},new Integer[]{2},"der2_Refset_SimpleSUFFIX_Metadata",1),
		RF2_ASSOCIATION(new int[]{4,5,6,1},new int[]{4,5,6},new Integer[]{2},"der2_cRefset_AssociationReferenceSUFFIX_Metadata",1),
		RF2_QUALIFIER(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9},"sct2_Qualifier_SUFFIX_Metadata",1),
		RF2_ICD9_MAP(new int[]{0,1},new int[]{0},new Integer[]{2,4,5,6,7,8,9,10,11},"der2_iissscRefset_ICD9CMEquivalenceMapSUFFIX_Metadata",1), 
		RF2_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredIsaRelationship_SUFFIX_Metadata",1), 
		RF2_ICDO_TARGETS(new int[]{6,1},new int[]{6},new Integer[]{2},"res2_CrossMapTargets_ICDO_Metadata",1), 
		RF2_STATED_ISA_RETIRED(new int[]{0,1},new int[]{0},new Integer[]{2,3,4,5,6,7,8,9},"res2_RetiredStatedIsaRelationship_SUFFIX_Metadata",1);
		

		private int[] columnIndexes;
		private Integer[] columnsToCompare;
		private int[] snapshotIndex;
		private String fileName;
		private int effectiveTimeColIndex;
		
		public Integer[] getColumnsToCompare() {
			return columnsToCompare;
		}

		FILE_TYPE(int[] columnIndexes,int[] snapshotIndex,Integer[] columnsToCompare,String fileName, int effectiveTimeColIndex){
			this.columnIndexes=columnIndexes;
			this.columnsToCompare=columnsToCompare;
			this.snapshotIndex=snapshotIndex;
			this.fileName=fileName;
			this.effectiveTimeColIndex=effectiveTimeColIndex;
		}

		public int[] getColumnIndexes() {
			return columnIndexes;
		}

		public int[] getSnapshotIndex() {
			return snapshotIndex;
		}

		public String getFileName() {
			return fileName;
		}
		public int getEffectiveTimeColIndex(){
			return effectiveTimeColIndex;
		}
	};

	public File getPreviousFile(String rf2FullFolder,FILE_TYPE fType) throws Exception {
		RF2FileRetrieve RF2fRetrieve=null;
		AuxiliaryFilesRetrieve AuxFileRetrieve=null;
		String retFile=null;
		switch (fType){
		case RF2_CONCEPT:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getConceptFile();
			break;
		case RF2_DESCRIPTION:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getDescriptionFile();
			break;
		case RF2_RELATIONSHIP:		
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getRelationshipFile();
			break; 
		case RF2_STATED_RELATIONSHIP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getStatedRelationshipFile();
			break; 
		case RF2_IDENTIFIER:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getIdentifierFile();
			break; 
		case RF2_TEXTDEFINITION:		
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getTextDefinitionFile();
			break;
		case RF2_LANGUAGE_REFSET:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getLanguageFile();
			break; 
		case RF2_ATTRIBUTE_VALUE:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getAttributeValueFile();
			break;
		case RF2_SIMPLE_MAP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getSimpleMapFile();
			break;
		case RF2_SIMPLE:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getRefsetSimpleFile();
			break;
		case RF2_ASSOCIATION:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getAssociationFile();
			break;
		case RF2_ICD9_MAP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getICD9CrossMapFile();
			break;
		case RF2_QUALIFIER:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getQualifierFile();
			break; 
		case RF2_COMPATIBILITY_IDENTIFIER:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getAssociationAuxiliaryFile();
			break; 
		case RF2_ISA_RETIRED:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getRelationshipAuxiliaryFile();
			break;
		case RF2_STATED_ISA_RETIRED:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getStatedRelationshipAuxiliaryFile();
			break;
		case RF2_ICDO_TARGETS:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getCrossMapICDOTgtAuxRF2File();
			break;
		}
		if (retFile==null){
			return null;
		}
		return new File(retFile);
	}
	public File getFullOutputFile(String parentFolder,FILE_TYPE fType,String date,String extension){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",fullSuffix);
		retFile+=extension +"_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public File getDeltaOutputFile(String parentFolder,FILE_TYPE fType,String date, String previuosReleaseDate,String extension){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",deltaSuffix);
		retFile+=extension +"_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public File getSnapshotOutputFile(String parentFolder,FILE_TYPE fType,String date,String extension){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",snapshotSuffix);
		retFile+=extension + "_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public String getTmpPostExport() {
		return tmpPostExport;
	}
	public String getTmpSort() {
		return tmpSort;
	}
	public String getTmpSnapShot() {
		return tmpSnapShot;
	}
	public String getFullOutputFolder() {
		return fullOutputFolder;
	}
	public String getDeltaOutputFolder() {
		return deltaOutputFolder;
	}
	public String getSnapshotOutputFolder() {
		return snapshotOutputFolder;
	}
	public String getTmpTmpSort() {
		return tmpTmpSort;
	}
}


