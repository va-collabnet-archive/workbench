package org.ihtsdo.lucene;

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.tk.api.NidBitSetBI;

public abstract class IndexGenerator implements I_ProcessConceptData {

    protected IndexWriter writer;
    protected NidBitSetBI nidSet;
    protected int lineCounter = 0;

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
    public boolean continueWork() {
        return true;
    }
}
