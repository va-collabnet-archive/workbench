package org.ihtsdo.tk.query.other;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

public class SearchResult {
	public TopDocs topDocs;
	public IndexSearcher searcher;
	
	public SearchResult(TopDocs topDocs, IndexSearcher searcher) {
		super();
		this.topDocs = topDocs;
		this.searcher = searcher;
	}
}
