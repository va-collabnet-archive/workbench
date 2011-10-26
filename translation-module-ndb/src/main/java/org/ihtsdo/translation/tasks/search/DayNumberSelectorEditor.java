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
package org.ihtsdo.translation.tasks.search;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.bpa.tasks.editor.AbstractComboEditor;

/**
 * The Class LangCodeSelectorEditor.
 */
public class DayNumberSelectorEditor extends AbstractComboEditor {

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.editor.AbstractComboEditor#setupEditor()
	 */
	public EditorComponent setupEditor() {
		List<Integer> days = new ArrayList<Integer>();
		days.add(1);
		days.add(2);
		days.add(3);
		days.add(4);
		days.add(5);
		days.add(6);
		days.add(7);
		days.add(8);
		days.add(9);
		days.add(10);
		days.add(11);
		days.add(12);
		days.add(13);
		days.add(14);
		days.add(15);
		days.add(16);
		days.add(17);
		days.add(18);
		days.add(19);
		days.add(20);
		days.add(21);
		days.add(22);
		days.add(23);
		days.add(24);
		days.add(25);
		days.add(26);
		days.add(27);
		days.add(28);
		days.add(29);
		days.add(30);
		days.add(31);
		return new EditorComponent(days.toArray());
	}
}
