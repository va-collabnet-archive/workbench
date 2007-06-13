package org.dwfa.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_ProcessDescriptions;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;

public class VodbCreateLuceneIndex extends AbstractMojo {
	
	private enum IndexerType {
		Snowball, Standard, Fuzzy
	}
	
	private IndexerType indexType = IndexerType.Fuzzy;
	/**
	 * Location of the lucene directory.
	 * 
	 * @parameter expression="${project.build.directory}/generated-resources/berkeley-db/lucene-custom"
	 * @required
	 */
	private File luceneDir;
	
	private class Indexer implements I_ProcessDescriptions {

		
		
		private IndexWriter writer;

		public Indexer() throws IOException {
			super();
			luceneDir.mkdirs();
			Directory dir = FSDirectory.getDirectory(luceneDir, true);
			
			switch (indexType) {
			case Standard:
			case Fuzzy:
				writer = new IndexWriter(dir, new StandardAnalyzer(), true);
				break;
			case Snowball:
				writer = new IndexWriter(dir, new StandardAnalyzer(), true);
				break;
			}
		
			writer.setUseCompoundFile(true);
			writer.mergeFactor = 10000;
		}

		public void processDescription(I_DescriptionVersioned desc) throws Exception {
			Document doc = new Document();
			doc.add(Field.Keyword("dnid", Integer.toString(desc
					.getDescId())));
			doc.add(Field.Keyword("cnid", Integer.toString(desc
					.getConceptId())));
			doc.add(Field.Keyword("tnid", Integer.toString(desc
					.getFirstTuple().getTypeId())));
			String lastDesc = null;
			for (I_DescriptionTuple tuple : desc.getTuples()) {
				if (lastDesc == null
						|| lastDesc.equals(tuple.getText()) == false) {
					doc.add(Field.UnStored("desc", tuple.getText()));
				}

			}
			writer.addDocument(doc);
		}

		public void close() throws IOException {
			writer.optimize();
			writer.close();
		}
				
	}
	public void execute() throws MojoExecutionException, MojoFailureException {
		I_TermFactory termFactory = LocalVersionedTerminology.get();

		try {
			Indexer descIndexer = new Indexer();
			termFactory.iterateDescriptions(descIndexer);
			descIndexer.close();
		} catch (Exception e) {
			throw new MojoExecutionException(e.getLocalizedMessage(), e);
		}		
		
	}

}
