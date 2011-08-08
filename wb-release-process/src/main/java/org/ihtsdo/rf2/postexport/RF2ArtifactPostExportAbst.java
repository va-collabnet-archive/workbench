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

	
	public static enum FILE_TYPE{

		RF2_CONCEPT(new int[]{0,1},0,new Integer[]{2,3,4},"sct2_Concept_SUFFIX_INT"),
		RF2_DESCRIPTION(new int[]{0,1},0,new Integer[]{2,3,4,5,6,7,8},"sct2_Description_SUFFIX-en_INT"),
		RF2_RELATIONSHIP(new int[]{0,1},0,new Integer[]{2,3,4,5,6,7,8,9},"sct2_Relationship_SUFFIX_INT"), 
		RF2_STATED_RELATIONSHIP(new int[]{0,1},0,new Integer[]{2,3,4,5,6,7,8,9},"sct2_StatedRelationship_SUFFIX_INT"), 
		RF2_QUALIFIER(new int[]{0,1},0,new Integer[]{2,4,5,6,7,8,9},"sct2_Qualifier_SUFFIX_INT"), 
		RF2_LANGUAGE_REFSET(new int[]{0,1},0,new Integer[]{2,4,5,6},"der2_cRefset_LanguageSUFFIX-en_INT"), 
		RF2_ATTRIBUTE_VALUE(new int[]{0,1},0,new Integer[]{2,4,5,6},"der2_cRefset_AttributeValueSUFFIX_INT"),
		RF2_ASSOCIATION(new int[]{0,1},0,new Integer[]{2,4,5,6},"der2_cRefset_AssociationReferenceSUFFIX_INT"),
		RF2_SIMPLE_MAP(new int[]{0,1},0,new Integer[]{2,4,5,6},"der2_Refset_SimpleMapSUFFIX_INT"),
		RF2_SIMPLE(new int[]{0,1},0,new Integer[]{2,4,5},"der2_Refset_SimpleSUFFIX_INT"),
		RF2_TEXTDEFINITION(new int[]{0,1},0,new Integer[]{2,4,5,6,7,8},"sct2_TextDefinition_SUFFIX-en_INT"),
		RF2_ICD9_MAP(new int[]{0,1},0,new Integer[]{2,4,5,6,7,8,9,10,11},"der2_iissscRefset_ICD9CMEquivalenceMapSUFFIX_INT");
		 

		private int[] columnIndexes;
		private Integer[] columnsToCompare;
		private int snapshotIndex;
		private String fileName;
		
		public Integer[] getColumnsToCompare() {
			return columnsToCompare;
		}

		FILE_TYPE(int[] columnIndexes,int snapshotIndex,Integer[] columnsToCompare,String fileName){
			this.columnIndexes=columnIndexes;
			this.columnsToCompare=columnsToCompare;
			this.snapshotIndex=snapshotIndex;
			this.fileName=fileName;
		}

		public int[] getColumnIndexes() {
			return columnIndexes;
		}

		public int getSnapshotIndex() {
			return snapshotIndex;
		}

		public String getFileName() {
			return fileName;
		}
	};

	public File getPreviousFile(String rf2FullFolder,FILE_TYPE fType) throws Exception {

		RF2FileRetrieve RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
		String retFile=null;
		switch (fType){
		case RF2_CONCEPT:
			retFile=RF2fRetrieve.getConceptFile();
			break;
		case RF2_DESCRIPTION:
			retFile=RF2fRetrieve.getDescriptionFile();
			break;
		case RF2_RELATIONSHIP:
			retFile=RF2fRetrieve.getRelationshipFile();
			break; 
		case RF2_LANGUAGE_REFSET:
			retFile=RF2fRetrieve.getLanguageFile();
			break; 
		case RF2_ATTRIBUTE_VALUE:
			retFile=RF2fRetrieve.getAttributeValueFile();
			break;
		case RF2_ASSOCIATION:
			retFile=RF2fRetrieve.getAssociationFile();
			break;
		case RF2_SIMPLE_MAP:
			retFile=RF2fRetrieve.getSimpleMapFile();
			break;
		case RF2_STATED_RELATIONSHIP:
			retFile=RF2fRetrieve.getStatedRelationshipFile();
			break; 
		case RF2_QUALIFIER:
			retFile=RF2fRetrieve.getQualifierFile();
			break; 
		case RF2_TEXTDEFINITION:
			retFile=RF2fRetrieve.getTextDefinitionFile();
			break;
		case RF2_ICD9_MAP:
			retFile=RF2fRetrieve.getICD9CrossMapFile();
			break;
		case RF2_SIMPLE:
			retFile=RF2fRetrieve.getRefsetSimpleFile();
			break;
		}
		if (retFile==null){
			return null;
		}
		return new File(retFile);
	}
	public File getFullOutputFile(String parentFolder,FILE_TYPE fType,String date){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",fullSuffix);
		retFile+="_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public File getDeltaOutputFile(String parentFolder,FILE_TYPE fType,String date, String previuosReleaseDate){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",deltaSuffix);
		retFile+="_" + previuosReleaseDate + "_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public File getSnapshotOutputFile(String parentFolder,FILE_TYPE fType,String date){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",snapshotSuffix);
		retFile+="_" + date + endFile;
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
