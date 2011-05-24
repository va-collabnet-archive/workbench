package org.ihtsdo.lucene;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.description.Description;

public class DescriptionIndexGenerator extends IndexGenerator {

    private int descCounter = 0;
    private int conceptCounter = 0;
    private int feedbackInterval = 1000;

    public DescriptionIndexGenerator(IndexWriter writer) throws IOException {
		super(writer);
	}

    @Override
    public void processConceptData(Concept concept) throws Exception {
        conceptCounter++;
        for (Description d : concept.getDescriptions()) {
            writer.addDocument(createDoc(d));
            descCounter++;

            if (descCounter % feedbackInterval == 0) {
                System.out.print(".");
                lineCounter++;
                if (lineCounter > 80) {
                    lineCounter = 0;
                    System.out.println();
                    System.out.print("c:" + conceptCounter
                            + " d:" + descCounter);
                }
            }
        }
    }

    public static Document createDoc(Description desc)
    throws IOException {
		Document doc = new Document();
		doc.add(new Field("dnid", Integer.toString(desc.getDescId()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("cnid", Integer.toString(desc.getConceptNid()), Field.Store.YES, Field.Index.NOT_ANALYZED));
		addIdsToIndex(doc, desc);
		addIdsToIndex(doc, Concept.get(desc.getConceptNid()).getConceptAttributes());
		
		String lastDesc = null;
		for (I_DescriptionTuple tuple : desc.getTuples()) {
		    if (lastDesc == null || lastDesc.equals(tuple.getText()) == false) {
		        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
		            AceLog.getAppLog().fine(
		                    "Adding to index. dnid:  " + desc.getDescId() + " desc: " + tuple.getText());
		        }
		        doc.add(new Field("desc", tuple.getText(), Field.Store.NO, Field.Index.ANALYZED));
		    }
		}
		return doc;
    }

    private static void addIdsToIndex(Document doc, I_Identify did) {
        if (did != null) {
            for (I_IdPart p : did.getMutableIdParts()) {
                doc.add(new Field("desc", p.getDenotation().toString(), Field.Store.NO,
                        Field.Index.NOT_ANALYZED));
            }
        } else {
            AceLog.getAppLog().alertAndLogException(new Exception("Identifier is null"));
        }
    }
}
