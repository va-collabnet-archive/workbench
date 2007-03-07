package org.dwfa.util.text;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class StringToWord {
	public static final String WORD_DELIMITERS = " \t\n\r\f,()[]\"'.-:?/{}|=&;<>";

	public static List<String> get(String s) {
		List<String> wordList = new ArrayList<String>();
		StringTokenizer st=new StringTokenizer(s, StringToWord.WORD_DELIMITERS);
		while(st.hasMoreTokens()){
			wordList.add(st.nextToken());
		}
		return wordList;
	}
}
