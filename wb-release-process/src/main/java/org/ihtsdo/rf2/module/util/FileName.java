package org.ihtsdo.rf2.module.util;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/**
 * Title: FileName Description: Reads Properties File to know file names requires to export Core data in RF2 format Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * * @author Alejandro Rodriguez.
 *
 * @version 1.0
 */

public class FileName {

	/** The concept file name. */
	public String conceptFileName;
	
	/** The description file name. */
	public String descriptionFileName;
	
	/** The relationship file name. */
	public String relationshipFileName;
	
	/** The stated relationship file name. */
	public String statedRelationshipFileName;
	
	/** The identifier file name. */
	public String identifierFileName;
	
	/** The concept inactivation refset file name. */
	public String conceptInactivationRefsetFileName;
	
	/** The description inactivation refset file name. */
	public String descriptionInactivationRefsetFileName;
	
	/** The refinability refset file name. */
	public String refinabilityRefsetFileName;
	
	/** The attribute value refset file name. */
	public String attributeValueRefsetFileName;
	
	/** The snomed id refset file name. */
	public String snomedIdRefsetFileName;
	
	/** The ctv3 id refset file name. */
	public String ctv3IdRefsetFileName;
	
	/** The simple map refset file name. */
	public String simpleMapRefsetFileName;
	
	/** The historical refset file name. */
	public String historicalRefsetFileName;
	
	/** The language refset file name. */
	public String languageRefsetFileName;
	
	/** The textdefinition name. */
	public String textdefinitionName;
	
	/** The rf2 format. */
	public String rf2Format;
	
	/** The invoke drool rules. */
	public String invokeDroolRules;

	/** The incremental release. */
	public String incrementalRelease;
	
	/** The from release date. */
	public String fromReleaseDate;
	
	/** The to release date. */
	public String toReleaseDate;

	/** The output folder name. */
	public String outputFolderName;
	
	/** The database. */
	public String database;
	
	/** The meta hierarchy. */
	public String metaHierarchy;
	
	/** The log file name. */
	public String logFileName;

	/**
	 * Instantiates a new file name.
	 *
	 * @param props the props
	 */
	public FileName(Properties props) {
		conceptFileName = props.getProperty("concept");
		descriptionFileName = props.getProperty("description");
		relationshipFileName = props.getProperty("relationship");
		statedRelationshipFileName = props.getProperty("statedrelationship");
		identifierFileName = props.getProperty("identifier");
		textdefinitionName = props.getProperty("textdefinition");

		conceptInactivationRefsetFileName = props.getProperty("conceptInactivationRefset");
		descriptionInactivationRefsetFileName = props.getProperty("descriptionInactivationRefset");
		refinabilityRefsetFileName = props.getProperty("refinabilityRefset");
		attributeValueRefsetFileName = props.getProperty("attributeValueRefset");

		historicalRefsetFileName = props.getProperty("historicalRefset");

		snomedIdRefsetFileName = props.getProperty("snomedIdRefset");
		ctv3IdRefsetFileName = props.getProperty("ctv3IdRefset");
		simpleMapRefsetFileName = props.getProperty("simpleMapRefset");

		languageRefsetFileName = props.getProperty("languageRefset");

		rf2Format = props.getProperty("rf2Format");
		incrementalRelease = props.getProperty("incrementalRelease");
		invokeDroolRules = props.getProperty("invokeDroolRules");
		fromReleaseDate = props.getProperty("fromReleaseDate");
		toReleaseDate = props.getProperty("toReleaseDate");

		outputFolderName = props.getProperty("output");
		database = props.getProperty("database");
		metaHierarchy = props.getProperty("metaHierarchy");
		logFileName = props.getProperty("log");
	}

	/**
	 * Gets the language refset file name.
	 *
	 * @return the language refset file name
	 */
	public String getLanguageRefsetFileName() {
		return languageRefsetFileName;
	}

	/**
	 * Sets the language refset file name.
	 *
	 * @param languageRefsetFileName the new language refset file name
	 */
	public void setLanguageRefsetFileName(String languageRefsetFileName) {
		this.languageRefsetFileName = languageRefsetFileName;
	}

	/**
	 * Gets the log file name.
	 *
	 * @return the log file name
	 */
	public String getLogFileName() {
		return logFileName;
	}

	/**
	 * Sets the log file name.
	 *
	 * @param logFileName the new log file name
	 */
	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	/**
	 * Gets the ctv3 id refset file name.
	 *
	 * @return the ctv3 id refset file name
	 */
	public String getCtv3IdRefsetFileName() {
		return ctv3IdRefsetFileName;
	}

