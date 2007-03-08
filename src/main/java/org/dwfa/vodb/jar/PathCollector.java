package org.dwfa.vodb.jar;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.vodb.bind.PathBinder;
import org.dwfa.vodb.types.I_ProcessPaths;
import org.dwfa.vodb.types.Path;

import com.sleepycat.je.DatabaseEntry;

public class PathCollector implements I_ProcessPaths {
	List<Path> paths = new ArrayList<Path>();
	PathBinder binder = new PathBinder();
	public PathCollector() {

	}

	public void processPath(DatabaseEntry key, DatabaseEntry value)
			throws Exception {
		Path p = (Path) binder.entryToObject(value);
		paths.add(p);
	}

	public List<Path> getPaths() {
		return paths;
	}

	public DatabaseEntry getDataEntry() {
		return new DatabaseEntry(); 
	}

	public DatabaseEntry getKeyEntry() {
		return new DatabaseEntry();
	}
}