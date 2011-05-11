package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;

public class DrDescription extends DrComponent{
	private String primordialUuid;
	
	private String text;
	private String conceptUuid;
	private boolean initialCaseSignificant;
	private String lang;
	private String typeUuid;
	private String acceptabilityUuid;
	private String languageRefsetUuid;
	
	private List<DrIdentifier> identifiers;
	private List<DrRefsetExtension> extensions;

	//Inferred properties
	private String referToConceptUuid = ""; //null if has no refer to concept extension
	private String caseSignificantCategory = "";
	private boolean variantGenerationCandidate = false;
	private boolean uniqueInConcept = true;
	private boolean uniqueInHierarchy = true;
	
	
	public DrDescription() {
		identifiers = new ArrayList<DrIdentifier>();
		extensions = new ArrayList<DrRefsetExtension>();
		referToConceptUuid=null;
	}

	public String getPrimordialUuid() {
		return primordialUuid;
	}

	public void setPrimordialUuid(String primordialUuid) {
		this.primordialUuid = primordialUuid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getConceptUuid() {
		return conceptUuid;
	}

	public void setConceptUuid(String conceptUuid) {
		this.conceptUuid = conceptUuid;
	}

	public boolean isInitialCaseSignificant() {
		return initialCaseSignificant;
	}

	public void setInitialCaseSignificant(boolean initialCaseSignificant) {
		this.initialCaseSignificant = initialCaseSignificant;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getTypeUuid() {
		return typeUuid;
	}

	public void setTypeUuid(String typeUuid) {
		this.typeUuid = typeUuid;
	}

	public List<DrIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<DrIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public String getLanguageRefsetUuid() {
		return languageRefsetUuid;
	}

	public void setLanguageRefsetUuid(String languageRefsetUuid) {
		this.languageRefsetUuid = languageRefsetUuid;
	}

	public List<DrRefsetExtension> getExtensions() {
		return extensions;
	}

	public void setExtensions(List<DrRefsetExtension> extensions) {
		this.extensions = extensions;
	}

	public String getAcceptabilityUuid() {
		return acceptabilityUuid;
	}

	public void setAcceptabilityUuid(String acceptabilityUuid) {
		this.acceptabilityUuid = acceptabilityUuid;
	}

	public String getReferToConceptUuid() {
		return referToConceptUuid;
	}

	public void setReferToConceptUuid(String referToConceptUuid) {
		this.referToConceptUuid = referToConceptUuid;
	}

	public String getCaseSignificantCategory() {
		return caseSignificantCategory;
	}

	public void setCaseSignificantCategory(String caseSignificantCategory) {
		this.caseSignificantCategory = caseSignificantCategory;
	}

	public boolean isVariantGenerationCandidate() {
		return variantGenerationCandidate;
	}

	public void setVariantGenerationCandidate(boolean variantGenerationCandidate) {
		this.variantGenerationCandidate = variantGenerationCandidate;
	}

	public boolean isUniqueInConcept() {
		return uniqueInConcept;
	}

	public void setUniqueInConcept(boolean uniqueInConcept) {
		this.uniqueInConcept = uniqueInConcept;
	}

	public boolean isUniqueInHierarchy() {
		return uniqueInHierarchy;
	}

	public void setUniqueInHierarchy(boolean uniqueInHierarchy) {
		this.uniqueInHierarchy = uniqueInHierarchy;
	}

}
