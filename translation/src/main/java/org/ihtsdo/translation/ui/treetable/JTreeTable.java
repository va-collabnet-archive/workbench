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
package org.ihtsdo.translation.ui.treetable;
/*

 * %W% %E%
 *
 * Copyright 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer. 
 *   
 * - Redistribution in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution. 
 *   
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.  
 * 
 * This software is provided "AS IS," without a warranty of any
 * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
 * EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY
 * DAMAGES OR LIABILITIES SUFFERED BY LICENSEE AS A RESULT OF OR
 * RELATING TO USE, MODIFICATION OR DISTRIBUTION OF THIS SOFTWARE OR
 * ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE 
 * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT,   
 * SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF 
 * THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS 
 * BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed, licensed or
 * intended for use in the design, construction, operation or
 * maintenance of any nuclear facility.
 */


import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowSorter;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;

/**
 * This example shows how to create a simple JTreeTable component, 
 * by using a JTree as a renderer (and editor) for the cells in a 
 * particular column in the JTable.  
 *
 * @version %I% %G%
 *
 * @author Philip Milne
 * @author Scott Violet
 */

public class JTreeTable extends JTable {
	
	/** The tree. */
	protected TreeTableCellRenderer tree;
	
	/** The row sorter. */
	private TableRowSorter rowSorter;

	/**
	 * Instantiates a new j tree table.
	 */
	public JTreeTable(){

//		this.setAutoCreateRowSorter(true);
	}

	/**
	 * Instantiates a new j tree table.
	 *
	 * @param treeTableModel the tree table model
	 */
	public JTreeTable(TreeTableModel treeTableModel) {
		super();
		setTreeTableModel( treeTableModel);
		this.setAutoCreateRowSorter(true);
		RowSorter rs=this.getRowSorter();
//		rs.addRowSorterListener(new TreeTableRowSorterListener());
	}
//
//	class TreeTableRowSorterListener implements RowSorterListener{
//
//		@Override
//		public void sorterChanged(RowSorterEvent e) {
//			System.out.println("event: " e.);
//			
//		}
//		
//	}
	/**
 * Sets the tree table model.
 *
 * @param treeTableModel the new tree table model
 */
public void setTreeTableModel(TreeTableModel treeTableModel){
		// Create the tree. It will be used as a renderer and editor. 
		tree = new TreeTableCellRenderer(treeTableModel); 

		// Install a tableModel representing the visible rows in the tree. 
		TreeTableModelAdapter ttModelAdapter=new TreeTableModelAdapter(treeTableModel, tree);
//		
//		rowSorter=new TreeTableRowSorter(ttModelAdapter);
//		this.setRowSorter(rowSorter);
		super.setModel(ttModelAdapter);

		// Force the JTable and JTree to share their row selection models. 
		tree.setSelectionModel(new DefaultTreeSelectionModel() { 
			// Extend the implementation of the constructor, as if: 
			/* public this() */ {
				setSelectionModel(listSelectionModel); 
			} 
		}); 
		// Make the tree and table row heights the same. 
		tree.setRowHeight(getRowHeight());

		// Install the tree editor renderer and editor. 
		setDefaultRenderer(TreeTableModel.class, tree); 
		setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());  

		setShowGrid(true);
		setIntercellSpacing(new Dimension(2, 0));
	}

	/* Workaround for BasicTableUI anomaly. Make sure the UI never tries to 
	 * paint the editor. The UI currently uses different techniques to 
	 * paint the renderers and editors and overriding setBounds() below 
	 * is not the right thing to do for an editor. Returning -1 for the 
	 * editing row in this case, ensures the editor is never painted. 
	 */
	/* (non-Javadoc)
	 * @see javax.swing.JTable#getEditingRow()
	 */
	public int getEditingRow() {
		return (getColumnClass(editingColumn) == TreeTableModel.class) ? -1 : editingRow;  
		
	}
	
	/**
	 * Sets the selected row.
	 *
	 * @param row the new selected row
	 */
	public void setSelectedRow(int row){
		tree.setSelectionRow(row);
	}
	
	/**
	 * Expand row.
	 *
	 * @param row the row
	 */
	public void expandRow(int row){
		tree.expandRow(row);
	}

	// 
	// The renderer used to display the tree nodes, a JTree.  
	//

	/**
	 * The Class TreeTableCellRenderer.
	 */
	public class TreeTableCellRenderer extends JTree implements TableCellRenderer {

		/** The visible row. */
		protected int visibleRow;

		/**
		 * Instantiates a new tree table cell renderer.
		 *
		 * @param model the model
		 */
		public TreeTableCellRenderer(TreeModel model) { 
			super(model); 
		}

		/* (non-Javadoc)
		 * @see java.awt.Component#setBounds(int, int, int, int)
		 */
		public void setBounds(int x, int y, int w, int h) {
			super.setBounds(x, 0, w, JTreeTable.this.getHeight());
		}

		/* (non-Javadoc)
		 * @see javax.swing.JComponent#paint(java.awt.Graphics)
		 */
		public void paint(Graphics g) {
			g.translate(0, -visibleRow * getRowHeight());
			super.paint(g);
		}

		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value,
				boolean isSelected,
				boolean hasFocus,
				int row, int column) {
			if(isSelected)
				setBackground(table.getSelectionBackground());
			else
				setBackground(table.getBackground());

			visibleRow = row;
			return this;
		}
	}

	// 
	// The editor used to interact with tree nodes, a JTree.  
	//

	/**
	 * The Class TreeTableCellEditor.
	 */
	public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {
		
		/* (non-Javadoc)
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
		 */
		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int r, int c) {
			return tree;
		}
	}

}

