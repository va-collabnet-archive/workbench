package org.dwfa.maven.transform;


public class UniqueWords extends ProcessWords {
	
	protected void processToken(String t) {
		addWord(t);
	}


}
