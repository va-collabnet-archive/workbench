/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.UUID;

/**
 * The Class Rule.
 */
public class Rule {

	/** The rule uuid. */
	private UUID ruleUuid;
	
	/** The name. */
	private String name;
	
	/** The description. */
	private String description;
	
	/** The status. */
	private int status; // create enum for this value
	
	/** The severity. */
	private Severity severity; // create enum for this value
	
	/** The package name. */
	private String packageName;
	
	/** The package url. */
	private String packageUrl;
	
	/** The dita uuid. */
	private UUID ditaUuid;
	
	/** The rule code. */
	private String ruleCode;
	
	/** The category. */
	private String category;
	
	/** The expected result. */
	private String expectedResult;
	
	/** The suggested resolution. */
	private String suggestedResolution;
	
	/** The example. */
	private String example;
	
	/** The standing issues. */
	private String standingIssues;
	
	/** The is whitelist allowed. */
	private boolean isWhitelistAllowed;
	
	/** The is whitelist reset allowed. */
	private boolean isWhitelistResetAllowed;
	
	/** The is whitelist reset when closed. */
	private boolean isWhitelistResetWhenClosed;
	
	/** The dita documentation link uuid. */
	private String ditaDocumentationLinkUuid;
	
	/** The dita generated topic uuid. */
	private String ditaGeneratedTopicUuid;
	
	/** The modified by. */
	private String modifiedBy;
	
	/** The effective time. */
	private Date effectiveTime;
	
	/** The documentation url. */
	private String documentationUrl;

	/**
	 * Instantiates a new rule.
	 */
	public Rule() {
		super();
	}

	/**
	 * Gets the documentation url.
	 *
	 * @return the documentation url
	 */
	public String getDocumentationUrl() {
		return documentationUrl;
	}

	/**
	 * Sets the documentation url.
	 *
	 * @param documentationUrl the new documentation url
	 */
	public void setDocumentationUrl(String documentationUrl) {
		this.documentationUrl = documentationUrl;
	}

	/**
	 * Gets the effective time.
	 *
	 * @return the effective time
	 */
	public Date getEffectiveTime() {
		return effectiveTime;
	}

