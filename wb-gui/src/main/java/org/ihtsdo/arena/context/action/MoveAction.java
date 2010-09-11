package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.dwfa.ace.batch.BatchMonitor;

//element_next.png
public class MoveAction extends AbstractAction {

	private static final long serialVersionUID = 1L;


	public MoveAction() {
		super("move");
		putValue(LARGE_ICON_KEY, new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/element_next.png")));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
