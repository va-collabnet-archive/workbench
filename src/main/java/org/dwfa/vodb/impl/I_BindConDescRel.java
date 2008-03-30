package org.dwfa.vodb.impl;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.dwfa.vodb.types.ConceptBean;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_BindConDescRel {

	public ConceptBean populateBean(TupleInput ti, ConceptBean conceptBean) throws DataFormatException, IOException;
	
	public ConceptBean entryToObject(TupleInput ti);
	
	public void objectToEntry(Object obj, TupleOutput to);

	public void objectToEntry(Object obj, DatabaseEntry value);
	
	public void close() throws DatabaseException;
	
	public void sync() throws DatabaseException;
	
}