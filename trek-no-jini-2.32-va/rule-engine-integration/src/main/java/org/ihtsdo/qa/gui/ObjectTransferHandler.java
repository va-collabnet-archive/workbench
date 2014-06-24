/*
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.qa.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.DescriptionTransferable;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.modeler.tool.I_GetItemForModel;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.ihtsdo.qa.gui.viewers.gui.TreeObj;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * The Class ObjectTransferHandler.
 */
public class ObjectTransferHandler extends TransferHandler {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant TARGET_LIST_NAME. */
	public final static String TARGET_LIST_NAME = "targetLanguageList";

	/** The indices. */
	private int[] indices = null;

	/** The add index. */
	private int addIndex = -1; // Location where items were added

	/** The add count. */
	private int addCount = 0; // Number of items added.

	/** The config. */
	private I_ConfigAceFrame config;

	/** The get item. */
	private I_GetItemForModel getItem;

	/** The concept bean flavor. */
	public static DataFlavor conceptBeanFlavor;

	/** The thin desc versioned flavor. */
	public static DataFlavor thinDescVersionedFlavor;

	/** The thin desc tuple flavor. */
	public static DataFlavor thinDescTupleFlavor;

	/** The supported flavors. */
	public static DataFlavor[] supportedFlavors;

	/**
	 * Instantiates a new object transfer handler.
	 * 
	 * @param config
	 *            the config
	 * @param getItem
	 *            the get item
	 */
	public ObjectTransferHandler(I_ConfigAceFrame config, I_GetItemForModel getItem) {
		this.config = config;
		this.getItem = getItem;
		if (conceptBeanFlavor == null) {
			try {
				conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
				thinDescVersionedFlavor = new DataFlavor(DescriptionTransferable.thinDescVersionedType);
				thinDescTupleFlavor = new DataFlavor(DescriptionTransferable.thinDescTupleType);
				supportedFlavors = new DataFlavor[] { thinDescVersionedFlavor, thinDescTupleFlavor, conceptBeanFlavor, FixedTerminologyTransferable.universalFixedConceptFlavor, FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor, FixedTerminologyTransferable.universalFixedDescFlavor,
						FixedTerminologyTransferable.universalFixedDescInterfaceFlavor, DataFlavor.stringFlavor };
			} catch (ClassNotFoundException e) {
				// should never happen.
				throw new RuntimeException(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent,
	 * java.awt.datatransfer.Transferable)
	 */
	public boolean importData(JComponent c, Transferable t) {
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				DataFlavor conceptBeanFlavor;
				conceptBeanFlavor = this.conceptBeanFlavor;

				if (hasConceptBeanFlavor(t.getTransferDataFlavors(), conceptBeanFlavor)) {
					if (c instanceof JLabel) {
						
						I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
						if (getItem == null) {
							((JLabel) c).setText(concept.toString());
						} else {
							try {
								getItem.getItemFromConcept(concept);
							} catch (Exception e) {
								AceLog.getAppLog().info(e.getMessage());
								AceLog.getAppLog().alertAndLogException(e);
								return false;
							}
						}
					} else if (c instanceof JTextField) {

						I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
						if (getItem == null) {
							((JTextField) c).setText(concept.toString());
						} else {
							try {
								getItem.getItemFromConcept(concept);
							} catch (Exception e) {
								AceLog.getAppLog().info(e.getMessage());
								AceLog.getAppLog().alertAndLogException(e);
								return false;
							}
						}
					} else if (c instanceof JTable) {
						JTable table = (JTable) c;
						if (!c.getName().equals("droolsEnumTable")) {
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
										AceLog.getAppLog().alertAndLogException(e);
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
							AceLog.getAppLog().alertAndLogException(e);
						}
					}else if (c instanceof JComboBox) {
						JComboBox combo = (JComboBox) c;
						try {
							I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
							ConceptVersionBI conceptVersion = Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(),concept.getConceptNid());
							combo.addItem(conceptVersion);
							combo.setSelectedItem(conceptVersion);
						} catch (Exception e) {
							AceLog.getAppLog().alertAndLogException(e);
						}
					}  else {
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
									AceLog.getAppLog().alertAndLogException(e);
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
							AceLog.getAppLog().alertAndLogException(e);
						}
					}
				}
				return true;
			} catch (UnsupportedFlavorException ufe) {
				AceLog.getAppLog().info("importData: unsupported data flavor");
				AceLog.getAppLog().alertAndLogException(ufe);
			} catch (IOException ioe) {
				AceLog.getAppLog().info("importData: I/O exception");
				AceLog.getAppLog().alertAndLogException(ioe);
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	protected Transferable createTransferable(JComponent c) {
		I_GetConceptData concept = null;
		if (c instanceof JTable) {
			JTable table = (JTable) c;
			indices = table.getSelectedRows();
			TableModel model = table.getModel();
			Object conceptUuid = model.getValueAt(indices[0], 0);
			try {
				concept = Terms.get().getConcept(conceptUuid.toString()).iterator().next();
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (ParseException ex) {
                        Logger.getLogger(ObjectTransferHandler.class.getName()).log(Level.SEVERE, null, ex);
                    } 
		} else if (c instanceof JTree) {
			JTree tree = (JTree) c;
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			TreeObj conceptvbi = (TreeObj) tn.getUserObject();
			try {
				DescriptionVersionBI description = (DescriptionVersionBI)conceptvbi.getAtrValue();
				concept = Terms.get().getConcept(description.getConceptNid());
			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			JList list = (JList) c;
			indices = list.getSelectedIndices();
			Object[] values = list.getSelectedValues();
			concept = (I_GetConceptData) values[0];
		}
		return new ConceptTransferable(concept);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
	 */
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#exportDone(javax.swing.JComponent,
	 * java.awt.datatransfer.Transferable, int)
	 */
	protected void exportDone(JComponent c, Transferable data, int action) {
		if (action == MOVE) {
			if (indices != null) {
				if (!(c instanceof JTable)) {
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
			}
			indices = null;
			addCount = 0;
			addIndex = -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent,
	 * java.awt.datatransfer.DataFlavor[])
	 */
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

	/**
	 * Checks for concept bean flavor.
	 * 
	 * @param flavors
	 *            the flavors
	 * @param conceptBeanFlavor
	 *            the concept bean flavor
	 * @return true, if successful
	 */
	private boolean hasConceptBeanFlavor(DataFlavor[] flavors, DataFlavor conceptBeanFlavor) {
		for (int i = 0; i < flavors.length; i++) {
			if (conceptBeanFlavor.equals(flavors[i])) {
				return true;
			}
		}

		return false;
	}
}