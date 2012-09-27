package org.dwfa.ace.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import org.apache.lucene.queryParser.ParseException;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.ace.task.search.I_TestSearchResults;

public interface I_Search {

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
			Collection<I_DescriptionVersioned<?>> matches, CountDownLatch latch,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config)
			throws IOException;

	public CountDownLatch searchLucene(I_TrackContinuation tracker,
			String query, Collection<LuceneMatch> matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, I_UpdateProgress updater)
			throws IOException, ParseException;

	public void searchConcepts(I_TrackContinuation tracker,
			I_RepresentIdSet matches,
			CountDownLatch latch, List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config) throws IOException,
			ParseException;

	public CountDownLatch searchWfHx(I_TrackContinuation tracker, 
			Collection<LuceneMatch> matches, CountDownLatch latch, 
			List<I_TestSearchResults> checkList,
			I_ConfigAceFrame config, I_UpdateProgress updater, 
			boolean wfInProgress, boolean completedWF)
			throws Exception;

}