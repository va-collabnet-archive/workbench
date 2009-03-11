/*
 * Created on Mar 15, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLayeredPane;

import org.dwfa.bpa.gui.GridBagPanel;



/**
 * @author kec
 *
 */
public class BringInternalFrameToFront implements ActionListener {
    private static Logger logger = Logger.getLogger("org.dwfa.bpa.gui.action");
    
    private GridBagPanel panel;
    /**
     * 
     */
    public BringInternalFrameToFront(GridBagPanel panel) {
        this.panel = panel;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        this.panel.setShowInLayout(true);
        this.panel.requestFocusInWindow();
        if (this.panel.getInternalFrame() != null) {
            this.panel.getInternalFrame().toFront();
            try {
                this.panel.getInternalFrame().setIcon(false);
                this.panel.getInternalFrame().setSelected(true);
            } catch (PropertyVetoException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } else {
            if ((this.panel.getParent() != null) && (JLayeredPane.class.isAssignableFrom(this.panel.getParent().getClass()))) {
                JLayeredPane parent = (JLayeredPane) this.panel.getParent();
                parent.setPosition(this.panel, 0);
            }
        }
        

    }

}