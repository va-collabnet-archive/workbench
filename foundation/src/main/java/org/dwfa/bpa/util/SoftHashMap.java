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
/*
 * Created on Mar 17, 2005
 */
package org.dwfa.bpa.util;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Adapted from an example located at:
 * http://archive.devx.com/java/free/articles/Kabutz01/Kabutz01-2.asp
 * <p>
 * Also see:
 * <p>
 * http://www.javalobby.org/java/forums/t16581
 * <p>
 * http://jakarta.apache.org/commons/collections/apidocs-COLLECTIONS_3_1/org/
 * apache/commons/collections/map/ReferenceMap.html Changes the hardCache from a
 * LikedList to a LeastRecentlyUsedCache.
 * 
 * @author kec
 * 
 */
public class SoftHashMap<K, V> extends AbstractMap<K, V> {
    /** The internal HashMap that will hold the SoftReference. */
    private Map<K, SoftValue<K, V>> hash = new HashMap<K, SoftValue<K, V>>();
    /** The number of "hard" references to hold internally. */
    private int HARD_SIZE = 1000;
    /** The FIFO list of hard references, order of last access. */
    private LinkedList<V> hardCache = new LinkedList<V>();
    /** Reference queue for cleared SoftReference objects. */
    private ReferenceQueue<V> queue = new ReferenceQueue<V>();

    public SoftHashMap() {
        this(100);
    }

    public SoftHashMap(int hardSize) {
        HARD_SIZE = hardSize;
    }

    public V get(Object key) {
        V result = null;
        // We get the SoftReference represented by that key
        SoftValue<K, V> soft_ref = hash.get(key);
        if (soft_ref != null) {
            // From the SoftReference we get the value, which can be
            // null if it was not in the map, or it was removed in
            // the processQueue() method defined below
            result = soft_ref.get();
            if (result == null) {
                // If the value has been garbage collected, remove the
                // entry from the HashMap.
                hash.remove(key);
            } else {
                // We now add this object to the beginning of the hard
                // reference queue. One reference can occur more than
                // once, because lookups of the FIFO queue are slow, so
                // we don't want to search through it each time to remove
                // duplicates.
                hardCache.addFirst(result);
                if (hardCache.size() > HARD_SIZE) {
                    // Remove the last entry if list longer than HARD_SIZE
                    try {
                        hardCache.removeLast();
                    } catch (NoSuchElementException ex) {
                        // Nothing to do...;
                    }
                }
            }
        }
        return result;
    }

    /**
     * We define our own subclass of SoftReference which contains
     * not only the value but also the key to make it easier to find
     * the entry in the HashMap after it's been garbage collected.
     */
    private static class SoftValue<SK, SV> extends SoftReference<SV> {
        private final SK key; // always make data member final

        private SoftValue(SK key, SV value, ReferenceQueue<SV> q) {
            super(value, q);
            this.key = key;
        }

        /**
         * @return Returns the key.
         */
        public SK getKey() {
            return key;
        }
    }

    /**
     * Here we go through the ReferenceQueue and remove garbage
     * collected SoftValue objects from the HashMap by looking them
     * up using the SoftValue.key data member.
     */
    @SuppressWarnings("unchecked")
    private void processQueue() {
        Reference<? extends V> r;
        while ((r = queue.poll()) != null) {
            SoftValue sv = (SoftValue) r;
            hash.remove(sv.key);
        }
    }

    /**
     * Put the key, value pair into the HashMap using
     * a SoftValue object.
     */
    public V put(K key, V value) {
        processQueue(); // throw out garbage collected values first
        return hash.put(key, new SoftValue<K, V>(key, value, queue)).get();
    }

    public V remove(Object key) {
        processQueue(); // throw out garbage collected values first
        return hash.remove(key).get();
    }

    public void clear() {
        hardCache.clear();
        processQueue(); // throw out garbage collected values
        hash.clear();
    }

    public int size() {
        processQueue(); // throw out garbage collected values first
        return hash.size();
    }

    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
