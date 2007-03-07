package org.dwfa.maven;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http://java.sun.com/developer/technicalArticles/releases/1.4regex/
 * 
 * @author kec
 *
 */
public class RegexReplace {
	
	private Pattern pattern;
	String replacementStr;

	public RegexReplace(String patternStr, String replacementStr) {
		super();
		pattern = Pattern.compile(patternStr);
		this.replacementStr = replacementStr;
	}

	public String execute(CharSequence inputStr) {
        Matcher matcher = pattern.matcher(inputStr);
        return matcher.replaceAll(replacementStr);		
	}

}
