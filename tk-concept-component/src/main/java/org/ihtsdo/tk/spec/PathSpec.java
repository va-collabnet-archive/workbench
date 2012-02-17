/*
 * Copyright 2010 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

/**
 *
 * @author kec
 */
public class PathSpec implements SpecBI {
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(pathConcept);
        out.writeObject(originConcept);
        out.writeObject(parentConcept);
    }

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
    
    private ConceptSpec pathConcept;

    private ConceptSpec originConcept;
    
    private ConceptSpec parentConcept;

    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }
    
    public PathSpec(ConceptSpec pathConcept, ConceptSpec originConcept, ConceptSpec parentConcept) {
        this.pathConcept = pathConcept;
        this.originConcept = originConcept;
    }

    public PathSpec() {
        super();
    }
 
    public boolean testPathConcept(UUID[] uuids){
        for(UUID uuid: uuids){
            if(Ts.get().hasUuid(uuid)){
                return true;
            }
        }
        return false;
    }
    
    public ConceptCB makePathConceptBluePrint() throws ValidationException, IOException, InvalidCAB, ContradictionException{
            ConceptCB conceptBp = new ConceptCB(pathConcept.getDescription(),
                    pathConcept.getDescription(),
                    LANG_CODE.EN,
                    TermAux.IS_A.getLenient().getPrimUuid(),
                    parentConcept.getLenient().getPrimUuid());
            return conceptBp;
    }
    
    public ConceptSpec getOriginConcept() {
        return originConcept;
    }

    public void setOriginConcept(ConceptSpec originConcept) {
        this.originConcept = originConcept;
    }

    public ConceptSpec getPathConcept() {
        return pathConcept;
    }

    public void setPathConcept(ConceptSpec pathConcept) {
        this.pathConcept = pathConcept;
    }
    
     public ConceptSpec getParentConcept() {
        return parentConcept;
    }

    public void setParentConcept(ConceptSpec parentConcept) {
        this.parentConcept = parentConcept;
    }

    @Override
    public String toString() {
        return "PathSpec[pathConcept: " + pathConcept +
                " originConcept: " + originConcept + 
                " parentConcept: " + parentConcept +"]";
    }

}
