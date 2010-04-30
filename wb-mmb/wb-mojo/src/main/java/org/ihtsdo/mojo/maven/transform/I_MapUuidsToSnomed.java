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
package org.ihtsdo.mojo.maven.transform;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.ihtsdo.mojo.maven.transform.SctIdGenerator.TYPE;

public interface I_MapUuidsToSnomed {

    public abstract void addFixedMap(Map<UUID, Long> fixedMap);

    public abstract Map<Long, List<UUID>> getSnomedUuidListMap();

    public abstract void clear();

    public abstract boolean containsKey(Object arg0);

    public abstract boolean containsValue(Object arg0);

    public abstract Set<Entry<UUID, Long>> entrySet();

    public abstract Long get(Object key);

    public abstract Long getWithGeneration(UUID key, TYPE type);

    public abstract boolean isEmpty();

    public abstract Set<UUID> keySet();

    public abstract Long put(UUID key, Long sctId);

    public abstract void putAll(Map<? extends UUID, ? extends Long> map);

    public abstract Long remove(Object key);

    public abstract int size();

    public abstract Collection<Long> values();

    public abstract long getMaxSequence();

    public abstract void write(File f) throws IOException;

    public abstract void putEffectiveDate(Long sctId, String date, boolean update);

    public abstract String getEffectiveDate(Long sctId);

}
