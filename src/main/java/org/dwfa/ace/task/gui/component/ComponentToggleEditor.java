package org.dwfa.ace.task.gui.component;

import org.dwfa.ace.api.I_HostConceptPlugins.TOGGLES;
import org.dwfa.bpa.tasks.editor.AbstractComboEditor;

public class ComponentToggleEditor extends AbstractComboEditor {

	@Override
	public EditorComponent setupEditor() {
		return new EditorComponent(TOGGLES.values() );
	}
	
}