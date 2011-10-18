package org.ihtsdo.translation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class JAccordianPanel extends JPanel {
	boolean movingComponents = false;
	int visibleIndex = 0;
	private int childCount=0;
	private int h=0;
	
	public JAccordianPanel() {
		setLayout(null);
//		// Add children and compute prefSize.
//		int childCount = 4;
//		Dimension d = new Dimension();
//		int h = 0;
//		for(int j = 0; j < childCount; j++) {
//			ChildPanel child = new ChildPanel(j+1, ml);
//			add(child);
//			d = child.getPreferredSize();
//			child.setBounds(0, h, d.width, d.height);
//			if(j < childCount-1)
//				h += ControlPanel.HEIGHT;
//		}
//		h += d.height;
//		setPreferredSize(new Dimension(d.width, h));
//		// Set z-order for children.
//		setZOrder();
	}
	public void addInnerPanel(MenuComponentPanel panel){

		// Add children and compute prefSize.
		Dimension d = new Dimension();
		ChildPanel child = new ChildPanel(childCount, ml,panel);
		add(child);

		setZOrder();
		this.visibleIndex=childCount;
		childCount++;
		
	}
	
	private void setZOrder() {
		Component[] c = getComponents();
		for(int j = 0;j<c.length; j++) {
			ChildPanel cp=((ChildPanel)c[j]);
			int cmpNr = cp.getId();	
			setComponentZOrder(c[j], c.length -1 -cmpNr);
		}
	}

	public void setComponentsSize(Dimension size) {	
		Component[] c = getComponents();
		int y;
		int visibleHeight = getHeight()-(ControlPanel.HEIGHT * (childCount-1));
		for(int j = 0;j<c.length;j++) {
			int cmpNr = ((ChildPanel)c[j]).getId();	
			if (cmpNr>visibleIndex){
				y=size.height-(ControlPanel.HEIGHT * (c.length-cmpNr));
			}
			else{
				y = ControlPanel.HEIGHT * (cmpNr);
			}
			c[j].setBounds(0, y, size.width, visibleHeight);
		}
		setPreferredSize(size);
		
	}
	
	private void setChildVisible(int indexToOpen) {
		// If visibleIndex < indexToOpen, components at
		// [visibleIndex+1 down to indexToOpen] move up.
		// If visibleIndex > indexToOpen, components at
		// [indexToOpen+1 up to visibleIndex] move down.
		// Collect indices of components that will move
		// and determine the distance/direction to move.
		int[] indices = new int[0];
		int travelLimit = 0;
		int visibleHeight = getHeight()-(ControlPanel.HEIGHT * (childCount-1));
		if(visibleIndex < indexToOpen) {
			travelLimit = ControlPanel.HEIGHT -visibleHeight;
			int n = indexToOpen - visibleIndex;
			indices = new int[n];
			for(int j = visibleIndex, k = 0; j < indexToOpen; j++, k++)
				indices[k] = j + 1;
		} else if(visibleIndex > indexToOpen) {
			travelLimit = visibleHeight -ControlPanel.HEIGHT;
			int n = visibleIndex - indexToOpen;
			indices = new int[n];
			for(int j = indexToOpen, k = 0; j < visibleIndex; j++, k++)
				indices[k] = j + 1;
		}
		movePanels(indices, travelLimit);
		visibleIndex = indexToOpen;
	}

	private void movePanels(final int[] indices, final int travel) {
		movingComponents = true;
		Thread thread = new Thread(new Runnable() {
			public void run() {
				Component[] c = getComponents();
				int limit = travel > 0 ? travel : 0;
				int count = travel > 0 ? 0 : travel;
				int dy    = travel > 0 ? 1 : -1;
				while(count < limit) {
//					try {
//						Thread.sleep(1);
//					} catch(InterruptedException e) {
//						System.out.println("interrupted");
//						break;
//					}
					for(int j = 0; j < indices.length; j++) {
						// The z-order reversed the order returned
						// by getComponents. Adjust the indices to
						// get the correct components to relocate.
						int index = c.length-1 - indices[j];
						Point p = c[index].getLocation();
						p.y += dy;
						c[index].setLocation(p.x, p.y);
					}
					repaint();
					count++;
				}
				movingComponents = false;
			}
		});
		thread.setPriority(Thread.NORM_PRIORITY);
		thread.start();
	}

	private MouseListener ml = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			int index = ((ControlPanel)e.getSource()).id;
			if(!movingComponents)
				setChildVisible(index);
		}
	};

	public JPanel getPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		panel.add(this, gbc);
		return panel;
	}
	public List<MenuComponentPanel> getMenuComponents(){
		List<MenuComponentPanel> menuComps=new ArrayList<MenuComponentPanel>();
		Component[] c = getComponents();
		int compsCount=0;
		for(int j = 0;j<c.length; j++) {
			if (c[j] instanceof ChildPanel){
				menuComps.add(((ChildPanel)c[j]).getMenuComponent());
			}
		}
		return menuComps;
	}
}

class ChildPanel extends JPanel {
	int id;
	public ChildPanel(int id, MouseListener ml, MenuComponentPanel panel) {
		setLayout(new BorderLayout());
		this.id=id;
		add(new ControlPanel(id, ml,panel.getLabelText(),panel.getLabelIcon()), "First");
		add(panel);
	}
	public MenuComponentPanel getMenuComponent(){
		for(Component component:getComponents()){
			if(component instanceof MenuComponentPanel){
				return (MenuComponentPanel)component;
			}
		}
		return null;
	}
	public int getId(){
		return id;
	}
//	private JPanel getContent(int id) {
//		JPanel panel = new JPanel(new GridBagLayout());
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.insets = new Insets(2,2,2,2);
//		gbc.weightx = 1.0;
//		gbc.weighty = 1.0;
//		gbc.anchor = gbc.NORTHWEST;
//		panel.add(new JLabel("Panel " + id + " Content"), gbc);
//		return panel;
//	}
//
//	public Dimension getPreferredSize() {
//		return new Dimension(300,150);
//	}
}

class ControlPanel extends JPanel {
	int id;
	JLabel titleLabel;
	Color c1 = new Color(220,193,209);
	Color c2 = new Color(220,233,249);
	Color fontFg = Color.blue;
	Color rolloverFg = Color.red;
	public final static int HEIGHT = 25;

	public ControlPanel(int id, MouseListener ml,String labelText, Icon labelIcon) {
		this.id = id;
		setLayout(new BorderLayout());
		String labelTxt;
		if (labelText!=null && !labelText.equals(""))
			labelTxt=labelText;
		else
			labelTxt="Panel " + id;
		add(titleLabel = new JLabel(labelTxt, JLabel.LEFT));
		if (labelIcon!=null)
			titleLabel.setIcon(labelIcon);
		titleLabel.setForeground(fontFg);
		Dimension d = getPreferredSize();
		d.height = HEIGHT;
		setPreferredSize(d);
		addMouseListener(ml);
		addMouseListener(listener);
	}

	protected void paintComponent(Graphics g) {
		int w = getWidth();
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(new GradientPaint(w/2, 0, c1, w/2, HEIGHT/2, c2));
		g2.fillRect(0,0,w,HEIGHT);
	}

	private MouseListener listener = new MouseAdapter() {
		public void mouseEntered(MouseEvent e) {
			titleLabel.setForeground(rolloverFg);
		}

		public void mouseExited(MouseEvent e) {
			titleLabel.setForeground(fontFg);
		}
	};
}
