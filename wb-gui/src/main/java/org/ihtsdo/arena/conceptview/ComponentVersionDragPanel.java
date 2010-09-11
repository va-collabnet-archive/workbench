package org.ihtsdo.arena.conceptview;

import java.awt.LayoutManager;

import org.ihtsdo.tk.api.ComponentVersionBI;

public abstract class ComponentVersionDragPanel<T extends ComponentVersionBI> extends DragPanel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ComponentVersionDragPanel(ConceptViewSettings settings) {
		super(settings);
	}

	public ComponentVersionDragPanel(LayoutManager layout,
			ConceptViewSettings settings) {
		super(layout, settings);
	}

	@Override
	public String getUserString(T obj) {
		return obj.toUserString();
	}
	
}
