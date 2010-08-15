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

import java.util.UUID;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.Coordinate;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class DescriptionSpec {

	private UUID[] uuids;

	private String descText;

	private ConceptSpec conceptSpec;

	public DescriptionSpec(String description, String uuid, ConceptSpec concept) {
		this(description, UUID.fromString(uuid), concept);
	}

	public DescriptionSpec(String description, UUID uuid, ConceptSpec concept) {
		this(description, new UUID[] { uuid }, concept);
	}

	public DescriptionSpec(String description, UUID[] uuids, ConceptSpec concept) {
		this.uuids = uuids;
		this.descText = description;
		this.conceptSpec = concept;
	}

	public DescriptionVersionBI get(Coordinate c) {
		ConceptVersionBI concept = conceptSpec.get(c);
		DescriptionVersionBI desc = (DescriptionVersionBI) Ts.get().getComponent(uuids);
		if (concept.getNid() != desc.getConceptNid()) {
			throw new RuntimeException("Concept NIDs do not match. 1: "
					+ desc.getConceptNid() + " " + descText + " 2: " + concept);
		}
		if (descText.equals(desc.getText())) {
			return desc;
		} else {
			throw new RuntimeException("Descriptions to not match. 1: "
					+ descText + " 2: " + desc.getText());
		}
	}
}