	/**
	 * Sets the effective time.
	 *
	 * @param effectiveTime the new effective time
	 */
	public void setEffectiveTime(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	/**
	 * Gets the rule uuid.
	 *
	 * @return the rule uuid
	 */
	public UUID getRuleUuid() {
		return ruleUuid;
	}

	/**
	 * Sets the rule uuid.
	 *
	 * @param ruleUuid the new rule uuid
	 */
	public void setRuleUuid(UUID ruleUuid) {
		this.ruleUuid = ruleUuid;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the description.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Gets the severity.
	 *
	 * @return the severity
	 */
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * Sets the severity.
	 *
	 * @param severity the new severity
	 */
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	/**
	 * Gets the package name.
	 *
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * Sets the package name.
	 *
	 * @param packageName the new package name
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	/**
	 * Gets the package url.
	 *
	 * @return the package url
	 */
	public String getPackageUrl() {
		return packageUrl;
	}

	/**
	 * Sets the package url.
	 *
	 * @param packageUrl the new package url
	 */
	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	/**
	 * Gets the dita uuid.
	 *
	 * @return the dita uuid
	 */
	public UUID getDitaUuid() {
		return ditaUuid;
	}

	/**
	 * Sets the dita uuid.
	 *
	 * @param ditaUuid the new dita uuid
	 */
	public void setDitaUuid(UUID ditaUuid) {
		this.ditaUuid = ditaUuid;
	}

	/**
	 * Gets the category.
	 *
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Sets the category.
	 *
	 * @param category the new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Gets the expected result.
	 *
	 * @return the expected result
	 */
	public String getExpectedResult() {
		return expectedResult;
	}

	/**
	 * Sets the expected result.
	 *
	 * @param expectedResult the new expected result
	 */
	public void setExpectedResult(String expectedResult) {
		this.expectedResult = expectedResult;
	}

	/**
	 * Gets the suggested resolution.
	 *
	 * @return the suggested resolution
	 */
	public String getSuggestedResolution() {
		return suggestedResolution;
	}

	/**
	 * Sets the suggested resolution.
	 *
	 * @param suggestedResolution the new suggested resolution
	 */
	public void setSuggestedResolution(String suggestedResolution) {
		this.suggestedResolution = suggestedResolution;
	}

	/**
	 * Gets the example.
	 *
	 * @return the example
	 */
	public String getExample() {
		return example;
	}

	/**
	 * Sets the example.
	 *
	 * @param example the new example
	 */
	public void setExample(String example) {
		this.example = example;
	}

	/**
	 * Gets the standing issues.
	 *
	 * @return the standing issues
	 */
	public String getStandingIssues() {
		return standingIssues;
	}

	/**
	 * Sets the standing issues.
	 *
	 * @param standingIssues the new standing issues
	 */
	public void setStandingIssues(String standingIssues) {
		this.standingIssues = standingIssues;
	}

	/**
	 * Checks if is whitelist allowed.
	 *
	 * @return true, if is whitelist allowed
	 */
	public boolean isWhitelistAllowed() {
		return isWhitelistAllowed;
	}

	/**
	 * Sets the whitelist allowed.
	 *
	 * @param isWhitelistAllowed the new whitelist allowed
	 */
	public void setWhitelistAllowed(boolean isWhitelistAllowed) {
		this.isWhitelistAllowed = isWhitelistAllowed;
	}

	/**
	 * Checks if is whitelist reset allowed.
	 *
	 * @return true, if is whitelist reset allowed
	 */
	public boolean isWhitelistResetAllowed() {
		return isWhitelistResetAllowed;
	}

	/**
	 * Sets the whitelist reset allowed.
	 *
	 * @param isWhitelistResetAllowed the new whitelist reset allowed
	 */
	public void setWhitelistResetAllowed(boolean isWhitelistResetAllowed) {
		this.isWhitelistResetAllowed = isWhitelistResetAllowed;
	}

	/**
	 * Checks if is whitelist reset when closed.
	 *
	 * @return true, if is whitelist reset when closed
	 */
	public boolean isWhitelistResetWhenClosed() {
		return isWhitelistResetWhenClosed;
	}

	/**
	 * Sets the whitelist reset when closed.
	 *
	 * @param isWhitelistResetWhenClosed the new whitelist reset when closed
	 */
	public void setWhitelistResetWhenClosed(boolean isWhitelistResetWhenClosed) {
		this.isWhitelistResetWhenClosed = isWhitelistResetWhenClosed;
	}

	/**
	 * Gets the dita documentation link uuid.
	 *
	 * @return the dita documentation link uuid
	 */
	public String getDitaDocumentationLinkUuid() {
		return ditaDocumentationLinkUuid;
	}

	/**
	 * Sets the dita documentation link uuid.
	 *
	 * @param ditaDocumentationLinkUuid the new dita documentation link uuid
	 */
	public void setDitaDocumentationLinkUuid(String ditaDocumentationLinkUuid) {
		this.ditaDocumentationLinkUuid = ditaDocumentationLinkUuid;
	}

	/**
	 * Gets the dita generated topic uuid.
	 *
	 * @return the dita generated topic uuid
	 */
	public String getDitaGeneratedTopicUuid() {
		return ditaGeneratedTopicUuid;
	}

	/**
	 * Sets the dita generated topic uuid.
	 *
	 * @param ditaGeneratedTopicUuid the new dita generated topic uuid
	 */
	public void setDitaGeneratedTopicUuid(String ditaGeneratedTopicUuid) {
		this.ditaGeneratedTopicUuid = ditaGeneratedTopicUuid;
	}

	/**
	 * Gets the modified by.
	 *
	 * @return the modified by
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Sets the modified by.
	 *
	 * @param modifiedBy the new modified by
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * Gets the rule code.
	 *
	 * @return the rule code
	 */
	public String getRuleCode() {
		return ruleCode;
	}

	/**
	 * Sets the rule code.
	 *
	 * @param ruleCode the new rule code
	 */
	public void setRuleCode(String ruleCode) {
		this.ruleCode = ruleCode;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

}
