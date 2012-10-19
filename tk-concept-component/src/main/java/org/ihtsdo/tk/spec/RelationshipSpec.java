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

import org.ihtsdo.tk.api.constraint.RelationshipConstraintIncoming;
import org.ihtsdo.tk.api.constraint.RelationshipConstraintOutgoing;

/**
 * The Class RelationshipSpec provides a way of representing a relationship in a
 * verifiable and human-readable way.
 */
public class RelationshipSpec implements SpecBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the relationship spec object, including the data version, origin,
     * relationship type, and destination.
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
     * Reads the relationship spec object, including the data version, origin,
     * relationship type, and destination.
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
     * Instantiates a new relationship spec for the relationships specified by
     * the given
     * <code>sorceSpec</code>,
     * <code>relationshipTypeSpec</code>, and
     * <code>targetSpec</code>.
     *
     * @param sourceSpec the concept spec representing the source concept of the relationship
     * @param relationshipTypeSpec the concept spec representing the relationship type
     * @param targetSpec the concept spec representing the target concept of the relationship
     */
    public RelationshipSpec(ConceptSpec sourceSpec, ConceptSpec relationshipTypeSpec, ConceptSpec targetSpec) {
        super();
        this.originSpec = sourceSpec;
        this.relTypeSpec = relationshipTypeSpec;
        this.destinationSpec = targetSpec;
    }

    /**
     * Gets the source concept associated with this relationship spec.
     *
     * @return the concept spec representing the source concept
     */
    public ConceptSpec getSourceSpec() {
        return originSpec;
    }

    /**
     * Gets the relationship type concept associated with this relationship spec.
     *
     * @return the concept spec representing the relationship type
     */
    public ConceptSpec getRelationshipTypeSpec() {
        return relTypeSpec;
    }

    /**
     * Gets the target concept associated with this relationship spec.
     *
     * @return the concept spec representing the target concept
     */
    public ConceptSpec getTargetSpec() {
        return destinationSpec;
    }

    /**
     * Gets the source relationship constraint.
     *
     * @return the source relationship constraint
     */
    public RelationshipConstraintOutgoing getSourceRelationshipConstraint() {
        return new RelationshipConstraintOutgoing(originSpec, relTypeSpec, destinationSpec);
    }

    /**
     * Gets the target relationship constraint.
     *
     * @return the target relationship constraint
     */
    public RelationshipConstraintIncoming getTargetRelationshipConstraint() {
        return new RelationshipConstraintIncoming(originSpec, relTypeSpec, destinationSpec);
    }

    /**
     * Sets the source concept associated with this relationship spec.
     *
     * @param sourceSpec the concept spec representing the source concept
     */
    public void setSourceSpec(ConceptSpec sourceSpec) {
        this.originSpec = sourceSpec;
    }

    /**
     * Sets the relationship type associated with this relationship spec.
     *
     * @param relationshipTypeSpec the concept spec representing the relationship type
     */
    public void setRelationshipTypeSpec(ConceptSpec relationshipTypeSpec) {
        this.relTypeSpec = relationshipTypeSpec;
    }

    /**
     * Sets the target concept associated with this relationship spec.
     *
     * @param targetSpec the concept spec representing the target concept
     */
    public void setTargetSpec(ConceptSpec targetSpec) {
        this.destinationSpec = targetSpec;
    }
}
