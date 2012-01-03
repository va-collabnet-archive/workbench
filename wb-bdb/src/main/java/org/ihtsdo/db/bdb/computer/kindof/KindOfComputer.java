package org.ihtsdo.db.bdb.computer.kindof;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ManageContradiction;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.ihtsdo.tk.api.coordinate.KindOfSpec;

public class KindOfComputer {

	public static boolean persistIsaCache = false;
	private static int cacheLimit = 10;
	private static ConcurrentHashMap<KindOfSpec, KindOfCache> caches =
		new ConcurrentHashMap<KindOfSpec, KindOfCache>(10);
	private static Map<IsaCoordinate, IsaCache> isaCache = new ConcurrentHashMap<IsaCoordinate, IsaCache>();
	protected static ExecutorService kindOfComputerService =
		Executors.newFixedThreadPool(1, new NamedThreadFactory(
				Bdb.dbdThreadGroup, "kind-of computer service"));

	public static void reset() {
		caches.clear();
		// Removed, IsaCache reset will be partial with the updateIsaCache method
		//        IsaCache tempCache = isaCache;
		//        isaCache = null;
		//        if (tempCache != null) {
		//            tempCache.setCancelled(true);
		//        }
	}

	/**
	 * TODO make this trim algorithm more intelligent. 
	 */
	public static void trimCache() {
		while (caches.size() >= cacheLimit) {
			Entry<KindOfSpec, KindOfCache> looser = null;
			for (Entry<KindOfSpec, KindOfCache> entry : caches.entrySet()) {
				if (looser == null) {
					looser = entry;
				} else {
					if (entry.getValue().getLastRequestTime()
							< looser.getValue().getLastRequestTime()) {
						looser = entry;
					}

				}
			}
			caches.remove(looser.getKey());
		}
	}
	static ReentrantLock lock = new ReentrantLock();

	public static boolean isKindOf(Concept c, KindOfSpec spec)
	throws IOException, TerminologyException {
		Map<IsaCoordinate, IsaCache> debugMap = isaCache;
		IsaCache debugIsaCache = debugMap.get(spec.getIsaCoordinate());
		if (isaCache.get(spec.getIsaCoordinate()) != null && isaCache.get(spec.getIsaCoordinate()).isReady()
				&& isaCache.get(spec.getIsaCoordinate()).isTested(c.getNid())) {
			return cachedIsKindOfWithDepth(c, spec, 0);
		}
		return isKindOfWithDepth(c, spec, 0);
	}

	public static void resetIsaCache(IsaCoordinate isaCoordinate) {
		if (isaCache.get(isaCoordinate) != null) {
			isaCache.get(isaCoordinate).setCancelled(true);
			isaCache.remove(isaCoordinate);
		}
	}

	public static void updateIsaCaches(ConceptChronicleBI c) throws Exception {
		for (IsaCache isac : isaCache.values()) {
			isac.updateCache(c);
		}
	}

	@Deprecated
	public static void updateIsaCachesUsingStatedView(ConceptChronicleBI c) throws Exception {
		updateIsaCaches(c);
	}

	public static void clearIsaCache() {
		isaCache.clear();
	}

	public static void setIsaCache(IsaCoordinate isaCoordinate, KindOfCacheBI newIsaCache) throws IOException {
		isaCache.put(isaCoordinate, (IsaCache) newIsaCache);
	}

