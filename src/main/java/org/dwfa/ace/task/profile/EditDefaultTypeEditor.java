package org.dwfa.ace.task.profile;

import org.dwfa.bpa.tasks.editor.AbstractComboEditor;

public class EditDefaultTypeEditor extends AbstractComboEditor {

	@Override
	public EditorComponent setupEditor() {
		return new EditorComponent(EditDefaultTypes.values() );
	}
	
}