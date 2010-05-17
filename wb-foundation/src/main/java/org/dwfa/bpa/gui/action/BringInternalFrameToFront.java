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
            if ((this.panel.getParent() != null)
                && (JLayeredPane.class.isAssignableFrom(this.panel.getParent().getClass()))) {
                JLayeredPane parent = (JLayeredPane) this.panel.getParent();
                parent.setPosition(this.panel, 0);
            }
        }

    }

}
