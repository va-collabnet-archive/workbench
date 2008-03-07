package org.dwfa.mojo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.cs.I_WriteChangeSet;
import org.dwfa.ace.cs.BinaryChangeSetWriter;

public class WriteChangeSetToMultipleBinaryFiles implements org.dwfa.ace.api.cs.I_WriteChangeSet {

	private static int changeCounter = 0;
	private static int fileCounter = 0;
	private String newfilename = "";
	List<I_WriteChangeSet> files = new LinkedList<I_WriteChangeSet>();

	I_WriteChangeSet binaryWriter = null;

	I_TermFactory tf = LocalVersionedTerminology.get();
	private File changeSetFile = null;

	public WriteChangeSetToMultipleBinaryFiles(File changeSetFile, File tempFile) throws IOException {
		this.changeSetFile = changeSetFile;
	}

	public void commit() throws IOException {
		for (I_WriteChangeSet writer: files) {
			writer.commit();
		}
		files.clear();
		binaryWriter = null;
	}

	public void open() throws IOException {
	}

	public void writeChanges(I_Transact change, long time) throws IOException {

		if (changeCounter%10000==0 || binaryWriter==null) {
			newfilename = changeSetFile.getAbsolutePath().replace(".jcs", "["+fileCounter+"].jcs");
			binaryWriter = tf.newBinaryChangeSetWriter(new File(newfilename));
			files.add(binaryWriter);
			binaryWriter.open();
			fileCounter++;
		}
		changeCounter++;
		binaryWriter.writeChanges(change,time);
	}
}