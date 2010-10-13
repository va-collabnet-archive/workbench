package org.ihtsdo.qa.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.DescriptionTransferable;
import org.dwfa.ace.modeler.tool.I_GetItemForModel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;

public class ObjectTransferHandler extends TransferHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String TARGET_LIST_NAME = "targetLanguageList";
	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added.
	private I_ConfigAceFrame config;
	private I_GetItemForModel getItem;
	public static DataFlavor conceptBeanFlavor;
	public static DataFlavor thinDescVersionedFlavor;
	public static DataFlavor thinDescTupleFlavor;
	public static DataFlavor[] supportedFlavors;


	public ObjectTransferHandler(I_ConfigAceFrame config, I_GetItemForModel getItem) {
		this.config = config;
		this.getItem = getItem;
		if (conceptBeanFlavor == null) {
            try {
                conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
                thinDescVersionedFlavor = new DataFlavor(DescriptionTransferable.thinDescVersionedType);
                thinDescTupleFlavor = new DataFlavor(DescriptionTransferable.thinDescTupleType);
                supportedFlavors = new DataFlavor[] { thinDescVersionedFlavor, thinDescTupleFlavor, conceptBeanFlavor,
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

	public boolean importData(JComponent c, Transferable t) {
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				DataFlavor conceptBeanFlavor;
				conceptBeanFlavor = this.conceptBeanFlavor;

				if (hasConceptBeanFlavor(t.getTransferDataFlavors(), conceptBeanFlavor)) {
					if (c instanceof JTextField) {

						I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
						if (getItem == null) {
							((JTextField) c).setText(concept.toString());
						} else {
							try {
								getItem.getItemFromConcept(concept);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
								return false;
							}
						}
					} else if (c instanceof JTable) {
						JTable table = (JTable) c;
						if(!c.getName().equals("droolsEnumTable")){
							return false;
						}
						DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
						int index = table.getSelectedRow();
						if (indices != null && index >= (indices[0] - 1) && index <= indices[indices.length - 1]) {
							indices = null;
							return false;
						}
						int max = tableModel.getRowCount();
						if (index < 0) {
							index = max;
						} else {
							index++;
							if (index > max) {
								index = max;
							}
						}
						try {
							I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
							Vector<Vector<Object>> tableData = tableModel.getDataVector();
							if (this.config == null) {
								this.config = Terms.get().getActiveAceFrameConfig();
								Object obj = null;
								if (getItem != null && concept != null) {
									try {
										for (Vector<Object> vector : tableData) {
											if (vector.get(0).toString().equals(concept.toString())) {
												return false;
											}
										}
										obj = getItem.getItemFromConcept(concept);
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									obj = concept;
								}
								addIndex = index;
								addCount = 1;

								if (obj != null) {
									for (Vector<Object> vector : tableData) {
										if (vector.get(0).toString().equals(concept.toString())) {
											return false;
										}
									}
									tableModel.addRow(new Object[] { obj });
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						JList target = (JList) c;
						DefaultListModel listModel = (DefaultListModel) target.getModel();
						int index = target.getSelectedIndex();
						if (indices != null && index >= (indices[0] - 1) && index <= indices[indices.length - 1]) {
							indices = null;
							return false;
						}
						int max = listModel.getSize();
						if (index < 0) {
							index = max;
						} else {
							index++;
							if (index > max) {
								index = max;
							}
						}
						try {
							I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
							if (this.config == null)
								this.config = Terms.get().getActiveAceFrameConfig();
							Object obj = null;
							if (getItem != null && concept != null) {
								try {
									for (int i = 0; i < listModel.getSize(); i++) {
										String objString = listModel.get(i).toString();
										if (objString.equals(concept.toString())) {
											return false;
										}
									}
									obj = getItem.getItemFromConcept(concept);
								} catch (Exception e) {
									e.printStackTrace();
								}
							} else {
								obj = concept;
							}
							addIndex = index;
							addCount = 1;

							if (obj != null) {
								if (c.getName() != null && c.getName().equals(TARGET_LIST_NAME)) {
									listModel.removeAllElements();
									listModel.add(0, obj);
								} else {
									for (int i = 0; i < listModel.getSize(); i++) {
										String objString = listModel.get(i).toString();
										if (objString.equals(obj.toString())) {
											return false;
										}
									}
									listModel.add(index++, obj);
								}
							}
							// }
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
					}
				}
				return true;
			} catch (UnsupportedFlavorException ufe) {
				System.out.println("importData: unsupported data flavor");
				ufe.printStackTrace();
			} catch (IOException ioe) {
				System.out.println("importData: I/O exception");
				ioe.printStackTrace();
			}
		}
		return false;
	}

	protected Transferable createTransferable(JComponent c) {
		JList list = (JList) c;
		indices = list.getSelectedIndices();
		Object[] values = list.getSelectedValues();
		I_GetConceptData concept = (I_GetConceptData) values[0];

		return new ConceptTransferable(concept);
	}

	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	protected void exportDone(JComponent c, Transferable data, int action) {
		if (action == MOVE) {
			if (indices != null) {
				JList source = (JList) c;
				DefaultListModel model = (DefaultListModel) source.getModel();
				// If we are moving items around in the same list, we
				// need to adjust the indices accordingly, since those
				// after the insertion point have moved.
				if (addCount > 0) {
					for (int i = 0; i < indices.length; i++) {
						if (indices[i] > addIndex) {
							indices[i] += addCount;
						}
					}
				}
				for (int i = indices.length - 1; i >= 0; i--) {
					model.remove(indices[i]);
				}
			}
			indices = null;
			addCount = 0;
			addIndex = -1;
		}
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		if (c.isEnabled()) {
			DataFlavor conceptBeanFlavor;
			conceptBeanFlavor = this.conceptBeanFlavor;

			if (hasConceptBeanFlavor(flavors, conceptBeanFlavor)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasConceptBeanFlavor(DataFlavor[] flavors, DataFlavor conceptBeanFlavor) {
		for (int i = 0; i < flavors.length; i++) {
			if (conceptBeanFlavor.equals(flavors[i])) {
				return true;
			}
		}

		return false;
	}
}