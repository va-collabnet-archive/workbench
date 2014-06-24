package org.ihtsdo.project.util;

import java.io.File;

public class RF2FileRetrieve {
	private String releaseFolder;

	private String conceptFile = "";
	private String descriptionFile = "";
	private String relationshipFile = "";
	private String qualifierFile = "";
	private String attributeValueFile = "";
	private String simpleMapFile = "";
	private String associationFile = "";
	private String languageFile = "";
	private String identifierFile = "";
	private String refsetSimpleFile = "";
	private String ICD9CrossMapFile = "";
	private String statedRelationshipFile = "";
	private String textDefinitionFile = "";
	private String refinabilityFile = "";
	private String descriptionTypeFile = "";
	private String refsetDescriptorFile = "";
	private String moduleDependencyFile = "";
	/* Snapshot files */
	private String snapshotConceptFile = "";
	private String snapshotDescriptionFile = "";
	private String snapshotRelationshipFile = "";
	private String snapshotQualifierFile = "";
	private String snapshotAttributeValueFile = "";
	private String snapshotSimpleMapFile = "";
	private String snapshotAssociationFile = "";
	private String snapshotLanguageFile = "";
	private String snapshotIdentifierFile = "";
	private String snapshotRefsetSimpleFile = "";
	private String snapshotICD9CrossMapFile = "";
	private String snapshotStatedRelationshipFile = "";
	private String snapshotTextDefinitionFile = "";
	private String snapshotRefinabilityFile = "";
	private String snapshotDescriptionTypeFile = "";
	private String snapshotRefsetDescriptorFile = "";
	private String snapshotModuleDependencyFile = "";

	private String crossMapICD9TgtAuxRF2File = "";
	private String crossMapICDOTgtAuxRF2File = "";
	private String compHistoryAuxiliaryFile = "";
	private String associationAuxiliaryFile = "";
	private String relationshipAuxiliaryFile = "";
	private String statedRelationshipAuxiliaryFile = "";

	private static final String ATTRIBUTEVALUE_FILENAME_PART = "_crefset_attributevalue";
	private static final String END_FILE = ".txt";
	private static final String ASSOCIATION_FILENAME_PART = "_crefset_associationreference";
	private static final String LANGUAGE_FILENAME_PART = "_crefset_languagefull";
	private static final String IDENTIFIER_FILENAME_PART = "_identifier_";
	private static final String SIMPLEMAP_FILENAME_PART = "srefset_simplemap";
	private static final String CONCEPT_FILENAME_PART = "_concept_";
	private static final String DESCRIPTION_FILENAME_PART = "_description_";
	private static final String RELATIONSHIP_FILENAME_PART = "_relationship_";
	private static final String STATEDRELATIONSHIP_FILENAME_PART = "_statedrelationship_";
	private static final String REFSETSIMPLE_FILENAME_PART = "_refset_simple";
	private static final String TEXTDEFINITION_FILENAME_PART = "_textdefinition_";
	private static final String DESCRIPTIONTYPE_FILENAME_PART = "_cirefset_descriptiontype";
	private static final String REFSETDESCRIPTOR_FILENAME_PART = "_ccirefset_refsetdescriptor";
	private static final String MODULEDEPENDENCY_FILENAME_PART = "_ssrefset_moduledependency";
	private static final String ICD9_CROSSMAP_FILENAME_PART = "_iissscrefset_complexmap";
	public RF2FileRetrieve(String releaseFolder) throws Exception {
		super();
		this.releaseFolder = releaseFolder;
		File rf = new File(releaseFolder);
		getFiles(rf);
	}

