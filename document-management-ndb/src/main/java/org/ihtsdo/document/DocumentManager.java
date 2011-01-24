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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

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
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The Class DocumentManager.
 */
public class DocumentManager {

	/**
	 * Index documents.
	 * 
	 * @return the string
	 */
	public static String indexDocuments() {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		try {
			File directory = new File("documents");
			File indexDir = new File("documentsIndex");
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);
			File files[] = directory.listFiles();
			int counter = 0;
			for (File f : files) {
				counter++;
				try {
					ContentHandler textHandler = new BodyContentHandler();
					if (f.getName().endsWith(".pdf")) {
						FileInputStream fi = new FileInputStream(f);
						Metadata metadata = new Metadata();
						ParseContext context = new ParseContext();
						org.apache.tika.parser.pdf.PDFParser parser = new  org.apache.tika.parser.pdf.PDFParser();
						parser.parse(fi, textHandler, metadata, context);
						fi.close();
					} else if (f.getName().endsWith(".doc") || f.getName().endsWith(".xls")) {
						FileInputStream fi = new FileInputStream(f);
						Metadata metadata = new Metadata();
						ParseContext context = new ParseContext();
						org.apache.tika.parser.microsoft.OfficeParser parser = new  org.apache.tika.parser.microsoft.OfficeParser();
						parser.parse(fi, textHandler, metadata, context);
						fi.close();
					} else if (f.getName().endsWith(".docx") || f.getName().endsWith(".xlsx")) {
						FileInputStream fi = new FileInputStream(f);
						Metadata metadata = new Metadata();
						ParseContext context = new ParseContext();
						org.apache.tika.parser.microsoft.ooxml.OOXMLParser parser = new  org.apache.tika.parser.microsoft.ooxml.OOXMLParser();
						parser.parse(fi, textHandler, metadata, context);
						fi.close();
					} else {
						throw new IOException();
					}
					Document doc = new Document();
					doc.add(new Field("path", f.getPath(),Field.Store.YES, Field.Index.ANALYZED));
					doc.add(new Field("text",textHandler.toString(),Field.Store.YES, Field.Index.ANALYZED));
					writer.addDocument(doc);
					output = output + counter + ") Indexing:" + doc.get("path") + " Size:" + doc.get("text").length() + "<br>";
				} catch (IOException e) {
					e.printStackTrace();
					output = output + counter + ") Skipping " + f.getName() + "<br>";
				} catch (SAXException e) {
					e.printStackTrace();
					output = output + counter + ") Skipping " + f.getName() + "<br>";
				} catch (TikaException e) {
					e.printStackTrace();
					output = output + counter + ") Skipping " + f.getName() + "<br>";
				}
			}
			writer.optimize();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		output = output + "</font></body></html>";
		return output;
	}

	/**
	 * Search documents.
	 * 
	 * @param q the q
	 * 
	 * @return the string
	 */
	public static String searchDocuments(String q) {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		try {
			Directory fsDir = FSDirectory.open(new File("documentsIndex"));
			IndexSearcher is = new IndexSearcher(fsDir);

			QueryParser qp = new QueryParser(Version.LUCENE_30, "text", 
					new StandardAnalyzer(Version.LUCENE_30));
			Query query = qp.parse(q);

			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter(
					"<font style='BACKGROUND-COLOR: yellow'>", 
					"</font>");
			Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));

			long start = new Date().getTime();
			TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
			is.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			long end = new Date().getTime();
			output = output + "Found " + hits.length + " document(s) (in " + (end - start) +
			" milliseconds) that matched query '" + q + "':<br><br>";

