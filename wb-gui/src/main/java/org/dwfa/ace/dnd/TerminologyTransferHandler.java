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

import java.awt.HeadlessException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.dwfa.ace.ACE;
import org.dwfa.ace.DropButton;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_ContainTermComponent;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartBoolean;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartInt;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.classifier.DiffTableModel;
import org.dwfa.ace.classifier.EquivTableModel;
import org.dwfa.ace.list.TerminologyIntList;
import org.dwfa.ace.list.TerminologyIntListModel;
import org.dwfa.ace.list.TerminologyList;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionTableModel;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.table.RelTableModel;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.ace.table.DescriptionTableModel.DESC_FIELD;
import org.dwfa.ace.table.DescriptionTableModel.StringWithDescTuple;
import org.dwfa.ace.table.RelTableModel.REL_FIELD;
import org.dwfa.ace.table.RelTableModel.StringWithRelTuple;
import org.dwfa.ace.table.refset.RefsetMemberTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.RefsetMemberTableModel.REFSET_FIELDS;
import org.dwfa.ace.tree.ConceptBeanForTree;
import org.dwfa.ace.tree.ExpandPathToNodeStateListener;
import org.dwfa.ace.tree.JTreeWithDragImage;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.dnd.FixedTerminologyTransferable;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WORKFLOW_FIELD;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel.WorkflowStringWithConceptTuple;
import org.ihtsdo.arena.conceptview.ConceptViewTitle;
import org.ihtsdo.arena.conceptview.FocusDrop;
import org.ihtsdo.arena.conceptview.I_AcceptConcept;

public class TerminologyTransferHandler extends TransferHandler {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    public static DataFlavor conceptBeanFlavor;

    public static DataFlavor thinDescVersionedFlavor;

    public static DataFlavor thinDescTupleFlavor;

    public static DataFlavor[] supportedFlavors;

    private static int fsnPrimoridalNid;
    
    public JComponent thisComponent;

    public static JComponent transferringComponent;

