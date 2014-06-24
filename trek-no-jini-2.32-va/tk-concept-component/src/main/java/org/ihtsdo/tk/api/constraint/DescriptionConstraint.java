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
package org.ihtsdo.tk.api.constraint;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class DescriptionConstraint represents a constraint which can be declared
 * on a description. The constraint subject-property-value is defined as:
 * enclosing concept-descriptionType-descriptionText.
 */
public class DescriptionConstraint implements ConstraintBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the description constraint object to an external source.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(conceptSpec);
        out.writeObject(descTypeSpec);
        out.writeUTF(text);
    }

    /**
     * Reads the description constraint object from an external source.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
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
    private ConceptSpec conceptSpec;
    private ConceptSpec descTypeSpec;
    private String text;

    /**
     * Instantiates a new description constraint based on the
     * <code>conceptSpec</code>,
     * <code>descriptionTypeSpec</code>, and
     * <code>text</code>.
     *
     * @param conceptSpec the concept spec representing the enclosing concept
     * @param descriptionTypeSpec the concept spec representing the description
     * type
     * @param text the text associated with the description
     */
    public DescriptionConstraint(ConceptSpec conceptSpec,
            ConceptSpec descriptionTypeSpec, String text) {
        super();
        this.conceptSpec = conceptSpec;
        this.descTypeSpec = descriptionTypeSpec;
        this.text = text;
    }

    /**
     * Gets the enclosing concept spec associated with this description
     * constraint.
     *
     * @return a concept spec representing the enclosing concept
     */
    public ConceptSpec getConceptSpec() {
        return conceptSpec;
    }

    /**
     * Gets the description type concept spec associated with this description
     * constraint.
     *
     * @return a concept spec representing the description type
     */
    public ConceptSpec getDescriptionTypeSpec() {
        return descTypeSpec;
    }

    /**
     * Gets the text associated with this description
     *
     * @return a String representing the description text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets a version of the enclosing concept associated with this description constraint.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to return
     * @return the specified concept version of the enclosing concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public ConceptVersionBI getConcept(ViewCoordinate viewCoordinate) throws IOException {
        return conceptSpec.get(viewCoordinate);
    }

    /**
     * Gets a version of the description type concept associated with this description
     * constraint.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to return
     * @return the specified concept version of the description type
     * @throws IOException signals that an I/O exception has occurred
     */
    public ConceptVersionBI getDescType(ViewCoordinate viewCoordinate) throws IOException {
        return descTypeSpec.get(viewCoordinate);
    }

    /**
     * Gets the nid of the enclosing concept associated with this description
     * constraint.
     *
     * @return the enclosing concept nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getConceptNid() throws IOException {
        return Ts.get().getNidForUuids(conceptSpec.getUuids());
    }

    /**
     * Gets the nid of the description type concept associated with this
     * description constraint.
     *
     * @return the description type nid
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getDescriptionTypeNid() throws IOException {
        return Ts.get().getNidForUuids(descTypeSpec.getUuids());
    }
}
