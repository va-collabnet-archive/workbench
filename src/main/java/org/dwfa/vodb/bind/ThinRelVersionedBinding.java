package org.dwfa.vodb.bind;

import org.dwfa.vodb.types.ThinRelPart;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinRelVersionedBinding extends TupleBinding {

	public ThinRelVersioned entryToObject(TupleInput ti) {
		int relId = ti.readInt();
		int c1id = ti.readInt();
		int c2id = ti.readInt();
		int size = ti.readInt();
		ThinRelVersioned versioned = new ThinRelVersioned(relId, c1id, c2id, size);
		for (int x = 0; x < size; x++) {
			ThinRelPart rel = new ThinRelPart();
			rel.setPathId(ti.readInt());
			rel.setVersion(ti.readInt());
			rel.setStatusId(ti.readInt());
			rel.setCharacteristicId(ti.readInt());
			rel.setGroup(ti.readInt());
			rel.setRefinabilityId(ti.readInt());
			rel.setRelTypeId(ti.readInt());
			versioned.addVersionNoRedundancyCheck(rel);
		}
		return versioned;
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		ThinRelVersioned versioned = (ThinRelVersioned) obj;
		to.writeInt(versioned.getRelId());
		to.writeInt(versioned.getC1Id());
		to.writeInt(versioned.getC2Id());
		to.writeInt(versioned.versionCount());
		for (ThinRelPart rel: versioned.getVersions()) {
			to.writeInt(rel.getPathId());
			to.writeInt(rel.getVersion());
			to.writeInt(rel.getStatusId());
			to.writeInt(rel.getCharacteristicId());
			to.writeInt(rel.getGroup());
			to.writeInt(rel.getRefinabilityId());
			to.writeInt(rel.getRelTypeId());
		}
	}

}
