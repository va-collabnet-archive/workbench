package org.ihtsdo.db.bdb.computer.kindof;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConceptVersion;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.KindOfCacheBI;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

public abstract class TypeCache implements I_ProcessUnfetchedConceptData, Runnable, KindOfCacheBI, Serializable {

	protected ConcurrentHashMap<Integer, int[]> typeMap;
	private List<ParallelConceptIterator> pcis;
	protected ViewCoordinate coordinate;
	private boolean ready = false;
	private boolean cancelled = false;
	private CountDownLatch latch = new CountDownLatch(1);
	protected NidSetBI types;

	@Override
	public CountDownLatch getLatch() {
		return latch;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isReady() {
		return ready;
	}

	public TypeCache() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.kindof.I_CacheKindOfRels#setup(org.ihtsdo.tk.api.Coordinate)
	 */
	@Override
	public void setup(ViewCoordinate coordinate) throws Exception {
		this.coordinate = coordinate;
		this.types = coordinate.getIsaTypeNids();
		typeMap = new ConcurrentHashMap<Integer, int[]>(Terms.get().getConceptCount());
		KindOfComputer.kindOfComputerService.execute(this);
	}
	private static int cacheCount = 1;

	@Override
	public void run() {

		int cacheNum = cacheCount++;
		AceLog.getAppLog().info("Starting cache setup: " + cacheNum + " "
				+ this.getClass().getSimpleName());
		long startTime = System.currentTimeMillis();
		try {

			Bdb.getConceptDb().iterateConceptDataInParallel(this);
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.INFO, e.getLocalizedMessage(), e);
			TypeCache.this.cancelled = true;
		}
		latch.countDown();
		ready = !cancelled;
		long elapsedTime = System.currentTimeMillis() - startTime;
		AceLog.getAppLog().info("Finished cache setup: " + cacheNum
				+ " in: " + elapsedTime);
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.computer.kindof.I_CacheKindOfRels#isKindOf(int, int)
	 */
	@Override
	public boolean isKindOf(int childNid, int parentNid) throws Exception {
		Set<String> callsStack = new HashSet<String>();
		boolean result = isKindOfNoLatch(childNid, parentNid, callsStack);
		callsStack = new HashSet<String>();
		cancelled = false;
		return result;
	}

	@Override
	public void updateCache(ConceptChronicleBI c) throws IOException, ContraditionException {
		if (c.isUncommitted()) {
			ConceptVersion cv = new ConceptVersion((Concept) c, coordinate);
			NidSet parentSet = new NidSet();
			for (RelationshipVersionBI relv : cv.getRelsOutgoingActive()) {
				if (types.contains(relv.getTypeNid())) {
					parentSet.add(relv.getDestinationNid());
				}
			}
			typeMap.put(c.getNid(), parentSet.getSetValues());
		}
	}

	public void addParents(int cNid, I_RepresentIdSet parentNidSet) {
		int[] parents = typeMap.get(cNid);
		if (parents != null) {
			for (int parentNid : parents) {
				if (!parentNidSet.isMember(parentNid)) {
					parentNidSet.setMember(parentNid);
					addParents(parentNid, parentNidSet);
				}
			}
		}
	}

	protected boolean isKindOfNoLatch(int childNid, int parentNid, Set<String> callsStack) {
		if (!cancelled) {
			String stackItem = childNid + "-" + parentNid;
			if (callsStack.contains(stackItem)) {
				cancelled = true;
				System.out.println("Cycle prevented. [TypeCache] childNid==" + childNid + " parentNid==" + parentNid);
				return false;
			}
			callsStack.add(stackItem);
			if (childNid == parentNid) {
				return true;
			}
			int[] parents = (int[]) typeMap.get(childNid);
			if (parents != null) {
				for (int pNid : parents) {
					if (isKindOfNoLatch(pNid, parentNid, callsStack)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public abstract void processUnfetchedConceptData(int cNid,
			ConceptFetcherBI fcfc) throws Exception;

	@Override
	public void setParallelConceptIterators(List<ParallelConceptIterator> pcis) {
		this.pcis = pcis;
	}

	@Override
	public boolean continueWork() {
		return true;
	}
}
