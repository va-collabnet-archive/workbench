package org.ihtsdo.tk.dto.concept.component.description;

import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionAsLastRelease;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionCurrentVersion;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionPreviousVersion;

public class TkTestFriendlyDescription extends TkDescription implements I_DescribeForTesting,
	DescriptionCurrentVersion, DescriptionAsLastRelease, DescriptionPreviousVersion {
	
	Boolean icsCandidate;
	Boolean spellCheckingRequired;
	Boolean spellVariantCandidate;
	Boolean changedComponent;
	Boolean newComponent;
	Boolean published;
	Boolean retired;
	
	public TkTestFriendlyDescription(TkDescription description, UUID pathUUID, Long time) {
	}

	public TkTestFriendlyDescription(TkDescription description, TkDescriptionRevision revision) {
		
		this.conceptUuid = description.conceptUuid;
		
		this.initialCaseSignificant = revision.isInitialCaseSignificant();
		this.lang = revision.getLang();
		this.text = revision.getText();
		this.typeUuid = revision.getTypeUuid();
		
		this.authorUuid = revision.getAuthorUuid();
		this.statusUuid = revision.getStatusUuid();
		this.pathUuid = revision.getPathUuid();
		this.time = revision.getTime();
		
		this.primordialUuid = description.getPrimordialComponentUuid();
		this.additionalIds = description.getAdditionalIdComponents();
	}

	public Boolean isIcsCandidate() {
		return icsCandidate;
	}

	public void setIcsCandidate(Boolean icsCandidate) {
		this.icsCandidate = icsCandidate;
	}

	public Boolean isSpellCheckingRequired() {
		return spellCheckingRequired;
	}

	public void setSpellCheckingRequired(Boolean spellCheckingRequired) {
		this.spellCheckingRequired = spellCheckingRequired;
	}

	public Boolean isSpellVariantCandidate() {
		return spellVariantCandidate;
	}

	public void setSpellVariantCandidate(Boolean spellVariantCandidate) {
		this.spellVariantCandidate = spellVariantCandidate;
	}

	public Boolean isChangedComponent() {
		return changedComponent;
	}

	public void setChangedComponent(Boolean changedComponent) {
		this.changedComponent = changedComponent;
	}

	public Boolean isNewComponent() {
		return newComponent;
	}

	public void setNewComponent(Boolean newComponent) {
		this.newComponent = newComponent;
	}

	public Boolean isPublished() {
		return published;
	}

	public void setPublished(Boolean published) {
		this.published = published;
	}

	public Boolean isRetired() {
		return retired;
	}

	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

}
