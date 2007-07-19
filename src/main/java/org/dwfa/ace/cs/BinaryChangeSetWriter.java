package org.dwfa.ace.cs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.utypes.UniversalAceBean;
import org.dwfa.ace.utypes.UniversalIdList;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.io.FileIO;

public class BinaryChangeSetWriter implements I_WriteChangeSet {

	@Override
	public String toString() {
		return "BinaryChangeSetWriter: changeSetFile: " + changeSetFile
				+ " tempFile: " + tempFile;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static class NoHeaderObjectOutputStream extends ObjectOutputStream {

		public NoHeaderObjectOutputStream(OutputStream out) throws IOException {
			super(out);
		}

		@Override
		protected void writeStreamHeader() throws IOException {
			reset();
		}

	}

	private File changeSetFile;

	private File tempFile;

	private transient ObjectOutputStream tempOut;

	public BinaryChangeSetWriter(File changeSetFile, File tempFile) {
		super();
		this.changeSetFile = changeSetFile;
		this.tempFile = tempFile;
	}

	public void commit() throws IOException {

		if (tempOut != null) {
			tempOut.flush();
			tempOut.close();
			tempOut = null;
		}
		String canonicalFileString = tempFile.getCanonicalPath();
		if (tempFile.renameTo(changeSetFile) == false) {
			System.out
					.println("tempFile.renameTo failed. Attempting FileIO.copyFile...");
			FileIO.copyFile(tempFile.getCanonicalPath(), changeSetFile
					.getCanonicalPath());
		}
		tempFile = new File(canonicalFileString);
		tempFile.delete();
	}

	public void open() throws IOException {
		if (changeSetFile.exists() == false) {
			changeSetFile.getParentFile().mkdirs();
			changeSetFile.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(changeSetFile));
			oos.writeObject(BinaryChangeSetReader.class);
			oos.flush();
			oos.close();
		}
		FileIO.copyFile(changeSetFile.getCanonicalPath(), tempFile
				.getCanonicalPath());
		tempOut = new NoHeaderObjectOutputStream(new FileOutputStream(tempFile,
				true));
	}

	public void writeChanges(I_Transact change, long time) throws IOException {
		tempOut.writeLong(time);
		if (I_GetConceptData.class.isAssignableFrom(change.getClass())) {
			writeChanges((I_GetConceptData) change, time);
		} else if (I_Path.class.isAssignableFrom(change.getClass())) {
			writeChanges((I_Path) change, time);
		} else if (UniversalIdList.class.isAssignableFrom(change.getClass())) {
			writeIds((UniversalIdList) change, time);
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

	private void writeIds(UniversalIdList idList, long time)
			throws IOException {
		tempOut.writeObject(idList);
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

}
