/*
 * Created on Mar 15, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * @author kec
 *  
 */
public class OpenFrames implements PropertyChangeListener {

    private static OpenFrames singleton = new OpenFrames();
    
	private Collection<JFrame> frames;
    
    private Collection<I_InitComponentMenus> newWindowMenuItemGenerators;

	private Collection<ListDataListener> frameListeners = new ArrayList<ListDataListener>();
    
    private static class FrameComparator implements Comparator<JFrame> {
        
        public int compare(JFrame f1, JFrame f2) {
            return f1.getTitle().compareTo(f2.getTitle());
        }
    }
    private static class MenuComparator implements Comparator<JMenuItem> {
        
        public int compare(JMenuItem m1, JMenuItem m2) {
            return m1.getText().compareTo(m2.getText());
        }
    }

	/**
	 * @param sourceBean
	 */
	private OpenFrames() {
		this.frames = new HashSet<JFrame>();
        this.newWindowMenuItemGenerators = new HashSet<I_InitComponentMenus>();
	}

    public static int getNumOfFrames() {
      int shownFrames = 0;
      for (JFrame f: singleton.frames) {
        if (f.isVisible()) {
          shownFrames++;
        }
      }
      return shownFrames;
    }
    public static Collection<JFrame> getFrames() {
        TreeSet<JFrame> sortedFrames = new TreeSet<JFrame>(new FrameComparator());
        sortedFrames.addAll(singleton.frames);
        return sortedFrames;
    }

	public static synchronized void addFrame(JFrame frame) {
        singleton.frames.add(frame);
        frame.addPropertyChangeListener("title", singleton);
        ListDataEvent lde = new ListDataEvent(singleton,  ListDataEvent.INTERVAL_ADDED,  0,  getNumOfFrames());
        singleton.fireIntervalAdded(lde);
    }

	public static synchronized void removeFrame(JFrame frame) {
        singleton.frames.remove(frame);
        ListDataEvent lde = new ListDataEvent(singleton,  ListDataEvent.INTERVAL_REMOVED,  0,  getNumOfFrames());
        singleton.fireIntervalRemoved(lde);
	}
    
    public static synchronized void addNewWindowMenuItemGenerator(I_InitComponentMenus generator) {
        singleton.newWindowMenuItemGenerators.add(generator);       
        ListDataEvent lde = new ListDataEvent(singleton,  ListDataEvent.CONTENTS_CHANGED,  0,  getNumOfFrames());
        singleton.fireContentsChanged(lde);
    }

	/**
	 * @param t
	 * @param l
	 */
	public static void addFrameListener(ListDataListener l) {
        singleton.frameListeners.add(l);
	}

	/**
	 * @param t
	 * @param l
	 */
	public static void removeFrameListener(ListDataListener l) {
        singleton.frameListeners.remove(l);
	}

	//   Notify all listeners that have registered interest for
	// notification on this event type. The event instance
	// is lazily created using the parameters passed into
	// the fire method.

	protected void fireContentsChanged(ListDataEvent e) {
        for (ListDataListener l: frameListeners) {
            l.contentsChanged(e);
        }
	}
    
    protected void fireIntervalAdded(ListDataEvent e) {
        for (ListDataListener l: frameListeners) {
            l.intervalAdded(e);
        }
    }
    
    protected void fireIntervalRemoved(ListDataEvent e) {
        for (ListDataListener l: frameListeners) {
            l.intervalRemoved(e);
        }
    }


	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent arg0) {
        ListDataEvent lde = new ListDataEvent(singleton,  ListDataEvent.CONTENTS_CHANGED,  0,  singleton.frames.size() -1);
		this.fireContentsChanged(lde);
		
	}


    public static Collection<JMenuItem> getNewWindowMenuItems() { 
        TreeSet<JMenuItem> items = new TreeSet<JMenuItem>(new MenuComparator());
        for (I_InitComponentMenus generator: singleton.newWindowMenuItemGenerators) {
            JMenuItem item = generator.getNewWindowMenu();
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }
    
    
}