			for (int i = 0; i < hits.length; i++) {
				int docId = hits[i].doc;
				Document doc = is.doc(docId);
				output = output + (i+1) + ") <a href=\"file://" + doc.get("path").trim() + "\">"+ doc.get("path") + "</a><br>";
				TokenStream tokenStream = TokenSources.getAnyTokenStream(is.getIndexReader(), docId, "text", 
						new StandardAnalyzer(Version.LUCENE_30));
				String frags = highlighter.getBestFragments(tokenStream, doc.get("text"), 3, "<br>");
				output = output + frags + "<br><br><br>";
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} 
		output = output + "</font></body></html>";
		//System.out.println(output);
		return output;
	}

	/**
	 * Index memory from xls.
	 * 
	 * @return the string
	 * @throws IOException 
	 */
	public static String indexMemoryFromXls() throws OfficeXmlFileException, IOException {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		File excelFile = null;
		JFileChooser fileopen = new JFileChooser();
		int ret = fileopen.showDialog(null, "Open file");

		if (ret == JFileChooser.APPROVE_OPTION) {
			excelFile = fileopen.getSelectedFile();
		}

		if (excelFile != null) {
			InputStream inp = new FileInputStream(excelFile);
			
			Workbook wb =  null;
			
			if (excelFile.getName().endsWith(".xls")){
				wb = new HSSFWorkbook(inp);
			} else if (excelFile.getName().endsWith(".xlsx")){
				wb = new XSSFWorkbook(inp);
			} else throw new IOException("Excel format not recognized");
			
			Sheet sheet = wb.getSheetAt(0);
			Row row = sheet.getRow(0);
			int sourceColumn = 0;
			int translationColumn = 1;
			int firstDataRow = 0;
			if (row != null) {
				for (Iterator<Cell> cells = row.cellIterator(); cells.hasNext();) { 
					Cell loopCell = cells.next();
					if (loopCell.getStringCellValue().equals("Source text")) {
						sourceColumn = loopCell.getColumnIndex();
						firstDataRow = 1;
					}
					if (loopCell.getStringCellValue().equals("Translation"))  {
						translationColumn = loopCell.getColumnIndex();
						firstDataRow = 1;
					}
					//System.out.println("Celda: " + loopCell.getColumnIndex() + " Valor: " + loopCell.getStringCellValue());
				}
			}

			File indexDir = new File("translationMemory");
			IndexWriter writer = new IndexWriter(FSDirectory.open(indexDir), 
					new StandardAnalyzer(Version.LUCENE_30), true, IndexWriter.MaxFieldLength.UNLIMITED);

			for (int i = firstDataRow ; i <= sheet.getLastRowNum() ; i++) {
				Row loopRow = sheet.getRow(i);
				//System.out.println("Row: " + i + " Values: " + loopRow.getCell(sourceColumn).getStringCellValue() + " | " +
				//		loopRow.getCell(translationColumn).getStringCellValue());

				if (loopRow.getCell(sourceColumn) != null & loopRow.getCell(translationColumn) != null) {
					String sourceText = loopRow.getCell(sourceColumn).getStringCellValue();
					String targetText = loopRow.getCell(translationColumn).getStringCellValue();
					Document doc = new Document();
					output = output + "Row: " + i + " Source text: " + sourceText + " Translation Text: " +	targetText + "<br>";
					doc.add(new Field("source", sourceText.toLowerCase().trim(), Field.Store.YES, Field.Index.ANALYZED));
					doc.add(new Field("translation", targetText.toLowerCase().trim(), Field.Store.YES, Field.Index.ANALYZED));
					writer.addDocument(doc);
				}
			}

			writer.optimize();
			writer.close();

			output = output + "<br><br>Done!<br>";
		}
		output = output + "</font></body></html>";
		return output;
	}

	/**
	 * Match translation memory.
	 * 
	 * @param term the term
	 * 
	 * @return the hash map< string, string>
	 */
	public static HashMap<String,String> matchTranslationMemory(String term) {
		term = term.toLowerCase().trim();
		if (term.contains("(")) {
			term = term.substring(0, term.indexOf("(")-1).trim();
		}
		HashMap<String,String> results = new HashMap<String,String>();

		try {
			Directory fsDir = FSDirectory.open(new File("translationMemory"));
			IndexSearcher is = new IndexSearcher(fsDir);

			if (term.equals("*")) {
				for (int i = 0 ; i<is.maxDoc() ; i++ ) {
					results.put(is.doc(i).get("source"), is.doc(i).get("translation"));
				}

			} else {

				QueryParser qp = new QueryParser(Version.LUCENE_30, "source", 
						new StandardAnalyzer(Version.LUCENE_30));
				Query query = qp.parse(term);

				//long start = new Date().getTime();
				TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
				is.search(query, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;
				//long end = new Date().getTime();
				//System.out.println("Found " + hits.length + " document(s) (in " + (end - start) +
				//		" milliseconds) that matched query '" + term + "'");

				for (int i = 0; i < hits.length; i++) {
					int docId = hits[i].doc;
					Document doc = is.doc(docId);
					results.put(doc.get("source"), doc.get("translation"));
					//System.out.println(doc.get("source") +" | " + doc.get("translation"));
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * Index dictionary from workbench database.
	 * 
	 * @param overwrite the overwrite
	 * 
	 * @return the string
	 */
	public static String indexDictionaryFromWorkbenchDatabase(boolean overwrite) {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		try {
			if (overwrite) {
				output = output + "Deleting dictionary...<br>";
				File dictionaryFolder = new File("spellIndexDirectory");
				if (!deleteDirectory(dictionaryFolder) && dictionaryFolder.exists()) {
					output = output + "Can't delete previous dictionary, adding new content...<br><br>";
				}
			}
			Directory fsDir = FSDirectory.open(new File("berkeley-db/read-only/lucene"));
			IndexReader dictionaryReader = ParallelReader.open(fsDir);
			Directory spellDir = FSDirectory.open(new File("spellIndexDirectory"));
			SpellChecker spellchecker = new SpellChecker(spellDir);
			spellchecker.indexDictionary(new LuceneDictionary(dictionaryReader, "desc"));
			fsDir.close();
			spellDir.close();
			output = output + "<br><br>Done!<br>";
		} catch (IOException e) {
			e.printStackTrace();
		}
		output = output + "</font></body></html>";
		return output;
	}

	/**
	 * Index dictionary from text file.
	 * 
	 * @param overwrite the overwrite
	 * 
	 * @return the string
	 */
	public static String indexDictionaryFromTextFile(boolean overwrite, String langCode) {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		try {
			File dictionaryTextFile = null;
			JFileChooser fileopen = new JFileChooser();
			int ret = fileopen.showDialog(null, "Open file");

			if (ret == JFileChooser.APPROVE_OPTION) {
				dictionaryTextFile = fileopen.getSelectedFile();
			}
			File dictionaryFolder = new File("spellIndexDirectory/" + langCode);
			if (overwrite) {
				output = output + "Deleting dictionary...<br>";
				if (!deleteDirectory(dictionaryFolder) && dictionaryFolder.exists()) {
					output = output + "Can't delete previous dictionary, adding new content...<br><br>";
				}
			}
			Directory spellDir = FSDirectory.open(new File(dictionaryFolder.getPath()));
			SpellChecker spellchecker = new SpellChecker(spellDir);
			// To index a file containing words:
			spellchecker.indexDictionary(new PlainTextDictionary( new InputStreamReader(new FileInputStream(dictionaryTextFile), "UTF-8")));
			spellDir.close();
			output = output + "<br><br>Done!<br>";
		} catch (IOException e) {
			e.printStackTrace();
		}
		output = output + "</font></body></html>";
		return output;
	}

	/**
	 * Adds the to dictionary.
	 * 
	 * @param word the word
	 * 
	 * @return the string
	 */
	public static String addToDictionary(String word, String langCode) {
		String output = "<html><body><font style='font-family:arial,sans-serif'>";
		try {
			Directory spellDir = FSDirectory.open(new File("spellIndexDirectory/" + langCode));
			SpellChecker spellchecker = new SpellChecker(spellDir);
			// To index a file containing words:
			spellchecker.indexDictionary(new PlainTextDictionary(new StringReader(word)));
			spellDir.close();
			output = output + "<br><br>Done!<br>";
		} catch (IOException e) {
			e.printStackTrace();
		}
		output = output + "</font></body></html>";
		return output;
	}

	public static void addToNewWordsfile(String word, String langCode){
		File file=new File("dictionaries/NewWords_" + langCode + ".txt");
		
	}
	/**
	 * Exists in dictionary.
	 * 
	 * @param word the word
	 * 
	 * @return true, if successful
	 */
	public static boolean existsInDictionary(String word,String langCode) {
		try {
			Directory spellDir = FSDirectory.open(new File("spellIndexDirectory/" + langCode));
			SpellChecker spellchecker = new SpellChecker(spellDir);
			boolean result = spellchecker.exist(word);
			spellDir.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Gets the sugestions from dictionary.
	 * 
	 * @param word the word
	 * 
	 * @return the sugestions from dictionary
	 */
	public static String[] getSugestionsFromDictionary(String word, String langCode) {
		try {
			Directory spellDir = FSDirectory.open(new File("spellIndexDirectory/" + langCode));
			SpellChecker spellchecker = new SpellChecker(spellDir);
			String[] results = spellchecker.suggestSimilar(word, 5);
			spellDir.close();
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Spellcheck phrase.
	 * 
	 * @param phrase the phrase
	 * @param frame the frame
	 * 
	 * @return the string
	 * @throws TerminologyException 
	 * @throws IOException 
	 * @throws NoSuchElementException 
	 */
	static public String spellcheckPhrase(String phrase, JFrame frame, String langCode)  {
		String[] words = phrase.split("[\\s+\\p{Punct}]");
		String modifiedPhrase = phrase;
		String parsedLangCode=null;
		try {
			parsedLangCode = ArchitectonicAuxiliary.getLanguageCode(ArchitectonicAuxiliary.getLanguageConcept(langCode).getUids());
		} catch (NoSuchElementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (parsedLangCode!=null){
			DictionaryResultsDialog spellCheckDialog = new DictionaryResultsDialog(frame, null, parsedLangCode);
			spellCheckDialog.pack();
			for (int i = 0; i < words.length; i++) {
				//&&!words[i].matches("[0-9\\.]*") // detect numeric
				String wordToCheck = words[i];
				//String wordToCheck = words[i].replaceAll("[^a-zA-Z0-9‡Ž’—œŸ–çƒêîò†„]", "");
				if (wordToCheck.length() > 2 && !DocumentManager.existsInDictionary(wordToCheck,parsedLangCode) &&!words[i].matches("[0-9\\.]*")) {
					spellCheckDialog.setLocationRelativeTo(frame);
					spellCheckDialog.queryField.setText(wordToCheck);
					spellCheckDialog.update(wordToCheck);
					spellCheckDialog.setVisible(true);
					String s = spellCheckDialog.getValidatedWord();
					//if (s!=null) words[i] = s;
					if (s!=null) {
						modifiedPhrase = modifiedPhrase.replace(wordToCheck, s);
					}
				}
			}
			spellCheckDialog.dispose();
		}

		/*String modifiedPhrase = "";

		for (int i = 0; i < words.length; i++) {
			modifiedPhrase = modifiedPhrase + " " + words[i];
		}*/


		return modifiedPhrase.trim();
	}

	/**
	 * Delete directory.
	 * 
	 * @param path the path
	 * 
	 * @return true, if successful
	 */
	static public boolean deleteDirectory(File path) {
		if( path.exists() ) {
			File[] files = path.listFiles();
			for(int i=0; i<files.length; i++) {
				if(files[i].isDirectory()) {
					deleteDirectory(files[i]);
				}
				else {
					files[i].delete();
				}
			}
		}
		return( path.delete() );
	}

	/**
	 * Gets the descendants.
	 * 
	 * @param descendants the descendants
	 * @param concept the concept
	 * 
	 * @return the descendants
	 */
	public static Set<I_GetConceptData> getDescendants(Set<I_GetConceptData> descendants, I_GetConceptData concept) {
		try {
			I_TermFactory termFactory = LocalVersionedTerminology.get();
			//TODO: get config as parameter
			I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
			I_IntSet allowedDestRelTypes =  termFactory.newIntSet();
			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			Set<I_GetConceptData> childrenSet = new HashSet<I_GetConceptData>();
			childrenSet.addAll(concept.getDestRelOrigins(config.getAllowedStatus(), allowedDestRelTypes, config.getViewPositionSetReadOnly()
					, config.getPrecedence(), config.getConflictResolutionStrategy()));
			descendants.addAll(childrenSet);
			for (I_GetConceptData loopConcept : childrenSet) {
				descendants = getDescendants(descendants, loopConcept);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TerminologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return descendants;
	}

	public static void addNewLinguisticGuideline(String name, String pattern, String recommendation, 
			I_GetConceptData infoRoot, I_ConfigAceFrame config) throws TerminologyException, IOException {
		I_TermFactory termFactory = Terms.get();
		termFactory.setActiveAceFrameConfig(config);

		I_GetConceptData newConcept = termFactory.newConcept(UUID.randomUUID(), false, config);

		termFactory.newDescription(UUID.randomUUID(), newConcept, "en", name + " (linguistic guideline)",
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()),
				config);

		termFactory.newDescription(UUID.randomUUID(), newConcept, "en", pattern,
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()), 
				config);

		termFactory.newDescription(UUID.randomUUID(), newConcept, "en", recommendation,
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.getUids()), 
				config);

		termFactory.newRelationship(UUID.randomUUID(), newConcept, 
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()), 
				infoRoot, 
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()), 
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()),
				termFactory.getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()), 
				0, config);
	}


	/**
	 * Gets the info for term.
	 * 
	 * @param term the term
	 * @param config the config
	 * 
	 * @return the info for term
	 */
	public static String getInfoForTerm(String term, I_ConfigAceFrame config){
		I_TermFactory tf = Terms.get();
		String selectedInfo = "";
		if (term != null) {
			try {
				config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
				config.getDescTypes().add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid());
				I_GetConceptData semtagsRoot = tf.getConcept(new UUID[] {UUID.fromString("9d3036a8-2c4b-3576-bbb4-a531370552c1")});
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, semtagsRoot);
				HashMap<String, String> infoMap = new HashMap<String, String>();
				for (I_GetConceptData infoConcept : descendants) {
					String key = "";
					String info = "";
					for (I_DescriptionTuple tuple : infoConcept.getDescriptionTuples(config.getAllowedStatus(), 
							config.getDescTypes(), 
							config.getViewPositionSetReadOnly(), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid() &&
								tuple.getLang().equals("en")) {
							key = tuple.getText();
						}
						if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid() &&
								tuple.getLang().equals("en")) {
							info = tuple.getText();
						}
					}
					infoMap.put(key.toLowerCase().trim(), info.trim());
				}

				for (String key : infoMap.keySet()) {
					String keyPattern = key.trim().replace("*", ".+");
					if (term.toLowerCase().trim().matches(keyPattern)) {
						selectedInfo = infoMap.get(key);
					}
				}
			} catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return selectedInfo;
	}

	public static String getInfoForTerm(String term, I_GetConceptData infoRoot, I_ConfigAceFrame config){
		String selectedInfo = "";
		if (term != null) {
			try {
				config.getDescTypes().add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
				config.getDescTypes().add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid());
				Set<I_GetConceptData> descendants = new HashSet<I_GetConceptData>();
				descendants = getDescendants(descendants, infoRoot);
				HashMap<String, String> infoMap = new HashMap<String, String>();
				for (I_GetConceptData infoConcept : descendants) {
					String key = "";
					String info = "";
					for (I_DescriptionTuple tuple : infoConcept.getDescriptionTuples(config.getAllowedStatus(), 
							config.getDescTypes(), 
							config.getViewPositionSetReadOnly(), config.getPrecedence(),
							config.getConflictResolutionStrategy())) {
						if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid() &&
								tuple.getLang().equals("en")) {
							key = tuple.getText();
						}
						if (tuple.getTypeId() == ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid() &&
								tuple.getLang().equals("en")) {
							info = tuple.getText();
						}
					}
					infoMap.put(key.toLowerCase().trim(), info.trim());
				}

				for (String key : infoMap.keySet()) {
					if (Pattern.matches(wildcardToRegex(key), term.toLowerCase().trim())) {
						selectedInfo = infoMap.get(key);
					}
				}
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return selectedInfo;
	}

	public static String wildcardToRegex(String wildcard){
		StringBuffer s = new StringBuffer(wildcard.length());
		s.append('^');
		for (int i = 0, is = wildcard.length(); i < is; i++) {
			char c = wildcard.charAt(i);
			switch(c) {
			case '*':
				s.append(".*");
				break;
			case '?':
				s.append(".");
				break;
				// escape special regexp-characters
			case '(': case ')': case '[': case ']': case '$':
			case '^': case '.': case '{': case '}': case '|':
			case '\\':
				s.append("\\");
				s.append(c);
				break;
			default:
				s.append(c);
			break;
			}
		}
		s.append('$');
		return(s.toString());
	}

}
