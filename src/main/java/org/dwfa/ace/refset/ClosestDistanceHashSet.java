package org.dwfa.ace.refset;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.tapi.TerminologyException;

public class ClosestDistanceHashSet implements Map<Integer, ConceptRefsetInclusionDetails> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	HashMap<Integer, ConceptRefsetInclusionDetails> map = new HashMap<Integer, ConceptRefsetInclusionDetails>();

	public void add(ConceptRefsetInclusionDetails o) {

		ConceptRefsetInclusionDetails oldObject = map.get(o.getConceptId());
		
		if (oldObject == null) {
			map.put(o.getConceptId(), o);
		} else {
			if (o.getDistance() < oldObject.getDistance()) {
				map.put(o.getConceptId(), o);
			}
		}
	}

	public void clear() {
		map.clear();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public Set<Entry<Integer, ConceptRefsetInclusionDetails>> entrySet() {
		return map.entrySet();
	}

	public ConceptRefsetInclusionDetails get(Object key) {
		return map.get(key);
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public Set<Integer> keySet() {
		return map.keySet();
	}

	public ConceptRefsetInclusionDetails remove(Object key) {
		return map.remove(key);
	}

	public int size() {
		return map.size();
	}

	public String toString() {
		return map.toString();
	}

	public Collection<ConceptRefsetInclusionDetails> values() {
		return map.values();
	}

	public ConceptRefsetInclusionDetails put(Integer arg0,
			ConceptRefsetInclusionDetails arg1) {
		throw new UnsupportedOperationException("This method is unsupported for this class, use the add(ConceptRefsetInclusionDetails) method instead");
	}
	

	public void putAll(
			Map<? extends Integer, ? extends ConceptRefsetInclusionDetails> arg0) {
		throw new UnsupportedOperationException("This method is unsupported for this class, use the add(ConceptRefsetInclusionDetails) method instead");
	}

	public void removeAll(ClosestDistanceHashSet newMembersToBeRemoved) {
		for (Integer key : newMembersToBeRemoved.keySet()) {
			map.remove(key);
		}		
	}
	
	
}
