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
 * Created on Feb 28, 2006
 */
package org.dwfa.bpa.util;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class PhantomFrame extends ComponentFrame implements ListDataListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    protected JMenu fileMenu;

    public PhantomFrame(String[] args) throws Exception {
        super(args, true);
        this.setName("Phantom Frame");
        JLabel l = new JLabel(
            "<html>This window shows up to give access to a menu bar<p>when no other windows are showing...");
        l.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(l);
        this.pack();
        this.setVisible(true);
        OpenFrames.addFrameListener(this);
        getQuitList().clear();
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    }

    @Override
    public void addAppMenus(JMenuBar mainMenuBar) throws Exception {
        mainMenuBar.add(fileMenu = new JMenu("File"));
    }

    @Override
    public JMenu getQuitMenu() {
        return fileMenu;
    }

    @Override
    public JMenuItem[] getNewWindowMenu() {
        return null;
    }

    @Override
    public String getNextFrameName() {
        return "Workflow Bundle";
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public void addInternalFrames(JMenu menu) {

    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        if ((e.getIndex1() == 0) && (OpenFrames.getNumOfFrames() == 0)) {
            this.setVisible(true);
            this.getQuitList().clear();
            this.getQuitList().add(new ComponentFrameBean.StandardQuitter(this));
        } else {
            if (this.isVisible() == true) {
                this.setVisible(false);
                getQuitList().clear();
            }
        }
    }

    /**
     * @param object
     */
    @Override
    public void intervalAdded(ListDataEvent e) {
        if ((e.getIndex1() == 0) && (OpenFrames.getNumOfFrames() == 0)) {
            this.setVisible(true);
            this.getQuitList().clear();
            this.getQuitList().add(new ComponentFrameBean.StandardQuitter(this));
        } else {
            if (this.isVisible() == true) {
                this.setVisible(false);
                getQuitList().clear();
            }
        }
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        if ((e.getIndex1() == 0) && (OpenFrames.getNumOfFrames() == 0)) {
            this.setVisible(true);
            this.getQuitList().clear();
            this.getQuitList().add(new ComponentFrameBean.StandardQuitter(this));
        } else {
            if (this.isVisible() == true) {
                this.setVisible(false);
                getQuitList().clear();
            }
        }
    }

}
