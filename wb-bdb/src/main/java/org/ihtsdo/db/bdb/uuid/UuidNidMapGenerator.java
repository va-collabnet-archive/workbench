/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
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
package org.ihtsdo.db.bdb.uuid;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.ihtsdo.tk.api.ComponentChronicleBI;
import org.ihtsdo.tk.api.NidBitSetBI;

/**
 *
 * @author kec
 */
public class UuidNidMapGenerator extends UuidRetriever {

    
    ConcurrentHashMap<UUID, Integer> map;
    ConcurrentHashMap<Integer, List<UUID>> reverseMap;

    public ConcurrentHashMap<UUID, Integer> getMap() {
        return map;
    }

    public ConcurrentHashMap<Integer, List<UUID>> getReverseMap() {
        return reverseMap;
    }
    
    public UuidNidMapGenerator(NidBitSetBI concepts, int initialCapacity) {
        super(concepts);
        map = new ConcurrentHashMap(initialCapacity);
        reverseMap = new ConcurrentHashMap(initialCapacity);
    }

    @Override
    public void process(ComponentChronicleBI componentChronicle) throws Exception {
        components.incrementAndGet();
        List<UUID> results = componentChronicle.getUUIDs();
        uuids.addAndGet(results.size());
        reverseMap.put(componentChronicle.getNid(), results);
        for (UUID uuid: results) {
            map.put(uuid, componentChronicle.getNid());
        }
    }
    
}
