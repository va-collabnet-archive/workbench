package org.dwfa.vodb;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Hits;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.search.I_TrackContinuation;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.vodb.types.I_ProcessDescriptionEntries;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;

public interface I_StoreDescriptions extends I_StoreInBdb {

	public Iterator<I_DescriptionVersioned> getDescriptionIterator()
			throws IOException;

	public void writeDescription(I_DescriptionVersioned desc)
		throws DatabaseException, IOException;

	public void writeDescriptionNoLuceneUpdate(I_DescriptionVersioned desc)
		throws DatabaseException, IOException;

	public boolean hasDescription(int descId, int concId) throws DatabaseException, IOException;

	public I_DescriptionVersioned getDescription(int descId, int concId)
			throws IOException, DatabaseException;

	public List<I_DescriptionVersioned> getDescriptions(int conceptId)
			throws DatabaseException, IOException;

	public int countDescriptions() throws DatabaseException, IOException;

	/**
	 * This method is multithreaded hot.
	 * 
	 * @param continueWork
	 * @param p
	 * @param matches
	 * @param latch
	 * @throws DatabaseException
	 * @throws IOException 
	 */
	public void searchRegex(I_TrackContinuation tracker, Pattern p,
			Collection<I_DescriptionVersioned> matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws DatabaseException, IOException;

	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, LuceneProgressUpdator updater)
			throws DatabaseException, IOException, ParseException;

	public void createLuceneDescriptionIndex() throws IOException;

	public void iterateDescriptionEntries(
			I_ProcessDescriptionEntries processor) throws Exception;

	public Hits doLuceneSearch(String query) throws IOException,
			ParseException;

	public I_DescriptionVersioned descEntryToObject(DatabaseEntry key, DatabaseEntry value);

}