package org.dwfa.ace.dnd;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import sun.awt.dnd.SunDragSourceContextPeer;

public class DragMonitor implements AWTEventListener {
	
	
	
	private class DragEndMonitor implements ActionListener {

		
		public DragEndMonitor() {
			super();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (dragging) {
				try {
					SunDragSourceContextPeer.checkDragDropInProgress();
					dragging = false;
					endTimer.stop();
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							pcs.firePropertyChange("dragging", true, false);
						}
					});				
				} catch (InvalidDnDOperationException e1) {
					// Ignore... Drag is in progress;
				}
			} else {
				endTimer.stop();
			}
			
		}
		
	}

	int tests = 0;
	boolean dragging = false;
	private static long eventMask  = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;

	private static DragMonitor singleton  = new DragMonitor();
	private static PropertyChangeSupport pcs;
	private Timer endTimer = new Timer(1000, new DragEndMonitor());

	public static void addDragListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}


	public static void removeDragListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}


	public static void setup() {
		if (singleton == null) {
			singleton = new DragMonitor(); 
		}
	}
	
	
	public DragMonitor() {
		super();
		endTimer.stop();
		pcs = new PropertyChangeSupport(this);
		Toolkit.getDefaultToolkit().addAWTEventListener(this, eventMask);
	}


	@Override
	public void eventDispatched(AWTEvent event) {
		if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
			if (!dragging) {
				endTimer.start();
				dragging = true;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", false, true);
					}
				});
			}
		} else if (event.getID() == MouseEvent.MOUSE_RELEASED) {
			if (dragging) {
				endTimer.stop();
				dragging = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", true, false);
					}
				});
			}
		} else if (event.getID() == MouseEvent.MOUSE_CLICKED) {
			if (dragging) {
				endTimer.stop();
				dragging = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", true, false);
					}
				});
			}
		} else if (event.getID() == MouseEvent.MOUSE_MOVED) {
			if (dragging) {
				endTimer.stop();
				dragging = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", true, false);
					}
				});
			}
		} else if (event.getID() == MouseEvent.MOUSE_ENTERED) {
			if (dragging) {
				endTimer.stop();
				dragging = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", true, false);
					}
				});
			}
		} else {
			if (dragging) {
				endTimer.stop();
				dragging = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						pcs.firePropertyChange("dragging", true, false);
					}
				});
			}
		}
	}

}
