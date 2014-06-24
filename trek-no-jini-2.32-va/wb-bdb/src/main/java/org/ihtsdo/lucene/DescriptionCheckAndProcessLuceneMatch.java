package org.ihtsdo.lucene;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.LuceneDescriptionMatch;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.description.Description;

public class DescriptionCheckAndProcessLuceneMatch extends CheckAndProcessLuceneMatch {


    public DescriptionCheckAndProcessLuceneMatch(CountDownLatch hitLatch,
			LuceneProgressUpdator updater, Document doc, float score,
			Collection<LuceneMatch> matches,
			List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
		super(hitLatch, updater, doc, score, matches, checkList, config);
	}

	public void run() {
        if (hitLatch.getCount() > 0) {
            int nid = Integer.parseInt(doc.get("dnid"));
            int cnid = Integer.parseInt(doc.get("cnid"));
            try {
                Concept c = Concept.get(cnid);
                if (c != null) {
                    Description descV = c.getDescription(nid);
                    if (descV != null) {
                        LuceneMatch match = new LuceneDescriptionMatch(descV, score);
                        if (checkList == null || checkList.size() == 0) {
                            matches.add(match);
                            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                                AceLog.getAppLog().fine("processing match: " + descV + " new match size: " + matches.size());
                            }
                        } else {
                            try {
                                boolean failed = false;
                                for (I_TestSearchResults test : checkList) {
                                    if (test != null) {
                                        if (test.test(descV, config) == false) {
                                            failed = true;
                                            break;
                                        }
                                    }
                                }

                                if (failed == false) {
                                    matches.add(match);
                                }
                            } catch (TaskFailedException e) {
                                AceLog.getAppLog().alertAndLogException(e);
                            }
                        }
                    } else {
                        AceLog.getAppLog().alertAndLogException(new Exception("Attempt to test missing description on concept: " + c.toString()));
                        AceLog.getAppLog().warning("Exception on concept: " + c.toLongString());
                    }
                } else {
                    AceLog.getAppLog().alertAndLogException(new Exception("Attempt to test  description on null: " + doc));
                }
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (Throwable e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
            this.hitLatch.countDown();
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Hit latch: " + this.hitLatch.getCount());
            }
        }
    }}
