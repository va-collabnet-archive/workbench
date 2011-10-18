package org.ihtsdo.project.refset.partition;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;

public class StringMatchPartitioner extends RefsetPartitioner implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String pattern;

	public StringMatchPartitioner() {
		super();
	}

	public StringMatchPartitioner(String pattern) {
		super();
		this.pattern = pattern;
	}

	protected boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config) {
		boolean result = false;
		
		// test I_GetConceptData.toString();
		if (Pattern.matches(wildcardToRegex(pattern.toLowerCase()), member.toString().toLowerCase())) {
			result = true;
		}
		
		// test any description;
//		try {
//			I_IntSet localDescTypes = null;
//			if (config.getDescTypes().getSetValues().length > 0) {
//				localDescTypes = config.getDescTypes();
//			}
//			List<? extends I_DescriptionTuple> descriptions = member.getDescriptionTuples(config.getAllowedStatus(), 
//					localDescTypes, 
//					config.getViewPositionSetReadOnly(),true);
//			for (I_DescriptionTuple description : descriptions) {
//				if (Pattern.matches(wildcardToRegex(pattern.toLowerCase()), description.getText().toLowerCase())) {
//					result = true;
//				}
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return result;
	}

	public void testWildcardSearch() {
		String test = "123ABC";
		System.out.println(test);
		System.out.println(Pattern.matches(wildcardToRegex("1*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("?2*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("??2*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("*A*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("*Z*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("123*"), test));
		System.out.println(Pattern.matches(wildcardToRegex("123"), test));
		System.out.println(Pattern.matches(wildcardToRegex("*ABC"), test));
		System.out.println(Pattern.matches(wildcardToRegex("*abc"), test));
		System.out.println(Pattern.matches(wildcardToRegex("ABC*"), test));
		/*
	           output :
	           123ABC
	            true
	            true
	            false
	            true
	            false
	            true
	            false
	            true
	            false
	            false
		 */

	}

	public String wildcardToRegex(String wildcard){
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

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String toString() {
		return "String match";
	}

}
