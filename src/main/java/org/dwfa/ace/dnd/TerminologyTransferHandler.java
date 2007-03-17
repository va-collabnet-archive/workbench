package org.dwfa.ace.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.DropButton;
import org.dwfa.ace.I_ContainTermComponent;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.termviewer.dnd.FixedTerminologyTransferable;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.I_GetConceptData;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinRelTuple;

import com.sleepycat.je.DatabaseException;

public class TerminologyTransferHandler extends TransferHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static String conceptBeanType = DataFlavor.javaJVMLocalObjectMimeType
			+ ";class=" + ConceptBean.class.getName();

	public static String thinDescTupleType = DataFlavor.javaJVMLocalObjectMimeType
			+ ";class=" + ThinDescTuple.class.getName();

	public static final String thinDescVersionedType = DataFlavor.javaJVMLocalObjectMimeType
			+ ";class=" + ThinDescVersioned.class.getName();

	public static DataFlavor conceptBeanFlavor;

	public static DataFlavor thinDescVersionedFlavor;

	public static DataFlavor thinDescTupleFlavor;

	public static DataFlavor[] supportedFlavors;

	public TerminologyTransferHandler() {
		super();

		if (conceptBeanFlavor == null) {
			try {
				conceptBeanFlavor = new DataFlavor(
						TerminologyTransferHandler.conceptBeanType);
				thinDescVersionedFlavor = new DataFlavor(
						TerminologyTransferHandler.thinDescVersionedType);
				thinDescTupleFlavor = new DataFlavor(
						TerminologyTransferHandler.thinDescTupleType);
				supportedFlavors = new DataFlavor[] { thinDescVersionedFlavor,
						thinDescTupleFlavor, 
						conceptBeanFlavor,
						FixedTerminologyTransferable.universalFixedConceptFlavor,
						FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
						FixedTerminologyTransferable.universalFixedDescFlavor,
						FixedTerminologyTransferable.universalFixedDescInterfaceFlavor,
						DataFlavor.stringFlavor };
			} catch (ClassNotFoundException e) {
				// should never happen.
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("Creating a transferable for: " + c);
		}
		if (JTree.class.isAssignableFrom(c.getClass())) {
			JTree tree = (JTree) c;
			Object obj = tree.getLastSelectedPathComponent();
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
			return new ConceptTransferable((I_GetConceptData) node
					.getUserObject());

		} else if (TerminologyList.class.isAssignableFrom(c.getClass())) {
			TerminologyList list = (TerminologyList) c;
			return new ConceptTransferable((I_GetConceptData) list
					.getSelectedValue());
		} else if (JTable.class.isAssignableFrom(c.getClass())) {
			JTable termTable = (JTable) c;
			TableModel tableModel = termTable.getModel();
			if (TableSorter.class.isAssignableFrom(tableModel.getClass())) {
				tableModel = ((TableSorter) tableModel).getTableModel();
			}
			if (RelTableModel.class.isAssignableFrom(tableModel.getClass())) {
				TableModel rtm = termTable.getModel();
				StringWithRelTuple swrt = (StringWithRelTuple) rtm.getValueAt(
						termTable.getSelectedRow(), termTable
								.getSelectedColumn());
				ThinRelTuple rel = swrt.getTuple();
				TableColumn column = termTable.getColumnModel().getColumn(
						termTable.getSelectedColumn());
				REL_FIELD columnDesc = (REL_FIELD) column.getIdentifier();
				switch (columnDesc) {
				case SOURCE_ID:
					return new ConceptTransferable(ConceptBean.get(rel
							.getC1Id()));
				case REL_TYPE:
					return new ConceptTransferable(ConceptBean.get(rel
							.getRelTypeId()));
				case DEST_ID:
					return new ConceptTransferable(ConceptBean.get(rel
							.getC2Id()));
				case REFINABILITY:
					return new ConceptTransferable(ConceptBean.get(rel
							.getRefinabilityId()));
				case CHARACTERISTIC:
					return new ConceptTransferable(ConceptBean.get(rel
							.getCharacteristicId()));
				case STATUS:
					return new ConceptTransferable(ConceptBean.get(rel
							.getStatusId()));
				case REL_ID:
				case VERSION:
				case BRANCH:
				case GROUP:
				default:
					throw new UnsupportedOperationException("Can't convert "
							+ columnDesc + " to a concept bean");
				}
			} else if (DescriptionTableModel.class.isAssignableFrom(tableModel
					.getClass())) {
				TableModel dtm = termTable.getModel();
				if (termTable.getSelectedRow() >= 0) {
					StringWithDescTuple swdt = (StringWithDescTuple) dtm
							.getValueAt(termTable.getSelectedRow(), termTable
									.getSelectedColumn());
					ThinDescTuple desc = swdt.getTuple();
					TableColumn column = termTable.getColumnModel().getColumn(
							termTable.getSelectedColumn());
					DESC_FIELD columnDesc = (DESC_FIELD) column.getIdentifier();
					switch (columnDesc) {

					case CON_ID:
						return new ConceptTransferable(ConceptBean.get(desc
								.getConceptId()));
					case STATUS:
						return new ConceptTransferable(ConceptBean.get(desc
								.getStatusId()));
					case TYPE:
						return new ConceptTransferable(ConceptBean.get(desc
								.getTypeId()));
					case CASE_FIXED:
						return new StringSelection(Boolean.toString(desc
								.getInitialCaseSignificant()));
					case LANG:
						return new StringSelection(desc.getLang());
					case TEXT:
						return new DescriptionTransferable(desc);
					case DESC_ID:
					case VERSION:
					case BRANCH:
					default:
						throw new UnsupportedOperationException(
								"Can't convert " + columnDesc
										+ " to a concept bean");
					}
				} else {
					JOptionPane.showMessageDialog(termTable,
						    "No row is selected.",
						    "Copy error",
						    JOptionPane.ERROR_MESSAGE);
					return null;
				}
			} else {
				throw new UnsupportedOperationException("JTable type: "
						+ tableModel.getClass().toString());
			}
		} else {
			I_ContainTermComponent ictc = (I_ContainTermComponent) c;
			return new ConceptTransferable((I_GetConceptData) ictc
					.getTermComponent());
		}
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("export done: " + source);
		}
		super.exportDone(source, data, action);
		if (action == MOVE) {
			if (TerminologyList.class.isAssignableFrom(source.getClass())) {
				TerminologyList tl = (TerminologyList) source;
				int selectedIndex = tl.getSelectedIndex();
				TerminologyListModel tm = (TerminologyListModel) tl.getModel();
				tm.removeElement(selectedIndex);
			}
		}
	}

	@Override
	public Icon getVisualRepresentation(Transferable t) {
		// return super.getVisualRepresentation(t);
		return new ImageIcon(ACE.class.getResource("/32x32/plain/history2.png"));
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("import: " + comp);
		}
		if (I_ContainTermComponent.class.isAssignableFrom(comp.getClass())) {
			I_ContainTermComponent ictc = (I_ContainTermComponent) comp;
			try {
				Object obj = t.getTransferData(conceptBeanFlavor);
				if (AceLog.isLoggable(Level.FINE)) {
					AceLog.fine("Transfer data for conceptBeanFlavor is: " + obj);
				}
				ConceptBean cb;
				if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
					ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
					cb = cbt.getCoreBean();
				} else {
					cb = (ConceptBean) obj;
				}
				ictc.setTermComponent(cb);
				return true;
			} catch (UnsupportedFlavorException e) {
				AceLog.log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		if (TerminologyList.class.isAssignableFrom(comp.getClass())) {
			TerminologyList tl = (TerminologyList) comp;
			TerminologyListModel model = (TerminologyListModel) tl.getModel();
			try {
				Object obj = t.getTransferData(conceptBeanFlavor);
				ConceptBean cb;
				if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
					ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
					cb = cbt.getCoreBean();
				} else {
					cb = (ConceptBean) obj;
				}
				model.addElement(cb);
				return true;
			} catch (UnsupportedFlavorException e) {
				AceLog.log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		if (JTable.class.isAssignableFrom(comp.getClass())) {
			JTable table = (JTable) comp;
			Point mouseLoc = table.getMousePosition();
			if (mouseLoc != null) {
				int column = table.columnAtPoint(mouseLoc);
				int row = table.rowAtPoint(mouseLoc);
				AceLog.info("Dropping on JTable row: " + row + " column: "
						+ column);
				if (table.isCellEditable(row, column)) {
					try {
						I_GetConceptData obj = (I_GetConceptData) t
								.getTransferData(conceptBeanFlavor);
						table.setValueAt(obj.getConceptId(), row, column);
					} catch (UnsupportedFlavorException e) {
						AceLog.info(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
					return true;
				}
			}
			AceLog.info("Cell is not editable");
			return false;
		}
		if (DropButton.class.isAssignableFrom(comp.getClass())) {
			try {
				I_GetConceptData obj = (I_GetConceptData) t
						.getTransferData(conceptBeanFlavor);
				DropButton db = (DropButton) comp;
				db.doDrop(obj);
				AceLog.info("Dropped on DropButton: " + obj);
			} catch (UnsupportedFlavorException e) {
				AceLog.log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return true;
		}
		if (JTreeWithDragImage.class.isAssignableFrom(comp.getClass())) {
			try {
				JTreeWithDragImage tree = (JTreeWithDragImage) comp;
				AceFrameConfig config = tree.getConfig();
				I_GetConceptData obj = (I_GetConceptData) t
						.getTransferData(conceptBeanFlavor);
				new ExpandPathToNodeStateListener(tree, config, obj);
			} catch (UnsupportedFlavorException e) {
				AceLog.log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (DatabaseException e) {
				AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return true;
		}
		try {
			Method setMethod = comp.getClass().getMethod("setText",
					new Class[] { String.class });
			if (setMethod != null) {
				for (DataFlavor f : t.getTransferDataFlavors()) {
					if (f.equals(DataFlavor.stringFlavor)) {
						String s = (String) t
								.getTransferData(DataFlavor.stringFlavor);
						setMethod.invoke(comp, new Object[] { s });
					}
				}
			}
		} catch (NoSuchMethodException e) {
			if (AceLog.isLoggable(Level.FINE)) {
				AceLog.fine("Can't paste: " + e.toString());
			}
			// Nothing to do
		} catch (UnsupportedFlavorException e) {
			AceLog.log(Level.FINE, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			AceLog.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return false;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("Can import: " + comp);
		}
		if (I_ContainTermComponent.class.isAssignableFrom(comp.getClass())) {
			for (DataFlavor f : transferFlavors) {
				if (f.equals(conceptBeanFlavor)) {
					return true;
				}
			}
		}
		if (TerminologyList.class.isAssignableFrom(comp.getClass())) {
			for (DataFlavor f : transferFlavors) {
				if (f.equals(conceptBeanFlavor)) {
					return true;
				}
			}
		}
		if (JTable.class.isAssignableFrom(comp.getClass())) {
			JTable table = (JTable) comp;
			Point mouseLoc = table.getMousePosition();
			if (mouseLoc != null) {
				return true;
			}
		}
		if (DropButton.class.isAssignableFrom(comp.getClass())) {
			return true;
		}
		if (JTreeWithDragImage.class.isAssignableFrom(comp.getClass())) {
			return true;
		}
		try {
			if ((comp.getClass().getMethod("setText",
					new Class[] { String.class }) != null)) {
				for (DataFlavor f : transferFlavors) {
					if (f.equals(DataFlavor.stringFlavor)) {
						return true;
					}
				}
			}
		} catch (NoSuchMethodException e) {
			if (AceLog.isLoggable(Level.FINE)) {
				AceLog.fine("Can't paste: " + e.toString());
			}
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent c) {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("getSourceActions ");
		}
		return COPY_OR_MOVE;
	}

	public static DataFlavor[] getSupportedFlavors() {
		if (AceLog.isLoggable(Level.FINE)) {
			AceLog.fine("getSupportedFlavors ");
		}
		return supportedFlavors;
	}

}
