package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class TimeStringtoXMLDateTime extends AbstractTransform implements I_ReadAndTransform {

	public void setupImpl(Transform transformer) {
		// Nothing to setup
	}

	public String transform(String input) throws Exception {

		/*
		   & - &amp;
		   < - &lt;
   		   > - &gt;
   		   " - &quot;
   		   ' - &#39; 
		*/
		
		String newstring = input.substring(0,4)+"-"+input.substring(4,6)+"-"+input.substring(6,8)+"T"+input.substring(9,input.length());

		return setLastTransform(newstring);
	}

}
