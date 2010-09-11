package org.ihtsdo.arena;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class ScrollablePanel extends JPanel implements Scrollable {

	private static final long serialVersionUID = 1L;
	
	public enum ScrollDirection {LEFT_TO_RIGHT, TOP_TO_BOTTOM };

	private ScrollDirection direction = ScrollDirection.TOP_TO_BOTTOM;
	
	
	public ScrollablePanel() {
		super();
		setupAutoScroll();	
	}

	public ScrollablePanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
		setupAutoScroll();	
	}

	public ScrollablePanel(boolean isDoubleBuffered, ScrollDirection direction) {
		super(isDoubleBuffered);
		setupAutoScroll();	
		this.direction = direction;
	}

	private void setupAutoScroll() {
		setAutoscrolls(true);
		MouseMotionListener doScrollRectToVisible = new MouseMotionAdapter() {
		          public void mouseDragged(MouseEvent e) {
		             Rectangle r = new Rectangle(e.getX(), e.getY(), 1, 1);
		             ((JPanel)e.getSource()).scrollRectToVisible(r);
		         }
		      };
		     addMouseMotionListener(doScrollRectToVisible);
	}

	public ScrollablePanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
		setupAutoScroll();	
	}

	public ScrollablePanel(LayoutManager layout, ScrollDirection direction) {
		super(layout);
		setupAutoScroll();	
		this.direction = direction;
	}

	public ScrollablePanel(LayoutManager layout) {
		super(layout);
		setupAutoScroll();	
	}

	public void setBounds( int x, int y, int width, int height ) {
		super.setBounds( x, y, getParent().getWidth(), height );
	}

	public Dimension getPreferredSize() {
		return new Dimension( getPreferredWidth(), getPreferredHeight() );
	}

	public Dimension getPreferredScrollableViewportSize() {
		return super.getPreferredSize();
	}

	public int getScrollableUnitIncrement( Rectangle visibleRect, int orientation, int direction ) {
		int hundredth = ( orientation ==  SwingConstants.VERTICAL
				? getParent().getHeight() : getParent().getWidth() ) / 100;
		return ( hundredth == 0 ? 1 : hundredth ); 
	}

	public int getScrollableBlockIncrement( Rectangle visibleRect, int orientation, int direction ) {
		return orientation == SwingConstants.VERTICAL ? getParent().getHeight() : getParent().getWidth();
	}

	public boolean getScrollableTracksViewportWidth() {
		return true;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	private int getPreferredHeight() {
		if (direction == ScrollDirection.TOP_TO_BOTTOM) {
			LayoutManager m = getLayout();
			int gap = 0;
			if (FlowLayout.class.isAssignableFrom(m.getClass())) {
				gap = ( (FlowLayout) m ).getVgap();
			} else if (GridLayout.class.isAssignableFrom(m.getClass())) {
				gap = ( (GridLayout) m ).getVgap();
			}
			int rv = 0;
			for ( int k = 0, count = getComponentCount(); k < count; k++ ) {
				Component comp = getComponent( k );
				Rectangle r = comp.getBounds();
				int height = r.y + r.height;
				if ( height > rv )
					rv = height;
				rv += gap;
			}
			if (rv < getParent().getHeight()) {
				rv = getParent().getHeight();
			}
			return rv;
		}
		return getHeight();
	}

	private int getPreferredWidth() {
		if (direction == ScrollDirection.LEFT_TO_RIGHT) {
			LayoutManager m = getLayout();
			int gap = 0;
			if (FlowLayout.class.isAssignableFrom(m.getClass())) {
				gap = ( (FlowLayout) m ).getHgap();
			} else if (GridLayout.class.isAssignableFrom(m.getClass())) {
				gap = ( (GridLayout) m ).getHgap();
			}
			int rv = 0;
			for ( int k = 0, count = getComponentCount(); k < count; k++ ) {
				Component comp = getComponent( k );
				Rectangle r = comp.getBounds();
				int width = r.x + r.width;
				if ( width > rv )
					rv = width;
				rv += gap;
			}
			if (rv < getParent().getWidth()) {
				rv = getParent().getWidth();
			}
			return rv;
		}
		return getWidth();
	}
}
