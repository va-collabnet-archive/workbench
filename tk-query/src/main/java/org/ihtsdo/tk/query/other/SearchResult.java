package org.ihtsdo.tk.query.other;

import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;

public class SearchResult {
	public TopDocs topDocs;
	public Searcher searcher;
	
	public SearchResult(TopDocs topDocs, Searcher searcher) {
		super();
		this.topDocs = topDocs;
		this.searcher = searcher;
	}
}
