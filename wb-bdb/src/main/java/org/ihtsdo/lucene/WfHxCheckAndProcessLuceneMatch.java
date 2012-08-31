package org.ihtsdo.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import org.apache.lucene.document.Document;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.workflow.LuceneWfHxMatch;
import org.dwfa.ace.search.workflow.SearchWfHxWorker.LuceneWfHxProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_string.RefexStringVersionBI;
import org.ihtsdo.tk.spec.ConceptSpec;

public class WfHxCheckAndProcessLuceneMatch extends CheckAndProcessLuceneMatch {

    public WfHxCheckAndProcessLuceneMatch(CountDownLatch hitLatch,
            LuceneWfHxProgressUpdator updater, Document doc, float score,
            Collection<LuceneMatch> matches,
            List<I_TestSearchResults> checkList,
            I_ConfigAceFrame config) {
        super(hitLatch, updater, doc, score, matches, checkList, config);
    }

    public Long getTimestamp() {
        return Long.parseLong(doc.get("lastTime"));
    }

    public String getConcept() {
        return doc.get("conceptId");
    }

    public float getScore() {
        return score;
    }

    public Document getDoc() {
        return doc;
    }

    public void run() {
        LuceneMatch match = null;

        if (hitLatch.getCount() > 0) {

            try {
                int fsnNid = Ts.get().getNidForUuids(UUID.fromString(doc.get("fsn")));
                int cNid = Ts.get().getNidForUuids(UUID.fromString(doc.get("conceptId")));

                ConceptVersionBI c = Ts.get().getConceptVersion(config.getViewCoordinate(), cNid);
                if (c != null) {
                    DescriptionVersionBI desc = null;
                    Collection<? extends DescriptionVersionBI> descActive = c.getDescriptionsFullySpecifiedActive();
                    if (!descActive.isEmpty()) {
                        desc = descActive.iterator().next();
                    }
                    ConceptSpec wfHxRefSpec = new ConceptSpec("history workflow refset",UUID.fromString("0b6f0e24-5fe2-3869-9342-c18008f53283"));
                    Collection<? extends RefexVersionBI<?>> activeAnnotations = c.getActiveAnnotations(config.getViewCoordinate(), wfHxRefSpec.getLenient().getNid());
                    ArrayList<RefexVersionBI> filteredAnnotations = new ArrayList<RefexVersionBI>();
                    for(RefexVersionBI annotation : activeAnnotations){
                        if(RefexStringVersionBI.class.isAssignableFrom(annotation.getClass())){
                            RefexStringVersionBI strAnnot = (RefexStringVersionBI) annotation;
                            if(strAnnot.getString1().contains(doc.get("workflowId"))){
                                filteredAnnotations.add(annotation);
                            }
                        }
                    }
                    if (desc != null && filteredAnnotations.size() > 0) {
                        String displayState = doc.get("lastState");
                        String displayAction = doc.get("lastAction");
                        String displayModeler = doc.get("lastModeler");
                        Long displayTime = Long.parseLong(doc.get("lastTime"));
                        String conceptId = doc.get("conceptId");
                        String fsn = doc.get("fsn");

                        WorkflowLuceneSearchResult val = new WorkflowLuceneSearchResult(displayAction, displayState, displayModeler, displayTime, conceptId, fsn);

                        match = new LuceneWfHxMatch(val, score);
                        matches.add(match);
                    }
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
    }
}