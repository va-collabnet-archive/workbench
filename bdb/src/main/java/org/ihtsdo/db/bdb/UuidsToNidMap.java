package org.ihtsdo.db.bdb;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.db.uuidmap.UuidToIntHashMap;
import org.ihtsdo.db.uuidmap.UuidUtil;

public class UuidsToNidMap {
	AtomicInteger sequence = new AtomicInteger(Integer.MIN_VALUE + 1);
	UuidToIntHashMap readOnlyUuidsToNidMap = new UuidToIntHashMap();
	UuidToIntHashMap mutableUuidsToNidMap = new UuidToIntHashMap();

	public int uuidToNidWithGeneration(UUID uuid) throws InterruptedException {
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

	public int uuidsToNidWithGeneration(Collection<UUID> uuids)
			throws InterruptedException {
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

	public int uuidToNid(UUID uuid) throws InterruptedException {
		if (readOnlyUuidsToNidMap.unlockedContainsKey(uuid)) {
			return readOnlyUuidsToNidMap.unlockedGet(uuid);
		}
		if (mutableUuidsToNidMap.containsKey(uuid)) {
			return mutableUuidsToNidMap.get(uuid);
		}
		return Integer.MAX_VALUE;
	}

	public int uuidsToNid(Collection<UUID> uuids)
			throws InterruptedException {
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
