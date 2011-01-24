/**
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
package org.ihtsdo.project.panel;

import java.io.IOException;

import javax.swing.JPanel;

import org.dwfa.tapi.TerminologyException;

/**
 * A factory for creating PanelHelper objects.
 */
public class PanelHelperFactory {

	/** The transl helper panel. */
	private static TranslationHelperPanel translHelperPanel;
	
	/**
	 * Gets the translation helper panel.
	 * 
	 * @return the translation helper panel
	 * 
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static TranslationHelperPanel getTranslationHelperPanel() throws TerminologyException, IOException{
		if (translHelperPanel == null)	
			translHelperPanel=new TranslationHelperPanel();
			
		return translHelperPanel;
	}
}
