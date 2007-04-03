package org.dwfa.vodb.bind;

import org.dwfa.ace.api.I_ConceptAttributePart;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.vodb.types.ThinConPart;
import org.dwfa.vodb.types.ThinConVersioned;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class ThinConVersionedBinding extends TupleBinding {

	public ThinConVersioned entryToObject(TupleInput ti) {
		int conId = ti.readInt();
		int size = ti.readInt();
		ThinConVersioned versioned = new ThinConVersioned(conId, size);
		for (int x = 0; x < size; x++) {
			ThinConPart con = new ThinConPart();
			con.setPathId(ti.readInt());
			con.setVersion(ti.readInt());
			con.setConceptStatus(ti.readInt());
			con.setDefined(ti.readBoolean());
			versioned.addVersion(con);
		}
		return versioned;
	}

	public void objectToEntry(Object obj, TupleOutput to) {
		I_ConceptAttributeVersioned versioned = (I_ConceptAttributeVersioned) obj;
		to.writeInt(versioned.getConId());
		to.writeInt(versioned.versionCount());
		for (I_ConceptAttributePart con: versioned.getVersions()) {
			to.writeInt(con.getPathId());
			to.writeInt(con.getVersion());
			to.writeInt(con.getConceptStatus());
			to.writeBoolean(con.isDefined());
		}
	}

}
