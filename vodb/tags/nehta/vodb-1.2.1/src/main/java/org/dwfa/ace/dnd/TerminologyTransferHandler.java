/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
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
import org.dwfa.ace.DropButton;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.list.TerminologyIntList;
import org.dwfa.ace.list.TerminologyIntListModel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.ThinDescTuple;
import org.dwfa.vodb.types.ThinDescVersioned;

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
	
	public JComponent thisComponent;
	
	public static JComponent transferringComponent;

	public TerminologyTransferHandler(JComponent thisComponent) {
		super();
		this.thisComponent = thisComponent;

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
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Creating a transferable for: " + c);
		}
		transferringComponent = c;
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
		} else if (TerminologyIntList.class.isAssignableFrom(c.getClass())) {
			TerminologyIntList list = (TerminologyIntList) c;
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
				I_RelTuple rel = swrt.getTuple();
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
				case PATH:
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
					I_DescriptionTuple desc = swdt.getTuple();
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
					case PATH:
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
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("export done: " + source);
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
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("import: " + comp);
		}
		if (I_ContainTermComponent.class.isAssignableFrom(comp.getClass())) {
			I_ContainTermComponent ictc = (I_ContainTermComponent) comp;
			try {
				ConceptBean cb = null;
				
				if (t.isDataFlavorSupported(conceptBeanFlavor)) {
					Object obj = t.getTransferData(conceptBeanFlavor);
					if (obj == null) {
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							AceLog.getAppLog().fine("t has null obj " + t);
							AceLog.getAppLog().fine("t has null obj " + Arrays.asList(t.getTransferDataFlavors()));
						}
					}
					if (AceLog.getAppLog().isLoggable(Level.FINE)) {
						AceLog.getAppLog().fine("Transfer data for conceptBeanFlavor is: " + obj);
					}
					if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
						ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
						cb = cbt.getCoreBean();
					} else {
						cb = (ConceptBean) obj;
					}
				} else if (t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedConceptFlavor) ||
						t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
					Object obj = t.getTransferData(FixedTerminologyTransferable.universalFixedConceptFlavor);
					if (obj == null) {
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							AceLog.getAppLog().fine("t has null obj 2 " + t);
							AceLog.getAppLog().fine("t has null obj 2" + Arrays.asList(t.getTransferDataFlavors()));
						}
					}
					if (AceLog.getAppLog().isLoggable(Level.FINE)) {
						AceLog.getAppLog().fine("Transfer data for universalFixedConceptFlavor is: " + obj);
					}
					I_ConceptualizeUniversally uc = (I_ConceptualizeUniversally) obj;
					cb = ConceptBean.get(uc.getUids());
				} else if (t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedDescFlavor) ||
						t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedDescInterfaceFlavor)) {
					Object obj = t.getTransferData(FixedTerminologyTransferable.universalFixedConceptFlavor);
					if (obj == null) {
						if (AceLog.getAppLog().isLoggable(Level.FINE)) {
							AceLog.getAppLog().fine("t has null obj 2 " + t);
							AceLog.getAppLog().fine("t has null obj 2" + Arrays.asList(t.getTransferDataFlavors()));
						}
					}
					if (AceLog.getAppLog().isLoggable(Level.FINE)) {
						AceLog.getAppLog().fine("Transfer data for universalFixedConceptFlavor is: " + obj);
					}
					I_DescribeConceptUniversally ud = (I_DescribeConceptUniversally) obj;
					cb = ConceptBean.get(ud.getConcept().getUids());
				}
				ictc.setTermComponent(cb);
				return true;
			} catch (UnsupportedFlavorException e) {
				AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		if (TerminologyIntList.class.isAssignableFrom(comp.getClass())) {
			TerminologyIntList tl = (TerminologyIntList) comp;
			TerminologyIntListModel model = (TerminologyIntListModel) tl.getModel();
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
				AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		if (JTable.class.isAssignableFrom(comp.getClass())) {
			JTable table = (JTable) comp;
			Point mouseLoc = table.getMousePosition();
			if (mouseLoc != null) {
				int column = table.columnAtPoint(mouseLoc);
				int row = table.rowAtPoint(mouseLoc);
				AceLog.getAppLog().info("Dropping on JTable row: " + row + " column: "
						+ column);
				if (table.isCellEditable(row, column)) {
					try {
						I_GetConceptData obj = (I_GetConceptData) t
								.getTransferData(conceptBeanFlavor);
						table.setValueAt(obj.getConceptId(), row, column);
					} catch (UnsupportedFlavorException e) {
						AceLog.getAppLog().info("Unsupported flavor: " + e.getMessage());
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
					return true;
				} else {
					AceLog.getAppLog().info("Cell is not editable");
					return false;
				}
			} else {
				AceLog.getAppLog().info("mouseLoc is null");
				if (table.getSelectedRow() >= 0 && table.getSelectedColumn() >= 0) {
					I_GetConceptData obj;
					try {
						obj = (I_GetConceptData) t
						.getTransferData(conceptBeanFlavor);
						table.setValueAt(obj.getConceptId(), table.getSelectedRow(), table.getSelectedColumn());
						return true;
					} catch (UnsupportedFlavorException e) {
						AceLog.getAppLog().info("Unsupported flavor: " + e.getMessage());
					} catch (IOException e) {
						AceLog.getAppLog().alertAndLogException(e);
					}
				}
				return false;
			}
		}
		if (DropButton.class.isAssignableFrom(comp.getClass())) {
			try {
				I_GetConceptData obj = (I_GetConceptData) t
						.getTransferData(conceptBeanFlavor);
				DropButton db = (DropButton) comp;
				db.doDrop(obj);
				AceLog.getAppLog().info("Dropped on DropButton: " + obj);
			} catch (UnsupportedFlavorException e) {
				AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
			return true;
		}
		if (JTreeWithDragImage.class.isAssignableFrom(comp.getClass())) {
			try {
				JTreeWithDragImage tree = (JTreeWithDragImage) comp;
				I_ConfigAceFrame config = tree.getConfig();
				I_GetConceptData obj = (I_GetConceptData) t
						.getTransferData(conceptBeanFlavor);
				new ExpandPathToNodeStateListener(tree, config, obj);
			} catch (UnsupportedFlavorException e) {
				AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
			} catch (IOException e) {
				AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
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
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("Can't paste: " + e.toString());
			}
			// Nothing to do
		} catch (UnsupportedFlavorException e) {
			AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
		} catch (IOException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalArgumentException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (IllegalAccessException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
		} catch (InvocationTargetException e) {
			AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return false;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Can import: " + comp.getClass().getCanonicalName());
		}
		if (I_ContainTermComponent.class.isAssignableFrom(comp.getClass())) {
			for (DataFlavor f : transferFlavors) {
				if (f.equals(conceptBeanFlavor)) {
					return true;
				}
			}
		}
		if (TerminologyList.class.isAssignableFrom(comp.getClass())) {
			if (thisComponent == transferringComponent) {
				return false;
			}
			for (DataFlavor f : transferFlavors) {
				if (f.equals(conceptBeanFlavor)) {
					return true;
				}
			}
		}
		if (TerminologyIntList.class.isAssignableFrom(comp.getClass())) {
			for (DataFlavor f : transferFlavors) {
				if (f.equals(conceptBeanFlavor)) {
					return true;
				}
			}
		}
		if (JTable.class.isAssignableFrom(comp.getClass())) {
			JTable table = (JTable) comp;
			TableModel model = table.getModel();
			if (TableSorter.class.isAssignableFrom(model.getClass())) {
				TableSorter sorter = (TableSorter) model;
				model = sorter.getTableModel();
			}
			if (DescriptionsFromCollectionTableModel.class.isAssignableFrom(model.getClass())) {
				return false;
			}
			Point mouseLoc = table.getMousePosition();
			if (mouseLoc != null) {
				return true;
			}
		}
		if (DropButton.class.isAssignableFrom(comp.getClass())) {
			return true;
		}
		if (JTreeWithDragImage.class.isAssignableFrom(comp.getClass())) {
			return false;
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
			if (AceLog.getAppLog().isLoggable(Level.FINE)) {
				AceLog.getAppLog().fine("Can't paste: " + e.toString());
			}
		}
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("Can import: " + comp.getClass().getCanonicalName() + " false");
		}
		return false;
	}

	@Override
	public int getSourceActions(JComponent c) {
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("getSourceActions ");
		}
		return COPY;
	}

	public static DataFlavor[] getSupportedFlavors() {
		if (AceLog.getAppLog().isLoggable(Level.FINE)) {
			AceLog.getAppLog().fine("getSupportedFlavors TerminologyTransferHandler");
		}
		return supportedFlavors;
	}

}
