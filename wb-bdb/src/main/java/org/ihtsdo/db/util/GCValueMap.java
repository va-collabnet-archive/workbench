package org.ihtsdo.db.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.db.bdb.Bdb;

/**
 * Garbage Collectible Value Map
 * @author kec
 *
 */
public abstract class GCValueMap<T> {

	private static AtomicInteger count = new AtomicInteger();
	
    protected class RefRemover extends Thread {

		public RefRemover() {
			super("WeakRefRemover: " + count);
		}

		@Override
		public void run() {
			while (true) {
	 			try {
	 				I_GetNid ref = (I_GetNid) queue.remove();
					impl.remove(ref.getNid(), ref);
					if (Bdb.watchList.containsKey(ref.getNid())) {
						AceLog.getAppLog().info("-------- removing: " + 
								ref.getNid() + " --------");
					}
				} catch (InterruptedException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
		}
    }

    protected ReferenceQueue<T> queue = new ReferenceQueue<T>();
    
    private RefRemover remover = new RefRemover();
    
	protected ConcurrentHashMap<Integer, Reference<T>> impl;
	
	protected ReferenceType refType;
	
	public GCValueMap(int size, ReferenceType refType) {
		impl = new ConcurrentHashMap<Integer, Reference<T>>(size);
		this.refType = refType;
		remover.start();
	}
	
	public GCValueMap(ReferenceType refType) {
		impl = new ConcurrentHashMap<Integer, Reference<T>>();
		this.refType = refType;
		remover.start();
	}


	public T get(Object key) {
	    return null;
	    /*
		Reference<T> ref = impl.get(key);
		if (ref != null) {
			T c = ref.get();
			return c;
		}
		return null;
		*/
	}


	public T remove(Object key) {
		Reference<T> ref = impl.remove(key);
		T c = null;
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

	protected abstract Reference<T> makeReference(T c,
			Reference<T> newRef);
	
	public T putIfAbsent(Integer key, T c) {
	    return null;
	    /*
		Reference<T> newRef = null;
		newRef = makeReference(c, newRef);
		Reference<T> oldRef = impl.putIfAbsent(key, newRef);
		if (oldRef == null) {
			return null;
		}
		T oldConcept = newRef.get();
		if (oldConcept == null) {
			if (impl.replace(key, oldRef, newRef)) {
				return null;
			} else {
				return putIfAbsent(key, c);
			}
		}
		return oldConcept;
		*/
	}

	public Collection<Reference<T>> values() {
		return impl.values();
	}

}
