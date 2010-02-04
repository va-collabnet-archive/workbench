package org.ihtsdo.db.util;

import java.lang.ref.ReferenceQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.concept.Concept;

public class WeakValueConceptMap {
	private static AtomicInteger count = new AtomicInteger();
	
    private class WeakRefRemover extends Thread {

		public WeakRefRemover() {
			super("WeakRefRemover: " + count);
		}

		@Override
		public void run() {
			try {
				WeakConceptRef ref = (WeakConceptRef) queue.remove();
				impl.remove(ref.nid, ref);
			} catch (InterruptedException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
		}
    }

    private ReferenceQueue<Concept> queue = new ReferenceQueue<Concept>();
    private WeakRefRemover remover = new WeakRefRemover();

	private ConcurrentHashMap<Integer, WeakConceptRef> impl;
	
	public WeakValueConceptMap(int size) {
		impl = new ConcurrentHashMap<Integer, WeakConceptRef>(size);
		remover.start();
	}
	
	public WeakValueConceptMap() {
		impl = new ConcurrentHashMap<Integer, WeakConceptRef>();
		remover.start();
	}


	public Concept get(Object key) {
		WeakConceptRef ref = impl.get(key);
		if (ref != null) {
			Concept c = ref.get();
			return c;
		}
		return null;
	}


	public Concept remove(Object key) {
		WeakConceptRef ref = impl.remove(key);
		Concept c = null;
		if (ref != null) {
			c = ref.get();
		}
		return c;
	}

	public boolean isEmpty() {
		return impl.isEmpty();
	}

	public Set<Integer> keySet() {
		return impl.keySet();
	}

	public int size() {
		return impl.size();
	}
	
	public void clear() {
		impl.clear();
	}

	public boolean containsKey(Object key) {
		return impl.containsKey(key);
	}

	public Concept putIfAbsent(Integer key, Concept c) {
		WeakConceptRef newRef = new WeakConceptRef(c, queue);
		WeakConceptRef oldRef = impl.putIfAbsent(key, newRef);
		if (oldRef == null) {
			return null;
		}
		Concept oldConcept = newRef.get();
		if (oldConcept == null) {
			if (impl.replace(key, oldRef, newRef)) {
				return null;
			} else {
				return putIfAbsent(key, c);
			}
		}
		return oldConcept;
	}
}
