package org.ihtsdo.rf2.postexport;

import java.io.File;

public class AuxiliaryFilesRetrieve {
	private static final String END_FILE = ".txt";
	private static final String QUALIFIER_FILENAME_PART = "_qualifier_full_int_";
	private static final String DER_REFINABILITY_FILENAME_PART = "der2_crefset_refinabilityfull_int_";
	private static final String RES_REFINABILITY_FILENAME_PART = "res2_crefset_refinabilityfull_int_";

	private static final String ICD9TARGETS_FILENAME_PART = "_crossmaptargets_icd9_int_";
	private static final String CROSSMAPHEADERS_FILENAME_PART = "_crossmapsets_int_";
	private static final String SUBSETHEADERS_FILENAME_PART = "_subsets_int_";
	private static final String ICDOTARGETS_FILENAME_PART = "_crossmaptargets_icdo_int_";
	private static final String COMPONENTHISTAUX_FILENAME_PART = "_componenthistory_";
	private static final String ASSOCIATIONAUX_FILENAME_PART = "res2_identifier_full_int_";
	private static final String RELATIONSHIPSAUX_FILENAME_PART = "_retiredisarelationship_full_int_";

	private static final String STATEDRELATIONSHIPSAUX_FILENAME_PART = "_retiredstatedisarelationship_full_int_";

	private String compatibilityFolder;

	private String snapshotRefinabilityFile = "";
	private String derRefinabilityFile = "";
	private String resRefinabilityFile = "";
	private String snapshotQualifierFile = "";
	private String qualifierFile = "";
	private String crossMapICD9TgtAuxRF2File = "";
	private String subsetHeadersFile = "";
	private String crossmapHeadersFile = "";
	private String crossMapICDOTgtAuxRF2File = "";
	private String compHistoryAuxiliaryFile = "";
	private String associationAuxiliaryFile = "";
	private String relationshipAuxiliaryFile = "";
	private String statedRelationshipAuxiliaryFile = "";

	public AuxiliaryFilesRetrieve(String compatibilityFolder) throws Exception {
		super();
		this.compatibilityFolder = compatibilityFolder;
		File rf = new File(compatibilityFolder);
		getFiles(rf);
	}
	private void getFiles(File releaseFolder2) {
		File[] fileList = releaseFolder2.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				getFiles(file);
			} else {
				if(!file.isHidden() && file.getName().endsWith(END_FILE)){
					if(file.getName().toLowerCase().contains(QUALIFIER_FILENAME_PART)){
						qualifierFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(DER_REFINABILITY_FILENAME_PART)){
						derRefinabilityFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(RES_REFINABILITY_FILENAME_PART)){
						resRefinabilityFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(ICD9TARGETS_FILENAME_PART)){
						crossMapICD9TgtAuxRF2File = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(CROSSMAPHEADERS_FILENAME_PART)){
						crossmapHeadersFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(SUBSETHEADERS_FILENAME_PART)){
						subsetHeadersFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(ICDOTARGETS_FILENAME_PART)){
						crossMapICDOTgtAuxRF2File = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(COMPONENTHISTAUX_FILENAME_PART)){
						compHistoryAuxiliaryFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(ASSOCIATIONAUX_FILENAME_PART)){
						associationAuxiliaryFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(RELATIONSHIPSAUX_FILENAME_PART)){
						relationshipAuxiliaryFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(STATEDRELATIONSHIPSAUX_FILENAME_PART)){
						statedRelationshipAuxiliaryFile = file.getAbsolutePath();
					}
				}
			}
		}
	}

	/**
	 * @return the releaseFolder
	 */
	public String getReleaseFolder() {
		return compatibilityFolder;
	}

	public String getQualifierFile() {
		return qualifierFile;
	}

	public String getDerRefinabilityFile() {
		return derRefinabilityFile;
	}
	public String getResRefinabilityFile() {
		return resRefinabilityFile;
	}

	public String getSnapshotQualifierFile() {
		return snapshotQualifierFile;
	}

	public String getSnapshotRefinabilityFile() {
		return snapshotRefinabilityFile;
	}

	public String getCrossMapICD9TgtAuxRF2File() {
		return crossMapICD9TgtAuxRF2File;
	}

	public String getCrossMapICDOTgtAuxRF2File() {
		return crossMapICDOTgtAuxRF2File;
	}

	public String getCompHistoryAuxiliaryFile() {
		return compHistoryAuxiliaryFile;
	}

	public String getAssociationAuxiliaryFile() {
		return associationAuxiliaryFile;
	}

	public String getRelationshipAuxiliaryFile() {
		return relationshipAuxiliaryFile;
	}

	public String getStatedRelationshipAuxiliaryFile() {
		return statedRelationshipAuxiliaryFile;
	}

	public String getSubsetHeadersFile() {
		return subsetHeadersFile;
	}

	public String getCrossmapHeadersFile() {
		return crossmapHeadersFile;
	}

}
