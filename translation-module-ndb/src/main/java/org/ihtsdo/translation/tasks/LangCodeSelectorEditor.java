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
package org.ihtsdo.translation.tasks;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;

/**
 * The Class LangCodeSelectorEditor.
 */
public class LangCodeSelectorEditor extends AbstractComboEditor {

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.editor.AbstractComboEditor#setupEditor()
	 */
	public EditorComponent setupEditor() {
		I_ConfigAceFrame config = ACE.getAceConfig().getAceFrames().iterator().next();
		List<String> langCodes = new ArrayList<String>();
		langCodes.add("en");
		langCodes.add("en-US");
		langCodes.add("en-GB");
		langCodes.add("es");
		langCodes.add("sv");
		langCodes.add("sv-SE");
		langCodes.add("da-DK");
		langCodes.add("fr-CA");
		return new EditorComponent(langCodes.toArray());
	}
}
