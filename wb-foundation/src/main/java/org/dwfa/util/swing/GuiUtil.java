package org.dwfa.util.swing;

import java.awt.Component;
import java.awt.Dimension;

public class GuiUtil {
	public static void tickle(Component c) {
		Dimension size = c.getSize();
		Dimension tempSize = new Dimension(size.width, size.height + 1);
		c.setSize(tempSize);
        c.validate();
		c.invalidate();
		c.setSize(size);
        c.validate();
		c.invalidate();
        c.repaint();
	}
}
