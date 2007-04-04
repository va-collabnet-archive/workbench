package org.dwfa.vodb.bind;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.vodb.types.Path;
import org.dwfa.vodb.types.Position;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class PathBinder extends TupleBinding {

	@Override
	public I_Path entryToObject(TupleInput ti) {
		int conceptId = ti.readInt();
		List<I_Position> origins = readOrigins(ti);
		return new Path(conceptId, origins);
	}

	@Override
	public void objectToEntry(Object obj, TupleOutput to) {
		I_Path p = (I_Path) obj; 
		to.writeInt(p.getConceptId());
		writeOrigins(p.getOrigins(), to);

	}
	private void writeOrigins(List<I_Position> origins, TupleOutput to) {
		to.writeInt(origins.size());
		for (I_Position p: origins) {
			to.writeInt(p.getVersion());
			to.writeInt(p.getPath().getConceptId());
			int numOfOrigins = p.getPath().getOrigins().size();
			if (numOfOrigins > 0) {
				writeOrigins(p.getPath().getOrigins(), to);
			} else {
				to.writeInt(0);
			}
		}
	}
	/**
	 * 
	 * @param ti
	 * @return
	 * @todo remove data hack..
	 */
	private List<I_Position> readOrigins(TupleInput ti) {
		int numOfOrigins = ti.readInt();
		List<I_Position> origins = new ArrayList<I_Position>(numOfOrigins);
		for (int i = 0; i < numOfOrigins; i++) {
			int version = ti.readInt();
			int conceptId = ti.readInt();
			List<I_Position> originOrigins = readOrigins(ti);
			Path originPath = new Path(conceptId, originOrigins);
			Position originPosition = new Position(version, originPath);
			origins.add(originPosition);
		}
		return origins;
	}
}
