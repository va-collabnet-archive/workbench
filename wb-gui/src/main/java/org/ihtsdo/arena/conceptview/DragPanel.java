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
import java.util.Collection;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.context.action.I_HandleContext;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;

public abstract class DragPanel<T extends ComponentBI> extends JPanel implements Transferable {
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
	
	@SuppressWarnings("unused")
	private Set<DataFlavor> supportedImportFlavors = null;
	
	protected I_HandleContext context;
	
	public DragPanel(I_HandleContext context) {
		super();
		this.context = context;
	}

	public DragPanel(LayoutManager layout, I_HandleContext context) {
		super(layout);
		this.context = context;
	}

	public T getThingToDrag() {
		return thingToDrag;
	}
	
	public Set<DataFlavor> getSupportedImportFlavors() {
		return DragPanelDataFlavors.dragPanelFlavorSet;
	}
	
	public void setDraggedThing(Object draggedThing) {
		JPopupMenu popup = new JPopupMenu();
		
		if (ComponentBI.class.isAssignableFrom(draggedThing.getClass())) {
			ComponentBI component = (ComponentBI) draggedThing;
	     	for (Action a: getActions(thingToDrag, component)) {
	    		popup.add(a);
	    	}
		}
		
		if (DescriptionChronicleBI.class.isAssignableFrom(draggedThing.getClass())) {
			DescriptionChronicleBI desc = (DescriptionChronicleBI) draggedThing;
	     	for (Action a: context.dropOnDesc(desc.getConceptNid(), desc.getNid())) {
	    		popup.add(a);
	    	}
		} else if (RelationshipChronicleBI.class.isAssignableFrom(draggedThing.getClass())) {
			
		} else if (ConceptChronicleBI.class.isAssignableFrom(draggedThing.getClass())) {
			
		}

		if (popup.getComponentCount() > 0) {
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
	        SwingUtilities.convertPointFromScreen(mouseLocation, this);
	        popup.show(this, mouseLocation.x, mouseLocation.y);
		}
	}

	protected abstract Collection<Action> getActions(ComponentBI targetComponent, ComponentBI droppedComponent);

	public void setupDrag(T thingToDrag) {
		// I_RelTuple r
		// List<I_RelTuple> group
		// I_DescriptionVersioned desc
		this.thingToDrag = thingToDrag;
		this.setTransferHandler(new DragPanelTransferHandler("draggedThing"));
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
