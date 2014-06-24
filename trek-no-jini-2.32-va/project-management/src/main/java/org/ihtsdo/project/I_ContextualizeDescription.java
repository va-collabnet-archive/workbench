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
package org.ihtsdo.project;

import java.util.Collection;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;

/**
 * The Interface I_ContextualizeDescription.
 */
public interface I_ContextualizeDescription {

    /**
     * Persist changes.
     *
     * @return true, if successful
     * @throws Exception the exception
     */
    public abstract boolean persistChanges() throws Exception;

    /**
     * Persist changes no checks.
     *
     * @return true, if successful
     * @throws Exception the exception
     */
    public abstract boolean persistChangesNoChecks() throws Exception;

    /**
     * Contextualize this description.
     *
     * @param newLanguageRefsetId the new language refset id
     * @param acceptabilityId the acceptability id
     * @return the i_ contextualize description
     * @throws Exception the exception
     */
    public abstract I_ContextualizeDescription contextualizeThisDescription(
            int newLanguageRefsetId, int acceptabilityId) throws Exception;

    /**
     * Gets the desc id.
     *
     * @return the desc id
     */
    public abstract int getDescId();

    /**
     * Gets the concept id.
     *
     * @return the concept id
     */
    public abstract int getConceptId();

    /**
     * Gets the type id.
     *
     * @return the type id
     */
    public abstract int getTypeId();

    /**
     * Sets the type id.
     *
     * @param typeId the new type id
     */
    public abstract void setTypeId(int typeId);

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    public abstract String getLang();

    /**
     * Sets the lang.
     *
     * @param lang the new lang
     */
    public abstract void setLang(String lang);

    /**
     * Gets the text.
     *
     * @return the text
     */
    public abstract String getText();

    /**
     * Sets the text.
     *
     * @param text the new text
     */
    public abstract void setText(String text);

    /**
     * Gets the acceptability id.
     *
     * @return the acceptability id
     */
    public abstract int getAcceptabilityId();

    /**
     * Sets the acceptability id.
     *
     * @param acceptabilityId the new acceptability id
     */
    public abstract void setAcceptabilityId(int acceptabilityId);

    /**
     * Gets the language refset id.
     *
     * @return the language refset id
     */
    public abstract int getLanguageRefsetId();

    /**
     * Gets the description versioned.
     *
     * @return the description versioned
     */
    public abstract I_DescriptionVersioned getDescriptionVersioned();

    /**
     * Gets the description part.
     *
     * @return the description part
     */
    public abstract I_DescriptionPart getDescriptionPart();

    /**
     * Gets the language extension.
     *
     * @return the language extension
     */
    public abstract I_ExtendByRef getLanguageExtension();

    /**
     * Gets the language extension part.
     *
     * @return the language extension part
     */
    public abstract I_ExtendByRefPartCid getLanguageExtensionPart();

    /**
     * Gets the concept.
     *
     * @return the concept
     */
    public abstract I_GetConceptData getConcept();

    /**
     * Gets the extension status id.
     *
     * @return the extension status id
     */
    public abstract int getExtensionStatusId();

    /**
     * Sets the extension status id.
     *
     * @param extensionStatusId the new extension status id
     */
    public abstract void setExtensionStatusId(int extensionStatusId);

    /**
     * Gets the uuids.
     *
     * @return the uuids
     */
    public abstract Collection<UUID> getUuids();

    /**
     * Gets the description status id.
     *
     * @return the description status id
     */
    public abstract int getDescriptionStatusId();

    /**
     * Sets the description status id.
     *
     * @param descriptionStatusId the new description status id
     */
    public abstract void setDescriptionStatusId(int descriptionStatusId);

    /**
     * Sets the initial case significant.
     *
     * @param isInitialCaseSignificant the new initial case significant
     */
    public abstract void setInitialCaseSignificant(
            boolean isInitialCaseSignificant);

    /**
     * Checks if is initial case significant.
     *
     * @return true, if is initial case significant
     */
    public abstract boolean isInitialCaseSignificant();

    /**
     * To string.
     *
     * @return the string
     */
    public abstract String toString();
}