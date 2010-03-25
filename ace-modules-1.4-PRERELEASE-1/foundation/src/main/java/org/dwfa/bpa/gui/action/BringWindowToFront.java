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

import javax.swing.JFrame;

import org.dwfa.bpa.util.OpenFrames;
import org.dwfa.bpa.util.PhantomFrame;

/**
 * @author kec
 * 
 */
public class BringWindowToFront implements ActionListener {
    private JFrame frame;

    /**
     * 
     */
    public BringWindowToFront(JFrame frame) {
        this.frame = frame;
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        this.frame.requestFocus();
        this.frame.toFront();
        this.frame.setVisible(true);
        if (PhantomFrame.class.isAssignableFrom(frame.getClass()) == false) {
            OpenFrames.addFrame(frame);
        }
    }
}
