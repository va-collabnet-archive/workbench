package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.component.relationship.Relationship;

public class KindOfComputer {
	private static int cacheLimit = 10;
	private static ConcurrentHashMap<KindOfSpec, KindOfCache> caches = 
		new ConcurrentHashMap<KindOfSpec, KindOfCache>(10);


	public static void reset() {
		caches.clear();
	}
	/**
	 * TODO make this trim algorithm more intelligent. 
	 */
	private static void trimCache() {
		while (caches.size() >= cacheLimit) {
			Entry<KindOfSpec, KindOfCache> looser = null;
			for (Entry<KindOfSpec, KindOfCache> entry: caches.entrySet()) {
				if (looser == null) {
					looser = entry;
				} else {
					if (looser.getValue().getSize() < 10 && 
							entry.getValue().getSize() < 10) {
						if (entry.getValue().getQueryCount() < 
								looser.getValue().getQueryCount()) {
							looser = entry;
						}
					} else if (looser.getValue().getSize() < 10 || 
							entry.getValue().getSize() < 10) {
						if (entry.getValue().getSize() < 10) {
							looser = entry;
						}
					} else {
						if (entry.getValue().getLastRequestTime() < 
								looser.getValue().getLastRequestTime()) {
							looser = entry;
						}
					}
				}
			}
			caches.remove(looser.getKey());
		}
	}

	public static boolean isKindOf(Concept c, KindOfSpec spec) throws IOException {
		KindOfCache cache = caches.get(spec);
		if (cache != null && cache.tested(c.getNid())) {
			return cache.isKindOf(c.getNid());
		}
		if (caches.size() >= cacheLimit) {
			trimCache();
		}
		if (cache == null) {
			cache = new KindOfCache();
			KindOfCache prevCache = caches.putIfAbsent(spec, cache);
			if (prevCache != null) {
				cache = prevCache;
			}
			cache.setKindOf(spec.kindNid, true);
			if (c.getNid() == spec.kindNid) {
				return true;
			}
		}
		Set<Integer> possibleDestRels = c.getPossibleRelsOfTypes(spec.relTypeNids);
		if (possibleDestRels.size() == 0) {
			cache.setKindOf(c.getNid(), false);
			return false;
		}
		for (int possibleDestRelNid: possibleDestRels) {
			Concept possibleParent = Bdb.getConceptForComponent(possibleDestRelNid);
			if (cache.tested(possibleParent.getNid())) {
				if (cache.isKindOf(possibleParent.getNid())) {
					cache.setKindOf(c.getNid(), true);
					return true;
				}
			}
			Relationship possibleDestRel = 
				possibleParent.getRelationship(possibleDestRelNid);
			for (Relationship.Version v: 
					possibleDestRel.getMatches(spec)) {
				if (v.getC1Id() == spec.kindNid) {
					cache.setKindOf(c.getNid(), true);
					return true;
				}
				if (isKindOf(possibleParent, spec)) {
					cache.setKindOf(c.getNid(), true);
					return true;
				}
			}
		}
		cache.setKindOf(c.getNid(), false);
		return false;
	}

	
}
