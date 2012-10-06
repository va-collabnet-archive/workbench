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
package org.ihtsdo.tk.api.blueprint;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.uuid.UuidT5Generator;

// TODO: Auto-generated Javadoc
/**
 * The Class DescriptionCAB.
 *
 * @author kec
 */
public class DescriptionCAB extends CreateOrAmendBlueprint {

    /** The Constant descSpecNamespace. */
    public static final UUID descSpecNamespace =
            UUID.fromString("457e4a20-5284-11e0-b8af-0800200c9a66");
    
    /** The concept uuid. */
    private UUID conceptUuid;
    
    /** The type uuid. */
    private UUID typeUuid;
    
    /** The lang. */
    public String lang;
    
    /** The text. */
    public String text;
    
    /** The initial case significant. */
    public boolean initialCaseSignificant;

    /**
     * Instantiates a new description cab.
     *
     * @param conceptNid the concept nid
     * @param typeNid the type nid
     * @param langCode the lang code
     * @param text the text
     * @param initialCaseSignificant the initial case significant
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public DescriptionCAB(
            int conceptNid, int typeNid, LANG_CODE langCode, String text, boolean initialCaseSignificant)
            throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                langCode, text, initialCaseSignificant);
    }

    /**
     * Instantiates a new description cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param langCode the lang code
     * @param text the text
     * @param initialCaseSignificant the initial case significant
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LANG_CODE langCode, String text, boolean initialCaseSignificant)
            throws IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, langCode, text, initialCaseSignificant,
                null, null, null);
    }

    /**
     * Instantiates a new description cab.
     *
     * @param conceptNid the concept nid
     * @param typeNid the type nid
     * @param langCode the lang code
     * @param text the text
     * @param initialCaseSignificant the initial case significant
     * @param descriptionVersion the description version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public DescriptionCAB(
            int conceptNid, int typeNid, LANG_CODE langCode, String text, boolean initialCaseSignificant,
            DescriptionVersionBI descriptionVersion, ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        this(Ts.get().getComponent(conceptNid).getPrimUuid(),
                Ts.get().getComponent(typeNid).getPrimUuid(),
                langCode, text, initialCaseSignificant, descriptionVersion, viewCoordinate);
    }

    /**
     * Instantiates a new description cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param langCode the lang code
     * @param text the text
     * @param initialCaseSignificant the initial case significant
     * @param descriptionVersion the description version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LANG_CODE langCode, String text, 
            boolean initialCaseSignificant, DescriptionVersionBI descriptionVersion, ViewCoordinate viewCoordinate) throws
            IOException, InvalidCAB, ContradictionException {
        this(conceptUuid, typeUuid, langCode, text, initialCaseSignificant,
                null, descriptionVersion, viewCoordinate);
    }

    /**
     * Instantiates a new description cab.
     *
     * @param conceptUuid the concept uuid
     * @param typeUuid the type uuid
     * @param langCode the lang code
     * @param text the text
     * @param initialCaseSignificant the initial case significant
     * @param componentUuid the component uuid
     * @param descriptionVersion the description version
     * @param viewCoordinate the view coordinate
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public DescriptionCAB(
            UUID conceptUuid, UUID typeUuid, LANG_CODE langCode, String text,
            boolean initialCaseSignificant, UUID componentUuid,
            DescriptionVersionBI descriptionVersion, ViewCoordinate viewCoordinate) throws IOException, InvalidCAB, ContradictionException {
        super(componentUuid, descriptionVersion, viewCoordinate);

        this.conceptUuid = conceptUuid;
        this.lang = langCode.getFormatedLanguageNoDialectCode();
        this.text = text;
        this.initialCaseSignificant = initialCaseSignificant;
        this.typeUuid = typeUuid;
        if (getComponentUuid() == null) {
            try {
                recomputeUuid();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (InvalidCAB ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint#recomputeUuid()
     */
    @Override
    public void recomputeUuid() throws NoSuchAlgorithmException, UnsupportedEncodingException,
            IOException, InvalidCAB, ContradictionException {
        setComponentUuid(UuidT5Generator.get(descSpecNamespace,
                getPrimoridalUuidString(conceptUuid)
                + typeUuid
                + lang
                + text));
        for(RefexCAB annotBp: getAnnotationBlueprints()){
            annotBp.setReferencedComponentUuid(getComponentUuid());
            annotBp.recomputeUuid();
            
        }

    }

    /**
     * Gets the type uuid.
     *
     * @return the type uuid
     */
    public UUID getTypeUuid() {
        return typeUuid;
    }

    /**
     * Gets the type nid.
     *
     * @return the type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTypeNid() throws IOException {
        return Ts.get().getNidForUuids(typeUuid);
    }

    /**
     * Gets the concept nid.
     *
     * @return the concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptUuid);
    }

    /**
     * Gets the concept uuid.
     *
     * @return the concept uuid
     */
    public UUID getConceptUuid() {
        return conceptUuid;
    }

    /**
     * Checks if is initial case significant.
     *
     * @return <code>true</code>, if is initial case significant
     */
    public boolean isInitialCaseSignificant() {
        return initialCaseSignificant;
    }

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets the concept uuid.
     *
     * @param conceptNewUuid the new concept uuid
     */
    protected void setConceptUuid(UUID conceptNewUuid){
        this.conceptUuid = conceptNewUuid;
    }
    
    /**
     * Sets the text.
     *
     * @param newText the new text
     */
    public void setText(String newText){
        this.text = newText;
    }

    /**
     * Validate.
     *
     * @param DescriptionVersion the description version
     * @return <code>true</code>, if successful
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean validate(DescriptionVersionBI DescriptionVersion) throws IOException {
        if (DescriptionVersion.getStatusNid() != getStatusNid()) {
            return false;
        }
        if (DescriptionVersion.getNid() != getComponentNid()) {
            return false;
        }
        if (DescriptionVersion.getConceptNid() != getConceptNid()) {
            return false;
        }
        if (DescriptionVersion.getTypeNid() != getTypeNid()) {
            return false;
        }
        if (!DescriptionVersion.getLang().equals(getLang())) {
            return false;
        }
        if (!DescriptionVersion.getText().equals(getText())) {
            return false;
        }
        if (DescriptionVersion.isInitialCaseSignificant() != isInitialCaseSignificant()) {
            return false;
        }
        return true;
    }
}
