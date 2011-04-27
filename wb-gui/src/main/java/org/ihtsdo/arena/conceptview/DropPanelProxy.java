/**
 * 
 */
package org.ihtsdo.arena.conceptview;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import org.dwfa.ace.dnd.DragMonitor;
import org.dwfa.ace.log.AceLog;

class DropPanelProxy implements PropertyChangeListener, HierarchyListener {

    WeakReference<I_DispatchDragStatus> dpmr;

    public DropPanelProxy(I_DispatchDragStatus dpm) {
        super();
        this.dpmr = new WeakReference<I_DispatchDragStatus>(dpm);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        I_DispatchDragStatus dpm = dpmr.get();
        if (dpm == null) {
            DragMonitor.removeDragListener(this);
        } else {
            if (evt.getNewValue().equals(Boolean.TRUE)) {
                try {
                    dpm.dragStarted();
                } catch (Exception e) {
                    DragMonitor.removeDragListener(this);
                    AceLog.getAppLog().warning("suppressed drag start: " + e.toString());
                }
            } else {
                dpm.dragFinished();
            }
        }
    }

    @Override
    public void hierarchyChanged(HierarchyEvent e) {
        long flags = e.getChangeFlags();
        boolean displayability = (flags & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
        boolean showing = (flags & HierarchyEvent.SHOWING_CHANGED) != 0;
        if (displayability || showing) {
            if (e.getChanged().isDisplayable()) {
                DragMonitor.addDragListener(this);
            } else {
                 DragMonitor.removeDragListener(this);
            }
        }
    }
}