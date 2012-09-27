/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.dto.concept.component.description;

import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionAsLastRelease;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionCurrentVersion;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionPreviousVersion;

// TODO: Auto-generated Javadoc
/**
 * The Class TkTestFriendlyDescription.
 */
public class TkTestFriendlyDescription extends TkDescription implements I_DescribeForTesting,
	DescriptionCurrentVersion, DescriptionAsLastRelease, DescriptionPreviousVersion {
	
	/** The ics candidate. */
	Boolean icsCandidate;
	
	/** The spell checking required. */
	Boolean spellCheckingRequired;
	
	/** The spell variant candidate. */
	Boolean spellVariantCandidate;
	
	/** The changed component. */
	Boolean changedComponent;
	
	/** The new component. */
	Boolean newComponent;
	
	/** The published. */
	Boolean published;
	
	/** The retired. */
	Boolean retired;
	
	/**
	 * Instantiates a new tk test friendly description.
	 *
	 * @param tkDescription the tk description
	 * @param pathUUID the path uuid
	 * @param time the time
	 */
	public TkTestFriendlyDescription(TkDescription tkDescription, UUID pathUUID, Long time) {
	}

	/**
	 * Instantiates a new tk test friendly description.
	 *
	 * @param tkDescription the tk description
	 * @param tkDescriptionRevision the tk description revision
	 */
	public TkTestFriendlyDescription(TkDescription tkDescription, TkDescriptionRevision tkDescriptionRevision) {
		
		this.conceptUuid = tkDescription.conceptUuid;
		
		this.initialCaseSignificant = tkDescriptionRevision.isInitialCaseSignificant();
		this.lang = tkDescriptionRevision.getLang();
		this.text = tkDescriptionRevision.getText();
		this.typeUuid = tkDescriptionRevision.getTypeUuid();
		
		this.authorUuid = tkDescriptionRevision.getAuthorUuid();
		this.statusUuid = tkDescriptionRevision.getStatusUuid();
		this.pathUuid = tkDescriptionRevision.getPathUuid();
		this.time = tkDescriptionRevision.getTime();
		
		this.primordialUuid = tkDescription.getPrimordialComponentUuid();
		this.additionalIds = tkDescription.getAdditionalIdComponents();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#isIcsCandidate()
	 */
	public Boolean isIcsCandidate() {
		return icsCandidate;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#setIcsCandidate(java.lang.Boolean)
	 */
	public void setIcsCandidate(Boolean icsCandidate) {
		this.icsCandidate = icsCandidate;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#isSpellCheckingRequired()
	 */
	public Boolean isSpellCheckingRequired() {
		return spellCheckingRequired;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#setSpellCheckingRequired(java.lang.Boolean)
	 */
	public void setSpellCheckingRequired(Boolean spellCheckingRequired) {
		this.spellCheckingRequired = spellCheckingRequired;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#isSpellVariantCandidate()
	 */
	public Boolean isSpellVariantCandidate() {
		return spellVariantCandidate;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.dto.concept.component.description.I_DescribeForTesting#setSpellVariantCandidate(java.lang.Boolean)
	 */
	public void setSpellVariantCandidate(Boolean spellVariantCandidate) {
		this.spellVariantCandidate = spellVariantCandidate;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#isChangedComponent()
	 */
	public Boolean isChangedComponent() {
		return changedComponent;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#setChangedComponent(java.lang.Boolean)
	 */
	public void setChangedComponent(Boolean changedComponent) {
		this.changedComponent = changedComponent;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#isNewComponent()
	 */
	public Boolean isNewComponent() {
		return newComponent;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#setNewComponent(java.lang.Boolean)
	 */
	public void setNewComponent(Boolean newComponent) {
		this.newComponent = newComponent;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#isPublished()
	 */
	public Boolean isPublished() {
		return published;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#setPublished(java.lang.Boolean)
	 */
	public void setPublished(Boolean published) {
		this.published = published;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#isRetired()
	 */
	public Boolean isRetired() {
		return retired;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.tk.I_AddCommonTestingProps#setRetired(java.lang.Boolean)
	 */
	public void setRetired(Boolean retired) {
		this.retired = retired;
	}

}
