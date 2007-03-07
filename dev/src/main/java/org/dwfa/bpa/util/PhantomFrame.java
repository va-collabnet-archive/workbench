/*
 * Created on Feb 28, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


import net.jini.config.ConfigurationException;

import com.sun.jini.start.LifeCycle;

public class PhantomFrame extends ComponentFrame implements ListDataListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected JMenu fileMenu;

    public PhantomFrame(String[] args, LifeCycle lc) throws Exception {
        super(args, lc, true);
        JLabel l = new JLabel("<html>This window shows up to give access to a menu bar<p>when all the other windows have closed...");
        l.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        this.add(l);
        this.pack();
        this.setVisible(true);
        OpenFrames.addFrameListener(this);
    }

    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(fileMenu = new JMenu("File"));
    }

    public JMenu getQuitMenu() {
        return fileMenu;
    }

    public JMenuItem getNewWindowMenu() {
        return null;
    }

    public String getNextFrameName() throws ConfigurationException {
        return "Workflow Bundle";
    }

    public int getCount() {
        return 0;
    }

    public void addInternalFrames(JMenu menu) {
        
    }

    public void intervalRemoved(ListDataEvent e) {
        if (OpenFrames.getNumOfFrames() == 0) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
        
    }

    /**
     * @param object
     */
    public void intervalAdded(ListDataEvent e) {
        if (OpenFrames.getNumOfFrames() == 0) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
    }

    public void contentsChanged(ListDataEvent e) {
        if (OpenFrames.getNumOfFrames() == 0) {
            this.setVisible(true);
        } else {
            this.setVisible(false);
        }
    }
}
