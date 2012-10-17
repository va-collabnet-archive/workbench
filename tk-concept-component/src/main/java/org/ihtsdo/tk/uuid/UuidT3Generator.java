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
package org.ihtsdo.tk.uuid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * The Class UuidT3Generator generates a type 3 UUID object. A type 3 UUID is
 * name based and uses MD5 hashing to create the uuid from the given name. This
 * generator should only be used for SNOMED Ids, all other users should use
 * <code>UuidT5Generator</code>
 *
 * @see <a
 * href="http://en.wikipedia.org/wiki/Universally_unique_identifier">http://en.wikipedia.org/wiki/Universally_unique_identifier</a>
 */
public class UuidT3Generator {

    /**
     * The SCTID of the SNOMED root concept.
     */
    public static final long SNOMED_ROOT_CONCEPTID = 138875005L;
    /**
     * The SCTID of the SNOMED description root concept.
     */
    public static final long SNOMED_ROOT_DESCID = 220309016L;
    /**
     * The SCTID of the "is a" relationship type concept.
     */
    public static final long SNOMED_ISA_REL = 116680003L;
    /**
     * The uuid of the SNOMED root concept.
     */
    public static final UUID SNOMED_ROOT_UUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
    /**
     * The uuid of the SNOMED description root concept.
     */
    public static final UUID SNOMED_ROOT_DESC_UUID = UUID.fromString("5fdbd08a-f7e5-311a-b9f6-3f27e6f43a14");
    /**
     * The uuid of the "is a" relationship type concept.
     */
    public static final UUID SNOMED_ISA_REL_UUID = UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");
    /**
     * The the encoding string.
     */
    public static final String encoding = "8859_1";

    /**
     * Generates a type 3 UUID from the given string representing a SNOMED id.
     *
     * @param id a String representation of a SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(String id) {
        String name = "org.snomed." + id;
        try {
            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates a type 3 UUID from the given SNOMED id.
     *
     * @param id the SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(long id) {
        return fromSNOMED(Long.toString(id));
    }

    /**
     * Generates a type 3 UUID from the given SNOMED id.
     *
     * @param id the SNOMED id
     * @return the generated uuid
     */
    public static UUID fromSNOMED(Long id) {
        return fromSNOMED(id.toString());
    }

    /**
     * Generates a type 3 UUID from the given enumeration
     * <code>e</code>.
     *
     * @param e the enumeration to generate the uuid from
     * @return the generated uuid
     * @deprecated use <code>UuidT5Generator</code>
     */
    public static UUID fromEnum(Enum<?> e) {
        String name = e.getClass().getName() + "." + e.name();
        try {
            return UUID.nameUUIDFromBytes(name.getBytes(encoding));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates a type 3 UUID for a relationship based on the given source
     * relationships, relationship types, and destination relationships.
     *
     * @param srcUids the uuids representing the source relationships
     * @param typeUids the uuids representing the relationship types
     * @param destUids the uuids representing the destination relationship
     * @return a collection containing the generated uuid
     * @deprecated use <code>UuidT5Generator</code>
     */
    public static Collection<UUID> forRel(Collection<UUID> srcUids, Collection<UUID> typeUids, Collection<UUID> destUids) {
        String name = "org.dwfa." + srcUids + typeUids + destUids;
        return uuidCollectionFromName(name);
    }

    /**
     * Generates a collection containing a type 3 UUID.
     *
     * @param name the name to generate the uuid from
     * @return the collection containing the generated uuid
     * @deprecated use <code>UuidT5Generator</code>
     */
    private static Collection<UUID> uuidCollectionFromName(String name) {
        try {
            UUID uid = UUID.nameUUIDFromBytes(name.getBytes(encoding));
            Collection<UUID> list = new ArrayList<>(1);
            list.add(uid);
            return list;
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Generates a type 3 UUID for a description based on the given concepts,
     * description types, and description text.
     *
     * @param conceptUids the uuids representing the concept associated with the
     * description
     * @param typeUids the uuids representing the description types
     * @param desc the String representation of the description text
     * @return the collection containing the generated uuid
     * @deprecated use <code>UuidT5Generator</code>
     */
    public static Collection<UUID> forDesc(Collection<UUID> conceptUids, Collection<UUID> typeUids, String desc) {
        String name = "org.dwfa." + conceptUids + typeUids + desc;
        return uuidCollectionFromName(name);
    }
}
