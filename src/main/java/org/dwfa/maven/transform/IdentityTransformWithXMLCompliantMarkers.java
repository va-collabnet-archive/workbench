package org.dwfa.maven.transform;

import org.dwfa.maven.I_ReadAndTransform;
import org.dwfa.maven.Transform;

public class IdentityTransformWithXMLCompliantMarkers extends AbstractTransform implements I_ReadAndTransform {

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
		
		input = input.replaceAll("&","&amp;");
		input = input.replaceAll("<","&lt;");
		input = input.replaceAll(">","&gt;");
		input = input.replaceAll("\"","&quot;");

		return setLastTransform(input);
	}

}
