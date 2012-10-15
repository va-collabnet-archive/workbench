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
 * The Class RelationshipConstraint represents a constraint which can be
 * declared on a relationship. The constraint subject-property-value is defined
 * as: enclosing sourceConcept-relationshipType-targetConcept.
 */
public class RelationshipConstraint implements ConstraintBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the relationship constraint object to an external source.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(originSpec);
        out.writeObject(relTypeSpec);
        out.writeObject(destinationSpec);
    }

    /**
     * Reads the relationship constraint object from an external source.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            originSpec = (ConceptSpec) in.readObject();
            relTypeSpec = (ConceptSpec) in.readObject();
            destinationSpec = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }
    private ConceptSpec originSpec;
    private ConceptSpec relTypeSpec;
    private ConceptSpec destinationSpec;

    /**
     * Instantiates a new relationship constraint based on the
     * <code>sourceSpec</code>,
     * <code>relationshipTypeSpec</code>, and
     * <code>targetSpec</code>.
     *
     * @param sourceSpec the concept spec representing the source concept
     * @param relationshipTypeSpec the concept spec representing the
     * relationship type concept
     * @param targetSpec the concept spec representing the target concept
     */
    public RelationshipConstraint(ConceptSpec sourceSpec,
            ConceptSpec relationshipTypeSpec,
            ConceptSpec targetSpec) {
        super();
        this.originSpec = sourceSpec;
        this.relTypeSpec = relationshipTypeSpec;
        this.destinationSpec = targetSpec;
    }

    /**
     * Gets the source concept spec associated with this relationship
     * constraint.
     *
     * @return a concept spec representing the source concept
     */
    public ConceptSpec getSourceSpec() {
        return originSpec;
    }

    /**
     * Gets the relationship type concept spec associated with this relationship
     * constraint.
     *
     * @return a concept spec representing the relationship type concept
     */
    public ConceptSpec getRelationshipTypeSpec() {
        return relTypeSpec;
    }

    /**
     * Gets the target concept spec associated with this relationship
     * constraint.
     *
     * @return a concept spec representing the target concept
     */
    public ConceptSpec getTargetSpec() {
        return destinationSpec;
    }

    /**
     * Gets a version of the source concept associated with this relationship
     * constraint.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to return
     * @return the specified concept version of the source concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public ConceptVersionBI getSource(ViewCoordinate viewCoordinate) throws IOException {
        return originSpec.get(viewCoordinate);
    }

    /**
     * Gets a version of the relationship type concept associated with this
     * relationship constraint.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to return
     * @return the specified concept version of the relationship type
     * @throws IOException signals that an I/O exception has occurred
     */
    public ConceptVersionBI getRelationshipType(ViewCoordinate viewCoordinate) throws IOException {
        return relTypeSpec.get(viewCoordinate);
    }

    /**
     * Gets a version of the target concept associated with this relationship
     * constraint.
     *
     * @param viewCoordinate the view coordinate specifying which version of the
     * concept to return
     * @return the specified concept version of the target concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public ConceptVersionBI getTarget(ViewCoordinate viewCoordinate) throws IOException {
        return destinationSpec.get(viewCoordinate);
    }

    /**
     * Gets the nid of the source concept associated with this relationship
     * constraint.
     *
     * @return the nid of the source concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getSourceNid() throws IOException {
        return Ts.get().getNidForUuids(originSpec.getUuids());
    }

    /**
     * Gets the nid of the relationship type concept associated with this
     * relationship constraint.
     *
     * @return the nid of the relationship type concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getRelationshipTypeNid() throws IOException {
        return Ts.get().getNidForUuids(relTypeSpec.getUuids());
    }

    /**
     * Gets the nid of the target concept associated with this relationship
     * constraint.
     *
     * @return the nid of the target concept
     * @throws IOException signals that an I/O exception has occurred
     */
    public int getTargetNid() throws IOException {
        return Ts.get().getNidForUuids(destinationSpec.getUuids());
    }
}
