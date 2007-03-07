/*
 * Created on Apr 29, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.event.ListDataListener;



public class OpenFramesWindowListener implements WindowListener {
    JFrame frame;
    ListDataListener frameListener;
    /**
     * @param frame
     */
    public OpenFramesWindowListener(JFrame frame, ListDataListener frameListener) {
        super();
        this.frame = frame;
        this.frameListener = frameListener;
    }
    public void windowOpened(WindowEvent arg0) {
        
    }

    public void windowClosing(WindowEvent arg0) {
        OpenFrames.removeFrame(frame);
        OpenFrames.removeFrameListener(frameListener);
    }

    public void windowClosed(WindowEvent arg0) {
    }

    public void windowIconified(WindowEvent arg0) {
        
    }

    public void windowDeiconified(WindowEvent arg0) {
        
    }

    public void windowActivated(WindowEvent arg0) {
        
    }

    public void windowDeactivated(WindowEvent arg0) {
        
    }
}