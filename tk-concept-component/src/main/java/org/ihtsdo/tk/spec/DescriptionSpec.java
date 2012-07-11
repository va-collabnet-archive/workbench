/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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

public class DescriptionSpec implements SpecBI {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeUTF(descText);
        out.writeUTF(langText);
        out.writeObject(descUuids);
    }

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

	public void setConceptSpec(ConceptSpec conceptSpec) {
		this.conceptSpec = conceptSpec;
	}

	public void setDescriptionTypeSpec(ConceptSpec descriptionTypeSpec) {
		this.descTypeSpec = descriptionTypeSpec;
	}

	private ConceptSpec descTypeSpec;


	public DescriptionSpec(UUID[] descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
		this.descUuids = descriptionUuids;
		this.descText = descriptionText;
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionType;
	}
	

	public DescriptionSpec(List<UUID> descriptionUuids, ConceptSpec conceptSpec, ConceptSpec descriptionType, String descriptionText) {
		UUID[] uuid = descriptionUuids.toArray(new UUID[0]);
		this.descUuids = uuid;
		this.descText = descriptionText;
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionType;
	}



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
	
	public UUID[] getUuids() {
		return descUuids;
	}

	public String getDescriptionText() {
		return descText;
	}

	public ConceptSpec getConceptSpec() {
		return conceptSpec;
	}

	public ConceptSpec getDescriptionTypeSpec() {
		return descTypeSpec;
	}

	public DescriptionSpec(String descriptionText, String descriptionUuid, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
		this(descriptionText, UUID.fromString(descriptionUuid), conceptSpec, descriptionType);
	}

	public DescriptionSpec(String descriptionText, UUID descriptionUuid, ConceptSpec conceptSpec, ConceptSpec descriptionType) {
		this(new UUID[] { descriptionUuid }, conceptSpec, descriptionType, descriptionText);
	}

	public void setDescText(String extractText) {
		descText = extractText;
	}
	
	public String getLangText() {
		return langText;
	}

	public void setLangText(String langText) {
		this.langText = langText;
	}



}
