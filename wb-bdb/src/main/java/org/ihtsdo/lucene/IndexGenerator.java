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
	
	public NidBitSetBI getNidSet() {
		return nidSet;
	}
	
	public IndexGenerator(IndexWriter writer) throws IOException {
		super();
		this.writer = writer;
		this.nidSet  = Bdb.getConceptDb().getConceptNidSet();
	}


	@Override
	public void processConceptData(Concept concept) throws Exception {
        int counter = 0;
        int optimizeInterval = 10000;
        for (Description d: concept.getDescriptions()) {
             writer.addDocument(LuceneManager.createDoc(d));
            counter = counter++;
            if (counter == optimizeInterval) {
                writer.optimize();
                counter = 0;
            }
        }
	}


	@Override
	public boolean continueWork() {
		return true;
	}


}
