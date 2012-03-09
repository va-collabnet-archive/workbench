package org.ihtsdo.rf2.util;

import java.util.Properties;

/**
 * Title: FileName Description: Reads Properties File to know file names requires to export Core data in RF2 format Copyright: Copyright (c) 2010 Company: IHTSDO
 * 
 * @author Varsha Parekh
 * @version 1.0
 */

public class FileName {

	public String conceptFileName;
	public String descriptionFileName;
	public String relationshipFileName;
	public String statedRelationshipFileName;
	public String identifierFileName;
	public String conceptInactivationRefsetFileName;
	public String descriptionInactivationRefsetFileName;
	public String refinabilityRefsetFileName;
	public String attributeValueRefsetFileName;
	public String snomedIdRefsetFileName;
	public String ctv3IdRefsetFileName;
	public String simpleMapRefsetFileName;
	public String historicalRefsetFileName;
	public String languageRefsetFileName;
	public String textdefinitionName;
	public String rf2Format;
	public String invokeDroolRules;

	public String incrementalRelease;
	public String fromReleaseDate;
	public String toReleaseDate;

	public String outputFolderName;
	public String database;
	public String metaHierarchy;
	public String logFileName;

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

	public String getLanguageRefsetFileName() {
		return languageRefsetFileName;
	}

	public void setLanguageRefsetFileName(String languageRefsetFileName) {
		this.languageRefsetFileName = languageRefsetFileName;
	}

	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}

	public String getCtv3IdRefsetFileName() {
		return ctv3IdRefsetFileName;
	}

	public void setCtv3IdRefsetFileName(String ctv3IdRefsetFileName) {
		this.ctv3IdRefsetFileName = ctv3IdRefsetFileName;
	}

	public String getSimpleMapRefsetFileName() {
		return simpleMapRefsetFileName;
	}

	public void setSimpleMapRefsetFileName(String simpleMapRefsetFileName) {
		this.simpleMapRefsetFileName = simpleMapRefsetFileName;
	}

	public String getSnomedIdRefsetFileName() {
		return snomedIdRefsetFileName;
	}

	public void setSnomedIdRefsetFileName(String snomedIdRefsetFileName) {
		this.snomedIdRefsetFileName = snomedIdRefsetFileName;
	}

	public String getConceptFileName() {
		return conceptFileName;
	}

	public void setConceptFileName(String conceptFileName) {
		this.conceptFileName = conceptFileName;
	}

	public String getDescriptionFileName() {
		return descriptionFileName;
	}

	public void setDescriptionFileName(String descriptionFileName) {
		this.descriptionFileName = descriptionFileName;
	}

	public String getRelationshipFileName() {
		return relationshipFileName;
	}

	public void setRelationshipFileName(String relationshipFileName) {
		this.relationshipFileName = relationshipFileName;
	}

	public String getStatedRelationshipFileName() {
		return statedRelationshipFileName;
	}

	public void setStatedRelationshipFileName(String statedRelationshipFileName) {
		this.statedRelationshipFileName = statedRelationshipFileName;
	}

	public String getIdentifierFileName() {
		return identifierFileName;
	}

	public void setIdentifierFileName(String identifierFileName) {
		this.identifierFileName = identifierFileName;
	}

	public String getTextdefinitionName() {
		return textdefinitionName;
	}

	public void setTextdefinitionName(String textdefinitionName) {
		this.textdefinitionName = textdefinitionName;
	}

	public String getInvokeDroolsRule() {
		return invokeDroolRules;
	}

	public void setInvokeDroolsRule(String invokeDroolRules) {
		this.invokeDroolRules = invokeDroolRules;
	}

	public String getHistoricalRefsetFileName() {
		return historicalRefsetFileName;
	}

	public void setHistoricalRefsetFileName(String historicalRefsetFileName) {
		this.historicalRefsetFileName = historicalRefsetFileName;
	}

	public String getAttributeValueRefsetFileName() {
		return attributeValueRefsetFileName;
	}

	public void setAttributeValueRefsetFileName(String attributeValueRefsetFileName) {
		this.attributeValueRefsetFileName = attributeValueRefsetFileName;
	}

	public String getConceptInactivationRefsetFileName() {
		return conceptInactivationRefsetFileName;
	}

	public void setConceptInactivationRefsetFileName(String conceptInactivationRefset) {
		this.conceptInactivationRefsetFileName = conceptInactivationRefset;
	}

	public String getDescriptionInactivationRefsetFileName() {
		return descriptionInactivationRefsetFileName;
	}

	public void setDescriptionInactivationRefsetFileName(String descriptionInactivationRefsetFileName) {
		this.descriptionInactivationRefsetFileName = descriptionInactivationRefsetFileName;
	}

	public String getRefinabilityRefsetFileName() {
		return refinabilityRefsetFileName;
	}

	public void setRefinabilityRefsetFileName(String refinabilityRefsetFileName) {
		this.refinabilityRefsetFileName = refinabilityRefsetFileName;
	}

	public String getRf2Format() {
		return rf2Format;
	}

	public void setRf2Format(String rf2Format) {
		this.rf2Format = rf2Format;
	}

	public String getIncrementalRelease() {
		return incrementalRelease;
	}

	public void setIncrementalRelease(String incrementalRelease) {
		this.incrementalRelease = incrementalRelease;
	}

	public String getFromReleaseDate() {
		return fromReleaseDate;
	}

	public void setFromReleaseDate(String fromReleaseDate) {
		this.fromReleaseDate = fromReleaseDate;
	}

	public String getToReleaseDate() {
		return toReleaseDate;
	}

	public void setToReleaseDate(String toReleaseDate) {
		this.toReleaseDate = toReleaseDate;
	}

	public String getMetaHierarchy() {
		return metaHierarchy;
	}

	public void setMetaHierarchy(String metaHierarchy) {
		this.metaHierarchy = metaHierarchy;
	}

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}

	public String getOutputFolderName() {
		return outputFolderName;
	}

	public void setOutputFolderName(String outputFoldername) {
		this.outputFolderName = outputFoldername;
	}

}
