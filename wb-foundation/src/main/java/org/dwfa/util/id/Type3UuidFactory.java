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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_Manifest;

public class Type3UuidFactory {
    public static final long SNOMED_ROOT_CONCEPTID = 138875005L;
    public static final long SNOMED_ROOT_DESCID = 220309016L;
    public static final long SNOMED_ISA_REL = 116680003L;

    public static final UUID SNOMED_ROOT_UUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
    public static final UUID SNOMED_ROOT_DESC_UUID = UUID.fromString("5fdbd08a-f7e5-311a-b9f6-3f27e6f43a14");
    public static final UUID SNOMED_ISA_REL_UUID = UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");

    public static final String encoding = "8859_1";

    public static UUID fromSNOMED(String id) {
        String name = "org.snomed." + id;
        try {
            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static UUID fromSNOMED(long id) {
        return fromSNOMED(Long.toString(id));
    }

    public static UUID fromSNOMED(Long id) {
        return fromSNOMED(id.toString());
    }

    public static UUID fromEnum(Enum<?> e) {
        String name = e.getClass().getName() + "." + e.name();
        try {
            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Collection<UUID> forRel(Collection<UUID> srcUids, Collection<UUID> typeUids, Collection<UUID> destUids) {
        String name = "org.dwfa." + srcUids + typeUids + destUids;
        return uuidCollectionFromName(name);
    }

    private static Collection<UUID> uuidCollectionFromName(String name) {
        try {
            UUID uid = UUID.nameUUIDFromBytes(name.getBytes(encoding));
            Collection<UUID> list = new ArrayList<UUID>(1);
            list.add(uid);
            return list;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Collection<UUID> forDesc(Collection<UUID> conceptUids, Collection<UUID> typeUids, String desc) {
        String name = "org.dwfa." + conceptUids + typeUids + desc;
        return uuidCollectionFromName(name);
    }

    public static Collection<UUID> forExtension(I_Manifest component, I_ConceptualizeUniversally extType,
            I_ConceptualizeUniversally extIdentity) throws Exception {
        String name = "org.dwfa." + component.getUids() + extType.getUids() + extIdentity.getUids();
        return uuidCollectionFromName(name);
    }

}
