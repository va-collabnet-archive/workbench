/**
 * 
 */
package org.ihtsdo.arena.conceptview;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.dwfa.ace.dnd.DragMonitor;

class DropPanelProxy implements PropertyChangeListener {
	WeakReference<I_DispatchDragStatus> dpmr;

	public DropPanelProxy(I_DispatchDragStatus dpm) {
		super();
		this.dpmr = new WeakReference<I_DispatchDragStatus>(dpm);
		DragMonitor.addDragListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		I_DispatchDragStatus dpm = dpmr.get();
		if (dpm == null) {
			DragMonitor.removeDragListener(this);
		} else {
			if (evt.getNewValue().equals(Boolean.TRUE)) {
				dpm.dragStarted();
			} else {
				dpm.dragFinished();
			}
		}
	}
}