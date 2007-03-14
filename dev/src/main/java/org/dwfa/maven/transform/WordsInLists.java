package org.dwfa.maven.transform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dwfa.maven.Transform;
import org.dwfa.util.io.FileIO;

public class WordsInLists extends ProcessWords {

	private String[] wordListFiles;
	
	private Map processed;
	
	private List sourceRoots;
	
	
	
	protected Set listWords = new HashSet();
	
	
	public void init(Writer w, Transform transform) throws Exception {
		this.sourceRoots = transform.getSourceRoots();
		processed = new HashMap(wordListFiles.length);
		for (String s: wordListFiles) {
			processed.put(s, false);
		}
		for (Object root: sourceRoots) {
			String rootStr = (String) root;
			int loc = rootStr.indexOf("src" + File.separator + "main" + File.separator);
			rootStr = rootStr.substring(0, loc);
			System.out.println(" rootStr: " + rootStr);
			
			
			for (String wordListFileStr: wordListFiles) {
				File wordListFile = new File(rootStr, wordListFileStr);
				System.out.println(" wordListFile: " + wordListFile);
				if (wordListFile.exists()) {
					processed.put(wordListFileStr, true);
					BufferedReader br = new BufferedReader(new FileReader(wordListFile));
					String input = FileIO.readerToString(br);
					StringTokenizer st = new StringTokenizer(input);
					while (st.hasMoreTokens()) {
						String t=st.nextToken();
						listWords.add(t);
					}
				}
			}
		}
		super.init(w, transform);
		for (Object object: processed.entrySet()) {
			Map.Entry entry = (Map.Entry) object;
			if (((Boolean) entry.getValue()) != true) {
				throw new Exception(entry.getKey() + " not processed");
			}
		}
	}

	
	protected void processToken(String t) throws IOException {
		if (listWords.contains(t)) {
			addWord(t);
		}
	}

	public String[] getWordListFiles() {
		return wordListFiles;
	}

	public void setWordListFiles(String[] wordListFiles) {
		this.wordListFiles = wordListFiles;
	}

}
