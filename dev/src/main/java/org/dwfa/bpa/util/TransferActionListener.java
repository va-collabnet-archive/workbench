/*
 * Created on Mar 2, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JComponent;

public class TransferActionListener implements ActionListener,
        PropertyChangeListener {

    static Logger logger = Logger.getLogger(TransferActionListener.class
            .getName());

    private JComponent focusOwner = null;

    public TransferActionListener() {
        KeyboardFocusManager manager = KeyboardFocusManager
                .getCurrentKeyboardFocusManager();
        manager.addPropertyChangeListener("permanentFocusOwner", this);
    }

    public void propertyChange(PropertyChangeEvent e) {
        Object o = e.getNewValue();
        if (o instanceof JComponent) {
            focusOwner = (JComponent) o;
        } else {
            focusOwner = null;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("new focus: " + e.getNewValue());
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (focusOwner == null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("null focus owner.");
            }
            return;
        }
        String action = (String) e.getActionCommand();
        Action a = focusOwner.getActionMap().get(action.toLowerCase());
        if (a != null) {
            a.actionPerformed(new ActionEvent(focusOwner,
                    ActionEvent.ACTION_PERFORMED, null));
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Sent action: " + e.getActionCommand() + " to: " + focusOwner);
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("null Action for:" + action);
                logger.fine("Available actions:" + Arrays.asList(focusOwner.getActionMap().allKeys()));
            }
            
        }
    }
}