	/**
	 * Sets the ctv3 id refset file name.
	 *
	 * @param ctv3IdRefsetFileName the new ctv3 id refset file name
	 */
	public void setCtv3IdRefsetFileName(String ctv3IdRefsetFileName) {
		this.ctv3IdRefsetFileName = ctv3IdRefsetFileName;
	}

	/**
	 * Gets the simple map refset file name.
	 *
	 * @return the simple map refset file name
	 */
	public String getSimpleMapRefsetFileName() {
		return simpleMapRefsetFileName;
	}

	/**
	 * Sets the simple map refset file name.
	 *
	 * @param simpleMapRefsetFileName the new simple map refset file name
	 */
	public void setSimpleMapRefsetFileName(String simpleMapRefsetFileName) {
		this.simpleMapRefsetFileName = simpleMapRefsetFileName;
	}

	/**
	 * Gets the snomed id refset file name.
	 *
	 * @return the snomed id refset file name
	 */
	public String getSnomedIdRefsetFileName() {
		return snomedIdRefsetFileName;
	}

	/**
	 * Sets the snomed id refset file name.
	 *
	 * @param snomedIdRefsetFileName the new snomed id refset file name
	 */
	public void setSnomedIdRefsetFileName(String snomedIdRefsetFileName) {
		this.snomedIdRefsetFileName = snomedIdRefsetFileName;
	}

	/**
	 * Gets the concept file name.
	 *
	 * @return the concept file name
	 */
	public String getConceptFileName() {
		return conceptFileName;
	}

	/**
	 * Sets the concept file name.
	 *
	 * @param conceptFileName the new concept file name
	 */
	public void setConceptFileName(String conceptFileName) {
		this.conceptFileName = conceptFileName;
	}

	/**
	 * Gets the description file name.
	 *
	 * @return the description file name
	 */
	public String getDescriptionFileName() {
		return descriptionFileName;
	}

	/**
	 * Sets the description file name.
	 *
	 * @param descriptionFileName the new description file name
	 */
	public void setDescriptionFileName(String descriptionFileName) {
		this.descriptionFileName = descriptionFileName;
	}

	/**
	 * Gets the relationship file name.
	 *
	 * @return the relationship file name
	 */
	public String getRelationshipFileName() {
		return relationshipFileName;
	}

	/**
	 * Sets the relationship file name.
	 *
	 * @param relationshipFileName the new relationship file name
	 */
	public void setRelationshipFileName(String relationshipFileName) {
		this.relationshipFileName = relationshipFileName;
	}

	/**
	 * Gets the stated relationship file name.
	 *
	 * @return the stated relationship file name
	 */
	public String getStatedRelationshipFileName() {
		return statedRelationshipFileName;
	}

	/**
	 * Sets the stated relationship file name.
	 *
	 * @param statedRelationshipFileName the new stated relationship file name
	 */
	public void setStatedRelationshipFileName(String statedRelationshipFileName) {
		this.statedRelationshipFileName = statedRelationshipFileName;
	}

	/**
	 * Gets the identifier file name.
	 *
	 * @return the identifier file name
	 */
	public String getIdentifierFileName() {
		return identifierFileName;
	}

	/**
	 * Sets the identifier file name.
	 *
	 * @param identifierFileName the new identifier file name
	 */
	public void setIdentifierFileName(String identifierFileName) {
		this.identifierFileName = identifierFileName;
	}

	/**
	 * Gets the textdefinition name.
	 *
	 * @return the textdefinition name
	 */
	public String getTextdefinitionName() {
		return textdefinitionName;
	}

	/**
	 * Sets the textdefinition name.
	 *
	 * @param textdefinitionName the new textdefinition name
	 */
	public void setTextdefinitionName(String textdefinitionName) {
		this.textdefinitionName = textdefinitionName;
	}

	/**
	 * Gets the invoke drools rule.
	 *
	 * @return the invoke drools rule
	 */
	public String getInvokeDroolsRule() {
		return invokeDroolRules;
	}

	/**
	 * Sets the invoke drools rule.
	 *
	 * @param invokeDroolRules the new invoke drools rule
	 */
	public void setInvokeDroolsRule(String invokeDroolRules) {
		this.invokeDroolRules = invokeDroolRules;
	}

	/**
	 * Gets the historical refset file name.
	 *
	 * @return the historical refset file name
	 */
	public String getHistoricalRefsetFileName() {
		return historicalRefsetFileName;
	}

	/**
	 * Sets the historical refset file name.
	 *
	 * @param historicalRefsetFileName the new historical refset file name
	 */
	public void setHistoricalRefsetFileName(String historicalRefsetFileName) {
		this.historicalRefsetFileName = historicalRefsetFileName;
	}

