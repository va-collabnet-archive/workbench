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
package org.dwfa.tapi.impl;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.DocumentAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static junit.framework.Assert.fail;

public class LocalFixedConceptTest {

    private MemoryTermServer mts;

    @Before
    public void setup() {
        mts = new MemoryTermServer();
        LocalFixedTerminology.setStore(mts);
        mts.setGenerateIds(true);
    }

    @After
    public void tearDown() {
        LocalFixedTerminology.setStore(null);
    }

    @Test
    public void testSerlialization() throws Exception {
        ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
        aa.addToMemoryTermServer(mts);
        DocumentAuxiliary da = new DocumentAuxiliary();
        da.addToMemoryTermServer(mts);
        RefsetAuxiliary rsa = new RefsetAuxiliary();
        rsa.addToMemoryTermServer(mts);
        mts.setGenerateIds(false);
        I_ConceptualizeLocally localConcept = ArchitectonicAuxiliary.Concept.ACTIVE.localize();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(localConcept);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        I_ConceptualizeLocally readConcept = (I_ConceptualizeLocally) ois.readObject();
        if (readConcept == localConcept) {
            // System.out.println("Concepts from same server are ==");
        } else {
            fail("Concepts should be ==");
        }

    }

}
