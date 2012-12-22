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
public class MonthNumberSelectorEditor extends AbstractComboEditor {

	/* (non-Javadoc)
	 * @see org.dwfa.bpa.tasks.editor.AbstractComboEditor#setupEditor()
	 */
	public EditorComponent setupEditor() {
		List<Integer> months = new ArrayList<Integer>();
		months.add(1);
		months.add(2);
		months.add(3);
		months.add(4);
		months.add(5);
		months.add(6);
		months.add(7);
		months.add(8);
		months.add(9);
		months.add(10);
		months.add(11);
		months.add(12);
		return new EditorComponent(months.toArray());
	}
}
