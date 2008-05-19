package org.dwfa.ace.task.cmrscs;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.api.cs.I_Count;
import org.dwfa.ace.api.cs.I_ReadChangeSet;
import org.dwfa.ace.api.cs.I_ValidateChangeSetChanges;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;

public class CmrscsReader implements I_ReadChangeSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private File changeSetFile;
	private I_Count counter;
	private boolean initialized = false;
	private DataInputStream dis;
	private Long nextCommit;

	public CmrscsReader(File changeSetFile) {
		super();
		this.changeSetFile = changeSetFile;
	}

	public List<I_ValidateChangeSetChanges> getValidators() {
		return new ArrayList<I_ValidateChangeSetChanges>();
	}

	public long nextCommitTime() throws IOException, ClassNotFoundException {
		lazyInit();
		if (nextCommit == null) {
			nextCommit = dis.readLong();
		}
		return nextCommit;
	}

	public void read() throws IOException, ClassNotFoundException {
		readUntil(Long.MAX_VALUE);
	}

	@SuppressWarnings("unchecked")
	public void readUntil(long endTime) throws IOException,
			ClassNotFoundException {
		HashSet<TimePathId> timePathValues = new HashSet<TimePathId>();
		UUID endUid = new UUID(0, 0);
		try {
			int unspecifiedUuidNid = ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID
					.localize().getNid();
			int booleanExt = RefsetAuxiliary.Concept.BOOLEAN_EXTENSION
					.localize().getNid();
			while (nextCommitTime() < endTime) {
				int count = 0;
				count++;
				if (counter != null) {
					counter.increment();
				}
				UUID pathUid = readUuid(dis);
				I_Path path = getVodb().getPath(new UUID[] { pathUid });
				UUID refsetUid = readUuid(dis);
				UUID memberUid = readUuid(dis);
				while (memberUid.equals(endUid) == false) {
					UUID componentUid = readUuid(dis);
					UUID statusUid = readUuid(dis);
					
					
					I_ThinExtByRefVersioned ebr;
					I_ThinExtByRefPartBoolean newPart = getVodb()
							.newBooleanExtensionPart();
					newPart.setPathId(getVodb().uuidToNative(pathUid));
					newPart.setStatus(getVodb().uuidToNative(statusUid));
					newPart.setValue(true);
					newPart.setVersion(getVodb().convertToThinVersion(nextCommit));
					if (getVodb().hasExtension(
							getVodb().uuidToNativeWithGeneration(memberUid,
									unspecifiedUuidNid, path,
									getVodb().convertToThinVersion(nextCommit)))) {
						ebr = getVodb().getExtension(
								getVodb().uuidToNative(memberUid));
						I_ThinExtByRefPartBoolean lastPart = (I_ThinExtByRefPartBoolean) 
							ebr.getVersions().get(ebr.getVersions().size() -1);
						ebr.getVersions().clear();
						ebr.addVersion(lastPart);
						ebr.addVersion(newPart);
					} else {
						ebr = getVodb().newExtension(
								getVodb().uuidToNative(refsetUid),
								getVodb().uuidToNative(memberUid),
								getVodb().uuidToNative(componentUid),
								booleanExt);
						((List<I_ThinExtByRefPartBoolean>) ebr.getVersions()).add(newPart);

					}
					getVodb().getDirectInterface().writeExt(ebr);
					memberUid = readUuid(dis);
				}
				nextCommit = dis.readLong();

			}
		} catch (EOFException ex) {
			dis.close();
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

	private UUID readUuid(DataInputStream dis) throws IOException {
		return new UUID(dis.readLong(), dis.readLong());

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
				dis = new DataInputStream(bis);
				initialized = true;
				nextCommit = dis.readLong();
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
