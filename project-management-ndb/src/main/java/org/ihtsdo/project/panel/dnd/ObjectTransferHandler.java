package org.ihtsdo.project.panel.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.logging.Level;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.ihtsdo.project.panel.details.ProjectDetailsPanel;

public class ObjectTransferHandler extends TransferHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[] indices = null;
	private int addIndex = -1; // Location where items were added
	private int addCount = 0; // Number of items added.
	private I_ConfigAceFrame config;
	private I_GetItemForModel getItem;

	public ObjectTransferHandler(I_ConfigAceFrame config, I_GetItemForModel getItem) {
		this.config = config;
		this.getItem = getItem;
	}

	public boolean importData(JComponent c, Transferable t) {
		boolean result = false;
		if (canImport(c, t.getTransferDataFlavors())) {
			try {
				DataFlavor conceptBeanFlavor;
				conceptBeanFlavor = TerminologyTransferHandler.conceptBeanFlavor;

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
								return result;
							}
						}
					} else if (c instanceof JLabel) {

						I_GetConceptData concept = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
						if (getItem == null) {
							((JLabel) c).setText(concept.toString());
						} else {
							try {
								getItem.getItemFromConcept(concept);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
								return result;
							}
						}
					} else {
						JList target = (JList) c;
						DefaultListModel listModel = (DefaultListModel) target.getModel();
						int index = target.getSelectedIndex();
						// Prevent the user from dropping data back on itself.
						// For example, if the user is moving items #4,#5,#6 and
						// #7 and
						// attempts to insert the items after item #5, this
						// would
						// be problematic when removing the original items.
						// So this is not allowed.
						if (indices != null && index >= (indices[0] - 1) && index <= indices[indices.length - 1]) {
							indices = null;
							return result;
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
							Object[] values = null;
							try {
								values = (Object[]) t.getTransferData(conceptBeanFlavor);
							} catch (ClassCastException e) {
							}
							if (values == null) {
								values = new Object[1];
								values[0] = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
							}
							for (Object concept : values) {

								if (this.config == null)
									this.config = Terms.get().getActiveAceFrameConfig();
								Object obj = null;
								if (getItem != null && concept != null) {
									try {
										boolean duplicated = false;
										for (int i = 0; i < listModel.getSize(); i++) {
											String objString = listModel.get(i).toString();
											if (objString.equals(concept.toString())) {
												duplicated = true;
											}
										}
										if(!duplicated){
											obj = getItem.getItemFromConcept((I_GetConceptData)concept);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else {
									obj = concept;
								}
								addIndex = index;
								addCount = 1;
								
								if (obj != null) {
									if (c.getName() != null && c.getName().equals(ProjectDetailsPanel.TARGET_LIST_NAME)) {
										listModel.removeAllElements();
										listModel.add(0, obj);
									} else {
										boolean duplicated = false;
										for (int i = 0; i < listModel.getSize(); i++) {
											String objString = listModel.get(i).toString();
											if (objString.equals(obj.toString())) {
												duplicated = true;
											}
										}
										if(!duplicated){
											listModel.add(index++, obj);
										}
									}
								}
							}
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
					}
				}
				return result;
			} catch (UnsupportedFlavorException ufe) {
				System.out.println("importData: unsupported data flavor");
				ufe.printStackTrace();
			} catch (IOException ioe) {
				System.out.println("importData: I/O exception");
				ioe.printStackTrace();
			}
		}
		return result;
	}

	protected Transferable createTransferable(JComponent c) {
		JList list = (JList) c;
		indices = list.getSelectedIndices();
		Object[] values = list.getSelectedValues();
		I_GetConceptData[] concepts = new I_GetConceptData[values.length];
		if (values.length == 1) {
			return new ConceptTransferable((I_GetConceptData) values[0]);
		}
		for (int i = 0; i < values.length; i++) {
			I_GetConceptData i_GetConceptData = (I_GetConceptData) values[i];
			concepts[i] = i_GetConceptData;
		}
		return new MyConceptTransferable(concepts);
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
			conceptBeanFlavor = TerminologyTransferHandler.conceptBeanFlavor;
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

class MyConceptTransferable extends ConceptTransferable {
	private I_GetConceptData[] concepts;
	public DataFlavor conceptBeanFlavor;
	DataFlavor[] supportedFlavors;

	public MyConceptTransferable(I_GetConceptData concept) {
		super(concept);
	}

	public MyConceptTransferable(I_GetConceptData[] concepts) {
		super(concepts[0]);
		this.concepts = concepts;
		try {
			conceptBeanFlavor = new DataFlavor(conceptBeanType);
		} catch (ClassNotFoundException e) {
			// should never happen.
			throw new RuntimeException(e);
		}
		supportedFlavors = new DataFlavor[] { conceptBeanFlavor, FixedTerminologyTransferable.universalFixedConceptFlavor, FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
				DataFlavor.stringFlavor };
	}

	public Object[] getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		for (I_GetConceptData concpet : concepts) {
			if (concepts == null) {
				return null;
			}
			if (flavor.equals(conceptBeanFlavor)) {
				return concepts;
			} else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptFlavor)) {
				try {
					I_ConceptualizeUniversally[] iconftUni = new I_ConceptualizeUniversally[concepts.length];
					for (int i = 0; i < concepts.length; i++) {
						I_ConceptualizeUniversally i_ConceptualizeUniversally = concepts[i].getConceptAttributes().getLocalFixedConcept().universalize();
						iconftUni[i] = i_ConceptualizeUniversally;
					}
					return iconftUni;
				} catch (IOException e) {
					AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else if (flavor.equals(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
				try {
					I_ConceptualizeUniversally[] iconftUni = new I_ConceptualizeUniversally[concepts.length];
					for (int i = 0; i < concepts.length; i++) {
						I_ConceptualizeUniversally i_ConceptualizeUniversally = concepts[i].getConceptAttributes().getLocalFixedConcept().universalize();
						iconftUni[i] = i_ConceptualizeUniversally;
					}
					return iconftUni;
				} catch (IOException e) {
					AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
				} catch (TerminologyException e) {
					AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else if (flavor.equals(DataFlavor.stringFlavor)) {
				String[] stringConcepts = new String[concepts.length];
				for (int i = 0; i < concepts.length; i++) {
					I_GetConceptData concept = concepts[i];
					stringConcepts[i] = concept.toString();
				}
				return stringConcepts;
			}
			throw new UnsupportedFlavorException(flavor);
		}
		return null;
	}

}