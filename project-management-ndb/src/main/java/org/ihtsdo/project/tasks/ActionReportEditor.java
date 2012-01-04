package org.ihtsdo.project.tasks;

import org.dwfa.bpa.tasks.editor.AbstractComboEditor;
import org.ihtsdo.project.workflow.model.WfInstance;

public class ActionReportEditor extends AbstractComboEditor {

	@Override
	public EditorComponent setupEditor() {
	     return new EditorComponent(WfInstance.ActionReport.values());
		
	}

}
