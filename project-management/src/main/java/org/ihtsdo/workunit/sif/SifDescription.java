package org.ihtsdo.workunit.sif;


public class SifDescription extends SifTerminologyComponent {
	
	
	private SifIdentifier conceptId;
	private SifIdentifier typeId;
	private SifIdentifier caseSignificanceId;
	private String term;
	private String languageCode;

	public SifDescription() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the conceptId
	 */
	public SifIdentifier getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(SifIdentifier conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return the typeId
	 */
	public SifIdentifier getTypeId() {
		return typeId;
	}

	/**
	 * @param typeId the typeId to set
	 */
	public void setTypeId(SifIdentifier typeId) {
		this.typeId = typeId;
	}

	/**
	 * @return the caseSignificanceId
	 */
	public SifIdentifier getCaseSignificanceId() {
		return caseSignificanceId;
	}

	/**
	 * @param caseSignificanceId the caseSignificanceId to set
	 */
	public void setCaseSignificanceId(SifIdentifier caseSignificanceId) {
		this.caseSignificanceId = caseSignificanceId;
	}

	/**
	 * @return the term
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * @param term the term to set
	 */
	public void setTerm(String term) {
		this.term = term;
	}

	/**
	 * @return the languageCode
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * @param languageCode the languageCode to set
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}


}
