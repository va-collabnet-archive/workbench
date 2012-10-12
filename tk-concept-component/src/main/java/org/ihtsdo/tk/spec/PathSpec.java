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
import java.util.UUID;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.CreateOrAmendBlueprint;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 * The Class PathSpec provides a way of representing a path in a
 * verifiable and human-readable way.
 *
 */
public class PathSpec implements SpecBI {

    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    /**
     * Writes the description spec object, including the data version, path
     * concept, origin concept, and parent concept.
     *
     * @param out the output stream
     * @throws IOException signals that an I/O exception has occurred
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathConcept);
        out.writeObject(originConcept);
        out.writeObject(parentConcept);
    }

    /**
     * Reads the description spec object, including the data version, path
     * concept, origin concept, and parent concept.
     *
     * @param in the input stream
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException indicates a specified class was not found
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            pathConcept = (ConceptSpec) in.readObject();
            originConcept = (ConceptSpec) in.readObject();
            parentConcept = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    private ConceptSpec pathConcept;
    private ConceptSpec originConcept;
    private ConceptSpec parentConcept;

    /**
     * Instantiates a new path spec based on the
     * <code>pathConcept</code> and
     * <code>originConcept</code>.
     *
     * @param pathConcept the concept spec representing path concept
     * @param originConcept the concept spec representing the path origin
     * concept
     */
    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }

    /**
     * Instantiates a new path spec based on the
     * <code>pathConcept</code>,
     * <code>originConcept</code>,
     * <code>parentConcept</code>.
     *
     * @param pathConcept the concept spec representing path concept
     * @param originConcept the concept spec representing the path origin
     * concept
     * @param parentConcept the parent concept
     * @deprecated not in TK3
     */
    @Deprecated
    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept, ConceptSpec parentConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }

    /**
     * Instantiates a new path spec.
     */
    public PathSpec() {
        super();
    }

    /**
     * Tests if the database has a uuid for the path concept.
     *
     * @return <code>true</code>, if the database has a uuid for the path
     * concept
     * @deprecated not in TK3
     */
    @Deprecated
    public boolean testPathConcept() {
        for (UUID uuid : pathConcept.getUuids()) {
            if (Ts.get().hasUuid(uuid)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Makes a blue print of the path concept.
     *
     * @return the blueprint of the path concept, can be constructed to create      * a <code>ConceptChronicleBI</code>
     * @throws ValidationException if the elements of a spec cannot be found, or
     * point to different concepts/components
     * @throws IOException signals that an I/O exception has occurred
     * @throws InvalidCAB if the any of the values in blueprint to make are
     * invalid
     * @throws ContradictionException if more than one version is returned for
     * the specified view coordinate
     *
     * @see CreateOrAmendBlueprint
     */
    public ConceptCB makePathConceptBluePrint() throws ValidationException, IOException, InvalidCAB, ContradictionException {
        ConceptCB conceptBp = new ConceptCB(pathConcept.getDescription(),
                pathConcept.getDescription(),
                LANG_CODE.EN,
                TermAux.IS_A.getLenient().getPrimUuid(),
                parentConcept.getLenient().getPrimUuid());
        for (UUID uuid : pathConcept.getUuids()) {
            conceptBp.setComponentUuid(uuid);
        }
        return conceptBp;
    }

    /**
     * Gets the origin concept associated with this path spec.
     *
     * @return the concept spec representing the origin concept
     */
    public ConceptSpec getOriginConcept() {
        return originConcept;
    }

    /**
     * Sets the origin concept associated with this path spec.
     *
     * @param originConcept the concept spec representing the origin concept
     */
    public void setOriginConcept(ConceptSpec originConcept) {
        this.originConcept = originConcept;
    }

    /**
     * Gets the path concept associated with this path spec.
     *
     * @return the concept spec representing the path concept
     */
    public ConceptSpec getPathConcept() {
        return pathConcept;
    }

    /**
     * Sets the path concept associated with this path spec.
     *
     * @param pathConcept the concept spec representing the path concept
     */
    public void setPathConcept(ConceptSpec pathConcept) {
        this.pathConcept = pathConcept;
    }

    /**
     * Gets the parent concept associated with this path spec.
     *
     * @return the concept spec representing the parent concept
     * @deprecated not in TK3
     */
    @Deprecated
    public ConceptSpec getParentConcept() {
        return parentConcept;
    }

    /**
     * Sets the parent concept associated with this path spec.
     *
     * @param parentConcept the concept spec representing the parent concept
     * @deprecated not in TK3
     */
    @Deprecated
    public void setParentConcept(ConceptSpec parentConcept) {
        this.parentConcept = parentConcept;
    }

    /**
     * Returns a String representation of this path spec object. Includes the
     * path concept, origin concept, and parent concept.
     *
     * @return a string representing this path spec
     */
    @Override
    public String toString() {
        return "PathSpec[pathConcept: " + pathConcept
                + " originConcept: " + originConcept
                + " parentConcept: " + parentConcept + "]";
    }
}
