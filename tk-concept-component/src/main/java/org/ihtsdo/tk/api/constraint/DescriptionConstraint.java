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
package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Class DescriptionConstraint.
 */
public class DescriptionConstraint implements ConstraintBI {

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
        out.writeObject(conceptSpec);
        out.writeObject(descTypeSpec);
        out.writeUTF(text);
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
        	conceptSpec = (ConceptSpec) in.readObject();
        	descTypeSpec = (ConceptSpec) in.readObject();
        	text = in.readUTF();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /** The concept spec. */
    private ConceptSpec conceptSpec;
	
	/** The desc type spec. */
	private ConceptSpec descTypeSpec;
    
    /** The text. */
    private String text;
    
	/**
	 * Instantiates a new description constraint.
	 *
	 * @param conceptSpec the concept spec
	 * @param descriptionTypeSpec the description type spec
	 * @param text the text
	 */
	public DescriptionConstraint(ConceptSpec conceptSpec,
			ConceptSpec descriptionTypeSpec, String text) {
		super();
		this.conceptSpec = conceptSpec;
		this.descTypeSpec = descriptionTypeSpec;
		this.text = text;
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
	 * Gets the text.
	 *
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Gets the concept.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the concept
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ConceptVersionBI getConcept(ViewCoordinate viewCoordinate) throws IOException {
		return conceptSpec.get(viewCoordinate);
	}

	/**
	 * Gets the desc type.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the desc type
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ConceptVersionBI getDescType(ViewCoordinate viewCoordinate) throws IOException {
		return descTypeSpec.get(viewCoordinate);
	}


	/**
	 * Gets the concept nid.
	 *
	 * @return the concept nid
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int getConceptNid() throws IOException {
		return Ts.get().getNidForUuids(conceptSpec.getUuids());
	}

	/**
	 * Gets the description type nid.
	 *
	 * @return the description type nid
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public int getDescriptionTypeNid() throws IOException {
		return Ts.get().getNidForUuids(descTypeSpec.getUuids());
	}

    

}
