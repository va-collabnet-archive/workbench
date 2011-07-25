package org.ihtsdo.rf2.postexport;

import java.io.File;

public class RF2FileRetrieve {
	private static final String TERMINOLOGY_FOLDER ="Terminology";
	private static final String REFSET_FOLDER = "Refset";
	private static final String ATTRIBUTEVALUE_FILENAME_PART = "AttributeValue";
	private static final Object CONTENT_FOLDER = "Content";
	private static final String END_FILE = ".txt";
	private static final String ASSOCIATION_FILENAME_PART = "AssociationReference";
	private static final Object LANGUAGE_FOLDER = "Language";
	private static final String LANGUAGE_FILENAME_PART = "LanguageFull";
	private static final Object CROSSMAP_FOLDER = "CrossMap";
	private static final String SIMPLEMAP_FILENAME_PART = "SimpleMapFull";
	private static final String ICD9MAP_FILENAME_PART = "ICD9CMEquivalence";
	private static final String CONCEPT_FILENAME_PART = "Concept_";
	private static final String DESCRIPTION_FILENAME_PART = "Description_";
	private static final String RELATIONSHIP_FILENAME_PART = "_Relationship_";
	private static final String STATEDRELATIONSHIP_FILENAME_PART = "StatedRelationship_";
	private static final String QUALIFIER_FILENAME_PART = "Qualifier";
	private static final String REFSETSIMPLE_FILENAME_PART = "Refset_Simple";
	private static final String TEXTDEFINITION_FILENAME_PART = "TextDefinition";
	
	private String releaseFolder;
	private String conceptFile;
	private String descriptionFile;
	private String relationshipFile;
	private String qualifierFile;
	private String attributeValueFile;
	private String simpleMapFile;
	private String associationFile;
	private String languageFile;
	private String refsetSimpleFile;
	private String ICD9CrossMapFile;
	private String statedRelationshipFile;
	private String textDefinitionFile;
	
	public RF2FileRetrieve(String releaseFolder) throws Exception {
		super();
		this.releaseFolder = releaseFolder;
		conceptFile="";
		descriptionFile="";
		relationshipFile="";
		qualifierFile="";
		attributeValueFile="";
		simpleMapFile="";
		associationFile="";
		languageFile="";
		refsetSimpleFile="";
		
		File rFolder=new File(this.releaseFolder);
		if (rFolder.isDirectory()){
			for (File folder:rFolder.listFiles()){
				if (folder.isDirectory() && folder.getName().equals(TERMINOLOGY_FOLDER)){
					getTerminologyComponents(folder);
				}
				if (folder.isDirectory() && folder.getName().equals(REFSET_FOLDER)){
					getRefsetComponents(folder);
				}
			}
		}
		String strErr="";
		if (conceptFile.equals("")){
			strErr="Concept file not found.\n";
		}
		if (descriptionFile.equals("")){
			strErr+="Description file not found.\n";
		}

		if (relationshipFile.equals("")){
			strErr+="Relationship file not found.\n";
		}

		if (attributeValueFile.equals("")){
			strErr+="Attribute value file not found.\n";
		}

		if (simpleMapFile.equals("")){
			strErr+="Simple map file not found.\n";
		}

		if (associationFile.equals("")){
			strErr+="Association file not found.\n";
		}

		if (languageFile.equals("")){
			strErr+="Language file not found.\n";
		}
		if (!strErr.equals("")){
			throw new Exception ("Errors in RF2 retrive file: " + strErr);
		}
		
	}
	private void getRefsetComponents(File folder) {
		for (File childFolder :folder.listFiles())		{
			if (childFolder.isDirectory() && childFolder.getName().equals(CONTENT_FOLDER)){
				for (File component:childFolder.listFiles()){
					if (component.getName().indexOf(ATTRIBUTEVALUE_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						attributeValueFile=component.getAbsolutePath();
					}
					if (component.getName().indexOf(ASSOCIATION_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						associationFile=component.getAbsolutePath();
					}
					if (component.getName().indexOf(REFSETSIMPLE_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						refsetSimpleFile=component.getAbsolutePath();
					}
				}
			}
			if (childFolder.isDirectory() && childFolder.getName().equals(LANGUAGE_FOLDER)){
				for (File component:childFolder.listFiles()){
					if (component.getName().indexOf(LANGUAGE_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						languageFile=component.getAbsolutePath();
					}
				}
			}
			if (childFolder.isDirectory() && childFolder.getName().equalsIgnoreCase((String) CROSSMAP_FOLDER)){
				for (File component:childFolder.listFiles()){
					if (component.getName().indexOf(SIMPLEMAP_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						simpleMapFile=component.getAbsolutePath();
					}
					if (component.getName().indexOf(ICD9MAP_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
						ICD9CrossMapFile=component.getAbsolutePath();
					}
				}
			}
		}
	}
	private void getTerminologyComponents(File folder) {
		for (File component:folder.listFiles()){
			if (component.getName().indexOf(CONCEPT_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				conceptFile=component.getAbsolutePath();
			}
			if (component.getName().indexOf(DESCRIPTION_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				descriptionFile=component.getAbsolutePath();
			}
			if (component.getName().indexOf(RELATIONSHIP_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				relationshipFile=component.getAbsolutePath();
			}
			if (component.getName().indexOf(STATEDRELATIONSHIP_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				statedRelationshipFile=component.getAbsolutePath();
			}
		
			if (component.getName().indexOf(QUALIFIER_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				qualifierFile=component.getAbsolutePath();
			}
			if (component.getName().indexOf(TEXTDEFINITION_FILENAME_PART)>-1 && component.getName().toLowerCase().endsWith(END_FILE)){
				textDefinitionFile=component.getAbsolutePath();
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
}
