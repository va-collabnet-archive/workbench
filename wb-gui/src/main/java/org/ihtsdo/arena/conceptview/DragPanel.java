package org.ihtsdo.arena.conceptview;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.dwfa.ace.log.AceLog;

public abstract class DragPanel<T> extends JPanel implements Transferable {
	protected class DragPanelTransferHandler extends TransferHandler {

		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public DragPanelTransferHandler(String propName) {
			super(propName);
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (DataFlavor f: transferFlavors) {
				if (getSupportedImportFlavors().contains(f)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			if (super.canImport(support)) {
				try {
					for (DataFlavor f: getSupportedImportFlavors()) {
						if (support.getTransferable().isDataFlavorSupported(f)) {
							Object transferData = support.getTransferable().getTransferData(f);
							T thingBeingDragged = getThingToDrag();
							if (transferData != thingBeingDragged) {
								return true;
							}
						}
					}
					
				} catch (UnsupportedFlavorException e) {
					AceLog.getAppLog().alertAndLogException(e);
				} catch (IOException e) {
					AceLog.getAppLog().alertAndLogException(e);
				}
			}
			return false;
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

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected T thingToDrag;

	private boolean dragEnabled;
	
	private Set<DataFlavor> supportedImportFlavors = null;
	
	
	public DragPanel() {
		super();
	}

	public DragPanel(LayoutManager layout) {
		super(layout);
	}

	public T getThingToDrag() {
		return thingToDrag;
	}
	
	public Set<DataFlavor> getSupportedImportFlavors() {
		return DragPanelDataFlavors.dragPanelFlavorSet;
	}
	
	public void setThingToDrag(T thingToDrag) {
		JPopupMenu popup = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem("Copy to Concept");
        popup.add(copyItem);
        JMenuItem moveItem = new JMenuItem("Move to Concept");
        popup.add(moveItem);
        
		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mouseLocation, this);
        popup.show(this, mouseLocation.x, mouseLocation.y);
	}

	protected abstract void addToDropPopupMenu(JPopupMenu popup);
	
	public abstract String getDragPropertyString();
	
	public void setupDrag(T thingToDrag) {
		// I_RelTuple r
		// List<I_RelTuple> group
		// I_DescriptionVersioned desc
		this.thingToDrag = thingToDrag;
		this.setTransferHandler(new DragPanelTransferHandler(getDragPropertyString()));
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int mode = getTransferMode(); 
				JComponent comp = (JComponent) e.getSource();
				TransferHandler th = comp.getTransferHandler();
				th.exportAsDrag(comp, e, mode);
				System.out.println("Drag started: " + e.getClickCount());
				e.consume();
			}
		});
	}

	public void setDragEnabled(boolean b) {
		if (b && GraphicsEnvironment.isHeadless()) {
			throw new HeadlessException();
		}
		dragEnabled = b;
	}
	
	protected abstract int getTransferMode();

	/**
	 * Gets the value of the <code>dragEnabled</code> property.
	 * 
	 * @return the value of the <code>dragEnabled</code> property
	 * @see #setDragEnabled
	 * @since 1.4
	 */
	public boolean getDragEnabled() {
		return dragEnabled;
	}

	@Override
	public T getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		return thingToDrag;
	}

	public abstract DataFlavor getNativeDataFlavor();

}