    public TerminologyTransferHandler(JComponent thisComponent) {
        super();
        this.thisComponent = thisComponent;

        if (conceptBeanFlavor == null) {
            try {
            	fsnPrimoridalNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getPrimoridalUid()).getNid();
                conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
                thinDescVersionedFlavor = new DataFlavor(DescriptionTransferable.thinDescVersionedType);
                thinDescTupleFlavor = new DataFlavor(DescriptionTransferable.thinDescTupleType);
                supportedFlavors =
                        new DataFlavor[] { thinDescVersionedFlavor, thinDescTupleFlavor, conceptBeanFlavor,
                                          FixedTerminologyTransferable.universalFixedConceptFlavor,
                                          FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor,
                                          FixedTerminologyTransferable.universalFixedDescFlavor,
                                          FixedTerminologyTransferable.universalFixedDescInterfaceFlavor,
                                          DataFlavor.stringFlavor };
            } catch (ClassNotFoundException e) {
                // should never happen.
                throw new RuntimeException(e);
            } catch (TerminologyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
            }
        }
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        try {
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Creating a transferable for: " + c);
            }
            transferringComponent = c;
            if (JTree.class.isAssignableFrom(c.getClass())) {
                JTree tree = (JTree) c;
                Object obj = tree.getLastSelectedPathComponent();
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) obj;
                if (node != null) {
                    return new ConceptTransferable((I_GetConceptData) node.getUserObject());
                }
                return new StringSelection("null");

            } else if (TerminologyList.class.isAssignableFrom(c.getClass())) {
                TerminologyList list = (TerminologyList) c;
                return new ConceptTransferable((I_GetConceptData) list.getSelectedValue());
            } else if (TerminologyIntList.class.isAssignableFrom(c.getClass())) {
                TerminologyIntList list = (TerminologyIntList) c;
                return new ConceptTransferable((I_GetConceptData) list.getSelectedValue());
            } else if (JTable.class.isAssignableFrom(c.getClass())) {
                JTable termTable = (JTable) c;
                TableModel tableModel = termTable.getModel();
                if (RefsetMemberTableModel.class.isAssignableFrom(tableModel.getClass())) {
                    TableModel dtm = termTable.getModel();
                    if (termTable.getSelectedRow() >= 0) {
                        int selectedRow = termTable.getSelectedRow();
                        int selectedColumn = termTable.getSelectedColumn();
                        int modelRow = termTable.convertRowIndexToModel(selectedRow);
                        int modelColumn = termTable.convertColumnIndexToModel(selectedColumn);
                        if (modelColumn < 0) {
                        	modelColumn = 0;
                        }
                        StringWithExtTuple swet = (StringWithExtTuple) dtm.getValueAt(modelRow, modelColumn);
                        I_ExtendByRefVersion extVersion = swet.getTuple();
                        TableColumn column = termTable.getColumnModel().getColumn(termTable.getSelectedColumn());
                        REFSET_FIELDS columnField = (REFSET_FIELDS) column.getIdentifier();
                        switch (columnField) {
						case BOOLEAN_VALUE:
	                        return new StringSelection(Boolean.toString(
	                        		((I_ExtendByRefPartBoolean) extVersion.getMutablePart()).getBooleanValue()));
						case COMPONENT_ID:
	                        return new ConceptTransferable(Terms.get().getConcept(extVersion.getComponentId()));
						case CONCEPT_ID:
	                        return new ConceptTransferable(Terms.get().getConcept(
	                        		((I_ExtendByRefPartCid) extVersion.getMutablePart()).getC1id()));
						case INTEGER_VALUE:
	                        return new StringSelection(Integer.toString(
	                        		((I_ExtendByRefPartInt) extVersion.getMutablePart()).getIntValue()));
						case MEMBER_ID:
	                        return new ConceptTransferable(Terms.get().getConcept(extVersion.getComponentId()));
						case PATH:
	                        return new ConceptTransferable(Terms.get().getConcept(extVersion.getPathNid()));
						case REFSET_ID:
	                        return new ConceptTransferable(Terms.get().getConcept(extVersion.getRefsetId()));
						case STATUS:
	                        return new ConceptTransferable(Terms.get().getConcept(extVersion.getStatusNid()));
						case STRING_VALUE:
	                        return new StringSelection(
	                        		((I_ExtendByRefPartStr) extVersion.getMutablePart()).getStringValue());
						case VERSION:
	                        return new StringSelection(swet.getCellText());
						default:
	                        throw new UnsupportedOperationException("Can't convert " + columnField + " to a transferable");
						}
                    }
                    return null;
                	
                } else if (RelTableModel.class.isAssignableFrom(tableModel.getClass())) {
                    TableModel rtm = termTable.getModel();
                    int selectedRow = termTable.getSelectedRow();
                    int selectedColumn = termTable.getSelectedColumn();
                    int modelRow = termTable.convertRowIndexToModel(selectedRow);
                    int modelColumn = termTable.convertColumnIndexToModel(selectedColumn);
                    if (modelColumn < 0) {
                    	modelColumn = 0;
                    }

                    StringWithRelTuple swrt = (StringWithRelTuple) rtm.getValueAt(modelRow, modelColumn);
                    I_RelTuple rel = swrt.getTuple();
                    TableColumn column = termTable.getColumnModel().getColumn(termTable.getSelectedColumn());
                    REL_FIELD columnDesc = (REL_FIELD) column.getIdentifier();
                    switch (columnDesc) {
                    case SOURCE_ID:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getC1Id()));
                    case REL_TYPE:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getTypeNid()));
                    case DEST_ID:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getC2Id()));
                    case REFINABILITY:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getRefinabilityId()));
                    case CHARACTERISTIC:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getCharacteristicId()));
                    case STATUS:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getStatusNid()));
                    case PATH:
                        return new ConceptTransferable(Terms.get().getConcept(rel.getPathNid()));
                    case REL_ID:
                        return new StringSelection(rel.toString());
                    case VERSION:
                        return new StringSelection(new Date(rel.getTime()).toString());
                    case GROUP:
                        return new StringSelection("" + rel.getGroup());
                    default:
                        throw new UnsupportedOperationException("Can't convert " + columnDesc + " to a concept bean");
                    }
                } else if (DescriptionTableModel.class.isAssignableFrom(tableModel.getClass())) {
                    TableModel dtm = termTable.getModel();
                    if (termTable.getSelectedRow() >= 0) {
                        int selectedRow = termTable.getSelectedRow();
                        int selectedColumn = termTable.getSelectedColumn();
                        int modelRow = termTable.convertRowIndexToModel(selectedRow);
                        int modelColumn = termTable.convertColumnIndexToModel(selectedColumn);
                        if (modelColumn < 0) {
                        	modelColumn = 0;
                        }
                        StringWithDescTuple swdt = (StringWithDescTuple) dtm.getValueAt(modelRow, modelColumn);
                        if (swdt != null) {
                            I_DescriptionTuple desc = swdt.getTuple();
                            TableColumn column = termTable.getColumnModel().getColumn(termTable.getSelectedColumn());
                            DESC_FIELD columnDesc = (DESC_FIELD) column.getIdentifier();
                            switch (columnDesc) {

                            case CON_ID:
                                return new ConceptTransferable(Terms.get().getConcept(desc.getConceptNid()));
                            case STATUS:
                                return new ConceptTransferable(Terms.get().getConcept(desc.getStatusNid()));
                            case TYPE:
                                return new ConceptTransferable(Terms.get().getConcept(desc.getTypeNid()));
                            case CASE_FIXED:
                                return new StringSelection(Boolean.toString(desc.isInitialCaseSignificant()));
                            case LANG:
                                return new StringSelection(desc.getLang());
                            case TEXT:
                                return new DescriptionTransferable(desc);
                            case PATH:
                                return new ConceptTransferable(Terms.get().getConcept(desc.getPathNid()));
                            case DESC_ID:
                                return new StringSelection(desc.toString());
                            case VERSION:
                                return new StringSelection(new Date(desc.getTime()).toString());
                            default:
                                throw new UnsupportedOperationException("Can't convert " + columnDesc
                                    + " to a concept bean");
                            }
                        } else {
                            JOptionPane.showMessageDialog(termTable, "No valid row is selected.", "Copy error",
                                    JOptionPane.ERROR_MESSAGE);
                                return null;

                        }
                    } else {
                        JOptionPane.showMessageDialog(termTable, "No row is selected.", "Copy error",
                            JOptionPane.ERROR_MESSAGE);
                        return null;
                    }
                } else if (DiffTableModel.class.isAssignableFrom(tableModel.getClass())) {

                    AceLog.getAppLog().info("\r\n::: FOUND JTable type: " + tableModel.getClass().toString());
                    DiffTableModel diffTableModel = (DiffTableModel) tableModel;
                    int selectedRow = termTable.getSelectedRow();
                    int modelRow = termTable.convertRowIndexToModel(selectedRow);
                    int nid = diffTableModel.getNidAt(modelRow, 0);
                    if (nid == Integer.MIN_VALUE)
                        return null;
                    return new ConceptTransferable(Terms.get().getConcept(nid));

                } else if (EquivTableModel.class.isAssignableFrom(tableModel.getClass())) {

                    AceLog.getAppLog().info("\r\n::: FOUND JTable type: " + tableModel.getClass().toString());
                    EquivTableModel equivTableModel = (EquivTableModel) tableModel;
                    int selectedRow = termTable.getSelectedRow();
                    int modelRow = termTable.convertRowIndexToModel(selectedRow);
                    int nid = equivTableModel.getNidAt(modelRow, 0);
                    if (nid == Integer.MIN_VALUE)
                        return null;
                    return new ConceptTransferable(Terms.get().getConcept(nid));
			    } else if (WorkflowHistoryTableModel.class.isAssignableFrom(tableModel.getClass())) {
			        TableModel wftm = termTable.getModel();
			        if (termTable.getSelectedRow() >= 0) 
			        {
			        	StringWithConceptTuple field = null;
		            	WorkflowStringWithConceptTuple conField = null;

		            	TableColumn column = termTable.getColumnModel().getColumn(termTable.getSelectedColumn());
			            WORKFLOW_FIELD columnDesc = (WORKFLOW_FIELD) column.getIdentifier();

			        	Object value = wftm.getValueAt(termTable.getSelectedRow(), termTable.getSelectedColumn());
        	
			        	if (columnDesc != WORKFLOW_FIELD.FSN)
		            		field = (StringWithConceptTuple)value;

			            switch (columnDesc) {
				            case FSN:
				            	int descId = 0;
				            	conField = (WorkflowStringWithConceptTuple)value;
				                //return new ConceptTransferable(Terms.get().getConcept(conField.getTuple().getConceptNid()));
				            	
				            	I_GetConceptData con = Terms.get().getConcept(conField.getTuple().getConceptNid());
				            	Collection<? extends I_DescriptionVersioned> descs = con.getDescriptions();
				            	for (I_DescriptionVersioned version : descs)
				            	{
				            		if (version.getTypeNid() == fsnPrimoridalNid)
				            		{
				            			descId = version.getDescId();
				            			break;
				            		}
				            	}
				            	return new DescriptionTransferable(Terms.get().getDescription(descId));
//				            case ACTION:
//				                return new StringSelection(field.getCellText());
				            case STATE:
				                return new StringSelection(field.getCellText());
				            case EDITOR:
				                return new StringSelection(field.getCellText());
//		                    case PATH:
//				                return new StringSelection(field.getCellText());
				            case TIMESTAMP:
//				            	conField = (StringWithConceptTuple)value;
//				                return new ConceptTransferable(Terms.get().getConcept(conField.getTuple().getConceptNid()));
				                return new StringSelection(field.getCellText());
			                default:
				                throw new UnsupportedOperationException("Can't convert " + columnDesc + " to a concept bean");
			            }
			        } else {
			            JOptionPane.showMessageDialog(termTable, "No row is selected.", "Copy error",
			                JOptionPane.ERROR_MESSAGE);
			            return null;
			        }
                } else {
                    throw new UnsupportedOperationException("JTable type: " + tableModel.getClass().toString());
                }
            } else {
                I_ContainTermComponent ictc = (I_ContainTermComponent) c;
                return new ConceptTransferable((I_GetConceptData) ictc.getTermComponent());
            }
        } catch (HeadlessException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        if (I_AcceptConcept.class.isAssignableFrom(comp.getClass())) {
            try {
                I_AcceptConcept title = (I_AcceptConcept) comp;
                if (t.isDataFlavorSupported(conceptBeanFlavor)) {
                    Object obj = t.getTransferData(conceptBeanFlavor);
                    if (obj == null) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().fine("t has null obj " + t);
                            AceLog.getAppLog().fine("t has null obj " + Arrays.asList(t.getTransferDataFlavors()));
                        }
                        return false;
                    }
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("Transfer data for conceptBeanFlavor is: " + obj);
                    }
                    if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
                        ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
                        title.sendConcept(cbt.getCoreBean());
                        return true;
                    } else {
                        title.sendConcept((I_GetConceptData) obj);
                        return true;
                    }
                }
            } catch (UnsupportedFlavorException e) {
                AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }

        if (I_ContainTermComponent.class.isAssignableFrom(comp.getClass())) {
            I_ContainTermComponent ictc = (I_ContainTermComponent) comp;
            try {
                I_GetConceptData cb = null;

                if (t.isDataFlavorSupported(conceptBeanFlavor)) {
                    Object obj = t.getTransferData(conceptBeanFlavor);
                    if (obj == null) {
                        if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                            AceLog.getAppLog().fine("t has null obj " + t);
                            AceLog.getAppLog().fine("t has null obj " + Arrays.asList(t.getTransferDataFlavors()));
                        }
                        return false;
                    }
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("Transfer data for conceptBeanFlavor is: " + obj);
                    }
                    if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
                        ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
                        cb = cbt.getCoreBean();
                    } else {
                        cb = (I_GetConceptData) obj;
                    }
                } else if (t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedConceptFlavor)
                    || t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedConceptInterfaceFlavor)) {
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
                    cb = Terms.get().getConcept(uc.getUids());
                } else if (t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedDescFlavor)
                    || t.isDataFlavorSupported(FixedTerminologyTransferable.universalFixedDescInterfaceFlavor)) {
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
                    cb = Terms.get().getConcept(ud.getConcept().getUids());
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
                I_GetConceptData cb;
                if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
                    ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
                    cb = cbt.getCoreBean();
                } else {
                    cb = (I_GetConceptData) obj;
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
                I_GetConceptData cb;
                if (ConceptBeanForTree.class.isAssignableFrom(obj.getClass())) {
                    ConceptBeanForTree cbt = (ConceptBeanForTree) obj;
                    cb = cbt.getCoreBean();
                } else {
                    cb = (I_GetConceptData) obj;
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
                AceLog.getAppLog().info("Dropping on JTable row: " + row + " column: " + column);
                if (table.isCellEditable(row, column)) {
                    try {
                        I_GetConceptData obj = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
                        table.setValueAt(obj.getConceptNid(), row, column);
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
                        obj = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
                        table.setValueAt(obj.getConceptNid(), table.getSelectedRow(), table.getSelectedColumn());
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
                I_GetConceptData obj = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
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
                I_GetConceptData obj = (I_GetConceptData) t.getTransferData(conceptBeanFlavor);
                new ExpandPathToNodeStateListener(tree, config, obj);
            } catch (UnsupportedFlavorException e) {
                AceLog.getAppLog().log(Level.FINE, e.getLocalizedMessage(), e);
            } catch (IOException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            } catch (TerminologyException e) {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
            return true;
        }
        try {
            Method setMethod = comp.getClass().getMethod("setText", new Class[] { String.class });
            if (setMethod != null) {
                for (DataFlavor f : t.getTransferDataFlavors()) {
                    if (f.equals(DataFlavor.stringFlavor)) {
                        String s = (String) t.getTransferData(DataFlavor.stringFlavor);
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
        if (ConceptViewTitle.class.isAssignableFrom(comp.getClass())) {
            for (DataFlavor f : transferFlavors) {
                if (f.equals(conceptBeanFlavor)) {
                    return true;
                }
            }
        }
        if (FocusDrop.class.isAssignableFrom(comp.getClass())) {
            for (DataFlavor f : transferFlavors) {
                if (f.equals(conceptBeanFlavor)) {
                    return true;
                }
            }
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
            if (DescriptionsFromCollectionTableModel.class.isAssignableFrom(model.getClass())) {
                return false;
            }
            Point loc = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(loc, table);
            boolean contains = table.contains(loc);
            if (contains) {
                int rowIndex = table.rowAtPoint(loc);
                int columnIndex = table.columnAtPoint(loc);
                if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                    AceLog.getAppLog().fine(
                        "table.getMousePosition() rowIndex " + rowIndex + " columnIndex " + columnIndex + " editable: "
                            + model.isCellEditable(rowIndex, columnIndex));
                }
                if (model.isCellEditable(rowIndex, columnIndex)) {
                    return true;
                }
            }
        }
        if (DropButton.class.isAssignableFrom(comp.getClass())) {
            return true;
        }
        if (JTreeWithDragImage.class.isAssignableFrom(comp.getClass())) {
            return false;
        }
        try {
            if ((comp.getClass().getMethod("setText", new Class[] { String.class }) != null)) {
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
