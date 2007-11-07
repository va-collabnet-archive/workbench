package org.dwfa.mojo;

import java.io.IOException;

import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.cs.I_WriteChangeSet;

public class WriteChangeSetToString implements I_WriteChangeSet {

	public void commit() throws IOException {
		// TODO Auto-generated method stub

	}

	public void open() throws IOException {
		// TODO Auto-generated method stub

	}

	public void writeChanges(I_Transact change, long time) throws IOException {
		System.out.println(change.toString() + ":" + time);

	}

}
