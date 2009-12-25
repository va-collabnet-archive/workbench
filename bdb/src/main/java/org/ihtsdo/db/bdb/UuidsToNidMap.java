package org.ihtsdo.db.bdb;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.cement.PrimordialId;
import org.ihtsdo.db.uuidmap.UuidToIntHashMap;
import org.ihtsdo.db.uuidmap.UuidUtil;

public class UuidsToNidMap {
	AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE + 1);
	UuidToIntHashMap readOnlyUuidsToNidMap = new UuidToIntHashMap();
	UuidToIntHashMap mutableUuidsToNidMap = new UuidToIntHashMap();

	protected UuidsToNidMap(int readOnlySize, int mutableSize) {
		super();
		readOnlyUuidsToNidMap = new UuidToIntHashMap(readOnlySize);
		mutableUuidsToNidMap = new UuidToIntHashMap(mutableSize);
		int max = Integer.MIN_VALUE;
        for (PrimordialId pid : PrimordialId.values()) {
            for (UUID uid : pid.getUids()) {
            	readOnlyUuidsToNidMap.put(UuidUtil.convert(uid), 
            			pid.getNativeId(Integer.MIN_VALUE));
             	max = Math.max(max, pid.getNativeId(Integer.MIN_VALUE));
            }
        }
        sequence = new AtomicInteger(max + 1);
	}

	public int uuidToNidWithGeneration(UUID uuid)  {
		if (readOnlyUuidsToNidMap.unlockedContainsKey(uuid)) {
			return readOnlyUuidsToNidMap.unlockedGet(uuid);
		}
		if (mutableUuidsToNidMap.containsKey(uuid)) {
			int nid = mutableUuidsToNidMap.get(uuid);
			return nid;
		}
		int newNid = sequence.getAndIncrement();
		readOnlyUuidsToNidMap.put(UuidUtil.convert(uuid), newNid);
		return newNid;
	}

	public int uuidsToNidWithGeneration(Collection<UUID> uuids) {
		for (UUID uuid : uuids) {
			if (readOnlyUuidsToNidMap.unlockedContainsKey(uuid)) {
				return readOnlyUuidsToNidMap.unlockedGet(uuid);
			}
		}
		for (UUID uuid : uuids) {
			if (mutableUuidsToNidMap.containsKey(uuid)) {
				int nid = mutableUuidsToNidMap.get(uuid);
				return nid;
			}
		}
		int newNid = sequence.getAndIncrement();
		for (UUID uuid : uuids) {
			readOnlyUuidsToNidMap.put(UuidUtil.convert(uuid), newNid);
		}
		return newNid;
	}

	public int uuidToNid(UUID uuid) {
		if (readOnlyUuidsToNidMap.unlockedContainsKey(uuid)) {
			return readOnlyUuidsToNidMap.unlockedGet(uuid);
		}
		if (mutableUuidsToNidMap.containsKey(uuid)) {
			return mutableUuidsToNidMap.get(uuid);
		}
		return Integer.MAX_VALUE;
	}

	public int uuidsToNid(Collection<UUID> uuids)  {
		for (UUID uuid : uuids) {
			if (readOnlyUuidsToNidMap.unlockedContainsKey(uuid)) {
				return readOnlyUuidsToNidMap.unlockedGet(uuid);
			}
		}
		for (UUID uuid : uuids) {
			if (mutableUuidsToNidMap.containsKey(uuid)) {
				return mutableUuidsToNidMap.get(uuid);
			}
		}
		return Integer.MAX_VALUE;
	}

}
