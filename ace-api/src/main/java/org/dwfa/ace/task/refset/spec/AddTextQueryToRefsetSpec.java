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
package org.dwfa.ace.task.refset.spec;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartConceptConceptString;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public abstract class AddTextQueryToRefsetSpec extends AbstractAddRefsetSpecTask {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    protected int getRefsetPartTypeId() throws IOException, TerminologyException {
        int typeId = RefsetAuxiliary.Concept.CONCEPT_CONCEPT_STRING_EXTENSION.localize().getNid();
        return typeId;
    }

    protected abstract int getStructuralQueryTokenId() throws IOException, TerminologyException;

    protected I_ThinExtByRefPart createAndPopulatePart(I_TermFactory tf, I_Path p, I_ConfigAceFrame configFrame)
            throws IOException, TerminologyException {
        I_ThinExtByRefPartConceptConceptString specPart =
                tf.newExtensionPart(I_ThinExtByRefPartConceptConceptString.class);
        if (getClauseIsTrue()) {
            specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_TRUE.localize().getNid());
        } else {
            specPart.setC1id(RefsetAuxiliary.Concept.BOOLEAN_CIRCLE_ICONS_FALSE.localize().getNid());
        }
        specPart.setC2id(getStructuralQueryTokenId());
        specPart.setStringValue("queue"); // TODO
        specPart.setPathId(p.getConceptId());
        specPart.setStatusId(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
        specPart.setVersion(Integer.MAX_VALUE);
        return specPart;
    }

}
