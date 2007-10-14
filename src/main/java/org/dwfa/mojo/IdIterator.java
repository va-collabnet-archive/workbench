package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.dwfa.ace.api.I_IdVersioned;
import org.dwfa.ace.api.I_ProcessIds;
import org.dwfa.ace.api.I_IdTuple;
import org.dwfa.ace.api.LocalVersionedTerminology;

import java.util.List;

public class IdIterator implements I_ProcessIds {

	
	private BufferedWriter output = null;
	public IdIterator(BufferedWriter output) throws IOException {
		this.output = output;
	}
	
	public void processId(I_IdVersioned idv) throws Exception {
		List<I_IdTuple> tuples = idv.getTuples();
				
		for (I_IdTuple tuple: tuples) {			
			String status = LocalVersionedTerminology.get().getUids(tuple.getIdStatus()).iterator().next().toString();
			output.write(tuple.getUIDs().iterator().next().toString() + "\t" + status);
		}
		
		output.newLine();	
	}
	

}
