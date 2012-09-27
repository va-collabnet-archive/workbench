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
package org.ihtsdo.tk.uuid;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

// TODO: Auto-generated Javadoc
/**
 * The Class UuidT3Generator.
 */
public class UuidT3Generator {
    
    /** The Constant SNOMED_ROOT_CONCEPTID. */
    public static final long SNOMED_ROOT_CONCEPTID = 138875005L;
    
    /** The Constant SNOMED_ROOT_DESCID. */
    public static final long SNOMED_ROOT_DESCID = 220309016L;
    
    /** The Constant SNOMED_ISA_REL. */
    public static final long SNOMED_ISA_REL = 116680003L;

    /** The Constant SNOMED_ROOT_UUID. */
    public static final UUID SNOMED_ROOT_UUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
    
    /** The Constant SNOMED_ROOT_DESC_UUID. */
    public static final UUID SNOMED_ROOT_DESC_UUID = UUID.fromString("5fdbd08a-f7e5-311a-b9f6-3f27e6f43a14");
    
    /** The Constant SNOMED_ISA_REL_UUID. */
    public static final UUID SNOMED_ISA_REL_UUID = UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25");

    /** The Constant encoding. */
    public static final String encoding = "8859_1";

    /**
     * From snomed.
     *
     * @param id the id
     * @return the uuid
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
     * From snomed.
     *
     * @param id the id
     * @return the uuid
     */
    public static UUID fromSNOMED(long id) {
        return fromSNOMED(Long.toString(id));
    }

    /**
     * From snomed.
     *
     * @param id the id
     * @return the uuid
     */
    public static UUID fromSNOMED(Long id) {
        return fromSNOMED(id.toString());
    }

    /**
     * From enum.
     *
     * @param e the e
     * @return the uuid
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
     * For rel.
     *
     * @param srcUids the src uids
     * @param typeUids the type uids
     * @param destUids the dest uids
     * @return the collection
     */
    public static Collection<UUID> forRel(Collection<UUID> srcUids, Collection<UUID> typeUids, Collection<UUID> destUids) {
        String name = "org.dwfa." + srcUids + typeUids + destUids;
        return uuidCollectionFromName(name);
    }

    /**
     * Uuid collection from name.
     *
     * @param name the name
     * @return the collection
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
     * For desc.
     *
     * @param conceptUids the concept uids
     * @param typeUids the type uids
     * @param desc the desc
     * @return the collection
     */
    public static Collection<UUID> forDesc(Collection<UUID> conceptUids, Collection<UUID> typeUids, String desc) {
        String name = "org.dwfa." + conceptUids + typeUids + desc;
        return uuidCollectionFromName(name);
    }
}
