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
	private static enum Side{TOP, BOTTOM, LEFT, RIGHT};
	public static int increment = 50;
	private JPanel ghostPanel = new JPanel();
	
	private class PaletteMover implements ActionListener {
		private Point currentLocation;
		private Point endLocation;
		private int delay = 20;
		private Timer t;
		
		public PaletteMover(Point currentLocation, Point endLocation, boolean selected) {
			super();
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

		public void stop() {
			t.stop();
			t.removeActionListener(this);
			setLocation(endLocation);
			setVisible(true);
			removeGhost();
		}


		private void movePalette() {
			if (Math.abs(currentLocation.x - endLocation.x) < increment) {
				currentLocation.x = endLocation.x;
				ghostPanel.setLocation(currentLocation);
				stop();
				return;
			} else  if (currentLocation.x > endLocation.x) {
				currentLocation.x = currentLocation.x - increment;
			} else {
				currentLocation.x = currentLocation.x + increment;
			}
			ghostPanel.setLocation(currentLocation);
		}

		public void actionPerformed(ActionEvent e) {
			movePalette();
		}
	}

	private PaletteMover mover;
	private I_GetPalettePoint locator;
	private Side currentSide;
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
	
	
	public void togglePalette(boolean selected) {
		Point locatorBounds = locator.getPalettePoint();
		if (getBounds().x == locatorBounds.x) {
			currentSide = Side.RIGHT;
		} else {
			currentSide = Side.LEFT;
		}
		if (mover != null) {
			mover.stop();
		}
		setLocation(getLocation().x, locator.getPalettePoint().y);
		mover = new PaletteMover(getLocation(), computeLocation(currentSide), selected);
	}
	
	public Point computeLocation(Side newSide) {
		Point locatorBounds = locator.getPalettePoint();
		Point newLocation;
		if (newSide == Side.RIGHT) {
			newLocation = new Point(locatorBounds.x - getBounds().width,
					locatorBounds.y);
		} else if (newSide == Side.LEFT) {
			newLocation = new Point(locatorBounds.x, locatorBounds.y);
		} else {
			newLocation = new Point(0,0);
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
		setLocation(computeLocation(currentSide));	
	}

	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}


}
