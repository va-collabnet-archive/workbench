package org.ihtsdo.rules.tasks;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringEscapeUtils;

public class test {

	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException {
		String p = "prueba";
		System.out.println(p + " - " + p.length() + " - "  + p.getBytes("UTF-8").length);
		p = "uñas";
		System.out.println(p + " - " + p.length() + " - "  + p.getBytes("UTF-8").length);
		p = "aorásfasaçdasdaódafsdfsú";
		System.out.println(p + " - " + p.length() + " - "  + p.getBytes("UTF-8").length);
		p = "asmático";
		System.out.println(p + " - " + p.length() + " - "  + p.getBytes("UTF-8").length);
//		p = "&#169; 2002-2012 International Health Terminology Standards Development Organisation (IHTSDO). All rights reserved. SNOMED CT&#174;, was originally created by The College of American Pathologists. &quot;SNOMED&quot; and &quot;SNOMED CT&quot; are registered trademarks of the IHTSDO.";
//		System.out.println(p + " - " + p.length() + " - "  + p.getBytes("UTF-8").length);
		
		String pre= "&#169; 2002-2012 International Health Terminology Standards Development Organisation (IHTSDO). All rights reserved. SNOMED CT&#174;, was originally created by The College of American Pathologists. &quot;SNOMED&quot; and &quot;SNOMED CT&quot; are registered trademarks of the IHTSDO.";
		String post =  StringEscapeUtils.unescapeHtml(pre);
		System.out.println("pre:" + pre);
		System.out.println("post:" + post);
		
	}

}
