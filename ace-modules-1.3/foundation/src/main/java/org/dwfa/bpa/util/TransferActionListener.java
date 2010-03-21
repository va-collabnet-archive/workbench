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
 * Created on Mar 2, 2006
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

public class TransferActionListener implements ActionListener, PropertyChangeListener {

    static Logger logger = Logger.getLogger(TransferActionListener.class.getName());

    private JComponent focusOwner = null;

    public TransferActionListener() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
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
            a.actionPerformed(new ActionEvent(focusOwner, ActionEvent.ACTION_PERFORMED, null));
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
