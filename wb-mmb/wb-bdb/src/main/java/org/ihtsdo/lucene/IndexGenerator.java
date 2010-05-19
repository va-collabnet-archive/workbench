package org.ihtsdo.lucene;

import org.apache.lucene.index.IndexWriter;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.description.Description;

public class IndexGenerator implements I_ProcessConceptData {

	private IndexWriter writer;
	
	public IndexGenerator(IndexWriter writer) {
		super();
		this.writer = writer;
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
