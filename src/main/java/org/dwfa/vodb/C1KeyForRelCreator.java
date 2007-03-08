package org.dwfa.vodb;

import org.dwfa.vodb.bind.ThinRelVersionedBinding;
import org.dwfa.vodb.types.ThinRelVersioned;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class C1KeyForRelCreator implements SecondaryKeyCreator {
	ThinRelVersionedBinding relBinding;
	EntryBinding intBinder = TupleBinding.getPrimitiveBinding(Integer.class);
	
	public static class RelAndC1Id {
		int relId;
		int c1Id;
		public RelAndC1Id() {
			super();
		}
		
		public RelAndC1Id(int c1Id, int relId) {
			super();
			this.relId = relId;
			this.c1Id = c1Id;
		}
		public int getC1Id() {
			return c1Id;
		}
		public void setC1Id(int c1Id) {
			this.c1Id = c1Id;
		}
		public int getRelId() {
			return relId;
		}
		public void setRelId(int relId) {
			this.relId = relId;
		}
	}
	
	public static class RelAndC1IdBinding extends TupleBinding {

		public RelAndC1Id entryToObject(TupleInput ti) {
			return new RelAndC1Id(ti.readInt(),ti.readInt());
		}

		public void objectToEntry(Object obj, TupleOutput to) {
			RelAndC1Id id = (RelAndC1Id) obj;
			to.writeInt(id.getC1Id());
			to.writeInt(id.getRelId());
		}

	}
	
	RelAndC1Id relAndC1Id = new RelAndC1Id();
	RelAndC1IdBinding relAndC1IdBinding = new RelAndC1IdBinding();
	
	public C1KeyForRelCreator(ThinRelVersionedBinding binding) {
		super();
		this.relBinding = binding;
	}


	public synchronized boolean createSecondaryKey(SecondaryDatabase secDb,
            DatabaseEntry keyEntry, 
            DatabaseEntry dataEntry,
            DatabaseEntry resultEntry)
			throws DatabaseException {
		ThinRelVersioned rel = (ThinRelVersioned) relBinding.entryToObject(dataEntry);
		relAndC1Id.setC1Id(rel.getC1Id());
		relAndC1Id.setRelId(rel.getRelId());
		relAndC1IdBinding.objectToEntry(relAndC1Id, resultEntry);
		return true;
	}
	public synchronized boolean createSecondaryKey(int relId, 
            int c1id,
            DatabaseEntry resultEntry)
			throws DatabaseException {
		relAndC1Id.setC1Id(c1id);
		relAndC1Id.setRelId(relId);
		relAndC1IdBinding.objectToEntry(relAndC1Id, resultEntry);
		return true;
	}
	
}
