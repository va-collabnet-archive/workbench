/*
 * Created by JFormDesigner on Tue Sep 21 18:34:34 GMT-03:00 2010
 */

package org.dwfa.ace.modeler.tool;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PRECEDENCE;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionManagerBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSet;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSetBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * @author Guillermo Reynoso
 */
public class AskForConceptName extends JPanel {
	
	
	private DefaultListModel parentListModel;
	
	private String conceptName;
	
	private String semanticTag;
	
	public AskForConceptName(I_ConfigAceFrame config, DefaultListModel parents ) {
		initComponents();
		this.parentListModel=parents;
		I_TermFactory termFactory = Terms.get();
		NidSetBI descriptionTypes=new NidSet() ;
		NidSetBI as=new NidSet() ;		
		try {
			descriptionTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));
			as.addAll(config.getAllowedStatus().getSetValues());
		} catch (TerminologyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashSet<String> semtags=new HashSet<String>();
		for (int i=0;i<parents.size();i++){
			String parentName="";
			try {
			
				Collection<? extends I_DescriptionVersioned> descTuples = ((I_GetConceptData)parents.get(i)).getDescriptions();
				if (!descTuples.isEmpty()) {
					for (I_DescriptionVersioned d:descTuples){
						int pos=d.getText().lastIndexOf("(");
						if (pos>0 ){
							if (d.getText().lastIndexOf(")")>pos){
								parentName=d.getText();
								break;
							}
						}
					}
				}
			
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (parentName.lastIndexOf("(")>0){
				String str = parentName.substring(parentName.lastIndexOf("("));
				if (str.lastIndexOf(")")>1){
					String semtag = str.substring(1,str.lastIndexOf(")"));
					semtags.add(semtag);
				}
			}
		}
		DefaultListModel listModel=new DefaultListModel();
		for (String semtag:semtags){
			listModel.addElement(semtag);
		}
		list1.setModel(listModel);
		list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (listModel.getSize()>0){
			list1.setSelectedIndex(0);
			semanticTag=(String) listModel.get(0);
		}
		conceptName="";
	}


	private void list1ValueChanged(ListSelectionEvent e) {
		semanticTag=(String) list1.getSelectedValue();
	}

	private void cptNameKeyPressed(KeyEvent e) {
		conceptName=cptName.getText();
	}

	private void cptNameKeyTyped(KeyEvent e) {
		conceptName=cptName.getText();
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		label1 = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		label2 = new JLabel();
		cptName = new JTextField();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0, 0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 25, 0, 0, 20, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

		//---- label1 ----
		label1.setText("Select an allowable semantic tags");
		add(label1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//======== scrollPane1 ========
		{

			//---- list1 ----
			list1.addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					list1ValueChanged(e);
				}
			});
			scrollPane1.setViewportView(list1);
		}
		add(scrollPane1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- label2 ----
		label2.setText("Write a name for concept");
		add(label2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));

		//---- cptName ----
		cptName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				cptNameKeyPressed(e);
			}
			@Override
			public void keyTyped(KeyEvent e) {
				cptNameKeyTyped(e);
			}
		});
		add(cptName, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 5), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JLabel label1;
	private JScrollPane scrollPane1;
	private JList list1;
	private JLabel label2;
	private JTextField cptName;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
	
	public String getConceptName() {
		return conceptName;
	}

	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	public DefaultListModel getParentListModel() {
		return parentListModel;
	}

	public void setParentListModel(DefaultListModel parentListModel) {
		this.parentListModel = parentListModel;
	}

	public String getSemanticTag() {
		return semanticTag;
	}

	public void setSemanticTag(String semanticTag) {
		this.semanticTag = semanticTag;
	}
}