	private void getFiles(File releaseFolder2) {
		File[] fileList = releaseFolder2.listFiles();
		for (File file : fileList) {
			if (file.isDirectory()) {
				getFiles(file);
			} else {
				if(!file.isHidden() && file.getName().endsWith(END_FILE)){
					if(file.getName().toLowerCase().contains(ATTRIBUTEVALUE_FILENAME_PART)){
						attributeValueFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(ASSOCIATION_FILENAME_PART)){
						associationFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(LANGUAGE_FILENAME_PART)){
						languageFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(IDENTIFIER_FILENAME_PART)){
						identifierFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(SIMPLEMAP_FILENAME_PART)){
						simpleMapFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(CONCEPT_FILENAME_PART)){
						conceptFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(DESCRIPTION_FILENAME_PART)){
						descriptionFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(RELATIONSHIP_FILENAME_PART)){
						relationshipFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(STATEDRELATIONSHIP_FILENAME_PART)){
						statedRelationshipFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(REFSETSIMPLE_FILENAME_PART)){
						refsetSimpleFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(TEXTDEFINITION_FILENAME_PART)){
						textDefinitionFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(DESCRIPTIONTYPE_FILENAME_PART)){
						descriptionTypeFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(REFSETDESCRIPTOR_FILENAME_PART)){
						refsetDescriptorFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(MODULEDEPENDENCY_FILENAME_PART)){
						moduleDependencyFile = file.getAbsolutePath();
					}else if(file.getName().toLowerCase().contains(ICD9_CROSSMAP_FILENAME_PART)){
						ICD9CrossMapFile = file.getAbsolutePath();
					}
				}
			}
		}
	}


	/**
	 * @return the releaseFolder
	 */
	public String getReleaseFolder() {
		return releaseFolder;
	}

	/**
	 * @return the conceptFile
	 */
	public String getConceptFile() {
		return conceptFile;
	}

	/**
	 * @return the descriptionFile
	 */
	public String getDescriptionFile() {
		return descriptionFile;
	}

	/**
	 * @return the relationshipFile
	 */
	public String getRelationshipFile() {
		return relationshipFile;
	}

	/**
	 * @return the attributeValueFile
	 */
	public String getAttributeValueFile() {
		return attributeValueFile;
	}

	/**
	 * @return the simpleMapFile
	 */
	public String getSimpleMapFile() {
		return simpleMapFile;
	}

	/**
	 * @return the associationFile
	 */
	public String getAssociationFile() {
		return associationFile;
	}

	/**
	 * @return the languageFile
	 */
	public String getLanguageFile() {
		return languageFile;
	}

	public String getQualifierFile() {
		return qualifierFile;
	}

	public String getRefsetSimpleFile() {
		return refsetSimpleFile;
	}

	public String getICD9CrossMapFile() {
		return ICD9CrossMapFile;
	}

	public String getStatedRelationshipFile() {
		return statedRelationshipFile;
	}

	public String getTextDefinitionFile() {
		return textDefinitionFile;
	}

	public String getRefinabilityFile() {
		return refinabilityFile;
	}

	public String getDescriptionTypeFile() {
		return descriptionTypeFile;
	}

	public String getRefsetDescriptorFile() {
		return refsetDescriptorFile;
	}

	public String getModuleDependencyFile() {
		return moduleDependencyFile;
	}

	public String getSnapshotConceptFile() {
		return snapshotConceptFile;
	}

	public String getSnapshotDescriptionFile() {
		return snapshotDescriptionFile;
	}

	public String getSnapshotRelationshipFile() {
		return snapshotRelationshipFile;
	}

	public String getSnapshotQualifierFile() {
		return snapshotQualifierFile;
	}

	public String getSnapshotAttributeValueFile() {
		return snapshotAttributeValueFile;
	}

	public String getSnapshotSimpleMapFile() {
		return snapshotSimpleMapFile;
	}

	public String getSnapshotAssociationFile() {
		return snapshotAssociationFile;
	}

	public String getSnapshotLanguageFile() {
		return snapshotLanguageFile;
	}

	public String getSnapshotRefsetSimpleFile() {
		return snapshotRefsetSimpleFile;
	}

	public String getSnapshotICD9CrossMapFile() {
		return snapshotICD9CrossMapFile;
	}

	public String getSnapshotStatedRelationshipFile() {
		return snapshotStatedRelationshipFile;
	}

	public String getSnapshotTextDefinitionFile() {
		return snapshotTextDefinitionFile;
	}

	public String getSnapshotRefinabilityFile() {
		return snapshotRefinabilityFile;
	}

	public String getSnapshotDescriptionTypeFile() {
		return snapshotDescriptionTypeFile;
	}

	public String getSnapshotRefsetDescriptorFile() {
		return snapshotRefsetDescriptorFile;
	}

	public String getSnapshotModuleDependencyFile() {
		return snapshotModuleDependencyFile;
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

	public String getIdentifierFile() {
		return identifierFile;
	}

	public String getSnapshotIdentifierFile() {
		return snapshotIdentifierFile;
	}

	public String getStatedRelationshipAuxiliaryFile() {
		return statedRelationshipAuxiliaryFile;
	}
}
