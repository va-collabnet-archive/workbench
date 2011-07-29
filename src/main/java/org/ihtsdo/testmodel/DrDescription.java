package org.ihtsdo.testmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

public class DrDescription extends DrComponent {
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

	// Inferred properties
	private String referToConceptUuid = ""; // null if has no refer to concept
											// extension
	private String caseSignificantCategory = "";
	private boolean variantGenerationCandidate = false;
	private boolean uniqueInConcept = true;

	public DrDescription() {
		identifiers = new ArrayList<DrIdentifier>();
		extensions = new ArrayList<DrRefsetExtension>();
		referToConceptUuid = null;
	}

	@Override
	public String toString() {
		StringBuffer descriptionSb = new StringBuffer("");

		try {
			descriptionSb.append("description: " + text + " (" + primordialUuid + "),");

			if (conceptUuid != null && !conceptUuid.equals("")) {
				ConceptChronicleBI concept = Ts.get().getConcept(UUID.fromString(conceptUuid));
				descriptionSb.append(" concept: " + concept + " (" + conceptUuid + "),");
			}

			descriptionSb.append(" initialCaseSignificant: " + initialCaseSignificant);
			descriptionSb.append(" lang: " + lang);
			if (typeUuid != null && !typeUuid.equals("")) {
				ConceptChronicleBI type = Ts.get().getConcept(UUID.fromString(typeUuid));
				descriptionSb.append(" type: " + type + " (" + typeUuid + "),");
			}

			if (acceptabilityUuid != null && !acceptabilityUuid.equals("")) {
				ConceptChronicleBI acceptability = Ts.get().getConcept(UUID.fromString(acceptabilityUuid));
				descriptionSb.append(" acceptability: " + acceptability + " (" + acceptabilityUuid + "),");
			}

			if (languageRefsetUuid != null && !languageRefsetUuid.equals("")) {
				ConceptChronicleBI languageRefset = Ts.get().getConcept(UUID.fromString(languageRefsetUuid));
				descriptionSb.append(" languageRefset: " + languageRefset + " (" + languageRefsetUuid + "),");
			}

			descriptionSb.append("Extensions: [");
			if (extensions != null) {
				int i = 0;
				for (DrRefsetExtension ext : extensions) {
					descriptionSb.append(ext.toString() + (i == extensions.size() - 1 ? "" : ","));
					i++;
				}
			}
			descriptionSb.append("], ");

			descriptionSb.append("Identifiers: [");
			if (identifiers != null) {
				for (DrIdentifier identifier : identifiers) {
					int i = 0;
					descriptionSb.append(identifier.toString() + (i == identifiers.size() - 1 ? "" : ","));
					i++;
				}
			}
			descriptionSb.append("]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return descriptionSb.toString();
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

}
