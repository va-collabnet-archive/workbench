/*
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
package org.ihtsdo.project.refset.partition;

import java.io.Serializable;
import java.util.regex.Pattern;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

/**
 * The Class StringMatchPartitioner.
 */
public class StringMatchPartitioner extends RefsetPartitioner implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The pattern. */
	String pattern;

	/**
	 * Instantiates a new string match partitioner.
	 */
	public StringMatchPartitioner() {
		super();
	}

	/**
	 * Instantiates a new string match partitioner.
	 *
	 * @param pattern the pattern
	 */
	public StringMatchPartitioner(String pattern) {
		super();
		this.pattern = pattern;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.partition.RefsetPartitioner#evaluateMember(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
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
//			AceLog.getAppLog().alertAndLogException(e);
//		}
		return result;
	}

	/**
	 * Test wildcard search.
	 */
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

	/**
	 * Wildcard to regex.
	 *
	 * @param wildcard the wildcard
	 * @return the string
	 */
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

	/**
	 * Gets the pattern.
	 *
	 * @return the pattern
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Sets the pattern.
	 *
	 * @param pattern the new pattern
	 */
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "String match";
	}

}
