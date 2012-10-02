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
import java.util.UUID;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.ConceptCB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.binding.snomed.TermAux;

// TODO: Auto-generated Javadoc
/**
 * The Class PathSpec.
 *
 * @author kec
 */
public class PathSpec implements SpecBI {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /**
     * Write object.
     *
     * @param out the out
     * @throws IOException signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathConcept);
        out.writeObject(originConcept);
        out.writeObject(parentConcept);
    }

    /**
     * Read object.
     *
     * @param in the in
     * @throws IOException signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     * @throws ValidationException the validation exception
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException,
            ValidationException, InvalidCAB, ContradictionException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            pathConcept = (ConceptSpec) in.readObject();
            originConcept = (ConceptSpec) in.readObject();
            parentConcept = (ConceptSpec) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }
    
    /** The path concept. */
    private ConceptSpec pathConcept;

    /** The origin concept. */
    private ConceptSpec originConcept;
    
    /** The parent concept. */
    private ConceptSpec parentConcept;

    /**
     * Instantiates a new path spec.
     *
     * @param pathConcept the path concept
     * @param originConcept the origin concept
     */
    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }
    
    /**
     * Instantiates a new path spec.
     *
     * @param pathConcept the path concept
     * @param originConcept the origin concept
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
     * Test path concept.
     *
     * @return <code>true</code>, if successful
     * @deprecated not in TK3
     */
    @Deprecated
    public boolean testPathConcept(){
        for(UUID uuid: pathConcept.getUuids()){
            if(Ts.get().hasUuid(uuid)){
                return true;
            }
        }
        return false;
    }
    
    /**
     * Make path concept blue print.
     *
     * @return the concept cb
     * @throws ValidationException the validation exception
     * @throws IOException signals that an I/O exception has occurred.
     * @throws InvalidCAB the invalid cab
     * @throws ContradictionException the contradiction exception
     */
    public ConceptCB makePathConceptBluePrint() throws ValidationException, IOException, InvalidCAB, ContradictionException{
            ConceptCB conceptBp = new ConceptCB(pathConcept.getDescription(),
                    pathConcept.getDescription(),
                    LANG_CODE.EN,
                    TermAux.IS_A.getLenient().getPrimUuid(),
                    parentConcept.getLenient().getPrimUuid());
            for(UUID uuid: pathConcept.getUuids()){
                conceptBp.setComponentUuid(uuid);
            }
            return conceptBp;
    }
    
    /**
     * Gets the origin concept.
     *
     * @return the origin concept
     */
    public ConceptSpec getOriginConcept() {
        return originConcept;
    }

    /**
     * Sets the origin concept.
     *
     * @param originConcept the new origin concept
     */
    public void setOriginConcept(ConceptSpec originConcept) {
        this.originConcept = originConcept;
    }

    /**
     * Gets the path concept.
     *
     * @return the path concept
     */
    public ConceptSpec getPathConcept() {
        return pathConcept;
    }

    /**
     * Sets the path concept.
     *
     * @param pathConcept the new path concept
     */
    public void setPathConcept(ConceptSpec pathConcept) {
        this.pathConcept = pathConcept;
    }
    
    /**
     * Gets the parent concept.
     *
     * @return the parent concept
     * @deprecated not in TK3
     */
    @Deprecated
    public ConceptSpec getParentConcept() {
        return parentConcept;
    }
    
    /**
     * Sets the parent concept.
     *
     * @param parentConcept the new parent concept
     * @deprecated not in TK3
     */
    @Deprecated
    public void setParentConcept(ConceptSpec parentConcept) {
        this.parentConcept = parentConcept;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PathSpec[pathConcept: " + pathConcept +
                " originConcept: " + originConcept + 
                " parentConcept: " + parentConcept +"]";
    }

}
