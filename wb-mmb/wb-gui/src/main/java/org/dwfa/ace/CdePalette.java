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

import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CdePalette extends JPanel implements ComponentListener {
    private static final long serialVersionUID = 1L;

    private static enum POSITION {
        UP, DOWN, LEFT, RIGHT
    };

    public static int increment = 25;
    private JPanel ghostPanel = new JPanel();

    private class PaletteMover implements ActionListener {
        private Point currentLocation;
        private Point endLocation;
        private int delay = 10;
        private Timer t;
        private TOGGLE_DIRECTION direction;
        private POSITION newPosition;

        public PaletteMover(Point currentLocation, Point endLocation, boolean selected, TOGGLE_DIRECTION direction,
                POSITION newPosition) {
            super();
            this.direction = direction;
            this.newPosition = newPosition;
            if (getRootPane() != null) {
                this.currentLocation = currentLocation;
                this.endLocation = endLocation;
                t = new Timer(delay, this);
                t.start();
                JLayeredPane layers = getRootPane().getLayeredPane();
                layers.add(ghostPanel, JLayeredPane.PALETTE_LAYER);
                ghostPanel.setBounds(getBounds());
                ghostPanel.setVisible(selected);
                getRootPane().getLayeredPane().moveToFront(ghostPanel);
                setVisible(false);
                setLocation(endLocation);
            }

        }

        public void stop() {
            if (t != null) {
                t.stop();
                t.removeActionListener(this);
                setLocation(endLocation);
                if (newPosition == POSITION.UP) {
                    setVisible(false);
                } else {
                    setVisible(true);
                }
                removeGhost();
            }
        }

        private void movePalette() {
            if ((Math.abs(currentLocation.x - endLocation.x) < increment)
                && (Math.abs(currentLocation.y - endLocation.y) < increment)) {
                currentLocation.x = endLocation.x;
                currentLocation.y = endLocation.y;
                ghostPanel.setLocation(currentLocation);
                stop();
                return;
            }
            if ((Math.abs(currentLocation.x - endLocation.x) < increment)) {
                currentLocation.x = endLocation.x;
            }
            if ((Math.abs(currentLocation.y - endLocation.y) < increment)) {
                currentLocation.y = endLocation.y;
            }

            if (currentLocation.x > endLocation.x) {
                currentLocation.x = currentLocation.x - increment;
            } else if (currentLocation.x < endLocation.x) {
                currentLocation.x = currentLocation.x + increment;
            }

            if (currentLocation.y > endLocation.y) {
                currentLocation.y = currentLocation.y - increment;
            } else if (currentLocation.y < endLocation.y) {
                currentLocation.y = currentLocation.y + increment;
            }
            ghostPanel.setLocation(currentLocation);
        }

        public void actionPerformed(ActionEvent e) {
            movePalette();
        }
    }

    private PaletteMover mover;
    private I_GetPalettePoint locator;
    private POSITION currentPosition;

    public CdePalette(I_GetPalettePoint locator) {
        super();
        this.locator = locator;
    }

    public void removeGhost() {
        if (getRootPane() != null) {
            JLayeredPane layers = getRootPane().getLayeredPane();
            if (layers != null) {
                layers.moveToFront(CdePalette.this);
                layers.remove(ghostPanel);
            }
        }
    }

    public CdePalette(boolean isDoubleBuffered, I_GetPalettePoint locator) {
        super(isDoubleBuffered);
        this.locator = locator;
    }

    public CdePalette(LayoutManager layout, boolean isDoubleBuffered, I_GetPalettePoint locator) {
        super(layout, isDoubleBuffered);
        this.locator = locator;
    }

    public CdePalette(LayoutManager layout, I_GetPalettePoint locator) {
        super(layout);
        this.locator = locator;
    }

    protected void paintComponent(Graphics g) {
        Rectangle r = getBounds();
        g.clearRect(0, 0, r.width, r.height);
        super.paintComponent(g);
    }

    public enum TOGGLE_DIRECTION {
        UP_DOWN, LEFT_RIGHT
    };

    public void togglePalette(boolean selected, TOGGLE_DIRECTION direction) {
        Point locatorBounds = locator.getPalettePoint();
        POSITION newPosition = null;
        if (direction == TOGGLE_DIRECTION.LEFT_RIGHT) {
            if (getBounds().x == locatorBounds.x) {
                currentPosition = POSITION.RIGHT;
                newPosition = POSITION.LEFT;
            } else {
                currentPosition = POSITION.LEFT;
                newPosition = POSITION.RIGHT;
            }
            if (mover != null) {
                mover.stop();
            }
            setLocation(getLocation().x, locator.getPalettePoint().y);
            mover = new PaletteMover(getLocation(), computeLocation(currentPosition), selected, direction, newPosition);
        } else {
            if (getBounds().y == locatorBounds.y) {
                currentPosition = POSITION.DOWN;
                newPosition = POSITION.UP;
            } else {
                currentPosition = POSITION.UP;
                newPosition = POSITION.DOWN;
            }
            if (mover != null) {
                mover.stop();
            }
            setLocation(getLocation().x, locator.getPalettePoint().y);
            mover = new PaletteMover(getLocation(), computeLocation(currentPosition), selected, direction, newPosition);
        }
    }

    public Point computeLocation(POSITION newSide) {
        Point locatorBounds = locator.getPalettePoint();
        Point newLocation;
        switch (newSide) {
        case DOWN:
            newLocation = new Point(locatorBounds.x, locatorBounds.y - getBounds().height);
            break;
        case LEFT:
            newLocation = new Point(locatorBounds.x, locatorBounds.y);
            break;
        case RIGHT:
            newLocation = new Point(locatorBounds.x - getBounds().width, locatorBounds.y);
            break;

        case UP:
            newLocation = new Point(locatorBounds.x, locatorBounds.y);
            break;
        default:
            throw new RuntimeException("Unexpected value: " + newSide);
        }
        return newLocation;
    }

    public void componentHidden(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    public void componentMoved(ComponentEvent e) {
        // TODO Auto-generated method stub

    }

    public void componentResized(ComponentEvent e) {
        setLocation(computeLocation(currentPosition));
    }

    public void componentShown(ComponentEvent e) {
        // TODO Auto-generated method stub
    }

    public void setLocator(I_GetPalettePoint locator) {
        this.locator = locator;
    }
}
