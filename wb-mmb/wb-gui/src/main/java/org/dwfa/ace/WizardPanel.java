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
package org.dwfa.ace;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import org.dwfa.ace.log.AceLog;

public class WizardPanel extends JPanel {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private class WizardMouseAdaptor extends MouseAdapter {

        Point wizardStartPoint;
        Point dragStartPoint;

        @Override
        public void mouseDragged(MouseEvent e) {
            int locationY = wizardStartPoint.y + (e.getLocationOnScreen().y - dragStartPoint.y);
            int locationX = wizardStartPoint.x + (e.getLocationOnScreen().x - dragStartPoint.x);
            setLocationRelativeToAcePanel(locationY, locationX);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            wizardStartPoint = getLocation();
            dragStartPoint = e.getLocationOnScreen();
        }

    }

    private class AceComponentListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
            int locationY = getLocation().y;
            int locationX = getLocation().x;
            setLocationRelativeToAcePanel(locationY, locationX);
        }

    }

    private JPanel wfPanel = new JPanel();
    private JPanel wfDetailsPanel = new JPanel();
    private JLabel dragLabel = new JLabel();
    private ACE acePanel;
    private static int dragLabelHeight = 49;


    private class WfDetailsChangedListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            int xLoc = wfPanel.getWidth() - wfDetailsPanel.getWidth();
            if (xLoc < dragLabel.getWidth()) {
                xLoc = dragLabel.getWidth();
            }
            if (wfDetailsPanel.getWidth() < wfPanel.getWidth()) {
                wfDetailsPanel.setSize(wfPanel.getWidth(), wfDetailsPanel.getHeight());
            }
            wfDetailsPanel.setLocation(xLoc, wfDetailsPanel.getLocation().y);
            AceLog.getAppLog().info("Set wf details panel to: " + wfDetailsPanel.getLocation());
        }

    }

    public WizardPanel(ACE acePanel) {
        super();
        this.acePanel = acePanel;
        setLayout(null);
        setOpaque(false);
        setSize(2000, 2000);
        setEnabled(true);

        dragLabel.setOpaque(true);
        dragLabel.setBackground(Color.GRAY);
        WizardMouseAdaptor adaptor = new WizardMouseAdaptor();
        dragLabel.addMouseListener(adaptor);
        dragLabel.addMouseMotionListener(adaptor);
        dragLabel.setSize(10, dragLabelHeight);
        add(dragLabel);
        dragLabel.setLocation(0, 0);
        setLocation(300, 20);

        wfPanel.setOpaque(true);
        add(wfPanel);
        wfPanel.setSize(500 - dragLabel.getWidth(), dragLabelHeight);
        wfPanel.setLocation(dragLabel.getLocation().x + dragLabel.getWidth() + 1, 0);
        wfPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        wfDetailsPanel.setOpaque(true);
        add(wfDetailsPanel);
        wfDetailsPanel.setSize(wfPanel.getWidth() - (dragLabel.getWidth() + 1), getHeight() - dragLabel.getHeight());
        wfDetailsPanel.setLocation(wfPanel.getLocation().x, dragLabel.getHeight());
        wfDetailsPanel.setVisible(false);
        wfDetailsPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        wfDetailsPanel.addComponentListener(new WfDetailsChangedListener());
        acePanel.addComponentListener(new AceComponentListener());
    }

    public JPanel getWfPanel() {
        return wfPanel;
    }

    public JPanel getWfDetailsPanel() {
        return wfDetailsPanel;
    }

    private void setLocationRelativeToAcePanel(int locationY, int locationX) {
        if (locationY < acePanel.getY() || locationY < 15) {
            locationY = acePanel.getY();
        }
        if (locationY > acePanel.getY() + acePanel.getHeight() - 20) {
            locationY = acePanel.getY() + acePanel.getHeight() - 20;
        }
        if (locationX < acePanel.getX()) {
            locationX = acePanel.getX();
        }
        if (locationX > acePanel.getX() + acePanel.getWidth() - 20) {
            locationX = acePanel.getX() + acePanel.getWidth() - 20;
        }
        setLocation(locationX, locationY);
    }

}
