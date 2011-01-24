/**
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.ParallelReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The Class App.
 */
public class App 
{
	
	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 */
	public static void main( String[] args ) throws IOException, ParseException
	{
		//File excelFile = new File("/Users/alo/Documents/TermMed/Proyectos/Translation/tm.xls");
		//DocumentManager.matchTranslationMemory("screening");
		//DocumentManager.indexMemoryFromXls();
		//spellCheck("procdure");
		for (String word : DocumentManager.getSugestionsFromDictionary("asthma","EN")) {
			System.out.println(word);
		}
		
	}

	/**
	 * Index.
	 */
	public static void index()
	{
		try {
			File directory = new File("documents");
			File indexDir = new File("documentsIndex");
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), 
					new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
			File files[] = directory.listFiles();
			int counter = 0;
			for (File f : files) {
				counter++;
				try {
					if (!f.getName().endsWith(".pdf")) {
						throw new IOException();
					}
//					FileInputStream fi = new FileInputStream(f);
//					PDFParser parser = null;
//					parser = new PDFParser(fi);
//					parser.parse();
//					COSDocument cd = parser.getDocument();
//					PDFTextStripper stripper = new PDFTextStripper();
//					String text = stripper.getText(new PDDocument(cd));
//					Document doc = new Document();
//					doc.add(new Field("path", f.getPath(),Field.Store.YES, Field.Index.ANALYZED));
//					doc.add(new Field("text",text,Field.Store.YES, Field.Index.ANALYZED));
//					writer.addDocument(doc);
//					System.out.println(counter + ") Indexing:" + doc.get("path") + " Size:" + doc.get("text").length());
//					cd.close();
				} catch (IOException e) {
					System.out.println("Skipping " + f.getName());
				}
			}
			writer.optimize();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Index tika.
	 */
	public static void indexTika()
	{
		try {
			File directory = new File("documents");
			File indexDir = new File("documentsIndex");
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), 
					new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
			File files[] = directory.listFiles();
			int counter = 0;
			for (File f : files) {
				counter++;
				try {
					ContentHandler textHandler = new BodyContentHandler();
					if (f.getName().endsWith(".pdf")) {
						FileInputStream fi = new FileInputStream(f);
						Metadata metadata = new Metadata();
						org.apache.tika.parser.pdf.PDFParser parser = new  org.apache.tika.parser.pdf.PDFParser();
						parser.parse(fi, textHandler, metadata);
						fi.close();
					} else if (f.getName().endsWith(".doc") || f.getName().endsWith(".xls")) {
						FileInputStream fi = new FileInputStream(f);
						Metadata metadata = new Metadata();
						org.apache.tika.parser.microsoft.OfficeParser parser = new  org.apache.tika.parser.microsoft.OfficeParser();
						parser.parse(fi, textHandler, metadata);
						fi.close();
					} else {
						throw new IOException();
					}
					Document doc = new Document();
					doc.add(new Field("path", f.getPath(),Field.Store.YES, Field.Index.ANALYZED));
					doc.add(new Field("text",textHandler.toString(),Field.Store.YES, Field.Index.ANALYZED));
					writer.addDocument(doc);
					System.out.println(counter + ") Indexing:" + doc.get("path") + " Size:" + doc.get("text").length());
				} catch (IOException e) {
					System.out.println("Skipping " + f.getName());
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TikaException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			writer.optimize();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Search.
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParseException the parse exception
	 */
	public static void search() throws IOException, ParseException
	{
		Directory fsDir = FSDirectory.open(new File("documentsIndex"));
		IndexSearcher is = new IndexSearcher(fsDir);
		PrintWriter htmlWriter = new PrintWriter(new FileWriter(new File("searchResuls.html")));
		htmlWriter.println("<html><body><font style='font-family:arial,sans-serif'>");

		String q = "\"clinical findings\"";

		//QueryParser qp = new QueryParser("transtiveclosure", new TermMedAnalyzer());
		QueryParser qp = new QueryParser(Version.LUCENE_30, "text", new StandardAnalyzer(Version.LUCENE_30));
		Query query = qp.parse(q);

		//SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
		SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font style='BACKGROUND-COLOR: yellow'>", 
		"</font>");
		Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

		long start = new Date().getTime();
		TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
		is.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		long end = new Date().getTime();
		htmlWriter.println(
				"Found " + hits.length + " document(s) (in " + (end - start) +
				" milliseconds) that matched query '" + q + "':<br><br>"
		);

		for (int i = 0; i < hits.length; i++) {
			int docId = hits[i].doc;
			Document doc = is.doc(docId);
			htmlWriter.println((i+1) + ") " + doc.get("path") + "<br>");
			TokenStream tokenStream = TokenSources.getAnyTokenStream(is.getIndexReader(), docId, "text", 
					new StandardAnalyzer(Version.LUCENE_30));
			String output;
			output = highlighter.getBestFragments(tokenStream, doc.get("text"), 3, "<br>");
			htmlWriter.println(output + "<br><br><br>");
		}
		htmlWriter.println("</font></body></html>");
		htmlWriter.close();
	}

	/**
	 * Spell check.
	 * 
	 * @param term the term
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void spellCheck(String term) throws IOException {
		Directory fsDir = FSDirectory.open(new File("berkeley-db/read-only/lucene"));
		IndexReader dictionaryReader = ParallelReader.open(fsDir);
		Directory spellDir = FSDirectory.open(new File("spellIndexDirectory"));
		SpellChecker spellchecker = new SpellChecker(spellDir);
		// To index a field of a user index:
		spellchecker.indexDictionary(new LuceneDictionary(dictionaryReader, "desc"));
		String[] suggestions = spellchecker.suggestSimilar(term, 5);
		for (String suggestion : suggestions) {
			System.out.println(suggestion);
		}
	}
}
