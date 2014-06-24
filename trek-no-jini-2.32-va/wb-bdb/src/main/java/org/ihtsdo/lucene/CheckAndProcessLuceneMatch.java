package org.ihtsdo.lucene;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.lucene.document.Document;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.task.search.I_TestSearchResults;

public abstract class CheckAndProcessLuceneMatch implements Runnable {

    Collection<LuceneMatch> matches;

    List<I_TestSearchResults> checkList;

    I_ConfigAceFrame config;

    Document doc;

    protected float score;

    protected CountDownLatch hitLatch;

    public CheckAndProcessLuceneMatch(CountDownLatch hitLatch, 
    		I_UpdateProgress updater, Document doc,
            float score, Collection<LuceneMatch> matches, 
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
        super();
        this.doc = doc;
        this.score = score;
        this.matches = matches;
        this.checkList = checkList;
        this.config = config;
        this.hitLatch = hitLatch;
    }
}