	public static IsaCache setupIsaCache(IsaCoordinate isaCoordinate) throws IOException {
		if (isaCache.get(isaCoordinate) != null) {
			return isaCache.get(isaCoordinate);
		} else {
			IsaCache tempIsaCache =
				new IsaCache(Bdb.getConceptDb().getConceptNidSet());
			try {
				tempIsaCache.setup(isaCoordinate.getCoordinate());
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (Exception e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			AceLog.getAppLog().info("Saving cache reference...");
			isaCache.put(isaCoordinate, tempIsaCache);
			return tempIsaCache;
		}
	}

	public static IsaCache setupIsaCacheAndWait(IsaCoordinate isaCoordinate)
	throws IOException, InterruptedException {
		IsaCache tempIsaCache = setupIsaCache(isaCoordinate);
		tempIsaCache.getLatch().await();
		isaCache.put(isaCoordinate, tempIsaCache);
		return tempIsaCache;
	}

	public static void updateIsaCache(int cNid) throws Exception {
		I_GetConceptData concept = Terms.get().getConcept(cNid);
		boolean isClassifierEdit = false;
		for (IsaCoordinate loopCoordinate : isaCache.keySet()) {
                    if (loopCoordinate.getContradictionMgr() instanceof I_ManageContradiction) {
                        I_ManageContradiction cm = (I_ManageContradiction) loopCoordinate.getContradictionMgr();
                        if (cm.getConfig() == null) {
                            cm.setConfig(ACE.getAceConfig().getActiveConfig());
                        }
                        
                    }
                    
			List<? extends I_RelTuple> inferredRels = concept.getSourceRelTuples(null, null, 
					loopCoordinate.getViewPositionSet(), 
					loopCoordinate.getPrecedence(), 
					loopCoordinate.getContradictionMgr(), 
					loopCoordinate.getClassifierNid(), 
					RelAssertionType.INFERRED); 

			for (I_RelTuple loopRel : inferredRels) {
				if (loopRel.getTime() == Long.MAX_VALUE) {
					isClassifierEdit = true;
				}
			}
			if (isClassifierEdit) {
				isaCache.get(loopCoordinate).updateCache(Terms.get().getConcept(cNid));
			} else {
				isaCache.get(loopCoordinate).updateCacheUsingStatedView(Terms.get().getConcept(cNid));
			}
		}
	}

	protected static void updateIsaCache(IsaCoordinate isaCoordinate, int cNid) throws Exception {
		if (isaCache.get(isaCoordinate) != null && isaCache.get(isaCoordinate).isReady()) {
			isaCache.get(isaCoordinate).updateCache(Terms.get().getConcept(cNid));
		}
	}

	protected static void updateIsaCacheUsingStatedView(IsaCoordinate isaCoordinate, int cNid) throws Exception {
		if (isaCache.get(isaCoordinate) != null && isaCache.get(isaCoordinate).isReady()) {
			isaCache.get(isaCoordinate).updateCacheUsingStatedView(Terms.get().getConcept(cNid));
		}
	}

	public static void persistIsaCache() throws Exception {
		writeIsaCacheToFile(new File("berkeley-db/isa-cache.oos"));
	}

	public static void writeIsaCacheToFile(File cacheFile) throws IOException {
		//use buffering
		AceLog.getAppLog().info("writing is-a cache to file: " + cacheFile);
		cacheFile.getParentFile().mkdirs();
		OutputStream file = new FileOutputStream(cacheFile);
		OutputStream buffer = new BufferedOutputStream(file);
		ObjectOutput output = new ObjectOutputStream(buffer);
		try {
			output.writeObject(isaCache);
		} finally {
			output.close();
		}
	}

	public static boolean loadIsaCacheFromFile(File cacheFile, Collection<IsaCoordinate> isaCoordinates) throws Exception {
		if (!cacheFile.exists()) {
			AceLog.getAppLog().info("Is-a cache file does not exist: " + cacheFile);
			return false;
		}
		AceLog.getAppLog().info("Reading is-a cache from file: " + cacheFile);
		InputStream file = new FileInputStream(cacheFile);
		InputStream buffer = new BufferedInputStream(file);
		ObjectInput input = new ObjectInputStream(buffer);
		try {
			isaCache = loadIsaCacheFromStream(input);
		} finally {
			input.close();
		}
		cacheFile.delete();
		if (isaCache.keySet().containsAll(isaCoordinates) && isaCoordinates.containsAll(isaCache.keySet())) {
			return true;
		} else {
			return false;
		}
	}

	public static Map<IsaCoordinate, IsaCache> loadIsaCacheFromStream(ObjectInput ois) throws Exception {
		return (Map<IsaCoordinate, IsaCache>) ois.readObject();
	}

	private static boolean cachedIsKindOfWithDepth(
			Concept c, KindOfSpec spec, int depth)
	throws IOException {
		if (depth > 15) {
			AceLog.getAppLog().info("depth of: " + depth + " testing: " + c);
			if (depth > 100) {
				AceLog.getAppLog().alertAndLogException(
						new Exception("Depth limit of 100 exceeded: "
								+ depth + " testing: " + c));
				return false;
			}
		}

		try {
			boolean result = false;
			result = isaCache.get(spec.getIsaCoordinate()).isKindOf(c.getConceptNid(), spec.getKindNid());
			return result;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		KindOfCache cache = caches.get(spec);
		if (cache != null && cache.tested(c.getNid())) {
			return cache.isKindOf(c.getNid());
		}
		if (cache == null) {
			Concept kindOf = Bdb.getConcept(spec.kindNid);
			cache = new KindOfCache(
					kindOf.getPossibleKindOfConcepts(spec.getRelTypeNids(), null));
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
			boolean isKindOf = isaCache.get(spec.getIsaCoordinate()).isKindOf(c.getNid(), spec.kindNid);
			cache.setKindOf(c.getNid(), isKindOf);
			return isKindOf;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private static boolean isKindOfWithDepth(Concept c, KindOfSpec spec, int depth)
	throws IOException, TerminologyException {
		if (depth > 15) {
			AceLog.getAppLog().info("depth of: " + depth + " testing: " + c);
			if (depth > 100) {
				AceLog.getAppLog().alertAndLogException(
						new Exception("Depth limit of 100 exceeded: "
								+ depth + " testing: " + c));
				return false;
			}
		}
		KindOfCache cache = caches.get(spec.getIsaCoordinate());
		if (cache != null && cache.tested(c.getNid())) {
			return cache.isKindOf(c.getNid());
		}
		if (cache == null) {
			Concept kindOf = Bdb.getConcept(spec.kindNid);
			cache = new KindOfCache(
					kindOf.getPossibleKindOfConcepts(spec.getRelTypeNids(), null));
			KindOfCache prevCache = caches.putIfAbsent(spec, cache);
			if (prevCache != null) {
				cache = prevCache;
			}
			cache.setKindOf(spec.kindNid, true);
			if (c.getNid() == spec.kindNid) {
				return true;
			}
		}
		Set<I_GetConceptData> parents = c.getSourceRelTargets(spec.getAllowedStatusNids(),
				spec.getRelTypeNids(), spec.getViewPositionSet(),
				spec.getPrecedence(), spec.getContradictionMgr());
		if (parents.isEmpty()) {
			cache.setKindOf(c.getNid(), false);
			return false;
		}
		for (I_GetConceptData parent : parents) {
			if (cache.tested(parent.getNid())) {
				if (cache.isKindOf(parent.getNid())) {
					return true;
				}
			} else {
				if (isaCache.get(spec.getIsaCoordinate()) != null && isaCache.get(spec.getIsaCoordinate()).isReady()) {
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

	public static Map<IsaCoordinate, IsaCache> getIsaCacheMap() {
		return isaCache;
	}
}
