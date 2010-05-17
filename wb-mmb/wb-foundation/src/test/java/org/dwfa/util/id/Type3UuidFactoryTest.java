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
package org.dwfa.util.id;

import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ISA_REL;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ISA_REL_UUID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_CONCEPTID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_DESCID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_DESC_UUID;
import static org.dwfa.util.id.Type3UuidFactory.SNOMED_ROOT_UUID;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.dwfa.cement.ArchitectonicAuxiliary;

import junit.framework.TestCase;

public class Type3UuidFactoryTest extends TestCase {

    private enum TestEnum {
        TEST1(UUID.fromString("a3223f79-b208-3be7-938e-4d884c691eee")), TEST2(UUID.fromString("024e8661-e896-39e9-87ee-047048eadf84"));

        private UUID generatedId;

        private TestEnum(UUID generatedId) {
            this.generatedId = generatedId;
        }

        public UUID getGeneratedId() {
            return generatedId;
        }

    }

    public void testFromSNOMEDString() {
        UUID uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_CONCEPTID).toString());
        if (uid.equals(SNOMED_ROOT_UUID) == false) {
            fail("UUIDs not equal");
        }
        uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_DESCID).toString());
        if (uid.equals(SNOMED_ROOT_DESC_UUID) == false) {
            fail("UUIDs not equal");
        }
        uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ISA_REL).toString());
        if (uid.equals(SNOMED_ISA_REL_UUID) == false) {
            fail("UUIDs not equal");
        }
    }

    public void testFromSNOMEDLong() {
        UUID uid = Type3UuidFactory.fromSNOMED(new Long(SNOMED_ROOT_CONCEPTID));
        if (uid.equals(SNOMED_ROOT_UUID) == false) {
            fail("UUIDs not equal");
        }
    }

    public void testFromSNOMEDLong1() {
        UUID uid = Type3UuidFactory.fromSNOMED(SNOMED_ROOT_CONCEPTID);
        if (uid.equals(SNOMED_ROOT_UUID) == false) {
            fail("UUIDs not equal");
        }
    }

    public void testFromEnum() {
        for (TestEnum e : TestEnum.values()) {
            if (e.getGeneratedId().equals(Type3UuidFactory.fromEnum(e)) == false) {
                fail("UUIDs not equal");
            }
        }
    }

    public void testSNOMEDCore() {
        System.out.println(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getClass().getName());
        assertEquals(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getClass().getName(),
            "org.dwfa.cement.ArchitectonicAuxiliary$Concept");
        System.out.println(ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids().iterator().next().toString());
        assertEquals("8c230474-9f11-30ce-9cad-185a96fd03a2", ArchitectonicAuxiliary.Concept.SNOMED_CORE.getUids()
            .iterator()
            .next()
            .toString());
        System.out.println(Type3UuidFactory.fromEnum(ArchitectonicAuxiliary.Concept.SNOMED_CORE));
        assertEquals("8c230474-9f11-30ce-9cad-185a96fd03a2", Type3UuidFactory.fromEnum(
            ArchitectonicAuxiliary.Concept.SNOMED_CORE).toString());
        try {
            String name = "org.dwfa.cement.ArchitectonicAuxiliary$Concept.SNOMED_CORE";
            System.out.println(UUID.nameUUIDFromBytes(name.getBytes("8859_1")).toString());
            assertEquals("8c230474-9f11-30ce-9cad-185a96fd03a2", UUID.nameUUIDFromBytes(name.getBytes("8859_1"))
                .toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
