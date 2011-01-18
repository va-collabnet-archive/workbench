package org.ihtsdo.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.NidBitSetBI;

public class IndexGenerator implements I_ProcessConceptData {

    private IndexWriter writer;
    private NidBitSetBI nidSet;
    private int lineCounter = 0;
    private int descCounter = 0;
    private int conceptCounter = 0;
    private int feedbackInterval =  1000;
 
    @Override
    public NidBitSetBI getNidSet() {
        return nidSet;
    }

    public IndexGenerator(IndexWriter writer) throws IOException {
        super();
        this.writer = writer;
        this.nidSet = Bdb.getConceptDb().getConceptNidSet();
    }

    @Override
    public void processConceptData(Concept concept) throws Exception {
        conceptCounter++;
       for (Description d : concept.getDescriptions()) {
            writer.addDocument(LuceneManager.createDoc(d));
            descCounter++;
            
            if (descCounter % feedbackInterval == 0) {
                System.out.print(".");
                lineCounter++;
                if (lineCounter > 80) {
                    lineCounter = 0;
                    System.out.println();
                    System.out.print("c:" + conceptCounter + 
                            " d:" + descCounter);
                }
             }
        }
    }

    @Override
    public boolean continueWork() {
        return true;
    }
}