	/**
	 * Gets the attribute value refset file name.
	 *
	 * @return the attribute value refset file name
	 */
	public String getAttributeValueRefsetFileName() {
		return attributeValueRefsetFileName;
	}

	/**
	 * Sets the attribute value refset file name.
	 *
	 * @param attributeValueRefsetFileName the new attribute value refset file name
	 */
	public void setAttributeValueRefsetFileName(String attributeValueRefsetFileName) {
		this.attributeValueRefsetFileName = attributeValueRefsetFileName;
	}

	/**
	 * Gets the concept inactivation refset file name.
	 *
	 * @return the concept inactivation refset file name
	 */
	public String getConceptInactivationRefsetFileName() {
		return conceptInactivationRefsetFileName;
	}

	/**
	 * Sets the concept inactivation refset file name.
	 *
	 * @param conceptInactivationRefset the new concept inactivation refset file name
	 */
	public void setConceptInactivationRefsetFileName(String conceptInactivationRefset) {
		this.conceptInactivationRefsetFileName = conceptInactivationRefset;
	}

	/**
	 * Gets the description inactivation refset file name.
	 *
	 * @return the description inactivation refset file name
	 */
	public String getDescriptionInactivationRefsetFileName() {
		return descriptionInactivationRefsetFileName;
	}

	/**
	 * Sets the description inactivation refset file name.
	 *
	 * @param descriptionInactivationRefsetFileName the new description inactivation refset file name
	 */
	public void setDescriptionInactivationRefsetFileName(String descriptionInactivationRefsetFileName) {
		this.descriptionInactivationRefsetFileName = descriptionInactivationRefsetFileName;
	}

	/**
	 * Gets the refinability refset file name.
	 *
	 * @return the refinability refset file name
	 */
	public String getRefinabilityRefsetFileName() {
		return refinabilityRefsetFileName;
	}

	/**
	 * Sets the refinability refset file name.
	 *
	 * @param refinabilityRefsetFileName the new refinability refset file name
	 */
	public void setRefinabilityRefsetFileName(String refinabilityRefsetFileName) {
		this.refinabilityRefsetFileName = refinabilityRefsetFileName;
	}

	/**
	 * Gets the rf2 format.
	 *
	 * @return the rf2 format
	 */
	public String getRf2Format() {
		return rf2Format;
	}

	/**
	 * Sets the rf2 format.
	 *
	 * @param rf2Format the new rf2 format
	 */
	public void setRf2Format(String rf2Format) {
		this.rf2Format = rf2Format;
	}

	/**
	 * Gets the incremental release.
	 *
	 * @return the incremental release
	 */
	public String getIncrementalRelease() {
		return incrementalRelease;
	}

	/**
	 * Sets the incremental release.
	 *
	 * @param incrementalRelease the new incremental release
	 */
	public void setIncrementalRelease(String incrementalRelease) {
		this.incrementalRelease = incrementalRelease;
	}

	/**
	 * Gets the from release date.
	 *
	 * @return the from release date
	 */
	public String getFromReleaseDate() {
		return fromReleaseDate;
	}

	/**
	 * Sets the from release date.
	 *
	 * @param fromReleaseDate the new from release date
	 */
	public void setFromReleaseDate(String fromReleaseDate) {
		this.fromReleaseDate = fromReleaseDate;
	}

	/**
	 * Gets the to release date.
	 *
	 * @return the to release date
	 */
	public String getToReleaseDate() {
		return toReleaseDate;
	}

	/**
	 * Sets the to release date.
	 *
	 * @param toReleaseDate the new to release date
	 */
	public void setToReleaseDate(String toReleaseDate) {
		this.toReleaseDate = toReleaseDate;
	}

	/**
	 * Gets the meta hierarchy.
	 *
	 * @return the meta hierarchy
	 */
	public String getMetaHierarchy() {
		return metaHierarchy;
	}

	/**
	 * Sets the meta hierarchy.
	 *
	 * @param metaHierarchy the new meta hierarchy
	 */
	public void setMetaHierarchy(String metaHierarchy) {
		this.metaHierarchy = metaHierarchy;
	}

	/**
	 * Gets the database.
	 *
	 * @return the database
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * Sets the database.
	 *
	 * @param database the new database
	 */
	public void setDatabase(String database) {
		this.database = database;
	}

	/**
	 * Gets the output folder name.
	 *
	 * @return the output folder name
	 */
	public String getOutputFolderName() {
		return outputFolderName;
	}

	/**
	 * Sets the output folder name.
	 *
	 * @param outputFoldername the new output folder name
	 */
	public void setOutputFolderName(String outputFoldername) {
		this.outputFolderName = outputFoldername;
	}

}
