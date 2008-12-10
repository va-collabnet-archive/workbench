package org.dwfa.bpa.tasks.editor;

public class PropertyNameLabelFrozenEditor extends PropertyNameLabelEditor {

	public PropertyNameLabelFrozenEditor(Object obj)
			throws ClassNotFoundException {
		super(obj);
		setFrozen(true);
	}

}
