package org.dwfa.ace.cs;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

public class BinaryChangeSetWriter implements I_WriteChangeSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private File changeSetFile;

	private File tempFile;

	private transient ObjectOutputStream tempOut;

	public void commit() throws IOException {
		tempOut.close();
		FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile
				.getCanonicalPath());
	}

	public void open() throws IOException {
		if (changeSetFile.exists() == false) {
			changeSetFile.getParentFile().mkdirs();
			changeSetFile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(changeSetFile));
			oos.writeObject(BinaryChangeSetReader.class);
			oos.close();
		}
		FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile
				.getCanonicalPath());
		tempOut = new ObjectOutputStream(new BufferedOutputStream(
				new FileOutputStream(tempFile, true)));
	}

	public void writeChanges(I_Transact change, long time) throws IOException {
		tempOut.writeObject("This is a comment...");
		tempOut.writeLong(time);
		if (I_GetConceptData.class.isAssignableFrom(change.getClass())) {
			writeChanges((I_GetConceptData) change, time);
		} else if (I_Path.class.isAssignableFrom(change.getClass())) {
			writeChanges((I_Path) change, time);
		} else {
			throw new IOException("Can't handle class: "
					+ change.getClass().getName());
		}
	}

	private void writeChanges(I_GetConceptData cb, long time)
			throws IOException {
		try {
			UniversalAceBean bean = cb.getUniversalAceBean();
			tempOut.writeObject(bean);
		} catch (TerminologyException e) {
			IOException ioe = new IOException(e.getLocalizedMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	private void writeChanges(I_Path path, long time) throws IOException {
		try {
			tempOut.writeObject(path.getUniversal());
		} catch (TerminologyException e) {
			IOException ioe = new IOException(e.getLocalizedMessage());
			ioe.initCause(e);
			throw ioe;
		}
	}

	public BinaryChangeSetWriter(File changeSetFile, File tempFile) {
		super();
		this.changeSetFile = changeSetFile;
		this.tempFile = tempFile;
	}

}
