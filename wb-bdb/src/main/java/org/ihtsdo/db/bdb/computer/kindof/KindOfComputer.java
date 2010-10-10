package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.thread.NamedThreadFactory;

public class KindOfComputer {
	private static int cacheLimit = 10;
	private static ConcurrentHashMap<KindOfSpec, KindOfCache> caches = 
		new ConcurrentHashMap<KindOfSpec, KindOfCache>(10);
	private static IsaCache isaCache = null;
	protected static ExecutorService kindOfComputerService = Executors.newFixedThreadPool(1, 
			new NamedThreadFactory(Bdb.dbdThreadGroup, "kind-of computor service"));

	public static void reset() {
		caches.clear();
		IsaCache tempCache = isaCache;
		isaCache = null;
		if (tempCache != null) {
			tempCache.setCancelled(true);
		}
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

	static ReentrantLock lock = new ReentrantLock();
	public static boolean isKindOf(Concept c, KindOfSpec spec) throws IOException, TerminologyException {
		if (isaCache != null && isaCache.isReady()) {
			return cachedIsKindOfWithDepth(c, spec, 0);
		}
		return isKindOfWithDepth(c, spec, 0);
	}
	public static IsaCache setupIsaCache() throws IOException {
		if (isaCache == null) {
			lock.lock();
			try {
				if (isaCache == null) {
					IsaCache tempIsaCache = new IsaCache();
					tempIsaCache.setup(Terms.get().getActiveAceFrameConfig().getCoordinate());
					isaCache = tempIsaCache;
				}
			} catch (Exception e) {
				throw new IOException(e);
			} finally {
				lock.unlock();
			}
		}
		return isaCache;
	}

	public static IsaCache setupIsaCacheAndWait() throws IOException, InterruptedException {
		setupIsaCache();
		isaCache.getLatch().await();
		return isaCache;
	}

	private static boolean cachedIsKindOfWithDepth(Concept c, KindOfSpec spec, int depth) throws IOException {
		if (depth > 15) {
			AceLog.getAppLog().info("depth of: " + depth + " testing: " + c);
			if (depth > 100) {
				AceLog.getAppLog().alertAndLogException(
						new Exception("Depth limit of 100 exceeded: " + 
								depth + " testing: " + c));
				return false;
			}
		}
		KindOfCache cache = caches.get(spec);
		if (cache != null && cache.tested(c.getNid())) {
			return cache.isKindOf(c.getNid());
		}
		if (caches.size() >= cacheLimit) {
			trimCache();
		}
		if (cache == null) {
			Concept kindOf = Bdb.getConcept(spec.kindNid);
			cache = new KindOfCache(kindOf.getPossibleKindOfConcepts(spec.getRelTypeNids(), null));
			KindOfCache prevCache = caches.putIfAbsent(spec, cache);
			if (prevCache != null) {
				cache = prevCache;
			}
			cache.setKindOf(spec.kindNid, true);
			if (c.getNid() == spec.kindNid) {
				return true;
			}
		}
		try {
			boolean isKindOf = isaCache.isKindOf(c.getNid(), spec.kindNid);
			cache.setKindOf(c.getNid(), isKindOf);
			return isKindOf;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	
	
	private static boolean isKindOfWithDepth(Concept c, KindOfSpec spec, int depth) throws IOException, TerminologyException {
		if (depth > 15) {
			AceLog.getAppLog().info("depth of: " + depth + " testing: " + c);
			if (depth > 100) {
				AceLog.getAppLog().alertAndLogException(
						new Exception("Depth limit of 100 exceeded: " + 
								depth + " testing: " + c));
				return false;
			}
		}
		KindOfCache cache = caches.get(spec);
		if (cache != null && cache.tested(c.getNid())) {
			return cache.isKindOf(c.getNid());
		}
		if (caches.size() >= cacheLimit) {
			trimCache();
		}
		if (cache == null) {
			Concept kindOf = Bdb.getConcept(spec.kindNid);
			cache = new KindOfCache(kindOf.getPossibleKindOfConcepts(spec.getRelTypeNids(), null));
			KindOfCache prevCache = caches.putIfAbsent(spec, cache);
			if (prevCache != null) {
				cache = prevCache;
			}
			cache.setKindOf(spec.kindNid, true);
			if (c.getNid() == spec.kindNid) {
				return true;
			}
		}
		Set<I_GetConceptData> parents = c.getSourceRelTargets(spec.allowedStatusNids, 
				spec.relTypeNids, spec.getViewPositionSet(), spec.precedence, spec.contradictionMgr);
		if (parents.size() == 0) {
			cache.setKindOf(c.getNid(), false);
			return false;
		}
		for (I_GetConceptData parent: parents) {
			if (cache.tested(parent.getNid())) {
				if (cache.isKindOf(parent.getNid())) {
					return true;
				}
			} else {
				if (isaCache != null && isaCache.isReady()) {
					if (cachedIsKindOfWithDepth((Concept) parent, spec, depth + 1)) {
						cache.setKindOf(c.getNid(), true);
						return true;
					}
				} else {
					if (isKindOfWithDepth((Concept) parent, spec, depth + 1)) {
						cache.setKindOf(c.getNid(), true);
						return true;
					}
				}
			}
		}
		cache.setKindOf(c.getNid(), false);
		return false;
	}
	
}
