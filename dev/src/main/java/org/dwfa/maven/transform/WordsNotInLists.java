package org.dwfa.maven.transform;

import java.io.IOException;

public class WordsNotInLists extends WordsInLists {


	
	protected void processToken(String t) throws IOException {
		if (listWords.contains(t) == false) {
			addWord(t);
		}

	}


}
