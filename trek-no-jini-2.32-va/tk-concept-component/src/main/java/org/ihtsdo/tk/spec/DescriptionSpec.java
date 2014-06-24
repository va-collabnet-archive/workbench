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
package org.ihtsdo.tk.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * The Class ConceptSpec provides a way of representing a concept in a
 * verifiable and human-readable way.
 */
public class DescriptionSpec implements SpecBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the description spec object, including the data version,
     * description text, language, and description uuids.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(descText);
        out.writeUTF(langText);
        out.writeObject(descUuids);
    }

    /**
     * Reads a description spec object, including the data version, description
     * text, language, and description uuids.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            descText = in.readUTF();
            langText = in.readUTF();
            descUuids = (UUID[]) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    private UUID[] descUuids;
    private String descText;
    private String langText = "en";
    private ConceptSpec conceptSpec;

    /**
     * Sets the concept spec for the enclosing concept associated with this
     * description spec.
     *
     * @param conceptSpec the concept spec associated with this description spec
     */
    public void setConceptSpec(ConceptSpec conceptSpec) {
        this.conceptSpec = conceptSpec;
    }

    /**
     * Sets the concept spec representing the description type.
     *
     * @param descriptionTypeSpec the concept spec representing the description
     * type
     */
    public void setDescriptionTypeSpec(ConceptSpec descriptionTypeSpec) {
        this.descTypeSpec = descriptionTypeSpec;
    }
    private ConceptSpec descTypeSpec;

    /**
     * Instantiates a new description spec for the concept associated with the
     * given
     * <code>descriptionUuids</code>,
     * <code>conceptSpec</code>,
     * <code>descriptionType</code>, and
     * <code>descriptionText</code>.
     *
     * @param descriptionUuids an array of uuids associated with the description
     * to represent
     * @param conceptSpec the concept spec representing the enclosing concept
     * associated with the description to represent
     * @param descriptionType the concept spec representing the description type
     * associated with the description to represent
     * @param descriptionText the description text associated with the
     * description to represent
     */
    public DescriptionSpec(UUID[] descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
        this.descUuids = descriptionUuids;
        this.descText = descriptionText;
        this.conceptSpec = conceptSpec;
        this.descTypeSpec = descriptionType;
    }

    /**
     * Instantiates a new description spec for the concept associated with the
     * given
     * <code>descriptionUuids</code>,
     * <code>conceptSpec</code>,
     * <code>descriptionType</code>, and
     * <code>descriptionText</code>.
     *
     * @param descriptionUuids a list of uuids associated with the description
     * to represent
     * @param conceptSpec the concept spec representing the enclosing concept
     * associated with the description to represent
     * @param descriptionType the concept spec representing the description type
     * associated with the description to represent
     * @param descriptionText the description text associated with the
     * description to represent
     */
    public DescriptionSpec(List<UUID> descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
        UUID[] uuid = descriptionUuids.toArray(new UUID[0]);
        this.descUuids = uuid;
        this.descText = descriptionText;
        this.conceptSpec = conceptSpec;
        this.descTypeSpec = descriptionType;
    }

    /**
     * Gets a
     * <code>DescriptionVersionBI</code> representing the concept associated
     * with this concept spec. Ensures the enclosing concept and descriptions
     * associated with this description spec match those on the description
     * version.
     *
     * @param viewCoordinate the view coordinate specifying which version are
     * active and inactive
     * @return the description version
     * @throws IOException signals that an I/O exception has occurred
     */
    public DescriptionVersionBI get(ViewCoordinate viewCoordinate) throws IOException {
        ConceptVersionBI concept = conceptSpec.get(viewCoordinate);
        DescriptionVersionBI desc = (DescriptionVersionBI) Ts.get().getComponent(descUuids);
        if (concept.getNid() != desc.getConceptNid()) {
            throw new RuntimeException("Concept NIDs do not match. 1: "
                    + desc.getConceptNid() + " " + descText + " 2: " + concept);
        }
        if (descText.equals(desc.getText())) {
            return desc;
        } else {
            throw new RuntimeException("Descriptions do not match. 1: "
                    + descText + " 2: " + desc.getText());
        }
    }

    /**
     * Gets the uuids associated with this description spec. No validation is
     * performed.
     *
     * @return the uuids associated with this description spec
     */
    public UUID[] getUuids() {
        return descUuids;
    }

    /**
     * Gets the description text associated with this description spec.
     *
     * @return the description text associated with this description spec
     */
    public String getDescriptionText() {
        return descText;
    }

    /**
     * Gets the concept spec representing the enclosing concept associated with
     * this description spec.
     *
     * @return the concept spec representing the enclosing concept
     */
    public ConceptSpec getConceptSpec() {
        return conceptSpec;
    }

    /**
     * Gets the concept spec representing the description type associated with
     * this description spec.
     *
     * @return the concept spec representing the description type
     */
    public ConceptSpec getDescriptionTypeSpec() {
        return descTypeSpec;
    }

    /**
     * Instantiates a new description spec for the concept associated with the
     * given
     * <code>descriptionText</code>,
     * <code>descriptionUuidString</code>,
     * <code>conceptSpec</code>, ands
     * <code>descriptionType</code>.
     *
     * @param descriptionText the description text associated with the
     * description to represent
     * @param descriptionUuidString a string representing the uuid associated
     * with the description to represent
     * @param conceptSpec the concept spec representing the enclosing concept
     * associated with the description to represent
     * @param descriptionType the concept spec representing the description type
     * associated with the description to represent
     */
    public DescriptionSpec(String descriptionText, String descriptionUuidString, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
        this(descriptionText, UUID.fromString(descriptionUuidString), conceptSpec, descriptionType);
    }

    /**
     * Instantiates a new description spec for the concept associated with the
     * given
     * <code>descriptionText</code>,
     * <code>descriptionUuid</code>,
     * <code>conceptSpec</code>, ands
     * <code>descriptionType</code>.
     *
     * @param descriptionText the description text associated with the
     * description to represent
     * @param descriptionUuid the uuid associated with the description to
     * represent
     * @param conceptSpec the concept spec representing the enclosing concept
     * associated with the description to represent
     * @param descriptionType the concept spec representing the description type
     * associated with the description to represent
     */
    public DescriptionSpec(String descriptionText, UUID descriptionUuid, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
        this(new UUID[]{descriptionUuid}, conceptSpec, descriptionType, descriptionText);
    }

    /**
     * Sets the text associated with this description spec.
     *
     * @param extractText the text associated with this description
     */
    public void setDescText(String extractText) {
        descText = extractText;
    }

    /**
     * Gets the string abbreviation of the language associated with this
     * description spec.
     *
     * @return the two character string abbreviation of the language
     */
    public String getLangText() {
        return langText;
    }

    /**
     * Sets the string abbreviation of the language associated with this
     * description spec.
     *
     * @param langText the two character string abbreviation of the language
     */
    public void setLangText(String langText) {
        this.langText = langText;
    }
}
