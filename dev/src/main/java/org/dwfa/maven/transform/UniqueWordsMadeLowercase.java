package org.dwfa.maven.transform;

import java.io.IOException;

public class UniqueWordsMadeLowercase extends ProcessWords {

	protected void processToken(String t) throws IOException {
		addWord(t.toLowerCase());
	}

}
