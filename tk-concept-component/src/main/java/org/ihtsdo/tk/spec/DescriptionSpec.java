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

// TODO: Auto-generated Javadoc
/**
 * The Class DescriptionSpec.
 */
public class DescriptionSpec implements SpecBI {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(descText);
        out.writeUTF(langText);
        out.writeObject(descUuids);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
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

    /** The desc uuids. */
    private UUID[] descUuids;

	/** The desc text. */
	private String descText;
	
	/** The lang text. */
	private String langText = "en";

	/** The concept spec. */
	private ConceptSpec conceptSpec;

	/**
	 * Sets the concept spec.
	 *
	 * @param conceptSpec the new concept spec
	 */
	public void setConceptSpec(ConceptSpec conceptSpec) {
		this.conceptSpec = conceptSpec;
	}

	/**
	 * Sets the description type spec.
	 *
	 * @param descriptionTypeSpec the new description type spec
	 */
	public void setDescriptionTypeSpec(ConceptSpec descriptionTypeSpec) {
		this.descTypeSpec = descriptionTypeSpec;
	}

	/** The desc type spec. */
	private ConceptSpec descTypeSpec;


	/**
	 * Instantiates a new description spec.
	 *
	 * @param descriptionUuids the description uuids
	 * @param conceptSpec the concept spec
	 * @param descriptionType the description type
	 * @param descriptionText the description text
	 */
	public DescriptionSpec(UUID[] descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
		this.descUuids = descriptionUuids;
		this.descText = descriptionText;
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionType;
	}
	

	/**
	 * Instantiates a new description spec.
	 *
	 * @param descriptionUuids the description uuids
	 * @param conceptSpec the concept spec
	 * @param descriptionType the description type
	 * @param descriptionText the description text
	 */
	public DescriptionSpec(List<UUID> descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
		UUID[] uuid = descriptionUuids.toArray(new UUID[0]);
		this.descUuids = uuid;
		this.descText = descriptionText;
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionType;
	}



	/**
	 * Gets the.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the description version bi
	 * @throws IOException Signals that an I/O exception has occurred.
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
	 * Gets the uuids.
	 *
	 * @return the uuids
	 */
	public UUID[] getUuids() {
		return descUuids;
	}

	/**
	 * Gets the description text.
	 *
	 * @return the description text
	 */
	public String getDescriptionText() {
		return descText;
	}

	/**
	 * Gets the concept spec.
	 *
	 * @return the concept spec
	 */
	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	/**
	 * Gets the description type spec.
	 *
	 * @return the description type spec
	 */
	public ConceptSpec getDescriptionTypeSpec() {
		return descTypeSpec;
	}

	/**
	 * Instantiates a new description spec.
	 *
	 * @param descriptionText the description text
	 * @param descriptionUuid the description uuid
	 * @param conceptSpec the concept spec
	 * @param descriptionType the description type
	 */
	public DescriptionSpec(String descriptionText, String descriptionUuid, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
		this(descriptionText, UUID.fromString(descriptionUuid), conceptSpec, descriptionType);
	}

	/**
	 * Instantiates a new description spec.
	 *
	 * @param descriptionText the description text
	 * @param descriptionUuid the description uuid
	 * @param conceptSpec the concept spec
	 * @param descriptionType the description type
	 */
	public DescriptionSpec(String descriptionText, UUID descriptionUuid, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
		this(new UUID[] { descriptionUuid }, conceptSpec, descriptionType, descriptionText);
	}

	/**
	 * Sets the desc text.
	 *
	 * @param extractText the new desc text
	 */
	public void setDescText(String extractText) {
		descText = extractText;
	}
	
	/**
	 * Gets the lang text.
	 *
	 * @return the lang text
	 */
	public String getLangText() {
		return langText;
	}

	/**
	 * Sets the lang text.
	 *
	 * @param langText the new lang text
	 */
	public void setLangText(String langText) {
		this.langText = langText;
	}



}
