package org.dwfa.maven.transform;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.dwfa.maven.transform.SctIdGenerator.TYPE;

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

	public abstract void putEffectiveDate(Long sctId, String date,
			boolean update);

	public abstract String getEffectiveDate(Long sctId);

}