package org.dwfa.ace.modeler.tool;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.dnd.ConceptTransferable;
import org.dwfa.tapi.TerminologyException;

public class ObjectTransferHandler extends TransferHandler {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Object TARGET_LIST_NAME = null;
	private int[] indices = null;
	private int addIndex = -1; //Location where items were added
	private int addCount = 0; //Number of items added.
	private I_ConfigAceFrame config;

    private DataFlavor conceptBeanFlavor;
	private I_GetItemForModel getItem;
	public ObjectTransferHandler(I_ConfigAceFrame config, I_GetItemForModel getItem) {
		this.config=config;
		this.getItem=getItem;
		conceptBeanFlavor=null;
        try {
			conceptBeanFlavor = new DataFlavor(ConceptTransferable.conceptBeanType);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
	}
	public boolean importData(JComponent c, Transferable t) {
		if (canImport(c, t.getTransferDataFlavors())) {
			try {    			    	
			
					
				if (hasConceptBeanFlavor(t.getTransferDataFlavors(), conceptBeanFlavor)) {
					if (c instanceof JTextField ){

						I_GetConceptData concept=(I_GetConceptData)t.getTransferData(conceptBeanFlavor);
						if (getItem==null){
							((JTextField)c).setText(concept.toString());
						}
						else{
							try {
								getItem.getItemFromConcept(concept);
							} catch (Exception e) {
								System.out.println(e.getMessage());
								e.printStackTrace();
								return false;
							}
						}
					}else if (c instanceof JLabel ){

							I_GetConceptData concept=(I_GetConceptData)t.getTransferData(conceptBeanFlavor);
							if (getItem==null){
								((JLabel)c).setText(concept.toString());
							}
							else{
								try {
									getItem.getItemFromConcept(concept);
								} catch (Exception e) {
									System.out.println(e.getMessage());
									e.printStackTrace();
									return false;
								}
							}
					}else{
						JList target = (JList)c;
						DefaultListModel listModel = (DefaultListModel)target.getModel();
						int index = target.getSelectedIndex();
						//Prevent the user from dropping data back on itself.
						//For example, if the user is moving items #4,#5,#6 and #7 and
						//attempts to insert the items after item #5, this would
						//be problematic when removing the original items.
						//So this is not allowed.
						if (indices != null && index >= (indices[0] - 1 )&& index <= indices[indices.length - 1]) {
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
							I_GetConceptData concept=(I_GetConceptData)t.getTransferData(conceptBeanFlavor);
							if (this.config==null)
								this.config=Terms.get().getActiveAceFrameConfig();
					//if (TerminologyProjectDAO.validateConceptAsRefset( concept,this.config)){
								Object obj=null;
								if (getItem!=null && concept!=null){
									try {
										for (int i=0;i<listModel.getSize();i++){
											String objString=listModel.get(i).toString();
											if (objString.equals(concept.toString())){
												return false;
											}
										}
										obj=getItem.getItemFromConcept(concept);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}else{
									obj=concept;
								}
								addIndex = index;
								addCount = 1;
	
								if (obj!=null){
									if (c.getName()!=null && c.getName().equals(TARGET_LIST_NAME)){
										listModel.removeAllElements();
										listModel.add(0, obj);
									}else{
										for (int i=0;i<listModel.getSize();i++){
											String objString=listModel.get(i).toString();
											if (objString.equals(obj.toString())){
												return false;
											}
										}
										listModel.add(index++, obj);
									}
								}
							//}
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
		JList list = (JList)c;
		indices =  list.getSelectedIndices();
		Object[] values = list.getSelectedValues();
		I_GetConceptData concept=(I_GetConceptData)values[0];
		
		return new ConceptTransferable(concept);
	}
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}
	protected void exportDone(JComponent c, Transferable data, int action) {
		if (action == MOVE) {
			if (indices != null) {
				JList source = (JList)c;
				DefaultListModel model = (DefaultListModel)source.getModel();
				//If we are moving items around in the same list, we
				//need to adjust the indices accordingly, since those
				//after the insertion point have moved.
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
	public boolean canImport(JComponent c, DataFlavor[] flavors ){
		if (c.isEnabled()) { 
			if (hasConceptBeanFlavor(flavors,conceptBeanFlavor )) {
				return true;
			}
		}
    	return false;
	}
	private boolean hasConceptBeanFlavor(DataFlavor[] flavors,DataFlavor conceptBeanFlavor) {
  		for (int i = 0; i < flavors.length; i++) {
			if (conceptBeanFlavor.equals(flavors[i])) {
				return true;
			}
		}
  			
		return false;
	}
}