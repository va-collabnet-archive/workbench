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

import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class RelationshipConstraintIncoming represents a constraint which can be
 * declared on a relationship. The constraint subject-property-value is defined
 * as: enclosing sourceConcept-relationshipType-targetConcept.
 */
public class RelationshipConstraintIncoming extends RelationshipConstraint {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the outgoing relationship constraint object to an external source.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    /**
     * Reads the outgoing relationship constraint object from an external
     * source.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    /**
     * Instantiates a new outgoing relationship constraint based on the
     * <code>sourceSpec</code>,
     * <code>relationshipTypeSpec</code>, and
     * <code>targetSpec</code>.
     *
     * @param sourceSpec the concept spec representing the source concept
     * @param relationshipTypeSpec the concept spec representing the
     * relationship type concept
     * @param targetSpec the concept spec representing the target concept
     */
    public RelationshipConstraintIncoming(ConceptSpec sourceSpec,
            ConceptSpec relationshipTypeSpec, ConceptSpec targetSpec) {
        super(sourceSpec, relationshipTypeSpec, targetSpec);
    }
}
