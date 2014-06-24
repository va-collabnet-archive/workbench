/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
/*
 * Created on Mar 15, 2005
 */
package org.dwfa.bpa.util;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private final Collection<ListDataListener> frameListeners = Collections.synchronizedList(new ArrayList<ListDataListener>());

    private static class FrameComparator implements Comparator<JFrame> {

        @Override
        public int compare(JFrame f1, JFrame f2) {
            return f1.getTitle().compareTo(f2.getTitle());
        }
    }

    private static class MenuComparator implements Comparator<JMenuItem> {

        @Override
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
        for (JFrame f : singleton.frames) {
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
        if (frame.getName().equals("Phantom Frame") && singleton.frames.size() > 0) {
            // Don't add a second phantom frame...
        } else {
            singleton.frames.add(frame);
            frame.addPropertyChangeListener("title", singleton);
            ListDataEvent lde = new ListDataEvent(singleton, ListDataEvent.INTERVAL_ADDED, 0, getNumOfFrames());
            singleton.fireIntervalAdded(lde);
        }
    }

    public static synchronized void removeFrame(JFrame frame) {
        singleton.frames.remove(frame);
        ListDataEvent lde = new ListDataEvent(singleton, ListDataEvent.INTERVAL_REMOVED, 0, getNumOfFrames());
        singleton.fireIntervalRemoved(lde);
    }

    public static synchronized void addNewWindowMenuItemGenerator(I_InitComponentMenus generator) {
        singleton.newWindowMenuItemGenerators.add(generator);
        ListDataEvent lde = new ListDataEvent(singleton, ListDataEvent.CONTENTS_CHANGED, 0, getNumOfFrames());
        singleton.fireContentsChanged(lde);
    }

    /**
     * @param t
     * @param l
     */
    public static void addFrameListener(ListDataListener l) {
        if (PhantomFrame.class.isAssignableFrom(l.getClass()) && singleton.frameListeners.size() > 0) {
            // Don't add a second phantom frame...
        } else {
            singleton.frameListeners.add(l);
        }
    }

    /**
     * @param t
     * @param l
     */
    public static void removeFrameListener(ListDataListener l) {
        synchronized (singleton.frameListeners) {
            singleton.frameListeners.remove(l);
        }
    }

    // Notify all listeners that have registered interest for
    // notification on this event type. The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    protected void fireContentsChanged(ListDataEvent e) {
        synchronized (frameListeners) {
            for (ListDataListener l : frameListeners) {
                l.contentsChanged(e);
            }
        }
    }

    protected void fireIntervalAdded(ListDataEvent e) {
        synchronized (frameListeners) {
            for (ListDataListener l : frameListeners) {
                l.intervalAdded(e);
            }
        }
    }

    protected void fireIntervalRemoved(ListDataEvent e) {
        synchronized (frameListeners) {
            for (ListDataListener l : frameListeners) {
                l.intervalRemoved(e);
            }
        }
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent arg0) {
        ListDataEvent lde = new ListDataEvent(singleton, ListDataEvent.CONTENTS_CHANGED, 0, singleton.frames.size() - 1);
        this.fireContentsChanged(lde);

    }

    public static Collection<JMenuItem> getNewWindowMenuItems() {
        TreeSet<JMenuItem> items = new TreeSet<JMenuItem>(new MenuComparator());
        for (I_InitComponentMenus generator : singleton.newWindowMenuItemGenerators) {
            JMenuItem[] newMenuItems = generator.getNewWindowMenu();
            if (newMenuItems != null) {
                items.addAll(Arrays.asList(newMenuItems));
            }
        }
        return items;
    }

    public static Component getActiveFrame() {
        for (JFrame f : singleton.frames) {
            if (f.isActive()) {
                return f;
            }
        }
        return new JFrame();
    }
}
