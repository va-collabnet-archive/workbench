package org.dwfa.vodb.jar;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_Path;
import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.types.I_ProcessPathEntries;
import org.dwfa.vodb.types.Path;

import com.sleepycat.je.DatabaseEntry;

public class PathCollector implements I_ProcessPathEntries {
	List<I_Path> paths = new ArrayList<I_Path>();
	PathBinder binder = new PathBinder();
	public PathCollector() {

	}

	public void processPath(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		Path p = (Path) binder.entryToObject(value);
		paths.add(p);
	}

	public List<I_Path> getPaths() {
		return paths;
	}

	public DatabaseEntry getDataEntry() {
		return new DatabaseEntry(); 
	}

	public DatabaseEntry getKeyEntry() {
		return new DatabaseEntry();
	}
}