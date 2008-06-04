package org.dwfa.ace.list;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.dnd.TerminologyTransferHandler;

public class TerminologyList extends JList {
	
	private class DeleteAction extends AbstractAction {

		public DeleteAction() {
			super("delete");
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void actionPerformed(ActionEvent e) {
			int selectedIndex = getSelectedIndex();
			if (selectedIndex >= 0) {
				TerminologyListModel tm = (TerminologyListModel) getModel();
				tm.removeElement(selectedIndex);
			}
		}
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TerminologyList(I_ConfigAceFrame config) {
		super(new TerminologyListModel());
		init(true, config);
	}
	public TerminologyList(TerminologyListModel dataModel, I_ConfigAceFrame config) {
		super(dataModel);
		init(true, config);
	}

	public TerminologyList(boolean allowDelete, I_ConfigAceFrame config) {
		super(new TerminologyListModel());
		init(allowDelete, config);
	}
	public TerminologyList(TerminologyListModel dataModel, boolean allowDelete, I_ConfigAceFrame config) {
		super(dataModel);
		init(allowDelete, config);
	}

	private void init(boolean allowDelete, I_ConfigAceFrame config) {
      setCellRenderer(new AceListRenderer(config));
		setTransferHandler(new TerminologyTransferHandler(this));
		setDragEnabled(true);
		if (allowDelete) {
			DeleteAction delete = new DeleteAction();
	        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
	                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), delete.getValue(Action.NAME));
	        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
	        		delete.getValue(Action.NAME));
	        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
	                KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), delete.getValue(Action.NAME));
	        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
	        		delete.getValue(Action.NAME));

	 		ActionMap map = this.getActionMap();
			map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
			map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
			map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
			map.put(delete.getValue(Action.NAME), delete);
		}
	}

}
