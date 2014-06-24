/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.dto.concept.component.description;

import java.util.UUID;

import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionAsLastRelease;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionCurrentVersion;
import org.ihtsdo.tk.dto.concept.component.description.marked.DescriptionPreviousVersion;

/**
 * The Class TkTestFriendlyDescription represents a
 * <code>TkDescription</code> which is associated with test properties.
 */
public class TkTestFriendlyDescription extends TkDescription implements I_DescribeForTesting,
        DescriptionCurrentVersion, DescriptionAsLastRelease, DescriptionPreviousVersion {

    Boolean icsCandidate;
    Boolean spellCheckingRequired;
    Boolean spellVariantCandidate;
    Boolean changedComponent;
    Boolean newComponent;
    Boolean published;
    Boolean retired;

    /**
     * Instantiates a new tk test friendly description based on the given
     * <code>tkDescription</code>,
     * <code>pathUUID</code>, and
     * <code>time</code>.
     *
     * @param tkDescription the description to use for testing
     * @param pathUUID the uuid representing the description path
     * @param time the long representing the description time
     */
    public TkTestFriendlyDescription(TkDescription tkDescription, UUID pathUUID, Long time) {
    }

    /**
     * Instantiates a new tk test friendly description based on the given
     * <code>tkDescription</code> and
     * <code>tkDescriptionRevision</code>.
     *
     * @param tkDescription the description to use for testing
     * @param tkDescriptionRevision the description revision to use for testing
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

    /**
     *
     * @return <code>true</code> if the description is initial case significant
     */
    public Boolean isIcsCandidate() {
        return icsCandidate;
    }

    /**
     *
     * @param icsCandidate set to <code>true</code> to indicate the description
     * is initial case significant
     */
    public void setIcsCandidate(Boolean icsCandidate) {
        this.icsCandidate = icsCandidate;
    }

    /**
     *
     * @return <code>true</code> if the description requires spell checking
     */
    public Boolean isSpellCheckingRequired() {
        return spellCheckingRequired;
    }

    /**
     *
     * @param spellCheckingRequired set to <code>true</code> to indicate the
     * description requires spell checking
     */
    public void setSpellCheckingRequired(Boolean spellCheckingRequired) {
        this.spellCheckingRequired = spellCheckingRequired;
    }

    /**
     *
     * @return <code>true</code> if the description has spelling variants
     */
    public Boolean isSpellVariantCandidate() {
        return spellVariantCandidate;
    }

    /**
     *
     * @param spellVariantCandidate set to <code>true</code> to indicate the
     * description has spelling variants
     */
    public void setSpellVariantCandidate(Boolean spellVariantCandidate) {
        this.spellVariantCandidate = spellVariantCandidate;
    }

    /**
     *
     * @return <code>true</code</code> if the component is changed
     */
    public Boolean isChangedComponent() {
        return changedComponent;
    }

    /**
     *
     * @param changedComponent set to <code>true</code> to indicate the
     * component has changed
     */
    public void setChangedComponent(Boolean changedComponent) {
        this.changedComponent = changedComponent;
    }

    /**
     *
     * @return <code>true</code</code> if the component is new
     */
    public Boolean isNewComponent() {
        return newComponent;
    }

    /**
     *
     * @param newComponent set to <code>true</code> to indicate the component is
     * new
     */
    public void setNewComponent(Boolean newComponent) {
        this.newComponent = newComponent;
    }

    /**
     *
     * @return <code>true</code> if the component is published
     */
    public Boolean isPublished() {
        return published;
    }

    /**
     *
     * @param published set to <code>true</code> to indicate the component is
     * published
     */
    public void setPublished(Boolean published) {
        this.published = published;
    }

    /**
     *
     * @return <code>true</code</code> if the component is retired
     */
    public Boolean isRetired() {
        return retired;
    }

    /**
     *
     * @param retired set to <code>true</code> to indicate the component is
     * retired
     */
    public void setRetired(Boolean retired) {
        this.retired = retired;
    }
}
