package org.dwfa.ace.task.cmrscs;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;

public class CmrscsReader implements I_ReadChangeSet {

	private File changeSetFile;
	private I_Count counter;
	private boolean initialized = false;
	private ObjectInputStream ois;
	private Long nextCommit;

	public List<I_ValidateChangeSetChanges> getValidators() {
		return new ArrayList<I_ValidateChangeSetChanges>();
	}

	public long nextCommitTime() throws IOException, ClassNotFoundException {
		lazyInit();
		if (nextCommit == null) {
			nextCommit = ois.readLong();
		}
		return nextCommit;
	}

	public void read() throws IOException, ClassNotFoundException {
		readUntil(Long.MAX_VALUE);
	}

	public void readUntil(long endTime) throws IOException,
			ClassNotFoundException {
		HashSet<TimePathId> timePathValues = new HashSet<TimePathId>();
		UUID endUid = new UUID(0,0);
		while (nextCommitTime() < endTime) {
			int count = 0;
			try {
				count++;
				if (counter != null) {
					counter.increment();
				}
				UUID pathUid = readUuid(ois);
				UUID refsetUid = readUuid(ois);
				long time = ois.readLong();
				UUID conceptUid = readUuid(ois);
				while (conceptUid.equals(endUid) == false) {
					UUID componentUid = readUuid(ois);
					UUID memberUid = readUuid(ois);
					UUID status = readUuid(ois);
					
					if (getVodb().hasExtension(getVodb().uuidToNative(componentUid))) {
						I_ThinExtByRefVersioned ebr = getVodb().getExtension(getVodb().uuidToNative(componentUid));
					} else {
						
					}
				}
				nextCommit = ois.readLong();
			} catch (EOFException ex) {
				ois.close();
				AceLog.getEditLog().info("End of change set. ");
				nextCommit = Long.MAX_VALUE;
				getVodb().setProperty(
						changeSetFile.toURI().toURL().toExternalForm(),
						Long.toString(changeSetFile.length()));
			} catch (TerminologyException e) {
				IOException ioe = new IOException();
				ioe.initCause(e);
				throw ioe;
			} 
		}
		if (AceLog.getEditLog().isLoggable(Level.FINE)) {
			AceLog.getEditLog().fine(
					"Committing time branches: " + timePathValues);
		}
		for (TimePathId timePath : timePathValues) {
			getVodb().getDirectInterface().writeTimePath(timePath);
		}
	}

	public void setChangeSetFile(File changeSetFile) {
		this.changeSetFile = changeSetFile;
	}

	public void setCounter(I_Count counter) {
		this.counter = counter;
	}
	
	private UUID readUuid(ObjectInputStream ois) throws IOException {
		return new UUID(ois.readLong(), ois.readLong());
		
	}

	@SuppressWarnings("unchecked")
	private void lazyInit() throws FileNotFoundException, IOException,
			ClassNotFoundException {
		if (initialized == false) {
			String lastImportSize = getVodb().getProperty(
					changeSetFile.toURI().toURL().toExternalForm());
			if (lastImportSize != null) {
				long lastSize = Long.parseLong(lastImportSize);
				if (lastSize == changeSetFile.length()) {
					AceLog.getAppLog().finer(
							"Change set already fully read: "
									+ changeSetFile.toURI().toURL()
											.toExternalForm());
					// already imported, set to nothing to do...
					nextCommit = Long.MAX_VALUE;
					initialized = true;
				}
			}
			if (initialized == false) {
				FileInputStream fis = new FileInputStream(changeSetFile);
				BufferedInputStream bis = new BufferedInputStream(fis);
				ois = new ObjectInputStream(bis);
				initialized = true;
				nextCommit = ois.readLong();
			}
		}
	}

	private I_TermFactory tf = null;
	private I_TermFactory getVodb() {
		if (tf == null) {
			tf = LocalVersionedTerminology.get();
		}
		return tf;
	}

}
