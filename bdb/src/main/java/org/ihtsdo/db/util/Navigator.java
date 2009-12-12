package org.ihtsdo.db.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.OpenBitSet;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_Position;
import org.dwfa.tapi.PathNotExistsException;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.PositionMapper;
import org.ihtsdo.db.bdb.concept.component.Part;

/**
 * The Navigation class can take multiple positions and determine where they are
 * in the bundle's "path space"
 * 
 * Maybe the navigator class can implement the conflict resolution policy?
 * 
 * @author kec
 * 
 */
public abstract class Navigator {

	public <T extends Part<T>> List<T> locateLatest(List<T> parts,
			I_ConfigAceFrame config) throws IOException, PathNotExistsException, TerminologyException {
		T latest = null;
		OpenBitSet resultsPartSet = new OpenBitSet(parts.size());
		for (I_Position pos : config.getViewPositionSetReadOnly()) {
			PositionMapper mapper = Bdb.getStatusAtPositionDb().getMapper(pos);
			OpenBitSet iteratorPartSet = new OpenBitSet(parts.size());
			for (int i = 0; i < parts.size(); i++) {
				T part = parts.get(i);
				if (mapper.onRoute(part)) {
					if (latest == null) {
						latest = part;
						iteratorPartSet.set(i);
					} else {
						switch (mapper.relativePosition(latest, part)) {
						case BEFORE:
							// nothing to do
							break;

						case CONFLICTING:
							iteratorPartSet.set(i);
							break;

						case AFTER:
							latest = part;
							iteratorPartSet.clear(0, Integer.MAX_VALUE);
							iteratorPartSet.set(i);
							break;

						default:
							break;
						}
					}
				}
			}
			resultsPartSet.or(iteratorPartSet);
		}
		List<T> resultsList = new ArrayList<T>((int) resultsPartSet.cardinality());
		DocIdSetIterator resultsItr = resultsPartSet.iterator();
		int id = resultsItr.nextDoc();
		while (id != DocIdSetIterator.NO_MORE_DOCS) {
			resultsList.add(parts.get(id));
			id = resultsItr.nextDoc();
		}
		return resultsList;
	}
}
