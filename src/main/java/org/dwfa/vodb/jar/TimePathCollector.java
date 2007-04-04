package org.dwfa.vodb.jar;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.TimePathId;
import org.dwfa.vodb.bind.TimePathIdBinder;
import org.dwfa.vodb.types.I_ProcessTimeBranch;

import com.sleepycat.je.DatabaseEntry;

public class TimePathCollector implements I_ProcessTimeBranch {
	List<TimePathId> timePathList = new ArrayList<TimePathId>();
	TimePathIdBinder binder = new TimePathIdBinder();
	public TimePathCollector() {

	}

	public List<TimePathId> getTimePathIdList() {
		return timePathList;
	}

	public DatabaseEntry getDataEntry() {
		return new DatabaseEntry(); 
	}

	public DatabaseEntry getKeyEntry() {
		return new DatabaseEntry();
	}

	public void processTimeBranch(DatabaseEntry key, DatabaseEntry value) throws Exception {
		TimePathId timePathId = (TimePathId) binder.entryToObject(value);
		timePathList.add(timePathId);
	}
}