package org.ihtsdo.translation.ui.translation;

import org.ihtsdo.project.I_ContextualizeDescription;

public class TranslationTermTableReport {
	private String targetFsn = "";
	private String targetPreferred = "";
	private I_ContextualizeDescription sourceFsnConcept;
	private I_ContextualizeDescription sourcePreferedConcept;
	private Integer targetFSNRow;

	public String getTargetFsn() {
		return targetFsn;
	}

	public void setTargetFsn(String targetFsn) {
		this.targetFsn = targetFsn;
	}

	public I_ContextualizeDescription getSourceFsnConcept() {
		return sourceFsnConcept;
	}

	public void setSourceFsnConcept(I_ContextualizeDescription sourceFsnConcept) {
		this.sourceFsnConcept = sourceFsnConcept;
	}

	public String getTargetPreferred() {
		return targetPreferred;
	}

	public void setTargetPreferred(String targetPreferred) {
		this.targetPreferred = targetPreferred;
	}

	public I_ContextualizeDescription getSourcePreferedConcept() {
		return sourcePreferedConcept;
	}

	public void setSourcePreferedConcept(I_ContextualizeDescription sourcePreferedConcept) {
		this.sourcePreferedConcept = sourcePreferedConcept;
	}

	public Integer getTargetFSNRow() {
		return targetFSNRow;
	}

	public void setTargetFSNRow(Integer targetFSNRow) {
		this.targetFSNRow = targetFSNRow;
	}

}
