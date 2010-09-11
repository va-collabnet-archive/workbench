package org.ihtsdo.arena.context.action;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.InputEvent;
import java.util.TooManyListenersException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

public class DropActionPanel extends JLabel {
	protected class DropActionPanelTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;

		public DropActionPanelTransferHandler(String propName) {
			super(propName);
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return true;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			return true;
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			return super.createTransferable(c);
		}

		@Override
		public void exportAsDrag(JComponent comp, InputEvent e, int action) {
			super.exportAsDrag(comp, e, action);
		}

		@Override
		protected void exportDone(JComponent source, Transferable data,
				int action) {
			super.exportDone(source, data, action);
		}

		@Override
		public int getSourceActions(JComponent c) {
			return DnDConstants.ACTION_COPY;
		}

		@Override
		public Icon getVisualRepresentation(Transferable t) {
			return super.getVisualRepresentation(t);
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			return super.importData(comp, t);
		}

		@Override
		public boolean importData(TransferSupport support) {
			return super.importData(support);
		}
	}
	
	protected class DropPanelDropTargetListener implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			DropActionPanel.this.setBorder(BorderFactory.createLoweredBevelBorder());
			//System.out.println("dragEnter: " + DropActionPanel.this.getText());
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			DropActionPanel.this.setBorder(BorderFactory.createEmptyBorder(bw, bw, bw, bw));
			//System.out.println("dragExit: " + DropActionPanel.this.getText());
		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {
			//System.out.println("dragOver: " + DropActionPanel.this.getText());
		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			DropActionPanel.this.setBorder(BorderFactory.createEmptyBorder(bw, bw, bw, bw));
			a.actionPerformed(null);
			//System.out.println("drop: " + DropActionPanel.this.getText());
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {
			//System.out.println("dropActionChanges: " + DropActionPanel.this.getText());
		}
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object draggedThing;
	private int bw = 3;
	private Action a;

	public DropActionPanel(Action a) throws TooManyListenersException {
		super();
		setText((String) a.getValue(Action.NAME));
		setIcon((Icon) a.getValue(Action.LARGE_ICON_KEY));
		setHorizontalAlignment(CENTER);
		setVerticalTextPosition(BOTTOM);
		this.setTransferHandler(new DropActionPanelTransferHandler("draggedThing"));
		this.getDropTarget().addDropTargetListener(new DropPanelDropTargetListener());
		this.setToolTipText("Test tip for: " + getText());
		setBorder(BorderFactory.createEmptyBorder(bw, bw, bw, bw));
		this.a = a;
	}
	
	public Object getDraggedThing() {
		return draggedThing;
	}

	public void setDraggedThing(Object draggedThing) {
		this.draggedThing = draggedThing;
		
	}

}
