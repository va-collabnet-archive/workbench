package org.dwfa.vodb.bind;

import java.util.logging.Level;

import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

public class ThinExtRefsetIdSecondaryKeyCreator implements SecondaryKeyCreator {
	private ThinExtBinder fixedOnlyBinder = new ThinExtBinder(true);

	MemberAndSecondaryIdBinding memberAndSecondaryIdBinding = new MemberAndSecondaryIdBinding();

	public ThinExtRefsetIdSecondaryKeyCreator() {
		super();
	}

	public boolean createSecondaryKey(SecondaryDatabase secDb,
			DatabaseEntry keyEntry, DatabaseEntry dataEntry,
			DatabaseEntry resultEntry) throws DatabaseException {
		I_ThinExtByRefVersioned core = (I_ThinExtByRefVersioned) fixedOnlyBinder
				.entryToObject(dataEntry);

		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine(
					"Creating secondary RefsetId key (2) for m: "
							+ core.getMemberId() + " refset: "
							+ core.getRefsetId());
		}
		memberAndSecondaryIdBinding.objectToEntry(new MemberAndSecondaryId(core
				.getRefsetId(), core.getMemberId()), resultEntry);
		return true;
	}

}
